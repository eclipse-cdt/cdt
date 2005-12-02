/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class Chunk {

	private MappedByteBuffer buffer;
	
	// Cache info
	private Database db;
	int index;
	private Chunk prevChunk;
	private Chunk nextChunk;
	
	Chunk(RandomAccessFile file, int offset) throws CoreException {
		try {
			index = offset / Database.CHUNK_SIZE;
			buffer = file.getChannel().map(MapMode.READ_WRITE, offset, Database.CHUNK_SIZE);
		} catch (IOException e) {
			throw new CoreException(new DBStatus(e));
		}
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
	
	// Strings have a 16 bit length followed by the chars
	
	public void putChars(int offset, char[] value) {
		buffer.position(offset % Database.CHUNK_SIZE);
		buffer.putChar((char)value.length);
		for (int i = 0; i < value.length; ++i)
			buffer.putChar(value[i]);
	}
	
	public char[] getChars(int offset) {
		buffer.position(offset % Database.CHUNK_SIZE);
		int n = buffer.getChar();
		char[] chars = new char[n];
		for (int i = 0; i < n; ++i)
			chars[i] = buffer.getChar();
		return chars;
	}
	
	public void putString(int offset, String value) {
		buffer.position(offset % Database.CHUNK_SIZE);
		int n = value.length();
		buffer.putChar((char)n);
		for (int i = 0; i < n; ++i)
			buffer.putChar(value.charAt(i));
	}
	
	public String getString(int offset) {
		return new String(getChars(offset));
	}

	Chunk getNextChunk() {
		return nextChunk;
	}
	
	void setNextChunk(Chunk nextChunk) {
		this.nextChunk = nextChunk;
	}
	
	Chunk getPrevChunk() {
		return prevChunk;
	}
	
	void setPrevChunk(Chunk prevChunk) {
		this.prevChunk = prevChunk;
	}
	
	void clear(int offset, int length) {
		buffer.position(offset % Database.CHUNK_SIZE);
		buffer.put(new byte[length]);
	}
	
	void free() {
		// nextChunk should be null
		db.toc[index] = null;
		db.lruChunk = prevChunk;
		prevChunk.nextChunk = null;
	}
	
}
