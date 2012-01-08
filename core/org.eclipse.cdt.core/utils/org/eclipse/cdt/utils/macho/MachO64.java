/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Craig Watson.
 *     Apple Computer - work on performance optimizations
 *******************************************************************************/
package org.eclipse.cdt.utils.macho;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.debug.stabs.StabsReader;
import org.eclipse.cdt.utils.debug.stabs.StabConstant;

/**
 * @since 5.2
 */
public class MachO64 {
	protected ERandomAccessFile efile;		

	protected MachOhdr mhdr;
	protected LoadCommand[] loadcommands;
    protected boolean cppFiltEnabled = true;
	protected CPPFilt cppFilt;
	protected String file;
	protected boolean debugsym = false;	/* contains debugging symbols */
	protected boolean b64 = false;
    private Symbol[] symbols;			/* symbols from SymtabCommand */
    private Symbol[] local_symbols;		/* local symbols from DySymtabCommand */
    private boolean dynsym = false;		/* set if DynSymtabCommand is present */
    Line[] lines;				/* line table */
    private ArrayList<Section> sections = new ArrayList<Section>();			/* sections from SegmentCommand */
    SymtabCommand symtab;		/* SymtabCommand that contains the symbol table */
    
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	protected static final SymbolComparator symbol_comparator = new SymbolComparator();

	public class MachOhdr {
	    /* values of magic */
	    public final static int MH_MAGIC = 0xfeedface;      /* the mach magic number */
	    public final static int MH_CIGAM = 0xcefaedfe;
	    public final static int MH_UNIVERSAL = 0xcafebabe;
	    public final static int MH_MAGIC_64 = 0xfeedfacf;
	    public final static int MH_CIGAM_64 = 0xcffaedfe;

	    /* Capability mashs for cpu type*/
	    public final static int CPU_ARCH_MASK =  0xff000000;
	    public final static int CPU_ARCH_ABI64 = 0x01000000;   

	    /* values of cputype */
	    public final static int CPU_TYPE_ANY = -1;
	    public final static int CPU_TYPE_VAX = 1;
	    public final static int CPU_TYPE_MC680x0 = 6;
	    public final static int CPU_TYPE_I386 = 7;
	    public final static int CPU_TYPE_X86 = 7;
	    public final static int CPU_TYPE_X86_64 = (CPU_TYPE_X86 | CPU_ARCH_ABI64);
	    public final static int CPU_TYPE_MC98000 = 10;
	    public final static int CPU_TYPE_HPPA = 11;
	    public final static int CPU_TYPE_MC88000 = 13;
	    public final static int CPU_TYPE_SPARC = 14;
	    public final static int CPU_TYPE_I860 = 15;
	    public final static int CPU_TYPE_POWERPC = 18;
	    public final static int CPU_TYPE_POWERPC64 = (CPU_TYPE_POWERPC | CPU_ARCH_ABI64);
	    
	    /* Capability bits for cpu_subtype*/
	    public final static int CPU_SUBTYPE_MASK = 0xff000000;
	    public final static int CPU_SUBTYPE_LIB64 = 0x80000000;
	    
	    /* values of cpusubtype */
	    public final static int CPU_SUBTYPE_MULTIPLE = -1;
	    public final static int CPU_SUBTYPE_LITTLE_ENDIAN = 0;
	    public final static int CPU_SUBTYPE_BIG_ENDIAN = 1;
	    public final static int CPU_SUBTYPE_VAX_ALL = 0;
	    public final static int CPU_SUBTYPE_VAX780 = 1;
	    public final static int CPU_SUBTYPE_VAX785 = 2;
	    public final static int CPU_SUBTYPE_VAX750 = 3;
	    public final static int CPU_SUBTYPE_VAX730 = 4;
	    public final static int CPU_SUBTYPE_UVAXI = 5;
	    public final static int CPU_SUBTYPE_UVAXII = 6;
	    public final static int CPU_SUBTYPE_VAX8200 = 7;
	    public final static int CPU_SUBTYPE_VAX8500 = 8;
	    public final static int CPU_SUBTYPE_VAX8600 = 9;
	    public final static int CPU_SUBTYPE_VAX8650 = 10;
	    public final static int CPU_SUBTYPE_VAX8800 = 11;
	    public final static int CPU_SUBTYPE_UVAXIII = 12;
	    public final static int CPU_SUBTYPE_MC680x0_ALL = 1;
	    public final static int CPU_SUBTYPE_MC68030 = 1;
	    public final static int CPU_SUBTYPE_MC68040 = 2;
	    public final static int CPU_SUBTYPE_MC68030_ONLY = 3;
	    public final static int CPU_SUBTYPE_I386_ALL = 3;
	    public final static int CPU_SUBTYPE_386 = 3;
	    public final static int CPU_SUBTYPE_486 = 4;
	    public final static int CPU_SUBTYPE_486SX = 132;
	    public final static int CPU_SUBTYPE_586 = 5;
	    public final static int CPU_SUBTYPE_PENT = 5;
	    public final static int CPU_SUBTYPE_PENTPRO = 32;
	    public final static int CPU_SUBTYPE_PENTII_M3 = 54;
	    public final static int CPU_SUBTYPE_PENTII_M5 = 86;
	    public final static int CPU_SUBTYPE_CELERON = 7 + (6 << 4);
	    public final static int CPU_SUBTYPE_CELERON_MOBILE = 7 + (7 << 4);
	    public final static int CPU_SUBTYPE_PENTIUM_3 = 8 + (0 << 4);
	    public final static int CPU_SUBTYPE_PENTIUM_3_M = 8 + (1 << 4);
	    public final static int CPU_SUBTYPE_PENTIUM_3_XEON = 8 + (2 << 4);
	    public final static int CPU_SUBTYPE_PENTIUM_M = 9 + (0 << 4);
	    public final static int CPU_SUBTYPE_PENTIUM_4 = 10 + (0 << 4);
	    public final static int CPU_SUBTYPE_PENTIUM_4_M = 10 + (0 << 1);
	    public final static int CPU_SUBTYPE_ITANIUM = 11 + (0 << 4);
	    public final static int CPU_SUBTYPE_ITANIUM_2 = 11 + (1 << 4);
	    public final static int CPU_SUBTYPE_XEON = 12 + (0 << 4);
	    public final static int CPU_SUBTYPE_XEON_MP = 12 + (1 << 4);	  
	    public final static int CPU_SUBTYPE_X86_ALL = 3;
	    public final static int CPU_SUBTYPE_X86_64_ALL = 3;
	    public final static int CPU_SUBTYPE_X86_ARCH1 = 4;
	    public final static int CPU_SUBTYPE_MIPS_ALL = 0;
	    public final static int CPU_SUBTYPE_MIPS_R2300 = 1;
	    public final static int CPU_SUBTYPE_MIPS_R2600 = 2;
	    public final static int CPU_SUBTYPE_MIPS_R2800 = 3;
	    public final static int CPU_SUBTYPE_MIPS_R2000a = 4;
	    public final static int CPU_SUBTYPE_MIPS_R2000 = 5;
	    public final static int CPU_SUBTYPE_MIPS_R3000a = 6;
	    public final static int CPU_SUBTYPE_MIPS_R3000 = 7;
	    public final static int CPU_SUBTYPE_MC98000_ALL = 0;
	    public final static int CPU_SUBTYPE_MC98601 = 1;
	    public final static int CPU_SUBTYPE_HPPA_ALL = 0;
	    public final static int CPU_SUBTYPE_HPPA_7100 = 0;
	    public final static int CPU_SUBTYPE_HPPA_7100LC = 1;
	    public final static int CPU_SUBTYPE_MC88000_ALL = 0;
	    public final static int CPU_SUBTYPE_MC88100 = 1;
	    public final static int CPU_SUBTYPE_MC88110 = 2;
	    public final static int CPU_SUBTYPE_SPARC_ALL = 0;
	    public final static int CPU_SUBTYPE_I860_ALL = 0;
	    public final static int CPU_SUBTYPE_I860_860 = 1;
	    public final static int CPU_SUBTYPE_POWERPC_ALL = 0;
	    public final static int CPU_SUBTYPE_POWERPC_601 = 1;
	    public final static int CPU_SUBTYPE_POWERPC_602 = 2;
	    public final static int CPU_SUBTYPE_POWERPC_603 = 3;
	    public final static int CPU_SUBTYPE_POWERPC_603e = 4;
	    public final static int CPU_SUBTYPE_POWERPC_603ev = 5;
	    public final static int CPU_SUBTYPE_POWERPC_604 = 6;
	    public final static int CPU_SUBTYPE_POWERPC_604e = 7;
	    public final static int CPU_SUBTYPE_POWERPC_620 = 8;
	    public final static int CPU_SUBTYPE_POWERPC_750 = 9;
	    public final static int CPU_SUBTYPE_POWERPC_7400 = 10;
	    public final static int CPU_SUBTYPE_POWERPC_7450 = 11;
	    public final static int CPU_SUBTYPE_POWERPC_970 = 100;
	    
