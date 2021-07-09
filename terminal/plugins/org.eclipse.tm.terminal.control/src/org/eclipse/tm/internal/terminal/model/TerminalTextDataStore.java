/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Anton Leherbauer (Wind River) - [453393] Add support for copying wrapped lines without line break
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.model;

import java.lang.reflect.Array;
import java.util.BitSet;

import org.eclipse.tm.terminal.model.ITerminalTextData;
import org.eclipse.tm.terminal.model.ITerminalTextDataSnapshot;
import org.eclipse.tm.terminal.model.LineSegment;
import org.eclipse.tm.terminal.model.TerminalStyle;

/**
 * This class is thread safe.
 *
 */
public class TerminalTextDataStore implements ITerminalTextData {
	private TerminalLine[] fChars;
	private int fWidth;
	private int fHeight;
	private int fMaxHeight;
	private int fCursorColumn;
	private int fCursorLine;
	final private BitSet fWrappedLines = new BitSet();

	public TerminalTextDataStore() {
		fChars = new TerminalLine[0];
		fWidth = 0;
	}

	/**
	 * This is used in asserts to throw an {@link RuntimeException}.
	 * This is useful for tests.
	 * @return never -- throws an exception
	 */
	private boolean throwRuntimeException() {
		throw new RuntimeException();
	}

	@Override
	public int getWidth() {
		return fWidth;
	}

	@Override
	public int getHeight() {
		return fHeight;
	}

	@Override
	public void setDimensions(int height, int width) {
		assert height >= 0 || throwRuntimeException();
		assert width >= 0 || throwRuntimeException();
		// just extend the region
		if (height > fChars.length) {
			fChars = (TerminalLine[]) resizeArray(fChars, height);
		}
		// clean the new lines
		if (height > fHeight) {
			for (int i = fHeight; i < height; i++) {
				cleanLine(i);
			}
		}
		// set dimensions after successful resize!
		fWidth = width;
		fHeight = height;
	}

