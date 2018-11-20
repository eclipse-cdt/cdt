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

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;

import junit.framework.TestSuite;

public class FunctionTests extends TestBase {
	public static class NonIndexing extends FunctionTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends FunctionTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	struct S {
	//		int x;
	//	};
	//	constexpr int g(S const& s) {
	//		return s.x;
	//	}
	//	constexpr int f() {
	//		S s{5};
	//		return g(s);
	//	}

	//	constexpr int x = f();
	public void testAccessMemberOfCompositeParameter() throws Exception {
		assertEvaluationEquals(5);
	}

	// constexpr int function(int n) { return n > 0 ? n + function(n-1) : n; }

	// constexpr int x = function(10);
	public void testRecursion() throws Exception {
		assertEvaluationEquals(55);
	}

	// constexpr int helper(int n) {
	//   int m = 5;
	//   return m + n;
	// }
	// constexpr int function() {
	//   int value = helper(5);
	//   return value + helper(5);
	// }

	// constexpr int x = function();
	public void testEvaluationOfConstexprFunctionCalls() throws Exception {
		assertEvaluationEquals(20);
	}

	//	constexpr int g(int i) {
	//		i++;
	//		return i;
	//	}
	//
	//	constexpr auto f() {
	//		int a = 3;
	//		int b = g(a);
	//      b++;
	//		return a;
	//	}

	//	constexpr auto x = f();
	public void testFunctionReturnValueIsCopiedAndNotReferenced() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr void incr(int x) {
	//		x = x + 1;
	//	}
	//	constexpr int f() {
	//		int a { 5 };
	//		incr(a);
	//		return a;
	//	}

	//	constexpr auto x = f();
	public void testPassingIntByValue() throws Exception {
		assertEvaluationEquals(5);
	}

	// constexpr void incr(int &x) {
	// 	x++;
	// }
	// constexpr int f() {
	// 	int a { 5 };
	// 	incr(a);
	// 	return a;
	// }

	//	constexpr auto x = f();
	public void testPassingIntByReference1() throws Exception {
		assertEvaluationEquals(6);
	}

	// constexpr void incr(int &x, int &y) {
	//  x++;
	//  y++;
	// }
	// constexpr int f() {
	//  int a { 5 };
	//  incr(a, a);
	//  return a;
	// }

	// constexpr auto x = f();
	public void testPassingIntByReference2() throws Exception {
		assertEvaluationEquals(7);
	}

	//	struct S {
	//		int x;
	//	};
	//	constexpr void g(S s) {
	//		s.x++;
	//	}
	//	constexpr int f() {
	//		S s{5};
	//		g(s);
	//		return s.x;
	//	}

	//	constexpr int x = f();
	public void testPassingCompositeByValue() throws Exception {
		assertEvaluationEquals(5);
	}

	// 	struct Point { int x, y; };
	//	constexpr void incr(Point &point) {
	//		point.x++;
	//	}
	//	constexpr int f() {
	//		Point p{ 2, 4 };
	//		incr(p);
	//		return p.x;
	//	}

	//	constexpr auto x = f();
	public void testPassingCompositeByReference() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int a[2][2] { { 1, 2 }, { 3, 4 } };
	//	constexpr int const * g() {
	//		return a[0];
	//	}
	//	constexpr int f() {
	//		return g()[1];
	//	}

	//	constexpr auto x = f();
	public void testPointerReturnValue() throws Exception {
		assertEvaluationEquals(2);
	}

	//	int const y { 5 };
	//	constexpr int const & g() {
	//		return y;
	//	}
	//	constexpr int f() {
	//		return g() + 1;
	//	}

	//	constexpr auto x = f();
	public void testReferenceReturnValue() throws Exception {
		assertEvaluationEquals(6);
	}

	//	constexpr void side_effect(int array[], int length) {
	//		for (int i = 0; i < length; ++i) {
	//			array[i]++;
	//		}
	//	}
	//	constexpr int f() {
	//		int array[4] { 1, 2, 3, 4 };
	//		side_effect(array, 4);
	//		return array[0];
	//	}

	//	constexpr auto x = f();
	public void testSideEffectsOnArrayParameter() throws Exception {
		assertEvaluationEquals(2);
	}

	//	constexpr int f(int a) {
	//		{
	//			int a = 5;
	//			return a;
	//		}
	//		return a;
	//	}

	//	constexpr int x = f(10);
	public void testBlockScopeValueLookup1() throws Exception {
		assertEvaluationEquals(5);
	}

	//	constexpr int f(int a) {
	//		{
	//			int a = 5;
	//		}
	//		return a;
	//	}

	//	constexpr int x = f(10);
	public void testBlockScopeValueLookup2() throws Exception {
		assertEvaluationEquals(10);
	}

	//	char foo();
	//	constexpr int almost = sizeof(foo());

	//	constexpr int x = almost;
	public void testSizeofCallToRegularFunction() throws Exception {
		assertEvaluationEquals(1);
	}

	//	int f() {
	//		return 5;
	//	}

	//	int x = f();
	public void testNonConstexprFunctionDoesntStoreBodyExecution() throws Exception {
		IASTInitializerClause clause = getLastDeclarationInitializer();
		IASTFunctionCallExpression funcExpr = (IASTFunctionCallExpression) clause;
		IASTIdExpression idExpr = (IASTIdExpression) funcExpr.getFunctionNameExpression();
		ICPPFunction function = (ICPPFunction) idExpr.getName().resolveBinding();
		ICPPExecution bodyExec = CPPFunction.getFunctionBodyExecution(function);
		assertNull(bodyExec);
	}

	//	// Empty header file

	//	struct A {
	//	  A() {}
	//	  A& m(int p) {
	//	    return *this;
	//	  }
	//	};
	//
	//	A a = A()
	//	    .m(1).m(2).m(3).m(4).m(5).m(6).m(7).m(8).m(9).m(10)
	//	    .m(11).m(12).m(13).m(14).m(15).m(16).m(17).m(18).m(19).m(20)
	//	    .m(21).m(22).m(23).m(24).m(25).m(26).m(27).m(28).m(29).m(30);
	public void testLongCallChain_505606() throws Exception {
	}
}