	    /* values of filetype */
	    public final static int MH_OBJECT = 0x1;			/* relocatable object file */
	    public final static int MH_EXECUTE = 0x2;			/* demand paged executable file */
	    public final static int MH_FVMLIB = 0x3;			/* fixed VM shared library file */
	    public final static int MH_CORE = 0x4;				/* core file */
	    public final static int MH_PRELOAD = 0x5;			/* preloaded executable file */
	    public final static int MH_DYLIB = 0x6;				/* dynamically bound shared library */
	    public final static int MH_DYLINKER = 0x7;			/* dynamic link editor */
	    public final static int MH_BUNDLE = 0x8;			/* dynamically bound bundle file */
	    public final static int MH_DYLIB_STUB = 0x9;		/* shared library stub for static linking only, no section contents */
	    public final static int MH_DSYM = 0xa;             	/* companion file with only debug */
	    
	    /* values of flags */
	    public final static int MH_NOUNDEFS = 0x1;			/* the object file has no undefined references */
		public final static int MH_INCRLINK = 0x2;			/* the object file is the output of an incremental link against a base file and can't be link edited again */
		public final static int MH_DYLDLINK = 0x4;			/* the object file is input for the dynamic linker and can't be staticly link edited again */
		public final static int MH_BINDATLOAD = 0x8;		/* the object file's undefined references are bound by the dynamic linker when loaded. */
		public final static int MH_PREBOUND = 0x10;			/* the file has its dynamic undefined references prebound. */
		public final static int MH_SPLIT_SEGS = 0x20;		/* the file has its read-only and read-write segments split */
		public final static int MH_LAZY_INIT = 0x40;		/* the shared library init routine is to be run lazily via catching memory faults to its writeable segments (obsolete) */
		public final static int MH_TWOLEVEL = 0x80;			/* the image is using two-level name space bindings */
		public final static int MH_FORCE_FLAT = 0x100;		/* the executable is forcing all images to use flat name space bindings */
		public final static int MH_NOMULTIDEFS = 0x200;		/* this umbrella guarantees no multiple defintions of symbols in its sub-images so the two-level namespace hints can always be used. */
		public final static int MH_NOFIXPREBINDING = 0x400;	/* do not have dyld notify the prebinding agent about this executable */
		public final static int MH_PREBINDABLE = 0x800;     /* the binary is not prebound but can  have its prebinding redone. only used  when MH_PREBOUND is not set. */
		public final static int MH_ALLMODSBOUND = 0x1000;   /* indicates that this binary binds to all two-level namespace modules of  its dependent libraries. only used  when MH_PREBINDABLE and MH_TWOLEVEL are both set. */ 
		public final static int MH_SUBSECTIONS_VIA_SYMBOLS = 0x2000; /* safe to divide up the sections into sub-sections via symbols for dead code stripping */
		public final static int MH_CANONICAL = 0x4000;      /* the binary has been canonicalized via the unprebind operation */
		public final static int MH_WEAK_DEFINES = 0x8000;   /* the final linked image contains external weak symbols */
		public final static int MH_BINDS_TO_WEAK = 0x10000; /* the final linked image uses weak symbols */
		public final static int MH_ALLOW_STACK_EXECUTION = 0x20000; /* When this bit is set, all stacks in the task will be given stack execution privilege.  Only used in MH_EXECUTE filetypes. */
		public final static int MH_ROOT_SAFE = 0x40000;     /* When this bit is set, the binary declares it is safe for use in processes with uid zero */
		public final static int MH_SETUID_SAFE = 0x80000;   /* When this bit is set, the binary declares it is safe for use in processes when issetugid() is true */
		public final static int MH_NO_REEXPORTED_DYLIBS = 0x100000; /* When this bit is set on a dylib,the static linker does not need to examine dependent dylibs to see if any are re-exported */
		public final static int MH_PIE = 0x200000;          /* When this bit is set, the OS will load the main executable at a random address.  Only used in  MH_EXECUTE filetypes. */
        public int magic;		/* mach magic number identifier */
        public int cputype;		/* cpu specifier */
        public int cpusubtype;	/* machine specifier */
        public int filetype;	/* type of file */
        public int ncmds;		/* number of load commands */
        public int sizeofcmds;	/* the size of all the load commands */
        public int flags;		/* flags */
        public int reserved; 	// 64bit reserved

		protected MachOhdr() throws IOException {
			efile.seek(0);
			efile.setEndian(false);
			magic = efile.readIntE();
			if (magic == MH_CIGAM || magic == MH_CIGAM_64) {
				efile.setEndian(true);
				if (MH_CIGAM_64 == magic)
					b64 = true;
			} else if (magic == MH_UNIVERSAL) { 
				String arch = System.getProperty("os.arch"); //$NON-NLS-1$
				int numArchives = efile.readIntE();
				while (numArchives-- > 0) {
					int cpuType =       efile.readIntE(); // cpuType
					                    efile.readIntE(); // cpuSubType
					int archiveOffset = efile.readIntE(); // archiveOffset
									 	efile.readIntE(); // archiveSize
									 	efile.readIntE(); // archiveAlignment
					if ((cpuType == MachO64.MachOhdr.CPU_TYPE_I386 &&
							(arch.equalsIgnoreCase("i386") || arch.equalsIgnoreCase("x86_64"))) ||  //$NON-NLS-1$ //$NON-NLS-2$
							(cpuType == MachO64.MachOhdr.CPU_TYPE_POWERPC && arch.equalsIgnoreCase("ppc")) || //$NON-NLS-1$
							(cpuType == MachO64.MachOhdr.CPU_TYPE_X86_64 && arch.equalsIgnoreCase("x86_64"))) { //$NON-NLS-1$
						if (cpuType == MachO64.MachOhdr.CPU_TYPE_X86_64)
							b64 = true;
						efile.seek(archiveOffset);
						magic = efile.readIntE();
						if (magic == MH_CIGAM || magic == MH_CIGAM_64)
							efile.setEndian(true);
						else if (magic != MH_MAGIC && magic != MH_MAGIC_64)
							throw new IOException(CCorePlugin.getResourceString("Util.exception.notMACHO")); //$NON-NLS-1$
						break;
					}
				}
			} else if (magic != MH_MAGIC && magic != MH_MAGIC_64) {
				throw new IOException(CCorePlugin.getResourceString("Util.exception.notMACHO")); //$NON-NLS-1$
			}
			if (magic == MH_MAGIC_64 || magic == MH_CIGAM_64)
				b64 = true;
			cputype = efile.readIntE();
			cpusubtype = efile.readIntE();
			filetype = efile.readIntE();
			ncmds = efile.readIntE();
			sizeofcmds = efile.readIntE();
			flags = efile.readIntE();
			if (b64) {
				reserved = efile.readIntE();
			}
		}
		
		protected MachOhdr(byte [] bytes) throws IOException {
			boolean isle = false;
			int offset = 0;
			magic = makeInt(bytes, offset, isle); offset += 4;
			if (magic == MH_CIGAM || magic == MH_CIGAM_64) {
				isle = true;
				if (MH_CIGAM_64 == magic)
					b64 = true;
			} else if (magic == MH_UNIVERSAL) { 
				String arch = System.getProperty("os.arch"); //$NON-NLS-1$
				int numArchives = makeInt(bytes, offset, isle); offset += 4;
				while (numArchives-- > 0) {
					int cpuType = makeInt(bytes, offset, isle); offset += 4;
					offset += 4; // cpuSubType
					int archiveOffset = makeInt(bytes, offset, isle); offset += 4; 
					offset += 4; // archiveSize
					offset += 4; // archiveAlignment
					if ((cpuType == MachO64.MachOhdr.CPU_TYPE_I386 &&
							(arch.equalsIgnoreCase("i386") || arch.equalsIgnoreCase("x86_64"))) ||  //$NON-NLS-1$ //$NON-NLS-2$
							(cpuType == MachO64.MachOhdr.CPU_TYPE_POWERPC && arch.equalsIgnoreCase("ppc")) || //$NON-NLS-1$
							(cpuType == MachO64.MachOhdr.CPU_TYPE_X86_64 && arch.equalsIgnoreCase("x86_64"))) { //$NON-NLS-1$
						if (cpuType == MachO64.MachOhdr.CPU_TYPE_X86_64)
							b64 = true;
						offset = archiveOffset;
						magic = makeInt(bytes, offset, isle); offset += 4;
						if (magic == MH_CIGAM || magic == MH_CIGAM_64 )
							isle = true;
						else if (magic != MH_MAGIC && magic != MH_MAGIC_64)
							throw new IOException(CCorePlugin.getResourceString("Util.exception.notMACHO")); //$NON-NLS-1$
						break;
					}
				}
			} else if (magic != MH_MAGIC && magic != MH_MAGIC_64) {
				throw new IOException(CCorePlugin.getResourceString("Util.exception.notMACHO")); //$NON-NLS-1$
			}
			if (magic == MH_MAGIC_64 || magic == MH_CIGAM_64)
				b64 = true;
			cputype = makeInt(bytes, offset, isle); offset += 4;
			cpusubtype = makeInt(bytes, offset, isle); offset += 4;
			filetype = makeInt(bytes, offset, isle); offset += 4;
			ncmds = makeInt(bytes, offset, isle); offset += 4;
			sizeofcmds = makeInt(bytes, offset, isle); offset += 4;
			flags = makeInt(bytes, offset, isle); offset += 4;
			if (b64) {
				reserved = makeInt(bytes, offset, isle); offset += 4;
			}
		}
		
