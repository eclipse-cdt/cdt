/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.VirtualMethodCallChecker;

/**
 * Test for {@link VirtualMethodCallChecker} class
 */
public class VirtualMethodCallCheckerTest extends CheckerTestCase {

	private static final String ERR_VIRTUAL_ID = VirtualMethodCallChecker.VIRTUAL_CALL_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_VIRTUAL_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//class Foo {
	//public:
	//Foo();
	//~Foo();
	//virtual void bar();
	//virtual void pure() = 0;
	//virtual void notpure();
	//};
	//Foo::Foo() {
	//	pure();
	//}
	//Foo::~Foo() {
	//}
	//Foo::bar() {
	//}
	public void testWithPureInCtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(10, ERR_VIRTUAL_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//~Foo();
	//virtual void bar();
	//virtual void pure() = 0;
	//virtual void notpure();
	//};
	//Foo::Foo() {
	//	notpure();
	//}
	//Foo::~Foo() {
	//}
	//Foo::bar() {
	//}
	public void testWithNotPureInCtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(10, ERR_VIRTUAL_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//~Foo();
	//virtual void bar();
	//virtual void pure() = 0;
	//virtual void notpure();
	//};
	//Foo::Foo() {
	//}
	//Foo::~Foo() {
	//	pure();
	//}
	//Foo::bar() {
	//}
	public void testWithPureInDtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(12, ERR_VIRTUAL_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//~Foo();
	//virtual void bar();
	//virtual void pure() = 0;
	//virtual void notpure();
	//};
	//Foo::Foo() {
	//}
	//Foo::~Foo() {
	//	notpure();
	//}
	//Foo::bar() {
	//}
	public void testWithNotPureInDtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(12, ERR_VIRTUAL_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//~Foo();
	//virtual void bar();
	//virtual void pure() = 0;
	//virtual void notpure();
	//};
	//Foo::Foo() {
	//}
	//Foo::~Foo() {
	//}
	//Foo::bar() {
	//	pure();
	//	notpure();
	//}
	public void testWithVirtualInMethod() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_VIRTUAL_ID);
	}

	//class A {
	//public:
	//	virtual void v() {}
	//};
	//class B {
	//private:
	//A a;
	//public:
	//	B() { a.v(); }
	//};
	public void testVirtualMethodOtherClass() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_VIRTUAL_ID);
	}

	//class B {
	//private:
	//A a;
	//public:
	//	B() { this->v(); }
	//	virtual void v() {}
	//};
	public void testVirtualMethodWithThis() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_VIRTUAL_ID);
	}

	//class B {
	//private:
	//A a;
	//public:
	//	B() { (*this).v(); }
	//	virtual void v() {}
	//};
	public void testVirtualMethodWithDerThis() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_VIRTUAL_ID);
	}

	//class A {
	//public:
	//	virtual void v() {}
	//};
	//class B: public A {
	//public:
	//	B() { A::v(); }
	//};
	public void testVirtualMethodChildClass() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(7, ERR_VIRTUAL_ID);
	}

	//class Foo {
	//public:
	//	Foo() {
	//		class LocalClass {
	//			LocalClass() {}
	//			virtual void foo() {
	//			}
	//			void func() {
	//				foo();
	//			}
	//			virtual ~LocalClass() {}
	//		};
	//	}
	//};
	public void testVirtualMethodLocalClass() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_VIRTUAL_ID);
	}

	//class A {
	//public:
	//	A() : A(5) { foo(); }
	//	A(int a) : a(a) { }
	//	virtual void foo();
	//private:
	//	  int a;
	//};
	public void testVirtualMethodDelCtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_VIRTUAL_ID);
	}

	//class A {
	//public:
	//	A() {
	//		A a;
	//		a.foo();
	//	}
	//	virtual void foo();
	//};
	public void testVirtualMethodOtherInstance() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_VIRTUAL_ID);
	}

	//class A {
	//public:
	//	A() {
	//		foo();
	//	}
	//	virtual void foo();
	//	class Nested {
	//	public:
	//		Nested() { bar(); };
	//		virtual void bar();
	//	}
	//};
	public void testVirtualMethodNested() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4, 9);
	}
}
