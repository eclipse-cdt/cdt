/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *     Ling Wang (Nokia) bug 201000
 *******************************************************************************/

package org.eclipse.cdt.utils.debug.dwarf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.utils.debug.IDebugEntryRequestor;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Light-weight parser of Dwarf2 data which is intended for getting only 
 * source files that contribute to the given executable.
 */
public class DwarfReader extends Dwarf implements ISymbolReader {

	// These are sections that need be parsed to get the source file list.
	final static String[] DWARF_SectionsToParse =
		{
			DWARF_DEBUG_INFO,
			DWARF_DEBUG_LINE,
			DWARF_DEBUG_ABBREV,
			DWARF_DEBUG_STR		// this is optional. Some compilers don't generate it.
		};

	private Collection	m_fileCollection = new ArrayList();
	private String[] 	m_fileNames = null;
	private String		m_exeFileWin32Drive; // Win32 drive of the exe file.
	private boolean		m_onWindows;
	private boolean		m_parsed = false;
	private int 		m_leb128Size = 0;
	private ArrayList	m_parsedLineTableOffsets = new ArrayList();
	private int			m_parsedLineTableSize = 0;
		
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
		
		// Don't print during parsing.
		printEnabled = false;

		m_parsed = false;
		
		Path pa = new Path(exe.getFilename());
		m_exeFileWin32Drive = pa.getDevice(); 
		