		public boolean is64() {
			return b64;
		}
	}

	private static final int makeInt(byte [] val, int offset, boolean isle) throws IOException {
		if (val.length < offset + 4)
			throw new IOException();
		if (isle ) {
			return (((val[offset + 3] & 0xff) << 24) |
					((val[offset + 2] & 0xff) << 16) |
					((val[offset + 1] & 0xff) << 8) |
					(val[offset + 0] & 0xff));
		} else {
			return (((val[offset + 0] & 0xff) << 24) |
					((val[offset + 1] & 0xff) << 16) |
					((val[offset + 2] & 0xff) << 8) |
					(val[offset + 3] & 0xff));
		}
	}

	public class LoadCommand {
		public final static int LC_REQ_DYLD = 0x80000000;

		/* values of cmd */
		public final static int LC_SEGMENT = 0x1;			/* segment of this file to be mapped */
		public final static int LC_SYMTAB = 0x2;			/* link-edit stab symbol table info */
		public final static int LC_SYMSEG = 0x3;			/* link-edit gdb symbol table info (obsolete) */
		public final static int LC_THREAD = 0x4;			/* thread */
		public final static int LC_UNIXTHREAD = 0x5;		/* unix thread (includes a stack) */
		public final static int LC_LOADFVMLIB = 0x6;		/* load a specified fixed VM shared library */
		public final static int LC_IDFVMLIB = 0x7;			/* fixed VM shared library identification */
		public final static int LC_IDENT = 0x8;				/* object identification info (obsolete) */
		public final static int LC_FVMFILE = 0x9;			/* fixed VM file inclusion (internal use) */
		public final static int LC_PREPAGE = 0xa;			/* prepage command (internal use) */
		public final static int LC_DYSYMTAB = 0xb;			/* dynamic link-edit symbol table info */
		public final static int LC_LOAD_DYLIB = 0xc;		/* load a dynamically linked shared library */
		public final static int LC_ID_DYLIB = 0xd;			/* dynamically linked shared lib ident */
		public final static int LC_LOAD_DYLINKER = 0xe;		/* load a dynamic linker */
		public final static int LC_ID_DYLINKER = 0xf;		/* dynamic linker identification */
		public final static int LC_PREBOUND_DYLIB = 0x10;	/* modules prebound for a dynamically linked shared library */
		public final static int LC_ROUTINES = 0x11;			/* image routines */
		public final static int LC_SUB_FRAMEWORK = 0x12;	/* sub framework */
		public final static int LC_SUB_UMBRELLA = 0x13;		/* sub umbrella */
		public final static int LC_SUB_CLIENT = 0x14;		/* sub client */
		public final static int LC_SUB_LIBRARY = 0x15;		/* sub library */
		public final static int LC_TWOLEVEL_HINTS = 0x16;	/* two-level namespace lookup hints */
		public final static int LC_PREBIND_CKSUM = 0x17;	/* prebind checksum */
		public final static int LC_SEGMENT_64 = 0x19;
		public final static int LC_ROUTINES_64 = 0x1a;
		public final static int LC_UUID = 0x1b;
		/*
		 * load a dynamically linked shared library that is allowed to be missing
		 * (all symbols are weak imported).
		 */
		public final static int LC_LOAD_WEAK_DYLIB = (0x18 | LC_REQ_DYLD);

		public int cmd;
		public int cmdsize;
	}
	
	public class UnknownCommand extends LoadCommand {
	}
	
	public class LCStr {
		public long offset;
		public long ptr;
	}
	
	public class SegmentCommand extends LoadCommand {
		/* values of flags */
		public final static long SG_HIGHVM = 0x1; 
		public final static long SG_FVMLIB = 0x2;
		public final static long SG_NORELOC = 0x4;

		/* VM protection values */
		public final static int VM_PROT_NONE = 0x00;
		public final static int VM_PROT_READ = 0x01;    /* read permission */
		public final static int VM_PROT_WRITE = 0x02;   /* write permission */
		public final static int VM_PROT_EXECUTE = 0x04; /* execute permission */
		public final static int VM_PROT_DEFAULT = (VM_PROT_READ|VM_PROT_WRITE);
		public final static int VM_PROT_ALL = (VM_PROT_READ|VM_PROT_WRITE|VM_PROT_EXECUTE);
		public final static int VM_PROT_NO_CHANGE = 0x08;
		public final static int VM_PROT_COPY = 0x10;
		public final static int VM_PROT_WANTS_COPY = 0x10;

        public String segname; 		/* segment name */
        public long vmaddr;        	/* memory address of this segment */
        public long vmsize;        	/* memory size of this segment */
        public long fileoff;        /* file offset of this segment */
        public long filesize;       /* amount to map from the file */
        public int maxprot;       	/* maximum VM protection */
        public int initprot;      	/* initial VM protection */
        public int nsects;        	/* number of sections in segment */
        public int flags;          	/* flags */
        
        public boolean prot(int val) {
       		return (initprot & val) == val;
        }
	}

	public class Section  {
		public final static int SECTION_TYP = 0x000000ff;				/* 256 section types */
		public final static int SECTION_ATTRIBUTES = 0xffffff00;		/*  24 section attributes */
		public final static int SECTION_ATTRIBUTES_USR =0xff000000;		/* User setable attributes */
		
		/* values of flags */
		public final static int S_REGULAR = 0x0;						/* regular section */
		public final static int S_ZEROFILL = 0x1;						/* zero fill on demand section */
		public final static int S_CSTRING_LITERALS = 0x2;				/* section with only literal C strings*/
		public final static int S_4BYTE_LITERALS = 0x3;					/* section with only 4 byte literals */
		public final static int S_8BYTE_LITERALS = 0x4;					/* section with only 8 byte literals */
		public final static int S_LITERAL_POINTERS = 0x5;				/* section with only pointers to literals */
		public final static int S_NON_LAZY_SYMBOL_POINTERS = 0x6;		/* section with only non-lazy symbol pointers */
		public final static int S_LAZY_SYMBOL_POINTERS = 0x7;			/* section with only lazy symbol pointers */
		public final static int S_SYMBOL_STUBS = 0x8;					/* section with only symbol stubs, byte size of stub in the reserved2 field */
		public final static int S_MOD_INIT_FUNC_POINTERS = 0x9;			/* section with only function pointers for initialization*/
		public final static int S_MOD_TERM_FUNC_POINTERS = 0xa;			/* section with only function pointers for termination */
		public final static int S_COALESCED = 0xb;						/* section contains symbols that are to be coalesced */
		public final static int S_ATTR_PURE_INSTRUCTIONS = 0x80000000;	/* section contains only true machine instructions */
		public final static int S_ATTR_NO_TOC =  0x40000000;			/* section contains coalesced symbols that are not to be in a ranlib table of contents */
		public final static int S_ATTR_STRIP_STATIC_SYMS = 0x20000000;	/* ok to strip static symbols in this section in files with the MH_DYLDLINK flag */
		public final static int SECTION_ATTRIBUTES_SYS = 0x00ffff00;	/* system setable attributes */
		public final static int S_ATTR_SOME_INSTRUCTIONS = 0x00000400;	/* section contains some machine instructions */
		public final static int S_ATTR_EXT_RELOC = 0x00000200;			/* section has external relocation entries */
		public final static int S_ATTR_LOC_RELOC = 0x00000100;			/* section has local relocation entries */
		
        public String sectname;			/* name of this section */
        public String segname;			/* name segment this section goes in */
        public SegmentCommand segment;	/* segment this section goes in */
        public long addr;				/* memory address of this section */
        public long size;				/* size in bytes of this section */
        public int offset;				/* file offset of this section */
        public int align;				/* section alignment (power of 2) */
        public int reloff;				/* file offset of relocation entries */
        public int nreloc;				/* number of relocation entries */
        public int flags;				/* flags (section type and attributes)*/
        public int reserved1;			/* reserved */
        public int reserved2;			/* reserved */
        public int reserved3;
        
        public int flags(int mask) {
       		return flags & mask;
        }
	}
	
	public class FVMLib {
        public int name;			/* library's target pathname */
        public int minor_version;	/* library's minor version number */
        public int header_addr;		/* library's header address */
        public String lc_str_name = null;
		
        @Override
		public String toString() {
			if (lc_str_name == null ) {
				return EMPTY_STRING;
			}
			return lc_str_name;
		}
	}
	public class MachUUID extends LoadCommand {
		public String uuid;
	}
	public class FVMLibCommand extends LoadCommand {
         public FVMLib fvmlib;	/* the library identification */
	}

	public class DyLib {
        public int name;						/* library's path name */
        public int timestamp;				/* library's build time stamp */
        public int current_version;			/* library's current version number */
        public int compatibility_version;	/* library's compatibility vers number*/
        public String lc_str_name = null;
		
        @Override
		public String toString() {
			if (lc_str_name == null ) {
				return EMPTY_STRING;
			}
			return lc_str_name;
		}
	}       
	        