	/**
	 * Reallocates an array with a new size, and copies the contents of the old
	 * array to the new array.
	 *
	 * @param origArray the old array, to be reallocated.
	 * @param newSize the new array size.
	 * @return A new array with the same contents (chopped off if needed or filled with 0 or null).
	 */
	private Object resizeArray(Object origArray, int newSize) {
		int oldSize = Array.getLength(origArray);
		if (oldSize == newSize)
			return origArray;
		Class<?> elementType = origArray.getClass().getComponentType();
		Object newArray = Array.newInstance(elementType, newSize);
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0)
			System.arraycopy(origArray, 0, newArray, 0, preserveLength);
		return newArray;
	}

	@Override
	public LineSegment[] getLineSegments(int line, int column, int len) {
		// get the styles and chars for this line
		TerminalLine chars = fChars[line];
		int n = column + len;

		// expand the line if needed....
		if (chars == null)
			chars = new TerminalLine(n);
		else if (chars.getWidth() < n)
			chars.setWidth(n);

		return chars.getLineSegments(column, len);
	}

	@Override
	public char getChar(int line, int column) {
		return (char) getCodePoint(line, column);
	}

	@Override
	public int getCodePoint(int line, int column) {
		assert column < fWidth || throwRuntimeException();
		if (fChars[line] == null || column >= fChars[line].getWidth())
			return 0;
		return fChars[line].getCodePointAt(column);
	}

	@Override
	public TerminalStyle getStyle(int line, int column) {
		assert column < fWidth || throwRuntimeException();
		if (fChars[line] == null || column >= fChars[line].getWidth())
			return null;
		return fChars[line].getStyleAt(column);
	}

	void ensureLineLength(int iLine, int length) {
		if (length > fWidth)
			throw new RuntimeException();
		if (fChars[iLine] == null) {
			fChars[iLine] = new TerminalLine(length);
		} else if (fChars[iLine].getWidth() < length) {
			fChars[iLine].setWidth(length);
		}
	}

	@Override
	@Deprecated
	public void setChar(int line, int column, char c, TerminalStyle style) {
		ensureLineLength(line, column + 1);
		fChars[line].setCodePointAt(column, c, style);
	}

	@Override
	@Deprecated
	public void setChars(int line, int column, char[] chars, TerminalStyle style) {
		setChars(line, column, chars, 0, chars.length, style);
	}

	@Override
	@Deprecated
	public void setChars(int line, int column, char[] chars, int start, int len, TerminalStyle style) {
		ensureLineLength(line, column + len);
		for (int i = 0; i < len; i++) {
			fChars[line].setCodePointAt(column + i, chars[i + start], style);
		}
	}

	@Override
	public void setCodePoint(int line, int column, int c, TerminalStyle style) {
		ensureLineLength(line, column + 1);
		fChars[line].setCodePointAt(column, c, style);
	}

	@Override
	public IWriteCodePointsResult writeCodePoints(int line, int startColumn, int[] input, int start, int length,
			TerminalStyle style) {
		ensureLineLength(line, fWidth);
		return fChars[line].writeCodePoints(startColumn, input, start, length, style);
	}

	@Override
	public void scroll(int startLine, int size, int shift) {
		assert startLine + size <= getHeight() || throwRuntimeException();
		if (shift < 0) {
			// move the region up
			// shift is negative!!
			for (int i = startLine; i < startLine + size + shift; i++) {
				fChars[i] = fChars[i - shift];
				fWrappedLines.set(i, fWrappedLines.get(i - shift));
			}
			// then clean the opened lines
			cleanLines(Math.max(startLine, startLine + size + shift), Math.min(-shift, getHeight() - startLine));
			//			cleanLines(Math.max(0, startLine+size+shift),Math.min(-shift, getHeight()-startLine));
		} else {
			for (int i = startLine + size - 1; i >= startLine && i - shift >= 0; i--) {
				fChars[i] = fChars[i - shift];
				fWrappedLines.set(i, fWrappedLines.get(i - shift));
			}
			cleanLines(startLine, Math.min(shift, getHeight() - startLine));
		}
	}

	/**
	 * Replaces the lines with new empty data
	 * @param line
	 * @param len
	 */
	private void cleanLines(int line, int len) {
		for (int i = line; i < line + len; i++) {
			cleanLine(i);
		}
	}

	/*
	 * @return a text representation of the object.
	 * Lines are separated by '\n'. No style information is returned.
	 */
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		for (int line = 0; line < getHeight(); line++) {
			if (line > 0)
				buff.append("\n"); //$NON-NLS-1$
			for (int column = 0; column < fWidth; column++) {
				buff.append(getChar(line, column));
			}
		}
		return buff.toString();
	}

	@Override
	public ITerminalTextDataSnapshot makeSnapshot() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addLine() {
		if (fMaxHeight > 0 && getHeight() < fMaxHeight) {
			setDimensions(getHeight() + 1, getWidth());
		} else {
			scroll(0, getHeight(), -1);
		}
	}

	@Override
	public void copy(ITerminalTextData source) {
		fWidth = source.getWidth();
		int n = source.getHeight();
		if (getHeight() != n) {
			fChars = new TerminalLine[n];
		}
		for (int i = 0; i < n; i++) {
			copyLine(source, i, i);
		}
		fHeight = n;
		fCursorLine = source.getCursorLine();
		fCursorColumn = source.getCursorColumn();
	}

	@Override
	public void copyRange(ITerminalTextData source, int sourceStartLine, int destStartLine, int length) {
		for (int i = 0; i < length; i++) {
			copyLine(source, i + sourceStartLine, i + destStartLine);
		}
	}

	@Override
	public void copyLine(ITerminalTextData source, int sourceLine, int destLine) {
		fChars[destLine] = (TerminalLine) source.getTerminalLine(sourceLine);
		fWrappedLines.set(destLine, source.isWrappedLine(sourceLine));
	}

	@Override
	public char[] getChars(int line) {
		if (fChars[line] == null)
			return null;
		return fChars[line].getChars();
	}

	@Override
	public TerminalLine getTerminalLine(int line) {
		return new TerminalLine(fChars[line]);
	}

	@Override
	public TerminalStyle[] getStyles(int line) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxHeight(int height) {
		fMaxHeight = height;
	}

	@Override
	public int getMaxHeight() {
		return fMaxHeight;
	}

	@Override
	public void cleanLine(int line) {
		fChars[line] = null;
		fWrappedLines.clear(line);
	}

	@Override
	public int getCursorColumn() {
		return fCursorColumn;
	}

	@Override
	public int getCursorLine() {
		return fCursorLine;
	}

	@Override
	public void setCursorColumn(int column) {
		fCursorColumn = column;
	}

	@Override
	public void setCursorLine(int line) {
		fCursorLine = line;
	}

	@Override
	public boolean isWrappedLine(int line) {
		return fWrappedLines.get(line);
	}

	@Override
	public void setWrappedLine(int line) {
		fWrappedLines.set(line);
	}

}
