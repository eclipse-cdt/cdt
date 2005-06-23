/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.coff;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;

/**
 *  The <code>AR</code> class is used for parsing standard ELF archive (ar) files.
 *
 *  Each object within the archive is represented by an ARHeader class.  Each of
 *  of these objects can then be turned into an PE object for performing PE
 *  class operations.
 *  @deprecated - use org.eclipse.cdt.ui.utils.AR
 *  @see ARHeader
 */
public class PEArchive {

	protected String filename;
	protected RandomAccessFile rfile;
	protected long strtbl_pos = -1;
	private ARHeader[] headers;

	public void dispose() {
		try {
			if (rfile != null) {
				rfile.close();
				rfile = null;
			}
		} catch (IOException e) {
		}
	}

	/**
	 * Do not leak fds.
	 */
	protected void finalize() throws Throwable {
		try {
			dispose();
		} finally {
			super.finalize();
		}
	}

	/**
	 * The <code>ARHeader</code> class is used to store the per-object file 
	 *  archive headers.  It can also create an PE object for inspecting
	 *  the object file data.
	 */
	public class ARHeader {

		private String object_name;
		private String modification_time;
		private String uid;
		private String gid;
		private String mode;
		private long size;
		private long elf_offset;

		/**
		 * Remove the padding from the archive header strings.
		 */
		private String removeBlanks(String str) {
			while (str.charAt(str.length() - 1) == ' ')
				str = str.substring(0, str.length() - 1);
			return str;
		}

		/**
		 * Look up the name stored in the archive's string table based
		 * on the offset given. 
		 *
		 * Maintains <code>rfile</code> file location.
		 *
		 * @param offset 
		 *    Offset into the string table for first character of the name.
		 * @throws IOException 
		 *    <code>offset</code> not in string table bounds.
		 */
		private String nameFromStringTable(long offset) throws IOException {
			StringBuffer name = new StringBuffer(0);
			long pos = rfile.getFilePointer();

			try {
				if (strtbl_pos != -1) {
					byte temp;
					rfile.seek(strtbl_pos + offset);
					while ((temp = rfile.readByte()) != '\n')
						name.append((char) temp);
				}
			} finally {
				rfile.seek(pos);
			}

			return name.toString();
		}

		/**
		 * Creates a new archive header object.  
		 *
		 * Assumes that rfile is already at the correct location in the file.
		 *
		 * @throws IOException 
		 *    There was an error processing the header data from the file.
		 */
		public ARHeader() throws IOException {
			byte[] object_name = new byte[16];
			byte[] modification_time = new byte[12];
			byte[] uid = new byte[6];
			byte[] gid = new byte[6];
			byte[] mode = new byte[8];
			byte[] size = new byte[10];
			byte[] trailer = new byte[2];

			//
			// Read in the archive header data. Fixed sizes.
			//
			rfile.read(object_name);
			rfile.read(modification_time);
			rfile.read(uid);
			rfile.read(gid);
			rfile.read(mode);
			rfile.read(size);
			rfile.read(trailer);

			//
			// Save this location so we can create the PE object later.
			//
			elf_offset = rfile.getFilePointer();

			//
			// Convert the raw bytes into strings and numbers.
			//
			this.object_name = removeBlanks(new String(object_name));
			this.modification_time = new String(modification_time);
			this.uid = new String(uid);
			this.gid = new String(gid);
			this.mode = new String(mode);
			this.size = Long.parseLong(removeBlanks(new String(size)));

			//
			// If the name is of the format "/<number>", get name from the
			// string table.
			//
			if (strtbl_pos != -1
				&& this.object_name.length() > 1
				&& this.object_name.charAt(0) == '/') {
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

		/**
		 *  Create an new PE object for the object file.
		 *
		 * @throws IOException 
		 *    Not a valid PE object file.
		 * @return A new PE object.  
		 * @see PE#PE( String, long )
		 */
		public PE getPE() throws IOException {
			return new PE(filename, elf_offset);
		}

		public PE getPE(boolean filter_on) throws IOException {
			return new PE(filename, elf_offset, filter_on);
		}

		public byte[] getObjectData() throws IOException {
			byte[] temp = new byte[(int) size];
			rfile.seek(elf_offset);
			rfile.read(temp);
			return temp;
		}
	}

	public static boolean isARHeader(byte[] ident) {
		if (ident == null || ident.length < 7
			|| ident[0] != '!'
			|| ident[1] != '<'
			|| ident[2] != 'a'
			|| ident[3] != 'r'
			|| ident[4] != 'c'
			|| ident[5] != 'h'
			|| ident[6] != '>')
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
	public PEArchive(String filename) throws IOException {
		this.filename = filename;
		rfile = new RandomAccessFile(filename, "r"); //$NON-NLS-1$
		String hdr = rfile.readLine();
		if (hdr == null || hdr.compareTo("!<arch>") != 0) { //$NON-NLS-1$
			rfile.close();
			throw new IOException(CCorePlugin.getResourceString("Util.exception.invalidArchive")); //$NON-NLS-1$
		}
	}

	/** Load the headers from the file (if required).  */
	private void loadHeaders() throws IOException {
		if (headers != null)
			return;

		Vector v = new Vector();
		try {
			//
			// Check for EOF condition
			//
			while (rfile.getFilePointer() < rfile.length()) {
				ARHeader header = new ARHeader();
				String name = header.getObjectName();

				long pos = rfile.getFilePointer();

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

				rfile.seek(pos);
			}
		} catch (IOException e) {
		}
		headers = (ARHeader[]) v.toArray(new ARHeader[0]);
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

	public String[] extractFiles(String outdir, String[] names)
		throws IOException {
		Vector names_used = new Vector();
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

		return (String[]) names_used.toArray(new String[0]);
	}

	public String[] extractFiles(String outdir) throws IOException {
		return extractFiles(outdir, null);
	}

}
