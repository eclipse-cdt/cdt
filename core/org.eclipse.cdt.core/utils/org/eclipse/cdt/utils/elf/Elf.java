/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.utils.elf;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr32Factory;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.cdt.utils.Addr64Factory;
import org.eclipse.cdt.utils.ERandomAccessFile;

// test checkin
public class Elf {

	public final static int ELF32_ADDR_SIZE = 4;
	public final static int ELF32_OFF_SIZE = 4;
	public final static int ELF64_ADDR_SIZE = 8;
	public final static int ELF64_OFF_SIZE = 8;

	protected ERandomAccessFile efile;

	protected ELFhdr ehdr;
	protected Section[] sections;
	protected String file;
	protected byte[] section_strtab;

	private int syms = 0;
	private Symbol[] symbols;
	private Symbol[] symtab_symbols;
	private Section symtab_sym;
	private Symbol[] dynsym_symbols;
	private Section dynsym_sym;

	protected String EMPTY_STRING = ""; //$NON-NLS-1$

	public class ELFhdr {

		/* e_ident offsets */
		public final static int EI_MAG0 = 0;
		public final static int EI_MAG1 = 1;
		public final static int EI_MAG2 = 2;
		public final static int EI_MAG3 = 3;
		public final static int EI_CLASS = 4;
		public final static int EI_DATA = 5;
		public final static int EI_VERSION = 6;
		public final static int EI_PAD = 7;
		public final static int EI_NDENT = 16;

		/* e_ident[EI_CLASS] */
		public final static int ELFCLASSNONE = 0;
		public final static int ELFCLASS32 = 1;
		public final static int ELFCLASS64 = 2;

		/* e_ident[EI_DATA] */
		public final static int ELFDATANONE = 0;
		public final static int ELFDATA2LSB = 1;
		public final static int ELFDATA2MSB = 2;

		/* values of e_type */
		public final static int ET_NONE = 0;
		public final static int ET_REL = 1;
		public final static int ET_EXEC = 2;
		public final static int ET_DYN = 3;
		public final static int ET_CORE = 4;
		public final static int ET_LOPROC = 0xff00;
		public final static int ET_HIPROC = 0xffff;

		/* values of e_machine */
		public final static int EM_NONE = 0;
		public final static int EM_M32 = 1;
		public final static int EM_SPARC = 2;
		public final static int EM_386 = 3;
		public final static int EM_68K = 4;
		public final static int EM_88K = 5;
		public final static int EM_486 = 6;
		public final static int EM_860 = 7;
		public final static int EM_MIPS = 8;
		public final static int EM_MIPS_RS3_LE = 10;
		public final static int EM_RS6000 = 11;
		public final static int EM_PARISC = 15;
		public final static int EM_nCUBE = 16;
		public final static int EM_VPP550 = 17;
		public final static int EM_SPARC32PLUS = 18;
		public final static int EM_PPC = 20;
		public final static int EM_PPC64 = 21;
		public final static int EM_ARM = 40;
		public final static int EM_SH = 42;
		public final static int EM_SPARCV9 = 43;
		public final static int EM_H8_300 = 46;
		public final static int EM_H8_300H = 47;
		public final static int EM_IA_64 = 50;
		public final static int EM_AVR = 83;
		public final static int EM_FR30 = 84; /* Fujitsu FR30 */
		public final static int EM_V850 = 87;
		public final static int EM_M32R = 88;
		public final static int EM_MN10300 = 89;
		public final static int EM_MN10200 = 90;
		public final static int EM_MSP430 = 105;
		public final static int EM_EXCESS = 111;
		public final static int EM_NIOSII = 113;
		public final static int EM_NIOS = 0xFEBB;
		public final static int EM_CYGNUS_POWERPC = 0x9025;
		public final static int EM_CYGNUS_M32R = 0x9041;
		public final static int EM_CYGNUS_V850 = 0x9080;
		public final static int EM_CYGNUS_MN10200 = 0xdead;
		public final static int EM_CYGNUS_MN10300 = 0xbeef;
		public final static int EM_CYGNUS_FR30 = 0x3330;
		public final static int EM_XSTORMY16 = 0xad45;
		public final static int EM_CYGNUS_FRV = 0x5441;
		public final static int EM_IQ2000 = 0xFEBA;

		public byte e_ident[] = new byte[EI_NDENT];
		public int e_type; /* file type (Elf32_Half) */
		public int e_machine; /* machine type (Elf32_Half) */
		public long e_version; /* version number (Elf32_Word) */
		public IAddress e_entry; /* entry point (Elf32_Addr) */
		public long e_phoff; /* Program hdr offset (Elf32_Off) */
		public long e_shoff; /* Section hdr offset (Elf32_Off) */
		public long e_flags; /* Processor flags (Elf32_Word) */
		public short e_ehsize; /* sizeof ehdr (Elf32_Half) */
		public short e_phentsize; /* Program header entry size (Elf32_Half) */
		public short e_phnum; /* Number of program headers (Elf32_Half) */
		public short e_shentsize; /* Section header entry size (Elf32_Half) */
		public short e_shnum; /* Number of section headers (Elf32_Half) */
		public short e_shstrndx; /* String table index (Elf32_Half) */

