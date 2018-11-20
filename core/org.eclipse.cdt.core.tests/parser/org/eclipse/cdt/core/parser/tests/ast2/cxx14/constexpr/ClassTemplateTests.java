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

public class ClassTemplateTests extends TestBase {
	public static class NonIndexing extends ClassTemplateTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends ClassTemplateTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	template<typename T>
	//	struct Point {
	//		T x;
	//		T y;
	//		constexpr T len() {
	//			return x * x + y * y;
	//		}
	//	};
	//	constexpr int f() {
	//		Point<int> a{3,4}	;
	//		return a.len();
	//	}

	//	constexpr int x = f();
	public void testInstantiationOfClassTemplate() throws Exception {
		assertEvaluationEquals(25);
	}

	//	template<int X>
	//	struct Multiplier {
	//		int y;
	//		constexpr int product() {
	//			return X * y;
	//		}
	//	};
	//	constexpr int f() {
	//		Multiplier<5> m{7};
	//		return m.product();
	//	}

	//	constexpr int x = f();
	public void testInstantiationOfClassTemplateWithNontypeTemplateParameter1() throws Exception {
		assertEvaluationEquals(35);
	}

	//	template<int X, int Y>
	//	struct Multiplier {
	//		int x = X;
	//		int y = Y;
	//		constexpr int product() {
	//			return x * y;
	//		}
	//	};
	//	constexpr int f() {
	//		Multiplier<5, 7> m{};
	//		return m.product();
	//	}

	//	constexpr int x = f();
	public void testInstantiationOfClassTemplateWithNontypeTemplateParameter2() throws Exception {
		assertEvaluationEquals(35);
	}

	//	template<int X, int Y>
	//	struct Adder {
	//		constexpr int sum() {
	//			return X + Y;
	//		}
	//	};
	//
	//	template<int Y>
	//	using FiveAdder = Adder<5, Y>;
	//
	//	constexpr int f() {
	//		FiveAdder<12> adder{};
	//		return adder.sum();
	//	}

	//	constexpr int x = f();
	public void testAliasTemplate1() throws Exception {
		assertEvaluationEquals(17);
	}

	//	template<int T>
	//	struct X {
	//		constexpr int get() const {
	//			return T;
	//		}
	//	};
	//	template<int T>
	//	struct S : X<2*T> {
	//	};
	//	constexpr int f() {
	//		S<5> s{};
	//		return s.get();
	//	}

	//	constexpr int x = f();
	public void testInstantiationOfBaseClassTemplate1() throws Exception {
		assertEvaluationEquals(10);
	}

	//	template<int T>
	//	struct X {
	//		int x = 2*T;
	//	};
	//	template<int T>
	//	struct S : X<T> {
	//		constexpr int get() const {
	//			return 3 * this->x;
	//		}
	//	};
	//	constexpr int f() {
	//		S<5> s{};
	//		return s.get();
	//	}
	//	constexpr int x = f();
	public void testInstantiationOfBaseClassTemplate2() throws Exception {
		assertEvaluationEquals(30);
	}

	//	template<int I>
	//	struct S {
	//		constexpr S():x{I*2} {}
	//		int x;
	//	};
	//
	//	constexpr int f() {
	//		S<5> s{};
	//		return s.x;
	//	}

	//	constexpr int x = f();
	public void testTemplateArgumentInMemberInitializerList() throws Exception {
		assertEvaluationEquals(10);
	}
}