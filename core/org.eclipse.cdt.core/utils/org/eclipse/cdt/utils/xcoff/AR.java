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
package org.eclipse.cdt.utils.xcoff;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;

/**
 *  The <code>AR</code> class is used for parsing standard XCOFF32 archive (ar) files.
 *
 *  Each object within the archive is represented by an ARHeader class.  Each of
 *  of these objects can then be turned into an XCOFF32 object for performing XCOFF32
 *  class operations.
 *  @see MemberHeader
 * 
 * @author vhirsl
 */
public class AR {
	protected String filename;
	private RandomAccessFile file;
	private ARHeader header;
	private MemberHeader[] memberHeaders;

	/**
	 * Content of an archive in AIX XCOFF32 format
	 * 
	 * @author vhirsl
	 */
	public class ARHeader {
		private final static int SAIAMAG = 8;
		
		private byte[] fl_magic = new byte[SAIAMAG];// Archive magic string
		private byte[] fl_memoff = new byte[20];	// Offset to member table
		private byte[] fl_gstoff = new byte[20];	// Offset to global symbol table
		private byte[] fl_gst64off = new byte[20];	// Offset to global symbol table for 64-bit objects  
		private byte[] fl_fstmoff = new byte[20];	// Offset to first archive member
		private byte[] fl_lstmoff = new byte[20];	// Offset to last archive member
		private byte[] fl_freeoff = new byte[20];	// Offset to first member on free list
		
		private long fstmoff = 0;
		private long lstmoff = 0;
		private long memoff = 0;
		
		public ARHeader() throws IOException {
			try {
				RandomAccessFile file = getRandomAccessFile();
				file.seek(0);
				file.read(fl_magic);
				if (isARHeader(fl_magic)) {
					file.read(fl_memoff);
					file.read(fl_gstoff);
					file.read(fl_gst64off);
					file.read(fl_fstmoff);
					file.read(fl_lstmoff);
					file.read(fl_freeoff);
					fstmoff = Long.parseLong(removeBlanks(new String(fl_fstmoff)));
					lstmoff = Long.parseLong(removeBlanks(new String(fl_lstmoff)));
					memoff = Long.parseLong(removeBlanks(new String(fl_memoff)));
				}
				
			} catch (IOException e) {
				dispose();
				CCorePlugin.log(e);
			}
		}

		/**
		 * @return boolean
		 */
		public boolean isXcoffARHeader() {
			return (fstmoff != 0);
		}

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
		file = new RandomAccessFile(filename, "r"); //$NON-NLS-1$
		header = new ARHeader();
		if (!header.isXcoffARHeader()) {
			file.close();
			throw new IOException(CCorePlugin.getResourceString("Util.exception.invalidArchive")); //$NON-NLS-1$
		}
	}

	public void dispose() {
		try {
			if (file != null) {
				file.close();
				file = null;
			}
		} catch (IOException e) {
		}
	}

	protected void finalize() throws Throwable {
		try {
			dispose();
		} finally {
			super.finalize();
		}
	}

	public static boolean isARHeader(byte[] ident) {
		if (ident == null || ident.length < 8
			|| ident[0] != '<'
			|| ident[1] != 'b'
			|| ident[2] != 'i'
			|| ident[3] != 'g'
			|| ident[4] != 'a'
			|| ident[5] != 'f'
			|| ident[6] != '>'
			|| ident[7] != '\n')
			return false;
		return true;
	}

	/**
	 * The <code>ARHeader</code> class is used to store the per-object file 
	 *  archive headers.  It can also create an XCOFF32 object for inspecting
	 *  the object file data.
	 */
	public class MemberHeader {
		byte[] ar_size = new byte[20];		// File member size - decimal
		byte[] ar_nxtmem = new byte[20];	// Next member offset - decimal
		byte[] ar_prvmem = new byte[20];	// Previous member offset - decimal
		byte[] ar_date = new byte[12];		// File member date - decimal
		byte[] ar_uid = new byte[12];		// File member userid - decimal
		byte[] ar_gid = new byte[12];		// File member group id - decimal
		byte[] ar_mode = new byte[12];		// File member mode - octal
		byte[] ar_namlen = new byte[4];		// File member name length -decimal
		byte[] ar_name;						// Start of member name
		byte[] ar_fmag = new byte[2];		// AIAFMAG - string to end "`\n"
		
		// converted values
		long size;
		long nxtmem;
		long prvmem;
		int namlen;
		String name;
		
		long file_offset;

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
		 * Maintains <code>file</code> file location.
		 *
		 * @param offset 
		 *    Offset into the string table for first character of the name.
		 * @throws IOException 
		 *    <code>offset</code> not in string table bounds.
		 */
//		private String nameFromStringTable(long offset) throws IOException {
//			StringBuffer name = new StringBuffer(0);
//			long pos = file.getFilePointer();
//
//			try {
//				if (strtbl_pos != -1) {
//					byte temp;
//					file.seek(strtbl_pos + offset);
//					while ((temp = file.readByte()) != '\n')
//						name.append((char) temp);
//				}
//			} finally {
//				file.seek(pos);
//			}
//
//			return name.toString();
//		}

