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
import org.eclipse.cdt.codan.internal.checkers.SuspiciousSemicolonChecker;

public class SuspiciousSemicolonCheckerTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems("org.eclipse.cdt.codan.internal.checkers.SuspiciousSemicolonProblem");
	}

	// void foo() {
	// if(0);
	// }
	public void testIf1() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// void foo() {
	// if(0);
	// {
	// }
	// }
	public void testIf2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// void foo() {
	// if(0)
	// foo();
	// }
	public void testIf3() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo() {
	// if(0)
	// ;
	// }
	public void testIf4() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// void foo() {
	// if(0);{
	// }
	// }
	public void testIf5() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// void foo() {
	// if(0) {};
	// }
	public void testIf6() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo() {
	// if(0
	// );
	// }
	// }
	public void testIf7() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// void foo() {
	// if(0)
	// ;
	// else if(0);
	// }
	public void testElseIf1() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
		checkErrorLine(4);
	}

	// void foo() {
	// if(0)
	// ;
	// else if(0);
	// {
	//
	// }
	// }
	public void testElseIf2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
		checkErrorLine(4);
	}

	// void foo() {
	// if(0)
	// ;
	// else if(0);{
	// }
	// }
	public void testElseIf3() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
		checkErrorLine(4);
	}

	// void foo() {
	// if(0)
	// ;
	// else if(0){};
	// }
	public void testElseIf4() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// void foo() {
	// if(0)
	// ;
	// else if(0
	// );
	// }
	public void testElseIf5() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
		checkErrorLine(5);
	}

	// #define OP
	// void foo() {
	//   if(0)
	//     OP;
	// }
	public void testMacro() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// #define MACRO(cond) if (cond) ;
	// void foo() {
	//   MACRO(true);
	// }
	public void testMacroExpansion() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// main() {
	//	   if (false)
	//	        ; // only this one is reported
	//	    else
	//	        ;
	// }
	public void testIfElse() throws Exception {
		setPreferenceValue(SuspiciousSemicolonChecker.ER_ID, SuspiciousSemicolonChecker.PARAM_ALFTER_ELSE,
				Boolean.TRUE);
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3, 5);
	}
}
