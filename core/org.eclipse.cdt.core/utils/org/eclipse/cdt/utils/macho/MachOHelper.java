/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Craig Watson
 *     Apple Computer - work on performance optimizations
 *******************************************************************************/
package org.eclipse.cdt.utils.macho;

import java.io.IOException;
import java.util.Vector;

import org.eclipse.cdt.utils.macho.MachO.DyLib;
import org.eclipse.cdt.utils.macho.MachO.Section;
import org.eclipse.cdt.utils.macho.MachO.Symbol;

/**
 * @deprecated. Deprecated as of CDT 6.1. Use 64 bit version {@link MachOHelper64}.
 * This class is planned for removal in next major release.
 * <br>
 *  <code>MachOHelper</code> is a wrapper class for the <code>MachO</code> class
 *  to provide higher level API for sorting/searching the MachO data.
 *
 *  @see MachO
 */
@Deprecated
public class MachOHelper {

	private MachO macho;
	private MachO.Symbol[] dynsyms;
	private MachO.Symbol[] symbols;
	private MachO.Section[] sections;
	private MachO.DyLib[] needed;
	private MachO.DyLib[] sonames;

	public void dispose() {
		if (macho != null) {
			macho.dispose();
			macho = null;
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

	private void loadBinary() throws IOException {
		if (symbols == null) {
			macho.loadBinary();
			symbols = macho.getSymtabSymbols();
			dynsyms = macho.getDynamicSymbols();
			sections = macho.getSections();
			needed = macho.getDyLibs(MachO.LoadCommand.LC_LOAD_DYLIB);
			sonames = macho.getDyLibs(MachO.LoadCommand.LC_ID_DYLIB);

			if (dynsyms == null)
				dynsyms = symbols;
		}
	}

	/**
	 * Create a new <code>MachOHelper</code> using an existing <code>MachO</code>
	 * object.
	 * @param macho An existing MachO object to wrap.
	 * @throws IOException Error processing the MachO file.
	 */
	public MachOHelper(MachO macho) throws IOException {
		this.macho = macho;
	}

	/**
	 * Create a new <code>MachOHelper</code> based on the given filename.
	 *
	 * @param filename The file to use for creating a new MachO object.
	 * @throws IOException Error processing the MachO file.
	 * @see MachO#MachO( String )
	 */
	public MachOHelper(String filename) throws IOException {
		macho = new MachO(filename);
	}

	/**
	 * Create a new <code>MachOHelper</code> based on the given filename.
	 *
	 * @param filename The file to use for creating a new MachO object.
	 * @throws IOException Error processing the MachO file.
	 * @see MachO#MachO( String )
	 */
	public MachOHelper(String filename, long offset) throws IOException {
		macho = new MachO(filename, offset);
	}

	public MachOHelper(String filename, boolean filton) throws IOException {
		macho = new MachO(filename, filton);
	}

	/** Give back the MachO object that this helper is wrapping */
	public MachO getMachO() {
		return macho;
	}

	public MachO.Symbol[] getExternalFunctions() throws IOException {
		Vector<Symbol> v = new Vector<>();

		loadBinary();

		for (Symbol sym : dynsyms) {
			if ((sym.n_type_mask(MachO.Symbol.N_PEXT) || sym.n_type_mask(MachO.Symbol.N_EXT))
					&& sym.n_desc(MachO.Symbol.REFERENCE_FLAG_UNDEFINED_LAZY)) {
				String name = sym.toString();
				if (name != null && name.trim().length() > 0)
					v.add(sym);
			}
		}

		MachO.Symbol[] ret = v.toArray(new MachO.Symbol[0]);
		return ret;
	}

	public MachO.Symbol[] getExternalObjects() throws IOException {
		Vector<Symbol> v = new Vector<>();

		loadBinary();

		for (Symbol sym : dynsyms) {
			if ((sym.n_type_mask(MachO.Symbol.N_PEXT) || sym.n_type_mask(MachO.Symbol.N_EXT))
					&& sym.n_desc(MachO.Symbol.REFERENCE_FLAG_UNDEFINED_NON_LAZY)) {
				String name = sym.toString();
				if (name != null && name.trim().length() > 0)
					v.add(sym);
			}
		}

		MachO.Symbol[] ret = v.toArray(new MachO.Symbol[0]);
		return ret;
	}

	public MachO.Symbol[] getUndefined() throws IOException {
		Vector<Symbol> v = new Vector<>();

		loadBinary();

		for (Symbol dynsym : dynsyms) {
			if (dynsym.n_type(MachO.Symbol.N_UNDF))
				v.add(dynsym);
		}

		MachO.Symbol[] ret = v.toArray(new MachO.Symbol[0]);
		return ret;
	}

	/*
	 * TODO: I'm not sure if this are correct. Need to check
	 */
	public MachO.Symbol[] getLocalFunctions() throws IOException {
		Vector<Symbol> v = new Vector<>();

		loadBinary();

		for (Symbol sym : dynsyms) {
			if ((!sym.n_type_mask(MachO.Symbol.N_PEXT) && !sym.n_type_mask(MachO.Symbol.N_EXT))
					&& sym.n_desc(MachO.Symbol.REFERENCE_FLAG_PRIVATE_UNDEFINED_LAZY)) {
				String name = sym.toString();
				if (name != null && name.trim().length() > 0)
					v.add(sym);
			}
		}

		MachO.Symbol[] ret = v.toArray(new MachO.Symbol[0]);
		return ret;
	}

	/*
	 * TODO: I'm not sure if this are correct. Need to check
	 */
	public MachO.Symbol[] getLocalObjects() throws IOException {
		Vector<Symbol> v = new Vector<>();

		loadBinary();

		for (Symbol sym : dynsyms) {
			if ((!sym.n_type_mask(MachO.Symbol.N_PEXT) && !sym.n_type_mask(MachO.Symbol.N_EXT))
					&& sym.n_desc(MachO.Symbol.REFERENCE_FLAG_PRIVATE_UNDEFINED_NON_LAZY)) {
				String name = sym.toString();
				if (name != null && name.trim().length() > 0)
					v.add(sym);
			}
		}

		MachO.Symbol[] ret = v.toArray(new MachO.Symbol[0]);
		return ret;
	}

	public MachO.Symbol[] getCommonObjects() throws IOException {
		Vector<Symbol> v = new Vector<>();

		loadBinary();

		for (int i = 0; i < dynsyms.length; i++) {
			MachO.Symbol sym = dynsyms[i];
			if (sym.n_type_mask(MachO.Symbol.N_EXT) && sym.n_type(MachO.Symbol.N_UNDF) && sym.n_value != 0) {
				v.add(symbols[i]);
			}
		}

		MachO.Symbol[] ret = v.toArray(new MachO.Symbol[0]);
		return ret;
	}

	public String[] getNeeded() throws IOException {
		Vector<String> v = new Vector<>();

		loadBinary();

		for (DyLib element : needed) {
			v.add(element.toString());
		}
		return v.toArray(new String[0]);
	}

	public String getSoname() throws IOException {
		String soname = ""; //$NON-NLS-1$

		loadBinary();

		for (DyLib soname2 : sonames) {
			soname = soname2.toString();
		}
		return soname;
	}

	//	private String getSubUsage(String full, String name) {
	//		int start, end;
	//		//boolean has_names = false;
	//		//boolean has_languages = false;
	//		start = 0;
	//		end = 0;
	//
	//		for (int i = 0; i < full.length(); i++) {
	//			if (full.charAt(i) == '%') {
	//				if (full.charAt(i + 1) == '-') {
	//					if (start == 0) {
	//						int eol = full.indexOf('\n', i + 2);
	//						String temp = full.substring(i + 2, eol);
	//						if (temp.compareTo(name) == 0)
	//							start = eol;
	//
	//						//has_names = true;
	//					} else if (end == 0) {
	//						end = i - 1;
	//					}
	//				}
	//
	//				//if( full.charAt( i+1 ) == '=' )
	//				//has_languages = true;
	//			}
	//		}
	//
	//		if (end == 0)
	//			end = full.length();
	//
	//		if (start == 0)
	//			return full;
	//
	//		return full.substring(start, end);
	//	}

	public String getQnxUsage() throws IOException {
		return ""; //$NON-NLS-1$
	}

	public Sizes getSizes() throws IOException {
		long text, data, bss;

		text = 0;
		data = 0;
		bss = 0;

		// TODO further optimization
		// TODO we only need to load the sections, not the whole shebang
		loadBinary();

		for (Section section : sections) {
			MachO.SegmentCommand seg = section.segment;
			if (section.flags(MachO.Section.SECTION_TYP) != MachO.Section.S_ZEROFILL) {
				if (seg.prot(MachO.SegmentCommand.VM_PROT_EXECUTE)) {
					text += section.size;
				} else if (!seg.prot(MachO.SegmentCommand.VM_PROT_WRITE)) {
					data += section.size;
				}
			} else {
				if (seg.prot(MachO.SegmentCommand.VM_PROT_WRITE)) {
					bss += section.size;
				}
			}
		}

		return new Sizes(text, data, bss);
	}

}
