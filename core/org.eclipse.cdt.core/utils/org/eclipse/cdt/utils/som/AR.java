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
package org.eclipse.cdt.utils.som;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.utils.coff.ReadMemoryAccess;

/**
 *  The <code>AR</code> class is used for parsing standard SOM archive (ar) files.
 *
 * @author vhirsl
 */
public class AR {
	public static final String NL = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	protected String filename;
	protected RandomAccessFile file;
	private byte[] ar_magic = new byte[8];
	private LSTHeader lstHeader;
	private ARHeader[] memberHeaders;

	/**
	 * Archive and archive member header. Does not include 8-byte magic character.
	 * 
	 * @author vhirsl
	 */
	public class ARHeader {
		public static final int HEADER_SIZE = 60;
		
		// fields
		private byte[] ar_name = new byte[16];	// file member name - '/' terminated 
		private byte[] ar_date = new byte[12]; 	// file member date - decimal
		private byte[] ar_uid  = new byte[6]; 	// file member user id - decimal
		private byte[] ar_gid  = new byte[6]; 	// file member group id - decimal
		private byte[] ar_mode = new byte[8]; 	// file member mode - octal
		private byte[] ar_size = new byte[10]; 	// file member size - decimal
		private byte[] ar_fmag = new byte[2]; 	// ARFMAG - string to end header

		// derived information
		String name;
		public int somOffset;
		public int somSize;
		
		public ARHeader(long offset) throws IOException {
			try {
				getRandomAccessFile();
				file.seek(offset);
				
				file.read(ar_name);
				for (int i = 0; i < 16; ++ i) {
					if (ar_name[i] == '/') {
						name = new String(ar_name, 0, i);
					}
				}
				file.read(ar_date);
				file.read(ar_uid);
				file.read(ar_gid);
				file.read(ar_mode);
				file.read(ar_size);
				file.read(ar_fmag);
			} catch (IOException e) {
				dispose();
				CCorePlugin.log(e);
			}
		}

		/** Get the name of the object file */
		public String getObjectName() {
			return name;
		}

		/** Get the size of the object file . */
		public long getSize() {
			return somSize;
		}
		
		public byte[] getObjectData() throws IOException {
			byte[] temp = new byte[somSize];
			file = getRandomAccessFile();
			file.seek(somOffset);
			file.read(temp);
			dispose();
			return temp;
		}

		/**
		 *  Create a new SOM object for the object file.
		 *
		 * @throws IOException 
		 *    Not a valid SOM object file.
		 * @return A new SOM object.  
		 * @see SOM#SOM( String, long )
		 */
		public long getObjectDataOffset() {
			return somOffset;
		}
	}

	/**
	 * Library Symbol Table header
	 * 
	 * @author vhirsl
	 */
	public class LSTHeader {
		public static final int LST_HEADER_OFFSET = 68;
		public static final int LST_HEADER_SIZE = 19 * 4;
		
		// record fields
		public short system_id;
		public short a_magic;
		public int version_id;
		public int file_time_sec;
		public int file_time_nano;
		public int hash_loc;
		public int hash_size;
		public int module_count;
		public int module_limit;
		public int dir_loc;
		public int export_loc;
		public int export_count;
		public int import_loc;
		public int aux_loc;
		public int aux_size;
		public int string_loc;
		public int string_size;
		public int free_list;
		public int file_end;
		public int checksum;
		