	public class DyLibCommand extends LoadCommand {
	    public DyLib dylib;     /* the library identification */
	}       
	        
	public class SubFrameworkCommand extends LoadCommand {    
        public int umbrella; /* the umbrella framework name */
        public String lc_str_name = null;
		
        @Override
		public String toString() {
			if (lc_str_name == null ) {
				return EMPTY_STRING;
			}
			return lc_str_name;
		}
	}       
	        
	public class SubClientCommand extends LoadCommand {       
        public int client;   /* the client name */
        public String lc_str_name = null;
		
        @Override
		public String toString() {
			if (lc_str_name == null ) {
				return EMPTY_STRING;
			}
			return lc_str_name;
		}
	}
	
	public class SubUmbrellaCommand extends LoadCommand {     
        public int sub_umbrella;     /* the sub_umbrella framework name */
        public String lc_str_name = null;
		
        @Override
		public String toString() {
			if (lc_str_name == null ) {
				return EMPTY_STRING;
			}
			return lc_str_name;
		}
	}
	
	public class SubLibraryCommand extends LoadCommand { 
        public int sub_library;      /* the sub_library name */
        public String lc_str_name = null;
		
        @Override
		public String toString() {
			if (lc_str_name == null ) {
				return EMPTY_STRING;
			}
			return lc_str_name;
		}
	}

	public class PreboundDyLibCommand extends LoadCommand {           
	    public int name;           /* library's path name */
	    public int nmodules;         /* number of modules in library */        
	    public int linked_modules;   /* bit vector of linked modules */
        public String lc_str_name = null;
		
        @Override
		public String toString() {
			if (lc_str_name == null ) {
				return EMPTY_STRING;
			}
			return lc_str_name;
		}
	}

	public class DyLinkerCommand extends LoadCommand {
	    public int name;             /* dynamic linker's path name */
        public String lc_str_name = null;
		
        @Override
		public String toString() {
			if (lc_str_name == null ) {
				return EMPTY_STRING;
			}
			return lc_str_name;
		}
	}

	public class ThreadCommand extends LoadCommand {
	}

	public class RoutinesCommand extends LoadCommand {
	    public long init_address;       /* address of initialization routine */
	    public long init_module;        /* index into the module table that the init routine is defined in */
	    public long reserved1;
	    public long reserved2;
	    public long reserved3;        
	    public long reserved4;
	    public long reserved5;
	    public long reserved6;
	}
	
	public class SymtabCommand extends LoadCommand {
	    public int symoff;     /* symbol table offset */
	    public int nsyms;      /* number of symbol table entries */
	    public int stroff;     /* string table offset */
	    public int strsize;    /* string table size in bytes */
	}

	public class DySymtabCommand extends LoadCommand {         
        public int ilocalsym;          /* index to local symbols */
        public int nlocalsym;          /* number of local symbols */        
        public int iextdefsym;         /* index to externally defined symbols */               
        public int nextdefsym;         /* number of externally defined symbols */      
        public int iundefsym;          /* index to undefined symbols */
        public int nundefsym;          /* number of undefined symbols */
        public int tocoff;             /* file offset to table of contents */        
        public int ntoc;               /* number of entries in table of contents */    
        public int modtaboff;          /* file offset to module table */ 
        public int nmodtab;            /* number of module table entries */ 
        public int extrefsymoff;       /* offset to referenced symbol table */        
        public int nextrefsyms;        /* number of referenced symbol table entries */         
        public int indirectsymoff;     /* file offset to the indirect symbol table */          
        public int nindirectsyms;      /* number of indirect symbol table entries */
        public int extreloff;          /* offset to external relocation entries */
        public int nextrel;            /* number of external relocation entries */
        public int locreloff;          /* offset to local relocation entries */
        public int nlocrel;            /* number of local relocation entries */
	}

	public class DyLibTableOfContents {
        public final static int INDIRECT_SYMBOL_LOCAL = 0x80000000;
        public final static int INDIRECT_SYMBOL_ABS = 0x40000000;
        public int symbol_index;       /* the defined external symbol (index into the symbol table) */        
        public int module_index;       /* index into the module table this symbol is defined in */
	}

	public class DyLibModule {             
        public int module_name;                /* the module name (index into string table) */
        public int iextdefsym;                 /* index into externally defined symbols */
        public int nextdefsym;                 /* number of externally defined symbols */
        public int irefsym;                    /* index into reference symbol table */
        public int nrefsym;                    /* number of reference symbol table entries */ 
        public int ilocalsym;                  /* index into symbols for local symbols */        
        public int nlocalsym;                  /* number of local symbols */
        public int iextrel;                    /* index into external relocation entries */
        public int nextrel;                    /* number of external relocation entries */        
        public int iinit_iterm;                /* low 16 bits are the index into the init section, high 16 bits are the index into the term section */        
        public int ninit_nterm;                /* low 16 bits are the number of init section entries, high 16 bits are the number of term section entries */        
        public long objc_module_info_addr;      /* for this module address of the start of the (__OBJC,__module_info) section */        
        public int objc_module_info_size;      /* for this module size of the (__OBJC,__module_info) section */        
    }  
        
	public class DyLibReference {          
        public int isym;       /* index into the symbol table */
        public int flags;      /* flags to indicate the type of reference */
	}
	        
	public class TwoLevelHintsCommand extends LoadCommand {
        public int offset;     /* offset to the hint table */        
        public int nhints;     /* number of hints in the hint table */
        public TwoLevelHint[] hints;
	}

	public class TwoLevelHint {
        public int isub_image; /* index into the sub images */
        public int itoc;       /* index into the table of contents */
	}

	public class PrebindCksumCommand extends LoadCommand {
        public int cksum;      /* the check sum or zero */
	}

	public class SymSegCommand extends LoadCommand {   
        public int offset;     /* symbol segment offset */
        public int size;       /* symbol segment size in bytes */
	}

	public class IdentCommand extends LoadCommand {    
	}

	public class FVMFileCommand extends LoadCommand {          
        public int name;             /* files pathname */
        public int header_addr;      /* files virtual address */
        public String lc_str_name = null;
		
        @Override
		public String toString() {
			if (lc_str_name == null ) {
				return EMPTY_STRING;
			}
			return lc_str_name;
		}
	}

    private void commonSetup(String file, long offset, boolean filton ) throws IOException {
        this.cppFiltEnabled = filton;

		try {
	        efile = new ERandomAccessFile(file, "r"); //$NON-NLS-1$
    	    efile.setFileOffset(offset );
			mhdr = new MachOhdr();
			this.file = file;
		} finally {
			if (mhdr == null ) {
				dispose();
			}
		}
    }
    
	protected String string_from_macho_symtab(MachO64.SymtabCommand symtab, int index) throws IOException {
		if (index > symtab.strsize ) {
			return EMPTY_STRING;
		}
		efile.seek(symtab.stroff + index);
		return getCStr();
	}
	
