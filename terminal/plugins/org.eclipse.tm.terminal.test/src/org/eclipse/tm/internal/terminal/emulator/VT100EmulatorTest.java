/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.emulator;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.terminal.model.ITerminalTextData;
import org.eclipse.tm.terminal.model.TerminalTextDataFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VT100EmulatorTest {

	private static final int WINDOW_COLUMNS = 80;
	private static final int WINDOW_LINES = 24;
	private static final String CLEAR_CURSOR_TO_EOL = "\033[K";
	private static final String CURSOR_POSITION_TOP_LEFT = "\033[H";
	private static final String CLEAR_ENTIRE_SCREEN_AND_SCROLLBACK = "\033[3J";
	private static final String CLEAR_ENTIRE_SCREEN = "\033[2J";
	private static final String SCROLL_REVERSE = "\033M";

	private static String TITLE(String title) {
		return "\033]0;" + title + "\007";
	}

	private static String SCROLL_REGION(int startRow, int endRow) {
		return "\033[" + startRow + ";" + endRow + "r";
	}

	/**
	 * Set the cursor position to line/column. Note that this is the logical
	 * line and column, so 1, 1 is the top left.
	 */
	private static String CURSOR_POSITION(int line, int column) {
		return "\033[" + line + ";" + column + "H";
	}

	@BeforeAll
	public static void beforeAll() {
		Logger.setUnderTest(true);
	}

	@AfterAll
	public static void afterAll() {
		Logger.setUnderTest(false);
	}

	private ITerminalTextData data;

	private MockTerminalControlForText control = new MockTerminalControlForText();

	private VT100Emulator emulator;

	@BeforeEach
	public void before() {
		data = TerminalTextDataFactory.makeTerminalTextData();
		emulator = new VT100Emulator(data, control, null);
		emulator.resetState();
		emulator.setDimensions(WINDOW_LINES, WINDOW_COLUMNS);
	}

	private Reader input(String... input) {
		StringReader reader = new StringReader(String.join("", input));
		emulator.setInputStreamReader(reader);
		return reader;
	}

	private void run(String... input) {
		Reader reader = input(input);
		emulator.processText();
		try {
			assertEquals(-1, reader.read());
		} catch (IOException e) {
			throw new RuntimeException("Wrap exception so that run can be called in functions", e);
		}
	}

	/**
	 * Convert the data's char arrays into a string that can be compared with
	 * an expected array of lines. Each line in the data has its \0 characters
	 * changed to spaces and then stripTrailing is run.
	 *
	 * @param expectedArray lines that are joined with \n before testing against actual
	 */
	private void assertTextEquals(String... expectedArray) {
		int height = data.getHeight();
		StringJoiner sj = new StringJoiner("\n");
		for (int i = 0; i < height; i++) {
			char[] chars = data.getChars(i);
			String line = chars == null ? "" : new String(chars);
			String lineCleanedup = line.replace('\0', ' ');
			String stripTrailing = lineCleanedup.stripTrailing();
			sj.add(stripTrailing);
		}
		String expected = String.join("\n", expectedArray).stripTrailing();
		String actual = sj.toString().stripTrailing();
		assertEquals(expected, actual);
	}

	private void assertTextEquals(List<String> expected) {
		assertTextEquals(expected.toArray(String[]::new));
	}

	private void assertCursorLocation(int line, int column) {
		assertAll(() -> assertEquals(line, data.getCursorLine(), "cursor line"),
				() -> assertEquals(column, data.getCursorColumn(), "cursor column"));
	}

	/**
	 * This tests the test harness ({@link #assertTextEquals(String...)} as much as the code.
	 */
	@Test
	public void testBasicOperaiion() {
		assertAll(() -> assertCursorLocation(0, 0), () -> assertTextEquals(""));
		run("Hello");
		assertAll(() -> assertCursorLocation(0, 5), () -> assertTextEquals("Hello"));
		emulator.clearTerminal();
		assertAll(() -> assertCursorLocation(0, 0), () -> assertTextEquals(""));

		// test multiline
		emulator.clearTerminal();
		run("Hello 1\r\nHello 2");
		// test both ways of asserting multiple lines
		assertAll(() -> assertCursorLocation(1, 7), //
				() -> assertTextEquals("Hello 1\nHello 2"), //
				() -> assertTextEquals("Hello 1", "Hello 2"));

		// test with no carriage return
		emulator.clearTerminal();
		run("Hello 1\nHello 2");
		assertTextEquals("Hello 1", "       Hello 2");

		// test \b backspace
		emulator.clearTerminal();
		run("Hello 1");
		assertAll(() -> assertCursorLocation(0, 7), () -> assertTextEquals("Hello 1"));
		run("\b\b");
		assertAll(() -> assertCursorLocation(0, 5), () -> assertTextEquals("Hello 1"));
		run(CLEAR_CURSOR_TO_EOL);
		assertAll(() -> assertCursorLocation(0, 5), () -> assertTextEquals("Hello"));
	}

	@Test
	public void testMultiline() {
		List<String> expected = new ArrayList<>();
		for (int i = 0; i < data.getHeight(); i++) {
			String line = "Hello " + i;
			expected.add(line);
			run(line);
			if (i != data.getHeight() - 1) {
				run("\r\n");
			}
		}
		assertTextEquals(expected);

		// add the final newline and check that the first line has been scrolled away
		run("\r\n");
		expected.remove(0);
		assertTextEquals(expected);
	}

	@Test
	public void testScrollBack() {
		data.setMaxHeight(1000);
		List<String> expected = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			String line = "Hello " + i;
			run(line + "\r\n");
			expected.add(line);
		}
		expected.remove(0);
		assertTextEquals(expected);
	}

	@Test
	public void testCursorPosition() {
		run(CURSOR_POSITION_TOP_LEFT);
		assertAll(() -> assertCursorLocation(0, 0), () -> assertTextEquals(""));
		run("Hello");
		assertAll(() -> assertCursorLocation(0, 5), () -> assertTextEquals("Hello"));
		run(CURSOR_POSITION_TOP_LEFT);
		assertAll(() -> assertCursorLocation(0, 0), () -> assertTextEquals("Hello"));
		run(CURSOR_POSITION(2, 2));
		assertAll(() -> assertCursorLocation(1, 1), () -> assertTextEquals("Hello"));
		emulator.clearTerminal();

		data.setMaxHeight(1000);
		List<String> expected = new ArrayList<>();
		for (int i = 0; i < WINDOW_LINES; i++) {
			String line = "Hello " + i;
			run(line + "\r\n");
			expected.add(line);
		}
		assertAll(() -> assertCursorLocation(WINDOW_LINES, 0), () -> assertTextEquals(expected));
		run(CURSOR_POSITION_TOP_LEFT);
		// because we added WINDOW_LINES number of lines, and ended it with a \r\n the first
		// line we added is now in the scrollback, so the cursor is at line 1
		assertAll(() -> assertCursorLocation(1, 0), () -> assertTextEquals(expected));
		run("Bye           \r\n");
		expected.set(1, "Bye");
		assertAll(() -> assertCursorLocation(2, 0), () -> assertTextEquals(expected));
		run(CURSOR_POSITION_TOP_LEFT);
		assertAll(() -> assertCursorLocation(1, 0), () -> assertTextEquals(expected));
		run(CURSOR_POSITION(2, 2));
		assertAll(() -> assertCursorLocation(2, 1), () -> assertTextEquals(expected));
	}

	@Test
	public void testTitle() {
		run( //
				TITLE("TITLE1"), //
				"HELLO", //
				TITLE("TITLE2"));
		assertAll(() -> assertTextEquals("HELLO"),
				() -> assertEquals(List.of("TITLE1", "TITLE2"), control.getAllTitles()));
	}

	@Test
	public void testE3ClearScreenAndScrollback() {
		data.setMaxHeight(1000);
		List<String> expected = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			String line = "Hello " + i;
			run(line + "\r\n");
			expected.add(line);
		}
		expected.remove(0);
		assertAll(() -> assertCursorLocation(999, 0), () -> assertTextEquals(expected));
		run(CLEAR_ENTIRE_SCREEN_AND_SCROLLBACK);
		assertAll(() -> assertTextEquals(""));
	}

	/**
	 * Runs what "clear" command does on modern Linux installs, including E3 extension
	 */
	@Test
	public void testClear() {
		data.setMaxHeight(1000);
		List<String> expected = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			String line = "Hello " + i;
			run(line + "\r\n");
			expected.add(line);
		}
		expected.remove(0);
		assertAll(() -> assertCursorLocation(999, 0), () -> assertTextEquals(expected));
		run(CURSOR_POSITION_TOP_LEFT + CLEAR_ENTIRE_SCREEN + CLEAR_ENTIRE_SCREEN_AND_SCROLLBACK);
		assertAll(() -> assertCursorLocation(0, 0), () -> assertTextEquals(""));
	}

	/**
	 * Runs what "up arrow" would send back to terminal in less/man/etc.
	 */
	@Test
	public void testScrollReverseNoScrollback() {
		List<String> expected = new ArrayList<>();
		for (int i = 0; i < WINDOW_LINES; i++) {
			String line = "Hello " + i;
			run(line);
			expected.add(line);
			if (i != data.getHeight() - 1) {
				run("\r\n");
			}
		}
		assertAll(() -> assertCursorLocation(WINDOW_LINES - 1, expected.get(expected.size() - 1).length()),
				() -> assertTextEquals(expected));
		run(CURSOR_POSITION_TOP_LEFT);
		assertAll(() -> assertCursorLocation(0, 0), () -> assertTextEquals(expected));
		run(SCROLL_REVERSE);
		expected.add(0, "");
		expected.remove(expected.size() - 1);
		assertAll(() -> assertCursorLocation(0, 0), () -> assertTextEquals(expected));
	}

	/**
	 * Runs what "up arrow" would send back to terminal in less/man/etc.
	 */
	@Test
	public void testScrollReverse() {
		data.setMaxHeight(1000);
		List<String> expected = new ArrayList<>();
		for (int i = 0; i < WINDOW_LINES; i++) {
			String line = "Hello " + i;
			run(line);
			expected.add(line);
			run("\r\n");
		}
		assertAll(() -> assertCursorLocation(WINDOW_LINES, 0), () -> assertTextEquals(expected));
		run(CURSOR_POSITION_TOP_LEFT);
		assertAll(() -> assertCursorLocation(1, 0), () -> assertTextEquals(expected));
		run(SCROLL_REVERSE);
		expected.add(1, "");
		assertAll(() -> assertCursorLocation(1, 0), () -> assertTextEquals(expected));
		run("New text on top line following scroll reverse");
		expected.set(1, "New text on top line following scroll reverse");
		assertAll(() -> assertCursorLocation(1, expected.get(1).length()), () -> assertTextEquals(expected));
	}

	/**
	 * Runs what "up arrow" would send back to terminal in less/man/etc.
	 * but with a scrolling region set
	 */
	@Test
	public void testScrollReverseScrollingRegion() {
		data.setMaxHeight(1000);
		List<String> expected = new ArrayList<>();
		for (int i = 0; i < WINDOW_LINES; i++) {
			String line = "Hello " + i;
			run(line);
			expected.add(line);
			run("\r\n");
		}
		assertAll(() -> assertCursorLocation(WINDOW_LINES, 0), () -> assertTextEquals(expected));
		run(CURSOR_POSITION_TOP_LEFT + "\n");
		assertAll(() -> assertCursorLocation(2, 0), () -> assertTextEquals(expected));
		run(SCROLL_REGION(2, WINDOW_LINES));
		run(SCROLL_REVERSE);
		expected.add(2, "");
		assertAll(() -> assertCursorLocation(2, 0), () -> assertTextEquals(expected));
		run("New text on top line following scroll reverse");
		expected.set(2, "New text on top line following scroll reverse");
		assertAll(() -> assertCursorLocation(2, expected.get(2).length()), () -> assertTextEquals(expected));
	}

}
