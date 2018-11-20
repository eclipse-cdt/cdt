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

public class BinaryOperatorOverloadingTests extends TestBase {
	public static class NonIndexing extends BinaryOperatorOverloadingTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends BinaryOperatorOverloadingTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	struct S {
	//		constexpr S(int x):x{x} {}
	//		constexpr int operator+(S const& other) {
	//			return 12;
	//		}
	//	private:
	//		int x;
	//	};
	//
	//	constexpr int f() {
	//		S s1{2};
	//		S s2{3};
	//		int x = s1 + s2;
	//		return x;
	//	}

	//	constexpr int x = f();
	public void testOverloadedPlusOperatorAsMemberFunction() throws Exception {
		assertEvaluationEquals(12);
	}

	//	struct S {
	//		constexpr S(int x):x{x} {}
	//		constexpr S operator*(S const& other) {
	//			return S{x * other.x * 2};
	//		}
	//		int x;
	//	};
	//
	//	constexpr int f() {
	//		S s1{2};
	//		S s2{3};
	//		S s3 = s1 * s2;
	//		return s3.x;
	//	}

	//	constexpr int x = f();
	public void testOverloadedMultiplicationOperatorAsMemberFunction() throws Exception {
		assertEvaluationEquals(12);
	}

	//	struct S {
	//		constexpr S(int x, int y):x{x}, y{y} {}
	//		int x, y;
	//	};
	//	constexpr S operator+(S const& s1, S const& s2) {
	//		return S{s1.x + s2.x + 2, s1.y + s2.y + 4};
	//	}
	//
	//	constexpr int f() {
	//		S s1{2, 4};
	//		S s2{3, 6};
	//		S s3 = s1 + s2;
	//		return s3.y;
	//	}

	//	constexpr int x = f();
	public void testOverloadedPlusOperatorAsNonMemberFunction() throws Exception {
		assertEvaluationEquals(14);
	}

	//	struct S {
	//		constexpr S(int x, int y):x{x*2}, y{y+1} {
	//		}
	//		constexpr S operator+(S const& other) {
	//			S result{x + other.x, y + other.y*2};
	//			return result;
	//		}
	//		int x, y;
	//	};
	//  constexpr int f() {
	//		S s1{2,4};
	//		S s2{4,8};
	//		S result{s1 + s2};
	//		return result.y;
	//	}

	//  constexpr int x = f();
	public void testOverloadedOperatorPlusComplex1() throws Exception {
		assertEvaluationEquals(24);
	}

	//	struct S {
	//		constexpr S(int x, int y):x{x*2}, y{y+1} {
	//		}
	//		constexpr S operator+(S const& other) {
	//			S result{x + other.x, y + other.y*2};
	//			return result;
	//		}
	//		int x, y;
	//	};
	//  constexpr int f() {
	//		S s1{2,4};
	//		S s2{4,8};
	//		S result = s1 + s2;
	//		return result.x;
	//	}

	//  constexpr int x = f();
	public void testOverloadedOperatorPlusComplex2() throws Exception {
		assertEvaluationEquals(24);
	}

	//	struct S {
	//		constexpr S(int x, int y):x{x*2}, y{y+1} {
	//		}
	//		constexpr S operator+(S const& other) {
	//			return S{x + other.x, y + other.y*2};
	//		}
	//		int x, y;
	//	};
	//  constexpr int f() {
	//		S s1{2,4};
	//		S s2{4,8};
	//		S result{s1 + s2};
	//		return result.y;
	//	}

	//  constexpr int x = f();
	public void testOverloadedOperatorPlusComplex3() throws Exception {
		assertEvaluationEquals(24);
	}

	//	class Point {
	//		int x, y;
	//	public:
	//		constexpr Point(int x, int y):x{x}, y{y} {}
	//		constexpr bool operator==(Point const& other) const {
	//			return x == other.x && y == other.y;
	//		}
	//	};
	//	constexpr int f() {
	//		Point p1{2,4};
	//		Point p2{2,4};
	//		return p1 == p2 ? 20 : 40;
	//	}

	//	constexpr int x = f();
	public void testOverloadedOperatorEquals() throws Exception {
		assertEvaluationEquals(20);
	}

	//	class Point {
	//		int x, y;
	//	public:
	//		constexpr Point(int x, int y):x{x}, y{y} {}
	//		constexpr Point& operator=(Point const& other) {
	//			x = 2 * other.x;
	//			y = 2 * other.y;
	//			return *this;
	//		}
	//		constexpr int getY() const { return y; }
	//	};
	//	constexpr int f() {
	//		Point p1{0, 0};
	//		Point p2{2,5};
	//		p1 = p2;
	//		return p1.getY();
	//	}

	//	constexpr int x = f();
	public void testOverloadedOperatorAssign() throws Exception {
		assertEvaluationEquals(10);
	}
}