	public class Symbol implements Comparable<Object> {
		/* n_type bit masks */
		public final static int N_STAB = 0xe0;
		public final static int N_PEXT = 0x10;
		public final static int N_EXT = 0x01;
		public final static int N_TYPE = 0x0e; /* type mask */
		/* Values of N_TYPE bits */
		public final static int N_UNDF = 0x0;
		public final static int N_ABS = 0x2;
		public final static int N_SECT = 0xe;
		public final static int N_PBUD = 0xc;
		public final static int N_INDR = 0xa;
		/* Values of n_type if N_STAB bits are set (stabs) */
		public final static int N_GSYM = 0x20;    /* global symbol: name,,NO_SECT,type,0 */
		public final static int N_FNAME = 0x22;   /* procedure name (f77 kludge): name,,NO_SECT,0,0 */
		public final static int N_FUN = 0x24;     /* procedure: name,,n_sect,linenumber,address */
		public final static int N_STSYM = 0x26;   /* static symbol: name,,n_sect,type,address */
		public final static int N_LCSYM = 0x28;   /* .lcomm symbol: name,,n_sect,type,address */
		public final static int N_BNSYM = 0x2e;   /* begin nsect sym: 0,,n_sect,0,address */
		public final static int N_OPT = 0x3c;     /* emitted with gcc2_compiled and in gcc source */
		public final static int N_RSYM = 0x40;    /* register sym: name,,NO_SECT,type,register */
		public final static int N_SLINE = 0x44;    /* src line: 0,,n_sect,linenumber,address */
		public final static int N_ENSYM = 0x4e;   /* end nsect sym: 0,,n_sect,0,address */
		public final static int N_SSYM = 0x60;    /* structure elt: name,,NO_SECT,type,struct_offset */
		public final static int N_SO = 0x64;      /* source file name: name,,n_sect,0,address */
		public final static int N_LSYM = 0x80;    /* local sym: name,,NO_SECT,type,offset */
		public final static int N_BINCL = 0x82;   /* include file beginning: name,,NO_SECT,0,sum */
		public final static int N_SOL = 0x84;     /* #included file name: name,,n_sect,0,address */
		public final static int N_PARAMS = 0x86;  /* compiler parameters: name,,NO_SECT,0,0 */
		public final static int N_VERSION = 0x88; /* compiler version: name,,NO_SECT,0,0 */
		public final static int N_OLEVEL = 0x8A;  /* compiler -O level: name,,NO_SECT,0,0 */
		public final static int N_PSYM = 0xa0;    /* parameter: name,,NO_SECT,type,offset */
		public final static int N_EINCL = 0xa2;   /* include file end: name,,NO_SECT,0,0 */
		public final static int N_ENTRY = 0xa4;   /* alternate entry: name,,n_sect,linenumber,address */
		public final static int N_LBRAC = 0xc0;   /* left bracket: 0,,NO_SECT,nesting level,address */
		public final static int N_EXCL = 0xc2;    /* deleted include file: name,,NO_SECT,0,sum */
		public final static int N_RBRAC = 0xe0;   /* right bracket: 0,,NO_SECT,nesting level,address */
		public final static int N_BCOMM = 0xe2;   /* begin common: name,,NO_SECT,0,0 */
		public final static int N_ECOMM = 0xe4;   /* end common: name,,n_sect,0,0 */
		public final static int N_ECOML = 0xe8;   /* end common (local name): 0,,n_sect,0,address */
		public final static int N_LENG = 0xfe;    /* second stab entry with length information */
		/* Values of n_sect */
		public final static int NO_SECT = 0;
		public final static int MAX_SECT = 255;
        /* Values of n_desc */
        public final static int REFERENCE_TYPE = 0xf; /* reference type mask */
        public final static int REFERENCE_FLAG_UNDEFINED_NON_LAZY = 0x0;
        public final static int REFERENCE_FLAG_UNDEFINED_LAZY = 0x1;
        public final static int REFERENCE_FLAG_DEFINED = 0x2;
        public final static int REFERENCE_FLAG_PRIVATE_DEFINED = 0x3; 
        public final static int REFERENCE_FLAG_PRIVATE_UNDEFINED_NON_LAZY = 0x4; 
        public final static int REFERENCE_FLAG_PRIVATE_UNDEFINED_LAZY = 0x5; 
        public final static int REFERENCED_DYNAMICALLY = 0x10; 
        public final static int N_DESC_DISCARDED = 0x20; 
        public final static int N_WEAK_REF = 0x40; 
        public final static int N_WEAK_DEF = 0x80; 
		
		public long n_strx;
		public long n_value;
		public short n_desc;
		public byte n_type;
		public byte n_sect;		
		public boolean is64;
		private String name = null;		/* symbol name */
		private Line line = null;		/* symbol line information */

		private String cppFilt(String in) {
            if (cppFiltEnabled) {
				try {
					if (in.indexOf("__") != -1 || in.indexOf("_._") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
						if (cppFilt == null) {
							cppFilt = new CPPFilt();
						}
						return cppFilt.getFunction(in);
					}
				} catch (IOException e) {
					return in;
				}
            }
			return in;
		}

        public Symbol() {
       	}

		public boolean n_type_mask(int mask) {
			return (n_type & mask) != 0;
		}
						
		public boolean n_type(int val) {
			return (n_type & N_TYPE) == val;
		}

		public boolean n_desc(int val) {
			return (n_type & REFERENCE_TYPE) == val;
		}

		@Override
		public int compareTo(Object obj) {
			long thisVal = 0;
			long anotherVal = 0;
			if (obj instanceof Symbol ) {
				Symbol sym = (Symbol)obj;
				thisVal = this.n_value;
				anotherVal = sym.n_value;
			} else if (obj instanceof Long ) {
				Long val = (Long)obj;
				anotherVal = val.longValue();
				thisVal = this.n_value;
			}
			return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
		}

		@Override
		public String toString() {
			if (n_strx == 0 || symtab == null) {
				return EMPTY_STRING;
			}
			if (name == null ) {
				try { 
					name = cppFilt(string_from_macho_symtab(symtab, (int)n_strx));
				} catch (IOException e ) {
					return EMPTY_STRING;
				}
			}
			return name;
		}

		/**
		 * Returns line information in the form of filename:line
		 * and if the information is not available may return null
		 * _or_ may return ??:??
		 */
		public String lineInfo() {
			if (!debugsym) {
				return null;
			}
			if (line == null) {
				long value = n_value;
				// We try to get the nearest match
				// since the symbol may not exactly align with debug info.
				// In C line number 0 is invalid, line starts at 1 for file, we use
				// this for validation.
				for (int i = 0; i <= 20; i += 4, value += i) {
					Line l = getLine(value);
					if (l != null && l.lineno != 0) {
						line = l;
						break; // bail out
					}
				}
			}
			if (line != null)
				return Integer.toString(line.lineno);
			return null;
		}
		
		public String lineInfo(long vma) {
			Line l = getLine(vma);
			if (l != null)
				return Integer.toString(l.lineno);
			return null;
		}
		
		/**
		 * If the function is available from the symbol information,
		 * this will return the function name. May return null if 
		 * the function can't be determined.
		 */
		public String getFunction() {
			if (line == null) {
				lineInfo();
			}
			if (line != null)
				return line.function;
			return null;
		}
			
		/**
		 * If the filename is available from the symbol information,
		 * this will return the base filename information. May
		 * return null if the filename can't be determined.
		 */
		public String getFilename() {
			if (line == null) {
				lineInfo();
			}
			if (line != null) 
				return line.file;
			return null;
		}

		/**
		 * Returns the line number of the function which is closest
		 * associated with the address if it is available.
		 * from the symbol information.  If it is not available,
		 * then -1 is returned.
		 */
		public int getFuncLineNumber() {
			if (line == null ) {
				lineInfo();
			}
			if (line == null) {
				return -1;
			}
			return line.lineno;
		}
		
		/**
		 * Returns the line number of the file if it is available
		 * from the symbol information.  If it is not available,
		 * then -1 is returned.  
		 */
		public int getLineNumber(long vma) {
			Line l = getLine(vma);
			if (l == null)
				return -1;
			return l.lineno;
		}
	}

	private Line getLine(long value) {
		if (!debugsym || lines == null) {
			return null;
		}
		
		int ndx = Arrays.binarySearch(lines, new Long(value));
		if (ndx >= 0 )
			return lines[ndx];
		if (ndx == -1 ) {
			return null;
		}
		ndx = -ndx - 1;
		for (int l = ndx - 1; l < lines.length; l++) {
			Line line = lines[l];
			if (value <= line.address)
				return line;
		}
		return null;
	}
	
	/**
	 * We have to implement a separate comparator since when we do the
	 * binary search down below we are using a Long and a Symbol object
	 * and the Long doesn't know how to compare against a Symbol so if
	 * we compare Symbol vs Long it is ok, but not if we do Long vs Symbol.
	 */
	public static class SymbolComparator implements Comparator<Object> {
		long val1, val2;
		@Override
		public int compare(Object o1, Object o2) {
			if (o1 instanceof Long) {
				val1 = ((Long) o1).longValue();
			} else if (o1 instanceof Symbol) {
				val1 = ((Symbol) o1).n_value;
			} else {
				return -1;
			}
			
			if (o2 instanceof Long) {
				val2 = ((Long) o2).longValue();
			} else if (o2 instanceof Symbol) {
				val2 = ((Symbol) o2).n_value;
			} else {
				return -1;
			}
			return val1 == val2 ? 0 : val1 < val2 ? -1 : 1;
		}
	}
	
	/**
	 * Simple class to implement a line table
	 */
	public static class Line implements Comparable<Object> {
		public long address;
		public int lineno;
		public String file;
		public String function;
		
		@Override
		public int compareTo(Object obj) {
			long thisVal = 0;
			long anotherVal = 0;
			if (obj instanceof Line ) {
				Line l = (Line)obj;
				thisVal = this.address;
				anotherVal = l.address;
			} else if (obj instanceof Long ) {
				Long val = (Long)obj;
				anotherVal = val.longValue();
				thisVal = this.address;
			}
			return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
		}
		
		@Override
		public boolean equals(Object obj) {
			Line line = (Line)obj;
			return (line.lineno == lineno && line.address == address);
		}
	}
	
    //A hollow entry, to be used with caution in controlled situations
    protected MachO64 () {
    }

	public MachO64 (String file, long offset) throws IOException {
        commonSetup(file, offset, true );
    }

    public MachO64 (String file) throws IOException {
        commonSetup(file, 0, true );
    }
     
    public MachO64 (String file, long offset, boolean filton) throws IOException {
        commonSetup(file, offset, filton );
    }

    public MachO64 (String file, boolean filton) throws IOException {
        commonSetup(file, 0, filton );
    }

    public boolean cppFilterEnabled() {
        return cppFiltEnabled;
    }

    public void setCppFilter(boolean enabled ) {
        cppFiltEnabled = enabled;
    }
  
	public MachOhdr getMachOhdr() throws IOException {	
		return mhdr;		
	}

	public class Attribute {
		public static final int MACHO_TYPE_OBJ = 1;
		public static final int MACHO_TYPE_EXE = 2;
		public static final int MACHO_TYPE_CORE = 3;
		public static final int MACHO_TYPE_SHLIB = 4;

