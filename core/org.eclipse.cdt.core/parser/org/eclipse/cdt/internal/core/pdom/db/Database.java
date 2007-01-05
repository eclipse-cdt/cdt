/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Symbian - Add some non-javadoc implementation notes
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Database encapsulates access to a flat binary format file with a memory-manager-like API for
 * obtaining and releasing areas of storage (memory).
 *
 * @author Doug Schaefer
 */
/* 
 * The file encapsulated is divided into Chunks of size CHUNK_SIZE, and a table of contents
 * mapping chunk index to chunk address is maintained. Chunk structure exists only conceptually -
 * its not a structure that appears in the file.
 * 
 * Chunks are paged in one at a time as they are accessed.  Frequently used
 * chunks remain in memory while infrequently used chunks are paged out to
 * to the file.  The database uses the CLOCK algorithm to determine which
 * chunk to evict from the page table on a cache miss.
 * 
 * ===== The first chunk is used by Database itself for house-keeping purposes and has structure
 * 
 * offset            content
 * 	                 _____________________________
 * 0                | version number
 * INT_SIZE         | pointer to head of linked list of blocks of size MIN_SIZE
 * ..               | ...
 * INT_SIZE * m (1) | pointer to head of linked list of blocks of size MIN_SIZE * m
 * DATA_AREA        | undefined (PDOM stores its own house-keeping data in this area) 
 * 
 * (1) where m <= (CHUNK_SIZE / MIN_SIZE)
 * 
 * ===== block structure
 * 
 * offset            content
 * 	                 _____________________________
 * 0                | size of block (negative indicates in use, positive unused)
 * PREV_OFFSET      | pointer to prev block (of same size)
 * NEXT_OFFSET      | pointer to next block (of same size)
 * 
 */
public class Database {

	private final File location;
	private final RandomAccessFile file;
	
	/**
	 * Table of Chunks allocated by the Database.
	 */
	Chunk[] toc;
	
	/**
	 * Table of actively used (paged-in) Chunks.
	 */
	int[] pageTable;
	
	/**
	 * The position where the next page in or page out will occur.
	 */
	int pageTableIndex;
	
	/**
	 * Indicates whether all slots in the page table have been allocated. 
	 */
	boolean pageTableIsFull;
	
	/**
	 * Number of times a request for a Chunk was served from the cache.
	 */
	long cacheHits;
	
	/**
	 * Number of times a request for a Chunk needed to be paged in.
	 */
	long cacheMisses;
	
	private long malloced;
	private long freed;
	
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
	
	/**
	 * The initial number of slots in the page table.
	 */
	public static final int DEFAULT_PAGE_TABLE_SIZE = 1 * 1024 * 1024 / CHUNK_SIZE;
	
	public Database(File location) throws CoreException {
		this(location, DEFAULT_PAGE_TABLE_SIZE);
	}
	
