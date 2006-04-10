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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This class is used to read entries from a tar file.
 */
public class TarFile implements ITarConstants {
	
	private File file;
	private Vector blockHeaders;
	
	private class TarEntryInputStream extends InputStream {
		
		private long size;
		private InputStream stream;
		private long numRead;
		
		/**
		 * Creates a tar entry input stream.
		 * @param size the size of the data in the tar entry.
		 * @param stream the underlying input stream.
		 */
		public TarEntryInputStream(long size, InputStream stream) {
			this.size = size;
			this.stream = stream;
			numRead = 0;
		}
	
		/**
		 * @see java.io.InputStream#read()
		 */
		public int read() throws IOException {
			
			if (numRead >= size) {
				return -1;
			}
			else {
				numRead += 1;
				return stream.read();
			}
		}
		
		/**
		 * @see java.io.InputStream#available()
		 */
		public int available() throws IOException {
			
			// get difference between file size and how much we have already read
			long diff = size - numRead;
			
			// get how much we can read from underlying stream.
			int av = stream.available();
			
			// return the smaller of the two
			// note although diff is a long, if it's smaller than av, we know it must fit
			// in an integer.
			return (int)Math.min(diff, av);
		}


		/**
		 * @see java.io.InputStream#close()
		 */
		public void close() throws IOException {
			stream.close();
		}

		/**
		 * @see java.io.InputStream#mark(int)
		 */
		public synchronized void mark(int readLimit) {
			stream.mark(readLimit);
		}

		/**
		 * @see java.io.InputStream#markSupported()
		 */
		public boolean markSupported() {
			return stream.markSupported();
		}

		/**
		 * @see java.io.InputStream#reset()
		 */
		public synchronized void reset() throws IOException {
			stream.reset();
		}
	}

	/**
	 * Opens a tar file for reading given the specified File object.
	 * @param file the tar file to be opened for reading.
	 * @throws FileNotFoundException if the file does not exist.
	 * @throws IOException if an I/O error occurs.
	 */
	public TarFile(File file) throws FileNotFoundException, IOException {
		this.file = file;
		loadTarEntries();
	}
	
	/**
	 * Opens a tar file for reading given the file name.
	 * @param name the name of the tar file to be opened for reading.
	 * @throws FileNotFoundException if the file with the given name does not exist.
	 * @throws IOException if an I/O error occurs.
	 */
	public TarFile(String name) throws FileNotFoundException, IOException {
		this(new File(name));
	}
	
	/**
	 * Loads tar entries.
	 * @throws FileNotFoundException if the file does not exist.
	 * @throws IOException if an I/O error occurs.
	 */
	private void loadTarEntries() throws FileNotFoundException, IOException {
		InputStream stream = getInputStream();
		blockHeaders = new Vector();
		
		// now read all the block headers
		byte[] blockData = readBlock(stream);

		// while end of stream is not reached, extract block headers
		while (blockData.length != 0) {
			
			// extract the header from the block
			TarEntry header = extractBlockHeader(blockData);

			// if header is not null, we add it to our list of headers
			if (header != null) {

				// add header 
				blockHeaders.add(header);

				// determine how many blocks make up the contents of the file
				long fileSize = header.getSize();
				int numFileBlocks = (int)(fileSize / BLOCK_SIZE);
				numFileBlocks += (int)((fileSize % BLOCK_SIZE) > 0 ? 1 : 0);

				// if the file is a symbolic link, number of blocks will be 0
				if (header.getTypeFlag() == ITarConstants.TF_SYMLINK) {
					numFileBlocks = 0;
				}

				// skip the blocks that contain file content
				stream.skip(numFileBlocks * BLOCK_SIZE);
			}

			// now read the next block
			blockData = readBlock(stream);
		}

		stream.close();
	}
	
	/**
	 * Gets the input stream for the tar file.
	 * @return the input stream for the tar file.
	 * @throws FileNotFoundException if the file does not exist.
	 */
	private InputStream getInputStream() throws FileNotFoundException {
		FileInputStream stream = new FileInputStream(file);
		return stream;
	}
	
