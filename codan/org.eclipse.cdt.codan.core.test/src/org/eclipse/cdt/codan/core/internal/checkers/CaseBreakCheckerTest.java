/*******************************************************************************
 * Copyright (c) 2010 Gil Barash
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gil Barash  - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.CaseBreakChecker;

/**
 * Test for {@link#CaseBreakChecker} class
 */
public class CaseBreakCheckerTest extends CheckerTestCase {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.test.CodanTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		// set default prefs
		setEmpty(false);
		setLast(true);
	}

	// void foo(void) {
	//  int a;
	//  switch( a ) {
	//  case 1:
	//  }
	// }
	public void testEmptyLastCaseBad() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// void foo(void) {
	//  int a;
	//  switch( a ) {
	//  default:
	//  }
	// }
	public void testEmptyLastCaseDefaultBad() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//    b = 2;
	//  }
	// }
	public void testLastCaseBad() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//  case 2:
	//    b = 2;
	//    break;
	//  }
	// }
	public void testEmptyCaseBad() {
		setEmpty(true);
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// void foo(void) {
	//  int a;
	//  switch( a ) {
	//  case 1:
	//    break;
	//  }
	// }
	public void testEmptyLastCaseOKbreak() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(void) {
	//  int a;
	//  switch( a ) {
	//  case 1:
	//    return;
	//  }
	// }
	public void testEmptyLastCaseWithReturn() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(int a) {
	//  while (a--)
	//  switch( a ) {
	//  case 1:
	//    continue;
	//  }
	// }
	public void testEmptyLastCaseWithContinue() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(int a) {
	//
	//  switch( a ) {
	//  case 1:
	//    throw 1;
	//  }
	// }
	public void testEmptyLastCaseWithThrow() {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//    b = 2;
	//    break;
	//  }
	// }
	public void testLastCaseOKbreak() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//    break;
	//  case 2:
	//    b = 2;
	//    break;
	//  }
	// }
	public void testEmptyCaseOKbreak() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(void) {
	//  int a;
	//  switch( a ) {
	//  case 1:
	//    /* no break */
	//  }
	// }
	public void testEmptyLastCaseOKcomment() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(int a, int b) {
	//  switch( a ) {
	//  case 1:
	//     switch (b) {
	//         case 2:
	//         break;
	//     }
	//  case 2:
	//     break;
	//  }
	// }
	public void testEmptyLastCaseTwoSwitches() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//    b = 2;
	//    /* no break */
	//  }
	// }
	public void testLastCaseOKcomment() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//    /* no break */
	//  case 2:
	//    b = 2;
	//    break;
	//  }
	public void testEmptyCaseOKcomment() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//    b = 2;
	//    /* no break */
	//    bye();
	//  }
	// }
	public void testLastCaseBadCommentNotLast() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//    b = 2;
	//    /* no break */
	//  case 2:
	//    b = 2;
	//    /*no break*/
	//  case 3:
	//    b = 2;
	//    //no break
	//  case 4:
	//    b = 2;
	//    // no break
	//  case 5:
	//    b = 2;
	//    /* no brea */
	//  case 6:
	//    b = 2;
	//    /* no break1 */
	//  }
	// }
	public void testDifferentComments() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(16, 19);
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//    // lolo
	//  case 2:
	//  case 3:
	//  }
	//
	//  switch( a ) {
	//  case 1:
	//    b = 2;
	//    // lolo
	//  case 2:
	//    b = 2;
	//  case 3:
	//  case 4:
	//    break;
	//  case 5:
	//  case 6:
	//  }
	//
	//  switch( a ) {
	//  case 1:
	//    b = 2;
	//    // lolo
	//  case 2:
	//    b = 2;
	//  case 3:
	//    b = 2;
	//    /* no break */
	//  case 4:
	//    b = 2;
	//  case 5:
	//    b = 2;
	//    break;
	//  case 6:
	//    b = 2;
	//    /* no break */
	//    b = 2;
	//  case 7:
	//    b = 2;
	//  }
	//
	//  switch( a ) {
	//  case 1:
	//    b = 2;
	//    // lolo
	//  case 2:
	//    b = 2;
	//  default:
	//  }
	// }
	public void testGeneral1() {
		setEmpty(true);
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4, 6, 7, 11, 14, 16, 19, 20, 24, 27, 32, 37, 41, 46, 49, 51);
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//    b = 2;
	//    // lolo
	//    /* no break */
	//  case 2:
	//    b = 2;
	//    /* no break */
	//    // lolo
	//  case 3:
	//    /* no break */
	//    b = 2;
	//    // loo
	//  case 4:
	//    b = 2;
	//    // lolo
	//    /* no break */
	//  case 5:
	//    // lolo
	//    b = 2;
	//    /* no break */
	//  }
	// }
	public void testGeneralComments1() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(8, 12);
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 0:
	//    switch( b ) {
	//    case 2:
	//    }
	//
	//  case 1:
	//    switch( b ) {
	//    case 2:
	//      break;
	//    }
	//  case 3:
	//    switch( b ) {
	//    case 2:
	//      break;
	//    }
	//    break;
	//  case 4:
	//    switch( b ) {
	//    case 2:
	//      /* no break */
	//    }
	//  case 5:
	//    switch( b ) {
	//    case 2:
	//    }
	//    /* no break */
	//  }
	// }
	public void testNestedSwitches() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4, 6, 9, 20, 27);
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//    b = 2;
	//  }
	// }
	public void testLastCaseIgnore() {
		setLast(false);
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
		setLast(true);
	}

	// void foo(void) {
	//  int a, b;
	//  switch( a ) {
	//  case 1:
	//  case 2:
	//    b = 2;
	//    break;
	//  case 3: case 4:
	//    b = 2;
	//    break;
	//  }
	// }
	public void testEmptyCaseIgnore() {
		setEmpty(false);
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(void) {
	//  int a;
	//  switch( a ) {
	//  case 1:
	//  }
	// }
	public void testEmptyLastCaseIgnore() {
		String code = getAboveComment();
		setLast(false);
		loadCodeAndRun(code);
		checkNoErrors();
		setLast(true);
		setEmpty(false);
		loadCodeAndRun(code);
		checkErrorLine(4);
	}

	private void setLast(boolean val) {
		IProblemPreference pref = getPreference(CaseBreakChecker.ER_ID, CaseBreakChecker.PARAM_LAST_CASE);
		pref.setValue(val);
	}

	private void setEmpty(boolean val) {
		IProblemPreference pref = getPreference(CaseBreakChecker.ER_ID, CaseBreakChecker.PARAM_EMPTY_CASE);
		pref.setValue(val);
	}

	// void foo(int a) {
	//   switch( a ) {
	//   case 1:
	//     while (a--)
	//       break;
	//   case 2:
	//     while (a--) {
	//       break;
	//     }
	//   }
	// }
	public void testEmptyCaseWithLoopBreak() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3, 6);
	}

	// void foo(int a) {
	//   switch( a ) {
	//   case 1: {
	//     break;
	//   }
	//   case 2: {
	//     a--;
	//     break;
	//   }
	//   }
	// }
	public void testCaseWithCurlyBrackets() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(void) {
	//  int a;
	//  switch( a ) {
	//  case 2:
	//     break;
	//  case 1:
	//  }
	// }
	public void testEmptyLastCaseError() {
		String code = getAboveComment();
		setLast(true);
		setEmpty(false);
		loadCodeAndRun(code);
		checkErrorLine(6);
		setLast(false);
		loadCodeAndRun(code);
		checkNoErrors();
	}
}