		protected ELFhdr() throws IOException {
			efile.seek(0);
			efile.readFully(e_ident);
			if (e_ident[ELFhdr.EI_MAG0] != 0x7f || e_ident[ELFhdr.EI_MAG1] != 'E' || e_ident[ELFhdr.EI_MAG2] != 'L'
					|| e_ident[ELFhdr.EI_MAG3] != 'F')
				throw new IOException(CCorePlugin.getResourceString("Util.exception.notELF")); //$NON-NLS-1$
			efile.setEndian(e_ident[ELFhdr.EI_DATA] == ELFhdr.ELFDATA2LSB);
			e_type = efile.readShortE();
			e_machine = efile.readShortE();
			e_version = efile.readIntE();
			switch (e_ident[ELFhdr.EI_CLASS]) {
				case ELFhdr.ELFCLASS32 : {
					byte[] addrArray = new byte[ELF32_ADDR_SIZE];
					efile.readFullyE(addrArray);
					e_entry = new Addr32(addrArray);
					e_phoff = efile.readIntE();
					e_shoff = efile.readIntE();
				}
					break;
				case ELFhdr.ELFCLASS64 : {
					byte[] addrArray = new byte[ELF64_ADDR_SIZE];
					efile.readFullyE(addrArray);
					e_entry = new Addr64(addrArray);
					e_phoff = readUnsignedLong(efile);
					e_shoff = readUnsignedLong(efile);
				}
					break;
				case ELFhdr.ELFCLASSNONE :
				default :
					throw new IOException("Unknown ELF class " + e_ident[ELFhdr.EI_CLASS]); //$NON-NLS-1$
			}
			e_flags = efile.readIntE();
			e_ehsize = efile.readShortE();
			e_phentsize = efile.readShortE();
			e_phnum = efile.readShortE();
			e_shentsize = efile.readShortE();
			e_shnum = efile.readShortE();
			e_shstrndx = efile.readShortE();
		}

		protected ELFhdr(byte[] bytes) throws IOException {
			if (bytes.length <= e_ident.length) {
				throw new EOFException(CCorePlugin.getResourceString("Util.exception.notELF")); //$NON-NLS-1$
			}
			System.arraycopy(bytes, 0, e_ident, 0, e_ident.length);
			if (e_ident[ELFhdr.EI_MAG0] != 0x7f || e_ident[ELFhdr.EI_MAG1] != 'E' || e_ident[ELFhdr.EI_MAG2] != 'L'
					|| e_ident[ELFhdr.EI_MAG3] != 'F')
				throw new IOException(CCorePlugin.getResourceString("Util.exception.notELF")); //$NON-NLS-1$
			boolean isle = (e_ident[ELFhdr.EI_DATA] == ELFhdr.ELFDATA2LSB);
			int offset = e_ident.length;
			e_type = makeShort(bytes, offset, isle);
			offset += 2;
			e_machine = makeShort(bytes, offset, isle);
			offset += 2;
			e_version = makeInt(bytes, offset, isle);
			offset += 4;
			switch (e_ident[ELFhdr.EI_CLASS]) {
				case ELFhdr.ELFCLASS32 : {
					byte[] addrArray = new byte[ELF32_ADDR_SIZE];
					System.arraycopy(bytes, offset, addrArray, 0, ELF32_ADDR_SIZE);
					offset += ELF32_ADDR_SIZE;
					e_entry = new Addr32(addrArray);
					e_phoff = makeInt(bytes, offset, isle);
					offset += ELF32_OFF_SIZE;
					e_shoff = makeInt(bytes, offset, isle);
					offset += ELF32_OFF_SIZE;
				}
					break;
				case ELFhdr.ELFCLASS64 : {
					byte[] addrArray = new byte[ELF64_ADDR_SIZE];
					System.arraycopy(bytes, offset, addrArray, 0, ELF64_ADDR_SIZE);
					offset += ELF64_ADDR_SIZE;
					e_entry = new Addr64(addrArray);
					e_phoff = makeUnsignedLong(bytes, offset, isle);
					offset += ELF64_OFF_SIZE;
					e_shoff = makeUnsignedLong(bytes, offset, isle);
					offset += ELF64_OFF_SIZE;
				}
					break;
				case ELFhdr.ELFCLASSNONE :
				default :
					throw new IOException("Unknown ELF class " + e_ident[ELFhdr.EI_CLASS]); //$NON-NLS-1$
			}
			e_flags = makeInt(bytes, offset, isle);
			offset += 4;
			e_ehsize = makeShort(bytes, offset, isle);
			offset += 2;
			e_phentsize = makeShort(bytes, offset, isle);
			offset += 2;
			e_phnum = makeShort(bytes, offset, isle);
			offset += 2;
			e_shentsize = makeShort(bytes, offset, isle);
			offset += 2;
			e_shnum = makeShort(bytes, offset, isle);
			offset += 2;
			e_shstrndx = makeShort(bytes, offset, isle);
			offset += 2;
		}

