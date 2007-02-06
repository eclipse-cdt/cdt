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

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class Chunk {

	private final Database db;
	private final int index;
	private final byte[] buffer;
	
	Chunk lruPrev;
	Chunk lruNext;
	
	private boolean dirty;
	
	Chunk(Database db, int index) throws CoreException {
		this.db = db;
		this.index = index;
		buffer = new byte[Database.CHUNK_SIZE];
		
		try {
			db.file.seek(index * Database.CHUNK_SIZE);
			db.file.read(buffer, 0, Database.CHUNK_SIZE);
		} catch (IOException e) {			
			throw new CoreException(new DBStatus(e));
		}
	}

	public final void save() throws CoreException {
		if (!dirty)
			return;
		
		if (index == 0 && getInt(0) != 11)
			return;
		
		try {
			db.file.seek(index * Database.CHUNK_SIZE);
			db.file.write(buffer, 0, Database.CHUNK_SIZE);
			dirty = false;
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
	}

	public void putByte(int offset, byte value) {
		dirty = true;
		buffer[offset % Database.CHUNK_SIZE] = value;
	}
	
	public byte getByte(int offset) {
		return buffer[offset % Database.CHUNK_SIZE];
	}
	
	public byte[] getBytes(int offset, int length) {
		byte[] bytes = new byte[length];
		System.arraycopy(buffer, offset % Database.CHUNK_SIZE, bytes, 0, length);
		return bytes;
	}
	
	public void putBytes(int offset, byte[] bytes) {
		dirty = true;
		System.arraycopy(bytes, 0, buffer, offset % Database.CHUNK_SIZE, bytes.length);
	}
	
	public void putInt(int offset, int value) {
		dirty = true;
		int i = offset % Database.CHUNK_SIZE;
		buffer[i++] = (byte)((value >>> 24) & 0xff);
		buffer[i++] = (byte)((value >>> 16) & 0xff);
		buffer[i++] = (byte)((value >>> 8) & 0xff);
		buffer[i] = (byte)(value & 0xff);
	}
	
	public int getInt(int offset) {
		int i = offset % Database.CHUNK_SIZE;
		return ((buffer[i] & 0xff) << 24)
			| ((buffer[i + 1] & 0xff) << 16)
			| ((buffer[i + 2] & 0xff) << 8)
			| (buffer[i + 3] & 0xff);
	}
	
	public void putChar(int offset, char value) {
		dirty = true;
		int i = offset % Database.CHUNK_SIZE;
		buffer[i] = (byte)((value >>> 8) & 0xff);
		buffer[i + 1] = (byte)(value & 0xff);
	}
	
	public char getChar(int offset) {
		int i = offset % Database.CHUNK_SIZE;
		return (char)(((buffer[i] & 0xff) << 8) + (buffer[i + 1] & 0xff));
	}
	
	void clear(int offset, int length) {
		dirty = true;
		int i = offset % Database.CHUNK_SIZE;
		while (length > 0) {
			buffer[i++] = 0;
			--length;
		}
	}
	
	void free() throws CoreException {
		save();
		db.freeChunk(index);
	}
	
}
