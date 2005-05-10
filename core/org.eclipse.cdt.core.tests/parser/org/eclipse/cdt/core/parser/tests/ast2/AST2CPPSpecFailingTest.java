/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 *
 * NOTE:  Once these tests pass (are fixed) then fix the test to work so that they
 * are tested for a pass instead of a failure and move them to AST2CPPSpecTest.java.
 * 
 * @author dsteffle
 */
public class AST2CPPSpecFailingTest extends AST2SpecBaseTest {

	/**
	 [--Start Example(CPP 2.3-2):
	 ??=define arraycheck(a,b) a??(b??) ??!??! b??(a??)
	 // becomes
	 #define arraycheck(a,b) a[b] || b[a]
	 --End Example]
	 */
	public void test2_3s2()  { // TODO exists bug 64993
		StringBuffer buffer = new StringBuffer();
		buffer.append("??=define arraycheck(a,b) a??(b??) ??!??! b??(a??)\n"); //$NON-NLS-1$
		buffer.append("// becomes\n"); //$NON-NLS-1$
		buffer.append("#define arraycheck(a,b) a[b] || b[a]\n"); //$NON-NLS-1$
		
		try {
		parseCandCPP(buffer.toString(), true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 3.2-5):
	// translation unit 1:
	struct X {
	X(int);
	X(int, int);
	};
	X::X(int = 0) { }
	class D: public X { };
	D d2; // X(int) called by D()
	// translation unit 2:
	struct X {
	X(int);
	X(int, int);
	};
	X::X(int = 0, int = 0) { }
	class D: public X { }; // X(int, int) called by D();
	// D()’s implicit definition
	// violates the ODR
	 --End Example]
	 */
	public void test3_2s5()  { // TODO raised bug 90602 
		StringBuffer buffer = new StringBuffer();
		buffer.append("// translation unit 1:\n"); //$NON-NLS-1$
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("X(int);\n"); //$NON-NLS-1$
		buffer.append("X(int, int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("X::X(int = 0) { }\n"); //$NON-NLS-1$
		buffer.append("class D: public X { };\n"); //$NON-NLS-1$
		buffer.append("D d2; // X(int) called by D()\n"); //$NON-NLS-1$
		buffer.append("// translation unit 2:\n"); //$NON-NLS-1$
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("X(int);\n"); //$NON-NLS-1$
		buffer.append("X(int, int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("X::X(int = 0, int = 0) { }\n"); //$NON-NLS-1$
		buffer.append("class D: public X { }; // X(int, int) called by D();\n"); //$NON-NLS-1$
		buffer.append("// D()’s implicit definition\n"); //$NON-NLS-1$
		buffer.append("// violates the ODR\n"); //$NON-NLS-1$
		try{
			parse(buffer.toString(), ParserLanguage.CPP, true, 0);
			assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 3.4.1-10):
	struct A {
	typedef int AT;
	void f1(AT);
	void f2(float);
	};
	struct B {
	typedef float BT;
	friend void A::f1(AT); // parameter type is A::AT
	friend void A::f2(BT); // parameter type is B::BT
	};
	 --End Example]
	 */
	public void test3_4_1s10()  { // TODO raised bug 90609
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("typedef int AT;\n"); //$NON-NLS-1$
		buffer.append("void f1(AT);\n"); //$NON-NLS-1$
		buffer.append("void f2(float);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("typedef float BT;\n"); //$NON-NLS-1$
		buffer.append("friend void A::f1(AT); // parameter type is A::AT\n"); //$NON-NLS-1$
		buffer.append("friend void A::f2(BT); // parameter type is B::BT\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 3.4.3-3):
	class X { };
	class C {
	class X { };
	static const int number = 50;
	static X arr[number];
	};
	X C::arr[number]; // illformed:
	// equivalent to: ::X C::arr[C::number];
	// not to: C::X C::arr[C::number];
	 --End Example]
	 */
	public void test3_4_3s3()  { // TODO raised bug 90610
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X { };\n"); //$NON-NLS-1$
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("class X { };\n"); //$NON-NLS-1$
		buffer.append("static const int number = 50;\n"); //$NON-NLS-1$
		buffer.append("static X arr[number];\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("X C::arr[number]; // illformed:\n"); //$NON-NLS-1$
		buffer.append("// equivalent to: ::X C::arr[C::number];\n"); //$NON-NLS-1$
		buffer.append("// not to: C::X C::arr[C::number];\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 6.4-3):
	int foo() {
	if (int x = f()) {
	int x; // illformed,redeclaration of x
	}
	else {
	int x; // illformed,redeclaration of x
	}
	}
	 --End Example]
	 */
	public void test6_4s3()  { // TODO raised bug 90618
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("if (int x = f()) {\n"); //$NON-NLS-1$
		buffer.append("int x; // illformed,redeclaration of x\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("else {\n"); //$NON-NLS-1$
		buffer.append("int x; // illformed,redeclaration of x\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0); //Andrew, there should be problem bindings here - 2
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 6.8-2):
	class T {
	// ...
	public:
	T();
	T(int);
	T(int, int);
	};
	T(a); //declaration
	T(*b)(); //declaration
	T(c)=7; //declaration
	T(d),e,f=3; //declaration
	extern int h;
	T(g)(h,2); //declaration
	 --End Example]
	 */
	public void test6_8s2()  { // TODO raised bug 90622
		StringBuffer buffer = new StringBuffer();
		buffer.append("class T {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("T();\n"); //$NON-NLS-1$
		buffer.append("T(int);\n"); //$NON-NLS-1$
		buffer.append("T(int, int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("T(a); //declaration\n"); //$NON-NLS-1$
		buffer.append("T(*b)(); //declaration\n"); //$NON-NLS-1$
		buffer.append("T(c)=7; //declaration\n"); //$NON-NLS-1$
		buffer.append("T(d),e,f=3; //declaration\n"); //$NON-NLS-1$
		buffer.append("extern int h;\n"); //$NON-NLS-1$
		buffer.append("T(g)(h,2); //declaration\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 8.2-3):
	// #include <cstddef>
	char *p;
	void *operator new(size_t, int);
	void foo() {
	const int x = 63;
	new (int(*p)) int; // newplacement expression
	new (int(*[x])); // new typeid
	}
	 --End Example]
	 */
	public void test8_2s3()  { // TODO raised bug 90640
		StringBuffer buffer = new StringBuffer();
		buffer.append("// #include <cstddef>\n"); //$NON-NLS-1$
		buffer.append("char *p;\n"); //$NON-NLS-1$
		buffer.append("void *operator new(size_t, int);\n"); //$NON-NLS-1$
		buffer.append("void foo() {\n"); //$NON-NLS-1$
		buffer.append("const int x = 63;\n"); //$NON-NLS-1$
		buffer.append("new (int(*p)) int; // newplacement expression\n"); //$NON-NLS-1$
		buffer.append("new (int(*[x])); // new typeid\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 8.2-4):
	template <class T>
	struct S {
	T *p;
	};
	S<int()> x; // typeid
	S<int(1)> y; // expression (illformed)
	 --End Example]
	 */
	public void test8_2s4()  { // TODO raised bug 90632
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T>\n"); //$NON-NLS-1$
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("T *p;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("S<int()> x; // typeid\n"); //$NON-NLS-1$
		buffer.append("S<int(1)> y; // expression (illformed)\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 8.2-7a):
	class C { };
	void f(int(C)) { } // void f(int (*fp)(C c)) { }
	// not: void f(int C);
	int g(C);
	void foo() {
	f(1); //error: cannot convert 1 to function pointer
	f(g); //OK
	}
	 --End Example]
	 */
	public void test8_2s7a()  { // TODO raised bug 90633
		StringBuffer buffer = new StringBuffer();
		buffer.append("class C { };\n"); //$NON-NLS-1$
		buffer.append("void f(int(C)) { } // void f(int (*fp)(C c)) { }\n"); //$NON-NLS-1$
		buffer.append("// not: void f(int C);\n"); //$NON-NLS-1$
		buffer.append("int g(C);\n"); //$NON-NLS-1$
		buffer.append("void foo() {\n"); //$NON-NLS-1$
		buffer.append("f(1); //error: cannot convert 1 to function pointer\n"); //$NON-NLS-1$
		buffer.append("f(g); //OK\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 8.5-2):
	int f(int);
	int a = 2;
	int b = f(a);
	int c(b);
	 --End Example]
	 */
	public void test8_5s2()  { // TODO raised bug 90641
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("int a = 2;\n"); //$NON-NLS-1$
		buffer.append("int b = f(a);\n"); //$NON-NLS-1$
		buffer.append("int c(b);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 8.5.2-1):
	char msg[] = "Syntax error on line %s\n";
	 --End Example]
	 */
	public void test8_5_2s1()  { // TODO raised bug 90647
		StringBuffer buffer = new StringBuffer();
		buffer.append("char msg[] = \"Syntax error on line %s\n\";\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}


	/**
	 [--Start Example(CPP 10.2-3b):
	struct U { static int i; };
	struct V : U { };
	struct W : U { using U::i; };
	struct X : V, W { void foo(); };
	void X::foo() {
	i; //finds U::i in two ways: as W::i and U::i in V
	// no ambiguity because U::i is static
	}
	 --End Example]
	 */
	public void test10_2s3b()  { // TODO raised bug 90652
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct U { static int i; };\n"); //$NON-NLS-1$
		buffer.append("struct V : U { };\n"); //$NON-NLS-1$
		buffer.append("struct W : U { using U::i; };\n"); //$NON-NLS-1$
		buffer.append("struct X : V, W { void foo(); };\n"); //$NON-NLS-1$
		buffer.append("void X::foo() {\n"); //$NON-NLS-1$
		buffer.append("i; //finds U::i in two ways: as W::i and U::i in V\n"); //$NON-NLS-1$
		buffer.append("// no ambiguity because U::i is static\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 11.3-2):
	class A {
	public:
	int z;
	int z1;
	};
	class B : public A {
	int a;
	public:
	int b, c;
	int bf();
	protected:
	int x;
	int y;
	};
	class D : private B {
	int d;
	public:
	B::c; //adjust access to B::c
	B::z; //adjust access to A::z
	A::z1; //adjust access to A::z1
	int e;
	int df();
	protected:
	B::x; //adjust access to B::x
	int g;
	};
	class X : public D {
	int xf();
	};
	int ef(D&);
	int ff(X&);
	 --End Example]
	 */
	public void test11_3s2() throws Exception { //bug 92793
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int z;\n"); //$NON-NLS-1$
		buffer.append("int z1;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class B : public A {\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int b, c;\n"); //$NON-NLS-1$
		buffer.append("int bf();\n"); //$NON-NLS-1$
		buffer.append("protected:\n"); //$NON-NLS-1$
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("int y;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D : private B {\n"); //$NON-NLS-1$
		buffer.append("int d;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("B::c; //adjust access to B::c\n"); //$NON-NLS-1$
		buffer.append("B::z; //adjust access to A::z\n"); //$NON-NLS-1$
		buffer.append("A::z1; //adjust access to A::z1\n"); //$NON-NLS-1$
		buffer.append("int e;\n"); //$NON-NLS-1$
		buffer.append("int df();\n"); //$NON-NLS-1$
		buffer.append("protected:\n"); //$NON-NLS-1$
		buffer.append("B::x; //adjust access to B::x\n"); //$NON-NLS-1$
		buffer.append("int g;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class X : public D {\n"); //$NON-NLS-1$
		buffer.append("int xf();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int ef(D&);\n"); //$NON-NLS-1$
		buffer.append("int ff(X&);\n"); //$NON-NLS-1$
		try {
			parse(buffer.toString(), ParserLanguage.CPP, true, 0);
			assertTrue(false);
		} catch (Exception e) {
		}
	}	

	/**
	 [--Start Example(CPP 14.3-2):
	template<class T> void f();
	template<int I> void f();
	void g()
	{
	f<int()>(); // int() is a typeid:call the first f()
	}
	 --End Example]
	 */
	public void test14_3s2()  { // TODO raised bug 90671
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f();\n"); //$NON-NLS-1$
		buffer.append("template<int I> void f();\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f<int()>(); // int() is a typeid:call the first f()\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}
	
	/**
	 [--Start Example(CPP 14.5.3-1):
	template<class T> class task;
	template<class T> task<T>* preempt(task<T>*);
	template<class T> class task {
	// ...
	friend void next_time();
	friend void process(task<T>*);
	friend task<T>* preempt<T>(task<T>*);
	template<class C> friend int func(C);
	friend class task<int>;
	template<class P> friend class frd;
	// ...
	};
	 --End Example]
	 */
	public void test14_5_3s1()  { // TODO raised bug 90678
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class task;\n"); //$NON-NLS-1$
		buffer.append("template<class T> task<T>* preempt(task<T>*);\n"); //$NON-NLS-1$
		buffer.append("template<class T> class task {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("friend void next_time();\n"); //$NON-NLS-1$
		buffer.append("friend void process(task<T>*);\n"); //$NON-NLS-1$
		buffer.append("friend task<T>* preempt<T>(task<T>*);\n"); //$NON-NLS-1$
		buffer.append("template<class C> friend int func(C);\n"); //$NON-NLS-1$
		buffer.append("friend class task<int>;\n"); //$NON-NLS-1$
		buffer.append("template<class P> friend class frd;\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.4.2-2):
	template<int I, int J, class T> class X { };
	template<int I, int J> class X<I, J, int> { }; // #1
	template<int I> class X<I, I, int> { }; // #2
	template<int I, int J> void f(X<I, J, int>); // #A
	template<int I> void f(X<I, I, int>); // #B
	 --End Example]
	 */
	public void test14_5_4_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<int I, int J, class T> class X { };\n"); //$NON-NLS-1$
		buffer.append("template<int I, int J>          class X<I, J, int> { }; // #1\n"); //$NON-NLS-1$
		buffer.append("template<int I>                 class X<I, I, int> { }; // #2\n"); //$NON-NLS-1$
		buffer.append("template<int I, int J> void f(X<I, J, int>); // #A\n"); //$NON-NLS-1$
		buffer.append("template<int I>        void f(X<I, I, int>); // #B\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 2);
	}

	/**
	 [--Start Example(CPP 14.5.5.1-5):
	template <int I, int J> A<I+J> f(A<I>, A<J>); // #1
	template <int K, int L> A<K+L> f(A<K>, A<L>); // same as #1
	template <int I, int J> A<IJ> f(A<I>, A<J>); // different from #1
	 --End Example]
	 */
	public void test14_5_5_1s5()  { // TODO raised bug 90683
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <int I, int J> A<I+J> f(A<I>, A<J>); // #1\n"); //$NON-NLS-1$
		buffer.append("template <int K, int L> A<K+L> f(A<K>, A<L>); // same as #1\n"); //$NON-NLS-1$
		buffer.append("template <int I, int J> A<IJ> f(A<I>, A<J>); // different from #1\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.5.1-6):
	template <int I, int J> void f(A<I+J>); // #1
	template <int K, int L> void f(A<K+L>); // same as #1
	 --End Example]
	 */
	public void test14_5_5_1s6()  { // TODO raised bug 90683
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <int I, int J> void f(A<I+J>); // #1\n"); //$NON-NLS-1$
		buffer.append("template <int K, int L> void f(A<K+L>); // same as #1\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.6.2-3):
	typedef double A;
	template<class T> B {
	typedef int A;
	};
	template<class T> struct X : B<T> {
	A a; // a has type double
	};
	 --End Example]
	 */
	public void test14_6_2s3()  { // TODO this doesn't compile via g++ ?
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef double A;\n"); //$NON-NLS-1$
		buffer.append("template<class T> B {\n"); //$NON-NLS-1$
		buffer.append("typedef int A;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> struct X : B<T> {\n"); //$NON-NLS-1$
		buffer.append("A a; // a has type double\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.2-2b):
	template <class T> int f(typename T::B*);
	int i = f<int>(0);
	 --End Example]
	 */
	public void test14_8_2s2b()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> int f(typename T::B*);\n"); //$NON-NLS-1$
		buffer.append("int i = f<int>(0);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 1);
		assertTrue(false);
		} catch (Exception e) {
		}
	}
	
	/**
	 [--Start Example(CPP 14.8.2-2c):
	template <class T> int f(typename T::B*);
	struct A {};
	struct C { int B; };
	int i = f<A>(0);
	int j = f<C>(0);
	 --End Example]
	 */
	public void test14_8_2s2c()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> int f(typename T::B*);\n"); //$NON-NLS-1$
		buffer.append("struct A {};\n"); //$NON-NLS-1$
		buffer.append("struct C { int B; };\n"); //$NON-NLS-1$
		buffer.append("int i = f<A>(0);\n"); //$NON-NLS-1$
		buffer.append("int j = f<C>(0);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 2);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.2-3):
	template <class T> void f(T t);
	template <class X> void g(const X x);
	template <class Z> void h(Z, Z*);
	int main()
	{
	// #1: function type is f(int), t is nonconst
	f<int>(1);
	// #2: function type is f(int), t is const
	f<const int>(1);
	// #3: function type is g(int), x is const
	g<int>(1);
	// #4: function type is g(int), x is const
	g<const int>(1);
	// #5: function type is h(int, const int*)
	h<const int>(1,0);
	}
	 --End Example]
	 */
	public void test14_8_2s3()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> void f(T t);\n"); //$NON-NLS-1$
		buffer.append("template <class X> void g(const X x);\n"); //$NON-NLS-1$
		buffer.append("template <class Z> void h(Z, Z*);\n"); //$NON-NLS-1$
		buffer.append("int main()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("// #1: function type is f(int), t is nonconst\n"); //$NON-NLS-1$
		buffer.append("f<int>(1);\n"); //$NON-NLS-1$
		buffer.append("// #2: function type is f(int), t is const\n"); //$NON-NLS-1$
		buffer.append("f<const int>(1);\n"); //$NON-NLS-1$
		buffer.append("// #3: function type is g(int), x is const\n"); //$NON-NLS-1$
		buffer.append("g<int>(1);\n"); //$NON-NLS-1$
		buffer.append("// #4: function type is g(int), x is const\n"); //$NON-NLS-1$
		buffer.append("g<const int>(1);\n"); //$NON-NLS-1$
		buffer.append("// #5: function type is h(int, const int*)\n"); //$NON-NLS-1$
		buffer.append("h<const int>(1,0);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.2.4-7):
	template<class T> void f(const T*) {}
	int *p;
	void s()
	{
	f(p); // f(const int *)
	}
	 --End Example]
	 */
	public void test14_8_2_4s7()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(const T*) {}\n"); //$NON-NLS-1$
		buffer.append("int *p;\n"); //$NON-NLS-1$
		buffer.append("void s()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(p); // f(const int *)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.2.4-8):
	template <class T> struct B { };
	template <class T> struct D : public B<T> {};
	struct D2 : public B<int> {};
	template <class T> void f(B<T>&){}
	void t()
	{
	D<int> d;
	D2 d2;
	f(d); //calls f(B<int>&)
	f(d2); //calls f(B<int>&)
	}
	 --End Example]
	 */
	public void test14_8_2_4s8()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> struct B { };\n"); //$NON-NLS-1$
		buffer.append("template <class T> struct D : public B<T> {};\n"); //$NON-NLS-1$
		buffer.append("struct D2 : public B<int> {};\n"); //$NON-NLS-1$
		buffer.append("template <class T> void f(B<T>&){}\n"); //$NON-NLS-1$
		buffer.append("void t()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("D<int> d;\n"); //$NON-NLS-1$
		buffer.append("D2 d2;\n"); //$NON-NLS-1$
		buffer.append("f(d); //calls f(B<int>&)\n"); //$NON-NLS-1$
		buffer.append("f(d2); //calls f(B<int>&)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.2.4-18):
	template <template X<class T> > struct A { };
	template <template X<class T> > void f(A<X>) { }
	template<class T> struct B { };
	int foo() {
	A<B> ab;
	f(ab); //calls f(A<B>)
	}
	 --End Example]
	 */
	public void test14_8_2_4s18()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <template X<class T> > struct A { };\n"); //$NON-NLS-1$
		buffer.append("template <template X<class T> > void f(A<X>) { }\n"); //$NON-NLS-1$
		buffer.append("template<class T> struct B { };\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("A<B> ab;\n"); //$NON-NLS-1$
		buffer.append("f(ab); //calls f(A<B>)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.3-4):
	template<class T> struct B {  };
	template<class T> struct D : public B<T> {  };
	template<class T> void f(B<T>&);
	void g(B<int>& bi, D<int>& di)
	{
	f(bi); // f(bi)
	f(di); // f( (B<int>&)di )
	}
	 --End Example]
	 */
	public void test14_8_3s4()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct B {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> struct D : public B<T> {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(B<T>&);\n"); //$NON-NLS-1$
		buffer.append("void g(B<int>& bi, D<int>& di)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(bi); // f(bi)\n"); //$NON-NLS-1$
		buffer.append("f(di); // f( (B<int>&)di )\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.5.1-8a):
	// Guaranteed to be the same
	template <int I> void f(A<I>, A<I+10>);
	template <int I> void f(A<I>, A<I+10>);
	// Guaranteed to be different
	template <int I> void f(A<I>, A<I+10>);
	template <int I> void f(A<I>, A<I+11>);
	 --End Example]
	 */
	public void test14_5_5_1s8a()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("// Guaranteed to be the same\n"); //$NON-NLS-1$
		buffer.append("template <int I> void f(A<I>, A<I+10>);\n"); //$NON-NLS-1$
		buffer.append("template <int I> void f(A<I>, A<I+10>);\n"); //$NON-NLS-1$
		buffer.append("// Guaranteed to be different\n"); //$NON-NLS-1$
		buffer.append("template <int I> void f(A<I>, A<I+10>);\n"); //$NON-NLS-1$
		buffer.append("template <int I> void f(A<I>, A<I+11>);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}
	
	/**
	 [--Start Example(CPP 14.5.5.1-8b):
	// Illformed, no diagnostic required
	template <int I> void f(A<I>, A<I+10>);
	template <int I> void f(A<I>, A<I+1+2+3+4>);
	 --End Example]
	 */
	public void test14_5_5_1s8b()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("// Illformed, no diagnostic required\n"); //$NON-NLS-1$
		buffer.append("template <int I> void f(A<I>, A<I+10>);\n"); //$NON-NLS-1$
		buffer.append("template <int I> void f(A<I>, A<I+1+2+3+4>);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.1-4):
	template<class T> void f(T);
	class Complex {
	// ...
	Complex(double);
	};
	void g()
	{
	f<Complex>(1); // OK, means f<Complex>(Complex(1))
	}
	 --End Example]
	 */
	public void test14_8_1s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T);\n"); //$NON-NLS-1$
		buffer.append("class Complex {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("Complex(double);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f<Complex>(1); // OK, means f<Complex>(Complex(1))\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 1);
	}
	
	/**
	 [--Start Example(CPP 14.8.2.4-14):
	template<int i, typename T>
	T deduce(typename A<T>::X x, // T is not deduced here
	T t, // but T is deduced here
	typename B<i>::Y y); // i is not deduced here
	A<int> a;
	B<77> b;
	int x = deduce<77>(a.xm, 62, y.ym);
	// T is deduced to be int, a.xm must be convertible to
	// A<int>::X
	// i is explicitly specified to be 77, y.ym must be convertible
	// to B<77>::Y
	 --End Example]
	 */
	public void test14_8_2_4s14()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<int i, typename T>\n"); //$NON-NLS-1$
		buffer.append("T deduce(typename A<T>::X x, // T is not deduced here\n"); //$NON-NLS-1$
		buffer.append("T t, // but T is deduced here\n"); //$NON-NLS-1$
		buffer.append("typename B<i>::Y y); // i is not deduced here\n"); //$NON-NLS-1$
		buffer.append("A<int> a;\n"); //$NON-NLS-1$
		buffer.append("B<77> b;\n"); //$NON-NLS-1$
		buffer.append("int x = deduce<77>(a.xm, 62, y.ym);\n"); //$NON-NLS-1$
		buffer.append("// T is deduced to be int, a.xm must be convertible to\n"); //$NON-NLS-1$
		buffer.append("// A<int>::X\n"); //$NON-NLS-1$
		buffer.append("// i is explicitly specified to be 77, y.ym must be convertible\n"); //$NON-NLS-1$
		buffer.append("// to B<77>::Y\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.3-5):
	template<class T> void f(T*,int); // #1
	template<class T> void f(T,char); // #2
	void h(int* pi, int i, char c)
	{
	f(pi,i); //#1: f<int>(pi,i)
	f(pi,c); //#2: f<int*>(pi,c)
	f(i,c); //#2: f<int>(i,c);
	f(i,i); //#2: f<int>(i,char(i))
	}
	 --End Example]
	 */
	public void test14_8_3s5()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T*,int); // #1\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T,char); // #2\n"); //$NON-NLS-1$
		buffer.append("void h(int* pi, int i, char c)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(pi,i); //#1: f<int>(pi,i)\n"); //$NON-NLS-1$
		buffer.append("f(pi,c); //#2: f<int*>(pi,c)\n"); //$NON-NLS-1$
		buffer.append("f(i,c); //#2: f<int>(i,c);\n"); //$NON-NLS-1$
		buffer.append("f(i,i); //#2: f<int>(i,char(i))\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}
    
    
}