		private final short makeShort(byte[] val, int offset, boolean isle) throws IOException {
			if (val.length < offset + 2)
				throw new IOException();
			if (isle) {
				return (short) ( (val[offset + 1] << 8) + val[offset + 0]);
			}
			return (short) ( (val[offset + 0] << 8) + val[offset + 1]);
		}

		private final long makeInt(byte[] val, int offset, boolean isle) throws IOException {
			if (val.length < offset + 4)
				throw new IOException();
			if (isle) {
				return ( (val[offset + 3] << 24) + (val[offset + 2] << 16) + (val[offset + 1] << 8) + val[offset + 0]);
			}
			return ( (val[offset + 0] << 24) + (val[offset + 1] << 16) + (val[offset + 2] << 8) + val[offset + 3]);
		}

		private final long makeLong(byte[] val, int offset, boolean isle) throws IOException {
			long result = 0;
			int shift = 0;
			if (isle)
				for (int i = 7; i >= 0; i--) {
					shift = i * 8;
					result += ( ((long)val[offset + i]) << shift) & (0xffL << shift);
				}
			else
				for (int i = 0; i <= 7; i++) {
					shift = (7 - i) * 8;
					result += ( ((long)val[offset + i]) << shift) & (0xffL << shift);
				}
			return result;
		}

		private final long makeUnsignedLong(byte[] val, int offset, boolean isle) throws IOException {
			long result = makeLong(val, offset, isle);
			if (result < 0) {
				throw new IOException("Maximal file offset is " + Long.toHexString(Long.MAX_VALUE) + //$NON-NLS-1$
						" given offset is " + Long.toHexString(result)); //$NON-NLS-1$
			}
			return result;

		}

	}

	public class Section {

		/* sh_type */
		public final static int SHT_NULL = 0;
		public final static int SHT_PROGBITS = 1;
		public final static int SHT_SYMTAB = 2;
		public final static int SHT_STRTAB = 3;
		public final static int SHT_RELA = 4;
		public final static int SHT_HASH = 5;
		public final static int SHT_DYNAMIC = 6;
		public final static int SHT_NOTE = 7;
		public final static int SHT_NOBITS = 8;
		public final static int SHT_REL = 9;
		public final static int SHT_SHLIB = 10;
		public final static int SHT_DYNSYM = 11;

		public final static int SHT_LOPROC = 0x70000000;

		/* sh_flags */
		public final static int SHF_WRITE = 1;
		public final static int SHF_ALLOC = 2;
		public final static int SHF_EXECINTR = 4;

		public long sh_name;
		public long sh_type;
		public long sh_flags;
		public IAddress sh_addr;
		public long sh_offset;
		public long sh_size;
		public long sh_link;
		public long sh_info;
		public long sh_addralign;
		public long sh_entsize;

		public byte[] loadSectionData() throws IOException {
			byte[] data = new byte[(int)sh_size];
			efile.seek(sh_offset);
			efile.read(data);
			return data;
		}

		public String toString() {
			try {
				if (section_strtab == null) {
					if (ehdr.e_shstrndx > sections.length || ehdr.e_shstrndx < 0)
						return EMPTY_STRING;
					int size = (int)sections[ehdr.e_shstrndx].sh_size;
					if (size <= 0 || size > efile.length())
						return EMPTY_STRING;
					section_strtab = new byte[size];
					efile.seek(sections[ehdr.e_shstrndx].sh_offset);
					efile.read(section_strtab);
				}
				int str_size = 0;
				if (sh_name > section_strtab.length) {
					return EMPTY_STRING;
				}
				while (section_strtab[(int)sh_name + str_size] != 0)
					str_size++;
				return new String(section_strtab, (int)sh_name, str_size);
			} catch (IOException e) {
				return EMPTY_STRING;
			}
		}
	}

	protected String string_from_elf_section(Elf.Section section, int index) throws IOException {
		StringBuffer str = new StringBuffer();
		byte tmp;
		if (index > section.sh_size) {
			return EMPTY_STRING;
		}
		efile.seek(section.sh_offset + index);
		while (true) {
			tmp = efile.readByte();
			if (tmp == 0)
				break;
			str.append((char)tmp);
		}
		return str.toString();
	}

