/*******************************************************************************
 * Copyright (c) 2000, 2019 Space Codesign Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Space Codesign Systems - Initial API and implementation
 *     QNX Software Systems - Initial Coff class
 *******************************************************************************/
package org.eclipse.cdt.utils.coff;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;

import com.ibm.icu.text.DateFormat;

/**
 * @since 6.9
 */
public class Coff64 {

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
		public final static int F_EXEC = 0x0002; // file is executable
		//  (no unresolved external references)
		public final static int F_LNNO = 0x0004; // line numbers stripped from file
		public final static int F_LSYMS = 0x0008; // local symbols stripped from file
		public final static int F_AR16WR = 0x0080; // file is 16-bit little-endian
		public final static int F_AR32WR = 0x0100; // file is 32-bit little-endian
		public final static int F_AR32W = 0x0200; // file is 32-bit big-endian
		public final static int F_DYNLOAD = 0x1000;// rs/6000 aix: dynamically
		// loadable w/imports & exports
		public final static int F_SHROBJ = 0x2000; // rs/6000 aix: file is a shared object
		public final static int F_DLL = 0x2000; // PE format DLL.

		public int f_magic; /* 00-01 2 bytes: magic number                 */
		public int f_nscns; /* 02-03 2 bytes: number of sections: 2 bytes  */
		public int f_timdat; /* 04-07 4 bytes: time & date stamp            */
		public int f_symptr; /* 08-11 4 bytes: file pointer to symtab       */
		public int f_nsyms; /* 12-15 4 bytes: number of symtab entries     */
		public int f_opthdr; /* 16-17 2 bytes: sizeof(optional hdr)         */
		public int f_flags; /* 18-19 2 bytes: flags                        */

		public FileHeader(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public FileHeader(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] hdr = new byte[FILHSZ];
			file.readFully(hdr);
			commonSetup(hdr, true);
		}

		public FileHeader(byte[] hdr, boolean little) throws EOFException {
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

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
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
		public short magic; /* 2 bytes: type of file */
		public OptionalHeader64 optionalHeader64;
		public OptionalHeader32 optionalHeader32;

		private final static int MAGICSZ = 2;
		private boolean is64Bits;

		public static class OptionalHeader64 {
			public final static int AOUTHDRSZ = 22;

			public short vstamp; /* 2 bytes: version stamp                        */
			public int tsize; /* 4 bytes: text size in bytes, padded to FW bdry*/
			public int dsize; /* 4 bytes: initialized data "  "                */
			public int bsize; /* 4 bytes: uninitialized data "   "             */
			public int entry; /* 4 bytes: entry pt.                            */
			public int text_start; /* 4 bytes: base of text used for this file      */

			public OptionalHeader64(RandomAccessFile file, long offset) throws IOException {
				file.seek(offset);
				byte[] hdr = new byte[AOUTHDRSZ];
				file.readFully(hdr);
				ReadMemoryAccess memory = new ReadMemoryAccess(hdr, true);
				vstamp = memory.getShort();
				tsize = memory.getInt();
				dsize = memory.getInt();
				bsize = memory.getInt();
				entry = memory.getInt();
				text_start = memory.getInt();
			}

			@Override
			public String toString() {
				StringBuilder buffer = new StringBuilder();
				buffer.append("vstamp     = ").append(vstamp).append(NL); //$NON-NLS-1$
				buffer.append("tsize      = ").append(tsize).append(NL); //$NON-NLS-1$
				buffer.append("dsize      = ").append(dsize).append(NL); //$NON-NLS-1$
				buffer.append("bsize      = ").append(bsize).append(NL); //$NON-NLS-1$
				buffer.append("entry      = ").append(entry).append(NL); //$NON-NLS-1$
				buffer.append("text_start = ").append(text_start).append(NL); //$NON-NLS-1$
				return buffer.toString();
			}
		}

		public static class OptionalHeader32 {
			public final static int AOUTHDRSZ = 26;

			public short vstamp; /* 2 bytes: version stamp                        */
			public int tsize; /* 4 bytes: text size in bytes, padded to FW bdry*/
			public int dsize; /* 4 bytes: initialized data "  "                */
			public int bsize; /* 4 bytes: uninitialized data "   "             */
			public int entry; /* 4 bytes: entry pt.                            */
			public int text_start; /* 4 bytes: base of text used for this file      */
			public int data_start; /* 4 bytes: base of data used for this file      */

			public OptionalHeader32(RandomAccessFile file, long offset) throws IOException {
				file.seek(offset);
				byte[] hdr = new byte[AOUTHDRSZ];
				file.readFully(hdr);
				ReadMemoryAccess memory = new ReadMemoryAccess(hdr, true);
				vstamp = memory.getShort();
				tsize = memory.getInt();
				dsize = memory.getInt();
				bsize = memory.getInt();
				entry = memory.getInt();
				text_start = memory.getInt();
				data_start = memory.getInt();
			}

			@Override
			public String toString() {
				StringBuilder buffer = new StringBuilder();
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

		public OptionalHeader(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer() + FileHeader.FILHSZ);
		}

		public OptionalHeader(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] hdr = new byte[MAGICSZ];
			file.readFully(hdr);
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, true);
			magic = memory.getShort();

			if (magic == 523) { // 64 bit executable
				optionalHeader64 = new OptionalHeader64(file, file.getFilePointer());
				is64Bits = true;
			} else if (magic == 267) { // 32 bit executable
				optionalHeader32 = new OptionalHeader32(file, file.getFilePointer());
				is64Bits = false;
			}
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("OPTIONAL HEADER VALUES").append(NL); //$NON-NLS-1$
			buffer.append("magic      = ").append(magic).append(NL); //$NON-NLS-1$

			if (is64Bits())
				buffer.append(optionalHeader64.toString());
			else
				buffer.append(optionalHeader32.toString());

			return buffer.toString();
		}

