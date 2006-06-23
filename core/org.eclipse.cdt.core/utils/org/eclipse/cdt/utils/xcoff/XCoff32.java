/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.xcoff;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.utils.coff.ReadMemoryAccess;

/**
 * Representation of AIX XCOFF32 binary format
 * 
 * @author vhirsl
 */
public class XCoff32 {
	public static final String NL = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	
	String filename;
	FileHeader filehdr;
	OptionalHeader opthdr;
	RandomAccessFile rfile;
	long startingOffset;
	byte[] string_table;
	SectionHeader[] scnhdrs;
	Symbol[] symbols;
	
	public static class FileHeader {
		public final static int FILHSZ = 20;
		
		// Consts
		public final static int U802TOCMAGIC = 0x01df;	// XCOFF32
		public final static int U803TOCMAGIC = 0x01e7;	// obsolete XCOFF64 - not used
		public final static int U803XTOCMAGIC = 0x01ef;	// discontinued AIX XCOFF64
		public final static int U64_TOCMAGIC = 0x01f7;	// XCOFF64

		// Flags
		public final static int F_RELFLG = 0x0001; // relocation info stripped from file
		public final static int F_EXEC = 0x0002;   // file is executable
							   //  (no unresolved external references)
		public final static int F_LNNO = 0x0004;   // line numbers stripped from file
		public final static int F_LSYMS = 0x0008;  // local symbols stripped from file
		public final static int F_FDPR_PROF = 0x0010;	// file was profiled with fdpr command
		public final static int F_FDPR_OPTI = 0x0020;	// file was reordered with fdpr command
		public final static int F_DSA = 0x0040;	// file uses Very Large Program Support
//		public final static int F_AR16WR = 0x0080; // file is 16-bit little-endian
//		public final static int F_AR32WR = 0x0100; // file is 32-bit little-endian
//		public final static int F_AR32W = 0x0200;  // file is 32-bit big-endian
		public final static int F_DYNLOAD = 0x1000;	// rs/6000 aix: dynamically
							   // loadable w/imports & exports
		public final static int F_SHROBJ = 0x2000; 	// rs/6000 aix: file is a shared object
		public final static int F_LOADONLY = 0x4000;// rs/6000 aix: if the object file is a member of an archive;
							   // it can be loaded by the system loader but the member is ignored by the binder.

		// Fields
		public short f_magic;	/* 00-01 2 bytes: magic number                 */
		public short f_nscns;	/* 02-03 2 bytes: number of sections: 2 bytes  */
		public int f_timdat;	/* 04-07 4 bytes: time & date stamp            */
		public int f_symptr;	/* 08-11 4 bytes: file pointer to symtab       */
		public int f_nsyms;		/* 12-15 4 bytes: number of symtab entries     */
		public short f_opthdr;	/* 16-17 2 bytes: sizeof(optional hdr)         */
		public short f_flags;	/* 18-19 2 bytes: flags                        */

		public FileHeader (RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public FileHeader (RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] hdr = new byte[FILHSZ];
			file.readFully(hdr);
			commonSetup(hdr, false);
		}

		public FileHeader (byte[] hdr, boolean little) throws IOException {
			commonSetup(hdr, little);
		}

		public void commonSetup(byte[] hdr, boolean little) throws IOException {
			if (hdr == null || hdr.length < FILHSZ) {
				throw new EOFException(CCorePlugin.getResourceString("Util.exception.arrayToSmall")); //$NON-NLS-1$
			}
			if (!isXCOFF32Header(hdr)) {
				throw new IOException(CCorePlugin.getResourceString("Util.exception.notXCOFF32")); //$NON-NLS-1$
			}
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, little);
			f_magic = memory.getShort();
			f_nscns = memory.getShort();
			f_timdat = memory.getInt();
			f_symptr = memory.getInt();
			f_nsyms = memory.getInt();
			f_opthdr = memory.getShort();
			f_flags = memory.getShort();
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

			buffer.append("f_magic  = ").append(f_magic).append(NL); //$NON-NLS-1$
			buffer.append("f_nscns  = ").append(f_nscns).append(NL); //$NON-NLS-1$

