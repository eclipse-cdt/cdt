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

import java.util.NoSuchElementException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * This is for strings that take up more than on chunk.
 * The string will need to be broken up into sections and then
 * reassembled when necessary.
 * 
 * @author Doug Schaefer
 */
public class LongString implements IString {
	private final Database db;
	private final long record;
	private int hash;

	// Additional fields of first record
	private static final int LENGTH = 0; // must be first to match ShortString
	private static final int NEXT1 = 4;
	private static final int CHARS1 = 8;
	
	private static final int NUM_CHARS1 = (Database.MAX_MALLOC_SIZE - CHARS1) / 2;
	
	// Additional fields of subsequent records
	private static final int NEXTN = 0;
	private static final int CHARSN = 4;
	
	private static final int NUM_CHARSN = (Database.MAX_MALLOC_SIZE - CHARSN) / 2;
	
	public LongString(Database db, long record) {
		this.db = db;
		this.record = record;
	}
	
	private interface IWriter {
		public void writeChars(int start, int length, long p) throws CoreException;
	}

	private long createString(int length, IWriter writer) throws CoreException {
		// write the first record
		long firstRecord = db.malloc(Database.MAX_MALLOC_SIZE);
		int start = 0;
		db.putInt(firstRecord, length);
		writer.writeChars(start, NUM_CHARS1, firstRecord + CHARS1);
		
		// write the subsequent records
		long lastNext = firstRecord + NEXT1;
		start += NUM_CHARS1;
		while (length - start > NUM_CHARSN) {
			long nextRecord = db.malloc(Database.MAX_MALLOC_SIZE);
			db.putRecPtr(lastNext, nextRecord);
			writer.writeChars(start, NUM_CHARSN, nextRecord + CHARSN);
			start += NUM_CHARSN;
			lastNext = nextRecord + NEXTN;
		}
		
		// Write the final record
		length -= start;
		long finalRecord = db.malloc(CHARSN + (length) * 2);
		db.putRecPtr(lastNext, finalRecord);
		writer.writeChars(start, length, finalRecord + CHARSN);
		
		return firstRecord;
	}
	
	public LongString(Database db, final String string) throws CoreException {
		this.db = db;
		this.record = createString(string.length(), new IWriter() {
			public void writeChars(int start, int length, long p) throws CoreException {
				for (int i = start; i < start + length; ++i) {
					LongString.this.db.putChar(p, string.charAt(i));
					p += 2;
				}
			}
		});
	}
	
	public LongString(Database db, final char[] chars) throws CoreException {
		this.db = db;
		this.record = createString(chars.length, new IWriter() {
			public void writeChars(int start, int length, long p) throws CoreException {
				for (int i = start; i < start + length; ++i) {
					LongString.this.db.putChar(p, chars[i]);
					p += 2;
				}
			}
		});
	}
	
	public long getRecord() {
		return record;
	}