		/**
		 * Creates a new archive header object.  
		 *
		 * Assumes that file is already at the correct location in the file.
		 *
		 * @throws IOException 
		 *    There was an error processing the header data from the file.
		 */
		public MemberHeader() throws IOException {
			//
			// Read in the archive header data. Fixed sizes.
			//
			RandomAccessFile file = getRandomAccessFile();
			file.read(ar_size);
			file.read(ar_nxtmem);
			file.read(ar_prvmem);
			file.read(ar_date);
			file.read(ar_uid);
			file.read(ar_gid);
			file.read(ar_mode);
			file.read(ar_namlen);
			namlen = Integer.parseInt(removeBlanks(new String(ar_namlen)));
			ar_name = new byte[namlen];
			file.read(ar_name);
			file.read(ar_fmag);

			size = Long.parseLong(removeBlanks(new String(ar_size)));
			nxtmem = Long.parseLong(removeBlanks(new String(ar_nxtmem)));
			prvmem = Long.parseLong(removeBlanks(new String(ar_prvmem)));
			name = new String(ar_name, 0, namlen);
			
			//
			// Save this location so we can create the XCOFF32 object later.
			//
			file_offset = file.getFilePointer();
			if ((file_offset % 2) == 1) {
				++file_offset;
			}
		}

		/** Get the name of the object file */
		public String getObjectName() {
			return name;
		}

		/** Get the size of the object file . */
		public long getSize() {
			return size;
		}
		
		public String getArchiveName() {
			return filename;
		}

		public long getObjectDataOffset() {
			return file_offset;
		}
		
		public byte[] getObjectData() throws IOException {
			byte[] temp = new byte[(int) size];
			RandomAccessFile file = getRandomAccessFile();
			file.seek(file_offset);
			file.read(temp);
			dispose();
			return temp;
		}
	}

	/** Load the headers from the file (if required).  */
	private void loadHeaders() throws IOException {
		if (memberHeaders != null)
			return;

		Vector v = new Vector();
		try {
			//
			// Check for EOF condition
			//
			MemberHeader aHeader;
			for (long pos = header.fstmoff; pos < file.length(); pos = aHeader.nxtmem) {
				file.seek(pos);
				aHeader = new MemberHeader();
				String name = aHeader.getObjectName();
				v.add(aHeader);
				if (pos == 0 || pos == header.lstmoff) {	// end of double linked list
					break;
				}
			}
		} catch (IOException e) {
		}
		memberHeaders = (MemberHeader[]) v.toArray(new MemberHeader[0]);
	}

	/**
	 *  Get an array of all the object file headers for this archive.
	 * 
	 * @throws IOException 
	 *    Unable to process the archive file.
	 * @return An array of headers, one for each object within the archive.
	 * @see ARHeader
	 */
	public MemberHeader[] getHeaders() throws IOException {
		loadHeaders();
		return memberHeaders;
	}

	public String[] extractFiles(String outdir, String[] names) throws IOException {
		Vector names_used = new Vector();
		String object_name;
		int count;

		loadHeaders();

		count = 0;
		for (int i = 0; i < memberHeaders.length; i++) {
			object_name = memberHeaders[i].getObjectName();
			if (names != null && !stringInStrings(object_name, names))
				continue;

			object_name = "" + count + "_" + object_name; //$NON-NLS-1$ //$NON-NLS-2$
			count++;

			byte[] data = memberHeaders[i].getObjectData();
			File output = new File(outdir, object_name);
			names_used.add(object_name);

			RandomAccessFile rfile = new RandomAccessFile(output, "rw"); //$NON-NLS-1$
			rfile.write(data);
			rfile.close();
		}

		return (String[]) names_used.toArray(new String[0]);
	}

	private boolean stringInStrings(String str, String[] set) {
		for (int i = 0; i < set.length; i++)
			if (str.compareTo(set[i]) == 0)
				return true;
		return false;
	}

	/**
	 * Remove the padding from the archive header strings.
	 */
	protected String removeBlanks(String str) {
		while (str.charAt(str.length() - 1) == ' ')
			str = str.substring(0, str.length() - 1);
		return str;
	}

	public String[] extractFiles(String outdir) throws IOException {
		return extractFiles(outdir, null);
	}

	protected RandomAccessFile getRandomAccessFile () throws IOException {
		if (file == null) {
			file = new RandomAccessFile(filename, "r"); //$NON-NLS-1$
		}
		return file;
	}

	public static void main(String[] args) {
		try {
			AR ar = new AR(args[0]);
			ar.getHeaders();
			ar.extractFiles(args[1]);
			System.out.println(ar);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

