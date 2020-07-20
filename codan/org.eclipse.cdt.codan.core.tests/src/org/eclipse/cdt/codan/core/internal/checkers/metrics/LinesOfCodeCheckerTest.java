/*******************************************************************************
 * Copyright (c) 2020 Sergey Vladimirov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers.metrics;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.metrics.LinesOfCodeChecker;

/**
 * Test for {@link LinesOfCodeChecker} class
 */
public class LinesOfCodeCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = LinesOfCodeChecker.ER_LINES_OF_CODE_EXCEEDED_ID;

	@Override
	public boolean isCpp() {
		return false;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	//void foo() {
	//  int a = 1;
	//  return;
	//}
	public void testNoFuncBodyGood() throws Exception {
		setPreferenceValue(ERR_ID, "maxLines", 2);
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//void foo() {
	//  int a = 1;
	//  int b = 2;
	//  return;
	//}
	public void testNoFuncBodyBad() throws Exception {
		setPreferenceValue(ERR_ID, "maxLines", 2);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	//void foo() {
	//  int a = 1;
	//  return;
	//}
	public void testWithFuncBodyGood() throws Exception {
		setPreferenceValue(ERR_ID, "countFuncBodyTokens", Boolean.TRUE);
		setPreferenceValue(ERR_ID, "maxLines", 4);
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//void foo() {
	//  int a = 1;
	//  int b = 2;
	//  return;
	//}
	public void testWithFuncBodyBad() throws Exception {
		setPreferenceValue(ERR_ID, "countFuncBodyTokens", Boolean.TRUE);
		setPreferenceValue(ERR_ID, "maxLines", 4);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	//void foo() {
	//  int a = 1;
	//  return;
	//}/*
	//
	//
	//
	//}*/
	public void testWithFuncBodyWithLongCommentAfterBody() throws Exception {
		setPreferenceValue(ERR_ID, "countFuncBodyTokens", Boolean.TRUE);
		setPreferenceValue(ERR_ID, "maxLines", 4);
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

}
