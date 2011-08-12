/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
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
	private final long record;
	private int hash;
	
	private static final int LENGTH = 0;
	private static final int CHARS = 4;
	
	public static final int MAX_LENGTH = (Database.MAX_MALLOC_SIZE - CHARS) / 2;
	
	public ShortString(Database db, long offset) {
		this.db = db;
		this.record = offset;
	}

	public ShortString(Database db, char[] chars) throws CoreException {
		this.db = db;
		this.record = db.malloc(CHARS + chars.length * 2);
		
		Chunk chunk = db.getChunk(record);
		chunk.putInt(record + LENGTH, (char)chars.length);
		int n = chars.length;
		long p = record + CHARS;
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
		long p = record + CHARS;
		for (int i = 0; i < n; ++i) {
			chunk.putChar(p, string.charAt(i));
			p += 2;
		}
	}
	
	public long getRecord() {
		return record;
	}
	
	public void delete() throws CoreException {
		db.free(record);
	}
	
	public char[] getChars() throws CoreException {
		Chunk chunk = db.getChunk(record);
		int length = chunk.getInt(record + LENGTH);
		char[] chars = new char[length];
		chunk.getCharArray(record + CHARS, chars);
		return chars;
	}
	
	public String getString() throws CoreException {
		return new String(getChars());
	}
	
	@Override
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
				
				long p1 = record + CHARS;
				long p2 = string.record + CHARS;
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
				long p = record + CHARS;
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
				long p = record + CHARS;
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
	
	/**
	 * Compatible with {@link String#hashCode()}
	 */
	@Override
	public int hashCode() {
		int h = hash;
		if (h == 0) {
			char chars[];
			try {
				chars = getChars();
				final int len = chars.length;
				for (int i = 0; i < len; i++) {
					h = 31*h + chars[i];
				}
			} catch (CoreException e) {
			}
			hash = h;
		}
		return h;
	}
	
	public int compare(char[] other, boolean caseSensitive) throws CoreException {
		Chunk chunk = db.getChunk(record);
		
		long i1 = record + CHARS;
		int i2 = 0;
		long n1 = i1 + chunk.getInt(record + LENGTH) * 2;
		int n2 = other.length;
		
		while (i1 < n1 && i2 < n2) {
			int cmp= compareChars(chunk.getChar(i1), other[i2], caseSensitive);
			if (cmp != 0)
				return cmp;
			
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
	
	public int compare(IString string, boolean caseSensitive) throws CoreException {
		if (string instanceof ShortString)
			return compare((ShortString)string, caseSensitive);
		else if (string instanceof LongString)
			return - ((LongString)string).compare(this, caseSensitive);
		else
			throw new IllegalArgumentException();
	}
	
	public int compare(ShortString other, boolean caseSensitive) throws CoreException {
		Chunk chunk1 = db.getChunk(record);
		Chunk chunk2 = other.db.getChunk(other.record);

		long i1 = record + CHARS;
		long i2 = other.record + CHARS;
		long n1 = i1 + chunk1.getInt(record + LENGTH) * 2;
		long n2 = i2 + chunk2.getInt(other.record + LENGTH) * 2;
		
		while (i1 < n1 && i2 < n2) {
			int cmp= compareChars(chunk1.getChar(i1), chunk2.getChar(i2), caseSensitive);
			if (cmp != 0)
				return cmp;
			
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
	
	public int compare(String other, boolean caseSensitive) throws CoreException {
		Chunk chunk = db.getChunk(record);
		
		long i1 = record + CHARS;
		int i2 = 0;
		long n1 = i1 + chunk.getInt(record + LENGTH) * 2;
		int n2 = other.length();
		
		while (i1 < n1 && i2 < n2) {
			int cmp= compareChars(chunk.getChar(i1), other.charAt(i2), caseSensitive);
			if (cmp != 0)
				return cmp;
			
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

	public int compareCompatibleWithIgnoreCase(IString string) throws CoreException {
		if (string instanceof ShortString)
			return compareCompatibleWithIgnoreCase((ShortString)string);
		else if (string instanceof LongString)
			return - ((LongString)string).compareCompatibleWithIgnoreCase(this);
		else
			throw new IllegalArgumentException();
	}
	
	public int compareCompatibleWithIgnoreCase(ShortString other) throws CoreException {
		Chunk chunk1 = db.getChunk(record);
		Chunk chunk2 = other.db.getChunk(other.record);

		long i1 = record + CHARS;
		long i2 = other.record + CHARS;
		long n1 = i1 + chunk1.getInt(record + LENGTH) * 2;
		long n2 = i2 + chunk2.getInt(other.record + LENGTH) * 2;
		int sensitiveCmp= 0;
		while (i1 < n1 && i2 < n2) {
			final char c1= chunk1.getChar(i1);
			final char c2= chunk2.getChar(i2);
			if (c1 != c2) {
				int cmp= compareChars(c1, c2, false); // insensitive
				if (cmp != 0)
					return cmp;
				
				if (sensitiveCmp == 0) {
					if (c1 < c2) {
						sensitiveCmp= -1;
					}
					else {
						sensitiveCmp= 1;
					}
				}
			}
			
			i1 += 2;
			i2 += 2;
		}
		
		if (i1 == n1 && i2 != n2)
			return -1;
		else if (i2 == n2 && i1 != n1)
			return 1;

		return sensitiveCmp;
	}

	public int compareCompatibleWithIgnoreCase(char[] chars) throws CoreException {
		Chunk chunk1 = db.getChunk(record);

		long i1 = record + CHARS;
		int i2 = 0;
		long n1 = i1 + chunk1.getInt(record + LENGTH) * 2;
		int n2 = chars.length;
		int sensitiveCmp= 0;
		while (i1 < n1 && i2 < n2) {
			final char c1= chunk1.getChar(i1);
			final char c2= chars[i2];
			if (c1 != c2) {
				int cmp= compareChars(c1, c2, false); // insensitive
				if (cmp != 0)
					return cmp;
				
				if (sensitiveCmp == 0) {
					if (c1 < c2) {
						sensitiveCmp= -1;
					}
					else {
						sensitiveCmp= 1;
					}
				}
			}
			
			i1 += 2;
			i2++;
		}
		
		if (i1 == n1 && i2 != n2)
			return -1;
		else if (i2 == n2 && i1 != n1)
			return 1;

		return sensitiveCmp;
	}
	
	public int comparePrefix(char[] other, boolean caseSensitive) throws CoreException {
		Chunk chunk = db.getChunk(record);
		
		long i1 = record + CHARS;
		int i2 = 0;
		long n1 = i1 + chunk.getInt(record + LENGTH) * 2;
		int n2 = other.length;
		
		while (i1 < n1 && i2 < n2) {
			int cmp= compareChars(chunk.getChar(i1), other[i2], caseSensitive);
			if (cmp != 0)
				return cmp;
			
			i1 += 2;
			++i2;
		}

		if (i1 == n1 && i2 != n2)
			return -1;
		else
			return 0;
	}
	
	public char charAt(int i) throws CoreException {
		long ptr = record + CHARS + (i * 2);
		return db.getChar(ptr);
	}
	
	public int getLength() throws CoreException {
		return db.getInt(record + LENGTH);
	}
	
	/**
	 * Compare characters case-sensitively, or case-insensitively.
	 * 
	 * <b>Limitation</b> This only maps the range a-z,A-Z onto each other
	 * @param a a character
	 * @param b a character
	 * @param caseSensitive whether to compare case-sensitively
	 * @return 
	 * <ul>
	 * <li>-1 if a < b
	 * <li>0 if a == b
	 * <li>1 if a > b
	 * </ul>
	 */
	public static int compareChars(char a, char b, boolean caseSensitive) {
		if (caseSensitive) {
			if (a < b)
				return -1;
			if (a > b)
				return 1;
		} else {
			if (a != b) {
				a= a >= 'a' && a <='z' ? (char) (a - 32) : a;
				b= b >= 'a' && b <='z' ? (char) (b - 32) : b;
				if (a < b)
					return -1;
				if (a > b)
					return 1;
			}
		}
		return 0;
	}
	
/* TODO - this is more correct than the above implementation, but we need to
 * benchmark first.
 * 
 * public static int compareChars(char a, char b, boolean caseSensitive) {
		if (caseSensitive) {
			if (a < b)
				return -1;
			if (a > b)
				return 1;
		} else {
			if (a != b) {
				a = Character.toUpperCase(a);
				b = Character.toUpperCase(b);
				if (a != b) {
					a = Character.toLowerCase(a);
					b = Character.toLowerCase(b);
					if (a != b) {
						if (a < b)
							return -1;
						if (a > b)
							return 1;
					}
				}
			}
		}
		return 0;
	}
*/

	@Override
	public String toString() {
		try {
			return getString();
		} catch (CoreException e) {
			return super.toString();
		}
	}
}
