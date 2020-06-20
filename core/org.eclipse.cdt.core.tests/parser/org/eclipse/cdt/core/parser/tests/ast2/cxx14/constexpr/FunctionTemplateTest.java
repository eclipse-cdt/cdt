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

public class FunctionTemplateTests extends TestBase {
	public static class NonIndexing extends FunctionTemplateTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends FunctionTemplateTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	template<typename T>
	//	constexpr T add(T a, T b) {
	//		return a + b;
	//	}

	//	constexpr auto x = add(5.5, 6.3);
	public void testImplicitTemplateInstantiation() throws Exception {
		assertEvaluationEquals(11.8);
	}

	//	class Integer {
	//	  int i;
	//	public:
	//	  constexpr Integer(int i):i{i} {}
	//	  constexpr int get() const { return i; }
	//	};
	//	template<typename T>
	//	constexpr int f() {
	//	  T t{10};
	//	  return t.get();
	//	}

	//	constexpr int x = f<Integer>();
	public void testExplicitTemplateInstantiation() throws Exception {
		assertEvaluationEquals(10);
	}

	//	template<int I>
	//	constexpr int f() {
	//	  int result = I * 4;
	//	  return result;
	//	}

	//	constexpr int x = f<5>();
	public void testTemplateWithNonTypeTemplateParameter() throws Exception {
		assertEvaluationEquals(20);
	}

	//	template<typename T>
	//	constexpr T sum(T v) {
	//	  return v;
	//	}
	//	template<typename T, typename... Args>
	//	constexpr T sum(T first, Args... args) {
	//	  return first + sum(args...);
	//	}

	//	constexpr int x = sum(1,2,3,4,5);
	public void testVariadicTemplate() throws Exception {
		assertEvaluationEquals(15);
	}

	//	template<typename... Args>
	//	constexpr int count(Args... args) {
	//	  return sizeof...(args);
	//	}

	//	constexpr int x = count(1,2,3,4,5);
	public void testParameterPackSizeof() throws Exception {
		assertEvaluationEquals(5);
	}

	//	class Integer {
	//		int i;
	//	public:
	//		constexpr Integer(int i):i{i} {}
	//		constexpr int get() const { return i; }
	//		constexpr bool operator<=(Integer const& rhs) const { return i <= rhs.i; }
	//		constexpr Integer& operator++() { ++i; return *this; }
	//		constexpr Integer& operator*=(Integer const& rhs) { i *= rhs.i; return *this; }
	//	};
	//
	//	template<typename T>
	//	constexpr int fac(T n) {
	//		T total{1};
	//		for(T i{1}; i <= n; ++i) {
	//			total *= i;
	//		}
	//		return total.get();
	//	}

	//	constexpr int x = fac(Integer{5});
	public void testTemplateInstantiationOfForLoop() throws Exception {
		assertEvaluationEquals(120);
	}

	//	class Integer {
	//		int i;
	//	public:
	//		constexpr Integer(int i):i{i} {}
	//		constexpr int get() const { return i; }
	//		constexpr bool operator<=(Integer const& rhs) const { return i <= rhs.i; }
	//		constexpr Integer& operator++() { ++i; return *this; }
	//		constexpr Integer& operator+=(Integer const& rhs) { i += rhs.i; return *this; }
	//	};
	//	template<typename T>
	//	constexpr int f(T n) {
	//		T sum { 0 };
	//		T i { 0 };
	//		do {
	//			sum += i;
	//			++i;
	//		} while (i <= n);
	//		return sum.get();
	//	}

	//	constexpr int x = f(Integer{10});
	public void testTemplateInstantiationOfDoWhileLoop() throws Exception {
		assertEvaluationEquals(55);
	}

	//	template<typename T>
	//	constexpr T add(T a, T b) {
	//		;
	//		return a + b;
	//	}

	//	constexpr auto x = add(5.5, 6.3);
	public void testNullStatementInFunctionTemplate() throws Exception {
		assertEvaluationEquals(11.8);
	}

	//	class Integer {
	//		int i;
	//	public:
	//		constexpr Integer(int i):i{i} {}
	//		constexpr int get() const { return i; }
	//		constexpr bool operator<=(Integer const& rhs) const { return i <= rhs.i; }
	//		constexpr Integer& operator++() { ++i; return *this; }
	//		constexpr Integer& operator*=(Integer const& rhs) { i *= rhs.i; return *this; }
	//	};
	//
	//	template<typename T>
	//	constexpr int fac(T n) {
	//		T total{1};
	//		T i{1};
	//		while(i <= n) {
	//			total *= i;
	//			++i;
	//		}
	//		return total.get();
	//	}

	//	constexpr int x = fac(Integer{5});
	public void testTemplateInstantiationOfWhileLoop() throws Exception {
		assertEvaluationEquals(120);
	}

	//	template<typename T>
	//	constexpr T div(T a, T b) {
	//		if(b > 0) {
	//			return a / b;
	//		}
	//		return -1;
	//	}

	//	constexpr auto x = div(11.5, 2.0);
	public void testTemplateInstantiationOfIfStatement() throws Exception {
		assertEvaluationEquals(5.75);
	}

	//	constexpr int count(int first) { return 1; }
	//	constexpr int count(double first) { return 4; }
	//	template<typename T, typename... Args>
	//	constexpr int count(T first, Args... args) {
	//		return count(first) + count(args...);
	//	}

