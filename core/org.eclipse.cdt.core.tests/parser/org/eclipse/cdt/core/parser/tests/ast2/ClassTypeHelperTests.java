/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
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

import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.parser.util.ASTPrinter;
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

	//  int a;
	//	const int& b;
	public void testTemp() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ASTPrinter.print(helper.getTranslationUnit());
	}

	//	struct A {
	//	  A(const A& a);
	//	};
	//
	//	class B {
	//	public:
	//	  B();
	//	  int x;
	//	  A* y;
	//	  const A& z;
	//    static A s;
	//	};
	//
	//	class C {
	//	public:
	//	  A a;
	//	};
	public void testHasTrivialCopyCtor() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classA = helper.assertNonProblem("A {", 1, ICPPClassType.class);
		assertFalse(ClassTypeHelper.hasTrivialCopyCtor(classA));
		ICPPClassType classB = helper.assertNonProblem("B {", 1, ICPPClassType.class);
		assertTrue(ClassTypeHelper.hasTrivialCopyCtor(classB));
		ICPPClassType classC = helper.assertNonProblem("C {", 1, ICPPClassType.class);
		assertFalse(ClassTypeHelper.hasTrivialCopyCtor(classC));
	}

	//	struct A {
	//	  ~A();
	//	};
	//
	//	class B {
	//	public:
	//	  B();
	//	  B(const B& a);
	//	  int x;
	//	  B* y;
	//	  const B& z;
	//	  static A s;
	//	};
	//
	//	class C {
	//	public:
	//	  A a;
	//	};
	public void testHasTrivialDestructor() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classA = helper.assertNonProblem("A {", 1, ICPPClassType.class);
		assertFalse(ClassTypeHelper.hasTrivialDestructor(classA));
		ICPPClassType classB = helper.assertNonProblem("B {", 1, ICPPClassType.class);
		assertTrue(ClassTypeHelper.hasTrivialDestructor(classB));
		ICPPClassType classC = helper.assertNonProblem("C {", 1, ICPPClassType.class);
		assertFalse(ClassTypeHelper.hasTrivialDestructor(classC));
	}

	//	struct A {
	//	  virtual void m();
	//	};
	//
	//	class B {
	//	public:
	//	  B();
	//	  B(const B& a);
	//	  void m();
	//	  int x;
	//	  B* y;
	//	  const B& z;
	//	};
	//
	//	class C : public A {
	//	};
	public void testIsPolymorphic() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classA = helper.assertNonProblem("A {", 1, ICPPClassType.class);
		assertTrue(ClassTypeHelper.isPolymorphic(classA));
		ICPPClassType classB = helper.assertNonProblem("B {", 1, ICPPClassType.class);
		assertFalse(ClassTypeHelper.isPolymorphic(classB));
		ICPPClassType classC = helper.assertNonProblem("C", 1, ICPPClassType.class);
		assertTrue(ClassTypeHelper.isPolymorphic(classC));
	}
}
