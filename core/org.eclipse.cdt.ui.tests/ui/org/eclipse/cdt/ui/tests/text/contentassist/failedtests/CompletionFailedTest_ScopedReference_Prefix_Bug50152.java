/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist.failedtests;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionProposalsBaseTest;

/**
 * @author hamer
 * 
 * Testing Scoped_Reference, with a prefix
 * Bug#50152: Wrong context sent after a "::"
 *
 */
public class CompletionFailedTest_ScopedReference_Prefix_Bug50152  extends CompletionProposalsBaseTest{
	
	private final String fileName = "CompletionTestStart31.cpp";
	private final String fileFullPath ="resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName;
	private final String expectedScopeName = "ASTMethod";
	private final String expectedContextName = "null"; // should be "ASTNamespaceDefinition";
	private final CompletionKind expectedKind = CompletionKind.SINGLE_NAME_REFERENCE; // should be CompletionKind.SCOPED_REFERENCE;
	private final String expectedPrefix = "a";
	private final String[] expectedResults = {
			// shoud be "aNamespaceFunction() void"
	};
	
	public CompletionFailedTest_ScopedReference_Prefix_Bug50152(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionFailedTest_ScopedReference_Prefix_Bug50152.class.getName());
		suite.addTest(new CompletionFailedTest_ScopedReference_Prefix_Bug50152("testCompletionProposals"));
		return suite;
	}		
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	protected int getCompletionPosition() {
		return getBuffer().indexOf("::a ") + 3;
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
