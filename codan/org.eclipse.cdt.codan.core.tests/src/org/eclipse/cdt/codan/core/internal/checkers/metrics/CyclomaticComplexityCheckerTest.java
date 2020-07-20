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
import org.eclipse.cdt.codan.internal.checkers.metrics.CyclomaticComplexityChecker;

/**
 * Test for {@link CyclomaticComplexityChecker} class
 */
public class CyclomaticComplexityCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = CyclomaticComplexityChecker.ER_CYCLOMATIC_COMPLEXITY_EXCEEDED_ID;

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
	//  return;
	//}
	public void testGood() throws Exception {
		setPreferenceValue(ERR_ID, "maxCyclomaticComplexity", 1);
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//void foo() {
	//  if (true == false);
	//  return;
	//}
	public void testBadWithIf() throws Exception {
		setPreferenceValue(ERR_ID, "maxCyclomaticComplexity", 1);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	//void foo() {
	//  int a = true == false ? 0 : 1;
	//  return;
	//}
	public void testBadWithTernary() throws Exception {
		setPreferenceValue(ERR_ID, "maxCyclomaticComplexity", 1);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	//void foo() {
	//  boolean a = true == false || false == true;
	//  return;
	//}
	public void testBadWithLogical() throws Exception {
		setPreferenceValue(ERR_ID, "maxCyclomaticComplexity", 1);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

}
