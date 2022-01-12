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
import org.eclipse.cdt.codan.internal.checkers.BlacklistChecker;

/**
 * Test for {@link BlacklistChecker} class
 */
public class BlacklistCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = BlacklistChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//void* dontcall() {
	//   return 0;
	//}
	//void foo() {
	//   int * p = (int*)dontcall());
	//}
	public void testWithFunctionCall() throws Exception {
		setPreferenceValue(BlacklistChecker.ERR_ID, BlacklistChecker.PARAM_BLACKLIST, new String[] { "dontcall" });
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
		checkErrorLine(5, ERR_ID);
	}

	//void* dontcall() {
	//   return 0;
	//}
	//void foo() {
	//   void* (*ptr)() = dontcall;
	//}
	public void testWithFunctionPtr() throws Exception {
		setPreferenceValue(BlacklistChecker.ERR_ID, BlacklistChecker.PARAM_BLACKLIST, new String[] { "dontcall" });
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
		checkErrorLine(5, ERR_ID);
	}

	//class Foo {
	//public:
	//   void* dontcall() {
	//      return 0;
	//   }
	//}
	//void foo() {
	//   Foo f;
	//	 f.dontcall();
	//}
	public void testWithMethod() throws Exception {
		setPreferenceValue(BlacklistChecker.ERR_ID, BlacklistChecker.PARAM_BLACKLIST, new String[] { "Foo::dontcall" });
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
		checkErrorLine(9, ERR_ID);
	}

	//void* dontcall() {
	//   return 0;
	//}
	//void foo() {
	//   int * p = (int*)dontcall());
	//}
	public void testWithEmptyList() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
