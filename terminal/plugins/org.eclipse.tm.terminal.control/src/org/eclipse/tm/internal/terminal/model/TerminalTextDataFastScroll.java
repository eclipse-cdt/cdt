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

import org.eclipse.tm.terminal.model.ITerminalTextData;
import org.eclipse.tm.terminal.model.ITerminalTextDataSnapshot;
import org.eclipse.tm.terminal.model.LineSegment;
import org.eclipse.tm.terminal.model.TerminalStyle;

/**
 * This class is optimized for scrolling the entire {@link #getHeight()}.
 * The scrolling is done by moving an offset into the data and using
 * the modulo operator.
 *
 */
public class TerminalTextDataFastScroll implements ITerminalTextData {

	final ITerminalTextData fData;
	private int fHeight;
	private int fMaxHeight;
	/**
	 * The offset into the array.
	 */
	int fOffset;

	public TerminalTextDataFastScroll(ITerminalTextData data, int maxHeight) {
		fMaxHeight = maxHeight;
		fData = data;
		fData.setDimensions(maxHeight, fData.getWidth());
		if (maxHeight > 2)
			assert shiftOffset(-2) || throwRuntimeException();
	}

	public TerminalTextDataFastScroll(int maxHeight) {
		this(new TerminalTextDataStore(), maxHeight);
	}

	public TerminalTextDataFastScroll() {
		this(new TerminalTextDataStore(), 1);
	}

	/**
	 * This is used in asserts to throw an {@link RuntimeException}.
	 * This is useful for tests.
	 * @return never -- throws an exception
	 */
	private boolean throwRuntimeException() {
		throw new RuntimeException();
	}

	/**
	 *
	 * @param line
	 * @return the actual line number in {@link #fData}
	 */
	int getPositionOfLine(int line) {
		return (line + fOffset) % fMaxHeight;
	}

	/**
	 * Moves offset by delta. This does <b>not</b> move the data!
	 * @param delta
	 */
	void moveOffset(int delta) {
		assert Math.abs(delta) < fMaxHeight || throwRuntimeException();
		fOffset = (fMaxHeight + fOffset + delta) % fMaxHeight;

	}

	/**
	 * Test method to shift the offset for testing (if assert ==true)
	 * @param shift TODO
	 * @return true
	 */
	private boolean shiftOffset(int shift) {
		moveOffset(shift);
		return true;
	}

	@Override
	public void addLine() {
		if (getHeight() < fMaxHeight) {
			setDimensions(getHeight() + 1, getWidth());
		} else {
			scroll(0, getHeight(), -1);
		}
	}

	@Override
	public void cleanLine(int line) {
		fData.cleanLine(getPositionOfLine(line));
	}

	@Override
	public void copy(ITerminalTextData source) {
		int n = source.getHeight();
		setDimensions(source.getHeight(), source.getWidth());
		for (int i = 0; i < n; i++) {
			fData.copyLine(source, i, getPositionOfLine(i));
		}
	}

	@Override
	public void copyLine(ITerminalTextData source, int sourceLine, int destLine) {
		fData.copyLine(source, sourceLine, getPositionOfLine(destLine));
	}

	@Override
	public void copyRange(ITerminalTextData source, int sourceStartLine, int destStartLine, int length) {
		assert (destStartLine >= 0 && destStartLine + length <= fHeight) || throwRuntimeException();
		for (int i = 0; i < length; i++) {
			fData.copyLine(source, i + sourceStartLine, getPositionOfLine(i + destStartLine));
		}
	}

	@Override
	public char getChar(int line, int column) {
		assert (line >= 0 && line < fHeight) || throwRuntimeException();
		return fData.getChar(getPositionOfLine(line), column);
	}

	@Override
	public char[] getChars(int line) {
		assert (line >= 0 && line < fHeight) || throwRuntimeException();
		return fData.getChars(getPositionOfLine(line));
	}

	@Override
	public int getHeight() {
		return fHeight;
	}

	@Override
	public LineSegment[] getLineSegments(int line, int startCol, int numberOfCols) {
		assert (line >= 0 && line < fHeight) || throwRuntimeException();
		return fData.getLineSegments(getPositionOfLine(line), startCol, numberOfCols);
	}

	@Override
	public int getMaxHeight() {
		return fMaxHeight;
	}

	@Override
	public TerminalStyle getStyle(int line, int column) {
		assert (line >= 0 && line < fHeight) || throwRuntimeException();
		return fData.getStyle(getPositionOfLine(line), column);
	}