		public static final int DEBUG_TYPE_NONE = 0;
		public static final int DEBUG_TYPE_STABS = 1;
		public static final int DEBUG_TYPE_DWARF = 2;

		String cpu;
		int type;
		int debugType;
		boolean bDebug;
		boolean isle;

		public String getCPU() {
			return cpu;
		}
		
		public int getType() {
			return type;
		}
		
		public boolean hasDebug() {
			return debugType != DEBUG_TYPE_NONE;
		}

		public int getDebugType() {
			return debugType;
		}
	
		public boolean isLittleEndian() {
			return isle;
		}
	}

    public Attribute getAttributes() throws IOException {
		Attribute attrib = new Attribute();
    
	    switch(mhdr.filetype ) {
    	case MachO64.MachOhdr.MH_OBJECT:
			attrib.type = Attribute.MACHO_TYPE_OBJ;
			break;
        case MachO64.MachOhdr.MH_EXECUTE:
        case MachO64.MachOhdr.MH_PRELOAD:
        case MachO64.MachOhdr.MH_BUNDLE:
        case MachO64.MachOhdr.MH_DYLINKER:
            attrib.type = Attribute.MACHO_TYPE_EXE;
            break;
        case MachO64.MachOhdr.MH_CORE:
            attrib.type = Attribute.MACHO_TYPE_CORE;
            break;
        case MachO64.MachOhdr.MH_DYLIB:
        case MachO64.MachOhdr.MH_FVMLIB:
           attrib.type = Attribute.MACHO_TYPE_SHLIB;
            break;
        }
	   
		switch (mhdr.cputype) {
		case MachO64.MachOhdr.CPU_TYPE_X86_64:
			attrib.cpu = "x86_64"; //$NON-NLS-1$
			break;
		case MachO64.MachOhdr.CPU_TYPE_I386:
			attrib.cpu = "x86"; //$NON-NLS-1$
			break;
		case MachO64.MachOhdr.CPU_TYPE_POWERPC:
			attrib.cpu = "ppc"; //$NON-NLS-1$
			break;
		case MachO64.MachOhdr.CPU_TYPE_VAX:
			attrib.cpu = "vax"; //$NON-NLS-1$
			break;
		case MachO64.MachOhdr.CPU_TYPE_MC680x0:
			attrib.cpu = "m68k"; //$NON-NLS-1$
			break;
		case MachO64.MachOhdr.CPU_TYPE_MC98000:
			attrib.cpu = "98000"; //$NON-NLS-1$
			break;
		case MachO64.MachOhdr.CPU_TYPE_MC88000:
			attrib.cpu = "88000"; //$NON-NLS-1$
			break;
		case MachO64.MachOhdr.CPU_TYPE_HPPA:
			attrib.cpu = "hp"; //$NON-NLS-1$
			break;
		case MachO64.MachOhdr.CPU_TYPE_SPARC:
			attrib.cpu = "sparc"; //$NON-NLS-1$
			break;
		case MachO64.MachOhdr.CPU_TYPE_I860:
			attrib.cpu = "i860"; //$NON-NLS-1$
			break;
		case MachO64.MachOhdr.CPU_TYPE_ANY:
		default:
			attrib.cpu = "any"; //$NON-NLS-1$
		}
		
		switch (mhdr.magic) {
		case MachO64.MachOhdr.MH_CIGAM_64:
		case MachO64.MachOhdr.MH_CIGAM:
			attrib.isle = true;
			break;
		case MachO64.MachOhdr.MH_MAGIC_64:
		case MachO64.MachOhdr.MH_MAGIC:
			attrib.isle = false;
			break;
		}
	
		if (debugsym) {
			attrib.debugType = Attribute.DEBUG_TYPE_STABS;
		}

        return attrib;
    }

	public static Attribute getAttributes(String file) throws IOException {
		MachO64 macho = new MachO64(file);
		Attribute attrib = macho.getAttributes();
		macho.dispose();	
		return attrib;	
	}

	public static Attribute getAttributes(byte [] array) throws IOException {
		MachO64 emptyMachO = new MachO64();
		emptyMachO.mhdr = emptyMachO.new MachOhdr(array);
		//emptyMachO.sections = new MachO64.Section[0];
		Attribute attrib = emptyMachO.getAttributes();
		emptyMachO.dispose();	
				
		return attrib;	
	}
	
	public static boolean isMachOHeader(byte[] bytes) {
		try {
			int magic = makeInt(bytes, 0, false);
			return (magic == MachO64.MachOhdr.MH_MAGIC || magic == MachO64.MachOhdr.MH_CIGAM || magic == MachO64.MachOhdr.MH_UNIVERSAL || magic == MachO64.MachOhdr.MH_CIGAM_64 || magic == MachO64.MachOhdr.MH_MAGIC_64);
		} catch (IOException e) {
			return false;
		}
	}

	public void dispose() {
		if (cppFilt != null) {
			cppFilt.dispose();
		}
		try {
			if (efile != null) {
				efile.close();
				efile = null;
			}
		} catch (IOException e) {
		}
	}

	/**
	 * Make sure we do not leak the fds.
	 */
	@Override
	protected void finalize() throws Throwable {
		try {
			dispose();
		} finally {
			super.finalize();
		}
	}
	
	private void loadSymbolTable() throws IOException {
		if (loadcommands == null) {
			return;
		}
		DySymtabCommand dysymtab = null;
		for (LoadCommand loadcommand : loadcommands) {
			switch (loadcommand.cmd) {
			case LoadCommand.LC_SYMTAB:
				symtab = (SymtabCommand)loadcommand;
				efile.seek(symtab.symoff);
				ArrayList<Symbol> symList = new ArrayList<Symbol>(symtab.nsyms);
				for (int s = 0; s < symtab.nsyms; s++) {
					Symbol symbol = new Symbol();
					symbol.is64 = b64;
					symbol.n_strx = efile.readIntE();
					symbol.n_type = (byte)efile.readUnsignedByte();
					symbol.n_sect = (byte)efile.readUnsignedByte();
					symbol.n_desc = efile.readShortE();
					// figure out 64 bit file an dload 64 bit symbols
					if (b64)
						symbol.n_value = efile.readLongE();
					else
						symbol.n_value = efile.readIntE();							
					symList.add(symbol);
					if ((symbol.n_type & Symbol.N_STAB) != 0) {
						debugsym = true;
					}
				}
				symbols = symList.toArray(new Symbol[0]);
				break;
				
			case LoadCommand.LC_DYSYMTAB:
				dysymtab = (DySymtabCommand)loadcommand;
				break;
			}
		}
		if (dysymtab != null) {
			ArrayList<Symbol> symList = new ArrayList<Symbol>(dysymtab.nlocalsym);
			for (int s = dysymtab.ilocalsym; s < dysymtab.nlocalsym; s++) {
				symList.add(symbols[s]);
			}
			local_symbols = symList.toArray(new Symbol[0]);
		}
	}
	
	private void loadLineTable() {
		if (symbols == null) {
			return;
		}
		/* count number of source line entries */
		int nlines = 0;
		for (Symbol symbol : symbols) {
			if (symbol.n_type == Symbol.N_SLINE || symbol.n_type == Symbol.N_FUN) {
				nlines++;
			}
		}
		if (nlines == 0) {
			return;
		}
		
		/* now create line table, sorted on address */
		Map<Line, Line> lineList = new HashMap<Line, Line>(nlines);
		for (Symbol sym : symbols) {
			if (sym.n_type == Symbol.N_SLINE || sym.n_type == Symbol.N_FUN) {
				Line lentry = new Line();
				lentry.address = sym.n_value;
				lentry.lineno = sym.n_desc;
				
				Line lookup = lineList.get(lentry);
				if (lookup != null) {
					lentry = lookup;
				} else {
					lineList.put(lentry, lentry);
				}
				
				if (lentry.function == null && sym.n_type == Symbol.N_FUN) {
					String func = sym.toString();
					if (func != null && func.length() > 0) {
						int colon = func.indexOf(':');
						if (colon > 0)
							lentry.function = func.substring(0, colon);
						else
							lentry.function = func;
					} else {
						lentry.function = EMPTY_STRING;
					}
				}
			}
		}
		Set<Line> k = lineList.keySet();
		lines = k.toArray(new Line[k.size()]);
		Arrays.sort(lines);
		
		/* now check for file names */
		for (Symbol sym : symbols) {
			if (sym.n_type == Symbol.N_SO) {
				Line line = getLine(sym.n_value);
				if (line != null) {
					line.file = sym.toString();
				}
			}
		}
	}
	
