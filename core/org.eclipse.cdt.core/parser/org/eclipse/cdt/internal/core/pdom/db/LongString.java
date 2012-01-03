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
	
	public LongString(Database db, final char[] chars, boolean useBytes) throws CoreException {
		final int numChars1 = useBytes ? NUM_CHARS1*2 : NUM_CHARS1;
		final int numCharsn = useBytes ? NUM_CHARSN*2 : NUM_CHARSN;

		this.db = db;
		this.record = db.malloc(Database.MAX_MALLOC_SIZE);

		// Write the first record
		final int length = chars.length;
		db.putInt(this.record, useBytes ? -length : length);
		Chunk chunk= db.getChunk(this.record);
		
		if (useBytes) {
			chunk.putCharsAsBytes(this.record + CHARS1, chars, 0, numChars1);
		} else {
			chunk.putChars(this.record + CHARS1, chars, 0, numChars1);
		}
		
		// write the subsequent records
		long lastNext = this.record + NEXT1;
		int start = numChars1;
		while (length-start > numCharsn) {
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
		
		// Write last record
		int remaining= length - start;
		long nextRecord = db.malloc(CHARSN + (useBytes ? remaining : remaining*2));
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
		Chunk chunk= db.getChunk(p);
		if (useBytes) {
			chunk.getCharsFromBytes(p+CHARS1, chars, 0, numChars1);
		} else {
			chunk.getChars(p+CHARS1, chars, 0, numChars1);
		}
		
		int start= numChars1;
		p= record + NEXT1;
				
		// Other records
		while (start < length) {
			p = db.getRecPtr(p);
			int partLen= Math.min(length-start, numCharsn);
			chunk= db.getChunk(p);
			if (useBytes) {
				chunk.getCharsFromBytes(p+CHARSN, chars, start, partLen);
			} else {
				chunk.getChars(p+CHARSN, chars, start, partLen);
			}
			start+= partLen;
			p=p+NEXTN;
		}
		return chars;
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
		length-= numChars1;
		
		// Middle records
		while (length > numCharsn) {
			length -= numCharsn;
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
	
	@Override
	public int compare(IString string, boolean caseSensitive) throws CoreException {
		return ShortString.compare(getChars(), string.getChars(), caseSensitive);
	}
		
	@Override
	public int compare(String other, boolean caseSensitive) throws CoreException {
		return ShortString.compare(getChars(), other.toCharArray(), caseSensitive);
	}

	@Override
	public int compare(char[] other, boolean caseSensitive) throws CoreException {
		return ShortString.compare(getChars(), other, caseSensitive);
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
