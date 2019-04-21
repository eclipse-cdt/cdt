/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.MagicNumberChecker;

/**
 * Test for {@link MagicNumberChecker} class
 */
public class MagicNumberCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = MagicNumberChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//void bar() {
	//	int a = -18;
	//}
	public void testWithInit() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void bar() {
	//	int a = -18;
	//	a = 15;
	//}
	public void testMagicNumberAssignment() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//void foo(int p) {
	//}
	//void bar() {
	//	int a = -18;
	//	foo(15);
	//}
	public void testMagicNumberFunctionCall() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}

	//void bar() {
	//	int a[100];
	//}
	public void testMagicNumberArray() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, ERR_ID);
	}

	//void bar() {
	//	int a = -18;
	//	a = 2;
	//}
	public void testException() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void bar() {
	//	int a = -18;
	//	a = 238;
	//}
	public void tesCustomException() throws Exception {
		setPreferenceValue(ERR_ID, MagicNumberChecker.PARAM_EXCEPTIONS, new String[] { "238" });
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void bar() {
	//	int a[100];
	//	++a[8];
	//}
	public void testArrayDisabled() throws Exception {
		setPreferenceValue(ERR_ID, MagicNumberChecker.PARAM_ARRAY, false);
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Foo {
	//private:
	//	int a[2];
	//public:
	//	Foo(int val) {
	//		a[0] = val;
	//	}
	//	int operator()(int idx) const {
	//		return a[idx];
	//	}
	//};
	//void bar() {
	//	Foo f(111);
	//	f(88);
	//}
	public void testOperatorParenDisabled() throws Exception {
		setPreferenceValue(ERR_ID, MagicNumberChecker.PARAM_OPERATOR_PAREN, false);
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Foo {
	//private:
	//	int a[2];
	//public:
	//	Foo(int val) {
	//		a[0] = val;
	//	}
	//	int operator()(int idx) const {
	//		return a[idx];
	//	}
	//};
	//void bar() {
	//	Foo f(111);
	//	f(88);
	//}
	public void testOperatorParenEnabled() throws Exception {
		setPreferenceValue(ERR_ID, MagicNumberChecker.PARAM_OPERATOR_PAREN, true);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(14, ERR_ID);
	}

	//class Foo {
	//private:
	//	int a[2];
	//public:
	//	Foo(int val) {
	//		a[0] = val;
	//	}
	//	int operator()(int idx) const {
	//		return a[idx];
	//	}
	//};
	//class Baz : public Foo {}
	//void bar() {
	//	Baz f(111);
	//	f(88);
	//}
	public void testOperatorParenEnabledHierarchy() throws Exception {
		setPreferenceValue(ERR_ID, MagicNumberChecker.PARAM_OPERATOR_PAREN, false);
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
