/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
package org.eclipse.cdt.utils.coff;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.cdt.utils.coff.Coff.FileHeader;
import org.eclipse.cdt.utils.coff.Coff.OptionalHeader;
import org.eclipse.cdt.utils.coff.Coff.SectionHeader;
import org.eclipse.cdt.utils.coff.Coff.Symbol;
import org.eclipse.cdt.utils.coff.Exe.ExeHeader;

/**
 * The PE file header consists of an MS-DOS stub, the PE signalture, the COFF file Header
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
 */
public class PE {

	public static final String NL = System.getProperty("line.separator", "\n");
	RandomAccessFile rfile;
	ExeHeader exeHeader;
	DOSHeader dosHeader;
	FileHeader fileHeader;
	OptionalHeader optionalHeader;
	NTOptionalHeader ntHeader;
	ImageDataDirectory[] dataDirectories;
	SectionHeader[] scnhdrs;
	Symbol[] symbolTable;
	byte[] stringTable;

	/**
	 */
	public static class DOSHeader {
		final static int DOSHDRSZ = 100;
		byte[] e_res = new byte[8];      /* Reserved words, all 0x0.  */
		byte[] e_oemid = new byte[2];    /* OEM identifier (for e_oeminfo), 0x0.  */
		byte[] e_oeminfo = new byte[2];  /* OEM information; e_oemid specific, 0x0.  */
		byte[] e_res2 = new byte[20];    /* Reserved words, all 0x0.  */
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
			memory.getBytes(e_res);
			memory.getBytes(e_oemid);
			memory.getBytes(e_oeminfo);
			memory.getBytes(e_res2);
			e_lfanew = memory.getInt();
			memory.getBytes(dos_message);
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("DOS STUB VALUES").append(NL);
			buffer.append("e_lfanew = ").append(e_lfanew).append(NL);
			buffer.append(new String(dos_message)).append(NL);
			return buffer.toString();
		}
	}

	public static class NTOptionalHeader {

		public final static int NTHDRSZ = 68;
		public int  ImageBase;                     // 4 bytes.
		public int  SectionAlignment;              // 4 bytes.
		public int  FileAlignment;                 // 4 bytes.
		public short  MajorOperatingSystemVersion; // 2 bytes.
		public short  MinorOperatingSystemVersion; // 2 bytes.
		public short  MajorImageVersion;           // 2 bytes.
		public short  MinorImageVersion;           // 2 bytes.
		public short  MajorSubsystemVersion;       // 2 bytes.
		public short  MinorSubsystemVersion;       // 2 bytes.
		public byte[]  Reserved = new byte[4];     // 4 bytes.
		public int  SizeOfImage;                   // 4 bytes. 
		public int  SizeOfHeaders;                 // 4 bytes. 
		public int  CheckSum;                      // 4 bytes. 
		public short Subsystem;                    // 2 bytes.
		public short DLLCharacteristics;           // 2 bytes.
		public int  SizeOfStackReserve;            // 4 bytes. 
		public int  SizeOfStackCommit;             // 4 bytes. 
		public int  SizeOfHeapReserve;             // 4 bytes. 
		public int  SizeOfHeapCommit;              // 4 bytes. 
		public int  LoaderFlags;                   // 4 bytes. 
		public int  NumberOfRvaAndSizes;           // 4 bytes. 

		public NTOptionalHeader(RandomAccessFile file) throws IOException {
			this(file, file.getFilePointer());
		}

		public NTOptionalHeader(RandomAccessFile file, long offset) throws IOException {
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
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("NT OPTIONAL HEADER VALUES").append(NL);
			buffer.append("ImageBase = ").append(ImageBase).append(NL);
			buffer.append("SexctionAlignement = ").append(SectionAlignment).append(NL);
			buffer.append("FileAlignment = ").append(FileAlignment).append(NL);
			buffer.append("MajorOSVersion = ").append(MajorOperatingSystemVersion).append(NL);
			buffer.append("MinorOSVersion = ").append(MinorOperatingSystemVersion).append(NL);
			buffer.append("MajorImageVersion = ").append(MajorImageVersion).append(NL);
			buffer.append("MinorImageVersion = ").append(MinorImageVersion).append(NL);
			buffer.append("MajorSubVersion = ").append(MajorSubsystemVersion).append(NL);
			buffer.append("MinorSubVersion = ").append(MinorSubsystemVersion).append(NL);
			buffer.append("Reserved = ").append(Reserved).append(NL);
			buffer.append("SizeOfImage = ").append(SizeOfImage).append(NL);
			buffer.append("SizeOfHeaders = ").append(SizeOfHeaders).append(NL);
			buffer.append("CheckSum = ").append(CheckSum).append(NL);
			buffer.append("Subsystem = ").append(Subsystem).append(NL);
			buffer.append("DLL = ").append(DLLCharacteristics).append(NL);
			buffer.append("StackReserve = ").append(SizeOfStackReserve).append(NL);
			buffer.append("StackCommit = ").append(SizeOfStackCommit).append(NL);
			buffer.append("HeapReserve = ").append(SizeOfHeapReserve).append(NL);
			buffer.append("HeapCommit = ").append(SizeOfHeapCommit).append(NL);
			buffer.append("LoaderFlags = ").append(LoaderFlags).append(NL);;
			buffer.append("#Rva size = ").append(NumberOfRvaAndSizes).append(NL);
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

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("rva = ").append(rva).append(" ");
			buffer.append("size = ").append(size).append(NL);
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

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("rva = ").append(rva);
			buffer.append(" timestamp = ").append(timestamp);
			buffer.append(" forwarder = ").append(forwarder);
			buffer.append(" name = ").append(name);
			buffer.append(" thunk = ").append(thunk).append(NL);
			return buffer.toString();
		}
	}


	public PE (String filename) throws IOException {

		rfile = new RandomAccessFile(filename, "r");

		exeHeader = new ExeHeader(rfile);

		dosHeader = new DOSHeader(rfile);

		// Jump the Coff header, and Check the sig.
		rfile.seek(dosHeader.e_lfanew);
		byte[] sig = new byte[4]; 
		rfile.readFully(sig);
		if (!((sig[0] == 'P') && (sig[1] == 'E')
		   && (sig[2] == '\0') && (sig[3] == '\0'))) {
			throw new IOException("Not a PE format");
		}

		fileHeader = new Coff.FileHeader(rfile, rfile.getFilePointer());

		optionalHeader = new Coff.OptionalHeader(rfile, rfile.getFilePointer());

		ntHeader = new NTOptionalHeader(rfile, rfile.getFilePointer());

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

	public NTOptionalHeader getNTOptionalHeader() {
		return ntHeader;
	}

	public ImageDataDirectory[] getImageDataDirectories() throws IOException {
		if (dataDirectories == null) {
			long offset = dosHeader.e_lfanew + FileHeader.FILHSZ +
				OptionalHeader.AOUTHDRSZ + NTOptionalHeader.NTHDRSZ + 4/*NT SIG*/;
			rfile.seek(offset);
			dataDirectories = new ImageDataDirectory[PEConstants.IMAGE_NUMBEROF_DIRECTORY_ENTRIES];
			byte[] data = new byte[dataDirectories.length * (4 + 4)];
			rfile.readFully(data);
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
			scnhdrs = new SectionHeader[fileHeader.f_nscns];
			long offset = dosHeader.e_lfanew +
				FileHeader.FILHSZ + fileHeader.f_opthdr + 4 /* NT SIG */;
			for (int i = 0; i < scnhdrs.length; i++, offset += SectionHeader.SCNHSZ) {
				scnhdrs[i] = new SectionHeader(rfile, offset);
			}
		}
		return scnhdrs;
	}

	public Symbol[] getSymbols() throws IOException {
		if (symbolTable == null) {
			long offset = fileHeader.f_symptr;
			symbolTable = new Symbol[fileHeader.f_nsyms];
			for (int i = 0; i < symbolTable.length; i++, offset += Symbol.SYMSZ) {
				symbolTable[i] = new Symbol(rfile, offset);
			}
		}
		return symbolTable;
	}

	public byte[] getStringTable() throws IOException {
		if (stringTable == null) {
			if (fileHeader.f_nsyms > 0) {
				long symbolsize = Symbol.SYMSZ * fileHeader.f_nsyms;
				long offset = fileHeader.f_symptr + symbolsize;
				rfile.seek(offset);
				byte[] bytes = new byte[4];
				rfile.readFully(bytes);
				int str_len = ReadMemoryAccess.getIntLE(bytes);
				if (str_len > 4) {
					str_len -= 4;
					stringTable = new byte[str_len];
					rfile.seek(offset + 4);
					rfile.readFully(stringTable);
				} else {
					stringTable = new byte[0];
				}
			} else {
				stringTable = new byte[0];
			}
		}
		return stringTable;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(exeHeader);
		buffer.append(dosHeader);
		buffer.append(fileHeader);
		buffer.append(optionalHeader);
		buffer.append(ntHeader);
		try {
			ImageDataDirectory[] dirs = getImageDataDirectories();
			for (int i = 0; i < dirs.length; i++) {
				buffer.append("Entry ").append(i);
				buffer.append(" ").append(dirs[i]);
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
			String[] strings = Coff.getStringTable(bytes);
			for (int i = 0; i < strings.length; i++) {
				buffer.append(strings[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}

	public static void main(String[] args) {
		try {
			PE pe = new PE(args[0]);
			System.out.println(pe);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
