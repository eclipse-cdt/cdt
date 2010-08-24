/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of char array for a file referencing content via 
 * soft references.
 */
public abstract class LazyCharArray extends AbstractCharArray {
	private final static int CHUNK_BITS= 16;  // 2^16 == 64K
	protected final static int CHUNK_SIZE= 1 << CHUNK_BITS;

	protected static class Chunk {
		final int fDataLength;
		final long fFileOffset;
		final long fFileEndOffset;
		private SoftReference<char[]> fData;

		private Chunk(long fileOffset, long fileEndOffset, char[] data) {
			fDataLength= data.length;
			fFileOffset= fileOffset;
			fFileEndOffset= fileEndOffset;
			fData= new SoftReference<char[]>(data);
		}
	}

	private int fLength= -1;
	private List<Chunk> fChunks= new ArrayList<Chunk>();
	private StreamHasher hasher;
	private long hash64;

	protected LazyCharArray() {
		hasher = new StreamHasher();
	}

	@Override
	public final int tryGetLength() {
		return fLength;
	}

	@Override
	public final int getLength() {
		readUpTo(Integer.MAX_VALUE);
		return fLength;
	}

	@Override
	public final boolean isValidOffset(int offset) {
		if (offset < 0)
			return false;

		readUpTo(offset);
		if (fLength >= 0)
			return offset < fLength;

		assert offset < fChunks.size() << CHUNK_BITS;
		return true;
	}

	@Override
	public long getContentsHash() {
		if (hasher != null) {
			readUpTo(Integer.MAX_VALUE);
			hash64 = hasher.computeHash();
			hasher = null;
		}
		return hash64;
	}

	private void readUpTo(int offset) {
		if (fLength >= 0)
			return;

		final int chunkOffset= offset >> CHUNK_BITS;
		getChunkData(chunkOffset);
	}

	@Override
	public final char get(int offset) {
		int chunkOffset= offset >> CHUNK_BITS;
		char[] data= getChunkData(chunkOffset);
		return data[offset & (CHUNK_SIZE - 1)];
	}

	@Override
	public final void arraycopy(int offset, char[] destination, int destinationPos, int length) {
		int chunkOffset= offset >> CHUNK_BITS;
		int loffset= offset & (CHUNK_SIZE - 1);
		char[] data= getChunkData(chunkOffset);
		final int canCopy = data.length - loffset;
		if (length <= canCopy) {
			System.arraycopy(data, loffset, destination, destinationPos, length);
			return;
		}
		System.arraycopy(data, loffset, destination, destinationPos, canCopy);
		arraycopy(offset+canCopy, destination, destinationPos+canCopy, length-canCopy);
	}

	private char[] getChunkData(int chunkOffset) {
		Chunk chunk= getChunk(chunkOffset);
		if (chunk != null) {
			char[] data= chunk.fData.get();
			if (data != null)
				return data;

			return loadChunkData(chunk);
		}
		return null;
	}

	private Chunk getChunk(int chunkOffset) {
		final int chunkCount = fChunks.size();
		if (chunkOffset < chunkCount)
			return fChunks.get(chunkOffset);

		if (fLength >=0)
			return null;

		return createChunk(chunkOffset);
	}

	/**
	 * Called when a chunk is requested for the first time. There is no
	 * need to override this method.
	 */
	protected Chunk createChunk(int chunkOffset) {
		final int chunkCount = fChunks.size();
		long fileOffset= chunkCount == 0 ? 0 : fChunks.get(chunkCount - 1).fFileEndOffset;
		try {
			for (int i = chunkCount; i <= chunkOffset; i++) {
				long[] fileEndOffset= {0};
				char[] data= readChunkData(fileOffset, fileEndOffset);
				final int charCount= data.length;
				if (charCount == 0) {
					fLength= fChunks.size() * CHUNK_SIZE;
					break;
				}
				if (hasher != null) {
					hasher.addChunk(data);
				}
				// New chunk
				Chunk chunk= new Chunk(fileOffset, fileEndOffset[0], data);
				fChunks.add(chunk);
				if (charCount < CHUNK_SIZE) {
					fLength= (fChunks.size() - 1) * CHUNK_SIZE + charCount;
					break;
				} 
				fileOffset= fileEndOffset[0];
			}
		} catch (Exception e) {
			// File cannot be read
			return null;
		} 
		
		if (chunkOffset < fChunks.size())
			return fChunks.get(chunkOffset);

		return null;
	}
	
	private char[] loadChunkData(Chunk chunk) {
		char[] result= new char[chunk.fDataLength];
		rereadChunkData(chunk.fFileOffset, chunk.fFileEndOffset, result);
		chunk.fData= new SoftReference<char[]>(result);
		return result;
	}

	/**
	 * Read the chunk data at the given source offset and provide the end-offset in
	 * the source.
	 */
	protected abstract char[] readChunkData(long sourceOffset, long[] sourceEndOffsetHolder) throws Exception;

	/**
	 * Read the chunk data at the given source range. In case the source range no longer (fully) exists,
	 * read as much as possible.
	 */
	protected abstract void rereadChunkData(long fileOffset, long fileEndOffset, char[] dest);
}
