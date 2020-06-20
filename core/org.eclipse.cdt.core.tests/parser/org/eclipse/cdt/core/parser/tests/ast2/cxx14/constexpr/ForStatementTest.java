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

public class ForStatementTests extends TestBase {
	public static class NonIndexing extends ForStatementTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends ForStatementTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   for (int i = 0; i <= n; i++) {
	//     sum += i;
	// 	 }
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testSimpleIndexBasedForLoop() throws Exception {
		assertEvaluationEquals(55);
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   for (int i = 0; i <= n; i++) {
	//     sum += i;
	//     return 42;
	// 	 }
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testReturnInIndexBasedForLoop() throws Exception {
		assertEvaluationEquals(42);
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   for (int i = 0; true; i++) {
	//     sum += i;
	// 	 }
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testInfiniteLoopInIndexBasedForLoop() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//	 int i { 0 };
	//   for (; i < n; i++) {
	//     sum += i;
	// 	 }
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testIndexBasedForLoopWithEmptyInitializationStatement() throws Exception {
		assertEvaluationEquals(45);
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   for (int i = 0; i < n;) {
	//     sum += i++;
	// 	 }
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testIndexBasedForLoopWithEmptyIterationSequence() throws Exception {
		assertEvaluationEquals(45);
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   for (int i = 0; i <= n; i++)
	//     sum += i;
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testIndexBasedForLoopWithNonCompoundBodyStatement() throws Exception {
		assertEvaluationEquals(55);
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   for (int i = 0; i <= n; i++)
	//     return 42;
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testIndexBasedForLoopWithReturnInNonCompoundBodyStatement() throws Exception {
		assertEvaluationEquals(42);
	}

	//	constexpr int f() {
	//		int sum = 0;
	//		for(int i = 0; i < 10; ++i) {
	//			sum++;
	//			continue;
	//			sum++;
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testIndexBasedForLoopWithContinueStatement() throws Exception {
		assertEvaluationEquals(10);
	}

	//	constexpr int f() {
	//		int sum = 0;
	//		int arr[] = {1,2,3,4,5,6,7,8,9,10};
	//		for(int i = 0; i < 10; ++i) {
	//			if(i % 2 == 0) {
	//				continue;
	//			}
	//			sum += arr[i];
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testIndexBasedForLoopWithNestedContinueStatement() throws Exception {
		assertEvaluationEquals(30);
	}

	//	constexpr int f() {
	//		int sum = 0;
	//		int arr[] = {1,2,3,4,5,6,7,8,9,10};
	//		for(int i = 0; i < 10; ++i) {
	//			if(i == 5) {
	//				break;
	//			}
	//			sum += arr[i];
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testIndexBasedForLoopWithNestedBreakStatement() throws Exception {
		assertEvaluationEquals(15);
	}

	//	constexpr int triple(int x) {
	//		return x * 3;
	//	}
	//	constexpr int f() {
	//		int sum = 0;
	//		for(int y = 4; int x = triple(y); y--) {
	//			sum += x;
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testDeclarationInForStatementCondition1() throws Exception {
		assertEvaluationEquals(30);
	}

	//	constexpr int f() {
	//		int count = 0;
	//		for(;;) {
	//			if(count++ > 10) {
	//				break;
	//			}
	//		}
	//		return count;
	//	}

	//	constexpr int x = f();
	public void testInfiniteForLoop() throws Exception {
		assertEvaluationEquals(12);
	}

	//	constexpr int fac(int n) {
	//		int result = 1;
	//		for(int i = 1; i <= n; result *= i++);
	//		return result;
	//	}

	//	constexpr int x = fac(5);
	public void testForLoopWithNullStatementAsBody() throws Exception {
		assertEvaluationEquals(120);
	}
}