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
	public void test2_3s2()  { // TODO Devin exists bug 64993
		StringBuffer buffer = new StringBuffer();
		buffer.append("??=define arraycheck(a,b) a??(b??) ??!??! b??(a??)\n"); //$NON-NLS-1$
		buffer.append("// becomes\n"); //$NON-NLS-1$
		buffer.append("#define arraycheck(a,b) a[b] || b[a]\n"); //$NON-NLS-1$
		
		try {
		parseCandCPP(buffer.toString(), true, true);
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
	public void test3_2s5()  { // TODO Devin raised bug 90602 
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
			parse(buffer.toString(), ParserLanguage.CPP, true, true);
			assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 3.3.6-5):
	typedef int c;
	enum { i = 1 };
	class X {
	int i=3;
	char v[i];
	int f() { return sizeof(c); } // OK: X::c
	char c;
	enum { i = 2 };
	};
	typedef char* T;
	struct Y {
	typedef long T;
	T b;
	};
	typedef int I;
	class D {
	typedef I I; // error, even though no reordering involved
	};
	 --End Example]
	 */
	public void test3_3_6s5()  { // TODO Devin raised bug 90606
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int c;\n"); //$NON-NLS-1$
		buffer.append("enum { i = 1 };\n"); //$NON-NLS-1$
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("int i=3;\n"); //$NON-NLS-1$
		buffer.append("char v[i];\n"); //$NON-NLS-1$
		buffer.append("int f() { return sizeof(c); } // OK: X::c\n"); //$NON-NLS-1$
		buffer.append("char c;\n"); //$NON-NLS-1$
		buffer.append("enum { i = 2 };\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("typedef char* T;\n"); //$NON-NLS-1$
		buffer.append("struct Y {\n"); //$NON-NLS-1$
		buffer.append("typedef long T;\n"); //$NON-NLS-1$
		buffer.append("T b;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("typedef int I;\n"); //$NON-NLS-1$
		buffer.append("class D {\n"); //$NON-NLS-1$
		buffer.append("typedef I I; // error, even though no reordering involved\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, false, true);
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
	public void test3_4_1s10()  { // TODO Devin raised bug 90609
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
	public void test3_4_3s3()  { // TODO Devin raised bug 90610
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
		parse(buffer.toString(), ParserLanguage.CPP, false, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 3.4.3.2-3):
	namespace A {
	int a;
	}
	namespace B {
	using namespace A;
	}
	namespace C {
	using namespace A;
	}
	namespace BC {
	using namespace B;
	using namespace C;
	}
	void f()
	{
	BC::a++; //OK: S is { A::a, A::a }
	}
	namespace D {
	using A::a;
	}
	namespace BD {
	using namespace B;
	using namespace D;
	}
	void g()
	{
	BD::a++; //OK: S is { A::a, A::a }
	}
	 --End Example]
	 */
	public void test3_4_3_2s3()  { // TODO Devin raised bug 90611
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("using namespace A;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace C {\n"); //$NON-NLS-1$
		buffer.append("using namespace A;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace BC {\n"); //$NON-NLS-1$
		buffer.append("using namespace B;\n"); //$NON-NLS-1$
		buffer.append("using namespace C;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("BC::a++; //OK: S is { A::a, A::a }\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace D {\n"); //$NON-NLS-1$
		buffer.append("using A::a;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace BD {\n"); //$NON-NLS-1$
		buffer.append("using namespace B;\n"); //$NON-NLS-1$
		buffer.append("using namespace D;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("BD::a++; //OK: S is { A::a, A::a }\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
	public void test6_4s3()  { // TODO Devin raised bug 90618
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
		parse(buffer.toString(), ParserLanguage.CPP, false, true);
		assertTrue(false);
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
	public void test6_8s2()  { // TODO Devin raised bug 90622
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 7.1.3-2):
	typedef struct s { //
	} s;
	typedef int I;
	typedef int I;
	typedef I I;
	 --End Example]
	 */
	public void test7_1_3s2()  { // TODO Devin raised bug 90623
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef struct s { //\n"); //$NON-NLS-1$
		buffer.append("} s;\n"); //$NON-NLS-1$
		buffer.append("typedef int I;\n"); //$NON-NLS-1$
		buffer.append("typedef int I;\n"); //$NON-NLS-1$
		buffer.append("typedef I I;\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 7.3.3-10):
	namespace A {
	int x;
	}
	namespace B {
	int i;
	struct g { };
	struct x { };
	void f(int);
	void f(double);
	void g(char); // OK: hides struct g
	}
	void func()
	{
	int i;
	using B::i; // error: i declared twice
	void f(char);
	using B::f; // OK: each f is a function
	f(3.5); //calls B::f(double)
	using B::g;
	g('a'); //calls B::g(char)
	struct g g1; // g1 has class type B::g
	using B::x;
	using A::x; // OK: hides struct B::x
	x = 99; // assigns to A::x
	struct x x1; // x1 has class type B::x
	}
	 --End Example]
	 */
	public void test7_3_3s10()  { // TODO Devin raised bug 90626
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("struct g { };\n"); //$NON-NLS-1$
		buffer.append("struct x { };\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("void f(double);\n"); //$NON-NLS-1$
		buffer.append("void g(char); // OK: hides struct g\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void func()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("using B::i; // error: i declared twice\n"); //$NON-NLS-1$
		buffer.append("void f(char);\n"); //$NON-NLS-1$
		buffer.append("using B::f; // OK: each f is a function\n"); //$NON-NLS-1$
		buffer.append("f(3.5); //calls B::f(double)\n"); //$NON-NLS-1$
		buffer.append("using B::g;\n"); //$NON-NLS-1$
		buffer.append("g('a'); //calls B::g(char)\n"); //$NON-NLS-1$
		buffer.append("struct g g1; // g1 has class type B::g\n"); //$NON-NLS-1$
		buffer.append("using B::x;\n"); //$NON-NLS-1$
		buffer.append("using A::x; // OK: hides struct B::x\n"); //$NON-NLS-1$
		buffer.append("x = 99; // assigns to A::x\n"); //$NON-NLS-1$
		buffer.append("struct x x1; // x1 has class type B::x\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, false, true);
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
	public void test8_2s3()  { // TODO Devin raised bug 90640
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
		parse(buffer.toString(), ParserLanguage.CPP, true, false);
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
	public void test8_2s4()  { // TODO Devin raised bug 90632
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T>\n"); //$NON-NLS-1$
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("T *p;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("S<int()> x; // typeid\n"); //$NON-NLS-1$
		buffer.append("S<int(1)> y; // expression (illformed)\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, false, true);
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
	public void test8_2s7a()  { // TODO Devin raised bug 90633
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
		parse(buffer.toString(), ParserLanguage.CPP, false, true);
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
	public void test8_5s2()  { // TODO Devin raised bug 90641
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("int a = 2;\n"); //$NON-NLS-1$
		buffer.append("int b = f(a);\n"); //$NON-NLS-1$
		buffer.append("int c(b);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 8.5.2-1):
	char msg[] = "Syntax error on line %s\n";
	 --End Example]
	 */
	public void test8_5_2s1()  { // TODO Devin raised bug 90647
		StringBuffer buffer = new StringBuffer();
		buffer.append("char msg[] = \"Syntax error on line %s\n\";\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 8.5.3-1):
	int g(int);
	void f()
	{
	int i;
	int& r = i; // r refers to i
	r = 1; // the value of i becomes 1
	int* p = &r; // p points to i
	int& rr = r; // rr refers to what r refers to, that is, to i
	int (&rg)(int) = g; // rg refers to the function g
	rg(i); //calls function g
	int a[3];
	int (&ra)[3] = a; // ra refers to the array a
	ra[1] = i; // modifies a[1]
	}
	 --End Example]
	 */
	public void test8_5_3s1()  { // TODO Devin raised bug 90648
		StringBuffer buffer = new StringBuffer();
		buffer.append("int g(int);\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("int& r = i; // r refers to i\n"); //$NON-NLS-1$
		buffer.append("r = 1; // the value of i becomes 1\n"); //$NON-NLS-1$
		buffer.append("int* p = &r; // p points to i\n"); //$NON-NLS-1$
		buffer.append("int& rr = r; // rr refers to what r refers to, that is, to i\n"); //$NON-NLS-1$
		buffer.append("int (&rg)(int) = g; // rg refers to the function g\n"); //$NON-NLS-1$
		buffer.append("rg(i); //calls function g\n"); //$NON-NLS-1$
		buffer.append("int a[3];\n"); //$NON-NLS-1$
		buffer.append("int (&ra)[3] = a; // ra refers to the array a\n"); //$NON-NLS-1$
		buffer.append("ra[1] = i; // modifies a[1]\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 9.5-2):
	void f()
	{
	union { int a; char* p; };
	a = 1;
	// ...
	p = "Jennifer";
	// ...
	}
	 --End Example]
	 */
	public void test9_5s2()  { // TODO Devin raised bug 90650
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("union { int a; char* p; };\n"); //$NON-NLS-1$
		buffer.append("a = 1;\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("p = \"Jennifer\";\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
	public void test10_2s3b()  { // TODO Devin raised bug 90652
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 12-1):
	struct A { }; // implicitlydeclared A::operator=
	struct B : A {
	B& operator=(const B &);
	};
	B& B::operator=(const B& s) {
	this->A::operator=(s); // wellformed
	return *this;
	}
	 --End Example]
	 */
	public void test12s1()  { // TODO Devin raised bug 90653
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A { }; // implicitlydeclared A::operator=\n"); //$NON-NLS-1$
		buffer.append("struct B : A {\n"); //$NON-NLS-1$
		buffer.append("B& operator=(const B &);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("B& B::operator=(const B& s) {\n"); //$NON-NLS-1$
		buffer.append("this->A::operator=(s); // wellformed\n"); //$NON-NLS-1$
		buffer.append("return *this;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 12.3-4):
	class X {
	// ...
	public:
	operator int();
	};
	class Y {
	// ...
	public:
	operator X();
	};
	Y a;
	int b = a; // error:
	// a.operator X().operator int() not tried
	int c = X(a); // OK: a.operator X().operator int()
	 --End Example]
	 */
	public void test12_3s4()  { // TODO Devin raised bug 90654
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("operator int();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Y {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("operator X();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("Y a;\n"); //$NON-NLS-1$
		buffer.append("int b = a; // error:\n"); //$NON-NLS-1$
		buffer.append("// a.operator X().operator int() not tried\n"); //$NON-NLS-1$
		buffer.append("int c = X(a); // OK: a.operator X().operator int()\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, false, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 12.7-2):
	struct A { };
	struct B : virtual A { };
	struct C : B { };
	struct D : virtual A { D(A*); };
	struct X { X(A*); };
	struct E : C, D, X {
	E() : D(this), // undefined: upcast from E* to A*
	// might use path E* ® D* ® A*
	// but D is not constructed
	// D((C*)this), // defined:
	// E* -> C* defined because E() has started
	// and C* -> A* defined because
	// C fully constructed
	X(this) //defined: upon construction of X,
	// C/B/D/A sublattice is fully constructed
	{ }
	};
	 --End Example]
	 */
	public void test12_7s2()  { // TODO Devin raised bug 90664
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A { };\n"); //$NON-NLS-1$
		buffer.append("struct B : virtual A { };\n"); //$NON-NLS-1$
		buffer.append("struct C : B { };\n"); //$NON-NLS-1$
		buffer.append("struct D : virtual A { D(A*); };\n"); //$NON-NLS-1$
		buffer.append("struct X { X(A*); };\n"); //$NON-NLS-1$
		buffer.append("struct E : C, D, X {\n"); //$NON-NLS-1$
		buffer.append("E() : D(this), // undefined: upcast from E* to A*\n"); //$NON-NLS-1$
		buffer.append("// might use path E* ® D* ® A*\n"); //$NON-NLS-1$
		buffer.append("// but D is not constructed\n"); //$NON-NLS-1$
		buffer.append("// D((C*)this), // defined:\n"); //$NON-NLS-1$
		buffer.append("// E* ® C* defined because E() has started\n"); //$NON-NLS-1$
		buffer.append("// and C* ® A* defined because\n"); //$NON-NLS-1$
		buffer.append("// C fully constructed\n"); //$NON-NLS-1$
		buffer.append("X(this) //defined: upon construction of X,\n"); //$NON-NLS-1$
		buffer.append("// C/B/D/A sublattice is fully constructed\n"); //$NON-NLS-1$
		buffer.append("{ }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 12.8-3d):
	void h(int());
	void h(int (*)()); // redeclaration of h(int())
	void h(int x()) { } // definition of h(int())
	void h(int (*x)()) { } // illformed: redefinition of h(int())
	 --End Example]
	 */
	public void test12_8s3d()  { // TODO Devin raised bug 90666
		StringBuffer buffer = new StringBuffer();
		buffer.append("void h(int());\n"); //$NON-NLS-1$
		buffer.append("void h(int (*)()); // redeclaration of h(int())\n"); //$NON-NLS-1$
		buffer.append("void h(int x()) { } // definition of h(int())\n"); //$NON-NLS-1$
		buffer.append("void h(int (*x)()) { } // illformed: redefinition of h(int())\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, false, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 13.3.3.2-3c):
	struct A {
	operator short();
	} a;
	int f(int);
	int f(float);
	int i = f(a); // Calls f(int), because short ® int is
	// better than short ® float.
	 --End Example]
	 */
	public void test13_3_3_2s3c()  { // TODO Devin raised bug 90667
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("operator short();\n"); //$NON-NLS-1$
		buffer.append("} a;\n"); //$NON-NLS-1$
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("int f(float);\n"); //$NON-NLS-1$
		buffer.append("int i = f(a); // Calls f(int), because short ® int is\n"); //$NON-NLS-1$
		buffer.append("// better than short ® float.\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 13.4-5a):
	int f(double);
	int f(int);
	int (*pfd)(double) = &f; // selects f(double)
	int (*pfi)(int) = &f; // selects f(int)
	int (*pfe)(...) = &f; // error: type mismatch
	int (&rfi)(int) = f; // selects f(int)
	int (&rfd)(double) = f; // selects f(double)
	void g() {
	(int (*)(int))&f; // cast expression as selector
	}
	 --End Example]
	 */
	public void test13_4s5a()  { // TODO Devin raised bug 90674
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(double);\n"); //$NON-NLS-1$
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("int (*pfd)(double) = &f; // selects f(double)\n"); //$NON-NLS-1$
		buffer.append("int (*pfi)(int) = &f; // selects f(int)\n"); //$NON-NLS-1$
		buffer.append("int (*pfe)(...) = &f; // error: type mismatch\n"); //$NON-NLS-1$
		buffer.append("int (&rfi)(int) = f; // selects f(int)\n"); //$NON-NLS-1$
		buffer.append("int (&rfd)(double) = f; // selects f(double)\n"); //$NON-NLS-1$
		buffer.append("void g() {\n"); //$NON-NLS-1$
		buffer.append("(int (*)(int))&f; // cast expression as selector\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, false, false);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.1-8):
	template<int *a> struct R {  };
	template<int b[5]> struct S {  };
	int *p;
	R<p> w; // OK
	S<p> x; // OK due to parameter adjustment
	int v[5];
	R<v> y; // OK due to implicit argument conversion
	S<v> z; // OK due to both adjustment and conversion
	 --End Example]
	 */
	public void test14_1s8()  { // TODO Devin raised bug 90668
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<int *a> struct R {  };\n"); //$NON-NLS-1$
		buffer.append("template<int b[5]> struct S {  };\n"); //$NON-NLS-1$
		buffer.append("int *p;\n"); //$NON-NLS-1$
		buffer.append("R<p> w; // OK\n"); //$NON-NLS-1$
		buffer.append("S<p> x; // OK due to parameter adjustment\n"); //$NON-NLS-1$
		buffer.append("int v[5];\n"); //$NON-NLS-1$
		buffer.append("R<v> y; // OK due to implicit argument conversion\n"); //$NON-NLS-1$
		buffer.append("S<v> z; // OK due to both adjustment and conversion\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.1-13):
	template<class T, T* p, class U = T> class X {  };
	template<class T> void f(T* p = new T);
	 --End Example]
	 */
	public void test14_1s13()  { // TODO Devin raised bug 60670
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T, T* p, class U = T> class X {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T* p = new T);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
	public void test14_3s2()  { // TODO Devin raised bug 90671
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f();\n"); //$NON-NLS-1$
		buffer.append("template<int I> void f();\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f<int()>(); // int() is a typeid:call the first f()\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.3-5):
	template<class T> struct A {
	~A();
	};
	void f(A<int>* p, A<int>* q) {
	p->A<int>::~A(); // OK: destructor call
	q->A<int>::~A<int>(); // OK: destructor call
	}
	 --End Example]
	 */
	public void test14_3s5()  { // TODO Devin raised bug 90672
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("~A();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f(A<int>* p, A<int>* q) {\n"); //$NON-NLS-1$
		buffer.append("p->A<int>::~A(); // OK: destructor call\n"); //$NON-NLS-1$
		buffer.append("q->A<int>::~A<int>(); // OK: destructor call\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.3.2-5):
	template<const int* pci> struct X {  };
	int ai[10];
	X<ai> xi; // array to pointer and qualification conversions
	struct Y {  };
	template<const Y& b> struct Z {  };
	Y y;
	Z<y> z; // no conversion, but note extra cvqualification
	template<int (&pa)[5]> struct W {  };
	int b[5];
	W<b> w; // no conversion
	void f(char);
	void f(int);
	template<void (*pf)(int)> struct A {  };
	A<&f> a; // selects f(int)
	 --End Example]
	 */
	public void test14_3_2s5()  { // TODO Devin raised bug 90673
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<const int* pci> struct X {  };\n"); //$NON-NLS-1$
		buffer.append("int ai[10];\n"); //$NON-NLS-1$
		buffer.append("X<ai> xi; // array to pointer and qualification conversions\n"); //$NON-NLS-1$
		buffer.append("struct Y {  };\n"); //$NON-NLS-1$
		buffer.append("template<const Y& b> struct Z {  };\n"); //$NON-NLS-1$
		buffer.append("Y y;\n"); //$NON-NLS-1$
		buffer.append("Z<y> z; // no conversion, but note extra cvqualification\n"); //$NON-NLS-1$
		buffer.append("template<int (&pa)[5]> struct W {  };\n"); //$NON-NLS-1$
		buffer.append("int b[5];\n"); //$NON-NLS-1$
		buffer.append("W<b> w; // no conversion\n"); //$NON-NLS-1$
		buffer.append("void f(char);\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("template<void (*pf)(int)> struct A {  };\n"); //$NON-NLS-1$
		buffer.append("A<&f> a; // selects f(int)\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.1.3-1):
	template<class T> class X {
	static T s;
	};
	template<class T> T X<T>::s = 0;
	 --End Example]
	 */
	public void test14_5_1_3s1()  { // TODO Devin no bug raised on this, can't reproduce in AST View
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X {\n"); //$NON-NLS-1$
		buffer.append("static T s;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> T X<T>::s = 0;\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
	public void test14_5_3s1()  { // TODO Devin raised bug 90678
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.3-6):
	template<class T> struct A {
	struct B { };
	void f();
	};
	class C {
	template<class T> friend struct A<T>::B;
	template<class T> friend void A<T>::f();
	};
	 --End Example]
	 */
	public void test14_5_3s6()  { // TODO Devin raised bug 90678
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("struct B { };\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("template<class T> friend struct A<T>::B;\n"); //$NON-NLS-1$
		buffer.append("template<class T> friend void A<T>::f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.4-6):
	template<class T> struct A {
	class C {
	template<class T2> struct B { };
	};
	};
	// partial specialization of A<T>::C::B<T2>
	template<class T> template<class T2>
	struct A<T>::C::B<T2*> { };
	A<short>::C::B<int*> absip; // uses partial specialization
	 --End Example]
	 */
	public void test14_5_4s6()  { // TODO Devin raised bug 90678
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("template<class T2> struct B { };\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("// partial specialization of A<T>::C::B<T2>\n"); //$NON-NLS-1$
		buffer.append("template<class T> template<class T2>\n"); //$NON-NLS-1$
		buffer.append("struct A<T>::C::B<T2*> { };\n"); //$NON-NLS-1$
		buffer.append("A<short>::C::B<int*> absip; // uses partial specialization\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.4-7):
	namespace N {
	template<class T1, class T2> class A { }; // primary template
	}
	using N::A; // refers to the primary template
	namespace N {
	template<class T> class A<T, T*> { }; // partial specialization
	}
	A<int,int*> a; // uses the partial specialization, which is found through
	// the using declaration which refers to the primary template
	 --End Example]
	 */
	public void test14_5_4s7()  { // TODO Devin raised bug 90678
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("template<class T1, class T2> class A { }; // primary template\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("using N::A; // refers to the primary template\n"); //$NON-NLS-1$
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("template<class T> class A<T, T*> { }; // partial specialization\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("A<int,int*> a; // uses the partial specialization, which is found through\n"); //$NON-NLS-1$
		buffer.append("// the using declaration which refers to the primary template\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.4.3-2):
	template<class T> struct A {
	template<class T2> struct B {}; // #1
	template<class T2> struct B<T2*> {}; // #2
	};
	template<> template<class T2> struct A<short>::B {}; // #3
	A<char>::B<int*> abcip; // uses #2
	A<short>::B<int*> absip; // uses #3
	A<char>::B<int> abci; // uses #1
	 --End Example]
	 */
	public void test14_5_4_3s2()  { // TODO Devin raised bug 90681
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("template<class T2> struct B {}; // #1\n"); //$NON-NLS-1$
		buffer.append("template<class T2> struct B<T2*> {}; // #2\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<> template<class T2> struct A<short>::B {}; // #3\n"); //$NON-NLS-1$
		buffer.append("A<char>::B<int*> abcip; // uses #2\n"); //$NON-NLS-1$
		buffer.append("A<short>::B<int*> absip; // uses #3\n"); //$NON-NLS-1$
		buffer.append("A<char>::B<int> abci; // uses #1\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.5.1-1):
	// file1.c 
	template<class T>
	void f(T*);
	void g(int* p) { 
	f(p); // call 
	// f<int>(int*) 
	}
	// file2.c
	template<class T>
	void f(T);
	void h(int* p) {
	f(p); // call
	// f<int*>(int*)
	}
	 --End Example]
	 */
	public void test14_5_5_1s1()  { // TODO Devin raised bug 90682
		StringBuffer buffer = new StringBuffer();
		buffer.append("// file1.c \n"); //$NON-NLS-1$
		buffer.append("template<class T>\n"); //$NON-NLS-1$
		buffer.append("void f(T*);\n"); //$NON-NLS-1$
		buffer.append("void g(int* p) { \n"); //$NON-NLS-1$
		buffer.append("f(p); // call \n"); //$NON-NLS-1$
		buffer.append("// f<int>(int*) \n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("// file2.c\n"); //$NON-NLS-1$
		buffer.append("template<class T>\n"); //$NON-NLS-1$
		buffer.append("void f(T);\n"); //$NON-NLS-1$
		buffer.append("void h(int* p) {\n"); //$NON-NLS-1$
		buffer.append("f(p); // call\n"); //$NON-NLS-1$
		buffer.append("// f<int*>(int*)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.5.1-5):
	template <int I, int J> A<I+J> f(A<I>, A<J>); // #1
	template <int K, int L> A<K+L> f(A<K>, A<L>); // same as #1
	template <int I, int J> A<IJ> f(A<I>, A<J>); // different from #1
	 --End Example]
	 */
	public void test14_5_5_1s5()  { // TODO Devin raised bug 90683
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <int I, int J> A<I+J> f(A<I>, A<J>); // #1\n"); //$NON-NLS-1$
		buffer.append("template <int K, int L> A<K+L> f(A<K>, A<L>); // same as #1\n"); //$NON-NLS-1$
		buffer.append("template <int I, int J> A<IJ> f(A<I>, A<J>); // different from #1\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
	public void test14_5_5_1s6()  { // TODO Devin raised bug 90683
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <int I, int J> void f(A<I+J>); // #1\n"); //$NON-NLS-1$
		buffer.append("template <int K, int L> void f(A<K+L>); // same as #1\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.5.2-6):
	template<class T> void f(T); // #1
	template<class T> void f(T*, int=1); // #2
	template<class T> void g(T); // #3
	template<class T> void g(T*, ...); // #4
	int main() {
	int* ip;
	f(ip); //calls #2
	g(ip); //calls #4
	}
	 --End Example]
	 */
	public void test14_5_5_2s6()  { // TODO Devin raised bug 90684
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T); // #1\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T*, int=1); // #2\n"); //$NON-NLS-1$
		buffer.append("template<class T> void g(T); // #3\n"); //$NON-NLS-1$
		buffer.append("template<class T> void g(T*, ...); // #4\n"); //$NON-NLS-1$
		buffer.append("int main() {\n"); //$NON-NLS-1$
		buffer.append("int* ip;\n"); //$NON-NLS-1$
		buffer.append("f(ip); //calls #2\n"); //$NON-NLS-1$
		buffer.append("g(ip); //calls #4\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.6.1-1):
	template<class T> class X {
	X* p; // meaning X<T>
	X<T>* p2;
	X<int>* p3;
	};
	 --End Example]
	 */
	public void test14_6_1s1()  { // TODO Devin can not reproduce IProblemBinding via DOMAST View
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X {\n"); //$NON-NLS-1$
		buffer.append("X* p; // meaning X<T>\n"); //$NON-NLS-1$
		buffer.append("X<T>* p2;\n"); //$NON-NLS-1$
		buffer.append("X<int>* p3;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.6.1-3a):
	template<class T, T* p, class U = T> class X {  };
	template<class T> void f(T* p = new T);
	 --End Example]
	 */
	public void test14_6_1s3a()  { // TODO Devin already have bug on this one
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T, T* p, class U = T> class X {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T* p = new T);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.6.1-6):
	namespace N {
	class C { };
	template<class T> class B {
	void f(T);
	};
	}
	template<class C> void N::B<C>::f(C) {
	C b; // C is the template parameter, not N::C
	}
	 --End Example]
	 */
	public void test14_6_1s6()  { // TODO Devin raised bug 90686
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("class C { };\n"); //$NON-NLS-1$
		buffer.append("template<class T> class B {\n"); //$NON-NLS-1$
		buffer.append("void f(T);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("template<class C> void N::B<C>::f(C) {\n"); //$NON-NLS-1$
		buffer.append("C b; // C is the template parameter, not N::C\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
	public void test14_6_2s3()  { // TODO Devin this doesn't compile via g++ ?
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef double A;\n"); //$NON-NLS-1$
		buffer.append("template<class T> B {\n"); //$NON-NLS-1$
		buffer.append("typedef int A;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> struct X : B<T> {\n"); //$NON-NLS-1$
		buffer.append("A a; // a has type double\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.7-6):
	template<class T> class X {
	static T s;
	// ...
	};
	template<class T> T X<T>::s = 0;
	X<int> aa;
	X<char*> bb;
	 --End Example]
	 */
	public void test14_7s6()  { // TODO Devin can't reproduce via ASTDOM View
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X {\n"); //$NON-NLS-1$
		buffer.append("static T s;\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> T X<T>::s = 0;\n"); //$NON-NLS-1$
		buffer.append("X<int> aa;\n"); //$NON-NLS-1$
		buffer.append("X<char*> bb;\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.7.1-5):
	template <class T> struct S {
	operator int();
	};
	void f(int);
	void f(S<int>&);
	void f(S<float>);
	void g(S<int>& sr) {
	f(sr); //instantiation of S<int> allowed but not required
	// instantiation of S<float> allowed but not required
	};
	 --End Example]
	 */
	public void test14_7_1s5()  { // TODO Devin already have similar bug
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> struct S {\n"); //$NON-NLS-1$
		buffer.append("operator int();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("void f(S<int>&);\n"); //$NON-NLS-1$
		buffer.append("void f(S<float>);\n"); //$NON-NLS-1$
		buffer.append("void g(S<int>& sr) {\n"); //$NON-NLS-1$
		buffer.append("f(sr); //instantiation of S<int> allowed but not required\n"); //$NON-NLS-1$
		buffer.append("// instantiation of S<float> allowed but not required\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.7.1-10):
	namespace N {
	template<class T> class List {
	public:
	T* get();
	// ...
	};
	}
	template<class K, class V> class Map {
	N::List<V> lt;
	V get(K);
	// ...
	};
	void g(Map<char*,int>& m)
	{
	int i = m.get("Nicholas");
	// ...
	}
	 --End Example]
	 */
	public void test14_7_1s10()  { // TODO Devin already have similar bug
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("template<class T> class List {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("T* get();\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("template<class K, class V> class Map {\n"); //$NON-NLS-1$
		buffer.append("N::List<V> lt;\n"); //$NON-NLS-1$
		buffer.append("V get(K);\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void g(Map<char*,int>& m)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int i = m.get(\"Nicholas\");\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.7.1-14):
	template<class T> class X {
	X<T>* p; // OK
	X<T*> a; // implicit generation of X<T> requires
	// the implicit instantiation of X<T*> which requires
	// the implicit instantiation of X<T**> which ...
	};
	 --End Example]
	 */
	public void test14_7_1s14()  { // TODO Devin can't reproduce via DOMAST View
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X {\n"); //$NON-NLS-1$
		buffer.append("X<T>* p; // OK\n"); //$NON-NLS-1$
		buffer.append("X<T*> a; // implicit generation of X<T> requires\n"); //$NON-NLS-1$
		buffer.append("// the implicit instantiation of X<T*> which requires\n"); //$NON-NLS-1$
		buffer.append("// the implicit instantiation of X<T**> which ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.7.2-6):
	template<class T> class Array {  };
	template<class T> void sort(Array<T>& v);
	// instantiate sort(Array<int>&) - templateargument deduced
	template void sort<>(Array<int>&);
	 --End Example]
	 */
	public void test14_7_2s6()  { // TODO Devin raised bug 90689
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Array {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void sort(Array<T>& v);\n"); //$NON-NLS-1$
		buffer.append("// instantiate sort(Array<int>&) - templateargument deduced\n"); //$NON-NLS-1$
		buffer.append("template void sort<>(Array<int>&);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.7.3-1):
	template<class T> class stream;
	template<> class stream<char> {  };
	template<class T> class Array {  };
	template<class T> void sort(Array<T>& v) {  }
	template<> void sort<char*>(Array<char*>&) ;
	 --End Example]
	 */
	public void test14_7_3s1()  { // TODO Devin have similar bug
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class stream;\n"); //$NON-NLS-1$
		buffer.append("template<> class stream<char> {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> class Array {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void sort(Array<T>& v) {  }\n"); //$NON-NLS-1$
		buffer.append("template<> void sort<char*>(Array<char*>&) ;\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.7.3-14):
	template<class T> void f(T) {  }
	template<class T> inline T g(T) {  }
	template<> inline void f<>(int) {  } // OK: inline
	template<> int g<>(int) {  } // OK: not inline
	 --End Example]
	 */
	public void test14_7_3s14()  { // TODO Devin similar bug already
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T) {  }\n"); //$NON-NLS-1$
		buffer.append("template<class T> inline T g(T) {  }\n"); //$NON-NLS-1$
		buffer.append("template<> inline void f<>(int) {  } // OK: inline\n"); //$NON-NLS-1$
		buffer.append("template<> int g<>(int) {  } // OK: not inline\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.7.3-16):
	template<class T> struct A {
	void f(T);
	template<class X> void g(T,X);
	void h(T) { }
	};
	// specialization
	template<> void A<int>::f(int);
	// out of class member template definition
	template<class T> template<class X> void A<T>::g(T,X) { }
	// member template partial specialization
	template<> template<class X> void A<int>::g(int,X);
	// member template specialization
	template<> template<>
	void A<int>::g(int,char); // X deduced as char
	template<> template<>
	void A<int>::g<char>(int,char); // X specified as char
	// member specialization even if defined in class definition
	template<> void A<int>::h(int) { }
	 --End Example]
	 */
	public void test14_7_3s16()  { // TODO Devin similar bug already
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("void f(T);\n"); //$NON-NLS-1$
		buffer.append("template<class X> void g(T,X);\n"); //$NON-NLS-1$
		buffer.append("void h(T) { }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("// specialization\n"); //$NON-NLS-1$
		buffer.append("template<> void A<int>::f(int);\n"); //$NON-NLS-1$
		buffer.append("// out of class member template definition\n"); //$NON-NLS-1$
		buffer.append("template<class T> template<class X> void A<T>::g(T,X) { }\n"); //$NON-NLS-1$
		buffer.append("// member template partial specialization\n"); //$NON-NLS-1$
		buffer.append("template<> template<class X> void A<int>::g(int,X);\n"); //$NON-NLS-1$
		buffer.append("// member template specialization\n"); //$NON-NLS-1$
		buffer.append("template<> template<>\n"); //$NON-NLS-1$
		buffer.append("void A<int>::g(int,char); // X deduced as char\n"); //$NON-NLS-1$
		buffer.append("template<> template<>\n"); //$NON-NLS-1$
		buffer.append("void A<int>::g<char>(int,char); // X specified as char\n"); //$NON-NLS-1$
		buffer.append("// member specialization even if defined in class definition\n"); //$NON-NLS-1$
		buffer.append("template<> void A<int>::h(int) { }\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.7.3-17):
	template<class T1> class A {
	template<class T2> class B {
	void mf();
	};
	};
	template<> template<> A<int>::B<double> { };
	template<> template<> void A<char>::B<char>::mf() { };
	 --End Example]
	 */
	public void test14_7_3s17()  { // TODO Devin doesn't compile via g++
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T1> class A {\n"); //$NON-NLS-1$
		buffer.append("template<class T2> class B {\n"); //$NON-NLS-1$
		buffer.append("void mf();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<> template<> A<int>::B<double> { };\n"); //$NON-NLS-1$
		buffer.append("template<> template<> void A<char>::B<char>::mf() { };\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8-2):
	template<class T> void f(T* p)
	{
	static T s;
	// ...
	};
	void g(int a, char* b)
	{
	f(&a); //call f<int>(int*)
	f(&b); //call f<char*>(char**)
	}
	 --End Example]
	 */
	public void test14_8s2()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T* p)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("static T s;\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void g(int a, char* b)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(&a); //call f<int>(int*)\n"); //$NON-NLS-1$
		buffer.append("f(&b); //call f<char*>(char**)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.3-6):
	template<class T> void f(T); // declaration
	void g()
	{
	f("Annemarie"); // call of f<const char*>
	}
	 --End Example]
	 */
	public void test14_8_3s6()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T); // declaration\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(\"Annemarie\"); // call of f<const char*>\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
		parse(buffer.toString(), ParserLanguage.CPP, false, false);
		assertTrue(false);
		} catch (Exception e) {
		}
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
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
		parse(buffer.toString(), ParserLanguage.CPP, true, true);
		assertTrue(false);
		} catch (Exception e) {
		}
	}


	
}
