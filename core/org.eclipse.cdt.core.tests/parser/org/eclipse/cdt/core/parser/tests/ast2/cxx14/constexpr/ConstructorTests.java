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

public class ConstructorTests extends TestBase {
	public static class NonIndexing extends ConstructorTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends ConstructorTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	public ConstructorTests() {
		setStrategy(new NonIndexingTestStrategy());
	}

	public static TestSuite suite() {
		return suite(NonIndexing.class);
	}

	//	struct S {
	//		int x;
	//		constexpr S(int i) : x{i*i} {}
	//	};
	//	constexpr auto f() {
	//		S s(5);
	//		return s.x;
	//	}

	//	constexpr auto x = f();
	public void testConstexprConstructorChainInitializers() throws Exception {
		assertEvaluationEquals(25);
	}

	//	struct S {
	//		int x;
	//		constexpr S(int i) : x{i*i} { x++; }
	//	};
	//	constexpr auto f() {
	//		S s(5);
	//		return s.x;
	//	}

	//	constexpr auto x = f();
	public void testConstexprConstructorConstructorBody() throws Exception {
		assertEvaluationEquals(26);
	}

	//	struct S {
	//		int x;
	//		constexpr S(int i) : x{i*i} { x++; }
	//	};
	//	constexpr auto f() {
	//		S s = S(5);
	//		return s.x;
	//	}

	//	constexpr auto x = f();
	public void testConstexprConstructorCopyConstruction() throws Exception {
		assertEvaluationEquals(26);
	}

	//	struct S {
	//		int x;
	//		constexpr S(int i) : x{i*i} { x++; }
	//	};
	//	constexpr auto f() {
	//		S s = S(5);
	//		return s.x;
	//	}

	//	constexpr auto var = f();
	public void testIdempotence() throws Exception {
		// Querying a value a second time should produce the same result.
		assertEvaluationEquals(26);
		assertEvaluationEquals(26);
	}

	//	struct S {
	//		int x;
	//      constexpr S() : x{5} { x++; x++; }
	//	};
	//	constexpr auto f() {
	//	S s;
	//	return s.x;
	//	}

	//	constexpr auto x = f();
	public void testConstexprConstructorDefaultConstruction() throws Exception {
		assertEvaluationEquals(7);
	}

	//	struct Base {
	//		int base_member;
	//		constexpr Base(int i) : base_member(i) {}
	//	};
	//	struct Derived : Base {
	//		int derived_member;
	//		constexpr Derived(int i) : Base(2), derived_member(i) {}
	//	};
	//	constexpr auto f() {
	//		Derived t(1);
	//		return t.base_member + t.derived_member;
	//	}

	//	constexpr auto x = f();
	public void testConstexprConstructorInheritance() throws Exception {
		assertEvaluationEquals(3);
	}

	// struct point {
	//	int x, y;
	// };
	//
	// constexpr int f() {
	//	point p{2,3};
	//	return p.y;
	// }

	// constexpr int x = f();
	public void testInitializationOfCompositeValues() throws Exception {
		assertEvaluationEquals(3);
	}

	//	struct T {
	//		constexpr T(int i):x{2*i} {}
	//		constexpr int get() const { return x; }
	//		int x;
	//	};
	//	struct S {
	//		T t{2};
	//	};
	//	constexpr int f() {
	//		S s;
	//		return s.t.get();
	//	}

	//	constexpr int x = f();
	public void testNestedConstructorCall() throws Exception {
		assertEvaluationEquals(4);
	}

	//	struct S {
	//		constexpr int get() const {
	//			return x + y;
	//		}
	//	private:
	//		int x = 2;
	//		int y = 4;
	//	};
	//	constexpr int f(S s) {
	//		return s.get();
	//	}

	//	constexpr int x = f(S{});
	public void testImplicitConstructorOfLiteralTypeWithImplicitDestructorIsConstexpr() throws Exception {
		assertEvaluationEquals(6);
	}

	//	struct S {
	//		constexpr int get() const {
	//			return x + y;
	//		}
	//		~S()=default;
	//	private:
	//		int x = 2;
	//		int y = 4;
	//	};
	//	constexpr int f(S s) {
	//		return s.get();
	//	}

	//	constexpr int x = f(S{});
	public void testImplicitConstructorOfLiteralTypeWithDefaultedDestructorIsConstexpr() throws Exception {
		assertEvaluationEquals(6);
	}

	//	struct S {
	//		constexpr int get() const {
	//			return x + y;
	//		}
	//		~S() {}
	//	private:
	//		int x = 2;
	//		int y = 4;
	//	};
	//	constexpr int f(S s) {
	//		return s.get();
	//	}

