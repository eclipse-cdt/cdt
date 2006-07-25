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
 * Testing Type_Reference, with prefix
 * Bug#50471 : Wrong completion kind after the "using" keyword
 *
 */
public class CompletionTest_TypeRef_Prefix  extends CompletionProposalsBaseTest{
	
	private final String fileName = "CompletionTestStart25.cpp";
	private final String fileFullPath ="resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "a"; 
	private final String[] expectedResults = {
			"aNamespace"
			/* FIXME: Additional results which should not be there. Run with trace enabled to reproduce:
Result: aClass
Result: aFirstEnum
Result: aFunction(void) bool
Result: anEnumeration
Result: anotherClass
Result: anotherFunction(void) void
Result: aSecondEnum
Result: aThirdEnum
Result: author - author name
Result: aVariable : int
			*/	};
	
	public CompletionTest_TypeRef_Prefix(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionTest_TypeRef_Prefix.class.getName());
		suite.addTest(new CompletionTest_TypeRef_Prefix("testCompletionProposals"));
		return suite;
	}		
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	protected int getCompletionPosition() {
		return getBuffer().indexOf("using a ") + 7;
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
