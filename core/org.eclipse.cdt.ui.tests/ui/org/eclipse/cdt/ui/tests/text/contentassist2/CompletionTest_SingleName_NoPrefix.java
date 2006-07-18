/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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

import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;

/**
 * @author hamer
 * 
 * Testing Single name reference, with prefix 
 *
 */
public class CompletionTest_SingleName_NoPrefix  extends CompletionProposalsBaseTest{
	private final String fileName = "CompletionTestStart15.cpp";
	private final String fileFullPath ="resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "";
	private final String[] expectedResults = {
			"x : int"
//			"aVariable : int",
//			"xVariable : int",
//			"aFunction() bool",
//			"anotherFunction() void",
//			"foo(int) void",
//			"xFunction() bool",
//			"xOtherFunction() void",
//			"aClass",
//			"anotherClass",
//			"xOtherClass",
//			"AStruct",
//			"XStruct",
//			"aNamespace",
//			"xNamespace",
//			"anEnumeration",
//			"xEnumeration",
//			"aFirstEnum",
//			"aSecondEnum",
//			"aThirdEnum",
//			"xFirstEnum",
//			"xSecondEnum",
//			"xThirdEnum",
//			"__cplusplus", 
//			"__DATE__",
//			"__FILE__",
//			"__LINE__",
//			"__STDC__",
//			"__STDC_HOSTED__",
//			"__STDC_VERSION__",
//			"__TIME__",
//			"AMacro(x)",
//			"DEBUG",
//			"XMacro(x,y)"
	};
	
	public CompletionTest_SingleName_NoPrefix(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionTest_SingleName_NoPrefix.class.getName());
		suite.addTest(new CompletionTest_SingleName_NoPrefix("testCompletionProposals"));
		return suite;
	}		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	protected int getCompletionPosition() {
		return getBuffer().indexOf("      ") + 2;
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
