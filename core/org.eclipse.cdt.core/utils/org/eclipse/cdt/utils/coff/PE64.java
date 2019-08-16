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
 *     QNX Software Systems - Initial PE class
 *******************************************************************************/

package org.eclipse.cdt.utils.coff;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.utils.Addr32Factory;
import org.eclipse.cdt.utils.coff.Coff64.FileHeader;
import org.eclipse.cdt.utils.coff.Coff64.OptionalHeader;
import org.eclipse.cdt.utils.coff.Coff64.SectionHeader;
import org.eclipse.cdt.utils.coff.Coff64.Symbol;
import org.eclipse.cdt.utils.coff.Exe.ExeHeader;
import org.eclipse.cdt.utils.debug.dwarf.DwarfReader;
import org.eclipse.cdt.utils.debug.stabs.StabsReader;

/**
 * The PE file header consists of an MS-DOS stub, the PE signature, the COFF file Header
 * and an Optional Header.
 * <pre>
 *  +-------------------+
 *  | DOS-stub          |
 *  +-------------------+
 *  | file-header       |
 *  +-------------------+
 *  | optional header   |
 *  |- - - - - - - - - -|
 *  |                   |
 *  | data directories  |
 *  |                   |
 *  +-------------------+
 *  |                   |
 *  | section headers   |
 *  |                   |
 *  +-------------------+
 *  |                   |
 *  | section 1         |
 *  |                   |
 *  +-------------------+
 *  |                   |
 *  | section 2         |
 *  |                   |
 *  +-------------------+
 *  |                   |
 *  | ...               |
 *  |                   |
 *  +-------------------+
 *  |                   |
 *  | section n         |
 *  |                   |
 *  +-------------------+
 * </pre>
 * @since 6.9
 */
public class PE64 {

	public static final String NL = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	RandomAccessFile rfile;
	String filename;
	ExeHeader exeHeader;
	DOSHeader dosHeader;
	FileHeader fileHeader;
	OptionalHeader optionalHeader;
	NTOptionalHeader64 ntHeader64;
	NTOptionalHeader32 ntHeader32;
	ImageDataDirectory[] dataDirectories;
	SectionHeader[] scnhdrs;
	Symbol[] symbolTable;
	byte[] stringTable;

	public static class Attribute {
		public static final int PE_TYPE_EXE = 1;
		public static final int PE_TYPE_SHLIB = 2;
		public static final int PE_TYPE_OBJ = 3;
		public static final int PE_TYPE_CORE = 4;

		String cpu;
		int type;
		int word;
		boolean bDebug;
		boolean isle;
		IAddressFactory addrFactory;

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

		public int getWord() {
			return word;
		}
	}

	/**
	 */
	public static class DOSHeader {
		final static int DOSHDRSZ = 100;
		byte[] e_res = new byte[8]; /* Reserved words, all 0x0.  */
		byte[] e_oemid = new byte[2]; /* OEM identifier (for e_oeminfo), 0x0.  */
		byte[] e_oeminfo = new byte[2]; /* OEM information; e_oemid specific, 0x0.  */
		byte[] e_res2 = new byte[20]; /* Reserved words, all 0x0.  */
		int e_lfanew; /* 4 byte File address of new exe header, offset 60(0x3c), 0x80. */
		byte[] dos_message = new byte[64]; /* Other stuff, always follow DOS header.  */

		public DOSHeader(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public DOSHeader(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] hdr = new byte[DOSHDRSZ];
			file.readFully(hdr);
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, true);
			commonSetup(memory);
		}

