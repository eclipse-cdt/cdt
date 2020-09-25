/*******************************************************************************
 * Copyright (c) 2015-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.eclipse.cdt.cmake.is.core.participant.Arglets;
import org.eclipse.cdt.cmake.is.core.participant.Arglets.MacroDefine_C_POSIX;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class MacroDefine_C_POSIXTest {

	private MacroDefine_C_POSIX testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new MacroDefine_C_POSIX();
	}

	/**
	 * Test method for {@link Arglets.MacroDefine_C_POSIX#processArgument}.
	 */
	@Test
	public final void testProcessArgument() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result;

		final IPath cwd = new Path("");
		int len;
		// -DFOO
		String name = "FOO";
		String arg = "-D" + name;

		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", "", result.getDefines().get(name));
		assertEquals(2 + name.length(), len);
		// -D FOO
		result = new ParseContext();
		arg = "-D   " + name;
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", "", result.getDefines().get(name));
		assertEquals(2 + name.length() + 3, len);
	}

	/**
	 * Test method for {@link Arglets.MacroDefine_C_POSIX#processArgument}.
	 */
	@Test
	public final void testProcessArgument_Value() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result;
		final IPath cwd = new Path("");

		final String name = "FOO";
		String val = "noWhiteSpace";

		// -DFOO=noWhiteSpace
		result = new ParseContext();
		assertEquals(2 + name.length() + 1 + val.length(),
				testee.processArgument(result, cwd, "-D" + name + "=" + val + more));
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		// -D FOO=noWhiteSpace
		result = new ParseContext();
		assertEquals(2 + name.length() + 1 + 3 + val.length(),
				testee.processArgument(result, cwd, "-D   " + name + "=" + val + more));
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));

		// ----------------------------------------
		val = "Wh it e s ap ac ";
		// -D 'FOO=Wh it e s ap ac '
		result = new ParseContext();
		assertEquals(2 + name.length() + 1 + 2 + 3 + val.length(),
				testee.processArgument(result, cwd, "-D   " + "'" + name + "=" + val + "'" + more));
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		// -D "FOO=Wh it e s ap ac "
		result = new ParseContext();
		assertEquals(2 + name.length() + 1 + 2 + 3 + val.length(),
				testee.processArgument(result, cwd, "-D   " + "\"" + name + "=" + val + "\"" + more));
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
	}

	/**
	 * Test method for {@link Arglets.MacroDefine_C_POSIX#processArgument}.
	 */
	@Test
	public final void testProcessArgument_Value_CharLiteral() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result;
		final IPath cwd = new Path("");
		int len;
		String val, arg;

		final String name = "FOO";

		// ----------------------------------------
		// -DFOO='noWhiteSpace'
		val = "'noWhiteSpace'";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// -D FOO='noWhiteSpace'
		arg = "-D   " + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// ----------------------------------------
		// -DFOO='noWhite\'escapedQuoteChar' (values with single quotes)
		val = "'noWhite\\'escapedQuoteChar'";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// -DFOO='noWhite\\escapedEscapeChar'
		val = "'noWhite\\\\escapedEscapeChar'";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// ----------------------------------------
		// -DFOO='Wh it e s ap ac '
		val = "'Wh it e s ap ac '";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// -D FOO='Wh it e s ap ac '
		arg = "-D   " + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// -DFOO='Wh it e s ap ac \'escaped Quote Char' (values with single quotes)
		val = "'Wh it e s ap ac \\'escaped Quote Char'";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// -DFOO='Wh it e s ap ac \\escaped Escape Char'
		val = "'Wh it e s ap ac \\\\escaped Escape Char'";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
	}

	/**
	 * Test method for {@link Arglets.MacroDefine_C_POSIX#processArgument}.
	 */
	@Test
	public final void testProcessArgument_Value_StringLiteral() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result;
		final IPath cwd = new Path("");
		int len;
		String val, arg;

		final String name = "FO$O";
		// ----------------------------------------
		// -DFOO="noWhiteSpace"
		val = "\"noWhiteSpace\"";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// -D FOO="noWhiteSpace"
		arg = "-D   " + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// ----------------------------------------
		// -DFOO="noWhite\"escapedQuoteChar" (values with single quotes)
		val = "\"noWhite\\\"escapedQuoteChar\"";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// -DFOO="noWhite\\escapedEscapeChar"
		val = "\"noWhite\\\\escapedEscapeChar\"";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// ----------------------------------------
		// -DFOO="Wh it e s ap ac "
		val = "\"Wh it e s ap ac \"";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// -D FOO="Wh it e s ap ac "
		arg = "-D   " + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// -DFOO="Wh it e s ap ac \"escaped Quote Char" (values with single quotes)
		val = "\"Wh it e s ap ac \\\"escaped Quote Char\"";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);
		// -DFOO="Wh it e s ap ac \\escaped Escape Char"
		val = "\"Wh it e s ap ac \\\\escaped Escape Char\"";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
		assertEquals(arg.length(), len);

		// ----------------------------------------
		// special case
		// -DFOO="2(vtkIOMySQL,vtkIOPostgreSQL)"
		val = "\"2(vtkIOMySQL,vtkIOPostgreSQL)\"";
		arg = "-D" + name + "=" + val;
		result = new ParseContext();
		len = testee.processArgument(result, cwd, arg + " " + arg + more);
		assertEquals(arg.length(), len);
		assertTrue("found", result.getDefines().containsKey(name));
		assertEquals("value", val, result.getDefines().get(name));
	}

	/**
	 * Test method for {@link Arglets.MacroDefine_C_POSIX#processArgument}.
	 */
	@Test
	public final void testProcessArgument_MacroWithArgs() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result;
		final IPath cwd = new Path("");

		// -DFOO=noWhiteSpace
		final String name = "FOO";
		final String args = "(a,b,c)";
		String val = "(a)/((b)+(c))";

		// -DFOO(a,b,c)=(a)/((b)+(c))
		result = new ParseContext();
		assertEquals(2 + name.length() + args.length() + 1 + val.length(),
				testee.processArgument(result, cwd, "-D" + name + args + "=" + val + more));
		Map<String, String> defines = result.getDefines();
		assertEquals("#entries", 1, defines.size());
		assertEquals("value", val, result.getDefines().get(name));
		// -D FOO(a,b,c)=(a)/((b)+(c))
		result = new ParseContext();
		assertEquals(2 + name.length() + args.length() + 1 + 3 + val.length(),
				testee.processArgument(result, cwd, "-D   " + name + args + "=" + val + more));
		defines = result.getDefines();
		assertEquals("#entries", 1, defines.size());
		assertEquals("value", val, result.getDefines().get(name));
	}
}
