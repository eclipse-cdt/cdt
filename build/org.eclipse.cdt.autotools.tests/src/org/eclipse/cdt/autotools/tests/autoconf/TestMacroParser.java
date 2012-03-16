/*******************************************************************************
 * Copyright (c) 2008 Nokia Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ed Swartz (Nokia) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.tests.autoconf;

import org.eclipse.cdt.autotools.ui.editors.AutoconfEditorMessages;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfIfElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfMacroArgumentElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfMacroElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfParser;


/**
 * Test parsing with macros
 * @author eswartz
 *
 */
public class TestMacroParser extends BaseParserTest {

	public void testEmpty() throws Exception {
		parse("");
	}
	
	public void testComments() throws Exception {
		// 
		String text = 
			"dnl first line\n" +
			"dnl second line\n";
		AutoconfElement root = parse(text);
		Object[] kids = root.getChildren();
		// these are stripped
		assertEquals(0, kids.length);
		assertTreeStructure(root, new String[] {});
	}
	
	public void testMacroParsing1() throws Exception {
		// 
		String text =
			"AC_REQUIRE([AM_SANITY_CHECK])\n" + 
			"";
		AutoconfElement root = parse(text);
		assertTreeStructure(root, new String[] { "AC_REQUIRE", "AM_SANITY_CHECK", null });
		
		AutoconfElement[] kids = root.getChildren();
		assertEquals(1, kids.length);
		assertTrue(kids[0] instanceof AutoconfMacroElement);
		AutoconfMacroElement macro = (AutoconfMacroElement) kids[0];
		assertEquals("AC_REQUIRE", macro.getName());
		assertEquals(1, macro.getParameterCount());
		
		AutoconfElement[] args = macro.getChildren();
		assertEquals(1, args.length);
		assertTrue(args[0] instanceof AutoconfMacroArgumentElement);
		
		assertEquals("AM_SANITY_CHECK", ((AutoconfMacroArgumentElement)args[0]).getName());
		assertEquals("AM_SANITY_CHECK", macro.getParameter(0));
		
		// keep quotes in source
		assertEqualSource("[AM_SANITY_CHECK]", args[0]);
		assertEqualSource("AC_REQUIRE([AM_SANITY_CHECK])", macro);
	}
	
	public void testMacroParsing2() throws Exception {
		// 
		String text =
			"AC_TWO_ARGS(first,second)\n" + 
			"";
		AutoconfElement root = parse(text);
		assertTreeStructure(root, new String[] { "AC_TWO_ARGS", "first", "second", null });
		
		AutoconfElement[] kids = root.getChildren();
		assertEquals(1, kids.length);
		assertTrue(kids[0] instanceof AutoconfMacroElement);
		AutoconfMacroElement macro = (AutoconfMacroElement) kids[0];
		assertEquals("AC_TWO_ARGS", macro.getName());
		assertEquals(2, macro.getParameterCount());
		
		AutoconfElement[] args = macro.getChildren();
		assertEquals(2, args.length);
		assertTrue(args[0] instanceof AutoconfMacroArgumentElement);
		assertEquals("first", ((AutoconfMacroArgumentElement)args[0]).getName());
		assertTrue(args[1] instanceof AutoconfMacroArgumentElement);
		assertEquals("second", ((AutoconfMacroArgumentElement)args[1]).getName());
		assertEquals("first", macro.getParameter(0));
		assertEquals("second", macro.getParameter(1));
		
		assertEqualSource("first", args[0]);
		assertEqualSource("second", args[1]);
		assertEqualSource("AC_TWO_ARGS(first,second)", macro);
	}
	
	public void testMacroParsing3() throws Exception {
		// 
		String text =
			"AC_ONE_ARG( [quoted( arg ), second] )\n" + 
			"";
		AutoconfElement root = parse(text);
		assertTreeStructure(root, new String[] { "AC_ONE_ARG", "quoted( arg ), second", null });
		
		AutoconfElement[] kids = root.getChildren();
		assertEquals(1, kids.length);
		assertTrue(kids[0] instanceof AutoconfMacroElement);
		AutoconfMacroElement macro = (AutoconfMacroElement) kids[0];
		assertEquals("AC_ONE_ARG", macro.getName());
		
		AutoconfElement[] args = macro.getChildren();
		assertEquals(1, macro.getParameterCount());
		assertEquals(1, args.length);
		assertTrue(args[0] instanceof AutoconfMacroArgumentElement);
		
		// spaces removed from outermost arguments, but not inner
		assertEquals("quoted( arg ), second", ((AutoconfMacroArgumentElement)args[0]).getName());
		assertEquals("quoted( arg ), second", macro.getParameter(0));
		
		assertEqualSource("[quoted( arg ), second]", args[0]);
		assertEqualSource("AC_ONE_ARG( [quoted( arg ), second] )", macro);
	}

	public void testMacroParsing4() throws Exception {
		// 
		String text =
			"AC_DEFUN([AM_SET_CURRENT_AUTOMAKE_VERSION],\r\n" + 
			"         [AM_AUTOMAKE_VERSION([1.4-p6])])\r\n" + 
			"\r\n" + 
			"";
		AutoconfElement root = parse(text);
		AutoconfElement[] kids = root.getChildren();
		assertEquals(1, kids.length);
		assertTrue(kids[0] instanceof AutoconfMacroElement);
		AutoconfMacroElement macro = (AutoconfMacroElement) kids[0];
		assertEquals("AC_DEFUN", macro.getName());

		// spaces and quotes dropped
		assertEquals(2, macro.getParameterCount());
		assertEquals("AM_SET_CURRENT_AUTOMAKE_VERSION", macro.getParameter(0));
		assertEquals("AM_AUTOMAKE_VERSION([1.4-p6])", macro.getParameter(1));
		
		// no spaces in source either, but quotes kept
		assertEqualSource("[AM_SET_CURRENT_AUTOMAKE_VERSION]", macro.getChildren()[0]);
		assertEqualSource("[AM_AUTOMAKE_VERSION([1.4-p6])]", macro.getChildren()[1]);
	}

