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
	final private ByteBuffer fBuffer;

	final Database fDatabase;
	final int fSequenceNumber;
	
	boolean fCacheHitFlag= false;
	boolean fDirty= false;
	boolean fLocked= false;	// locked chunks must not be released from cache.
	int fCacheIndex= -1;
		
	Chunk(Database db, int sequenceNumber) {
		fDatabase= db;
		fBuffer= ByteBuffer.allocate(Database.CHUNK_SIZE);
		fSequenceNumber= sequenceNumber;
	}

	void read() throws CoreException {
		try {
			fBuffer.position(0);
			fDatabase.getChannel().read(fBuffer, fSequenceNumber*Database.CHUNK_SIZE);
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}

	void flush() throws CoreException {
		try {
			fBuffer.position(0);
			fDatabase.getChannel().write(fBuffer, fSequenceNumber*Database.CHUNK_SIZE);
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
		fDirty= false;
	}

	public void putByte(int offset, byte value) {
		fDirty= true;
		fBuffer.put(offset % Database.CHUNK_SIZE, value);
	}
	
	public byte getByte(int offset) {
		return fBuffer.get(offset % Database.CHUNK_SIZE);
	}
	
	public byte[] getBytes(int offset, int length) {
		byte[] bytes = new byte[length];
		fBuffer.position(offset % Database.CHUNK_SIZE);
		fBuffer.get(bytes, 0, length);
		return bytes;
	}
	
	public void putBytes(int offset, byte[] bytes) {
		fDirty= true;
		fBuffer.position(offset % Database.CHUNK_SIZE);
		fBuffer.put(bytes, 0, bytes.length);
	}
	
	public void putInt(int offset, int value) {
		fDirty= true;
		fBuffer.putInt(offset % Database.CHUNK_SIZE, value);
	}
	
	public int getInt(int offset) {
		return fBuffer.getInt(offset % Database.CHUNK_SIZE);
	}

	public void putShort(int offset, short value) {
		fDirty= true;
		fBuffer.putShort(offset % Database.CHUNK_SIZE, value);
	}
	
	public short getShort(int offset) {
		return fBuffer.getShort(offset % Database.CHUNK_SIZE);
	}

	public long getLong(int offset) {
		return fBuffer.getLong(offset % Database.CHUNK_SIZE);
	}

	public void putLong(int offset, long value) {
		fDirty= true;
		fBuffer.putLong(offset % Database.CHUNK_SIZE, value);
	}
	
	public void putChar(int offset, char value) {
		fDirty= true;
		fBuffer.putChar(offset % Database.CHUNK_SIZE, value);
	}
	
	public char getChar(int offset) {
		return fBuffer.getChar(offset % Database.CHUNK_SIZE);
	}

	public void getCharArray(int offset, char[] result) {
		fBuffer.position(offset % Database.CHUNK_SIZE);
		fBuffer.asCharBuffer().get(result);
	}
	
	void clear(int offset, int length) {
		fDirty= true;
		fBuffer.position(offset % Database.CHUNK_SIZE);
		fBuffer.put(new byte[length]);
	}
}
