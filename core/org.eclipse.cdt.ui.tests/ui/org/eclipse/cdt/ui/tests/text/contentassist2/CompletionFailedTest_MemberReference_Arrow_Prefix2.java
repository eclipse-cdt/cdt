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
 * Testing Member_Reference, with a prefix
 * Complex Context: Function return value: foo()->a(CTRL+SPACE)
 *
 */
public class CompletionFailedTest_MemberReference_Arrow_Prefix2  extends CompletionProposalsBaseTest{
	private final String fileName = "CompletionTestStart7.cpp";
	private final String fileFullPath ="resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName;
	private final String expectedScopeName = "ASTMethod";
	private final String expectedContextName = "ASTBinaryExpression";
	private final CompletionKind expectedKind = CompletionKind.MEMBER_REFERENCE;
	private final String expectedPrefix = "a";
	private final String[] expectedResults = {
			"aField : int",
			"aMethod(void) int"
			/* FIXME: Additional results which should not be found. Run with trace activated to reproduce: 
Result: aClass
Result: author - author name
			 */
	};
	
	public CompletionFailedTest_MemberReference_Arrow_Prefix2(String name) {
		super(name);
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
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getExpectedScope()
	 */
	protected String getExpectedScopeClassName() {
		return expectedScopeName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getExpectedContext()
	 */
	protected String getExpectedContextClassName() {
		return expectedContextName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getExpectedKind()
	 */
	protected CompletionKind getExpectedKind() {
		return expectedKind;
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
