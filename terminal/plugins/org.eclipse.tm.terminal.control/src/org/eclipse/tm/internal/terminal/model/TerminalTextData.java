/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [168197] Fix Terminal for CDC-1.1/Foundation-1.1
 * Anton Leherbauer (Wind River) - [453393] Add support for copying wrapped lines without line break
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.tm.terminal.model.ITerminalTextData;
import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;
import org.eclipse.tm.terminal.model.ITerminalTextDataSnapshot;
import org.eclipse.tm.terminal.model.LineSegment;
import org.eclipse.tm.terminal.model.TerminalStyle;

/**
 * This class is thread safe.
 *
 */
public class TerminalTextData implements ITerminalTextData {
	final ITerminalTextData fData;
	/**
	 * A list of active snapshots
	 */
	public TerminalTextDataSnapshot[] fSnapshots = new TerminalTextDataSnapshot[0];
	private int fCursorColumn;
	private int fCursorLine;

	/**
	 * Debug helper method -- use as "New Detail Formatter.." in the
	 * debugger variables view:
	 *   <pre>TerminalTextData.toMultiLineText(this,0,200))</pre>
	 * @param term the terminal
	 * @param start start line to show
	 * @param len number of lines to show -- negative numbers means show all
	 * @return a string representation of the content
	 */
	static public String toMultiLineText(ITerminalTextDataReadOnly term, int start, int len) {
		if (len < 0)
			len = term.getHeight();
		StringBuffer buff = new StringBuffer();
		int width = term.getWidth();
		int n = Math.min(len, term.getHeight() - start);
		for (int line = start; line < n; line++) {
			if (line > 0)
				buff.append("\n"); //$NON-NLS-1$
			for (int column = 0; column < width; column++) {
				buff.append(term.getChar(line, column));
			}
		}
		// get rid of the empty space at the end of the lines
		//return buff.toString().replaceAll("\000+", "");  //$NON-NLS-1$//$NON-NLS-2$
		//<J2ME CDC-1.1 Foundation-1.1 variant>
		int i = buff.length() - 1;
		while (i >= 0 && buff.charAt(i) == '\000') {
			i--;
		}
		buff.setLength(i + 1);
		return buff.toString();
		//</J2ME CDC-1.1 Foundation-1.1 variant>
	}

	/**
	 * Show the first 100 lines
	 * see {@link #toMultiLineText(ITerminalTextDataReadOnly, int, int)}
	 * @param term A read-only terminal model
	 * @return a string representation of the terminal
	 */
	static public String toMultiLineText(ITerminalTextDataReadOnly term) {
		return toMultiLineText(term, 0, 100);
	}

	public TerminalTextData() {
		this(new TerminalTextDataFastScroll());

		//		this(new TerminalTextDataStore());
	}

	public TerminalTextData(ITerminalTextData data) {
		fData = data;
	}

	@Override
	public int getWidth() {
		return fData.getWidth();
	}

	@Override
	public int getHeight() {
		// no need for an extra variable
		return fData.getHeight();
	}

	@Override
	public void setDimensions(int height, int width) {
		int h = getHeight();
		int w = getWidth();
		if (w == width && h == height)
			return;
		fData.setDimensions(height, width);
		sendDimensionsChanged(h, w, height, width);
	}

	private void sendDimensionsChanged(int oldHeight, int oldWidth, int newHeight, int newWidth) {
		// determine what has changed
		if (oldWidth == newWidth) {
			if (oldHeight < newHeight)
				sendLinesChangedToSnapshot(oldHeight, newHeight - oldHeight);
			else
				sendLinesChangedToSnapshot(newHeight, oldHeight - newHeight);
		} else {
			sendLinesChangedToSnapshot(0, oldHeight);
		}
		sendDimensionsChanged();
	}

	@Override
	public LineSegment[] getLineSegments(int line, int column, int len) {
		return fData.getLineSegments(line, column, len);
	}

	@Override
	public char getChar(int line, int column) {
		return fData.getChar(line, column);
	}

	@Override
	public TerminalStyle getStyle(int line, int column) {
		return fData.getStyle(line, column);
	}

	@Override
	public void setChar(int line, int column, char c, TerminalStyle style) {
		fData.setChar(line, column, c, style);
		sendLineChangedToSnapshots(line);
	}

	@Override
	public void setChars(int line, int column, char[] chars, TerminalStyle style) {
		fData.setChars(line, column, chars, style);
		sendLineChangedToSnapshots(line);
	}

	@Override
	public void setChars(int line, int column, char[] chars, int start, int len, TerminalStyle style) {
		fData.setChars(line, column, chars, start, len, style);
		sendLineChangedToSnapshots(line);
	}

