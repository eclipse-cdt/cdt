/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author hamer
 *
 * Testing Exception_Reference, with prefix
 * Bug#50640 : Wrong completion kind when expecting an exception
 *
 */
public class ExceptionReference_Prefix_CompletionTest extends CompletionProposalsBaseTest {

	private final String fileName = "CompletionTestStart22.cpp";
	private final String fileFullPath = "resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath = "resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "a";
	private final String[] expectedResults = { "aClass", "anotherClass", "aNamespace", "anEnumeration", "AStruct",
			"AMacro(x)" };

	public ExceptionReference_Prefix_CompletionTest(String name) {
		super(name);
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=109724
		// setExpectFailure(109724);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ExceptionReference_Prefix_CompletionTest.class.getName());
		suite.addTest(new ExceptionReference_Prefix_CompletionTest("testCompletionProposals"));
		return suite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	@Override
	protected int getCompletionPosition() {
		return getBuffer().indexOf(" a ") + 2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getExpectedPrefix()
	 */
	@Override
	protected String getExpectedPrefix() {
		return expectedPrefix;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getExpectedResultsValues()
	 */
	@Override
	protected String[] getExpectedResultsValues() {
		return expectedResults;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getFileName()
	 */
	@Override
	protected String getFileName() {
		return fileName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getFileFullPath()
	 */
	@Override
	protected String getFileFullPath() {
		return fileFullPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getHeaderFileFullPath()
	 */
	@Override
	protected String getHeaderFileFullPath() {
		return headerFileFullPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getHeaderFileName()
	 */
	@Override
	protected String getHeaderFileName() {
		return headerFileName;
	}

}