	public class Symbol implements Comparable {

		/* Symbol bindings */
		public final static int STB_LOCAL = 0;
		public final static int STB_GLOBAL = 1;
		public final static int STB_WEAK = 2;
		/* Symbol type */
		public final static int STT_NOTYPE = 0;
		public final static int STT_OBJECT = 1;
		public final static int STT_FUNC = 2;
		public final static int STT_SECTION = 3;
		public final static int STT_FILE = 4;
		/* Special Indexes */
		public final static int SHN_UNDEF = 0;
		public final static int SHN_LORESERVE = 0xffffff00;
		public final static int SHN_LOPROC = 0xffffff00;
		public final static int SHN_HIPROC = 0xffffff1f;
		public final static int SHN_LOOS = 0xffffff20;
		public final static int SHN_HIOS = 0xffffff3f;
		public final static int SHN_ABS = 0xfffffff1;
		public final static int SHN_COMMON = 0xfffffff2;
		public final static int SHN_XINDEX = 0xffffffff;
		public final static int SHN_HIRESERVE = 0xffffffff;

		/* NOTE: 64 bit and 32 bit ELF sections has different order */
		public long st_name;
		public IAddress st_value;
		public long st_size;
		public short st_info;
		public short st_other;
		public short st_shndx;

		private String name = null;

		private Section sym_section;

		public Symbol(Section section) {
			sym_section = section;
		}

		public int st_type() {
			return st_info & 0xf;
		}

		public int st_bind() {
			return (st_info >> 4) & 0xf;
		}

		public int compareTo(Object obj) {
			/*
			 * long thisVal = 0; long anotherVal = 0; if ( obj instanceof Symbol ) {
			 * Symbol sym = (Symbol)obj; thisVal = this.st_value; anotherVal =
			 * sym.st_value; } else if ( obj instanceof Long ) { Long val =
			 * (Long)obj; anotherVal = val.longValue(); thisVal = this.st_value; }
			 * return (thisVal <anotherVal ? -1 : (thisVal==anotherVal ? 0 :
			 * 1));
			 */
			return this.st_value.compareTo( ((Symbol)obj).st_value);
		}

		public String toString() {
			if (name == null) {
				try {
					Section sections[] = getSections();
					Section symstr = sections[(int)sym_section.sh_link];
					name = string_from_elf_section(symstr, (int)st_name);
				} catch (IOException e) {
					return EMPTY_STRING;
				}
			}
			return name;
		}

	}

	/**
	 * We have to implement a separate compararator since when we do the binary
	 * search down below we are using a Long and a Symbol object and the Long
	 * doesn't know how to compare against a Symbol so if we compare Symbol vs
	 * Long it is ok, but not if we do Long vs Symbol.
	 */

	class SymbolComparator implements Comparator {

		IAddress val1, val2;
		public int compare(Object o1, Object o2) {

			if (o1 instanceof IAddress) {
				val1 = (IAddress)o1;
			} else if (o1 instanceof Symbol) {
				val1 = ((Symbol)o1).st_value;
			} else {
				return -1;
			}

			if (o2 instanceof IAddress) {
				val2 = (IAddress)o2;
			} else if (o2 instanceof Symbol) {
				val2 = ((Symbol)o2).st_value;
			} else {
				return -1;
			}
			return val1.compareTo(val2);
		}
	}

	public class PHdr {

		public final static int PT_NULL = 0;
		public final static int PT_LOAD = 1;
		public final static int PT_DYNAMIC = 2;
		public final static int PT_INTERP = 3;
		public final static int PT_NOTE = 4;
		public final static int PT_SHLIB = 5;
		public final static int PT_PHDR = 6;

		public final static int PF_X = 1;
		public final static int PF_W = 2;
		public final static int PF_R = 4;
		/* NOTE: 64 bit and 32 bit ELF have different order and size of elements */
		public long p_type;
		public long p_offset;
		public IAddress p_vaddr;
		public IAddress p_paddr;
		public long p_filesz;
		public long p_memsz;
		public long p_flags;
		public long p_align;
	}

