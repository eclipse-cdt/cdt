/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 *
 * NOTE:  Once these tests pass (are fixed) then fix the test to work so that they
 * are tested for a pass instead of a failure and move them to AST2CPPSpecTest.java.
 * 
 * @author dsteffle
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
	
	// int foo() {
	// if (int x = f()) {
	// int x; // illformed,redeclaration of x
	// }
	// else {
	// int x; // illformed,redeclaration of x
	// }
	// }
	public void _test6_4s3() throws Exception { // TODO raised bug 90618
		parse(getAboveComment(), ParserLanguage.CPP, true, 2);
	}

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

	// char msg[] = "Syntax error on line %s
	// ";
	public void _test8_5_2s1() throws Exception { // TODO raised bug 90647
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

	// template<int I, int J, class T> class X { };
	// template<int I, int J>          class X<I, J, int> { }; // #1
	// template<int I>                 class X<I, I, int> { }; // #2
	// template<int I, int J> void f(X<I, J, int>); // #A
	// template<int I>        void f(X<I, I, int>); // #B
	public void _test14_5_4_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <int I, int J> A<I+J> f(A<I>, A<J>); // #1
	// template <int K, int L> A<K+L> f(A<K>, A<L>); // same as #1
	// template <int I, int J> A<IJ> f(A<I>, A<J>); // different from #1
	public void _test14_5_5_1s5() throws Exception { // TODO raised bug 90683
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <int I, int J> void f(A<I+J>); // #1
	// template <int K, int L> void f(A<K+L>); // same as #1
	public void _test14_5_5_1s6() throws Exception { // TODO raised bug 90683
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef double A;
	// template<class T> B {
	// typedef int A;
	// };
	// template<class T> struct X : B<T> {
	// A a; // a has type double
	// };
	public void _test14_6_2s3() throws Exception { // TODO this doesn't compile via g++ ?
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <class T> int f(typename T::B*);
	// int i = f<int>(0);
	public void _test14_8_2s2b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 1);
	}

	// template <class T> int f(typename T::B*);
	// struct A {};
	// struct C { int B; };
	// int i = f<A>(0);
	// int j = f<C>(0);
	public void _test14_8_2s2c() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 2);
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

	// template <template X<class T> > struct A { };
	// template <template X<class T> > void f(A<X>) { }
	// template<class T> struct B { };
	// int foo() {
	// A<B> ab;
	// f(ab); //calls f(A<B>)
	// }
	public void _test14_8_2_4s18() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// // Guaranteed to be the same
	// template <int I> void f(A<I>, A<I+10>);
	// template <int I> void f(A<I>, A<I+10>);
	// // Guaranteed to be different
	// template <int I> void f(A<I>, A<I+10>);
	// template <int I> void f(A<I>, A<I+11>);
	public void _test14_5_5_1s8a() throws Exception {
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
