/*******************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Symbian - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Testing operator is not followed on pointer to class with overloaded operator ->
 */
public class CompletionTest_MemberReference_Arrow_NoPrefix3  extends CompletionProposalsBaseTest{
	private final String fileName = "CompletionTestStart42.cpp";
	private final String fileFullPath ="resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart41.h";
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "";
	private final String[] expectedResults = {
			"B", "bar(void) void"
	};
	
	public CompletionTest_MemberReference_Arrow_NoPrefix3(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionTest_MemberReference_Arrow_NoPrefix3.class.getName());
		suite.addTest(new CompletionTest_MemberReference_Arrow_NoPrefix3("testCompletionProposals"));
		return suite;
	}		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	protected int getCompletionPosition() {
		return getBuffer().indexOf("a->")+3;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getExpectedPrefix()
	 */
	protected String getExpectedPrefix() {
		return expectedPrefix;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getExpectedResultsValues()
	 */
	protected String[] getExpectedResultsValues() {
		return expectedResults;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getFileName()
	 */
	protected String getFileName() {
		return fileName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getFileFullPath()
	 */
	protected String getFileFullPath() {
		return fileFullPath;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getHeaderFileFullPath()
	 */
	protected String getHeaderFileFullPath() {
		return headerFileFullPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getHeaderFileName()
	 */
	protected String getHeaderFileName() {
		return headerFileName;
	}

}
