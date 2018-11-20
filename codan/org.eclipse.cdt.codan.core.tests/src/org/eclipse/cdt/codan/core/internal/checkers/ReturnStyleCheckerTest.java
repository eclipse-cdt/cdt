/*******************************************************************************
 * Copyright (c) 2010, 2011 Marc-Andre Laperle and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;

public class ReturnStyleCheckerTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems("org.eclipse.cdt.codan.internal.checkers.ReturnStyleProblem"); //$NON-NLS-1$
	}

	// void foo() {
	// return; // no error
	// }
	public void testSimpleReturn() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo() {
	// return
	// ; // no error
	// }
	public void testSimpleReturnMultiLine() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int foo() {
	// return(0); // no error
	// }
	public void testSimpleReturnValue() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int foo() {
	// return 0; // error line 2
	// }
	public void testSimpleReturnValueError() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// int foo() {
	// return // no error
	// (
	// 0
	// );
	// }
	public void testReturnValueMultiline() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int foo() {
	// return // error line 2
	// 0
	// ;
	// }
	public void testReturnValueMultilineError() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// int foo() {
	// return ((0));// no error
	// }
	public void testReturnValueMultipleBrackets() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int foo() {
	// return // no error
	// (
	// (0)
	// );
	// }
	public void testReturnValueMultilineMultipleBrackets() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// #define MY_RETURN return(0);
	//
	// int foo() {
	// MY_RETURN // no error
	// }
	public void testReturnDefine() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// #define MY_RETURN return 0;
	//
	// int foo() {
	// MY_RETURN // error line 4
	// }
	public void testReturnDefineError() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}
}