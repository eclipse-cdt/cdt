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

public class DoWhileStatementTests extends TestBase {
	public static class NonIndexing extends DoWhileStatementTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends DoWhileStatementTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	// constexpr int f(int n) {
	//   int sum { 0 };
	//   int i { 0 };
	//   do {
	//     sum += i;
	//     i++;
	// 	 } while (i <= n);
	//   return sum;
	// }

	// constexpr int x = f(10);
	public void testDoWhile() throws Exception {
		assertEvaluationEquals(55);
	}

	// constexpr int f() {
	//   int sum { 0 };
	//   do {
	//     sum++;
	// 	 } while (true);
	//   return sum;
	// }

	// constexpr int x = f();
	public void testDoWhileInfiniteLoop() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	// constexpr int f() {
	//   int sum { 0 };
	//   do {
	//     return 42;
	// 	 } while (true);
	//   return sum;
	// }

	// constexpr int x = f();
	public void testDoWhileReturn() throws Exception {
		assertEvaluationEquals(42);
	}

	//	constexpr int f(int n) {
	//	  int sum { 0 };
	//	  do
	//	    --n, ++sum;
	//	  while(n > 0);
	//	  return sum;
	//	}

	//	constexpr int x = f(10);
	public void testDoWhileWithNonCompoundBodyStatement() throws Exception {
		assertEvaluationEquals(10);
	}

	//	constexpr int f(int n) {
	//	  int sum { 0 };
	//	  do
	//	    return 42;
	//	  while(n > 0);
	//	  return sum;
	//	}

	//	constexpr int x = f(10);
	public void testDoWhileWithReturnInNonCompoundBodyStatement() throws Exception {
		assertEvaluationEquals(42);
	}
}