	//	constexpr int x = f(S{});
	public void testImplicitConstructorOfLiteralTypeWithUserDefinedDestructorIsNotConstexpr() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	struct S {
	//		int x = 2;
	//		int y = 4;
	//	};
	//	constexpr int f(S s) {
	//		return s.x;
	//	}

	//	constexpr int x = f(S{});
	public void testImplicitConstructorOfAggregateTypeIsConstexpr() throws Exception {
		assertEvaluationEquals(2);
	}

	//	struct S {
	//		S() {}
	//		int x = 2;
	//		int y = 4;
	//	};
	//	constexpr int f(S s) {
	//		return s.x;
	//	}

	//	constexpr int x = f(S{});
	public void testUserDefinedDefaultConstructorIsNotConstexpr() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	struct S {
	//		constexpr S(int x):x{x+1} {
	//		}
	//		int x;
	//	};
	//	constexpr int f() {
	//		S s(5);
	//		return s.x;
	//	}

	//	constexpr int x = f();
	public void testCtorCall() throws Exception {
		assertEvaluationEquals(6);
	}

	//	struct S {
	//		constexpr S(int x):x{x} {
	//		}
	//		int x;
	//	};
	//
	//	constexpr int f() {
	//		S s1{5};
	//		S s2{s1.x * 10};
	//		s1.x = 10;
	//		return s2.x;
	//	}

	//	constexpr int x = f();
	public void testArgumentEvaluation() throws Exception {
		assertEvaluationEquals(50);
	}

	// struct B {
	//	int x, y;
	// };
	// struct A {
	//  int m, n, k;
	//  B b;
	// };
	// constexpr int f() {
	//	A a{1, 2, 3, { 4, 5 } };
	//	return a.b.y;
	// }

	// constexpr int x = f();
	public void testInitializationOfNestedCompositeValues() throws Exception {
		assertEvaluationEquals(5);
	}

	// struct point {
	//	int x, y;
	// };
	//
	// constexpr int f() {
	//	point p{2,3};
	//  p.x = p.y * p.y;  // 3 * 3
	//	return p.x;
	// }

	// constexpr int x = f();
	public void testAssignmentOfCompositeValues() throws Exception {
		assertEvaluationEquals(9);
	}

	// struct B {
	//	int x, y;
	// };
	// struct A {
	//  int m, n, k;
	//  B b;
	// };
	//
	// constexpr int f() {
	//	A a{1, 2, 3, { 4, 5 } };
	//	a.b.y = a.k + a.b.x;   // 3 + 4
	//  return a.b.y;
	// }

	// constexpr int x = f();
	public void testAssignmentOfNestedCompositeValues() throws Exception {
		assertEvaluationEquals(7);
	}

	// struct S {
	// 	int x = 1, y = 3;
	// };
	// constexpr auto f() {
	// 	S s;
	// 	s.x++;
	// 	return s.x;
	// }

	// constexpr auto x = f();
	public void testStructDefaultInitialization() throws Exception {
		assertEvaluationEquals(2);
	}

	// struct S {
	// 	int x = 1, y = 3;
	// };
	// constexpr auto f() {
	// 	S s{5, 7};
	// 	s.x++;
	// 	return s.x;
	// }

	// constexpr auto x = f();
	public void testStructDefaultInitializationOverride() throws Exception {
		assertEvaluationEquals(6);
	}

	//	struct T {
	//		int a = 7;
	//	};
	//	struct S {
	//		int x = 1;
	//		T t;
	//	};
	//	constexpr auto f() {
	//		S s;
	//		s.t.a++;
	//		return s.t.a;
	//	}

	//	constexpr auto x = f();
	public void testNestedStructDefaultInitialization() throws Exception {
		assertEvaluationEquals(8);
	}

	//	struct S {
	//		constexpr S(int x, int y):x{x}, y{y*2} {}
	//		constexpr int getY() const {
	//			return y;
	//		}
	//	private:
	//		int x;
	//		int y;
	//	};
	//	constexpr S f() {
	//		return S{3, 5};
	//	}

	//	constexpr int x = f().getY();
	public void testSimpleTypeConstructorExpression2() throws Exception {
		assertEvaluationEquals(10);
	}

	// 	struct S {
	// 		int x, y;
	//	};
	//	constexpr S s{1,5};

	//	constexpr int x = s.y;
	public void testInitialValueOfComposite() throws Exception {
		assertEvaluationEquals(5);
	}

	//	struct Point {
	//		constexpr Point(int x, int y):x{x}, y{y*2} {
	//		}
	//		int x, y;
	//	};
	//	constexpr int f() {
	//		Point p{5, 6};
	//		return p.y;
	//	}

	//	constexpr int x = f();
	public void testCtorInitializerList() throws Exception {
		assertEvaluationEquals(12);
	}

	//	struct Point {
	//		constexpr Point(int x, int y):x{x}, y{y*2} {
	//		}
	//		int x, y;
	//	};
	//	constexpr int f() {
	//		Point p(5, 6);
	//		return p.y;
	//	}

	//	constexpr int x = f();
	public void testCtorConstructorInitializer() throws Exception {
		assertEvaluationEquals(12);
	}

	//	struct Point {
	//		constexpr Point(int x, int y):x{x}, y{y*2} {
	//		}
	//		int x, y;
	//	};
	//	constexpr int f() {
	//		Point p = {5, 6};
	//		return p.y;
	//	}

	//	constexpr int x = f();
	public void testCtorEqualsInitializer() throws Exception {
		assertEvaluationEquals(12);
	}

	//	struct S {
	//		constexpr S(int x):x{x*2} {
	//		}
	//		int x;
	//	};
	//	constexpr int f() {
	//		S s = 6;
	//		return s.x;
	//	}

	//	constexpr int x = f();
	public void testCtorImplicitConversion() throws Exception {
		assertEvaluationEquals(12);
	}

	//	struct Point {
	//		constexpr Point(int x, int y):x{x}, y{y*2} {
	//		}
	//		int x, y;
	//	};
	//	constexpr int f() {
	//		Point p1{5, 6};
	//		Point p2 = p1;
	//		return p2.y;
	//	}

	//	constexpr int x = f();
	public void testCtorLvalueCopyConstruction() throws Exception {
		assertEvaluationEquals(12);
	}

	//	struct Point {
	//		constexpr Point(int x, int y):x{x}, y{y*2} {
	//		}
	//		int x, y;
	//	};
	//	constexpr int f() {
	//		Point p = Point{5, 6};
	//		return p.y;
	//	}

	//	constexpr int x = f();
	public void testCtorRvalueCopyConstruction() throws Exception {
		assertEvaluationEquals(12);
	}

	//	struct T {
	//		int y = 7, z = 11;
	//	};
	//	struct S {
	//		int x = 1;
	//		T t{8, 12};
	//	};
	//	constexpr auto f() {
	//		S s;
	//		s.t.y++;
	//      s.t.z++;
	//		return s.t.y + s.t.z;
	//	}

	//	constexpr auto x = f();
	public void testNestedStructDefaultInitializationOverride() throws Exception {
		assertEvaluationEquals(22);
	}

	// struct T {
	//	int member;
	//	constexpr T(int i) : member(i) {}
	// };

	// constexpr auto x = T(2).member;
	public void testFundamentalTypeDirectInitializationWithParenthesis() throws Exception {
		assertEvaluationEquals(2);
	}

	//	struct Base {
	//		int x = 5;
	//	};
	//	struct Derived : Base {
	//		int y = 10;
	//	};
	//	constexpr int f() {
	//		Derived d{};
	//		return d.x;
	//	}

	//	constexpr int x = f();
	public void testInheritedMemberVariable1() throws Exception {
		assertEvaluationEquals(5);
	}

	//	struct X {
	//		constexpr X(int y):y{2*y} {}
	//		int y;
	//	};
	//	struct Base {
	//		const X x{5};
	//	};
	//	struct Derived : Base {
	//		int n = 2 * x.y;
	//	};
	//	constexpr int f() {
	//		Derived d{};
	//		return d.n;
	//	}

	//	constexpr int x = f();
	public void testInheritedMemberVariable2() throws Exception {
		assertEvaluationEquals(20);
	}

	//	struct S {
	//		constexpr S(int x):x{x} {
	//		}
	//		int x;
	//		int y{2 * x};
	//	};
	//	constexpr int f() {
	//		S s{5};
	//		return s.y;
	//	}

	//	constexpr int x = f();
	public void testOrderOfFieldInitialization() throws Exception {
		assertEvaluationEquals(10);
	}

	//	struct S {
	//		int value = 42;
	//	};
	//	constexpr S waldo{23};

	//	constexpr int x = waldo.value;
	public void testDirectInitializedVariable_510151() throws Exception {
		assertEvaluationEquals(23);
	}

	//	struct S {
	//		int value = 42;
	//	};
	//	constexpr S waldo{};

	//	constexpr int x = waldo.value;
	public void testDirectDefaultInitializedVariable_510151() throws Exception {
		assertEvaluationEquals(42);
	}

	//	struct S {
	//		int value = 42;
	//	};
	//	constexpr S waldo;

	//	constexpr int x = waldo.value;
	public void testDefaultInitializedVariable_510151() throws Exception {
		assertEvaluationEquals(42);
	}
}