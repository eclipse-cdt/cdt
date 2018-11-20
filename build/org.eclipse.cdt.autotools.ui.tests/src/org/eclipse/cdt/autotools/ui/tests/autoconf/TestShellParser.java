/*******************************************************************************
 * Copyright (c) 2008, 2015 Nokia Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ed Swartz (Nokia) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.tests.autoconf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.autotools.ui.editors.AutoconfEditorMessages;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfCaseConditionElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfCaseElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfParser;
import org.junit.Test;

/**
 * @author eswartz
 *
 */
public class TestShellParser extends BaseParserTest {

	@Test
	public void testHERE() {
		String HERE_TEXT = "\n" + "while true; do \n" + "AM_INIT_AUTOMAKE([confusion], [$2], EOF)\n" + "done\n";
		String text = "cat <<EOF" + HERE_TEXT + "EOF\n" + "blah";
		AutoconfElement tree = parse(text);

		// only see a macro call, not a loop
		assertTreeStructure(tree, new String[] { "AM_INIT_AUTOMAKE", "confusion", "$2", "EOF", null });
	}

	@Test
	public void testHERE2() {
		String HERE_TEXT = "\n" + "while true; do \n" + "AM_INIT_AUTOMAKE([confusion], [$2], EOF)\n" + "done\n";
		String text = "cat <<-EOF" + HERE_TEXT + "EOF\n" + "blah";
		AutoconfElement tree = parse(text);

		// only see a macro call, not a loop
		assertTreeStructure(tree, new String[] { "AM_INIT_AUTOMAKE", "confusion", "$2", "EOF", null });
	}

	@Test
	public void testIf0() {
		String text = "# comment\n" + "\tif true; then\n" + "\t\tfoo;\n" + "\tfi\n";
		AutoconfElement tree = parse(text);
		assertTreeStructure(tree, new String[] { "if" });
	}

	@Test
	public void testIf1() {
		String text = "# comment\n" + "\tif true; then\n" + "\t\tAC_SOMETHING();\n" + "\tfi\n";
		AutoconfElement tree = parse(text);
		assertTreeStructure(tree, new String[] { "if", "AC_SOMETHING", "", null, null });
	}

	@Test
	public void testIfElse0() {
		String text = "# comment\n" + "\tif true; then\n" + "\t\tfoo;\n" + "\telse\n" + "\t\tbar;\n" + "\tfi\n";
		AutoconfElement tree = parse(text);
		assertTreeStructure(tree, new String[] { "if", "else", null });
	}

	@Test
	public void testIfElse1() {
		String text = "# comment\n" + "\tif true; then\n" + "\t\tAC_ONE(...);\n" + "\telse\n"
				+ "\t\tAC_TWO(AC_THREE());\n" + "\tfi\n";
		AutoconfElement tree = parse(text);
		assertTreeStructure(tree, new String[] { "if", "AC_ONE", "...", null, "else", "AC_TWO", "AC_THREE", "AC_THREE",
				"", null, null, null, null, null });

		AutoconfElement[] kids = tree.getChildren();
		assertEqualSource("AC_ONE(...)", kids[0].getChildren()[0]);
		assertEqualSource("AC_TWO(AC_THREE())", kids[0].getChildren()[1].getChildren()[0]);
	}

	@Test
	public void testIf2() {
		String text = "if blah\n" + "then fi\n";
		AutoconfElement tree = parse(text);
		assertTreeStructure(tree, new String[] { "if" });
	}

	@Test
	public void testIfElif() {
		String text = "# comment\n" + "\tif true; then\n" + "\t\tAC_ONE(...);\n" + "\telif false; then \n"
				+ "\t\tAC_TWO(...);\n" + "\tfi\n";
		AutoconfElement tree = parse(text);
		assertTreeStructure(tree,
				new String[] { "if", "AC_ONE", "...", null, "elif", "AC_TWO", "...", null, null, null });
	}

	@Test
	public void testIfErr1() {
		String text = "if then fi\n";
		AutoconfElement tree = parse(text, true);
		assertEquals(1, errors.size());
		checkError(AutoconfEditorMessages.getFormattedString(AutoconfParser.INVALID_SPECIFIER, "then"));
		assertTreeStructure(tree, new String[] { "if" });
	}

	@Test
	public void testIfErr2() {
		String text = "if true; do fi\n";
		AutoconfElement tree = parse(text, true);
		assertEquals(1, errors.size());
		checkError(AutoconfEditorMessages.getString(AutoconfParser.INVALID_DO));
		assertTreeStructure(tree, new String[] { "if" });
	}

	@Test
	public void testIfErr3() {
		String text = "if; else bar; fi\n";
		AutoconfElement tree = parse(text, true);
		assertEquals(1, errors.size());
		checkError(AutoconfEditorMessages.getFormattedString(AutoconfParser.MISSING_SPECIFIER, "then"));
		assertTreeStructure(tree, new String[] { "if", "else", null });
	}

