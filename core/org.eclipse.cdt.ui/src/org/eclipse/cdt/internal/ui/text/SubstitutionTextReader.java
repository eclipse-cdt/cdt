/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.io.IOException;
import java.io.Reader;

/**
 * Reads the text contents from a reader and computes for each character
 * a potential substitution. The substitution may eat more characters than
 * only the one passed into the computation routine.
 */
public abstract class SubstitutionTextReader extends SingleCharReader {

	protected static final String LINE_DELIM = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	private Reader fReader;
	private boolean fWasWhiteSpace;
	private int fCharAfterWhiteSpace;

	private boolean fReadFromBuffer;
	private StringBuilder fBuffer;
	private int fIndex;

	protected SubstitutionTextReader(Reader reader) {
		fReader = reader;
		fBuffer = new StringBuilder();
		fIndex = 0;
		fReadFromBuffer = false;
		fCharAfterWhiteSpace = -1;
		fWasWhiteSpace = true;
	}

	/**
	 * Implement to compute the substitution for the given character and
	 * if necessary subsequent characters. Use <code>nextChar</code>
	 * to read subsequent characters.
	 */
	protected abstract String computeSubstitution(int c) throws IOException;

	/**
	 * Returns the internal reader.
	 */
	protected Reader getReader() {
		return fReader;
	}

	/**
	 * Returns the next character.
	 */
	protected int nextChar() throws IOException {
		fReadFromBuffer = (fBuffer.length() > 0);
		if (fReadFromBuffer) {
			char ch = fBuffer.charAt(fIndex++);
			if (fIndex >= fBuffer.length()) {
				fBuffer.setLength(0);
				fIndex = 0;
			}
			return ch;
		}
		int ch = fCharAfterWhiteSpace;
		if (ch == -1) {
			ch = fReader.read();
		}
		if (Character.isWhitespace((char) ch)) {
			do {
				ch = fReader.read();
			} while (Character.isWhitespace((char) ch));
			if (ch != -1) {
				fCharAfterWhiteSpace = ch;
				return ' ';
			}
		} else {
			fCharAfterWhiteSpace = -1;
		}
		return ch;
	}

	/**
	 * @see Reader#read()
	 */
	@Override
	public int read() throws IOException {
		int c;
		do {

			c = nextChar();
			while (!fReadFromBuffer) {
				String s = computeSubstitution(c);
				if (s == null)
					break;
				if (s.length() > 0)
					fBuffer.insert(0, s);
				c = nextChar();
			}

		} while (fWasWhiteSpace && (c == ' '));

		fWasWhiteSpace = (c == ' ' || c == '\r' || c == '\n');
		return c;
	}

	/**
	 * @see Reader#ready()
	 */
	@Override
	public boolean ready() throws IOException {
		return fReader.ready();
	}

	/**
	 * @see Reader#close()
	 */
	@Override
	public void close() throws IOException {
		fReader.close();
	}

	/**
	 * @see Reader#reset()
	 */
	@Override
	public void reset() throws IOException {
		fReader.reset();
		fWasWhiteSpace = true;
		fCharAfterWhiteSpace = -1;
		fBuffer.setLength(0);
		fIndex = 0;
	}
}
