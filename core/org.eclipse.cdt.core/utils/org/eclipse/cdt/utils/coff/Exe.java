/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.coff;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.cdt.core.CCorePlugin;

public class Exe {

	public static final String NL = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	protected RandomAccessFile rfile;
	ExeHeader ehdr;

	static public class ExeHeader {

		public final static int EXEHDRSZ = 28;
		public byte[] e_signature = new byte[2]; // 00-01 "MZ" - Link file .EXE signature
		public short e_lastsize;  // 02-03 Length of EXE file modulo 512
		public short e_nblocks;   // 04-05 Number of 512 pages (including the last page)
		public short e_nreloc;    // 06-07 Number of relocation entries
		public short e_hdrsize;   // 08-09 Size of header in 16 byte paragraphs,
					  //       occupied by "EXE" header and relo table.

		public short e_minalloc;  // 0A-0B Minimum paragraphs of memory allocated
		public short e_maxalloc;  // 0C-0D Maximum number of paragraphs allocated
					  //       in addition to the code size
		public short e_ss;        // 0E-0F Initial SS relative to start of executable
		public short e_sp;        // 10-11 Initial SP
		public short e_checksum;  // 12-13 Checksum (or 0) of executable
		public short e_ip;        // 14-15 CS:IP relative to start of executable
		public short e_cs;        // 16-17 CS:IP relative to start of executable
		public short e_relocoffs; // 18-19 Offset of relocation table;
					  //       40h for new-(NE,LE,LX,W3,PE etc.) executable
		public short e_noverlay;  // 1A-1B Overlay number (0h = main program)

		protected ExeHeader(RandomAccessFile file)  throws IOException {
			this(file, file.getFilePointer());
		}

		protected ExeHeader(RandomAccessFile file, long offset)  throws IOException {
			file.seek(offset);
			byte[] hdr = new byte[EXEHDRSZ];
			file.readFully(hdr);
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, true);
			commonSetup(memory);
		}

		public ExeHeader(byte[] hdr, boolean little) throws IOException {
			ReadMemoryAccess memory = new ReadMemoryAccess(hdr, true);
			commonSetup(memory);
		}

		public ExeHeader(ReadMemoryAccess memory) throws IOException {
			commonSetup(memory);
		}

		void commonSetup(ReadMemoryAccess memory) throws IOException {
			if (memory.getSize() < EXEHDRSZ) {
				throw new IOException("Not DOS EXE format"); //$NON-NLS-1$
			}
			memory.getBytes(e_signature);
			if (e_signature[0] != 'M' || e_signature[1] != 'Z') {
				throw new IOException(CCorePlugin.getResourceString("Util.exception.notDOSFormat")); //$NON-NLS-1$
			}
			e_lastsize = memory.getShort();
			e_nblocks = memory.getShort();
			e_nreloc = memory.getShort();
			e_hdrsize = memory.getShort();
			e_minalloc = memory.getShort();
			e_maxalloc = memory.getShort();
			e_ss = memory.getShort();
			e_sp = memory.getShort();
			e_checksum = memory.getShort();
			e_ip = memory.getShort();
			e_cs = memory.getShort();
			e_relocoffs = memory.getShort();
			e_noverlay = memory.getShort();
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();

			buffer.append("EXE HEADER VALUES").append(NL); //$NON-NLS-1$
			buffer.append("signature "); //$NON-NLS-1$
			buffer.append((char)e_signature[0] + " " + (char)e_signature[1]); //$NON-NLS-1$
			buffer.append(NL);

			buffer.append("lastsize: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_lastsize).longValue()));
			buffer.append(NL);

			buffer.append("nblocks: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_nblocks).longValue()));
			buffer.append(NL);

			buffer.append("nreloc: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_nreloc).longValue()));
			buffer.append(NL);

			buffer.append("hdrsize: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_hdrsize).longValue()));
			buffer.append(NL);

			buffer.append("minalloc: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_minalloc).longValue()));
			buffer.append(NL);

			buffer.append("maxalloc: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_maxalloc).longValue()));
			buffer.append(NL);
			buffer.append("ss: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_ss).longValue()));
			buffer.append(NL);

			buffer.append("sp: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_sp).longValue()));
			buffer.append(NL);

			buffer.append("checksum: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_checksum).longValue()));
			buffer.append(NL);

			buffer.append("ip: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_ip).longValue()));
			buffer.append(NL);

			buffer.append("cs: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_cs).longValue()));
			buffer.append(NL);

			buffer.append("relocoffs: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_relocoffs).longValue()));
			buffer.append(NL);

			buffer.append("overlay: 0x"); //$NON-NLS-1$
			buffer.append(Long.toHexString(new Short(e_noverlay).longValue()));
			buffer.append(NL);
			return buffer.toString();
		}
	}

	public ExeHeader getExeHeader() throws IOException {
		return ehdr;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(rfile).append(NL);
		buffer.append(ehdr);
		return buffer.toString();
	}

	public Exe(String file) throws IOException {
		rfile = new RandomAccessFile(file, "r"); //$NON-NLS-1$
		try {
			ehdr = new ExeHeader(rfile);
		} finally {
			if (ehdr == null) {
				rfile.close();
			}
		}
	}

	public static void main(String[] args) {
		try {
			Exe exe = new Exe(args[0]);
			System.out.println(exe);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
