/*******************************************************************************
 * Copyright (c) 2002, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.macho;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;

/**
 *  The <code>AR</code> class is used for parsing standard archive (ar) files.
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
	 *  archive headers.  It can also create an Mach-O object for inspecting
	 *  the object file data.
	 */
	public class ARHeader {

		private String object_name;
//		private String modification_time;
//		private String uid;
//		private String gid;
//		private String mode;
		private long size;
//		private long file_offset;
		private long macho_offset;

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
//		private String nameFromStringTable(long offset) throws IOException {
//			StringBuffer name = new StringBuffer(0);
//			long pos = efile.getFilePointer();
//
//			try {
//				if (strtbl_pos != -1) {
//					byte temp;
//					efile.seek(strtbl_pos + offset);
//					while ((temp = efile.readByte()) != '\n')
//						name.append((char) temp);
//				}
//			} finally {
//				efile.seek(pos);
//			}
//
//			return name.toString();
//		}

		/**
		 * Creates a new archive header object.  
		 *
		 * Assumes that efile is already at the correct location in the file.
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
			efile.read(object_name);
			efile.read(modification_time);
			efile.read(uid);
			efile.read(gid);
			efile.read(mode);
			efile.read(size);
			efile.read(trailer);

			//
			// Save this location so we can create the Mach-O object later.
			//
			macho_offset = efile.getFilePointer();

			//
			// Convert the raw bytes into strings and numbers.
			//
			this.object_name = new String(object_name).trim();
//			this.modification_time = new String(modification_time);
//			this.uid = new String(uid);
//			this.gid = new String(gid);
//			this.mode = new String(mode);
			this.size = Long.parseLong(new String(size).trim());

			//
			// If the name is of the format "#1/<number>", real name directly follows the
			// header.
			//
			if (this.object_name.length() > 3 
					&& this.object_name.charAt(0) == '#' 
					&& this.object_name.charAt(1) == '1' 
					&& this.object_name.charAt(2) == '/') {
				try {
					int len = Integer.parseInt(this.object_name.substring(3));
					byte[] real_name = new byte[len];
					efile.read(real_name);
					this.object_name = new String(real_name);
					long pos = efile.getFilePointer();
					efile.seek(macho_offset);
					macho_offset = pos;
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

		/**
		 *  Create an new MachO object for the object file.
		 *
		 * @throws IOException 
		 *    Not a valid MachO object file.
		 * @return A new MachO object.  
		 * @see MachO#MachO( String, long )
		 */
		public long getObjectDataOffset() throws IOException {
			return macho_offset;
		}

		public byte[] getObjectData() throws IOException {
			byte[] temp = new byte[(int) size];
			if (efile != null) {
				efile.seek(macho_offset);
				efile.read(temp);
			} else {
				efile = new ERandomAccessFile(filename, "r"); //$NON-NLS-1$
				efile.seek(macho_offset);
				efile.read(temp);
				efile.close();
				efile = null;
			}
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
	public AR(String filename) throws IOException {
		this.filename = filename;
		efile = new ERandomAccessFile(filename, "r"); //$NON-NLS-1$
		String hdr = efile.readLine();
		if (hdr == null || hdr.compareTo("!<arch>") != 0) { //$NON-NLS-1$
			efile.close();
			throw new IOException(CCorePlugin.getResourceString("Util.exception.invalidArchive")); //$NON-NLS-1$
		}
	}

	/** Load the headers from the file (if required).  */
	private void loadHeaders() throws IOException {
		if (headers != null)
			return;

		Vector<ARHeader> v = new Vector<ARHeader>();
		try {
			//
			// Check for EOF condition
			//
			while (efile.getFilePointer() < efile.length()) {
				ARHeader header = new ARHeader();
				header.getObjectName();

				long pos = efile.getFilePointer();

				v.add(header);

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
		// strtbl_pos = ???;
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
		for (String element : set)
			if (str.compareTo(element) == 0)
				return true;
		return false;
	}

	public String[] extractFiles(String outdir, String[] names) throws IOException {
		Vector<String> names_used = new Vector<String>();
		String object_name;
		int count;

		loadHeaders();

		count = 0;
		for (ARHeader header : headers) {
			object_name = header.getObjectName();
			if (names != null && !stringInStrings(object_name, names))
				continue;

			object_name = "" + count + "_" + object_name; //$NON-NLS-1$ //$NON-NLS-2$
			count++;

			byte[] data = header.getObjectData();
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
