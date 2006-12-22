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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class Chunk {

	private ByteBuffer buffer;
	
	// Cache info
	private Database db;
	int index;
	
	Chunk(RandomAccessFile file, int offset) throws CoreException {
		index = offset / Database.CHUNK_SIZE;
		try {
			buffer = file.getChannel().map(MapMode.READ_WRITE, offset, Database.CHUNK_SIZE);
		} catch (IOException e) {
			try {
				buffer = ByteBuffer.allocateDirect(Database.CHUNK_SIZE);
				file.seek(offset);
				file.getChannel().read(buffer);
			} catch (IOException e2) {			
				throw new CoreException(new DBStatus(e2));
			}
		}
	}

	public void save() throws CoreException {
		// if we're not memory mapped, write the buffer out to the file
		if (buffer instanceof MappedByteBuffer)
			((MappedByteBuffer)buffer).force();
		else {
			try {
				db.file.seek(index * Database.CHUNK_SIZE);
				db.file.getChannel().write(buffer);
			} catch (IOException e) {
				throw new CoreException(new DBStatus(e));
			}
		}
	}

	public void putByte(int offset, byte value) {
		buffer.put(offset % Database.CHUNK_SIZE, value);
	}
	
	public byte getByte(int offset) {
		return buffer.get(offset % Database.CHUNK_SIZE);
	}
	
	public byte[] getBytes(int offset, int length) {
		byte[] bytes = new byte[length];
		buffer.position(offset % Database.CHUNK_SIZE);
		buffer.get(bytes, 0, length);
		return bytes;
	}
	
	public void putBytes(int offset, byte[] bytes) {
		buffer.position(offset % Database.CHUNK_SIZE);
		buffer.put(bytes, 0, bytes.length);
	}
	
	public void putInt(int offset, int value) {
		buffer.putInt(offset % Database.CHUNK_SIZE, value);
	}
	
	public int getInt(int offset) {
		return buffer.getInt(offset % Database.CHUNK_SIZE);
	}
	
	public void putChar(int offset, char value) {
		buffer.putChar(offset % Database.CHUNK_SIZE, value);
	}
	
	public char getChar(int offset) {
		return buffer.getChar(offset % Database.CHUNK_SIZE);
	}
	
	void clear(int offset, int length) {
		buffer.position(offset % Database.CHUNK_SIZE);
		buffer.put(new byte[length]);
	}
	
	void free() {
		db.toc[index] = null;
	}
	
}
