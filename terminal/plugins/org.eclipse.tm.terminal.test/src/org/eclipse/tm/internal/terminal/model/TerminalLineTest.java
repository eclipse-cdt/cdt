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

import static org.junit.Assert.assertEquals;

import org.eclipse.tm.terminal.model.ITerminalTextData.IWriteCodePointsResult;
import org.eclipse.tm.terminal.model.LineSegment;
import org.eclipse.tm.terminal.model.TerminalStyle;
import org.junit.Before;
import org.junit.Test;

public class TerminalLineTest {
	@Before
	public void setUp() throws Exception {
		try {
			assert false;
			throw new Error("No Assertions! Run this code with assertions enabled! (vmargs: -ea)");
		} catch (AssertionError e) {
			// OK, assertions are enabled!
		}
	}

	@Test
	public void test1() {
		TerminalLineExtension line = simpleLine();
		line.setWidth(3);
		line.setCodePointAt(0, 'a');
		line.setCodePointAt(1, 'b');
		assertEquals(3, line.getWidth());
		assertEquals("ab\0", line.getString());
		line.setCodePointAt(0, '⌚');
		assertEquals(3, line.getWidth());
		assertEquals("⌚\0", line.getString());
		line.setCodePointAt(0, 'a');
		assertEquals(3, line.getWidth());
		assertEquals("a\0\0", line.getString());
		line.setCodePointAt(1, 'b');
		assertEquals(3, line.getWidth());
		assertEquals("ab\0", line.getString());
		line.setCodePointAt(1, '⌚');
		assertEquals(3, line.getWidth());
		assertEquals("a⌚", line.getString());
		assertEquals(2, line.getString().length());
		assertEquals(2, line.getString().codePointCount(0, line.getString().length()));
	}

	@Test
	public void test2() {
		TerminalLineExtension line = simpleLine();
		line.setWidth(3);
		line.setCodePointAt(0, 'a');
		line.setCodePointAt(1, 'b');
		assertEquals(3, line.getWidth());
		assertEquals("ab\0", line.getString());
		line.setCodePointAt(0, "🐑");
		assertEquals(3, line.getWidth());
		assertEquals("🐑\0", line.getString());
		line.setCodePointAt(0, 'a');
		assertEquals(3, line.getWidth());
		assertEquals("a\0\0", line.getString());
		line.setCodePointAt(1, 'b');
		assertEquals(3, line.getWidth());
		assertEquals("ab\0", line.getString());
		line.setCodePointAt(1, "🐑");
		assertEquals(3, line.getWidth());
		assertEquals("a🐑", line.getString());
		assertEquals(3, line.getString().length());
		assertEquals(2, line.getString().codePointCount(0, line.getString().length()));
	}

	@Test
	public void testWideSurrogatePair_1() {
		TerminalLineExtension line = simpleLine();
		line.setCodePointAt(1, "🐑");
		assertEquals("\0🐑", line.getString());
	}

	@Test
	public void testWideNoSurrogate_1() {
		TerminalLineExtension line = simpleLine();
		line.setCodePointAt(1, "⌚");
		assertEquals("\0⌚", line.getString());
	}

	@Test
	public void testWideSurrogatePair_2() {
		TerminalLineExtension line = simpleLine();
		line.setCodePointAt(0, "🐑");
		assertEquals("🐑\0", line.getString());
	}

	@Test
	public void testWideNoSurrogate_2() {
		TerminalLineExtension line = simpleLine();
		line.setCodePointAt(0, "⌚");
		assertEquals("⌚\0", line.getString());
	}

	@Test
	public void testWideSurrogatePair_3() {
		TerminalLineExtension line = simpleLine();
		line.setCodePointAt(1, "🐑");
		assertEquals("\0🐑", line.getString());
		line.setCodePointAt(0, "🐑");
		assertEquals("🐑\0", line.getString());
	}

	@Test
	public void testWideNoSurrogate_3() {
		TerminalLineExtension line = simpleLine();
		line.setCodePointAt(1, "⌚");
		assertEquals("\0⌚", line.getString());
		line.setCodePointAt(0, "⌚");
		assertEquals("⌚\0", line.getString());
	}

	@Test
	public void testWideSurrogatePair_4() {
		TerminalLineExtension line = simpleLine();
		line.setCodePointAt(1, "🐑");
		assertEquals("\0🐑", line.getString());
		line.setCodePointAt(0, "⌚");
		assertEquals("⌚\0", line.getString());
	}

	@Test
	public void testWideNoSurrogate_4() {
		TerminalLineExtension line = simpleLine();
		line.setCodePointAt(1, "⌚");
		assertEquals("\0⌚", line.getString());
		line.setCodePointAt(0, "🐑");
		assertEquals("🐑\0", line.getString());
	}

	@Test
	public void test() {
		TerminalLineExtension line = simpleLine();
		line.setCodePointAt(0, "🐑");
		assertEquals("🐑\0", line.getString());
		line.setCodePointAt(0, "\0");
		assertEquals("\0\0\0", line.getString());
	}

