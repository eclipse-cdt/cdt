/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text;

import java.io.IOException;

/**
 * A piece of text on a scratch file.
 */
public class REDRun implements CharSequence {

	IFileRider fRider;
	int fOffset;
	int fLength;

	/**
	 * @pre rider != null
	 * @pre style != null
	 * @pre length > 0
	 */
	public REDRun(IFileRider rider, int offset, int length) {
		fRider = rider;
		fOffset = offset;
		fLength = length;
	}

	/**
	 * @pre rider != null
	 * @pre style != null
	 * @pre str.length() > 0
	 */
	public REDRun(IFileRider rider, String str) throws IOException {
		fRider = rider;
		fLength = str.length();
		fOffset = fRider.length();
		fRider.seek(fOffset);
		fRider.writeChars(str, 0, fLength);
	}

	/**
	 * @param rider
	 * @param buf
	 * @param off
	 * @param n
	 */
	public REDRun(IFileRider rider, char[] buf, int off, int n) throws IOException {
		fRider = rider;
		fLength = n;
		fOffset = fRider.length();
		fRider.seek(fOffset);
		fRider.writeChars(buf, off, n);
	}

	/**
	 * @post return.length() == length()
	 */
	public String asString() throws IOException {
		String retVal;
		char[] buf = new char[fLength];
		fRider.seek(fOffset);
		fRider.readChars(buf);
		retVal = new String(buf);
		return retVal;
	}

	/**
	 * Copy parts of run into char-array
	 * @param arr array to copy into
	 * @param from offset of arr to copy bytes to
	 * @param arrSize max offset of arr to write into
	 * @param myOff offset of run to start reading at
	 * @return the number of bytes copied
	 */
	public int copyInto(char[] arr, int from, int arrSize, int myOff) throws IOException {
		fRider.seek(fOffset + myOff);
		int readAmount = Math.min(arrSize - from, fLength - myOff);
		fRider.readChars(arr, from, readAmount);
		return readAmount;
	}

	/**
	 * Append parts of run to a StringBuffer
	 * @param buffer StringBuffer to append to
	 * @param length number of characters to append
	 * @param myOff offset of run to start reading at
	 * @return the number of bytes appended
	 */
	public int appendTo(StringBuffer buffer, int length, int myOff) throws IOException {
		fRider.seek(fOffset + myOff);
		int readAmount = Math.min(length, fLength - myOff);
		fRider.readChars(buffer, readAmount);
		return readAmount;
	}

	/**
	 * A run is mergable with another if the other a direct successor in the scratch file.
	 * @pre r != null
	 */
	public boolean isMergeableWith(REDRun r) {
		return r.fRider == fRider && r.fOffset == fOffset + fLength;
	}

	
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			return asString();
		} catch (IOException e) {
			return null;
		}
	}

	/*
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int pos) {
		try {
			fRider.seek(fOffset + pos);
			return fRider.readChar();
		} catch (IOException e) {
			return 0;
		}
	}

	/*
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
		return new REDRun(fRider, fOffset + start, end - start);
	}

	/*
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return fLength;
	}
}
