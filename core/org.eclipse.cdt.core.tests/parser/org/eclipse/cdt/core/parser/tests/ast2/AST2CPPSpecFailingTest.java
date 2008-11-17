/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Devin Steffler (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * NOTE:  Once these tests pass (are fixed) then fix the test to work so that they
 * are tested for a pass instead of a failure and move them to AST2CPPSpecTest.java.
 */
public class AST2CPPSpecFailingTest extends AST2SpecBaseTest {

	public AST2CPPSpecFailingTest() {
	}

	public AST2CPPSpecFailingTest(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(AST2CPPSpecFailingTest.class);
	}
	
	public void testDummy() {} // avoids JUnit "no tests" warning
	
	// struct B {
	// virtual void f(int);
	// virtual void f(char);
	// void g(int);
	// void h(int);
	// };
	// struct D : B {
	// using B::f;
	// void f(int); // OK: D::f(int) overrides B::f(int);
	// using B::g;
	// void g(char); // OK
	// using B::h;
	// void h(int); // OK: D::h(int) hides B::h(int)
	// };
	// void k(D* p)
	// {
	// p->f(1); //calls D::f(int)
	// p->f('a'); //calls B::f(char)
	// p->g(1); //calls B::g(int)
	// p->g('a'); //calls D::g(char)
	// }
	public void _test7_3_3s12() throws Exception { // raised bug 161562 for that
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class task;
	// template<class T> task<T>* preempt(task<T>*);
	// template<class T> class task {
	// // ...
	// friend void next_time();
	// friend void process(task<T>*);
	// friend task<T>* preempt<T>(task<T>*);
	// template<class C> friend int func(C);
	// friend class task<int>;
	// template<class P> friend class frd;
	// // ...
	// };
	public void _test14_5_3s1() throws Exception { // TODO raised bug 90678
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <int I> class A;
	// template <int I, int J> A<I+J> f/*1*/(A<I>, A<J>); // #1
	// template <int K, int L> A<K+L> f/*2*/(A<K>, A<L>); // same as #1
	// template <int I, int J> A<I-J> f/*3*/(A<I>, A<J>); // different from #1
	public void _test14_5_5_1s5() throws Exception { 
		final String content= getAboveComment();
		IASTTranslationUnit tu= parse(content, ParserLanguage.CPP, true, 0);
		BindingAssertionHelper bh= new BindingAssertionHelper(content, true);
		ICPPFunctionTemplate f1= bh.assertNonProblem("f/*1*/", 1);
		ICPPFunctionTemplate f2= bh.assertNonProblem("f/*2*/", 1);
		ICPPFunctionTemplate f3= bh.assertNonProblem("f/*3*/", 1);
		assertSame(f1, f2);
		assertNotSame(f1, f3);
	}
	
	// template <class T> void f(T t);
	// template <class X> void g(const X x);
	// template <class Z> void h(Z, Z*);
	// int main()
	// {
	// // #1: function type is f(int), t is nonconst
	// f<int>(1);
	// // #2: function type is f(int), t is const
	// f<const int>(1);
	// // #3: function type is g(int), x is const
	// g<int>(1);
	// // #4: function type is g(int), x is const
	// g<const int>(1);
	// // #5: function type is h(int, const int*)
	// h<const int>(1,0);
	// }
	public void _test14_8_2s3() throws Exception {
		// mstodo this one must pass
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <class T> struct B { };
	// template <class T> struct D : public B<T> {};
	// struct D2 : public B<int> {};
	// template <class T> void f(B<T>&){}
	// void t()
	// {
	// D<int> d;
	// D2 d2;
	// f(d); //calls f(B<int>&)
	// f(d2); //calls f(B<int>&)
	// }
	public void _test14_8_2_4s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> void f(T);
	// class Complex {
	// // ...
	// Complex(double);
	// };
	// void g()
	// {
	// f<Complex>(1); // OK, means f<Complex>(Complex(1))
	// }
	public void _test14_8_1s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<int i, typename T>
	// T deduce(typename A<T>::X x, // T is not deduced here
	// T t, // but T is deduced here
	// typename B<i>::Y y); // i is not deduced here
	// A<int> a;
	// B<77> b;
	// int x = deduce<77>(a.xm, 62, y.ym);
	// // T is deduced to be int, a.xm must be convertible to
	// // A<int>::X
	// // i is explicitly specified to be 77, y.ym must be convertible
	// // to B<77>::Y
	public void _test14_8_2_4s14() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> void f(T*,int); // #1
	// template<class T> void f(T,char); // #2
	// void h(int* pi, int i, char c)
	// {
	// f(pi,i); //#1: f<int>(pi,i)
	// f(pi,c); //#2: f<int*>(pi,c)
	// f(i,c); //#2: f<int>(i,c);
	// f(i,i); //#2: f<int>(i,char(i))
	// }
	public void _test14_8_3s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}
}