		m_onWindows = (File.separatorChar == '\\');
	}

	/*
	 * Parse line table data of a compilation unit to get names of all source files
	 * that contribute to the compilation unit. 
	 */
	void parseSourceInCULineInfo(
			String cuCompDir,	// compilation directory of the CU 
			int cuStmtList) 	// offset of the CU line table in .debug_line section 
	{
		byte[] data = (byte[]) dwarfSections.get(DWARF_DEBUG_LINE);
		if (data != null) {
			try {
				int offset = cuStmtList;
				
				/* Read line table header:
				 * 
				 *  total_length:				4 bytes (excluding itself)
				 *  version:					2
				 *  prologue length:			4
				 *  minimum_instruction_len:	1
				 *  default_is_stmt:			1
				 *  line_base:					1
				 *  line_range:					1
				 *  opcode_base:				1
				 *  standard_opcode_lengths:	(value of opcode_base)
				 */
				
				// Remember the CU line tables we've parsed.
				Integer cuOffset = new Integer(cuStmtList);
				if (! m_parsedLineTableOffsets.contains(cuOffset)) {
					m_parsedLineTableOffsets.add(cuOffset);

					int length = read_4_bytes(data, offset) + 4;
					m_parsedLineTableSize += length + 4;
				}
				else {
					// Compiler like ARM RVCT may produce several CUs for the
					// same source files.
					return;
				}
					
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

	/*
	 * Check if there are any line tables in .debug_line section that are
	 * not referenced by any TAG_compile_units. If yes, add source files
	 * in those table entries to our "m_fileCollection".
	 * If the compiler/linker is fully dwarf standard compliant, that should 
	 * not happen. But that case does exist, hence this workaround. 
	 * .................. LWang. 08/24/07
	 */
	private void getSourceFilesFromDebugLineSection()
	{
		byte[] data = (byte[]) dwarfSections.get(DWARF_DEBUG_LINE);
		if (data == null) 
			return;
		
		int sectionSize = data.length;
		int minHeaderSize = 16;

		// Check if there is data in .debug_line section that is not parsed
		// yet by parseSourceInCULineInfo().
		if (m_parsedLineTableSize >= sectionSize - minHeaderSize)
			return;
		
		// The .debug_line section contains a list of line tables
		// for compile_units. We'll iterate through all line tables
		// in the section.
		/*
		 * Line table header for one compile_unit:
		 * 
		 * total_length: 			4 bytes (excluding itself) 
		 * version: 				2 
		 * prologue length: 		4
		 * minimum_instruction_len: 1 
		 * default_is_stmt: 		1 
		 * line_base: 				1
		 * line_range: 				1
		 * opcode_base: 			1 
		 * standard_opcode_lengths: (value of opcode_base)
		 */

		int lineTableStart = 0;	// offset in the .debug_line section
		
		try {
			while (lineTableStart < sectionSize - minHeaderSize) {
				int offset = lineTableStart;

				Integer currLineTableStart = new Integer(lineTableStart);
				
				// Read length of the line table for one compile unit
				// Note the length does not including the "length" field itself.
				int tableLength = read_4_bytes(data, offset);
				
				// Record start of next CU line table
				lineTableStart += tableLength + 4;

				// According to Dwarf standard, the "tableLength" should cover the
				// the whole CU line table. But some compilers (e.g. ARM RVCT 2.2)
				// produce extra padding (1 to 3 bytes) beyond that in order for 
				// "lineTableStart" to be aligned at multiple of 4. The padding
				// bytes are beyond the "tableLength" and not indicated by 
				// any flag, which I believe is not Dwarf2 standard compliant.
				// How to determine if that type of padding exists ? 
				// I don't have a 100% safe way. But following hacking seems
				// good enough in practice.........08/26/07
				if (lineTableStart < sectionSize - minHeaderSize && 
						(lineTableStart & 0x3) != 0) 
				{
					int ltLength = read_4_bytes(data, lineTableStart);
					int dwarfVer = read_2_bytes(data, lineTableStart+4);
					int minInstLengh = data[lineTableStart+4+2+4];
					
					boolean dataValid = 
						ltLength > minHeaderSize && 
						ltLength < 16*64*1024 &&   // One source file has that much line data ? 
						dwarfVer > 0 &&	dwarfVer < 4 &&  // ver 3 is still draft at present.
						minInstLengh > 0 && minInstLengh <= 8;
						
					if (! dataValid)	// padding exists !
						lineTableStart = (lineTableStart+3) & ~0x3;
				}
				
				if (m_parsedLineTableOffsets.contains(currLineTableStart))
					// current line table has already been parsed, skip it.
					continue;

				// Skip following fields till "opcode_base"
				offset = offset + 14;
				int opcode_base = data[offset++];
				offset += opcode_base - 1;

				// Read in directories.
				//
				ArrayList dirList = new ArrayList();

				String str, fileName;

				// first dir should be TAG_comp_dir from CU, which we don't have here.
				dirList.add("");
				
				while (true) {
					str = readString(data, offset);
					if (str.length() == 0)
						break;
					dirList.add(str);
					offset += str.length() + 1;
				}
				offset++;

				// Read file names
				//
				long leb128;
				while (true) {
					fileName = readString(data, offset);
					if (fileName.length() == 0) // no more file entry
						break;
					offset += fileName.length() + 1;

					// dir index. Note "0" is reserved for compilation directory. 
					leb128 = read_unsigned_leb128(data, offset);
					offset += m_leb128Size;

					addSourceFile((String) dirList.get((int) leb128), fileName);

					// Skip the followings
					//
					// modification time
					leb128 = read_unsigned_leb128(data, offset);
					offset += m_leb128Size;

					// file size in bytes
					leb128 = read_unsigned_leb128(data, offset);
					offset += m_leb128Size;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public String[] getSourceFiles() {
		if (!m_parsed) {
			m_fileCollection.clear();

			getSourceFilesFromDebugInfoSection();
			
			getSourceFilesFromDebugLineSection();
			
			m_parsed = true;

			m_fileNames = new String[m_fileCollection.size()];
			m_fileCollection.toArray(m_fileNames);
		}

		return m_fileNames;
	}

	/*
	 * Get source file names from compile units (CU) in .debug_info section,
	 * which will also search line table for the CU in .debug_line section.
	 * 
	 * The file names are stored in member "m_fileCollection".
	 */
	private void getSourceFilesFromDebugInfoSection() {
		// This will parse the data in .debug_info section which
		// will call this->processCompileUnit() to get source files.
		parse(null);
	}

	private void addSourceFile(String dir, String name)
	{
		if (name == null || name.length() == 0)
			return;
		
		if (name.charAt(0) == '<')	//  don't count the entry "<internal>" from GCCE compiler
			return;
		
		String fullName = name;
		
		IPath dirPa = new Path(dir);
		IPath pa = new Path(name);
		
		// Combine dir & name if needed.
		if (!pa.isAbsolute() && dir.length() > 0)
			pa = dirPa.append(pa);
		
		// For win32 only.
		// On Windows, there are cases where the source file itself has the full path
		// except the drive letter.
		if (m_onWindows && pa.isAbsolute() && pa.getDevice() == null) {
			// Try to get drive letter from comp_dir.
			if (dirPa.getDevice() != null)
				pa = pa.setDevice(dirPa.getDevice());
			else if (m_exeFileWin32Drive != null)
				// No drive from Dwarf data, which is also possible with RVCT or GCCE 
				// compilers for ARM. A practically good solution is to assume
				// drive of the exe file as the drive. Though it's not good in theory, 
				// it does not hurt when the assumption is wrong, as user still has the
				// option to locate the file manually...03/15/07
				pa = pa.setDevice(m_exeFileWin32Drive);
		}
	
		// This convert the path to canonical path (but not necessarily absolute, which
		// is different from java.io.File.getCanonicalPath()).
		fullName = pa.toOSString();
		
		if (!m_fileCollection.contains(fullName))
			m_fileCollection.add(fullName);					
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
	
	// Note this method modifies a data member
	//
	long read_unsigned_leb128(byte[] data, int offset) throws IOException {
		/* unsigned */
		long result = 0;
		int shift = 0;
		short b;

		m_leb128Size = 0;
		while (true) {
			b = (short) data[offset++];
			if (data.length == offset)
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
		int tag = (int) entry.tag;
		switch (tag) {
			case DwarfConstants.DW_TAG_compile_unit :
				processCompileUnit(requestor, list);
				break;
			default:
				break;
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
