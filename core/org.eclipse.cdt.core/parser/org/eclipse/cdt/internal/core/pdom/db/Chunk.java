/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.core.runtime.CoreException;

/**
 * Caches the content of a piece of the database.
 */
final class Chunk {
	final private byte[] fBuffer= new byte[Database.CHUNK_SIZE];

	final Database fDatabase;
	final int fSequenceNumber;
	
	boolean fCacheHitFlag= false;
	boolean fDirty= false;
	boolean fLocked= false;	// locked chunks must not be released from cache.
	int fCacheIndex= -1;
		
	Chunk(Database db, int sequenceNumber) {
		fDatabase= db;
		fSequenceNumber= sequenceNumber;
	}

	void read() throws CoreException {
		try {
			final ByteBuffer buf= ByteBuffer.wrap(fBuffer);
			fDatabase.read(buf, (long)fSequenceNumber*Database.CHUNK_SIZE);
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}

	void flush() throws CoreException {
		try {
			final ByteBuffer buf= ByteBuffer.wrap(fBuffer);
			fDatabase.write(buf, (long)fSequenceNumber*Database.CHUNK_SIZE);
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
		fDirty= false;
	}
	private static int recPtrToIndex( final long offset ) {
		return (int)(offset & Database.OFFSET_IN_CHUNK_MASK );
	}

	public void putByte(final long offset, final byte value) {
		assert fLocked;
		fDirty= true;
		fBuffer[recPtrToIndex( offset )]= value;
	}
	
	public byte getByte(final long offset) {
		return fBuffer[recPtrToIndex( offset )];
	}
	
	public byte[] getBytes(final long offset, final int length) {
		final byte[] bytes = new byte[length];
		System.arraycopy(fBuffer, recPtrToIndex( offset ), bytes, 0, length);
		return bytes;
	}
	
	public void putBytes(final long offset, final byte[] bytes) {
		assert fLocked;
		fDirty= true;
		System.arraycopy(bytes, 0, fBuffer, recPtrToIndex( offset ), bytes.length);
	}
	
	public void putInt(final long offset, final int value) {
		assert fLocked;
		fDirty= true;
		int idx= recPtrToIndex( offset );
		fBuffer[idx]=   (byte)(value >> 24);
		fBuffer[++idx]= (byte)(value >> 16);
		fBuffer[++idx]= (byte)(value >> 8);
		fBuffer[++idx]= (byte)(value);
	}
	
	public int getInt(final long offset) {
		int idx= recPtrToIndex( offset );
		return ((fBuffer[idx] & 0xff) << 24) |
			((fBuffer[++idx] & 0xff) << 16) |
			((fBuffer[++idx] & 0xff) <<  8) |
			((fBuffer[++idx] & 0xff) <<  0);
	}
	
	/*
	 * A Record Pointer is a pointer as returned by Database.malloc().
	 * This is a pointer to a block + BLOCK_HEADER_SIZE.
	 * 
	 * A free Record Pointer is a pointer to a raw block, i.e. the
	 * pointer is not moved past the BLOCK_HEADER_SIZE.
	 */
	public void putRecPtr(final long offset, final long value) {
		if (!fDatabase.usesDensePointers()) {
			putFreeRecPtr(offset, value);
		} else {
			putFreeRecPtr(offset, value == 0 ? value : value - Database.BLOCK_HEADER_SIZE);
		}
		return;
	}
	
	public void putFreeRecPtr(final long offset, final long value) {
		if (!fDatabase.usesDensePointers()) {
			putInt(offset, (int) value);
			return;
		}
		/*
		 * This assert verifies the alignment. We expect the low bits to be clear.
		 */
		assert (value & (Database.BLOCK_SIZE_DELTA - 1)) == 0;
		putInt(offset, (int) (value >> Database.BLOCK_SIZE_DELTA_BITS));
	}
	
	public long getRecPtr(final long offset) {
		if (!fDatabase.usesDensePointers()) {
			return getInt(offset);
		}
		long address = getFreeRecPtr(offset);
		return address != 0 ? (address + Database.BLOCK_HEADER_SIZE) : address;
	}
	
	
	public long getFreeRecPtr(final long offset) {
		int value = getInt(offset);
		if (!fDatabase.usesDensePointers()) {
			return value;
		}
		/*
		 * We need to properly manage the integer that was read. The value will be sign-extended 
		 * so if the most significant bit is set, the resulting long will look negative. By 
		 * masking it with ((long)1 << 32) - 1 we remove all the sign-extended bits and just 
		 * have an unsigned 32-bit value as a long. This gives us one more useful bit in the 
		 * stored record pointers.
		 */
		long address = value & (((long) 1 << Integer.SIZE) - 1);
		return address << Database.BLOCK_SIZE_DELTA_BITS;
	}

	
	public void put3ByteUnsignedInt(final long offset, final int value) {
		assert fLocked;
		fDirty= true;
		int idx= recPtrToIndex( offset );
		fBuffer[idx]= (byte)(value >> 16);
		fBuffer[++idx]= (byte)(value >> 8);
		fBuffer[++idx]= (byte)(value);
	}
	
	public int get3ByteUnsignedInt(final long offset) {
		int idx= recPtrToIndex( offset );
		return ((fBuffer[idx] & 0xff) << 16) |
			((fBuffer[++idx] & 0xff) <<  8) |
			((fBuffer[++idx] & 0xff) <<  0);
	}

	public void putShort(final long offset, final short value) {
		assert fLocked;
		fDirty= true;
		int idx= recPtrToIndex( offset );
		fBuffer[idx]= (byte)(value >> 8);
		fBuffer[++idx]= (byte)(value);
	}
	
	public short getShort(final long offset) {
		int idx= recPtrToIndex( offset );
		return (short) (((fBuffer[idx] << 8) | (fBuffer[++idx] & 0xff)));
	}

	public long getLong(final long offset) {
		int idx= recPtrToIndex( offset );
		return ((((long)fBuffer[idx] & 0xff) << 56) |
				(((long)fBuffer[++idx] & 0xff) << 48) |
				(((long)fBuffer[++idx] & 0xff) << 40) |
				(((long)fBuffer[++idx] & 0xff) << 32) |
				(((long)fBuffer[++idx] & 0xff) << 24) |
				(((long)fBuffer[++idx] & 0xff) << 16) |
				(((long)fBuffer[++idx] & 0xff) <<  8) |
				(((long)fBuffer[++idx] & 0xff) <<  0));
	}

	public void putLong(final long offset, final long value) {
		assert fLocked;
		fDirty= true;
		int idx= recPtrToIndex( offset );

		fBuffer[idx]=   (byte)(value >> 56);
		fBuffer[++idx]= (byte)(value >> 48);
		fBuffer[++idx]= (byte)(value >> 40);
		fBuffer[++idx]= (byte)(value >> 32);
		fBuffer[++idx]= (byte)(value >> 24);
		fBuffer[++idx]= (byte)(value >> 16);
		fBuffer[++idx]= (byte)(value >> 8);
		fBuffer[++idx]= (byte)(value);
	}
	
	public void putChar(final long offset, final char value) {
		assert fLocked;
		fDirty= true;
		int idx= recPtrToIndex( offset );
		fBuffer[idx]= (byte)(value >> 8);
		fBuffer[++idx]= (byte)(value);
	}
	
	public char getChar(final long offset) {
		int idx= recPtrToIndex( offset );
		return (char) (((fBuffer[idx] << 8) | (fBuffer[++idx] & 0xff)));
	}

	public void getCharArray(final long offset, final char[] result) {
		final ByteBuffer buf= ByteBuffer.wrap(fBuffer);
		buf.position(recPtrToIndex( offset ));
		buf.asCharBuffer().get(result);
	}
	
	void clear(final long offset, final int length) {
		assert fLocked;
		fDirty= true;
		int idx= recPtrToIndex( offset );
		final int end= idx + length;
		for (; idx < end; idx++) {
			fBuffer[idx]= 0;
		}
	}
}
