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
import org.eclipse.cdt.codan.internal.checkers.CStyleCastChecker;
import org.eclipse.cdt.codan.internal.checkers.UsingInHeaderChecker;

/**
 * Test for {@link UsingInHeaderChecker} class
 */
public class CStyleCastCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = CStyleCastChecker.ERR_ID;

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
	//	int* p = (int*) malloc(10);
	//};
	public void testUsingInGlobalNamespace() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, ERR_ID);
	}

	//void bar() {
	//	int* p = static_cast<int*>(malloc(10));
	//};
	public void testUsingInNamespace() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