	/**
	 * Reads the next block.
	 * @param stream the input stream of the tar file.
	 * @return the data in the next block, or an empty array if end of stream has been reached.
	 * @throws IOException if an I/O error occurs.
	 */
	private byte[] readBlock(InputStream stream) throws IOException {
		byte[] blockData = new byte[BLOCK_SIZE];

		// read a block of data
		int byteRead = 0;

		for (int i = 0; i < BLOCK_SIZE; i++) {
			byteRead = stream.read();
			
			if (byteRead != -1) {
				blockData[i] = (byte)byteRead;
			}
			else {
				break;
			}
		}

		// if end of stream has been reached, return an empty array
		if (byteRead == -1) {
			return new byte[0];
		}

		return blockData;
	}
	
	/**
	 * Extracts the header of a block given the block data.
	 * @param blockData the block data.
	 * @return the header of the block, or <code>null</code> if the block indicates end of file.
	 */
	private TarEntry extractBlockHeader(byte[] blockData) throws IOException {
		
		TarEntry entry = new TarEntry(blockData);
		
		// if the name of the entry is an empty string, it means we have reached end of file
		// so just return null
		if (entry.getName().equals("")) {
			return null;
		}
		else {
			return entry;
		}
	}
	
	/**
	 * Returns an enumeration of the tar file entries.
	 * @return an enumeration of the tar file entries.
	 */
	public Enumeration entries() {
		return blockHeaders.elements();
	}
	
	/**
	 * Returns the number of entries in the tar file.
	 * @return the number of entries in the tar file.
	 */
	public int size() {
		return blockHeaders.size();
	}
	
	/**
	 * Returns the tar file entry with that name, or <code>null</code> if not found.
	 * @param name the name of the entry.
	 * @return the tar file entry, or <code>null</code> if not found.
	 */
	public TarEntry getEntry(String name) {
		
		// TODO: could we maybe keep a hash instead to make it faster?
		// The hash could be keyed by names. But tars do allow headers with the same name.
		// Research this.
		Enumeration headers = entries();
		
		// go through all block headers 
		while (headers.hasMoreElements()) {
			TarEntry entry = (TarEntry)(headers.nextElement());
			String entryName = entry.getName();
			
			// if name of entry matches the given name, then that is the entry we are looking for
			if (entryName.equals(name) || entryName.equals(name + "/")) {
				return entry;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the input stream of the data in the given entry.
	 * @param entry the entry.
	 * @return the input stream containing the data in that entry.
	 * @throws IOException if an I/O error occurs.
	 */
	public InputStream getInputStream(TarEntry entry) throws IOException {
		InputStream stream = getInputStream();
		
		// now read all the block headers
		byte[] blockData = readBlock(stream);

		// while end of stream is not reached, extract block headers
		while (blockData.length != 0) {
			
			// extract the header from the block
			TarEntry header = extractBlockHeader(blockData);

			// if header is not null, we add it to our list of headers
			if (header != null) {
				
				long fileSize = header.getSize();

				// if the header name does not match the entry name
				if (!header.getName().equals(entry.getName())) {
					
					// determine how many blocks make up the contents of the file
					int numFileBlocks = (int)(fileSize / BLOCK_SIZE);
					numFileBlocks += (int)((fileSize % BLOCK_SIZE) > 0 ? 1 : 0);

					// if the file is a symbolic link, number of blocks will be 0
					if (header.getTypeFlag() == ITarConstants.TF_SYMLINK) {
						numFileBlocks = 0;
					}

					// skip the blocks that contain file content
					stream.skip(numFileBlocks * BLOCK_SIZE);
				}
				// the header name matches the entry name, so return the input stream with
				// the data for that entry
				else {
					return new TarEntryInputStream(fileSize, stream);
				}
			}

			// now read the next block
			blockData = readBlock(stream);
		}
		
		return null;
	}
}