		public LSTHeader() throws IOException {
			try {
				getRandomAccessFile();
				file.seek(LST_HEADER_OFFSET);
				byte[] lstRecord = new byte[LST_HEADER_SIZE];
				file.readFully(lstRecord);
				ReadMemoryAccess memory = new ReadMemoryAccess(lstRecord, false);
				
				system_id = memory.getShort();
				a_magic = memory.getShort();
				version_id = memory.getInt();
				file_time_sec = memory.getInt();
				file_time_nano = memory.getInt();
				hash_loc = memory.getInt();
				hash_size = memory.getInt();
				module_count = memory.getInt();
				module_limit = memory.getInt();
				dir_loc = memory.getInt();
				export_loc = memory.getInt();
				export_count = memory.getInt();
				import_loc = memory.getInt();
				aux_loc = memory.getInt();
				aux_size = memory.getInt();
				string_loc = memory.getInt();
				string_size = memory.getInt();
				free_list = memory.getInt();
				file_end = memory.getInt();
				checksum = memory.getInt();
			} catch (IOException e) {
				dispose();
				CCorePlugin.log(e);
			}
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
		file.read(ar_magic);
		if (!isARHeader(ar_magic)) {
			file.close();
			throw new IOException(CCorePlugin.getResourceString("Util.exception.invalidArchive")); //$NON-NLS-1$
		}
		// load a LST header
		lstHeader = new LSTHeader();
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
		if (ident.length < 8
			|| ident[0] != '!'
			|| ident[1] != '<'
			|| ident[2] != 'a'
			|| ident[3] != 'r'
			|| ident[4] != 'c'
			|| ident[5] != 'h'
			|| ident[6] != '>'
			|| ident[7] != '\n')
			return false;
		return true;
	}

	protected RandomAccessFile getRandomAccessFile () throws IOException {
		if (file == null) {
			file = new RandomAccessFile(filename, "r"); //$NON-NLS-1$
		}
		return file;
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
		return memberHeaders;
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
			// get the SOM directory
			long somDirOffset = lstHeader.dir_loc + LSTHeader.LST_HEADER_OFFSET;
			// each SOM Directory entry has 2 32bit words: SOM offset from LST and size
			int somDirSize = lstHeader.module_limit * 8;  
			getRandomAccessFile();
			file.seek(somDirOffset);
			byte[] somDirectory = new byte[somDirSize];
			file.readFully(somDirectory);
			ReadMemoryAccess memory = new ReadMemoryAccess(somDirectory, false);
			for (int i = 0; i < lstHeader.module_limit; ++i) {
				int somOffset = memory.getInt();
				int somSize = memory.getInt();
				ARHeader aHeader = new ARHeader(somOffset-ARHeader.HEADER_SIZE);
				aHeader.somOffset = somOffset;
				aHeader.somSize = somSize;
				v.add(aHeader);
			}
		} catch (IOException e) {
		}
		memberHeaders = (ARHeader[]) v.toArray(new ARHeader[v.size()]);
	}
	
	public String[] extractFiles(String outdir) throws IOException {
		return extractFiles(outdir, null);
	}

	private String[] extractFiles(String outdir, String[] names) throws IOException {
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

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (lstHeader != null) {
			buffer.append("LST HEADER VALUES").append(NL); //$NON-NLS-1$
			buffer.append("system_id    = ").append(lstHeader.system_id).append(NL); //$NON-NLS-1$
			buffer.append("a_magic      = ").append(lstHeader.a_magic).append(NL); //$NON-NLS-1$
			buffer.append("version_id   = ").append(lstHeader.version_id).append(NL); //$NON-NLS-1$
			buffer.append("module_count = ").append(lstHeader.module_count).append(NL); //$NON-NLS-1$
			buffer.append("module_limit = ").append(lstHeader.module_limit).append(NL); //$NON-NLS-1$
			buffer.append("dir_loc      = ").append(lstHeader.dir_loc).append(NL); //$NON-NLS-1$
			
			for (int i = 0; i < memberHeaders.length; ++i) {
				buffer.append("MEMBER HEADER VALUES").append(NL); //$NON-NLS-1$
				buffer.append("name      = ").append(memberHeaders[i].getObjectName()).append(NL); //$NON-NLS-1$
				buffer.append("somOffset = ").append(memberHeaders[i].somOffset).append(NL); //$NON-NLS-1$
				buffer.append("somSize   = ").append(memberHeaders[i].getSize()).append(NL); //$NON-NLS-1$
			}
		}
		return buffer.toString();
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
