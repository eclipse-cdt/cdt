/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.IOException;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.c.CVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPMarkOccurrences;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

/**
 * Unit tests for CPPVariableReadWriteFlags and CVariableReadWriteFlags classes.
 */
public class VariableReadWriteFlagsTest extends AST2TestBase {
	private static final int READ = PDOMName.READ_ACCESS;
	private static final int WRITE = PDOMName.WRITE_ACCESS;
	private static boolean onlyMarkOccurrences = false;

	protected class AssertionHelper extends BindingAssertionHelper {
		AssertionHelper(String contents, boolean isCPP, boolean markOccurrences) throws ParserException {
			super(contents, isCPP);
			onlyMarkOccurrences = markOccurrences;
		}

		void assertReadWriteFlags(String context, String name, int expectedFlags) throws Exception {
			IASTName variable = findName(context, name);
			assertNotNull(variable);
			assertEquals(flagsToString(expectedFlags), flagsToString(getReadWriteFlags(variable)));
		}

		void assertReadWriteFlags(String name, int expectedFlags) throws Exception {
			assertReadWriteFlags(null, name, expectedFlags);
		}

		int getReadWriteFlags(IASTName variable) {
			if (onlyMarkOccurrences) {
				if (isCPP) {
					return CPPMarkOccurrences.getReadWriteFlags(variable);
				}
			} else if (isCPP) {
				return CPPVariableReadWriteFlags.getReadWriteFlags(variable);
			} else {
				return CVariableReadWriteFlags.getReadWriteFlags(variable);
			}
			return 0;
		}

		private String flagsToString(int flags) {
			StringBuilder buf = new StringBuilder();
			if ((flags & READ) != 0) {
				buf.append("READ");
			}
			if ((flags & WRITE) != 0) {
				if (buf.length() != 0)
					buf.append(" | ");
				buf.append("WRITE");
			}
			if (buf.length() == 0)
				buf.append("0");
			return buf.toString();
		}
	}

	public VariableReadWriteFlagsTest() {
	}

	public VariableReadWriteFlagsTest(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(VariableReadWriteFlagsTest.class);
	}

	protected AssertionHelper getCAssertionHelper() throws ParserException, IOException {
		String code= getAboveComment();
		return new AssertionHelper(code, false, false);
	}

	protected AssertionHelper getCPPAssertionHelper() throws ParserException, IOException {
		String code= getAboveComment();
		return new AssertionHelper(code, true, false);
	}
	
	protected AssertionHelper getCPPAssertionHelperMarkOccurrences() throws ParserException, IOException {
		String code= getAboveComment();
		return new AssertionHelper(code, true, true);
	}

	//	int test(int a) {
	//	  a = 2;
	//	  a *= 3;
	//	  return a + 1;
	//	}
	public void testSimpleAccess() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("a = 2", "a", WRITE);
		a.assertReadWriteFlags("a *= 3", "a", READ | WRITE);
		a.assertReadWriteFlags("a + 1", "a", READ);
		
