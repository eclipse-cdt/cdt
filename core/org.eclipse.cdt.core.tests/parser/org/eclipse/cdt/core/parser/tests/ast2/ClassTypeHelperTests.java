/*******************************************************************************
 * Copyright (c) 2010 Google, Inc and others.
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

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * Tests for ClassTypeHelper class.
 */
public class ClassTypeHelperTests extends AST2BaseTest {

	public ClassTypeHelperTests() {
	}

	public ClassTypeHelperTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(ClassTypeHelperTests.class);
	}

	protected BindingAssertionHelper getAssertionHelper() throws ParserException, IOException {
		String code= getAboveComment();
		return new BindingAssertionHelper(code, true);
	}

	//	class A {
	//	public:
	//	  A();
	//	  int x;
	//	  A* y;
	//	  const A& z;
	//	};
	public void testHasTrivialCopyCtor_1() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classType = helper.assertNonProblem("A {", 1, ICPPClassType.class);
		assertTrue(ClassTypeHelper.hasTrivialCopyCtor(classType));
	}

	//	struct A {
	//	  A(const A& a);
	//	};
	//
	//	class B {
	//	public:
	//	  A a;
	//	};
	public void testHasTrivialCopyCtor_2() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classType = helper.assertNonProblem("B {", 1, ICPPClassType.class);
		assertFalse(ClassTypeHelper.hasTrivialCopyCtor(classType));
	}

	//	class A {
	//	public:
	//	  A();
	//	  A(const A& a);
	//	  int x;
	//	  A* y;
	//	  const A& z;
	//	};
	public void testHasTrivialDesctructor_1() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classType = helper.assertNonProblem("A {", 1, ICPPClassType.class);
		assertTrue(ClassTypeHelper.hasTrivialDestructor(classType));
	}

	//	struct A {
	//	  ~A();
	//	};
	//
	//	class B {
	//	public:
	//	  A a;
	//	};
	public void testHasTrivialDesctructor_2() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classType = helper.assertNonProblem("B {", 1, ICPPClassType.class);
		assertFalse(ClassTypeHelper.hasTrivialDestructor(classType));
	}

	//	class A {
	//	public:
	//	  A();
	//	  A(const A& a);
	//	  void m();
	//	  int x;
	//	  A* y;
	//	  const A& z;
	//	};
	public void testIsPolymorphic_1() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classType = helper.assertNonProblem("A {", 1, ICPPClassType.class);
		assertFalse(ClassTypeHelper.isPolymorphic(classType));
	}

	//	struct A {
	//	  virtual void m();
	//	};
	//
	//	class B : public A {
	//	};
	public void testIsPolymorphic_2() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classType = helper.assertNonProblem("B", 1, ICPPClassType.class);
		assertTrue(ClassTypeHelper.isPolymorphic(classType));
	}
}
