/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.coff;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;

public class Coff {

	public static final String NL = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	FileHeader filehdr;
	OptionalHeader opthdr;
	RandomAccessFile rfile;
	long startingOffset;
	byte[] string_table;
	SectionHeader[] scnhdrs;
	Symbol[] symbols;
	
	public static class FileHeader {
		public final static int FILHSZ = 20;

		public final static int F_RELFLG = 0x0001; // relocation info stripped from file
		public final static int F_EXEC = 0x0002;   // file is executable
							   //  (no unresolved external references)
		public final static int F_LNNO = 0x0004;   // line numbers stripped from file
		public final static int F_LSYMS = 0x0008;  // local symbols stripped from file
		public final static int F_AR16WR = 0x0080; // file is 16-bit little-endian
		public final static int F_AR32WR = 0x0100; // file is 32-bit little-endian
		public final static int F_AR32W = 0x0200;  // file is 32-bit big-endian
		public final static int F_DYNLOAD = 0x1000;// rs/6000 aix: dynamically
							   // loadable w/imports & exports
		public final static int F_SHROBJ = 0x2000; // rs/6000 aix: file is a shared object
		public final static int F_DLL = 0x2000;    // PE format DLL.

		public int f_magic;   /* 00-01 2 bytes: magic number                 */
		public int f_nscns;   /* 02-03 2 bytes: number of sections: 2 bytes  */
		public int f_timdat;  /* 04-07 4 bytes: time & date stamp            */
		public int f_symptr;  /* 08-11 4 bytes: file pointer to symtab       */
		public int f_nsyms;   /* 12-15 4 bytes: number of symtab entries     */
		public int f_opthdr;  /* 16-17 2 bytes: sizeof(optional hdr)         */
		public int f_flags;   /* 18-19 2 bytes: flags                        */

		public FileHeader (RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public FileHeader (RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] hdr = new byte[FILHSZ];
			file.readFully(hdr);
			commonSetup(hdr, true);
		}

		public FileHeader (byte[] hdr, boolean little) throws EOFException {
			commonSetup(hdr, little);
		}

		public void commonSetup(byte[] hdr, boolean little) throws EOFException {
			if (hdr == null || hdr.length < FILHSZ) {
				throw new EOFException(CCorePlugin.getResourceString("Util.exception.arrayToSmall")); //$NON-NLS-1$
			}
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, little);
			f_magic = memory.getUnsignedShort();
			f_nscns = memory.getUnsignedShort();
			f_timdat = memory.getInt();
			f_symptr = memory.getInt();
			f_nsyms = memory.getInt();
			f_opthdr = memory.getUnsignedShort();
			f_flags = memory.getUnsignedShort();
		}

		public boolean isStrip() {
			return (f_flags & F_RELFLG) == F_RELFLG;
		}

		public boolean isExec() {
			return (f_flags & F_EXEC) == F_EXEC;
		}

