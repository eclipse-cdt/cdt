package org.eclipse.cdt.utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CommandLineUtilTest extends TestCase {

    public static Test suite() {
        return new TestSuite(CommandLineUtilTest.class);
    }
	public void testArgumentsToArraySimple() {
		// [A=B C]
		String[] args = CommandLineUtil.argumentsToArray("A=B C");
		assertEquals(2, args.length);
		assertEquals("A=B", args[0]);
		assertEquals("C", args[1]);
	}

	public void testArgumentsToArraySpaces() {
		// [A=B    C]
		String[] args = CommandLineUtil.argumentsToArray("A=B    C");
		assertEquals(2, args.length);
		assertEquals("A=B", args[0]);
		assertEquals("C", args[1]);
	}

	public void testArgumentsToArraySpaces2() {
		// [  A=B    C ]
		String[] args = CommandLineUtil.argumentsToArray("  A=B    C ");
		assertEquals(2, args.length);
		assertEquals("A=B", args[0]);
		assertEquals("C", args[1]);
	}

	public void testArgumentsToArrayDoubleQuotes() {
		// [Arg="a b c"]
		String[] args = CommandLineUtil.argumentsToArray("Arg=\"a b c\"");
		assertEquals(1, args.length);
		assertEquals("Arg=a b c", args[0]);
	}

	public void testArgumentsToArrayDoubleQuotes2() {
		// [Arg="\"quoted\""]
		String[] args = CommandLineUtil.argumentsToArray("Arg=\"\\\"quoted\\\"\"");
		assertEquals(1, args.length);
		assertEquals("Arg=\"quoted\"", args[0]);
	}

	public void testArgumentsToArraySingleQuotes() {
		// [Arg='"quoted"']
		String[] args = CommandLineUtil.argumentsToArray("Arg='\"quoted\"'");
		assertEquals(1, args.length);
		assertEquals("Arg=\"quoted\"", args[0]);
	}

	public void testArgumentsToArrayQuote() {
		// [\"]
		String[] args = CommandLineUtil.argumentsToArray("\\\"");
		assertEquals(1, args.length);
		assertEquals("\"", args[0]);
	}

	public void testArgumentsToArrayQuotSpaces() {
		// [  \"]
		String[] args = CommandLineUtil.argumentsToArray("  \\\"");
		assertEquals(1, args.length);
		assertEquals("\"", args[0]);
	}

	public void testArgumentsToArrayOnlySpaces() {
		// ["   "]
		String[] args = CommandLineUtil.argumentsToArray("\"   \"");
		assertEquals(1, args.length);
		assertEquals("   ", args[0]);
	}

	public void testArgumentsToArrayJumbledString() {
		// ["a b"-c]
		String[] args = CommandLineUtil.argumentsToArray("\"a b\"-c");
		assertEquals(1, args.length);
		assertEquals("a b-c", args[0]);
	}

	public void testArgumentsToArrayJumbledString2() {
		// [x "a b"-c]
		String[] args = CommandLineUtil.argumentsToArray(" x  \"a b\"-c");
		assertEquals(2, args.length);
		assertEquals("x", args[0]);
		assertEquals("a b-c", args[1]);
	}

	public void testArgumentsToArrayJumbledSQ() {
		// [x' 'x y]
		String[] args = CommandLineUtil.argumentsToArray("x' 'x y");
		assertEquals(2, args.length);
		assertEquals("x x", args[0]);
		assertEquals("y", args[1]);
	}

	public void testArgumentsToArrayEmptyString() {
		// [""]
		String[] args = CommandLineUtil.argumentsToArray("\"\"");
		assertEquals(1, args.length);
		assertEquals("", args[0]);
	}

	public void testArgumentsToArrayEmptyString2() {
		// ['']
		String[] args = CommandLineUtil.argumentsToArray("''");
		assertEquals(1, args.length);
		assertEquals("", args[0]);
	}

	public void testArgumentsToArrayEmpty3() {
		// ['' a]
		String[] args = CommandLineUtil.argumentsToArray("'' a");
		assertEquals(2, args.length);
		assertEquals("", args[0]);
		assertEquals("a", args[1]);
	}

	public void testArgumentsToArrayQuot1() {
		// ['"']
		String[] args = CommandLineUtil.argumentsToArray("'\"'");
		assertEquals(1, args.length);
		assertEquals("\"", args[0]);
	}

	public void testArgumentsToArrayQuot2() {
		// ["\""]
		String[] args = CommandLineUtil.argumentsToArray("\"\\\"\"");
		assertEquals(1, args.length);
		assertEquals("\"", args[0]);
	}

	public void testArgumentsToArrayNull() {
		// []
		String[] args = CommandLineUtil.argumentsToArray(null);
		assertEquals(0, args.length);
	}
	public void testArgumentsToArrayEmpty() {
		// []
		String[] args = CommandLineUtil.argumentsToArray("");
		assertEquals(0, args.length);
	}
	public void testArgumentsToArrayEmptySpaces() {
		// [   ]
		String[] args = CommandLineUtil.argumentsToArray("   ");
		assertEquals(0, args.length);
	}
}
