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
 *     QNX Software Systems - Initial Coff class
 *     Space Codesign Systems - Support for 64 bit executables
 *******************************************************************************/
package org.eclipse.cdt.utils.coff;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @since 6.9
 */
public class Coff64 extends Coff {

	private OptionalHeader opthdr;

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
		if (opthdr != null) {
			buffer.append(opthdr);
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

	public Coff64(String filename) throws IOException {
		this(new RandomAccessFile(filename, "r"), 0); //$NON-NLS-1$
	}

	public Coff64(RandomAccessFile file, long offset) throws IOException {
		super(file, offset);
		commonSetup(file, offset);
	}

	@Override
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
