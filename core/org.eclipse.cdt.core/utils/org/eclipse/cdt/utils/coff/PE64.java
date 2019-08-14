/*******************************************************************************
 * Copyright (c) 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial PE class
 *     Markus Schorn (Wind River Systems)
 *     Space Codesign Systems - Support for 64 bit executables
 *******************************************************************************/

package org.eclipse.cdt.utils.coff;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.utils.coff.Coff.FileHeader;
import org.eclipse.cdt.utils.coff.Coff.SectionHeader;
import org.eclipse.cdt.utils.coff.Coff.Symbol;
import org.eclipse.cdt.utils.coff.Coff64.OptionalHeader;
import org.eclipse.cdt.utils.coff.Exe.ExeHeader;

/**
 * @since 6.9
 */
public class PE64 extends PE {

	NTOptionalHeader64 ntHeader64;
	NTOptionalHeader32 ntHeader32;
	private OptionalHeader optionalHeader;

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

	public PE64(String filename) throws IOException {
		this(filename, 0);
	}

	public PE64(String filename, long pos) throws IOException {
		this(filename, pos, true);
	}

	public PE64(String filename, long pos, boolean filter) throws IOException {
		super(filename, pos, filter);
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

	public NTOptionalHeader64 getNTOptionalHeader64() {
		return ntHeader64;
	}

	public NTOptionalHeader32 getNTOptionalHeader32() {
		return ntHeader32;
	}

	@Override
	public ImageDataDirectory[] getImageDataDirectories() throws IOException {
		if (dataDirectories == null) {
			RandomAccessFile accessFile = getRandomAccessFile();
			long offset = 0;
			if (dosHeader != null) {
				offset = dosHeader.e_lfanew + 4/*NT SIG*/;
			}
			offset += FileHeader.FILHSZ + optionalHeader.getSize() + NTOptionalHeader.NTHDRSZ;
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

	@Override
	public Symbol[] getSymbols() throws IOException {
		if (symbolTable == null) {
			SectionHeader[] secHeaders = getSectionHeaders();
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

}
