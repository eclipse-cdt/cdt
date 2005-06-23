/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.elf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

/**
 * <code>ElfHelper</code> is a wrapper class for the <code>Elf</code> class
 * to provide higher level API for sorting/searching the ELF data.
 * 
 * @see Elf
 */
public class ElfHelper {

	private Elf elf;
	private Elf.Symbol[] dynsyms;
	private Elf.Symbol[] symbols;
	private Elf.Section[] sections;
	private Elf.Dynamic[] dynamics;

	public void dispose() {
		if (elf != null) {
			elf.dispose();
			elf = null;
		}
	}

	public class Sizes {

		public long text;
		public long data;
		public long bss;
		public long total;
		public Sizes(long t, long d, long b) {
			text = t;
			data = d;
			bss = b;
			total = text + data + bss;
		}
	}

	private void loadSymbols() throws IOException {
		if (symbols == null) {
			elf.loadSymbols();
			symbols = elf.getSymtabSymbols();
			dynsyms = elf.getDynamicSymbols();

			if (symbols.length <= 0)
				symbols = dynsyms;
			if (dynsyms.length <= 0)
				dynsyms = symbols;
		}
	}

	private void loadSections() throws IOException {
		if (sections == null)
			sections = elf.getSections();
	}

	private void loadDynamics() throws IOException {
		if (dynamics == null) {
			dynamics = new Elf.Dynamic[0];
			Elf.Section dynSect = elf.getSectionByName(".dynamic"); //$NON-NLS-1$
			if (dynSect != null) {
				dynamics = elf.getDynamicSections(dynSect);
			}

		}
	}

	/**
	 * Create a new <code>ElfHelper</code> using an existing <code>Elf</code>
	 * object.
	 * 
	 * @param elf
	 *            An existing Elf object to wrap.
	 * @throws IOException
	 *             Error processing the Elf file.
	 */
	public ElfHelper(Elf elf) throws IOException {
		this.elf = elf;
	}

	/**
	 * Create a new <code>ElfHelper</code> based on the given filename.
	 * 
	 * @param filename
	 *            The file to use for creating a new Elf object.
	 * @throws IOException
	 *             Error processing the Elf file.
	 * @see Elf#Elf( String )
	 */
	public ElfHelper(String filename) throws IOException {
		elf = new Elf(filename);
	}

	/**
	 * Create a new <code>ElfHelper</code> based on the given filename.
	 * 
	 * @param filename
	 *            The file to use for creating a new Elf object.
	 * @throws IOException
	 *             Error processing the Elf file.
	 * @see Elf#Elf( String )
	 */
	public ElfHelper(String filename, long fileoffset) throws IOException {
		elf = new Elf(filename, fileoffset);
	}


	/** Give back the Elf object that this helper is wrapping */
	public Elf getElf() {
		return elf;
	}

