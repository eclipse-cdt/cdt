/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class Database {

	private final RandomAccessFile file;
	Chunk[] toc;
	
	// public for tests only, you shouldn't need these
	public static final int VERSION_OFFSET = 0;
	public static final int CHUNK_SIZE = 4096;
	public static final int MIN_SIZE = 16;
	public static final int INT_SIZE = 4;
	public static final int CHAR_SIZE = 2;
	public static final int PREV_OFFSET = INT_SIZE;
	public static final int NEXT_OFFSET = INT_SIZE * 2;
	public static final int DATA_AREA = CHUNK_SIZE / MIN_SIZE * INT_SIZE + INT_SIZE;
	
	public static final int MAX_SIZE = CHUNK_SIZE - 4; // Room for overhead
		
	public Database(String filename) throws CoreException {
		try {
			file = new RandomAccessFile(filename, "rw"); //$NON-NLS-1$
			
			// Allocate chunk table, make sure we have at least one
			long nChunks = file.length() / CHUNK_SIZE;
			if (nChunks == 0) {
				create();
				++nChunks;
			}
			
			toc = new Chunk[(int)nChunks];
			init();
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}
	
	private void create() throws CoreException {
		try {
			file.seek(0);
			file.write(new byte[CHUNK_SIZE]); // the header chunk
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}

	private void init() throws CoreException {
		// Load in the magic chunk zero
		toc[0] = new Chunk(file, 0);
	}
	
	public int getVersion() {
		return toc[0].getInt(0);
	}
	
	public void setVersion(int version) {
		toc[0].putInt(0, version);
	}

	/**
	 * Empty the contents of the Database, make it ready to start again
	 * @throws CoreException
	 */
	public void clear() throws CoreException {
		int version = toc[0].getInt(0);
		create();
		toc = new Chunk[1];
		init();
		setVersion(version);
	}
	
	/**
	 * Return the Chunk that contains the given offset.
	 * 
	 * @param offset
	 * @return
	 */
	public Chunk getChunk(int offset) throws CoreException {
		int index = offset / CHUNK_SIZE;
		Chunk chunk = toc[index];
		if (chunk == null) {
			chunk = toc[index] = new Chunk(file, index * CHUNK_SIZE);
		}
		
		return chunk;
	}

	/**
	 * Allocate a block out of the database.
	 * 
	 * @param size
	 * @return
	 */ 
	public int malloc(int size) throws CoreException {
		if (size > MAX_SIZE)
			// Too Big
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, 0,
					CCorePlugin.getResourceString("pdom.requestTooLarge"), new IllegalArgumentException())); //$NON-NLS-1$
		
		// Which block size
		int freeblock = 0;
		int blocksize;
		int matchsize = 0;
		for (blocksize = MIN_SIZE; blocksize <= CHUNK_SIZE; blocksize += MIN_SIZE) {
			if (blocksize - INT_SIZE >= size) {
				if (matchsize == 0) // our real size
					matchsize = blocksize;
				freeblock = getFirstBlock(blocksize);
				if (freeblock != 0)
					break;
			}
		}
		
		// get the block
		Chunk chunk;
		if (freeblock == 0) {
			// Out of memory, allocate a new chunk
			int i = createChunk();
			chunk = toc[i];
			freeblock = i * CHUNK_SIZE;
			blocksize = CHUNK_SIZE;
		} else {
			chunk = getChunk(freeblock);
			removeBlock(chunk, blocksize, freeblock);
		}
 
		if (blocksize != matchsize) {
			// Add in the unused part of our block
			addBlock(chunk, blocksize - matchsize, freeblock + matchsize);
		}
		
		// Make our size negative to show in use
		chunk.putInt(freeblock, - matchsize);
		
		return freeblock + INT_SIZE;
	}
	
	private int createChunk() throws CoreException {
		try {
			Chunk[] oldtoc = toc;
			int n = oldtoc.length;
			int offset = n * CHUNK_SIZE;
			file.seek(offset);
			file.write(new byte[CHUNK_SIZE]);
			toc = new Chunk[n + 1];
			System.arraycopy(oldtoc, 0, toc, 0, n);
			toc[n] = new Chunk(file, offset);
			return n;
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}
	
	private int getFirstBlock(int blocksize) {
		return toc[0].getInt((blocksize / MIN_SIZE) * INT_SIZE);
	}
	
	private void setFirstBlock(int blocksize, int block) {
		toc[0].putInt((blocksize / MIN_SIZE) * INT_SIZE, block);
	}
	
	private void removeBlock(Chunk chunk, int blocksize, int block) throws CoreException {
		int prevblock = chunk.getInt(block + PREV_OFFSET);
		int nextblock = chunk.getInt(block + NEXT_OFFSET);
		if (prevblock != 0)
			putInt(prevblock + NEXT_OFFSET, nextblock);
		else // we were the head
			setFirstBlock(blocksize, nextblock);
			
		if (nextblock != 0)
			putInt(nextblock + PREV_OFFSET, prevblock);
	}
	
	private void addBlock(Chunk chunk, int blocksize, int block) throws CoreException {
		// Mark our size
		chunk.putInt(block, blocksize);

		// Add us to the head of the list
		int prevfirst = getFirstBlock(blocksize);
		chunk.putInt(block + PREV_OFFSET, 0);
		chunk.putInt(block + NEXT_OFFSET, prevfirst);
		if (prevfirst != 0)
			putInt(prevfirst + PREV_OFFSET, block);
		setFirstBlock(blocksize, block);
	}
	
	/**
	 * Free an allocate block.
	 * 
	 * @param offset
	 */
	public void free(int offset) throws CoreException {
		// TODO - look for opportunities to merge blocks
		int block = offset - 4;
		Chunk chunk = getChunk(block);
		int blocksize = - chunk.getInt(block);
		if (blocksize < 0)
			// already freed
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, 0, "Already Freed", new Exception()));
		chunk.clear(offset, blocksize - 4);
		addBlock(chunk, blocksize, block);
	}

	public void putByte(int offset, byte value) throws CoreException {
		Chunk chunk = getChunk(offset);
		chunk.putByte(offset, value);
	}
	
	public byte getByte(int offset) throws CoreException {
		Chunk chunk = getChunk(offset);
		return chunk.getByte(offset);
	}
	
	public void putInt(int offset, int value) throws CoreException {
		Chunk chunk = getChunk(offset);
		chunk.putInt(offset, value);
	}
	
	public int getInt(int offset) throws CoreException {
		Chunk chunk = getChunk(offset);
		return chunk.getInt(offset);
	}

	public void putChar(int offset, char value) throws CoreException {
		Chunk chunk = getChunk(offset);
		chunk.putChar(offset, value);
	}

	public char getChar(int offset) throws CoreException {
		Chunk chunk = getChunk(offset);
		return chunk.getChar(offset);
	}
	
	public IString newString(String string) throws CoreException {
		if (string.length() > ShortString.MAX_LENGTH)
			return new LongString(this, string);
		else
			return new ShortString(this, string);
	}

	public IString newString(char[] chars) throws CoreException {
		if (chars.length > ShortString.MAX_LENGTH)
			return new LongString(this, chars);
		else
			return new ShortString(this, chars);
	}

	public IString getString(int offset) throws CoreException {
		int length = getInt(offset);
		if (length > ShortString.MAX_LENGTH)
			return new LongString(this, offset);
		else
			return new ShortString(this, offset);
	}
	
	public int getNumChunks() {
		return toc.length;
	}

}
