/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.externaltool;

import org.eclipse.cdt.codan.core.externaltool.IProblemDisplay;
import org.eclipse.cdt.codan.core.externaltool.InvocationParameters;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.test.CodanTestCase;
import org.eclipse.core.resources.IResource;

/**
 * Tests for <code>{@link CppcheckOutputParser}</code>.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
@SuppressWarnings("nls")
public class CppcheckOutputParserTest extends CodanTestCase {
	private static final String MATCHING_LINE_FORMAT = "[%s:%d]: (%s) %s";

	private ProblemDisplayStub problemDisplay;

	private CppcheckOutputParser parser;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		problemDisplay = new ProblemDisplayStub();
		loadcode("void f(int x) {}", "test.cpp");
		InvocationParameters parameters = new InvocationParameters(currentIFile, currentIFile,
				actualFilePath(), null);
		parser = new CppcheckOutputParser(parameters, problemDisplay);
	}

	public void testProblemIsReportedWhenParsingMatchingLine() {
		int lineNumber = 2;
		String severity = "warning";
		String description = "The scope of the variable 'i' can be reduced";
		boolean parsed = parser.parse(createMatchingLine(lineNumber, severity, description));
		assertTrue(parsed);
		problemDisplay.assertThatLocationHasFile(currentIFile);
		problemDisplay.assertThatLocationHasLineNumber(lineNumber);
		problemDisplay.assertThatLocationHasNoStartingAndEndingChars();
		problemDisplay.assertThatReceivedDescription(description);
		problemDisplay.assertThatReceivedSeverity(severity);
	}

	private String createMatchingLine(int lineNumber, String severity, String description) {
		return String.format(MATCHING_LINE_FORMAT, actualFilePath(), lineNumber, severity, description);
	}

	private String actualFilePath() {
		return currentIFile.getLocation().toOSString();
	}

	public void testProblemIsNotReportedWhenLineDoesNotMatch() {
		boolean parsed = parser.parse("Checking usage of global functions..");
		assertFalse(parsed);
		problemDisplay.assertThatProblemWasNotReported();
	}

	private static class ProblemDisplayStub implements IProblemDisplay {
		private boolean problemReported;
		private IProblemLocation location;
		private String description;
		private String severity;

		public void reportProblem(IProblemLocation location, String description) {
			throw new UnsupportedOperationException();
		}

		public void reportProblem(IProblemLocation location, String description, String severity) {
			problemReported = true;
			this.location = location;
			this.description = description;
			this.severity = severity;
		}

		void assertThatLocationHasFile(IResource expected) {
			assertSame(expected, location.getFile());
		}

		void assertThatLocationHasLineNumber(int expected) {
			assertEquals(expected, location.getLineNumber());
		}

		void assertThatLocationHasNoStartingAndEndingChars() {
			assertEquals(-1, location.getStartingChar());
			assertEquals(-1, location.getEndingChar());
		}

		void assertThatReceivedDescription(String expected) {
			assertEquals(expected, description);
		}

		void assertThatReceivedSeverity(String expected) {
			assertEquals(expected, severity);
		}

		void assertThatProblemWasNotReported() {
			assertFalse(problemReported);
		}
	}
}
