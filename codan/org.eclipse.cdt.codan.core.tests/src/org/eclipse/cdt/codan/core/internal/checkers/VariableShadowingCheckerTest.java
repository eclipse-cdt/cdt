/*******************************************************************************
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
 *    Marco Stornelli - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.SymbolShadowingChecker;

/**
 * Test for {@link#VariableShadowingChecker} class
 */
public class VariableShadowingCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = SymbolShadowingChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//int a;
	//void foo(void) {
	//	int a;
	//}
	public void testGlobalFuncLoc() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//int a;
	//void foo(void) {
	//	for( int a = 1; a < 2; a++ ) {
	//	}
	//}
	public void testGlobalFor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//class Fpp {
	//	int a;
	//	void foo() {
	//		int a;
	//	}
	//};
	public void testClassVSFuncLoc() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}

	//class Fpp {
	//	int a;
	//	void foo() {
	//		for( int a = 1; a < 2; a++ ) {
	//		}
	//	}
	//};
	public void testClassVSFor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}

	//class Fpp {
	//	int a;
	//	void foo() {
	//	}
	//};
	//class Bar {
	//	void foo() {
	//		int a;
	//	}
	//};
	public void testOtherClassVSFuncLoc() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void foo() {
	//	int a;
	//	for( int a = 1; a < 2; a++ ) {
	//	}
	//}
	public void testFuncLocVSFor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//void bar(int a) {
	//}
	//void foo(void) {
	//	for( int a = 1; a < 2; a++ ) {
	//	}
	//}
	public void testFuncLocVSForOK() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void foo() {
	//	for( int a = 1; a < 2; a++ ) {
	//	}
	//	for( int a = 1; a < 2; a++ ) {
	//	}
	//}
	public void test2ForOK() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void foo() {
	//	for( int a = 1; a < 2; a++ ) {
	//		for( int a = 1; a < 2; a++ ) {
	//		}
	//	}
	//}
	public void testInnerFor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//int a;
	//class Foo {
	//	int a;
	//	void foo() {
	//		int a;
	//		for( int a = 1; a < 2; a++ ) {
	//		}
	//	}
	//};
	public void test5Hirarchies() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, ERR_ID);
	}

	//class Foo {
	//	int a;
	//	void foo(int a) {
	//		a = 1;
	//	}
	//};
	public void testParameter() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//class Foo {
	//	void foo(int a) {
	//		a = 1;
	//	}
	//	void bar(int a) {
	//		a = 1;
	//	}
	//};
	public void testParameterTwoMethods() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Foo {
	//	int a;
	//	Foo() {
	//		class Local {
	//			void localMethod(int a) {
	//			}
	//		};
	//	}
	//};
	public void testParameterLocalClassMethod() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}

	//class Foo {
	//	int a;
	//	Foo() {
	//		class Local {
	//			void localMethod() {
	//				int a = 1;
	//			}
	//		};
	//	}
	//};
	public void testLocalClassAfter() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, ERR_ID);
	}

	//class Foo {
	//	Foo() {
	//		class Local {
	//			void localMethod() {
	//				int a = 1;
	//			}
	//		};
	//	}
	//	int a;
	//};
	public void testLocalClassBefore() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}

	//int foo() {
	//	int c = 0;
	//	switch(c) {
	//		case default: {
	//			int a = c;
	//			a++;
	//		}
	//	}
	//	int a = c + 1;
	//	return a;
	//};
	public void testSwitch() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
