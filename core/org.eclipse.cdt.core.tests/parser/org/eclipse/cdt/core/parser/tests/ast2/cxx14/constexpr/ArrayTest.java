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

public class ArrayTests extends TestBase {
	public static class NonIndexing extends ArrayTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends ArrayTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	// constexpr int f() {
	//	int foo[3][2] { {1,2}, {2, 3},{ 4, 5} };
	//	return foo[2][1];
	// }

	// constexpr int x = f();
	public void testInitializationOfMultiDimensionalArrays() throws Exception {
		assertEvaluationEquals(5);
	}

	// constexpr int f() {
	//	int foo[3] { 1, 2, 3 };
	//	foo[1] = foo[0] + foo[2];
	//  return foo[1];
	// }

	// constexpr int x = f();
	public void testAssignmentOfArrays() throws Exception {
		assertEvaluationEquals(4);
	}

	// constexpr int f() {
	//	int foo[3][2] { {1,2}, {2, 3},{ 4, 5} };
	//	foo[0][1] = 3;
	//  return foo[0][1];
	// }

	// constexpr int x = f();
	public void testAssignmentOfMultiDimensionalArrays() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int a[2][2] { { 1, 2 }, { 3, 4 } };
	//	constexpr int f() {
	//		return a[0][0];
	//	}

	//	constexpr auto x = f();
	public void testGlobalArrayAccessValue() throws Exception {
		assertEvaluationEquals(1);
	}

	// constexpr int f() {
	//  int x[2][2] { { 1, 2 }, { 3, 4 } };
	//  int &xref { x[1][1] };
	//  int &xref2 { x[1][1] };
	//  xref++;
	//  xref = xref * xref2;
	//  return x[1][1];
	// }

	// constexpr auto x = f();
	public void testReferenceToArrayCell() throws Exception {
		assertEvaluationEquals(25);
	}

	//	constexpr int f() {
	//		int bar[2] { 3, 7 };
	//		(*bar)++;
	//		return bar[0];
	//	}

	//	constexpr int x = f();
	public void testPointerDereferencingOnArrayName() throws Exception {
		assertEvaluationEquals(4);
	}

	//	class S {
	//		int arr[4];
	//	public:
	//		constexpr S():arr{5,6,7,8} {}
	//		constexpr int *getPtr() {
	//			return arr;
	//		}
	//	};
	//	constexpr int f() {
	//		S s{};
	//		int *ptr = s.getPtr();
	//		return *ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerToArrayReturnedFromMemberFunction1() throws Exception {
		assertEvaluationEquals(5);
	}

	//	class S {
	//		int arr[4];
	//	public:
	//		constexpr S():arr{5,7,9,11} {}
	//		constexpr int *getPtr() {
	//			return arr;
	//		}
	//	};
	//	constexpr int f() {
	//		S s{};
	//		int *ptr = s.getPtr();
	//		ptr += 2;
	//		return *ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerToArrayReturnedFromMemberFunction2() throws Exception {
		assertEvaluationEquals(9);
	}

	//	class S {
	//		int arr[4];
	//	public:
	//	  constexpr S():arr{5,7,9,11} {}
	//	  constexpr int *getBegin() {
	//	    return arr;
	//	  }
	//	  constexpr int *getEnd() {
	//	    return arr + 4;
	//	  }
	//	};
	//
	//	constexpr int f() {
	//	  S s{};
	//	  int *begin = s.getBegin();
	//	  int *end = s.getEnd();
	//	  int sum = 0;
	//	  for(; begin != end; begin++) {
	//	    sum += *begin;
	//	  }
	//	  return sum;
	//	}

	//	constexpr int x = f();
	public void testPointerToArrayReturnedFromMemberFunction3() throws Exception {
		assertEvaluationEquals(32);
	}

	//	constexpr int f() {
	//		int arr[] = {1, 2, 3};
	//		int (&arrRef)[3] = arr;
	//		return arrRef[2];
	//	}

	//	constexpr int x = f();
	public void testReferenceToArray1() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int f() {
	//		int arr[] = {1, 2, 3};
	//		int (&arrRef)[3] = arr;
	//		arrRef[2] *= 2;
	//		return arr[2];
	//	}

	//	constexpr int x = f();
	public void testReferenceToArray2() throws Exception {
		assertEvaluationEquals(6);
	}

	//	constexpr int f() {
	//		int arr[] = {1, 2, 3};
	//		int (&arrRef)[3] = arr;
	//		for(int& i : arrRef) {
	//			i *= 2;
	//		}
	//		return arr[2];
	//	}

	//	constexpr int x = f();
	public void testReferenceToArray3() throws Exception {
		assertEvaluationEquals(6);
	}

	//	constexpr int f() {
	//		int bar[2][2] { { 3, 5 }, {7, 11 } };
	//		int * bar_ptr { bar[1] };
	//		(*bar_ptr)++;
	//		return *bar_ptr;
	//	}

	//	constexpr int x = f();
	public void testPointerArithmeticsOnMultidimensionalArray() throws Exception {
		assertEvaluationEquals(8);
	}

	//	constexpr void g(int * array) {
	//		array[0] = 1337;
	//	}
	//	constexpr int f() {
	//		int bar[2] { 1, 2 };
	//		g(bar);
	//		return bar[0];
	//	}

	//	constexpr int x = f();
	public void testPassArrayToFunctionAsPointerAndModifyCell() throws Exception {
		assertEvaluationEquals(1337);
	}

	//	constexpr void g(int array[2][2]) {
	//		array[1][0] = 1337;
	//	}
	//	constexpr int f() {
	//		int bar[2][2] { { 3, 5 }, { 7, 11 } };
	//		g(bar);
	//		return bar[1][0];
	//	}

	//	constexpr int x = f();
	public void testPassMultiDimensionalArrayToFunctionAsPointerAndModifyCell() throws Exception {
		assertEvaluationEquals(1337);
	}

	// constexpr int f() {
	//	int foo[] { 1, 2, 3, 4, 5 };
	//	return foo[2];
	// }

	// constexpr int x = f();
	public void testInitializationOfArrays() throws Exception {
		assertEvaluationEquals(3);
	}
}