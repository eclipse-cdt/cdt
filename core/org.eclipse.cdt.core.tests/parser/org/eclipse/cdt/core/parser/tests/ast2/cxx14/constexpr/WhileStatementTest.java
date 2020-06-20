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

import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;

import junit.framework.TestSuite;

public class WhileStatementTests extends TestBase {
	public static class NonIndexing extends WhileStatementTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends WhileStatementTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   while (n > 0) {
	//     sum += n;
	// 	   n--;
	// 	 }
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testWhileLoopWithConditionalExpression() throws Exception {
		assertEvaluationEquals(55);
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   while (true) {
	//     sum += n;
	// 	   n--;
	// 	 }
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testEvalShouldAbortOnWhileWitInfiniteLoop() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   while (true) {
	//     sum += n;
	// 	   return 42;
	// 	 }
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testReturnInWhileStatement() throws Exception {
		assertEvaluationEquals(42);
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   while (n > 0)
	// 	   sum += n--;
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testWhileLoopWithNonCompoundBodyStatement() throws Exception {
		assertEvaluationEquals(55);
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   while (n > 0)
	//		return 42;
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testWhileLoopWithReturnInNonCompoundBodyStatement() throws Exception {
		assertEvaluationEquals(42);
	}

	//	constexpr int triple(int x) {
	//	  return x * 3;
	//	}
	//	constexpr int f(int y) {
	//	  int sum = 0;
	//	  while(int x = triple(y--)) {
	//	    sum += x;
	//	  }
	//	  return sum;
	//	}

	//	constexpr int x = f(4);
	public void testDeclarationInWhileStatementCondition1() throws Exception {
		assertEvaluationEquals(30);
	}
}