/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx14.constexpr;

import junit.framework.TestSuite;

public class FloatingPointValueTests extends TestBase {
	public static class NonIndexing extends FloatingPointValueTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends FloatingPointValueTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	// 	constexpr auto x = 2.5;
	public void testDoubleLiteral() throws Exception {
		assertEvaluationEquals(2.5);
	}

	//	constexpr auto x = -2.5;
	public void testNegativeDoubleLiteral() throws Exception {
		assertEvaluationEquals(-2.5);
	}

	//  constexpr auto x = .5f;
	public void testFloatLiteral() throws Exception {
		assertEvaluationEquals(0.5);
	}

	//	constexpr auto x = 2.l;
	public void testLongDoubleLiteral() throws Exception {
		assertEvaluationEquals(2.0);
	}

	//	constexpr auto x = 123.456e-67;
	public void testDoubleLiteralWithScientificNotation() throws Exception {
		assertEvaluationEquals(123.456e-67);
	}

	//	constexpr auto x = .1E4f;
	public void testFloatLiteralWithScientificNotation() throws Exception {
		assertEvaluationEquals(.1E4f);
	}

	//	constexpr double f() {
	//	  double x = 5.5;
	//	  double y = 2.1;
	//	  return x * 4 + y / 3;
	//	}

	//	constexpr double x = f();
	public void testBinaryOperationsWithFloatingPointNumbers() throws Exception {
		assertEvaluationEquals(22.7);
	}

	//	constexpr bool f() {
	//	  double x = 5.0;
	//	  int y = 5;
	//	  return x == y;
	//	}

	//	constexpr bool x = f();
	public void testComparisonBetweenFloatingPointValueAndIntegralValue1() throws Exception {
		assertEvaluationEquals(true);
	}

	//	constexpr bool f() {
	//	  double x = 5.1;
	//	  int y = 5;
	//	  return x == y;
	//	}

	//	constexpr bool x = f();
	public void testComparisonBetweenFloatingPointValueAndIntegralValue2() throws Exception {
		assertEvaluationEquals(false);
	}

	//	constexpr auto x = float{} + float();
	public void testFloatDefaultValue() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr auto f() {
	//		float x{};
	//		return x;
	//	}

	//	constexpr auto x = f();
	public void testFloatValueInitialization() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr auto x = double{} + double();
	public void testDoubleDefaultValue() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr auto f() {
	//		double x{};
	//		return x;
	//	}

	//	constexpr auto x = f();
	public void testDoubleValueInitialization() throws Exception {
		assertEvaluationEquals(0);
	}
}