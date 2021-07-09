/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tm.terminal.model.ITerminalLine;
import org.eclipse.tm.terminal.model.ITerminalTextData.IWriteCodePointsResult;
import org.eclipse.tm.terminal.model.LineSegment;
import org.eclipse.tm.terminal.model.TerminalStyle;

public class TerminalLine implements ITerminalLine {
	private static final int NULL_CODE_POINT = 0;
	final List<Integer> codePoints;
	final List<TerminalStyle> styles;

	public TerminalLine() {
		codePoints = new ArrayList<>();
		styles = new ArrayList<>();
	}

	public TerminalLine(int n) {
		codePoints = new ArrayList<>();
		styles = new ArrayList<>();
		setWidth(n);
	}

	public TerminalLine(TerminalLine terminalLine) {
		if (terminalLine == null) {
			codePoints = new ArrayList<>();
			styles = new ArrayList<>();
		} else {
			codePoints = new ArrayList<>(terminalLine.codePoints);
			styles = new ArrayList<>(terminalLine.styles);
		}
	}

	/**
	 * This is used in asserts to throw an {@link RuntimeException}.
	 * This is useful for tests.
	 * @return never -- throws an exception
	 */
	private boolean throwRuntimeException() {
		throw new RuntimeException();
	}

	public String getString() {
		int[] array = codePoints.stream().mapToInt(Integer::intValue).toArray();
		return new String(array, 0, array.length);
	}

	public char[] getChars() {
		return getString().toCharArray();
	}

	public String getStringTrimmed() {
		int[] array = codePoints.stream().mapToInt(Integer::intValue).toArray();
		int i = array.length - 1;
		while (i >= 0 && array[i] == '\0') {
			i--;
		}
		String s = new String(array, 0, i + 1);
		return s;
	}

	public int getWidth() {
		// TODO this is an invariant, should probably cache width and check invariant
		int sum = 0;
		for (int i = 0; i < codePoints.size(); i++) {
			sum += UnicodeCalc.width(codePoints.get(i));
		}
		return sum;
	}

	public void setCodePointAt(int column, int codePoint, TerminalStyle style) {
		assert column >= 0 || throwRuntimeException();
		int widthBefore = 0;
		assert (widthBefore = getWidth()) >= 0;

		int c = 0;
		int i = 0;
		while (c < column) {
			c += UnicodeCalc.width(codePoints.get(i++));
		}
		assert i < codePoints.size();
		assert styles.size() == codePoints.size();
		int oldPoint = codePoints.get(i);
		codePoints.set(i, codePoint);
		styles.set(i, style);
		if (UnicodeCalc.width(oldPoint) != UnicodeCalc.width(codePoint)) {
			if (UnicodeCalc.width(oldPoint) == 2) {
				// newpoint is narrower, so we need to add a space
				codePoints.add(i + 1, NULL_CODE_POINT);
				styles.add(i + 1, null /* TODO? which style */);
			} else {
				// newpoint is wider, so we need to get rid of next codepoint
				int codePointAfter = codePoints.get(i + 1);
				if (UnicodeCalc.width(codePointAfter) == 2) {
					codePoints.set(i + 1, NULL_CODE_POINT);
					// don't change the style of the changed char
				} else {
					codePoints.remove(i + 1);
					styles.remove(i + 1);
				}
			}
		}

		assert widthBefore == getWidth();
		assert styles.size() == codePoints.size();
	}

	public void setWidth(int width) {
		assert styles.size() == codePoints.size();
		for (int i = getWidth(); i < width; i++) {
			codePoints.add(NULL_CODE_POINT);
			styles.add(null);
		}
		// TODO handle narrowing of line
		assert width == getWidth();
		assert styles.size() == codePoints.size();
	}

	public int getCodePointAt(int column) {
		assert column >= 0 || throwRuntimeException();
		int c = 0;
		int i = 0;
		while (c < column) {
			c += UnicodeCalc.width(codePoints.get(i++));
		}
		// if c != column it means the requested column
		// is the right column of a wide character
		assert c == column;
		assert i < codePoints.size();
		return codePoints.get(i);
	}

	public TerminalStyle getStyleAt(int column) {
		assert column >= 0 || throwRuntimeException();
		int c = 0;
		int i = 0;
		while (c < column) {
			c += UnicodeCalc.width(codePoints.get(i++));
		}
		// if c != column it means the requested column
		// is the right column of a wide character
		assert c == column;
		assert i < styles.size();
		return styles.get(i);
	}

	public LineSegment[] getLineSegments(int column, int width) {
		int c = 0;
		int i = 0;
		while (c < column) {
			c += UnicodeCalc.width(codePoints.get(i++));
		}

		List<LineSegment> segments = new ArrayList<>();
		int w = 0;
		int col = c;
		TerminalStyle style = styles.get(i);
		StringBuilder sb = new StringBuilder();
		while (w < width) {
			if (styles.get(i) != style) {
				segments.add(new LineSegment(col, sb.toString(), style));
				sb.setLength(0);
				style = styles.get(i);
				col = c;
			}
			int codePoint = codePoints.get(i);
			sb.appendCodePoint(codePoint);
			int thisWidth = UnicodeCalc.width(codePoint);
			w += thisWidth;
			c += thisWidth;
			i++;
		}
		if (col < (column + width)) {
			segments.add(new LineSegment(col, sb.toString(), style));
		}
		return segments.toArray(new LineSegment[segments.size()]);
	}

	/**
	 *
	 * @param startColumn
	 * @param codePoints
	 * @param start
	 * @param length
	 * @return number of code points consumed
	 */
	public IWriteCodePointsResult writeCodePoints(int startColumn, int[] codePoints, int start, int length,
			TerminalStyle style) {
		int col = startColumn;
		int consumed = 0;
		while (consumed < length) {
			int codePoint = codePoints[consumed + start];
			int width = UnicodeCalc.width(codePoint);
			if (col + width > getWidth()) {
				break;
			}
			setCodePointAt(col, codePoint, style);
			col += width;
			consumed++;
		}
		int widthConsumed = col - startColumn;
		int codePointsConsumed = consumed;
		return new IWriteCodePointsResult() {

			@Override
			public int getWidthConsumed() {
				return widthConsumed;
			}

			@Override
			public int getCodePointsConsumed() {
				return codePointsConsumed;
			}
		};
	}

}