		AssertionHelper b = getCPPAssertionHelperMarkOccurrences();
		b.assertReadWriteFlags("a = 2", "a", WRITE);
		b.assertReadWriteFlags("a *= 3", "a", READ | WRITE);
		b.assertReadWriteFlags("a + 1", "a", READ);
	}

	//	class C {
	//	public:
	//	  C(int);
	//	};
	//
	//	class D {
	//	public:
	//	  D();
	//	};
	//
	//	int a;
	//	int b = 1;
	//	C c;
	//	D d;
	//	C e(1);
	//	template<typename T> void foo(T p) {
	//	  T f;
	//	}
	public void testVariableDeclaration() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("int a", "a", 0);
		a.assertReadWriteFlags("int b = 1", "b", WRITE);
		a.assertReadWriteFlags("C c", "c", 0);
		a.assertReadWriteFlags("D d", "d", WRITE);
		a.assertReadWriteFlags("C e(1)", "e", WRITE);
		a.assertReadWriteFlags("T f", "f", WRITE);
		
		AssertionHelper b = getCPPAssertionHelperMarkOccurrences();
		b.assertReadWriteFlags("int a", "a", 0);
		b.assertReadWriteFlags("int b = 1", "b", WRITE);
		b.assertReadWriteFlags("C c", "c", 0);
		b.assertReadWriteFlags("D d", "d", WRITE);
		b.assertReadWriteFlags("C e(1)", "e", WRITE);
		b.assertReadWriteFlags("T f", "f", WRITE);
	}

	//	struct A { int x; };
	//
	//	void test(A a, A* ap) {
	//	  a.x = 1;
	//	  (&a)->x = 1;
	//	  ap->x = 1;
	//	};
	public void testFieldAccess() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("a.x", "a", WRITE);
		a.assertReadWriteFlags("a.x", "x", WRITE);
		a.assertReadWriteFlags("(&a)->x", "a", WRITE);
		a.assertReadWriteFlags("(&a)->x", "x", WRITE);
		a.assertReadWriteFlags("ap->x", "ap", READ);
		a.assertReadWriteFlags("ap->x", "x", WRITE);
		
		AssertionHelper b = getCPPAssertionHelperMarkOccurrences();
		b.assertReadWriteFlags("a.x", "a", WRITE);
		b.assertReadWriteFlags("a.x", "x", WRITE);
		b.assertReadWriteFlags("(&a)->x", "a", WRITE);
		b.assertReadWriteFlags("(&a)->x", "x", WRITE);
		b.assertReadWriteFlags("ap->x", "ap", WRITE);//differs
		b.assertReadWriteFlags("ap->x", "x", WRITE);
	}

	//	void f(int* x, int& y);
	//	void g(const int* x, const int& y, int z);
	//
	//	void test(int a, int b, int c) {
	//	  f(&a, b);
	//	  g(&a, b, c);
	//	};
	public void testFunctionCall() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("f(&a, b)", "a", READ | WRITE);
		a.assertReadWriteFlags("f(&a, b)", "b", READ | WRITE);
		a.assertReadWriteFlags("f(&a, b)", "f", READ);
		a.assertReadWriteFlags("g(&a, b, c)", "a", READ);
		a.assertReadWriteFlags("g(&a, b, c)", "b", READ);
		a.assertReadWriteFlags("g(&a, b, c)", "c", READ);
		
		AssertionHelper b = getCPPAssertionHelperMarkOccurrences();
		b.assertReadWriteFlags("f(&a, b)", "a", READ | WRITE);
		b.assertReadWriteFlags("f(&a, b)", "b", READ | WRITE);
		b.assertReadWriteFlags("f(&a, b)", "f", READ);
		b.assertReadWriteFlags("g(&a, b, c)", "a", READ);
		b.assertReadWriteFlags("g(&a, b, c)", "b", READ);
		b.assertReadWriteFlags("g(&a, b, c)", "c", READ);
	}

	//	struct A {
	//	  A(int* x, int& y);
	//	  A(const int* x, const int& y, int z);
	//	};
	//
	//	void test(int a, int b, int c) {
	//	  A u = A(&a, b);
	//	  A* v = new A(&a, b);
	//	  A w(&a, b);
	//	  A x = A(&a, b, c);
	//	  A* y = new A(&a, b, c);
	//	  A z(&a, b, c);
	//	};
	public void testConstructorCall_393068() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("= A(&a, b)", "a", READ | WRITE);
		a.assertReadWriteFlags("= A(&a, b)", "b", READ | WRITE);
		a.assertReadWriteFlags("new A(&a, b)", "a", READ | WRITE);
		a.assertReadWriteFlags("new A(&a, b)", "b", READ | WRITE);
		a.assertReadWriteFlags("w(&a, b)", "a", READ | WRITE);
		a.assertReadWriteFlags("w(&a, b)", "b", READ | WRITE);
		a.assertReadWriteFlags("w(&a, b)", "w", WRITE);
		a.assertReadWriteFlags("= A(&a, b, c)", "a", READ);
		a.assertReadWriteFlags("= A(&a, b, c)", "b", READ);
		a.assertReadWriteFlags("= A(&a, b, c)", "c", READ);
		a.assertReadWriteFlags("new A(&a, b, c)", "a", READ);
		a.assertReadWriteFlags("new A(&a, b, c)", "b", READ);
		a.assertReadWriteFlags("new A(&a, b, c)", "c", READ);
		a.assertReadWriteFlags("z(&a, b, c)", "a", READ);
		a.assertReadWriteFlags("z(&a, b, c)", "b", READ);
		a.assertReadWriteFlags("z(&a, b, c)", "c", READ);
		
		AssertionHelper b = getCPPAssertionHelperMarkOccurrences();
		b.assertReadWriteFlags("= A(&a, b)", "a", READ | WRITE);
		b.assertReadWriteFlags("= A(&a, b)", "b", READ | WRITE);
		b.assertReadWriteFlags("new A(&a, b)", "a", READ | WRITE);
		b.assertReadWriteFlags("new A(&a, b)", "b", READ | WRITE);
		b.assertReadWriteFlags("w(&a, b)", "a", READ | WRITE);
		b.assertReadWriteFlags("w(&a, b)", "b", READ | WRITE);
		b.assertReadWriteFlags("w(&a, b)", "w", WRITE);
		b.assertReadWriteFlags("= A(&a, b, c)", "a", READ);
		b.assertReadWriteFlags("= A(&a, b, c)", "b", READ);
		b.assertReadWriteFlags("= A(&a, b, c)", "c", READ);
		b.assertReadWriteFlags("new A(&a, b, c)", "a", READ);
		b.assertReadWriteFlags("new A(&a, b, c)", "b", READ);
		b.assertReadWriteFlags("new A(&a, b, c)", "c", READ);
		b.assertReadWriteFlags("z(&a, b, c)", "a", READ);
		b.assertReadWriteFlags("z(&a, b, c)", "b", READ);
		b.assertReadWriteFlags("z(&a, b, c)", "c", READ);
	}

	//	struct A {
	//	  void m();
	//	  void mc() const;
	//	};
	//
	//	void test(A a, A* ap) {
	//	  a.m();
	//	  a.mc();
	//	  (&a)->m();
	//	  (&a)->mc();
	//	  ap->m();
	//	  (*ap).m();
	//	};
	public void testMethodCall() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("a.m()", "a", READ | WRITE);
		a.assertReadWriteFlags("a.m()", "m", READ);
		a.assertReadWriteFlags("a.mc()", "a", READ);
		a.assertReadWriteFlags("(&a)->m()", "a", READ | WRITE);
		a.assertReadWriteFlags("(&a)->m()", "m", READ);
		a.assertReadWriteFlags("ap->m()", "ap", READ);
		a.assertReadWriteFlags("(*ap).m()", "ap", READ);
		
		AssertionHelper b = getCPPAssertionHelperMarkOccurrences();
		b.assertReadWriteFlags("a.m()", "a", READ | WRITE);
		b.assertReadWriteFlags("a.m()", "m", READ);
		b.assertReadWriteFlags("a.mc()", "a", READ);
		b.assertReadWriteFlags("(&a)->m()", "a", READ | WRITE);
		b.assertReadWriteFlags("(&a)->m()", "m", READ);
		b.assertReadWriteFlags("ap->m()", "ap", READ | WRITE);//differs
		b.assertReadWriteFlags("(*ap).m()", "ap", READ | WRITE);//differs
	}
	
	//  void test(int b) {
	//    int* a = new int[10];
	//    a = 1;
	//    *a = 1;
	//    *a += 1;
	//    a[1] = 1;
	//    ++a[1];
	//    a[1]--;
	//    a[1] += 1;
	//    b = a[0];
	//    b = *a;
	//    b += a[0];
	//    int c[5][5];
	//    c[1][2] = 1;
	//    **c = 1;
	//    b = **c;
	//    c[1][2]++;
	//    int d[5][5][5];
	//    d[1][2][3] = 1; 
	//     };
	public void testArray_385750() throws Exception {	
		// tests for variable read/write flags
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("int* a = new int[10]", "a", WRITE);
		a.assertReadWriteFlags("a = 1", "a", WRITE);
		a.assertReadWriteFlags("*a = 1", "a", READ);
		a.assertReadWriteFlags("*a += 1", "a", READ);
		a.assertReadWriteFlags("a[1] = 1", "a", READ);	
		a.assertReadWriteFlags("++a[1]", "a", READ);
		a.assertReadWriteFlags("a[1]--", "a", READ);
		a.assertReadWriteFlags("a[1] += 1", "a", READ);
		a.assertReadWriteFlags("b = a[0]", "a", READ);
		a.assertReadWriteFlags("b = *a", "a", READ);	
		a.assertReadWriteFlags("b += a[0]", "a", READ);	
		a.assertReadWriteFlags("c[1][2] = 1", "c", READ);
		a.assertReadWriteFlags("**c = 1", "c", READ);
		a.assertReadWriteFlags("b = **c", "c", READ);
		a.assertReadWriteFlags("c[1][2]++", "c", READ);	
		a.assertReadWriteFlags("d[1][2][3] = 1", "d", READ);
		/* tests for occurrence read/write flags 
		 * (different treatment for pointers and arrays)
		 */
		AssertionHelper b = getCPPAssertionHelperMarkOccurrences();	
		b.assertReadWriteFlags("int* a = new int[10]", "a", WRITE);
		a.assertReadWriteFlags("a = 1", "a", WRITE);
		b.assertReadWriteFlags("*a = 1", "a", WRITE);//differs
		b.assertReadWriteFlags("*a += 1", "a", READ | WRITE);//differs
		b.assertReadWriteFlags("a[1] = 1", "a", WRITE);	//differs
		b.assertReadWriteFlags("++a[1]", "a", READ | WRITE);//differs
		b.assertReadWriteFlags("a[1]--", "a", READ | WRITE);//differs
		b.assertReadWriteFlags("a[1] += 1", "a", READ | WRITE);//differs
		b.assertReadWriteFlags("b = a[0]", "a", READ);
		b.assertReadWriteFlags("b = *a", "a", READ);	
		b.assertReadWriteFlags("b += a[0]", "a", READ);	
		b.assertReadWriteFlags("c[1][2] = 1", "c", WRITE);//differs
		b.assertReadWriteFlags("**c = 1", "c", WRITE);//differs
		b.assertReadWriteFlags("b = **c", "c", READ);
		b.assertReadWriteFlags("c[1][2]++", "c", READ | WRITE);//differs	
		b.assertReadWriteFlags("d[1][2][3] = 1", "d", WRITE);//differs	
	}
	
}
