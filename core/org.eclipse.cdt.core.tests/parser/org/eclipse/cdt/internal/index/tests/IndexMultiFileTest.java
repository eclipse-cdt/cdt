/*******************************************************************************
 * Copyright (c) 2013, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import junit.framework.TestSuite;

/**
 * Index tests involving multiple header and source files.
 *
 * The first line of each comment section preceding a test contains the name of the file
 * to put the contents of the section to. To request the AST of a file, put an asterisk after
 * the file name.
 */
public class IndexMultiFileTest extends IndexBindingResolutionTestBase {

	public IndexMultiFileTest() {
		setStrategy(new SinglePDOMTestNamedFilesStrategy(true));
	}

	public static TestSuite suite() {
		return suite(IndexMultiFileTest.class);
	}

	// A.h
	//	template <typename T>
	//	struct A {
	//	  void m(T p);
	//	};

	// B.h
	//	struct B {};

	// confuser.cpp
	//	#include "A.h"
	//
	//	namespace {
	//	struct B {};
	//	}
	//	A<B*> z;

	// test.cpp *
	//	#include "A.h"
	//	#include "B.h"
	//
	//	void test(A<B*> a, B* b) {
	//	  a.m(b);
	//	}
	public void testAnonymousNamespace_416278() throws Exception {
		checkBindings();
	}

	// a.h
	//	namespace ns1 {
	//	}
	//
	//	namespace ns2 {
	//	namespace ns3 {
	//	namespace waldo = ns1;
	//	}
	//	}

	// a.cpp
	//	#include "a.h"

	// b.h
	//	namespace ns1 {
	//	}
	//
	//	namespace ns2 {
	//	namespace waldo = ns1;
	//	}

	// b.cpp
	//	#include "b.h"
	//
	//	namespace ns2 {
	//	namespace waldo = ::ns1;
	//	}

	// c.cpp
	//	namespace ns1 {
	//	class A {};
	//	}
	//
	//	namespace waldo = ns1;
	//
	//	namespace ns2 {
	//	namespace ns3 {
	//	waldo::A a;
	//	}
	//	}
	public void testNamespaceAlias_442117() throws Exception {
		checkBindings();
	}

	// confuser.cpp
	//	namespace ns1 {
	//	namespace waldo {}
	//	namespace ns2 {
	//	namespace waldo {}
	//	}
	//	}

	// test.cpp *
	//	namespace waldo {
	//	class A {};
	//	}
	//
	//	namespace ns1 {
	//	namespace ns2 {
	//
	//	waldo::A* x;
	//
	//	}
	//	}
	public void testNamespace_481161() throws Exception {
		checkBindings();
	}

	// A.h
	//	#include "B.h"
	//
	//	template <typename T, typename U>
	//	struct A {
	//	  A(D<U> p);
	//	};
	//
	//	template <>
	//	struct A<B, C> {
	//	  A(D<C> p);
	//	};

	// B.h
	//	struct B {};
	//
	//	struct C {};
	//
	//	template <typename T>
	//	struct D {};

	// C.h
	//	class B;

	// confuser.cpp
	//	#include "B.h"

	// test.cpp *
	//	#include "A.h"
	//	#include "C.h"
	//
	//	void test() {
	//	  D<C> x;
	//	  new A<B, C>(x);
	//	}
	public void testExplicitSpecialization_494359() throws Exception {
		checkBindings();
	}

	// test1.h
	//	namespace ns {
	//
	//	struct C {
	//	  friend class B;
	//	};
	//
	//	}

	// test2.h
	//	class B {};
	//
	//	namespace ns {
	//
	//	struct A {
	//	  operator B();
	//	};
	//
	//	}
	//
	//	void waldo(B);

	// confuser.cpp
	//	#include "test1.h"

	// test.cpp *
	//	#include "test1.h"
	//	#include "test2.h"
	//
	//	void test(ns::A a) {
	//	  waldo(a);
	//	}
	public void testFriendClassDeclaration_508338() throws Exception {
		checkBindings();
	}