	public Database(File location, int pageTableSize) throws CoreException {
		try {
			this.location = location;
			this.file = new RandomAccessFile(location, "rw"); //$NON-NLS-1$
			
			// Allocate chunk table, make sure we have at least one
			long nChunks = file.length() / CHUNK_SIZE;
			if (nChunks == 0) {
				file.seek(0);
				file.write(new byte[CHUNK_SIZE]); // the header chunk
				++nChunks;
			}
			
			toc = new Chunk[(int)nChunks];
			toc[0] = new Chunk(file, 0);
			pageTable = new int[pageTableSize];
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
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
	public void clear(long timeout) throws CoreException {
		// Clear out the memory headers
		toc[0].clear(4, DATA_AREA - 4);
		
		if (!truncate(timeout)) {
			// Truncation timed out so the database size couldn't be changed.
			// The best we can do is mark all chunks as unallocated blocks.

			// Since the block list grows at the head, add all non-header
			// chunks backwards to ensure list of blocks is ordered first
			// to last.
			for (int block = (toc.length - 1) * CHUNK_SIZE; block > 0; block -= CHUNK_SIZE) {
				addBlock(getChunk(block), CHUNK_SIZE, block); 
			}
		}
		malloced = freed = 0;
		
		// Clear the page table
		resizePageTable(pageTable.length);
	}
	
	/**
	 * Truncate the database as small as possible to reclaim disk space.
	 * This method returns false if truncation does not succeed within the
	 * given timeout period (in milliseconds).  A timeout of 0 will cause
	 * this method to block until the database is successfully truncated.
	 *  
	 * @param timeout maximum amount of milliseconds to wait before giving up;
	 *                0 means wait indefinitely.
	 * @return true if truncation succeeds; false if the operation times out.
	 * @throws CoreException if an IO error occurs during truncation
	 */
	private boolean truncate(long timeout) throws CoreException {
		// Queue all the chunks to be reclaimed.  
		ReferenceQueue queue = new ReferenceQueue();
		Set references = new HashSet();
		int totalChunks = toc.length;
		for (int i = 0; i < toc.length; i++) {
			if (toc[i] != null) {
				toc[i].reclaim(queue, references);
				toc[i] = null;
			}
		}
		
		System.gc();
		try {
			// Wait for each chunk to be reclaimed.
			int totalReclaimed = references.size();
			while (totalReclaimed > 0) {
				queue.remove(timeout);
				totalReclaimed--;
			}
			
			// Truncate everything but the header chunk.
			try {
				file.getChannel().truncate(CHUNK_SIZE);
				// Reinitialize header chunk.
				toc = new Chunk[] { new Chunk(file, 0) }; 
				return true;
			} catch (IOException e) {
				// Bug 168420:
				// Truncation failed so we'll reuse the existing
				// file.
				toc = new Chunk[totalChunks];
				toc[0] = new Chunk(file, 0); 
				return false;
			}
		}
		catch (InterruptedException e) {
			// Truncation took longer than we wanted, so we'll
			// reinitialize the header chunk and leave the file
			// size alone.
			toc[0] = new Chunk(file, 0);
			return false;
		}
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
			cacheMisses++;
			chunk = pageIn(offset);
		}
		else {
			cacheHits++;
			chunk.referenceFlag = true;
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

		// Clear out the block, lots of people are expecting this
		chunk.clear(freeblock + 4, size);

		malloced += matchsize;
		return freeblock + 4;
	}
	
	/**
	 * Extends the database by one chunk.
	 * @return The index of the new chunk.
	 * @throws CoreException
	 */
	private int createChunk() throws CoreException {
		try {
			Chunk[] oldtoc = toc;
			int n = oldtoc.length;
			int offset = n * CHUNK_SIZE;
			file.seek(offset);
			file.write(new byte[CHUNK_SIZE]);
			toc = new Chunk[n + 1];
			System.arraycopy(oldtoc, 0, toc, 0, n);
			getChunk(offset);
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
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, 0, "Already Freed", new Exception())); //$NON-NLS-1$
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

	public void putShort(int offset, short value) throws CoreException {
		Chunk chunk = getChunk(offset);
		chunk.putShort(offset, value);
	}
	
	public short getShort(int offset) throws CoreException {
		Chunk chunk = getChunk(offset);
		return chunk.getShort(offset);
	}

	public void putLong(int offset, long value) throws CoreException {
		Chunk chunk= getChunk(offset);
		chunk.putLong(offset, value);
	}
	
	public long getLong(int offset) throws CoreException {
		Chunk chunk = getChunk(offset);
		return chunk.getLong(offset);
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

	public void reportFreeBlocks() throws CoreException {
		System.out.println("Allocated size: " + toc.length * CHUNK_SIZE); //$NON-NLS-1$
		System.out.println("malloc'ed: " + malloced); //$NON-NLS-1$
		System.out.println("free'd: " + freed); //$NON-NLS-1$
		System.out.println("wasted: " + (toc.length * CHUNK_SIZE - (malloced - freed))); //$NON-NLS-1$
		System.out.println("Free blocks"); //$NON-NLS-1$
		for (int bs = MIN_SIZE; bs <= CHUNK_SIZE; bs += MIN_SIZE) {
			int count = 0;
			int block = getFirstBlock(bs);
			while (block != 0) {
				++count;
				block = getInt(block + NEXT_OFFSET);
			}
			if (count != 0)
				System.out.println("Block size: " + bs + "=" + count); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Closes the database, releasing the file lock. This is public for testing purposes only. 
	 * <p>
	 * The behaviour of any further calls to the Database is undefined
	 * @throws IOException
	 */
	public void close() throws IOException {
		file.close();
	}
	
	/**
     * This method is public for testing purposes only.
     */
	public File getLocation() {
		return location;
	}

	/**
	 * Clears the page table and changes it to have <code>size</code> number
	 * of slots. 
	 * @param size The number of slots the page table should have.
	 */
	public void resizePageTable(int size) {
		// Page out everything in the chunk/page tables.
		pageTable = new int[size];
		for (int i = 1; i < toc.length; i++) {
			toc[i] = null;
		}
		
		pageTableIndex = 0;
		pageTableIsFull = false;
		cacheHits = 0;
		cacheMisses = 0;
	}
	
	/**
	 * Returns the number of cache hits since the page table was created.
	 * @return The number of cache hits since the page table was created.
	 */
	public long getCacheHits() {
		return cacheHits;
	}
	
	/**
	 * Returns the number of cache misses since the page table was created.
	 * @return The number of cache misses since the page table was created.
	 */
	public long getCacheMisses() {
		return cacheMisses;
	}
	
	/**
	 * Adds the chunk which contains the given given offset to the page table.
	 * This method will evict chunks from the page table as necessary to page
	 * in the requested chunk.
	 * 
	 * @param offset The database offset that will be accessed.
	 * @return The chunk which contains the given offset.
	 * @throws CoreException
	 */
	private Chunk pageIn(int offset) throws CoreException {
		int index = offset / CHUNK_SIZE;
		if (toc[index] != null) {
			return toc[index];
		}
		
		Chunk chunk = new Chunk(file, index * CHUNK_SIZE);
		toc[index] = chunk;
		
		if (pageTableIsFull) {
			evictChunk();
			chunk.pageTableIndex = pageTableIndex;
			pageTable[pageTableIndex] = index;
		}
		else {
			chunk.pageTableIndex = pageTableIndex;
			pageTable[pageTableIndex] = index;

			pageTableIndex++;
			if (pageTableIndex == pageTable.length) {
				pageTableIndex = 0;
				pageTableIsFull = true;
			}
		}
		return chunk;
	}
	
	/**
	 * Evicts a chunk from the page table and the chunk table.
	 * After this method returns, <code>pageTableIndex</code> will contain
	 * the index of the evicted chunk within the page table.
	 */
	private void evictChunk() {
		/*
		 * Use the CLOCK algorithm to determine which chunk to evict.
		 * i.e., if the chunk in the current slot of the page table has been
		 * recently referenced (i.e. the reference flag is set), unset the
		 * reference flag and move to the next slot.  Otherwise, evict the
		 * chunk in the current slot.
		 */
		while (true) {
			int chunkIndex = pageTable[pageTableIndex];
			Chunk chunk = toc[chunkIndex];
			if (chunk.referenceFlag) {
				chunk.referenceFlag = false;
				pageTableIndex = (pageTableIndex + 1) % pageTable.length;
			} else {
				toc[chunkIndex] = null;
				pageTable[pageTableIndex] = 0;
				return;
			}
		}
	}
}
