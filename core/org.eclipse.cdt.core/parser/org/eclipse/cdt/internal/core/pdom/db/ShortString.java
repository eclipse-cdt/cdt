/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * This is for strings that fit inside a single chunk.
 * 
 * @author Doug Schaefer
 */
public class ShortString implements IString {
	
	private final Database db;
	private final int record;
	
	private static final int LENGTH = 0;
	private static final int CHARS = 4;
	
	public static final int MAX_LENGTH = (Database.MAX_SIZE - CHARS) / 2;
	
	public ShortString(Database db, int offset) {
		this.db = db;
		this.record = offset;
	}

	public ShortString(Database db, char[] chars) throws CoreException {
		this.db = db;
		this.record = db.malloc(CHARS + chars.length * 2);
		
		Chunk chunk = db.getChunk(record);
		chunk.putInt(record + LENGTH, (char)chars.length);
		int n = chars.length;
		int p = record + CHARS;
		for (int i = 0; i < n; ++i) {
			chunk.putChar(p, chars[i]);
			p += 2;
		}
	}
	
	public ShortString(Database db, String string) throws CoreException {
		this.db = db;
		this.record = db.malloc(CHARS + string.length() * 2);
		
		Chunk chunk = db.getChunk(record);
		chunk.putInt(record + LENGTH, string.length());
		int n = string.length();
		int p = record + CHARS;
		for (int i = 0; i < n; ++i) {
			chunk.putChar(p, string.charAt(i));
			p += 2;
		}
	}
	
	public int getRecord() {
		return record;
	}
	
	public void delete() throws CoreException {
		db.free(record);
	}
	
	public char[] getChars() throws CoreException {
		Chunk chunk = db.getChunk(record);
		int length = chunk.getInt(record + LENGTH);
		char[] chars = new char[length];
		int p = record + CHARS;
		for (int i = 0; i < length; ++i) {
			chars[i] = chunk.getChar(p);
			p += 2;
		}
		return chars;
	}
	
	public String getString() throws CoreException {
		return new String(getChars());
	}
	
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		try {
			if (obj instanceof ShortString) {
				ShortString string = (ShortString)obj;
				if (db == string.db && record == string.record)
					return true;
				
				Chunk chunk1 = db.getChunk(record);
				Chunk chunk2 = string.db.getChunk(string.record);
				
				int n1 = chunk1.getInt(record);
				int n2 = chunk2.getInt(string.record);
				if (n1 != n2)
					return false;
				
				int p1 = record + CHARS;
				int p2 = string.record + CHARS;
				for (int i = 0; i < n1; ++i) {
					if (chunk1.getChar(p1) != chunk2.getChar(p2))
						return false;
					p1 += 2;
					p2 += 2;
				}
				return true;
			} else if (obj instanceof char[]) {
				char[] chars = (char[])obj;
				Chunk chunk = db.getChunk(record);

				// Make sure size is the same
				int n = chunk.getInt(record);
				if (n != chars.length)
					return false;
				
				// Check each character
				int p = record + CHARS;
				for (int i = 0; i < n; ++i) {
					if (chunk.getChar(p) != chars[i])
						return false;
					p += 2;
				}
				return true;
			} else if (obj instanceof String) {
				String string = (String)obj;
				Chunk chunk = db.getChunk(record);

				// Make sure size is the same
				int n = chunk.getInt(record);
				if (n != string.length())
					return false;
				
				// Check each character
				int p = record + CHARS;
				for (int i = 0; i < n; ++i) {
					if (chunk.getChar(p) != string.charAt(i))
						return false;
					p += 2;
				}
				return true;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return false;
	}
	
	public int hashCode() {
		// Custom hash code function to allow DBStrings in hashmaps.
		return record;
	}

	public int compare(IString string) throws CoreException {
		if (string instanceof ShortString)
			return compare((ShortString)string);
		else if (string instanceof LongString)
			return - ((LongString)string).compare(this);
		else
			throw new IllegalArgumentException();
	}
	
	public int compare(ShortString other) throws CoreException {
		Chunk chunk1 = db.getChunk(record);
		Chunk chunk2 = other.db.getChunk(other.record);

		int i1 = record + CHARS;
		int i2 = other.record + CHARS;
		int n1 = i1 + chunk1.getInt(record + LENGTH) * 2;
		int n2 = i2 + chunk2.getInt(other.record + LENGTH) * 2;
		
		while (i1 < n1 && i2 < n2) {
			char c1 = chunk1.getChar(i1);
			char c2 = chunk2.getChar(i2);
			
			if (c1 < c2)
				return -1;
			if (c1 > c2)
				return 1;
			
			i1 += 2;
			i2 += 2;
		}

		if (i1 == n1 && i2 != n2)
			return -1;
		else if (i2 == n2 && i1 != n1)
			return 1;
		else
			return 0;
	}
	
	public int compare(char[] other) throws CoreException {
		Chunk chunk = db.getChunk(record);
		
		int i1 = record + CHARS;
		int i2 = 0;
		int n1 = i1 + chunk.getInt(record + LENGTH) * 2;
		int n2 = other.length;
		
		while (i1 < n1 && i2 < n2) {
			char c1 = chunk.getChar(i1);
			char c2 = other[i2];
			
			if (c1 < c2)
				return -1;
			if (c1 > c2)
				return 1;
			
			i1 += 2;
			++i2;
		}

		if (i1 == n1 && i2 != n2)
			return -1;
		else if (i2 == n2 && i1 != n1)
			return 1;
		else
			return 0;
	}
	
	public int compare(String other) throws CoreException {
		Chunk chunk = db.getChunk(record);
		
		int i1 = record + CHARS;
		int i2 = 0;
		int n1 = i1 + chunk.getInt(record + LENGTH) * 2;
		int n2 = other.length();
		
		while (i1 < n1 && i2 < n2) {
			char c1 = chunk.getChar(i1);
			char c2 = other.charAt(i2);
			
			if (c1 < c2)
				return -1;
			if (c1 > c2)
				return 1;
			
			i1 += 2;
			++i2;
		}

		if (i1 == n1 && i2 != n2)
			return -1;
		else if (i2 == n2 && i1 != n1)
			return 1;
		else
			return 0;
	}

}