		public DOSHeader(byte[] hdr, boolean little) throws IOException {
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, little);
			commonSetup(memory);
		}

		public DOSHeader(ReadMemoryAccess memory) throws IOException {
			commonSetup(memory);
		}

		public void commonSetup(ReadMemoryAccess memory) throws IOException {
			if (memory.getSize() < DOSHDRSZ) {
				throw new IOException("Not a Dos Header"); //$NON-NLS-1$
			}
			memory.getBytes(e_res);
			memory.getBytes(e_oemid);
			memory.getBytes(e_oeminfo);
			memory.getBytes(e_res2);
			e_lfanew = memory.getInt();
			memory.getBytes(dos_message);
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("DOS STUB VALUES").append(NL); //$NON-NLS-1$
			buffer.append("e_lfanew = ").append(e_lfanew).append(NL); //$NON-NLS-1$
			buffer.append(new String(dos_message)).append(NL);
			return buffer.toString();
		}
	}

	public static class IMAGE_DEBUG_DIRECTORY {
		final int DEBUGDIRSZ = 28;
		public int Characteristics;
		public int TimeDateStamp;
		public short MajorVersion;
		public short MinorVersion;
		public int Type;
		public int SizeOfData;
		public int AddressOfRawData;
		public int PointerToRawData;

		public IMAGE_DEBUG_DIRECTORY(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] dir = new byte[DEBUGDIRSZ];
			file.readFully(dir);
			ReadMemoryAccess memory = new ReadMemoryAccess(dir, true);
			Characteristics = memory.getInt();
			TimeDateStamp = memory.getInt();
			MajorVersion = memory.getShort();
			MinorVersion = memory.getShort();
			Type = memory.getInt();
			SizeOfData = memory.getInt();
			AddressOfRawData = memory.getInt();
			PointerToRawData = memory.getInt();
		}
	}

	public static class IMAGE_DATA_DIRECTORY {

		public int VirtualAddress;
		public int Size;
	}

	public static class NTOptionalHeader64 {

		public final static int NTHDRSZ = 216;
		public long ImageBase; // 8 bytes.
		public int SectionAlignment; // 4 bytes.
		public int FileAlignment; // 4 bytes.
		public short MajorOperatingSystemVersion; // 2 bytes.
		public short MinorOperatingSystemVersion; // 2 bytes.
		public short MajorImageVersion; // 2 bytes.
		public short MinorImageVersion; // 2 bytes.
		public short MajorSubsystemVersion; // 2 bytes.
		public short MinorSubsystemVersion; // 2 bytes.
		public byte[] Reserved = new byte[4]; // 4 bytes.
		public int SizeOfImage; // 4 bytes.
		public int SizeOfHeaders; // 4 bytes.
		public int CheckSum; // 4 bytes.
		public short Subsystem; // 2 bytes.
		public short DLLCharacteristics; // 2 bytes.
		public long SizeOfStackReserve; // 8 bytes.
		public long SizeOfStackCommit; // 8 bytes.
		public long SizeOfHeapReserve; // 8 bytes.
		public long SizeOfHeapCommit; // 8 bytes.
		public int LoaderFlags; // 4 bytes.
		public int NumberOfRvaAndSizes; // 4 bytes.
		public IMAGE_DATA_DIRECTORY DataDirectory[];

		public NTOptionalHeader64(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public NTOptionalHeader64(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] hdr = new byte[NTHDRSZ];
			file.readFully(hdr);
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, true);
			ImageBase = memory.getLong();
			SectionAlignment = memory.getInt();
			FileAlignment = memory.getInt();
			MajorOperatingSystemVersion = memory.getShort();
			MinorOperatingSystemVersion = memory.getShort();
			MajorImageVersion = memory.getShort();
			MinorImageVersion = memory.getShort();
			MajorSubsystemVersion = memory.getShort();
			MinorSubsystemVersion = memory.getShort();
			memory.getBytes(Reserved);
			SizeOfImage = memory.getInt();
			SizeOfHeaders = memory.getInt();
			CheckSum = memory.getInt();
			Subsystem = memory.getShort();
			DLLCharacteristics = memory.getShort();
			SizeOfStackReserve = memory.getLong();
			SizeOfStackCommit = memory.getLong();
			SizeOfHeapReserve = memory.getLong();
			SizeOfHeapCommit = memory.getLong();
			LoaderFlags = memory.getInt();
			NumberOfRvaAndSizes = memory.getInt();

			DataDirectory = new IMAGE_DATA_DIRECTORY[NumberOfRvaAndSizes]; // 8*16=128 bytes
			for (int i = 0; i < NumberOfRvaAndSizes; i++) {
				DataDirectory[i] = new IMAGE_DATA_DIRECTORY();
				DataDirectory[i].VirtualAddress = memory.getInt();
				DataDirectory[i].Size = memory.getInt();
			}
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("NT OPTIONAL HEADER VALUES").append(NL); //$NON-NLS-1$
			buffer.append("ImageBase = ").append(ImageBase).append(NL); //$NON-NLS-1$
			buffer.append("SexctionAlignement = ").append(SectionAlignment).append(NL); //$NON-NLS-1$
			buffer.append("FileAlignment = ").append(FileAlignment).append(NL); //$NON-NLS-1$
			buffer.append("MajorOSVersion = ").append(MajorOperatingSystemVersion).append(NL); //$NON-NLS-1$
			buffer.append("MinorOSVersion = ").append(MinorOperatingSystemVersion).append(NL); //$NON-NLS-1$
			buffer.append("MajorImageVersion = ").append(MajorImageVersion).append(NL); //$NON-NLS-1$
			buffer.append("MinorImageVersion = ").append(MinorImageVersion).append(NL); //$NON-NLS-1$
			buffer.append("MajorSubVersion = ").append(MajorSubsystemVersion).append(NL); //$NON-NLS-1$
			buffer.append("MinorSubVersion = ").append(MinorSubsystemVersion).append(NL); //$NON-NLS-1$
			buffer.append("Reserved = ").append(Reserved).append(NL); //$NON-NLS-1$
			buffer.append("SizeOfImage = ").append(SizeOfImage).append(NL); //$NON-NLS-1$
			buffer.append("SizeOfHeaders = ").append(SizeOfHeaders).append(NL); //$NON-NLS-1$
			buffer.append("CheckSum = ").append(CheckSum).append(NL); //$NON-NLS-1$
			buffer.append("Subsystem = ").append(Subsystem).append(NL); //$NON-NLS-1$
			buffer.append("DLL = ").append(DLLCharacteristics).append(NL); //$NON-NLS-1$
			buffer.append("StackReserve = ").append(SizeOfStackReserve).append(NL); //$NON-NLS-1$
			buffer.append("StackCommit = ").append(SizeOfStackCommit).append(NL); //$NON-NLS-1$
			buffer.append("HeapReserve = ").append(SizeOfHeapReserve).append(NL); //$NON-NLS-1$
			buffer.append("HeapCommit = ").append(SizeOfHeapCommit).append(NL); //$NON-NLS-1$
			buffer.append("LoaderFlags = ").append(LoaderFlags).append(NL); //$NON-NLS-1$
			buffer.append("#Rva size = ").append(NumberOfRvaAndSizes).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}
	}

	public static class NTOptionalHeader32 {

		public final static int NTHDRSZ = 196;
		public int ImageBase; // 4 bytes.
		public int SectionAlignment; // 4 bytes.
		public int FileAlignment; // 4 bytes.
		public short MajorOperatingSystemVersion; // 2 bytes.
		public short MinorOperatingSystemVersion; // 2 bytes.
		public short MajorImageVersion; // 2 bytes.
		public short MinorImageVersion; // 2 bytes.
		public short MajorSubsystemVersion; // 2 bytes.
		public short MinorSubsystemVersion; // 2 bytes.
		public byte[] Reserved = new byte[4]; // 4 bytes.
		public int SizeOfImage; // 4 bytes.
		public int SizeOfHeaders; // 4 bytes.
		public int CheckSum; // 4 bytes.
		public short Subsystem; // 2 bytes.
		public short DLLCharacteristics; // 2 bytes.
		public int SizeOfStackReserve; // 4 bytes.
		public int SizeOfStackCommit; // 4 bytes.
		public int SizeOfHeapReserve; // 4 bytes.
		public int SizeOfHeapCommit; // 4 bytes.
		public int LoaderFlags; // 4 bytes.
		public int NumberOfRvaAndSizes; // 4 bytes.
		public IMAGE_DATA_DIRECTORY DataDirectory[];

		public NTOptionalHeader32(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public NTOptionalHeader32(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] hdr = new byte[NTHDRSZ];
			file.readFully(hdr);
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, true);
			ImageBase = memory.getInt();
			SectionAlignment = memory.getInt();
			FileAlignment = memory.getInt();
			MajorOperatingSystemVersion = memory.getShort();
			MinorOperatingSystemVersion = memory.getShort();
			MajorImageVersion = memory.getShort();
			MinorImageVersion = memory.getShort();
			MajorSubsystemVersion = memory.getShort();
			MinorSubsystemVersion = memory.getShort();
			memory.getBytes(Reserved);
			SizeOfImage = memory.getInt();
			SizeOfHeaders = memory.getInt();
			CheckSum = memory.getInt();
			Subsystem = memory.getShort();
			DLLCharacteristics = memory.getShort();
			SizeOfStackReserve = memory.getInt();
			SizeOfStackCommit = memory.getInt();
			SizeOfHeapReserve = memory.getInt();
			SizeOfHeapCommit = memory.getInt();
			LoaderFlags = memory.getInt();
			NumberOfRvaAndSizes = memory.getInt();

			DataDirectory = new IMAGE_DATA_DIRECTORY[NumberOfRvaAndSizes]; // 8*16=128 bytes
			for (int i = 0; i < NumberOfRvaAndSizes; i++) {
				DataDirectory[i] = new IMAGE_DATA_DIRECTORY();
				DataDirectory[i].VirtualAddress = memory.getInt();
				DataDirectory[i].Size = memory.getInt();
			}
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("NT OPTIONAL HEADER VALUES").append(NL); //$NON-NLS-1$
			buffer.append("ImageBase = ").append(ImageBase).append(NL); //$NON-NLS-1$
			buffer.append("SexctionAlignement = ").append(SectionAlignment).append(NL); //$NON-NLS-1$
			buffer.append("FileAlignment = ").append(FileAlignment).append(NL); //$NON-NLS-1$
			buffer.append("MajorOSVersion = ").append(MajorOperatingSystemVersion).append(NL); //$NON-NLS-1$
			buffer.append("MinorOSVersion = ").append(MinorOperatingSystemVersion).append(NL); //$NON-NLS-1$
			buffer.append("MajorImageVersion = ").append(MajorImageVersion).append(NL); //$NON-NLS-1$
			buffer.append("MinorImageVersion = ").append(MinorImageVersion).append(NL); //$NON-NLS-1$
			buffer.append("MajorSubVersion = ").append(MajorSubsystemVersion).append(NL); //$NON-NLS-1$
			buffer.append("MinorSubVersion = ").append(MinorSubsystemVersion).append(NL); //$NON-NLS-1$
			buffer.append("Reserved = ").append(Reserved).append(NL); //$NON-NLS-1$
			buffer.append("SizeOfImage = ").append(SizeOfImage).append(NL); //$NON-NLS-1$
			buffer.append("SizeOfHeaders = ").append(SizeOfHeaders).append(NL); //$NON-NLS-1$
			buffer.append("CheckSum = ").append(CheckSum).append(NL); //$NON-NLS-1$
			buffer.append("Subsystem = ").append(Subsystem).append(NL); //$NON-NLS-1$
			buffer.append("DLL = ").append(DLLCharacteristics).append(NL); //$NON-NLS-1$
			buffer.append("StackReserve = ").append(SizeOfStackReserve).append(NL); //$NON-NLS-1$
			buffer.append("StackCommit = ").append(SizeOfStackCommit).append(NL); //$NON-NLS-1$
			buffer.append("HeapReserve = ").append(SizeOfHeapReserve).append(NL); //$NON-NLS-1$
			buffer.append("HeapCommit = ").append(SizeOfHeapCommit).append(NL); //$NON-NLS-1$
			buffer.append("LoaderFlags = ").append(LoaderFlags).append(NL); //$NON-NLS-1$
			buffer.append("#Rva size = ").append(NumberOfRvaAndSizes).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}
	}

	public class ImageDataDirectory {
		public int rva;
		public int size;

		public ImageDataDirectory(int r, int s) {
			rva = r;
			size = s;
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("rva = ").append(rva).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
			buffer.append("size = ").append(size).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}
	}

	public class ImportDirectoryEntry {
		public final static int ENTRYSZ = 20;
		public int rva;
		public int timestamp;
		public int forwarder;
		public int name;
		public int thunk;

		public ImportDirectoryEntry(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public ImportDirectoryEntry(RandomAccessFile file, long offset) throws IOException {
			file.seek(offset);
			byte[] bytes = new byte[ENTRYSZ];
			file.readFully(bytes);
			ReadMemoryAccess memory = new ReadMemoryAccess(bytes, true);
			rva = memory.getInt();
			timestamp = memory.getInt();
			forwarder = memory.getInt();
			name = memory.getInt();
			thunk = memory.getInt();
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("rva = ").append(rva); //$NON-NLS-1$
			buffer.append(" timestamp = ").append(timestamp); //$NON-NLS-1$
			buffer.append(" forwarder = ").append(forwarder); //$NON-NLS-1$
			buffer.append(" name = ").append(name); //$NON-NLS-1$
			buffer.append(" thunk = ").append(thunk).append(NL); //$NON-NLS-1$
			return buffer.toString();
		}
	}

	public PE64(String filename) throws IOException {
		this(filename, 0);
	}

	public PE64(String filename, long pos) throws IOException {
		this(filename, pos, true);
	}

	public PE64(String filename, long pos, boolean filter) throws IOException {
		try {
			rfile = new RandomAccessFile(filename, "r"); //$NON-NLS-1$
			this.filename = filename;
			rfile.seek(pos);

			// Object files do not have exe/dos header.
			try {
				exeHeader = new ExeHeader(rfile);
				dosHeader = new DOSHeader(rfile);
				// Jump the Coff header, and Check the sig.
				rfile.seek(dosHeader.e_lfanew);
				byte[] sig = new byte[4];
				rfile.readFully(sig);
				if (!((sig[0] == 'P') && (sig[1] == 'E') && (sig[2] == '\0') && (sig[3] == '\0'))) {
					throw new IOException(CCorePlugin.getResourceString("Util.exception.notPE")); //$NON-NLS-1$
				}
			} catch (IOException e) {
				rfile.seek(pos);
			}

			fileHeader = new Coff64.FileHeader(rfile, rfile.getFilePointer());

			// Check if this a valid machine.
			if (!isValidMachine(fileHeader.f_magic)) {
				throw new IOException(CCorePlugin.getResourceString("Util.exception.unknownFormat")); //$NON-NLS-1$
			}

			if (fileHeader.f_opthdr > 0) {
				optionalHeader = new Coff64.OptionalHeader(rfile, rfile.getFilePointer());

				if (optionalHeader.is64Bits())
					ntHeader64 = new NTOptionalHeader64(rfile, rfile.getFilePointer());
				else
					ntHeader32 = new NTOptionalHeader32(rfile, rfile.getFilePointer());
			}
		} finally {
			if (rfile != null) {
				rfile.close();
				rfile = null;
			}
		}
	}

	public static boolean isValidMachine(int magic) {
		// Check if this a valid machine.
		switch (magic) {
		case PEConstants.IMAGE_FILE_MACHINE_ALPHA:
		case PEConstants.IMAGE_FILE_MACHINE_ARM:
		case PEConstants.IMAGE_FILE_MACHINE_ARM2:
		case PEConstants.IMAGE_FILE_MACHINE_ALPHA64:
		case PEConstants.IMAGE_FILE_MACHINE_AMD64:
		case PEConstants.IMAGE_FILE_MACHINE_I386:
		case PEConstants.IMAGE_FILE_MACHINE_IA64:
		case PEConstants.IMAGE_FILE_MACHINE_M68K:
		case PEConstants.IMAGE_FILE_MACHINE_MIPS16:
		case PEConstants.IMAGE_FILE_MACHINE_MIPSFPU:
		case PEConstants.IMAGE_FILE_MACHINE_MIPSFPU16:
		case PEConstants.IMAGE_FILE_MACHINE_POWERPC:
		case PEConstants.IMAGE_FILE_MACHINE_R3000:
		case PEConstants.IMAGE_FILE_MACHINE_R4000:
		case PEConstants.IMAGE_FILE_MACHINE_R10000:
		case PEConstants.IMAGE_FILE_MACHINE_SH3:
		case PEConstants.IMAGE_FILE_MACHINE_SH4:
		case PEConstants.IMAGE_FILE_MACHINE_THUMB:
			// Ok;
			return true;
		//throw new IOException("Unknow machine/format");
		}
		return false;
	}

	public static Attribute getAttributes(FileHeader filhdr) {
		Attribute attrib = new Attribute();
		// Machine type.
		switch (filhdr.f_magic) {
		case PEConstants.IMAGE_FILE_MACHINE_UNKNOWN:
			attrib.cpu = "none"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_ALPHA:
			attrib.cpu = "alpha"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_ARM:
		case PEConstants.IMAGE_FILE_MACHINE_ARM2:
			attrib.cpu = "arm"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_ALPHA64:
			attrib.cpu = "arm64"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_AMD64:
			attrib.cpu = "amd64"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_I386:
			attrib.cpu = "x86"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_IA64:
			attrib.cpu = "ia64"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_M68K:
			attrib.cpu = "m68k"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_MIPS16:
			attrib.cpu = "mips16"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_MIPSFPU:
			attrib.cpu = "mipsfpu"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_MIPSFPU16:
			attrib.cpu = "mipsfpu16"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_POWERPC:
			attrib.cpu = "powerpc"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_R3000:
			attrib.cpu = "r3000"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_R4000:
			attrib.cpu = "r4000"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_R10000:
			attrib.cpu = "r10000"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_SH3:
			attrib.cpu = "sh3"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_SH4:
			attrib.cpu = "sh4"; //$NON-NLS-1$
			break;
		case PEConstants.IMAGE_FILE_MACHINE_THUMB:
			attrib.cpu = "thumb"; //$NON-NLS-1$
			break;
		}

		/* PE characteristics, FileHeader.f_flags.  */
		if ((filhdr.f_flags & PEConstants.IMAGE_FILE_DLL) != 0) {
			attrib.type = Attribute.PE_TYPE_SHLIB;
		} else if ((filhdr.f_flags & PEConstants.IMAGE_FILE_EXECUTABLE_IMAGE) != 0) {
			attrib.type = Attribute.PE_TYPE_EXE;
		} else {
			attrib.type = Attribute.PE_TYPE_OBJ;
		}

		// For PE always assume little endian unless otherwise.
		attrib.isle = true;
		// Little Endian.
		if ((filhdr.f_flags & PEConstants.IMAGE_FILE_BYTES_REVERSED_LO) != 0) {
			attrib.isle = true;
		}
		// Big Endian.
		if ((filhdr.f_flags & PEConstants.IMAGE_FILE_BYTES_REVERSED_HI) != 0) {
			attrib.isle = false;
		}

		// No debug information.
		if ((filhdr.f_flags & PEConstants.IMAGE_FILE_DEBUG_STRIPPED) != 0) {
			attrib.bDebug = false;
		} else {
			attrib.bDebug = true;
		}

		// sizeof word.
		if ((filhdr.f_flags & PEConstants.IMAGE_FILE_16BIT_MACHINE) != 0) {
			attrib.word = 16;
		}
		if ((filhdr.f_flags & PEConstants.IMAGE_FILE_32BIT_MACHINE) != 0) {
			attrib.word = 32;
		}

		attrib.addrFactory = new Addr32Factory();
		return attrib;
	}

	public static boolean isExeHeader(byte[] e_signature) {
		if (e_signature == null || e_signature.length < 2 || e_signature[0] != 'M' || e_signature[1] != 'Z')
			return false;
		return true;
	}

	public Attribute getAttribute() throws IOException {
		return getAttributes(getFileHeader());
	}

	public static Attribute getAttribute(byte[] data) throws IOException {
		ReadMemoryAccess memory = new ReadMemoryAccess(data, true);
		int idx = 0;
		try {
			//Exe.ExeHeader exeHdr = new Exe.ExeHeader(memory);
			new Exe.ExeHeader(memory);
			DOSHeader dosHdr = new DOSHeader(memory);
			// Jump the Coff header, and Check the sig.
			idx = dosHdr.e_lfanew;
			if (idx + 4 < data.length) {
				if (!((data[idx + 0] == 'P') && (data[idx + 1] == 'E') && (data[idx + 2] == '\0')
						&& (data[idx + 3] == '\0'))) {
					throw new IOException(CCorePlugin.getResourceString("Util.exception.notPE")); //$NON-NLS-1$
				}
				idx += 4;
			}
		} catch (IOException e) {
		}
		if (idx < data.length) {
			byte[] bytes = new byte[data.length - idx];
			System.arraycopy(data, idx, bytes, 0, data.length - idx);
			Coff64.FileHeader filehdr = new Coff64.FileHeader(bytes, true);
			if (isValidMachine(filehdr.f_magic)) {
				return getAttributes(filehdr);
			}
		}
		throw new IOException(CCorePlugin.getResourceString("Util.exception.notPE")); //$NON-NLS-1$
	}

	public static Attribute getAttribute(String file) throws IOException {
		PE64 pe = new PE64(file);
		Attribute attrib = pe.getAttribute();
		pe.dispose();
		return attrib;
	}

	public void dispose() throws IOException {
		if (rfile != null) {
			rfile.close();
			rfile = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			dispose();
		} finally {
			super.finalize();
		}
	}

	public ExeHeader getExeHeader() {
		return exeHeader;
	}

	public DOSHeader getDOSHeader() {
		return dosHeader;
	}

	public FileHeader getFileHeader() {
		return fileHeader;
	}

	public OptionalHeader getOptionalHeader() {
		return optionalHeader;
	}

	public NTOptionalHeader64 getNTOptionalHeader64() {
		return ntHeader64;
	}

	public NTOptionalHeader32 getNTOptionalHeader32() {
		return ntHeader32;
	}

	public ImageDataDirectory[] getImageDataDirectories() throws IOException {
		if (dataDirectories == null) {
			RandomAccessFile accessFile = getRandomAccessFile();
			long offset = 0;
			if (dosHeader != null) {
				offset = dosHeader.e_lfanew + 4/*NT SIG*/;
			}

			int ntHeaderSize = 0;
			if (ntHeader64 != null)
				ntHeaderSize = NTOptionalHeader64.NTHDRSZ;
			else if (ntHeader32 != null)
				ntHeaderSize = NTOptionalHeader32.NTHDRSZ;

			offset += FileHeader.FILHSZ + getOptionalHeader().getSize() + ntHeaderSize;
			accessFile.seek(offset);
			dataDirectories = new ImageDataDirectory[PEConstants.IMAGE_NUMBEROF_DIRECTORY_ENTRIES];
			byte[] data = new byte[dataDirectories.length * (4 + 4)];
			accessFile.readFully(data);
			ReadMemoryAccess memory = new ReadMemoryAccess(data, true);
			for (int i = 0; i < dataDirectories.length; i++) {
				int rva = memory.getInt();
				int size = memory.getInt();
				dataDirectories[i] = new ImageDataDirectory(rva, size);
			}
		}
		return dataDirectories;
	}

	public SectionHeader[] getSectionHeaders() throws IOException {
		if (scnhdrs == null) {
			RandomAccessFile accessFile = getRandomAccessFile();
			scnhdrs = new SectionHeader[fileHeader.f_nscns];
			long offset = 0;
			if (dosHeader != null) {
				offset = dosHeader.e_lfanew + 4 /* NT SIG */;
			}
			offset += FileHeader.FILHSZ + fileHeader.f_opthdr;
			for (int i = 0; i < scnhdrs.length; i++, offset += SectionHeader.SCNHSZ) {
				scnhdrs[i] = new SectionHeader(accessFile, offset);
			}
		}
		return scnhdrs;
	}

	public Symbol[] getSymbols() throws IOException {
		if (symbolTable == null) {
			SectionHeader[] secHeaders = getSectionHeaders();
			NTOptionalHeader64 ntHeader64 = getNTOptionalHeader64();
			NTOptionalHeader32 ntHeader32 = getNTOptionalHeader32();

			RandomAccessFile accessFile = getRandomAccessFile();
			long offset = fileHeader.f_symptr;
			symbolTable = new Symbol[fileHeader.f_nsyms];
			for (int i = 0; i < symbolTable.length; i++, offset += Symbol.SYMSZ) {
				Symbol newSym = new Symbol(accessFile, offset, (fileHeader.f_flags & FileHeader.F_AR32WR) == 0);

				// Now convert section offset of the symbol to image offset.
				if (newSym.n_scnum >= 1 && newSym.n_scnum <= secHeaders.length) // valid section #
					newSym.n_value += secHeaders[newSym.n_scnum - 1].s_vaddr;

				// convert to absolute address.
				if (ntHeader64 != null)
					newSym.n_value += ntHeader64.ImageBase;
				else if (ntHeader32 != null)
					newSym.n_value += ntHeader32.ImageBase;

				symbolTable[i] = newSym;
			}
		}
		return symbolTable;
	}

	public byte[] getStringTable() throws IOException {
		if (stringTable == null) {
			if (fileHeader.f_nsyms > 0) {
				RandomAccessFile accessFile = getRandomAccessFile();
				long symbolsize = Symbol.SYMSZ * fileHeader.f_nsyms;
				long offset = fileHeader.f_symptr + symbolsize;
				accessFile.seek(offset);
				byte[] bytes = new byte[4];
				accessFile.readFully(bytes);
				int str_len = ReadMemoryAccess.getIntLE(bytes);
				if (str_len > 4 && str_len < accessFile.length()) {
					str_len -= 4;
					stringTable = new byte[str_len];
					accessFile.seek(offset + 4);
					accessFile.readFully(stringTable);
				} else {
					stringTable = new byte[0];
				}
			} else {
				stringTable = new byte[0];
			}
		}
		return stringTable;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		if (exeHeader != null) {
			buffer.append(exeHeader);
		}
		if (dosHeader != null) {
			buffer.append(dosHeader);
		}
		buffer.append(fileHeader);
		if (optionalHeader != null) {
			buffer.append(optionalHeader);
		}
		if (ntHeader64 != null) {
			buffer.append(ntHeader64);
		} else if (ntHeader32 != null) {
			buffer.append(ntHeader32);
		}
		try {
			ImageDataDirectory[] dirs = getImageDataDirectories();
			for (int i = 0; i < dirs.length; i++) {
				buffer.append("Entry ").append(i); //$NON-NLS-1$
				buffer.append(" ").append(dirs[i]); //$NON-NLS-1$
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
			e.printStackTrace();
		}

		try {
			Symbol[] symbols = getSymbols();
			for (int i = 0; i < symbols.length; i++) {
				buffer.append(symbols[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			byte[] bytes = getStringTable();
			String[] strings = Coff64.getStringTable(bytes);
			for (int i = 0; i < strings.length; i++) {
				buffer.append(strings[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}

	RandomAccessFile getRandomAccessFile() throws IOException {
		if (rfile == null) {
			rfile = new RandomAccessFile(filename, "r"); //$NON-NLS-1$
		}
		return rfile;
	}

	private ISymbolReader createCodeViewReader() {
		ISymbolReader symReader = null;
		final int IMAGE_DIRECTORY_ENTRY_DEBUG = 6;

		try {
			// the debug directory is the 6th entry
			NTOptionalHeader64 ntHeader64 = getNTOptionalHeader64();
			NTOptionalHeader32 ntHeader32 = getNTOptionalHeader32();
			if (ntHeader32 == null && ntHeader64 == null)
				return null;

			int debugDir = 0, debugFormats = 0;

			if (ntHeader64 != null) {
				if (ntHeader64.NumberOfRvaAndSizes < IMAGE_DIRECTORY_ENTRY_DEBUG)
					return null;

				debugDir = ntHeader64.DataDirectory[IMAGE_DIRECTORY_ENTRY_DEBUG].VirtualAddress;
				if (debugDir == 0)
					return null;

				debugFormats = ntHeader64.DataDirectory[IMAGE_DIRECTORY_ENTRY_DEBUG].Size / 28;
				if (debugFormats == 0)
					return null;
			} else if (ntHeader32 != null) {
				if (ntHeader32.NumberOfRvaAndSizes < IMAGE_DIRECTORY_ENTRY_DEBUG)
					return null;

				debugDir = ntHeader32.DataDirectory[IMAGE_DIRECTORY_ENTRY_DEBUG].VirtualAddress;
				if (debugDir == 0)
					return null;

				debugFormats = ntHeader32.DataDirectory[IMAGE_DIRECTORY_ENTRY_DEBUG].Size / 28;
				if (debugFormats == 0)
					return null;
			}

			SectionHeader[] sections = getSectionHeaders();

			// loop through the section headers to find the .rdata section
			for (int i = 0; i < sections.length; i++) {
				String name = new String(sections[i].s_name).trim();
				if (name.equals(".rdata")) { //$NON-NLS-1$
					// figure out the file offset of the debug ddirectory entries
					int offsetInto_rdata = debugDir - sections[i].s_vaddr;
					int fileOffset = sections[i].s_scnptr + offsetInto_rdata;
					RandomAccessFile accessFile = getRandomAccessFile();

					// loop through the debug directories looking for CodeView (type 2)
					for (int j = 0; j < debugFormats; j++) {
						PE64.IMAGE_DEBUG_DIRECTORY dir = new PE64.IMAGE_DEBUG_DIRECTORY(accessFile, fileOffset);

						if ((2 == dir.Type) && (dir.SizeOfData > 0)) {
							// CodeView found, seek to actual data
							int debugBase = dir.PointerToRawData;
							accessFile.seek(debugBase);

							// sanity check.  the first four bytes of the CodeView
							// data should be "NB11"
							String s2 = accessFile.readLine();
							if (s2.startsWith("NB11")) { //$NON-NLS-1$
								Attribute att = getAttribute();
								symReader = new CodeViewReader(accessFile, debugBase, att.isLittleEndian());
								return symReader;
							}
						}
						fileOffset += dir.DEBUGDIRSZ;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return symReader;
	}

	private ISymbolReader createStabsReader() {
		ISymbolReader symReader = null;
		try {
			SectionHeader[] sections = getSectionHeaders();
			byte[] stab = null;
			byte[] stabstr = null;

			// loop through the section headers looking for stabs info
			for (int i = 0; i < sections.length; i++) {
				String name = new String(sections[i].s_name).trim();
				if (name.equals(".stab")) { //$NON-NLS-1$
					stab = sections[i].getRawData();
				}
				if (name.equals(".stabstr")) { //$NON-NLS-1$
					stabstr = sections[i].getRawData();
				}
			}

			// if we found both sections then proceed
			if (stab != null && stabstr != null) {
				Attribute att = getAttribute();
				symReader = new StabsReader(stab, stabstr, att.isLittleEndian());
			}

		} catch (IOException e) {
		}
		return symReader;
	}

	public ISymbolReader getSymbolReader() {
		ISymbolReader reader = null;
		reader = createStabsReader();
		if (reader == null) {
			reader = createCodeViewReader();
		}
		if (reader == null) {
			reader = createDwarfReader();
		}
		return reader;
	}

	private ISymbolReader createDwarfReader() {
		DwarfReader reader = null;
		// Check if Dwarf data exists
		try {
			reader = new DwarfReader(this);
		} catch (IOException e) {
			// No Dwarf data in the Elf.
		}
		return reader;
	}

	/**
	 * @since 5.1
	 */
	public String getStringTableEntry(int offset) throws IOException {
		byte[] bytes = getStringTable();
		offset = offset - 4;
		for (int i = offset; i < bytes.length; i++) {
			if (bytes[i] == 0) {
				return new String(bytes, offset, i - offset);
			}
		}

		return ""; //$NON-NLS-1$
	}

	/**
	 * @since 5.1
	 */
	public String getFilename() {
		return filename;
	}
}
