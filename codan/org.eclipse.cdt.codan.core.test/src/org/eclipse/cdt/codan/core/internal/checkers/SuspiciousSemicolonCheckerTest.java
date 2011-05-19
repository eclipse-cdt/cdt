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
import org.eclipse.cdt.codan.internal.checkers.SuspiciousSemicolonChecker;

public class SuspiciousSemicolonCheckerTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems("org.eclipse.cdt.codan.internal.checkers.SuspiciousSemicolonProblem"); //$NON-NLS-1$
	}

	// void foo() {
	// if(0);
	// }
	public void testIf1() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// void foo() {
	// if(0);
	// {
	// }
	// }
	public void testIf2() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// void foo() {
	// if(0)
	// foo();
	// }
	public void testIf3() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo() {
	// if(0)
	// ;
	// }
	public void testIf4() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// void foo() {
	// if(0);{
	// }
	// }
	public void testIf5() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// void foo() {
	// if(0) {};
	// }
	public void testIf6() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo() {
	// if(0
	// );
	// }
	// }
	public void testIf7() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// void foo() {
	// if(0)
	// ;
	// else if(0);
	// }
	public void testElseIf1() {
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
	public void testElseIf2() {
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
	public void testElseIf3() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
		checkErrorLine(4);
	}

	// void foo() {
	// if(0)
	// ;
	// else if(0){};
	// }
	public void testElseIf4() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// void foo() {
	// if(0)
	// ;
	// else if(0
	// );
	// }
	public void testElseIf5() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
		checkErrorLine(5);
	}

	// #define OP
	// void foo() {
	//   if(0)
	//     OP;
	// }
	public void testMacro() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// #define MACRO(cond) if (cond) ;
	// void foo() {
	//   MACRO(true);
	// }
	public void testMacroExpansion() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// main() {
	//	   if (false)
	//	        ; // only this one is reported
	//	    else
	//	        ;
	// }
	public void testIfElse() {
		setPreferenceValue(SuspiciousSemicolonChecker.ER_ID, SuspiciousSemicolonChecker.PARAM_ALFTER_ELSE, Boolean.TRUE);
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3, 5);
	}
}
