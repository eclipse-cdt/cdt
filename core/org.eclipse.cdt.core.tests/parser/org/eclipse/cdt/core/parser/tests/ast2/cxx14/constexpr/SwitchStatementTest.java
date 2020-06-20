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

public class SwitchStatementTests extends TestBase {
	public static class NonIndexing extends SwitchStatementTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends SwitchStatementTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	constexpr int f() {
	//		int x { 1 };
	//		switch (x) {
	//		case 1:
	//			return 1;
	//		case 2:
	//			return 2;
	//		default:
	//			return -1;
	//		}
	//	}

	//	constexpr int x = f();
	public void testSwitchFirstCase() throws Exception {
		assertEvaluationEquals(1);
	}

	//	constexpr int f() {
	//		int x { 2 };
	//		switch (x) {
	//		case 1:
	//			return 1;
	//		case 2:
	//			return 2;
	//		default:
	//			return -1;
	//		}
	//	}

	//	constexpr int x = f();
	public void testSwitchMiddleCase() throws Exception {
		assertEvaluationEquals(2);
	}

	//	constexpr int f() {
	//		int x { 3 };
	//		switch (x) {
	//		case 1:
	//			return 1;
	//		case 2:
	//			return 2;
	//		default:
	//			return -1;
	//		}
	//	}

	//	constexpr int x = f();
	public void testSwitchDefault() throws Exception {
		assertEvaluationEquals(-1);
	}

	//	constexpr int f() {
	//		int x { 1 };
	//		switch (x) {
	//		case 1:
	//		case 2:
	//			return 2;
	//		default:
	//			return -1;
	//		}
	//	}

	//	constexpr int x = f();
	public void testSwitchFallThrough() throws Exception {
		assertEvaluationEquals(2);
	}

	//	constexpr int f() {
	//		int x { 1 };
	//		int y = 10;
	//		switch(x)
	//		case 1:
	//			y++;
	//		y--;
	//		return y;
	//	}

	//	constexpr int x = f();
	public void testSwitchWithOnlyOneClause1() throws Exception {
		assertEvaluationEquals(10);
	}

	//	constexpr int f() {
	//		int x { 0 };
	//		int y = 10;
	//		switch(x)
	//		case 1:
	//			y++;
	//		y--;
	//		return y;
	//	}

	//	constexpr int x = f();
	public void testSwitchWithOnlyOneClause2() throws Exception {
		assertEvaluationEquals(9);
	}

	//	constexpr int f() {
	//		int x { 2 };
	//		int y = 2;
	//		switch (x) {
	//		case 1:
	//			y = 10;
	//			break;
	//		case 2:
	//			y = 20;
	//			break;
	//		default:
	//			y = 30;
	//		}
	//		return y;
	//	}

	//	constexpr int x = f();
	public void testSwitchBreak() throws Exception {
		assertEvaluationEquals(20);
	}

	//	constexpr int f() {
	//		int x { 3 };
	//		int y = 2;
	//		switch (x) {
	//		case 1:
	//			y = 10;
	//			break;
	//		case 2:
	//			y = 20;
	//			break;
	//		}
	//		return y;
	//	}

	//	constexpr int x = f();
	public void testSwitchNoMatchingCaseAndNoDefault() throws Exception {
		assertEvaluationEquals(2);
	}

	//	class Point {
	//		int x, y;
	//	public:
	//		constexpr Point(int x, int y):x{x}, y{y*2} {}
	//		constexpr int getY() const { return y; }
	//	};
	//	constexpr int f() {
	//		int x { 2 };
	//		int y = 5;
	//		constexpr Point p{4, 1};
	//
	//		switch (x) {
	//		case 1:
	//			y = 10;
	//			break;
	//		case p.getY():
	//			y = 20;
	//			break;
	//		}
	//		return y;
	//	}

	//	constexpr int x = f();
	public void testSwitchCaseConstants() throws Exception {
		assertEvaluationEquals(20);
	}

	//	constexpr int triple(int x) {
	//		return x * 3;
	//	}
	//	constexpr int f(int y) {
	//		switch(int x = triple(y)) {
	//			case 9:
	//				return 1;
	//			case 12:
	//				return 2;
	//			case 15:
	//				return 3;
	//			default:
	//				return 4;
	//		}
	//	}

	//	constexpr int x = f(5);
	public void testDeclarationInSwitchStatementController() throws Exception {
		assertEvaluationEquals(3);
	}

	//	enum Color { RED, GREEN, BLUE };
	//	constexpr int f(Color color) {
	//		switch(color) {
	//			case RED:
	//				return 1;
	//			case GREEN:
	//				return 2;
	//			case BLUE:
	//				return 3;
	//		}
	//	}

	//	constexpr int x = f(BLUE);
	public void testSwitchOnEnumValue() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int f() {
	//		int arr[] = {1,2,1,3,5,6,1,2,0};
	//		int sum{};
	//		for(int i : arr) {
	//			switch(i) {
	//			case 1:
	//				sum++;
	//				break;
	//			default:
	//				continue;
	//				sum += 2;
	//			}
	//		}
	//		return sum;
	//	}

	//	constexpr int x = f();
	public void testSwitchWithNestedContinueStatement() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int triple(int x) {
	//		return x * 3;
	//	}
	//	constexpr int f(int y) {
	//		switch(int x = triple(y); x) {
	//			case 9:
	//				return 1;
	//			case 12:
	//				return 2;
	//			case 15:
	//				return 3;
	//			default:
	//				return 4;
	//		}
	//	}

	//	constexpr int x = f(5);
	public void testDeclarationInSwitchInitStatement() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int triple(int x) {
	//		return x * 3;
	//	}
	//	constexpr int f(int y) {
	//		switch(; int x = triple(y)) {
	//			case 9:
	//				return 1;
	//			case 12:
	//				return 2;
	//			case 15:
	//				return 3;
	//			default:
	//				return 4;
	//		}
	//	}

	//	constexpr int x = f(5);
	public void testDeclarationInSwitchStatementControllerEmptyInit() throws Exception {
		assertEvaluationEquals(3);
	}
}