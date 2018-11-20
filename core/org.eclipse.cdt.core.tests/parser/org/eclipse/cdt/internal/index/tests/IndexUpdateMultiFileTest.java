/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
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
public class IndexUpdateMultiFileTest extends IndexBindingResolutionTestBase {

	public IndexUpdateMultiFileTest() {
		setStrategy(new SinglePDOMTestNamedFilesStrategy(true));
	}

	public static TestSuite suite() {
		return suite(IndexUpdateMultiFileTest.class);
	}

	// A.h
	//	#if !defined(MACRO2)
	//	#define MACRO1
	//	#endif

	// B.h
	//	template <class T>
	//	struct A {
	//	};
	//
	//	template <class U>
	//	struct B : public A<typename U::t> {
	//	};
	//
	//	template <typename T>
	//	struct C {
	//	  typedef T t;
	//	  void waldo(A<t>* p);
	//	};

	// test.cpp
	//	#include "A.h"
	//	#include "B.h"
	//
	//	struct E : public C<int> {
	//	  void test() {
	//	    waldo(new B<E>());
	//	  }
	//	};

	// test.cpp *
	//	//#include "A.h"
	//	#include "B.h"
	//
	//	struct E : public C<int> {
	//	  void test() {
	//	    waldo(new B<E>());
	//	  }
	//	};
	public void testMacroRemoval_450888() throws Exception {
		checkBindings();
	}
}
