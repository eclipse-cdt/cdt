/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Anton Leherbauer - adding tokens for preprocessing directives
 *     Markus Schorn - classification of preprocessing directives.
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter.scanner;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

public class ScannerContext {
	private Reader fReader;
	private int fOffset;
	private Stack<Integer> fUndo = new Stack<>();

	public ScannerContext() {
	}

	public ScannerContext initialize(Reader r) {
		fReader = r;
		fOffset = 0;
		return this;
	}

	public ScannerContext initialize(Reader r, int offset) {
		try {
			r.skip(offset);
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
		fReader = r;
		fOffset = offset;
		return this;
	}

	public int read() throws IOException {
		++fOffset;
		return fReader.read();
	}

	/**
	 * Returns the offset.
	 * @return int
	 */
	public final int getOffset() {
		return fOffset;
	}

	/**
	 * Returns the reader.
	 * @return Reader
	 */
	public final Reader getReader() {
		return fReader;
	}

	public final int undoStackSize() {
		return fUndo.size();
	}

	/**
	 * Returns the undo.
	 * @return int
	 */
	public final int popUndo() {
		return fUndo.pop().intValue();
	}

	/**
	 * Sets the undo.
	 * @param undo The undo to set
	 */
	public void pushUndo(int undo) {
		this.fUndo.push(Integer.valueOf(undo));
	}
}