		public boolean is64Bits() {
			return is64Bits;
		}

		public int getSize() {
			if (is64Bits())
				return OptionalHeader64.AOUTHDRSZ;
			else
				return OptionalHeader32.AOUTHDRSZ;
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
		public final static int STYP_REG = 0x0000; /* "regular": allocated, relocated,
													loaded */
		public final static int STYP_DSECT = 0x0001; /* "dummy":  relocated only */
		public final static int STYP_NOLOAD = 0x0002; /* "noload": allocated, relocated,
														not loaded */
		public final static int STYP_GROUP = 0x0004; /* "grouped": formed of input
														sections */
		public final static int STYP_PAD = 0x0008; /* "padding": not allocated, not
													relocated, loaded */
		public final static int STYP_COPY = 0x0010; /* "copy": for decision function
													used by field update;
													not allocated, not relocated,
													loaded; reloc & lineno entries
													processed normally */
		public final static int STYP_TEXT = 0x0020; /* section contains text only.  */
		public final static int S_SHRSEG = 0x0020; /* In 3b Update files (output of
													ogen), sections which appear in
													SHARED segments of the Pfile
													will have the S_SHRSEG flag set
													by ogen, to inform dufr that
													updating 1 copy of the proc. will
													update all process invocations. */
		public final static int STYP_DATA = 0x0040; /* section contains data only */
		public final static int STYP_BSS = 0x0080; /* section contains bss only */
		public final static int S_NEWFCN = 0x0100; /* In a minimal file or an update
													file, a new function (as
													compared with a replaced
													function) */
		public final static int STYP_INFO = 0x0200; /* comment: not allocated not
													relocated, not loaded */
		public final static int STYP_OVER = 0x0400; /* overlay: relocated not allocated
													or loaded */
		public final static int STYP_LIB = 0x0800; /* for .lib: same as INFO */
		public final static int STYP_MERGE = 0x2000; /* merge section -- combines with
														text, data or bss sections only */
		public final static int STYP_REVERSE_PAD = 0x4000; /* section will be padded
															with no-op instructions
															wherever padding is necessary
															and there is a word of
															contiguous bytes beginning on a
															word boundary. */

		public final static int STYP_LIT = 0x8020; /* Literal data (like STYP_TEXT) */

		public byte[] s_name = new byte[8]; // 8 bytes: section name
		public int s_paddr; // 4 bytes: physical address, aliased s_nlib
		public int s_vaddr; // 4 bytes: virtual address
		public int s_size; // 4 bytes: section size
		public int s_scnptr; // 4 bytes: file ptr to raw data for section
		public int s_relptr; // 4 bytes: file ptr to relocation
		public int s_lnnoptr; // 4 bytes: file ptr to line numbers
		public int s_nreloc; // 2 bytes: number of relocation entries
		public int s_nlnno; // 2 bytes: number of line number entries
		public int s_flags; // 4 bytes: flags

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

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
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

		/**
		 * @since 5.1
		 */
		public ByteBuffer mapSectionData() throws IOException {
			return sfile.getChannel().map(MapMode.READ_ONLY, s_scnptr, s_paddr).load().asReadOnlyBuffer();
		}
	}

