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
import org.eclipse.cdt.codan.internal.checkers.VariablesChecker;

/**
 * Test for {@link VariablesChecker} class
 */
public class VariablesCheckerTest extends CheckerTestCase {

	public static final String STATIC_ID = VariablesChecker.STATIC_VAR_ID;
	public static final String MULTI_ID = VariablesChecker.VAR_MULTI_DEC_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(STATIC_ID, MULTI_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public boolean isHeader() {
		return true;
	}

	//static int a = 0;
	public void testStaticVar() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, STATIC_ID);
	}

	//void foo() {
	//    int p = 0, u = 0;
	//}
	public void testMultiInitVar() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, MULTI_ID);
	}

	//void foo() {
	//    int p = 0;
	//    int u = 0;
	//}
	public void testCleanVar() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MULTI_ID);
		checkNoErrorsOfKind(STATIC_ID);
	}

	//struct Bar {
	//   int a;
	//   Bar() {a = 0;}
	//}
	//void foo() {
	//    struct Bar b;
	//}
	public void testWithStructCtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MULTI_ID);
		checkNoErrorsOfKind(STATIC_ID);
	}

	//struct Bar {
	//   int a;
	//}
	//void foo() {
	//    struct Bar b = {0};
	//}
	public void testWithInitStruct() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MULTI_ID);
		checkNoErrorsOfKind(STATIC_ID);
	}

	//union Bar {
	//   int a;
	//   Bar() {a = 0;}
	//}
	//void foo() {
	//    union Bar b;
	//}
	public void testWithUnionCtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MULTI_ID);
		checkNoErrorsOfKind(STATIC_ID);
	}

	//union Bar {
	//   int a;
	//}
	//void foo() {
	//    union Bar b = {0};
	//}
	public void testWithInitUnion() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MULTI_ID);
		checkNoErrorsOfKind(STATIC_ID);
	}

	//class Bar {
	//public:
	//   int a;
	//}
	//void foo() {
	//    Bar b;
	//}
	public void testWithClass() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MULTI_ID);
		checkNoErrorsOfKind(STATIC_ID);
	}

	//int foo() {
	//	try {
	//		throw 1;
	//	} catch(int& e) {
	//	}
	//}
	public void testWithTryCatch() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MULTI_ID);
		checkNoErrorsOfKind(STATIC_ID);
	}
}
