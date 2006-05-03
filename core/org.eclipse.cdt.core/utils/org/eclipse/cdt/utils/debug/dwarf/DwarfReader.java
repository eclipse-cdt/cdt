/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.debug.dwarf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.utils.debug.IDebugEntryRequestor;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.core.runtime.Path;

/**
 * Light-weight parser of Dwarf2 data which is intended for getting only 
 * source files that contribute to the given executable.
 */
public class DwarfReader extends Dwarf implements ISymbolReader {

	// These are sections that must be parsed to get the source file list.
	final static String[] DWARF_SectionsToParse =
		{
			DWARF_DEBUG_INFO,
			DWARF_DEBUG_LINE,
			DWARF_DEBUG_ABBREV,
			DWARF_DEBUG_STR
		};

	ArrayList		 	fileList;
	String[] 			files = null;
	boolean 			m_parsed = false;
	private int 		m_leb128Size = 0;
		
	public DwarfReader(String file) throws IOException {
		super(file);
	}

	public DwarfReader(Elf exe) throws IOException {
		super(exe);
	}

	// Override parent.
	// 
	public void init(Elf exe) throws IOException {
		Elf.ELFhdr header = exe.getELFhdr();
		isLE = header.e_ident[Elf.ELFhdr.EI_DATA] == Elf.ELFhdr.ELFDATA2LSB;

		Elf.Section[] sections = exe.getSections();
		
		// Read in sections (and only the sections) we care about.
		//
		for (int i = 0; i < sections.length; i++) {
			String name = sections[i].toString();
			for (int j = 0; j < DWARF_SectionsToParse.length; j++) {
				if (name.equals(DWARF_SectionsToParse[j])) {
					dwarfSections.put(DWARF_SectionsToParse[j], sections[i].loadSectionData());
				}
			}
		}
		
		if (dwarfSections.size() < DWARF_SectionsToParse.length)
			throw new IOException("No enough Dwarf data.");

		// Don't print during parsing.
		printEnabled = false;

		m_parsed = false;
		fileList = new ArrayList();
	}

	/*
	 * Parse line table data of a compilation unit to get names of all source files
	 * that contribute to the compilation unit. 
	 */
	void parseSourceInCULineInfo(
			String cuCompDir,	// compilation directory of the CU 
			int cuStmtList) 	// offste of the CU in the line table section 
	{
		byte[] data = (byte[]) dwarfSections.get(DWARF_DEBUG_LINE);
		if (data != null) {
			try {
				int offset = cuStmtList;
				
				/* Read line table header:
				 * 
				 *  total_length:				4 bytes
				 *  version:					2
				 *  prologue length:			4
				 *  minimum_instruction_len:	1
				 *  default_is_stmt:			1
				 *  line_base:					1
				 *  line_range:					1
				 *  opcode_base:				1
				 *  standard_opcode_lengths:	(value of opcode_base)
				 */
				// Skip the following till "opcode_base"
				offset = offset + 14;
				int opcode_base = data[offset++];
				offset += opcode_base - 1;

				// Read in directories.
				//
				ArrayList	dirList = new ArrayList();

				// Put the compilation directory of the CU as the first dir
				dirList.add(cuCompDir);
				
				String 			str, fileName;
				
				while (true) {
					str = readString(data, offset);
					if (str.length() == 0)
						break;
					dirList.add(str);
					offset += str.length()+1;
				}
				offset++;
				
				// Read file names
				//
				long	leb128;
				while (true) {
					fileName = readString(data, offset);
					if (fileName.length() == 0)	// no more file entry
						break;
					offset += fileName.length()+1;
					
					// dir index
					leb128 = read_unsigned_leb128(data, offset);
					offset += m_leb128Size;
					
					addSourceFile((String)dirList.get((int)leb128), fileName);
					
					// Skip the followings
					//
					// modification time
					leb128 = read_unsigned_leb128(data, offset);
					offset += m_leb128Size;						

					// file size in bytes
					leb128 = read_unsigned_leb128(data, offset);
					offset += m_leb128Size;						
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String[] getSourceFiles() {
		if (!m_parsed) {
			parse(null);
			m_parsed = true;

			files = new String[fileList.size()];
			fileList.toArray(files);
		}

		return files;
	}

	private void addSourceFile(String dir, String name)
	{
		if (name == null)
			return;
		if (name.charAt(0) == '<')	//  don't count the entry "<internal>" from GCCE compiler
			return;
		
		String fullName = name;
		
		Path pa = new Path(name);
		if (! pa.isAbsolute() && dir.length() > 0)
			fullName = dir + File.separatorChar + name;

		// This convert the path to canonical path (but not necessarily absolute, which
		// is different from java.io.File.getCanonicalPath()).
		pa = new Path(fullName);
		fullName = pa.toOSString();
		
		if (!fileList.contains(fullName))
			fileList.add(fullName);					
	}
	
	/**
	 * Read a null-ended string from the given "data" stream starting at the given "offset".
	 * data	:  IN, byte stream
	 * offset: IN, offset in the stream
	 */
	String readString(byte[] data, int offset)
	{
		String str;
		
		StringBuffer sb = new StringBuffer();
		for (; offset < data.length; offset++) {
			byte c = data[offset];
			if (c == 0) {
				break;
			}
			sb.append((char) c);
		}

		str = sb.toString();
		return str;
	}
	
	long read_unsigned_leb128(byte[] data, int offset) throws IOException {
		/* unsigned */
		long result = 0;
		int shift = 0;
		short b;

		m_leb128Size = 0;
		while (true) {
			b = (short) data[offset];
			if (b == -1)
				break; //throw new IOException("no more data");
			m_leb128Size++;
			result |= ((long) (b & 0x7f) << shift);
			if ((b & 0x80) == 0) {
				break;
			}
			shift += 7;
		}
		
		return result;
	}

	// Override parent: only handle TAG_Compile_Unit.
	void processDebugInfoEntry(IDebugEntryRequestor requestor, AbbreviationEntry entry, List list) {
		int len = list.size();
		int tag = (int) entry.tag;
		for (int i = 0; i < len; i++) {
			switch (tag) {
				case DwarfConstants.DW_TAG_compile_unit :
					processCompileUnit(requestor, list);
					break;
				default:
					break;
			}
		}
	}

	// Override parent.
	// Just get the file name of the CU.
	// Argument "requestor" is ignored.
	void processCompileUnit(IDebugEntryRequestor requestor, List list) {
		
		String cuName, cuCompDir;
		int		stmtList = -1;
		
		cuName = cuCompDir = "";
		
		for (int i = 0; i < list.size(); i++) {
			AttributeValue av = (AttributeValue)list.get(i);
			try {
				int name = (int)av.attribute.name;
				switch(name) {
					case DwarfConstants.DW_AT_name:
						cuName = (String)av.value;
						break;
					case DwarfConstants.DW_AT_comp_dir:
						cuCompDir = (String)av.value;
						break;
					case DwarfConstants.DW_AT_stmt_list:
						stmtList = ((Number)av.value).intValue();
						break;
					default:
						break;
				}
			} catch (ClassCastException e) {
			}
		}

		addSourceFile(cuCompDir, cuName);
		if (stmtList > -1)	// this CU has "stmt_list" attribute
			parseSourceInCULineInfo(cuCompDir, stmtList);
	}
}