	public static class Reloc {
		public static final int RELSZ = 16;
		public int r_vaddr; /* 4 byte: Pointer to an area in raw data that represents a
							referenced address. */
		public int r_symndx; /* 4 byte: Index into symbol table.  */
		public int r_type; /* 2 byte(unsigned short): Type of address reference.  */

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

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
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

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
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
		public final static int DT_NON = 0; /* no derived type */
		public final static int DT_PTR = 1; /* pointer */
		public final static int DT_FCN = 2; /* function */
		public final static int DT_ARY = 3; /* array */

		public final static int N_TMASK = 0x30;
		public final static int N_BTSHFT = 4;
		public final static int N_TSHIFT = 2;

		/** @since 5.3 */
		public final static int T_NULL = 0x00; /* No symbol                                       */
		/** @since 5.3 */
		public final static int T_VOID = 0x01; /* -- 0001 	void function argument (not used)      */
		/** @since 5.3 */
		public final static int T_CHAR = 0x02; /* -- 0010 	character                              */
		/** @since 5.3 */
		public final static int T_SHORT = 0x03; /* -- 0011 	short integer                          */
		/** @since 5.3 */
		public final static int T_INT = 0x04; /* -- 0100 	integer                                */
		/** @since 5.3 */
		public final static int T_LONG = 0x05; /* -- 0101 	long integer                           */
		/** @since 5.3 */
		public final static int T_FLOAT = 0x06; /* -- 0110 	floating point                         */
		/** @since 5.3 */
		public final static int T_DOUBLE = 0x07; /* -- 0111 	double precision float                 */
		/** @since 5.3 */
		public final static int T_STRUCT = 0x08; /* -- 1000 	structure                              */
		/** @since 5.3 */
		public final static int T_UNION = 0x09; /* -- 1001 	union                                  */
		/** @since 5.3 */
		public final static int T_ENUM = 0x10; /* -- 1010 	enumeration                            */
		/** @since 5.3 */
		public final static int T_MOE = 0x11; /* -- 1011 	member of enumeration                  */
		/** @since 5.3 */
		public final static int T_UCHAR = 0x12; /* -- 1100 	unsigned character                     */
		/** @since 5.3 */
		public final static int T_USHORT = 0x13; /* -- 1101 	unsigned short                         */
		/** @since 5.3 */
		public final static int T_UINT = 0x14; /* -- 1110 	unsigned integer                       */
		/** @since 5.3 */
		public final static int T_ULONG = 0x15; /* -- 1111 	unsigned long                          */
		/** @since 5.3 */
		public final static int T_LNGDBL = 0x16; /* -1 0000 	long double (special case bit pattern) */

