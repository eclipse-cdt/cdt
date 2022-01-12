/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.pro.parser.tests;

import org.eclipse.cdt.internal.qt.ui.pro.parser.QtProjectFileModifier;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.Test;

import junit.framework.TestCase;

public class QtProjectFileModifierTest extends TestCase {

	@Test
	public void test_ReplaceValue_SingleValue() {
		IDocument document = new Document("SOURCES += main.cpp"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);

		assertTrue(modifier.replaceVariableValue("SOURCES", "main.cpp", "main2.cpp")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("SOURCES += main2.cpp", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_ReplaceValue_HasCommentOnMainLine() {
		IDocument document = new Document("SOURCES += main.cpp  # This is a comment"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);

		assertTrue(modifier.replaceVariableValue("SOURCES", "main.cpp", "main2.cpp")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("SOURCES += main2.cpp  # This is a comment", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_ReplaceValue_HasCommentOnSubsequentLine() {
		IDocument document = new Document("SOURCES += main.cpp \\ # This is a comment\n" //$NON-NLS-1$
				+ "          main2.cpp   # This is a comment"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);

		assertTrue(modifier.replaceVariableValue("SOURCES", "main2.cpp", "main3.cpp")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("SOURCES += main.cpp \\ # This is a comment\n" //$NON-NLS-1$
				+ "          main3.cpp   # This is a comment", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_ReplaceValue_MatchWholeLineFalse() {
		IDocument document = new Document("CONFIG = qt debug"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);

		assertTrue(modifier.replaceVariableValue("CONFIG", "debug", "console", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("CONFIG = qt console", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_ReplaceValue_DoesNotExist() {
		IDocument document = new Document("CONFIG = qt debug"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);

		assertFalse(modifier.replaceVariableValue("CONFIG", "console", "debug", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("CONFIG = qt debug", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_ReplaceMultilineValue_MatchWholeLineFalse() {
		IDocument document = new Document("CONFIG = qt \\\n" //$NON-NLS-1$
				+ "      debug"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);

		assertTrue(modifier.replaceVariableValue("CONFIG", "debug", "console", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("CONFIG = qt \\\n" //$NON-NLS-1$
				+ "      console", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_ReplaceMultilineValue() {
		IDocument document = new Document("SOURCES += main.cpp \\\n" //$NON-NLS-1$
				+ "    main2.cpp"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);

		assertTrue(modifier.replaceVariableValue("SOURCES", "main2.cpp", "main3.cpp")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("SOURCES += main.cpp \\\n" //$NON-NLS-1$
				+ "    main3.cpp", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_ReplaceMultilineValue_HasComment() {
		IDocument document = new Document("SOURCES += main.cpp \\\n" //$NON-NLS-1$
				+ "    main2.cpp  # This is a comment"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);

		assertTrue(modifier.replaceVariableValue("SOURCES", "main2.cpp", "main3.cpp")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("SOURCES += main.cpp \\\n" //$NON-NLS-1$
				+ "    main3.cpp  # This is a comment", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_AddValue() {
		IDocument document = new Document("SOURCES += main.cpp"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.addVariableValue("SOURCES", "main2.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("SOURCES += main.cpp \\\n" //$NON-NLS-1$
				+ "           main2.cpp", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_AddValue_NoIndentation() {
		IDocument document = new Document("SOURCES += main.cpp \\\n" //$NON-NLS-1$
				+ "noindent.cpp"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.addVariableValue("SOURCES", "main2.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("SOURCES += main.cpp \\\n" //$NON-NLS-1$
				+ "noindent.cpp \\\n" //$NON-NLS-1$
				+ "main2.cpp", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_AddValue_AlreadyExists() {
		IDocument document = new Document("SOURCES += main.cpp"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.addVariableValue("SOURCES", "main.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("SOURCES += main.cpp", document.get()); //$NON-NLS-1$
	}

	@Test
	public void test_AddValue_HasCommentOnMainLine() {
		IDocument document = new Document("SOURCES += main.cpp # This is a comment"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.addVariableValue("SOURCES", "main2.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("SOURCES += main.cpp \\ # This is a comment\n" //$NON-NLS-1$
				+ "           main2.cpp", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_AddValue_HasCommentOnSubsequentLine() {
		IDocument document = new Document("SOURCES += main.cpp   \\ # This is a comment \n" //$NON-NLS-1$
				+ "  main2.cpp          # this is a comment\n\n"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.addVariableValue("SOURCES", "main3.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("SOURCES += main.cpp   \\ # This is a comment \n" //$NON-NLS-1$
				+ "  main2.cpp \\          # this is a comment\n" //$NON-NLS-1$
				+ "  main3.cpp\n\n", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_AddValue_CommentIndentation() {
		IDocument document = new Document("SOURCES += main.cpp      \\ # Test comment\n" //$NON-NLS-1$
				+ "           main2.cpp     \\ # Test comment2\n" //$NON-NLS-1$
				+ "           main3.cpp       # Test comment3"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.addVariableValue("SOURCES", "main4.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("SOURCES += main.cpp      \\ # Test comment\n" //$NON-NLS-1$
				+ "           main2.cpp     \\ # Test comment2\n" //$NON-NLS-1$
				+ "           main3.cpp \\       # Test comment3\n" //$NON-NLS-1$
				+ "           main4.cpp", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_AddValue_MultipleVariables() {
		IDocument document = new Document("SOURCES += main.cpp\n" //$NON-NLS-1$
				+ "\n" //$NON-NLS-1$
				+ "QT = app"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.addVariableValue("SOURCES", "main2.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("SOURCES += main.cpp \\\n" + //$NON-NLS-1$
				"           main2.cpp\n" + //$NON-NLS-1$
				"\n" + //$NON-NLS-1$
				"QT = app", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_AddValue_EmptyDocument() {
		IDocument document = new Document("\t  \n\n\t\n\n\n\n"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.addVariableValue("SOURCES", "main.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("SOURCES += main.cpp\n", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_AddValue_VariableDoesNotExist() {
		IDocument document = new Document("CONFIG += qt debug"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.addVariableValue("SOURCES", "main.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("CONFIG += qt debug\n" //$NON-NLS-1$
				+ "\n" //$NON-NLS-1$
				+ "SOURCES += main.cpp\n", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_AddValue_VariableDoesNotExist2() {
		IDocument document = new Document("CONFIG += qt debug\n"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.addVariableValue("SOURCES", "main.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("CONFIG += qt debug\n" //$NON-NLS-1$
				+ "\n" //$NON-NLS-1$
				+ "SOURCES += main.cpp\n", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_AddValue_VariableDoesNotExist3() {
		IDocument document = new Document("CONFIG += qt debug\n\n"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.addVariableValue("SOURCES", "main.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("CONFIG += qt debug\n" //$NON-NLS-1$
				+ "\n" //$NON-NLS-1$
				+ "\n" //$NON-NLS-1$
				+ "SOURCES += main.cpp\n", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_RemoveThenAddValue() {
		IDocument document = new Document("SOURCES += main.cpp \\\n" //$NON-NLS-1$
				+ " main2.cpp \\\n" //$NON-NLS-1$
				+ " main3.cpp\n"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.removeVariableValue("SOURCES", "main3.cpp"); //$NON-NLS-1$ //$NON-NLS-2$
		modifier.addVariableValue("SOURCES", "main4.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("SOURCES += main.cpp \\\n" //$NON-NLS-1$
				+ " main2.cpp \\\n" //$NON-NLS-1$
				+ " main4.cpp\n", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_RemoveValue_FirstLine() {
		IDocument document = new Document("SOURCES += main.cpp  \\   # Test comment\n" //$NON-NLS-1$
				+ "           main2.cpp \\   # Test comment2\n" //$NON-NLS-1$
				+ "           main3.cpp \\   # Test comment3\n" //$NON-NLS-1$
				+ "           main4.cpp     # Test comment4"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.removeVariableValue("SOURCES", "main.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("SOURCES += main2.cpp \\   # Test comment2\n" //$NON-NLS-1$
				+ "           main3.cpp \\   # Test comment3\n" //$NON-NLS-1$
				+ "           main4.cpp     # Test comment4", //$NON-NLS-1$
				document.get());
	}

	@Test
	public void test_RemoveValue_MiddleLine() {
		IDocument document = new Document("SOURCES += main.cpp  \\   # Test comment\n" //$NON-NLS-1$
				+ "           main2.cpp \\   # Test comment2\n" //$NON-NLS-1$
				+ "           main3.cpp \\   # Test comment3\n" //$NON-NLS-1$
				+ "           main4.cpp     # Test comment4"); //$NON-NLS-1$

		QtProjectFileModifier modifier = new QtProjectFileModifier(document);
		modifier.removeVariableValue("SOURCES", "main2.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("SOURCES += main.cpp  \\   # Test comment\n" //$NON-NLS-1$
				+ "           main3.cpp \\   # Test comment3\n" //$NON-NLS-1$
				+ "           main4.cpp     # Test comment4", //$NON-NLS-1$
				document.get());
	}
}
