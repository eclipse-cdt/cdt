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
 * Testing Single name reference, Macro lookups
 *
 */
public class CompletionTest_SingleName_Prefix extends CompletionProposalsBaseTest {
	private final String fileName = "CompletionTestStart8.cpp";
	private final String fileFullPath = "resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath = "resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "AM";
	private final String[] expectedResults = {
			// missing results
			"AMacro(x)" };

	public CompletionTest_SingleName_Prefix(String name) {
		super(name);
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=171708
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CompletionTest_SingleName_Prefix.class.getName());
		suite.addTest(new CompletionTest_SingleName_Prefix("testCompletionProposals"));
		return suite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	@Override
	protected int getCompletionPosition() {
		return getBuffer().indexOf(" AM ") + 3;
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