		public byte[] _n_name = new byte[SYMNMLEN]; /* Symbol name, or pointer into
													string table if symbol name
													is greater than SYMNMLEN.  */
		public int n_value; /* long. Symbol;s value: dependent on section number,
							storage class and type.  */
		public short n_scnum; /* short, Section number.  */
		public int n_type; /* Unsigned short. Symbolic type.  */
		public byte n_sclass; /* char, Storage class.  */
		public byte n_numaux; /* char. Number of auxiliary enties.  */
		/** @since 5.4 */
		public short n_aux_lnno; /* short, line number in auxiliary entry */
		/** @since 5.4 */
		public short n_aux_size; /* short, size in bytes in auxiliary entry */
		/** @since 5.4 */
		public int n_aux_fcn_size; /* long, size of function in bytes found in auxiliary entry */

		private boolean is64Bit;

		public Symbol(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer(), false);
		}

		public Symbol(RandomAccessFile file, long offset) throws IOException {
			this(file, offset, false);
		}

		/**
		 * @since 5.4
		 */
		public Symbol(RandomAccessFile file, boolean is64Bit) throws IOException {
			this(file, file.getFilePointer(), is64Bit);
		}

		/**
		 * @since 5.4
		 */
		public Symbol(RandomAccessFile file, long offset, boolean is64Bit) throws IOException {
			this.is64Bit = is64Bit;
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
			if (n_numaux > 0) {
				// read auxiliary section
				byte[] bytes2 = new byte[SYMSZ * n_numaux];
				file.readFully(bytes2);
				memory = new ReadMemoryAccess(bytes2, true);
				memory.getInt(); // ignore first 4 bytes - tag index
				n_aux_lnno = memory.getShort();
				n_aux_size = memory.getShort();
				// function size is unioned with lnno and size so we must rewind and
				// reread
				memory = new ReadMemoryAccess(bytes2, true);
				memory.getInt(); // ignore first 4 bytes - tag index
				n_aux_fcn_size = memory.getInt();
			}
		}

		public boolean isLongName() {
			return (_n_name[0] == 0);
		}

		public String getName() {
			// For a long name, _n_name[0] == 0 and this would just return empty string.
			for (int i = 0; i < _n_name.length; i++) {
				if (_n_name[i] == 0) {
					return new String(_n_name, 0, i);
				}
			}
			// all eight bytes are filled
			return new String(_n_name);
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

		/** @since 5.3 */
		public boolean isNoSymbol() {
			return (n_type == T_NULL);
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

		/**
		 * @since 5.4
		 */
		public int getSize() {
			if (n_type <= T_LNGDBL) {
				switch (n_type) {
				case T_CHAR:
				case T_UCHAR:
					return 1;
				case T_SHORT:
				case T_USHORT:
					return 2;
				case T_LONG:
				case T_ULONG:
					return 4;
				case T_INT:
				case T_UINT:
					return 4;
				case T_FLOAT:
					return 4;
				case T_DOUBLE:
					return 8;
				case T_MOE:
					return 4;
				case T_LNGDBL:
					return 16;
				case T_ENUM:
				case T_STRUCT:
				case T_UNION:
					return n_aux_size;
				}
			} else if (isFunction()) {
				return n_aux_fcn_size;
			} else if (isArray()) {
				return n_aux_size;
			} else if (isPointer()) {
				return is64Bit ? 8 : 4;
			}
			return 1;
		}

		@Override
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
				symbols[i] = new Symbol(rfile, (getFileHeader().f_flags & FileHeader.F_AR32WR) == 0);
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

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
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

		return buffer.toString();
	}

	public static String[] getStringTable(byte[] bytes) {
		List<String> aList = new ArrayList<>();
		int offset = 0;
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 0) {
				aList.add(new String(bytes, offset, i - offset));
				offset = i + 1;
			}
		}
		return aList.toArray(new String[0]);
	}

	public Coff64(String filename) throws IOException {
		this(new RandomAccessFile(filename, "r"), 0); //$NON-NLS-1$
	}

	public Coff64(RandomAccessFile file, long offset) throws IOException {
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
			Coff64 coff = new Coff64(args[0]);
			System.out.println(coff);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
