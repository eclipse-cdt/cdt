/*******************************************************************************
* Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx14.constexpr;

import junit.framework.TestSuite;

public class StructuredBindingTests extends TestBase {
	public static class NonIndexing extends StructuredBindingTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends StructuredBindingTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	constexpr int f() {
	//		int arr[]{8, 9};
	//		auto [first, second] = arr;
	//		return first;
	//	}

	//	constexpr int x = f();
	public void testBindingFirstElementOfArray() throws Exception {
		assertEvaluationEquals(8);
	}

	//	constexpr int f() {
	//		int arr[]{8, 9};
	//		auto [first, second] = arr;
	//		return second;
	//	}

	//	constexpr int x = f();
	public void testBindingSecondElementOfArray() throws Exception {
		assertEvaluationEquals(9);
	}

	//	constexpr int f() {
	//		int arr[]{8, 9};
	//		auto [first, second, third] = arr;
	//		return third;
	//	}

	//	constexpr int x = f();
	public void testBindingOutOfBoundElementOfArray() throws Exception {
		assertEvaluationIsError();
	}

	//	struct Pair {
	//		int i;
	//		double d;
	//	} p{42, 5.0};
	//	constexpr auto f() {
	//		auto [first, second] = p;
	//		return first;
	//	}

	//	constexpr auto x = f();
	public void testBindingFirstMemberOfObject() throws Exception {
		assertEvaluationEquals(42);
	}

	//	struct Pair {
	//		int i;
	//		double d;
	//	} p{42, 5.0};
	//	constexpr auto f() {
	//		auto [first, second] = p;
	//		return second;
	//	}

	//	constexpr auto x = f();
	public void testBindingSecondMemberOfObject() throws Exception {
		assertEvaluationEquals(5.0);
	}

	//	struct Base {
	//		int i;
	//	};
	//	struct Sub : Base {
	//	} s{5};
	//	auto [inherited] = s;

	//	auto x = inherited;
	public void testBindingInheritedMember() throws Exception {
		assertEvaluationEquals(5);
	}

	//	struct Mono {
	//		int i;
	//	} p{42};
	//	constexpr auto f() {
	//		auto [first, second] = p;
	//		return second;
	//	}

	//	constexpr auto x = f();
	public void testBindingOutOfBoundElementOfObject() throws Exception {
		assertEvaluationIsUnkown();
	}

	//	namespace std {
	//	using size_t = unsigned long long;
	//	template <typename T, size_t N>
	//	struct array {
	//		T elements[N];
	//		template <size_t I>
	//		constexpr auto get() {
	//			return elements[I];
	//		}
	//	};
	//	template <typename T>
	//	struct tuple_size;
	//	template <typename T, size_t N>
	//	struct tuple_size<array<T, N>> {
	//		constexpr static size_t value = N;
	//	};
	//	template <size_t I, typename T>
	//	struct tuple_element;
	//	template <size_t I, typename T, size_t N>
	//	struct tuple_element<I, array<T, N>> {
	//		using type = T;
	//	};
	//	}
	//	constexpr auto createValues() {
	//		std::array<int, 3> values{{1, 2, 3}};
	//		return values;
	//	}
	//	constexpr auto foo() {
	//		auto [f, s, t] = createValues();
	//		return t;
	//	}

	//	constexpr auto x = foo();
	public void testBindingOutOfTupleLikeObjectWithMemberGet() throws Exception {
		assertEvaluationEquals(3);
	}

	//	namespace std {
	//	using size_t = unsigned long long;
	//	template <typename T, size_t N>
	//	struct array {
	//		T elements[N];
	//	};
	//	template <typename T>
	//	struct tuple_size;
	//	template <typename T, size_t N>
	//	struct tuple_size<array<T, N>> {
	//		constexpr static size_t value = N;
	//	};
	//	template <size_t I, typename T>
	//	struct tuple_element;
	//	template <size_t I, typename T, size_t N>
	//	struct tuple_element<I, array<T, N>> {
	//		using type = T;
	//	};
	//	template <size_t I, typename T, size_t N>
	//	constexpr auto get(std::array<T, N> const & values) {
	//		return values.elements[I];
	//	}
	//	}
	//	constexpr auto createValues() {
	//		std::array<int, 3> values{{1, 2, 3}};
	//		return values;
	//	}
	//	constexpr auto foo() {
	//		auto [f, s, t] = createValues();
	//		return t;
	//		//return get<2>(createValues());
	//	}

	//	constexpr auto x = foo();
	public void testBindingOutOfTupleLikeObjectWithFreeGet() throws Exception {
		assertEvaluationEquals(3);
	}
}