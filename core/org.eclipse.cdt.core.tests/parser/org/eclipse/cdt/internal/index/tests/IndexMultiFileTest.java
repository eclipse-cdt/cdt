/*******************************************************************************
 * Copyright (c) 2013, 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
}
