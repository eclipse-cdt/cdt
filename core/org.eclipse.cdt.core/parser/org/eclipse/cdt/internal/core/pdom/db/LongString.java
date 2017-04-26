/*******************************************************************************
 * Copyright (c) 2006, 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *     Patrick Koenemann (itemis)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

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

	// Additional fields of first record.
	private static final int LENGTH = 0; // Must be first to match ShortString.
	private static final int NEXT1 = 4;
	private static final int CHARS1 = 8;
	
	private static final int NUM_CHARS1 = (Database.MAX_MALLOC_SIZE - CHARS1) / 2;
	
	// Additional fields of subsequent records.
	private static final int NEXTN = 0;
	private static final int CHARSN = 4;
	
	private static final int NUM_CHARSN = (Database.MAX_MALLOC_SIZE - CHARSN) / 2;
	
	public LongString(Database db, long record) {
		this.db = db;
		this.record = record;
	}
	
	public LongString(Database db, final char[] chars, boolean useBytes) throws CoreException {
		final int numChars1 = useBytes ? NUM_CHARS1 * 2 : NUM_CHARS1;
		final int numCharsn = useBytes ? NUM_CHARSN * 2 : NUM_CHARSN;

		this.db = db;
		this.record = db.malloc(Database.MAX_MALLOC_SIZE);

		// Write the first record.
		final int length = chars.length;
		db.putInt(this.record, useBytes ? -length : length);
		Chunk chunk= db.getChunk(this.record);
		
		if (useBytes) {
			chunk.putCharsAsBytes(this.record + CHARS1, chars, 0, numChars1);
		} else {
			chunk.putChars(this.record + CHARS1, chars, 0, numChars1);
		}
		
		// Write the subsequent records.
		long lastNext = this.record + NEXT1;
		int start = numChars1;
		while (length - start > numCharsn) {
			long nextRecord = db.malloc(Database.MAX_MALLOC_SIZE);
			db.putRecPtr(lastNext, nextRecord);
			chunk= db.getChunk(nextRecord);
			if (useBytes) {
				chunk.putCharsAsBytes(nextRecord + CHARSN, chars, start, numCharsn);
			} else {
				chunk.putChars(nextRecord + CHARSN, chars, start, numCharsn);
			}
			start += numCharsn;
			lastNext = nextRecord + NEXTN;
		}
		
		// Write the last record.
		int remaining= length - start;
		long nextRecord = db.malloc(CHARSN + (useBytes ? remaining : remaining * 2));
		db.putRecPtr(lastNext, nextRecord);
		chunk= db.getChunk(nextRecord);
		if (useBytes) {
			chunk.putCharsAsBytes(nextRecord + CHARSN, chars, start, remaining);
		} else {
			chunk.putChars(nextRecord + CHARSN, chars, start, remaining);
		}
	}
	
	@Override
	public long getRecord() {
		return record;
	}

	@Override
	public char[] getChars() throws CoreException {
		int length = db.getInt(record + LENGTH);
		final boolean useBytes = length < 0;
		int numChars1 = NUM_CHARS1;
		int numCharsn = NUM_CHARSN;
		if (useBytes) {
			length= -length;
			numChars1 *= 2;
			numCharsn *= 2;
		}

		final char[] chars = new char[length];

		// First record
		long p = record;
		getCharsFromChunk(db, p, CHARS1, 0, numChars1, useBytes, chars);

		int start= numChars1;
		p= record + NEXT1;

		// Other records
		while (start < length) {
			p = db.getRecPtr(p);
			int partLen= Math.min(length - start, numCharsn);
			getCharsFromChunk(db, p, CHARSN, start, partLen, useBytes, chars);
			start += partLen;
			p= p + NEXTN;
		}
		return chars;
	}

	private static void getCharsFromChunk(Database db, long record, int key, int start, int length,
			boolean useBytes, char[] chars) throws CoreException {
		final Chunk chunk= db.getChunk(record);
		if (useBytes) {
			chunk.getCharsFromBytes(record + key, chars, start, length);
		} else {
			chunk.getChars(record + key, chars, start, length);
		}
	}

	@Override
	public void delete() throws CoreException {
		int length = db.getInt(record + LENGTH);
		final boolean useBytes = length < 0;
		int numChars1 = NUM_CHARS1;
		int numCharsn = NUM_CHARSN;
		if (useBytes) {
			length= -length;
			numChars1 *= 2;
			numCharsn *= 2;
		}
		long nextRecord = db.getRecPtr(record + NEXT1);
		db.free(record);
		length -= numChars1;
		
		// Middle records.
		while (length > numCharsn) {
			length -= numCharsn;
			long nextnext = db.getRecPtr(nextRecord + NEXTN);
			db.free(nextRecord);
			nextRecord = nextnext;
		}
		
		// Last record.
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
					h = 31 * h + chars[i];
				}
			} catch (CoreException e) {
			}
			hash = h;
		}
		return h;
	}
	
	@Override
	public int compare(IString other, boolean caseSensitive) throws CoreException {
		if (!(other instanceof LongString))
			return compare(other.getChars(), caseSensitive);

		/*
		 * In this case, we have two pointers to the actual data;
		 * compare them chunk by chunk to avoid loading all chunks.
		 */
		int length = db.getInt(record + LENGTH);
		final boolean useBytes = length < 0;
		int numChars1 = NUM_CHARS1;
		int numCharsn = NUM_CHARSN;
		if (useBytes) {
			length= -length;
			numChars1 *= 2;
			numCharsn *= 2;
		}
		// get the same information for the other
		final Database otherDb = ((LongString)other).db;
		int otherLength = otherDb.getInt(other.getRecord() + LENGTH);
		final boolean otherUseBytes = otherLength < 0;
		int otherNumChars1 = NUM_CHARS1;
		int otherNumCharsn = NUM_CHARSN;
		if (otherUseBytes) {
			otherLength= -otherLength;
			otherNumChars1 *= 2;
			otherNumCharsn *= 2;
		}

		final int n = Math.min(length, otherLength);
		final char[] chars = new char[Math.max(numChars1, numCharsn)];
		final char[] otherChars = new char[Math.max(otherNumChars1, otherNumCharsn)];

		// First records
		long p = record;
		getCharsFromChunk(db, p, CHARS1, 0, numChars1, useBytes, chars);
		long otherP = other.getRecord();
		getCharsFromChunk(otherDb, otherP, CHARS1, 0, otherNumChars1, otherUseBytes, otherChars);

		// compare first chunk pair
		int max = Math.min(numChars1, otherNumChars1);
		for (int i = 0; i < max; i++) {
			int cmp= ShortString.compareChars(chars[i], otherChars[i], caseSensitive);
			if (cmp != 0)
				return cmp;
		}

		int start= numChars1;
		p= record + NEXT1;
		otherP= other.getRecord() + NEXT1;

		// Other records
		while (start < n) {
			p = db.getRecPtr(p);
			otherP = otherDb.getRecPtr(otherP);
			int partLen= Math.min(length - start, numCharsn);
			int otherPartLen= Math.min(otherLength - start, otherNumCharsn);
			getCharsFromChunk(db, p, CHARSN, 0, partLen, useBytes, chars);
			getCharsFromChunk(otherDb, otherP, CHARSN, 0, otherPartLen, otherUseBytes, otherChars);

			// compare next chunk pair
			max = Math.min(partLen, otherPartLen);
			for (int i = 0; i < max; i++) {
				int cmp= ShortString.compareChars(chars[i], otherChars[i], caseSensitive);
				if (cmp != 0)
					return cmp;
			}

			start += partLen;
			p= p + NEXTN;
		}

		return length - otherLength;
	}

	@Override
	public int compare(String other, boolean caseSensitive) throws CoreException {
		return compare(other.toCharArray(), caseSensitive);
	}

	@Override
	public int compare(char[] other, boolean caseSensitive) throws CoreException {
		/*
		 * In this case, we have a pointer to the data and another char array;
		 * compare the char array chunk by chunk to avoid loading all chunks.
		 */
		int length = db.getInt(record + LENGTH);
		final boolean useBytes = length < 0;
		int numChars1 = NUM_CHARS1;
		int numCharsn = NUM_CHARSN;
		if (useBytes) {
			length= -length;
			numChars1 *= 2;
			numCharsn *= 2;
		}

		final int n = Math.min(length, other.length);
		final char[] chars = new char[Math.max(numChars1, numCharsn)];

		// First record
		long p = record;
		getCharsFromChunk(db, p, CHARS1, 0, numChars1, useBytes, chars);

		// compare first chunk
		int max = Math.min(numChars1, n);
		for (int i = 0; i < max; i++) {
			int cmp= ShortString.compareChars(chars[i], other[i], caseSensitive);
			if (cmp != 0)
				return cmp;
		}

		int start= numChars1;
		p= record + NEXT1;

		// Other records
		while (start < n) {
			p = db.getRecPtr(p);
			int partLen= Math.min(length - start, numCharsn);
			getCharsFromChunk(db, p, CHARSN, 0, partLen, useBytes, chars);

			// compare next chunk
			max = Math.min(partLen, n - start);
			for (int i = 0; i < max; i++) {
				int cmp= ShortString.compareChars(chars[i], other[start + i], caseSensitive);
				if (cmp != 0)
					return cmp;
			}

			start += partLen;
			p= p + NEXTN;
		}

		return length - other.length;
	}
	
	@Override
	public int compareCompatibleWithIgnoreCase(IString string) throws CoreException {
		return ShortString.compareCompatibleWithIgnoreCase(getChars(), string.getChars());
	}
	
	@Override
	public int comparePrefix(char[] other, boolean caseSensitive) throws CoreException {
		return ShortString.comparePrefix(getChars(), other, caseSensitive);
	}

	@Override
	public String getString() throws CoreException {
		return new String(getChars());
	}

	@Override
	public int compareCompatibleWithIgnoreCase(char[] other) throws CoreException {
		return ShortString.compareCompatibleWithIgnoreCase(getChars(), other);
	}
}