			buffer.append("f_timdat = "); //$NON-NLS-1$
			buffer.append(DateFormat.getDateInstance().format(new Date(f_timdat)));
			buffer.append(NL);

			buffer.append("f_symptr = ").append(f_symptr).append(NL); //$NON-NLS-1$
			buffer.append("f_nsyms  = ").append(f_nsyms).append(NL); //$NON-NLS-1$
			buffer.append("f_opthdr = ").append(f_opthdr).append(NL); //$NON-NLS-1$
			buffer.append("f_flags  = ").append(f_flags).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}
	}

	public static class OptionalHeader {
		public final static int AOUTHDRSZ = 72;	// First 28 bytes same as for COFF

		// Fields (as in COFF)
		public short magic;		/* 2 bytes: type of file (0x010B)               	*/
		public short vstamp;	/* 2 bytes: version stamp (1)                   	*/
		public int tsize;		/* 4 bytes: text size in bytes, padded to FW bdry	*/
		public int dsize;		/* 4 bytes: initialized data "  "               	*/
		public int bsize;		/* 4 bytes: uninitialized data "   "            	*/
		public int entry;		/* 4 bytes: entry pt.                           	*/
		public int text_start;	/* 4 bytes: base of text used for this file     	*/
		public int data_start;	/* 4 bytes: base of data used for this file     	*/
		// Additional fields
		public int o_toc;	   	/* 4 bytes: Address of TOC anchor 					*/
		public short o_snentry;	/* 2 bytes: Section number for entry point			*/
		public short o_sntext; 	/* 2 bytes: Section number for .text				*/
		public short o_sndata; 	/* 2 bytes: Section number for .data				*/
		public short o_sntoc;	/* 2 bytes: Section number for TOC					*/
		public short o_snloader;/* 2 bytes: Section number for loader data			*/
		public short o_snbss;	/* 2 bytes: Section number for .bss					*/
		public short o_algntext;/* 2 bytes: Maximum alignment for .text				*/
		public short o_algndata;/* 2 bytes: Maximum alignment for .data				*/
		public short o_modtype;	/* 2 bytes: Maximum alignment for .data				*/
		public byte o_cpuflag;	/* 1 byte: Bit flags - cpu types of objects			*/		
		public byte o_cputype;	/* 1 byte: Reserved for cpu type					*/
		public int o_maxstack;	/* 4 bytes: Maximum stack size allowed (bytes)		*/
		public int o_maxdata;	/* 4 bytes: Maximum data size allowed (bytes)		*/
		public int o_debugger;	/* 4 bytes: Reserved for debuggers					*/
//		public byte o_resv2[8];	/* 8 bytes: Reserved. Field must contain 0s			*/

		public OptionalHeader(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer() + FileHeader.FILHSZ);
		}

		public OptionalHeader(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] hdr = new byte[AOUTHDRSZ];
			file.readFully(hdr);
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, false);	// big endian
			// COFF common
			magic = memory.getShort();
			vstamp = memory.getShort();
			tsize = memory.getInt();
			dsize = memory.getInt();
			bsize = memory.getInt();
			entry = memory.getInt();
			text_start = memory.getInt();
			data_start = memory.getInt();
			// XCOFF32 specific
			o_toc = memory.getInt();
			o_snentry = memory.getShort();
			o_sntext = memory.getShort();
			o_sndata = memory.getShort();
			o_sntoc = memory.getShort();
			o_snloader = memory.getShort();
			o_snbss = memory.getShort();
			o_algntext = memory.getShort();
			o_algndata = memory.getShort();
			o_modtype = memory.getShort();
			o_cpuflag = memory.getByte();
			o_cputype = memory.getByte();
			o_maxstack = memory.getInt();
			o_maxdata = memory.getInt();
			o_debugger = memory.getInt();
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
			buffer.append("o_toc      = ").append(o_toc).append(NL); //$NON-NLS-1$
			buffer.append("o_snentry  = ").append(o_snentry).append(NL); //$NON-NLS-1$
			buffer.append("o_sntext   = ").append(o_sntext).append(NL); //$NON-NLS-1$
			buffer.append("o_sndata   = ").append(o_sndata).append(NL); //$NON-NLS-1$
			buffer.append("o_sntoc    = ").append(o_sntoc).append(NL); //$NON-NLS-1$
			buffer.append("o_snloader = ").append(o_snloader).append(NL); //$NON-NLS-1$
			buffer.append("o_snbss    = ").append(o_snbss).append(NL); //$NON-NLS-1$
			buffer.append("o_algntext = ").append(o_algntext).append(NL); //$NON-NLS-1$
			buffer.append("o_algndata = ").append(o_algndata).append(NL); //$NON-NLS-1$
			buffer.append("o_modtype  = ").append(o_modtype).append(NL); //$NON-NLS-1$
			buffer.append("o_cpuflag  = ").append(o_cpuflag).append(NL); //$NON-NLS-1$
			buffer.append("o_cputype  = ").append(o_cputype).append(NL); //$NON-NLS-1$
			buffer.append("o_maxstack = ").append(o_maxstack).append(NL); //$NON-NLS-1$
			buffer.append("o_maxdata  = ").append(o_maxdata).append(NL); //$NON-NLS-1$
			buffer.append("o_debugger = ").append(o_debugger).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}
	}

	public static class SectionHeader {
		public final static int SCNHSZ = 40;

		/* names of "special" sections */
		public final static String _TEXT = ".text"; //$NON-NLS-1$
		public final static String _DATA = ".data"; //$NON-NLS-1$
		public final static String _BSS = ".bss"; //$NON-NLS-1$
		public final static String _PAD = ".pad"; //$NON-NLS-1$
		public final static String _LOADER = ".loader"; //$NON-NLS-1$
		public final static String _DEBUG = ".debug"; //$NON-NLS-1$
		public final static String _TYPCHK = ".typchk"; //$NON-NLS-1$
		public final static String _EXCEPT = ".except"; //$NON-NLS-1$
		public final static String _OVRFLO = ".ovrflo"; //$NON-NLS-1$
		public final static String _INFO = ".info"; //$NON-NLS-1$

		/* s_flags "type".  */
//		public final static int STYP_REG = 0x0000;    /* "regular": allocated, relocated,
//								loaded */
//		public final static int STYP_DSECT = 0x0001;  /* "dummy":  relocated only */
//		public final static int STYP_NOLOAD = 0x0002; /* "noload": allocated, relocated,
//								not loaded */
//		public final static int STYP_GROUP = 0x0004;  /* "grouped": formed of input
//								sections */
		public final static int STYP_PAD = 0x0008;    /* "padding": not allocated, not
								relocated, loaded */
//		public final static int STYP_COPY = 0x0010;   /* "copy": for decision function
//								used by field update;
//								not allocated, not relocated,
//								loaded; reloc & lineno entries
//								processed normally */
		public final static int STYP_TEXT = 0x0020;   /* section contains text only.  */
//		public final static int S_SHRSEG = 0x0020;    /* In 3b Update files (output of
//								ogen), sections which appear in
//								SHARED segments of the Pfile
//								will have the S_SHRSEG flag set
//								by ogen, to inform dufr that
//								updating 1 copy of the proc. will
//								update all process invocations. */
		public final static int STYP_DATA = 0x0040;   /* section contains data only */
		public final static int STYP_BSS = 0x0080;    /* section contains bss only */
		public final static int STYP_EXCEPT = 0x0080;    /* section contains exceptions info only */
//		public final static int S_NEWFCN = 0x0100;    /* In a minimal file or an update
//								file, a new function (as
//								compared with a replaced
//								function) */
		public final static int STYP_INFO = 0x0200;   /* comment: not allocated not
								relocated, not loaded */
//		public final static int STYP_OVER = 0x0400;   /* overlay: relocated not allocated
//								or loaded */
//		public final static int STYP_LIB = 0x0800;    /* for .lib: same as INFO */
		public final static int STYP_LOADER = 0x1000;  /* loader section:
								imported symbols,
								exported symbols,
								relocation data,
								type-check information and
								shared object names */
//		public final static int STYP_MERGE = 0x2000;  /* merge section -- combines with
//								text, data or bss sections only */
		public final static int STYP_DEBUG = 0x2000;  /* debug section - su
								information used by the symbolic debugger */
//		public final static int STYP_REVERSE_PAD = 0x4000; /* section will be padded
//								with no-op instructions
//								wherever padding is necessary
//								and there is a word of
//								contiguous bytes beginning on a
//								word boundary. */
		public final static int STYP_TYPCHK = 0x4000; /* type-check section - contains
								parameter/argument type-check strings used by the binder */
		public final static int STYP_OVRFLO = 0x8000; /* overflow section:
								Specifies a relocation or line-number field overflow section.
								A section header of this type contains the count of relocation 
								entries and line number entries for some other section. 
								This section header is required when either of the counts
								exceeds 65,534. */

//		public final static int STYP_LIT = 0x8020;  /* Literal data (like STYP_TEXT) */


		public byte[] s_name= new byte[8]; // 8 bytes: section name
		public int s_paddr;    // 4 bytes: physical address, aliased s_nlib
		public int s_vaddr;    // 4 bytes: virtual address
		public int s_size;     // 4 bytes: section size
		public int s_scnptr;   // 4 bytes: file ptr to raw data for section
		public int s_relptr;   // 4 bytes: file ptr to relocation
		public int s_lnnoptr;  // 4 bytes: file ptr to line numbers
		public short s_nreloc;   // 2 bytes: number of relocation entries
		public short s_nlnno;    // 2 bytes: number of line number entries
		public int s_flags;    // 4 bytes: flags

		RandomAccessFile sfile;

		public SectionHeader(RandomAccessFile file, long offset) throws IOException {
			sfile = file;
			file.seek(offset);
			byte[] hdr = new byte[SCNHSZ];
			file.readFully(hdr);
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, false);
			memory.getBytes(s_name);
			s_paddr = memory.getInt();
			s_vaddr = memory.getInt();
			s_size = memory.getInt();
			s_scnptr = memory.getInt();
			s_relptr = memory.getInt();
			s_lnnoptr = memory.getInt();
			s_nreloc = memory.getShort();
			s_nlnno = memory.getShort();
			s_flags = memory.getInt();
		}

		public byte[] getRawData() throws IOException {
			byte[] data = new byte[s_size];
			sfile.seek(s_scnptr);
			sfile.readFully(data);
			return data;
		}

