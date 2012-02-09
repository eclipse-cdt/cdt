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
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

/**
 * Unit tests for CPPVariableReadWriteFlags and CVariableReadWriteFlags classes.
 */
public class VariableReadWriteFlagsTest extends AST2BaseTest {
	private static final int READ = PDOMName.READ_ACCESS;
	private static final int WRITE = PDOMName.WRITE_ACCESS;

	protected class AssertionHelper extends BindingAssertionHelper {
		AssertionHelper(String contents, boolean isCPP) throws ParserException {
			super(contents, isCPP);
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
			return isCPP ?
					CPPVariableReadWriteFlags.getReadWriteFlags(variable) :
					CVariableReadWriteFlags.getReadWriteFlags(variable);
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
		return new AssertionHelper(code, false);
	}

	protected AssertionHelper getCPPAssertionHelper() throws ParserException, IOException {
		String code= getAboveComment();
		return new AssertionHelper(code, true);
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
	}
}
