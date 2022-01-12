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
	//	float a;
	//	float b;
	//	if (a == b)
	//		return 1;
	//	return 0;
	//}
	public void testEqualWithFloat() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}

	//int main() {
	//	float a;
	//	float b;
	//	if (a != b)
	//		return 1;
	//	return 0;
	//}
	public void testDifferentWithFloat() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}

	//int main() {
	//	double a;
	//	double b;
	//	if (a == b)
	//		return 1;
	//	return 0;
	//}
	public void testWithDouble() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}

	//int main() {
	//	double a;
	//	float b;
	//	if (a == b)
	//		return 1;
	//	return 0;
	//}
	public void testWithMixedTypes() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}

	//typedef float myType;
	//int main() {
	//	myType a;
	//	myType b;
	//	if (a == b)
	//		return 1;
	//	return 0;
	//}
	public void testWithTypedef() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}

	//int main() {
	//	float a1;
	//	float b1;
	//	const float& a = a1;
	//	const float& b = b1;
	//	if (a == b)
	//		return 1;
	//	return 0;
	//}
	public void testWithQualififedTypes() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, ERR_ID);
	}

	//int main() {
	//	float a1;
	//	float b1;
	//	float* a = &a1;
	//	float* b = &b1;
	//	if (*a == *b)
	//		return 1;
	//	return 0;
	//}
	public void testWithDerefPointers() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, ERR_ID);
	}

	//int main() {
	//	float a1;
	//	float b1;
	//	float* a = &a1;
	//	float* b = &b1;
	//	if (a == b)
	//		return 1;
	//	return 0;
	//}
	public void testWithPointers() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//int foo() {
	//	float a1;
	//	float b1;
	//	return a1 == b1 ? 0 : 1;
	//}
	public void testWithTernaryOperator() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}

	//int foo() {
	//	float a1;
	//	if (a1 <= 3.14 && a1 >= 3.14)
	//		a1++;
	//}
	public void testWithIndirectEqual1() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//int foo() {
	//	float a1;
	//	if (a1 >= 3.14 && a1 <= 3.14)
	//		a1++;
	//}
	public void testWithIndirectEqual2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//int foo() {
	//	float a1;
	//	if (!(a1 != 2))
	//		a1++;
	//}
	public void testWithIndirectEqual3() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//int foo() {
	//	float a1;
	//	if (!(a1 < 3.14 || a1 > 3.14))
	//		a1++;
	//}
	public void testWithIndirectEqual4() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//int foo() {
	//	float a1;
	//	if (a1 < 3.14 || a1 > 3.14)
	//		a1++;
	//}
	public void testWithIndirectNotEqual1() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//int foo() {
	//	float a1;
	//	if (a1 > 3.14 || a1 < 3.14)
	//		a1++;
	//}
	public void testWithIndirectNotEqual2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//int foo() {
	//	float a1;
	//	if (a1 < 3.14 || a1 > 3.15)
	//		a1++;
	//}
	public void testWithUpperLowerBounds() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
