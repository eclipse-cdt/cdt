/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.utils.som;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.utils.coff.ReadMemoryAccess;

/**
 * Representation of a HP-UX SOM binary format
 * 
 * @author vhirsl
 */
public class SOM {
	public static final String NL = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	
	String filename;
	FileHeader filehdr;
	RandomAccessFile rfile;
	long startingOffset;
	byte[] string_table;
	Symbol[] symbols;

	/**
	 * SOM Header record
	 * 
	 * @author vhirsl
	 */
	public static class FileHeader {
		public final static int FILHSZ = 32*4;
		
		// Consts
		public static final short PA_RISC_10 = 0x20b;
		public static final short PA_RISC_11 = 0x210;
		public static final short PA_RISC_20 = 0x214;
		
		public static final short EXE_SOM_LIB               = 0x104;	// executable SOM library
		public static final short REL_SOM                   = 0x106;	// relocatable SOM
		public static final short PRIV_EXEC_SOM             = 0x107;	// non-sharable, executable SOM
		public static final short SHARE_EXEC_SOM            = 0x108;	// sharable, executable SOM
		public static final short SHARE_DEMAND_LOAD_EXE_SOM = 0x10b;	// sharable, demand-loadable executable SOM
		public static final short DYN_LOAD_LIB              = 0x10d;	// dynamic load library
		public static final short SHARED_LIB                = 0x10e;	// shared library
		public static final short RELOC_SOM_LIB             = 0x619;	// relocatable SOM library
		
		// Fields
		public short system_id;			// magic number - system
		public short a_magic;			// magic number - file type
		public int version_id;			// version id; format = YYMMDDHH
		public long file_time_sec;		// system clock - zero if unused
		public long file_time_nano;		// system clock - zero if unused
		public int entry_space;			// index of space containing entry point
		public int entry_subspace;		// index of subspace for entry point
		public int entry_offset;		// offset of entry point
		public int aux_header_location;	// auxiliary header location
		public int aux_header_size;		// auxiliary header size
		public int som_length;			// length in bytes of entire som
		public int presumed_dp;			// DP value assumed during compilation
		public int space_location;		// location in file of space dictionary
		public int space_total;			// number of space entries
		public int subspace_location;	// location of subspace entries
		public int subspace_total;		// number of subspace entries
		public int loader_fixup_location;	// MPE/iX loader fixup
		public int loader_fixup_total;	// number of loader fixup records
		public int space_strings_location;	// file location of string area for space and subspace names
		public int space_strings_size;	// size of string area for space and subspace names
		public int init_array_location;	// reserved for use by system
		public int init_array_total;	// reserved for use by system
		public int compiler_location;	// location in file of module dictionary
		public int compiler_total;		// number of modules
		public int symbol_location;		// location in file of symbol dictionary
		public int symbol_total;		// number of symbol records
		public int fixup_request_location;	// location in file of fix-up requests
		public int fixup_request_total;	// number of fixup requests
		public int symbol_strings_location;	// file location of string area for module and symbol names
		public int symbol_strings_size;	// size of string area for module and symbol names
		public int unloadable_sp_location;	// byte offset of first byte of datafor unloadable spaces
		public int unloadable_sp_size;	// byte length of data for unloadable spaces
		public int checksum;
		
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
			if (!isSOMHeader(hdr)) {
				throw new IOException(CCorePlugin.getResourceString("Util.exception.notSOM")); //$NON-NLS-1$
			}
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, little);
			
