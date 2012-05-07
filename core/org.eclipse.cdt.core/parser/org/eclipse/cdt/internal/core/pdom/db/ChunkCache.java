/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.db;

public final class ChunkCache {
	private static ChunkCache sSharedInstance= new ChunkCache();
	
	private Chunk[] fPageTable;
	private boolean fTableIsFull;
	private int fPointer;
	
	public static ChunkCache getSharedInstance() {
		return sSharedInstance;
	}

	public ChunkCache() {
		this(5 * 1024 * 1024);
	}
	
	public ChunkCache(long maxSize) {
		fPageTable= new Chunk[computeLength(maxSize)];
	}
	
	public synchronized void add(Chunk chunk, boolean locked) {
		if (locked) {
			chunk.fLocked= true;
		}
		if (chunk.fCacheIndex >= 0) {
			chunk.fCacheHitFlag= true;
			return;
		}
		if (fTableIsFull) {
			evictChunk();
			chunk.fCacheIndex= fPointer;
			fPageTable[fPointer]= chunk;
		} else {
			chunk.fCacheIndex= fPointer;
			fPageTable[fPointer]= chunk;

			fPointer++;
			if (fPointer == fPageTable.length) {
				fPointer= 0;
				fTableIsFull= true;
			}
		}
	}
	
	/**                                                                   
	 * Evicts a chunk from the page table and the chunk table.            
	 * After this method returns, {@link #fPointer}  will contain
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
			Chunk chunk = fPageTable[fPointer];
			if (chunk.fCacheHitFlag) {
				chunk.fCacheHitFlag= false;
				fPointer= (fPointer + 1) % fPageTable.length;
			} else {
				chunk.fDatabase.releaseChunk(chunk);
				chunk.fCacheIndex= -1;
				fPageTable[fPointer] = null;
				return;
			}
		}
	}

	public synchronized void remove(Chunk chunk) {
		final int idx= chunk.fCacheIndex;
		if (idx >= 0) {
			if (fTableIsFull) {
				fPointer= fPageTable.length-1;
				fTableIsFull= false;
			} else {
				fPointer--;
			}
			chunk.fCacheIndex= -1;
			final Chunk move= fPageTable[fPointer];
			fPageTable[idx]= move;
			move.fCacheIndex= idx;
			fPageTable[fPointer]= null;
		}	
	}

	/**                                                                           
	 * Returns the maximum size of the chunk cache in bytes.
	 */                                                                           
	public synchronized long getMaxSize() {
		return (long) fPageTable.length * Database.CHUNK_SIZE;
	}

	/**                                                                           
	 * Clears the page table and changes it to hold chunks with
	 * maximum total memory of <code>maxSize</code>.       
	 * @param maxSize the total size of the chunks in bytes.                
	 */                                                                           
	public synchronized void setMaxSize(long maxSize) {
		final int newLength= computeLength(maxSize);
		final int oldLength= fTableIsFull ? fPageTable.length : fPointer;
		if (newLength > oldLength) {
			Chunk[] newTable= new Chunk[newLength];
			System.arraycopy(fPageTable, 0, newTable, 0, oldLength);
			fTableIsFull= false;
			fPointer= oldLength;
			fPageTable= newTable;
		} else {
			for (int i= newLength; i < oldLength; i++) {
				final Chunk chunk= fPageTable[i];
				chunk.fDatabase.releaseChunk(chunk);
				chunk.fCacheIndex= -1;
			}
			Chunk[] newTable= new Chunk[newLength];
			System.arraycopy(fPageTable, 0, newTable, 0, newLength);
			fTableIsFull= true;
			fPointer= 0;
			fPageTable= newTable;
		}       
	}                                                                             

	private int computeLength(long maxSize) {
		long maxLength= Math.min(maxSize/Database.CHUNK_SIZE, Integer.MAX_VALUE);
		return Math.max(1, (int)maxLength);
	}
}
