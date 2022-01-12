/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text;

import java.io.IOException;
import java.nio.CharBuffer;

/**
 * StringRider
 */
public class StringRider implements IFileRider {

	CharBuffer fBuffer;

	public StringRider(CharSequence text) {
		super();
		// create a readonly buffer
		fBuffer = CharBuffer.wrap(text);
	}

	public StringRider(CharBuffer buffer) {
		super();
		fBuffer = buffer;
	}

	@Override
	public void seek(int pos) throws IOException {
		fBuffer.position(pos);
	}

	@Override
	public void writeChar(char c) throws IOException {
		fBuffer.put(c);
	}

	@Override
	public void writeChars(char[] buf) throws IOException {
		fBuffer.put(buf);
	}

	@Override
	public void writeChars(char[] buf, int n) throws IOException {
		fBuffer.put(buf, 0, n);
	}

	@Override
	public void writeChars(char[] buf, int off, int n) throws IOException {
		fBuffer.put(buf, off, n);
	}

	@Override
	public void writeChars(String buf, int off, int n) throws IOException {
		fBuffer.put(buf, off, off + n);
	}

	@Override
	public char readChar() throws IOException {
		return fBuffer.get();
	}

	@Override
	public void readChars(char[] buf) throws IOException {
		fBuffer.get(buf, 0, buf.length);
	}

	@Override
	public void readChars(char[] buf, int n) throws IOException {
		fBuffer.get(buf, 0, n);
	}

	@Override
	public void readChars(char[] buf, int off, int n) throws IOException {
		fBuffer.get(buf, off, n);
	}

	@Override
	public void readChars(StringBuffer buf, int n) throws IOException {
		int pos = fBuffer.position();
		if (fBuffer.hasArray()) {
			buf.append(fBuffer.array(), fBuffer.arrayOffset() + pos, n);
		} else {
			fBuffer.limit(pos + n);
			String str = fBuffer.toString();
			assert str.length() == n;
			buf.append(str);
			fBuffer.limit(fBuffer.capacity());
		}
		fBuffer.position(pos + n);
	}

	@Override
	public int length() {
		return fBuffer.length();
	}

	@Override
	public int limit() {
		return fBuffer.limit();
	}

	@Override
	public boolean isReadonly() {
		return fBuffer.isReadOnly();
	}

}
