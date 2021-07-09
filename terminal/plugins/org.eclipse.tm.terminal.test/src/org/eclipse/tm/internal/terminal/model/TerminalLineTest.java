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
		TerminalLine line = new TerminalLine();
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
		TerminalLine line = new TerminalLine();
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
		TerminalLine line = simpleLine();
		line.setCodePointAt(1, "🐑");
		assertEquals("\0🐑", line.getString());
	}

	@Test
	public void testWideNoSurrogate_1() {
		TerminalLine line = simpleLine();
		line.setCodePointAt(1, "⌚");
		assertEquals("\0⌚", line.getString());
	}

	@Test
	public void testWideSurrogatePair_2() {
		TerminalLine line = simpleLine();
		line.setCodePointAt(0, "🐑");
		assertEquals("🐑\0", line.getString());
	}

	@Test
	public void testWideNoSurrogate_2() {
		TerminalLine line = simpleLine();
		line.setCodePointAt(0, "⌚");
		assertEquals("⌚\0", line.getString());
	}

	@Test
	public void testWideSurrogatePair_3() {
		TerminalLine line = simpleLine();
		line.setCodePointAt(1, "🐑");
		assertEquals("\0🐑", line.getString());
		line.setCodePointAt(0, "🐑");
		assertEquals("🐑\0", line.getString());
	}

	@Test
	public void testWideNoSurrogate_3() {
		TerminalLine line = simpleLine();
		line.setCodePointAt(1, "⌚");
		assertEquals("\0⌚", line.getString());
		line.setCodePointAt(0, "⌚");
		assertEquals("⌚\0", line.getString());
	}

	@Test
	public void testWideSurrogatePair_4() {
		TerminalLine line = simpleLine();
		line.setCodePointAt(1, "🐑");
		assertEquals("\0🐑", line.getString());
		line.setCodePointAt(0, "⌚");
		assertEquals("⌚\0", line.getString());
	}

	@Test
	public void testWideNoSurrogate_4() {
		TerminalLine line = simpleLine();
		line.setCodePointAt(1, "⌚");
		assertEquals("\0⌚", line.getString());
		line.setCodePointAt(0, "🐑");
		assertEquals("🐑\0", line.getString());
	}

	@Test
	public void test() {
		TerminalLine line = simpleLine();
		line.setCodePointAt(0, "🐑");
		assertEquals("🐑\0", line.getString());
		line.setCodePointAt(0, "\0");
		assertEquals("\0\0\0", line.getString());
	}

	@Test
	public void testLineSegments() {
		TerminalLine line = simpleLine();
		line.setCodePointAt(0, "🐑", TerminalStyle.getDefaultStyle());
		assertEquals("🐑\0", line.getString());
		LineSegment[] lineSegments = line.getLineSegments(0, 3);
		lineSegments.toString();
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void noWideCharInLastColumn() {
		TerminalLine line = simpleLine();
		// cannot set a wide character at the end of a line (it would get too wide)
		line.setCodePointAt(2, "🐑");
	}

	private TerminalLine simpleLine() {
		TerminalLine line = new TerminalLine();
		line.setWidth(3);
		assertEquals(3, line.getWidth());
		assertEquals("\0\0\0", line.getString());
		return line;
	}
}
