/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * Testing Namespace_Reference, with no prefix
 * Bug#50471 : Wrong completion kind after the "using" keyword
 */
public class CompletionTest_NamespaceRef_NoPrefix extends CompletionProposalsBaseTest {
	private final String fileName = "CompletionTestStart32.cpp";
	private final String fileFullPath = "resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath = "resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "";
	private final String[] expectedResults = { "aNamespace", "xNamespace" };

	public CompletionTest_NamespaceRef_NoPrefix(String name) {
		super(name);
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169860
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CompletionTest_NamespaceRef_NoPrefix.class.getName());
		suite.addTest(new CompletionTest_NamespaceRef_NoPrefix("testCompletionProposals"));
		return suite;
	}

	@Override
	protected int getCompletionPosition() {
		return getBuffer().indexOf("namespace ") + 10;
	}

	@Override
	protected String getExpectedPrefix() {
		return expectedPrefix;
	}

	@Override
	protected String[] getExpectedResultsValues() {
		return expectedResults;
	}

	@Override
	protected String getFileName() {
		return fileName;
	}

	@Override
	protected String getFileFullPath() {
		return fileFullPath;
	}

	@Override
	protected String getHeaderFileFullPath() {
		return headerFileFullPath;
	}

	@Override
	protected String getHeaderFileName() {
		return headerFileName;
	}
}