		public boolean isDebug() {
			return !((f_flags & F_LNNO) == F_LNNO);
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("FILE HEADER VALUES").append(NL); //$NON-NLS-1$

			buffer.append("f_magic = ").append(f_magic).append(NL); //$NON-NLS-1$
			buffer.append("f_nscns = ").append(f_nscns).append(NL); //$NON-NLS-1$

			buffer.append("f_timdat = "); //$NON-NLS-1$
			buffer.append(DateFormat.getDateInstance().format(new Date(f_timdat)));
			buffer.append(NL);

			buffer.append("f_symptr = ").append(f_symptr).append(NL); //$NON-NLS-1$
			buffer.append("f_nsyms = ").append(f_nsyms).append(NL); //$NON-NLS-1$
			buffer.append("f_opthdr = ").append(f_opthdr).append(NL); //$NON-NLS-1$
			buffer.append("f_flags = ").append(f_flags).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}
	}

	public static class OptionalHeader {
		public final static int AOUTHDRSZ = 28;

		public short magic;    /* 2 bytes: type of file                         */
		public short vstamp;   /* 2 bytes: version stamp                        */
		public int tsize;      /* 4 bytes: text size in bytes, padded to FW bdry*/
		public int dsize;      /* 4 bytes: initialized data "  "                */
		public int bsize;      /* 4 bytes: uninitialized data "   "             */
		public int entry;      /* 4 bytes: entry pt.                            */
		public int text_start; /* 4 bytes: base of text used for this file      */
		public int data_start; /* 4 bytes: base of data used for this file      */

		public OptionalHeader(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer() + FileHeader.FILHSZ);
		}

		public OptionalHeader(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] hdr = new byte[AOUTHDRSZ];
			file.readFully(hdr);
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, true);
			magic = memory.getShort();
			vstamp = memory.getShort();
			tsize = memory.getInt();
			dsize = memory.getInt();
			bsize = memory.getInt();
			entry = memory.getInt();
			text_start = memory.getInt();
			data_start = memory.getInt();
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("OPTIONAL HEADER VALUES").append(NL); //$NON-NLS-1$
			buffer.append("magic      = ").append(magic).append(NL); //$NON-NLS-1$
			buffer.append("vstamp     = ").append(vstamp).append(NL); //$NON-NLS-1$
			buffer.append("tsize      = ").append(tsize).append(NL); //$NON-NLS-1$
			buffer.append("dsize      = ").append(dsize).append(NL); //$NON-NLS-1$
			buffer.append("bsize      = ").append(bsize).append(NL); //$NON-NLS-1$
			buffer.append("entry      = ").append(entry).append(NL); //$NON-NLS-1$
			buffer.append("text_start = ").append(text_start).append(NL); //$NON-NLS-1$
			buffer.append("data_start = ").append(data_start).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}
	}

	public static class SectionHeader {

		public final static int SCNHSZ = 40;

		/* names of "special" sections */
		public final static String _TEXT = ".text"; //$NON-NLS-1$
		public final static String _DATA = ".data"; //$NON-NLS-1$
		public final static String _BSS = ".bss"; //$NON-NLS-1$
		public final static String _COMMENT = ".comment"; //$NON-NLS-1$
		public final static String _LIB = ".lib"; //$NON-NLS-1$

		/* s_flags "type".  */
		public final static int STYP_REG = 0x0000;    /* "regular": allocated, relocated,
								loaded */
		public final static int STYP_DSECT = 0x0001;  /* "dummy":  relocated only */
		public final static int STYP_NOLOAD = 0x0002; /* "noload": allocated, relocated,
								not loaded */
		public final static int STYP_GROUP = 0x0004;  /* "grouped": formed of input
								sections */
		public final static int STYP_PAD = 0x0008;    /* "padding": not allocated, not
								relocated, loaded */
		public final static int STYP_COPY = 0x0010;   /* "copy": for decision function
								used by field update;
								not allocated, not relocated,
								loaded; reloc & lineno entries
								processed normally */
		public final static int STYP_TEXT = 0x0020;   /* section contains text only.  */
		public final static int S_SHRSEG = 0x0020;    /* In 3b Update files (output of
								ogen), sections which appear in
								SHARED segments of the Pfile
								will have the S_SHRSEG flag set
								by ogen, to inform dufr that
								updating 1 copy of the proc. will
								update all process invocations. */
		public final static int STYP_DATA = 0x0040;   /* section contains data only */
		public final static int STYP_BSS = 0x0080;    /* section contains bss only */
		public final static int S_NEWFCN = 0x0100;    /* In a minimal file or an update
								file, a new function (as
								compared with a replaced
								function) */
		public final static int STYP_INFO = 0x0200;   /* comment: not allocated not
								relocated, not loaded */
		public final static int STYP_OVER = 0x0400;   /* overlay: relocated not allocated
								or loaded */
		public final static int STYP_LIB = 0x0800;    /* for .lib: same as INFO */
		public final static int STYP_MERGE = 0x2000;  /* merge section -- combines with
								text, data or bss sections only */
		public final static int STYP_REVERSE_PAD = 0x4000; /* section will be padded
								with no-op instructions
								wherever padding is necessary
								and there is a word of
								contiguous bytes beginning on a
								word boundary. */

		public final static int STYP_LIT = 0x8020;  /* Literal data (like STYP_TEXT) */


		public byte[] s_name= new byte[8]; // 8 bytes: section name
		public int s_paddr;    // 4 bytes: physical address, aliased s_nlib
		public int s_vaddr;    // 4 bytes: virtual address
		public int s_size;     // 4 bytes: section size
		public int s_scnptr;   // 4 bytes: file ptr to raw data for section
		public int s_relptr;   // 4 bytes: file ptr to relocation
		public int s_lnnoptr;  // 4 bytes: file ptr to line numbers
		public int s_nreloc;   // 2 bytes: number of relocation entries
		public int s_nlnno;    // 2 bytes: number of line number entries
		public int s_flags;    // 4 bytes: flags

		RandomAccessFile sfile;

		public SectionHeader(RandomAccessFile file, long offset) throws IOException {
			sfile = file;
			file.seek(offset);
			byte[] hdr = new byte[SCNHSZ];
			file.readFully(hdr);
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, true);
			memory.getBytes(s_name);
			s_paddr = memory.getInt();
			s_vaddr = memory.getInt();
			s_size = memory.getInt();
			s_scnptr = memory.getInt();
			s_relptr = memory.getInt();
			s_lnnoptr = memory.getInt();
			s_nreloc = memory.getUnsignedShort();
			s_nlnno = memory.getUnsignedShort();
			s_flags = memory.getInt();
		}

		public byte[] getRawData() throws IOException {
			byte[] data = new byte[s_size];
			sfile.seek(s_scnptr);
			sfile.readFully(data);
			return data;
		}

		public Reloc[] getRelocs() throws IOException {
			Reloc[] relocs = new Reloc[s_nreloc];
			sfile.seek(s_relptr);
			for (int i = 0; i < s_nreloc; i++) {
				relocs[i] = new Reloc(sfile);
			}
			return relocs;
		}

		public Lineno[] getLinenos() throws IOException {
			Lineno[] lines = new Lineno[s_nlnno];
			sfile.seek(s_lnnoptr);
			for (int i = 0; i < s_nlnno; i++) {
				lines[i] = new Lineno(sfile);
			}
			return lines;
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("SECTION HEADER VALUES").append(NL); //$NON-NLS-1$
			buffer.append(new String(s_name)).append(NL);
			buffer.append("s_paddr = ").append(s_paddr).append(NL); //$NON-NLS-1$
			buffer.append("s_vaddr = ").append(s_vaddr).append(NL); //$NON-NLS-1$
			buffer.append("s_size = ").append(s_size).append(NL); //$NON-NLS-1$
			buffer.append("s_scnptr = ").append(s_scnptr).append(NL); //$NON-NLS-1$
			buffer.append("s_relptr = ").append(s_relptr).append(NL); //$NON-NLS-1$
			buffer.append("s_lnnoptr = ").append(s_lnnoptr).append(NL); //$NON-NLS-1$
			buffer.append("s_nreloc = ").append(s_nreloc).append(NL); //$NON-NLS-1$
			buffer.append("s_nlnno = ").append(s_nlnno).append(NL); //$NON-NLS-1$
			buffer.append("s_flags = ").append(s_flags).append(NL); //$NON-NLS-1$
///*
			try {
				Reloc[] rcs = getRelocs();
				for (int i = 0; i < rcs.length; i++) {
					buffer.append(rcs[i]);
				}
			} catch (IOException e) {
			}
			try {
				Lineno[] nos = getLinenos();
				for (int i = 0; i < nos.length; i++) { 
					buffer.append(nos[i]);
				}
			} catch (IOException e) {
			}
//*/
			return buffer.toString();
		}
	}

	public static class Reloc {
		public static final int RELSZ = 16;
		public int r_vaddr; /* 4 byte: Pointer to an area in raw data that represents a
				      referenced address. */
		public int r_symndx; /* 4 byte: Index into symbol table.  */
		public int r_type;  /* 2 byte(unsigned short): Type of address reference.  */

		public Reloc(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public Reloc(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] bytes = new byte[RELSZ];
			file.readFully(bytes);
			ReadMemoryAccess memory = new ReadMemoryAccess(bytes, true);
			r_vaddr = memory.getInt();
			r_symndx = memory.getInt();
			r_type = memory.getUnsignedShort();
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("RELOC VALUES").append(NL); //$NON-NLS-1$
			buffer.append("r_vaddr = ").append(r_vaddr); //$NON-NLS-1$
			buffer.append(" r_symndx = ").append(r_symndx).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}
	}

	public static class Lineno {
		public final static int LINESZ = 6;
		public int l_addr; /* long. Index into symbol table if l_linn0 == 0.
			              Break-pointable address if l_lnno > 0.  */
		public int l_lnno; /* unsigned short. Line number  */

		public Lineno(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public Lineno(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] bytes = new byte[LINESZ];
			file.readFully(bytes);
			ReadMemoryAccess memory = new ReadMemoryAccess(bytes, true);
			l_addr = memory.getInt();
			l_lnno = memory.getUnsignedShort();
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			if (l_lnno == 0) {
				buffer.append("Function address = ").append(l_addr).append(NL); //$NON-NLS-1$
			} else {
				buffer.append("line# ").append(l_lnno); //$NON-NLS-1$
				buffer.append(" at address = ").append(l_addr).append(NL); //$NON-NLS-1$
			}
			return buffer.toString();
		}
	}

	public static class Symbol {
		public final static int SYMSZ = 18;
		public final static int SYMNMLEN = 8;

		/* Derived types, in n_type.  */
		public final static int DT_NON = 0;     /* no derived type */
		public final static int DT_PTR = 1;     /* pointer */
		public final static int DT_FCN = 2;     /* function */
		public final static int DT_ARY = 3;     /* array */

		public final static int N_TMASK  = 0x30;
		public final static int N_BTSHFT = 4;
		public final static int N_TSHIFT = 2;


		public byte[] _n_name = new byte[SYMNMLEN]; /* Symbol name, or pointer into
								string table if symbol name
								is greater than SYMNMLEN.  */
		public int n_value; /* long. Symbol;s value: dependent on section number,
				       storage class and type.  */
		public short n_scnum; /* short, Section number.  */
		public int n_type;   /* Unsigned short. Symbolic type.  */
		public byte n_sclass; /* char, Storage class.  */
		public byte n_numaux; /* char. Nuymber of auxiliary enties.  */

		public Symbol(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public Symbol(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] bytes = new byte[SYMSZ];
			file.readFully(bytes);
			ReadMemoryAccess memory = new ReadMemoryAccess(bytes, true);
			memory.getBytes(_n_name);
			n_value = memory.getInt();
			n_scnum = memory.getShort();
			n_type = memory.getUnsignedShort();
			n_sclass = memory.getByte();
			n_numaux = memory.getByte();
		}

		public boolean isLongName() {
			return (_n_name[0] == 0);
		}

		public String getName() {
			for (int i = 0; i < _n_name.length; i++) {
				if (_n_name[i] == 0) {
					return new String(_n_name, 0, i);
				}
			}
			return ""; //$NON-NLS-1$
		}

		public String getName(byte[] table) {
			if (table.length > 0 && isLongName()) {
				ReadMemoryAccess memory = new ReadMemoryAccess(_n_name, true);
				memory.getInt(); // pass over the first 4 bytes.
				// The first for bytes of the string table represent the
				// number of bytes in the string table.
				int offset = memory.getInt() - 4;
				if (offset > 0) {
					for (int i = offset; i < table.length; i++) {
						if (table[i] == 0) {
							return new String(table, offset, i - offset);
						}
					}
				}
			}
			return getName();
		}

		public boolean isPointer() {
			return (n_type & N_TMASK) == (DT_PTR << N_BTSHFT);
		}

		public boolean isFunction() {
			return (n_type & N_TMASK) == (DT_FCN << N_BTSHFT);
		}

		public boolean isArray() {
			return (n_type & N_TMASK) == (DT_ARY << N_BTSHFT);
		}

		public String toString() {
			return getName();
		}

	}

	public FileHeader getFileHeader() throws IOException {
		return filehdr;
	}
	
	public OptionalHeader getOptionalHeader() throws IOException {
		return opthdr;
	}

	public SectionHeader[] getSectionHeaders() throws IOException {
		if (scnhdrs == null) {
			scnhdrs = new SectionHeader[getFileHeader().f_nscns];
			long sec = FileHeader.FILHSZ + getFileHeader().f_opthdr;
			for (int i = 0; i < scnhdrs.length; i++, sec += SectionHeader.SCNHSZ) {
				scnhdrs[i] = new SectionHeader(rfile, sec);
			}
		}
		return scnhdrs;
	}

	public Symbol[] getSymbols() throws IOException {
		if (symbols == null) {
			long offset = getFileHeader().f_symptr;
			rfile.seek(offset);
			symbols = new Symbol[getFileHeader().f_nsyms];
			for (int i = 0; i < symbols.length; i++) {
				symbols[i] = new Symbol(rfile); 
			}
		}
		return symbols;
	}

	public byte[] getStringTable() throws IOException {
		if (string_table == null) {
			long symbolsize = Symbol.SYMSZ * getFileHeader().f_nsyms;
			long offset = getFileHeader().f_symptr + symbolsize;
			rfile.seek(offset);
			byte[] bytes = new byte[4];
			rfile.readFully(bytes);
			int str_len = ReadMemoryAccess.getIntLE(bytes);
			if (str_len > 4 && str_len < rfile.length()) {
				str_len -= 4;
				string_table = new byte[str_len];
				rfile.seek(offset + 4);
				rfile.readFully(string_table);
			} else {
				string_table = new byte[0];
			}
		}
		return string_table;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		try {
			FileHeader header = null;
			header = getFileHeader();
			if (header != null) {
				buffer.append(header);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			OptionalHeader opt = null;
			opt = getOptionalHeader();
			if (opt != null) {
				buffer.append(opt);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			SectionHeader[] sections = getSectionHeaders();
			for (int i = 0; i < sections.length; i++) {
				buffer.append(sections[i]);
			}
		} catch (IOException e) {
		}

		try {
			Symbol[] table = getSymbols();
			for (int i = 0; i < table.length; i++) {
				buffer.append(table[i].getName(getStringTable())).append(NL);
			}
		} catch (IOException e) {
		}

//		try {
//			String[] strings = getStringTable(getStringTable());
//			for (int i = 0; i < strings.length; i++) {
//				buffer.append(strings[i]);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return buffer.toString();
	}

	public static String[] getStringTable(byte[] bytes) {
		List aList = new ArrayList();
		int offset = 0;
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 0) {
				aList.add(new String(bytes, offset, i - offset));
				offset = i + 1;
			}
		}
		return (String[])aList.toArray(new String[0]);
	}

	public Coff(String filename) throws IOException {
		this(new RandomAccessFile(filename, "r"), 0); //$NON-NLS-1$
	}

	public Coff(RandomAccessFile file, long offset) throws IOException {
		commonSetup(file, offset);
	}

	void commonSetup(RandomAccessFile file, long offset) throws IOException {
		startingOffset = offset;
		rfile = file;
		try {
			filehdr = new FileHeader(rfile, offset);
			if (filehdr.f_opthdr > 0) {
				opthdr = new OptionalHeader(rfile, startingOffset + 20);
			}
		} finally {
			if (filehdr == null) {
				rfile.close();
			}
		}
	}

	public static void main(String[] args) {
		try {
			Coff coff = new Coff(args[0]);
			System.out.println(coff);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
