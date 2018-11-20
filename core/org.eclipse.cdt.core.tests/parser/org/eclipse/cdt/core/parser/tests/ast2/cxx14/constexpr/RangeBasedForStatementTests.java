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

public class RangeBasedForStatementTests extends TestBase {
	public static class NonIndexing extends RangeBasedForStatementTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends RangeBasedForStatementTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int sum { 0 };
	//		for (auto i : bar) {
	//			sum += i;
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testSimpleRangeBasedForLoop() throws Exception {
		assertEvaluationEquals(26);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int sum { 0 };
	//		for (auto i : bar) {
	//			return 42;
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testReturnInRangeBasedForLoop() throws Exception {
		assertEvaluationEquals(42);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int sum { 0 };
	//		for (auto& i : bar) {
	//			i++;
	//		}
	//		for (auto i : bar) {
	//			sum += i;
	//		}
	//      return sum;
	//	}

	//	constexpr int x = f();
	public void testRangeBasedForLoopReferences() throws Exception {
		assertEvaluationEquals(30);
	}

	//	constexpr void incr(int & i) {
	//		i++;
	//	}
	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int sum { 0 };
	//		for (auto& i : bar) {
	//			incr(i);
	//		}
	//		for (auto i : bar) {
	//			sum += i;
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testPassReferenceObtainedFromRangeBasedForLoopToFunctionAndModify() throws Exception {
		assertEvaluationEquals(30);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int sum { 0 };
	//		for (auto i : bar)
	//			sum += i;
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testRangeBasedForLoopWithNonCompoundBodyStatement() throws Exception {
		assertEvaluationEquals(26);
	}

	//	constexpr int f() {
	//		int bar[4] { 3, 5, 7, 11 };
	//		int sum { 0 };
	//		for (auto i : bar)
	//			return 42;
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testRangeBasedForLoopWithReturnInNonCompoundBodyStatement() throws Exception {
		assertEvaluationEquals(42);
	}

	//	class Range {
	//		int arr[5];
	//	public:
	//		constexpr Range():arr{1,2,3,4,5} {}
	//		constexpr const int* begin() const { return arr; }
	//		constexpr const int* end() const { return arr + 5; }
	//	};
	//	constexpr int f() {
	//		Range range{};
	//		int sum{0};
	//		for(int x : range) {
	//			sum += x;
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testRangeBasedForLoopOverCustomType() throws Exception {
		assertEvaluationEquals(15);
	}

	// 	class Range {
	//		int arr[5];
	//	public:
	//		constexpr Range():arr{1,2,3,4,5} {}
	//		constexpr int* begin() {
	//			return arr;
	//		}
	//		constexpr int* end() {
	//			return arr + 5;
	//		}
	//	};
	//	constexpr int f() {
	//	  Range range{};
	//	  int sum{0};
	//	  for(int &x : range) {
	//	    x++;
	//	  }
	//	  for(int const& x : range) {
	//	    sum += x;
	//	  }
	//	  return sum;
	//	}

	//	constexpr int x = f();
	public void testRangeBasedForLoopThatModifiesElementsInCustomType() throws Exception {
		assertEvaluationEquals(20);
	}

	//	class Range {
	//	  int arr1[5];
	//	  int arr2[5];
	//	public:
	//	  constexpr Range():arr1{1,2,3,4,5}, arr2{6,7,8,9,10} {}
	//	  constexpr int* begin() { return arr1; }
	//	  constexpr int* end() { return arr1 + 5; }
	//	  constexpr const int* begin() const { return arr2; }
	//	  constexpr const int* end() const { return arr2 + 5; }
	//	};
	//	constexpr int f() {
	//	  Range range{};
	//	  int sum{0};
	//	  for(int x : range) {
	//	    sum += x;
	//	  }
	//	  return sum;
	//	}

	//	constexpr int x = f();
	public void testRangeBasedForLoopOverNonConstRangeChoosesNonConstBeginEnd() throws Exception {
		assertEvaluationEquals(15);
	}

	//	class Range {
	//	  int arr1[5];
	//	  int arr2[5];
	//	public:
	//	  constexpr Range():arr1{1,2,3,4,5}, arr2{6,7,8,9,10} {}
	//	  constexpr int* begin() { return arr1; }
	//	  constexpr int* end() { return arr1 + 5; }
	//	  constexpr const int* begin() const { return arr2; }
	//	  constexpr const int* end() const { return arr2 + 5; }
	//	};
	//	constexpr int f() {
	//	  const Range range{};
	//	  int sum{0};
	//	  for(int x : range) {
	//	    sum += x;
	//	  }
	//	  return sum;
	//	}

	//	constexpr int x = f();
	public void testRangeBasedForLoopOverConstRangeChoosesConstBeginEnd() throws Exception {
		assertEvaluationEquals(40);
	}

	//	class Range {
	//	  int arr1[5];
	//	  int arr2[5];
	//	public:
	//	  constexpr Range():arr1{1,2,3,4,5}, arr2{6,7,8,9,10} {}
	//	  constexpr int* begin() { return arr1; }
	//	  constexpr int* end() { return arr1 + 5; }
	//	  constexpr const int* begin() const { return arr2; }
	//	  constexpr const int* end() const { return arr2 + 5; }
	//	};
	//	constexpr int f() {
	//	  Range range{};
	//	  Range const& rangeRef = range;
	//	  int sum{0};
	//	  for(int x : rangeRef) {
	//	    sum += x;
	//	  }
	//	  return sum;
	//	}

	//	constexpr int x = f();
	public void testRangeBasedForLoopOverConstRefRangeChoosesConstBeginEnd() throws Exception {
		assertEvaluationEquals(40);
	}

	//	class Range {
	//		int arr[5];
	//	public:
	//		constexpr Range():arr{1,2,3,4,5} {}
	//		constexpr const int* begin(int i) const { return arr + i; }
	//		constexpr const int* end() const { return arr + 5; }
	//	};
	//	constexpr int f() {
	//		Range range{};
	//		int sum{0};
	//		for(int x : range) {
	//			sum += x;
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testRangeBasedForLoopOverCustomTypeWithInvalidBeginMemberFunction() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	class Range {
	//		int arr[5];
	//	public:
	//		constexpr Range():arr{1,2,3,4,5} {}
	//		constexpr const int* begin(int i = 0) const { return arr + i; }
	//		constexpr const int* end() const { return arr + 5; }
	//	};
	//	constexpr int f() {
	//		Range range{};
	//		int sum{0};
	//		for(int x : range) {
	//			sum += x;
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testRangeBasedForLoopOverCustomTypeWithBeginMemberFunctionWithDefaultParameterValue() throws Exception {
		assertEvaluationEquals(15);
	}

	//	namespace ns {
	//	  class Vec {
	//	  public:
	//	    int arr1[5];
	//	    constexpr Vec():arr1{1,2,3,4,5} {}
	//	  };
	//	  constexpr int const* begin(Vec const& v) { return v.arr1; }
	//	  constexpr int const* end(Vec const& v) { return v.arr1 + 5; }
	//	}
	//
	//
	//	constexpr int f() {
	//	  ns::Vec v{};
	//	  int sum{0};
	//	  for(int x : v) {
	//	    sum += x;
	//	  }
	//	  return sum;
	//	}

	//	constexpr int x = f();
	public void testDoesArgumentDependentLookupIfBeginEndMemberFunctionsDontExist() throws Exception {
		assertEvaluationEquals(15);
	}

	//	namespace ns {
	//	  class Vec {
	//	  public:
	//	    int arr1[5];
	//	    int arr2[5];
	//	    constexpr Vec():arr1{1,2,3,4,5}, arr2{6,7,8,9,10} {}
	//	    constexpr int const* begin() const { return arr2; }
	//	    constexpr int const* end() const { return arr2 + 5; }
	//	  };
	//	  constexpr int const* begin(Vec const& v) { return v.arr1; }
	//	  constexpr int const* end(Vec const& v) { return v.arr1 + 5; }
	//	}
	//
	//
	//	constexpr int f() {
	//	  ns::Vec v{};
	//	  int sum{0};
	//	  for(int x : v) {
	//	    sum += x;
	//	  }
	//	  return sum;
	//	}

	//	constexpr int x = f();
	public void testChoosesMemberFunctionsOverFreeFunctions() throws Exception {
		assertEvaluationEquals(40);
	}

	//	namespace ns {
	//	  class Vec {
	//	  public:
	//	    int arr1[5];
	//	    constexpr Vec():arr1{1,2,3,4,5} {}
	//	    constexpr int const* begin() const { return arr1; }
	//	  };
	//	  constexpr int const* end(Vec const& v) { return v.arr1 + 5; }
	//	}
	//
	//
	//	constexpr int f() {
	//	  ns::Vec v{};
	//	  int sum{0};
	//	  for(int x : v) {
	//	    sum += x;
	//	  }
	//	  return sum;
	//	}

	//	constexpr int x = f();
	public void testDoesntMixMemberFunctionsAndFreeFunctions() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	namespace ns {
	//	    class Vec {
	//	      int arr[5];
	//	    public:
	//	      constexpr Vec():arr{1,2,3,4,5} {}
	//	      constexpr int const* start() const { return arr; }
	//	      constexpr int size() const { return 5; }
	//	    };
	//	    template<typename T>
	//	    constexpr int const* begin(T const& t) { return t.start(); }
	//	    template<typename T>
	//	    constexpr int const* end(T const& t) { return t.start() + t.size(); }
	//	}
	//
	//	constexpr int f() {
	//	  ns::Vec v{};
	//	  int sum{0};
	//	  for(int x : v) {
	//	    sum += x;
	//	  }
	//	  return sum;
	//	}

	//	constexpr int x = f();
	public void testWorksWithBeginEndTemplates() throws Exception {
		assertEvaluationEquals(15);
	}

	//	constexpr int f() {
	//		int sum = 0;
	//		for(auto x : {1,2,3,4,5}) {
	//			sum += x;
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testRangeBasedForLoopOverInitializerList() throws Exception {
		assertEvaluationEquals(15);
	}
}