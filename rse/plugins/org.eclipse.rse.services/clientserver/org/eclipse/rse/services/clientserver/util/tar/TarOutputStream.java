/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.clientserver.util.tar;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class implements an output stream filter for writing files in the
 * tar file format.
 */
public class TarOutputStream extends OutputStream implements ITarConstants {
	
	private OutputStream out;
	private boolean isClosed;
	private boolean entryOpen;
	private long dataCount;

	/**
	 * Creates a new tar output stream.
	 * @param out the actual output stream.
	 */
	public TarOutputStream(OutputStream out) {
		this.out = out;
		isClosed = false;
		entryOpen = false;
	}

	/**
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		
		// if not already closed, then close the stream
		// before closing though, write out a block of empty data
		if (!isClosed) {
			
			byte[] dummy = new byte[BLOCK_SIZE];
			out.write(dummy);
		
			out.close();
			
			isClosed = true;
		}
	}
	
	/**
	 * Ensure that the stream is open.
	 * @throws IOException if the stream is closed.
	 */
	private void ensureOpen() throws IOException {
		
		if (isClosed) {
			throw new IOException("Stream closed");
		}
	}

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		ensureOpen();
		out.write(b);
		dataCount += 1;
	}
	
	/**
	 * Begins writing a new tar entry, and positions the stream to the start of the entry data.
	 * Closes the current entry if still active.
	 * @throws IOException if an I/O occurs.
	 */
	public void putNextEntry(TarEntry entry) throws IOException {
		
		// previous entry open, so close it
		if (entryOpen) {
			closeEntry();
		}
		
		// defer to the entry to write the entry fields
		entry.writeFields(out);
		
		// get the part of a block we need to fill
		int diff = BLOCK_SIZE - HEADER_LENGTH;
		
		// fill the block if we have used a part of it
		if (diff != 0) {
			byte[] dummy = new byte[diff];
			out.write(dummy);
		}
		
		// set data count to 0
		dataCount = 0;
		
		// indicate that entry is open
		entryOpen = true;
	}
	
	/**
	 * Closes the current tar entry, and positions the stream for writing the next entry.
	 * @throws IOException if an I/O error occurs.
	 */
	public void closeEntry() throws IOException {
		
		// get the part of a block
		int temp = (int)(dataCount % BLOCK_SIZE);
		
		// fill the rest of the block with dummy data if we have filled part of a block
		if (temp != 0) {
			int diff = BLOCK_SIZE - temp;
			byte[] dummy = new byte[diff];
			out.write(dummy);
		}
		
		// indicate that entry has been closed
		entryOpen = false;
	}
}