	// test.h
	//	friend int operator*(double, C) { return 0; }

	// test.cpp *
	//	namespace N {
	//
	//	    struct unrelated {};
	//
	//	    struct B {
	//	        friend int operator*(unrelated, unrelated) { return 0; }
	//	    };
	//	}
	//
	//	template <typename = int>
	//	struct C : public N::B {
	//	    #include "test.h"
	//	};
	//	template <typename> struct Waldo;
	//	Waldo<decltype(0.5 * C<>{})> w;
	public void testFriendFunctionInHeaderIncludedAtClassScope_509662() throws Exception {
		checkBindings();
	}

	// test.h
	//	template <typename T>
	//	struct atomic;
	//
	//	template <typename T>
	//	struct atomic<T*>;

	// test1.cpp
	//	#include "test.h"

	// test2.cpp *
	//	#include "test.h"
	//
	//	template <typename T>
	//	struct atomic {};
	//
	//	template <typename T>
	//	struct atomic<T*> {
	//		void fetch_sub();
	//	};
	public void testClassTemplatePartialSpecialization_470726() throws Exception {
		checkBindings();
	}

	// test.h
	//	template <bool = false>
	//	struct base {};
	//
	//	template <bool B = false>
	//	struct derived : private base<B> {
	//	    constexpr derived() : base<B>() {}
	//	};

	// test1.cpp
	//	#include "test.h"

	// test2.cpp *
	//	template <typename = void>
	//	struct base {};
	//
	//	static derived<> waldo;
	public void testProblemBindingInMemInitList_508254() throws Exception {
		// This code is invalid, so we don't checkBindings().
		// If the test gets this far (doesn't throw in setup() during indexing), it passes.
	}

	// test.h
	//	namespace ns {
	//
	//	template <typename T>
	//	class A {
	//	  friend void waldo(A<int> p);
	//	};
	//
	//	void waldo(A<int> p);
	//
	//	}

	// test.cpp *
	//	#include "test.h"
	//
	//	ns::A<int> b;
	//
	//	void test() {
	//	  ns::waldo(b);
	//	}

	// z.cpp
	//	#include "test.h"
	//
	//	ns::A<int> a;
	//
	//	void func() {
	//	  waldo(a);
	//	}
	public void testFriendFunctionDeclarationInNamespace_513681() throws Exception {
		checkBindings();
	}

	// test.hpp
	// #ifndef test_hpp
	// #define test_hpp
	//
	// struct S {
	//   const S* x;
	// };
	//
	// struct Base {
	//   static const S field;
	// };
	// struct Conjugate {
	//   static const S field;
	// };
	//
	// #endif

	//test1.cpp
	//# include "test.hpp"
	//
	// const S Base::field = {
	//   &Conjugate::field
	// };
	//
	// const S Conjugate::field = {
	//   &Base::field
	// };

	//test2.cpp
	// #include "test.hpp"
	//
	// struct Waldo {
	//   static const S s;
	// };
	//
	// const S Waldo::s = {
	//   &Base::field
	// };
	public void testStackOverflow_514459() throws Exception {
		checkBindings();
	}

	//test.hpp *
	//	template <typename> class A {};
	//
	//	struct C {
	//	    C();
	//	};
	//
	//	namespace Ptr2 {
	//		using C = A<C>;
	//	}

	//test.cpp
	//	#include "test.hpp"
	//	C::C() {}
	public void testAliasTemplateReferencingSameName_518937() throws Exception {
		checkBindings();
	}

	//h1.h
	//	class A {
	//	    friend class B1;
	//	};

	//s1.cpp
	//	#include "h1.h"

	//h2.h
	//	class B1 {};
	//	class B2 {};

	//s2.cpp *
	//	#include "h2.h"
	//	B1 b1;
	//	B2 b2;
	public void testClassFirstDeclaredAsFriend_530430() throws Exception {
		checkBindings();
	}
}
