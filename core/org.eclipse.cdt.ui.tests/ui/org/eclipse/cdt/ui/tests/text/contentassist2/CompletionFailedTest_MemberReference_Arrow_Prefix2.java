/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Testing Member_Reference, with a prefix
 * Complex Context: Function return value: foo()->a(CTRL+SPACE)
 *
 */
public class CompletionFailedTest_MemberReference_Arrow_Prefix2  extends CompletionProposalsBaseTest{
	private final String fileName = "CompletionTestStart7.cpp";
	private final String fileFullPath ="resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "a";
	private final String[] expectedResults = {
			"aClass",
			"aField : int",
			"aMethod(void) int"
	};
	
	/* Additional results which should not be found. Run with trace activated to reproduce: 
	 * Result: author - author name
	 * is a template --> relax extra results checking
	 */
	
	public CompletionFailedTest_MemberReference_Arrow_Prefix2(String name) {
		super(name) ;
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionFailedTest_MemberReference_Arrow_Prefix2.class.getName());
		suite.addTest(new CompletionFailedTest_MemberReference_Arrow_Prefix2("testCompletionProposals"));
		return suite;
	}		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	protected int getCompletionPosition() {
		return getBuffer().indexOf("->a ") + 3;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.tests.text.contentassist2.CompletionProposalsBaseTest#doCheckExtraResults()
	 */
	protected boolean doCheckExtraResults() {
		return false ;
	}

}
