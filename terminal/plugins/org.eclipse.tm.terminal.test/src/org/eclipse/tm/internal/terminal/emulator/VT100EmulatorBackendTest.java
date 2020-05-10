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
 * Anton Leherbauer (Wind River) - [458218] Add support for ANSI insert mode
 * Anton Leherbauer (Wind River) - [458402] Add support for scroll up/down and scroll region
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.emulator;

import static org.eclipse.tm.terminal.model.TerminalColor.BLACK;
import static org.eclipse.tm.terminal.model.TerminalColor.WHITE;

import org.eclipse.tm.internal.terminal.model.TerminalTextDataStore;
import org.eclipse.tm.internal.terminal.model.TerminalTextTestHelper;
import org.eclipse.tm.terminal.model.ITerminalTextData;
import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;
import org.eclipse.tm.terminal.model.TerminalStyle;

import junit.framework.TestCase;

public class VT100EmulatorBackendTest extends TestCase {

	protected IVT100EmulatorBackend makeBakend(ITerminalTextData term) {
		return new VT100EmulatorBackend(term);
	}

	protected ITerminalTextData makeITerminalTextData() {
		return new TerminalTextDataStore();
	}

	protected String toSimple(ITerminalTextData term) {
		return TerminalTextTestHelper.toSimple(term);
	}

	protected String toMultiLineText(ITerminalTextDataReadOnly term) {
		return TerminalTextTestHelper.toMultiLineText(term);
	}

	protected void fill(ITerminalTextData term, String s) {
		TerminalTextTestHelper.fill(term, s);
	}

	protected void fillSimple(ITerminalTextData term, String s) {
		TerminalTextTestHelper.fillSimple(term, s);
	}

	/**
	 * Used for multi line text
	 * @param expected
	 * @param actual
	 */
	protected void assertEqualsTerm(String expected, String actual) {
		assertEquals(expected.replace(' ', '.'), actual.replace('\000', '.'));
	}

	/**
	 * Used for simple text
	 * @param expected
	 * @param actual
	 */
	protected void assertEqualsSimple(String expected, String actual) {
		assertEquals(-1, actual.indexOf('\n'));
		assertEquals(expected, actual);
	}

