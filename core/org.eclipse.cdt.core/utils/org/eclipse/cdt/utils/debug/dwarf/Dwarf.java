/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.utils.debug.dwarf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.utils.elf.Elf;

public class Dwarf {

	/* Section names. */
	final static String DWARF_DEBUG_INFO = ".debug_info";
	final static String DWARF_DEBUG_ABBREV = ".debug_abbrev";
	final static String DWARF_DEBUG_ARANGES = ".debug_aranges";
	final static String DWARF_DEBUG_LINE = ".debug_line";
	final static String DWARF_DEBUG_FRAME = ".debug_frame";
	final static String DWARF_EH_FRAME = ".eh_frame";
	final static String DWARF_DEBUG_LOC = ".debug_loc";
	final static String DWARF_DEBUG_PUBNAMES = ".debug_pubnames";
	final static String DWARF_DEBUG_STR = ".debug_str";
	final static String DWARF_DEBUG_FUNCNAMES = ".debug_funcnames";
	final static String DWARF_DEBUG_TYPENAMES = ".debug_typenames";
	final static String DWARF_DEBUG_VARNAMES = ".debug_varnames";
	final static String DWARF_DEBUG_WEAKNAMES = ".debug_weaknames";
	final static String DWARF_DEBUG_MACINFO = ".debug_macinfo";
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
			DWARF_DEBUG_MACINFO
		};

	public static class DwarfSection {
		String name;
		byte[] data;
		public DwarfSection (String n, byte[] d) {
			name = n;
			data = d;
		}
	}

	DwarfSection[] dwarfSections;
	boolean isLE;

	public Dwarf(String file) throws IOException {
		Elf exe = new Elf(file);
		init(exe);
		exe.dispose();
	}

	public Dwarf(Elf exe) throws IOException {
		init(exe);
	}

	public void init(Elf exe) throws IOException {
		Elf.ELFhdr header = exe.getELFhdr();
		isLE = header.e_ident[Elf.ELFhdr.EI_DATA] == Elf.ELFhdr.ELFDATA2LSB;

		Elf.Section[] sections = exe.getSections();
		List list = new ArrayList();
		for (int i = 0; i < sections.length; i++) {
			String name = sections[i].toString();
			for (int j = 0; j < DWARF_SCNNAMES.length; j++) {
				if (name.equals(DWARF_SCNNAMES[j])) {
					list.add(new DwarfSection(name, sections[i].loadSectionData()));
				}
			}
		}
		dwarfSections = new DwarfSection[list.size()];
		list.toArray(dwarfSections);
	}

	int read_4_bytes(byte[] bytes, int offset) {
		if (isLE) {
			return (((bytes[offset + 3] & 0xff) << 24) + ((bytes[offset + 2] & 0xff) << 16)
					+ ((bytes[offset + 1] & 0xff) << 8) + (bytes[offset] & 0xff));
		}
		return (((bytes[offset] & 0xff) << 24) + ((bytes[offset + 1] & 0xff) << 16) + ((bytes[offset + 2] & 0xff) << 8) + (bytes[offset + 3] & 0xff));
	}

	short read_2_bytes(byte[] bytes, int offset) {
		if (isLE) {
			return (short) (((bytes[offset + 1] & 0xff) << 8) + (bytes[offset] & 0xff));
		}
		return (short) (((bytes[offset] & 0xff) << 8) + (bytes[offset + 1] & 0xff));
	}

	public void parse() {
		for (int i = 0; i < dwarfSections.length; i++) {
			if (dwarfSections[i].name.equals(DWARF_DEBUG_INFO)) {
				parse_debug_info(dwarfSections[i].data);
			}
		}
	}

	void parse_debug_info(byte[] data) {
		int offset = 0;
		int nentries = data.length / 11;
		for (int i = 0; i < nentries; offset += 11) {
			int length = read_4_bytes(data, offset);
			short version = read_2_bytes(data, offset + 4);
			int abbrev_offset = read_4_bytes(data, offset + 4 + 2);
			byte address_size = data[offset + 4 + 2 + 4];
			System.out.println("Length:" + length);
			System.out.println("Version:" + version);
			System.out.println("Abbreviation:" + abbrev_offset);
			System.out.println("Address size:" + address_size);
		}
	}

	void parse_compilation_unit() {
		
	}

	public static void main(String[] args) {
		try {
			Dwarf dwarf = new Dwarf(args[0]);
			dwarf.parse();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