	@Test
	public void testIfErr4() {
		String text = "if true; then stmt fi\n";
		AutoconfElement tree = parse(text, true);
		assertEquals(1, errors.size());
		checkError(AutoconfEditorMessages.getFormattedString(AutoconfParser.INVALID_TERMINATION, "fi"));
		assertTreeStructure(tree, new String[] { "if" });
	}

	@Test
	public void testIfErr5() {
		String text = "if true; then\n";
		AutoconfElement tree = parse(text, true);
		assertEquals(1, errors.size());
		checkError(AutoconfEditorMessages.getFormattedString(AutoconfParser.UNTERMINATED_CONSTRUCT, "if"));
		assertTreeStructure(tree, new String[] { "if" });
	}

	@Test
	public void testIfErr6() {
		String text = "if true; then foo; else\n";
		AutoconfElement tree = parse(text, true);
		assertEquals(2, errors.size());
		checkError(AutoconfEditorMessages.getFormattedString(AutoconfParser.UNTERMINATED_CONSTRUCT, "if"));
		checkError(AutoconfEditorMessages.getFormattedString(AutoconfParser.UNTERMINATED_CONSTRUCT, "else"));
		assertTreeStructure(tree, new String[] { "if", "else", null });
	}

	@Test
	public void testWhile() {
		String text = "while true; do foo; done\n";
		AutoconfElement tree = parse(text);
		assertTreeStructure(tree, new String[] { "while" });
	}

	@Test
	public void testWhile2() {
		String text = "while true\n" + "do\n" + "AC_SOMETHING(...); done\n";
		AutoconfElement tree = parse(text);
		assertTreeStructure(tree, new String[] { "while", "AC_SOMETHING", "...", null, null });
	}

	@Test
	public void testWhileErr() {
		String text = "while; AC_SOMETHING(...) done\n";
		AutoconfElement tree = parse(text, true);
		assertEquals(2, errors.size());
		checkError(AutoconfEditorMessages.getFormattedString(AutoconfParser.MISSING_SPECIFIER, "do"));
		checkError(AutoconfEditorMessages.getFormattedString(AutoconfParser.INVALID_TERMINATION, "done"));
		assertTreeStructure(tree, new String[] { "while", "AC_SOMETHING", "...", null, null });
	}

	@Test
	public void testWhileErr2() {
		String text = "while true; do AC_SOMETHING(...)\n";
		AutoconfElement tree = parse(text, true);
		assertEquals(1, errors.size());
		checkError(AutoconfEditorMessages.getFormattedString(AutoconfParser.UNTERMINATED_CONSTRUCT, "while"));
		assertTreeStructure(tree, new String[] { "while", "AC_SOMETHING", "...", null, null });
	}

	@Test
	public void testCase() {
		String text = "case $VAL in\n" + "linux-*-*) AC_FIRST($VAL) ; true ;;\n" + "bsd-* | macosx-*) : ;;\n"
				+ "*) echo \"I dunno $VAL\";;\n" + "esac\n";
		AutoconfElement tree = parse(text, false);
		assertTreeStructure(tree,
				new String[] { "case", "linux-*-*", "AC_FIRST", "$VAL", null, null, "bsd-* | macosx-*", "*", null });

		AutoconfElement[] kids = tree.getChildren();
		assertEquals(1, kids.length);
		assertTrue(kids[0] instanceof AutoconfCaseElement);

		AutoconfCaseElement caseEl = (AutoconfCaseElement) kids[0];
		assertEqualSource(text.substring(0, text.length() - 1), caseEl);

		assertEquals(3, caseEl.getChildren().length);

		assertTrue(caseEl.getChildren()[0] instanceof AutoconfCaseConditionElement);
		assertTrue(caseEl.getChildren()[1] instanceof AutoconfCaseConditionElement);
		assertTrue(caseEl.getChildren()[2] instanceof AutoconfCaseConditionElement);

		AutoconfCaseConditionElement caseCond = (AutoconfCaseConditionElement) caseEl.getChildren()[0];
		assertEquals("linux-*-*) AC_FIRST($VAL) ; true ;;", caseCond.getSource());
		assertEquals(1, caseCond.getChildren().length);
		assertEqualSource("AC_FIRST($VAL)", caseCond.getChildren()[0]);

		caseCond = (AutoconfCaseConditionElement) caseEl.getChildren()[1];
		assertEquals("bsd-* | macosx-*) : ;;", caseCond.getSource());
		assertEquals(0, caseCond.getChildren().length);

		caseCond = (AutoconfCaseConditionElement) caseEl.getChildren()[2];
		assertEquals("*) echo \"I dunno $VAL\";;", caseCond.getSource());
		assertEquals(0, caseCond.getChildren().length);

	}

