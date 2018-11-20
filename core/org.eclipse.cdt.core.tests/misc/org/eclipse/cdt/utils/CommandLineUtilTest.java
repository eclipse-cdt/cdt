/*******************************************************************************
 *  Copyright (c) 2008, 2009 QNX Software Systems and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CommandLineUtilTest extends TestCase {

	public static Test suite() {
		return new TestSuite(CommandLineUtilTest.class);
	}

	private String[] parse(String line) {
		return CommandLineUtil.argumentsToArrayUnixStyle(line);
	}

	private String[] parseWin(String line) {
		return CommandLineUtil.argumentsToArrayWindowsStyle(line);
	}

	private String[] parseU(String line) {
		return CommandLineUtil.argumentsToArray(line);
	}

	public void testArgumentsToArraySimple() {
		String[] args = parse("A=B C");
		assertEquals(2, args.length);
		assertEquals("A=B", args[0]);
		assertEquals("C", args[1]);
	}

	public void testArgumentsToArraySpaces() {
		// [A=B    C]
		String[] args = parse("A=B    C");
		assertEquals(2, args.length);
		assertEquals("A=B", args[0]);
		assertEquals("C", args[1]);
	}

	public void testArgumentsToArraySpaces2() {
		// [  A=B    C ]
		String[] args = parse("  A=B    C ");
		assertEquals(2, args.length);
		assertEquals("A=B", args[0]);
		assertEquals("C", args[1]);
	}

	public void testArgumentsToArrayDoubleQuotes() {
		// [Arg="a b c"]
		String[] args = parse("Arg=\"a b c\"");
		assertEquals(1, args.length);
		assertEquals("Arg=a b c", args[0]);
	}

	public void testArgumentsToArrayDoubleQuotes2() {
		// [Arg="\"quoted\""]
		String[] args = parse("Arg=\"\\\"quoted\\\"\"");
		assertEquals(1, args.length);
		assertEquals("Arg=\"quoted\"", args[0]);
	}

	public void testArgumentsToArraySingleQuotes() {
		// [Arg='"quoted"']
		String[] args = parse("Arg='\"quoted\"'");
		assertEquals(1, args.length);
		assertEquals("Arg=\"quoted\"", args[0]);
	}

	public void testArgumentsToArrayQuote() {
		// [\"]
		String[] args = parse("\\\"");
		assertEquals(1, args.length);
		assertEquals("\"", args[0]);
	}

	public void testArgumentsToArrayQuotSpaces() {
		// [  \"]
		String[] args = parse("  \\\"");
		assertEquals(1, args.length);
		assertEquals("\"", args[0]);
	}

	public void testArgumentsToArrayOnlySpaces() {
		// ["   "]
		String[] args = parse("\"   \"");
		assertEquals(1, args.length);
		assertEquals("   ", args[0]);
	}

	public void testArgumentsToArrayJumbledString() {
		// ["a b"-c]
		String[] args = parse("\"a b\"-c");
		assertEquals(1, args.length);
		assertEquals("a b-c", args[0]);
	}

	public void testArgumentsToArrayJumbledString2() {
		// [x "a b"-c]
		String[] args = parse(" x  \"a b\"-c");
		assertEquals(2, args.length);
		assertEquals("x", args[0]);
		assertEquals("a b-c", args[1]);
	}

	public void testArgumentsToArrayJumbledSQ() {
		// [x' 'x y]
		String[] args = parse("x' 'x y");
		assertEquals(2, args.length);
		assertEquals("x x", args[0]);
		assertEquals("y", args[1]);
	}

	public void testArgumentsToArrayEmptyString() {
		// [""]
		String[] args = parse("\"\"");
		assertEquals(1, args.length);
		assertEquals("", args[0]);
	}

	public void testArgumentsToArrayEmptyString2() {
		// ['']
		String[] args = parse("''");
		assertEquals(1, args.length);
		assertEquals("", args[0]);
	}

	public void testArgumentsToArrayEmpty3() {
		// ['' a]
		String[] args = parse("'' a");
		assertEquals(2, args.length);
		assertEquals("", args[0]);
		assertEquals("a", args[1]);
	}

	public void testArgumentsToArrayQuot1() {
		// ['"']
		String[] args = parse("'\"'");
		assertEquals(1, args.length);
		assertEquals("\"", args[0]);
	}

	public void testArgumentsToArrayQuot2() {
		// ["\""]
		String[] args = parse("\"\\\"\"");
		assertEquals(1, args.length);
		assertEquals("\"", args[0]);
	}

	public void testArgumentsToArrayNull() {
		// []
		String[] args = parse(null);
		assertEquals(0, args.length);
	}

	public void testArgumentsToArrayEmpty() {
		// []
		String[] args = parse("");
		assertEquals(0, args.length);
	}

	public void testArgumentsToArrayEmptySpaces() {
		// [   ]
		String[] args = parse("   ");
		assertEquals(0, args.length);
	}

	public void testArgumentsToArrayTabs() {
		// [a	b]
		String[] args = parse("a \tb");
		assertEquals(2, args.length);
		assertEquals("a", args[0]);
	}

	public void testArgumentsToArrayNL() {
		// ["a\nb"]
		String[] args = parse("\"a\\nb\"");
		assertEquals(1, args.length);
		assertEquals("a\nb", args[0]);
	}

	public void testArgumentsToArraySimpleWin() {
		String[] args = parseWin("A=B C");
		assertEquals(2, args.length);
		assertEquals("A=B", args[0]);
		assertEquals("C", args[1]);
	}

	public void testArgumentsToArrayWindowsFiles() {
		String[] args = parseWin("my\\file\\path");
		assertEquals(1, args.length);
		assertEquals("my\\file\\path", args[0]);
	}

	public void testArgumentsToArrayWindowsSpaces() {
		String[] args = parseWin("\"my\\file\\path space\"");
		assertEquals(1, args.length);
		assertEquals("my\\file\\path space", args[0]);
	}

	public void testArgumentsToArrayWindowsEmpty() {
		String[] args = parseWin("\"\"");
		assertEquals(1, args.length);
		assertEquals("", args[0]);
	}

	public void testArgumentsToArrayWindowsQuotes() {
		String[] args = parseWin("\\\"a b\\\"");
		assertEquals(2, args.length);
		assertEquals("\"a", args[0]);
		assertEquals("b\"", args[1]);
	}

	public void testArgumentsToArraySimpleUniversal() {
		String[] args = parseU("A=B C   D");
		assertEquals(3, args.length);
		assertEquals("A=B", args[0]);
		assertEquals("C", args[1]);
		assertEquals("D", args[2]);
	}
}
