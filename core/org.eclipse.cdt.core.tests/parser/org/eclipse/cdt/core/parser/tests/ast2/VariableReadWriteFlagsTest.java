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

		void assertReadWriteFlags(String section, int expectedFlags) throws Exception {
			int len;
			for (len = 0; len < section.length(); len++) {
				if (!Character.isJavaIdentifierPart(section.charAt(len)))
					break;
			}
			assertReadWriteFlags(section, len, expectedFlags);
		}

		void assertReadWriteFlags(String section, int len, int expectedFlags) throws Exception {
			IASTName variable = findName(section, len);
			assertNotNull(variable);
			assertEquals(flagsToString(expectedFlags), flagsToString(getReadWriteFlags(variable)));
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

	//	int test() {
	//	  int a;
	//	  a = 2;
	//	  return a + 1;
	//	}
	public void testSimpleAccess() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("a = 2", WRITE);
		a.assertReadWriteFlags("a +", READ);
	}

	//	int a = 1;
	public void _testEqualsInitializer() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("a", WRITE);
	}

	//	struct A { int x; };
	//
	//	void test() {
	//	  A a;
	//	  a.x = 1;
	//	};
	public void testFieldAccess() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("a.", WRITE);
	}

	//	struct A { int x; };
	//
	//	void test(A* a) {
	//	  a->x = 1;
	//	};
	public void testFieldAccessWithDereference() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("a->", READ);
	}

	//	void f(int* x);
	//	void g(const int* x);
	//
	//	void test() {
	//	  int a, b;
	//	  f(&a);
	//	  g(&b);
	//	};
	public void testExplicitArgument() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("a)", READ | WRITE);
		a.assertReadWriteFlags("b)", READ);
	}

	//	struct A {
	//	  void m1();
	//	  void m2() const;
	//	};
	//
	//	void test() {
	//	  A a;
	//	  a.m1();
	//	  a.m2();
	//	};
	public void _testImplicitArgument() throws Exception {
		AssertionHelper a = getCPPAssertionHelper();
		a.assertReadWriteFlags("a.m1", READ | WRITE);
		a.assertReadWriteFlags("a.m2", READ);
	}
}