//		public Reloc[] getRelocs() throws IOException {
//			Reloc[] relocs = new Reloc[s_nreloc];
//			sfile.seek(s_relptr);
//			for (int i = 0; i < s_nreloc; i++) {
//				relocs[i] = new Reloc(sfile);
//			}
//			return relocs;
//		}
//
//		public Lineno[] getLinenos() throws IOException {
//			Lineno[] lines = new Lineno[s_nlnno];
//			sfile.seek(s_lnnoptr);
//			for (int i = 0; i < s_nlnno; i++) {
//				lines[i] = new Lineno(sfile);
//			}
//			return lines;
//		}

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
/*
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
*/
			return buffer.toString();
		}
	}

	public class Symbol {
		public final static int SYMSZ = 18;
		public final static int SYMNMLEN = 8;

		/* section number, in n_scnum.  */
		public final static int N_DEBUG = -2;
		public final static int N_ABS   = -1;
		public final static int N_UNDEF = 0;
		
		/* Storage class, in n_sclass.  */
		public final static int C_BCOMM = 135;	/* beginning of the common block */
		public final static int C_BINCL = 108;	/* beginning of include file */
		public final static int C_BLOCK = 100;	/* beginning or end of inner block */
		public final static int C_BSTAT = 143;	/* beginning of static block */
		public final static int C_DECL  = 140;	/* declaration of object (type) */
		public final static int C_ECOML = 136;	/* local member of common block */
		public final static int C_ECOMM = 127;	/* end of common block */
		public final static int C_EINCL = 109;	/* end of include file */
		public final static int C_ENTRY = 141;	/* alternate entry */
		public final static int C_ESTAT = 144;	/* end of static block */
		public final static int C_EXT   = 2;	/* external symbol */
		public final static int C_FCN   = 101;	/* beginning or end of function */
		public final static int C_FILE  = 103;	/* source file name and compiler information */
		public final static int C_FUN   = 142;	/* function or procedure */
		public final static int C_GSYM  = 128;	/* global variable */
		public final static int C_HIDEXT = 107;	/* unnamed external symbol */
		public final static int C_INFO  = 100;	/* comment section reference */
		public final static int C_LSYM  = 129;	/* automatic variable allocated on stack */
		public final static int C_NULL  = 0;	/* symbol table entry marked for deletion */
		public final static int C_PSYM  = 130;	/* argument to subroutine allocated on stack */
		public final static int C_RPSYM = 132;	/* argument to function or procedure stored in register */
		public final static int C_RSYM  = 131;	/* register variable */
		public final static int C_STAT  = 3;	/* static symbol (unknown) */
		public final static int C_STSYM = 133;	/* statically allocated symbol */
		public final static int C_TCSYM = 134;	/* reserved */
		public final static int C_WEAKEXT = 111;/* weak external symbol */ 

		/* csect storage class, in x_smlas.  */
		public final static int XMC_PR = 0;		/* program code */
		public final static int XMC_RO = 1;		/* read only constant */
		public final static int XMC_DB = 2;		/* debug dictionary table */
		public final static int XMC_TC = 3;		/* general TOC entry */
		public final static int XMC_UA = 4;		/* unclassified */
		public final static int XMC_RW = 5;		/* read/write data */
		public final static int XMC_GL = 6;		/* global linkage */
		public final static int XMC_XO = 7;		/* extended operation */
		public final static int XMC_SV = 8;		/* 32-bit supervisor call descriptor csect */
		public final static int XMC_BS = 9;		/* BSS class (uninitialized static internal) */
		public final static int XMC_DS = 10;	/* csect containing a function descriptor */
		public final static int XMC_UC = 11;	/* unnamed FORTRAN common */
		public final static int XMC_TI = 12;	/* reserved */
		public final static int XMC_TB = 13;	/* reserved */
		public final static int XMC_TC0 = 15;	/* TOC anchor for TOC addressability */
		public final static int XMC_TD = 16;	/* scalar data entry in TOC */
		public final static int XMC_SV64 = 17;	/* 64-bit supervisor call descriptor csect */
		public final static int XMC_SV3264 = 18;/* supervisor call descriptor csect for both 32-bit and 64-bit */
		

		// fields
		public byte[] _n_name = new byte[SYMNMLEN]; /* Symbol name, or pointer into
								   string table if symbol name
								   is greater than SYMNMLEN.  */
		public int n_value; 	/* long. Symbol's value: dependent on section number,
				       			   storage class and type.  */
		public short n_scnum; 	/* short, Section number.  */
		public short n_type;   	/* Unsigned short. Symbolic type. Obsolete in XCOFF */
		public byte n_sclass; 	/* char, Storage class.  */
		public byte n_numaux; 	/* char. Nuymber of auxiliary enties.  */
		private byte[] aux;
		public byte x_smclas;	/* storage mapping class in csect auxiliary entry */

		public Symbol(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public Symbol(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] bytes = new byte[SYMSZ];
			file.readFully(bytes);
			ReadMemoryAccess memory = new ReadMemoryAccess(bytes, false); // big endian
			memory.getBytes(_n_name);
			n_value = memory.getInt();
			n_scnum = memory.getShort();
			n_type = memory.getShort();
			n_sclass = memory.getByte();
			n_numaux = memory.getByte();
			aux = new byte[n_numaux * SYMSZ];
			file.readFully(aux);
			// 11th byte in the last auxiliary entry (csect)
			x_smclas = (n_numaux > 0) ? aux[aux.length - 7] : 0;	
		}

		private boolean isLongName() {
			return (_n_name[0] == 0 && 
					_n_name[1] == 0 &&
					_n_name[2] == 0 &&
					_n_name[3] == 0);
		}

		private String getShortName() {
			for (int i = 0; i < _n_name.length; i++) {
				if (_n_name[i] == 0) {
					return new String(_n_name, 0, i);
				}
			}
			return ""; //$NON-NLS-1$
		}

		public String getName(byte[] table) {
			if (table.length > 0 && isLongName()) {
				ReadMemoryAccess memory = new ReadMemoryAccess(_n_name, false);
				memory.getInt(); // pass over the first 4 bytes.
				// The first for bytes of the string table represent the
				// number of bytes in the string table.
				int offset = memory.getInt() - 4;
				if (offset >= 0) {
					for (int i = offset; i < table.length; i++) {
						if (table[i] == 0) {
							return new String(table, offset, i - offset);
						}
					}
				}
			}
			return getShortName();
		}

		public boolean isFunction() {
			return ((n_sclass == C_EXT || n_sclass == C_HIDEXT || n_sclass == C_WEAKEXT) && 
					n_scnum == opthdr.o_sntext &&
					!getShortName().equals(SectionHeader._TEXT));
		}

		public boolean isVariable() {
			return ((n_sclass == C_EXT || n_sclass == C_HIDEXT || n_sclass == C_WEAKEXT) &&
					(n_scnum == opthdr.o_snbss || n_scnum == opthdr.o_sndata) &&
					x_smclas != XMC_TC0 && x_smclas != XMC_TC && x_smclas != XMC_DS &&
					!getShortName().equals(SectionHeader._BSS) &&
					!getShortName().equals(SectionHeader._DATA));
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("SYMBOL TABLE ENTRY").append(NL); //$NON-NLS-1$
			buffer.append("n_value = ").append(n_value).append(NL); //$NON-NLS-1$
			buffer.append("n_scnum = ").append(n_scnum).append(NL); //$NON-NLS-1$
			buffer.append("n_type = ").append(n_type).append(NL); //$NON-NLS-1$
			buffer.append("n_sclass = ").append(n_sclass).append(NL); //$NON-NLS-1$
			buffer.append("n_numaux = ").append(n_numaux).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}

	}

	public static class Attribute {
		public static final int XCOFF_TYPE_EXE   = 1;
		public static final int XCOFF_TYPE_SHLIB = 2;
		public static final int XCOFF_TYPE_OBJ   = 3;
		public static final int XCOFF_TYPE_CORE  = 4;

		String cpu;
		int type;
		boolean bDebug;
		boolean isle;

		public String getCPU() {
			return cpu;
		}
                
		public int getType() {
			return type;
		}
                
		public boolean hasDebug() {
			return bDebug;
		}

		public boolean isLittleEndian() {
			return isle;
		}
	}

	public Attribute getAttributes() {
		Attribute attrib = new Attribute();
		// Machine type.
		switch (filehdr.f_magic) {
			case FileHeader.U802TOCMAGIC:
				attrib.cpu = "xcoff32"; //$NON-NLS-1$
			break;
			case FileHeader.U64_TOCMAGIC:
				attrib.cpu = "xcoff64"; //$NON-NLS-1$
			break;
			default:
				attrib.cpu = "unknown"; //$NON-NLS-1$
			break;
		}

		/* XCOFF characteristics, FileHeader.f_flags.  */
		if ((filehdr.f_flags & FileHeader.F_SHROBJ) != 0) {
			attrib.type = Attribute.XCOFF_TYPE_SHLIB;
		} else if ((filehdr.f_flags & FileHeader.F_EXEC) != 0) {
			attrib.type = Attribute.XCOFF_TYPE_EXE;
		} else {
			attrib.type = Attribute.XCOFF_TYPE_OBJ;
		}

		// For AIX XCOFF always assume big endian unless otherwise.
		attrib.isle = false;

		// No debug information.
		if ((filehdr.f_flags & (FileHeader.F_LNNO & FileHeader.F_LSYMS & FileHeader.F_RELFLG)) != 0) {
			attrib.bDebug = false;
		} else {
			attrib.bDebug = true;
		}
		
		return attrib;
	}

	public FileHeader getFileHeader() throws IOException {
		return filehdr;
	}
	
	public OptionalHeader getOptionalHeader() throws IOException {
		return opthdr;
	}

	public SectionHeader[] getSectionHeaders() throws IOException {
		if (scnhdrs == null) {
			getRandomAccessFile();
			scnhdrs = new SectionHeader[getFileHeader().f_nscns];
			long sec = startingOffset + FileHeader.FILHSZ + getFileHeader().f_opthdr;
			for (int i = 0; i < scnhdrs.length; i++, sec += SectionHeader.SCNHSZ) {
				scnhdrs[i] = new SectionHeader(rfile, sec);
			}
		}
		return scnhdrs;
	}

	public Symbol[] getSymbols() throws IOException {
		if (symbols == null) {
			long offset = startingOffset + getFileHeader().f_symptr;
			getRandomAccessFile();
			rfile.seek(offset);
			int numSymbols = getFileHeader().f_nsyms;
			ArrayList symList = new ArrayList(numSymbols);
			for (int i = 0; i < numSymbols; ++i) {
				Symbol v = new Symbol(rfile);
				symList.add(v);
				i += v.n_numaux; // account for auxiliary entries
			}
			symbols = (Symbol[]) symList.toArray(new Symbol[symList.size()]);
		}
		return symbols;
	}

	public byte[] getStringTable() throws IOException {
		if (string_table == null) {
			if (filehdr.f_nsyms > 0) {
				getRandomAccessFile();
				long symbolsize = Symbol.SYMSZ * getFileHeader().f_nsyms;
				long offset = startingOffset+ getFileHeader().f_symptr + symbolsize;
				rfile.seek(offset);
				byte[] bytes = new byte[4];
				rfile.readFully(bytes);
				int str_len = ReadMemoryAccess.getIntBE(bytes);
				if (str_len > 4 && str_len < rfile.length()) {
					str_len -= 4;
					string_table = new byte[str_len];
					rfile.seek(offset + 4);
					rfile.readFully(string_table);
				} else {
					string_table = new byte[0];
				}
			}
		}
		return string_table;
	}

    // A hollow entry, to be used with caution in controlled situations
	protected XCoff32() {
	}
	
	public XCoff32(String filename) throws IOException {
		this(filename, 0);
	}

	public XCoff32(String filename, long offset) throws IOException {
		this.filename = filename;
		commonSetup(new RandomAccessFile(filename, "r"), offset); //$NON-NLS-1$
	}

	void commonSetup(RandomAccessFile file, long offset) throws IOException {
		startingOffset = offset;
		rfile = file;
		try {
			filehdr = new FileHeader(rfile, startingOffset);
			if (filehdr.f_opthdr > 0) {
				opthdr = new OptionalHeader(rfile, startingOffset + FileHeader.FILHSZ);
			}
			if (filehdr.f_opthdr < OptionalHeader.AOUTHDRSZ) {
				// auxiliary header does not contain needed information, 
				// must load section headers
				getSectionHeaders();
				for (int i = 0; i < filehdr.f_nscns; ++i) {
					if ((scnhdrs[i].s_flags & SectionHeader.STYP_TEXT) != 0) {
						opthdr.o_sntext = (short)(i+1);
					}
					else if ((scnhdrs[i].s_flags & SectionHeader.STYP_BSS) != 0) {
						opthdr.o_snbss = (short)(i+1);
					}
					else if ((scnhdrs[i].s_flags & SectionHeader.STYP_DATA) != 0) {
						opthdr.o_sndata = (short)(i+1);
					}
				}
			}
		} finally {
			dispose();
		}
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
				buffer.append(table[i]).append("n_name = "); //$NON-NLS-1$
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

	public void dispose() throws IOException {
		if (rfile != null) {
			rfile.close();
			rfile = null;
		}
	}

	RandomAccessFile getRandomAccessFile () throws IOException {
		if (rfile == null) {
			rfile = new RandomAccessFile(filename, "r"); //$NON-NLS-1$
		}
		return rfile;
	}

	public static void main(String[] args) {
		try {
			XCoff32 xcoff = new XCoff32(args[0]);
			System.out.println(xcoff);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param hints
	 * @return
	 */
	public static boolean isXCOFF32Header(byte[] hints) {
		if (hints != null && hints[0] == 0x01 && (hints[1] == (byte)0xdf) ) {
			return true;
		}
		return false;
	}

	/**
	 * @param hints
	 * @return
	 * @throws IOException
	 */
	public static Attribute getAttributes(byte[] hints) throws IOException {
		XCoff32 emptyXCoff = new XCoff32();
		emptyXCoff.filehdr = new XCoff32.FileHeader(hints, false); // big endian
		Attribute attribute = emptyXCoff.getAttributes();
		emptyXCoff.dispose();
		return attribute;
	}

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Attribute getAttributes(String file) throws IOException {
		XCoff32 xcoff = new XCoff32(file);
		Attribute attribute = xcoff.getAttributes();
		xcoff.dispose();
		return attribute;
	}
}
