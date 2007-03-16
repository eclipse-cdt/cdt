/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * For testing PDOM binding resolution
 */
public class IndexBindingResolutionBugs extends IndexBindingResolutionTestBase {

	public static class SingleProject extends IndexBindingResolutionBugs {
		public SingleProject() {setStrategy(new SinglePDOMTestStrategy(true));}
	}
	public static class ProjectWithDepProj extends IndexBindingResolutionBugs {
		public ProjectWithDepProj() {setStrategy(new ReferencedProject(true));}
	}
	
	public static void addTests(TestSuite suite) {		
		suite.addTest(suite(SingleProject.class));
		suite.addTest(suite(ProjectWithDepProj.class));
	}
	
	public static TestSuite suite() {
		return suite(IndexBindingResolutionBugs.class);
	}
	
	public IndexBindingResolutionBugs() {
		setStrategy(new SinglePDOMTestStrategy(true));
	}
	
	// // header file
	//  class cl;
	//	typedef cl* t1;
	//  typedef t1 t2;
	
	//// referencing content
	//  void func(t2 a);
	//  void func(int b);
	//  void ref() {
	//     cl* a;
	//     func(a);
	//  }
	public void testBug166954() {
		IBinding b0 = getBindingFromASTName("func(a)", 4);
	}
	
	// // header
	//	class Base { 
	//  public: 
	//     void foo(int i);
	//     int  fooint();
	//     char* fooovr();
	//     char* fooovr(int a);
	//     char* fooovr(char x);
	//  };

	// // references
	// #include "header.h"
	// void Base::foo(int i) {}
	// int Base::fooint() {return 0;}
	// char* Base::fooovr() {return 0;}
	// char* Base::fooovr(int a) {return 0;}
	// char* Base::fooovr(char x) {return 0;}
	//
	// void refs() {
	//   Base b;
	//   b.foo(1);
	//   b.fooint();
	//   b.fooovr();
	//   b.fooovr(1);
	//   b.fooovr('a');
	// }
	public void test168020() {
		getBindingFromASTName("foo(int i)", 3);
		getBindingFromASTName("fooint()", 6);
		getBindingFromASTName("fooovr()", 6);
		getBindingFromASTName("fooovr(int", 6);
		getBindingFromASTName("fooovr(char", 6);

		getBindingFromASTName("foo(1)", 3);
		getBindingFromASTName("fooint();", 6);
		getBindingFromASTName("fooovr();", 6);
		getBindingFromASTName("fooovr(1", 6);
		getBindingFromASTName("fooovr('", 6);
	}

	
	// // header
	//	class Base { 
	//  public: 
	//     void foo(int i);
	//     int  foo2(int i);
	//  };
	//
	//  void func(int k);
	//  void func2(int i);

	// // references
	// #include "header.h"
	// void Base::foo(int i) {
	//   i=2;
	// }
	// void Base::foo2(int j) {
	//   j=2;
	// }
	// void func(int k) {
	//  k=2;
	// }
	// void func2(int l) {
	//  l=2;
	// }
	public void test168054() {
		getBindingFromASTName("i=2", 1);
		getBindingFromASTName("j=2", 1);
		getBindingFromASTName("k=2", 1);
		getBindingFromASTName("l=2", 1);
	}

}
