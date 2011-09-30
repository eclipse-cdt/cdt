/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
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

import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;

/**
 * Tests for header files included in multiple variants.
 */
public class IndexMultiVariantHeaderTest extends IndexBindingResolutionTestBase {

	public IndexMultiVariantHeaderTest() {
		setStrategy(new SinglePDOMTestNamedFilesStrategy(true));
	}

	public static TestSuite suite() {
		return suite(IndexMultiVariantHeaderTest.class);
	}
	
	// test.h
	//	#ifdef func
	//	#undef func
	//	#endif
	//
	//	#define func(x) foo ## x

	// test.c *
	//	#include "test.h"
	//
	//	void func(1)() {}
	//
	//	#undef func
	//	#define func(x) bar ## x
	//
	//	void func(2)() {}
	//
	//	#include "test.h"
	//	void func(3)() {}
	public void testExampleFromBug197989_Comment0() throws Exception {
		IFunction f1 = getBindingFromASTName("func(1)", 7, IFunction.class);
		assertEquals("foo1", f1.getName());
		IFunction f2 = getBindingFromASTName("func(2)", 7, IFunction.class);
		assertEquals("bar2", f2.getName());
		IFunction f3 = getBindingFromASTName("func(3)", 7, IFunction.class);
		assertEquals("foo3", f3.getName());
	}

	// stddef.h
	//	#if !defined(_STDDEF_H) || defined(__need_NULL)
	//
	//	#if !defined(__need_NULL)
	//	#define _STDDEF_H
	//	#endif /* !defined(__need_NULL) */
	//
	//	#if defined(_STDDEF_H) || defined(__need_NULL)
	//	#define NULL 0
	//	#endif /* defined(_STDDEF_H) || defined(__need_NULL)  */
	//	#undef __need_NULL
	//
	//	#if defined(_STDDEF_H)
	//	typedef unsigned int size_t;
	//	#endif /* defined(_STDDEF_H) */
	//
	//	#endif /* !defined(_STDDEF_H) || defined(__need_NULL) */

	// a.h
	//  #ifndef A_H_
	//  #define A_H_
	//	#include "stddef.h"
	//	#endif /* A_H_ */

	// a.cpp *
	//	#define __need_NULL
	//	#include "stddef.h"
	//	void f1(char* p) {}
	//	void test() {
	//	  f1(NULL);
	//	}

	// b.cpp
	//	#include "stddef.h"
	//	#include "a.h"

	// c.cpp *
	//	#include "a.h"
	//	void f2(char* p, size_t t) {}
	//	void test() {
	//	  f2(NULL, 1);
	//	}
	public void testExampleFromBug197989_Comment73() throws Exception {
		getBindingFromASTName("f1(NULL)", 2, ICPPFunction.class);
		getBindingFromASTName("f2(NULL, 1)", 2, ICPPFunction.class);
	}
}
