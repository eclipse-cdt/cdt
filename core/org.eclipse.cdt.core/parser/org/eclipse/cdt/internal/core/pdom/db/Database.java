/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
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

	// Access directly by the Chunks
	final RandomAccessFile file;
	private Chunk[] toc;
	
	private long malloced;
	private long freed;
	
	int version = 0;
	
	// public for tests only, you shouldn't need these
	public static final int VERSION_OFFSET = 0;
	public static final int CHUNK_SIZE = 1024 * 16;
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
			
			long nChunks = file.length() / CHUNK_SIZE;
			if (nChunks == 0) {
				// New file, allocate the header chunk
				file.seek(0);
				file.write(new byte[CHUNK_SIZE]);
				
				// Write out the version
				file.seek(0);
				file.writeInt(version);
				nChunks = 1;
			} else {
				// Read in the version
				file.seek(0);
				version = file.readInt();
			}
			
			toc = new Chunk[(int)nChunks];
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}
	
	public int getVersion() {
		return version;
	}
	
	/**
	 * Empty the contents of the Database, make it ready to start again
	 * @throws CoreException
	 */
	public synchronized void clear(int version) throws CoreException {
		// Clear out the data area and reset the version
		Chunk chunk = getChunk(0);
		chunk.putInt(0, version);
		chunk.clear(4, DATA_AREA - 4);

		// Add the remainder of the chunks backwards
		for (int block = (toc.length - 1) * CHUNK_SIZE; block > 0; block -= CHUNK_SIZE) {
			addBlock(getChunk(block), CHUNK_SIZE, block); 
		}
		malloced = freed = 0;
	}
	
	/**
	 * Return the Chunk that contains the given offset.
	 * 
	 * @param offset
	 * @return
	 */
	public Chunk getChunk(int offset) throws CoreException {
//		if (lockCount == 0)
			// Accessing database without locking
//			CCorePlugin.log(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, 0, "Index not locked", new Exception())); //$NON-NLS-1$
		
		int index = offset / CHUNK_SIZE;
		Chunk chunk;
		boolean isNew;
		synchronized (this) {
			chunk = toc[index];
			isNew = false;
			if (chunk == null) {
				chunk = toc[index] = new Chunk(this, index);
				isNew = true;
			}
		}
		Database.lruPutFirst(chunk, isNew);
		return chunk;
	}

	// Called by the chunk to set itself free
	synchronized void freeChunk(int index) {
		toc[index] = null;
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
			synchronized (this) {
				Chunk[] oldtoc = toc;
				int n = oldtoc.length;
				freeblock = n * CHUNK_SIZE;
				blocksize = CHUNK_SIZE;
				try {
					file.seek(freeblock);
					file.write(new byte[CHUNK_SIZE]);
				} catch (IOException e) {
					throw new CoreException(new DBStatus(e));
				}
				toc = new Chunk[n + 1];
				System.arraycopy(oldtoc, 0, toc, 0, n);
				toc[n] = chunk = new Chunk(this, n);
			}
			Database.lruPutFirst(chunk, true);
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

		// Clear out the block, lots of people are expecting this
		chunk.clear(freeblock + 4, size);

		malloced += matchsize;
		return freeblock + 4;
	}
	
	private int getFirstBlock(int blocksize) throws CoreException {
		return getChunk(0).getInt((blocksize / MIN_SIZE) * INT_SIZE);
	}
	
	private void setFirstBlock(int blocksize, int block) throws CoreException {
		getChunk(0).putInt((blocksize / MIN_SIZE) * INT_SIZE, block);
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
		addBlock(chunk, blocksize, block);
		freed += blocksize;
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
	
	// Chunk management
	static private Object lruMutex = new Object();
	static private Chunk lruFirst;
	static private Chunk lruLast;
	static private int lruSize;
	static private final int lruMax;
	static private final int MEG = 1024 * 1024; 
	
	static {
		String maxString = System.getProperty("cdt.index.lruMax"); //$NON-NLS-1$
		if (maxString != null) {
			int max = Integer.valueOf(maxString).intValue();
			if (max > 0)
				lruMax = max * MEG / CHUNK_SIZE;
			else
				lruMax = 64 * MEG / CHUNK_SIZE;
		} else {
			long max = Runtime.getRuntime().maxMemory();
			if (max < 512 * MEG)
				lruMax = (int)max / 8 / CHUNK_SIZE;
			else
				lruMax = 64 * MEG / CHUNK_SIZE;
		}
	}
	
	static private final void lruPutFirst(Chunk chunk, boolean isNew) throws CoreException {
		synchronized (lruMutex) {
			// If this chunk is already first, we're good
			if (chunk == lruFirst)
				return;
			
			if (!isNew) {
				// Remove from current position in cache
				if (chunk.lruPrev != null) {
					chunk.lruPrev.lruNext = chunk.lruNext;
				}
				if (chunk.lruNext != null) {
					chunk.lruNext.lruPrev = chunk.lruPrev;
				} else {
					// No next => New last
					lruLast = chunk.lruPrev;
				}
			}
	
			// Insert at front of cache
			chunk.lruNext = lruFirst;
			chunk.lruPrev = null;
			if (lruFirst != null)
				lruFirst.lruPrev = chunk;
			lruFirst = chunk;
			if (lruLast == null)
				lruLast = chunk;
	
			if (isNew) {
				// New chunk, see if we need to release one
				if (lruSize == lruMax) {
					Chunk last = lruLast;
					lruLast = last.lruPrev;
					lruLast.lruNext = null;
					last.free();
				} else {
					++lruSize;
				}
			}
		}
	}
	
	private Object lockMutex = new Object();
	private Thread lockOwner;
	private int lockCount;
	
	public void acquireLock() throws InterruptedException {
		synchronized (lockMutex) {
			if (lockOwner != Thread.currentThread())
				while (lockCount > 0)
					lockMutex.wait();
			++lockCount;
			lockOwner = Thread.currentThread();
		}
	}
	
	public void releaseLock() {
		synchronized (lockMutex) {
			--lockCount;
			if (lockCount == 0) {
				lockOwner = null;
				lockMutex.notify();
			}
		}
	}
	
	public static void saveAll() {
		// save the database
		try {
			for (Chunk chunk = lruFirst; chunk != null; chunk = chunk.lruNext) {
				chunk.save();
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

}
