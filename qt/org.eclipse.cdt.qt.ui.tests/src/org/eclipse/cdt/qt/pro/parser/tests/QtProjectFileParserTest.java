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

import java.util.List;

import org.eclipse.cdt.internal.qt.ui.pro.parser.QtProjectFileParser;
import org.eclipse.cdt.internal.qt.ui.pro.parser.QtProjectVariable;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.Test;

import junit.framework.TestCase;

public class QtProjectFileParserTest extends TestCase {

	@Test
	public void test_AssignmentOperator_Equals() {
		IDocument document = new Document("SOURCES = main.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		List<QtProjectVariable> variables = parser.getAllVariables();
		assertFalse("Unable to parse variable", variables.isEmpty()); //$NON-NLS-1$
		assertEquals("Invalid assignment operator", "=", variables.get(0).getAssignmentOperator()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void test_AssignmentOperator_PlusEquals() {
		IDocument document = new Document("SOURCES += main.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		List<QtProjectVariable> variables = parser.getAllVariables();
		assertFalse("Unable to parse variable", variables.isEmpty()); //$NON-NLS-1$
		assertEquals("Invalid assignment operator", "+=", variables.get(0).getAssignmentOperator()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void test_AssignmentOperator_MinusEquals() {
		IDocument document = new Document("SOURCES -= main.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		List<QtProjectVariable> variables = parser.getAllVariables();
		assertFalse("Unable to parse variable", variables.isEmpty()); //$NON-NLS-1$
		assertEquals("Invalid assignment operator", "-=", variables.get(0).getAssignmentOperator()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void test_AssignmentOperator_AsterixEquals() {
		IDocument document = new Document("SOURCES *= main.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		List<QtProjectVariable> variables = parser.getAllVariables();
		assertFalse("Unable to parse variable", variables.isEmpty()); //$NON-NLS-1$
		assertEquals("Invalid assignment operator", "*=", variables.get(0).getAssignmentOperator()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void test_CommentedVariable() {
		IDocument document = new Document("# SOURCES += main.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);
		assertTrue("Found variable even though it was commented", parser.getAllVariables().isEmpty()); //$NON-NLS-1$
	}

	@Test
	public void test_CommentedVariable2() {
		IDocument document = new Document("SOURCES # += main.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);
		assertTrue("Found variable even though it was commented", parser.getAllVariables().isEmpty()); //$NON-NLS-1$
	}

	@Test
	public void test_MalformedVariable() {
		IDocument document = new Document("MY VARIABLE  # += main.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);
		assertTrue("Found variable even though it was malformed", parser.getAllVariables().isEmpty()); //$NON-NLS-1$
	}

	@Test
	public void test_MalformedVariable2() {
		IDocument document = new Document("\\SOURCES # += main.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);
		assertTrue("Found variable even though it was malformed", parser.getAllVariables().isEmpty()); //$NON-NLS-1$
	}

	@Test
	public void test_FullyQualifiedName() {
		IDocument document = new Document("fully.qualified.Name += main.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		QtProjectVariable sources = parser.getVariable("fully.qualified.Name"); //$NON-NLS-1$
		assertNotNull("Unable to parse variable", sources); //$NON-NLS-1$
	}

	@Test
	public void test_SingleLineVariable() {
		IDocument document = new Document("SOURCES += main.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		QtProjectVariable sources = parser.getVariable("SOURCES"); //$NON-NLS-1$
		assertNotNull("Unable to parse variable", sources); //$NON-NLS-1$
		assertTrue("Unable to parse \"main.cpp\" from SOURCES variable", sources.getValueIndex("main.cpp") == 0); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void test_SingleLineVariable_MultipleValues() {
		IDocument document = new Document("CONFIG += qt debug"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		QtProjectVariable sources = parser.getVariable("CONFIG"); //$NON-NLS-1$
		assertNotNull("Unable to parse variable", sources); //$NON-NLS-1$
		assertTrue("Unable to parse \"qt debug\" from SOURCES variable", sources.getValueIndex("qt debug") == 0); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void test_VariableWithComment() {
		IDocument document = new Document("SOURCES += main.cpp # this is a comment\n"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		QtProjectVariable sources = parser.getVariable("SOURCES"); //$NON-NLS-1$
		assertNotNull("Unable to parse variable", sources); //$NON-NLS-1$
		assertEquals("Unable to parse assignment from SOURCES variable", "+=", sources.getAssignmentOperator()); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unable to parse \"main.cpp\" from SOURCES variable", sources.getValueIndex("main.cpp") == 0); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void test_MultilineVariable() {
		IDocument document = new Document("SOURCES += main.cpp \\\n          main2.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		QtProjectVariable sources = parser.getVariable("SOURCES"); //$NON-NLS-1$
		assertNotNull("Unable to parse variable", sources); //$NON-NLS-1$
		assertEquals("Incorrect number of lines", sources.getNumberOfLines(), 2); //$NON-NLS-1$
		assertTrue("Unable to parse \"main.cpp\" from SOURCES variable", sources.getValueIndex("main.cpp") == 0); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unable to parse \"main2.cpp\" from SOURCES variable", sources.getValueIndex("main2.cpp") == 1); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void test_MultilineVariable2() {
		IDocument document = new Document("SOURCES += main.cpp \\\n          main2.cpp \\\n            main3.cpp"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		QtProjectVariable sources = parser.getVariable("SOURCES"); //$NON-NLS-1$
		assertNotNull("Unable to parse variable", sources); //$NON-NLS-1$
		assertEquals("Incorrect number of lines", 3, sources.getNumberOfLines()); //$NON-NLS-1$
		assertTrue("Unable to parse \"main.cpp\" from SOURCES variable", sources.getValueIndex("main.cpp") == 0); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unable to parse \"main2.cpp\" from SOURCES variable", sources.getValueIndex("main2.cpp") == 1); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unable to parse \"main3.cpp\" from SOURCES variable", sources.getValueIndex("main3.cpp") == 2); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void test_MalformedMultilineVariable() {
		IDocument document = new Document("SOURCES += main.cpp \\\n          main2.cpp \\"); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		QtProjectVariable sources = parser.getVariable("SOURCES"); //$NON-NLS-1$
		assertNotNull("Unable to parse variable", sources); //$NON-NLS-1$
		assertEquals("Incorrect number of lines", 2, sources.getNumberOfLines()); //$NON-NLS-1$
		assertTrue("Unable to parse \"main.cpp\" from SOURCES variable", sources.getValueIndex("main.cpp") == 0); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unable to parse \"main2.cpp\" from SOURCES variable", sources.getValueIndex("main2.cpp") == 1); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void test_MultilineVariable_WithComment() {
		IDocument document = new Document(
				"SOURCES += main.cpp \\ # this is a comment \n          main2.cpp # this is a comment "); //$NON-NLS-1$
		QtProjectFileParser parser = new QtProjectFileParser(document);

		QtProjectVariable sources = parser.getVariable("SOURCES"); //$NON-NLS-1$
		assertNotNull("Unable to parse variable", sources); //$NON-NLS-1$
		assertEquals("Incorrect number of lines", 2, sources.getNumberOfLines()); //$NON-NLS-1$
		assertTrue("Unable to parse \"main.cpp\" from SOURCES variable", sources.getValueIndex("main.cpp") == 0); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unable to parse \"main2.cpp\" from SOURCES variable", sources.getValueIndex("main2.cpp") == 1); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
