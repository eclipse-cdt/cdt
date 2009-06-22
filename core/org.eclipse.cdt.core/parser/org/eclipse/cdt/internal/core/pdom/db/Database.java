/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Symbian - Add some non-javadoc implementation notes
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

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
 * INT_SIZE         | pointer to head of linked list of blocks of size MIN_BLOCK_DELTAS*BLOCK_SIZE_DELTA
 * ..               | ...
 * INT_SIZE * m (1) | pointer to head of linked list of blocks of size (m+MIN_BLOCK_DELTAS) * BLOCK_SIZE_DELTA 
 * DATA_AREA        | undefined (PDOM stores its own house-keeping data in this area) 
 * 
 * (1) where 2 <= m <= CHUNK_SIZE/BLOCK_SIZE_DELTA - MIN_BLOCK_DELTAS + 1
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
	// public for tests only, you shouldn't need these
	public static final int INT_SIZE = 4;
	public static final int CHUNK_SIZE = 1024 * 4;
	public static final int OFFSET_IN_CHUNK_MASK= CHUNK_SIZE-1;
	public static final int BLOCK_HEADER_SIZE= 2;
	public static final int BLOCK_SIZE_DELTA= 8;
	public static final int MIN_BLOCK_DELTAS = 2;	// a block must at least be 2 + 2*4 bytes to link the free blocks.
	public static final int MAX_BLOCK_DELTAS = CHUNK_SIZE/BLOCK_SIZE_DELTA;	
	public static final int MAX_MALLOC_SIZE = MAX_BLOCK_DELTAS*BLOCK_SIZE_DELTA - BLOCK_HEADER_SIZE;  

	public static final int VERSION_OFFSET = 0;
	public static final int DATA_AREA = (CHUNK_SIZE / BLOCK_SIZE_DELTA - MIN_BLOCK_DELTAS + 2) * INT_SIZE;
	
	private static final int BLOCK_PREV_OFFSET = BLOCK_HEADER_SIZE;
	private static final int BLOCK_NEXT_OFFSET = BLOCK_HEADER_SIZE + INT_SIZE;
	
	private final File fLocation;
	private final boolean fReadOnly;
	private RandomAccessFile fFile;
	private boolean fExclusiveLock= false;	// necessary for any write operation
	private boolean fLocked;				// necessary for any operation.
	private boolean fIsMarkedIncomplete= false;

	private int fVersion;
	private final Chunk fHeaderChunk;
	private Chunk[] fChunks;
	private ChunkCache fCache;
	
	private long malloced;
	private long freed;
	private long cacheHits;
	private long cacheMisses;
	
	/**
	 * Construct a new Database object, creating a backing file if necessary.
	 * @param location the local file path for the database 
	 * @param cache the cache to be used optimization
	 * @param version the version number to store in the database (only applicable for new databases)
	 * @param openReadOnly whether this Database object will ever need writing to
	 * @throws CoreException
	 */
	public Database(File location, ChunkCache cache, int version, boolean openReadOnly) throws CoreException {
		try {
			fLocation = location;
			fReadOnly= openReadOnly;
			fCache= cache;
			openFile();
			
			int nChunksOnDisk = (int) (fFile.length() / CHUNK_SIZE);
			fHeaderChunk= new Chunk(this, 0);
			fHeaderChunk.fLocked= true;		// never makes it into the cache, needed to satisfy assertions
			if (nChunksOnDisk <= 0) {
				fVersion= version;
				fChunks= new Chunk[1];
			}
			else {
				fHeaderChunk.read();
				fVersion= fHeaderChunk.getInt(VERSION_OFFSET);
				fChunks = new Chunk[nChunksOnDisk];	// chunk[0] is unused.
			}
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}
		
	private void openFile() throws FileNotFoundException {
		fFile = new RandomAccessFile(fLocation, fReadOnly ? "r" : "rw"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	void read(ByteBuffer buf, int i) throws IOException {
		int retries= 0;
		do {
			try {
				fFile.getChannel().read(buf, i);
				return;
			}
			catch (ClosedChannelException e) {
				// bug 219834 file may have be closed by interrupting a thread during an I/O operation.
				reopen(e, ++retries);
			} 
		} while (true);
	}

	void write(ByteBuffer buf, int i) throws IOException {
		int retries= 0;
		do {
			try {
				fFile.getChannel().write(buf, i);
				return;
			}
			catch (ClosedChannelException e) {
				// bug 219834 file may have be closed by interrupting a thread during an I/O operation.
				reopen(e, ++retries);
			} 
		} while(true);
	}

	private void reopen(ClosedChannelException e, int attempt) throws ClosedChannelException, FileNotFoundException {
		// only if the current thread was not interrupted we try to reopen the file.
		if (e instanceof ClosedByInterruptException || attempt >= 20) {
			throw e;
		}
		openFile();
	}


	public void transferTo(FileChannel target) throws IOException {
		assert fLocked;
        final FileChannel from= fFile.getChannel();
        long nRead = 0;
        long position = 0;
        long size = from.size();
        while (position < size) {
        	nRead = from.transferTo(position, 4096*16, target);
        	if (nRead == 0) {
        		break;		// should not happen
        	} else {
        		position+= nRead;
        	}
        }
	}
	
	public int getVersion() throws CoreException {
		return fVersion;
	}
	
	public void setVersion(int version) throws CoreException {
		assert fExclusiveLock;
		fHeaderChunk.putInt(VERSION_OFFSET, version);
		fVersion= version;
	}

	/**
	 * Empty the contents of the Database, make it ready to start again
	 * @throws CoreException
	 */
	public void clear(int version) throws CoreException {
		assert fExclusiveLock;
		removeChunksFromCache();
		
		fVersion= version;
		// clear the first chunk.
		fHeaderChunk.clear(0, CHUNK_SIZE);
		// chunks have been removed from the cache, so we may just reset the array of chunks.
		fChunks = new Chunk[] {null};
		try {
			fHeaderChunk.flush();	// zero out header chunk
			fFile.getChannel().truncate(CHUNK_SIZE);	// truncate database
		}
		catch (IOException e) {
			CCorePlugin.log(e);
		}
		malloced = freed = 0;
	}

	private void removeChunksFromCache() {
		synchronized (fCache) {
			for (int i=1; i < fChunks.length; i++) {
				Chunk chunk= fChunks[i];
				if (chunk != null) {
					fCache.remove(chunk);
					fChunks[i]= null;
				}
			}
		}
	}
	
	
	/**
	 * Return the Chunk that contains the given offset.
	 * @throws CoreException 
	 */
	public Chunk getChunk(int offset) throws CoreException {
		if (offset < CHUNK_SIZE) {
			return fHeaderChunk;
		}
		synchronized(fCache) {
			assert fLocked;
			final int index = offset / CHUNK_SIZE;
			Chunk chunk= fChunks[index];
			if (chunk == null) {
				cacheMisses++;
				chunk = fChunks[index] = new Chunk(this, index);
				chunk.read();
			}
			else {
				cacheHits++;
			}
			fCache.add(chunk, fExclusiveLock);
			return chunk;
		}
	}

	/**
	 * Allocate a block out of the database.
	 */ 
	public int malloc(final int datasize) throws CoreException {
		assert fExclusiveLock;
		if (datasize < 0 || datasize > MAX_MALLOC_SIZE)
			// Too Big
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, 0,
					CCorePlugin.getResourceString("pdom.requestTooLarge"), new IllegalArgumentException())); //$NON-NLS-1$
		
		int needDeltas= (datasize + BLOCK_HEADER_SIZE + BLOCK_SIZE_DELTA - 1) / BLOCK_SIZE_DELTA;
		if (needDeltas < MIN_BLOCK_DELTAS) {
			needDeltas= MIN_BLOCK_DELTAS;
		}

		// Which block size
		int freeblock = 0;
		int useDeltas;
		for (useDeltas= needDeltas; useDeltas <= MAX_BLOCK_DELTAS; useDeltas++) {
			freeblock = getFirstBlock(useDeltas*BLOCK_SIZE_DELTA);
			if (freeblock != 0)
				break;
		}
		
		// get the block
		Chunk chunk;
		if (freeblock == 0) {
			// allocate a new chunk
			freeblock= createNewChunk();
			useDeltas = MAX_BLOCK_DELTAS;
			chunk = getChunk(freeblock);
		} else {
			chunk = getChunk(freeblock);
			removeBlock(chunk, useDeltas*BLOCK_SIZE_DELTA, freeblock);
		}
 
		final int unusedDeltas = useDeltas-needDeltas;
		if (unusedDeltas >= MIN_BLOCK_DELTAS) {
			// Add in the unused part of our block
			addBlock(chunk, unusedDeltas*BLOCK_SIZE_DELTA, freeblock + needDeltas*BLOCK_SIZE_DELTA);
			useDeltas= needDeltas;
		}
		
		// Make our size negative to show in use
		final int usedSize= useDeltas*BLOCK_SIZE_DELTA;
		chunk.putShort(freeblock, (short) -usedSize);

		// Clear out the block, lots of people are expecting this
		chunk.clear(freeblock + BLOCK_HEADER_SIZE, usedSize-BLOCK_HEADER_SIZE);

		malloced+= usedSize;
		return freeblock + BLOCK_HEADER_SIZE;
	}
	
	private int createNewChunk() throws CoreException {
		assert fExclusiveLock;
		synchronized (fCache) {
			final int oldLen= fChunks.length;
			final Chunk chunk= new Chunk(this, oldLen);
			chunk.fDirty= true;

			Chunk[] newchunks = new Chunk[oldLen+1];
			System.arraycopy(fChunks, 0, newchunks, 0, oldLen);
			newchunks[oldLen]= chunk;
			fChunks= newchunks;
			fCache.add(chunk, true);
			return oldLen * CHUNK_SIZE;
		}
	}
	
	private int getFirstBlock(int blocksize) throws CoreException {
		assert fLocked;
		return fHeaderChunk.getInt((blocksize/BLOCK_SIZE_DELTA - MIN_BLOCK_DELTAS + 1) * INT_SIZE);
	}
	
	private void setFirstBlock(int blocksize, int block) throws CoreException {
		assert fExclusiveLock;
		fHeaderChunk.putInt((blocksize/BLOCK_SIZE_DELTA - MIN_BLOCK_DELTAS + 1) * INT_SIZE, block);
	}
	
	private void removeBlock(Chunk chunk, int blocksize, int block) throws CoreException {
		assert fExclusiveLock;
		int prevblock = chunk.getInt(block + BLOCK_PREV_OFFSET);
		int nextblock = chunk.getInt(block + BLOCK_NEXT_OFFSET);
		if (prevblock != 0)
			putInt(prevblock + BLOCK_NEXT_OFFSET, nextblock);
		else // we were the head
			setFirstBlock(blocksize, nextblock);
			
		if (nextblock != 0)
			putInt(nextblock + BLOCK_PREV_OFFSET, prevblock);
	}
	
	private void addBlock(Chunk chunk, int blocksize, int block) throws CoreException {
		assert fExclusiveLock;
		// Mark our size
		chunk.putShort(block, (short) blocksize);

		// Add us to the head of the list
		int prevfirst = getFirstBlock(blocksize);
		chunk.putInt(block + BLOCK_PREV_OFFSET, 0);
		chunk.putInt(block + BLOCK_NEXT_OFFSET, prevfirst);
		if (prevfirst != 0)
			putInt(prevfirst + BLOCK_PREV_OFFSET, block);
		setFirstBlock(blocksize, block);
	}
	
	/**
	 * Free an allocate block.
	 * 
	 * @param offset
	 */
	public void free(int offset) throws CoreException {
		assert fExclusiveLock;
		// TODO - look for opportunities to merge blocks
		int block = offset - BLOCK_HEADER_SIZE;
		Chunk chunk = getChunk(block);
		int blocksize = - chunk.getShort(block);
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

	public void put3ByteUnsignedInt(int offset, int value) throws CoreException {
		getChunk(offset).put3ByteUnsignedInt(offset, value);
	}
	
	public int get3ByteUnsignedInt(int offset) throws CoreException {
		return getChunk(offset).get3ByteUnsignedInt(offset);
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
	
	/**
	 * For debugging purposes, only.
	 */
	public void reportFreeBlocks() throws CoreException {
		System.out.println("Allocated size: " + fChunks.length * CHUNK_SIZE); //$NON-NLS-1$
		System.out.println("malloc'ed: " + malloced); //$NON-NLS-1$
		System.out.println("free'd: " + freed); //$NON-NLS-1$
		System.out.println("wasted: " + (fChunks.length * CHUNK_SIZE - (malloced - freed))); //$NON-NLS-1$
		System.out.println("Free blocks"); //$NON-NLS-1$
		for (int bs = MIN_BLOCK_DELTAS*BLOCK_SIZE_DELTA; bs <= CHUNK_SIZE; bs += BLOCK_SIZE_DELTA) {
			int count = 0;
			int block = getFirstBlock(bs);
			while (block != 0) {
				++count;
				block = getInt(block + BLOCK_NEXT_OFFSET);
			}
			if (count != 0)
				System.out.println("Block size: " + bs + "=" + count); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
		
	/**
	 * Closes the database. 
	 * <p>
	 * The behavior of any further calls to the Database is undefined
	 * @throws CoreException 
	 */
	public void close() throws CoreException {
		assert fExclusiveLock;
		flush();
		removeChunksFromCache();
		
		// chunks have been removed from the cache, so we are fine
		fHeaderChunk.clear(0, CHUNK_SIZE);
		fHeaderChunk.fDirty= false;
		fChunks= new Chunk[] {null};
		try {
			fFile.close();
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}
	
	/**
     * This method is public for testing purposes only.
     */
	public File getLocation() {
		return fLocation;
	}

	/**
	 * Called from any thread via the cache, protected by {@link #fCache}.
	 */
	void releaseChunk(final Chunk chunk) {
		if (!chunk.fLocked) {
			fChunks[chunk.fSequenceNumber]= null;
		}			
	}

	/**
	 * Returns the cache used for this database.
	 * @since 4.0
	 */
	public ChunkCache getChunkCache() {
		return fCache;
	}

	/**
	 * Asserts that database is used by one thread exclusively. This is necessary when doing
	 * write operations.
	 */
	public void setExclusiveLock() {
		fExclusiveLock= true;
		fLocked= true;
	}

	public void setLocked(boolean val) {
		fLocked= val;
	}
	
	public void giveUpExclusiveLock(final boolean flush) throws CoreException {
		if (fExclusiveLock) {
			try {
				ArrayList<Chunk> dirtyChunks= new ArrayList<Chunk>();
				synchronized (fCache) {
					for (int i= 1; i < fChunks.length; i++) {
						Chunk chunk= fChunks[i];
						if (chunk != null) {
							if (chunk.fCacheIndex < 0) { 	
								// locked chunk that has been removed from cache.
								if (chunk.fDirty) {
									dirtyChunks.add(chunk); // keep in fChunks until it is flushed.
								}
								else {
									chunk.fLocked= false;
									fChunks[i]= null;
								}
							}
							else if (chunk.fLocked) {
								// locked chunk, still in cache.
								if (chunk.fDirty) {
									if (flush) {
										dirtyChunks.add(chunk);
									}
								}
								else {
									chunk.fLocked= false;
								}
							}
							else {
								assert !chunk.fDirty; // dirty chunks must be locked.
							}
						}
					}
				}
				// also handles header chunk
				flushAndUnlockChunks(dirtyChunks, flush);
			}
			finally {
				fExclusiveLock= false;
			}
		}
	}
	
	public void flush() throws CoreException {
		assert fLocked;
		if (fExclusiveLock) {
			try {
				giveUpExclusiveLock(true);
			}
			finally {
				setExclusiveLock();
			}
			return;
		}

		// be careful as other readers may access chunks concurrently
		ArrayList<Chunk> dirtyChunks= new ArrayList<Chunk>();
		synchronized (fCache) {
			for (int i= 1; i < fChunks.length ; i++) {
				Chunk chunk= fChunks[i];
				if (chunk != null && chunk.fDirty) {
					dirtyChunks.add(chunk);
				}
			}
		}

		// also handles header chunk
		flushAndUnlockChunks(dirtyChunks, true);
	}

	private void flushAndUnlockChunks(final ArrayList<Chunk> dirtyChunks, boolean isComplete) throws CoreException {
		assert !Thread.holdsLock(fCache);
		synchronized(fHeaderChunk) {
			final boolean haveDirtyChunks = !dirtyChunks.isEmpty();
			if (haveDirtyChunks || fHeaderChunk.fDirty) {
				markFileIncomplete();
			}
			if (haveDirtyChunks) {
				for (Chunk chunk : dirtyChunks) {
					if (chunk.fDirty) {
						chunk.flush();
					}
				}

				// only after the chunks are flushed we may unlock and release them.
				synchronized (fCache) {
					for (Chunk chunk : dirtyChunks) {
						chunk.fLocked= false;
						if (chunk.fCacheIndex < 0) {
							fChunks[chunk.fSequenceNumber]= null;
						}
					}
				}
			}

			if (isComplete) {
				if (fHeaderChunk.fDirty || fIsMarkedIncomplete) {
					fHeaderChunk.putInt(VERSION_OFFSET, fVersion);
					fHeaderChunk.flush();
					fIsMarkedIncomplete= false;
				}
			}
		}
	}
		
	private void markFileIncomplete() throws CoreException {
		if (!fIsMarkedIncomplete) {
			fIsMarkedIncomplete= true;
			try {
				final ByteBuffer buf= ByteBuffer.wrap(new byte[4]);
				fFile.getChannel().write(buf, 0);
			} catch (IOException e) {
				throw new CoreException(new DBStatus(e));
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

	public long getSizeBytes() {
		try {
			return fFile.length();
		} catch (IOException e) {
		}
		return 0;
	}
}