	public Elf.Symbol[] getExternalFunctions() throws IOException {
		Vector v = new Vector();

		loadSymbols();
		loadSections();

		for (int i = 0; i < dynsyms.length; i++) {
			if (dynsyms[i].st_bind() == Elf.Symbol.STB_GLOBAL && dynsyms[i].st_type() == Elf.Symbol.STT_FUNC) {
				int idx = dynsyms[i].st_shndx;
				if (idx < Elf.Symbol.SHN_HIPROC && idx > Elf.Symbol.SHN_LOPROC) {
					String name = dynsyms[i].toString();
					if (name != null && name.trim().length() > 0)
						v.add(dynsyms[i]);
				} else if (idx >= 0 && sections[idx].sh_type == Elf.Section.SHT_NULL) {
					v.add(dynsyms[i]);
				}
			}
		}

		Elf.Symbol[] ret = (Elf.Symbol[])v.toArray(new Elf.Symbol[v.size()]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	public Elf.Symbol[] getExternalObjects() throws IOException {
		Vector v = new Vector();

		loadSymbols();
		loadSections();

		for (int i = 0; i < dynsyms.length; i++) {
			if (dynsyms[i].st_bind() == Elf.Symbol.STB_GLOBAL && dynsyms[i].st_type() == Elf.Symbol.STT_OBJECT) {
				int idx = dynsyms[i].st_shndx;
				if (idx < Elf.Symbol.SHN_HIPROC && idx > Elf.Symbol.SHN_LOPROC) {
					String name = dynsyms[i].toString();
					if (name != null && name.trim().length() > 0)
						v.add(dynsyms[i]);
				} else if (idx >= 0 && sections[idx].sh_type == Elf.Section.SHT_NULL) {
					v.add(dynsyms[i]);
				}
			}
		}

		Elf.Symbol[] ret = (Elf.Symbol[])v.toArray(new Elf.Symbol[v.size()]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	public Elf.Symbol[] getUndefined() throws IOException {
		Vector v = new Vector();

		loadSymbols();

		for (int i = 0; i < dynsyms.length; i++) {
			if (dynsyms[i].st_shndx == Elf.Symbol.SHN_UNDEF)
				v.add(dynsyms[i]);
		}

		Elf.Symbol[] ret = (Elf.Symbol[])v.toArray(new Elf.Symbol[v.size()]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	public Elf.Symbol[] getLocalFunctions() throws IOException {
		Vector v = new Vector();

		loadSymbols();
		loadSections();

		for (int i = 0; i < symbols.length; i++) {
			if ( symbols[i].st_type() == Elf.Symbol.STT_FUNC) {
				int idx = symbols[i].st_shndx;
				if (idx < Elf.Symbol.SHN_HIPROC && idx > Elf.Symbol.SHN_LOPROC) {
					String name = symbols[i].toString();
					if (name != null && name.trim().length() > 0)
						v.add(symbols[i]);
				} else if (idx >= 0 && sections[idx].sh_type != Elf.Section.SHT_NULL) {
					v.add(symbols[i]);
				}
			}
		}

		Elf.Symbol[] ret = (Elf.Symbol[])v.toArray(new Elf.Symbol[v.size()]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	public Elf.Symbol[] getLocalObjects() throws IOException {
		Vector v = new Vector();

		loadSymbols();
		loadSections();

		for (int i = 0; i < symbols.length; i++) {
			if ( symbols[i].st_type() == Elf.Symbol.STT_OBJECT) {
				int idx = symbols[i].st_shndx;
				if (idx < Elf.Symbol.SHN_HIPROC && idx > Elf.Symbol.SHN_LOPROC) {
					String name = symbols[i].toString();
					if (name != null && name.trim().length() > 0)
						v.add(symbols[i]);
				} else if (idx >= 0 && sections[idx].sh_type != Elf.Section.SHT_NULL) {
					v.add(symbols[i]);
				}
			}
		}

		Elf.Symbol[] ret = (Elf.Symbol[])v.toArray(new Elf.Symbol[v.size()]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	public Elf.Symbol[] getCommonObjects() throws IOException {
		Vector v = new Vector();

		loadSymbols();
		loadSections();

		for (int i = 0; i < symbols.length; i++) {
			if (symbols[i].st_bind() == Elf.Symbol.STB_GLOBAL && symbols[i].st_type() == Elf.Symbol.STT_OBJECT) {
				int idx = symbols[i].st_shndx;
				if (idx == Elf.Symbol.SHN_COMMON) {
					v.add(symbols[i]);
				}
			}
		}

		Elf.Symbol[] ret = (Elf.Symbol[])v.toArray(new Elf.Symbol[v.size()]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	public Elf.Dynamic[] getNeeded() throws IOException {
		Vector v = new Vector();

		loadDynamics();

		for (int i = 0; i < dynamics.length; i++) {
			if (dynamics[i].d_tag == Elf.Dynamic.DT_NEEDED)
				v.add(dynamics[i]);
		}
		return (Elf.Dynamic[])v.toArray(new Elf.Dynamic[v.size()]);
	}

	public String getSoname() throws IOException {
		String soname = ""; //$NON-NLS-1$

		loadDynamics();

		for (int i = 0; i < dynamics.length; i++) {
			if (dynamics[i].d_tag == Elf.Dynamic.DT_SONAME)
				soname = dynamics[i].toString();
		}
		return soname;
	}

	private String getSubUsage(String full, String name) {
		int start, end;
		//boolean has_names = false;
		//boolean has_languages = false;
		start = 0;
		end = 0;

		for (int i = 0; i < full.length(); i++) {
			if (full.charAt(i) == '%') {
				if (full.charAt(i + 1) == '-') {
					if (start == 0) {
						int eol = full.indexOf('\n', i + 2);
						String temp = full.substring(i + 2, eol);
						if (temp.compareTo(name) == 0)
							start = eol;

						//has_names = true;
					} else if (end == 0) {
						end = i - 1;
					}
				}

				//if( full.charAt( i+1 ) == '=' )
				//has_languages = true;
			}
		}

		if (end == 0)
			end = full.length();

		if (start == 0)
			return full;

		return full.substring(start, end);
	}

	public String getQnxUsage() throws IOException {

		loadSections();

		for (int i = 0; i < sections.length; i++) {
			if (sections[i].toString().compareTo("QNX_usage") == 0) { //$NON-NLS-1$
				File file = new File(elf.getFilename());

				String full_usage = new String(sections[i].loadSectionData());
				String usage = getSubUsage(full_usage, file.getName());
				StringBuffer buffer = new StringBuffer(usage);

				for (int j = 0; j < buffer.length(); j++) {
					if (buffer.charAt(j) == '%') {
						if (buffer.charAt(j + 1) == 'C')
							buffer.replace(j, j + 2, file.getName());
					}
				}

				return buffer.toString();
			}
		}
		return new String(""); //$NON-NLS-1$
	}

	public Sizes getSizes() throws IOException {
		long text, data, bss;

		text = 0;
		data = 0;
		bss = 0;

		loadSections();

		for (int i = 0; i < sections.length; i++) {
			if (sections[i].sh_type != Elf.Section.SHT_NOBITS) {
				if (sections[i].sh_flags == (Elf.Section.SHF_WRITE | Elf.Section.SHF_ALLOC)) {
					data += sections[i].sh_size;
				} else if ( (sections[i].sh_flags & Elf.Section.SHF_ALLOC) != 0) {
					text += sections[i].sh_size;
				}
			} else {
				if (sections[i].sh_flags == (Elf.Section.SHF_WRITE | Elf.Section.SHF_ALLOC)) {
					bss += sections[i].sh_size;
				}
			}
		}

		return new Sizes(text, data, bss);
	}

}
