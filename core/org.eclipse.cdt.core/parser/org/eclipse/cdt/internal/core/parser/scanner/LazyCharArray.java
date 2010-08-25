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
 * Because of bug 320157 we need to deal with chunks of different length.
 */
public abstract class LazyCharArray extends AbstractCharArray {
	private final static int CHUNK_BITS= 16;  // 2^16 == 64K
	public final static int CHUNK_SIZE= 1 << CHUNK_BITS;

	protected static class Chunk {
		final int fCharOffset;
		final int fCharEndOffset;
		final long fSourceOffset;
		final long fSourceEndOffset;
		private SoftReference<char[]> fCharsReference;

		private Chunk(long sourceOffset, long sourceEndOffset, int charOffset, char[] chars) {
			fCharOffset= charOffset;
			fCharEndOffset= charOffset+ chars.length;
			fSourceOffset= sourceOffset;
			fSourceEndOffset= sourceEndOffset;
			fCharsReference= new SoftReference<char[]>(chars);
		}
	}

	private int fLength= -1;
	private List<Chunk> fChunks= new ArrayList<Chunk>();
	private StreamHasher fHasher;
	private long fHash64;
	// Make a reference to the currently used char[], such that it is not collected.
	private char[] fCurrentChars;

	protected LazyCharArray() {
		fHasher = new StreamHasher();
	}

	@Override
	public final int tryGetLength() {
		return fLength;
	}

	@Override
	public final int getLength() {
		readAllChunks();
		return fLength;
	}

	@Override
	public final boolean isValidOffset(int offset) {
		if (offset < 0)
			return false;

		if (fLength >= 0)
			return offset < fLength;

		return getChunkForOffset(offset) != null;
	}

	@Override
	public long getContentsHash() {
		if (fHasher != null) {
			readAllChunks();
			fHash64 = fHasher.computeHash();
			fHasher = null;
		}
		return fHash64;
	}

	@Override
	public final char get(int offset) {
		Chunk chunk= getChunkForOffset(offset);
		if (chunk != null) {
			return getChunkData(chunk)[offset - chunk.fCharOffset];
		}
		return 0;
	}

	@Override
	public final void arraycopy(int offset, char[] destination, int destinationPos, int length) {
		final Chunk chunk= getChunkForOffset(offset);
		final int offsetInChunk= offset-chunk.fCharOffset;
		final char[] data= getChunkData(chunk);
		final int maxLenInChunk = data.length - offsetInChunk;
		if (length <= maxLenInChunk) {
			System.arraycopy(data, offsetInChunk, destination, destinationPos, length);
		} else {
			System.arraycopy(data, offsetInChunk, destination, destinationPos, maxLenInChunk);
			arraycopy(offset+maxLenInChunk, destination, destinationPos+maxLenInChunk, length-maxLenInChunk);
		}
	}

	private void readAllChunks() {
		if (fLength < 0) {
			getChunkForOffset(Integer.MAX_VALUE);
		}
	}

	private Chunk getChunkForOffset(int offset) {
		int minChunkNumber= offset >> CHUNK_BITS;
		for(;;) {
			Chunk chunk= getChunkByNumber(minChunkNumber);
			if (chunk == null) 
				return null;
			
			if (offset < chunk.fCharEndOffset) {
				return chunk;
			}
			minChunkNumber++;
		}
	}

	private Chunk getChunkByNumber(int chunkNumber) {
		final int chunkCount = fChunks.size();
		if (chunkNumber < chunkCount)
			return fChunks.get(chunkNumber);

		if (fLength >=0)
			return null;

		return createChunk(chunkNumber);
	}

	/**
	 * Called when a chunk is requested for the first time. There is no
	 * need to override this method.
	 */
	protected Chunk createChunk(int chunkNumber) {
		for (int i = fChunks.size(); i <= chunkNumber; i++) {
			Chunk chunk= nextChunk();
			if (chunk == null) {
				final int chunkCount= fChunks.size();
				fLength= chunkCount == 0 ? 0 : fChunks.get(chunkCount-1).fCharEndOffset;
				break;
			}
			if (fHasher != null) {
				final char[] chunkData = getChunkData(chunk);
				fHasher.addChunk(chunkData);
			}
			fChunks.add(chunk);
		}

		if (chunkNumber < fChunks.size())
			return fChunks.get(chunkNumber);

		return null;
	}
	
	/**
	 * Creates a new chunk.
	 */
	protected Chunk newChunk(long sourceOffset, long sourceEndOffset, int charOffset, char[] chars) {
		fCurrentChars= chars;
		return new Chunk(sourceOffset, sourceEndOffset, charOffset, chars);
	}

	/**
	 * Read the next chunk from the input.
	 */
	protected abstract Chunk nextChunk();

	private char[] getChunkData(Chunk chunk) {
		char[] data= chunk.fCharsReference.get();
		if (data == null) {
			data= new char[chunk.fCharEndOffset - chunk.fCharOffset];
			rereadChunkData(chunk, data);
			chunk.fCharsReference= new SoftReference<char[]>(data);
		}
		return fCurrentChars= data;
	}

	/**
	 * Reread the data for the chunk. In case the source range no longer (fully) exists,
	 * read as much as possible.
	 */
	protected abstract void rereadChunkData(Chunk chunk, char[] data);

	/** 
	 * For testing purposes: Simulates that all the data gets collected.
	 */
	public void testClearData() {
		for (Chunk chunk : fChunks) {
			chunk.fCharsReference= new SoftReference<char[]>(null);
		}
		if (fCurrentChars != null)
			fCurrentChars= null;
	}
}
