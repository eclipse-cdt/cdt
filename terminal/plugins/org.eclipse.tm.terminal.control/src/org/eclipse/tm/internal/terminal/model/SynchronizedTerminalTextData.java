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
 * This is a decorator to make all access to
 * ITerminalTextData synchronized
 *
 */
public class SynchronizedTerminalTextData implements ITerminalTextData {
	final ITerminalTextData fData;

	public SynchronizedTerminalTextData(ITerminalTextData data) {
		fData = data;
	}

	@Override
	synchronized public void addLine() {
		fData.addLine();
	}

	@Override
	synchronized public void cleanLine(int line) {
		fData.cleanLine(line);
	}

	@Override
	synchronized public void copy(ITerminalTextData source) {
		fData.copy(source);
	}

	@Override
	synchronized public void copyLine(ITerminalTextData source, int sourceLine, int destLine) {
		fData.copyLine(source, sourceLine, destLine);
	}

	@Override
	synchronized public void copyRange(ITerminalTextData source, int sourceStartLine, int destStartLine, int length) {
		fData.copyRange(source, sourceStartLine, destStartLine, length);
	}

	@Override
	synchronized public char getChar(int line, int column) {
		return fData.getChar(line, column);
	}

	@Override
	synchronized public char[] getChars(int line) {
		return fData.getChars(line);
	}

	@Override
	synchronized public int getCursorColumn() {
		return fData.getCursorColumn();
	}

	@Override
	synchronized public int getCursorLine() {
		return fData.getCursorLine();
	}

	@Override
	synchronized public int getHeight() {
		return fData.getHeight();
	}

	@Override
	synchronized public LineSegment[] getLineSegments(int line, int startCol, int numberOfCols) {
		return fData.getLineSegments(line, startCol, numberOfCols);
	}

	@Override
	synchronized public int getMaxHeight() {
		return fData.getMaxHeight();
	}

	@Override
	synchronized public TerminalStyle getStyle(int line, int column) {
		return fData.getStyle(line, column);
	}

	@Override
	synchronized public TerminalStyle[] getStyles(int line) {
		return fData.getStyles(line);
	}

	@Override
	synchronized public int getWidth() {
		return fData.getWidth();
	}

	@Override
	synchronized public ITerminalTextDataSnapshot makeSnapshot() {
		return fData.makeSnapshot();
	}

	@Override
	synchronized public void scroll(int startLine, int size, int shift) {
		fData.scroll(startLine, size, shift);
	}

	@Override
	synchronized public void setChar(int line, int column, char c, TerminalStyle style) {
		fData.setChar(line, column, c, style);
	}

	@Override
	synchronized public void setChars(int line, int column, char[] chars, int start, int len, TerminalStyle style) {
		fData.setChars(line, column, chars, start, len, style);
	}

	@Override
	synchronized public void setChars(int line, int column, char[] chars, TerminalStyle style) {
		fData.setChars(line, column, chars, style);
	}

	@Override
	synchronized public void setCursorColumn(int column) {
		fData.setCursorColumn(column);
	}

	@Override
	synchronized public void setCursorLine(int line) {
		fData.setCursorLine(line);
	}

	@Override
	synchronized public void setDimensions(int height, int width) {
		fData.setDimensions(height, width);
	}

	@Override
	synchronized public void setMaxHeight(int height) {
		fData.setMaxHeight(height);
	}

	@Override
	synchronized public boolean isWrappedLine(int line) {
		return fData.isWrappedLine(line);
	}

	@Override
	synchronized public void setWrappedLine(int line) {
		fData.setWrappedLine(line);
	}
}