			system_id = memory.getShort();
			a_magic = memory.getShort();
			version_id = memory.getInt();
			file_time_sec = memory.getInt();
			file_time_nano = memory.getInt();
			entry_space = memory.getInt();
			entry_subspace = memory.getInt();
			entry_offset = memory.getInt();
			aux_header_location = memory.getInt();
			aux_header_size = memory.getInt();
			som_length = memory.getInt();
			presumed_dp = memory.getInt();
			space_location = memory.getInt();
			space_total = memory.getInt();
			subspace_location = memory.getInt();
			subspace_total = memory.getInt();
			loader_fixup_location = memory.getInt();
			loader_fixup_total = memory.getInt();
			space_strings_location = memory.getInt();
			space_strings_size = memory.getInt();
			init_array_location = memory.getInt();
			init_array_total = memory.getInt();
			compiler_location = memory.getInt();
			compiler_total = memory.getInt();
			symbol_location = memory.getInt();
			symbol_total = memory.getInt();
			fixup_request_location = memory.getInt();
			fixup_request_total = memory.getInt();
			symbol_strings_location = memory.getInt();
			symbol_strings_size = memory.getInt();
			unloadable_sp_location = memory.getInt();
			unloadable_sp_size = memory.getInt();
			checksum = memory.getInt();
		}
		
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("FILE HEADER VALUES").append(NL); //$NON-NLS-1$

			buffer.append("system_id               = ").append(system_id).append(NL); //$NON-NLS-1$
			buffer.append("a_magic                 = ").append(a_magic).append(NL); //$NON-NLS-1$
			buffer.append("version_id              = ").append(version_id).append(NL); //$NON-NLS-1$
			buffer.append("file_time_sec           = ").append(file_time_sec).append(NL); //$NON-NLS-1$
			buffer.append("file_time_nano          = ").append(file_time_nano).append(NL); //$NON-NLS-1$
			buffer.append("entry_space             = ").append(entry_space).append(NL); //$NON-NLS-1$
			buffer.append("entry_subspace          = ").append(entry_subspace).append(NL); //$NON-NLS-1$
			buffer.append("aux_header_location     = ").append(aux_header_location).append(NL); //$NON-NLS-1$
			buffer.append("aux_header_size         = ").append(aux_header_size).append(NL); //$NON-NLS-1$
			buffer.append("som_length              = ").append(som_length).append(NL); //$NON-NLS-1$
			buffer.append("presumed_dp             = ").append(presumed_dp).append(NL); //$NON-NLS-1$
			buffer.append("space_location          = ").append(space_location).append(NL); //$NON-NLS-1$
			buffer.append("space_total             = ").append(space_total).append(NL); //$NON-NLS-1$
			buffer.append("subspace_location       = ").append(subspace_location).append(NL); //$NON-NLS-1$
			buffer.append("subspace_total          = ").append(subspace_total).append(NL); //$NON-NLS-1$
			buffer.append("loader_fixup_location   = ").append(loader_fixup_location).append(NL); //$NON-NLS-1$
			buffer.append("loader_fixup_total      = ").append(loader_fixup_total).append(NL); //$NON-NLS-1$
			buffer.append("space_strings_location  = ").append(space_strings_location).append(NL); //$NON-NLS-1$
			buffer.append("space_strings_size      = ").append(space_strings_size).append(NL); //$NON-NLS-1$
			buffer.append("init_array_location     = ").append(init_array_location).append(NL); //$NON-NLS-1$
			buffer.append("init_array_total        = ").append(init_array_total).append(NL); //$NON-NLS-1$
			buffer.append("compiler_location       = ").append(compiler_location).append(NL); //$NON-NLS-1$
			buffer.append("compiler_total          = ").append(compiler_total).append(NL); //$NON-NLS-1$
			buffer.append("symbol_location         = ").append(symbol_location).append(NL); //$NON-NLS-1$
			buffer.append("symbol_total            = ").append(symbol_total).append(NL); //$NON-NLS-1$
			buffer.append("fixup_request_location  = ").append(fixup_request_location).append(NL); //$NON-NLS-1$
			buffer.append("fixup_request_total     = ").append(fixup_request_total).append(NL); //$NON-NLS-1$
			buffer.append("symbol_strings_location = ").append(symbol_strings_location).append(NL); //$NON-NLS-1$
			buffer.append("symbol_strings_size     = ").append(symbol_strings_size).append(NL); //$NON-NLS-1$
			buffer.append("unloadable_sp_location  = ").append(unloadable_sp_location).append(NL); //$NON-NLS-1$
			buffer.append("unloadable_sp_size      = ").append(unloadable_sp_size).append(NL); //$NON-NLS-1$
			buffer.append("checksum                = ").append(checksum).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}
	}
	
	public class Symbol {
		public static final int SYMSZ = 5*4;	// 5 words = 20 bytes
		// masks
		// NOTE: HP-UX denotes bit 0 as a leftmost bit in a word
		// following representation denotes bit 0 as a rightmost bit in a word
		public static final int B31_MASK    = 0x80000000;
		public static final int B30_MASK    = 0x40000000;
		public static final int B29_24_MASK = 0x3f000000;
		public static final int B23_20_MASK = 0x00f00000;
		public static final int B19_17_MASK = 0x000e0000;
		public static final int B16_MASK    = 0x00010000;
		public static final int B15_MASK    = 0x00008000;
		public static final int B14_MASK    = 0x00004000;
		public static final int B13_MASK    = 0x00002000;
		public static final int B12_MASK    = 0x00001000;
		public static final int B11_10_MASK = 0x00000C00;
		public static final int B9_0_MASK   = 0x000003ff;
		public static final int B23_0_MASK  = 0x00ffffff;
		public static final int B7_0_MASK   = 0x000000ff;

		// symbol type
		public static final int NULL      = 0;	// Invalid symbol record
		public static final int ABSOLUTE  = 1;	// Absolute constant
		public static final int DATA      = 2;	// Normal initialized data
		public static final int CODE      = 3;	// Unspecified code 
		public static final int PRI_PROG  = 4;	// Primary program entry point
		public static final int SEC_PROG  = 5;	// Secondary program entry point
		public static final int ENTRY     = 6;	// Any code entry point
		public static final int STORAGE   = 7;	// Uninitialized common data blocks
		public static final int STUB      = 8;	// Import external call stub or a parameter relocation stub
		public static final int MODULE    = 9;	// Source module name
		public static final int SYM_EXT   = 10;	// Extension record of the current entry
		public static final int ARG_EXT   = 11;	// -||-
		public static final int MILLICODE = 12;	// Millicode routine
		public static final int PLABEL    = 13;	// Export stub for a procedure
		public static final int OCT_DIS   = 14;	// Pointer to translated code segment exists but disabled
		public static final int MILLI_EXT = 15;	// Address of an external millicode routine
		public static final int ST_DATA   = 15;	// Thread specific data
		
		// symbol scope
		public static final int UNSAT     = 0;	// Import request that has not been satisfied
		public static final int EXTERNAL  = 1;	// Import request linked to a symbol in another SOM
		public static final int LOCAL     = 2;	// The symbol is not exported for use outside the SOM
		public static final int UNIVERSAL = 3;	// The symbol is exported for use outside the SOM
		
		// fields
		public boolean hidden;				// W1 b31
		public boolean secondary_def;		// W1 b30
		public int symbol_type;				// W1 b29-24
		public int symbol_scope;			// W1 b23-20
		public int check_level;				// W1 b19-17
		public boolean must_qualify;		// W1 b16
		public boolean initially_frozen;	// W1 b15
		public boolean memory_resident;		// W1 b14
		public boolean is_common;			// W1 b13
		public boolean dup_common;			// W1 b12
		public int xleast;					// W1 b11-10
		public int arg_reloc;				// W1 b9-0
		public int name_offset;				// W2
		public int qualifier_name_offset;	// W3
		public boolean has_long_return;		// W4 b31
		public boolean no_relocation;		// W4 b30
		public int symbol_info;				// W4 b23-0
		public int symbol_value;			// W5
		
		public Symbol(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public Symbol(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] bytes = new byte[SYMSZ];
			file.readFully(bytes);
			ReadMemoryAccess memory = new ReadMemoryAccess(bytes, false); // big endian
			// first word
			int word = memory.getInt();
			hidden = (word & B31_MASK) != 0;
			secondary_def = (word & B30_MASK) != 0;
			symbol_type = (word & B29_24_MASK) >> 24;
			symbol_scope = (word & B23_20_MASK) >> 20;
			check_level = (word & B19_17_MASK) >> 17;
			must_qualify = (word & B16_MASK) != 0;
			initially_frozen = (word & B15_MASK) != 0;
			memory_resident = (word & B14_MASK) != 0;
			is_common = (word & B13_MASK) != 0;
			dup_common = (word & B12_MASK) != 0;
			xleast = (word & B11_10_MASK) >> 10;
			arg_reloc = word & B9_0_MASK;
			// second word
			name_offset = memory.getInt();
			// third word
			qualifier_name_offset = memory.getInt();
			// fourth word
			word = memory.getInt();
			has_long_return = (word & B31_MASK) != 0;
			no_relocation = (word & B30_MASK) != 0;
			symbol_info = word & B23_0_MASK;
			// fifth word
			symbol_value = memory.getInt();
			
			// check for symbol extension record and descriptor array records
			if (check_level >= 1) {
				// bytes = new byte[SYMSZ];
				file.readFully(bytes);
				memory = new ReadMemoryAccess(bytes, false); // big endian
				// an extension record is present (size 5 words = 20 bytes)
				word = memory.getInt();
				int num_args = word & B7_0_MASK;
				// check for argument descriptor arrays
				if (num_args > 3 && check_level >=3) {
					int num_descs = (num_args-3)%4 == 0 ? (num_args-3)/4 : (num_args-3)/4 + 1;
					for (int i = 0; i < num_descs; ++ i) {
						file.readFully(bytes);
					}
				}
			}
		}

		public String getName(byte[] table) {
			if (qualifier_name_offset != 0) {
				byte[] len = new byte[4];
				System.arraycopy(table, qualifier_name_offset-4, len, 0, 4);
				ReadMemoryAccess memory = new ReadMemoryAccess(len, false); // big endian
				int length = memory.getInt();
				return new String(table, qualifier_name_offset, length);
			}
			if (name_offset != 0) {
				byte[] len = new byte[4];
				System.arraycopy(table, name_offset-4, len, 0, 4);
				ReadMemoryAccess memory = new ReadMemoryAccess(len, false); // big endian
				int length = memory.getInt();
				return new String(table, name_offset, length);
			}
			return "";	//$NON-NLS-1$
		}
		
		public boolean isFunction() {
			return (symbol_type == PRI_PROG ||  
					(symbol_type == ENTRY && symbol_scope != LOCAL));
		}
		
		public boolean isVariable() {
			return ((symbol_type == DATA && symbol_scope != LOCAL) ||
					symbol_type == STORAGE);
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("SYMBOL TABLE ENTRY").append(NL); //$NON-NLS-1$
			buffer.append("symbol_name  = ");
			try {
				buffer.append(getName(getStringTable())).append(NL);
			}
			catch (IOException e) {
				buffer.append("I/O error");	//$NON-NLS-1$
			}
			buffer.append("symbol_value = ").append(symbol_value).append(NL); //$NON-NLS-1$
			buffer.append("symbol_type  = ").append(symbol_type).append(NL); //$NON-NLS-1$
			buffer.append("symbol_scope = ").append(symbol_scope).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}
	}
	
	public static class Attribute {
		public static final int SOM_TYPE_EXE   = 1;
		public static final int SOM_TYPE_SHLIB = 2;
		public static final int SOM_TYPE_OBJ   = 3;
		public static final int SOM_TYPE_CORE  = 4;

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
	
	/*
	 * SOM class implementation
	 */
    // A hollow entry, to be used with caution in controlled situations
	protected SOM() {
	}
	
	public SOM(String filename) throws IOException {
		this(filename, 0);
	}

	public SOM(String filename, long offset) throws IOException {
		this.filename = filename;
		commonSetup(new RandomAccessFile(filename, "r"), offset);
	}

	void commonSetup(RandomAccessFile file, long offset) throws IOException {
		startingOffset = offset;
		rfile = file;
		try {
			filehdr = new FileHeader(rfile, startingOffset);
		} finally {
			dispose();
		}
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

	public FileHeader getFileHeader() throws IOException {
		return filehdr;
	}
	
	public Attribute getAttributes() {
		Attribute attrib = new Attribute();
		// Machine type.
		switch (filehdr.system_id) {
			case FileHeader.PA_RISC_10:
				attrib.cpu = "pa-risc_1.0"; //$NON-NLS-1$
			break;
			case FileHeader.PA_RISC_11:
				attrib.cpu = "pa-risc_1.1"; //$NON-NLS-1$
			break;
			case FileHeader.PA_RISC_20:
				attrib.cpu = "pa-risc_2.0"; //$NON-NLS-1$
			break;
			default:
				attrib.cpu = "unknown"; //$NON-NLS-1$
			break;
		}

		/* SOM characteristics, FileHeader.a_magic.  */
		switch (filehdr.a_magic) {
			case FileHeader.EXE_SOM_LIB:
			case FileHeader.PRIV_EXEC_SOM:
			case FileHeader.SHARE_EXEC_SOM:
			case FileHeader.SHARE_DEMAND_LOAD_EXE_SOM:
				attrib.type = Attribute.SOM_TYPE_EXE;
				break;
			case FileHeader.DYN_LOAD_LIB:
			case FileHeader.SHARED_LIB:
				attrib.type = Attribute.SOM_TYPE_SHLIB;
				break;
			default:
				attrib.type = Attribute.SOM_TYPE_OBJ;
		}

		// For HP-UX SOM always assume big endian unless otherwise.
		attrib.isle = false;

		// No debug information.
		if (filehdr.symbol_location == 0 && filehdr.symbol_total == 0) {
			attrib.bDebug = false;
		} else {
			attrib.bDebug = true;
		}
		
		return attrib;
	}

	public Symbol[] getSymbols() throws IOException {
		if (symbols == null) {
			long offset = startingOffset + getFileHeader().symbol_location;
			getRandomAccessFile();
			rfile.seek(offset);
			int numSymbols = getFileHeader().symbol_total;
			ArrayList symList = new ArrayList(numSymbols);
			for (int i = 0; i < numSymbols; ++i) {
				Symbol v = new Symbol(rfile);
				symList.add(v);
			}
			symbols = (Symbol[]) symList.toArray(new Symbol[symList.size()]);
		}
		return symbols;
	}

	public byte[] getStringTable() throws IOException {
		if (string_table == null) {
			if (getFileHeader().symbol_strings_size > 0) {
				getRandomAccessFile();
				long offset = startingOffset+ getFileHeader().symbol_strings_location;
				rfile.seek(offset);
				string_table = new byte[getFileHeader().symbol_strings_size];
				rfile.readFully(string_table);
			}
			else {
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
			getSymbols();
			for (int i = 0; i < symbols.length; ++i) {
				buffer.append(symbols[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}
	
	/**
	 * @param hints
	 * @return
	 */
	public static boolean isSOMHeader(byte[] hints) {
		if (hints != null && hints[0] == 0x02 && 
			(hints[1] == (byte)0xb || hints[1] == (byte)0x10 || hints[1] == (byte)0x14) ) {
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
		SOM emptyXCoff = new SOM();
		emptyXCoff.filehdr = new SOM.FileHeader(hints, false); // big endian
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
		SOM xcoff = new SOM(file);
		Attribute attribute = xcoff.getAttributes();
		xcoff.dispose();
		return attribute;
	}

	public static void main(String[] args) {
		try {
			SOM som = new SOM(args[0]);
			System.out.println(som);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
