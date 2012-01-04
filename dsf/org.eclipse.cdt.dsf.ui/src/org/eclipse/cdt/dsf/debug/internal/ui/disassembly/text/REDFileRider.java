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
 * Accessor to <code>REDFile</code>s.
 */
public final class REDFileRider implements IFileRider {

	private REDFile fFile;
	private int fLimit = Integer.MAX_VALUE;
	private int fResult;
	private boolean fEof;
	private char[] fOneCharBuf = new char[1];

	/** @pre f != null */
	public REDFileRider(REDFile f) throws IOException {
		set(f, 0);
	}
	public REDFileRider(REDFile f, int limit) throws IOException {
		fLimit = limit;
		set(f, 0);
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text.IFileRider#seek(int)
	 */
	@Override
	public void seek(int pos) throws IOException {
		fFile.seek(pos);
		fEof = false;
		fResult = 0;
	}

	/**
	 * Set rider to file and position
	 * 
	 * @param f    the file the rider should operate on
	 * @param pos  is normalized to be in range [0, f.length()]
	 * @pre f != null
	 * @pre pos >= 0 && pos <= f.length()
	 * @post fBuffer != null
	 */
	private void set(REDFile f, int pos) throws IOException {

		assert f != null;
		assert pos >= 0 && pos <= f.length();

		fFile = f;
		fFile.seek(pos);
		fEof = false;
		fResult = 0;
	}

	/**
	 * Get end of file status
	 * @return true, if rider has tried to read beyond the end of file
	 */
	public boolean eof() {
		return fEof;
	}

	/**
	 * Get result of last operation
	 * will be 0 after successful write operation
	 * will contain nr. of characters requested but unavailable read after read operation
	 */
	public int getResult() {
		return fResult;
	}

	/**
	 * Get the REDFile the rider operates on.
	 * @post return != null
	 */
	public REDFile getFile() {
		return fFile;
	}

	@Override
	public void writeChar(char c) throws IOException {
		fOneCharBuf[0] = c;
		writeChars(fOneCharBuf, 0, 1);
	}

	@Override
	public void writeChars(char[] buf) throws IOException {
		writeChars(buf, 0, buf.length);
	}

	@Override
	public void writeChars(char[] buf, int n) throws IOException {
		writeChars(buf, 0, n);
	}

	@Override
	public void writeChars(char[] buf, int off, int n) throws IOException {
		fFile.writeBuffered(buf, off, n);
		fResult = 0;
	}

	@Override
	public void writeChars(String buf, int off, int n) throws IOException {
		fFile.writeBuffered(buf, off, n);
		fResult = 0;
	}

	@Override
	public char readChar() throws IOException {
		readChars(fOneCharBuf, 0, 1);
		return fEof ? '\0' : fOneCharBuf[0];
	}

	@Override
	public void readChars(char[] buf) throws IOException {
		readChars(buf, 0, buf.length);
	}

	@Override
	public void readChars(char[] buf, int n) throws IOException {
		readChars(buf, 0, n);
	}

	@Override
	public void readChars(char[] buf, int off, int n) throws IOException {
		int count = fFile.readBuffered(buf, off, n);
		fResult = n-count;
		fEof = fResult > 0;
	}

	@Override
	public void readChars(StringBuffer buf, int n) throws IOException {
		int count = fFile.readBuffered(buf, n);
		fResult = n-count;
		fEof = fResult > 0;
	}

	@Override
	public int length() {
		return fFile.length();
	}

	@Override
	public int limit() {
		return fLimit;
	}

	@Override
	public boolean isReadonly() {
		return fFile.isReadonly();
	}

}