	public PHdr[] getPHdrs() throws IOException {
		if (ehdr.e_phnum == 0) {
			return new PHdr[0];
		}
		efile.seek(ehdr.e_phoff);
		PHdr phdrs[] = new PHdr[ehdr.e_phnum];
		for (int i = 0; i < ehdr.e_phnum; i++) {
			phdrs[i] = new PHdr();
			switch (ehdr.e_ident[ELFhdr.EI_CLASS]) {
				case ELFhdr.ELFCLASS32 : {
					byte[] addrArray = new byte[ELF32_ADDR_SIZE];

					phdrs[i].p_type = efile.readIntE();
					phdrs[i].p_offset = efile.readIntE();
					efile.readFullyE(addrArray);
					phdrs[i].p_vaddr = new Addr32(addrArray);
					efile.readFullyE(addrArray);
					phdrs[i].p_paddr = new Addr32(addrArray);
					phdrs[i].p_filesz = efile.readIntE();
					phdrs[i].p_memsz = efile.readIntE();
					phdrs[i].p_flags = efile.readIntE();
					phdrs[i].p_align = efile.readIntE();
				}
					break;
				case ELFhdr.ELFCLASS64 : {
					byte[] addrArray = new byte[ELF64_ADDR_SIZE];

					phdrs[i].p_type = efile.readIntE();
					phdrs[i].p_flags = efile.readIntE();
					phdrs[i].p_offset = readUnsignedLong(efile);
					efile.readFullyE(addrArray);
					phdrs[i].p_vaddr = new Addr64(addrArray);
					efile.readFullyE(addrArray);
					phdrs[i].p_paddr = new Addr64(addrArray);
					phdrs[i].p_filesz = readUnsignedLong(efile);
					phdrs[i].p_memsz = readUnsignedLong(efile);
					phdrs[i].p_align = readUnsignedLong(efile);
				}
					break;
				case ELFhdr.ELFCLASSNONE :
				default :
					throw new IOException("Unknown ELF class " + ehdr.e_ident[ELFhdr.EI_CLASS]); //$NON-NLS-1$
			}

		}
		return phdrs;
	}

	public class Dynamic {

		public final static int DYN_ENT_SIZE_32 = 8;
		public final static int DYN_ENT_SIZE_64 = 16;

		public final static int DT_NULL = 0;
		public final static int DT_NEEDED = 1;
		public final static int DT_PLTRELSZ = 2;
		public final static int DT_PLTGOT = 3;
		public final static int DT_HASH = 4;
		public final static int DT_STRTAB = 5;
		public final static int DT_SYMTAB = 6;
		public final static int DT_RELA = 7;
		public final static int DT_RELASZ = 8;
		public final static int DT_RELAENT = 9;
		public final static int DT_STRSZ = 10;
		public final static int DT_SYMENT = 11;
		public final static int DT_INIT = 12;
		public final static int DT_FINI = 13;
		public final static int DT_SONAME = 14;
		public final static int DT_RPATH = 15;
		public long d_tag;
		public long d_val;
		private Section section;
		private String name;

		protected Dynamic(Section section) {
			this.section = section;
		}

		public String toString() {
			if (name == null) {
				switch ((int)d_tag) {
					case DT_NEEDED :
					case DT_SONAME :
					case DT_RPATH :
						try {
							Section symstr = sections[(int)section.sh_link];
							name = string_from_elf_section(symstr, (int)d_val);
						} catch (IOException e) {
							name = EMPTY_STRING;
						}
						break;
					default :
						name = EMPTY_STRING;
				}
			}
			return name;
		}
	}

	public Dynamic[] getDynamicSections(Section section) throws IOException {
		if (section.sh_type != Section.SHT_DYNAMIC) {
			return new Dynamic[0];
		}
		ArrayList dynList = new ArrayList();
		efile.seek(section.sh_offset);
		int off = 0;
		// We must assume the section is a table ignoring the sh_entsize as it
		// is not
		// set for MIPS.
		while (off < section.sh_size) {
			Dynamic dynEnt = new Dynamic(section);
			switch (ehdr.e_ident[ELFhdr.EI_CLASS]) {
				case ELFhdr.ELFCLASS32 : {
					dynEnt.d_tag = efile.readIntE();
					dynEnt.d_val = efile.readIntE();
					off += Dynamic.DYN_ENT_SIZE_32;
				}
					break;
				case ELFhdr.ELFCLASS64 : {
					dynEnt.d_tag = efile.readLongE();
					dynEnt.d_val = efile.readLongE();
					off += Dynamic.DYN_ENT_SIZE_64;
				}
					break;
				case ELFhdr.ELFCLASSNONE :
				default :
					throw new IOException("Unknown ELF class " + ehdr.e_ident[ELFhdr.EI_CLASS]); //$NON-NLS-1$
			}

			if (dynEnt.d_tag != Dynamic.DT_NULL)
				dynList.add(dynEnt);
		}
		return (Dynamic[])dynList.toArray(new Dynamic[0]);
	}

	private void commonSetup(String file, long offset) throws IOException {
		try {
			efile = new ERandomAccessFile(file, "r"); //$NON-NLS-1$
			efile.setFileOffset(offset);
			ehdr = new ELFhdr();
			this.file = file;
		} finally {
			if (ehdr == null) {
				dispose();
			}
		}
	}

	//A hollow entry, to be used with caution in controlled situations
	protected Elf() {
	}

