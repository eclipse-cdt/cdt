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

public class PointerTests extends TestBase {
	public static class NonIndexing extends PointerTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends PointerTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	// constexpr int f() {
	//  int bar[2] { 3, 7 };
	//  int * bar_ptr { bar };
	//  bar_ptr++;
	// 	return *bar_ptr;
	// }

	// constexpr int x = f();
	public void testPointerArithmeticsPostFixIncr() throws Exception {
		assertEvaluationEquals(7);
	}

	// constexpr int f() {
	//  int bar[2] { 3, 7 };
	//  int * bar_ptr { bar };
	//  bar_ptr++;
	//  bar_ptr--;
	// 	return *bar_ptr;
	// }

	// constexpr int x = f();
	public void testPointerArithmeticsPostFixDecr() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int f() {
	//		int n { 0 };
	//		int * nPtr { &n };
	//		nPtr++;
	//		return *nPtr;
	//	}

	//	constexpr int x = f();
	public void testDereferencingOfPointerToInvalidMemoryShouldFail() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar + 2 };
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerArithmeticInDeclaration() throws Exception {
		assertEvaluationEquals(7);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar };
	//		int * bar_ptr2 { bar + 3 };
	//		return bar_ptr2 - bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testSubtractionOfPointersToSameArrayShouldYieldDistance() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar };
	//		bar_ptr = bar_ptr + 1;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerAddition() throws Exception {
		assertEvaluationEquals(5);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar };
	//		bar_ptr += 2;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerAdditionAndAssignment() throws Exception {
		assertEvaluationEquals(7);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar + 2};
	//		bar_ptr = bar_ptr - 2;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerSubtraction() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar + 2 };
	//		bar_ptr -= 2;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerSubtractionAndAssignment() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar };
	//		int * bar_ptr2 { bar_ptr };
	//		return *bar_ptr2;
	//	}

	//	constexpr int x = f();
	public void testPointerDeclarationFromPointer() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar };
	//		int * bar_ptr2 { bar_ptr };
	//		bar_ptr++;
	//		return *bar_ptr2;
	//	}

	//	constexpr int x = f();
	public void testPointersHaveSeparatePositions() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar };
	//		int * bar_ptr2 { bar_ptr + 1 };
	//		return *bar_ptr2;
	//	}

	//	constexpr int x = f();
	public void testPointerAdditionInDeclaration() throws Exception {
		assertEvaluationEquals(5);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar + 3 };
	//		int * bar_ptr2 { bar_ptr - 1 };
	//		return *bar_ptr2;
	//	}

	//	constexpr int x = f();
	public void testPointerSubtractionInDeclaration() throws Exception {
		assertEvaluationEquals(7);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar + 3 };
	//		bar_ptr++;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testDereferencingOnePastTheEndPointerIsInvalid() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar + 3 };
	//		bar_ptr++;
	//		bar_ptr--;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testDereferencingIncrementedOnePastTheEndAndThenDecrementedBackInRageAgainPointerIsValid()
			throws Exception {
		assertEvaluationEquals(11);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar + 3 };
	//		bar_ptr += 2;
	//		bar_ptr -= 2;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testDereferencingIncrementedTWOPastTheEndAndThenDecrementedBackInRageAgainPointerIsInvalid()
			throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar };
	//		bar_ptr--;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerWithNegativePositionIsInvalid() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar };
	//		bar_ptr--;
	//		bar_ptr++;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerThatOnceHasNegativePositionStaysInvalid() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar + 4 };
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerDeclaredOnePastTheEndIsInvalid() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar + 4 };
	//		bar_ptr--;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerDeclaredOnePastTheEndAndThenDecrementedBackInRageAgainIsValid() throws Exception {
		assertEvaluationEquals(11);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar + 5 };
	//		bar_ptr -= 2;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerDeclaredTwoPastTheEndAndThenDecrementedBackInRageAgainStaysInvalid() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar - 1 };
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerDeclaredWithNegativePositionIsInvalid() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int * bar_ptr { bar - 1 };
	//		bar_ptr++;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerDelcaredWithNegativePositionStaysInvalid() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	constexpr int f() {
	//		int a { 1 };   // declaration
	//      int b { 2 };
	//		int * ptr { &a };
	//      ptr = &b;
	//		return *ptr;
	//	}

	//	constexpr auto x = f();
	public void testPointerAssignment() throws Exception {
		assertEvaluationEquals(2);
	}

	//	constexpr auto f() {
	//		int x { 1 };
	//		int * x_ptr { &x };
	//		*x_ptr = 2;
	//		return *x_ptr;
	//	}

	//	constexpr auto x = f();
	public void testPointerValueAssignment() throws Exception {
		assertEvaluationEquals(2);
	}

	//	struct S {
	//		int x, y;
	//	};
	//	constexpr int f() {
	//		S s { 1, 2 };
	//		int * s_ptr { &s.x };
	//		*s_ptr = 3;
	//		return *s_ptr;
	//	}

	//	constexpr auto x = f();
	public void testPointerToStructMember() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int f() {
	//		int a { 5 };
	//		int * aPtr { &a };
	//		(*aPtr)++;
	//		return a;
	//	}

	//	constexpr auto x = f();
	public void testPointer() throws Exception {
		assertEvaluationEquals(6);
	}
}