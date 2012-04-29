/*******************************************************************************
 * Copyright (c) 2010 Gil Barash
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gil Barash  - Initial implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.CaseBreakChecker;

/**
 * Test for {@link CaseBreakChecker} class
 */
public class CaseBreakCheckerTest extends CheckerTestCase {
	public static final String ER_ID = CaseBreakChecker.ER_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// Set default preferences.
		setEmpty(false);
		setLast(true);
	}

	// void foo(void) {
	//  int a;
	//  switch (a) {
	//  case 1:
	//  }
	// }
	public void testEmptyLastCaseBad() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// void foo(void) {
	//  int a;
	//  switch (a) {
	//  default:
	//  }
	// }
	public void testEmptyLastCaseDefaultBad() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    b = 2;
	//  }
	// }
	public void testLastCaseBad() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(5);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
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
	//  switch (a) {
	//  case 1:
	//    break;
	//  }
	// }
	public void testEmptyLastCaseOKbreak() {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a;
	//  switch (a) {
	//  case 1:
	//    return;
	//  }
	// }
	public void testEmptyLastCaseWithReturn() {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(int a) {
	//  while (a--)
	//  switch (a) {
	//  case 1:
	//    continue;
	//  }
	// }
	public void testEmptyLastCaseWithContinue() {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(int a) {
	//
	//  switch (a) {
	//  case 1:
	//    throw 1;
	//  }
	// }
	public void testEmptyLastCaseWithThrow() {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    b = 2;
	//    break;
	//  }
	// }
	public void testLastCaseOKbreak() {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    break;
	//  case 2:
	//    b = 2;
	//    break;
	//  }
	// }
	public void testEmptyCaseOKbreak() {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a;
	//  switch (a) {
	//  case 1:
	//    /* no break */
	//  }
	// }
	public void testEmptyLastCaseOKcomment() {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(int a, int b) {
	//  switch (a) {
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
		checkErrorLines(7);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    b = 2;
	//    /* no break */
	//  }
	// }
	public void testLastCaseOKcomment() {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    /* no break */
	//  case 2:
	//    b = 2;
	//    break;
	//  }
	public void testEmptyCaseOKcomment() {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    b = 2;
	//    /* no break */
	//    bye();
	//  }
	// }
	public void testLastCaseBadCommentNotLast() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(7);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
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
	//  case 7:
	//    b = 2;
	//    /* fallthrough */
	//  }
	// }
	public void testDifferentComments() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(17,23);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1: //err
	//    // lolo
	//  case 2: //err
	//  case 3://err
	//  }
	//
	//  switch (a) {
	//  case 1:
	//    b = 2; // err
	//    // lolo
	//  case 2:
	//    b = 2; // err
	//  case 3: // err
	//  case 4:
	//    break;
	//  case 5: // err
	//  case 6: // err
	//  }
	//
	//  switch (a) {
	//  case 1:
	//    b = 2; // err
	//    // lolo
	//  case 2:
	//    b = 2; //err
	//  case 3:
	//    b = 2;
	//    /* no break */
	//  case 4:
	//    b = 2; // err
	//  case 5:
	//    b = 2;
	//    break;
	//  case 6:
	//    b = 2;
	//    /* no break */
	//    b = 2; //err
	//  case 7:
	//    b = 2;//err
	//  }
	//
	//  switch (a) {
	//  case 1:
	//    b = 2; // err
	//    // lolo
	//  case 2:
	//    b = 2; // err
	//  default: //err
	//  }
	// }
	public void testGeneral1() {
		setEmpty(true);
		setLast(true);
		loadCodeAndRun(getAboveComment());
		checkErrorComments();
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
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
		checkErrorLines(9, 14);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 0:
	//    switch( b ) {
	//    case 2: // err
	//    } // err
	//
	//  case 1:
	//    switch( b ) {
	//    case 2:
	//      break;
	//    } // err
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
	//    } // err
	//  case 5:
	//    switch( b ) {
	//    case 2: // err
	//    }
	//    /* no break */
	//  }
	// }
	public void testNestedSwitches() {
		loadCodeAndRun(getAboveComment());
		checkErrorComments();
	}

	// void foo(void) {
	//   int a, b;
	//   switch (a) {
	//   case 1:
	//     b = 2;
	//   }
	// }
	public void testLastCaseIgnore() {
		setLast(false);
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
		setLast(true);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
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
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a;
	//  switch (a) {
	//  case 1:
	//  }
	// }
	public void testEmptyLastCaseIgnore() {
		String code = getAboveComment();
		setLast(false);
		loadCodeAndRun(code);
		checkNoErrorsOfKind(ER_ID);
		setLast(true);
		setEmpty(false);
		loadCodeAndRun(code);
		checkErrorLine(4, ER_ID);
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
	//   switch (a) {
	//   case 1:
	//     while (a--)
	//       break; // err
	//   case 2:
	//     while (a--) {
	//       break;
	//     } // err
	//   }
	// }
	public void testEmptyCaseWithLoopBreak() {
		loadCodeAndRun(getAboveComment());
		checkErrorComments();
	}

	// void foo(int a) {
	//   switch (a) {
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
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//   int a;
	//   switch (a) {
	//   case 2:
	//     break;
	//   case 1:
	//   }
	// }
	public void testEmptyLastCaseError() {
		String code = getAboveComment();
		setLast(true);
		setEmpty(false);
		loadCodeAndRun(code);
		checkErrorLine(6, ER_ID);
		setLast(false);
		loadCodeAndRun(code);
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(int a) {
	//   switch (a) {
	//   case 2:
	//     if (a*2<10)
	//       return;
	//     else
	//       break;
	//   case 1:
	//     break;
	//   }
	// }
	public void testIf() {
		String code = getAboveComment();
		loadCodeAndRun(code);
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(int a) {
	//   switch(a) {
	//   case 2:
	//     if (a*2<10)
	//       return;
	//     else
	//       a++;
	//   case 1:
	//     break;
	//   }
	// }
	public void testIfErr() {
		String code = getAboveComment();
		loadCodeAndRun(code);
		checkErrorLine(7, ER_ID);
	}

	//	#define DEFINE_BREAK {break;}
	//	void foo(int a) {
	//	  switch (a) {
	//	    case 1:
	//	      DEFINE_BREAK  // No warning here
	//	  }
	//	}
	public void testBreakInBraces() {
		String code = getAboveComment();
		loadCodeAndRun(code);
		checkNoErrorsOfKind(ER_ID);
	}

	//	#define MY_MACRO(i) \
	//	  case i: {         \
	//	  }
	//
	//	void f() {
	//	  int x;
	//	  switch (x) {
	//	    MY_MACRO(1)  // No warning here
	//	  }
	//	}
	public void testInMacro() {
		String code = getAboveComment();
		loadCodeAndRun(code);
		checkNoErrorsOfKind(ER_ID);
	}

	//  void foo() {
	//    switch (0)
	//    default: {
	//    }
	//  }
	public void testEmptyCompoundStatement() {
		String code = getAboveComment();
		loadCodeAndRun(code);
		checkErrorLine(4, ER_ID);
	}
}