	//	constexpr int x = count(1, 0.5, 3.4, 5, 2.2);
	public void testVariadicTemplateWithVaryingTypes() throws Exception {
		assertEvaluationEquals(14);
	}

	//	template<typename... Args>
	//	constexpr int sum(Args... args) {
	//		int sum = 0;
	//		for(auto x : {args...}) {
	//			sum += x;
	//		}
	//		return sum;
	//	}

	//	constexpr long long x = sum(1,2,3,4,5);
	public void testExpansionOfVariadicTemplateParameterIntoInitializerList() throws Exception {
		assertEvaluationEquals(15);
	}

	//	template<typename... Args>
	//	constexpr int sum(Args... args) {
	//		int sum = 0;
	//		for(auto x : {(args*2)...}) {
	//			sum += x;
	//		}
	//		return sum;
	//	}

	//	constexpr long long x = sum(1,2,3,4,5);
	public void testExpressionInVariadicTemplateParameterExpansion1() throws Exception {
		assertEvaluationEquals(30);
	}

	//	template<typename... Indices>
	//	constexpr int sumOfPrimes(Indices... indices) {
	//		// all prime numbers below 100
	//		int primes[] = {2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97};
	//		int sum = 0;
	//		for(int prime : {primes[indices]...}) {
	//			sum += prime;
	//		}
	//		return sum;
	//	}

	//	constexpr int x = sumOfPrimes(0, 4, 9, 11, 19);
	public void testExpressionInVariadicTemplateParameterExpansion2() throws Exception {
		assertEvaluationEquals(150);
	}

	//	template<unsigned... Ints>
	//	class index_sequence{};
	//
	//	template<int... indices>
	//	constexpr int sumOfPrimes(index_sequence<indices...>) {
	//		// all prime numbers below 100
	//		int primes[] = {2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97};
	//		int sum = 0;
	//		for(int prime : {primes[indices]...}) {
	//			sum += prime;
	//		}
	//		return sum;
	//	}

	//	constexpr int x = sumOfPrimes(index_sequence<0, 4, 9, 11, 19>{});
	public void testIndexSequence1() throws Exception {
		assertEvaluationEquals(150);
	}

	//	template<typename T, int size>
	//	constexpr int getArrayLength(T(&)[size]){
	//		return size;
	//	}
	//	constexpr int f() {
	//		int arr[10]{};
	//		return getArrayLength(arr);
	//	}

	//	constexpr int x = f();
	public void testFunctionTemplateWithArrayParameter1() throws Exception {
		assertEvaluationEquals(10);
	}

	//	template<typename T, int size>
	//	constexpr void doubleArrayContents(T(&arr)[size]) {
	//		for(int i = 0; i < size; i++) {
	//			arr[i] *= 2;
	//		}
	//	}
	//	constexpr int f() {
	//		int arr[]{1,2,3,4,5};
	//		doubleArrayContents(arr);
	//		return arr[3];
	//	}

	//	constexpr int x = f();
	public void testFunctionTemplateWithArrayParameter2() throws Exception {
		assertEvaluationEquals(8);
	}

	//	struct S {
	//		constexpr S(int n):x{n*2} {}
	//		constexpr int get() { return x; }
	//	private:
	//		int x;
	//	};
	//	template<int N>
	//	constexpr int f() {
	//		S s{N};
	//		return s.get();
	//	}

	//	constexpr int x = f<10>();
	public void testInstantiationOfConstructorInFunctionTemplate1() throws Exception {
		assertEvaluationEquals(20);
	}

	//	struct Number {
	//		constexpr Number(int):isFP{false} {}
	//		constexpr Number(double):isFP{true} {}
	//		constexpr bool isFloatingPoint() { return isFP; }
	//	private:
	//		bool isFP;
	//	};
	//	template<typename T>
	//	constexpr bool f() {
	//		Number n{T{}};
	//		return n.isFloatingPoint();
	//	}
	//	constexpr bool x = f<double>();
	public void testInstantiationOfConstructorInFunctionTemplate2() throws Exception {
		assertEvaluationEquals(true);
	}

	//	template<int A, int B>
	//	struct Adder {
	//		constexpr int sum() {
	//			return A + B;
	//		}
	//	};
	//	template<int A, int B>
	//	constexpr int f() {
	//		switch(Adder<A, B>{}.sum()) {
	//			case 10:
	//				return 1;
	//			case 11:
	//				return 2;
	//			case 12:
	//				return 3;
	//			default:
	//				return 4;
	//		}
	//	}

	//	constexpr int x = f<9,2>();
	public void testInstantiationOfSwitchStatement() throws Exception {
		assertEvaluationEquals(2);
	}

	//	template<typename T>
	//	constexpr int f() {
	//		typedef T myType;
	//		myType x = 5;
	//		x *= 5;
	//		return x;
	//	}

	//	constexpr int x = f<int>();
	public void testInstantiationOfTypedefDeclaration() throws Exception {
		assertEvaluationEquals(25);
	}

	//	template<typename T>
	//	constexpr int f() {
	//		using myint = T;
	//		myint x = 5;
	//		x *= 5;
	//		return x;
	//	}

	//	constexpr int x = f<int>();
	public void testInstantiationOfAliasDeclaration() throws Exception {
		assertEvaluationEquals(25);
	}
}