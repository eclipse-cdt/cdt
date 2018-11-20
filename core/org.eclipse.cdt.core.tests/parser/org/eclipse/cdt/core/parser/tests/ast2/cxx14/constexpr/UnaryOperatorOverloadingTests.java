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

public class UnaryOperatorOverloadingTests extends TestBase {
	public static class NonIndexing extends UnaryOperatorOverloadingTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends UnaryOperatorOverloadingTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	class Point {
	//		int x, y;
	//	public:
	//		constexpr Point(int x, int y):x{x}, y{y} {}
	//		constexpr Point& operator++() {
	//			++x;
	//	  		++y;
	//	  		return *this;
	//		}
	//		constexpr int getY() const { return y; }
	//	};
	//
	//	constexpr int f() {
	//	  Point p{4,5};
	//	  ++p;
	//	  return p.getY();
	//	}

	//	constexpr int x = f();
	public void testPrefixIncrementAsMemberFunction() throws Exception {
		assertEvaluationEquals(6);
	}

	//	class Point {
	//	  int x, y;
	//	  friend constexpr Point& operator++(Point&);
	//	public:
	//	  constexpr Point(int x, int y):x{x}, y{y} {}
	//	  constexpr int getY() const {
	//	    return y;
	//	  }
	//	};
	//
	//	constexpr Point& operator++(Point& p) {
	//	  ++p.x;
	//	  ++p.y;
	//	  return p;
	//	}
	//
	//	constexpr int f() {
	//	  Point p{4,5};
	//	  ++p;
	//	  return p.getY();
	//	}

	//	constexpr int x = f();
	public void testPrefixIncrementAsNonMemberFunction() throws Exception {
		assertEvaluationEquals(6);
	}
}
