/*******************************************************************************
 * Copyright (c) 2011, 2012 Google, Inc and others.
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeTraits;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * Tests for ClassTypeHelper class.
 */
public class TypeTraitsTests extends AST2BaseTest {

	public TypeTraitsTests() {
	}

	public TypeTraitsTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(TypeTraitsTests.class);
	}

	protected BindingAssertionHelper getAssertionHelper() throws ParserException, IOException {
		String code= getAboveComment();
		return new BindingAssertionHelper(code, true);
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
		assertFalse(TypeTraits.hasTrivialCopyCtor(classA, null));
		ICPPClassType classB = helper.assertNonProblem("B {", 1, ICPPClassType.class);
		assertTrue(TypeTraits.hasTrivialCopyCtor(classB, null));
		ICPPClassType classC = helper.assertNonProblem("C {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.hasTrivialCopyCtor(classC, null));
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
		assertFalse(TypeTraits.hasTrivialDestructor(classA, null));
		ICPPClassType classB = helper.assertNonProblem("B {", 1, ICPPClassType.class);
		assertTrue(TypeTraits.hasTrivialDestructor(classB, null));
		ICPPClassType classC = helper.assertNonProblem("C {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.hasTrivialDestructor(classC, null));
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
		assertTrue(TypeTraits.isPolymorphic(classA, null));
		ICPPClassType classB = helper.assertNonProblem("B {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isPolymorphic(classB, null));
		ICPPClassType classC = helper.assertNonProblem("C", 1, ICPPClassType.class);
		assertTrue(TypeTraits.isPolymorphic(classC, null));
	}

	//	struct A {
	//	  A* a;
	//	  int b;
	//	  A(A* a, int b);
	//	  A(A& a);
	//	  ~A();
	//	};
	//
	//	class B : public A {
	//	  static int c;
	//	  void m(A* a);
	//	};
	//
	//	class C : public A {
	//	  int c;
	//	};
	//
	//	struct D {
	//	  C c;
	//	};
	//
	//	struct E : public C {
	//	};
	//
	//	struct F : public B {
	//	  virtual ~F();
	//	};
	//
	//	struct G {
	//	  int a;
	//	private:
	//	  int b;
	//	};
	//
	//	struct H {
	//	  int& a;
	//	};
	public void testIsStandardLayout() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classA = helper.assertNonProblem("A {", 1, ICPPClassType.class);
		assertTrue(TypeTraits.isStandardLayout(classA, null));
		ICPPClassType classB = helper.assertNonProblem("B {", 1, ICPPClassType.class);
		assertTrue(TypeTraits.isStandardLayout(classB, null));
		ICPPClassType classC = helper.assertNonProblem("C :", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isStandardLayout(classC, null));
		ICPPClassType classD = helper.assertNonProblem("D {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isStandardLayout(classD, null));
		ICPPClassType classE = helper.assertNonProblem("E :", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isStandardLayout(classE, null));
		ICPPClassType classF = helper.assertNonProblem("F :", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isStandardLayout(classF, null));
		ICPPClassType classG = helper.assertNonProblem("G {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isStandardLayout(classG, null));
		ICPPClassType classH = helper.assertNonProblem("H {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isStandardLayout(classH, null));
	}

	//	struct A {
	//	  A* a;
	//	  int b;
	//	  A(char* s);
	//	  A(const A& a, int b);
	//	  A& operator =(const A& a, A* b);
	//	};
	//
	//	class B : public A {
	//	  A a;
	//	};
	//
	//	struct C {
	//	  C(char* s = 0);
	//	};
	//
	//	struct D {
	//	  D(const D& a, int b = 1);
	//	};
	//
	//	struct E {
	//	  E& operator =(const E& a, E* b = nullptr);
	//	};
	//
	//	struct F {
	//	  ~F();
	//	};
	//
	//	struct G {
	//	  C c;
	//	};
	//
	//	struct H : public C {
	//	};
	//
	//	struct I {
	//	  virtual void m();
	//	};
	public void testIsTrivial() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classA = helper.assertNonProblem("A {", 1, ICPPClassType.class);
		assertTrue(TypeTraits.isTrivial(classA, null));
		ICPPClassType classB = helper.assertNonProblem("B :", 1, ICPPClassType.class);
		assertTrue(TypeTraits.isTrivial(classB, null));
		ICPPClassType classC = helper.assertNonProblem("C {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isTrivial(classC, null));
		ICPPClassType classD = helper.assertNonProblem("D {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isTrivial(classD, null));
		ICPPClassType classE = helper.assertNonProblem("E {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isTrivial(classE, null));
		ICPPClassType classF = helper.assertNonProblem("F {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isTrivial(classF, null));
		ICPPClassType classG = helper.assertNonProblem("G {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isTrivial(classG, null));
		ICPPClassType classH = helper.assertNonProblem("H :", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isTrivial(classH, null));
		ICPPClassType classI = helper.assertNonProblem("I {", 1, ICPPClassType.class);
		assertFalse(TypeTraits.isTrivial(classI, null));
	}
}