	public void testClearAll() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		vt100.setDimensions(3, 4);
		fill(term, "0000\n" + "1111\n" + "2222\n" + "3333\n" + "4444\n" + "5555");
		vt100.clearAll();
		assertEqualsTerm("    \n" + "    \n" + "    ", toMultiLineText(term));
	}

	public void testSetDimensions() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		String s = "0000\n" + "1111\n" + "2222\n" + "3333\n" + "4444\n" + "5555";
		fill(term, s);
		vt100.setDimensions(3, 4);
		assertEquals(3, vt100.getLines());
		assertEquals(4, vt100.getColumns());
		assertEqualsTerm(s, toMultiLineText(term));

		vt100.setCursor(0, 2);
		vt100.setDimensions(2, 4);
		assertEquals(0, vt100.getCursorLine());
		assertEquals(2, vt100.getCursorColumn());

		vt100.setCursor(0, 2);
		vt100.setDimensions(5, 4);
		assertEquals(3, vt100.getCursorLine());
		assertEquals(2, vt100.getCursorColumn());

		assertEqualsTerm(s, toMultiLineText(term));

		vt100.setCursor(0, 3);
		vt100.setDimensions(5, 2);
		assertEquals(0, vt100.getCursorLine());
		assertEquals(1, vt100.getCursorColumn());
	}

	public void testToAbsoluteLine() {
		ITerminalTextData term = makeITerminalTextData();
		VT100EmulatorBackend vt100 = new VT100EmulatorBackend(term);
		vt100.setDimensions(2, 3);
		assertEquals(vt100.toAbsoluteLine(0), 0);
		// TODO
		term = makeITerminalTextData();
		vt100 = new VT100EmulatorBackend(term);
		vt100.setDimensions(1, 10);
		assertEquals(vt100.toAbsoluteLine(0), 0);
	}

	public void testInsertCharacters() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		vt100.setDimensions(3, 4);
		String s = "aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "1234\n" + "4567\n" + "9012";
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.insertCharacters(1);
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + " 123\n" + "4567\n" + "9012",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 1);
		vt100.insertCharacters(1);
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "1234\n" + "4 56\n" + "9012",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 1);
		vt100.insertCharacters(2);
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "1234\n" + "4  5\n" + "9012",
				toMultiLineText(term));

		vt100.setDimensions(1, 10);
		fill(term, "0123456789");
		vt100.setCursor(0, 0);
		vt100.insertCharacters(10);
		assertEqualsTerm("          ", toMultiLineText(term));

		vt100.setDimensions(1, 10);
		fill(term, "0123456789");
		vt100.setCursor(0, 0);
		vt100.insertCharacters(14);
		assertEqualsTerm("          ", toMultiLineText(term));

		vt100.setDimensions(1, 10);
		fill(term, "0123456789");
		vt100.setCursor(0, 3);
		vt100.insertCharacters(14);
		assertEqualsTerm("012       ", toMultiLineText(term));

		vt100.setDimensions(1, 10);
		fill(term, "0123456789");
		vt100.setCursor(0, 3);
		vt100.insertCharacters(0);
		assertEqualsTerm("0123456789", toMultiLineText(term));

		vt100.setDimensions(1, 10);
		fill(term, "0123456789");
		vt100.setCursor(0, 3);
		vt100.insertCharacters(2);
		assertEqualsTerm("012  34567", toMultiLineText(term));
	}

	public void testEraseToEndOfScreen() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		vt100.setDimensions(3, 4);
		String s = "aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "0123\n" + "4567\n" + "8901";
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.eraseToEndOfScreen();
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "    \n" + "    \n" + "    ",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 0);
		vt100.eraseToEndOfScreen();
		assertEquals(1, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "0123\n" + "    \n" + "    ",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 1);
		vt100.eraseToEndOfScreen();
		assertEquals(1, vt100.getCursorLine());
		assertEquals(1, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "0123\n" + "4   \n" + "    ",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 4);
		assertEquals(1, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		vt100.eraseToEndOfScreen();
		assertEquals(1, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "0123\n" + "456.\n" + "    ",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 5);
		vt100.eraseToEndOfScreen();
		assertEquals(1, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "0123\n" + "456.\n" + "    ",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(2, 3);
		vt100.eraseToEndOfScreen();
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "0123\n" + "4567\n" + "890 ",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(2, 5);
		vt100.eraseToEndOfScreen();
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "0123\n" + "4567\n" + "890.",
				toMultiLineText(term));
	}

	public void testEraseToCursor() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		vt100.setDimensions(3, 4);
		String s = "aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "0123\n" + "4567\n" + "8901";
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.eraseToCursor();
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + " 123\n" + "4567\n" + "8901",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 0);
		vt100.eraseToCursor();
		assertEquals(1, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "    \n" + " 567\n" + "8901",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 1);
		vt100.eraseToCursor();
		assertEquals(1, vt100.getCursorLine());
		assertEquals(1, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "    \n" + "  67\n" + "8901",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 4);
		vt100.eraseToCursor();
		assertEquals(1, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "    \n" + "    \n" + "8901",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 5);
		vt100.eraseToCursor();
		assertEquals(1, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "    \n" + "    \n" + "8901",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(2, 3);
		vt100.eraseToCursor();
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "    \n" + "    \n" + "    ",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(2, 5);
		vt100.eraseToCursor();
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "    \n" + "    \n" + "    ",
				toMultiLineText(term));
	}

	public void testEraseAll() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		vt100.setDimensions(3, 4);
		fill(term, "0000\n" + "1111\n" + "2222\n" + "3333\n" + "4444\n" + "5555");
		vt100.eraseAll();
		assertEqualsTerm("0000\n" + "1111\n" + "2222\n" + "    \n" + "    \n" + "    ", toMultiLineText(term));
	}

	public void testEraseLine() {
		String s = "abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "uvwxy\n" + "zABCD\n" + "EFGHI";

		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);

		vt100.setDimensions(3, 5);
		fill(term, s);
		vt100.setCursor(0, 3);
		vt100.eraseLine();
		assertEquals(0, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "     \n" + "zABCD\n" + "EFGHI",
				toMultiLineText(term));

		vt100.setDimensions(3, 5);
		fill(term, s);
		vt100.setCursor(2, 3);
		vt100.eraseLine();
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "uvwxy\n" + "zABCD\n" + "     ",
				toMultiLineText(term));
	}

	public void testEraseLineToEnd() {
		String s = "abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "uvwxy\n" + "zABCD\n" + "EFGHI";

		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);

		vt100.setDimensions(3, 5);
		fill(term, s);
		vt100.setCursor(0, 3);
		vt100.eraseLineToEnd();
		assertEquals(0, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "uvw  \n" + "zABCD\n" + "EFGHI",
				toMultiLineText(term));

		vt100.setDimensions(3, 5);
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.eraseLineToEnd();
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "     \n" + "zABCD\n" + "EFGHI",
				toMultiLineText(term));

		vt100.setDimensions(3, 5);
		fill(term, s);
		vt100.setCursor(2, 3);
		vt100.eraseLineToEnd();
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "uvwxy\n" + "zABCD\n" + "EFG  ",
				toMultiLineText(term));
		vt100.setDimensions(3, 5);
		fill(term, s);

		vt100.setCursor(2, 4);
		vt100.eraseLineToEnd();
		assertEquals(2, vt100.getCursorLine());
		assertEquals(4, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "uvwxy\n" + "zABCD\n" + "EFGH ",
				toMultiLineText(term));

		vt100.setCursor(2, 5);
		vt100.eraseLineToEnd();
		assertEquals(2, vt100.getCursorLine());
		assertEquals(4, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "uvwxy\n" + "zABCD\n" + "EFGH ",
				toMultiLineText(term));

	}

	public void testEraseLineToCursor() {
		String s = "abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "uvwxy\n" + "zABCD\n" + "EFGHI";

		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);

		vt100.setDimensions(3, 5);
		fill(term, s);
		vt100.setCursor(0, 3);
		vt100.eraseLineToCursor();
		assertEquals(0, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "    y\n" + "zABCD\n" + "EFGHI",
				toMultiLineText(term));

		vt100.setDimensions(3, 5);
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.eraseLineToCursor();
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + " vwxy\n" + "zABCD\n" + "EFGHI",
				toMultiLineText(term));

		vt100.setDimensions(3, 5);
		fill(term, s);
		vt100.setCursor(2, 3);
		vt100.eraseLineToCursor();
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "uvwxy\n" + "zABCD\n" + "    I",
				toMultiLineText(term));
		vt100.setDimensions(3, 5);
		fill(term, s);

		vt100.setCursor(2, 4);
		vt100.eraseLineToCursor();
		assertEquals(2, vt100.getCursorLine());
		assertEquals(4, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "uvwxy\n" + "zABCD\n" + "     ",
				toMultiLineText(term));

		vt100.setCursor(2, 5);
		vt100.eraseLineToCursor();
		assertEquals(2, vt100.getCursorLine());
		assertEquals(4, vt100.getCursorColumn());
		assertEqualsTerm("abcde\n" + "fghij\n" + "klmno\n" + "pqrst\n" + "uvwxy\n" + "zABCD\n" + "     ",
				toMultiLineText(term));

	}

	public void testInsertLines() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		String s = "0000\n" + "1111\n" + "2222\n" + "3333\n" + "4444\n" + "5555";
		vt100.setDimensions(3, 4);
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.insertLines(1);
		assertEqualsTerm("0000\n" + "1111\n" + "2222\n" + "    \n" + "3333\n" + "4444", toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 0);
		vt100.insertLines(1);
		assertEqualsTerm("0000\n" + "1111\n" + "2222\n" + "3333\n" + "    \n" + "4444", toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 0);
		vt100.insertLines(2);
		assertEqualsTerm("0000\n" + "1111\n" + "2222\n" + "3333\n" + "    \n" + "    ", toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 3);
		vt100.insertLines(2);
		assertEqualsTerm("0000\n" + "1111\n" + "2222\n" + "3333\n" + "    \n" + "    ", toMultiLineText(term));

		vt100.setDimensions(6, 4);
		fill(term, s);
		vt100.setCursor(1, 3);
		vt100.insertLines(2);
		assertEqualsTerm("0000\n" + "    \n" + "    \n" + "1111\n" + "2222\n" + "3333", toMultiLineText(term));

		vt100.setDimensions(6, 4);
		fill(term, s);
		vt100.setCursor(1, 3);
		vt100.insertLines(7);
		assertEqualsTerm("0000\n" + "    \n" + "    \n" + "    \n" + "    \n" + "    ", toMultiLineText(term));

		vt100.setDimensions(6, 4);
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.insertLines(7);
		assertEqualsTerm("    \n" + "    \n" + "    \n" + "    \n" + "    \n" + "    ", toMultiLineText(term));

		vt100.setDimensions(6, 4);
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.insertLines(5);
		assertEqualsTerm("    \n" + "    \n" + "    \n" + "    \n" + "    \n" + "0000", toMultiLineText(term));
	}

	public void testDeleteCharacters() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		vt100.setDimensions(3, 4);
		String s = "aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "1234\n" + "4567\n" + "9012";
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.deleteCharacters(1);
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "234 \n" + "4567\n" + "9012",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 1);
		vt100.deleteCharacters(1);
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "1234\n" + "467 \n" + "9012",
				toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 1);
		vt100.deleteCharacters(2);
		assertEqualsTerm("aaaa\n" + "bbbb\n" + "cccc\n" + "dddd\n" + "eeee\n" + "ffff\n" + "1234\n" + "47  \n" + "9012",
				toMultiLineText(term));

		vt100.setDimensions(1, 10);
		fill(term, "0123456789");
		vt100.setCursor(0, 0);
		vt100.deleteCharacters(10);
		assertEqualsTerm("          ", toMultiLineText(term));

		vt100.setDimensions(1, 10);
		fill(term, "0123456789");
		vt100.setCursor(0, 0);
		vt100.deleteCharacters(14);
		assertEqualsTerm("          ", toMultiLineText(term));

		vt100.setDimensions(1, 10);
		fill(term, "0123456789");
		vt100.setCursor(0, 3);
		vt100.deleteCharacters(0);
		assertEqualsTerm("0123456789", toMultiLineText(term));

		vt100.setDimensions(1, 10);
		fill(term, "0123456789");
		vt100.setCursor(0, 3);
		vt100.deleteCharacters(2);
		assertEqualsTerm("01256789  ", toMultiLineText(term));

		vt100.setDimensions(1, 10);
		fill(term, "0123456789");
		vt100.setCursor(0, 3);
		vt100.deleteCharacters(14);
		assertEqualsTerm("012       ", toMultiLineText(term));

	}

	public void testDeleteLines() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		String s = "0000\n" + "1111\n" + "2222\n" + "3333\n" + "4444\n" + "5555";
		vt100.setDimensions(3, 4);
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.deleteLines(1);
		assertEqualsTerm("0000\n" + "1111\n" + "2222\n" + "4444\n" + "5555\n" + "    ", toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 0);
		vt100.deleteLines(1);
		assertEqualsTerm("0000\n" + "1111\n" + "2222\n" + "3333\n" + "5555\n" + "    ", toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 0);
		vt100.deleteLines(2);
		assertEqualsTerm("0000\n" + "1111\n" + "2222\n" + "3333\n" + "    \n" + "    ", toMultiLineText(term));

		fill(term, s);
		vt100.setCursor(1, 3);
		vt100.deleteLines(2);
		assertEqualsTerm("0000\n" + "1111\n" + "2222\n" + "3333\n" + "    \n" + "    ", toMultiLineText(term));

		vt100.setDimensions(6, 4);
		fill(term, s);
		vt100.setCursor(1, 3);
		vt100.deleteLines(2);
		assertEqualsTerm("0000\n" + "3333\n" + "4444\n" + "5555\n" + "    \n" + "    ", toMultiLineText(term));

		vt100.setDimensions(6, 4);
		fill(term, s);
		vt100.setCursor(1, 3);
		vt100.deleteLines(7);
		assertEqualsTerm("0000\n" + "    \n" + "    \n" + "    \n" + "    \n" + "    ", toMultiLineText(term));

		vt100.setDimensions(6, 4);
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.deleteLines(7);
		assertEqualsTerm("    \n" + "    \n" + "    \n" + "    \n" + "    \n" + "    ", toMultiLineText(term));

		vt100.setDimensions(6, 4);
		fill(term, s);
		vt100.setCursor(0, 0);
		vt100.deleteLines(5);
		assertEqualsTerm("5555\n" + "    \n" + "    \n" + "    \n" + "    \n" + "    ", toMultiLineText(term));
	}

	public void testGetDefaultStyle() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		TerminalStyle style = TerminalStyle.getStyle(WHITE, BLACK);
		vt100.setDefaultStyle(style);
		assertSame(style, vt100.getDefaultStyle());
		TerminalStyle style2 = style.setBold(true);
		vt100.setDefaultStyle(style2);
		assertSame(style2, vt100.getDefaultStyle());
	}

	public void testGetStyle() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		TerminalStyle style = TerminalStyle.getStyle(WHITE, BLACK);
		vt100.setStyle(style);
		assertSame(style, vt100.getStyle());
		TerminalStyle style2 = style.setBold(true);
		vt100.setStyle(style2);
		assertSame(style2, vt100.getStyle());
	}

	public void testAppendString() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		term.setMaxHeight(6);
		vt100.setDimensions(3, 4);
		vt100.setCursor(0, 0);
		assertEqualsTerm("    \n" + "    \n" + "    ", toMultiLineText(term));
		vt100.appendString("012");
		assertEqualsTerm("012 \n" + "    \n" + "    ", toMultiLineText(term));
		assertEquals(0, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
		vt100.appendString("3");
		assertEqualsTerm("0123\n" + "    \n" + "    ", toMultiLineText(term));
		assertEquals(1, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());

		vt100.appendString("567890");
		assertEqualsTerm("0123\n" + "5678\n" + "90  ", toMultiLineText(term));
		assertEquals(2, vt100.getCursorLine());
		assertEquals(2, vt100.getCursorColumn());

		vt100.appendString("a");
		assertEqualsTerm("0123\n" + "5678\n" + "90a ", toMultiLineText(term));
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());

		vt100.appendString("b");
		assertEqualsTerm("0123\n" + "5678\n" + "90ab\n" + "    ", toMultiLineText(term));
		assertEquals(2, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());

		vt100.appendString("cd");
		assertEqualsTerm("0123\n" + "5678\n" + "90ab\n" + "cd  ", toMultiLineText(term));
		assertEquals(2, vt100.getCursorLine());
		assertEquals(2, vt100.getCursorColumn());

		vt100.appendString("efgh");
		assertEqualsTerm("0123\n" + "5678\n" + "90ab\n" + "cdef\n" + "gh  ", toMultiLineText(term));
		assertEquals(2, vt100.getCursorLine());
		assertEquals(2, vt100.getCursorColumn());

		vt100.appendString("ijklmnopqrstuvwx");
		assertEqualsTerm("cdef\n" + "ghij\n" + "klmn\n" + "opqr\n" + "stuv\n" + "wx  ", toMultiLineText(term));
		assertEquals(2, vt100.getCursorLine());
		assertEquals(2, vt100.getCursorColumn());

		vt100.setCursor(1, 1);
		vt100.appendString("123");
		assertEqualsTerm("cdef\n" + "ghij\n" + "klmn\n" + "opqr\n" + "s123\n" + "wx  ", toMultiLineText(term));
		assertEquals(2, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());

		vt100.setCursor(1, 1);
		vt100.appendString("ABCDEFGHIJKL");
		assertEqualsTerm("klmn\n" + "opqr\n" + "sABC\n" + "DEFG\n" + "HIJK\n" + "L   ", toMultiLineText(term));
		assertEquals(2, vt100.getCursorLine());
		assertEquals(1, vt100.getCursorColumn());
	}

	public void testProcessNewline() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		String s = "0000\n" + "1111\n" + "2222\n" + "3333\n" + "4444\n" + "5555";
		term.setMaxHeight(6);
		vt100.setDimensions(3, 4);
		vt100.setCursor(0, 0);
		fill(term, s);
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		vt100.processNewline();
		assertEqualsTerm(s, toMultiLineText(term));
		assertEquals(1, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		vt100.setCursorColumn(3);
		vt100.processNewline();
		assertEqualsTerm(s, toMultiLineText(term));
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());

		vt100.processNewline();
		assertEqualsTerm("1111\n" + "2222\n" + "3333\n" + "4444\n" + "5555\n" + "    ", toMultiLineText(term));
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());

		vt100.processNewline();
		assertEqualsTerm("2222\n" + "3333\n" + "4444\n" + "5555\n" + "    \n" + "    ", toMultiLineText(term));
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
	}

	public void testSetCursorLine() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		term.setMaxHeight(6);
		vt100.setDimensions(3, 4);
		// the cursor still at the beginning....
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		vt100.setCursor(0, 2);
		vt100.setCursorLine(1);
		assertEquals(1, vt100.getCursorLine());
		assertEquals(2, vt100.getCursorColumn());
		vt100.setCursor(0, -2);
		vt100.setCursorLine(-1);
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		vt100.setCursor(0, 10);
		vt100.setCursorLine(10);
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
	}

	public void testSetCursorAndSetDimensions() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		term.setMaxHeight(10);
		vt100.setDimensions(3, 4);
		// the cursor still at the beginning....
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		vt100.setDimensions(6, 4);
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		vt100.setCursor(2, 3);
		vt100.setDimensions(8, 4);
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
	}

	public void testSetCursorColumn() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		term.setMaxHeight(6);
		vt100.setDimensions(3, 4);
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		vt100.setCursor(1, 0);
		vt100.setCursorColumn(2);
		assertEquals(1, vt100.getCursorLine());
		assertEquals(2, vt100.getCursorColumn());
		vt100.setCursor(-1, -2);
		vt100.setCursorColumn(-2);
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		vt100.setCursor(10, 0);
		vt100.setCursorColumn(10);
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
	}

	public void testSetCursor() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		term.setMaxHeight(6);
		vt100.setDimensions(3, 4);
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		vt100.setCursor(0, 0);
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		vt100.setCursor(1, 2);
		assertEquals(1, vt100.getCursorLine());
		assertEquals(2, vt100.getCursorColumn());
		vt100.setCursor(-1, -2);
		assertEquals(0, vt100.getCursorLine());
		assertEquals(0, vt100.getCursorColumn());
		vt100.setCursor(10, 10);
		assertEquals(2, vt100.getCursorLine());
		assertEquals(3, vt100.getCursorColumn());
	}

	public void testVT100LineWrappingOn() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		term.setMaxHeight(10);
		vt100.setDimensions(6, 4);
		vt100.setVT100LineWrapping(true);
		vt100.appendString("abcd");
		vt100.setCursorColumn(0);
		vt100.processNewline();
		vt100.appendString("1234");
		vt100.setCursorColumn(0);
		vt100.processNewline();
		assertEquals(2, vt100.getCursorLine());
	}

	public void testVT100LineWrappingOff() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		term.setMaxHeight(10);
		vt100.setDimensions(6, 4);
		vt100.setVT100LineWrapping(false);
		vt100.appendString("abcd");
		vt100.setCursorColumn(0);
		vt100.processNewline();
		vt100.appendString("1234");
		vt100.setCursorColumn(0);
		vt100.processNewline();
		assertEquals(4, vt100.getCursorLine());
	}

	public void testWrappedLines() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		term.setMaxHeight(10);
		vt100.setDimensions(6, 4);
		vt100.setVT100LineWrapping(true);
		vt100.appendString("abcd123");
		vt100.setCursorColumn(0);
		vt100.processNewline();
		vt100.appendString("abc");
		vt100.setCursorColumn(0);
		vt100.processNewline();
		vt100.appendString("1234abcd");
		assertEquals(4, vt100.getCursorLine());
		assertTrue(term.isWrappedLine(0));
		assertFalse(term.isWrappedLine(1));
		assertFalse(term.isWrappedLine(2));
		assertTrue(term.isWrappedLine(3));
	}

	public void testInsertMode() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		term.setMaxHeight(10);
		vt100.setDimensions(4, 6);
		// replace mode
		vt100.appendString("123");
		vt100.setCursorColumn(0);
		vt100.appendString("abc");
		assertEquals("abc", new String(term.getChars(0)));
		vt100.clearAll();
		// insert mode
		vt100.setCursorColumn(0);
		vt100.appendString("123");
		vt100.setCursorColumn(0);
		vt100.setInsertMode(true);
		vt100.appendString("abc");
		vt100.setInsertMode(false);
		assertEquals("abc123", new String(term.getChars(0)));
	}

	public void testScrollRegion() {
		ITerminalTextData term = makeITerminalTextData();
		IVT100EmulatorBackend vt100 = makeBakend(term);
		term.setMaxHeight(10);
		vt100.setDimensions(8, 6);
		vt100.appendString("123");
		vt100.setCursorColumn(0);
		vt100.processNewline();
		vt100.appendString("456");
		vt100.setCursorColumn(0);
		vt100.processNewline();
		vt100.appendString("789");
		vt100.setCursorColumn(0);
		vt100.processNewline();
		vt100.appendString("abc");
		vt100.setCursorColumn(0);
		vt100.processNewline();
		vt100.appendString("def");
		vt100.setCursorColumn(0);
		vt100.processNewline();
		vt100.appendString("ghi");

		// test scroll within region
		vt100.setCursorLine(1);
		vt100.setScrollRegion(1, 4);
		vt100.scrollUp(1);
		assertEquals("123", new String(term.getChars(0)));
		assertEquals("789", new String(term.getChars(1)));
		assertEquals("abc", new String(term.getChars(2)));
		assertEquals("def", new String(term.getChars(3)));
		assertNull(term.getChars(4));
		assertEquals("ghi", new String(term.getChars(5)));
		vt100.scrollDown(1);
		assertEquals("123", new String(term.getChars(0)));
		assertNull(term.getChars(1));
		assertEquals("789", new String(term.getChars(2)));
		assertEquals("abc", new String(term.getChars(3)));
		assertEquals("def", new String(term.getChars(4)));
		assertEquals("ghi", new String(term.getChars(5)));

		// test scroll without region
		vt100.setScrollRegion(-1, -1);
		vt100.scrollDown(1);
		assertNull(term.getChars(0));
		assertEquals("123", new String(term.getChars(1)));
		assertNull(term.getChars(2));
		assertEquals("789", new String(term.getChars(3)));
		assertEquals("abc", new String(term.getChars(4)));
		assertEquals("def", new String(term.getChars(5)));
		assertEquals("ghi", new String(term.getChars(6)));
		vt100.scrollUp(1);
		assertEquals("123", new String(term.getChars(0)));
		assertNull(term.getChars(1));
		assertEquals("789", new String(term.getChars(2)));
		assertEquals("abc", new String(term.getChars(3)));
		assertEquals("def", new String(term.getChars(4)));
		assertEquals("ghi", new String(term.getChars(5)));

		// test scroll by newline
		vt100.setScrollRegion(1, 4);
		vt100.setCursorLine(4);
		vt100.processNewline();
		assertEquals("123", new String(term.getChars(0)));
		assertEquals("789", new String(term.getChars(1)));
		assertEquals("abc", new String(term.getChars(2)));
		assertEquals("def", new String(term.getChars(3)));
		assertNull(term.getChars(4));
		assertEquals("ghi", new String(term.getChars(5)));
	}

}