	public Elf(String file, long offset) throws IOException {
		commonSetup(file, offset);
	}

	public Elf(String file) throws IOException {
		commonSetup(file, 0);
	}

	public ELFhdr getELFhdr() throws IOException {
		return ehdr;
	}

	public class Attribute {

		public static final int ELF_TYPE_EXE = 1;
		public static final int ELF_TYPE_SHLIB = 2;
		public static final int ELF_TYPE_OBJ = 3;
		public static final int ELF_TYPE_CORE = 4;

		public static final int DEBUG_TYPE_NONE = 0;
		public static final int DEBUG_TYPE_STABS = 1;
		public static final int DEBUG_TYPE_DWARF = 2;

		String cpu;
		int type;
		int debugType;
		boolean bDebug;
		boolean isle;
		IAddressFactory addressFactory;

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

		public IAddressFactory getAddressFactory() {
			return addressFactory;
		}
	}

	public Attribute getAttributes() throws IOException {
		Attribute attrib = new Attribute();

		switch (ehdr.e_type) {
			case Elf.ELFhdr.ET_CORE :
				attrib.type = Attribute.ELF_TYPE_CORE;
				break;
			case Elf.ELFhdr.ET_EXEC :
				attrib.type = Attribute.ELF_TYPE_EXE;
				break;
			case Elf.ELFhdr.ET_REL :
				attrib.type = Attribute.ELF_TYPE_OBJ;
				break;
			case Elf.ELFhdr.ET_DYN :
				attrib.type = Attribute.ELF_TYPE_SHLIB;
				break;
		}

		switch (ehdr.e_machine) {
			case Elf.ELFhdr.EM_386 :
			case Elf.ELFhdr.EM_486 :
				attrib.cpu = "x86"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_68K :
				attrib.cpu = "m68k"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_PPC :
			case Elf.ELFhdr.EM_CYGNUS_POWERPC :
			case Elf.ELFhdr.EM_RS6000 :
				attrib.cpu = "ppc"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_PPC64 :
				attrib.cpu = "ppc64"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_SH :
				attrib.cpu = "sh"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_ARM :
				attrib.cpu = "arm"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_MIPS_RS3_LE :
			case Elf.ELFhdr.EM_MIPS :
				attrib.cpu = "mips"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_SPARC32PLUS :
			case Elf.ELFhdr.EM_SPARC :
			case Elf.ELFhdr.EM_SPARCV9 :
				attrib.cpu = "sparc"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_H8_300 :
			case Elf.ELFhdr.EM_H8_300H :
				attrib.cpu = "h8300"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_V850 :
			case Elf.ELFhdr.EM_CYGNUS_V850 :
				attrib.cpu = "v850"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_MN10300 :
			case Elf.ELFhdr.EM_CYGNUS_MN10300 :
				attrib.cpu = "mn10300"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_MN10200 :
			case Elf.ELFhdr.EM_CYGNUS_MN10200 :
				attrib.cpu = "mn10200"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_M32R :
				attrib.cpu = "m32r"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_FR30 :
			case Elf.ELFhdr.EM_CYGNUS_FR30 :
				attrib.cpu = "fr30"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_XSTORMY16 :
				attrib.cpu = "xstormy16"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_CYGNUS_FRV :
				attrib.cpu = "frv"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_IQ2000 :
				attrib.cpu = "iq2000"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_EXCESS :
				attrib.cpu = "excess"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_NIOSII :
				attrib.cpu = "alteranios2"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_NIOS :
				attrib.cpu = "alteranios"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_IA_64 :
				attrib.cpu = "ia64"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_AVR :
				attrib.cpu = "avr"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_MSP430 :
				attrib.cpu = "msp430"; //$NON-NLS-1$
				break;
			case Elf.ELFhdr.EM_NONE :
			default :
				attrib.cpu = "none"; //$NON-NLS-1$
		}
		switch (ehdr.e_ident[Elf.ELFhdr.EI_DATA]) {
			case Elf.ELFhdr.ELFDATA2LSB :
				attrib.isle = true;
				break;
			case Elf.ELFhdr.ELFDATA2MSB :
				attrib.isle = false;
				break;
		}
		switch (ehdr.e_ident[ELFhdr.EI_CLASS]) {
			case ELFhdr.ELFCLASS32 :
				attrib.addressFactory = new Addr32Factory();
				break;
			case ELFhdr.ELFCLASS64 :
				attrib.addressFactory = new Addr64Factory();
				break;
			case ELFhdr.ELFCLASSNONE :
			default :
				attrib.addressFactory = null;
		}
		// getSections
		// find .debug using toString
		Section[] sec = getSections();
		if (sec != null) {
			for (int i = 0; i < sec.length; i++) {
				String s = sec[i].toString();
				if (s.equals(".debug_info")) { //$NON-NLS-1$
					attrib.debugType = Attribute.DEBUG_TYPE_DWARF;
					break;
				} else if (s.equals(".stab")) { //$NON-NLS-1$
					attrib.debugType = Attribute.DEBUG_TYPE_STABS;
					break;
				}
			}
		}
		return attrib;
	}

