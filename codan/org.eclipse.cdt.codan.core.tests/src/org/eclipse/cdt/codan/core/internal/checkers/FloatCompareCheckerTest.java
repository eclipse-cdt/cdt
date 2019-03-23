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
import org.eclipse.cdt.codan.internal.checkers.FloatCompareChecker;

/**
 * Test for {@link FloatCompareChecker} class
 */
public class FloatCompareCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = FloatCompareChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public boolean isHeader() {
		return true;
	}

	//int main() {
	//float a;
	//float b;
	//if (a == b)
	//	return 1;
	//return 0;
	//}
	public void testEqualWithFloat() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}

	//int main() {
	//float a;
	//float b;
	//if (a != b)
	//	return 1;
	//return 0;
	//}
	public void testDifferentWithFloat() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//int main() {
	//double a;
	//double b;
	//if (a == b)
	//	return 1;
	//return 0;
	//}
	public void testWithDouble() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}

	//int main() {
	//double a;
	//float b;
	//if (a == b)
	//	return 1;
	//return 0;
	//}
	public void testWithMixedTypes() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}
}
