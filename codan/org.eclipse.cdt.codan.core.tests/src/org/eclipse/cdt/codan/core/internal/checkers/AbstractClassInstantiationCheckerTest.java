/*******************************************************************************
 * Copyright (c) 2011, 2014 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov  - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.AbstractClassInstantiationChecker;

/**
 * Test for {@see AbstractClassInstantiationChecker} class
 */
public class AbstractClassInstantiationCheckerTest extends CheckerTestCase {
	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(AbstractClassInstantiationChecker.ER_ID);
	}

	// class C {
	//   virtual void f() {}
	// };
	// void scope () {
	//   C c;  // No errors.
	// }
	public void testNotAbstractClassCreationOnStack() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   virtual void f() {}
	// };
	// void scope () {
	//   C* c = new C();  // No errors.
	// }
	public void testNotAbstractClassCreationWithNew() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   virtual void f() {}
	// };
	// void scope () {
	//   C::C();  // No errors.
	// }
	public void testNotAbstractClassCreationWithDirectConstructorCall() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   virtual void f() = 0;
	// };
	// void scope () {
	//   C* c1;  // No errors.
	//   C& c2;  // No errors.
	// }
	public void testAbstractClassPointerOrReverenceDeclaration() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   virtual void f() = 0;
	// };
	// typedef C typedefC;
	// void scope () {
	//   C c;         // 1 error for: C::f().
	//   typedefC tc;  // 1 error for: C::f().
	// }
	public void testAbstractClassCreationOnStack() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(6, 7);
	}

	// class C {
	//   virtual void f() = 0;
	// };
	// typedef C typedefC;
	// void scope () {
	//   C *c1, c2, &c3;            // 1 error for: C::f().
	//   typedefC *tc1, tc2, &tc3;  // 1 error for: C::f().
	// }
	public void testAbstractClassCreationOnStackWithRefAndPtr() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(6, 7);
	}

	// class C {
	//   virtual void f() = 0;
	// };
	// typedef C typedefC;
	// void test ( C _c ) {}                // 1 error for: C::f().
	// void test2 ( typedefC _c ) {}        // 1 error for: C::f().
	// void test3 ( C _c, typedefC _c ) {}  // 2 errors for: C::f(), C::f().
	// void test4 ( C ) {}                  // 1 error for: C::f().
	// void test5 ( C* _c ) {}              // No errors.
	// void test6 ( typedefC& _c ) {}       // No errors.
	public void testAbstractClassCreationAsFunctionParameter() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(5, 6, 7, 7, 8);
	}

	// class C {
	//   virtual void f() = 0;
	// };
	// template <typename C>  // No errors.
	// void test () {}
	public void testAbstractClassCreationAsFunctionTemplateParameter() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   virtual void f() = 0;
	// };
	// typedef C typedefC;
	// void scope () {
	//   C* c1 = new C();                  // 1 error for: C::f().
	//   C* c2 = new C[10];                // 1 error for: C::f().
	//   C* c3 = new typedefC();           // 1 error for: C::f().
	//   C* c4 = new typedefC;             // 1 error for: C::f().
	//   C* c5 (new C());                  // 1 error for: C::f().
	//   C* c6 (new typedefC());           // 1 error for: C::f().
	//   C* c7 = new typedefC[10];         // 1 error for: C::f().
	//   C** x1 = new C*();                // No errors.
	//   typedefC** x2 = new typedefC*();  // No errors.
	// }
	public void testAbstractClassCreationWithNew() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(6, 7, 8, 9, 10, 11, 12);
	}

	// class C {
	//   virtual void f() = 0;
	// };
	// typedef C typedefC;
	// void scope () {
	//   C::C();         // 1 error for: C::f().
	//   typedefC::C();  // 1 error for: C::f().
	// }
	public void testAbstractClassCreationWithDirectConstructorCall() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(6, 7);
	}

	// namespace N {
	//   class C {
	//     virtual void f() = 0;
	//   };
	// }
	// void scope () {
	//   N::C* c = new N::C();  // 1 error for: N::C::f().
	// }
	public void testAbstractClassFromNamespace() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(7);
	}

	// class C {
	//   virtual void f() = 0;
	//   virtual int g() const = 0;
	// };
	// void scope () {
	//   C* c = new C();  // 2 errors for: C::f(), C::g().
	// }
	public void testAbstractClassWithAFewVirtualMethods() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(6, 6);
	}

	// class Base {
	//   virtual void f() = 0;
	// };
	// class Derived : public Base {
	// };
	// void scope () {
	//   Derived* d = new Derived();  // 1 error for: Base::f().
	// }
	public void testAbstractClassBecauseOfBaseClass() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(7);
	}

	// class Base {
	//   virtual void f() = 0;
	//   virtual int g() const = 0;
	// };
	// class Derived : public Base {
	//   virtual int g() const = 0;
	// };
	// void scope () {
	//   Derived* c = new Derived();  // 2 errors for: Base::f(), Derived::g().
	// }
	public void testAbstractClassWithVirtualRedefinition() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(9, 9);
	}

	// class C {
	//   virtual void f() = 0;
	// };
	// typedef C typedefC;  // No errors.
	public void testAbstractClassTypedef() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   virtual void f() = 0;
	// };
	// extern C typedefC;  // 1 error for: C::f().
	public void testExternAbstractClassDeclaration() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// class A {
	// public:
	//   virtual ~A() = 0;
	// };
	//
	// class B : public A {
	// public:
	//   virtual ~B() {}
	// };
	//
	// B b;
	public void testPureVirtualDestructorOverride_1() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class A {
	// public:
	//   virtual ~A() = 0;
	// };
	//
	// class B : public A {
	// };
	//
	// B b;
	public void testPureVirtualDestructorOverride_2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	struct MyInterface {
	//	    virtual void doIt() = 0;
	//	};
	//
	//	struct Empty: virtual public MyInterface {};
	//
	//	struct Implementer: virtual public MyInterface {
	//	    virtual void doIt();
	//	};
	//
	//	struct Multiple: public Empty, public Implementer {};
	//
	//	static Multiple sharedMultiple;
	public void testDiamondInheritanceWithOneImplementor_bug351612a() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	struct MyInterface {
	//	    virtual void doIt() = 0;
	//	};
	//
	//	struct Empty: virtual public MyInterface {};
	//
	//	struct Implementer: public MyInterface {
	//	    virtual void doIt();
	//	};
	//
	//	struct Multiple: public Empty, public Implementer {};
	//
	//	static Multiple sharedMultiple;
	public void testDiamondInheritanceWithOneImplementor_bug351612b() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(13);
	}

	//	struct MyInterface {
	//	    virtual void doIt() = 0;
	//	};
	//
	//	struct Empty: public MyInterface {};
	//
	//	struct Implementer: virtual public MyInterface {
	//	    virtual void doIt();
	//	};
	//
	//	struct Multiple: public Empty, public Implementer {};
	//
	//	static Multiple sharedMultiple;
	public void testDiamondInheritanceWithOneImplementor_bug351612c() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(13);
	}

	//	struct MyInterface {
	//	    virtual void doIt() = 0;
	//	};
	//
	//	struct Empty: virtual public MyInterface {};
	//
	//	struct Implementer: virtual public MyInterface {
	//	    virtual void doIt();
	//	};
	//
	//	struct Multiple: virtual public Implementer, virtual public Empty {};
	//
	//	static Multiple sharedMultiple;
	public void testDiamondInheritanceWithOneImplementor_bug419938() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	struct A {
	//	    virtual void test(int) = 0;
	//	    virtual ~A();
	//	};
	//
	//	struct B : public A {
	//	    using A::test;
	//	    void test(const char*);
	//	    void test(int);
	//	};
	//
	//	int main() {
	//	    B c;
	//	}
	public void testUsingDeclarationInDerivedClass_bug414279() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	struct N {
	//	    int node;
	//	};
	//
	//	template <typename T>
	//	struct List {
	//	    template <int T::*>
	//	    struct Base {};
	//	};
	//
	//	List<N>::Base<&N::node> base;
	public void testUnsafeMethodCallException_bug416284() throws Exception {
		// Just check that codan runs without any exceptions being thrown.
		loadCodeAndRun(getAboveComment());
	}

	//	template <int I>
	//	struct S : S<I - 1> {};
	//
	//	S<1> waldo;
	public void testMaxInstantiationDepth_430282() throws Exception {
		// Just check that codan runs without any exceptions being thrown.
		loadCodeAndRun(getAboveComment());
	}
}