	public void testMacroParsing5() throws Exception {
		// check that complex shell constructs don't throw off the
		// parser, and also that we don't mistake shell tokens in a macro argument
		String arg2 =
			"AC_MSG_CHECKING(for working $2)\n" + 
			"# Run test in a subshell; some versions of sh will print an error if\n" + 
			"# an executable is not found, even if stderr is redirected.\n" + 
			"# Redirect stdin to placate older versions of autoconf.  Sigh.\n" + 
			"if ($2 --version) < /dev/null > /dev/null 2>&1; then\n" + 
			"   $1=$2\n" + 
			"   AC_MSG_RESULT(found)\n" + 
			"else\n" + 
			"   $1=\"$3/missing $2\"\n" + 
			"   AC_MSG_RESULT(missing)\n" + 
			"fi\n" + 
			"AC_SUBST($1)";
		String text =
			"AC_DEFUN([AM_MISSING_PROG],\n" + 
			"[" + arg2 + "])\n" + 
			"";
		AutoconfElement root = parse(text);
		assertTreeStructure(root, new String[] {
				"AC_DEFUN", 
					"AM_MISSING_PROG", 
					arg2, 
					null,
		});
		AutoconfElement[] kids = root.getChildren();
		assertEquals(1, kids.length);
		assertTrue(kids[0] instanceof AutoconfMacroElement);
		AutoconfMacroElement macro = (AutoconfMacroElement) kids[0];
		assertEquals("AC_DEFUN", macro.getName());
		assertEquals("AM_MISSING_PROG", macro.getVar());

		// spaces dropped
		assertEquals(2, macro.getParameterCount());
		assertEquals("AM_MISSING_PROG", macro.getParameter(0));
		
		// be sure complex arguments aren't mangled
		assertEquals(arg2, macro.getParameter(1));
		assertEqualSource("[" + arg2 + "]", macro.getChildren()[1]);
		
	}

	public void testMacroParsing6() throws Exception {
		// empty arguments
		String text =
			"AC_DEFUN( ,\n" +
			")\n"; 
		AutoconfElement root = parse(text);
		AutoconfElement[] kids = root.getChildren();
		assertEquals(1, kids.length);
		assertTrue(kids[0] instanceof AutoconfMacroElement);
		AutoconfMacroElement macro = (AutoconfMacroElement) kids[0];
		assertEquals("AC_DEFUN", macro.getName());
		assertEquals("", macro.getVar());

		// spaces dropped
		assertEquals(2, macro.getParameterCount());
		assertEquals("", macro.getParameter(0));
		assertEquals("", macro.getParameter(1));

		assertEqualSource("", macro.getChildren()[0]);
		assertEqualSource("", macro.getChildren()[1]);
	}

	public void testWithErrorUnmatchedLeftParen() {
		String text =
			"AC_BAD_MACRO(\n";
		
		AutoconfElement root = parse(text, true);
		assertEquals(1, root.getChildren().length);
		assertTrue(root.getChildren()[0] instanceof AutoconfMacroElement);
		checkError(AutoconfEditorMessages.getString(AutoconfParser.UNMATCHED_LEFT_PARENTHESIS));
	}
	public void testWithErrorUnmatchedRightParen() {
		String text =
			"AC_BAD_MACRO())\n";
		
		AutoconfElement root = parse(text, true);
		assertEquals(1, root.getChildren().length);
		assertTrue(root.getChildren()[0] instanceof AutoconfMacroElement);
		checkError(AutoconfEditorMessages.getString(AutoconfParser.UNMATCHED_RIGHT_PARENTHESIS));
	}
	public void testNoFalseUnmatchedRightParen() {
		String text =
			"AC_BAD_MACRO()\n" +
			"(\n"+
			"cd foo;\n"+
			"if test -f myfile; then exit 1; fi\n"+
			")\n";
		
		// nothing but the macro and 'if' is detected as meaningful 
		AutoconfElement root = parse(text);
		assertEquals(2, root.getChildren().length);
		assertTrue(root.getChildren()[0] instanceof AutoconfMacroElement);
		assertTrue(root.getChildren()[1] instanceof AutoconfIfElement);
	}

	public void testNestedMacro() {
		String text =
			"AC_1(AC_2())\n";
		
		AutoconfElement root = parse(text);
		assertEquals(1, root.getChildren().length);
		assertTrue(root.getChildren()[0] instanceof AutoconfMacroElement);
		AutoconfMacroElement ac1 = (AutoconfMacroElement) root.getChildren()[0];
		assertEquals(1, ac1.getChildren().length);
		assertTrue(ac1.getChildren()[0] instanceof AutoconfMacroArgumentElement);
		AutoconfMacroArgumentElement ac2 = (AutoconfMacroArgumentElement) ac1.getChildren()[0];
		// one empty argument
		assertEquals(1, ac2.getChildren().length);
	}

}
