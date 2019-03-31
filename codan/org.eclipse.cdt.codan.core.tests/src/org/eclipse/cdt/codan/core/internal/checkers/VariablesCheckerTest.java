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
import org.eclipse.cdt.codan.internal.checkers.VariableInitializationChecker;

/**
 * Test for {@link VariableInitializationChecker} class
 */
public class VariablesCheckerTest extends CheckerTestCase {

	public static final String STATIC_ID = VariableInitializationChecker.STATIC_VAR_ID;
	public static final String MULTI_ID = VariableInitializationChecker.VAR_MULTI_DEC_ID;

	private boolean header;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		header = true;
		enableProblems(STATIC_ID, MULTI_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public boolean isHeader() {
		return header;
	}

	//static int a = 0;
	public void testStaticVarInHeader() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, STATIC_ID);
	}

	//static int a = 0;
	public void testStaticVarInCpp() throws Exception {
		header = false;
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(STATIC_ID);
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

	//void foo() {
	//		for (int i = 0, j = 1; i < 10; ++i, ++j) {
	//		}
	//}
	public void testFor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MULTI_ID);
		checkNoErrorsOfKind(STATIC_ID);
	}

	//void foo() {
	//		if (int i = 0, j = 0; i + j  < 0) {
	//		}
	//}
	public void testIf() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MULTI_ID);
		checkNoErrorsOfKind(STATIC_ID);
	}

	//void foo() {
	//		switch (int i = 0, j = 0; i + j) {
	//		default:
	//			break;
	//		}
	//}
	public void testSwitch() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MULTI_ID);
		checkNoErrorsOfKind(STATIC_ID);
	}
}
