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

import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
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
	private final int record1;

	// Additional fields of first record
	private static final int LENGTH = 0; // must be first to match ShortString
	private static final int NEXT1 = 4;
	private static final int CHARS1 = 8;
	
	private static final int NUM_CHARS1 = (Database.MAX_SIZE - CHARS1) / 2;
	
	// Additional fields of subsequent records
	private static final int NEXTN = 0;
	private static final int CHARSN = 4;
	
	private static final int NUM_CHARSN = (Database.MAX_SIZE - CHARSN) / 2;
	
	public LongString(Database db, int record1) {
		this.db = db;
		this.record1 = record1;
	}
	
	private interface IWriter {
		public void writeChars(int start, int length, int p) throws CoreException;
	}

	private int createString(int length, IWriter writer) throws CoreException {
		// write the first record
		int firstRecord = db.malloc(Database.MAX_SIZE);
		int start = 0;
		db.putInt(firstRecord, length);
		writer.writeChars(start, NUM_CHARS1, firstRecord + CHARS1);
		
		// write the subsequent records
		int lastNext = firstRecord + NEXT1;
		start += NUM_CHARS1;
		while (length - start > NUM_CHARSN) {
			int nextRecord = db.malloc(Database.MAX_SIZE);
			db.putInt(lastNext, nextRecord);
			writer.writeChars(start, NUM_CHARSN, nextRecord + CHARSN);
			start += NUM_CHARSN;
			lastNext = nextRecord + NEXTN;
		}
		
		// Write the final record
		length -= start;
		int finalRecord = db.malloc(CHARSN + (length) * 2);
		db.putInt(lastNext, finalRecord);
		writer.writeChars(start, length, finalRecord + CHARSN);
		
		return firstRecord;
	}
	
	public LongString(Database db, final String string) throws CoreException {
		this.db = db;
		this.record1 = createString(string.length(), new IWriter() {
			public void writeChars(int start, int length, int p) throws CoreException {
				for (int i = start; i < start + length; ++i) {
					LongString.this.db.putChar(p, string.charAt(i));
					p += 2;
				}
			}
		});
	}
	
	public LongString(Database db, final char[] chars) throws CoreException {
		this.db = db;
		this.record1 = createString(chars.length, new IWriter() {
			public void writeChars(int start, int length, int p) throws CoreException {
				for (int i = start; i < start + length; ++i) {
					LongString.this.db.putChar(p, chars[i]);
					p += 2;
				}
			}
		});
	}
	
	public int getRecord() {
		return record1;
	}

	public void delete() throws CoreException {
		int length = db.getInt(record1 + LENGTH) - NUM_CHARS1;
		int nextRecord = db.getInt(record1 + NEXT1);
		db.free(record1);
		
		// Middle records
		while (length > NUM_CHARSN) {
			length -= NUM_CHARSN;
			int nextnext = db.getInt(nextRecord + NEXTN);
			db.free(nextRecord);
			nextRecord = nextnext;
		}
		
		// Last record
		db.free(nextRecord);
	}
	
	public boolean equals(Object obj) {
		throw new PDOMNotImplementedError();
	}
	
	public int hashCode() {
		return record1;
	}
	
	public int compare(IString string) throws CoreException {
		if (string instanceof LongString)
			return compare((LongString)string);
		else if (string instanceof ShortString)
			return compare((ShortString)string);
		else
			throw new IllegalArgumentException();
	}

	public int compare(LongString string) throws CoreException {
		throw new PDOMNotImplementedError();
	}
	
	public int compare(ShortString string) throws CoreException {
		throw new PDOMNotImplementedError();
	}
	
	public int compare(String string) throws CoreException {
		throw new PDOMNotImplementedError();
	}

	public int compare(char[] chars) throws CoreException {
		throw new PDOMNotImplementedError();
	}

	private interface IReader {
		public void appendChar(char c);
	}
	
	private void readChars(int length, IReader reader) throws CoreException {
		// First record
		int p = record1 + CHARS1;
		for (int i = 0; i < NUM_CHARS1; ++i) {
			reader.appendChar(db.getChar(p));
			p += 2;
		}
		length -= NUM_CHARS1;
		int nextRecord = db.getInt(record1 + NEXT1);
		
		// Middle records
		while (length > NUM_CHARSN) {
			p = nextRecord + CHARSN;
			for (int i = 0; i < NUM_CHARSN; ++i) {
				reader.appendChar(db.getChar(p));
				p += 2;
			}
			length -= NUM_CHARSN;
			nextRecord = db.getInt(nextRecord + NEXTN);
		}
		
		// Last record
		p = nextRecord + CHARSN;
		for (int i = 0; i < length; ++i) {
			reader.appendChar(db.getChar(p));
			p += 2;
		}
	}
	
	public char[] getChars() throws CoreException {
		int length = db.getInt(record1 + LENGTH);
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
		int length = db.getInt(record1 + LENGTH);
		final StringBuffer buffer = new StringBuffer(length);
		readChars(length, new IReader() {
			public void appendChar(char c) {
				buffer.append(c);
			}
		});
		return buffer.toString();
	}

}
