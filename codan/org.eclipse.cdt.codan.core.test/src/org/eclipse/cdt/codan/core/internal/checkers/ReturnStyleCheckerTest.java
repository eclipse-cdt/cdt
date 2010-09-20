/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;

public class ReturnStyleCheckerTest extends CheckerTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems("org.eclipse.cdt.codan.internal.checkers.ReturnStyleProblem"); //$NON-NLS-1$
	}

	// void foo() {
	// return; // no error
	// }
	public void testSimpleReturn() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo() {
	// return
	// ; // no error
	// }
	public void testSimpleReturnMultiLine() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int foo() {
	// return(0); // no error
	// }
	public void testSimpleReturnValue() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int foo() {
	// return 0; // error line 2
	// }
	public void testSimpleReturnValueError() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// int foo() {
	// return // no error
	// (
	// 0
	// );
	// }
	public void testReturnValueMultiline() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int foo() {
	// return // error line 2
	// 0
	// ;
	// }
	public void testReturnValueMultilineError() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// int foo() {
	// return ((0));// no error
	// }
	public void testReturnValueMultipleBrackets() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int foo() {
	// return // no error
	// (
	// (0)
	// );
	// }
	public void testReturnValueMultilineMultipleBrackets() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// #define MY_RETURN return(0);
	//
	// int foo() {
	// MY_RETURN // no error
	// }
	public void testReturnDefine() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// #define MY_RETURN return 0;
	//
	// int foo() {
	// MY_RETURN // error line 4
	// }
	public void testReturnDefineError() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}
}