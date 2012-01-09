/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author hamer
 * 
 * Testing Class scope, declaration start, with NO prefix
 * Bug#50344 :Wrong completion in Class scope if before the first declaration
 *
 */
public class CompletionTest_FieldType_NoPrefix2  extends CompletionProposalsBaseTest{
	
	private final String fileName = "CompletionTestStart13.h";
	private final String fileFullPath ="resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "";
	private final String[] expectedResults = {
			"AStruct",
			"XStruct",
			"aClass",
			"aNamespace",
			"aThirdClass",
			"anEnumeration",
			"anotherClass",
			"xEnumeration",
			"xNamespace",
			"xOtherClass"
	};
	
	public CompletionTest_FieldType_NoPrefix2(String name) {
		super(name);
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169860
	}

	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionTest_FieldType_NoPrefix2.class.getName());
		suite.addTest(new CompletionTest_FieldType_NoPrefix2("testCompletionProposals"));
		return suite;
	}		
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	@Override
	protected int getCompletionPosition() {
		return getBuffer().indexOf("      ") + 3;
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
