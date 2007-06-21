/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
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
			fDatabase.getChannel().read(buf, fSequenceNumber*Database.CHUNK_SIZE);
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}

	void flush() throws CoreException {
		try {
			final ByteBuffer buf= ByteBuffer.wrap(fBuffer);
			fDatabase.getChannel().write(buf, fSequenceNumber*Database.CHUNK_SIZE);
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
		fDirty= false;
	}

	public void putByte(final int offset, final byte value) {
		assert fLocked;
		fDirty= true;
		fBuffer[offset % Database.CHUNK_SIZE]= value;
	}
	
	public byte getByte(final int offset) {
		return fBuffer[offset % Database.CHUNK_SIZE];
	}
	
	public byte[] getBytes(final int offset, final int length) {
		final byte[] bytes = new byte[length];
		System.arraycopy(fBuffer, offset % Database.CHUNK_SIZE, bytes, 0, length);
		return bytes;
	}
	
	public void putBytes(final int offset, final byte[] bytes) {
		assert fLocked;
		fDirty= true;
		System.arraycopy(bytes, 0, fBuffer, offset % Database.CHUNK_SIZE, bytes.length);
	}
	
	public void putInt(final int offset, final int value) {
		assert fLocked;
		fDirty= true;
		int idx= offset % Database.CHUNK_SIZE;
		fBuffer[idx]=   (byte)(value >> 24);
		fBuffer[++idx]= (byte)(value >> 16);
		fBuffer[++idx]= (byte)(value >> 8);
		fBuffer[++idx]= (byte)(value);
	}
	
	public int getInt(final int offset) {
		int idx= offset % Database.CHUNK_SIZE;
		return ((fBuffer[idx] & 0xff) << 24) |
			((fBuffer[++idx] & 0xff) << 16) |
			((fBuffer[++idx] & 0xff) <<  8) |
			((fBuffer[++idx] & 0xff) <<  0);
	}

	public void putShort(final int offset, final short value) {
		assert fLocked;
		fDirty= true;
		int idx= offset % Database.CHUNK_SIZE;
		fBuffer[idx]= (byte)(value >> 8);
		fBuffer[++idx]= (byte)(value);
	}
	
	public short getShort(final int offset) {
		int idx= offset % Database.CHUNK_SIZE;
		return (short) (((fBuffer[idx] << 8) | (fBuffer[++idx] & 0xff)));
	}

	public long getLong(final int offset) {
		int idx= offset % Database.CHUNK_SIZE;
		return ((((long)fBuffer[idx] & 0xff) << 56) |
				(((long)fBuffer[++idx] & 0xff) << 48) |
				(((long)fBuffer[++idx] & 0xff) << 40) |
				(((long)fBuffer[++idx] & 0xff) << 32) |
				(((long)fBuffer[++idx] & 0xff) << 24) |
				(((long)fBuffer[++idx] & 0xff) << 16) |
				(((long)fBuffer[++idx] & 0xff) <<  8) |
				(((long)fBuffer[++idx] & 0xff) <<  0));
	}

	public void putLong(final int offset, final long value) {
		assert fLocked;
		fDirty= true;
		int idx= offset % Database.CHUNK_SIZE;

		fBuffer[idx]=   (byte)(value >> 56);
		fBuffer[++idx]= (byte)(value >> 48);
		fBuffer[++idx]= (byte)(value >> 40);
		fBuffer[++idx]= (byte)(value >> 32);
		fBuffer[++idx]= (byte)(value >> 24);
		fBuffer[++idx]= (byte)(value >> 16);
		fBuffer[++idx]= (byte)(value >> 8);
		fBuffer[++idx]= (byte)(value);
	}
	
	public void putChar(final int offset, final char value) {
		assert fLocked;
		fDirty= true;
		int idx= offset % Database.CHUNK_SIZE;
		fBuffer[idx]= (byte)(value >> 8);
		fBuffer[++idx]= (byte)(value);
	}
	
	public char getChar(final int offset) {
		int idx= offset % Database.CHUNK_SIZE;
		return (char) (((fBuffer[idx] << 8) | (fBuffer[++idx] & 0xff)));
	}

	public void getCharArray(final int offset, final char[] result) {
		final ByteBuffer buf= ByteBuffer.wrap(fBuffer);
		buf.position(offset % Database.CHUNK_SIZE);
		buf.asCharBuffer().get(result);
	}
	
	void clear(final int offset, final int length) {
		assert fLocked;
		fDirty= true;
		int idx= (offset % Database.CHUNK_SIZE);
		final int end= idx + length;
		for (; idx < end; idx++) {
			fBuffer[idx]= 0;
		}
	}
}
