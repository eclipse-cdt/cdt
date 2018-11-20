/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Abeer Bagul (Tensilica) - bug 102434
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;

/**
 *  The <code>AR</code> class is used for parsing standard ELF archive (ar) files.
 *
 *  Each object within the archive is represented by an ARHeader class.  Each of
 *  of these objects can then be turned into an Elf object for performing Elf
 *  class operations.
 *  @see ARHeader
 */
public class AR {

	protected String filename;
	protected ERandomAccessFile efile;
	protected long strtbl_pos = -1;
	private ARHeader[] headers;

	public void dispose() {
		try {
			if (efile != null) {
				efile.close();
				efile = null;
			}
		} catch (IOException e) {
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

	/**
	 * The <code>ARHeader</code> class is used to store the per-object file
	 *  archive headers.  It can also create an Elf object for inspecting
	 *  the object file data.
	 */
	public class ARHeader {

		private static final int NAME_IDX = 0;
		private static final int NAME_LEN = 16;
		//		private static final int MTIME_IDX= 16;
		//		private static final int MTIME_LEN= 12;
		//		private static final int UID_IDX= 28;
		//		private static final int UID_LEN= 6;
		//		private static final int GID_IDX= 34;
		//		private static final int GID_LEN= 6;
		//		private static final int MODE_IDX= 40;
		//		private static final int MODE_LEN= 8;
		private static final int SIZE_IDX = 48;
		private static final int SIZE_LEN = 10;
		//		private static final int TRAILER_IDX= 58;
		//		private static final int TRAILER_LEN= 2;
		private static final int HEADER_LEN = 60;

		private String object_name;
		//		private String modification_time;
		//		private String uid;
		//		private String gid;
		//		private String mode;
		private long size;
		private long obj_offset;

		/**
		 * Remove the padding from the archive header strings.
		 */
		private String removeBlanks(String str) {
			return str.trim();
		}

		/**
		 * Look up the name stored in the archive's string table based
		 * on the offset given.
		 *
		 * Maintains <code>efile</code> file location.
		 *
		 * @param offset
		 *    Offset into the string table for first character of the name.
		 * @throws IOException
		 *    <code>offset</code> not in string table bounds.
		 */
		private String nameFromStringTable(long offset) throws IOException {
			StringBuilder name = new StringBuilder(0);
			long pos = efile.getFilePointer();

			try {
				if (strtbl_pos != -1) {
					byte temp;
					efile.seek(strtbl_pos + offset);
					while ((temp = efile.readByte()) != '\n')
						name.append((char) temp);
				}
			} finally {
				efile.seek(pos);
			}

			return name.toString();
		}

		/**
		 * Creates a new archive header object.
		 *
		 * Assumes that efile is already at the correct location in the file.
		 *
		 * @throws IOException
		 *    There was an error processing the header data from the file.
		 */
		ARHeader() throws IOException {
			byte[] buf = new byte[HEADER_LEN];
			//
			// Read in the archive header data. Fixed sizes.
			//
			efile.readFully(buf);
			//
			// Save this location so we can create the Elf object later.
			//
			obj_offset = efile.getFilePointer();

			//
			// Convert the raw bytes into strings and numbers.
			//
			this.object_name = removeBlanks(new String(buf, NAME_IDX, NAME_LEN));
			//			this.modification_time = new String(buf, MTIME_IDX, MTIME_LEN);
			//			this.uid = new String(buf, UID_IDX, UID_LEN);
			//			this.gid = new String(buf, GID_IDX, GID_LEN);
			//			this.mode = new String(buf, MODE_IDX, MODE_LEN);
			this.size = Long.parseLong(removeBlanks(new String(buf, SIZE_IDX, SIZE_LEN)));

			//
			// If the name is of the format "/<number>", get name from the
			// string table.
			//
			if (strtbl_pos != -1 && this.object_name.length() > 1 && this.object_name.charAt(0) == '/') {
				try {
					long offset = Long.parseLong(this.object_name.substring(1));
					this.object_name = nameFromStringTable(offset);
				} catch (java.lang.Exception e) {
				}
			}

			//
			// Strip the trailing / from the object name.
			//
			int len = this.object_name.length();
			if (len > 2 && this.object_name.charAt(len - 1) == '/') {
				this.object_name = this.object_name.substring(0, len - 1);
			}
		}

		/** Get the name of the object file */
		public String getObjectName() {
			return object_name;
		}

		/** Get the size of the object file . */
		public long getSize() {
			return size;
		}

		public String getArchiveName() {
			return filename;
		}

		public long getObjectDataOffset() {
			return obj_offset;
		}

		public byte[] getObjectData() throws IOException {
			byte[] temp = new byte[(int) size];
			if (efile != null) {
				efile.seek(obj_offset);
				efile.read(temp);
			} else {
				efile = new ERandomAccessFile(filename, "r"); //$NON-NLS-1$
				efile.seek(obj_offset);
				efile.read(temp);
				efile.close();
				efile = null;
			}
			return temp;
		}
	}

	public static boolean isARHeader(byte[] ident) {
		if (ident.length < 7 || ident[0] != '!' || ident[1] != '<' || ident[2] != 'a' || ident[3] != 'r'
				|| ident[4] != 'c' || ident[5] != 'h' || ident[6] != '>')
			return false;
		return true;
	}

	/**
	 *  Creates a new <code>AR</code> object from the contents of
	 *  the given file.
	 *
	 *  @param filename The file to process.
	 *  @throws IOException The file is not a valid archive.
	 */
	public AR(String filename) throws IOException {
		this.filename = filename;
		boolean goodAr = false;
		try {
			efile = new ERandomAccessFile(filename, "r"); //$NON-NLS-1$
			byte[] hdrBytes = new byte[7];
			efile.readFully(hdrBytes);
			goodAr = isARHeader(hdrBytes);
			if (!goodAr) {
				throw new IOException(CCorePlugin.getResourceString("Util.exception.invalidArchive")); //$NON-NLS-1$
			}
			efile.readLine();
		} finally {
			if (!goodAr && efile != null) {
				efile.close();
				efile = null;
			}
		}
	}

	/** Load the headers from the file (if required).  */
	private void loadHeaders() throws IOException {
		if (headers != null)
			return;

		Vector<ARHeader> v = new Vector<>();
		try {
			//
			// Check for EOF condition
			//
			while (efile.getFilePointer() < efile.length()) {
				ARHeader header = new ARHeader();
				String name = header.getObjectName();

				long pos = efile.getFilePointer();

				//
				// If the name starts with a / it is specical.
				//
				if (name.charAt(0) != '/')
					v.add(header);

				//
				// If the name is "//" then this is the string table section.
				//
				if (name.compareTo("//") == 0) //$NON-NLS-1$
					strtbl_pos = pos;

				//
				// Compute the location of the next header in the archive.
				//
				pos += header.getSize();
				if ((pos % 2) != 0)
					pos++;

				efile.seek(pos);
			}
		} catch (IOException e) {
		}
		headers = v.toArray(new ARHeader[0]);
	}

	/**
	 *  Get an array of all the object file headers for this archive.
	 *
	 * @throws IOException
	 *    Unable to process the archive file.
	 * @return An array of headers, one for each object within the archive.
	 * @see ARHeader
	 */
	public ARHeader[] getHeaders() throws IOException {
		loadHeaders();
		return headers;
	}

	private boolean stringInStrings(String str, String[] set) {
		for (int i = 0; i < set.length; i++)
			if (str.compareTo(set[i]) == 0)
				return true;
		return false;
	}

	public String[] extractFiles(String outdir, String[] names) throws IOException {
		Vector<String> names_used = new Vector<>();
		String object_name;
		int count;

		loadHeaders();

		count = 0;
		for (int i = 0; i < headers.length; i++) {
			object_name = headers[i].getObjectName();
			if (names != null && !stringInStrings(object_name, names))
				continue;

			object_name = "" + count + "_" + object_name; //$NON-NLS-1$ //$NON-NLS-2$
			count++;

			byte[] data = headers[i].getObjectData();
			File output = new File(outdir, object_name);
			names_used.add(object_name);

			RandomAccessFile rfile = new RandomAccessFile(output, "rw"); //$NON-NLS-1$
			rfile.write(data);
			rfile.close();
		}

		return names_used.toArray(new String[0]);
	}

	public String[] extractFiles(String outdir) throws IOException {
		return extractFiles(outdir, null);
	}

}
