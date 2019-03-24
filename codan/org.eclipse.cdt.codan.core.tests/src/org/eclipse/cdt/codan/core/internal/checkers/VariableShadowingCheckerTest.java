/*******************************************************************************
 * Copyright (c) 2010 Gil Barash
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Gil Barash  - Initial implementation
 *    Marco Stornelli - Improvements
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.VariableShadowingChecker;

/**
 * Test for {@link#VariableShadowingChecker} class
 */
public class VariableShadowingCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = VariableShadowingChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
		setMarkShadowed(true);
	}

	private void setMarkShadowed(boolean val) {
		IProblemPreference pref = getPreference(VariableShadowingChecker.ERR_ID,
				VariableShadowingChecker.PARAM_MARK_SHADOWED);
		pref.setValue(val);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	// int a;
	// void foo(void) {
	//  int a;
	// }
	public void testGlobalVSFuncLoc() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1, 3);
	}

	// int a;
	// class c {
	//  int a;
	// };
	public void testGlobalVSClass() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1, 3);
	}

	// int a;
	// void foo(int a) {
	// }
	public void testGlobalVSFuncParam() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1, 2);
	}

	// int a;
	// void foo(void) {
	//  for( int a = 1; a < 2; a++ ) {
	//  }
	// }
	public void testGlobalVSFor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1, 3, 1, 3); // for some reason, "a" is also found as a local variable in the scope of the function "foo"...
	}

	// class c {
	//  int a;
	//  void foo(void) {
	//   int a;
	//  }
	// };
	public void testClassVSFuncLoc() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2, 4);
	}

	// class c {
	//  int a;
	//  void foo(int a) {
	//  }
	// };
	public void testClassVSFuncParam() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2, 3);
	}

	// class c {
	//  int a;
	//  void foo(void) {
	//   for( int a = 1; a < 2; a++ ) {
	//   }
	//  }
	// };
	public void testClassVSFor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2, 4, 2, 4); // Same problem as in "testGlobalVSFor"
	}

	// class c {
	//  int a;
	//  void foo(void) {
	//  }
	// };
	// class c2 {
	//  void foo(void) {
	//   int a;
	//  }
	// };
	public void testClassVSFuncLocOK() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(void) {
	//  int a;
	//  for( int a = 1; a < 2; a++ ) {
	//  }
	// }
	public void testFuncLocVSFor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2, 3);
	}

	// void foo(int a) {
	//  for( int a = 1; a < 2; a++ ) {
	//  }
	// }
	public void testFuncParamVSFor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1, 2);
	}

	// void foo2(int a) {
	// }
	// void foo(void) {
	//  for( int a = 1; a < 2; a++ ) {
	//  }
	// }
	public void testFuncLocVSForOK() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(void) {
	//  for( int a = 1; a < 2; a++ ) {
	//  }
	//  for( int a = 1; a < 2; a++ ) {
	//  }
	// }
	public void test2ForOK() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo(void) {
	//  for( int a = 1; a < 2; a++ ) {
	//   for( int a = 1; a < 2; a++ ) {
	//   }
	//  }
	// }
	public void testInnerFor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2, 3, 2, 3); // Same problem as in "testGlobalVSFor"
	}

	// int a;
	// class c {
	//  int a;
	//  void foo(void) {
	//   int a;
	//   for( int a = 1; a < 2; a++ ) {
	//   }
	//  }
	// };
	public void test5Hirarchies() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1, 1, 1, 3, 3, 3, 5, 5, 5, 6, 6, 6);
	}

	// void foo(int a) {
	//  for( int b = 1; b < 2; b++ ) {
	//   int a;
	//  }
	// }
	public void testFuncParamVSlocal() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1, 3, 1, 3); // Same problem as in "testGlobalVSFor"
	}
}