	@Override
	public void scroll(int startLine, int size, int shift) {
		fData.scroll(startLine, size, shift);
		sendScrolledToSnapshots(startLine, size, shift);
	}

	@Override
	public String toString() {
		return fData.toString();
	}

	private void sendDimensionsChanged() {
		for (int i = 0; i < fSnapshots.length; i++) {
			fSnapshots[i].markDimensionsChanged();
		}
	}

	/**
	 * @param line notifies snapshots that line line has changed
	 */
	protected void sendLineChangedToSnapshots(int line) {
		for (int i = 0; i < fSnapshots.length; i++) {
			fSnapshots[i].markLineChanged(line);
		}
	}

	/**
	 * Notify snapshots that multiple lines have changed
	 * @param line changed line
	 * @param n number of changed lines
	 */
	protected void sendLinesChangedToSnapshot(int line, int n) {
		for (int i = 0; i < fSnapshots.length; i++) {
			fSnapshots[i].markLinesChanged(line, n);
		}
	}

	/**
	 * Notify snapshot that a region was scrolled
	 * @param startLine first line of scrolled region
	 * @param size size of scrolled region (number of lines)
	 * @param shift delta by which the region is scrolled
	 */
	protected void sendScrolledToSnapshots(int startLine, int size, int shift) {
		for (int i = 0; i < fSnapshots.length; i++) {
			fSnapshots[i].scroll(startLine, size, shift);
		}
	}

	protected void sendCursorChanged() {
		for (int i = 0; i < fSnapshots.length; i++) {
			fSnapshots[i].markCursorChanged();
		}
	}

	/**
	 * Removes the snapshot from the @observer@ list
	 * @param snapshot A snapshot of a terminal model
	 */
	protected void removeSnapshot(TerminalTextDataSnapshot snapshot) {
		// poor mans approach to modify the array
		List<TerminalTextDataSnapshot> list = new ArrayList<>();
		list.addAll(Arrays.asList(fSnapshots));
		list.remove(snapshot);
		fSnapshots = list.toArray(new TerminalTextDataSnapshot[list.size()]);
	}

	@Override
	public ITerminalTextDataSnapshot makeSnapshot() {
		// poor mans approach to modify the array
		TerminalTextDataSnapshot snapshot = new TerminalTextDataSnapshot(this);
		snapshot.markDimensionsChanged();
		List<TerminalTextDataSnapshot> list = new ArrayList<>();
		list.addAll(Arrays.asList(fSnapshots));
		list.add(snapshot);
		fSnapshots = list.toArray(new TerminalTextDataSnapshot[list.size()]);
		return snapshot;
	}

	@Override
	public void addLine() {
		int oldHeight = getHeight();
		fData.addLine();
		// was is an append or a scroll?
		int newHeight = getHeight();
		if (newHeight > oldHeight) {
			//the line was appended
			sendLinesChangedToSnapshot(oldHeight, 1);
			int width = getWidth();
			sendDimensionsChanged(oldHeight, width, newHeight, width);

		} else {
			// the line was scrolled
			sendScrolledToSnapshots(0, oldHeight, -1);
		}

	}

	@Override
	public void copy(ITerminalTextData source) {
		fData.copy(source);
		fCursorLine = source.getCursorLine();
		fCursorColumn = source.getCursorColumn();
	}

	@Override
	public void copyLine(ITerminalTextData source, int sourceLine, int destLine) {
		fData.copyLine(source, sourceLine, destLine);
	}

	@Override
	public void copyRange(ITerminalTextData source, int sourceStartLine, int destStartLine, int length) {
		fData.copyRange(source, sourceStartLine, destStartLine, length);
	}

	@Override
	public char[] getChars(int line) {
		return fData.getChars(line);
	}

	@Override
	public TerminalStyle[] getStyles(int line) {
		return fData.getStyles(line);
	}

	@Override
	public int getMaxHeight() {
		return fData.getMaxHeight();
	}

	@Override
	public void setMaxHeight(int height) {
		fData.setMaxHeight(height);
	}

	@Override
	public void cleanLine(int line) {
		fData.cleanLine(line);
		sendLineChangedToSnapshots(line);
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
		sendCursorChanged();
	}

	@Override
	public void setCursorLine(int line) {
		fCursorLine = line;
		sendCursorChanged();
	}

	@Override
	public boolean isWrappedLine(int line) {
		return fData.isWrappedLine(line);
	}

	@Override
	public void setWrappedLine(int line) {
		fData.setWrappedLine(line);
	}
}
