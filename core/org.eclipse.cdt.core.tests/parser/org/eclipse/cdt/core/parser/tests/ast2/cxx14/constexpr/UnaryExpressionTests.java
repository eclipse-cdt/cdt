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

public class UnaryExpressionTests extends TestBase {
	public static class NonIndexing extends UnaryExpressionTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends UnaryExpressionTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	// constexpr int doubleIncrement(int n) {
	//   ++n;
	//   ++n;
	//   return n;
	// }

	// constexpr int x = doubleIncrement(0);
	public void testSimpleSequence() throws Exception {
		assertEvaluationEquals(2);
	}

	// constexpr int function() {
	//   int n { 0 };
	//   int m { n++ };
	//   int o { n++ };
	//   return n;
	// }

	// constexpr int x = function();
	public void testAssignmentWithPostfixIncrSideEffects() throws Exception {
		assertEvaluationEquals(2);
	}

	// constexpr int function() {
	//   int n { 0 };
	//   int m { n-- };
	//   int o { n-- };
	//   return n;
	// }

	// constexpr int x = function();
	public void testAssignmentWithPostfixDecrSideEffects() throws Exception {
		assertEvaluationEquals(-2);
	}

	// constexpr int function() {
	//   int n { 0 };
	//   int m { --n };
	//   int o { --n };
	//   return n;
	// }

	// constexpr int x = function();
	public void testAssignmentWithPrefixDecrSideEffects() throws Exception {
		assertEvaluationEquals(-2);
	}

	// constexpr int function() {
	//   int n { 0 };
	//   int m { ++n };
	//   int o { ++n };
	//   return n;
	// }

	// constexpr int x = function();
	public void testAssignmentWithPrefixIncrSideEffects() throws Exception {
		assertEvaluationEquals(2);
	}

	// constexpr int function() {
	//   int n { 0 };
	//   return n++;
	// }

	// constexpr int x = function();
	public void testPostfixIncrSemantics() throws Exception {
		assertEvaluationEquals(0);
	}

	// constexpr int function() {
	//   int n { 0 };
	//   return n++;
	// }

	// constexpr int x = function();
	public void testPostfixDecrSemantics() throws Exception {
		assertEvaluationEquals(0);
	}

	// constexpr int function() {
	//   int n { 0 };
	//   return ++n;
	// }

	// constexpr int x = function();
	public void testPrefixIncrSemantics() throws Exception {
		assertEvaluationEquals(1);
	}

	// constexpr int function() {
	//   int n { 0 };
	//   return --n;
	// }

	// constexpr int x = function();
	public void testPrefixDecrSemantics() throws Exception {
		assertEvaluationEquals(-1);
	}

	//	constexpr int f() {
	//	  int x = 2;
	//	  ++(++x);
	//	  return x;
	//	}

	//	constexpr int x = f();
	public void testPrefixIncrementReturnsLvalue() throws Exception {
		assertEvaluationEquals(4);
	}

	//	struct BooleanConvertible {
	//		bool value;
	//		constexpr explicit operator bool() const {
	//			return value;
	//		}
	//	};
	//	constexpr BooleanConvertible variable{true};

	//	constexpr bool actual = !variable;
	public void testContextualConversionInNot_506972() throws Exception {
		assertEvaluationEquals(false);
	}
}