	@Override
	public TerminalStyle[] getStyles(int line) {
		assert (line >= 0 && line < fHeight) || throwRuntimeException();
		return fData.getStyles(getPositionOfLine(line));
	}

	@Override
	public int getWidth() {
		return fData.getWidth();
	}

	@Override
	public ITerminalTextDataSnapshot makeSnapshot() {
		return fData.makeSnapshot();
	}

	private void cleanLines(int line, int len) {
		for (int i = line; i < line + len; i++) {
			fData.cleanLine(getPositionOfLine(i));
		}
	}

	@Override
	public void scroll(int startLine, int size, int shift) {
		assert (startLine >= 0 && startLine + size <= fHeight) || throwRuntimeException();
		if (shift >= fMaxHeight || -shift >= fMaxHeight) {
			cleanLines(startLine, fMaxHeight - startLine);
			return;
		}
		if (size == fHeight) {
			// This is the case this class is optimized for!
			moveOffset(-shift);
			// we only have to clean the lines that appear by the move
			if (shift < 0) {
				cleanLines(Math.max(startLine, startLine + size + shift), Math.min(-shift, getHeight() - startLine));
			} else {
				cleanLines(startLine, Math.min(shift, getHeight() - startLine));
			}
		} else {
			// we have to copy the lines.
			if (shift < 0) {
				// move the region up
				// shift is negative!!
				for (int i = startLine; i < startLine + size + shift; i++) {
					fData.copyLine(fData, getPositionOfLine(i - shift), getPositionOfLine(i));
				}
				// then clean the opened lines
				cleanLines(Math.max(0, startLine + size + shift), Math.min(-shift, getHeight() - startLine));
			} else {
				for (int i = startLine + size - 1; i >= startLine && i - shift >= 0; i--) {
					fData.copyLine(fData, getPositionOfLine(i - shift), getPositionOfLine(i));
				}
				cleanLines(startLine, Math.min(shift, getHeight() - startLine));
			}
		}
	}

	@Override
	public void setChar(int line, int column, char c, TerminalStyle style) {
		assert (line >= 0 && line < fHeight) || throwRuntimeException();
		fData.setChar(getPositionOfLine(line), column, c, style);
	}

	@Override
	public void setChars(int line, int column, char[] chars, int start, int len, TerminalStyle style) {
		assert (line >= 0 && line < fHeight) || throwRuntimeException();
		fData.setChars(getPositionOfLine(line), column, chars, start, len, style);
	}

	@Override
	public void setChars(int line, int column, char[] chars, TerminalStyle style) {
		assert (line >= 0 && line < fHeight) || throwRuntimeException();
		fData.setChars(getPositionOfLine(line), column, chars, style);
	}

	@Override
	public void setDimensions(int height, int width) {
		assert height >= 0 || throwRuntimeException();
		assert width >= 0 || throwRuntimeException();
		if (height > fMaxHeight)
			setMaxHeight(height);
		fHeight = height;
		if (width != fData.getWidth())
			fData.setDimensions(fMaxHeight, width);
	}

	@Override
	public void setMaxHeight(int maxHeight) {
		assert maxHeight >= fHeight || throwRuntimeException();
		// move everything to offset0
		int start = getPositionOfLine(0);
		if (start != 0) {
			// invent a more efficient algorithm....
			ITerminalTextData buffer = new TerminalTextDataStore();
			// create a buffer with the expected height
			buffer.setDimensions(maxHeight, getWidth());
			int n = Math.min(fMaxHeight - start, maxHeight);
			// copy the first part
			buffer.copyRange(fData, start, 0, n);
			// copy the second part
			if (n < maxHeight)
				buffer.copyRange(fData, 0, n, Math.min(fMaxHeight - n, maxHeight - n));
			// copy the buffer back to our data
			fData.copy(buffer);
			shiftOffset(-start);
		} else {
			fData.setDimensions(maxHeight, fData.getWidth());
		}
		fMaxHeight = maxHeight;
	}

	@Override
	public int getCursorColumn() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCursorLine() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCursorColumn(int column) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCursorLine(int line) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isWrappedLine(int line) {
		assert (line >= 0 && line < fHeight) || throwRuntimeException();
		return fData.isWrappedLine(getPositionOfLine(line));
	}

	@Override
	public void setWrappedLine(int line) {
		assert (line >= 0 && line < fHeight) || throwRuntimeException();
		fData.setWrappedLine(getPositionOfLine(line));
	}

}
