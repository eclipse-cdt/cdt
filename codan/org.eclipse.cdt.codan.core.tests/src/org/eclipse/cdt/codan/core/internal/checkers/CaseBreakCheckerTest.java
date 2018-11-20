/*******************************************************************************
 * Copyright (c) 2010, 2015 Gil Barash
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gil Barash  - Initial implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
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

	@Override
	public boolean isCpp() {
		return true;
	}

	// void foo(void) {
	//  int a;
	//  switch (a) {
	//  case 1:
	//  }
	// }
	public void testEmptyLastCaseBad() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// void foo(void) {
	//  int a;
	//  switch (a) {
	//  default:
	//  }
	// }
	public void testEmptyLastCaseDefaultBad() throws Exception {
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
	public void testLastCaseBad() throws Exception {
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
	public void testEmptyCaseBad() throws Exception {
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
	public void testEmptyLastCaseOKbreak() throws Exception {
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
	public void testEmptyLastCaseWithReturn() throws Exception {
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
	public void testEmptyLastCaseWithContinue() throws Exception {
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
	public void testEmptyLastCaseWithThrow() throws Exception {
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
	public void testLastCaseOKbreak() throws Exception {
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
	public void testEmptyCaseOKbreak() throws Exception {
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
	public void testEmptyLastCaseOKcomment() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a;
	//  switch (a) {
	//  case 1:
	//    [[fallthrough]]; // invalid in last case
	//  }
	// }
	public void testEmptyLastCaseBadFallthrough_514685() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkErrorLine(5);
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
	public void testEmptyLastCaseTwoSwitches() throws Exception {
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
	public void testLastCaseOKcomment() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    b = 2;
	//    [[fallthrough]];
	//  }
	// }
	public void testLastCaseBadFallthrough_514685() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkErrorLine(5);
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
	// }
	public void testEmptyCaseOKcomment() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    [[fallthrough]];
	//  case 2:
	//    b = 2;
	//    break;
	//  }
	// }
	public void testEmptyCaseOKFallthrough_514685() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    b = 2;
	//    /* no break */
	//    foo();
	//  }
	// }
	public void testLastCaseBadCommentNotLast() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(7);
	}

	// void bye() {}
	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    b = 2;
	//    [[fallthrough]];
	//    bye();
	//  }
	// }
	public void testLastCaseBadFallthroughNotLast_514685() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkErrorLines(8);
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
	public void testDifferentComments() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(17, 23);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    b = 2;
	//    [[fallthrough]];
	//  case 2:
	//    b = 2;
	//    [[ fallthrough ]];
	//  case 3:
	//    b = 2;
	//    [ [ fallthrough ] ] ;
	//  case 4:
	//    b = 2;[[fallthrough]];
	//  case 5:
	//    b = 2;
	//    [[fall]];
	//  case 6:
	//    b = 2;
	//    [[FALLTHROUGH]];
	//  case 7:
	//    b = 2;
	//    [[fallthrough]]
	//  }
	// }
	public void testDifferentFallthroughs_514685() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkErrorLines(16, 19, 24);
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
	//    b = 2;
	//    [[fallthrough]];
	//  case 5:
	//    b = 2; // err
	//  case 6:
	//    b = 2;
	//    break;
	//  case 7:
	//    b = 2;
	//    /* no break */
	//    b = 2; //err
	//  case 8:
	//    b = 2;
	//    [[fallthrough]];
	//    b = 2; //err
	//  case 9:
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
	public void testGeneral1() throws Exception {
		setEmpty(true);
		setLast(true);
		loadCodeAndRunCpp(getAboveComment());
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
	public void testGeneralComments1() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(9, 14);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    b = 2;
	//    // lolo
	//    [[fallthrough]];
	//  case 2:
	//    b = 2;
	//    [[fallthrough]];
	//    // lolo
	//  case 3:
	//    [[fallthrough]]; // not valid, not last statement
	//    b = 2;
	//    // loo
	//  case 4:
	//    b = 2;
	//    // lolo
	//    [[fallthrough]];
	//  case 5:
	//    // lolo
	//    b = 2;
	//    [[fallthrough]]; // not valid in last case
	//  }
	// }
	public void testGeneralFallthroughs1_514685() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkErrorLines(14, 22);
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
	//    case 2:
	//      [[fallthrough]]; // err
	//    } // err
	//  case 6:
	//    switch( b ) {
	//    case 2: // err
	//    }
	//    /* no break */
	//  case 7:
	//    switch( b ) {
	//    case 2: // err
	//    } // err
	//    [[fallthrough]];
	//  }
	// }
	public void testNestedSwitches() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkErrorComments();
	}

	// void foo(void) {
	//   int a, b;
	//   switch (a) {
	//   case 1:
	//     b = 2;
	//   }
	// }
	public void testLastCaseIgnore() throws Exception {
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
	public void testEmptyCaseIgnore() throws Exception {
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
	public void testEmptyLastCaseIgnore() throws Exception {
		String code = getAboveComment();
		setLast(false);
		loadCodeAndRun(code);
		checkNoErrorsOfKind(ER_ID);
		setLast(true);
		setEmpty(false);
		loadCodeAndRun(code);
		checkErrorLine(4, ER_ID);
	}

	// void foo() {
	// int a, b;
	// switch (a) {
	// case 1: {
	//   b = 2;
	//   [[fallthrough]];
	//  }
	// case 2: {
	//   b = 2;
	//  }
	//  [[fallthrough]];
	// case 3: {
	//   {
	//    b = 2;
	//    [[fallthrough]];
	//   }
	//  }
	// case 4: {
	//   b = 2;
	//   break;
	//  }
	// }
	//}
	public void testFallthroughAndCompoundStatementCombinations_514685() throws Exception {
		String code = getAboveComment();
		loadCodeAndRunCpp(code);
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo() {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//   b = 2; // err
	//   [[fallthrough]];
	//  }
	//  switch (a) {
	//  case 1: {
	//   b = 2;
	//   [[fallthrough]];
	//  } // err
	//  }
	//  switch (a) {
	//  case 1: {
	//   b = 2;
	//  } // err
	//   [[fallthrough]];
	//  }
	// }
	public void testBadFallthroughInLastStatement_514685() throws Exception {
		String code = getAboveComment();
		loadCodeAndRunCpp(code);
		checkErrorComments();
	}

	private void setLast(boolean val) {
		IProblemPreference pref = getPreference(CaseBreakChecker.ER_ID, CaseBreakChecker.PARAM_LAST_CASE);
		pref.setValue(val);
	}

	private void setEmpty(boolean val) {
		IProblemPreference pref = getPreference(CaseBreakChecker.ER_ID, CaseBreakChecker.PARAM_EMPTY_CASE);
		pref.setValue(val);
	}

	private void setComment(String str) {
		IProblemPreference pref = getPreference(CaseBreakChecker.ER_ID, CaseBreakChecker.PARAM_NO_BREAK_COMMENT);
		pref.setValue(str);
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
	public void testEmptyCaseWithLoopBreak() throws Exception {
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
	public void testCaseWithCurlyBrackets() throws Exception {
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
	public void testEmptyLastCaseError() throws Exception {
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
	public void testIf() throws Exception {
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
	public void testIfErr() throws Exception {
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
	public void testBreakInBraces() throws Exception {
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
	public void testInMacro() throws Exception {
		String code = getAboveComment();
		loadCodeAndRun(code);
		checkNoErrorsOfKind(ER_ID);
	}

	//  void foo() {
	//    switch (0)
	//    default: {
	//    }
	//  }
	public void testEmptyCompoundStatement() throws Exception {
		String code = getAboveComment();
		loadCodeAndRun(code);
		checkErrorLine(4, ER_ID);
	}

	//	void foo() {
	//		switch (0) {
	//		case 0:
	//			return 42;;
	//		case 1:
	//			break;
	//		}
	//	}
	public void testDoubleSemicolon_bug441714() throws Exception {
		String code = getAboveComment();
		loadCodeAndRun(code);
		checkNoErrorsOfKind(ER_ID);
	}

	// void foo(void) {
	//  int a, b;
	//  switch (a) {
	//  case 1:
	//    b = 2;
	//    // lolo
	//    /* fallthru */
	//  case 2:
	//    b = 2;
	//    /* falls thru */
	//    // lolo
	//  case 3:
	//    /* no break */
	//    b = 2;
	//    // loo
	//  case 4:
	//    b = 2;
	//    // lolo
	//    /* fallthrough */
	//  case 5:
	//    // lolo
	//    b = 2;
	//    /* fall-thru */
	//  }
	// }
	public void testCommentsParam() throws Exception {
		setComment("fall(s)?[ \\t-]*thr(ough|u)");
		loadCodeAndRun(getAboveComment());
		checkErrorLines(9, 14);
	}
}
