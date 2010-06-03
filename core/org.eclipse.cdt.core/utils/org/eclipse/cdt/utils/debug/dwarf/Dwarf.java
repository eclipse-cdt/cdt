/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.debug.dwarf;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.coff.Coff.SectionHeader;
import org.eclipse.cdt.utils.debug.DebugUnknownType;
import org.eclipse.cdt.utils.debug.IDebugEntryRequestor;
import org.eclipse.cdt.utils.debug.tools.DebugSym;
import org.eclipse.cdt.utils.debug.tools.DebugSymsRequestor;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.Section;

public class Dwarf {

	/* Section names. */
	final static String DWARF_DEBUG_INFO = ".debug_info"; //$NON-NLS-1$
	final static String DWARF_DEBUG_ABBREV = ".debug_abbrev"; //$NON-NLS-1$
	final static String DWARF_DEBUG_ARANGES = ".debug_aranges"; //$NON-NLS-1$
	final static String DWARF_DEBUG_LINE = ".debug_line"; //$NON-NLS-1$
	final static String DWARF_DEBUG_FRAME = ".debug_frame"; //$NON-NLS-1$
	final static String DWARF_EH_FRAME = ".eh_frame"; //$NON-NLS-1$
	final static String DWARF_DEBUG_LOC = ".debug_loc"; //$NON-NLS-1$
	final static String DWARF_DEBUG_PUBNAMES = ".debug_pubnames"; //$NON-NLS-1$
	final static String DWARF_DEBUG_STR = ".debug_str"; //$NON-NLS-1$
	final static String DWARF_DEBUG_FUNCNAMES = ".debug_funcnames"; //$NON-NLS-1$
	final static String DWARF_DEBUG_TYPENAMES = ".debug_typenames"; //$NON-NLS-1$
	final static String DWARF_DEBUG_VARNAMES = ".debug_varnames"; //$NON-NLS-1$
	final static String DWARF_DEBUG_WEAKNAMES = ".debug_weaknames"; //$NON-NLS-1$
	final static String DWARF_DEBUG_MACINFO = ".debug_macinfo"; //$NON-NLS-1$
	final static String[] DWARF_SCNNAMES =
		{
			DWARF_DEBUG_INFO,
			DWARF_DEBUG_ABBREV,
			DWARF_DEBUG_ARANGES,
			DWARF_DEBUG_LINE,
			DWARF_DEBUG_FRAME,
			DWARF_EH_FRAME,
			DWARF_DEBUG_LOC,
			DWARF_DEBUG_PUBNAMES,
			DWARF_DEBUG_STR,
			DWARF_DEBUG_FUNCNAMES,
			DWARF_DEBUG_TYPENAMES,
			DWARF_DEBUG_VARNAMES,
			DWARF_DEBUG_WEAKNAMES,
			DWARF_DEBUG_MACINFO };

