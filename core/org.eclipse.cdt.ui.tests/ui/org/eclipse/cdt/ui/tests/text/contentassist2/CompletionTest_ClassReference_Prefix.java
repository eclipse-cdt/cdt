/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author hamer
 *
 * Testing class reference, with prefix
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50621
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=169860
 */
public class CompletionTest_ClassReference_Prefix extends CompletionProposalsBaseTest {
	private final String fileName = "CompletionTestStart20.h";
	private final String fileFullPath = "resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath = "resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "a";
	private final String[] expectedResults = { "aClass", "anotherClass", "AStruct" };

	public CompletionTest_ClassReference_Prefix(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CompletionTest_ClassReference_Prefix.class.getName());
		suite.addTest(new CompletionTest_ClassReference_Prefix("testCompletionProposals"));
		return suite;
	}

	@Override
	protected int getCompletionPosition() {
		return getBuffer().indexOf(" a ") + 2;
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
