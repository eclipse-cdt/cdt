package org.eclipse.cdt.utils.macho;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

/**
 *  <code>MachOHelper</code> is a wrapper class for the <code>MachO</code> class
 *  to provide higher level API for sorting/searching the MachO data.
 *
 *  @see MachO
 */
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
		Vector v = new Vector();

		loadBinary();

		for (int i = 0; i < dynsyms.length; i++) {
			MachO.Symbol sym = dynsyms[i];
			if ((sym.n_type_mask(MachO.Symbol.N_PEXT) 
					|| sym.n_type_mask(MachO.Symbol.N_EXT))
					&& sym.n_desc(MachO.Symbol.REFERENCE_FLAG_UNDEFINED_LAZY)) {
				String name = sym.toString();
				if (name != null && name.trim().length() > 0)
					v.add(sym);
			}
		}

		MachO.Symbol[] ret = (MachO.Symbol[]) v.toArray(new MachO.Symbol[0]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	public MachO.Symbol[] getExternalObjects() throws IOException {
		Vector v = new Vector();

		loadBinary();

		for (int i = 0; i < dynsyms.length; i++) {
			MachO.Symbol sym = dynsyms[i];
			if ((sym.n_type_mask(MachO.Symbol.N_PEXT) 
					|| sym.n_type_mask(MachO.Symbol.N_EXT))
					&& sym.n_desc(MachO.Symbol.REFERENCE_FLAG_UNDEFINED_NON_LAZY)) {
				String name = sym.toString();
				if (name != null && name.trim().length() > 0)
					v.add(sym);
			}
		}

		MachO.Symbol[] ret = (MachO.Symbol[]) v.toArray(new MachO.Symbol[0]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	public MachO.Symbol[] getUndefined() throws IOException {
		Vector v = new Vector();

		loadBinary();

		for (int i = 0; i < dynsyms.length; i++) {
			if (dynsyms[i].n_type(MachO.Symbol.N_UNDF))
				v.add(dynsyms[i]);
		}

		MachO.Symbol[] ret = (MachO.Symbol[]) v.toArray(new MachO.Symbol[0]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	/*
	 * TODO: I'm not sure if this are correct. Need to check
	 */
	public MachO.Symbol[] getLocalFunctions() throws IOException {
		Vector v = new Vector();

		loadBinary();

		for (int i = 0; i < dynsyms.length; i++) {
			MachO.Symbol sym = dynsyms[i];
			if ((!sym.n_type_mask(MachO.Symbol.N_PEXT) 
					&& !sym.n_type_mask(MachO.Symbol.N_EXT))
					&& sym.n_desc(MachO.Symbol.REFERENCE_FLAG_PRIVATE_UNDEFINED_LAZY)) {
				String name = sym.toString();
				if (name != null && name.trim().length() > 0)
					v.add(sym);
			}
		}

		MachO.Symbol[] ret = (MachO.Symbol[]) v.toArray(new MachO.Symbol[0]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	/*
	 * TODO: I'm not sure if this are correct. Need to check
	 */
	public MachO.Symbol[] getLocalObjects() throws IOException {
		Vector v = new Vector();

		loadBinary();

		for (int i = 0; i < dynsyms.length; i++) {
			MachO.Symbol sym = dynsyms[i];
			if ((!sym.n_type_mask(MachO.Symbol.N_PEXT) 
					&& !sym.n_type_mask(MachO.Symbol.N_EXT))
					&& sym.n_desc(MachO.Symbol.REFERENCE_FLAG_PRIVATE_UNDEFINED_NON_LAZY)) {
				String name = sym.toString();
				if (name != null && name.trim().length() > 0)
					v.add(sym);
			}
		}

		MachO.Symbol[] ret = (MachO.Symbol[]) v.toArray(new MachO.Symbol[0]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	public MachO.Symbol[] getCommonObjects() throws IOException {
		Vector v = new Vector();

		loadBinary();

		for (int i = 0; i < dynsyms.length; i++) {
			MachO.Symbol sym = dynsyms[i];
			if (sym.n_type_mask(MachO.Symbol.N_EXT) 
					&& sym.n_type(MachO.Symbol.N_UNDF)
					&& sym.n_value != 0) {
				v.add(symbols[i]);
			}
		}

		MachO.Symbol[] ret = (MachO.Symbol[]) v.toArray(new MachO.Symbol[0]);
		Arrays.sort(ret, new SymbolSortCompare());
		return ret;
	}

	public String[] getNeeded() throws IOException {
		Vector v = new Vector();

		loadBinary();

		for (int i = 0; i < needed.length; i++) {
			v.add(needed[i].toString());
		}
		return (String[]) v.toArray(new String[0]);
	}

	public String getSoname() throws IOException {
		String soname = ""; //$NON-NLS-1$

		loadBinary();

		for (int i = 0; i < sonames.length; i++) {
			soname = sonames[i].toString();
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
		return new String(""); //$NON-NLS-1$
	}

	public Sizes getSizes() throws IOException {
		long text, data, bss;

		text = 0;
		data = 0;
		bss = 0;

		loadBinary();

		for (int i = 0; i < sections.length; i++) {
			MachO.SegmentCommand seg = sections[i].segment;
			if (sections[i].flags(MachO.Section.SECTION_TYP) != MachO.Section.S_ZEROFILL) {
				if (seg.prot(MachO.SegmentCommand.VM_PROT_EXECUTE)) {
					text += sections[i].size;
				} else if (!seg.prot(MachO.SegmentCommand.VM_PROT_WRITE)) {
					data += sections[i].size;
				}
			} else {
				if (seg.prot(MachO.SegmentCommand.VM_PROT_WRITE)) {
					bss += sections[i].size;
				}
			}
		}

		return new Sizes(text, data, bss);
	}

}
