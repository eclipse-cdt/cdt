/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
 * Testing statement start, with no prefix
 * Lookup.THIS
 *
 */
public class CompletionTest_SingleName_Method_NoPrefix  extends CompletionProposalsBaseTest{
	private final String fileName = "CompletionTestStart5.cpp";
	private final String fileFullPath ="resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "";
	
	private final String[] expectedResults = {
			"AStruct",
			"XStruct",
			"aClass",
			"aFirstEnum",
			"aFunction(void) bool",
			"aNamespace",
			"aSecondEnum",
			"aThirdEnum",
			"aVariable : int",
			"anEnumeration",
			"anotherClass",
			"anotherField : int",
			"anotherFunction(void) void",
			"anotherMethod(void) void",
			"xEnumeration",
			"xFirstEnum",
			"xFunction(void) bool",
			"xNamespace",
			"xOtherClass",
			"xOtherFunction(void) void",
			"xSecondEnum",
			"xThirdEnum",
			"xVariable : int",
			"~anotherClass(void) " 
			// extra result
			// "operator =(const anotherClass &) anotherClass &",
	};
	
	public CompletionTest_SingleName_Method_NoPrefix(String name) {
		super(name);
		// operators should not be proposed
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172304
		setExpectFailure(172304);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionTest_SingleName_Method_NoPrefix.class.getName());
		suite.addTest(new CompletionTest_SingleName_Method_NoPrefix("testCompletionProposals"));
		return suite;
	}		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	@Override
	protected int getCompletionPosition() {
		return getBuffer().indexOf("    ") + 2;
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