	class CompilationUnitHeader {
		int length;
		short version;
		int abbreviationOffset;
		byte addressSize;
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Length: " + length).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Version: " + version).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Abbreviation: " + abbreviationOffset).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Address size: " + addressSize).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			return sb.toString();
		}
	}

	class AbbreviationEntry {
		/* unsigned */
		long code;
		/* unsigned */
		long tag;
		byte hasChildren;
		List<Attribute> attributes;
		AbbreviationEntry(long c, long t, byte h) {
			code = c;
			tag = t;
			hasChildren = h;
			attributes = new ArrayList<Attribute>();
		}
	}

	class Attribute {
		/* unsigned */
		long name;
		/* unsigned */
		long form;
		Attribute(long n, long f) {
			name = n;
			form = f;
		}
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("name: " + Long.toHexString(name)); //$NON-NLS-1$
			sb.append(" value: " + Long.toHexString(form)); //$NON-NLS-1$
			return sb.toString();
		}
	}

	class AttributeValue {
		Attribute attribute;
		Object value;
		AttributeValue(Attribute a, Object o) {
			attribute = a;
			value = o;
		}
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(attribute.toString()).append(' ');
			if (value != null) {
				Class<? extends Object> clazz = value.getClass();
				if (clazz.isArray()) {
					int len = Array.getLength(value);
					sb.append(len).append(' ');
					sb.append(clazz.getComponentType().toString());
					sb.append(':');
					for (int i = 0; i < len; i++) {
						byte b = Array.getByte(value, i);
						sb.append(' ').append(Integer.toHexString(b));
					}
				} else {
					if (value instanceof Number) {
						Number n = (Number) value;
						sb.append(Long.toHexString(n.longValue()));
					} else if (value instanceof String) {
						sb.append(value);
					} else {
						sb.append(value);
					}
				}
			}
			return sb.toString();
		}
	}

	class CompileUnit {
		long lowPC;
		long highPC;
		int stmtList;
		String name;
		int language;
		int macroInfo;
		String compDir;
		String producer;
		int identifierCase;
	}

	Map<String, ByteBuffer> dwarfSections = new HashMap<String, ByteBuffer>();
	Map<Integer, Map<Long, AbbreviationEntry>> abbreviationMaps = new HashMap<Integer, Map<Long, AbbreviationEntry>>();

	boolean isLE;

	CompileUnit currentCU;

	boolean printEnabled = true;
	
	public Dwarf(String file) throws IOException {
		Elf exe = new Elf(file);
		init(exe);
		exe.dispose();
	}

	public Dwarf(Elf exe) throws IOException {
		init(exe);
	}

	/**
	 * @since 5.1
	 */
	public Dwarf(PE exe) throws IOException {
		init(exe);
	}

	public void init(Elf exe) throws IOException {
		Elf.ELFhdr header = exe.getELFhdr();
		isLE = header.e_ident[Elf.ELFhdr.EI_DATA] == Elf.ELFhdr.ELFDATA2LSB;

		Elf.Section[] sections = exe.getSections();
		for (Section section : sections) {
			String name = section.toString();
			for (String element : DWARF_SCNNAMES) {
				if (name.equals(element)) {
					try {
						dwarfSections.put(element, section.mapSectionData());
					} catch (Exception e) {
						e.printStackTrace();
						CCorePlugin.log(e);
					}
				}
			}
		}
	}

	/**
	 * @since 5.1
	 */
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
						e.printStackTrace();
						CCorePlugin.log(e);
					}
				}
			}
			}

	}

	int read_4_bytes(ByteBuffer in) throws IOException {
		try {
			byte[] bytes = new byte[4];
			in.get(bytes);
			return read_4_bytes(bytes);
		} catch (Exception e) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.missingBytes")); //$NON-NLS-1$
		}
	}

	// FIXME:This is wrong, it's signed.
	int read_4_bytes(byte[] bytes) throws IndexOutOfBoundsException {
		if (isLE) {
			return (
				((bytes[3] & 0xff) << 24)
					| ((bytes[2] & 0xff) << 16)
					| ((bytes[1] & 0xff) << 8)
					| (bytes[0] & 0xff));
		}
		return (
			((bytes[0] & 0xff) << 24)
				| ((bytes[1] & 0xff) << 16)
				| ((bytes[2] & 0xff) << 8)
				| (bytes[3] & 0xff));
	}

	long read_8_bytes(ByteBuffer in) throws IOException {
		try {
			byte[] bytes = new byte[8];
			in.get(bytes);
			return read_8_bytes(bytes);
		} catch (Exception e) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.missingBytes")); //$NON-NLS-1$
		}
	}

	// FIXME:This is wrong, for unsigned.
	long read_8_bytes(byte[] bytes) throws IndexOutOfBoundsException {

		if (isLE) {
			return (((bytes[7] & 0xff) << 56)
				| ((bytes[6] & 0xff) << 48)
				| ((bytes[5] & 0xff) << 40)
				| ((bytes[4] & 0xff) << 32)
				| ((bytes[3] & 0xff) << 24)
				| ((bytes[2] & 0xff) << 16)
				| ((bytes[1] & 0xff) << 8)
				| (bytes[0] & 0xff));
		}

		return (((bytes[0] & 0xff) << 56)
			| ((bytes[1] & 0xff) << 48)
			| ((bytes[2] & 0xff) << 40)
			| ((bytes[3] & 0xff) << 32)
			| ((bytes[4] & 0xff) << 24)
			| ((bytes[5] & 0xff) << 16)
			| ((bytes[6] & 0xff) << 8)
			| (bytes[7] & 0xff));
	}

	short read_2_bytes(ByteBuffer in) throws IOException {
		try {
			byte[] bytes = new byte[2];
			in.get(bytes);
			return read_2_bytes(bytes);
		} catch (Exception e) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.missingBytes")); //$NON-NLS-1$
		}
	}

	short read_2_bytes(byte[] bytes) throws IndexOutOfBoundsException {
		if (isLE) {
			return (short) (((bytes[1] & 0xff) << 8) + (bytes[0] & 0xff));
		}
		return (short) (((bytes[0] & 0xff) << 8) + (bytes[1] & 0xff));
	}

	/* unsigned */
	long read_unsigned_leb128(ByteBuffer in) throws IOException {
		/* unsigned */
		long result = 0;
		int shift = 0;
		short b;

		while (true) {
			b = in.get();
			if (!in.hasRemaining())
				break; //throw new IOException("no more data");
			result |= ((long) (b & 0x7f) << shift);
			if ((b & 0x80) == 0) {
				break;
			}
			shift += 7;
		}
		return result;
	}

	/* unsigned */
	long read_signed_leb128(ByteBuffer in) throws IOException {
		/* unsigned */
		long result = 0;
		int shift = 0;
		int size = 32;
		short b;

		while (true) {
			b = in.get();
			if (!in.hasRemaining())
				throw new IOException(CCorePlugin.getResourceString("Util.exception.noData")); //$NON-NLS-1$
			result |= ((long) (b & 0x7f) << shift);
			shift += 7;
			if ((b & 0x80) == 0) {
				break;
			}
		}
		if ((shift < size) && (b & 0x40) != 0) {
			result |= - (1 << shift);
		}
		return result;
	}

	public void parse(IDebugEntryRequestor requestor) {
		parseDebugInfo(requestor);
	}

	void parseDebugInfo(IDebugEntryRequestor requestor) {
		ByteBuffer data = dwarfSections.get(DWARF_DEBUG_INFO);
		if (data != null) {
			try {
				while (data.hasRemaining()) {
					CompilationUnitHeader header = new CompilationUnitHeader();
					header.length = read_4_bytes(data);
					header.version = read_2_bytes(data);
					header.abbreviationOffset = read_4_bytes(data);
					header.addressSize = data.get();

					if (printEnabled) {
						System.out.println("Compilation Unit @ " + Long.toHexString(data.position())); //$NON-NLS-1$
						System.out.println(header);
					}

					// read the abbrev section.
					Map<Long, AbbreviationEntry> abbrevs = parseDebugAbbreviation(header);
					// Note "length+4" is the total size in bytes of the CU data.
					ByteBuffer entryBuffer = data.slice();
					entryBuffer.limit(header.length + 4 - 11);
					parseDebugInfoEntry(requestor, entryBuffer, abbrevs, header);

					data.position(data.position() + header.length + 4 - 11);
					
					if (printEnabled)
						System.out.println();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	Map<Long, AbbreviationEntry> parseDebugAbbreviation(CompilationUnitHeader header) throws IOException {
		Integer key = new Integer(header.abbreviationOffset);
		Map<Long, AbbreviationEntry> abbrevs = abbreviationMaps.get(key);
		if (abbrevs == null) {
			abbrevs = new HashMap<Long, AbbreviationEntry>();
			abbreviationMaps.put(key, abbrevs);
			ByteBuffer data = dwarfSections.get(DWARF_DEBUG_ABBREV);
			if (data != null) {
				data.position(header.abbreviationOffset);
				while (data.remaining() > 0) {
					long code = read_unsigned_leb128(data);
					if (code == 0) {
						break;
					}
					long tag = read_unsigned_leb128(data);
					byte hasChildren = data.get();
					AbbreviationEntry entry = new AbbreviationEntry(code, tag, hasChildren);

					//System.out.println("\tAbrev Entry: " + code + " " + Long.toHexString(entry.tag) + " " + entry.hasChildren);

					// attributes
					long name = 0;
					long form = 0;
					do {
						name = read_unsigned_leb128(data);
						form = read_unsigned_leb128(data);
						if (name != 0) {
							entry.attributes.add(new Attribute(name, form));
						}
						//System.out.println("\t\t " + Long.toHexString(name) + " " + Long.toHexString(value));
					} while (name != 0 && form != 0);
					abbrevs.put(new Long(code), entry);
				}
			}
		}
		return abbrevs;
	}

	void parseDebugInfoEntry(IDebugEntryRequestor requestor, ByteBuffer in, Map<Long, AbbreviationEntry> abbrevs, CompilationUnitHeader header)
		throws IOException {
		while (in.remaining() > 0) {
			long code = read_unsigned_leb128(in);
			AbbreviationEntry entry = abbrevs.get(new Long(code));
			if (entry != null) {
				int len = entry.attributes.size();
				List<AttributeValue> list = new ArrayList<AttributeValue>(len);
				try {
					for (int i = 0; i < len; i++) {
						Attribute attr = entry.attributes.get(i);
						Object obj = readAttribute((int) attr.form, in, header);
						list.add(new AttributeValue(attr, obj));
					}
				} catch (IOException e) {
					//break;
				}
				processDebugInfoEntry(requestor, entry, list);
			}
		}
	}

	Object readAttribute(int form, ByteBuffer in, CompilationUnitHeader header) throws IOException {
		Object obj = null;
		switch (form) {
			case DwarfConstants.DW_FORM_addr :
			case DwarfConstants.DW_FORM_ref_addr :
				obj = readAddress(in, header);
				break;

			case DwarfConstants.DW_FORM_block :
				{
					int size = (int) read_unsigned_leb128(in);
					byte[] bytes = new byte[size];
					in.get(bytes);
					obj = bytes;
				}
				break;

			case DwarfConstants.DW_FORM_block1 :
				{
					int size = in.get();
					byte[] bytes = new byte[size];
					in.get(bytes);
					obj = bytes;
				}
				break;

			case DwarfConstants.DW_FORM_block2 :
				{
					int size = read_2_bytes(in);
					byte[] bytes = new byte[size];
					in.get(bytes);
					obj = bytes;
				}
				break;

			case DwarfConstants.DW_FORM_block4 :
				{
					int size = read_4_bytes(in);
					byte[] bytes = new byte[size];
					in.get(bytes);
					obj = bytes;
				}
				break;

			case DwarfConstants.DW_FORM_data1 :
				obj = new Byte(in.get());
				break;

			case DwarfConstants.DW_FORM_data2 :
				obj = new Short(read_2_bytes(in));
				break;

			case DwarfConstants.DW_FORM_data4 :
				obj = new Integer(read_4_bytes(in));
				break;

			case DwarfConstants.DW_FORM_data8 :
				obj = new Long(read_8_bytes(in));
				break;

			case DwarfConstants.DW_FORM_sdata :
				obj = new Long(read_signed_leb128(in));
				break;

			case DwarfConstants.DW_FORM_udata :
				obj = new Long(read_unsigned_leb128(in));
				break;

			case DwarfConstants.DW_FORM_string :
				{
					int c;
					StringBuffer sb = new StringBuffer();
					while ((c = in.get()) != -1) {
						if (c == 0) {
							break;
						}
						sb.append((char) c);
					}
					obj = sb.toString();
				}
				break;

			case DwarfConstants.DW_FORM_flag :
				obj = new Byte(in.get());
				break;

			case DwarfConstants.DW_FORM_strp :
				{
					int offset = read_4_bytes(in);
					ByteBuffer data = dwarfSections.get(DWARF_DEBUG_STR);
					if (data == null) {
						obj = new String();
					} else if (offset < 0 || offset > data.capacity()) {
						obj = new String();
					} else {
						StringBuffer sb = new StringBuffer();
						data.position(offset);
						while (data.hasRemaining()) {
							byte c = data.get();
							if (c == 0) {
								break;
							}
							sb.append((char) c);
						}
						obj = sb.toString();
					}
				}
				break;

			case DwarfConstants.DW_FORM_ref1 :
				obj = new Byte(in.get());
				break;

			case DwarfConstants.DW_FORM_ref2 :
				obj = new Short(read_2_bytes(in));
				break;

			case DwarfConstants.DW_FORM_ref4 :
				obj = new Integer(read_4_bytes(in));
				break;

			case DwarfConstants.DW_FORM_ref8 :
				obj = new Long(read_8_bytes(in));
				break;

			case DwarfConstants.DW_FORM_ref_udata :
				obj = new Long(read_unsigned_leb128(in));
				break;

			case DwarfConstants.DW_FORM_indirect :
				{
					int f = (int) read_unsigned_leb128(in);
					return readAttribute(f, in, header);
				}

			default :
				break;
		}

		return obj;
	}

	void processDebugInfoEntry(IDebugEntryRequestor requestor, AbbreviationEntry entry, List<AttributeValue> list) {
		int len = list.size();
		int tag = (int) entry.tag;
		if (printEnabled)
			System.out.println("Abbrev Number " + entry.code); //$NON-NLS-1$
		
		for (int i = 0; i < len; i++) {
			AttributeValue av = list.get(i);
			if (printEnabled)
				System.out.println(av);
			// We are only interrested in certain tags.
			switch (tag) {
				case DwarfConstants.DW_TAG_array_type :
					break;
				case DwarfConstants.DW_TAG_class_type :
					break;
				case DwarfConstants.DW_TAG_enumeration_type :
					break;
				case DwarfConstants.DW_TAG_formal_parameter :
					break;
				case DwarfConstants.DW_TAG_lexical_block :
					break;
				case DwarfConstants.DW_TAG_member :
					break;
				case DwarfConstants.DW_TAG_pointer_type :
					break;
				case DwarfConstants.DW_TAG_reference_type :
					break;
				case DwarfConstants.DW_TAG_compile_unit :
					processCompileUnit(requestor, list);
					break;
				case DwarfConstants.DW_TAG_structure_type :
					break;
				case DwarfConstants.DW_TAG_subroutine_type :
					break;
				case DwarfConstants.DW_TAG_typedef :
					break;
				case DwarfConstants.DW_TAG_union_type :
					break;
				case DwarfConstants.DW_TAG_unspecified_parameters :
					break;
				case DwarfConstants.DW_TAG_inheritance :
					break;
				case DwarfConstants.DW_TAG_ptr_to_member_type :
					break;
				case DwarfConstants.DW_TAG_with_stmt :
					break;
				case DwarfConstants.DW_TAG_base_type :
					break;
				case DwarfConstants.DW_TAG_catch_block :
					break;
				case DwarfConstants.DW_TAG_const_type :
					break;
				case DwarfConstants.DW_TAG_enumerator :
					break;
				case DwarfConstants.DW_TAG_file_type :
					break;
				case DwarfConstants.DW_TAG_friend :
					break;
				case DwarfConstants.DW_TAG_subprogram :
					processSubProgram(requestor, list);
					break;
				case DwarfConstants.DW_TAG_template_type_param :
					break;
				case DwarfConstants.DW_TAG_template_value_param :
					break;
				case DwarfConstants.DW_TAG_thrown_type :
					break;
				case DwarfConstants.DW_TAG_try_block :
					break;
				case DwarfConstants.DW_TAG_variable :
					break;
				case DwarfConstants.DW_TAG_volatile_type :
					break;
			}
		}
	}

	Long readAddress(ByteBuffer in, CompilationUnitHeader header) throws IOException {
		long value = 0;

		switch (header.addressSize) {
			case 2 :
				value = read_2_bytes(in);
				break;
			case 4 :
				value = read_4_bytes(in);
				break;
			case 8 :
				value = read_8_bytes(in);
				break;
			default :
				// ????
		}
		return new Long(value);
	}

	void processSubProgram(IDebugEntryRequestor requestor, List<AttributeValue> list) {
		long lowPC = 0;
		long highPC = 0;
		String funcName = ""; //$NON-NLS-1$
		boolean isExtern = false;

		for (int i = 0; i < list.size(); i++) {
			AttributeValue av = list.get(i);
			try {
				int name = (int)av.attribute.name;
				switch(name) {
					case DwarfConstants.DW_AT_low_pc:
						lowPC = ((Number)av.value).longValue();
						break;

					case DwarfConstants.DW_AT_high_pc:
						highPC = ((Number)av.value).longValue();
						break;

					case DwarfConstants.DW_AT_name:
						funcName = (String)av.value;
						break;

					case DwarfConstants.DW_AT_external:
						isExtern = ((Number)av.value).intValue() > 0;
						break;
				}
			} catch (ClassCastException e) {
			}
		}
		requestor.enterFunction(funcName, new DebugUnknownType(""), isExtern, lowPC); //$NON-NLS-1$
		requestor.exitFunction(highPC);
	}

	void processCompileUnit(IDebugEntryRequestor requestor, List<AttributeValue> list) {
		if (currentCU != null) {
			requestor.exitCompilationUnit(currentCU.highPC);
		}
		currentCU = new CompileUnit();
		for (int i = 0; i < list.size(); i++) {
			AttributeValue av = list.get(i);
			try {
				int name = (int)av.attribute.name;
				switch(name) {
					case DwarfConstants.DW_AT_low_pc:
						currentCU.lowPC = ((Number)av.value).longValue();
						break;
			
					case DwarfConstants.DW_AT_high_pc:
						currentCU.highPC = ((Number)av.value).longValue();
						break;

					case DwarfConstants.DW_AT_name:
						currentCU.name = (String)av.value;
						break;

					case DwarfConstants.DW_AT_language:
						currentCU.language = ((Number)av.value).intValue();
						break;

					case DwarfConstants.DW_AT_stmt_list:
						currentCU.stmtList = ((Number)av.value).intValue();
						break;

					case DwarfConstants.DW_AT_macro_info:
						currentCU.macroInfo = ((Number)av.value).intValue();
						break;

					case DwarfConstants.DW_AT_comp_dir:
						currentCU.compDir = (String)av.value;
						break;

					case DwarfConstants.DW_AT_producer:
						currentCU.producer = (String)av.value;
						break;

					//case DW_AT_identifier_case:
				}
			} catch (ClassCastException e) {
			}
		}
		requestor.enterCompilationUnit(currentCU.name, currentCU.lowPC);
	}

	public static void main(String[] args) {
		try {
			DebugSymsRequestor symreq = new DebugSymsRequestor();				
			Dwarf dwarf = new Dwarf(args[0]);
			dwarf.parse(symreq);
			DebugSym[] entries = symreq.getEntries();
			for (DebugSym entry : entries) {
				System.out.println(entry);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
