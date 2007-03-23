/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
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
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;

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
	private boolean fWritable= false;
	private Chunk[] chunks;
	
	private long malloced;
	private long freed;
	private long cacheHits;
	private long cacheMisses;
	private ChunkCache fCache;
	
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
		
	public Database(File location, ChunkCache cache, int version) throws CoreException {
		try {
			this.location = location;
			this.file = new RandomAccessFile(location, "rw"); //$NON-NLS-1$
			fCache= cache;
			
			// Allocate chunk table, make sure we have at least one
			long nChunks = file.length() / CHUNK_SIZE;
			chunks = new Chunk[(int)nChunks];
			if (nChunks == 0) {
				setWritable();
				createNewChunk();
				setVersion(version);
				setReadOnly();
			}
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}
	
	public FileChannel getChannel() {
		return file.getChannel();
	}
	
	public int getVersion() throws CoreException {
		return getChunk(0).getInt(0);
	}
	
	public void setVersion(int version) throws CoreException {
		getChunk(0).putInt(0, version);
	}

	/**
	 * Empty the contents of the Database, make it ready to start again
	 * @throws CoreException
	 */
	public void clear(long timeout) throws CoreException {
		int version= getVersion();
		removeChunksFromCache();
		
		// clear out memory headers
		Chunk header= getChunk(0);
		setVersion(version);
		header.clear(4, DATA_AREA - 4);
		chunks = new Chunk[] {header};

		try {
			getChannel().truncate(CHUNK_SIZE);
		}
		catch (IOException e) {
			CCorePlugin.log(e);
		}
		malloced = freed = 0;
	}

	private void removeChunksFromCache() {
		synchronized (fCache) {
			for (int i = 0; i < chunks.length; i++) {
				Chunk chunk= chunks[i];
				if (chunk != null) {
					fCache.remove(chunk);
					chunks[i]= null;
				}
			}
		}
	}
	
	
	/**
	 * Return the Chunk that contains the given offset.
	 * @throws CoreException 
	 */
	public Chunk getChunk(int offset) throws CoreException {
		int index = offset / CHUNK_SIZE;
		
		// for performance reasons try to find chunk and mark it without
		// synchronizing. This means that we might pick up a chunk that
		// has been paged out, which is ok.
		// Furthermore the hitflag may not be seen by the clock-alorithm,
		// which might lead to the eviction of a chunk. With the next
		// cache failure we are in sync again, though.
		Chunk chunk = chunks[index];		
		if (chunk != null && chunk.fWritable == fWritable) {
			chunk.fCacheHitFlag= true;
			cacheHits++;
			return chunk;
		}
		
		// here is the safe code that has to be performed if we cannot
		// get ahold of the chunk.
		synchronized(fCache) {
			chunk= chunks[index];
			if (chunk == null) {
				cacheMisses++;
				chunk = chunks[index] = new Chunk(this, index);
			}
			else {
				cacheHits++;
			}
			fCache.add(chunk, fWritable);
			return chunk;
		}
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
			// allocate a new chunk
			freeblock= createNewChunk();
			blocksize = CHUNK_SIZE;
			chunk = getChunk(freeblock);
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
	
	private int createNewChunk() throws CoreException {
		try {
			Chunk[] oldtoc = chunks;
			int n = oldtoc.length;
			int offset = n * CHUNK_SIZE;
			file.seek(offset);
			file.write(new byte[CHUNK_SIZE]);
			chunks = new Chunk[n + 1];
			System.arraycopy(oldtoc, 0, chunks, 0, n);
			return offset;
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
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
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, 0, "Already Freed", new Exception())); //$NON-NLS-1$
		addBlock(chunk, blocksize, block);
		freed += blocksize;
	}

	public void putByte(int offset, byte value) throws CoreException {
		getChunk(offset).putByte(offset, value);
	}
	
	public byte getByte(int offset) throws CoreException {
		return getChunk(offset).getByte(offset);
	}
	
	public void putInt(int offset, int value) throws CoreException {
		getChunk(offset).putInt(offset, value);
	}
	
	public int getInt(int offset) throws CoreException {
		return getChunk(offset).getInt(offset);
	}

	public void putShort(int offset, short value) throws CoreException {
		getChunk(offset).putShort(offset, value);
	}
	
	public short getShort(int offset) throws CoreException {
		return getChunk(offset).getShort(offset);
	}

	public void putLong(int offset, long value) throws CoreException {
		getChunk(offset).putLong(offset, value);
	}
	
	public long getLong(int offset) throws CoreException {
		return getChunk(offset).getLong(offset);
	}

	public void putChar(int offset, char value) throws CoreException {
		getChunk(offset).putChar(offset, value);
	}

	public char getChar(int offset) throws CoreException {
		return getChunk(offset).getChar(offset);
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
	
	public int getChunkCount() {
		return chunks.length;
	}

	public void reportFreeBlocks() throws CoreException {
		System.out.println("Allocated size: " + chunks.length * CHUNK_SIZE); //$NON-NLS-1$
		System.out.println("malloc'ed: " + malloced); //$NON-NLS-1$
		System.out.println("free'd: " + freed); //$NON-NLS-1$
		System.out.println("wasted: " + (chunks.length * CHUNK_SIZE - (malloced - freed))); //$NON-NLS-1$
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
	 * Closes the database. 
	 * <p>
	 * The behaviour of any further calls to the Database is undefined
	 * @throws IOException
	 * @throws CoreException 
	 */
	public void close() throws CoreException {
		setReadOnly();
		removeChunksFromCache();
		chunks= new Chunk[0];
		try {
			file.close();
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}
	
	/**
     * This method is public for testing purposes only.
     */
	public File getLocation() {
		return location;
	}

	/**
	 * Called from any thread via the cache, protected by {@link #fCache}.
	 */
	void releaseChunk(Chunk chunk) {
		if (!chunk.fWritable)
			chunks[chunk.fSequenceNumber]= null;
	}

	/**
	 * Returns the cache used for this database.
	 * @since 4.0
	 */
	public ChunkCache getChunkCache() {
		return fCache;
	}

	public void setWritable() {
		fWritable= true;
	}

	public void setReadOnly() throws CoreException {
		if (fWritable) {
			fWritable= false;
			flushDirtyChunks();
		}
	}
	
	public void flushDirtyChunks() throws CoreException {
		ArrayList dirtyChunks= new ArrayList();
		synchronized (fCache) {
			for (int i = 0; i < chunks.length; i++) {
				Chunk chunk= chunks[i];
				if (chunk != null && chunk.fWritable) {
					chunk.fWritable= false;
					if (chunk.fCacheIndex < 0) {
						chunks[i]= null;
					}
					if (chunk.fDirty) {
						dirtyChunks.add(chunk);
					}
				}
			}
		}
		
		if (!dirtyChunks.isEmpty()) {
			for (Iterator it = dirtyChunks.iterator(); it.hasNext();) {
				Chunk chunk = (Chunk) it.next();
				chunk.flush();
			}
		}
	}
	
	public void resetCacheCounters() {
		cacheHits= cacheMisses= 0;
	}
	
	public long getCacheHits() {
		return cacheHits;
	}
	
	public long getCacheMisses() {
		return cacheMisses;
	}
}
