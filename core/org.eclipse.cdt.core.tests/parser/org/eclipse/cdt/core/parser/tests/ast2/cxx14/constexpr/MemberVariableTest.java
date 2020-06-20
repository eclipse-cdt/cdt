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

public class MemberVariableTests extends TestBase {
	public static class NonIndexing extends MemberVariableTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends MemberVariableTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	// struct Point { int x, y; };
	// constexpr int f() {
	// 	Point p{ 2, 4 };
	// 	p.x++;
	// 	return p.x;
	// }

	//	constexpr auto x = f();
	public void testIncrementOnCompositeValues() throws Exception {
		assertEvaluationEquals(3);
	}

	//	struct S {
	//		int x, y;
	//	};
	//	constexpr int f() {
	//		const S s{3,7};
	//		return s.y;
	//	}

	//	constexpr auto x = f();
	public void testMemberAccessWithConstObject() throws Exception {
		assertEvaluationEquals(7);
	}

	//	struct S {
	//		int x, y;
	//	};
	//	constexpr S s{5, 6};
	//  constexpr int f() { return s.y; }

	//	constexpr auto x = f();
	public void testGlobalMemberAccessFromConstexprFunction() throws Exception {
		assertEvaluationEquals(6);
	}

	//	struct S {
	//		int x, y;
	//	};
	//	constexpr S s{5, 6};

	//	constexpr auto x = s.y;
	public void testGlobalMemberAccessFromGlobalConstexpr() throws Exception {
		assertEvaluationEquals(6);
	}

	//	struct T {
	//		constexpr T(int x):x{2*x}{}
	//		int x;
	//	};
	//	struct S {
	//		T t{5};
	//		int i = t.x * 2;
	//	};
	//	constexpr int f() {
	//		S s{};
	//		return s.i;
	//	}

	//	constexpr int x = f();
	public void testFieldDependsOnOtherField() throws Exception {
		assertEvaluationEquals(20);
	}

	//	class S {
	//		int arr[4]{2,4,6,8};
	//	public:
	// 		constexpr int *getPtr() {
	//   		return arr;
	//    	}
	//	};
	//	constexpr int f() {
	// 		S s{};
	//  	int *ptr = s.getPtr()+2;
	//    	return *ptr;
	//	}

	//	constexpr int x = f();
	public void testMemberInitializationWithoutUserDefinedCtor() throws Exception {
		assertEvaluationEquals(6);
	}

	//    struct S {
	//    	static const int x = 5;
	//    };
	//    constexpr int f() {
	//    	return S::x;
	//    }

	//    constexpr int x = f();
	public void testAccessOfStaticField() throws Exception {
		assertEvaluationEquals(5);
	}
}