	private ArrayList<Section> getSections(SegmentCommand seg) throws IOException {
		if (seg.nsects == 0 ) {
			return new ArrayList<Section>();			
		}
		ArrayList<Section> sections = new ArrayList<Section>();
		for (int i = 0; i < seg.nsects; i++ ) {
			Section section = new Section();
			byte[] sectname = new byte[16];
			byte[] segname = new byte[16];
			efile.readFully(sectname);
			section.sectname = new String(sectname, 0, 16);
			efile.readFully(segname);
			section.segment = seg;
			section.segname = new String(segname, 0, 16);
			if (b64) {
				section.addr = efile.readLongE();
				section.size = efile.readLongE();
			} else {
				section.addr = efile.readIntE();
				section.size = efile.readIntE();				
			}
			section.offset = efile.readIntE();
			section.align = efile.readIntE();
			section.reloff = efile.readIntE();
			section.nreloc = efile.readIntE();
			section.flags = efile.readIntE();
			section.reserved1 = efile.readIntE();
			section.reserved2 = efile.readIntE();
			if (b64)
				section.reserved3 = efile.readInt();
			sections.add(section);
		}
		return sections;
	}

//	private TwoLevelHint[] getTwoLevelHints(int nhints) throws IOException {
//		if (nhints == 0 ) {
//			return new TwoLevelHint[0];			
//		}
//		TwoLevelHint[] tlhints = new TwoLevelHint[nhints];
//		for (int i = 0; i < nhints; i++ ) {
//			int field = efile.readIntE();
//			tlhints[i] = new TwoLevelHint();
//			tlhints[i].isub_image = (field & 0xff000000) >> 24;
//			tlhints[i].itoc = field & 0x00ffffff;
//		}
//		return tlhints;
//	}
	
	private String getCStr() throws IOException {
		StringBuffer str = new StringBuffer();
		while(true ) {
			byte tmp = efile.readByte();
			if (tmp == 0)
				break;
			str.append((char)tmp);
		}
		return str.toString();
	}
	
	private String getLCStr(int len) throws IOException {
		if (len == 0)
			return EMPTY_STRING;
		StringBuffer str = new StringBuffer();
		for (; len > 0; len--) {
			byte tmp = efile.readByte();
			if (tmp == 0)
				break;
			str.append((char)tmp);
		}
		return str.toString();
	}

	private void loadLoadCommands() throws IOException {
		if (loadcommands == null ) {
			if (mhdr.ncmds == 0 ) {
				loadcommands = new LoadCommand[0];			
				return;
			}
			loadcommands = new LoadCommand[mhdr.ncmds];
			for (int i = 0; i < mhdr.ncmds; i++ ) {
				int cmd = efile.readIntE();
				int len = efile.readIntE();
				
				switch (cmd) {
				case LoadCommand.LC_SEGMENT_64:
					SegmentCommand seg64 = new SegmentCommand();
					byte[] segname64 = new byte[16];
					seg64.cmd = cmd;
					seg64.cmdsize = len;
					efile.readFully(segname64);
					seg64.segname = new String(segname64, 0, 16);
					seg64.vmaddr = efile.readLongE();
					seg64.vmsize = efile.readLongE();
					seg64.fileoff = efile.readLongE();
					seg64.filesize = efile.readLongE();
			        seg64.maxprot = efile.readIntE();
			        seg64.initprot = efile.readIntE();
			        seg64.nsects = efile.readIntE();
			        seg64.flags = efile.readIntE();
			        sections.addAll(getSections(seg64));
			        loadcommands[i] = seg64;
					break;
				case LoadCommand.LC_SEGMENT:
					SegmentCommand seg = new SegmentCommand();
					byte[] segname = new byte[16];
					seg.cmd = cmd;
					seg.cmdsize = len;
					efile.readFully(segname);
					seg.segname = new String(segname, 0, 16);
					seg.vmaddr = efile.readIntE();
					seg.vmsize = efile.readIntE();
					seg.fileoff = efile.readIntE();
					seg.filesize = efile.readIntE();
			        seg.maxprot = efile.readIntE();
			        seg.initprot = efile.readIntE();
			        seg.nsects = efile.readIntE();
			        seg.flags = efile.readIntE();
			        sections.addAll(getSections(seg));
			        loadcommands[i] = seg;
					break;

				case LoadCommand.LC_SYMTAB:
					SymtabCommand stcmd = new SymtabCommand();
					stcmd.cmd = cmd;
					stcmd.cmdsize = len;
					stcmd.symoff = efile.readIntE();
					stcmd.nsyms = efile.readIntE();
					stcmd.stroff = efile.readIntE();
					stcmd.strsize = efile.readIntE();
					loadcommands[i] = stcmd;
					break;
					
				case LoadCommand.LC_SYMSEG:
					SymSegCommand sscmd = new SymSegCommand();
					sscmd.cmd = cmd;
					sscmd.cmdsize = len;
					sscmd.offset = efile.readIntE();
					sscmd.size = efile.readIntE();
					loadcommands[i] = sscmd;
					break;
				
				case LoadCommand.LC_THREAD:
				case LoadCommand.LC_UNIXTHREAD:
					ThreadCommand thcmd = new ThreadCommand();
					thcmd.cmd = cmd;
					thcmd.cmdsize = len;
					efile.skipBytes(len - 8);
					loadcommands[i] = thcmd;
					break;
				
				case LoadCommand.LC_LOADFVMLIB:
				case LoadCommand.LC_IDFVMLIB:
					FVMLibCommand fvmcmd = new FVMLibCommand();
					fvmcmd.cmd = cmd;
					fvmcmd.cmdsize = len;
					fvmcmd.fvmlib = new FVMLib();
					fvmcmd.fvmlib.name = efile.readIntE();
					fvmcmd.fvmlib.minor_version = efile.readIntE();
					fvmcmd.fvmlib.header_addr = efile.readIntE();
					len = fvmcmd.cmdsize - 20 /* sizeof FVMLibCommand */;
					fvmcmd.fvmlib.lc_str_name = getLCStr(len);
					len -= fvmcmd.fvmlib.lc_str_name.length() + 1;
					efile.skipBytes(len);
					loadcommands[i] = fvmcmd;
					break;

				case LoadCommand.LC_IDENT:
					IdentCommand icmd = new IdentCommand();
					icmd.cmd = cmd;
					icmd.cmdsize = len;
					loadcommands[i] = icmd;
					break;
					
				case LoadCommand.LC_FVMFILE:
					FVMFileCommand fcmd = new FVMFileCommand();
					fcmd.cmd = cmd;
					fcmd.cmdsize = len;
					fcmd.name = efile.readIntE();
					fcmd.header_addr = efile.readIntE();
					len = fcmd.cmdsize - 16 /* sizeof FVMFileCommand */;
					fcmd.lc_str_name = getLCStr(len);
					len -= fcmd.lc_str_name.length() + 1;
					efile.skipBytes(len);						
					loadcommands[i] = fcmd;
					break;

				case LoadCommand.LC_DYSYMTAB:
					DySymtabCommand dscmd = new DySymtabCommand();
					dscmd.cmd = cmd;
					dscmd.cmdsize = len;
					dscmd.ilocalsym = efile.readIntE();
					dscmd.nlocalsym = efile.readIntE();
					dscmd.iextdefsym = efile.readIntE();
					dscmd.nextdefsym = efile.readIntE();
					dscmd.iundefsym = efile.readIntE();
					dscmd.nundefsym = efile.readIntE();
					dscmd.tocoff = efile.readIntE();
					dscmd.ntoc = efile.readIntE();
					dscmd.modtaboff = efile.readIntE();
					dscmd.nmodtab = efile.readIntE();
					dscmd.extrefsymoff = efile.readIntE();
					dscmd.nextrefsyms = efile.readIntE();
					dscmd.indirectsymoff = efile.readIntE();
					dscmd.nindirectsyms = efile.readIntE();
					dscmd.extreloff = efile.readIntE();
					dscmd.nextrel = efile.readIntE();
					dscmd.locreloff = efile.readIntE();
					dscmd.nlocrel = efile.readIntE();
					loadcommands[i] = dscmd;
					dynsym = true;
					break;

				case LoadCommand.LC_LOAD_DYLIB:
				case LoadCommand.LC_ID_DYLIB:
				case LoadCommand.LC_LOAD_WEAK_DYLIB:
					DyLibCommand dylcmd = new DyLibCommand();
					dylcmd.cmd = cmd;
					dylcmd.cmdsize = len;
					dylcmd.dylib = new DyLib();
					dylcmd.dylib.name = efile.readIntE();
					dylcmd.dylib.timestamp = efile.readIntE();
					dylcmd.dylib.current_version = efile.readIntE();
					dylcmd.dylib.compatibility_version = efile.readIntE();
					len = dylcmd.cmdsize - 24 /* sizeof DyLibCommand */;
					dylcmd.dylib.lc_str_name = getLCStr(len);
					len -= dylcmd.dylib.lc_str_name.length() + 1;
					efile.skipBytes(len);
					loadcommands[i] = dylcmd;
					break;
		        	
				case LoadCommand.LC_LOAD_DYLINKER:
				case LoadCommand.LC_ID_DYLINKER:
					DyLinkerCommand dylkcmd = new DyLinkerCommand();
					dylkcmd.cmd = cmd;
					dylkcmd.cmdsize = len;
					dylkcmd.name = efile.readIntE();
					len = dylkcmd.cmdsize - 12 /* sizeof(DyLinkerCommand) */;
					dylkcmd.lc_str_name = getLCStr(len);
					len -= dylkcmd.lc_str_name.length() + 1;
					efile.skipBytes(len);
					loadcommands[i] = dylkcmd;
					break;
				
				case LoadCommand.LC_PREBOUND_DYLIB:
					PreboundDyLibCommand pbcmd = new PreboundDyLibCommand();
					pbcmd.cmd = cmd;
					pbcmd.cmdsize = len;
					pbcmd.name = efile.readIntE();
					pbcmd.nmodules = efile.readIntE();
					pbcmd.linked_modules = efile.readIntE();
					len = pbcmd.cmdsize - 20 /* sizeof(PreboundDyLibCommand) */;
					pbcmd.lc_str_name = getLCStr(len);
					len -= pbcmd.lc_str_name.length() + 1;
					efile.skipBytes(len);
					loadcommands[i] = pbcmd;
					break;

				case LoadCommand.LC_ROUTINES_64:
					RoutinesCommand rcmd64 = new RoutinesCommand();
					rcmd64.cmd = cmd;
					rcmd64.cmdsize = len;
					rcmd64.init_address = efile.readLongE();
					rcmd64.init_module = efile.readLongE();
					rcmd64.reserved1 = efile.readLongE();
					rcmd64.reserved2 = efile.readLongE();
					rcmd64.reserved3 = efile.readLongE();
					rcmd64.reserved4 = efile.readLongE();
					rcmd64.reserved5 = efile.readLongE();
					rcmd64.reserved6 = efile.readLongE();
					loadcommands[i] = rcmd64;
					break;

				case LoadCommand.LC_ROUTINES:
					RoutinesCommand rcmd = new RoutinesCommand();
					rcmd.cmd = cmd;
					rcmd.cmdsize = len;
					rcmd.init_address = efile.readIntE();
					rcmd.init_module = efile.readIntE();
					rcmd.reserved1 = efile.readIntE();
					rcmd.reserved2 = efile.readIntE();
					rcmd.reserved3 = efile.readIntE();
					rcmd.reserved4 = efile.readIntE();
					rcmd.reserved5 = efile.readIntE();
					rcmd.reserved6 = efile.readIntE();
					loadcommands[i] = rcmd;
					break;
				
				case LoadCommand.LC_SUB_FRAMEWORK:
					SubFrameworkCommand subfcmd = new SubFrameworkCommand();
					subfcmd.cmd = cmd;
					subfcmd.cmdsize = len;
					subfcmd.umbrella = efile.readIntE();
					len = subfcmd.cmdsize - 12 /* sizeof(SubFrameworkCommand) */   ;
					subfcmd.lc_str_name = getLCStr(len);
					len -= subfcmd.lc_str_name.length() + 1;
					efile.skipBytes(len);
					loadcommands[i] = subfcmd;
					break;

				case LoadCommand.LC_SUB_UMBRELLA:
					SubUmbrellaCommand subucmd = new SubUmbrellaCommand();
					subucmd.cmd = cmd;
					subucmd.cmdsize = len;
					subucmd.sub_umbrella = efile.readIntE();
					len = subucmd.cmdsize - 12 /* sizeof(SubUmbrellaCommand) */;
					subucmd.lc_str_name = getLCStr(len);
					len -= subucmd.lc_str_name.length() + 1;
					efile.skipBytes(len);
					loadcommands[i] = subucmd;
					break;

				case LoadCommand.LC_SUB_CLIENT:
					SubClientCommand subccmd = new SubClientCommand();
					subccmd.cmd = cmd;
					subccmd.cmdsize = len;
					subccmd.client = efile.readIntE();
					len = subccmd.cmdsize - 12 /* sizeof(SubClientCommand) */;
					subccmd.lc_str_name = getLCStr(len);
					len -= subccmd.lc_str_name.length() + 1;
					efile.skipBytes(len);
					loadcommands[i] = subccmd;
					break;
					
				case LoadCommand.LC_SUB_LIBRARY:
					SubLibraryCommand sublcmd = new SubLibraryCommand();
					sublcmd.cmd = cmd;
					sublcmd.cmdsize = len;
					sublcmd.sub_library = efile.readIntE();
					len = sublcmd.cmdsize - 12 /* sizeof(SubLibraryCommand) */;
					sublcmd.lc_str_name = getLCStr(len);
					len -= sublcmd.lc_str_name.length() + 1;
					efile.skipBytes(len);
					loadcommands[i] = sublcmd;
					break;
				
				case LoadCommand.LC_TWOLEVEL_HINTS:
					TwoLevelHintsCommand tlhcmd = new TwoLevelHintsCommand();
					tlhcmd.cmd = cmd;
					tlhcmd.cmdsize = len;
					tlhcmd.offset = efile.readIntE();
					tlhcmd.nhints = efile.readIntE();
					loadcommands[i] = tlhcmd;
					break;
				case LoadCommand.LC_UUID:
					MachUUID uuidCmd = new MachUUID();
					uuidCmd.cmd = cmd;
					uuidCmd.cmdsize = len;
					byte[] uuid = new byte[16];
					efile.readFully(uuid);
					uuidCmd.uuid = new String(uuid,0,16);
					loadcommands[i] = uuidCmd;
					break;
				case LoadCommand.LC_PREBIND_CKSUM:
					PrebindCksumCommand pbccmd = new PrebindCksumCommand();
					pbccmd.cmd = cmd;
					pbccmd.cmdsize = len;
					pbccmd.cksum = efile.readIntE();
					loadcommands[i] = pbccmd;
					break;
					
				default:
					// fallback, just in case we don't recognize the command
					UnknownCommand unknowncmd = new UnknownCommand();
					unknowncmd.cmd = cmd;
					unknowncmd.cmdsize = 0;
					loadcommands[i] = unknowncmd;
					break;
				}
			}
		}
	}

