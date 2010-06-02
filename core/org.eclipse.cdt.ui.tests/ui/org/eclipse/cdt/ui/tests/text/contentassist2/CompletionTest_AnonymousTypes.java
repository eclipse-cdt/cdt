/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *        Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test that anonymous types are not returned as possibilities.
 */
public class CompletionTest_AnonymousTypes  extends CompletionProposalsBaseTest{
	private final String fileName = "CompletionTestStart40.cpp";
	private final String fileFullPath ="resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart40.h";
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "";
	private final String[] expectedResults = {"notAnonymousEnum", "notAnonymousClass", "xOtherClass"};
	
	public CompletionTest_AnonymousTypes(String name) {
		super(name);
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=174809
		// setExpectFailure(174809);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionTest_AnonymousTypes.class.getName());
		suite.addTest(new CompletionTest_AnonymousTypes("testCompletionProposals"));
		return suite;
	}		
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	@Override
	protected int getCompletionPosition() {
		return getBuffer().indexOf(" x ");
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
