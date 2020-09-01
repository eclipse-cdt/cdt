/*******************************************************************************
 * Copyright (c) 2007, 2019 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *     Ling Wang (Nokia) bug 201000
 *     Serge Beauchamp (Freescale Semiconductor) - Bug 421070
 *     Red Hat Inc. - add debuginfo and macro section support
 *******************************************************************************/

package org.eclipse.cdt.utils.debug.dwarf;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICompileOptionsFinder;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.utils.coff.Coff.SectionHeader;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.coff.PE64;
import org.eclipse.cdt.utils.debug.IDebugEntryRequestor;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.Section;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Light-weight parser of Dwarf2 data which is intended for getting only
 * source files that contribute to the given executable.
 */
public class DwarfReader extends Dwarf implements ISymbolReader, ICompileOptionsFinder {

	// These are sections that need be parsed to get the source file list.
	final static String[] DWARF_SectionsToParse = { DWARF_DEBUG_INFO, DWARF_DEBUG_LINE, DWARF_DEBUG_ABBREV,
			DWARF_DEBUG_STR, // this is optional. Some compilers don't generate it.
			DWARF_DEBUG_MACRO, };

	final static String[] DWARF_ALT_SectionsToParse = { DWARF_DEBUG_STR, DWARF_DEBUG_MACRO };

	private final Collection<String> m_fileCollection = new HashSet<>();
	private final Map<Long, String> m_stmtFileMap = new HashMap<>();
	private final Map<String, ArrayList<String>> m_compileOptionsMap = new HashMap<>();
	private String[] m_fileNames = null;
	private boolean m_parsed = false;
	private boolean m_macros_parsed = false;
	private final ArrayList<Integer> m_parsedLineTableOffsets = new ArrayList<>();
	private long m_parsedLineTableSize = 0;

	public DwarfReader(String file) throws IOException {
		super(file);
	}

	public DwarfReader(Elf exe) throws IOException {
		super(exe);
	}

	/**
	 * @since 5.1
	 */
	public DwarfReader(PE exe) throws IOException {
		super(exe);
	}

	/**
	 * @since 6.9
	 */
	public DwarfReader(PE64 exe) throws IOException {
		super(exe);
	}

