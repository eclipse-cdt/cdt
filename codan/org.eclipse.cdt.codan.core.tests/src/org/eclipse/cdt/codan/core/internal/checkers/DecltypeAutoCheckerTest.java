/*******************************************************************************
 * Copyright (c) 2017 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
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
import org.eclipse.cdt.codan.internal.checkers.DecltypeAutoChecker;

/**
 * Test for {@link DecltypeAutoChecker} class
 */
public class DecltypeAutoCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = DecltypeAutoChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	// int i { 42 }
	// decltype(i) k = i;
	public void testDecltypeExpressionVariable() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	// int i { 42 }
	// decltype(i) volatile k = i;
	public void testDecltypeExpressionVolatileVariable() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	// int i { 42 }
	// decltype(i) const k = i;
	public void testDecltypeExpressionConstVariable() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	// int i { 42 }
	// decltype(i) const volatile k = i;
	public void testDecltypeExpressionCVVariable() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	// decltype(auto) i { 42 };
	public void testDecltypeAutoVariable() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	// decltype(auto) const i { 42 };
	public void testDecltypeAutoConstVariable() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	// decltype(auto) volatile i { 42 };
	public void testDecltypeAutoVolatileVariable() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	// decltype(auto) const volatile i { 42 };
	public void testDecltypeAutoCVVariable() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	// void foo() {
	//   decltype(auto) const i { 42 };
	// }
	public void testDecltypeAutoConstVariablInsideFunction() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, ERR_ID);
	}

	// void foo() {
	//   decltype(auto) volatile i { 42 };
	// }
	public void testDecltypeAutoVolatileVariableInsideFunction() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, ERR_ID);
	}

	// void foo() {
	//   decltype(auto) const volatile i { 42 };
	// }
	public void testDecltypeAutoCVVariableInsideFunction() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, ERR_ID);
	}

	// decltype(auto) foo() {
	//    return 42;
	// }
	public void testDecltypeAutoReturnType() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	// decltype(auto) const foo() {
	//    return 42;
	// }
	public void testDecltypeAutoConstReturnType() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	// decltype(auto) volatile foo() {
	//    return 42;
	// }
	public void testDecltypeAutoVolatileReturnType() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	// decltype(auto) const volatile foo() {
	//    return 42;
	// }
	public void testDecltypeAutoCVReturnType() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	// auto foo() -> decltype(auto) {
	//    return 42;
	// }
	public void testDecltypeAutoTrailingReturnType() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	// auto foo() -> decltype(auto) const {
	//    return 42;
	// }
	public void testDecltypeAutoConstTrailingReturnType() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	// auto foo() -> decltype(auto) volatile {
	//    return 42;
	// }
	public void testDecltypeAutoVolatileTrailingReturnType() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	// auto foo() -> decltype(auto) const volatile {
	//    return 42;
	// }
	public void testDecltypeAutoCVTrailingReturnType() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}
}
