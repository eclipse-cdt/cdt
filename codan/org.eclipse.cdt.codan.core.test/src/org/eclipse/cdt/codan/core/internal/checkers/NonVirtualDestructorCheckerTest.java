/*******************************************************************************
 * Copyright (c) 2011 Patrick Hofer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Hofer - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.NonVirtualDestructor;

/**
 * Test for {@see NonVirtualDestructor} class.
 */
public class NonVirtualDestructorCheckerTest extends CheckerTestCase {
	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(NonVirtualDestructor.ER_ID);
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
	//   virtual void f() = 0;
	//   ~A(); // warn! public non-virtual dtor.
	// };
	public void testPublicVirtualDtorInClass() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	// struct A {
	//   virtual void f() = 0;
	//   // warn! implicit public non-virtual dtor.
	// };
	public void testImplicitPublicNonVirtualDtorInClass() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1);
	}

	// struct F { };
	//
	// struct A {
	//   virtual void f() = 0;
	// private:
	//   friend class F;
	//   ~A(); // warn! can be called from class F.
	// };
	public void testPublicNonVirtualDtorCanBeCalledFromFriendClass() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(7);
	}

	// struct A {
	//   virtual void f() = 0;
	//   virtual ~A();
	// };
	//
	// struct B {
	//   ~B(); // ok.
	// };
	public void testVirtualDtorInBaseClass() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// struct A {
	//   virtual void f() = 0;
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

	// struct A {
	//   virtual void f() = 0;
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
	public void _testNonVirtualDtorInBaseClass2() {
		checkErrorLines(3, 7, 11, 13);
	}
}