	public void loadBinary() throws IOException {
		if (loadcommands == null ) {
			loadLoadCommands();
			loadSymbolTable();
			loadLineTable();
		}
	}
	public boolean is64() {
		return b64;
	}
    public Symbol[] getSymbols() {
        return symbols;
    }

    public Symbol[] getDynamicSymbols() {
        if (dynsym) {
    		return symbols;
        }
        return null;
    }

    public Symbol[] getSymtabSymbols() {
        return symbols;
    }
    
    public Symbol[] getLocalSymbols() {
		if (local_symbols == null) {
			return symbols;
		}
		return local_symbols;
    }

    public Line[] getLineTable() {
		return lines;
    	}
    
    public Section[] getSections() {
        return sections.toArray(new Section[sections.size()]);
    }

    public DyLib[] getDyLibs(int type) {
		ArrayList<DyLib> v = new ArrayList<DyLib>();
		for (LoadCommand loadcommand : loadcommands) {
			if (loadcommand.cmd == type) {
				DyLibCommand dl = (DyLibCommand)loadcommand;
				v.add(dl.dylib);				
			}
		}
		return v.toArray(new DyLib[v.size()]);
    }
	
	/* return the address of the function that address is in */
	public Symbol getSymbol(long vma ) {
		if (symbols == null ) {
			return null;
		}

		int ndx = Arrays.binarySearch(symbols, new Long(vma), symbol_comparator);
		if (ndx > 0 )
			return symbols[ndx];
		if (ndx == -1 ) {
			return null;
		}
		ndx = -ndx - 1;
		return symbols[ndx-1];
	}
		
	public long swapInt(long val ) {
		if (mhdr.magic == MachOhdr.MH_CIGAM || mhdr.magic == MachOhdr.MH_CIGAM_64 ) {
			short tmp[] = new short[4];
			tmp[0] = (short)(val & 0x00ff);
			tmp[1] = (short)((val >> 8) & 0x00ff);
			tmp[2] = (short)((val >> 16) & 0x00ff); 
			tmp[3] = (short)((val >> 24) & 0x00ff);
			return ((tmp[0] << 24) + (tmp[1] << 16) + (tmp[2] << 8) + tmp[3]);
		}
		return val;
	}

	public int swapShort(short val ) {
		if (mhdr.magic == MachOhdr.MH_CIGAM || mhdr.magic == MachOhdr.MH_CIGAM_64) {
			short tmp[] = new short[2];
			tmp[0] = (short)(val & 0x00ff);
			tmp[1] = (short)((val >> 8) & 0x00ff);
			return (short)((tmp[0] << 8) + tmp[1]);
		}
		return val;
	}

    public String getFilename() {
        return file;
    }

	private ISymbolReader createStabsReader() {
		ISymbolReader symReader = null;

		try {
			if (loadcommands == null ) {
				loadLoadCommands();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		for (LoadCommand loadcommand : loadcommands) {
			if (loadcommand.cmd == LoadCommand.LC_SYMTAB) {
				symtab = (SymtabCommand)loadcommand;
				try {
					int symSize = symtab.nsyms * (mhdr.is64() ? StabConstant.SIZE_64 : StabConstant.SIZE);
					byte[] data = new byte[symSize];
					efile.seek(symtab.symoff);
					efile.readFully(data);
					byte[] stabstr = new byte[symtab.strsize];			
					efile.seek(symtab.stroff);
					efile.readFully(stabstr);
					symReader = new StabsReader(data, stabstr, getAttributes().isLittleEndian(),mhdr.is64());
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
		return symReader;
	}

	public Object getSymbolReader() {
		ISymbolReader reader = null;
		reader = createStabsReader();
		return reader;
	}
}
