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
	String filename;
	ExeHeader exeHeader;
	DOSHeader dosHeader;
	FileHeader fileHeader;
	OptionalHeader optionalHeader;
	NTOptionalHeader ntHeader;
	ImageDataDirectory[] dataDirectories;
	SectionHeader[] scnhdrs;
	Symbol[] symbolTable;
	byte[] stringTable;

	public class Attribute {
		public static final int PE_TYPE_EXE   = 1;
		public static final int PE_TYPE_SHLIB = 2;
		public static final int PE_TYPE_OBJ   = 3;
		public static final int PE_TYPE_CORE  = 4;

		String cpu;
		int type;
		int word;
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

		public int getWord() {
			return word;
		}
	}

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
		this(filename, 0);
	}

	public PE(String filename, long pos) throws IOException {
		this(filename, pos, true);
	}

	public PE (String filename, long pos, boolean filter) throws IOException {
		try {
			rfile = new RandomAccessFile(filename, "r");
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
				if (!((sig[0] == 'P') && (sig[1] == 'E')
				   && (sig[2] == '\0') && (sig[3] == '\0'))) {
					throw new IOException("Not a PE format");
				}
			} catch (IOException e) {
				rfile.seek(pos);
			}

			fileHeader = new Coff.FileHeader(rfile, rfile.getFilePointer());

			// Check if this a valid machine.
			switch (fileHeader.f_magic) {
				case PEConstants.IMAGE_FILE_MACHINE_ALPHA:
				case PEConstants.IMAGE_FILE_MACHINE_ARM:
				case PEConstants.IMAGE_FILE_MACHINE_ALPHA64:
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
				break;

				default:
					throw new IOException("Unknow machine/format");
			}

			if (fileHeader.f_opthdr > 0) {
				optionalHeader = new Coff.OptionalHeader(rfile, rfile.getFilePointer());
				ntHeader = new NTOptionalHeader(rfile, rfile.getFilePointer());
			}
		} finally {
			if (rfile != null) {
				rfile.close();
				rfile = null;
			}
		}
	}

	public Attribute getAttribute() {
		Attribute attrib = new Attribute();
		FileHeader filhdr = getFileHeader();

		// Machine type.
		switch (filhdr.f_magic) {
			case PEConstants.IMAGE_FILE_MACHINE_UNKNOWN:
				attrib.cpu = "none";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_ALPHA:
				attrib.cpu = "alpha";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_ARM:
				attrib.cpu = "arm";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_ALPHA64:
				attrib.cpu = "arm64";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_I386:
				attrib.cpu = "i386";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_IA64:
				attrib.cpu = "ia64";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_M68K:
				attrib.cpu = "m68k";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_MIPS16:
				attrib.cpu = "mips16";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_MIPSFPU:
				attrib.cpu = "mipsfpu";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_MIPSFPU16:
				attrib.cpu = "mipsfpu16";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_POWERPC:
				attrib.cpu = "powerpc";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_R3000:
				attrib.cpu = "r3000";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_R4000:
				attrib.cpu = "r4000";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_R10000:
				attrib.cpu = "r10000";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_SH3:
				attrib.cpu = "sh3";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_SH4:
				attrib.cpu = "sh4";
			break;
			case PEConstants.IMAGE_FILE_MACHINE_THUMB:
				attrib.cpu = "thumb";
			break;
		}

		/* PE characteristics, FileHeader.f_flags.  */
		if ((filhdr.f_flags & PEConstants.IMAGE_FILE_DLL) != 0) {
			attrib.type = attrib.PE_TYPE_SHLIB;
		} else if ((filhdr.f_flags & PEConstants.IMAGE_FILE_EXECUTABLE_IMAGE) != 0) {
			attrib.type = attrib.PE_TYPE_EXE;
		} else {
			attrib.type = attrib.PE_TYPE_OBJ;
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
		return attrib;
	}

	public void dispose() throws IOException {
		if (rfile != null) {
			rfile.close();
			rfile = null;
		}
	}

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

	public NTOptionalHeader getNTOptionalHeader() {
		return ntHeader;
	}

	public ImageDataDirectory[] getImageDataDirectories() throws IOException {
		if (dataDirectories == null) {
			RandomAccessFile accessFile = getRandomAccessFile();
			long offset = 0;
			if (dosHeader != null) {
				offset = dosHeader.e_lfanew + 4/*NT SIG*/;
			}
			offset += FileHeader.FILHSZ + OptionalHeader.AOUTHDRSZ + NTOptionalHeader.NTHDRSZ;
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
			dispose();
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
			dispose();
		}
		return scnhdrs;
	}

	public Symbol[] getSymbols() throws IOException {
		if (symbolTable == null) {
			RandomAccessFile accessFile = getRandomAccessFile();
			long offset = fileHeader.f_symptr;
			symbolTable = new Symbol[fileHeader.f_nsyms];
			for (int i = 0; i < symbolTable.length; i++, offset += Symbol.SYMSZ) {
				symbolTable[i] = new Symbol(accessFile, offset);
			}
			dispose();
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
				if (str_len > 4) {
					str_len -= 4;
					stringTable = new byte[str_len];
					accessFile.seek(offset + 4);
					accessFile.readFully(stringTable);
				} else {
					stringTable = new byte[0];
				}
				dispose();
			} else {
				stringTable = new byte[0];
			}
		}
		return stringTable;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
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
		if (ntHeader != null) {
			buffer.append(ntHeader);
		}
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

	RandomAccessFile getRandomAccessFile () throws IOException {
		if (rfile == null) {
			rfile = new RandomAccessFile(filename, "r");
		}
		return rfile;
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
