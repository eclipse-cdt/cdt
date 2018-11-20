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

public class IfStatementTests extends TestBase {
	public static class NonIndexing extends IfStatementTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends IfStatementTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	constexpr int f() {
	//		if (true) {
	//			return 1;
	//		}
	//		return 0;
	//	}

	//	constexpr int x = f();
	public void testSimpleIfTrueBranch() throws Exception {
		assertEvaluationEquals(1);
	}

	//	constexpr int f() {
	//		if (false) {
	//			return 1;
	//		}
	//      return 0;
	//	}

	//	constexpr int x = f();
	public void testSimpleIfFalseBranch() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr int f() {
	//		if (true) {
	//			return 1;
	//		} else {
	//			return 0;
	//      }
	//	}

	//	constexpr int x = f();
	public void testIfElseTrueBranch() throws Exception {
		assertEvaluationEquals(1);
	}

	//	constexpr int f() {
	//		if (false) {
	//			return 1;
	//		} else {
	//			return 0;
	//      }
	//	}

	//	constexpr int x = f();
	public void testSimpleIfElseFalseBranch() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr int f() {
	//		if (false) {
	//			return 1;
	//		} else if (true) {
	//			return 2;
	//      } else {
	//			return 0;
	//		}
	//	}

	//	constexpr int x = f();
	public void testNestedIfTrueBranch() throws Exception {
		assertEvaluationEquals(2);
	}

	//	constexpr int f() {
	//		if (false) {
	//			return 1;
	//		} else if (false) {
	//			return 2;
	//      } else {
	//			return 0;
	//		}
	//	}

	//	constexpr int x = f();
	public void testNestedIfFalseBranch() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr int f() {
	//		if (true)
	//			return 1;
	//		return 0;
	//	}

	//	constexpr int x = f();
	public void testIfStatementWithNonCompoundThenClause() throws Exception {
		assertEvaluationEquals(1);
	}

	//	constexpr int f() {
	//		if (false)
	//			return 1;
	//		else
	//			return 0;
	//	}

	//	constexpr int x = f();
	public void testIfStatementWithNonCompoundElseClause() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr int f() {
	//		int i;
	//		if (true) {
	//			i = 10;
	//		} else {
	//			i = 20;
	//      }
	//		return i;
	//	}

	//	constexpr int x = f();
	public void testIfStatementWithNonReturnClauses() throws Exception {
		assertEvaluationEquals(10);
	}

	//	constexpr int f() {
	//		int i;
	//		if (false)
	//			i = 10;
	//		else
	//			i = 20;
	//		return i;
	//	}

	//	constexpr int x = f();
	public void testIfStatementWithNonReturnClausesAndNonCompoundElseClause() throws Exception {
		assertEvaluationEquals(20);
	}

	//	constexpr int f(int y) {
	//	  if(int x = y*2) {
	//	    return 14 / x;
	//	  } else {
	//	    return 0;
	//	  }
	//	}

	//	constexpr int x = f(1);
	public void testDeclarationInIfStatementCondition1() throws Exception {
		assertEvaluationEquals(7);
	}

	//	constexpr int f(int y) {
	//	  if(int x = y*2) {
	//	    return 14 / x;
	//	  } else {
	//	    return 0;
	//	  }
	//	}

	//	constexpr int x = f(0);
	public void testDeclarationInIfStatementCondition2() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr int g(int x) {
	//	  return x * 2;
	//	}
	//	constexpr int f(int y) {
	//	  if(int x = g(y)) {
	//	    return 14 / x;
	//	  } else {
	//	    return 0;
	//	  }
	//	}

	//	constexpr int x = f(1);
	public void testDeclarationInIfStatementCondition3() throws Exception {
		assertEvaluationEquals(7);
	}

	//	constexpr int g(int x) {
	//	  return x * 2;
	//	}
	//	constexpr int f(int y) {
	//	  if(int x = g(y); x == 2) {
	//	    return 14 / x;
	//	  } else {
	//	    return 0;
	//	  }
	//	}

	//	constexpr int x = f(1);
	public void testInitStatementInIfStatementCondition1() throws Exception {
		assertEvaluationEquals(7);
	}

	//	constexpr int g(int x) {
	//	  return x * 2;
	//	}
	//	constexpr int f(int y) {
	//	  if(int x = g(y); x != 2) {
	//	    return 14 / x;
	//	  } else {
	//	    return 0;
	//	  }
	//	}

	//	constexpr int x = f(1);
	public void testInitStatementInIfStatementCondition2() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr int g(int x) {
	//	  return x * 2;
	//	}
	//	constexpr int f() {
	//	  if constexpr (constexpr int x = g(1); x != 2) {
	//	    return 14 / x;
	//	  } else {
	//	    return 0;
	//	  }
	//	}

	//	constexpr int x = f();
	public void testInitStatementInIfStatementCondition3() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr int g(int x) {
	//	  return x * 2;
	//	}
	//	constexpr int f(int y) {
	//    int x = g(y);
	//	  if(; x == 2) {
	//	    return 14 / x;
	//	  } else {
	//	    return 0;
	//	  }
	//	}

	//	constexpr int x = f(1);
	public void testEmptyInitStatementInIfStatementCondition1() throws Exception {
		assertEvaluationEquals(7);
	}
}
