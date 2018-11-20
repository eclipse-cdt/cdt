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

public class BinaryExpressionTests extends TestBase {
	public static class NonIndexing extends BinaryExpressionTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends BinaryExpressionTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	// constexpr bool f() {
	//	bool a { true };
	//	return a && false;
	// }

	// constexpr int x = f();
	public void testSimpleBooleanValues() throws Exception {
		assertEvaluationEquals(false);
	}

	// 	constexpr int f() {
	// 		int x = 5;
	//		(x=3)++;
	//		return x;
	//	}

	//	constexpr auto x = f();
	public void testAssignmentReturnsLValue() throws Exception {
		assertEvaluationEquals(4);
	}

	// constexpr int addTwice(int op1, int op2) {
	//   op1 += op2;
	//   op1 += op2;
	//   return op1;
	// }

	// constexpr int x = addTwice(2, 5);
	public void testBinaryExpressionSequence() throws Exception {
		assertEvaluationEquals(12);
	}

	//	struct BooleanConvertible {
	//		bool value;
	//		constexpr explicit operator bool() const {
	//			return value;
	//		}
	//	};
	//	constexpr BooleanConvertible variable{true};

	//	constexpr bool actual = variable && variable;
	public void testContextualConversionInAnd_506972() throws Exception {
		assertEvaluationEquals(true);
	}

	//	struct BooleanConvertible {
	//		bool value;
	//		constexpr explicit operator bool() const {
	//			return value;
	//		}
	//	};
	//	constexpr BooleanConvertible variable{true};

	//	constexpr bool actual = variable || variable;
	public void testContextualConversionInOr_506972() throws Exception {
		assertEvaluationEquals(true);
	}
}
