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
	public static final String MISS_INIT_ID = VariablesChecker.VAR_MISS_INIT_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(STATIC_ID, MULTI_ID, MISS_INIT_ID);
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
	//    int p;
	//}
	public void testNoInitVar() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, MISS_INIT_ID);
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
		checkNoErrorsOfKind(MISS_INIT_ID);
		checkNoErrorsOfKind(MULTI_ID);
		checkNoErrorsOfKind(STATIC_ID);
	}
}
