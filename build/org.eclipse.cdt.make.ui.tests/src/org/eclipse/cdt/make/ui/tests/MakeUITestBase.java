/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.tests;

import java.io.IOException;

import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Base for unit testing of Make UI test suite.
 */
public class MakeUITestBase {
	@Rule
	public TestName testNameRule = new TestName();
	private TestSourceReader commentReader;

	/**
	 * Constructor.
	 */
	protected MakeUITestBase() {
		this("src");
	}

	/**
	 * Constructor.
	 *
	 * @param srcRoot - project folder where the test package is rooted.
	 */
	protected MakeUITestBase(String srcRoot) {
		this.commentReader = new TestSourceReader(MakeUITestsPlugin.getDefault().getBundle(), srcRoot, this.getClass(),
				1);
	}

	/**
	 * Get name of the current test method.
	 *
	 * @return Name of the current test method.
	 */
	public String getName() {
		return testNameRule.getMethodName();
	}

	/**
	 * Retrieve comments above the current test method.
	 *
	 * @return First section of comments above the current test method.
	 *    A sections is defined as a block of comments starting with "//". Sections are separated by empty lines.
	 * @throws IOException
	 */
	public StringBuilder getTestComments() throws IOException {
		return commentReader.getContentsForTest(getName())[0];
	}

}
