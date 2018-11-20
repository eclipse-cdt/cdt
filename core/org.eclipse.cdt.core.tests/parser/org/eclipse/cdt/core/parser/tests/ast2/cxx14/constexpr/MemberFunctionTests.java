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

public class MemberFunctionTests extends TestBase {
	public static class NonIndexing extends MemberFunctionTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends MemberFunctionTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	struct S {
	//		int x, y;
	//		constexpr int member() {
	//			return 4;
	//		}
	//	};
	//	constexpr int f() {
	//		S s{3,7};
	//		return s.member();
	//	}

	//	constexpr auto x = f();
	public void testMemberFunctionCall() throws Exception {
		assertEvaluationEquals(4);
	}

	//	struct S {
	//		int x, y;
	//		constexpr int member() {
	//			y++;
	//			return y;
	//		}
	//	};
	//	constexpr int f() {
	//		S s{3,7};
	//		return s.member();
	//	}

	//	constexpr auto x = f();
	public void testMemberFunctionWithImplicitThis() throws Exception {
		assertEvaluationEquals(8);
	}

	//	class Point {
	//		int x, y;
	//	public:
	//		constexpr Point(int x, int y):x{x}, y{y} {}
	//		constexpr int getY() const;
	//	};
	//	constexpr int Point::getY() const { return y; }
	//
	//	constexpr int f() {
	//	  Point p{4,5};
	//	  return p.getY();
	//	}

	//	constexpr int x = f();
	public void testExternallyDefinedMemberFunction() throws Exception {
		assertEvaluationEquals(5);
	}

	//	class S {
	//		int x;
	//	public:
	//		constexpr S(int x):x{x} {}
	//		constexpr void inc() { x += 30; }
	//		constexpr int get() const { return x; }
	//	};
	//
	//	constexpr int f() {
	//	  S s{20};
	//	  s.inc();
	//	  return s.get();
	//	}

	//	constexpr int x = f();
	public void testPlusEqualsWithinMemberFunction() throws Exception {
		assertEvaluationEquals(50);
	}

	//	class Point {
	//		int x, y;
	//	public:
	//		constexpr Point(int x, int y):x{x}, y{y} {}
	//		constexpr int getY() const { return this->y; }
	//	};
	//	constexpr int f() {
	//		Point p{10,40};
	//		return p.getY();
	//	}

	//	constexpr int x = f();
	public void testMemberAccessThroughThisPointer() throws Exception {
		assertEvaluationEquals(40);
	}

	//	struct S {
	//		int x, y;
	//		constexpr int member() {
	//			y = member2();
	//			return y;
	//		}
	//		constexpr int member2() {
	//			y++;
	//			return y;
	//		}
	//	};
	//	constexpr int f() {
	//		S s{3,7};
	//		return s.member();
	//	}

	//	constexpr auto x = f();
	public void testNestedMemberFunctionCallsWithImplicitThis() throws Exception {
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
	//
	//	constexpr S s{3, 5};
	//	constexpr int f() { return s.getY(); }

	//	constexpr int x = f();
	public void testGlobalMemberFunctionCallFromConstexprFunction() throws Exception {
		assertEvaluationEquals(10);
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
	//	constexpr S s{3, 5};

	//	constexpr int x = s.getY();
	public void testGlobalMemberFunctionCallFromGlobalConstexpr() throws Exception {
		assertEvaluationEquals(10);
	}

	//	struct S {
	//		constexpr S(int x, int y):x{x}, y{y} {}
	//		constexpr int add(S const& other) const {
	//			return y + other.x * 2;
	//		}
	//	private:
	//		int x, y;
	//	};
	//
	//	constexpr int f() {
	//		S s1{2, 5};
	//		S s2{5, 4};
	//      return s1.add(s2);
	//	}

	//	constexpr int x = f();
	public void testManualAddMemberFunction() throws Exception {
		assertEvaluationEquals(15);
	}
}