	public static Attribute getAttributes(String file) throws IOException {
		Elf elf = new Elf(file);
		Attribute attrib = elf.getAttributes();
		elf.dispose();
		return attrib;
	}

	public static Attribute getAttributes(byte[] array) throws IOException {

		Elf emptyElf = new Elf();
		emptyElf.ehdr = emptyElf.new ELFhdr(array);
		emptyElf.sections = new Elf.Section[0];
		Attribute attrib = emptyElf.getAttributes();
		emptyElf.dispose();

		return attrib;
	}

	public static boolean isElfHeader(byte[] e_ident) {
		if (e_ident.length < 4 || e_ident[ELFhdr.EI_MAG0] != 0x7f || e_ident[ELFhdr.EI_MAG1] != 'E'
				|| e_ident[ELFhdr.EI_MAG2] != 'L' || e_ident[ELFhdr.EI_MAG3] != 'F')
			return false;
		return true;
	}

	public void dispose() {
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
	protected void finalize() throws Throwable {
		try {
			dispose();
		} finally {
			super.finalize();
		}
	}

	public Section getSectionByName(String name) throws IOException {
		if (sections == null)
			getSections();
		for (int i = 0; i < sections.length; i++) {
			if (sections[i].toString().equals(name)) {
				return sections[i];
			}
		}
		return null;
	}

	public Section[] getSections(int type) throws IOException {
		if (sections == null)
			getSections();
		ArrayList slist = new ArrayList();
		for (int i = 0; i < sections.length; i++) {
			if (sections[i].sh_type == type)
				slist.add(sections[i]);
		}
		return (Section[])slist.toArray(new Section[0]);
	}

	public Section[] getSections() throws IOException {
		if (sections == null) {
			if (ehdr.e_shoff == 0) {
				sections = new Section[0];
				return sections;
			}
			efile.seek(ehdr.e_shoff);
			sections = new Section[ehdr.e_shnum];
			for (int i = 0; i < ehdr.e_shnum; i++) {
				sections[i] = new Section();
				sections[i].sh_name = efile.readIntE();
				sections[i].sh_type = efile.readIntE();
				switch (ehdr.e_ident[ELFhdr.EI_CLASS]) {
					case ELFhdr.ELFCLASS32 : {
						byte[] addrArray = new byte[ELF32_ADDR_SIZE];
						sections[i].sh_flags = efile.readIntE();
						efile.readFullyE(addrArray);
						sections[i].sh_addr = new Addr32(addrArray);
						sections[i].sh_offset = efile.readIntE();
						sections[i].sh_size = efile.readIntE();
					}
						break;
					case ELFhdr.ELFCLASS64 : {
						byte[] addrArray = new byte[ELF64_ADDR_SIZE];
						sections[i].sh_flags = efile.readLongE();
						efile.readFullyE(addrArray);
						sections[i].sh_addr = new Addr64(addrArray);
						sections[i].sh_offset = readUnsignedLong(efile);
						sections[i].sh_size = readUnsignedLong(efile);
					}
						break;
					case ELFhdr.ELFCLASSNONE :
					default :
						throw new IOException("Unknown ELF class " + ehdr.e_ident[ELFhdr.EI_CLASS]); //$NON-NLS-1$
				}

				sections[i].sh_link = efile.readIntE();
				sections[i].sh_info = efile.readIntE();
				switch (ehdr.e_ident[ELFhdr.EI_CLASS]) {
					case ELFhdr.ELFCLASS32 : {
						sections[i].sh_addralign = efile.readIntE();
						sections[i].sh_entsize = efile.readIntE();
					}
						break;
					case ELFhdr.ELFCLASS64 : {
						sections[i].sh_addralign = efile.readLongE();
						sections[i].sh_entsize = readUnsignedLong(efile);
					}
						break;
					case ELFhdr.ELFCLASSNONE :
					default :
						throw new IOException("Unknown ELF class " + ehdr.e_ident[ELFhdr.EI_CLASS]); //$NON-NLS-1$
				}
				if (sections[i].sh_type == Section.SHT_SYMTAB)
					syms = i;
				if (syms == 0 && sections[i].sh_type == Section.SHT_DYNSYM)
					syms = i;
			}
		}
		return sections;
	}

	private Symbol[] loadSymbolsBySection(Section section) throws IOException {
		int numSyms = 1;
		if (section.sh_entsize != 0) {
			numSyms = (int)section.sh_size / (int)section.sh_entsize;
		}
		ArrayList symList = new ArrayList(numSyms);
		long offset = section.sh_offset;
		for (int c = 0; c < numSyms; offset += section.sh_entsize, c++) {
			efile.seek(offset);
			Symbol symbol = new Symbol(section);
			switch (ehdr.e_ident[ELFhdr.EI_CLASS]) {
				case ELFhdr.ELFCLASS32 : {
					byte[] addrArray = new byte[ELF32_ADDR_SIZE];

					symbol.st_name = efile.readIntE();
					efile.readFullyE(addrArray);
					symbol.st_value = new Addr32(addrArray);
					symbol.st_size = efile.readIntE();
					symbol.st_info = efile.readByte();
					symbol.st_other = efile.readByte();
					symbol.st_shndx = efile.readShortE();
				}
					break;
				case ELFhdr.ELFCLASS64 : {
					byte[] addrArray = new byte[ELF64_ADDR_SIZE];

					symbol.st_name = efile.readIntE();
					symbol.st_info = efile.readByte();
					symbol.st_other = efile.readByte();
					symbol.st_shndx = efile.readShortE();
					efile.readFullyE(addrArray);
					symbol.st_value = new Addr64(addrArray);
					symbol.st_size = readUnsignedLong(efile);
				}
					break;
				case ELFhdr.ELFCLASSNONE :
				default :
					throw new IOException("Unknown ELF class " + ehdr.e_ident[ELFhdr.EI_CLASS]); //$NON-NLS-1$
			}
			if (symbol.st_info == 0)
				continue;
			symList.add(symbol);
		}
		Symbol[] results = (Symbol[])symList.toArray(new Symbol[0]);
		Arrays.sort(results);
		return results;
	}

	public void loadSymbols() throws IOException {
		if (symbols == null) {
			Section section[] = getSections(Section.SHT_SYMTAB);
			if (section.length > 0) {
				symtab_sym = section[0];
				symtab_symbols = loadSymbolsBySection(section[0]);
			} else {
				symtab_sym = null;
				symtab_symbols = new Symbol[0];
			}

			section = getSections(Section.SHT_DYNSYM);
			if (section.length > 0) {
				dynsym_sym = section[0];
				dynsym_symbols = loadSymbolsBySection(section[0]);
			} else {
				dynsym_sym = null;
				dynsym_symbols = new Symbol[0];
			}

			if (symtab_sym != null) {
				// sym = symtab_sym;
				symbols = symtab_symbols;
			} else if (dynsym_sym != null) {
				// sym = dynsym_sym;
				symbols = dynsym_symbols;
			}
		}
	}

	public Symbol[] getSymbols() {
		return symbols;
	}

	public Symbol[] getDynamicSymbols() {
		return dynsym_symbols;
	}

	public Symbol[] getSymtabSymbols() {
		return symtab_symbols;
	}

	/* return the address of the function that address is in */
	public Symbol getSymbol(IAddress vma) {
		if (symbols == null) {
			return null;
		}

		//@@@ If this works, move it to a single instance in this class.
		SymbolComparator symbol_comparator = new SymbolComparator();

		int ndx = Arrays.binarySearch(symbols, vma, symbol_comparator);
		if (ndx > 0)
			return symbols[ndx];
		if (ndx == -1) {
			return null;
		}
		ndx = -ndx - 1;
		return symbols[ndx - 1];
	}
	/*
	 * public long swapInt( long val ) { if ( ehdr.e_ident[ELFhdr.EI_DATA] ==
	 * ELFhdr.ELFDATA2LSB ) { short tmp[] = new short[4]; tmp[0] = (short)(val &
	 * 0x00ff); tmp[1] = (short)((val >> 8) & 0x00ff); tmp[2] = (short)((val >>
	 * 16) & 0x00ff); tmp[3] = (short)((val >> 24) & 0x00ff); return ((tmp[0] < <
	 * 24) + (tmp[1] < < 16) + (tmp[2] < < 8) + tmp[3]); } return val; }
	 * 
	 * public int swapShort( short val ) { if ( ehdr.e_ident[ELFhdr.EI_DATA] ==
	 * ELFhdr.ELFDATA2LSB ) { short tmp[] = new short[2]; tmp[0] = (short)(val &
	 * 0x00ff); tmp[1] = (short)((val >> 8) & 0x00ff); return (short)((tmp[0] < <
	 * 8) + tmp[1]); } return val; }
	 */
	public String getFilename() {
		return file;
	}

	protected long readUnsignedLong(ERandomAccessFile file) throws IOException {
		long result = file.readLongE();
		if (result < 0) {
			throw new IOException("Maximal file offset is " + Long.toHexString(Long.MAX_VALUE) + //$NON-NLS-1$
					" given offset is " + Long.toHexString(result)); //$NON-NLS-1$
		}
		return result;
	}
}