	@Test
	public void testLineSegments() {
		TerminalLineExtension line = simpleLine();
		line.setCodePointAt(0, "🐑", TerminalStyle.getDefaultStyle());
		assertEquals("🐑\0", line.getString());
		LineSegment[] lineSegments = line.getLineSegments(0, 3);
		lineSegments.toString();
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void noWideCharInLastColumn() {
		TerminalLineExtension line = simpleLine();
		// cannot set a wide character at the end of a line (it would get too wide)
		line.setCodePointAt(2, "🐑");
	}

	@Test
	public void setCodePoints() {
		TerminalLineExtension line;
		IWriteCodePointsResult consumed;

		line = simpleLine();
		consumed = line.writeCodePoints(0, "X");
		assertEquals(1, consumed.getCodePointsConsumed());
		assertEquals(1, consumed.getWidthConsumed());
		assertEquals("X\0\0", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(0, "XX");
		assertEquals(2, consumed.getCodePointsConsumed());
		assertEquals(2, consumed.getWidthConsumed());
		assertEquals("XX\0", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(0, "XXX");
		assertEquals(3, consumed.getCodePointsConsumed());
		assertEquals(3, consumed.getWidthConsumed());
		assertEquals("XXX", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(0, "XXXX");
		assertEquals(3, consumed.getCodePointsConsumed());
		assertEquals(3, consumed.getWidthConsumed());
		assertEquals("XXX", line.getString());
		line = simpleLine();

		line = simpleLine();
		consumed = line.writeCodePoints(1, "X");
		assertEquals(1, consumed.getCodePointsConsumed());
		assertEquals(1, consumed.getWidthConsumed());
		assertEquals("\0X\0", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(1, "XX");
		assertEquals(2, consumed.getCodePointsConsumed());
		assertEquals(2, consumed.getWidthConsumed());
		assertEquals("\0XX", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(1, "XXX");
		assertEquals(2, consumed.getCodePointsConsumed());
		assertEquals(2, consumed.getWidthConsumed());
		assertEquals("\0XX", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(2, "X");
		assertEquals(1, consumed.getCodePointsConsumed());
		assertEquals(1, consumed.getWidthConsumed());
		assertEquals("\0\0X", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(2, "XX");
		assertEquals(1, consumed.getCodePointsConsumed());
		assertEquals(1, consumed.getWidthConsumed());
		assertEquals("\0\0X", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(3, "X");
		assertEquals(0, consumed.getCodePointsConsumed());
		assertEquals(0, consumed.getWidthConsumed());
		assertEquals("\0\0\0", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(0, "🐑");
		assertEquals(1, consumed.getCodePointsConsumed());
		assertEquals(2, consumed.getWidthConsumed());
		assertEquals("🐑\0", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(0, "🐑🐑");
		assertEquals(1, consumed.getCodePointsConsumed());
		assertEquals(2, consumed.getWidthConsumed());
		assertEquals("🐑\0", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(1, "🐑");
		assertEquals(1, consumed.getCodePointsConsumed());
		assertEquals(2, consumed.getWidthConsumed());
		assertEquals("\0🐑", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(1, "🐑🐑");
		assertEquals(1, consumed.getCodePointsConsumed());
		assertEquals(2, consumed.getWidthConsumed());
		assertEquals("\0🐑", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(2, "🐑");
		assertEquals(0, consumed.getCodePointsConsumed());
		assertEquals(0, consumed.getWidthConsumed());
		assertEquals("\0\0\0", line.getString());

		line = simpleLine();
		consumed = line.writeCodePoints(2, "🐑🐑");
		assertEquals(0, consumed.getCodePointsConsumed());
		assertEquals(0, consumed.getWidthConsumed());
		assertEquals("\0\0\0", line.getString());
	}

	private TerminalLineExtension simpleLine() {
		TerminalLineExtension line = new TerminalLineExtension();
		line.setWidth(3);
		assertEquals(3, line.getWidth());
		assertEquals("\0\0\0", line.getString());
		return line;
	}

	/**
	 * Convenience methods to make write testing easier, but not part of the API
	 */
	public static class TerminalLineExtension extends TerminalLine {
		/**
		 *
		 * @param startColumn
		 * @param string
		 * @return number of code points consumed
		 */
		public IWriteCodePointsResult writeCodePoints(int startColumn, String string) {
			int[] codePoints = string.codePoints().toArray();
			return writeCodePoints(startColumn, codePoints, 0, codePoints.length, null);
		}

		public void setCodePointAt(int column, String s) {
			assert s.codePointCount(0, s.length()) == 1;
			setCodePointAt(column, Character.codePointAt(s, 0), null);
		}

		public void setCodePointAt(int column, String s, TerminalStyle style) {
			assert s.codePointCount(0, s.length()) == 1;
			setCodePointAt(column, Character.codePointAt(s, 0), style);
		}

		public void setCodePointAt(int column, char c) {
			assert !Character.isSurrogate(c);
			setCodePointAt(column, Character.codePointAt(new char[] { c }, 0), null);
		}

		public void setCodePointAt(int column, char c, TerminalStyle style) {
			assert !Character.isSurrogate(c);
			setCodePointAt(column, Character.codePointAt(new char[] { c }, 0), style);
		}

		public void setCodePointAt(int column, int codePoint) {
			setCodePointAt(column, codePoint, null);
		}
	}

}