	// Override parent.
	//
	@Override
	public void init(Elf exe) throws IOException {
		Elf.ELFhdr header = exe.getELFhdr();
		isLE = header.e_ident[Elf.ELFhdr.EI_DATA] == Elf.ELFhdr.ELFDATA2LSB;

		IPath debugInfoPath = new Path(exe.getFilename());
		Elf.Section[] sections = exe.getSections();

		boolean have_build_id = false;

		// Look for a special GNU build-id note which means the debug data resides in a separate
		// file with a name based on the build-id.
		for (Section section : sections) {
			if (section.sh_type == Elf.Section.SHT_NOTE) {
				ByteBuffer data = section.mapSectionData();
				if (data.remaining() > 12) {
					try {
						// Read .note section, looking to see if it is named "GNU" and is of GNU_BUILD_ID type
						@SuppressWarnings("unused")
						int name_sz = read_4_bytes(data);
						int data_sz = read_4_bytes(data);
						int note_type = read_4_bytes(data);

						String noteName = readString(data);
						String buildId = null;
						if (noteName.equals("GNU") && note_type == Elf.Section.NT_GNU_BUILD_ID) { //$NON-NLS-1$
							// We have the special GNU build-id note section.  Skip over the name to
							// a 4-byte boundary.
							byte[] byteArray = new byte[data_sz];
							while ((data.position() & 0x3) != 0)
								data.get();
							int i = 0;
							// Read in the hex bytes from the note section's data.
							while (data.hasRemaining() && data_sz-- > 0) {
								byteArray[i++] = data.get();
							}
							// The build-id location is taken by converting the binary bytes to hex string.
							// The first byte is used as a directory specifier (e.g. 51/a4578fe2).
							String bName = DatatypeConverter.printHexBinary(byteArray).toLowerCase();
							buildId = bName.substring(0, 2) + "/" + bName.substring(2) + ".debug"; //$NON-NLS-1$ //$NON-NLS-2$
							// The build-id file should be in the special directory /usr/lib/debug/.build-id
							IPath buildIdPath = new Path("/usr/lib/debug/.build-id").append(buildId); //$NON-NLS-1$
							File buildIdFile = buildIdPath.toFile();
							if (buildIdFile.exists()) {
								// if the debug file exists from above, open it and get the section info from it
								try (Elf debugInfo = new Elf(buildIdFile.getCanonicalPath())) {
									sections = debugInfo.getSections();
								}
								have_build_id = true;
								debugInfoPath = new Path(buildIdFile.getCanonicalPath()).removeLastSegments(1);
								break;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						CCorePlugin.log(e);
					}
				}
			}
		}

		if (!have_build_id) {
			// No build-id.  Look for a .gnu_debuglink section which will have the name of the debug info file
			Elf.Section gnuDebugLink = exe.getSectionByName(DWARF_GNU_DEBUGLINK);
			if (gnuDebugLink != null) {
				ByteBuffer data = gnuDebugLink.mapSectionData();
				if (data != null) { // we have non-empty debug info link
					try {
						// name is zero-byte terminated character string
						String debugName = ""; //$NON-NLS-1$
						if (data.hasRemaining()) {
							int c;
							StringBuilder sb = new StringBuilder();
							while ((c = data.get()) != -1) {
								if (c == 0) {
									break;
								}
								sb.append((char) c);
							}
							debugName = sb.toString();
						}
						if (debugName.length() > 0) {
							// try and open the debug info from 3 separate places in order
							File debugFile = null;
							IPath exePath = new Path(exe.getFilename());
							IPath p = exePath.removeLastSegments(1);
							// 1. try and open the file in the same directory as the executable
							debugFile = p.append(debugName).toFile();
							if (!debugFile.exists()) {
								// 2. try and open the file in the .debug directory where the executable is
								debugFile = p.append(".debug").append(debugName).toFile(); //$NON-NLS-1$
								if (!debugFile.exists())
									// 3. try and open /usr/lib/debug/$(EXE_DIR)/$(DEBUGINFO_NAME)
									debugFile = new Path("/usr/lib/debug").append(p).append(debugName).toFile(); //$NON-NLS-1$
							}
							if (debugFile.exists()) {
								// if the debug file exists from above, open it and get the section info from it
								try (Elf debugInfo = new Elf(debugFile.getCanonicalPath())) {
									sections = debugInfo.getSections();
								}
								debugInfoPath = new Path(debugFile.getCanonicalPath()).removeLastSegments(1);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						CCorePlugin.log(e);
					}
				}

			}
		}

		// Read in sections (and only the sections) we care about.
		//
		for (Section section : sections) {
			String name = section.toString();
			if (name.equals(DWARF_GNU_DEBUGALTLINK)) {
				ByteBuffer data = section.mapSectionData();
				try {
					// name is zero-byte terminated character string
					String altInfoName = readString(data);
					if (altInfoName.length() > 0) {
						IPath altPath = new Path(altInfoName);
						if (!altPath.isAbsolute()) {
							altPath = debugInfoPath.append(altPath);
						}
						File altFile = altPath.toFile();
						if (altFile.exists()) {
							try (Elf altInfo = new Elf(altFile.getCanonicalPath())) {
								Elf.Section[] altSections = altInfo.getSections();
								for (Section altSection : altSections) {
									String altName = altSection.toString();
									for (String element : DWARF_ALT_SectionsToParse) {
										if (altName.equals(element)) {
											try {
												dwarfAltSections.put(element, altSection.mapSectionData());
											} catch (Exception e) {
												CCorePlugin.log(e);
											}
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					CCorePlugin.log(e);
				}
			} else {
				for (String element : DWARF_SectionsToParse) {
					if (name.equals(element)) {
						// catch out of memory exceptions which might happen trying to
						// load large sections (like .debug_info).  not a fix for that
						// problem itself, but will at least continue to load the other
						// sections.
						try {
							dwarfSections.put(element, section.mapSectionData());
						} catch (Exception e) {
							CCorePlugin.log(e);
						}
					}
				}
			}
		}

		// Don't print during parsing.
		printEnabled = false;
		m_parsed = false;
	}

	@Override
	public void init(PE exe) throws IOException {

		isLE = true;
		SectionHeader[] sections = exe.getSectionHeaders();

		for (int i = 0; i < sections.length; i++) {
			String name = new String(sections[i].s_name).trim();
			if (name.startsWith("/")) //$NON-NLS-1$
			{
				int stringTableOffset = Integer.parseInt(name.substring(1));
				name = exe.getStringTableEntry(stringTableOffset);
			}
			for (String element : Dwarf.DWARF_SCNNAMES) {
				if (name.equals(element)) {
					try {
						dwarfSections.put(element, sections[i].mapSectionData());
					} catch (Exception e) {
						CCorePlugin.log(e);
					}
				}
			}
		}
		// Don't print during parsing.
		printEnabled = false;
		m_parsed = false;
	}

	/*
	 * Parse line table data of a compilation unit to get names of all source files
	 * that contribute to the compilation unit.
	 */
	void parseSourceInCULineInfo(String cuCompDir, // compilation directory of the CU
			int cuStmtList) // offset of the CU line table in .debug_line section
	{
		ByteBuffer data = dwarfSections.get(DWARF_DEBUG_LINE);
		if (data != null) {
			try {
				data.position(cuStmtList);

				/* Read line table header:
				 *
				 *  total_length:				4/12 bytes (excluding itself)
				 *  version:					2
				 *  prologue length:			4/8 bytes (depending on section version)
				 *  minimum_instruction_len:	1
				 *  maximum_operations_per_instruction 1 - it is defined for version >= 4
				 *  default_is_stmt:			1
				 *  line_base:					1
				 *  line_range:					1
				 *  opcode_base:				1
				 *  standard_opcode_lengths:	(value of opcode_base)
				 */

				// Remember the CU line tables we've parsed.
				Integer cuOffset = Integer.valueOf(cuStmtList);

				boolean dwarf64Bit = false;
				if (!m_parsedLineTableOffsets.contains(cuOffset)) {
					m_parsedLineTableOffsets.add(cuOffset);

					// Note the length does not including the "length" field(s) itself.
					InitialLengthValue length = readInitialLengthField(data);
					dwarf64Bit = length.offsetSize == 8;
					m_parsedLineTableSize += length.length + (dwarf64Bit ? 12 : 4);
				} else {
					// Compiler like ARM RVCT may produce several CUs for the
					// same source files.
					return;
				}

				short version = read_2_bytes(data);
				// Skip the following till "opcode_base"
				short skip_bytes = 8;
				if (version >= 4)
					skip_bytes += 1; // see maximum_operations_per_instruction
				if (dwarf64Bit)
					skip_bytes += 4; // see prologue length for 64-bit DWARF format
				data.position(data.position() + skip_bytes);
				int opcode_base = data.get();
				data.position(data.position() + opcode_base - 1);

				// Read in directories.
				//
				ArrayList<String> dirList = new ArrayList<>();

				// Put the compilation directory of the CU as the first dir
				dirList.add(cuCompDir);

				String str, fileName;

				while (true) {
					str = readString(data);
					if (str.length() == 0)
						break;
					// If the directory is relative, append it to the CU dir
					IPath dir = new Path(str);
					if (!dir.isAbsolute())
						dir = new Path(cuCompDir).append(str);
					dirList.add(dir.toString());
				}

				// Read file names
				//
				long leb128;
				while (true) {
					fileName = readString(data);
					if (fileName.length() == 0) // no more file entry
						break;

					// dir index
					leb128 = read_unsigned_leb128(data);

					addSourceFile(dirList.get((int) leb128), fileName);

					// Skip the followings
					//
					// modification time
					leb128 = read_unsigned_leb128(data);

					// file size in bytes
					leb128 = read_unsigned_leb128(data);
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
	private void getSourceFilesFromDebugLineSection() {
		ByteBuffer data = dwarfSections.get(DWARF_DEBUG_LINE);
		if (data == null)
			return;

		int sectionSize = data.capacity();
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
		 * total_length: 			4/12 bytes (excluding itself)
		 * version:					2
		 * prologue length:			4/8 bytes (depending on section version)
		 * minimum_instruction_len: 1
		 * maximum_operations_per_instruction 1 - it is defined for version >= 4
		 * default_is_stmt:			1
		 * line_base:				1
		 * line_range:				1
		 * opcode_base:				1
		 * standard_opcode_lengths: (value of opcode_base)		 */

		int lineTableStart = 0; // offset in the .debug_line section

		try {
			while (lineTableStart < sectionSize - minHeaderSize) {
				data.position(lineTableStart);

				Integer currLineTableStart = Integer.valueOf(lineTableStart);

				// Read length of the line table for one compile unit
				// Note the length does not including the "length" field(s) itself.
				InitialLengthValue sectionLength = readInitialLengthField(data);

				// Record start of next CU line table
				boolean dwarf64Bit = sectionLength.offsetSize == 8;
				lineTableStart += (int) (sectionLength.length + (dwarf64Bit ? 12 : 4));

				m_parsedLineTableSize += sectionLength.length + (dwarf64Bit ? 12 : 4);

				// According to Dwarf standard, the "tableLength" should cover the
				// the whole CU line table. But some compilers (e.g. ARM RVCT 2.2)
				// produce extra padding (1 to 3 bytes) beyond that in order for
				// "lineTableStart" to be aligned at multiple of 4. The padding
				// bytes are beyond the "tableLength" and not indicated by
				// any flag, which I believe is not Dwarf2 standard compliant.
				// How to determine if that type of padding exists ?
				// I don't have a 100% safe way. But following hacking seems
				// good enough in practice.........08/26/07
				if (lineTableStart < sectionSize - minHeaderSize && (lineTableStart & 0x3) != 0) {
					int savedPosition = data.position();
					data.position(lineTableStart);

					long ltLength = dwarf64Bit ? read_8_bytes(data) : read_4_bytes(data);

					int dwarfVer = read_2_bytes(data);
					int minInstLengh = data.get(data.position() + (dwarf64Bit ? 8 : 4));

					boolean dataValid = ltLength > minHeaderSize && ltLength < 16 * 64 * 1024 && // One source file has that much line data ?
							dwarfVer > 0 && dwarfVer < 5 && // ver 5 is still draft at present.
							minInstLengh > 0 && minInstLengh <= 8;

					if (!dataValid) // padding exists !
						lineTableStart = (lineTableStart + 3) & ~0x3;

					data.position(savedPosition);
				}

				if (m_parsedLineTableOffsets.contains(currLineTableStart))
					// current line table has already been parsed, skip it.
					continue;

				short version = read_2_bytes(data);

				// Skip following fields till "opcode_base"
				short skip_bytes = 8;
				if (version >= 4)
					skip_bytes += 1; // see maximum_operations_per_instruction
				if (dwarf64Bit)
					skip_bytes += 4; // see prologue length for 64-bit DWARF format
				data.position(data.position() + skip_bytes);
				int opcode_base = data.get();
				data.position(data.position() + opcode_base - 1);

				// Read in directories.
				//
				ArrayList<String> dirList = new ArrayList<>();

				String str, fileName;

				// first dir should be TAG_comp_dir from CU, which we don't have here.
				dirList.add(""); //$NON-NLS-1$

				while (true) {
					str = readString(data);
					if (str.length() == 0)
						break;
					dirList.add(str);
				}

				// Read file names
				//
				long leb128;
				while (true) {
					fileName = readString(data);
					if (fileName.length() == 0) // no more file entry
						break;

					// dir index. Note "0" is reserved for compilation directory.
					leb128 = read_unsigned_leb128(data);

					addSourceFile(dirList.get((int) leb128), fileName);

					// Skip the followings
					//
					// modification time
					leb128 = read_unsigned_leb128(data);

					// file size in bytes
					leb128 = read_unsigned_leb128(data);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
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

	private String addSourceFileWithStmt(String dir, String name, int stmt) {
		String fullName = addSourceFile(dir, name);
		m_stmtFileMap.put(Long.valueOf(stmt), fullName);
		return fullName;
	}

	private String addSourceFile(String dir, String name) {
		if (name == null || name.length() == 0)
			return null;

		if (name.charAt(0) == '<') //  don't count the entry "<internal>" from GCCE compiler
			return null;

		String fullName = name;

		IPath dirPa = new Path(dir);
		IPath pa = new Path(name);

		// Combine dir & name if needed.
		if (!pa.isAbsolute() && dir.length() > 0)
			pa = dirPa.append(pa);

		// This convert the path to canonical path (but not necessarily absolute, which
		// is different from java.io.File.getCanonicalPath()).
		fullName = pa.toOSString();

		if (!m_fileCollection.contains(fullName))
			m_fileCollection.add(fullName);

		return fullName;
	}

	// Override parent: only handle TAG_Compile_Unit.
	@Override
	void processDebugInfoEntry(IDebugEntryRequestor requestor, AbbreviationEntry entry,
			List<Dwarf.AttributeValue> list) {
		int tag = (int) entry.tag;
		switch (tag) {
		case DwarfConstants.DW_TAG_compile_unit:
			processCompileUnit(requestor, list);
			break;
		default:
			break;
		}
	}

	// Override parent.
	// Just get the file name of the CU.
	// Argument "requestor" is ignored.
	@Override
	void processCompileUnit(IDebugEntryRequestor requestor, List<AttributeValue> list) {

		String cuName, cuCompDir;
		int stmtList = -1;

		cuName = cuCompDir = ""; //$NON-NLS-1$

		for (int i = 0; i < list.size(); i++) {
			AttributeValue av = list.get(i);
			try {
				int name = (int) av.attribute.name;
				switch (name) {
				case DwarfConstants.DW_AT_name:
					cuName = (String) av.value;
					break;
				case DwarfConstants.DW_AT_comp_dir:
					cuCompDir = (String) av.value;
					break;
				case DwarfConstants.DW_AT_stmt_list:
					stmtList = ((Number) av.value).intValue();
					break;
				default:
					break;
				}
			} catch (ClassCastException e) {
			}
		}

		addSourceFileWithStmt(cuCompDir, cuName, stmtList);
		if (stmtList > -1) // this CU has "stmt_list" attribute
			parseSourceInCULineInfo(cuCompDir, stmtList);
	}

	/**
	 * @since 5.2
	 */
	@Override
	public String[] getSourceFiles(IProgressMonitor monitor) {
		return getSourceFiles();
	}

	private class OpcodeInfo {
		private int numArgs;
		private final boolean offset_size_8;
		ArrayList<Integer> argTypes;

		public OpcodeInfo(boolean offset_size_8) {
			this.offset_size_8 = offset_size_8;
		}

		public void setNumArgs(int numArgs) {
			this.numArgs = numArgs;
		}

		public void addArgType(int argType) {
			argTypes.add(Integer.valueOf(argType));
		}

		public void readPastEntry(ByteBuffer data) {
			for (int i = 0; i < numArgs; ++i) {
				int argType = argTypes.get(i).intValue();
				switch (argType) {
				case DwarfConstants.DW_FORM_flag:
				case DwarfConstants.DW_FORM_data1:
					data.get();
					break;
				case DwarfConstants.DW_FORM_data2:
					data.getShort();
					break;
				case DwarfConstants.DW_FORM_data4:
					data.getInt();
					break;
				case DwarfConstants.DW_FORM_data8:
					data.getLong();
					break;
				case DwarfConstants.DW_FORM_sdata:
				case DwarfConstants.DW_FORM_udata:
					try {
						read_signed_leb128(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case DwarfConstants.DW_FORM_block: {
					try {
						long off = read_signed_leb128(data);
						data.position((int) (data.position() + off));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
					break;
				case DwarfConstants.DW_FORM_block1: {
					int off = data.get();
					data.position(data.position() + off + 1);
				}
					break;
				case DwarfConstants.DW_FORM_block2: {
					int off = data.getShort();
					data.position(data.position() + off + 2);
				}
					break;
				case DwarfConstants.DW_FORM_block4: {
					int off = data.getInt();
					data.position(data.position() + off + 4);
				}
					break;
				case DwarfConstants.DW_FORM_string:
					while (data.get() != 0) {
						// loop until we find 0 byte
					}
					break;
				case DwarfConstants.DW_FORM_strp:
				case DwarfConstants.DW_FORM_GNU_strp_alt:
				case DwarfConstants.DW_FORM_GNU_ref_alt:
				case DwarfConstants.DW_FORM_sec_offset:
					if (offset_size_8)
						data.getLong();
					else
						data.getInt();
					break;
				}
			}
		}
	}

	// Convert a macro to its command line form.
	private String getCommandLineMacro(String macro) {
		String commandLineMacro = "-D" + macro; //$NON-NLS-1$
		commandLineMacro = commandLineMacro.replaceFirst(" ", "="); //$NON-NLS-1$ //$NON-NLS-2$
		return commandLineMacro;
	}

	// Go through regular and alt macro sections and find any macros that were set on the
	// compilation command line (e.g. gcc -Dflagx=1 my.c).  Built-in macros and macros that
	// are set within files (source and include) are ignored since they can and will be
	// discovered by indexing.
	private void getCommandMacrosFromMacroSection() {
		ByteBuffer data = dwarfSections.get(DWARF_DEBUG_MACRO);
		ByteBuffer str = dwarfSections.get(DWARF_DEBUG_STR);
		ByteBuffer altdata = dwarfAltSections.get(DWARF_DEBUG_MACRO);
		ByteBuffer altstr = dwarfAltSections.get(DWARF_DEBUG_STR);
		Set<String> fixupList = new HashSet<>();
		Set<String> fixupAltList = new HashSet<>();
		boolean DEBUG = false;
		if (data == null)
			return;

		HashMap<Long, ArrayList<String>> t_macros = new HashMap<>();
		HashMap<Long, ArrayList<String>> t_alt_macros = new HashMap<>();

		// Parse the macro section, looking for command-line macros meant for compiling files (i.e.
		// not internal macro definitions in headers or C/C++ files.  Keep track of any forward
		// references to fix-up later when we have a complete list of command-line macros.
		parseMacroSection(data, str, altstr, fixupList, fixupAltList, "=FIXUP=", DEBUG, t_macros); //$NON-NLS-1$

		// Check if there is an alternate macro section.  If there is, parse the alternate section, but any references
		// found there should be considered referring to the alternate string and macro sections.
		// All forward reference fix-ups should be put on the alternate fix-up list.
		if (altdata != null) {
			if (DEBUG)
				System.out.println("Processing Alternate Macro Section"); //$NON-NLS-1$
			parseMacroSection(altdata, altstr, altstr, fixupAltList, fixupAltList, "=FIXUPALT=", DEBUG, t_alt_macros); //$NON-NLS-1$
		}

		// Fix up all forward references from transparent includes
		fixupMacros(fixupList, "=FIXUP=", DEBUG, t_macros); //$NON-NLS-1$

		// Fix up all forward references from transparent alt includes
		if (DEBUG)
			System.out.println("Fix up forward references in alternate macro section"); //$NON-NLS-1$
		fixupMacros(fixupAltList, "=FIXUPALT=", DEBUG, t_alt_macros); //$NON-NLS-1$
	}

	// Fix up forward references made by transparent includes now that a complete macro list has been retrieved.
	private void fixupMacros(Set<String> fixupList, String fixupMarker, boolean DEBUG,
			HashMap<Long, ArrayList<String>> t_macros) {
		for (String name : fixupList) {
			ArrayList<String> macros = m_compileOptionsMap.get(name);
			for (int i = 0; i < macros.size(); ++i) {
				String macroLine = macros.get(i);
				if (macroLine.startsWith(fixupMarker)) {
					Long offset = Long.valueOf(macroLine.substring(7));
					if (DEBUG)
						System.out.println("Found fixup needed for offset: " + offset + " for file: " + name); //$NON-NLS-1$ //$NON-NLS-2$
					ArrayList<String> insertMacros = t_macros.get(offset);
					if (DEBUG)
						System.out.println("insert macros are: " + insertMacros.toString()); //$NON-NLS-1$
					macros.remove(i);
					macros.addAll(i, insertMacros);
					i += insertMacros.size();
				}
			}
			m_compileOptionsMap.put(name, macros); // replace updated list
		}
	}

	// Parse a macro section, looking for command-line macros that are used as flags to compile source files.
	// Keep track of any forward references to macros not yet defined in the file.  We will later
	// fix-up these references when we have seen all command-line macros for the file.
	private void parseMacroSection(ByteBuffer data, ByteBuffer str, ByteBuffer altstr, Set<String> fixupList,
			Set<String> fixupAltList, String fixupMarker, boolean DEBUG, HashMap<Long, ArrayList<String>> t_macros) {
		byte op;
		while (data.hasRemaining()) {
			try {
				int original_position = data.position();
				int type = read_2_bytes(data);
				byte flags = data.get();
				boolean offset_size_8;
				long lt_offset = -1;
				String fileName = null;

				HashMap<Integer, OpcodeInfo> opcodeInfos = null;

				if (DEBUG)
					System.out.println("type is " + type); //$NON-NLS-1$

				// bottom bit 0 tells us whether we have 8 byte offsets or 4 byte offsets
				offset_size_8 = (flags & 0x1) == 1;
				if (DEBUG)
					System.out.println("offset size is " + (offset_size_8 ? 8 : 4)); //$NON-NLS-1$
				// bit 1 indicates we have an offset from the start of .debug_line section
				if ((flags & 0x2) != 0) {
					lt_offset = (offset_size_8 ? read_8_bytes(data) : read_4_bytes(data));
					fileName = m_stmtFileMap.get(Long.valueOf(lt_offset));
					if (DEBUG)
						System.out.println("debug line offset is " + lt_offset); //$NON-NLS-1$
				}

				// if bit 2 flag is on, then we have a macro entry table which may
				// have non-standard entry types defined which we need to know when
				// we come across macro entries later
				if ((flags & 0x4) != 0) {
					opcodeInfos = new HashMap<>();
					int num_opcodes = data.get();
					for (int i = 0; i < num_opcodes; ++i) {
						OpcodeInfo info = new OpcodeInfo(offset_size_8);

						int opcode = data.get();
						long numArgs = read_unsigned_leb128(data);
						info.setNumArgs((int) numArgs);
						for (int j = 0; j < numArgs; ++j) {
							int argType = data.get();
							info.addArgType(argType);
						}
						opcodeInfos.put(Integer.valueOf(opcode), info);
					}
				}

				ArrayList<String> macros = new ArrayList<>();

				boolean done = false;

				while (!done) {
					op = data.get();
					switch (op) {

					case DwarfConstants.DW_MACRO_start_file: {
						long filenum;
						long lineno;
						lineno = read_signed_leb128(data);
						filenum = read_signed_leb128(data);
						// All command line macros are defined as being included before start of file
						if (filenum == 1 && lt_offset >= 0) {
							// we have a source file so add all macros defined before it with lineno 0
							m_compileOptionsMap.put(fileName, macros);
							if (DEBUG)
								System.out.println("following macros found for file " + macros.toString()); //$NON-NLS-1$
							macros = new ArrayList<>();
						}
						if (fileName != null)
							if (DEBUG)
								System.out.println(" DW_MACRO_start_file - lineno: " + lineno + " filenum: " //$NON-NLS-1$ //$NON-NLS-2$
										+ filenum + " " + fileName); //$NON-NLS-1$
							else if (DEBUG)
								System.out.println(" DW_MACRO_start_file - lineno: " + lineno + " filenum: " //$NON-NLS-1$ //$NON-NLS-2$
										+ filenum);
					}
						break;
					case DwarfConstants.DW_MACRO_end_file: {
						if (DEBUG)
							System.out.println(" DW_MACRO_end_file"); //$NON-NLS-1$
					}
						break;
					case DwarfConstants.DW_MACRO_define: {
						long lineno;
						String string;
						lineno = read_signed_leb128(data);
						string = readString(data);
						if (lineno == 0)
							macros.add(getCommandLineMacro(string));
						if (DEBUG)
							System.out.println(" DW_MACRO_define - lineno : " + lineno + " macro : " //$NON-NLS-1$ //$NON-NLS-2$
									+ string);
					}
						break;
					case DwarfConstants.DW_MACRO_undef: {
						long lineno;
						String macro;
						lineno = read_signed_leb128(data);
						macro = readString(data);
						if (DEBUG)
							System.out.println(" DW_MACRO_undef - lineno : " + lineno + " macro : " //$NON-NLS-1$ //$NON-NLS-2$
									+ macro);
					}
						break;
					case DwarfConstants.DW_MACRO_define_indirect: {
						long lineno;
						long offset;
						lineno = read_signed_leb128(data);
						offset = (offset_size_8 ? read_8_bytes(data) : read_4_bytes(data));
						str.position((int) offset);
						String macro = readString(str);
						if (lineno == 0)
							macros.add(getCommandLineMacro(macro));
						if (DEBUG)
							System.out.println(" DW_MACRO_define_indirect - lineno : " + lineno + " macro : " //$NON-NLS-1$ //$NON-NLS-2$
									+ macro);
					}
						break;
					case DwarfConstants.DW_MACRO_define_indirect_alt: {
						long lineno;
						long offset;
						lineno = read_signed_leb128(data);
						offset = (offset_size_8 ? read_8_bytes(data) : read_4_bytes(data));
						altstr.position((int) offset);
						String macro = readString(altstr);
						if (lineno == 0)
							macros.add(getCommandLineMacro(macro));
						if (DEBUG)
							System.out.println(" DW_MACRO_define_indirect_alt - lineno : " + lineno + " macro : " //$NON-NLS-1$ //$NON-NLS-2$
									+ macro);
					}
						break;
					case DwarfConstants.DW_MACRO_undef_indirect: {
						long lineno;
						long offset;
						String macro;
						lineno = read_signed_leb128(data);
						offset = (offset_size_8 ? read_8_bytes(data) : read_4_bytes(data));
						str.position((int) offset);
						macro = readString(str);
						if (DEBUG)
							System.out.println(" DW_MACRO_undef_indirect - lineno : " + lineno + " macro : " //$NON-NLS-1$ //$NON-NLS-2$
									+ macro);
					}
						break;
					case DwarfConstants.DW_MACRO_undef_indirect_alt: {
						long lineno;
						long offset;
						String macro;
						lineno = read_signed_leb128(data);
						offset = (offset_size_8 ? read_8_bytes(data) : read_4_bytes(data));
						altstr.position((int) offset);
						macro = readString(altstr);
						if (DEBUG)
							System.out.println(" DW_MACRO_undef_indirect_alt - lineno : " + lineno + " macro : " //$NON-NLS-1$ //$NON-NLS-2$
									+ macro);
					}
						break;
					case DwarfConstants.DW_MACRO_transparent_include: {
						long offset;
						offset = (offset_size_8 ? read_8_bytes(data) : read_4_bytes(data));
						ArrayList<String> foundMacros = t_macros.get(Long.valueOf(offset));
						if (foundMacros != null)
							macros.addAll(foundMacros);
						else if (lt_offset >= 0) {
							macros.add(fixupMarker + offset); // leave a marker we can fix up later
							if (DEBUG)
								System.out.println("Adding fixup for offset: " + offset + " for file: " + fileName); //$NON-NLS-1$ //$NON-NLS-2$
							fixupList.add(fileName);
						}

						if (DEBUG)
							System.out.println(" DW_MACRO_transparent_include - offset : " + offset); //$NON-NLS-1$
					}
						break;
					case DwarfConstants.DW_MACRO_transparent_include_alt: {
						long offset;
						offset = (offset_size_8 ? read_8_bytes(data) : read_4_bytes(data));
						if (lt_offset >= 0) {
							macros.add("=FIXUPALT=" + offset); // leave a marker we can fix up later //$NON-NLS-1$
							if (DEBUG)
								System.out.println("Adding alt fixup for offset: " + offset + " for file: " + fileName); //$NON-NLS-1$ //$NON-NLS-2$
							fixupAltList.add(fileName);
						}

						if (DEBUG)
							System.out.println(" DW_MACRO_transparent_include - offset : " + offset); //$NON-NLS-1$
					}
						break;
					case DwarfConstants.DW_MACRO_end: {
						if (lt_offset < 0) {
							if (DEBUG)
								System.out.println(
										"creating transparent include macros for offset: " + original_position); //$NON-NLS-1$
							t_macros.put(Long.valueOf(original_position), macros);
						}
						done = true;
					}
						break;
					default: {
						if (opcodeInfos != null) {
							OpcodeInfo info = opcodeInfos.get(op);
							info.readPastEntry(data);
						}
					}
						break;
					}
				}
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	/**
	 * Get the set of command line flags used for a particular file name.
	 *
	 * @param fileName - name of file
	 * @return string containing all macros used on command line to compile the file
	 *
	 * @since 5.7
	 */
	@Override
	public String getCompileOptions(String fileName) {
		if (!m_macros_parsed) {
			getSourceFiles();
			getCommandMacrosFromMacroSection();
			m_macros_parsed = true;
		}
		ArrayList<String> macros = m_compileOptionsMap.get(fileName);
		if (macros == null)
			return ""; //$NON-NLS-1$

		StringBuilder sb = new StringBuilder();
		for (String option : macros) {
			sb.append(option);
			sb.append(" "); //$NON-NLS-1$
		}
		return sb.toString();
	}

}
