/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;

/**
 * @author hamer
 * 
 * Testing Class_Reference, with prefix
 * Bug#50621 :Wrong completion kind in a class declaration
 *
 */
public class CompletionTest_ClassReference_Prefix  extends CompletionProposalsBaseTest{
	
	private final String fileName = "CompletionTestStart20.h"; //$NON-NLS-1$
	private final String fileFullPath ="resources/contentassist/" + fileName; //$NON-NLS-1$
	private final String headerFileName = "CompletionTestStart.h"; //$NON-NLS-1$
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName; //$NON-NLS-1$
	private final String expectedPrefix = "a";  //$NON-NLS-1$
	private final String[] expectedResults = {
			"aClass", //$NON-NLS-1$
			"anotherClass" //$NON-NLS-1$
			/* FIXME: Additional results which should not be there. Run with trace enabled to reproduce:
			Result: aFirstEnum
			Result: aFunction(void) bool
			Result: aNamespace
			Result: anEnumeration
			Result: anotherFunction(void) void
			Result: aSecondEnum
			Result: aThirdEnum
			Result: author - author name
			Result: aVariable : int
			*/
	};
	
	public CompletionTest_ClassReference_Prefix(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionTest_ClassReference_Prefix.class.getName());
		suite.addTest(new CompletionTest_ClassReference_Prefix("testCompletionProposals")); //$NON-NLS-1$
		return suite;
	}		
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	protected int getCompletionPosition() {
		return getBuffer().indexOf(" a ") + 2; //$NON-NLS-1$
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