	@Test
	public void testCaseErr() {
		String text = "case $VAL; linux-*-*) AC_FIRST($VAL) ; true esac\n";
		AutoconfElement tree = parse(text, true);
		assertEquals(2, errors.size());
		checkError(AutoconfEditorMessages.getString(AutoconfParser.INVALID_IN));
		checkError(AutoconfEditorMessages.getFormattedString(AutoconfParser.INVALID_TERMINATION, "esac"));

		assertTreeStructure(tree, new String[] { "case", "linux-*-*", "AC_FIRST", "$VAL", null, null, null });

		AutoconfElement[] kids = tree.getChildren();
		AutoconfCaseElement caseEl = (AutoconfCaseElement) kids[0];
		AutoconfCaseConditionElement caseCond = (AutoconfCaseConditionElement) caseEl.getChildren()[0];
		// goofed up, but ok
		assertEquals("linux-*-*) AC_FIRST($VAL) ; true esac", caseCond.getSource());
		assertEqualSource("AC_FIRST($VAL)", caseCond.getChildren()[0]);

	}

	@Test
	public void testCaseErr2() {
		String text = "case $VAL in\n";
		AutoconfElement tree = parse(text, true);
		assertEquals(1, errors.size());
		checkError(AutoconfEditorMessages.getFormattedString(AutoconfParser.UNTERMINATED_CONSTRUCT, "case"));

		assertTreeStructure(tree, new String[] { "case" });

	}

	@Test
	public void testForIn() {
		// don't get upset by 'in'
		String text = "for VAL in 1 2 3 4; do echo $VAL; done\n";
		AutoconfElement tree = parse(text, false);

		assertTreeStructure(tree, new String[] { "for" });
		AutoconfElement[] kids = tree.getChildren();

		AutoconfElement forEl = kids[0];
		assertEqualSource(text.substring(0, text.length() - 1), forEl);

	}

	@Test
	public void testForDo() {
		// don't get upset by parentheses
		String text = "for (( AC_1; AC_2(); AC_3(...) )); do echo $VAL; done\n";
		AutoconfElement tree = parse(text, false);

		assertTreeStructure(tree, new String[] { "for", "AC_1", "AC_2", "", null, "AC_3", "...", null, null });

	}

	@Test
	public void testUntil() {
		String text = "until false; do AC_SOMETHING(...); done\n";
		AutoconfElement tree = parse(text, false);
		assertTreeStructure(tree, new String[] { "until", "AC_SOMETHING", "...", null, null });
	}

	@Test
	public void testSelect() {
		String text = "select VAR in 1 2 3; do AC_SOMETHING(...); done\n" + "select VAR; do AC_SOMETHING; done\n";

		AutoconfElement tree = parse(text, false);
		assertTreeStructure(tree,
				new String[] { "select", "AC_SOMETHING", "...", null, null, "select", "AC_SOMETHING", null });
	}

	@Test
	public void testComplex1() {
		String text = "AM_INIT_AUTOMAKE([foo1], 1.96)\n" + "while true; do \n" + "	var=shift;\n"
				+ "	if [ test -f \"$var\"] ; then\n" + "		AC_SOMETHING($var);\n" + "	fi\n" + "done;\n"
				+ "AM_GENERATE(Makefile)\n";
		AutoconfElement tree = parse(text, false);
		assertTreeStructure(tree, new String[] { "AM_INIT_AUTOMAKE", "foo1", "1.96", null, "while", "if",
				"AC_SOMETHING", "$var", null, null, null, "AM_GENERATE", "Makefile", null });
	}

	@Test
	public void testComplex2() {
		String text = "if true; then\n" + "AC_CANONICAL_HOST\n" + "else\n" + "case foo in \n" + "	3) 1 ;;\n"
				+ "esac;\n" + "fi\n";
		AutoconfElement tree = parse(text, false);
		assertTreeStructure(tree, new String[] { "if", "AC_CANONICAL_HOST", "else", "case", "3", null, null, null });
	}

	@Test
	public void testEarlyClose() {
		String text = "if true; then foo ; fi\n" + "fi\n" + "while true; do done;\n";
		AutoconfElement tree = parse(text, true);
		assertTreeStructure(tree, new String[] { "if", "while" });
	}

	@Test
	public void testOverlapping() {
		String text = "for foo\n" + "if bar\n";
		AutoconfElement tree = parse(text, true);
		assertTreeStructure(tree, new String[] { "for", "if" });
	}

	@Test
	public void testDollar() {
		// dollars guard keywords
		String text = "if [ $if == 3 ] ; then $for; fi\n";
		AutoconfElement tree = parse(text);
		assertTreeStructure(tree, new String[] { "if" });
	}
}