	public void delete() throws CoreException {
		int length = db.getInt(record + LENGTH) - NUM_CHARS1;
		long nextRecord = db.getRecPtr(record + NEXT1);
		db.free(record);
		
		// Middle records
		while (length > NUM_CHARSN) {
			length -= NUM_CHARSN;
			long nextnext = db.getRecPtr(nextRecord + NEXTN);
			db.free(nextRecord);
			nextRecord = nextnext;
		}
		
		// Last record
		db.free(nextRecord);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		try {
			if (obj instanceof LongString) {
				LongString lstr = (LongString)obj;
				if (db == lstr.db && record == lstr.record)
					return true;
				return compare(lstr, true) == 0;
			}
			if (obj instanceof char[]) {
				return compare((char[]) obj, true) == 0;
			}
			if (obj instanceof String) {
				return compare((String) obj, true) == 0;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return false;
	}
	
	
	private class HashCodeComputer implements IReader {
		private int fHashcode= 0;

		public int getHashcode() {
			return fHashcode;
		}

		public void appendChar(char c) {
			fHashcode = 31*fHashcode + c;
		}
	}
	
	/**
	 * Compatible with {@link String#hashCode()}
	 */
	@Override
	public int hashCode() {
		int h = hash;
		if (h == 0) {
			HashCodeComputer hcc;
			try {
				int length = db.getInt(record + LENGTH);
				hcc = new HashCodeComputer();
				readChars(length, hcc);
				h= hcc.getHashcode();
				hash = h;
			} catch (CoreException e) {
			}
		}
		return h;
	}
	
	public int compare(IString string, boolean caseSensitive) throws CoreException {
		if (string instanceof LongString)
			return compare((LongString)string, caseSensitive);
		else if (string instanceof ShortString)
			return compare((ShortString)string, caseSensitive);
		else
			throw new IllegalArgumentException();
	}

	public int compare(char[] other, boolean caseSensitive) throws CoreException {
		CharIterator i1 = new CharIterator();
		int i2 = 0;
		int n2 = other.length;
		
		while (i1.hasNext() && i2 < n2) {
			int cmp= ShortString.compareChars(i1.next(), other[i2], caseSensitive);
			if(cmp!=0)
				return cmp;
			
			++i2;
		}

		if (!i1.hasNext() && i2 != n2)
			return -1;
		else if (i2 == n2 && i1.hasNext())
			return 1;
		else
			return 0;
	}
	
	public int compare(ShortString other, boolean caseSensitive) throws CoreException {
		CharIterator i1 = new CharIterator();
		int index2 = 0;
		int length2 = other.getLength();
		
		while (i1.hasNext() && index2<length2) {
			int cmp= ShortString.compareChars(i1.next(), other.charAt(index2), caseSensitive);
			if(cmp!=0)
				return cmp;
			index2++;
		}

		if (!i1.hasNext() && index2 != length2)
			return -1;
		else if (index2 == length2 && i1.hasNext())
			return 1;
		else
			return 0;
	}
	
	public int compare(LongString other, boolean caseSensitive) throws CoreException {
		CharIterator i1 = new CharIterator();
		CharIterator i2 = other.new CharIterator();
		
		while (i1.hasNext() && i2.hasNext()) {
			int cmp= ShortString.compareChars(i1.next(), i2.next(), caseSensitive);
			if(cmp!=0)
				return cmp;
		}

		if (!i1.hasNext() && i2.hasNext())
			return -1;
		else if (!i2.hasNext() && i1.hasNext())
			return 1;
		else
			return 0;
	}
	
	public int compare(String other, boolean caseSensitive) throws CoreException {
		CharIterator i1 = new CharIterator();
		int i2 = 0;
		int n2 = other.length();
		
		while (i1.hasNext() && i2 < n2) {
			int cmp= ShortString.compareChars(i1.next(), other.charAt(i2), caseSensitive);
			if(cmp!=0)
				return cmp;
			
			++i2;
		}

		if (!i1.hasNext() && i2 != n2)
			return -1;
		else if (i2 == n2 && i1.hasNext())
			return 1;
		else
			return 0;
	}

	public int comparePrefix(char[] other, boolean caseSensitive) throws CoreException {
		CharIterator i1 = new CharIterator();
		int i2 = 0;
		int n2 = other.length;
		
		while (i1.hasNext() && i2 < n2) {
			int cmp= ShortString.compareChars(i1.next(), other[i2], caseSensitive);
			if(cmp!=0)
				return cmp;
			
			++i2;
		}

		if (!i1.hasNext() && i2 != n2)
			return -1;
		else
			return 0;
	}

	private interface IReader {
		public void appendChar(char c);
	}
	
	private void readChars(int length, IReader reader) throws CoreException {
		// First record
		long p = record + CHARS1;
		for (int i = 0; i < NUM_CHARS1; ++i) {
			reader.appendChar(db.getChar(p));
			p += 2;
		}
		length -= NUM_CHARS1;
		long nextRecord = db.getRecPtr(record + NEXT1);
		
		// Middle records
		while (length > NUM_CHARSN) {
			p = nextRecord + CHARSN;
			for (int i = 0; i < NUM_CHARSN; ++i) {
				reader.appendChar(db.getChar(p));
				p += 2;
			}
			length -= NUM_CHARSN;
			nextRecord = db.getRecPtr(nextRecord + NEXTN);
		}
		
		// Last record
		p = nextRecord + CHARSN;
		for (int i = 0; i < length; ++i) {
			reader.appendChar(db.getChar(p));
			p += 2;
		}
	}
	
	/**
     * Convenience class for sequential access to LongString characters
     */
	private class CharIterator {
		long p;
		int count;
		int length;
		
		public CharIterator() throws CoreException {
			p = record + CHARS1;
			length =  db.getInt(record + LENGTH);
		}
		
		public char next() throws CoreException {
			char result = db.getChar(p);
			p += 2;
			count++;
			if(count>length) {
				throw new NoSuchElementException();
			}
			if(count == NUM_CHARS1) {
				p = db.getRecPtr(record + NEXT1) + CHARSN;
			}
			if(count > NUM_CHARS1 && ((count-NUM_CHARS1) % NUM_CHARSN)==0) {
				p = db.getRecPtr(p-(NUM_CHARSN*2)-4) + CHARSN;
			}
			return result;
		}
		
		public boolean hasNext() {
			return count<length;
		}
	}
	
	public char[] getChars() throws CoreException {
		int length = db.getInt(record + LENGTH);
		final char[] chars = new char[length];
		readChars(length, new IReader() {
			int cp = 0;
			public void appendChar(char c) {
				chars[cp++] = c;
			}
		});
		return chars;
	}

	public String getString() throws CoreException {
		int length = db.getInt(record + LENGTH);
		final StringBuilder buffer = new StringBuilder(length);
		readChars(length, new IReader() {
			public void appendChar(char c) {
				buffer.append(c);
			}
		});
		return buffer.toString();
	}

	public int compareCompatibleWithIgnoreCase(IString string) throws CoreException {
		int cmp= compare(string, false);
		return cmp==0 ? compare(string, true) : cmp;	
	}

	public int compareCompatibleWithIgnoreCase(char[] chars) throws CoreException {
		int cmp= compare(chars, false);
		return cmp==0 ? compare(chars, true) : cmp;	
	}
}
