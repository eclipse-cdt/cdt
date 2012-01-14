/*******************************************************************************
 * Copyright (c) 2011, 2012 Patrick Hofer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Hofer - Initial API and implementation
 *     Tomasz Wesolowski
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.NonVirtualDestructor;

/**
 * Test for {@link NonVirtualDestructor} class.
 */
public class NonVirtualDestructorCheckerTest extends CheckerTestCase {
	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(NonVirtualDestructor.PROBLEM_ID);
	}

	// struct A {
	//   virtual void f() = 0;
	//   virtual ~A(); // ok.
	// };
	public void testVirtualDtorInClass() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// struct A {
	//   virtual void f() = 0;
	// protected:
	//   ~A(); // ok.
	// };
	public void testNonPublicVirtualDtorInClass() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// struct A {
	//   virtual void f() { };
	//   ~A(); // warn! public non-virtual dtor.
	// };
	public void testPublicVirtualDtorInClass() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	// struct A {
	//   virtual void f() { };
	//   // warn! implicit public non-virtual dtor.
	// };
	public void testImplicitPublicNonVirtualDtorInClass() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1);
	}

	// struct F { };
	//
	// struct A {
	//   virtual void f() { };
	// private:
	//   friend class F;
	//   ~A(); // warn! can be called from class F.
	// };
	public void testPublicNonVirtualDtorCanBeCalledFromFriendClass() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(7);
	}

	// struct A {
	//   virtual void f() { };
	//   virtual ~A();
	// };
	//
	// struct B {
	//   ~B(); // ok.
	// };
	public void testVirtualDtorInBaseClass1() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// struct A {
	//   virtual void f() { };
	//   virtual ~A();  // ok.
	// };
	//
	// struct B : public A { };
	//
	// struct C { };
	//
	// struct D : public B, C { };
	//
	// struct E : public D { };
	public void testVirtualDtorInBaseClass2() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	class A {
	//	public:
	//	  virtual ~A();
	//	};
	//
	//	class B : public A {
	//	public:
	//	  ~B();
	//	  virtual void m();
	//	  friend class C;
	//	};
	public void testVirtualDtorInBaseClass3() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// struct A {
	//   virtual void f() { };
	//   ~A();  // warn! public non-virtual dtor.
	//          // this affects B, D and E further down in the hierarchy as well
	// };
	//
	// struct B : public A { };
	//
	// struct C { };
	//
	// struct D : public B, C { };
	//
	// struct E : public D {
	// };
	public void testNonVirtualDtorInBaseClass() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3, 7, 11, 13);
	}

	// class A {
	//   virtual void f1() { };
	//   virtual void f2() = 0;
	// };
	//
	// class B : public A {
	//   virtual void f1() { };
	//   virtual void f2() { };
	//   virtual ~B();
	// };
	public void testAbstractBaseClass() {
		loadCodeAndRun(getAboveComment());
		// It doesn't matter if the class is abstract or not - dtor can be called polymorphically.
		checkErrorLines(1);
	}

	//	struct Base {
	//	};
	//	struct Derived : Base {
	//		virtual void bar();
	//	};
	public void testImplicitDtorInBaseClass() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	//	struct Base {
	//  	~Base();
	//	};
	//	struct Derived : Base {
	//		virtual void bar();
	//	};
	public void testExplicitDtorInBaseClass() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// class C : public C {};
	public void testBug368446_stackOverflow() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class B;
	// class A : public B {};
	// class B : public A {};
	public void testBug368446_stackOverflow_indirect() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}
}
