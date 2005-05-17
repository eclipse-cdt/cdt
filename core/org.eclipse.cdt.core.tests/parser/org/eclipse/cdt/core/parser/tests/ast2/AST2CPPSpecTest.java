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
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author dsteffle
 */
public class AST2CPPSpecTest extends AST2SpecBaseTest {

	/**
	 * Note:  Each example should have the example in a comment before
	 * the tested method with the following format:
	  
	 [--Start Example(specID sectionID):
	 // example code
	 --End Example]
	 
	 ex:
	 [--Start Example(CPP 2.3-2):
	 ??=define arraycheck(a,b) a??(b??) ??!??! b??(a??)
	 // becomes
	 #define arraycheck(a,b) a[b] || b[a]
	 --End Example]
	 
	 * This way it will be easy to extract the examples for future use via a
	 * 'run once' script.
	 *
	 */
	
	/**
	 [--Start Example(CPP 2.4-5):
	 int x=x+++++y;
	 --End Example]
	 */
	public void test2_4s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int x=x+++++y;\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(CPP 2.13.1-1):
	 int a=12, b=014, c=0XC;
	 --End Example]
	 */
	public void test2_13_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int a=12, b=014, c=0XC;\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.1-3):
	 int a; // defines a
	 extern const int c = 1; // defines c
	 int f(int x) { return x+a; } // defines f and defines x
	 struct S { int a; int b; }; // defines S, S::a, and S::b
	 struct X { // defines X
	 int x; // defines nonstatic data member x
	 static int y; // declares static data member y
	 X(): x(0) { } // defines a constructor of X
	 };
	 int X::y = 1; // defines X::y
	 enum { up, down }; // defines up and down
	 namespace N { int d; } // defines N and N::d
	 namespace N1 = N; // defines N1
	 X anX; // defines anX
	 // whereas these are just declarations:
	 extern int a; // declares a
	 extern const int c; // declares c
	 int f(int); // declares f
	 struct S; // declares S
	 typedef int Int; // declares Int
	 extern X anotherX; // declares anotherX
	 using N::d; // declares N::d
	 --End Example]
	 */
	public void test3_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int a; // defines a\n"); //$NON-NLS-1$
		buffer.append("extern const int c = 1; // defines c\n"); //$NON-NLS-1$
		buffer.append("int f(int x) { return x+a; } // defines f and defines x\n"); //$NON-NLS-1$
		buffer.append("struct S { int a; int b; }; // defines S, S::a, and S::b\n"); //$NON-NLS-1$
		buffer.append("struct X { // defines X\n"); //$NON-NLS-1$
		buffer.append("int x; // defines nonstatic data member x\n"); //$NON-NLS-1$
		buffer.append("static int y; // declares static data member y\n"); //$NON-NLS-1$
		buffer.append("X(): x(0) { } // defines a constructor of X\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int X::y = 1; // defines X::y\n"); //$NON-NLS-1$
		buffer.append("enum { up, down }; // defines up and down\n"); //$NON-NLS-1$
		buffer.append("namespace N { int d; } // defines N and N::d\n"); //$NON-NLS-1$
		buffer.append("namespace N1 = N; // defines N1\n"); //$NON-NLS-1$
		buffer.append("X anX; // defines anX\n"); //$NON-NLS-1$
		buffer.append("// whereas these are just declarations:\n"); //$NON-NLS-1$
		buffer.append("extern int a; // declares a\n"); //$NON-NLS-1$
		buffer.append("extern const int c; // declares c\n"); //$NON-NLS-1$
		buffer.append("int f(int); // declares f\n"); //$NON-NLS-1$
		buffer.append("struct S; // declares S\n"); //$NON-NLS-1$
		buffer.append("typedef int Int; // declares Int\n"); //$NON-NLS-1$
		buffer.append("extern X anotherX; // declares anotherX\n"); //$NON-NLS-1$
		buffer.append("using N::d; // declares N::d\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.1-4a):
	 struct C {
	 string s; // string is the standard library class (clause 21)
	 };
	 int main()
	 {
	 C a;
	 C b = a;
	 b = a;
	 }
	 --End Example]
	 */
	public void test3_1s4a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		 buffer.append("struct C {\n"); //$NON-NLS-1$
		 buffer.append("string s; // string is the standard library class (clause 21)\n"); //$NON-NLS-1$
		 buffer.append("};\n"); //$NON-NLS-1$
		 buffer.append("int main()\n"); //$NON-NLS-1$
		 buffer.append("{\n"); //$NON-NLS-1$
		 buffer.append("C a;\n"); //$NON-NLS-1$
		 buffer.append("C b = a;\n"); //$NON-NLS-1$
		 buffer.append("b = a;\n"); //$NON-NLS-1$
		 buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 3.1-4b):
	 struct C {
	 string s;
	 C(): s() { }
	 C(const C& x): s(x.s) { }
	 C& operator=(const C& x) { s = x.s; return *this; }
	 ~C() { }
	 };
	 --End Example]
	 */
	public void test3_1s4b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		 buffer.append("struct C {\n"); //$NON-NLS-1$
		 buffer.append("string s;\n"); //$NON-NLS-1$
		 buffer.append("C(): s() { }\n"); //$NON-NLS-1$
		 buffer.append("C(const C& x): s(x.s) { }\n"); //$NON-NLS-1$
		 buffer.append("C& operator=(const C& x) { s = x.s; return *this; }\n"); //$NON-NLS-1$
		 buffer.append("~C() { }\n"); //$NON-NLS-1$
		 buffer.append("};\n"); //$NON-NLS-1$
		 buffer.append("\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 3.2-4):
	 struct X; // declare X as a struct type
	 struct X* x1; // use X in pointer formation
	 X* x2; // use X in pointer formation
	 --End Example]
	 */
	public void test3_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X; // declare X as a struct type\n"); //$NON-NLS-1$
		buffer.append("struct X* x1; // use X in pointer formation\n"); //$NON-NLS-1$
		buffer.append("X* x2; // use X in pointer formation\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test3_2s5() throws Exception { 
		StringBuffer buffer = new StringBuffer();
		buffer.append("// translation unit 1:\n"); //$NON-NLS-1$
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("X(int);\n"); //$NON-NLS-1$
		buffer.append("X(int, int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("X::X(int = 0) { }\n"); //$NON-NLS-1$
		buffer.append("class D: public X { };\n"); //$NON-NLS-1$
		buffer.append("D d2; // X(int) called by D()\n"); //$NON-NLS-1$
		
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		
		buffer = new StringBuffer();
		buffer.append("// translation unit 2:\n"); //$NON-NLS-1$
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("X(int);\n"); //$NON-NLS-1$
		buffer.append("X(int, int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("X::X(int = 0, int = 0) { }\n"); //$NON-NLS-1$
		buffer.append("class D: public X { }; // X(int, int) called by D();\n"); //$NON-NLS-1$
		buffer.append("// D()’s implicit definition\n"); //$NON-NLS-1$
		buffer.append("// violates the ODR\n"); //$NON-NLS-1$
		
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.3-2):
	int j = 24;
	int main()
	{
	int i = j, j;
	j = 42;
	}
	 --End Example]
	 */
	public void test3_3s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int j = 24;\n"); //$NON-NLS-1$
		buffer.append("int main()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int i = j, j;\n"); //$NON-NLS-1$
		buffer.append("j = 42;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.3.1-1):
	int foo() {
	int x = 12;
	{ int x = x; }
	}
	 --End Example]
	 */
	public void test3_3_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("int x = 12;\n"); //$NON-NLS-1$
		buffer.append("{ int x = x; }\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}

	/**
	 [--Start Example(CPP 3.3.1-2):
	int foo() {
	const int i = 2;
	{ int i[i]; }
	}
	 --End Example]
	 */
	public void test3_3_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("const int i = 2;\n"); //$NON-NLS-1$
		buffer.append("{ int i[i]; }\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}

	/**
	 [--Start Example(CPP 3.3.1-3):
	int foo() {
	const int x = 12;
	{ enum { x = x }; }
	}
	 --End Example]
	 */
	public void test3_3_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("const int x = 12;\n"); //$NON-NLS-1$
		buffer.append("{ enum { x = x }; }\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}

	/**
	 [--Start Example(CPP 3.3.5-1):
	namespace N {
	int i;
	int g(int a) { return a; }
	int j();
	void q();
	}
	namespace { int l=1; }
	// the potential scope of l is from its point of declaration
	// to the end of the translation unit
	namespace N {
	int g(char a) // overloads N::g(int)
	{
	return l+a; // l is from unnamed namespace
	}
	int i; // error: duplicate definition
	int j(); // OK: duplicate function declaration
	int j() // OK: definition of N::j()
	{
	return g(i); // calls N::g(int)
	}
	int q(); // error: different return type
	}
	 --End Example]
	 */
	public void test3_3_5s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("int g(int a) { return a; }\n"); //$NON-NLS-1$
		buffer.append("int j();\n"); //$NON-NLS-1$
		buffer.append("void q();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace { int l=1; }\n"); //$NON-NLS-1$
		buffer.append("// the potential scope of l is from its point of declaration\n"); //$NON-NLS-1$
		buffer.append("// to the end of the translation unit\n"); //$NON-NLS-1$
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("int g(char a) // overloads N::g(int)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("return l+a; // l is from unnamed namespace\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("int i; // error: duplicate definition\n"); //$NON-NLS-1$
		buffer.append("int j(); // OK: duplicate function declaration\n"); //$NON-NLS-1$
		buffer.append("int j() // OK: definition of N::j()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("return g(i); // calls N::g(int)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("int q(); // error: different return type\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}

	/**
	 [--Start Example(CPP 3.4.1-6):
	namespace A {
	namespace N {
	void f();
	}
	}
	void A::N::f() {
	int i = 5;
	// The following scopes are searched for a declaration of i:
	// 1) outermost block scope of A::N::f, before the use of i
	// 2) scope of namespace N
	// 3) scope of namespace A
	// 4) global scope, before the definition of A::N::f
	}
	 --End Example]
	 */
	public void test3_4_1s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void A::N::f() {\n"); //$NON-NLS-1$
		buffer.append("int i = 5;\n"); //$NON-NLS-1$
		buffer.append("// The following scopes are searched for a declaration of i:\n"); //$NON-NLS-1$
		buffer.append("// 1) outermost block scope of A::N::f, before the use of i\n"); //$NON-NLS-1$
		buffer.append("// 2) scope of namespace N\n"); //$NON-NLS-1$
		buffer.append("// 3) scope of namespace A\n"); //$NON-NLS-1$
		buffer.append("// 4) global scope, before the definition of A::N::f\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}

	/**
	 [--Start Example(CPP 3.4.1-7):
	namespace M {
	class B { };
	}
	 --End Example]
	 */
	public void test3_4_1s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace M {\n"); //$NON-NLS-1$
		buffer.append("class B { };\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}

	/**
	 [--Start Example(CPP 3.4.1-8):
	class B { };
	namespace M {
	namespace N {
	class X : public B {
	void f();
	};
	}
	}
	void M::N::X::f() {
	int i = 16;
	}
	// The following scopes are searched for a declaration of i:
	// 1) outermost block scope of M::N::X::f, before the use of i
	// 2) scope of class M::N::X
	// 3) scope of M::N::X’s base class B
	// 4) scope of namespace M::N
	// 5) scope of namespace M
	// 6) global scope, before the definition of M::N::X::f
	 --End Example]
	 */
	public void test3_4_1s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B { };\n"); //$NON-NLS-1$
		buffer.append("namespace M {\n"); //$NON-NLS-1$
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("class X : public B {\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void M::N::X::f() {\n"); //$NON-NLS-1$
		buffer.append("int i = 16;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("// The following scopes are searched for a declaration of i:\n"); //$NON-NLS-1$
		buffer.append("// 1) outermost block scope of M::N::X::f, before the use of i\n"); //$NON-NLS-1$
		buffer.append("// 2) scope of class M::N::X\n"); //$NON-NLS-1$
		buffer.append("// 3) scope of M::N::X’s base class B\n"); //$NON-NLS-1$
		buffer.append("// 4) scope of namespace M::N\n"); //$NON-NLS-1$
		buffer.append("// 5) scope of namespace M\n"); //$NON-NLS-1$
		buffer.append("// 6) global scope, before the definition of M::N::X::f\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}

	/**
	 [--Start Example(CPP 3.4.2-2):
	namespace NS {
	class T { };
	void f(T);
	}
	NS::T parm;
	int main() {
	f(parm); //OK: calls NS::f
	}
	 --End Example]
	 */
	public void test3_4_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace NS {\n"); //$NON-NLS-1$
		buffer.append("class T { };\n"); //$NON-NLS-1$
		buffer.append("void f(T);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("NS::T parm;\n"); //$NON-NLS-1$
		buffer.append("int main() {\n"); //$NON-NLS-1$
		buffer.append("f(parm); //OK: calls NS::f\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}

	/**
	 [--Start Example(CPP 3.4.3-1):
	class A {
	public:
	static int n;
	};
	int main()
	{
	int A;
	A::n = 42; // OK
	A b; // illformed: A does not name a type
	}
	 --End Example]
	 */
	public void test3_4_3s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test3_4_3s3() throws Exception {
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

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3_4_3-5):
	struct C {
	typedef int I;
	};
	typedef int I1, I2;
	int foo() {
	extern int* p;
	extern int* q;
	p->C::I::~I(); // I is looked up in the scope of C
	q->I1::~I2(); // I2 is looked up in the scope of
	}
	// the postfixexpression
	struct A {
	~A();
	};
	typedef A AB;
	int main()
	{
	AB *p;
	p->AB::~AB(); // explicitly calls the destructor for A
	}
	 --End Example]
	 */
	public void test3_4_3s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct C {\n"); //$NON-NLS-1$
		buffer.append("typedef int I;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("typedef int I1, I2;\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("extern int* p;\n"); //$NON-NLS-1$
		buffer.append("extern int* q;\n"); //$NON-NLS-1$
		buffer.append("p->C::I::~I(); // I is looked up in the scope of C\n"); //$NON-NLS-1$
		buffer.append("q->I1::~I2(); // I2 is looked up in the scope of\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("// the postfixexpression\n"); //$NON-NLS-1$
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("~A();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("typedef A AB;\n"); //$NON-NLS-1$
		buffer.append("int main()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("AB *p;\n"); //$NON-NLS-1$
		buffer.append("p->AB::~AB(); // explicitly calls the destructor for A\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 3.4.3.2-2):
	int x;
	namespace Y {
	void f(float);
	void h(int);
	}
	namespace Z {
	void h(double);
	}
	namespace A {
	using namespace Y;
	void f(int);
	void g(int);
	int i;
	}
	namespace B {
	using namespace Z;
	void f(char);
	int i;
	}
	namespace AB {
	using namespace A;
	using namespace B;
	void g();
	}
	void h()
	{
	AB::g(); // g is declared directly in AB,
	// therefore S is { AB::g() } and AB::g() is chosen
	AB::f(1); // f is not declared directly in AB so the rules are
	// applied recursively to A and B;
	// namespace Y is not searched and Y::f(float)
	// is not considered;
	// S is { A::f(int), B::f(char) } and overload
	// resolution chooses A::f(int)
	AB::f('c'); //as above but resolution chooses B::f(char)
	AB::x++; // x is not declared directly in AB, and
	// is not declared in A or B, so the rules are
	// applied recursively to Y and Z,
	// S is { } so the program is illformed
	AB::i++; // i is not declared directly in AB so the rules are
	// applied recursively to A and B,
	// S is { A::i, B::i } so the use is ambiguous
	// and the program is illformed
	AB::h(16.8); // h is not declared directly in AB and
	// not declared directly in A or B so the rules are
	// applied recursively to Y and Z,
	// S is { Y::h(int), Z::h(double) } and overload
	// resolution chooses Z::h(double)
	}
	 --End Example]
	 */
	public void test3_4_3_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("namespace Y {\n"); //$NON-NLS-1$
		buffer.append("void f(float);\n"); //$NON-NLS-1$
		buffer.append("void h(int);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace Z {\n"); //$NON-NLS-1$
		buffer.append("void h(double);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("using namespace Y;\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("void g(int);\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("using namespace Z;\n"); //$NON-NLS-1$
		buffer.append("void f(char);\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace AB {\n"); //$NON-NLS-1$
		buffer.append("using namespace A;\n"); //$NON-NLS-1$
		buffer.append("using namespace B;\n"); //$NON-NLS-1$
		buffer.append("void g();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void h()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("AB::g(); // g is declared directly in AB,\n"); //$NON-NLS-1$
		buffer.append("// therefore S is { AB::g() } and AB::g() is chosen\n"); //$NON-NLS-1$
		buffer.append("AB::f(1); // f is not declared directly in AB so the rules are\n"); //$NON-NLS-1$
		buffer.append("// applied recursively to A and B;\n"); //$NON-NLS-1$
		buffer.append("// namespace Y is not searched and Y::f(float)\n"); //$NON-NLS-1$
		buffer.append("// is not considered;\n"); //$NON-NLS-1$
		buffer.append("// S is { A::f(int), B::f(char) } and overload\n"); //$NON-NLS-1$
		buffer.append("// resolution chooses A::f(int)\n"); //$NON-NLS-1$
		buffer.append("AB::f('c'); //as above but resolution chooses B::f(char)\n"); //$NON-NLS-1$
		buffer.append("AB::x++; // x is not declared directly in AB, and\n"); //$NON-NLS-1$
		buffer.append("// is not declared in A or B, so the rules are\n"); //$NON-NLS-1$
		buffer.append("// applied recursively to Y and Z,\n"); //$NON-NLS-1$
		buffer.append("// S is { } so the program is illformed\n"); //$NON-NLS-1$
		buffer.append("AB::i++; // i is not declared directly in AB so the rules are\n"); //$NON-NLS-1$
		buffer.append("// applied recursively to A and B,\n"); //$NON-NLS-1$
		buffer.append("// S is { A::i, B::i } so the use is ambiguous\n"); //$NON-NLS-1$
		buffer.append("// and the program is illformed\n"); //$NON-NLS-1$
		buffer.append("AB::h(16.8); // h is not declared directly in AB and\n"); //$NON-NLS-1$
		buffer.append("// not declared directly in A or B so the rules are\n"); //$NON-NLS-1$
		buffer.append("// applied recursively to Y and Z,\n"); //$NON-NLS-1$
		buffer.append("// S is { Y::h(int), Z::h(double) } and overload\n"); //$NON-NLS-1$
		buffer.append("// resolution chooses Z::h(double)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
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
	public void test3_4_3_2s3() throws Exception { 
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
		
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.4.3.2-4):
	namespace B {
	int b;
	}
	namespace A {
	using namespace B;
	int a;
	}
	namespace B {
	using namespace A;
	}
	void f()
	{
	A::a++; //OK: a declared directly in A, S is { A::a }
	B::a++; //OK: both A and B searched (once), S is { A::a }
	A::b++; //OK: both A and B searched (once), S is { B::b }
	B::b++; //OK: b declared directly in B, S is { B::b }
	}
	 --End Example]
	 */
	public void test3_4_3_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("int b;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("using namespace B;\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("using namespace A;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("A::a++; //OK: a declared directly in A, S is { A::a }\n"); //$NON-NLS-1$
		buffer.append("B::a++; //OK: both A and B searched (once), S is { A::a }\n"); //$NON-NLS-1$
		buffer.append("A::b++; //OK: both A and B searched (once), S is { B::b }\n"); //$NON-NLS-1$
		buffer.append("B::b++; //OK: b declared directly in B, S is { B::b }\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.4.3.2-5):
	namespace A {
	struct x { };
	int x;
	int y;
	}
	namespace B {
	struct y {};
	}
	namespace C {
	using namespace A;
	using namespace B;
	int i = C::x; // OK, A::x (of type int)
	int j = C::y; // ambiguous, A::y or B::y
	}
	 --End Example]
	 */
	public void test3_4_3_2s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("struct x { };\n"); //$NON-NLS-1$
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("int y;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("struct y {};\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace C {\n"); //$NON-NLS-1$
		buffer.append("using namespace A;\n"); //$NON-NLS-1$
		buffer.append("using namespace B;\n"); //$NON-NLS-1$
		buffer.append("int i = C::x; // OK, A::x (of type int)\n"); //$NON-NLS-1$
		buffer.append("int j = C::y; // ambiguous, A::y or B::y\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.4.3.2-6a):
	namespace A {
	namespace B {
	void f1(int);
	}
	using namespace B;
	}
	void A::f1(int) { } // illformed,
	// f1 is not a member of A
	 --End Example]
	 */
	public void test3_4_3_2s6a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("void f1(int);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("using namespace B;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void A::f1(int) { } // illformed,\n"); //$NON-NLS-1$
		buffer.append("// f1 is not a member of A\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.4.3.2-6b):
	namespace A {
	namespace B {
	void f1(int);
	}
	}
	namespace C {
	namespace D {
	void f1(int);
	}
	}
	using namespace A;
	using namespace C::D;
	void B::f1(int){} // OK, defines A::B::f1(int)
	 --End Example]
	 */
	public void test3_4_3_2s6b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("void f1(int);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace C {\n"); //$NON-NLS-1$
		buffer.append("namespace D {\n"); //$NON-NLS-1$
		buffer.append("void f1(int);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("using namespace A;\n"); //$NON-NLS-1$
		buffer.append("using namespace C::D;\n"); //$NON-NLS-1$
		buffer.append("void B::f1(int){} // OK, defines A::B::f1(int)\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.4.4-3):
	struct Node {
	struct Node* Next; // OK: Refers to Node at global scope
	struct Data* Data; // OK: Declares type Data
	// at global scope and member Data
	};
	struct Data {
	struct Node* Node; // OK: Refers to Node at global scope
	friend struct ::Glob; // error: Glob is not declared
	// cannot introduce a qualified type (7.1.5.3)
	friend struct Glob; // OK: Refers to (as yet) undeclared Glob
	// at global scope.
	
	};
	struct Base {
	struct Data; // OK: Declares nested Data
	struct ::Data* thatData; // OK: Refers to ::Data
	struct Base::Data* thisData; // OK: Refers to nested Data
	friend class ::Data; // OK: global Data is a friend
	friend class Data; // OK: nested Data is a friend
	struct Data { //
	 }; // Defines nested Data
	struct Data; // OK: Redeclares nested Data
	};
	struct Data; // OK: Redeclares Data at global scope
	struct ::Data; // error: cannot introduce a qualified type (7.1.5.3)
	struct Base::Data; // error: cannot introduce a qualified type (7.1.5.3)
	struct Base::Datum; // error: Datum undefined
	struct Base::Data* pBase; // OK: refers to nested Data
	 
	 --End Example]
	 */
	public void test3_4_4s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct Node {\n"); //$NON-NLS-1$
		buffer.append("struct Node* Next; // OK: Refers to Node at global scope\n"); //$NON-NLS-1$
		buffer.append("struct Data* Data; // OK: Declares type Data\n"); //$NON-NLS-1$
		buffer.append("// at global scope and member Data\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct Data {\n"); //$NON-NLS-1$
		buffer.append("struct Node* Node; // OK: Refers to Node at global scope\n"); //$NON-NLS-1$
		buffer.append("friend struct ::Glob; // error: Glob is not declared\n"); //$NON-NLS-1$
		buffer.append("// cannot introduce a qualified type (7.1.5.3)\n"); //$NON-NLS-1$
		buffer.append("friend struct Glob; // OK: Refers to (as yet) undeclared Glob\n"); //$NON-NLS-1$
		buffer.append("// at global scope.\n"); //$NON-NLS-1$
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct Base {\n"); //$NON-NLS-1$
		buffer.append("struct Data; // OK: Declares nested Data\n"); //$NON-NLS-1$
		buffer.append("struct ::Data* thatData; // OK: Refers to ::Data\n"); //$NON-NLS-1$
		buffer.append("struct Base::Data* thisData; // OK: Refers to nested Data\n"); //$NON-NLS-1$
		buffer.append("friend class ::Data; // OK: global Data is a friend\n"); //$NON-NLS-1$
		buffer.append("friend class Data; // OK: nested Data is a friend\n"); //$NON-NLS-1$
		buffer.append("struct Data { //\n"); //$NON-NLS-1$
		buffer.append("}; // Defines nested Data\n"); //$NON-NLS-1$
		buffer.append("struct Data; // OK: Redeclares nested Data\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct Data; // OK: Redeclares Data at global scope\n"); //$NON-NLS-1$
		buffer.append("struct ::Data; // error: cannot introduce a qualified type (7.1.5.3)\n"); //$NON-NLS-1$
		buffer.append("struct Base::Data; // error: cannot introduce a qualified type (7.1.5.3)\n"); //$NON-NLS-1$
		buffer.append("struct Base::Datum; // error: Datum undefined\n"); //$NON-NLS-1$
		buffer.append("struct Base::Data* pBase; // OK: refers to nested Data\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.5-6):
	static void f();
	static int i = 0; //1
	void g() {
	extern void f(); // internal linkage
	int i; //2: i has no linkage
	{
	extern void f(); // internal linkage
	extern int i; //3: external linkage
	}
	}
	 --End Example]
	 */
	public void test3_5s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("static void f();\n"); //$NON-NLS-1$
		buffer.append("static int i = 0; //1\n"); //$NON-NLS-1$
		buffer.append("void g() {\n"); //$NON-NLS-1$
		buffer.append("extern void f(); // internal linkage\n"); //$NON-NLS-1$
		buffer.append("int i; //2: i has no linkage\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("extern void f(); // internal linkage\n"); //$NON-NLS-1$
		buffer.append("extern int i; //3: external linkage\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.5-7):
	namespace X {
	void p()
	{
	q(); //error: q not yet declared
	extern void q(); // q is a member of namespace X
	}
	void middle()
	{
	q(); //error: q not yet declared
	}
	void q() { //
	 } // definition of X::q
	}
	void q() { //
	 } // some other, unrelated q
	 --End Example]
	 */
	public void test3_5s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace X {\n"); //$NON-NLS-1$
		buffer.append("void p()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("q(); //error: q not yet declared\n"); //$NON-NLS-1$
		buffer.append("extern void q(); // q is a member of namespace X\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void middle()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("q(); //error: q not yet declared\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void q() { //\n"); //$NON-NLS-1$
		buffer.append("} // definition of X::q\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void q() { //\n"); //$NON-NLS-1$
		buffer.append("} // some other, unrelated q\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 3.5-8):
	 void f()
	 {
	 struct A { int x; }; // no linkage
	 extern A a; // illformed
	 typedef A B;
	 extern B b; // illformed
	 }
	 --End Example]
	 */
	public void test3_5s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		 buffer.append("void f()\n"); //$NON-NLS-1$
		 buffer.append("{\n"); //$NON-NLS-1$
		 buffer.append("struct A { int x; }; // no linkage\n"); //$NON-NLS-1$
		 buffer.append("extern A a; // illformed\n"); //$NON-NLS-1$
		 buffer.append("typedef A B;\n"); //$NON-NLS-1$
		 buffer.append("extern B b; // illformed\n"); //$NON-NLS-1$
		 buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.6.1-2):
	int main(int argc, char* argv[]) { //
	}
	 --End Example]
	 */
	public void test3_6_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int main(int argc, char* argv[]) { //\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.8-5):
	struct B {
	virtual void f();
	void mutate();
	virtual ~B();
	};
	struct D1 : B { void f(); };
	struct D2 : B { void f(); };
	void B::mutate() {
	new (this) D2; // reuses storage – ends the lifetime of *this
	f(); //undefined behavior
	this; // OK, this points to valid memory
	}
	void g() {
	void* p = malloc(sizeof(D1) + sizeof(D2));
	B* pb = new (p) D1;
	pb->mutate();
	&pb; //OK: pb points to valid memory
	void* q = pb; // OK: pb points to valid memory
	pb->f(); //undefined behavior, lifetime of *pb has ended
	}
	 --End Example]
	 */
	public void test3_8s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("virtual void f();\n"); //$NON-NLS-1$
		buffer.append("void mutate();\n"); //$NON-NLS-1$
		buffer.append("virtual ~B();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D1 : B { void f(); };\n"); //$NON-NLS-1$
		buffer.append("struct D2 : B { void f(); };\n"); //$NON-NLS-1$
		buffer.append("void B::mutate() {\n"); //$NON-NLS-1$
		buffer.append("new (this) D2; // reuses storage – ends the lifetime of *this\n"); //$NON-NLS-1$
		buffer.append("f(); //undefined behavior\n"); //$NON-NLS-1$
		buffer.append("this; // OK, this points to valid memory\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void g() {\n"); //$NON-NLS-1$
		buffer.append("void* p = malloc(sizeof(D1) + sizeof(D2));\n"); //$NON-NLS-1$
		buffer.append("B* pb = new (p) D1;\n"); //$NON-NLS-1$
		buffer.append("pb->mutate();\n"); //$NON-NLS-1$
		buffer.append("&pb; //OK: pb points to valid memory\n"); //$NON-NLS-1$
		buffer.append("void* q = pb; // OK: pb points to valid memory\n"); //$NON-NLS-1$
		buffer.append("pb->f(); //undefined behavior, lifetime of *pb has ended\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 3.8-7):
	struct C {
	int i;
	void f();
	const C& operator=( const C& );
	};
	const C& C::operator=( const C& other)
	{
	if ( this != &other ) {
	this->~C(); //lifetime of *this ends
	new (this) C(other); // new object of type C created
	f(); //welldefined
	}
	return *this;
	}
	int foo() {
	C c1;
	C c2;
	c1 = c2; // welldefined
	c1.f(); //welldefined; c1 refers to a new object of type C
	}
	 --End Example]
	 */
	public void test3_8s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct C {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("const C& operator=( const C& );\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("const C& C::operator=( const C& other)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("if ( this != &other ) {\n"); //$NON-NLS-1$
		buffer.append("this->~C(); //lifetime of *this ends\n"); //$NON-NLS-1$
		buffer.append("new (this) C(other); // new object of type C created\n"); //$NON-NLS-1$
		buffer.append("f(); //welldefined\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("return *this;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("C c1;\n"); //$NON-NLS-1$
		buffer.append("C c2;\n"); //$NON-NLS-1$
		buffer.append("c1 = c2; // welldefined\n"); //$NON-NLS-1$
		buffer.append("c1.f(); //welldefined; c1 refers to a new object of type C\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.8-8):
	class T { };
	struct B {
	~B();
	};
	void h() {
	B b;
	new (&b) T;
	} //undefined behavior at block exit
	 --End Example]
	 */
	public void test3_8s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class T { };\n"); //$NON-NLS-1$
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("~B();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void h() {\n"); //$NON-NLS-1$
		buffer.append("B b;\n"); //$NON-NLS-1$
		buffer.append("new (&b) T;\n"); //$NON-NLS-1$
		buffer.append("} //undefined behavior at block exit\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.8-9):
	struct B {
	B();
	~B();
	};
	const B b;
	void h() {
	b.~B();
	new (&b) const B; // undefined behavior
	}
	 --End Example]
	 */
	public void test3_8s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("B();\n"); //$NON-NLS-1$
		buffer.append("~B();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("const B b;\n"); //$NON-NLS-1$
		buffer.append("void h() {\n"); //$NON-NLS-1$
		buffer.append("b.~B();\n"); //$NON-NLS-1$
		buffer.append("new (&b) const B; // undefined behavior\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.9-2):
	#define N sizeof(T)
	char buf[N];
	T obj; // obj initialized to its original value
	memcpy(buf, &obj, N); // between these two calls to memcpy,
	// obj might be modified
	memcpy(&obj, buf, N); // at this point, each subobject of obj of scalar type
	// holds its original value
	 --End Example]
	 */
	public void test3_9s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define N sizeof(T)\n"); //$NON-NLS-1$
		buffer.append("char buf[N];\n"); //$NON-NLS-1$
		buffer.append("T obj; // obj initialized to its original value\n"); //$NON-NLS-1$
		buffer.append("memcpy(buf, &obj, N); // between these two calls to memcpy,\n"); //$NON-NLS-1$
		buffer.append("// obj might be modified\n"); //$NON-NLS-1$
		buffer.append("memcpy(&obj, buf, N); // at this point, each subobject of obj of scalar type\n"); //$NON-NLS-1$
		buffer.append("// holds its original value\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 3.9-3):
	T* t1p;
	T* t2p;
	// provided that t2p points to an initialized object ...
	memcpy(t1p, t2p, sizeof(T)); // at this point, every subobject of POD type in *t1p contains
	// the same value as the corresponding subobject in *t2p
	 --End Example]
	 */
	public void test3_9s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("T* t1p;\n"); //$NON-NLS-1$
		buffer.append("T* t2p;\n"); //$NON-NLS-1$
		buffer.append("// provided that t2p points to an initialized object ...\n"); //$NON-NLS-1$
		buffer.append("memcpy(t1p, t2p, sizeof(T)); // at this point, every subobject of POD type in *t1p contains\n"); //$NON-NLS-1$
		buffer.append("// the same value as the corresponding subobject in *t2p\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 3.9-7):
	class X; // X is an incomplete type
	extern X* xp; // xp is a pointer to an incomplete type
	extern int arr[]; // the type of arr is incomplete
	typedef int UNKA[]; // UNKA is an incomplete type
	UNKA* arrp; // arrp is a pointer to an incomplete type
	UNKA** arrpp;
	void foo()
	{
	xp++; //illformed: X is incomplete
	arrp++; //illformed: incomplete type
	arrpp++; //OK: sizeof UNKA* is known
	}
	struct X { int i; }; // now X is a complete type
	int arr[10]; // now the type of arr is complete
	X x;
	void bar()
	{
	xp = &x; // OK; type is ‘‘pointer to X’’
	arrp = &arr; // illformed: different types
	xp++; //OK: X is complete
	arrp++; //illformed: UNKA can’t be completed
	}
	 --End Example]
	 */
	public void test3_9s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X; // X is an incomplete type\n"); //$NON-NLS-1$
		buffer.append("extern X* xp; // xp is a pointer to an incomplete type\n"); //$NON-NLS-1$
		buffer.append("extern int arr[]; // the type of arr is incomplete\n"); //$NON-NLS-1$
		buffer.append("typedef int UNKA[]; // UNKA is an incomplete type\n"); //$NON-NLS-1$
		buffer.append("UNKA* arrp; // arrp is a pointer to an incomplete type\n"); //$NON-NLS-1$
		buffer.append("UNKA** arrpp;\n"); //$NON-NLS-1$
		buffer.append("void foo()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("xp++; //illformed: X is incomplete\n"); //$NON-NLS-1$
		buffer.append("arrp++; //illformed: incomplete type\n"); //$NON-NLS-1$
		buffer.append("arrpp++; //OK: sizeof UNKA* is known\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("struct X { int i; }; // now X is a complete type\n"); //$NON-NLS-1$
		buffer.append("int arr[10]; // now the type of arr is complete\n"); //$NON-NLS-1$
		buffer.append("X x;\n"); //$NON-NLS-1$
		buffer.append("void bar()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("xp = &x; // OK; type is ‘‘pointer to X’’\n"); //$NON-NLS-1$
		buffer.append("arrp = &arr; // illformed: different types\n"); //$NON-NLS-1$
		buffer.append("xp++; //OK: X is complete\n"); //$NON-NLS-1$
		buffer.append("arrp++; //illformed: UNKA can’t be completed\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.10-3):
	int& f();
	 --End Example]
	 */
	public void test3_10s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int& f();\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 5-4):
	int foo() {
	i = v[i++]; // the behavior is unspecified
	i = 7, i++, i++; // i becomes 9
	i = ++i + 1; // the behavior is unspecified
	i = i + 1; // the value of i is incremented
	}
	 --End Example]
	 */
	public void test5s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("i = v[i++]; // the behavior is unspecified\n"); //$NON-NLS-1$
		buffer.append("i = 7, i++, i++; // i becomes 9\n"); //$NON-NLS-1$
		buffer.append("i = ++i + 1; // the behavior is unspecified\n"); //$NON-NLS-1$
		buffer.append("i = i + 1; // the value of i is incremented\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 5.2.7-5):
	struct B {};
	struct D : B {};
	void foo(D* dp)
	{
	B* bp = dynamic_cast<B*>(dp); // equivalent to B* bp = dp;
	}
	 --End Example]
	 */
	public void test5_2_7s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {};\n"); //$NON-NLS-1$
		buffer.append("struct D : B {};\n"); //$NON-NLS-1$
		buffer.append("void foo(D* dp)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("B* bp = dynamic_cast<B*>(dp); // equivalent to B* bp = dp;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 5.2.7-9):
	class A { virtual void f(); };
	class B { virtual void g(); };
	class D : public virtual A, private B {};
	void g()
	{
	D d;
	B* bp = (B*)&d; // cast needed to break protection
	A* ap = &d; // public derivation, no cast needed
	D& dr = dynamic_cast<D&>(*bp); // fails
	ap = dynamic_cast<A*>(bp); // fails
	bp = dynamic_cast<B*>(ap); // fails
	ap = dynamic_cast<A*>(&d); // succeeds
	bp = dynamic_cast<B*>(&d); // fails
	}
	class E : public D, public B {};
	class F : public E, public D {};
	void h()
	{
	F f;
	A* ap = &f; // succeeds: finds unique A
	D* dp = dynamic_cast<D*>(ap); // fails: yields 0
	// f has two D subobjects
	E* ep = (E*)ap; // illformed:
	// cast from virtual base
	E* ep1 = dynamic_cast<E*>(ap); // succeeds
	}
	 --End Example]
	 */
	public void test5_2_7s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A { virtual void f(); };\n"); //$NON-NLS-1$
		buffer.append("class B { virtual void g(); };\n"); //$NON-NLS-1$
		buffer.append("class D : public virtual A, private B {};\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("D d;\n"); //$NON-NLS-1$
		buffer.append("B* bp = (B*)&d; // cast needed to break protection\n"); //$NON-NLS-1$
		buffer.append("A* ap = &d; // public derivation, no cast needed\n"); //$NON-NLS-1$
		buffer.append("D& dr = dynamic_cast<D&>(*bp); // fails\n"); //$NON-NLS-1$
		buffer.append("ap = dynamic_cast<A*>(bp); // fails\n"); //$NON-NLS-1$
		buffer.append("bp = dynamic_cast<B*>(ap); // fails\n"); //$NON-NLS-1$
		buffer.append("ap = dynamic_cast<A*>(&d); // succeeds\n"); //$NON-NLS-1$
		buffer.append("bp = dynamic_cast<B*>(&d); // fails\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("class E : public D, public B {};\n"); //$NON-NLS-1$
		buffer.append("class F : public E, public D {};\n"); //$NON-NLS-1$
		buffer.append("void h()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("F f;\n"); //$NON-NLS-1$
		buffer.append("A* ap = &f; // succeeds: finds unique A\n"); //$NON-NLS-1$
		buffer.append("D* dp = dynamic_cast<D*>(ap); // fails: yields 0\n"); //$NON-NLS-1$
		buffer.append("// f has two D subobjects\n"); //$NON-NLS-1$
		buffer.append("E* ep = (E*)ap; // illformed:\n"); //$NON-NLS-1$
		buffer.append("// cast from virtual base\n"); //$NON-NLS-1$
		buffer.append("E* ep1 = dynamic_cast<E*>(ap); // succeeds\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 5.2.8-5):
	class D { // ... 
	};
	D d1;
	const D d2;
	int foo() {
	typeid(d1) == typeid(d2); // yields true
	typeid(D) == typeid(const D); // yields true
	typeid(D) == typeid(d2); // yields true
	typeid(D) == typeid(const D&); // yields true
	}
	 --End Example]
	 */
	public void test5_2_8s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class D { // ... \n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("D d1;\n"); //$NON-NLS-1$
		buffer.append("const D d2;\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("typeid(d1) == typeid(d2); // yields true\n"); //$NON-NLS-1$
		buffer.append("typeid(D) == typeid(const D); // yields true\n"); //$NON-NLS-1$
		buffer.append("typeid(D) == typeid(d2); // yields true\n"); //$NON-NLS-1$
		buffer.append("typeid(D) == typeid(const D&); // yields true\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 5.2.9-5):
	struct B {};
	struct D : public B {};
	D d;
	B &br = d;
	int foo() {
	static_cast<D&>(br); // produces lvalue to the original d object
	}
	 --End Example]
	 */
	public void test5_2_9s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {};\n"); //$NON-NLS-1$
		buffer.append("struct D : public B {};\n"); //$NON-NLS-1$
		buffer.append("D d;\n"); //$NON-NLS-1$
		buffer.append("B &br = d;\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("static_cast<D&>(br); // produces lvalue to the original d object\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 5.3.1-2):
	struct A { int i; };
	struct B : A { };
	int foo() {
	&B::i; // has type int A::*
	}
	 --End Example]
	 */
	public void test5_3_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A { int i; };\n"); //$NON-NLS-1$
		buffer.append("struct B : A { };\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("&B::i; // has type int A::*\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 5.3.4-6):
	int n=2;
	int x=new float[n][5];
	int y=new float[5][n];
	 --End Example]
	 */
	public void test5_3_4s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int n=2;\n"); //$NON-NLS-1$
		buffer.append("int x=new float[n][5];\n"); //$NON-NLS-1$
		buffer.append("int y=new float[5][n];\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 5.4-5):
	struct A {};
	struct I1 : A {};
	struct I2 : A {};
	struct D : I1, I2 {};
	A *foo( D *p ) {
	return (A*)( p ); // illformed
	// static_cast interpretation
	}
	 --End Example]
	 */
	public void test5_4s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {};\n"); //$NON-NLS-1$
		buffer.append("struct I1 : A {};\n"); //$NON-NLS-1$
		buffer.append("struct I2 : A {};\n"); //$NON-NLS-1$
		buffer.append("struct D : I1, I2 {};\n"); //$NON-NLS-1$
		buffer.append("A *foo( D *p ) {\n"); //$NON-NLS-1$
		buffer.append("return (A*)( p ); // illformed\n"); //$NON-NLS-1$
		buffer.append("// static_cast interpretation\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 5.5-6):
	int foo() {
	(ptr_to_obj->*ptr_to_mfct)(10);
	}
	 --End Example]
	 */
	public void test5_5s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("(ptr_to_obj->*ptr_to_mfct)(10);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 5.9-2):
	void *p;
	const int *q;
	int **pi;
	const int *const *pci;
	void ct()
	{
	p <= q; // Both converted to const void * before comparison
	pi <= pci; // Both converted to const int *const * before comparison
	}
	 --End Example]
	 */
	public void test5_9s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void *p;\n"); //$NON-NLS-1$
		buffer.append("const int *q;\n"); //$NON-NLS-1$
		buffer.append("int **pi;\n"); //$NON-NLS-1$
		buffer.append("const int *const *pci;\n"); //$NON-NLS-1$
		buffer.append("void ct()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("p <= q; // Both converted to const void * before comparison\n"); //$NON-NLS-1$
		buffer.append("pi <= pci; // Both converted to const int *const * before comparison\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 5.10-2):
	struct B {
	int f();
	};
	struct L : B { };
	struct R : B { };
	struct D : L, R { };
	int (B::*pb)() = &B::f;
	int (L::*pl)() = pb;
	int (R::*pr)() = pb;
	int (D::*pdl)() = pl;
	int (D::*pdr)() = pr;
	bool x = (pdl == pdr); // false
	 --End Example]
	 */
	public void test5_10s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("int f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct L : B { };\n"); //$NON-NLS-1$
		buffer.append("struct R : B { };\n"); //$NON-NLS-1$
		buffer.append("struct D : L, R { };\n"); //$NON-NLS-1$
		buffer.append("int (B::*pb)() = &B::f;\n"); //$NON-NLS-1$
		buffer.append("int (L::*pl)() = pb;\n"); //$NON-NLS-1$
		buffer.append("int (R::*pr)() = pb;\n"); //$NON-NLS-1$
		buffer.append("int (D::*pdl)() = pl;\n"); //$NON-NLS-1$
		buffer.append("int (D::*pdr)() = pr;\n"); //$NON-NLS-1$
		buffer.append("bool x = (pdl == pdr); // false\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 5.18-2):
	int f(int, int, int) {}
	int foo() {
	int a=0, t=1, c=2;
	f(a, (t=3, t+2), c);
	}
	 --End Example]
	 */
	public void test5_18s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(int, int, int) {}\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("int a=0, t=1, c=2;\n"); //$NON-NLS-1$
		buffer.append("f(a, (t=3, t+2), c);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 6.4-1):
	int foo() {
	int x=0;
	if (x)
	int i;
		
	if (x) {
	int i;
	}
	}
	 --End Example]
	 */
	public void test6_4s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("int x=0;\n"); //$NON-NLS-1$
		buffer.append("if (x)\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append("if (x) {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 6.5-3):
	int foo() {
	int x=5;
	while (-­x >= 0)
	int i;
	//can be equivalently rewritten as
	while (-­x >= 0) {
	int i;
	}
	}
	 --End Example]
	 */
	public void test6_5s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("int x=5;\n"); //$NON-NLS-1$
		buffer.append("while (--x >= 0)\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("//can be equivalently rewritten as\n"); //$NON-NLS-1$
		buffer.append("while (--x >= 0) {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 6.5.1-2):
	struct A {
	int val;
	A(int i) : val(i) { }
	~A() { }
	operator bool() { return val != 0; }
	};
	
	int foo() {
	int i = 1;
	while (A a = i) {
	//...
	i = 0;
	}
	}	
	 --End Example]
	 */
	public void test6_5_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("int val;\n"); //$NON-NLS-1$
		buffer.append("A(int i) : val(i) { }\n"); //$NON-NLS-1$
		buffer.append("~A() { }\n"); //$NON-NLS-1$
		buffer.append("operator bool() { return val != 0; }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("int i = 1;\n"); //$NON-NLS-1$
		buffer.append("while (A a = i) {\n"); //$NON-NLS-1$
		buffer.append("//...\n"); //$NON-NLS-1$
		buffer.append("i = 0;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 6.5.3-3):
	int foo() {
	int i = 42;
	int a[10];
	for (int i = 0; i < 10; i++)
	a[i] = i;
	int j = i; // j = 42
	}
	 --End Example]
	 */
	public void test6_5_3s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("int i = 42;\n"); //$NON-NLS-1$
		buffer.append("int a[10];\n"); //$NON-NLS-1$
		buffer.append("for (int i = 0; i < 10; i++)\n"); //$NON-NLS-1$
		buffer.append("a[i] = i;\n"); //$NON-NLS-1$
		buffer.append("int j = i; // j = 42\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 6.7-3):
	void f()
	{
	// ...
	goto lx; // illformed: jump into scope of a
	// ...
	ly:
	X a = 1;
	// ...
	lx:
	goto ly; // OK, jump implies destructor
	// call for a followed by construction
	// again immediately following label ly
	}
	 --End Example]
	 */
	public void test6_7s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 6.7-4):
	int foo(int i)
	{
	static int s = foo(2*i); // recursive call – undefined
	return i+1;
	}
	 --End Example]
	 */
	public void test6_7s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo(int i)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("static int s = foo(2*i); // recursive call – undefined\n"); //$NON-NLS-1$
		buffer.append("return i+1;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 6.8-1):
	int foo() {
	T(a)­>m = 7; // expressionstatement
	T(a)++; //expressionstatement
	T(a,5)<<c; //expressionstatement
	T(*d)(int); //declaration
	T(e)[5]; //declaration
	T(f) = { 1, 2 }; // declaration
	T(*g)(double(3)); // declaration
	}
	 --End Example]
	 */
	public void test6_8s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("T(a)->m = 7; // expressionstatement\n"); //$NON-NLS-1$
		buffer.append("T(a)++; //expressionstatement\n"); //$NON-NLS-1$
		buffer.append("T(a,5)<<c; //expressionstatement\n"); //$NON-NLS-1$
		buffer.append("T(*d)(int); //declaration\n"); //$NON-NLS-1$
		buffer.append("T(e)[5]; //declaration\n"); //$NON-NLS-1$
		buffer.append("T(f) = { 1, 2 }; // declaration\n"); //$NON-NLS-1$
		buffer.append("T(*g)(double(3)); // declaration\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 6.8-3):
	struct T1 {
	T1 operator()(int x) { return T1(x); }
	int operator=(int x) { return x; }
	T1(int) { }
	};
	struct T2 { T2(int){ } };
	int a, (*(*b)(T2))(int), c, d;
	void f() {
	// disambiguation requires this to be parsed
	// as a declaration
	T1(a) = 3,
	T2(4), // T2 will be declared as
	(*(*b)(T2(c)))(int(d)); // a variable of type T1
	// but this will not allow
	// the last part of the
	// declaration to parse
	// properly since it depends
	// on T2 being a typename
	}
	 --End Example]
	 */
	public void test6_8s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct T1 {\n"); //$NON-NLS-1$
		buffer.append("T1 operator()(int x) { return T1(x); }\n"); //$NON-NLS-1$
		buffer.append("int operator=(int x) { return x; }\n"); //$NON-NLS-1$
		buffer.append("T1(int) { }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct T2 { T2(int){ } };\n"); //$NON-NLS-1$
		buffer.append("int a, (*(*b)(T2))(int), c, d;\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("// disambiguation requires this to be parsed\n"); //$NON-NLS-1$
		buffer.append("// as a declaration\n"); //$NON-NLS-1$
		buffer.append("T1(a) = 3,\n"); //$NON-NLS-1$
		buffer.append("T2(4), // T2 will be declared as\n"); //$NON-NLS-1$
		buffer.append("(*(*b)(T2(c)))(int(d)); // a variable of type T1\n"); //$NON-NLS-1$
		buffer.append("// but this will not allow\n"); //$NON-NLS-1$
		buffer.append("// the last part of the\n"); //$NON-NLS-1$
		buffer.append("// declaration to parse\n"); //$NON-NLS-1$
		buffer.append("// properly since it depends\n"); //$NON-NLS-1$
		buffer.append("// on T2 being a typename\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1-2):
	 typedef char* Pc;
	void f(const Pc); // void f(char* const) (not const char*)
	void g(const int Pc); // void g(const int)
	 --End Example]
	 */
	public void test7_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef char* Pc;\n"); //$NON-NLS-1$
		buffer.append("void f(const Pc); // void f(char* const) (not const char*)\n"); //$NON-NLS-1$
		buffer.append("void g(const int Pc); // void g(const int)\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1-3):
	void h(unsigned Pc); // void h(unsigned int)
	void k(unsigned int Pc); // void k(unsigned int)
	 --End Example]
	 */
	public void test7_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void h(unsigned Pc); // void h(unsigned int)\n"); //$NON-NLS-1$
		buffer.append("void k(unsigned int Pc); // void k(unsigned int)\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1.1-7):
	static char* f(); // f() has internal linkage
	char* f() // f() still has internal linkage
	{ //
	}
	char* g(); // g() has external linkage
	static char* g() // error: inconsistent linkage
	{ // 
	}
	void h();
	inline void h(); // external linkage
	inline void l();
	void l(); // external linkage
	inline void m();
	extern void m(); // external linkage
	static void n();
	inline void n(); // internal linkage
	static int a; // a has internal linkage
	int a; // error: two definitions
	static int b; // b has internal linkage
	extern int b; // b still has internal linkage
	int c; // c has external linkage
	static int c; // error: inconsistent linkage
	extern int d; // d has external linkage
	static int d; // error: inconsistent linkage
	 --End Example]
	 */
	public void test7_1_1s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("static char* f(); // f() has internal linkage\n"); //$NON-NLS-1$
		buffer.append("char* f() // f() still has internal linkage\n"); //$NON-NLS-1$
		buffer.append("{ //\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("char* g(); // g() has external linkage\n"); //$NON-NLS-1$
		buffer.append("static char* g() // error: inconsistent linkage\n"); //$NON-NLS-1$
		buffer.append("{ // \n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void h();\n"); //$NON-NLS-1$
		buffer.append("inline void h(); // external linkage\n"); //$NON-NLS-1$
		buffer.append("inline void l();\n"); //$NON-NLS-1$
		buffer.append("void l(); // external linkage\n"); //$NON-NLS-1$
		buffer.append("inline void m();\n"); //$NON-NLS-1$
		buffer.append("extern void m(); // external linkage\n"); //$NON-NLS-1$
		buffer.append("static void n();\n"); //$NON-NLS-1$
		buffer.append("inline void n(); // internal linkage\n"); //$NON-NLS-1$
		buffer.append("static int a; // a has internal linkage\n"); //$NON-NLS-1$
		buffer.append("int a; // error: two definitions\n"); //$NON-NLS-1$
		buffer.append("static int b; // b has internal linkage\n"); //$NON-NLS-1$
		buffer.append("extern int b; // b still has internal linkage\n"); //$NON-NLS-1$
		buffer.append("int c; // c has external linkage\n"); //$NON-NLS-1$
		buffer.append("static int c; // error: inconsistent linkage\n"); //$NON-NLS-1$
		buffer.append("extern int d; // d has external linkage\n"); //$NON-NLS-1$
		buffer.append("static int d; // error: inconsistent linkage\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1.1-8a):
	struct S;
	extern S a;
	extern S f();
	extern void g(S);
	void h()
	{
	g(a); //error: S is incomplete
	f(); //error: S is incomplete
	}
	 --End Example]
	 */
	public void test7_1_1s8a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct S;\n"); //$NON-NLS-1$
		buffer.append("extern S a;\n"); //$NON-NLS-1$
		buffer.append("extern S f();\n"); //$NON-NLS-1$
		buffer.append("extern void g(S);\n"); //$NON-NLS-1$
		buffer.append("void h()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("g(a); //error: S is incomplete\n"); //$NON-NLS-1$
		buffer.append("f(); //error: S is incomplete\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1.1-8b):
	class X {
	mutable const int* p; // OK
	mutable int* const q; // illformed
	};
	 --End Example]
	 */
	public void test7_1_1s8b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("mutable const int* p; // OK\n"); //$NON-NLS-1$
		buffer.append("mutable int* const q; // illformed\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1.3-1):
	typedef int MILES, *KLICKSP;
	MILES distance;
	extern KLICKSP metricp;
	 --End Example]
	 */
	public void test7_1_3s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int MILES, *KLICKSP;\n"); //$NON-NLS-1$
		buffer.append("MILES distance;\n"); //$NON-NLS-1$
		buffer.append("extern KLICKSP metricp;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test7_1_3s2() throws Exception { 
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef struct s { //\n"); //$NON-NLS-1$
		buffer.append("} s;\n"); //$NON-NLS-1$
		buffer.append("typedef int I;\n"); //$NON-NLS-1$
		buffer.append("typedef int I;\n"); //$NON-NLS-1$
		buffer.append("typedef I I;\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}

	
	/**
	 [--Start Example(CPP 7.1.3-3a):
	class complex { //
	};
	typedef int complex; // error: redefinition
	 --End Example]
	 */
	public void test7_1_3s3a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class complex { // \n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("typedef int complex; // error: redefinition\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1.3-3b):
	typedef int complex;
	class complex { // 
	}; // error: redefinition
	 --End Example]
	 */
	public void test7_1_3s3b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int complex;\n"); //$NON-NLS-1$
		buffer.append("class complex { // \n"); //$NON-NLS-1$
		buffer.append("}; // error: redefinition\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1.3-4):
	struct S {
	S();
	~S();
	};
	typedef struct S T;
	S a = T(); // OK
	struct T * p; // error
	 --End Example]
	 */
	public void test7_1_3s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("S();\n"); //$NON-NLS-1$
		buffer.append("~S();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("typedef struct S T;\n"); //$NON-NLS-1$
		buffer.append("S a = T(); // OK\n"); //$NON-NLS-1$
		buffer.append("struct T * p; // error\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1.3-5a):
	typedef struct { } *ps, S; // S is the class name for linkage purposes
	 --End Example]
	 */
	public void test7_1_3s5a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef struct { } *ps, S; // S is the class name for linkage purposes\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1.3-5b):
	typedef struct {
	S(); //error: requires a return type because S is
	// an ordinary member function, not a constructor
	} S;
	 --End Example]
	 */
	public void test7_1_3s5b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef struct {\n"); //$NON-NLS-1$
		buffer.append("S(); //error: requires a return type because S is\n"); //$NON-NLS-1$
		buffer.append("// an ordinary member function, not a constructor\n"); //$NON-NLS-1$
		buffer.append("} S;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1.5.1-5):
	int foo() {
	const int ci = 3; // cvqualified (initialized as required)
	ci = 4; // illformed: attempt to modify const
	int i = 2; // not cvqualified
	const int* cip; // pointer to const int
	cip = &i; // OK: cvqualified access path to unqualified
	*cip = 4; // illformed: attempt to modify through ptr to const
	int* ip;
	ip = const_cast<int*>(cip); // cast needed to convert const int* to int*
	*ip = 4; // defined: *ip points to i, a nonconst object
	const int* ciq = new const int (3); // initialized as required
	int* iq = const_cast<int*>(ciq); // cast required
	*iq = 4; // undefined: modifies a const object
	}
	 --End Example]
	 */
	public void test7_1_5_1s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("const int ci = 3; // cvqualified (initialized as required)\n"); //$NON-NLS-1$
		buffer.append("ci = 4; // illformed: attempt to modify const\n"); //$NON-NLS-1$
		buffer.append("int i = 2; // not cvqualified\n"); //$NON-NLS-1$
		buffer.append("const int* cip; // pointer to const int\n"); //$NON-NLS-1$
		buffer.append("cip = &i; // OK: cvqualified access path to unqualified\n"); //$NON-NLS-1$
		buffer.append("*cip = 4; // illformed: attempt to modify through ptr to const\n"); //$NON-NLS-1$
		buffer.append("int* ip;\n"); //$NON-NLS-1$
		buffer.append("ip = const_cast<int*>(cip); // cast needed to convert const int* to int*\n"); //$NON-NLS-1$
		buffer.append("*ip = 4; // defined: *ip points to i, a nonconst object\n"); //$NON-NLS-1$
		buffer.append("const int* ciq = new const int (3); // initialized as required\n"); //$NON-NLS-1$
		buffer.append("int* iq = const_cast<int*>(ciq); // cast required\n"); //$NON-NLS-1$
		buffer.append("*iq = 4; // undefined: modifies a const object\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.1.5.1-6):
	class X {
	public:
	mutable int i;
	int j;
	};
	class Y {
	public:
	X x;
	Y();
	};
	
	int foo() {
	const Y y;
	y.x.i++; //wellformed: mutable member can be modified
	y.x.j++; //illformed: constqualified member modified
	Y* p = const_cast<Y*>(&y); // cast away constness of y
	p->x.i = 99; // wellformed: mutable member can be modified
	p->x.j = 99; // undefined: modifies a const member
	}
	 --End Example]
	 */
	public void test7_1_5_1s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("mutable int i;\n"); //$NON-NLS-1$
		buffer.append("int j;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Y {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("X x;\n"); //$NON-NLS-1$
		buffer.append("Y();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("const Y y;\n"); //$NON-NLS-1$
		buffer.append("y.x.i++; //wellformed: mutable member can be modified\n"); //$NON-NLS-1$
		buffer.append("y.x.j++; //illformed: constqualified member modified\n"); //$NON-NLS-1$
		buffer.append("Y* p = const_cast<Y*>(&y); // cast away constness of y\n"); //$NON-NLS-1$
		buffer.append("p->x.i = 99; // wellformed: mutable member can be modified\n"); //$NON-NLS-1$
		buffer.append("p->x.j = 99; // undefined: modifies a const member\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.2-2):
	enum { a, b, c=0 };
	enum { d, e, f=e+2 };
	 --End Example]
	 */
	public void test7_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("enum { a, b, c=0 };\n"); //$NON-NLS-1$
		buffer.append("enum { d, e, f=e+2 };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.2-3):
	 int foo() {
	 const int x = 12;
	{ enum { x = x }; }
	}
	 --End Example]
	 */
	public void test7_2s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("const int x = 12;\n"); //$NON-NLS-1$
		buffer.append("{ enum { x = x }; }\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.2-8):
	int foo() {
	enum color { red, yellow, green=20, blue };
	color col = red;
	color* cp = &col;
	if (*cp == blue) // ...
	return 0;
	}
	 --End Example]
	 */
	public void test7_2s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("enum color { red, yellow, green=20, blue };\n"); //$NON-NLS-1$
		buffer.append("color col = red;\n"); //$NON-NLS-1$
		buffer.append("color* cp = &col;\n"); //$NON-NLS-1$
		buffer.append("if (*cp == blue) // ...\n"); //$NON-NLS-1$
		buffer.append("return 0;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.2-10):
	class X {
	public:
	enum direction { left='l', right='r' };
	int f(int i)
	{ return i==left ? 0 : i==right ? 1 : 2; }
	};
	void g(X* p)
	{
	direction d; // error: direction not in scope
	int i;
	i = p->f(left); // error: left not in scope
	i = p>
	f(X::right); // OK
	i = p>
	f(p->left); // OK
	// ...
	}
	 --End Example]
	 */
	public void test7_2s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("enum direction { left='l', right='r' };\n"); //$NON-NLS-1$
		buffer.append("int f(int i)\n"); //$NON-NLS-1$
		buffer.append("{ return i==left ? 0 : i==right ? 1 : 2; }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void g(X* p)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("direction d; // error: direction not in scope\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("i = p->f(left); // error: left not in scope\n"); //$NON-NLS-1$
		buffer.append("i = p>\n"); //$NON-NLS-1$
		buffer.append("f(X::right); // OK\n"); //$NON-NLS-1$
		buffer.append("i = p>\n"); //$NON-NLS-1$
		buffer.append("f(p->left); // OK\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.1-5):
	namespace Outer {
	int i;
	namespace Inner {
	void f() { i++; } // Outer::i
	int i;
	void g() { i++; } // Inner::i
	}
	}
	 --End Example]
	 */
	public void test7_3_1s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace Outer {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("namespace Inner {\n"); //$NON-NLS-1$
		buffer.append("void f() { i++; } // Outer::i\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("void g() { i++; } // Inner::i\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.1-1):
	namespace { int i; } // unique::i
	void f() { i++; } // unique::i++
	namespace A {
	namespace {
	int i; // A::unique::i
	int j; // A::unique::j
	}
	void g() { i++; } // A::unique::i++
	}
	using namespace A;
	void h() {
	i++; //error: unique::i or A::unique::i
	A::i++; // A::unique::i
	j++; // A::unique::j
	}
	 --End Example]
	 */
	public void test7_3_1_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace { int i; } // unique::i\n"); //$NON-NLS-1$
		buffer.append("void f() { i++; } // unique::i++\n"); //$NON-NLS-1$
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("namespace {\n"); //$NON-NLS-1$
		buffer.append("int i; // A::unique::i\n"); //$NON-NLS-1$
		buffer.append("int j; // A::unique::j\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void g() { i++; } // A::unique::i++\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("using namespace A;\n"); //$NON-NLS-1$
		buffer.append("void h() {\n"); //$NON-NLS-1$
		buffer.append("i++; //error: unique::i or A::unique::i\n"); //$NON-NLS-1$
		buffer.append("A::i++; // A::unique::i\n"); //$NON-NLS-1$
		buffer.append("j++; // A::unique::j\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.1.2-1):
	namespace X {
	void f() { //
	}
	}
	 --End Example]
	 */
	public void test7_3_1_2s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace X {\n"); //$NON-NLS-1$
		buffer.append("void f() { //\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.1.2-2):
	namespace Q {
	namespace V {
	void f();
	}
	void V::f() { } // OK
	void V::g() { } // error: g() is not yet a member of V
	namespace V {
	void g();
	}
	}
	namespace R {
	void Q::V::g() {  } // error: R doesn’t enclose Q
	}
	 --End Example]
	 */
	public void test7_3_1_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace Q {\n"); //$NON-NLS-1$
		buffer.append("namespace V {\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void V::f() { } // OK\n"); //$NON-NLS-1$
		buffer.append("void V::g() { } // error: g() is not yet a member of V\n"); //$NON-NLS-1$
		buffer.append("namespace V {\n"); //$NON-NLS-1$
		buffer.append("void g();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace R {\n"); //$NON-NLS-1$
		buffer.append("void Q::V::g() {  } // error: R doesn’t enclose Q\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 2);
	}
	
	/**
	 [--Start Example(CPP 7.3.1.2-3):
	// Assume f and g have not yet been defined.
	void h(int);
	namespace A {
	class X {
	friend void f(X); // A::f is a friend
	class Y {
	friend void g(); // A::g is a friend
	friend void h(int); // A::h is a friend
	// ::h not considered
	};
	};
	// A::f, A::g and A::h are not visible here
	X x;
	void g() { f(x); } // definition of A::g
	void f(X) { } // definition of A::f
	void h(int) { } // definition of A::h
	// A::f, A::g and A::h are visible here and known to be friends
	}
	using A::x;
	void h()
	{
	A::f(x);
	A::X::f(x); //error: f is not a member of A::X
	A::X::Y::g(); // error: g is not a member of A::X::Y
	}
	 --End Example]
	 */
	public void test7_3_1_2s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("// Assume f and g have not yet been defined.\n"); //$NON-NLS-1$
		buffer.append("void h(int);\n"); //$NON-NLS-1$
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("friend void f(X); // A::f is a friend\n"); //$NON-NLS-1$
		buffer.append("class Y {\n"); //$NON-NLS-1$
		buffer.append("friend void g(); // A::g is a friend\n"); //$NON-NLS-1$
		buffer.append("friend void h(int); // A::h is a friend\n"); //$NON-NLS-1$
		buffer.append("// ::h not considered\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("// A::f, A::g and A::h are not visible here\n"); //$NON-NLS-1$
		buffer.append("X x;\n"); //$NON-NLS-1$
		buffer.append("void g() { f(x); } // definition of A::g\n"); //$NON-NLS-1$
		buffer.append("void f(X) { } // definition of A::f\n"); //$NON-NLS-1$
		buffer.append("void h(int) { } // definition of A::h\n"); //$NON-NLS-1$
		buffer.append("// A::f, A::g and A::h are visible here and known to be friends\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("using A::x;\n"); //$NON-NLS-1$
		buffer.append("void h()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("A::f(x);\n"); //$NON-NLS-1$
		buffer.append("A::X::f(x); //error: f is not a member of A::X\n"); //$NON-NLS-1$
		buffer.append("A::X::Y::g(); // error: g is not a member of A::X::Y\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 4);
	}
	
	/**
	 [--Start Example(CPP 7.3.2-3):
	namespace Company_with_very_long_name {  }
	namespace CWVLN = Company_with_very_long_name;
	namespace CWVLN = Company_with_very_long_name; // OK: duplicate
	namespace CWVLN = CWVLN;
	 --End Example]
	 */
	public void test7_3_2s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace Company_with_very_long_name {  }\n"); //$NON-NLS-1$
		buffer.append("namespace CWVLN = Company_with_very_long_name;\n"); //$NON-NLS-1$
		buffer.append("namespace CWVLN = Company_with_very_long_name; // OK: duplicate\n"); //$NON-NLS-1$
		buffer.append("namespace CWVLN = CWVLN;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.3-3):
	struct B {
	void f(char);
	void g(char);
	enum E { e };
	union { int x; };
	};
	struct D : B {
	using B::f;
	void f(int) { f('c'); } // calls B::f(char)
	void g(int) { g('c'); } // recursively calls D::g(int)
	};
	 --End Example]
	 */
	public void test7_3_3s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("void f(char);\n"); //$NON-NLS-1$
		buffer.append("void g(char);\n"); //$NON-NLS-1$
		buffer.append("enum E { e };\n"); //$NON-NLS-1$
		buffer.append("union { int x; };\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D : B {\n"); //$NON-NLS-1$
		buffer.append("using B::f;\n"); //$NON-NLS-1$
		buffer.append("void f(int) { f('c'); } // calls B::f(char)\n"); //$NON-NLS-1$
		buffer.append("void g(int) { g('c'); } // recursively calls D::g(int)\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.3-4):
	class C {
	int g();
	};
	class D2 : public B {
	using B::f; // OK: B is a base of D2
	using B::e; // OK: e is an enumerator of base B
	using B::x; // OK: x is a union member of base B
	using C::g; // error: C isn’t a base of D2
	};
	 --End Example]
	 */
	public void test7_3_3s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("int g();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D2 : public B {\n"); //$NON-NLS-1$
		buffer.append("using B::f; // OK: B is a base of D2\n"); //$NON-NLS-1$
		buffer.append("using B::e; // OK: e is an enumerator of base B\n"); //$NON-NLS-1$
		buffer.append("using B::x; // OK: x is a union member of base B\n"); //$NON-NLS-1$
		buffer.append("using C::g; // error: C isn’t a base of D2\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.3-5):
	class A {
	public:
	template <class T> void f(T);
	template <class T> struct X { };
	};
	class B : public A {
	public:
	using A::f<double>; // illformed
	using A::X<int>; // illformed
	};
	 --End Example]
	 */
	public void test7_3_3s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("template <class T> void f(T);\n"); //$NON-NLS-1$
		buffer.append("template <class T> struct X { };\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class B : public A {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("using A::f<double>; // illformed\n"); //$NON-NLS-1$
		buffer.append("using A::X<int>; // illformed\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.3-6):
	struct X {
	int i;
	static int s;
	};
	void f()
	{
	using X::i; // error: X::i is a class member
	// and this is not a member declaration.
	using X::s; // error: X::s is a class member
	// and this is not a member declaration.
	}
	 --End Example]
	 */
	public void test7_3_3s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("static int s;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("using X::i; // error: X::i is a class member\n"); //$NON-NLS-1$
		buffer.append("// and this is not a member declaration.\n"); //$NON-NLS-1$
		buffer.append("using X::s; // error: X::s is a class member\n"); //$NON-NLS-1$
		buffer.append("// and this is not a member declaration.\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.3-7):
	void f();
	namespace A {
	void g();
	}
	namespace X {
	using ::f; // global f
	using A::g; // A’s g
	}
	void h()
	{
	X::f(); //calls ::f
	X::g(); //calls A::g
	}
	 --End Example]
	 */
	public void test7_3_3s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("void g();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace X {\n"); //$NON-NLS-1$
		buffer.append("using ::f; // global f\n"); //$NON-NLS-1$
		buffer.append("using A::g; // A’s g\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void h()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("X::f(); //calls ::f\n"); //$NON-NLS-1$
		buffer.append("X::g(); //calls A::g\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.3-8):
	namespace A {
	int i;
	}
	namespace A1 {
	using A::i;
	using A::i; // OK: double declaration
	}
	void f()
	{
	using A::i;
	using A::i; // error: double declaration
	}
	class B {
	public:
	int i;
	};
	class X : public B {
	using B::i;
	using B::i; // error: double member declaration
	};
	 --End Example]
	 */
	public void test7_3_3s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace A1 {\n"); //$NON-NLS-1$
		buffer.append("using A::i;\n"); //$NON-NLS-1$
		buffer.append("using A::i; // OK: double declaration\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("using A::i;\n"); //$NON-NLS-1$
		buffer.append("using A::i; // error: double declaration\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("class B {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class X : public B {\n"); //$NON-NLS-1$
		buffer.append("using B::i;\n"); //$NON-NLS-1$
		buffer.append("using B::i; // error: double member declaration\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.3-9):
	namespace A {
	void f(int);
	}
	using A::f; // f is a synonym for A::f;
	// that is, for A::f(int).
	namespace A {
	void f(char);
	}
	void foo()
	{
	f('a'); //calls f(int),
	} //even though f(char) exists.
	void bar()
	{
	using A::f; // f is a synonym for A::f;
	// that is, for A::f(int) and A::f(char).
	f('a'); //calls f(char)
	}
	 --End Example]
	 */
	public void test7_3_3s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("using A::f; // f is a synonym for A::f;\n"); //$NON-NLS-1$
		buffer.append("// that is, for A::f(int).\n"); //$NON-NLS-1$
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("void f(char);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void foo()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f('a'); //calls f(int),\n"); //$NON-NLS-1$
		buffer.append("} //even though f(char) exists.\n"); //$NON-NLS-1$
		buffer.append("void bar()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("using A::f; // f is a synonym for A::f;\n"); //$NON-NLS-1$
		buffer.append("// that is, for A::f(int) and A::f(char).\n"); //$NON-NLS-1$
		buffer.append("f('a'); //calls f(char)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test7_3_3s10() throws Exception {
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
		buffer.append("//using B::i; // error: i declared twice\n"); //$NON-NLS-1$
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

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.3-11):
	namespace B {
	void f(int);
	void f(double);
	}
	namespace C {
	void f(int);
	void f(double);
	void f(char);
	}
	void h()
	{
	using B::f; // B::f(int) and B::f(double)
	using C::f; // C::f(int), C::f(double), and C::f(char)
	f('h'); //calls C::f(char)
	f(1); //error: ambiguous: B::f(int) or C::f(int) ?
	void f(int); // error:
	// f(int) conflicts with C::f(int) and B::f(int)
	}
	 --End Example]
	 */
	public void test7_3_3s11() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("void f(double);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace C {\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("void f(double);\n"); //$NON-NLS-1$
		buffer.append("void f(char);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void h()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("using B::f; // B::f(int) and B::f(double)\n"); //$NON-NLS-1$
		buffer.append("using C::f; // C::f(int), C::f(double), and C::f(char)\n"); //$NON-NLS-1$
		buffer.append("f('h'); //calls C::f(char)\n"); //$NON-NLS-1$
		buffer.append("f(1); //error: ambiguous: B::f(int) or C::f(int) ?\n"); //$NON-NLS-1$
		buffer.append("void f(int); // error:\n"); //$NON-NLS-1$
		buffer.append("// f(int) conflicts with C::f(int) and B::f(int)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.3-12):
	struct B {
	virtual void f(int);
	virtual void f(char);
	void g(int);
	void h(int);
	};
	struct D : B {
	using B::f;
	void f(int); // OK: D::f(int) overrides B::f(int);
	using B::g;
	void g(char); // OK
	using B::h;
	void h(int); // OK: D::h(int) hides B::h(int)
	};
	void k(D* p)
	{
	p->f(1); //calls D::f(int)
	p->f('a'); //calls B::f(char)
	p->g(1); //calls B::g(int)
	p->g('a'); //calls D::g(char)
	}
	 --End Example]
	 */
	public void test7_3_3s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("virtual void f(int);\n"); //$NON-NLS-1$
		buffer.append("virtual void f(char);\n"); //$NON-NLS-1$
		buffer.append("void g(int);\n"); //$NON-NLS-1$
		buffer.append("void h(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D : B {\n"); //$NON-NLS-1$
		buffer.append("using B::f;\n"); //$NON-NLS-1$
		buffer.append("void f(int); // OK: D::f(int) overrides B::f(int);\n"); //$NON-NLS-1$
		buffer.append("using B::g;\n"); //$NON-NLS-1$
		buffer.append("void g(char); // OK\n"); //$NON-NLS-1$
		buffer.append("using B::h;\n"); //$NON-NLS-1$
		buffer.append("void h(int); // OK: D::h(int) hides B::h(int)\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void k(D* p)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("p->f(1); //calls D::f(int)\n"); //$NON-NLS-1$
		buffer.append("p->f('a'); //calls B::f(char)\n"); //$NON-NLS-1$
		buffer.append("p->g(1); //calls B::g(int)\n"); //$NON-NLS-1$
		buffer.append("p->g('a'); //calls D::g(char)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.3-14):
	struct A { int x(); };
	struct B : A { };
	struct C : A {
	using A::x;
	int x(int);
	};
	struct D : B, C {
	using C::x;
	int x(double);
	};
	int f(D* d) {
	return d>
	x(); // ambiguous: B::x or C::x
	}
	 --End Example]
	 */
	public void test7_3_3s14() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A { int x(); };\n"); //$NON-NLS-1$
		buffer.append("struct B : A { };\n"); //$NON-NLS-1$
		buffer.append("struct C : A {\n"); //$NON-NLS-1$
		buffer.append("using A::x;\n"); //$NON-NLS-1$
		buffer.append("int x(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D : B, C {\n"); //$NON-NLS-1$
		buffer.append("using C::x;\n"); //$NON-NLS-1$
		buffer.append("int x(double);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int f(D* d) {\n"); //$NON-NLS-1$
		buffer.append("return d>\n"); //$NON-NLS-1$
		buffer.append("x(); // ambiguous: B::x or C::x\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.3-15):
	class A {
	private:
	void f(char);
	public:
	void f(int);
	protected:
	void g();
	};
	class B : public A {
	using A::f; // error: A::f(char) is inaccessible
	public:
	using A::g; // B::g is a public synonym for A::g
	};
	 --End Example]
	 */
	public void test7_3_3s15() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("private:\n"); //$NON-NLS-1$
		buffer.append("void f(char);\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("protected:\n"); //$NON-NLS-1$
		buffer.append("void g();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class B : public A {\n"); //$NON-NLS-1$
		buffer.append("using A::f; // error: A::f(char) is inaccessible\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("using A::g; // B::g is a public synonym for A::g\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.4-1):
	namespace A {
	int i;
	namespace B {
	namespace C {
	int i;
	}
	using namespace A::B::C;
	void f1() {
	i = 5; // OK, C::i visible in B and hides A::i
	}
	}
	namespace D {
	using namespace B;
	using namespace C;
	void f2() {
	i = 5; // ambiguous, B::C::i or A::i?
	}
	}
	void f3() {
	i = 5; // uses A::i
	}
	}
	void f4() {
	i = 5; // illformed; neither i is visible
	}
	 --End Example]
	 */
	public void test7_3_4s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("namespace C {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("using namespace A::B::C;\n"); //$NON-NLS-1$
		buffer.append("void f1() {\n"); //$NON-NLS-1$
		buffer.append("i = 5; // OK, C::i visible in B and hides A::i\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace D {\n"); //$NON-NLS-1$
		buffer.append("using namespace B;\n"); //$NON-NLS-1$
		buffer.append("using namespace C;\n"); //$NON-NLS-1$
		buffer.append("void f2() {\n"); //$NON-NLS-1$
		buffer.append("i = 5; // ambiguous, B::C::i or A::i?\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void f3() {\n"); //$NON-NLS-1$
		buffer.append("i = 5; // uses A::i\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void f4() {\n"); //$NON-NLS-1$
		buffer.append("i = 5; // illformed; neither i is visible\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.4-2a):
	namespace M {
	int i;
	}
	namespace N {
	int i;
	using namespace M;
	}
	void f()
	{
	using namespace N;
	i = 7; // error: both M::i and N::i are visible
	}
	 --End Example]
	 */
	public void test7_3_4s2a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace M {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("using namespace M;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("using namespace N;\n"); //$NON-NLS-1$
		buffer.append("i = 7; // error: both M::i and N::i are visible\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.4-2b):
	namespace A {
	int i;
	}
	namespace B {
	int i;
	int j;
	namespace C {
	namespace D {
	using namespace A;
	int j;
	int k;
	int a = i; // B::i hides A::i
	}
	using namespace D;
	int k = 89; // no problem yet
	int l = k; // ambiguous: C::k or D::k
	int m = i; // B::i hides A::i
	int n = j; // D::j hides B::j
	}
	}
	 --End Example]
	 */
	public void test7_3_4s2b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("int j;\n"); //$NON-NLS-1$
		buffer.append("namespace C {\n"); //$NON-NLS-1$
		buffer.append("namespace D {\n"); //$NON-NLS-1$
		buffer.append("using namespace A;\n"); //$NON-NLS-1$
		buffer.append("int j;\n"); //$NON-NLS-1$
		buffer.append("int k;\n"); //$NON-NLS-1$
		buffer.append("int a = i; // B::i hides A::i\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("using namespace D;\n"); //$NON-NLS-1$
		buffer.append("int k = 89; // no problem yet\n"); //$NON-NLS-1$
		buffer.append("int l = k; // ambiguous: C::k or D::k\n"); //$NON-NLS-1$
		buffer.append("int m = i; // B::i hides A::i\n"); //$NON-NLS-1$
		buffer.append("int n = j; // D::j hides B::j\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.4-5):
	namespace D {
	int d1;
	void f(char);
	}
	using namespace D;
	int d1; // OK: no conflict with D::d1
	namespace E {
	int e;
	void f(int);
	}
	namespace D { // namespace extension
	int d2;
	using namespace E;
	void f(int);
	}
	void f()
	{
	d1++; //error: ambiguous ::d1 or D::d1?
	::d1++; //OK
	D::d1++; //OK
	d2++; //OK: D::d2
	e++; //OK: E::e
	f(1); //error: ambiguous: D::f(int) or E::f(int)?
	f('a'); //OK: D::f(char)
	}
	 --End Example]
	 */
	public void test7_3_4s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace D {\n"); //$NON-NLS-1$
		buffer.append("int d1;\n"); //$NON-NLS-1$
		buffer.append("void f(char);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("using namespace D;\n"); //$NON-NLS-1$
		buffer.append("int d1; // OK: no conflict with D::d1\n"); //$NON-NLS-1$
		buffer.append("namespace E {\n"); //$NON-NLS-1$
		buffer.append("int e;\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace D { // namespace extension\n"); //$NON-NLS-1$
		buffer.append("int d2;\n"); //$NON-NLS-1$
		buffer.append("using namespace E;\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("d1++; //error: ambiguous ::d1 or D::d1?\n"); //$NON-NLS-1$
		buffer.append("::d1++; //OK\n"); //$NON-NLS-1$
		buffer.append("D::d1++; //OK\n"); //$NON-NLS-1$
		buffer.append("d2++; //OK: D::d2\n"); //$NON-NLS-1$
		buffer.append("e++; //OK: E::e\n"); //$NON-NLS-1$
		buffer.append("f(1); //error: ambiguous: D::f(int) or E::f(int)?\n"); //$NON-NLS-1$
		buffer.append("f('a'); //OK: D::f(char)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 7.5-3):
	complex sqrt(complex); // C++ linkage by default
	extern "C" {
	double sqrt(double); // C linkage
	}
	 --End Example]
	 */
	public void test7_5s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("complex sqrt(complex); // C++ linkage by default\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" {\n"); //$NON-NLS-1$
		buffer.append("double sqrt(double); // C linkage\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 7.5-4a):
	extern "C" void f1(void(*pf)(int));
	// the name f1 and its function type have C language
	// linkage; pf is a pointer to a C function
	extern "C" typedef void FUNC();
	FUNC f2; // the name f2 has C++ language linkage and the
	// function’s type has C language linkage
	extern "C" FUNC f3; // the name of function f3 and the function’s type
	// have C language linkage
	void (*pf2)(FUNC*); // the name of the variable pf2 has C++ linkage and
	// the type of pf2 is pointer to C++ function that
	// takes one parameter of type pointer to C function
	 --End Example]
	 */
	public void test7_5s4a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern \"C\" void f1(void(*pf)(int));\n"); //$NON-NLS-1$
		buffer.append("// the name f1 and its function type have C language\n"); //$NON-NLS-1$
		buffer.append("// linkage; pf is a pointer to a C function\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" typedef void FUNC();\n"); //$NON-NLS-1$
		buffer.append("FUNC f2; // the name f2 has C++ language linkage and the\n"); //$NON-NLS-1$
		buffer.append("// function’s type has C language linkage\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" FUNC f3; // the name of function f3 and the function’s type\n"); //$NON-NLS-1$
		buffer.append("// have C language linkage\n"); //$NON-NLS-1$
		buffer.append("void (*pf2)(FUNC*); // the name of the variable pf2 has C++ linkage and\n"); //$NON-NLS-1$
		buffer.append("// the type of pf2 is pointer to C++ function that\n"); //$NON-NLS-1$
		buffer.append("// takes one parameter of type pointer to C function\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.5-4b):
	extern "C" typedef void FUNC_c();
	class C {
	void mf1(FUNC_c*); // the name of the function mf1 and the member
	// function’s type have C++ language linkage; the
	// parameter has type pointer to C function
	FUNC_c mf2; // the name of the function mf2 and the member
	// function’s type have C++ language linkage
	static FUNC_c* q; // the name of the data member q has C++ language
	// linkage and the data member’s type is pointer to
	// C function
	};
	extern "C" {
	class X {
	void mf(); // the name of the function mf and the member
	// function’s type have C++ language linkage
	void mf2(void(*)()); // the name of the function mf2 has C++ language
	// linkage; the parameter has type pointer to
	// C function
	};
	}
	 --End Example]
	 */
	public void test7_5s4b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern \"C\" typedef void FUNC_c();\n"); //$NON-NLS-1$
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("void mf1(FUNC_c*); // the name of the function mf1 and the member\n"); //$NON-NLS-1$
		buffer.append("// function’s type have C++ language linkage; the\n"); //$NON-NLS-1$
		buffer.append("// parameter has type pointer to C function\n"); //$NON-NLS-1$
		buffer.append("FUNC_c mf2; // the name of the function mf2 and the member\n"); //$NON-NLS-1$
		buffer.append("// function’s type have C++ language linkage\n"); //$NON-NLS-1$
		buffer.append("static FUNC_c* q; // the name of the data member q has C++ language\n"); //$NON-NLS-1$
		buffer.append("// linkage and the data member’s type is pointer to\n"); //$NON-NLS-1$
		buffer.append("// C function\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" {\n"); //$NON-NLS-1$
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("void mf(); // the name of the function mf and the member\n"); //$NON-NLS-1$
		buffer.append("// function’s type have C++ language linkage\n"); //$NON-NLS-1$
		buffer.append("void mf2(void(*)()); // the name of the function mf2 has C++ language\n"); //$NON-NLS-1$
		buffer.append("// linkage; the parameter has type pointer to\n"); //$NON-NLS-1$
		buffer.append("// C function\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.5-6):
	namespace A {
	extern "C" int f();
	extern "C" int g() { return 1; }
	extern "C" int h();
	}
	namespace B {
	extern "C" int f(); // A::f and B::f refer
	// to the same function
	extern "C" int g() { return 1; } // illformed,
	// the function g
	// with C language linkage
	// has two definitions
	}
	int A::f() { return 98; } // definition for the function f
	// with C language linkage
	extern "C" int h() { return 97; }
	// definition for the function h
	// with C language linkage
	// A::h and ::h refer to the same function
	 --End Example]
	 */
	public void test7_5s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" int f();\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" int g() { return 1; }\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" int h();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" int f(); // A::f and B::f refer\n"); //$NON-NLS-1$
		buffer.append("// to the same function\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" int g() { return 1; } // illformed,\n"); //$NON-NLS-1$
		buffer.append("// the function g\n"); //$NON-NLS-1$
		buffer.append("// with C language linkage\n"); //$NON-NLS-1$
		buffer.append("// has two definitions\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("int A::f() { return 98; } // definition for the function f\n"); //$NON-NLS-1$
		buffer.append("// with C language linkage\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" int h() { return 97; }\n"); //$NON-NLS-1$
		buffer.append("// definition for the function h\n"); //$NON-NLS-1$
		buffer.append("// with C language linkage\n"); //$NON-NLS-1$
		buffer.append("// A::h and ::h refer to the same function\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.5-7a):
	extern "C" double f();
	static double f(); // error
	 --End Example]
	 */
	public void test7_5s7a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern \"C\" double f();\n"); //$NON-NLS-1$
		buffer.append("static double f(); // error\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.5-7b):
	extern "C" int i; // declaration
	extern "C" {
	int i; // definition
	}
	 --End Example]
	 */
	public void test7_5s7b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern \"C\" int i; // declaration\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" {\n"); //$NON-NLS-1$
		buffer.append("int i; // definition\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.5-7c):
	extern "C" static void f(); // error
	 --End Example]
	 */
	public void test7_5s7c() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern \"C\" static void f(); // error\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.1-1):
	int i;
	int *pi;
	int *p[3];
	int (*p3i)[3];
	int *f();
	int (*pf)(double);
	 --End Example]
	 */
	public void test8_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("int *pi;\n"); //$NON-NLS-1$
		buffer.append("int *p[3];\n"); //$NON-NLS-1$
		buffer.append("int (*p3i)[3];\n"); //$NON-NLS-1$
		buffer.append("int *f();\n"); //$NON-NLS-1$
		buffer.append("int (*pf)(double);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.2-1):
	struct S {
	S(int);
	};
	void foo(double a)
	{
	S w(int(a)); // function declaration
	S x(int()); // function declaration
	S y((int)a); // object declaration
	S z = int(a); // object declaration
	}
	 --End Example]
	 */
	public void test8_2s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("S(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void foo(double a)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("S w(int(a)); // function declaration\n"); //$NON-NLS-1$
		buffer.append("S x(int()); // function declaration\n"); //$NON-NLS-1$
		buffer.append("S y((int)a); // object declaration\n"); //$NON-NLS-1$
		buffer.append("S z = int(a); // object declaration\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.2-5):
	void foo()
	{
	sizeof(int(1)); // expression
	// sizeof(int()); // typeid (illformed)
	}
	 --End Example]
	 */
	public void test8_2s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void foo()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("sizeof(int(1)); // expression\n"); //$NON-NLS-1$
		buffer.append("// sizeof(int()); // typeid (illformed)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.2-6):
	void foo()
	{
	(int(1)); //expression
	// (int())1; //typeid (illformed)
	}
	 --End Example]
	 */
	public void test8_2s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void foo()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("(int(1)); //expression\n"); //$NON-NLS-1$
		buffer.append("// (int())1; //typeid (illformed)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.2-7b):
	class C { };
	void h(int *(C[10])); // void h(int *(*_fp)(C _parm[10]));
	// not: void h(int *C[10]);
	 --End Example]
	 */
	public void test8_2s7b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class C { };\n"); //$NON-NLS-1$
		buffer.append("void h(int *(C[10])); // void h(int *(*_fp)(C _parm[10]));\n"); //$NON-NLS-1$
		buffer.append("// not: void h(int *C[10]);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3-1):
	namespace A {
	struct B {
	void f();
	};
	void A::B::f() { } // illformed: the declarator must not be
	// qualified with A::
	}
	 --End Example]
	 */
	public void test8_3s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void A::B::f() { } // illformed: the declarator must not be\n"); //$NON-NLS-1$
		buffer.append("// qualified with A::\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3-4):
	int unsigned i;
	--End Example]
	 */
	public void test8_3s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int unsigned i;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.1-2a):
	const int ci = 10, *pc = &ci, *const cpc = pc, **ppc;
	int i, *p, *const cp = &i;
	
	int f() {
	i = ci;
	*cp = ci;
	pc++;
	pc = cpc;
	pc = p;
	ppc = &pc;
	}
	 --End Example]
	 */
	public void test8_3_1s2a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("const int ci = 10, *pc = &ci, *const cpc = pc, **ppc;\n"); //$NON-NLS-1$
		buffer.append("int i, *p, *const cp = &i;\n"); //$NON-NLS-1$
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("i = ci;\n"); //$NON-NLS-1$
		buffer.append("*cp = ci;\n"); //$NON-NLS-1$
		buffer.append("pc++;\n"); //$NON-NLS-1$
		buffer.append("pc = cpc;\n"); //$NON-NLS-1$
		buffer.append("pc = p;\n"); //$NON-NLS-1$
		buffer.append("ppc = &pc;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.1-2b):
	const int ci = 10, *pc = &ci, *const cpc = pc, **ppc;
	int i, *p, *const cp = &i;
	int f() {
	ci = 1; // error
	ci++; //error
	*pc = 2; // error
	cp = &ci; // error
	cpc++; //error
	p = pc; // error
	ppc = &p; // error
	}
	 --End Example]
	 */
	public void test8_3_1s2b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("const int ci = 10, *pc = &ci, *const cpc = pc, **ppc;\n"); //$NON-NLS-1$
		buffer.append("int i, *p, *const cp = &i;\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("ci = 1; // error\n"); //$NON-NLS-1$
		buffer.append("ci++; //error\n"); //$NON-NLS-1$
		buffer.append("*pc = 2; // error\n"); //$NON-NLS-1$
		buffer.append("cp = &ci; // error\n"); //$NON-NLS-1$
		buffer.append("cpc++; //error\n"); //$NON-NLS-1$
		buffer.append("p = pc; // error\n"); //$NON-NLS-1$
		buffer.append("ppc = &p; // error\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.1-2c):
	const int ci = 10, *pc = &ci, *const cpc = pc, **ppc;
	int i, *p, *const cp = &i;
	int f() {
	*ppc = &ci; // OK, but would make p point to ci ...
	*p = 5; // clobber ci
	}
	 --End Example]
	 */
	public void test8_3_1s2c() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("const int ci = 10, *pc = &ci, *const cpc = pc, **ppc;\n"); //$NON-NLS-1$
		buffer.append("int i, *p, *const cp = &i;\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("*ppc = &ci; // OK, but would make p point to ci ...\n"); //$NON-NLS-1$
		buffer.append("*p = 5; // clobber ci\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.2-1):
	typedef int& A;
	const A aref = 3; // illformed;
	// nonconst reference initialized with rvalue
	 --End Example]
	 */
	public void test8_3_2s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int& A;\n"); //$NON-NLS-1$
		buffer.append("const A aref = 3; // illformed;\n"); //$NON-NLS-1$
		buffer.append("// nonconst reference initialized with rvalue\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.2-2):
	void f(double& a) { a += 3.14; }
	// ...
	int foo() {
	double d = 0;
	f(d);
	int v[20];
	// ...
	int& g(int i) { return v[i]; }
	// ...
	g(3) = 7;
	}
	struct link {
	link* next;
	};
	link* first;
	void h(link*& p) // p is a reference to pointer
	{
	p->next = first;
	first = p;
	p = 0;
	}
	void k()
	{
	link* q = new link;
	h(q);
	}
	 --End Example]
	 */
	public void test8_3_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f(double& a) { a += 3.14; }\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("double d = 0;\n"); //$NON-NLS-1$
		buffer.append("f(d);\n"); //$NON-NLS-1$
		buffer.append("int v[20];\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("int& g(int i) { return v[i]; }\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("g(3) = 7;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("struct link {\n"); //$NON-NLS-1$
		buffer.append("link* next;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("link* first;\n"); //$NON-NLS-1$
		buffer.append("void h(link*& p) // p is a reference to pointer\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("p->next = first;\n"); //$NON-NLS-1$
		buffer.append("first = p;\n"); //$NON-NLS-1$
		buffer.append("p = 0;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void k()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("link* q = new link;\n"); //$NON-NLS-1$
		buffer.append("h(q);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.3-2):
	class X {
	public:
	void f(int);
	int a;
	};
	class Y;
	
	void f() {
	int X::* pmi = &X::a;
	void (X::* pmf)(int) = &X::f;
	double X::* pmd;
	char Y::* pmc;
	X obj;
	//...
	obj.*pmi = 7; // assign 7 to an integer
	// member of obj
	(obj.*pmf)(7); //call a function member of obj
	// with the argument 7
	}
	 --End Example]
	 */
	public void test8_3_3s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Y;\n"); //$NON-NLS-1$
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("int X::* pmi = &X::a;\n"); //$NON-NLS-1$
		buffer.append("void (X::* pmf)(int) = &X::f;\n"); //$NON-NLS-1$
		buffer.append("double X::* pmd;\n"); //$NON-NLS-1$
		buffer.append("char Y::* pmc;\n"); //$NON-NLS-1$
		buffer.append("X obj;\n"); //$NON-NLS-1$
		buffer.append("//...\n"); //$NON-NLS-1$
		buffer.append("obj.*pmi = 7; // assign 7 to an integer\n"); //$NON-NLS-1$
		buffer.append("// member of obj\n"); //$NON-NLS-1$
		buffer.append("(obj.*pmf)(7); //call a function member of obj\n"); //$NON-NLS-1$
		buffer.append("// with the argument 7\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.4-1):
	typedef int A[5], AA[2][3];
	typedef const A CA; // type is ‘‘array of 5 const int’’
	typedef const AA CAA; // type is ‘‘array of 2 array of 3 const int’’
	 --End Example]
	 */
	public void test8_3_4s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int A[5], AA[2][3];\n"); //$NON-NLS-1$
		buffer.append("typedef const A CA; // type is ‘‘array of 5 const int’’\n"); //$NON-NLS-1$
		buffer.append("typedef const AA CAA; // type is ‘‘array of 2 array of 3 const int’’\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.4-4):
	float fa[17], *afp[17];
	static int x3d[3][5][7];
	 --End Example]
	 */
	public void test8_3_4s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("float fa[17], *afp[17];\n"); //$NON-NLS-1$
		buffer.append("static int x3d[3][5][7];\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.4-8):
	int x[3][5];
	 --End Example]
	 */
	public void test8_3_4s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int x[3][5];\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.5-2):
	int printf(const char*, ...);
	int f() {
	int a=1, b=0;
	printf("hello world");
	printf("a=%d b=%d", a, b);
	}
	 --End Example]
	 */
	public void test8_3_5s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int printf(const char*, ...);\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("int a=1, b=0;\n"); //$NON-NLS-1$
		buffer.append("printf(\"hello world\");\n"); //$NON-NLS-1$
		buffer.append("printf(\"a=%d b=%d\", a, b);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.5-4):
	typedef void F();
	struct S {
	const F f; // illformed:
	// not equivalent to: void f() const;
	};
	 --End Example]
	 */
	public void test8_3_5s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef void F();\n"); //$NON-NLS-1$
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("const F f; // illformed:\n"); //$NON-NLS-1$
		buffer.append("// not equivalent to: void f() const;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.5-5):
	#define FILE int
	int fseek(FILE*, long, int);
	 --End Example]
	 */
	public void test8_3_5s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define FILE int\n"); //$NON-NLS-1$
		buffer.append("int fseek(FILE*, long, int);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.5-7a):
	typedef void F();
	F fv; // OK: equivalent to void fv();
	// F fv { } // illformed
	void fv() { } // OK: definition of fv
	 --End Example]
	 */
	public void test8_3_5s7a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef void F();\n"); //$NON-NLS-1$
		buffer.append("F fv; // OK: equivalent to void fv();\n"); //$NON-NLS-1$
		buffer.append("// F fv { } // illformed\n"); //$NON-NLS-1$
		buffer.append("void fv() { } // OK: definition of fv\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.5-7b):
	typedef int FIC(int) const;
	FIC f; // illformed:
	//does not declare a member function
	struct S {
	FIC f; // OK
	};
	FIC S::*pm = &S::f; // OK
	 --End Example]
	 */
	public void test8_3_5s7b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int FIC(int) const;\n"); //$NON-NLS-1$
		buffer.append("FIC f; // illformed:\n"); //$NON-NLS-1$
		buffer.append("//does not declare a member function\n"); //$NON-NLS-1$
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("FIC f; // OK\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("FIC S::*pm = &S::f; // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.5-9a):
	int i,
	*pi,
	f(),
	*fpi(int),
	(*pif)(const char*, const char*);
	(*fpif(int))(int);
	 --End Example]
	 */
	public void test8_3_5s9a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int i,\n"); //$NON-NLS-1$
		buffer.append("*pi,\n"); //$NON-NLS-1$
		buffer.append("f(),\n"); //$NON-NLS-1$
		buffer.append("*fpi(int),\n"); //$NON-NLS-1$
		buffer.append("(*pif)(const char*, const char*);\n"); //$NON-NLS-1$
		buffer.append("(*fpif(int))(int);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.5-9b):
	typedef int IFUNC(int);
	IFUNC* fpif(int);
	 --End Example]
	 */
	public void test8_3_5s9b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int IFUNC(int);\n"); //$NON-NLS-1$
		buffer.append("IFUNC* fpif(int);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.6-2):
	void point(int = 3, int = 4);
	void f() {
	point(1,2); point(1); point();
	}
	 --End Example]
	 */
	public void test8_3_6s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void point(int = 3, int = 4);\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("point(1,2); point(1); point();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.6-4):
	void f(int, int);
	void f(int, int = 7);
	void h()
	{
	f(3); //OK, calls f(3, 7)
	void f(int = 1, int); // error: does not use default
	// from surrounding scope
	}
	void m()
	{
	void f(int, int); // has no defaults
	f(4); //error: wrong number of arguments
	void f(int, int = 5); // OK
	f(4); //OK, calls f(4, 5);
	void f(int, int = 5); // error: cannot redefine, even to
	// same value
	}
	void n()
	{
	f(6); //OK, calls f(6, 7)
	}
	 --End Example]
	 */
	public void test8_3_6s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f(int, int);\n"); //$NON-NLS-1$
		buffer.append("void f(int, int = 7);\n"); //$NON-NLS-1$
		buffer.append("void h()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(3); //OK, calls f(3, 7)\n"); //$NON-NLS-1$
		buffer.append("void f(int = 1, int); // error: does not use default\n"); //$NON-NLS-1$
		buffer.append("// from surrounding scope\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void m()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("void f(int, int); // has no defaults\n"); //$NON-NLS-1$
		buffer.append("f(4); //error: wrong number of arguments\n"); //$NON-NLS-1$
		buffer.append("void f(int, int = 5); // OK\n"); //$NON-NLS-1$
		buffer.append("f(4); //OK, calls f(4, 5);\n"); //$NON-NLS-1$
		buffer.append("void f(int, int = 5); // error: cannot redefine, even to\n"); //$NON-NLS-1$
		buffer.append("// same value\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void n()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(6); //OK, calls f(6, 7)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.6-5):
	int a = 1;
	int f(int);
	int g(int x = f(a)); // default argument: f(::a)
	void h() {
	a = 2;
	{
	int a = 3;
	g(); // g(f(::a))
	}
	}
	 --End Example]
	 */
	public void test8_3_6s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int a = 1;\n"); //$NON-NLS-1$
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("int g(int x = f(a)); // default argument: f(::a)\n"); //$NON-NLS-1$
		buffer.append("void h() {\n"); //$NON-NLS-1$
		buffer.append("a = 2;\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int a = 3;\n"); //$NON-NLS-1$
		buffer.append("g(); // g(f(::a))\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.6-6):
	class C {
	void f(int i = 3);
	void g(int i, int j = 99);
	};
	void C::f(int i = 3) // error: default argument already
	{ } // specified in class scope
	void C::g(int i = 88, int j) // in this translation unit,
	{ }
	 --End Example]
	 */
	public void test8_3_6s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("void f(int i = 3);\n"); //$NON-NLS-1$
		buffer.append("void g(int i, int j = 99);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void C::f(int i = 3) // error: default argument already\n"); //$NON-NLS-1$
		buffer.append("{ } // specified in class scope\n"); //$NON-NLS-1$
		buffer.append("void C::g(int i = 88, int j) // in this translation unit,\n"); //$NON-NLS-1$
		buffer.append("{ }\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.6-7):
	void f()
	{
	int i;
	extern void g(int x = i); // error
	// ...
	}
	 --End Example]
	 */
	public void test8_3_6s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("extern void g(int x = i); // error\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.6-8):
	class A {
	void f(A* p = this) { } // error
	};
	 --End Example]
	 */
	public void test8_3_6s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("void f(A* p = this) { } // error\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.6-9a):
	int a;
	int f(int a, int b = a); // error: parameter a
	// used as default argument
	typedef int I;
	int g(float I, int b = I(2)); // error: parameter I found
	int h(int a, int b = sizeof(a)); // error, parameter a used
	 --End Example]
	 */
	public void test8_3_6s9a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("int f(int a, int b = a); // error: parameter a\n"); //$NON-NLS-1$
		buffer.append("// used as default argument\n"); //$NON-NLS-1$
		buffer.append("typedef int I;\n"); //$NON-NLS-1$
		buffer.append("int g(float I, int b = I(2)); // error: parameter I found\n"); //$NON-NLS-1$
		buffer.append("int h(int a, int b = sizeof(a)); // error, parameter a used\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.6-9b):
	int b;
	class X {
	int a;
	int mem1(int i = a); // error: nonstatic member a
	// used as default argument
	int mem2(int i = b); // OK; use X::b
	static int b;
	};
	 --End Example]
	 */
	public void test8_3_6s9b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int b;\n"); //$NON-NLS-1$
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("int mem1(int i = a); // error: nonstatic member a\n"); //$NON-NLS-1$
		buffer.append("// used as default argument\n"); //$NON-NLS-1$
		buffer.append("int mem2(int i = b); // OK; use X::b\n"); //$NON-NLS-1$
		buffer.append("static int b;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.6-9c):
	int f(int = 0);
	void h()
	{
	int j = f(1);
	int k = f(); // OK, means f(0)
	}
	int (*p1)(int) = &f;
	int (*p2)() = &f; // error: type mismatch
	 --End Example]
	 */
	public void test8_3_6s9c() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(int = 0);\n"); //$NON-NLS-1$
		buffer.append("void h()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int j = f(1);\n"); //$NON-NLS-1$
		buffer.append("int k = f(); // OK, means f(0)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("int (*p1)(int) = &f;\n"); //$NON-NLS-1$
		buffer.append("int (*p2)() = &f; // error: type mismatch\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.3.6-10):
	struct A {
	virtual void f(int a = 7);
	};
	struct B : public A {
	void f(int a);
	};
	void m()
	{
	B* pb = new B;
	A* pa = pb;
	pa->f(); //OK, calls pa->B::f(7)
	pb->f(); //error: wrong number of arguments for B::f()
	}
	 --End Example]
	 */
	public void test8_3_6s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("virtual void f(int a = 7);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct B : public A {\n"); //$NON-NLS-1$
		buffer.append("void f(int a);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void m()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("B* pb = new B;\n"); //$NON-NLS-1$
		buffer.append("A* pa = pb;\n"); //$NON-NLS-1$
		buffer.append("pa->f(); //OK, calls pa->B::f(7)\n"); //$NON-NLS-1$
		buffer.append("pb->f(); //error: wrong number of arguments for B::f()\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 8.4-2):
	int max(int a, int b, int c)
	{
	int m = (a > b) ? a : b;
	return (m > c) ? m : c;
	}
	 --End Example]
	 */
	public void test8_4s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int max(int a, int b, int c)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int m = (a > b) ? a : b;\n"); //$NON-NLS-1$
		buffer.append("return (m > c) ? m : c;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5-10):
	int a;
	struct X {
	static int a;
	static int b;
	};
	int X::a = 1;
	int X::b = a; // X::b = X::a
	 --End Example]
	 */
	public void test8_5s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("static int a;\n"); //$NON-NLS-1$
		buffer.append("static int b;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int X::a = 1;\n"); //$NON-NLS-1$
		buffer.append("int X::b = a; // X::b = X::a\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.1-2):
	struct A {
	int x;
	struct B {
	int i;
	int j;
	} b;
	} a = { 1, { 2, 3 } };
	 --End Example]
	 */
	public void test8_5_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("int j;\n"); //$NON-NLS-1$
		buffer.append("} b;\n"); //$NON-NLS-1$
		buffer.append("} a = { 1, { 2, 3 } };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.1-4):
	int x[] = { 1, 3, 5 };
	 --End Example]
	 */
	public void test8_5_1s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int x[] = { 1, 3, 5 };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.1-5):
	struct A {
	int i;
	static int s;
	int j;
	} a = { 1, 2 };
	 --End Example]
	 */
	public void test8_5_1s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("static int s;\n"); //$NON-NLS-1$
		buffer.append("int j;\n"); //$NON-NLS-1$
		buffer.append("} a = { 1, 2 };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.1-6):
	char cv[4] = { 'a', 's', 'd', 'f', 0 }; // error
	 --End Example]
	 */
	public void test8_5_1s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("char cv[4] = { 'a', 's', 'd', 'f', 0 }; // error\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.1-7):
	struct S { int a; char* b; int c; };
	S ss = { 1, "asdf" };
	 --End Example]
	 */
	public void test8_5_1s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct S { int a; char* b; int c; };\n"); //$NON-NLS-1$
		buffer.append("S ss = { 1, \"asdf\" };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.1-8):
	struct S { };
	struct A {
	S s;
	int i;
	} a = { { } , 3 };
	 --End Example]
	 */
	public void test8_5_1s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct S { };\n"); //$NON-NLS-1$
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("S s;\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("} a = { { } , 3 };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.1-10):
	int x[2][2] = { 3, 1, 4, 2 };
	float y[4][3] = {
	{ 1 }, { 2 }, { 3 }, { 4 }
	};
	 --End Example]
	 */
	public void test8_5_1s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int x[2][2] = { 3, 1, 4, 2 };\n"); //$NON-NLS-1$
		buffer.append("float y[4][3] = {\n"); //$NON-NLS-1$
		buffer.append("{ 1 }, { 2 }, { 3 }, { 4 }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.1-11a):
	float y[4][3] = {
	{ 1, 3, 5 },
	{ 2, 4, 6 },
	{ 3, 5, 7 },
	};
	 --End Example]
	 */
	public void test8_5_1s11a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("float y[4][3] = {\n"); //$NON-NLS-1$
		buffer.append("{ 1, 3, 5 },\n"); //$NON-NLS-1$
		buffer.append("{ 2, 4, 6 },\n"); //$NON-NLS-1$
		buffer.append("{ 3, 5, 7 },\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.1-11b):
	float y[4][3] = {
	1, 3, 5, 2, 4, 6, 3, 5, 7
	};
	 --End Example]
	 */
	public void test8_5_1s11b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("float y[4][3] = {\n"); //$NON-NLS-1$
		buffer.append("1, 3, 5, 2, 4, 6, 3, 5, 7\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.1-12):
	struct A {
	int i;
	operator int();
	};
	struct B {
	A a1, a2;
	int z;
	};
	A a;
	B b = { 4, a, a };
	 --End Example]
	 */
	public void test8_5_1s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("operator int();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("A a1, a2;\n"); //$NON-NLS-1$
		buffer.append("int z;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("A a;\n"); //$NON-NLS-1$
		buffer.append("B b = { 4, a, a };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.1-15):
	union u { int a; char* b; };
	u a = { 1 };
	u b = a;
	u c = 1; // error
	u d = { 0, "asdf" }; // error
	u e = { "asdf" }; // error
	 --End Example]
	 */
	public void test8_5_1s15() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("union u { int a; char* b; };\n"); //$NON-NLS-1$
		buffer.append("u a = { 1 };\n"); //$NON-NLS-1$
		buffer.append("u b = a;\n"); //$NON-NLS-1$
		buffer.append("u c = 1; // error\n"); //$NON-NLS-1$
		buffer.append("u d = { 0, \"asdf\" }; // error\n"); //$NON-NLS-1$
		buffer.append("u e = { \"asdf\" }; // error\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.2-2):
	char cv[4] = "asdf"; // error
	 --End Example]
	 */
	public void test8_5_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("	char cv[4] = \"asdf\"; // error\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.3-2):
	int& r1; // error: initializer missing
	extern int& r2; // OK
	 --End Example]
	 */
	public void test8_5_3s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int& r1; // error: initializer missing\n"); //$NON-NLS-1$
		buffer.append("extern int& r2; // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.3-5a):
	double d = 2.0;
	double& rd = d; // rd refers to d
	const double& rcd = d; // rcd refers to d
	struct A { };
	struct B : public A { } b;
	A& ra = b; // ra refers to A subobject in b
	const A& rca = b; // rca refers to A subobject in b
	 --End Example]
	 */
	public void test8_5_3s5a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("double d = 2.0;\n"); //$NON-NLS-1$
		buffer.append("double& rd = d; // rd refers to d\n"); //$NON-NLS-1$
		buffer.append("const double& rcd = d; // rcd refers to d\n"); //$NON-NLS-1$
		buffer.append("struct A { };\n"); //$NON-NLS-1$
		buffer.append("struct B : public A { } b;\n"); //$NON-NLS-1$
		buffer.append("A& ra = b; // ra refers to A subobject in b\n"); //$NON-NLS-1$
		buffer.append("const A& rca = b; // rca refers to A subobject in b\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.3-5b):
	double& rd2 = 2.0; // error: not an lvalue and reference not const
	int i = 2;
	double& rd3 = i; // error: type mismatch and reference not const
	 --End Example]
	 */
	public void test8_5_3s5b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("double& rd2 = 2.0; // error: not an lvalue and reference not const\n"); //$NON-NLS-1$
		buffer.append("int i = 2;\n"); //$NON-NLS-1$
		buffer.append("double& rd3 = i; // error: type mismatch and reference not const\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.3-5c):
	struct A { };
	struct B : public A { } b;
	extern B f();
	const A& rca = f(); // Either bound to the A subobject of the B rvalue,
	// or the entire B object is copied and the reference
	// is bound to the A subobject of the copy
	 --End Example]
	 */
	public void test8_5_3s5c() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A { };\n"); //$NON-NLS-1$
		buffer.append("struct B : public A { } b;\n"); //$NON-NLS-1$
		buffer.append("extern B f();\n"); //$NON-NLS-1$
		buffer.append("const A& rca = f(); // Either bound to the A subobject of the B rvalue,\n"); //$NON-NLS-1$
		buffer.append("// or the entire B object is copied and the reference\n"); //$NON-NLS-1$
		buffer.append("// is bound to the A subobject of the copy\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5.3-5d):
	const double& rcd2 = 2; // rcd2 refers to temporary with value 2.0
	const volatile int cvi = 1;
	const int& r = cvi; // error: type qualifiers dropped
	 --End Example]
	 */
	public void test8_5_3s5d() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("const double& rcd2 = 2; // rcd2 refers to temporary with value 2.0\n"); //$NON-NLS-1$
		buffer.append("const volatile int cvi = 1;\n"); //$NON-NLS-1$
		buffer.append("const int& r = cvi; // error: type qualifiers dropped\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.1-1):
	struct X { int a; };
	struct Y { int a; };
	X a1;
	Y a2;
	int a3;
	a1 = a2; // error: Y assigned to X
	a1 = a3; // error: int assigned to X
	int f(X);
	int f(Y);
	struct S { int a; };
	struct S { int a; }; // error, double definition
	 --End Example]
	 */
	public void test9_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X { int a; };\n"); //$NON-NLS-1$
		buffer.append("struct Y { int a; };\n"); //$NON-NLS-1$
		buffer.append("X a1;\n"); //$NON-NLS-1$
		buffer.append("Y a2;\n"); //$NON-NLS-1$
		buffer.append("int a3;\n"); //$NON-NLS-1$
		buffer.append("a1 = a2; // error: Y assigned to X\n"); //$NON-NLS-1$
		buffer.append("a1 = a3; // error: int assigned to X\n"); //$NON-NLS-1$
		buffer.append("int f(X);\n"); //$NON-NLS-1$
		buffer.append("int f(Y);\n"); //$NON-NLS-1$
		buffer.append("struct S { int a; };\n"); //$NON-NLS-1$
		buffer.append("struct S { int a; }; // error, double definition\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 9.1-2a):
	struct stat {
	// ...
	};
	stat gstat; // use plain stat to
	// define variable
	int stat(struct stat*); // redeclare stat as function
	void f()
	{
	struct stat* ps; // struct prefix needed
	// to name struct stat
	// ...
	stat(ps); //call stat()
	// ...
	}
	 --End Example]
	 */
	public void test9_1s2a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct stat {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("stat gstat; // use plain stat to\n"); //$NON-NLS-1$
		buffer.append("// define variable\n"); //$NON-NLS-1$
		buffer.append("int stat(struct stat*); // redeclare stat as function\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("struct stat* ps; // struct prefix needed\n"); //$NON-NLS-1$
		buffer.append("// to name struct stat\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("stat(ps); //call stat()\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.1-2b):
	struct s { int a; };
	void g()
	{
	struct s; // hide global struct s
	// with a local declaration
	s* p; // refer to local struct s
	struct s { char* p; }; // define local struct s
	struct s; // redeclaration, has no effect
	}
	 --End Example]
	 */
	public void test9_1s2b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct s { int a; };\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("struct s; // hide global struct s\n"); //$NON-NLS-1$
		buffer.append("// with a local declaration\n"); //$NON-NLS-1$
		buffer.append("s* p; // refer to local struct s\n"); //$NON-NLS-1$
		buffer.append("struct s { char* p; }; // define local struct s\n"); //$NON-NLS-1$
		buffer.append("struct s; // redeclaration, has no effect\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.1-2c):
	class Vector;
	class Matrix {
	// ...
	friend Vector operator*(Matrix&, Vector&);
	};
	class Vector {
	// ...
	friend Vector operator*(Matrix&, Vector&);
	};
	 --End Example]
	 */
	public void test9_1s2c() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class Vector;\n"); //$NON-NLS-1$
		buffer.append("class Matrix {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("friend Vector operator*(Matrix&, Vector&);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Vector {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("friend Vector operator*(Matrix&, Vector&);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.1-3):
	struct s { int a; };
	void g(int s)
	{
	struct s* p = new struct s; // global s
	p->a = s; // local s
	}
	 --End Example]
	 */
	public void test9_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct s { int a; };\n"); //$NON-NLS-1$
		buffer.append("void g(int s)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("struct s* p = new struct s; // global s\n"); //$NON-NLS-1$
		buffer.append("p->a = s; // local s\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.1-4):
	class A * A;
	 --End Example]
	 */
	public void test9_1s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A * A;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.2-11):
	struct tnode {
	char tword[20];
	int count;
	tnode *left;
	tnode *right;
	tnode s, *sp;
	};
	 --End Example]
	 */
	public void test9_2s11() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct tnode {\n"); //$NON-NLS-1$
		buffer.append("char tword[20];\n"); //$NON-NLS-1$
		buffer.append("int count;\n"); //$NON-NLS-1$
		buffer.append("tnode *left;\n"); //$NON-NLS-1$
		buffer.append("tnode *right;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("tnode s, *sp;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.3-5):
	struct X {
	typedef int T;
	static T count;
	void f(T);
	};
	void X::f(T t = count) { }
	 --End Example]
	 */
	public void test9_3s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("typedef int T;\n"); //$NON-NLS-1$
		buffer.append("static T count;\n"); //$NON-NLS-1$
		buffer.append("void f(T);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void X::f(T t = count) { }\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.3-9):
	typedef void fv(void);
	typedef void fvc(void) const;
	struct S {
	fv memfunc1; // equivalent to: void memfunc1(void);
	void memfunc2();
	fvc memfunc3; // equivalent to: void memfunc3(void) const;
	};
	fv S::* pmfv1 = &S::memfunc1;
	fv S::* pmfv2 = &S::memfunc2;
	fvc S::* pmfv3 = &S::memfunc3;
	 --End Example]
	 */
	public void test9_3s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef void fv(void);\n"); //$NON-NLS-1$
		buffer.append("typedef void fvc(void) const;\n"); //$NON-NLS-1$
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("fv memfunc1; // equivalent to: void memfunc1(void);\n"); //$NON-NLS-1$
		buffer.append("void memfunc2();\n"); //$NON-NLS-1$
		buffer.append("fvc memfunc3; // equivalent to: void memfunc3(void) const;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("fv S::* pmfv1 = &S::memfunc1;\n"); //$NON-NLS-1$
		buffer.append("fv S::* pmfv2 = &S::memfunc2;\n"); //$NON-NLS-1$
		buffer.append("fvc S::* pmfv3 = &S::memfunc3;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.3.1-2):
	struct tnode {
	char tword[20];
	int count;
	tnode *left;
	tnode *right;
	void set(char*, tnode* l, tnode* r);
	};
	void tnode::set(char* w, tnode* l, tnode* r)
	{
	count = strlen(w)+1;
	if (sizeof(tword)<=count)
	perror("tnode string too long");
	strcpy(tword,w);
	left = l;
	right = r;
	}
	void f(tnode n1, tnode n2)
	{
	n1.set("abc",&n2,0);
	n2.set("def",0,0);
	}
	 --End Example]
	 */
	public void test9_3_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct tnode {\n"); //$NON-NLS-1$
		buffer.append("char tword[20];\n"); //$NON-NLS-1$
		buffer.append("int count;\n"); //$NON-NLS-1$
		buffer.append("tnode *left;\n"); //$NON-NLS-1$
		buffer.append("tnode *right;\n"); //$NON-NLS-1$
		buffer.append("void set(char*, tnode* l, tnode* r);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void tnode::set(char* w, tnode* l, tnode* r)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("count = strlen(w)+1;\n"); //$NON-NLS-1$
		buffer.append("if (sizeof(tword)<=count)\n"); //$NON-NLS-1$
		buffer.append("perror(\"tnode string too long\");\n"); //$NON-NLS-1$
		buffer.append("strcpy(tword,w);\n"); //$NON-NLS-1$
		buffer.append("left = l;\n"); //$NON-NLS-1$
		buffer.append("right = r;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void f(tnode n1, tnode n2)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("n1.set(\"abc\",&n2,0);\n"); //$NON-NLS-1$
		buffer.append("n2.set(\"def\",0,0);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 9.3.1-3):
	struct X {
	void g() const;
	void h() const volatile;
	};
	 --End Example]
	 */
	public void test9_3_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("void g() const;\n"); //$NON-NLS-1$
		buffer.append("void h() const volatile;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.3.2-2):
	struct s {
	int a;
	int f() const;
	int g() { return a++; }
	int h() const { return a++; } // error
	};
	int s::f() const { return a; }
	 --End Example]
	 */
	public void test9_3_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct s {\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("int f() const;\n"); //$NON-NLS-1$
		buffer.append("int g() { return a++; }\n"); //$NON-NLS-1$
		buffer.append("int h() const { return a++; } // error\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int s::f() const { return a; }\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.3.2-4):
	struct s {
	int a;
	int f() const;
	int g() { return a++; }
	};
	int s::f() const { return a; }
	
	void k(s& x, const s& y)
	{
	x.f();
	x.g();
	y.f();
	y.g(); //error
	}
	 --End Example]
	 */
	public void test9_3_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct s {\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("int f() const;\n"); //$NON-NLS-1$
		buffer.append("int g() { return a++; }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int s::f() const { return a; }\n"); //$NON-NLS-1$
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append("void k(s& x, const s& y)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("x.f();\n"); //$NON-NLS-1$
		buffer.append("x.g();\n"); //$NON-NLS-1$
		buffer.append("y.f();\n"); //$NON-NLS-1$
		buffer.append("y.g(); //error\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.4-2a):
	class process {
	public:
	static void reschedule();
	};
	process& g();
	void f()
	{
	process::reschedule(); // OK: no object necessary
	g().reschedule(); // g() is called
	}
	 --End Example]
	 */
	public void test9_4s2a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class process {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("static void reschedule();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("process& g();\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("process::reschedule(); // OK: no object necessary\n"); //$NON-NLS-1$
		buffer.append("g().reschedule(); // g() is called\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.4-2b):
	int g();
	struct X {
	static int g();
	};
	struct Y : X {
	static int i;
	};
	int Y::i = g(); // equivalent to Y::g();
	 --End Example]
	 */
	public void test9_4s2b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int g();\n"); //$NON-NLS-1$
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("static int g();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct Y : X {\n"); //$NON-NLS-1$
		buffer.append("static int i;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int Y::i = g(); // equivalent to Y::g();\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.4.2-2):
	class process {
	static process* run_chain;
	static process* running;
	};
	process* process::running = get_main();
	process* process::run_chain = running;
	 --End Example]
	 */
	public void test9_4_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class process {\n"); //$NON-NLS-1$
		buffer.append("static process* run_chain;\n"); //$NON-NLS-1$
		buffer.append("static process* running;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("process* process::running = get_main();\n"); //$NON-NLS-1$
		buffer.append("process* process::run_chain = running;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
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
	public void test9_5s2() throws Exception { 
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("union { int a; char* p; };\n"); //$NON-NLS-1$
		buffer.append("a = 1;\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("p = \"Jennifer\";\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.5-4):
	int foo() {
	union { int aa; char* p; } obj, *ptr = &obj;
	aa = 1; // error
	ptr->aa = 1; // OK
	}
	 --End Example]
	 */
	public void test9_5s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("union { int aa; char* p; } obj, *ptr = &obj;\n"); //$NON-NLS-1$
		buffer.append("aa = 1; // error\n"); //$NON-NLS-1$
		buffer.append("ptr->aa = 1; // OK\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 9.6-4):
	enum BOOL { f=0, t=1 };
	struct A {
	BOOL b:1;
	};
	A a;
	void f() {
	a.b = t;
	if (a.b == t) // shall yield true
	{  }
	}
	 --End Example]
	 */
	public void test9_6s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("enum BOOL { f=0, t=1 };\n"); //$NON-NLS-1$
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("BOOL b:1;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("A a;\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("a.b = t;\n"); //$NON-NLS-1$
		buffer.append("if (a.b == t) // shall yield true\n"); //$NON-NLS-1$
		buffer.append("{  }\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.7-1):
	int x;
	int y;
	class enclose {
	public:
	int x;
	static int s;
	class inner {
	void f(int i)
	{
	int a = sizeof(x); // error: refers to enclose::x
	x = i; // error: assign to enclose::x
	s = i; // OK: assign to enclose::s
	::x = i; // OK: assign to global x
	y = i; // OK: assign to global y
	}
	void g(enclose* p, int i)
	{
	p>
	x = i; // OK: assign to enclose::x
	}
	};
	};
	inner* p = 0; // error: inner not in scope
	 --End Example]
	 */
	public void test9_7s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("int y;\n"); //$NON-NLS-1$
		buffer.append("class enclose {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("static int s;\n"); //$NON-NLS-1$
		buffer.append("class inner {\n"); //$NON-NLS-1$
		buffer.append("void f(int i)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int a = sizeof(x); // error: refers to enclose::x\n"); //$NON-NLS-1$
		buffer.append("x = i; // error: assign to enclose::x\n"); //$NON-NLS-1$
		buffer.append("s = i; // OK: assign to enclose::s\n"); //$NON-NLS-1$
		buffer.append("::x = i; // OK: assign to global x\n"); //$NON-NLS-1$
		buffer.append("y = i; // OK: assign to global y\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void g(enclose* p, int i)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("p>\n"); //$NON-NLS-1$
		buffer.append("x = i; // OK: assign to enclose::x\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("inner* p = 0; // error: inner not in scope\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 9.7-2):
	class enclose {
	public:
	class inner {
	static int x;
	void f(int i);
	};
	};
	int enclose::inner::x = 1;
	void enclose::inner::f(int i) {  }
	 --End Example]
	 */
	public void test9_7s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class enclose {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("class inner {\n"); //$NON-NLS-1$
		buffer.append("static int x;\n"); //$NON-NLS-1$
		buffer.append("void f(int i);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int enclose::inner::x = 1;\n"); //$NON-NLS-1$
		buffer.append("void enclose::inner::f(int i) {  }\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.7-3):
	class E {
	class I1; // forward declaration of nested class
	class I2;
	class I1 {}; // definition of nested class
	};
	class E::I2 {}; // definition of nested class
	 --End Example]
	 */
	public void test9_7s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class E {\n"); //$NON-NLS-1$
		buffer.append("class I1; // forward declaration of nested class\n"); //$NON-NLS-1$
		buffer.append("class I2;\n"); //$NON-NLS-1$
		buffer.append("class I1 {}; // definition of nested class\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class E::I2 {}; // definition of nested class\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 9.8-1):
	int x;
	void f()
	{
	static int s ;
	int x;
	extern int g();
	struct local {
	int g() { return x; } // error: x is auto
	int h() { return s; } // OK
	int k() { return ::x; } // OK
	int l() { return g(); } // OK
	};
	// ...
	}
	local* p = 0; // error: local not in scope
	 --End Example]
	 */
	public void test9_8s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("static int s ;\n"); //$NON-NLS-1$
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("extern int g();\n"); //$NON-NLS-1$
		buffer.append("struct local {\n"); //$NON-NLS-1$
		buffer.append("int g() { return x; } // error: x is auto\n"); //$NON-NLS-1$
		buffer.append("int h() { return s; } // OK\n"); //$NON-NLS-1$
		buffer.append("int k() { return ::x; } // OK\n"); //$NON-NLS-1$
		buffer.append("int l() { return g(); } // OK\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("local* p = 0; // error: local not in scope\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 9.9-1):
	 class X {
	public:
	typedef int I;
	class Y {  };
	I a;
	};
	I b; // error
	Y c; // error
	X::Y d; // OK
	X::I e; // OK
	 --End Example]
	 */
	public void test9_9s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("typedef int I;\n"); //$NON-NLS-1$
		buffer.append("class Y { /* ... */ };\n"); //$NON-NLS-1$
		buffer.append("I a;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("I b; // error\n"); //$NON-NLS-1$
		buffer.append("Y c; // error\n"); //$NON-NLS-1$
		buffer.append("X::Y d; // OK\n"); //$NON-NLS-1$
		buffer.append("X::I e; // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 10-2):
	class Base {
	public:
	int a, b, c;
	};
	class Derived : public Base {
	public:
	int b;
	};
	class Derived2 : public Derived {
	public:
	int c;
	};
	 --End Example]
	 */
	public void test10s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class Base {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int a, b, c;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Derived : public Base {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int b;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Derived2 : public Derived {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int c;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.1-1):
	class A {  };
	class B {  };
	class C {  };
	class D : public A, public B, public C {  };
	 --End Example]
	 */
	public void test10_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {  };\n"); //$NON-NLS-1$
		buffer.append("class B {  };\n"); //$NON-NLS-1$
		buffer.append("class C {  };\n"); //$NON-NLS-1$
		buffer.append("class D : public A, public B, public C {  };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.1-3a):
	class X {  };
	class Y : public X, public X {  }; // illformed
	 --End Example]
	 */
	public void test10_1s3a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {  };\n"); //$NON-NLS-1$
		buffer.append("class Y : public X, public X {  }; // illformed\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.1-3b):
	class L { public: int next;  };
	class A : public L {  };
	class B : public L {  };
	class C : public A, public B { void f();  }; // wellformed
	class D : public A, public L { void f();  }; // wellformed
	 --End Example]
	 */
	public void test10_1s3b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class L { public: int next;  };\n"); //$NON-NLS-1$
		buffer.append("class A : public L {  };\n"); //$NON-NLS-1$
		buffer.append("class B : public L {  };\n"); //$NON-NLS-1$
		buffer.append("class C : public A, public B { void f();  }; // wellformed\n"); //$NON-NLS-1$
		buffer.append("class D : public A, public L { void f();  }; // wellformed\n"); //$NON-NLS-1$
		buffer.append("\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.2-3a):
	class A {
	public:
	int a;
	int (*b)();
	int f();
	int f(int);
	int g();
	};
	class B {
	int a;
	int b();
	public:
	int f();
	int g;
	int h();
	int h(int);
	};
	class C : public A, public B {};
	void g(C* pc)
	{
	pc->a = 1; // error: ambiguous: A::a or B::a
	pc->b(); //error: ambiguous: A::b or B::b
	pc->f(); //error: ambiguous: A::f or B::f
	pc->f(1); //error: ambiguous: A::f or B::f
	pc->g(); //error: ambiguous: A::g or B::g
	pc->g = 1; // error: ambiguous: A::g or B::g
	pc->h(); //OK
	pc->h(1); //OK
	}
	 --End Example]
	 */
	public void test10_2s3a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("int (*b)();\n"); //$NON-NLS-1$
		buffer.append("int f();\n"); //$NON-NLS-1$
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("int g();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class B {\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("int b();\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int f();\n"); //$NON-NLS-1$
		buffer.append("int g;\n"); //$NON-NLS-1$
		buffer.append("int h();\n"); //$NON-NLS-1$
		buffer.append("int h(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class C : public A, public B {};\n"); //$NON-NLS-1$
		buffer.append("void g(C* pc)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("pc->a = 1; // error: ambiguous: A::a or B::a\n"); //$NON-NLS-1$
		buffer.append("pc->b(); //error: ambiguous: A::b or B::b\n"); //$NON-NLS-1$
		buffer.append("pc->f(); //error: ambiguous: A::f or B::f\n"); //$NON-NLS-1$
		buffer.append("pc->f(1); //error: ambiguous: A::f or B::f\n"); //$NON-NLS-1$
		buffer.append("pc->g(); //error: ambiguous: A::g or B::g\n"); //$NON-NLS-1$
		buffer.append("pc->g = 1; // error: ambiguous: A::g or B::g\n"); //$NON-NLS-1$
		buffer.append("pc->h(); //OK\n"); //$NON-NLS-1$
		buffer.append("pc->h(1); //OK\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 10.2-4):
	class A {
	public:
	int f();
	};
	class B {
	public:
	int f();
	};
	class C : public A, public B {
	int f() { return A::f() + B::f(); }
	};
	 --End Example]
	 */
	public void test10_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class B {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class C : public A, public B {\n"); //$NON-NLS-1$
		buffer.append("int f() { return A::f() + B::f(); }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.2-5):
	class V { public: int v; };
	class A {
	public:
	int a;
	static int s;
	enum { e };
	};
	class B : public A, public virtual V {};
	class C : public A, public virtual V {};
	class D : public B, public C { };
	void f(D* pd)
	{
	pd->v++; //OK: only one v (virtual)
	pd->s++; //OK: only one s (static)
	int i = pd>
	e; // OK: only one e (enumerator)
	pd->a++; //error, ambiguous: two as in D
	}
	 --End Example]
	 */
	public void test10_2s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class V { public: int v; };\n"); //$NON-NLS-1$
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("static int s;\n"); //$NON-NLS-1$
		buffer.append("enum { e };\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class B : public A, public virtual V {};\n"); //$NON-NLS-1$
		buffer.append("class C : public A, public virtual V {};\n"); //$NON-NLS-1$
		buffer.append("class D : public B, public C { };\n"); //$NON-NLS-1$
		buffer.append("void f(D* pd)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("pd->v++; //OK: only one v (virtual)\n"); //$NON-NLS-1$
		buffer.append("pd->s++; //OK: only one s (static)\n"); //$NON-NLS-1$
		buffer.append("int i = pd>\n"); //$NON-NLS-1$
		buffer.append("e; // OK: only one e (enumerator)\n"); //$NON-NLS-1$
		buffer.append("pd->a++; //error, ambiguous: two as in D\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 10.2-6):
	class V { public: int f(); int x; };
	class W { public: int g(); int y; };
	class B : public virtual V, public W
	{
	public:
	int f(); int x;
	int g(); int y;
	};
	class C : public virtual V, public W { };
	class D : public B, public C { void glorp(); };
	 --End Example]
	 */
	public void test10_2s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class V { public: int f(); int x; };\n"); //$NON-NLS-1$
		buffer.append("class W { public: int g(); int y; };\n"); //$NON-NLS-1$
		buffer.append("class B : public virtual V, public W\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int f(); int x;\n"); //$NON-NLS-1$
		buffer.append("int g(); int y;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class C : public virtual V, public W { };\n"); //$NON-NLS-1$
		buffer.append("class D : public B, public C { void glorp(); };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.2-7):
	class V { };
	class A { };
	class B : public A, public virtual V { };
	class C : public A, public virtual V { };
	class D : public B, public C { };
	void g()
	{
	D d;
	B* pb = &d;
	A* pa = &d; // error, ambiguous: C’s A or B’s A?
	V* pv = &d; // OK: only one V subobject
	}
	 --End Example]
	 */
	public void test10_2s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class V { };\n"); //$NON-NLS-1$
		buffer.append("class A { };\n"); //$NON-NLS-1$
		buffer.append("class B : public A, public virtual V { };\n"); //$NON-NLS-1$
		buffer.append("class C : public A, public virtual V { };\n"); //$NON-NLS-1$
		buffer.append("class D : public B, public C { };\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("D d;\n"); //$NON-NLS-1$
		buffer.append("B* pb = &d;\n"); //$NON-NLS-1$
		buffer.append("A* pa = &d; // error, ambiguous: C’s A or B’s A?\n"); //$NON-NLS-1$
		buffer.append("V* pv = &d; // OK: only one V subobject\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.3-2):
	struct A {
	virtual void f();
	};
	struct B : virtual A {
	virtual void f();
	};
	struct C : B , virtual A {
	using A::f;
	};
	void foo() {
	C c;
	c.f(); //calls B::f, the final overrider
	c.C::f(); //calls A::f because of the usingdeclaration
	}
	 --End Example]
	 */
	public void test10_3s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("virtual void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct B : virtual A {\n"); //$NON-NLS-1$
		buffer.append("virtual void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct C : B , virtual A {\n"); //$NON-NLS-1$
		buffer.append("using A::f;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void foo() {\n"); //$NON-NLS-1$
		buffer.append("C c;\n"); //$NON-NLS-1$
		buffer.append("c.f(); //calls B::f, the final overrider\n"); //$NON-NLS-1$
		buffer.append("c.C::f(); //calls A::f because of the usingdeclaration\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.3-5):
	class B {};
	class D : private B { friend class Derived; };
	struct Base {
	virtual void vf1();
	virtual void vf2();
	virtual void vf3();
	virtual B* vf4();
	virtual B* vf5();
	void f();
	};
	struct No_good : public Base {
	D* vf4(); // error: B (base class of D) inaccessible
	};
	class A;
	struct Derived : public Base {
	void vf1(); // virtual and overrides Base::vf1()
	void vf2(int); // not virtual, hides Base::vf2()
	char vf3(); // error: invalid difference in return type only
	D* vf4(); // OK: returns pointer to derived class
	A* vf5(); // error: returns pointer to incomplete class
	void f();
	};
	void g()
	{
	Derived d;
	Base* bp = &d; // standard conversion:
	// Derived* to Base*
	bp->vf1(); //calls Derived::vf1()
	bp->vf2(); //calls Base::vf2()
	bp->f(); //calls Base::f() (not virtual)
	B* p = bp->vf4(); // calls Derived::pf() and converts the
	// result to B*
	Derived* dp = &d;
	D* q = dp->vf4(); // calls Derived::pf() and does not
	// convert the result to B*
	dp->vf2(); //illformed: argument mismatch
	}
	 --End Example]
	 */
	public void test10_3s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B {};\n"); //$NON-NLS-1$
		buffer.append("class D : private B { friend class Derived; };\n"); //$NON-NLS-1$
		buffer.append("struct Base {\n"); //$NON-NLS-1$
		buffer.append("virtual void vf1();\n"); //$NON-NLS-1$
		buffer.append("virtual void vf2();\n"); //$NON-NLS-1$
		buffer.append("virtual void vf3();\n"); //$NON-NLS-1$
		buffer.append("virtual B* vf4();\n"); //$NON-NLS-1$
		buffer.append("virtual B* vf5();\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct No_good : public Base {\n"); //$NON-NLS-1$
		buffer.append("D* vf4(); // error: B (base class of D) inaccessible\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class A;\n"); //$NON-NLS-1$
		buffer.append("struct Derived : public Base {\n"); //$NON-NLS-1$
		buffer.append("void vf1(); // virtual and overrides Base::vf1()\n"); //$NON-NLS-1$
		buffer.append("void vf2(int); // not virtual, hides Base::vf2()\n"); //$NON-NLS-1$
		buffer.append("char vf3(); // error: invalid difference in return type only\n"); //$NON-NLS-1$
		buffer.append("D* vf4(); // OK: returns pointer to derived class\n"); //$NON-NLS-1$
		buffer.append("A* vf5(); // error: returns pointer to incomplete class\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("Derived d;\n"); //$NON-NLS-1$
		buffer.append("Base* bp = &d; // standard conversion:\n"); //$NON-NLS-1$
		buffer.append("// Derived* to Base*\n"); //$NON-NLS-1$
		buffer.append("bp->vf1(); //calls Derived::vf1()\n"); //$NON-NLS-1$
		buffer.append("bp->vf2(); //calls Base::vf2()\n"); //$NON-NLS-1$
		buffer.append("bp->f(); //calls Base::f() (not virtual)\n"); //$NON-NLS-1$
		buffer.append("B* p = bp->vf4(); // calls Derived::pf() and converts the\n"); //$NON-NLS-1$
		buffer.append("// result to B*\n"); //$NON-NLS-1$
		buffer.append("Derived* dp = &d;\n"); //$NON-NLS-1$
		buffer.append("D* q = dp->vf4(); // calls Derived::pf() and does not\n"); //$NON-NLS-1$
		buffer.append("// convert the result to B*\n"); //$NON-NLS-1$
		buffer.append("dp->vf2(); //illformed: argument mismatch\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 10.3-9):
	struct A {
	virtual void f();
	};
	struct B1 : A { // note nonvirtual
	derivation
	void f();
	};
	struct B2 : A {
	void f();
	};
	struct D : B1, B2 { // D has two separate A subobjects
	};
	void foo()
	{
	D d;
	// A* ap = &d; // would be illformed:ambiguous
	B1* b1p = &d;
	A* ap = b1p;
	D* dp = &d;
	ap->f(); //calls D::B1::f
	dp->f(); //illformed: ambiguous
	}
	 --End Example]
	 */
	public void test10_3s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("virtual void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct B1 : A { // note nonvirtual\n"); //$NON-NLS-1$
		buffer.append("derivation\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct B2 : A {\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D : B1, B2 { // D has two separate A subobjects\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void foo()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("D d;\n"); //$NON-NLS-1$
		buffer.append("// A* ap = &d; // would be illformed:ambiguous\n"); //$NON-NLS-1$
		buffer.append("B1* b1p = &d;\n"); //$NON-NLS-1$
		buffer.append("A* ap = b1p;\n"); //$NON-NLS-1$
		buffer.append("D* dp = &d;\n"); //$NON-NLS-1$
		buffer.append("ap->f(); //calls D::B1::f\n"); //$NON-NLS-1$
		buffer.append("dp->f(); //illformed: ambiguous\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 10.3-10):
	struct A {
	virtual void f();
	};
	struct VB1 : virtual A { // note virtual derivation
	void f();
	};
	struct VB2 : virtual A {
	void f();
	};
	struct Error : VB1, VB2 { // illformed
	};
	struct Okay : VB1, VB2 {
	void f();
	};
	 --End Example]
	 */
	public void test10_3s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("virtual void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct VB1 : virtual A { // note virtual derivation\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct VB2 : virtual A {\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct Error : VB1, VB2 { // illformed\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct Okay : VB1, VB2 {\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.3-11):
	struct VB1a : virtual A { // does not declare f
	};
	struct Da : VB1a, VB2 {
	};
	void foe()
	{
	VB1a* vb1ap = new Da;
	vb1ap->f(); //calls VB2::f
	}
	 --End Example]
	 */
	public void test10_3s11() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct VB1a : virtual A { // does not declare f\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct Da : VB1a, VB2 {\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void foe()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("VB1a* vb1ap = new Da;\n"); //$NON-NLS-1$
		buffer.append("vb1ap->f(); //calls VB2::f\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 10.3-12):
	class B { public: virtual void f(); };
	class D : public B { public: void f(); };
	void D::f() { B::f(); }
	 --End Example]
	 */
	public void test10_3s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B { public: virtual void f(); };\n"); //$NON-NLS-1$
		buffer.append("class D : public B { public: void f(); };\n"); //$NON-NLS-1$
		buffer.append("void D::f() { B::f(); }\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.4-2a):
	class point {  };
	class shape { // abstract class
	point center;
	// ...
	public:
	point where() { return center; }
	void move(point p) { center=p; draw(); }
	virtual void rotate(int) = 0; // pure virtual
	virtual void draw() = 0; // pure virtual
	// ...
	};
	 --End Example]
	 */
	public void test10_4s2a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class point {  };\n"); //$NON-NLS-1$
		buffer.append("class shape { // abstract class\n"); //$NON-NLS-1$
		buffer.append("point center;\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("point where() { return center; }\n"); //$NON-NLS-1$
		buffer.append("void move(point p) { center=p; draw(); }\n"); //$NON-NLS-1$
		buffer.append("virtual void rotate(int) = 0; // pure virtual\n"); //$NON-NLS-1$
		buffer.append("virtual void draw() = 0; // pure virtual\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.4-2b):
	struct C {
	virtual void f() { }=0; // illformed
	};
	 --End Example]
	 */
	public void test10_4s2b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct C {\n"); //$NON-NLS-1$
		buffer.append("virtual void f() { }=0; // illformed\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.4-3):
	shape x; // error: object of abstract class
	shape* p; // OK
	shape f(); // error
	void g(shape); // error
	shape& h(shape&); // OK
	 --End Example]
	 */
	public void test10_4s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("shape x; // error: object of abstract class\n"); //$NON-NLS-1$
		buffer.append("shape* p; // OK\n"); //$NON-NLS-1$
		buffer.append("shape f(); // error\n"); //$NON-NLS-1$
		buffer.append("void g(shape); // error\n"); //$NON-NLS-1$
		buffer.append("shape& h(shape&); // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 10.4-4a):
	class ab_circle : public shape {
	int radius;
	public:
	void rotate(int) {}
	// ab_circle::draw() is a pure virtual
	};
	 --End Example]
	 */
	public void test10_4s4a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class ab_circle : public shape {\n"); //$NON-NLS-1$
		buffer.append("int radius;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("void rotate(int) {}\n"); //$NON-NLS-1$
		buffer.append("// ab_circle::draw() is a pure virtual\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 10.4-4b):
	class circle : public shape {
	int radius;
	public:
	void rotate(int) {}
	void draw(); // a definition is required somewhere
	};
	 --End Example]
	 */
	public void test10_4s4b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class circle : public shape {\n"); //$NON-NLS-1$
		buffer.append("int radius;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("void rotate(int) {}\n"); //$NON-NLS-1$
		buffer.append("void draw(); // a definition is required somewhere\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 11-2):
	class X {
	int a; // X::a is private by default
	};
	struct S {
	int a; // S::a is public by default
	};
	 --End Example]
	 */
	public void test11s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("int a; // X::a is private by default\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("int a; // S::a is public by default\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11-3):
	class A
	{
	class B { };
	public:
	typedef B BB;
	};
	void f()
	{
	A::BB x; // OK, typedef name A::BB is public
	A::B y; // access error, A::B is private
	}
	 --End Example]
	 */
	public void test11s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("class B { };\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("typedef B BB;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("A::BB x; // OK, typedef name A::BB is public\n"); //$NON-NLS-1$
		buffer.append("A::B y; // access error, A::B is private\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11-5):
	class A {
	typedef int I; // private member
	I f();
	friend I g(I);
	static I x;
	};
	A::I A::f() { return 0; }
	A::I g(A::I p = A::x);
	A::I g(A::I p) { return 0; }
	A::I A::x = 0;
	 --End Example]
	 */
	public void test11s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("typedef int I; // private member\n"); //$NON-NLS-1$
		buffer.append("I f();\n"); //$NON-NLS-1$
		buffer.append("friend I g(I);\n"); //$NON-NLS-1$
		buffer.append("static I x;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("A::I A::f() { return 0; }\n"); //$NON-NLS-1$
		buffer.append("A::I g(A::I p = A::x);\n"); //$NON-NLS-1$
		buffer.append("A::I g(A::I p) { return 0; }\n"); //$NON-NLS-1$
		buffer.append("A::I A::x = 0;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11-6):
	class D {
	class E {
	static int m;
	};
	};
	int D::E::m = 1; // OK, no access error on private E
	 --End Example]
	 */
	public void test11s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class D {\n"); //$NON-NLS-1$
		buffer.append("class E {\n"); //$NON-NLS-1$
		buffer.append("static int m;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int D::E::m = 1; // OK, no access error on private E\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.1-1a):
	class X {
	int a; // X::a is private by default: class used
	public:
	int b; // X::b is public
	int c; // X::c is public
	};
	 --End Example]
	 */
	public void test11_1s1a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("int a; // X::a is private by default: class used\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int b; // X::b is public\n"); //$NON-NLS-1$
		buffer.append("int c; // X::c is public\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11-1b):
	struct S {
	int a; // S::a is public by default: struct used
	protected:
	int b; // S::b is protected
	private:
	int c; // S::c is private
	public:
	int d; // S::d is public
	};
	 --End Example]
	 */
	public void test11s1b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("int a; // S::a is public by default: struct used\n"); //$NON-NLS-1$
		buffer.append("protected:\n"); //$NON-NLS-1$
		buffer.append("int b; // S::b is protected\n"); //$NON-NLS-1$
		buffer.append("private:\n"); //$NON-NLS-1$
		buffer.append("int c; // S::c is private\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int d; // S::d is public\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.1-3):
	struct S {
	class A;
	private:
	class A { }; // error: cannot change access
	};
	 --End Example]
	 */
	public void test11_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("class A;\n"); //$NON-NLS-1$
		buffer.append("private:\n"); //$NON-NLS-1$
		buffer.append("class A { }; // error: cannot change access\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.2-2):
	class B {  };
	class D1 : private B {  };
	class D2 : public B {  };
	class D3 : B {  }; // B private by default
	struct D4 : public B {  };
	struct D5 : private B {  };
	struct D6 : B {  }; // B public by default
	class D7 : protected B {  };
	struct D8 : protected B {  };
	 --End Example]
	 */
	public void test11_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B {  };\n"); //$NON-NLS-1$
		buffer.append("class D1 : private B {  };\n"); //$NON-NLS-1$
		buffer.append("class D2 : public B {  };\n"); //$NON-NLS-1$
		buffer.append("class D3 : B {  }; // B private by default\n"); //$NON-NLS-1$
		buffer.append("struct D4 : public B {  };\n"); //$NON-NLS-1$
		buffer.append("struct D5 : private B {  };\n"); //$NON-NLS-1$
		buffer.append("struct D6 : B {  }; // B public by default\n"); //$NON-NLS-1$
		buffer.append("class D7 : protected B {  };\n"); //$NON-NLS-1$
		buffer.append("struct D8 : protected B {  };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.2-3):
	class B {
	public:
	int mi; // nonstatic member
	static int si; // static member
	};
	class D : private B {
	};
	class DD : public D {
	void f();
	};
	void DD::f() {
	mi = 3; // error: mi is private in D
	si = 3; // error: si is private in D
	B b;
	b.mi = 3; // OK (b.mi is different from this->mi)
	b.si = 3; // OK (b.si is different from this->si)
	B::si = 3; // OK
	B* bp1 = this; // error: B is a private base class
	B* bp2 = (B*)this; // OK with cast
	bp2->mi = 3; // OK: access through a pointer to B.
	}
	 --End Example]
	 */
	public void test11_2s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int mi; // nonstatic member\n"); //$NON-NLS-1$
		buffer.append("static int si; // static member\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D : private B {\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class DD : public D {\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void DD::f() {\n"); //$NON-NLS-1$
		buffer.append("mi = 3; // error: mi is private in D\n"); //$NON-NLS-1$
		buffer.append("si = 3; // error: si is private in D\n"); //$NON-NLS-1$
		buffer.append("B b;\n"); //$NON-NLS-1$
		buffer.append("b.mi = 3; // OK (b.mi is different from this->mi)\n"); //$NON-NLS-1$
		buffer.append("b.si = 3; // OK (b.si is different from this->si)\n"); //$NON-NLS-1$
		buffer.append("B::si = 3; // OK\n"); //$NON-NLS-1$
		buffer.append("B* bp1 = this; // error: B is a private base class\n"); //$NON-NLS-1$
		buffer.append("B* bp2 = (B*)this; // OK with cast\n"); //$NON-NLS-1$
		buffer.append("bp2->mi = 3; // OK: access through a pointer to B.\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.2-4):
	class B;
	class A {
	private:
	int i;
	friend void f(B*);
	};
	class B : public A { };
	void f(B* p) {
	p->i = 1; // OK: B* can be implicitly cast to A*,
	// and f has access to i in A
	}
	 --End Example]
	 */
	public void test11_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B;\n"); //$NON-NLS-1$
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("private:\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("friend void f(B*);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class B : public A { };\n"); //$NON-NLS-1$
		buffer.append("void f(B* p) {\n"); //$NON-NLS-1$
		buffer.append("p->i = 1; // OK: B* can be implicitly cast to A*,\n"); //$NON-NLS-1$
		buffer.append("// and f has access to i in A\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}

	/**
	 [--Start Example(CPP 11.4-1):
	class X {
	int a;
	friend void friend_set(X*, int);
	public:
	void member_set(int);
	};
	void friend_set(X* p, int i) { p->a = i; }
	void X::member_set(int i) { a = i; }
	void f()
	{
	X obj;
	friend_set(&obj,10);
	obj.member_set(10);
	}
	 --End Example]
	 */
	public void test11_4s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("friend void friend_set(X*, int);\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("void member_set(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void friend_set(X* p, int i) { p->a = i; }\n"); //$NON-NLS-1$
		buffer.append("void X::member_set(int i) { a = i; }\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("X obj;\n"); //$NON-NLS-1$
		buffer.append("friend_set(&obj,10);\n"); //$NON-NLS-1$
		buffer.append("obj.member_set(10);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.4-2a):
	class A {
	class B { };
	friend class X;
	};
	class X : A::B { // illformed:
	A::B cannot be accessed
	// in the baseclause for X
	A::B mx; // OK: A::B used to declare member of X
	class Y : A::B { // OK: A::B used to declare member of X
	A::B my; // illformed: A::B cannot be accessed
	// to declare members of nested class of X
	};
	};
	 --End Example]
	 */
	public void test11_4s2a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("class B { };\n"); //$NON-NLS-1$
		buffer.append("friend class X;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class X : A::B { // illformed:\n"); //$NON-NLS-1$
		buffer.append("//A::B cannot be accessed\n"); //$NON-NLS-1$
		buffer.append("// in the baseclause for X\n"); //$NON-NLS-1$
		buffer.append("A::B mx; // OK: A::B used to declare member of X\n"); //$NON-NLS-1$
		buffer.append("class Y : A::B { // OK: A::B used to declare member of X\n"); //$NON-NLS-1$
		buffer.append("A::B my; // illformed: A::B cannot be accessed\n"); //$NON-NLS-1$
		buffer.append("// to declare members of nested class of X\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.4-2b):
	class X {
	enum { a=100 };
	friend class Y;
	};
	class Y {
	int v[X::a]; // OK, Y is a friend of X
	};
	class Z {
	int v[X::a]; // error: X::a is private
	};
	 --End Example]
	 */
	public void test11_4s2b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("enum { a=100 };\n"); //$NON-NLS-1$
		buffer.append("friend class Y;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Y {\n"); //$NON-NLS-1$
		buffer.append("int v[X::a]; // OK, Y is a friend of X\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Z {\n"); //$NON-NLS-1$
		buffer.append("int v[X::a]; // error: X::a is private\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.4-4):
	class Y {
	friend char* X::foo(int);
	// ...
	};
	 --End Example]
	 */
	public void test11_4s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class Y {\n"); //$NON-NLS-1$
		buffer.append("friend char* X::foo(int);\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 11.4-5):
	class M {
	friend void f() { } // definition of global f, a friend of M,
	// not the definition of a member function
	};
	 --End Example]
	 */
	public void test11_4s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class M {\n"); //$NON-NLS-1$
		buffer.append("friend void f() { } // definition of global f, a friend of M,\n"); //$NON-NLS-1$
		buffer.append("// not the definition of a member function\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.4-8):
	class A {
	friend class B;
	int a;
	};
	class B {
	friend class C;
	};
	class C {
	void f(A* p)
	{
	p->a++; //error: C is not a friend of A
	// despite being a friend of a friend
	}
	};
	class D : public B {
	void f(A* p)
	{
	p->a++; //error: D is not a friend of A
	// despite being derived from a friend
	}
	};
	 --End Example]
	 */
	public void test11_4s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("friend class B;\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class B {\n"); //$NON-NLS-1$
		buffer.append("friend class C;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("void f(A* p)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("p->a++; //error: C is not a friend of A\n"); //$NON-NLS-1$
		buffer.append("// despite being a friend of a friend\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D : public B {\n"); //$NON-NLS-1$
		buffer.append("void f(A* p)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("p->a++; //error: D is not a friend of A\n"); //$NON-NLS-1$
		buffer.append("// despite being derived from a friend\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.4-9):
	class X;
	void a();
	void f() {
	class Y;
	extern void b();
	class A {
	friend class X; // OK, but X is a local class, not ::X
	friend class Y; // OK
	friend class Z; // OK, introduces local class Z
	friend void a(); // error, ::a is not considered
	friend void b(); // OK
	friend void c(); // error
	};
	X *px; // OK, but ::X is found
	Z *pz; // error, no Z is found
	}
	 --End Example]
	 */
	public void test11_4s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X;\n"); //$NON-NLS-1$
		buffer.append("void a();\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("class Y;\n"); //$NON-NLS-1$
		buffer.append("extern void b();\n"); //$NON-NLS-1$
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("friend class X; // OK, but X is a local class, not ::X\n"); //$NON-NLS-1$
		buffer.append("friend class Y; // OK\n"); //$NON-NLS-1$
		buffer.append("friend class Z; // OK, introduces local class Z\n"); //$NON-NLS-1$
		buffer.append("friend void a(); // error, ::a is not considered\n"); //$NON-NLS-1$
		buffer.append("friend void b(); // OK\n"); //$NON-NLS-1$
		buffer.append("friend void c(); // error\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("X *px; // OK, but ::X is found\n"); //$NON-NLS-1$
		buffer.append("Z *pz; // error, no Z is found\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 11.5-1):
	class B {
	protected:
	int i;
	static int j;
	};
	class D1 : public B {
	};
	class D2 : public B {
	friend void fr(B*,D1*,D2*);
	void mem(B*,D1*);
	};
	void fr(B* pb, D1* p1, D2* p2)
	{
	pb->i = 1; // illformed
	p1->i = 2; // illformed
	p2->i = 3; // OK (access through a D2)
	p2->B::i = 4; // OK (access through a D2, even though
	// naming class is B)
	int B::* pmi_B = &B::i; // illformed
	int B::* pmi_B2 = &D2::i; // OK (type of &D2::i is int B::*)
	B::j = 5; // OK (because refers to static member)
	D2::j =6; // OK (because refers to static member)
	}
	void D2::mem(B* pb, D1* p1)
	{
	pb->i = 1; // illformed
	p1->i = 2; // illformed
	i = 3; // OK (access through this)
	B::i = 4; // OK (access through this, qualification ignored)
	int B::* pmi_B = &B::i; // illformed
	int B::* pmi_B2 = &D2::i; // OK
	j = 5; // OK (because j refers to static member)
	B::j = 6; // OK (because B::j refers to static member)
	}
	void g(B* pb, D1* p1, D2* p2)
	{
	pb->i = 1; // illformed
	p1->i = 2; // illformed
	p2->i = 3; // illformed
	}
	 --End Example]
	 */
	public void test11_5s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B {\n"); //$NON-NLS-1$
		buffer.append("protected:\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("static int j;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D1 : public B {\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D2 : public B {\n"); //$NON-NLS-1$
		buffer.append("friend void fr(B*,D1*,D2*);\n"); //$NON-NLS-1$
		buffer.append("void mem(B*,D1*);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void fr(B* pb, D1* p1, D2* p2)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("pb->i = 1; // illformed\n"); //$NON-NLS-1$
		buffer.append("p1->i = 2; // illformed\n"); //$NON-NLS-1$
		buffer.append("p2->i = 3; // OK (access through a D2)\n"); //$NON-NLS-1$
		buffer.append("p2->B::i = 4; // OK (access through a D2, even though\n"); //$NON-NLS-1$
		buffer.append("// naming class is B)\n"); //$NON-NLS-1$
		buffer.append("int B::* pmi_B = &B::i; // illformed\n"); //$NON-NLS-1$
		buffer.append("int B::* pmi_B2 = &D2::i; // OK (type of &D2::i is int B::*)\n"); //$NON-NLS-1$
		buffer.append("B::j = 5; // OK (because refers to static member)\n"); //$NON-NLS-1$
		buffer.append("D2::j =6; // OK (because refers to static member)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void D2::mem(B* pb, D1* p1)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("pb->i = 1; // illformed\n"); //$NON-NLS-1$
		buffer.append("p1->i = 2; // illformed\n"); //$NON-NLS-1$
		buffer.append("i = 3; // OK (access through this)\n"); //$NON-NLS-1$
		buffer.append("B::i = 4; // OK (access through this, qualification ignored)\n"); //$NON-NLS-1$
		buffer.append("int B::* pmi_B = &B::i; // illformed\n"); //$NON-NLS-1$
		buffer.append("int B::* pmi_B2 = &D2::i; // OK\n"); //$NON-NLS-1$
		buffer.append("j = 5; // OK (because j refers to static member)\n"); //$NON-NLS-1$
		buffer.append("B::j = 6; // OK (because B::j refers to static member)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void g(B* pb, D1* p1, D2* p2)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("pb->i = 1; // illformed\n"); //$NON-NLS-1$
		buffer.append("p1->i = 2; // illformed\n"); //$NON-NLS-1$
		buffer.append("p2->i = 3; // illformed\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.6-1):
	class B {
	public:
	virtual int f();
	};
	class D : public B {
	private:
	int f();
	};
	void f()
	{
	D d;
	B* pb = &d;
	D* pd = &d;
	pb->f(); //OK: B::f() is public,
	// D::f() is invoked
	pd->f(); //error: D::f() is private
	}
	 --End Example]
	 */
	public void test11_6s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("virtual int f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D : public B {\n"); //$NON-NLS-1$
		buffer.append("private:\n"); //$NON-NLS-1$
		buffer.append("int f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("D d;\n"); //$NON-NLS-1$
		buffer.append("B* pb = &d;\n"); //$NON-NLS-1$
		buffer.append("D* pd = &d;\n"); //$NON-NLS-1$
		buffer.append("pb->f(); //OK: B::f() is public,\n"); //$NON-NLS-1$
		buffer.append("// D::f() is invoked\n"); //$NON-NLS-1$
		buffer.append("pd->f(); //error: D::f() is private\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.7-1):
	class W { public: void f(); };
	class A : private virtual W { };
	class B : public virtual W { };
	class C : public A, public B {
	void f() { W::f(); } // OK
	};
	 --End Example]
	 */
	public void test11_7s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class W { public: void f(); };\n"); //$NON-NLS-1$
		buffer.append("class A : private virtual W { };\n"); //$NON-NLS-1$
		buffer.append("class B : public virtual W { };\n"); //$NON-NLS-1$
		buffer.append("class C : public A, public B {\n"); //$NON-NLS-1$
		buffer.append("void f() { W::f(); } // OK\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.8-1):
	class E {
	int x;
	class B { };
	class I {
	B b; // error: E::B is private
	int y;
	void f(E* p, int i)
	{
	p->x = i; // error: E::x is private
	}
	};
	int g(I* p)
	{
	return p->y; // error: I::y is private
	}
	};
	 --End Example]
	 */
	public void test11_8s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class E {\n"); //$NON-NLS-1$
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("class B { };\n"); //$NON-NLS-1$
		buffer.append("class I {\n"); //$NON-NLS-1$
		buffer.append("B b; // error: E::B is private\n"); //$NON-NLS-1$
		buffer.append("int y;\n"); //$NON-NLS-1$
		buffer.append("void f(E* p, int i)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("p->x = i; // error: E::x is private\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int g(I* p)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("return p->y; // error: I::y is private\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 11.8-2):
	class C {
	class A { };
	A *p; // OK
	class B : A // OK
	{
	A *q; // OK because of injection of name A in A
	C::A *r; // error, C::A is inaccessible
	B *s; // OK because of injection of name B in B
	C::B *t; // error, C::B is inaccessible
	};
	};
	 --End Example]
	 */
	public void test11_8s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("class A { };\n"); //$NON-NLS-1$
		buffer.append("A *p; // OK\n"); //$NON-NLS-1$
		buffer.append("class B : A // OK\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("A *q; // OK because of injection of name A in A\n"); //$NON-NLS-1$
		buffer.append("C::A *r; // error, C::A is inaccessible\n"); //$NON-NLS-1$
		buffer.append("B *s; // OK because of injection of name B in B\n"); //$NON-NLS-1$
		buffer.append("C::B *t; // error, C::B is inaccessible\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.1-1):
	class C {
	public:
	C(); //declares the constructor
	};
	C::C() { } // defines the constructor
	 --End Example]
	 */
	public void test12_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("C(); //declares the constructor\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("C::C() { } // defines the constructor\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.1-15):
	struct C;
	void no_opt(C*);
	struct C {
	int c;
	C() : c(0) { no_opt(this); }
	};
	const C cobj;
	void no_opt(C* cptr) {
	int i = cobj.c * 100; // value of cobj.c is unspecified
	cptr->c = 1;
	cout << cobj.c * 100 // value of cobj.c is unspecified
	<< '\n';
	}
	 --End Example]
	 */
	public void test12_1s15() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct C;\n"); //$NON-NLS-1$
		buffer.append("void no_opt(C*);\n"); //$NON-NLS-1$
		buffer.append("struct C {\n"); //$NON-NLS-1$
		buffer.append("int c;\n"); //$NON-NLS-1$
		buffer.append("C() : c(0) { no_opt(this); }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("const C cobj;\n"); //$NON-NLS-1$
		buffer.append("void no_opt(C* cptr) {\n"); //$NON-NLS-1$
		buffer.append("int i = cobj.c * 100; // value of cobj.c is unspecified\n"); //$NON-NLS-1$
		buffer.append("cptr->c = 1;\n"); //$NON-NLS-1$
		buffer.append("cout << cobj.c * 100 // value of cobj.c is unspecified\n"); //$NON-NLS-1$
		buffer.append("<< '\n';\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 12.2-2):
	class X {
	// ...
	public:
	// ...
	X(int);
	X(const X&);
	~X();
	};
	X f(X);
	void g()
	{
	X a(1);
	X b = f(X(2));
	a = f(a);
	}
	 --End Example]
	 */
	public void test12_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("X(int);\n"); //$NON-NLS-1$
		buffer.append("X(const X&);\n"); //$NON-NLS-1$
		buffer.append("~X();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("X f(X);\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("X a(1);\n"); //$NON-NLS-1$
		buffer.append("X b = f(X(2));\n"); //$NON-NLS-1$
		buffer.append("a = f(a);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.2-5):
	class C {
	// ...
	public:
	C();
	C(int);
	friend C operator+(const C&, const C&);
	~C();
	};
	C obj1;
	const C& cr = C(16)+C(23);
	C obj2;
	 --End Example]
	 */
	public void test12_2s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("C();\n"); //$NON-NLS-1$
		buffer.append("C(int);\n"); //$NON-NLS-1$
		buffer.append("friend C operator+(const C&, const C&);\n"); //$NON-NLS-1$
		buffer.append("~C();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("C obj1;\n"); //$NON-NLS-1$
		buffer.append("const C& cr = C(16)+C(23);\n"); //$NON-NLS-1$
		buffer.append("C obj2;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test12_3s4() throws Exception {
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

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.3-5):
	class X {
	public:
	// ...
	operator int();
	};
	class Y : public X {
	public:
	// ...
	operator char();
	};
	void f(Y& a)
	{
	if (a) { // illformed:
	// X::operator int() or Y::operator char()
	// ...
	}
	}
	 --End Example]
	 */
	public void test12_3s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("operator int();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Y : public X {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("operator char();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f(Y& a)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("if (a) { // illformed:\n"); //$NON-NLS-1$
		buffer.append("// X::operator int() or Y::operator char()\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.3.1-1):
	class X {
	// ...
	public:
	X(int);
	X(const char*, int =0);
	};
	void f(X arg)
	{
	X a = 1; // a = X(1)
	X b = "Jessie"; // b = X("Jessie",0)
	a = 2; // a = X(2)
	f(3); // f(X(3))
	}
	 --End Example]
	 */
	public void test12_3_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("X(int);\n"); //$NON-NLS-1$
		buffer.append("X(const char*, int =0);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f(X arg)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("X a = 1; // a = X(1)\n"); //$NON-NLS-1$
		buffer.append("X b = \"Jessie\"; // b = X(\"Jessie\",0)\n"); //$NON-NLS-1$
		buffer.append("a = 2; // a = X(2)\n"); //$NON-NLS-1$
		buffer.append("f(3); // f(X(3))\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.3.1-2):
	class Z {
	public:
	explicit Z();
	explicit Z(int);
	// ...
	};
	Z a; // OK: defaultinitialization performed
	Z a1 = 1; // error: no implicit conversion
	Z a3 = Z(1); // OK: direct initialization syntax used
	Z a2(1); // OK: direct initialization syntax used
	Z* p = new Z(1); // OK: direct initialization syntax used
	Z a4 = (Z)1; // OK: explicit cast used
	Z a5 = static_cast<Z>(1); // OK: explicit cast used
	 --End Example]
	 */
	public void test12_3_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class Z {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("explicit Z();\n"); //$NON-NLS-1$
		buffer.append("explicit Z(int);\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("Z a; // OK: defaultinitialization performed\n"); //$NON-NLS-1$
		buffer.append("Z a1 = 1; // error: no implicit conversion\n"); //$NON-NLS-1$
		buffer.append("Z a3 = Z(1); // OK: direct initialization syntax used\n"); //$NON-NLS-1$
		buffer.append("Z a2(1); // OK: direct initialization syntax used\n"); //$NON-NLS-1$
		buffer.append("Z* p = new Z(1); // OK: direct initialization syntax used\n"); //$NON-NLS-1$
		buffer.append("Z a4 = (Z)1; // OK: explicit cast used\n"); //$NON-NLS-1$
		buffer.append("Z a5 = static_cast<Z>(1); // OK: explicit cast used\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.3.2-2):
	class X {
	// ...
	public:
	operator int();
	};
	void f(X a)
	{
	int i = int(a);
	i = (int)a;
	i = a;
	}
	 --End Example]
	 */
	public void test12_3_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("operator int();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f(X a)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int i = int(a);\n"); //$NON-NLS-1$
		buffer.append("i = (int)a;\n"); //$NON-NLS-1$
		buffer.append("i = a;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.3.2-3):
	void g(X a, X b)
	{
	int i = (a) ? 1+a : 0;
	int j = (a&&b) ? a+b : i;
	if (a) { // ...
	}
	}
	 --End Example]
	 */
	public void test12_3_2s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void g(X a, X b)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int i = (a) ? 1+a : 0;\n"); //$NON-NLS-1$
		buffer.append("int j = (a&&b) ? a+b : i;\n"); //$NON-NLS-1$
		buffer.append("if (a) { // ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 12.4-12):
	struct B {
	virtual ~B() { }
	};
	struct D : B {
	~D() { }
	};
	D D_object;
	typedef B B_alias;
	B* B_ptr = &D_object;
	void f() {
	D_object.B::~B(); // calls B’s destructor
	B_ptr->~B(); //calls D’s destructor
	B_ptr->~B_alias(); // calls D’s destructor
	B_ptr->B_alias::~B(); // calls B’s destructor
	B_ptr->B_alias::~B_alias(); // error, no B_alias in class B
	}
	 --End Example]
	 */
	public void test12_4s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("virtual ~B() { }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D : B {\n"); //$NON-NLS-1$
		buffer.append("~D() { }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("D D_object;\n"); //$NON-NLS-1$
		buffer.append("typedef B B_alias;\n"); //$NON-NLS-1$
		buffer.append("B* B_ptr = &D_object;\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("D_object.B::~B(); // calls B’s destructor\n"); //$NON-NLS-1$
		buffer.append("B_ptr->~B(); //calls D’s destructor\n"); //$NON-NLS-1$
		buffer.append("B_ptr->~B_alias(); // calls D’s destructor\n"); //$NON-NLS-1$
		buffer.append("B_ptr->B_alias::~B(); // calls B’s destructor\n"); //$NON-NLS-1$
		buffer.append("B_ptr->B_alias::~B_alias(); // error, no B_alias in class B\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 12.4-13):
	void* operator new(size_t, void* p) { return p; }
	struct X {
	// ...
	X(int);
	~X();
	};
	void f(X* p);
	void g() // rare, specialized use:
	{
	char* buf = new char[sizeof(X)];
	X* p = new(buf) X(222); // use buf[] and initialize
	f(p);
	p->X::~X(); //cleanup
	}
	 --End Example]
	 */
	public void test12_4s13() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void* operator new(size_t, void* p) { return p; }\n"); //$NON-NLS-1$
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("X(int);\n"); //$NON-NLS-1$
		buffer.append("~X();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f(X* p);\n"); //$NON-NLS-1$
		buffer.append("void g() // rare, specialized use:\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("char* buf = new char[sizeof(X)];\n"); //$NON-NLS-1$
		buffer.append("X* p = new(buf) X(222); // use buf[] and initialize\n"); //$NON-NLS-1$
		buffer.append("f(p);\n"); //$NON-NLS-1$
		buffer.append("p->X::~X(); //cleanup\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 12.5-2):
	class Arena;
	struct B {
	void* operator new(size_t, Arena*);
	};
	struct D1 : B {
	};
	Arena* ap;
	void foo(int i)
	{
	new (ap) D1; // calls B::operator new(size_t, Arena*)
	new D1[i]; // calls ::operator new[](size_t)
	new D1; // illformed: ::operator new(size_t) hidden
	}
	 --End Example]
	 */
	public void test12_5s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class Arena;\n"); //$NON-NLS-1$
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("void* operator new(size_t, Arena*);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D1 : B {\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("Arena* ap;\n"); //$NON-NLS-1$
		buffer.append("void foo(int i)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("new (ap) D1; // calls B::operator new(size_t, Arena*)\n"); //$NON-NLS-1$
		buffer.append("new D1[i]; // calls ::operator new[](size_t)\n"); //$NON-NLS-1$
		buffer.append("new D1; // illformed: ::operator new(size_t) hidden\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 12.5-6):
	class X {
	// ...
	void operator delete(void*);
	void operator delete[](void*, size_t);
	};
	class Y {
	// ...
	void operator delete(void*, size_t);
	void operator delete[](void*);
	};
	 --End Example]
	 */
	public void test12_5s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("void operator delete(void*);\n"); //$NON-NLS-1$
		buffer.append("void operator delete[](void*, size_t);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Y {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("void operator delete(void*, size_t);\n"); //$NON-NLS-1$
		buffer.append("void operator delete[](void*);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 12.6.1-1):
	class complex {
	// ...
	public:
	complex();
	complex(double);
	complex(double,double);
	// ...
	};
	complex sqrt(complex,complex);
	int foo() {
	complex a(1); // initialize by a call of
	// complex(double)
	complex b = a; // initialize by a copy of a
	complex c = complex(1,2); // construct complex(1,2)
	// using complex(double,double)
	// copy it into c
	complex d = sqrt(b,c); // call sqrt(complex,complex)
	// and copy the result into d
	complex e; // initialize by a call of
	// complex()
	complex f = 3; // construct complex(3) using
	// complex(double)
	// copy it into f
	complex g = { 1, 2 }; // error; constructor is required
	}
	 --End Example]
	 */
	public void test12_6_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class complex {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("complex();\n"); //$NON-NLS-1$
		buffer.append("complex(double);\n"); //$NON-NLS-1$
		buffer.append("complex(double,double);\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("complex sqrt(complex,complex);\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("complex a(1); // initialize by a call of\n"); //$NON-NLS-1$
		buffer.append("// complex(double)\n"); //$NON-NLS-1$
		buffer.append("complex b = a; // initialize by a copy of a\n"); //$NON-NLS-1$
		buffer.append("complex c = complex(1,2); // construct complex(1,2)\n"); //$NON-NLS-1$
		buffer.append("// using complex(double,double)\n"); //$NON-NLS-1$
		buffer.append("// copy it into c\n"); //$NON-NLS-1$
		buffer.append("complex d = sqrt(b,c); // call sqrt(complex,complex)\n"); //$NON-NLS-1$
		buffer.append("// and copy the result into d\n"); //$NON-NLS-1$
		buffer.append("complex e; // initialize by a call of\n"); //$NON-NLS-1$
		buffer.append("// complex()\n"); //$NON-NLS-1$
		buffer.append("complex f = 3; // construct complex(3) using\n"); //$NON-NLS-1$
		buffer.append("// complex(double)\n"); //$NON-NLS-1$
		buffer.append("// copy it into f\n"); //$NON-NLS-1$
		buffer.append("complex g = { 1, 2 }; // error; constructor is required\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.6.1-2):
	class complex {
	// ...
	public:
	complex();
	complex(double);
	complex(double,double);
	// ...
	};
	complex v[6] = { 1,complex(1,2),complex(),2 };
	class X {
	public:
	int i;
	float f;
	complex c;
	} x = { 99, 88.8, 77.7 };
	 --End Example]
	 */
	public void test12_6_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class complex {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("complex();\n"); //$NON-NLS-1$
		buffer.append("complex(double);\n"); //$NON-NLS-1$
		buffer.append("complex(double,double);\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("complex v[6] = { 1,complex(1,2),complex(),2 };\n"); //$NON-NLS-1$
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("float f;\n"); //$NON-NLS-1$
		buffer.append("complex c;\n"); //$NON-NLS-1$
		buffer.append("} x = { 99, 88.8, 77.7 };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.6.2-2a):
	struct A { A(); };
	typedef A global_A;
	struct B { };
	struct C: public A, public B { C(); };
	C::C(): global_A() { } // meminitializer for base A
	 --End Example]
	 */
	public void test12_6_2s2a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A { A(); };\n"); //$NON-NLS-1$
		buffer.append("typedef A global_A;\n"); //$NON-NLS-1$
		buffer.append("struct B { };\n"); //$NON-NLS-1$
		buffer.append("struct C: public A, public B { C(); };\n"); //$NON-NLS-1$
		buffer.append("C::C(): global_A() { } // meminitializer for base A\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.6.2-2b):
	struct A { A(); };
	struct B: public virtual A { };
	struct C: public A, public B { C(); };
	C::C(): A() { } // illformed: which A?
	 --End Example]
	 */
	public void test12_6_2s2b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A { A(); };\n"); //$NON-NLS-1$
		buffer.append("struct B: public virtual A { };\n"); //$NON-NLS-1$
		buffer.append("struct C: public A, public B { C(); };\n"); //$NON-NLS-1$
		buffer.append("C::C(): A() { } // illformed: which A?\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 12.6.2-3):
	struct B1 { B1(int); };
	struct B2 { B2(int); };
	struct D : B1, B2 {
	D(int);
	B1 b;
	const int c;
	};
	D::D(int a) : B2(a+1), B1(a+2), c(a+3), b(a+4)
	{  }
	D d(10);
	 --End Example]
	 */
	public void test12_6_2s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B1 { B1(int); };\n"); //$NON-NLS-1$
		buffer.append("struct B2 { B2(int); };\n"); //$NON-NLS-1$
		buffer.append("struct D : B1, B2 {\n"); //$NON-NLS-1$
		buffer.append("D(int);\n"); //$NON-NLS-1$
		buffer.append("B1 b;\n"); //$NON-NLS-1$
		buffer.append("const int c;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("D::D(int a) : B2(a+1), B1(a+2), c(a+3), b(a+4)\n"); //$NON-NLS-1$
		buffer.append("{  }\n"); //$NON-NLS-1$
		buffer.append("D d(10);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.6.2-6):
	class V {
	public:
	V();
	V(int);
	// ...
	};
	class A : public virtual V {
	public:
	A();
	A(int);
	// ...
	};
	class B : public virtual V {
	public:
	B();
	B(int);
	// ...
	};
	class C : public A, public B, private virtual V {
	public:
	C();
	C(int);
	// ...
	};
	A::A(int i) : V(i) { }
	B::B(int i) { }
	C::C(int i) { }
	V v(1); // use V(int)
	A a(2); // use V(int)
	B b(3); // use V()
	C c(4); // use V()
	 --End Example]
	 */
	public void test12_6_2s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class V {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("V();\n"); //$NON-NLS-1$
		buffer.append("V(int);\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class A : public virtual V {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("A();\n"); //$NON-NLS-1$
		buffer.append("A(int);\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class B : public virtual V {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("B();\n"); //$NON-NLS-1$
		buffer.append("B(int);\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class C : public A, public B, private virtual V {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("C();\n"); //$NON-NLS-1$
		buffer.append("C(int);\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("A::A(int i) : V(i) { }\n"); //$NON-NLS-1$
		buffer.append("B::B(int i) { }\n"); //$NON-NLS-1$
		buffer.append("C::C(int i) { }\n"); //$NON-NLS-1$
		buffer.append("V v(1); // use V(int)\n"); //$NON-NLS-1$
		buffer.append("A a(2); // use V(int)\n"); //$NON-NLS-1$
		buffer.append("B b(3); // use V()\n"); //$NON-NLS-1$
		buffer.append("C c(4); // use V()\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.6.2-7):
	class X {
	int a;
	int b;
	int i;
	int j;
	public:
	const int& r;
	X(int i): r(a), b(i), i(i), j(this->i) {}
	};
	 --End Example]
	 */
	public void test12_6_2s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("int b;\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("int j;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("const int& r;\n"); //$NON-NLS-1$
		buffer.append("X(int i): r(a), b(i), i(i), j(this->i) {}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.6.2-8):
	class A {
	public:
	A(int);
	};
	class B : public A {
	int j;
	public:
	int f();
	B() : A(f()), // undefined: calls member function
	// but base A not yet initialized
	j(f()) { } // welldefined: bases are all initialized
	};
	class C {
	public:
	C(int);
	};
	class D : public B, C {
	int i;
	public:
	D() : C(f()), // undefined: calls member function
	// but base C not yet initialized
	i(f()) {} // welldefined: bases are all initialized
	};
	 --End Example]
	 */
	public void test12_6_2s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("A(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class B : public A {\n"); //$NON-NLS-1$
		buffer.append("int j;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int f();\n"); //$NON-NLS-1$
		buffer.append("B() : A(f()), // undefined: calls member function\n"); //$NON-NLS-1$
		buffer.append("// but base A not yet initialized\n"); //$NON-NLS-1$
		buffer.append("j(f()) { } // welldefined: bases are all initialized\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("C(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D : public B, C {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("D() : C(f()), // undefined: calls member function\n"); //$NON-NLS-1$
		buffer.append("// but base C not yet initialized\n"); //$NON-NLS-1$
		buffer.append("i(f()) {} // welldefined: bases are all initialized\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.7-1 a):
	struct X { int i; };
	struct Y : X { };
	struct A { int a; };
	struct B : public A { int j; Y y; };
	extern B bobj;
	B* pb = &bobj; // OK
	int* p1 = &bobj.a; // undefined, refers to base class member
	int* p2 = &bobj.y.i; // undefined, refers to member’s member
	A* pa = &bobj; // undefined, upcast to a base class type
	B bobj; // definition of bobj
	extern X xobj;
	int* p3 = &xobj.i; // OK, X is a POD class
	X xobj;
	 --End Example]
	 */
	public void test12_7s1_a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X { int i; };\n"); //$NON-NLS-1$
		buffer.append("struct Y : X { };\n"); //$NON-NLS-1$
		buffer.append("struct A { int a; };\n"); //$NON-NLS-1$
		buffer.append("struct B : public A { int j; Y y; };\n"); //$NON-NLS-1$
		buffer.append("extern B bobj;\n"); //$NON-NLS-1$
		buffer.append("B* pb = &bobj; // OK\n"); //$NON-NLS-1$
		buffer.append("int* p1 = &bobj.a; // undefined, refers to base class member\n"); //$NON-NLS-1$
		buffer.append("int* p2 = &bobj.y.i; // undefined, refers to member’s member\n"); //$NON-NLS-1$
		buffer.append("A* pa = &bobj; // undefined, upcast to a base class type\n"); //$NON-NLS-1$
		buffer.append("B bobj; // definition of bobj\n"); //$NON-NLS-1$
		buffer.append("extern X xobj;\n"); //$NON-NLS-1$
		buffer.append("int* p3 = &xobj.i; // OK, X is a POD class\n"); //$NON-NLS-1$
		buffer.append("X xobj;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.7-1 b):
	struct W { int j; };
	struct X : public virtual W { };
	struct Y {
	int *p;
	X x;
	Y() : p(&x.j) // undefined, x is not yet constructed
	{ }
	};
	 --End Example]
	 */
	public void test12_7s1_b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct W { int j; };\n"); //$NON-NLS-1$
		buffer.append("struct X : public virtual W { };\n"); //$NON-NLS-1$
		buffer.append("struct Y {\n"); //$NON-NLS-1$
		buffer.append("int *p;\n"); //$NON-NLS-1$
		buffer.append("X x;\n"); //$NON-NLS-1$
		buffer.append("Y() : p(&x.j) // undefined, x is not yet constructed\n"); //$NON-NLS-1$
		buffer.append("{ }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.7-3):
	class V {
	public:
	virtual void f();
	virtual void g();
	};
	class A : public virtual V {
	public:
	virtual void f();
	};
	class B : public virtual V {
	public:
	virtual void g();
	B(V*, A*);
	};
	class D : public A, B {
	public:
	virtual void f();
	virtual void g();
	D() : B((A*)this, this) { }
	};
	B::B(V* v, A* a) {
	f(); //calls V::f, not A::f
	g(); //calls B::g, not D::g
	v->g(); // v is base of B, the call is welldefined,	calls B::g
	a->f(); //undefined behavior, a’s type not a base of B
	}
	 --End Example]
	 */
	public void test12_7s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class V {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("virtual void f();\n"); //$NON-NLS-1$
		buffer.append("virtual void g();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class A : public virtual V {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("virtual void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class B : public virtual V {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("virtual void g();\n"); //$NON-NLS-1$
		buffer.append("B(V*, A*);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D : public A, B {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("virtual void f();\n"); //$NON-NLS-1$
		buffer.append("virtual void g();\n"); //$NON-NLS-1$
		buffer.append("D() : B((A*)this, this) { }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("B::B(V* v, A* a) {\n"); //$NON-NLS-1$
		buffer.append("f(); //calls V::f, not A::f\n"); //$NON-NLS-1$
		buffer.append("g(); //calls B::g, not D::g\n"); //$NON-NLS-1$
		buffer.append("v->g(); // v is base of B, the call is welldefined, calls B::g\n"); //$NON-NLS-1$
		buffer.append("a->f(); //undefined behavior, a’s type not a base of B\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.7-6):
	class V {
	public:
	virtual void f();
	};
	class A : public virtual V { };
	class B : public virtual V {
	public:
	B(V*, A*);
	};
	class D : public A, B {
	public:
	D() : B((A*)this, this) { }
	};
	B::B(V* v, A* a) {
	typeid(*this); // type_info for B
	typeid(*v); //welldefined: *v has type V, a base of B
	// yields type_info for B
	typeid(*a); //undefined behavior: type A not a base of B
	dynamic_cast<B*>(v); // welldefined: v of type V*, V base of B
	// results in B*
	dynamic_cast<B*>(a); // undefined behavior,
	// a has type A*, A not a base of B
	}
	 --End Example]
	 */
	public void test12_7s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class V {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("virtual void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class A : public virtual V { };\n"); //$NON-NLS-1$
		buffer.append("class B : public virtual V {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("B(V*, A*);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D : public A, B {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("D() : B((A*)this, this) { }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("B::B(V* v, A* a) {\n"); //$NON-NLS-1$
		buffer.append("typeid(*this); // type_info for B\n"); //$NON-NLS-1$
		buffer.append("typeid(*v); //welldefined: *v has type V, a base of B\n"); //$NON-NLS-1$
		buffer.append("// yields type_info for B\n"); //$NON-NLS-1$
		buffer.append("typeid(*a); //undefined behavior: type A not a base of B\n"); //$NON-NLS-1$
		buffer.append("dynamic_cast<B*>(v); // welldefined: v of type V*, V base of B\n"); //$NON-NLS-1$
		buffer.append("// results in B*\n"); //$NON-NLS-1$
		buffer.append("dynamic_cast<B*>(a); // undefined behavior,\n"); //$NON-NLS-1$
		buffer.append("// a has type A*, A not a base of B\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.8-2a):
	class X {
	// ...
	public:
	X(int);
	X(const X&, int = 1);
	};
	X a(1); // calls X(int);
	X b(a, 0); // calls X(const X&, int);
	X c = b; // calls X(const X&, int);
	 --End Example]
	 */
	public void test12_8s2a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("X(int);\n"); //$NON-NLS-1$
		buffer.append("X(const X&, int = 1);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("X a(1); // calls X(int);\n"); //$NON-NLS-1$
		buffer.append("X b(a, 0); // calls X(const X&, int);\n"); //$NON-NLS-1$
		buffer.append("X c = b; // calls X(const X&, int);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.8-2b):
	class X {
	// ...
	public:
	X(const X&);
	X(X&); //OK
	};
	 --End Example]
	 */
	public void test12_8s2b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("X(const X&);\n"); //$NON-NLS-1$
		buffer.append("X(X&); //OK\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.8-2c):
	struct X {
	X(); //default constructor
	X(X&); //copy constructor with a nonconst parameter
	};
	const X cx;
	X x = cx; // error - X::X(X&) cannot copy cx into x
	 --End Example]
	 */
	public void test12_8s2c() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("X(); //default constructor\n"); //$NON-NLS-1$
		buffer.append("X(X&); //copy constructor with a nonconst parameter\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("const X cx;\n"); //$NON-NLS-1$
		buffer.append("X x = cx; // error - X::X(X&) cannot copy cx into x\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.8-3):
	struct S {
	template<typename T> S(T);
	};
	S f();
	void g() {
	S a( f() ); // does not instantiate member template
	}
	 --End Example]
	 */
	public void test12_8s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("template<typename T> S(T);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("S f();\n"); //$NON-NLS-1$
		buffer.append("void g() {\n"); //$NON-NLS-1$
		buffer.append("S a( f() ); // does not instantiate member template\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 12.8-3d):
	void h(int());
	void h(int (*)()); // redeclaration of h(int())
	void h(int x()) { } // definition of h(int())
	void h(int (*x)()) { } // illformed: redefinition of h(int())
	 --End Example]
	 */
	public void test12_8s3d() throws Exception { 
		StringBuffer buffer = new StringBuffer();
		buffer.append("void h(int());\n"); //$NON-NLS-1$
		buffer.append("void h(int (*)()); // redeclaration of h(int())\n"); //$NON-NLS-1$
		buffer.append("void h(int x()) { } // definition of h(int())\n"); //$NON-NLS-1$
		buffer.append("void h(int (*x)()) { } // illformed: redefinition of h(int())\n"); //$NON-NLS-1$
		
		parse(buffer.toString(), ParserLanguage.CPP, true, 1);
	}
	
	/**
	 [--Start Example(CPP 12.8-4a):
	struct X {
	X(const X&, int);
	};
	 --End Example]
	 */
	public void test12_8s4a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("X(const X&, int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.8-4b):
	struct X {
	X(const X&, int);
	};
	X::X(const X& x, int i =0) {  }
	 --End Example]
	 */
	public void test12_8s4b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("X(const X&, int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("X::X(const X& x, int i =0) {  }\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.8-9):
	struct X {
	X();
	X& operator=(X&);
	};
	const X cx;
	X x;
	void f() {
	x = cx; // error:
	// X::operator=(X&) cannot assign cx into x
	}
	 --End Example]
	 */
	public void test12_8s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("X();\n"); //$NON-NLS-1$
		buffer.append("X& operator=(X&);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("const X cx;\n"); //$NON-NLS-1$
		buffer.append("X x;\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("x = cx; // error:\n"); //$NON-NLS-1$
		buffer.append("// X::operator=(X&) cannot assign cx into x\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.8-13):
	struct V { };
	struct A : virtual V { };
	struct B : virtual V { };
	struct C : B, A { };
	 --End Example]
	 */
	public void test12_8s13() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct V { };\n"); //$NON-NLS-1$
		buffer.append("struct A : virtual V { };\n"); //$NON-NLS-1$
		buffer.append("struct B : virtual V { };\n"); //$NON-NLS-1$
		buffer.append("struct C : B, A { };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.8-15):
	class Thing {
	public:
	Thing();
	~Thing();
	Thing(const Thing&);
	Thing operator=(const Thing&);
	void fun();
	};
	Thing f() {
	Thing t;
	return t;
	}
	Thing t2 = f();
	 --End Example]
	 */
	public void test12_8s15() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class Thing {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("Thing();\n"); //$NON-NLS-1$
		buffer.append("~Thing();\n"); //$NON-NLS-1$
		buffer.append("Thing(const Thing&);\n"); //$NON-NLS-1$
		buffer.append("Thing operator=(const Thing&);\n"); //$NON-NLS-1$
		buffer.append("void fun();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("Thing f() {\n"); //$NON-NLS-1$
		buffer.append("Thing t;\n"); //$NON-NLS-1$
		buffer.append("return t;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("Thing t2 = f();\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13-2):
	double abs(double);
	int abs(int);
	int foo() {
	abs(1); //call abs(int);
	abs(1.0); //call abs(double);
	}
	 --End Example]
	 */
	public void test13s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("double abs(double);\n"); //$NON-NLS-1$
		buffer.append("int abs(int);\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("abs(1); //call abs(int);\n"); //$NON-NLS-1$
		buffer.append("abs(1.0); //call abs(double);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.1-2):
	class X {
	static void f();
	void f(); // illformed
	void f() const; // illformed
	void f() const volatile; // illformed
	void g();
	void g() const; // OK: no static g
	void g() const volatile; // OK: no static g
	};
	 --End Example]
	 */
	public void test12_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("static void f();\n"); //$NON-NLS-1$
		buffer.append("void f(); // illformed\n"); //$NON-NLS-1$
		buffer.append("void f() const; // illformed\n"); //$NON-NLS-1$
		buffer.append("void f() const volatile; // illformed\n"); //$NON-NLS-1$
		buffer.append("void g();\n"); //$NON-NLS-1$
		buffer.append("void g() const; // OK: no static g\n"); //$NON-NLS-1$
		buffer.append("void g() const volatile; // OK: no static g\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.1-3a):
	typedef int Int;
	void f(int i);
	void f(Int i); // OK: redeclaration of f(int)
	void f(int i) {  }
	void f(Int i) {  } // error: redefinition of f(int)
	 --End Example]
	 */
	public void test12_1s3a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int Int;\n"); //$NON-NLS-1$
		buffer.append("void f(int i);\n"); //$NON-NLS-1$
		buffer.append("void f(Int i); // OK: redeclaration of f(int)\n"); //$NON-NLS-1$
		buffer.append("void f(int i) {  }\n"); //$NON-NLS-1$
		buffer.append("void f(Int i) {  } // error: redefinition of f(int)\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 1);
	}
	
	/**
	 [--Start Example(CPP 12.1-3b):
	enum E { a };
	void f(int i) { }
	void f(E i) {  }
	 --End Example]
	 */
	public void test12_1s3b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("enum E { a };\n"); //$NON-NLS-1$
		buffer.append("void f(int i) { }\n"); //$NON-NLS-1$
		buffer.append("void f(E i) {  }\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.1-3c):
	int f(char*);
	int f(char[]); // same as f(char*);
	int f(char[7]); // same as f(char*);
	int f(char[9]); // same as f(char*);
	int g(char(*)[10]);
	int g(char[5][10]); // same as g(char(*)[10]);
	int g(char[7][10]); // same as g(char(*)[10]);
	int g(char(*)[20]); // different from g(char(*)[10]);
	 --End Example]
	 */
	public void test12_1s3c() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(char*);\n"); //$NON-NLS-1$
		buffer.append("int f(char[]); // same as f(char*);\n"); //$NON-NLS-1$
		buffer.append("int f(char[7]); // same as f(char*);\n"); //$NON-NLS-1$
		buffer.append("int f(char[9]); // same as f(char*);\n"); //$NON-NLS-1$
		buffer.append("int g(char(*)[10]);\n"); //$NON-NLS-1$
		buffer.append("int g(char[5][10]); // same as g(char(*)[10]);\n"); //$NON-NLS-1$
		buffer.append("int g(char[7][10]); // same as g(char(*)[10]);\n"); //$NON-NLS-1$
		buffer.append("int g(char(*)[20]); // different from g(char(*)[10]);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.8-3e):
	typedef const int cInt;
	int f (int);
	int f (const int); // redeclaration of f(int)
	int f (int) {  } // definition of f(int)
	int f (cInt) {  } // error: redefinition of f(int)
	 --End Example]
	 */
	public void test12_8s3e() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef const int cInt;\n"); //$NON-NLS-1$
		buffer.append("int f (int);\n"); //$NON-NLS-1$
		buffer.append("int f (const int); // redeclaration of f(int)\n"); //$NON-NLS-1$
		buffer.append("int f (int) {  } // definition of f(int)\n"); //$NON-NLS-1$
		buffer.append("int f (cInt) {  } // error: redefinition of f(int)\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.8-3f):
	void f (int i, int j);
	void f (int i, int j = 99); // OK: redeclaration of f(int, int)
	void f (int i = 88, int j); // OK: redeclaration of f(int, int)
	void f (); // OK: overloaded declaration of f
	void prog ()
	{
	f (1, 2); // OK: call f(int, int)
	f (1); // OK: call f(int, int)
	f (); // Error: f(int, int) or f()?
	}
	 --End Example]
	 */
	public void test12_8s3f() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f (int i, int j);\n"); //$NON-NLS-1$
		buffer.append("void f (int i, int j = 99); // OK: redeclaration of f(int, int)\n"); //$NON-NLS-1$
		buffer.append("void f (int i = 88, int j); // OK: redeclaration of f(int, int)\n"); //$NON-NLS-1$
		buffer.append("void f (); // OK: overloaded declaration of f\n"); //$NON-NLS-1$
		buffer.append("void prog ()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f (1, 2); // OK: call f(int, int)\n"); //$NON-NLS-1$
		buffer.append("f (1); // OK: call f(int, int)\n"); //$NON-NLS-1$
		buffer.append("f (); // Error: f(int, int) or f()?\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 13.2-1a):
	class B {
	public:
	int f(int);
	};
	class D : public B {
	public:
	int f(char*);
	};
	 --End Example]
	 */
	public void test13_2s1a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D : public B {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int f(char*);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.2-1b):
	class B {
	public:
	int f(int);
	};
	class D : public B {
	public:
	int f(char*);
	};
	void h(D* pd)
	{
	pd->f(1); //error:
	// D::f(char*) hides B::f(int)
	pd->B::f(1); //OK
	pd->f("Ben"); //OK, calls D::f
	}
	 --End Example]
	 */
	public void test13_2s1b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D : public B {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("int f(char*);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void h(D* pd)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("pd->f(1); //error:\n"); //$NON-NLS-1$
		buffer.append("// D::f(char*) hides B::f(int)\n"); //$NON-NLS-1$
		buffer.append("pd->B::f(1); //OK\n"); //$NON-NLS-1$
		buffer.append("pd->f(\"Ben\"); //OK, calls D::f\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 13.2-2):
	int f(char*);
	void g()
	{
	extern f(int);
	f("asdf"); //error: f(int) hides f(char*)
	// so there is no f(char*) in this scope
	}
	void caller ()
	{
	extern void callee(int, int);
	{
	extern void callee(int); // hides callee(int, int)
	callee(88, 99); // error: only callee(int) in scope
	}
	}
	 --End Example]
	 */
	public void test13_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(char*);\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("extern f(int);\n"); //$NON-NLS-1$
		buffer.append("f(\"asdf\"); //error: f(int) hides f(char*)\n"); //$NON-NLS-1$
		buffer.append("// so there is no f(char*) in this scope\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void caller ()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("extern void callee(int, int);\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("extern void callee(int); // hides callee(int, int)\n"); //$NON-NLS-1$
		buffer.append("callee(88, 99); // error: only callee(int) in scope\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 13.2-3):
	class buffer {
	private:
	char* p;
	int size;
	protected:
	buffer(int s, char* store) { size = s; p = store; }
	// ...
	public:
	buffer(int s) { p = new char[size = s]; }
	// ...
	};
	 --End Example]
	 */
	public void test13_2s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class buffer {\n"); //$NON-NLS-1$
		buffer.append("private:\n"); //$NON-NLS-1$
		buffer.append("char* p;\n"); //$NON-NLS-1$
		buffer.append("int size;\n"); //$NON-NLS-1$
		buffer.append("protected:\n"); //$NON-NLS-1$
		buffer.append("buffer(int s, char* store) { size = s; p = store; }\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("buffer(int s) { p = new char[size = s]; }\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.1-6):
	class T {
	public:
	T();
	// ...
	};
	class C : T {
	public:
	C(int);
	// ...
	};
	T a = 1; // illformed: T(C(1)) not tried
	 --End Example]
	 */
	public void test13_3_1s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class T {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("T();\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class C : T {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("C(int);\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("T a = 1; // illformed: T(C(1)) not tried\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.1.1.2-4):
	int f1(int);
	int f2(float);
	typedef int (*fp1)(int);
	typedef int (*fp2)(float);
	struct A {
	operator fp1() { return f1; }
	operator fp2() { return f2; }
	} a;
	int i = a(1); // Calls f1 via pointer returned from
	// conversion function
	 --End Example]
	 */
	public void test13_3_1_1_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f1(int);\n"); //$NON-NLS-1$
		buffer.append("int f2(float);\n"); //$NON-NLS-1$
		buffer.append("typedef int (*fp1)(int);\n"); //$NON-NLS-1$
		buffer.append("typedef int (*fp2)(float);\n"); //$NON-NLS-1$
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("operator fp1() { return f1; }\n"); //$NON-NLS-1$
		buffer.append("operator fp2() { return f2; }\n"); //$NON-NLS-1$
		buffer.append("} a;\n"); //$NON-NLS-1$
		buffer.append("int i = a(1); // Calls f1 via pointer returned from\n"); //$NON-NLS-1$
		buffer.append("// conversion function\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.1.2-1):
	class String {
	public:
	String (const String&);
	String (char*);
	operator char* ();
	};
	String operator + (const String&, const String&);
	void f(void)
	{
	char* p= "one" + "two"; // illformed because neither
	// operand has user defined type
	int I = 1 + 1; // Always evaluates to 2 even if
	// user defined types exist which
	// would perform the operation.
	}
	 --End Example]
	 */
	public void test13_3_1_2s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class String {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("String (const String&);\n"); //$NON-NLS-1$
		buffer.append("String (char*);\n"); //$NON-NLS-1$
		buffer.append("operator char* ();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("String operator + (const String&, const String&);\n"); //$NON-NLS-1$
		buffer.append("void f(void)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("char* p= \"one\" + \"two\"; // illformed because neither\n"); //$NON-NLS-1$
		buffer.append("// operand has user defined type\n"); //$NON-NLS-1$
		buffer.append("int I = 1 + 1; // Always evaluates to 2 even if\n"); //$NON-NLS-1$
		buffer.append("// user defined types exist which\n"); //$NON-NLS-1$
		buffer.append("// would perform the operation.\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.1.2-6):
	struct A {
	operator int();
	};
	A operator+(const A&, const A&);
	void m() {
	A a, b;
	a + b; // operator+(a,b) chosen over int(a) + int(b)
	}
	 --End Example]
	 */
	public void test13_3_1_2s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("operator int();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("A operator+(const A&, const A&);\n"); //$NON-NLS-1$
		buffer.append("void m() {\n"); //$NON-NLS-1$
		buffer.append("A a, b;\n"); //$NON-NLS-1$
		buffer.append("a + b; // operator+(a,b) chosen over int(a) + int(b)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.3-1):
	struct A {
	A();
	operator int();
	operator double();
	} a;
	int i = a; // a.operator int() followed by no conversion
	// is better than a.operator double() followed by
	// a conversion to int
	float x = a; // ambiguous: both possibilities require conversions,
	// and neither is better than the other
	 --End Example]
	 */
	public void test13_3_3s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("A();\n"); //$NON-NLS-1$
		buffer.append("operator int();\n"); //$NON-NLS-1$
		buffer.append("operator double();\n"); //$NON-NLS-1$
		buffer.append("} a;\n"); //$NON-NLS-1$
		buffer.append("int i = a; // a.operator int() followed by no conversion\n"); //$NON-NLS-1$
		buffer.append("// is better than a.operator double() followed by\n"); //$NON-NLS-1$
		buffer.append("// a conversion to int\n"); //$NON-NLS-1$
		buffer.append("float x = a; // ambiguous: both possibilities require conversions,\n"); //$NON-NLS-1$
		buffer.append("// and neither is better than the other\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.3-3):
	void Fcn(const int*, short);
	void Fcn(int*, int);
	int i;
	short s = 0;
	void f() {
	Fcn(&i, s); // is ambiguous because
	// &i ® int* is better than &i ® const int*
	// but s ® short is also better than s ® int
	Fcn(&i, 1L); // calls Fcn(int*, int), because
	// &i ® int* is better than &i ® const int*
	// and 1L ® short and 1L ® int are indistinguishable
	Fcn(&i,’c’); //callsFcn(int*, int), because
	// &i ® int* is better than &i ® const int*
	// and c ® int is better than c ® short
	}
	 --End Example]
	 */
	public void test13_3_3s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void Fcn(const int*, short);\n"); //$NON-NLS-1$
		buffer.append("void Fcn(int*, int);\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("short s = 0;\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("Fcn(&i, s); // is ambiguous because\n"); //$NON-NLS-1$
		buffer.append("// &i ® int* is better than &i ® const int*\n"); //$NON-NLS-1$
		buffer.append("// but s ® short is also better than s ® int\n"); //$NON-NLS-1$
		buffer.append("Fcn(&i, 1L); // calls Fcn(int*, int), because\n"); //$NON-NLS-1$
		buffer.append("// &i ® int* is better than &i ® const int*\n"); //$NON-NLS-1$
		buffer.append("// and 1L ® short and 1L ® int are indistinguishable\n"); //$NON-NLS-1$
		buffer.append("Fcn(&i,'c'); //callsFcn(int*, int), because\n"); //$NON-NLS-1$
		buffer.append("// &i ® int* is better than &i ® const int*\n"); //$NON-NLS-1$
		buffer.append("// and c ® int is better than c ® short\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.3.1.1-2):
	class B;
	class A { A (B&); };
	class B { operator A (); };
	class C { C (B&); };
	void f(A) { }
	void f(C) { }
	B b;
	f(b); //ambiguous because b ­> C via constructor and
	// b -> A via constructor or conversion function.
	 --End Example]
	 */
	public void test13_3_3_1_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B;\n"); //$NON-NLS-1$
		buffer.append("class A { A (B&); };\n"); //$NON-NLS-1$
		buffer.append("class B { operator A (); };\n"); //$NON-NLS-1$
		buffer.append("class C { C (B&); };\n"); //$NON-NLS-1$
		buffer.append("void f(A) { }\n"); //$NON-NLS-1$
		buffer.append("void f(C) { }\n"); //$NON-NLS-1$
		buffer.append("B b;\n"); //$NON-NLS-1$
		buffer.append("f(b); //ambiguous because b ­> C via constructor and\n"); //$NON-NLS-1$
		buffer.append("// b -> A via constructor or conversion function.\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.3.1.4-1):
	struct A {};
	struct B : public A {} b;
	int f(A&);
	int f(B&);
	int i = f(b); // Calls f(B&), an exact match, rather than
	// f(A&), a conversion
	 --End Example]
	 */
	public void test13_3_3_1_4s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {};\n"); //$NON-NLS-1$
		buffer.append("struct B : public A {} b;\n"); //$NON-NLS-1$
		buffer.append("int f(A&);\n"); //$NON-NLS-1$
		buffer.append("int f(B&);\n"); //$NON-NLS-1$
		buffer.append("int i = f(b); // Calls f(B&), an exact match, rather than\n"); //$NON-NLS-1$
		buffer.append("// f(A&), a conversion\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.3.2-3a):
	int f(const int *);
	int f(int *);
	int i;
	int j = f(&i); // Calls f(int *)
	 --End Example]
	 */
	public void test13_3_3_2s3a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(const int *);\n"); //$NON-NLS-1$
		buffer.append("int f(int *);\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("int j = f(&i); // Calls f(int *)\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.3.2-3b):
	int f(const int &);
	int f(int &);
	int g(const int &);
	int g(int);
	int i;
	int j = f(i); // Calls f(int &)
	int k = g(i); // ambiguous
	class X {
	public:
	void f() const;
	void f();
	};
	void g(const X& a, X b)
	{
	a.f(); //CallsX::f() const
	b.f(); //Calls X::f()
	}
	 --End Example]
	 */
	public void test13_3_3_2s3b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(const int &);\n"); //$NON-NLS-1$
		buffer.append("int f(int &);\n"); //$NON-NLS-1$
		buffer.append("int g(const int &);\n"); //$NON-NLS-1$
		buffer.append("int g(int);\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("int j = f(i); // Calls f(int &)\n"); //$NON-NLS-1$
		buffer.append("int k = g(i); // ambiguous\n"); //$NON-NLS-1$
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("void f() const;\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void g(const X& a, X b)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("a.f(); //CallsX::f() const\n"); //$NON-NLS-1$
		buffer.append("b.f(); //Calls X::f()\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test13_3_3_2s3c() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("operator short();\n"); //$NON-NLS-1$
		buffer.append("} a;\n"); //$NON-NLS-1$
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("int f(float);\n"); //$NON-NLS-1$
		buffer.append("int i = f(a); // Calls f(int), because short -> int is\n"); //$NON-NLS-1$
		buffer.append("// better than short -> float.\n"); //$NON-NLS-1$
		
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.3.2-4):
	struct A {};
	struct B : public A {};
	struct C : public B {};
	C *pc;
	int f(A *);
	int f(B *);
	int i = f(pc); // Calls f(B *)
	 --End Example]
	 */
	public void test13_3_3_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {};\n"); //$NON-NLS-1$
		buffer.append("struct B : public A {};\n"); //$NON-NLS-1$
		buffer.append("struct C : public B {};\n"); //$NON-NLS-1$
		buffer.append("C *pc;\n"); //$NON-NLS-1$
		buffer.append("int f(A *);\n"); //$NON-NLS-1$
		buffer.append("int f(B *);\n"); //$NON-NLS-1$
		buffer.append("int i = f(pc); // Calls f(B *)\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.4-5b):
	struct X {
	int f(int);
	static int f(long);
	};
	int (X::*p1)(int) = &X::f; // OK
	int (*p2)(int) = &X::f; // error: mismatch
	int (*p3)(long) = &X::f; // OK
	int (X::*p4)(long) = &X::f; // error: mismatch
	int (*p6)(long) = &(X::f); // OK
	 --End Example]
	 */
	public void test13_4s5b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("static int f(long);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int (X::*p1)(int) = &X::f; // OK\n"); //$NON-NLS-1$
		buffer.append("int (*p2)(int) = &X::f; // error: mismatch\n"); //$NON-NLS-1$
		buffer.append("int (*p3)(long) = &X::f; // OK\n"); //$NON-NLS-1$
		buffer.append("int (X::*p4)(long) = &X::f; // error: mismatch\n"); //$NON-NLS-1$
		buffer.append("int (*p6)(long) = &(X::f); // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 13.5-4):
	class complex {
	public:
	complex operator+(complex a) {}
	};
	int n;
	int foo(complex &a, complex &b) {
	complex z = a.operator+(b); // complex z = a+b;
	void* p = operator new(sizeof(int)*n);
	}
	 --End Example]
	 */
	public void test13_5s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class complex {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("complex operator+(complex a) {}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int n;\n"); //$NON-NLS-1$
		buffer.append("int foo(complex &a, complex &b) {\n"); //$NON-NLS-1$
		buffer.append("complex z = a.operator+(b); // complex z = a+b;\n"); //$NON-NLS-1$
		buffer.append("void* p = operator new(sizeof(int)*n);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 13.5.3-2):
	struct B {
	virtual int operator= (int);
	virtual B& operator= (const B&);
	};
	struct D : B {
	virtual int operator= (int);
	virtual D& operator= (const B&);
	};
	D dobj1;
	D dobj2;
	B* bptr = &dobj1;
	void f() {
	bptr->operator=(99); // calls D::operator=(int)
	*bptr = 99; // ditto
	bptr->operator=(dobj2); // calls D::operator=(const B&)
	*bptr = dobj2; // ditto
	dobj1 = dobj2; // calls implicitlydeclared
	// D::operator=(const D&)
	}
	 --End Example]
	 */
	public void test13_5_3s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("virtual int operator= (int);\n"); //$NON-NLS-1$
		buffer.append("virtual B& operator= (const B&);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D : B {\n"); //$NON-NLS-1$
		buffer.append("virtual int operator= (int);\n"); //$NON-NLS-1$
		buffer.append("virtual D& operator= (const B&);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("D dobj1;\n"); //$NON-NLS-1$
		buffer.append("D dobj2;\n"); //$NON-NLS-1$
		buffer.append("B* bptr = &dobj1;\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("bptr->operator=(99); // calls D::operator=(int)\n"); //$NON-NLS-1$
		buffer.append("*bptr = 99; // ditto\n"); //$NON-NLS-1$
		buffer.append("bptr->operator=(dobj2); // calls D::operator=(const B&)\n"); //$NON-NLS-1$
		buffer.append("*bptr = dobj2; // ditto\n"); //$NON-NLS-1$
		buffer.append("dobj1 = dobj2; // calls implicitlydeclared\n"); //$NON-NLS-1$
		buffer.append("// D::operator=(const D&)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 13.5.7-1):
	class X {
	public:
	X& operator++(); // prefix ++a
	X operator++(int); // postfix a++
	};
	class Y { };
	Y& operator++(Y&); // prefix ++b
	Y operator++(Y&, int); // postfix b++
	void f(X a, Y b) {
	++a; // a.operator++();
	a++; // a.operator++(0);
	++b; // operator++(b);
	b++; // operator++(b, 0);
	a.operator++(); // explicit call: like ++a;
	a.operator++(0); // explicit call: like a++;
	operator++(b); //explicit call: like ++b;
	operator++(b, 0); // explicit call: like b++;
	}
	 --End Example]
	 */
	public void test13_5_7s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("X& operator++(); // prefix ++a\n"); //$NON-NLS-1$
		buffer.append("X operator++(int); // postfix a++\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Y { };\n"); //$NON-NLS-1$
		buffer.append("Y& operator++(Y&); // prefix ++b\n"); //$NON-NLS-1$
		buffer.append("Y operator++(Y&, int); // postfix b++\n"); //$NON-NLS-1$
		buffer.append("void f(X a, Y b) {\n"); //$NON-NLS-1$
		buffer.append("++a; // a.operator++();\n"); //$NON-NLS-1$
		buffer.append("a++; // a.operator++(0);\n"); //$NON-NLS-1$
		buffer.append("++b; // operator++(b);\n"); //$NON-NLS-1$
		buffer.append("b++; // operator++(b, 0);\n"); //$NON-NLS-1$
		buffer.append("a.operator++(); // explicit call: like ++a;\n"); //$NON-NLS-1$
		buffer.append("a.operator++(0); // explicit call: like a++;\n"); //$NON-NLS-1$
		buffer.append("operator++(b); //explicit call: like ++b;\n"); //$NON-NLS-1$
		buffer.append("operator++(b, 0); // explicit call: like b++;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.1-6):
	template<const X& x, int i> void f()
	{
	i++; //error: change of templateparameter
	value
	&x; //OK
	&i; //error: address of nonreference templateparameter
	int& ri = i; // error: nonconst reference bound to temporary
	const int& cri = i; // OK: const reference bound to temporary
	}
	 --End Example]
	 */
	public void test14_1s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<const X& x, int i> void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("i++; //error: change of template parameter\n"); //$NON-NLS-1$
		buffer.append("value\n"); //$NON-NLS-1$
		buffer.append("&x; //OK\n"); //$NON-NLS-1$
		buffer.append("&i; //error: address of nonreference template parameter\n"); //$NON-NLS-1$
		buffer.append("int& ri = i; // error: nonconst reference bound to temporary\n"); //$NON-NLS-1$
		buffer.append("const int& cri = i; // OK: const reference bound to temporary\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.1-7):
	template<double d> class X; // error
	template<double* pd> class Y; // OK
	template<double& rd> class Z; // OK
	 --End Example]
	 */
	public void test14_1s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<double d> class X; // error\n"); //$NON-NLS-1$
		buffer.append("template<double* pd> class Y; // OK\n"); //$NON-NLS-1$
		buffer.append("template<double& rd> class Z; // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_1s8() throws Exception { // TODO raised bug 90668
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<int *a> struct R {  };\n"); //$NON-NLS-1$
		buffer.append("template<int b[5]> struct S {  };\n"); //$NON-NLS-1$
		buffer.append("int *p;\n"); //$NON-NLS-1$
		buffer.append("R<p> w; // OK\n"); //$NON-NLS-1$
		buffer.append("S<p> x; // OK due to parameter adjustment\n"); //$NON-NLS-1$
		buffer.append("int v[5];\n"); //$NON-NLS-1$
		buffer.append("R<v> y; // OK due to implicit argument conversion\n"); //$NON-NLS-1$
		buffer.append("S<v> z; // OK due to both adjustment and conversion\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.1-10a):
	template<class T1, class T2 = int> class A;
	template<class T1 = int, class T2> class A;
	 --End Example]
	 */
	public void test14_1s10a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T1, class T2 = int> class A;\n"); //$NON-NLS-1$
		buffer.append("template<class T1 = int, class T2> class A;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.1-10b):
	template<class T1 = int, class T2 = int> class A;
	 --End Example]
	 */
	public void test14_1s10b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T1 = int, class T2 = int> class A;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.1-11):
	template<class T1 = int, class T2> class B; // error
	 --End Example]
	 */
	public void test14_1s11() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T1 = int, class T2> class B; // error\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.1-12):
	template<class T = int> class X;
	template<class T = int> class X {  }; // error
	 --End Example]
	 */
	public void test14_1s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T = int> class X;\n"); //$NON-NLS-1$
		buffer.append("template<class T = int> class X {  }; // error\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.1-13):
	template<class T, T* p, class U = T> class X {  };
	template<class T> void f(T* p = new T);
	 --End Example]
	 */
	public void test14_1s13() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T, T* p, class U = T> class X {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T* p = new T);\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.1-15):
	template<int i = (3 > 4) > // OK
	class Y {  };
	 --End Example]
	 */
	public void test14_1s15() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<int i = (3 > 4) > // OK\n"); //$NON-NLS-1$
		buffer.append("class Y {  };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.2-3):
	template<int i> class X {  };
	X<(1>2)> x2; // OK
	template<class T> class Y {  };
	Y< X<1> > x3; // OK
	Y<X<6>> 1> > x4; // OK: Y< X< (6>>1) > >
	 --End Example]
	 */
	public void test14_2s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<int i> class X {  };\n"); //$NON-NLS-1$
		buffer.append("X<(1>2)> x2; // OK\n"); //$NON-NLS-1$
		buffer.append("template<class T> class Y {  };\n"); //$NON-NLS-1$
		buffer.append("Y< X<1> > x3; // OK\n"); //$NON-NLS-1$
		buffer.append("Y<X<6>> 1> > x4; // OK: Y< X< (6>>1) > >\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.2-4):
	class X {
	public:
	template<size_t> X* alloc();
	template<size_t> static X* adjust();
	};
	template<class T> void f(T* p)
	{
	T* p1 = p>
	alloc<200>(); // illformed: < means less than
	T* p2 = p->template alloc<200>();
	// OK: < starts template argument list
	T::adjust<100>();
	// illformed: < means less than
	T::template adjust<100>();
	// OK: < starts explicit qualification
	}
	 --End Example]
	 */
	public void test14_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("template<size_t> X* alloc();\n"); //$NON-NLS-1$
		buffer.append("template<size_t> static X* adjust();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T* p)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("T* p1 = p>\n"); //$NON-NLS-1$
		buffer.append("alloc<200>(); // illformed: < means less than\n"); //$NON-NLS-1$
		buffer.append("T* p2 = p->template alloc<200>();\n"); //$NON-NLS-1$
		buffer.append("// OK: < starts template argument list\n"); //$NON-NLS-1$
		buffer.append("T::adjust<100>();\n"); //$NON-NLS-1$
		buffer.append("// illformed: < means less than\n"); //$NON-NLS-1$
		buffer.append("T::template adjust<100>();\n"); //$NON-NLS-1$
		buffer.append("// OK: < starts explicit qualification\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.3-1):
	template<class T> class Array {
	T* v;
	int sz;
	public:
	explicit Array(int);
	T& operator[](int);
	T& elem(int i) { return v[i]; }
	// ...
	};
	Array<int> v1(20);
	typedef complex<double> dcomplex; // complex is a standard
	// library template
	Array<dcomplex> v2(30);
	Array<dcomplex> v3(40);
	void bar() {
	v1[3] = 7;
	v2[3] = v3.elem(4) = dcomplex(7,8);
	}
	 --End Example]
	 */
	public void test14_3s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Array {\n"); //$NON-NLS-1$
		buffer.append("T* v;\n"); //$NON-NLS-1$
		buffer.append("int sz;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("explicit Array(int);\n"); //$NON-NLS-1$
		buffer.append("T& operator[](int);\n"); //$NON-NLS-1$
		buffer.append("T& elem(int i) { return v[i]; }\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("Array<int> v1(20);\n"); //$NON-NLS-1$
		buffer.append("typedef complex<double> dcomplex; // complex is a standard\n"); //$NON-NLS-1$
		buffer.append("// library template\n"); //$NON-NLS-1$
		buffer.append("Array<dcomplex> v2(30);\n"); //$NON-NLS-1$
		buffer.append("Array<dcomplex> v3(40);\n"); //$NON-NLS-1$
		buffer.append("void bar() {\n"); //$NON-NLS-1$
		buffer.append("v1[3] = 7;\n"); //$NON-NLS-1$
		buffer.append("v2[3] = v3.elem(4) = dcomplex(7,8);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.3-3):
	template<class T> class X {
	static T t;
	};
	class Y {
	private:
	struct S {  };
	X<S> x; // OK: S is accessible
	// X<Y::S> has a static member of type Y::S
	// OK: even though Y::S is private
	};
	X<Y::S> y; // error: S not accessible
	 --End Example]
	 */
	public void test14_3s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X {\n"); //$NON-NLS-1$
		buffer.append("static T t;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Y {\n"); //$NON-NLS-1$
		buffer.append("private:\n"); //$NON-NLS-1$
		buffer.append("struct S {  };\n"); //$NON-NLS-1$
		buffer.append("X<S> x; // OK: S is accessible\n"); //$NON-NLS-1$
		buffer.append("// X<Y::S> has a static member of type Y::S\n"); //$NON-NLS-1$
		buffer.append("// OK: even though Y::S is private\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("X<Y::S> y; // error: S not accessible\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_3s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("~A();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f(A<int>* p, A<int>* q) {\n"); //$NON-NLS-1$
		buffer.append("p->A<int>::~A(); // OK: destructor call\n"); //$NON-NLS-1$
		buffer.append("q->A<int>::~A<int>(); // OK: destructor call\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
	
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.3.1-2):
	template <class T> class X {  };
	void f()
	{
	struct S {  };
	X<S> x3; // error: local type used as templateargument
	X<S*> x4; // error: pointer to local type used as templateargument
	}
	 --End Example]
	 */
	public void test14_3_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> class X {  };\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("struct S {  };\n"); //$NON-NLS-1$
		buffer.append("X<S> x3; // error: local type used as templateargument\n"); //$NON-NLS-1$
		buffer.append("X<S*> x4; // error: pointer to local type used as templateargument\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.3.1-3):
	template<class T> struct A {
	static T t;
	};
	typedef int function();
	A<function> a; // illformed: would declare A<function>::t
	// as a static member function
	 --End Example]
	 */
	public void test14_3_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("static T t;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("typedef int function();\n"); //$NON-NLS-1$
		buffer.append("A<function> a; // illformed: would declare A<function>::t\n"); //$NON-NLS-1$
		buffer.append("// as a static member function\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.3.3-2):
	template<class T> class A { // primary template
	int x;
	};
	template<class T> class A<T*> { // partial specialization
	long x;
	};
	template<template<class U> class V> class C {
	V<int> y;
	V<int*> z;
	};
	C<A> c; // V<int> within C<A> uses the primary template,
	// so c.y.x has type int
	// V<int*> within C<A> uses the partial specialization,
	// so c.z.x has type long
	 --End Example]
	 */
	public void test14_3_3s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class A { // primary template\n"); //$NON-NLS-1$
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> class A<T*> { // partial specialization\n"); //$NON-NLS-1$
		buffer.append("long x;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<template<class U> class V> class C {\n"); //$NON-NLS-1$
		buffer.append("V<int> y;\n"); //$NON-NLS-1$
		buffer.append("V<int*> z;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("C<A> c; // V<int> within C<A> uses the primary template,\n"); //$NON-NLS-1$
		buffer.append("// so c.y.x has type int\n"); //$NON-NLS-1$
		buffer.append("// V<int*> within C<A> uses the partial specialization,\n"); //$NON-NLS-1$
		buffer.append("// so c.z.x has type long\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.4-1a):
	template<class E, int size> class buffer {  };
	buffer<char,2*512> x;
	buffer<char,1024> y;
	 --End Example]
	 */
	public void test14_2s1a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class E, int size> class buffer {  };\n"); //$NON-NLS-1$
		buffer.append("buffer<char,2*512> x;\n"); //$NON-NLS-1$
		buffer.append("buffer<char,1024> y;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.4-1b):
	template<class T, void(*err_fct)()> class list {  };
	list<int,&error_handler1> x1;
	list<int,&error_handler2> x2;
	list<int,&error_handler2> x3;
	list<char,&error_handler2> x4;
	 --End Example]
	 */
	public void test14_4s1b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T, void(*err_fct)()> class list {  };\n"); //$NON-NLS-1$
		buffer.append("list<int,&error_handler1> x1;\n"); //$NON-NLS-1$
		buffer.append("list<int,&error_handler2> x2;\n"); //$NON-NLS-1$
		buffer.append("list<int,&error_handler2> x3;\n"); //$NON-NLS-1$
		buffer.append("list<char,&error_handler2> x4;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.1-2):
	template<class T> class Array {
	T* v;
	int sz;
	public:
	explicit Array(int);
	T& operator[](int);
	T& elem(int i) { return v[i]; }
	// ...
	};
	 --End Example]
	 */
	public void test14_5_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Array {\n"); //$NON-NLS-1$
		buffer.append("T* v;\n"); //$NON-NLS-1$
		buffer.append("int sz;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("explicit Array(int);\n"); //$NON-NLS-1$
		buffer.append("T& operator[](int);\n"); //$NON-NLS-1$
		buffer.append("T& elem(int i) { return v[i]; }\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.1-3a):
	template<class T1, class T2> struct A {
	void f1();
	void f2();
	};
	template<class T2, class T1> void A<T2,T1>::f1() { } // OK
	 --End Example]
	 */
	public void test14_5_1s3a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T1, class T2> struct A {\n"); //$NON-NLS-1$
		buffer.append("void f1();\n"); //$NON-NLS-1$
		buffer.append("void f2();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T2, class T1> void A<T2,T1>::f1() { } // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.1.1-1):
	template<class T> class Array {
	T* v;
	int sz;
	public:
	explicit Array(int);
	T& operator[](int);
	T& elem(int i) { return v[i]; }
	// ...
	};
	template<class T> T& Array<T>::operator[](int i)
	{
	if (i<0 || sz<=i) error("Array: range error");
	return v[i];
	}
	 --End Example]
	 */
	public void test14_5_1_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Array {\n"); //$NON-NLS-1$
		buffer.append("T* v;\n"); //$NON-NLS-1$
		buffer.append("int sz;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("explicit Array(int);\n"); //$NON-NLS-1$
		buffer.append("T& operator[](int);\n"); //$NON-NLS-1$
		buffer.append("T& elem(int i) { return v[i]; }\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> T& Array<T>::operator[](int i)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("if (i<0 || sz<=i) error(\"Array: range error\");\n"); //$NON-NLS-1$
		buffer.append("return v[i];\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.1.1-2):
	Array<int> v1(20);
	Array<dcomplex> v2(30);
	v1[3] = 7; // Array<int>::operator[]()
	v2[3] = dcomplex(7,8); // Array<dcomplex>::operator[]()
	 --End Example]
	 */
	public void test14_5_1_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Array<int> v1(20);\n"); //$NON-NLS-1$
		buffer.append("Array<dcomplex> v2(30);\n"); //$NON-NLS-1$
		buffer.append("v1[3] = 7; // Array<int>::operator[]()\n"); //$NON-NLS-1$
		buffer.append("v2[3] = dcomplex(7,8); // Array<dcomplex>::operator[]()\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.1.2-1):
	template<class T> struct A {
	class B;
	};
	A<int>::B* b1; // OK: requires A to be defined but not A::B
	template<class T> class A<T>::B { };
	A<int>::B b2; // OK: requires A::B to be defined
	 --End Example]
	 */
	public void test14_5_1_2s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("class B;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("A<int>::B* b1; // OK: requires A to be defined but not A::B\n"); //$NON-NLS-1$
		buffer.append("template<class T> class A<T>::B { };\n"); //$NON-NLS-1$
		buffer.append("A<int>::B b2; // OK: requires A::B to be defined\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.2-1):
	template<class T> class string {
	public:
	template<class T2> int compare(const T2&);
	template<class T2> string(const string<T2>& s) {  }
	// ...
	};
	template<class T> template<class T2> int string<T>::compare(const T2& s)
	{
	// ...
	}
	 --End Example]
	 */
	public void test14_5_2s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class string {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("template<class T2> int compare(const T2&);\n"); //$NON-NLS-1$
		buffer.append("template<class T2> string(const string<T2>& s) {  }\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> template<class T2> int string<T>::compare(const T2& s)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	

	
	/**
	 [--Start Example(CPP 14.5.2-4):
	class B {
	virtual void f(int);
	};
	class D : public B {
	template <class T> void f(T); // does not override B::f(int)
	void f(int i) { f<>(i); } // overriding function that calls
	// the template instantiation
	};
	 --End Example]
	 */
	public void test14_5_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class B {\n"); //$NON-NLS-1$
		buffer.append("virtual void f(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class D : public B {\n"); //$NON-NLS-1$
		buffer.append("template <class T> void f(T); // does not override B::f(int)\n"); //$NON-NLS-1$
		buffer.append("void f(int i) { f<>(i); } // overriding function that calls\n"); //$NON-NLS-1$
		buffer.append("// the template instantiation\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.2-5):
	struct A {
	template <class T> operator T*();
	};
	template <class T> A::operator T*(){ return 0; }
	template <> A::operator char*(){ return 0; } // specialization
	template A::operator void*(); // explicit instantiation
	int main()
	{
	A a;
	int* ip;
	ip = a.operator int*(); // explicit call to template operator
	// A::operator int*()
	}
	 --End Example]
	 */
	public void test14_5_2s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("template <class T> operator T*();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template <class T> A::operator T*(){ return 0; }\n"); //$NON-NLS-1$
		buffer.append("template <> A::operator char*(){ return 0; } // specialization\n"); //$NON-NLS-1$
		buffer.append("template A::operator void*(); // explicit instantiation\n"); //$NON-NLS-1$
		buffer.append("int main()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("A a;\n"); //$NON-NLS-1$
		buffer.append("int* ip;\n"); //$NON-NLS-1$
		buffer.append("ip = a.operator int*(); // explicit call to template operator\n"); //$NON-NLS-1$
		buffer.append("// A::operator int*()\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.1.3-1):
	template<class T> class X {
	static T s;
	};
	template<class T> T X<T>::s = 0;
	 --End Example]
	 */
	public void test14_5_1_3s1() throws Exception { 
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X {\n"); //$NON-NLS-1$
		buffer.append("static T s;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> T X<T>::s = 0;\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.3-2):
	namespace N {
	template <class T> void f(T);
	void g(int);
	namespace M {
	template <class T> void h(T);
	template <class T> void i(T);
	struct A {
	friend void f<>(int); // illformed- N::f
	friend void h<>(int); // OK - M::h
	friend void g(int); // OK - new decl of M::g
	template <class T> void i(T);
	friend void i<>(int); // illformed- A::i
	};
	}
	}
	 --End Example]
	 */
	public void test14_5_3s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("template <class T> void f(T);\n"); //$NON-NLS-1$
		buffer.append("void g(int);\n"); //$NON-NLS-1$
		buffer.append("namespace M {\n"); //$NON-NLS-1$
		buffer.append("template <class T> void h(T);\n"); //$NON-NLS-1$
		buffer.append("template <class T> void i(T);\n"); //$NON-NLS-1$
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("friend void f<>(int); // illformed- N::f\n"); //$NON-NLS-1$
		buffer.append("friend void h<>(int); // OK - M::h\n"); //$NON-NLS-1$
		buffer.append("friend void g(int); // OK - new decl of M::g\n"); //$NON-NLS-1$
		buffer.append("template <class T> void i(T);\n"); //$NON-NLS-1$
		buffer.append("friend void i<>(int); // illformed- A::i\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.3-3):
	class A {
	template<class T> friend class B; // OK
	template<class T> friend void f(T){  } // OK
	};
	 --End Example]
	 */
	public void test14_5_3s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {\n"); //$NON-NLS-1$
		buffer.append("template<class T> friend class B; // OK\n"); //$NON-NLS-1$
		buffer.append("template<class T> friend void f(T){  } // OK\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.3-4):
	class X {
	template<class T> friend struct A;
	class Y { };
	};
	template<class T> struct A { X::Y ab; }; // OK
	template<class T> struct A<T*> { X::Y ab; }; // OK
	 --End Example]
	 */
	public void test14_5_3s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("template<class T> friend struct A;\n"); //$NON-NLS-1$
		buffer.append("class Y { };\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> struct A { X::Y ab; }; // OK\n"); //$NON-NLS-1$
		buffer.append("template<class T> struct A<T*> { X::Y ab; }; // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_5_3s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("struct B { };\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("template<class T> friend struct A<T>::B;\n"); //$NON-NLS-1$
		buffer.append("template<class T> friend void A<T>::f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_5_4_3s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("template<class T2> struct B {}; // #1\n"); //$NON-NLS-1$
		buffer.append("template<class T2> struct B<T2*> {}; // #2\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<> template<class T2> struct A<short>::B {}; // #3\n"); //$NON-NLS-1$
		buffer.append("A<char>::B<int*> abcip; // uses #2\n"); //$NON-NLS-1$
		buffer.append("A<short>::B<int*> absip; // uses #3\n"); //$NON-NLS-1$
		buffer.append("A<char>::B<int> abci; // uses #1\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.4-4):
	template<class T1, class T2, int I> class A { }; // #1
	template<class T, int I> class A<T, T*, I> { }; // #2
	template<class T1, class T2, int I> class A<T1*, T2, I> { }; // #3
	template<class T> class A<int, T*, 5> { }; // #4
	template<class T1, class T2, int I> class A<T1, T2*, I> { }; // #5
	 --End Example]
	 */
	public void test14_5_4s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T1, class T2, int I> class A { }; // #1\n"); //$NON-NLS-1$
		buffer.append("template<class T, int I> class A<T, T*, I> { }; // #2\n"); //$NON-NLS-1$
		buffer.append("template<class T1, class T2, int I> class A<T1*, T2, I> { }; // #3\n"); //$NON-NLS-1$
		buffer.append("template<class T> class A<int, T*, 5> { }; // #4\n"); //$NON-NLS-1$
		buffer.append("template<class T1, class T2, int I> class A<T1, T2*, I> { }; // #5\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.4-9b):
	template <int I, int J> struct B {};
	template <int I> struct B<I, I> {}; // OK
	 --End Example]
	 */
	public void test14_5_4s9b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <int I, int J> struct B {};\n"); //$NON-NLS-1$
		buffer.append("template <int I> struct B<I, I> {}; // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.4.1-2a):
	template<class T1, class T2, int I> class A { }; // #1
	template<class T, int I> class A<T, T*, I> { }; // #2
	template<class T1, class T2, int I> class A<T1*, T2, I> { }; // #3
	template<class T> class A<int, T*, 5> { }; // #4
	template<class T1, class T2, int I> class A<T1, T2*, I> { }; // #5
	A<int, int, 1> a1; // uses #1
	A<int, int*, 1> a2; // uses #2, T is int, I is 1
	A<int, char*, 5> a3; // uses #4, T is char
	A<int, char*, 1> a4; // uses #5, T1 is int, T2 is char, I is 1
	 --End Example]
	 */
	public void test14_5_4_1s2a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T1, class T2, int I> class A { }; // #1\n"); //$NON-NLS-1$
		buffer.append("template<class T, int I> class A<T, T*, I> { }; // #2\n"); //$NON-NLS-1$
		buffer.append("template<class T1, class T2, int I> class A<T1*, T2, I> { }; // #3\n"); //$NON-NLS-1$
		buffer.append("template<class T> class A<int, T*, 5> { }; // #4\n"); //$NON-NLS-1$
		buffer.append("template<class T1, class T2, int I> class A<T1, T2*, I> { }; // #5\n"); //$NON-NLS-1$
		buffer.append("A<int, int, 1> a1; // uses #1\n"); //$NON-NLS-1$
		buffer.append("A<int, int*, 1> a2; // uses #2, T is int, I is 1\n"); //$NON-NLS-1$
		buffer.append("A<int, char*, 5> a3; // uses #4, T is char\n"); //$NON-NLS-1$
		buffer.append("A<int, char*, 1> a4; // uses #5, T1 is int, T2 is char, I is 1\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.4.1-2b):
	template<class T1, class T2, int I> class A { }; // #1
	template<class T, int I> class A<T, T*, I> { }; // #2
	template<class T1, class T2, int I> class A<T1*, T2, I> { }; // #3
	template<class T> class A<int, T*, 5> { }; // #4
	template<class T1, class T2, int I> class A<T1, T2*, I> { }; // #5
	A<int*, int*, 2> a5; // ambiguous: matches #3 and #5
	--End Example]
	 */
	public void test14_5_4_1s2b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T1, class T2, int I> class A { };             // #1\n"); //$NON-NLS-1$
		buffer.append("template<class T, int I>            class A<T, T*, I> { };   // #2\n"); //$NON-NLS-1$
		buffer.append("template<class T1, class T2, int I> class A<T1*, T2, I> { }; // #3\n"); //$NON-NLS-1$
		buffer.append("template<class T>                   class A<int, T*, 5> { }; // #4\n"); //$NON-NLS-1$
		buffer.append("template<class T1, class T2, int I> class A<T1, T2*, I> { }; // #5\n"); //$NON-NLS-1$
		buffer.append("A<int*, int*, 2> a5; // ambiguous: matches #3 and #5 : expect problem \n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 1);
	}
	
	/**
	 [--Start Example(CPP 14.5.4.3-1):
	// primary template
	template<class T, int I> struct A {
	void f();
	};
	template<class T, int I> void A<T,I>::f() { }
	// class template partial specialization
	template<class T> struct A<T,2> {
	void f();
	void g();
	void h();
	};
	// member of class template partial specialization
	template<class T> void A<T,2>::g() { }
	// explicit specialization
	template<> void A<char,2>::h() { }
	int main()
	{
	A<char,0> a0;
	A<char,2> a2;
	a0.f(); //OK, uses definition of primary template’s member
	a2.g(); //OK, uses definition of
	// partial specialization’s member
	a2.h(); //OK, uses definition of
	// explicit specialization’s member
	a2.f(); //illformed, no definition of f for A<T,2>
	// the primary template is not used here
	}
	 --End Example]
	 */
	public void test14_5_4_3s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("// primary template\n"); //$NON-NLS-1$
		buffer.append("template<class T, int I> struct A {\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T, int I> void A<T,I>::f() { }\n"); //$NON-NLS-1$
		buffer.append("// class template partial specialization\n"); //$NON-NLS-1$
		buffer.append("template<class T> struct A<T,2> {\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("void g();\n"); //$NON-NLS-1$
		buffer.append("void h();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("// member of class template partial specialization\n"); //$NON-NLS-1$
		buffer.append("template<class T> void A<T,2>::g() { }\n"); //$NON-NLS-1$
		buffer.append("// explicit specialization\n"); //$NON-NLS-1$
		buffer.append("template<> void A<char,2>::h() { }\n"); //$NON-NLS-1$
		buffer.append("int main()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("A<char,0> a0;\n"); //$NON-NLS-1$
		buffer.append("A<char,2> a2;\n"); //$NON-NLS-1$
		buffer.append("a0.f(); //OK, uses definition of primary template’s member\n"); //$NON-NLS-1$
		buffer.append("a2.g(); //OK, uses definition of\n"); //$NON-NLS-1$
		buffer.append("// partial specialization’s member\n"); //$NON-NLS-1$
		buffer.append("a2.h(); //OK, uses definition of\n"); //$NON-NLS-1$
		buffer.append("// explicit specialization’s member\n"); //$NON-NLS-1$
		buffer.append("a2.f(); //illformed, no definition of f for A<T,2>\n"); //$NON-NLS-1$
		buffer.append("// the primary template is not used here\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.5-1):
	template<class T> class Array { };
	template<class T> void sort(Array<T>&);
	 --End Example]
	 */
	public void test14_5_5s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Array { };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void sort(Array<T>&);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.5.1-1a):
	// file1.c 
	template<class T>
	void f(T*);
	void g(int* p) { 
	f(p); // call 
	// f<int>(int*) 
	}
	}
	 --End Example]
	 */
	public void test14_5_5_1s1a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("// file1.c \n"); //$NON-NLS-1$
		buffer.append("template<class T>\n"); //$NON-NLS-1$
		buffer.append("void f(T*);\n"); //$NON-NLS-1$
		buffer.append("void g(int* p) { \n"); //$NON-NLS-1$
		buffer.append("f(p); // call \n"); //$NON-NLS-1$
		buffer.append("// f<int>(int*) \n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	/**
	 [--Start Example(CPP 14.5.5.1-1b):
	// file2.c
	template<class T>
	void f(T);
	void h(int* p) {
	f(p); // call
	// f<int*>(int*)
	}
	 --End Example]
	 */
	public void test14_5_5_1s1b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("// file2.c\n"); //$NON-NLS-1$
		buffer.append("template<class T>\n"); //$NON-NLS-1$
		buffer.append("void f(T);\n"); //$NON-NLS-1$
		buffer.append("void h(int* p) {\n"); //$NON-NLS-1$
		buffer.append("f(p); // call\n"); //$NON-NLS-1$
		buffer.append("// f<int*>(int*)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	/**
	 [--Start Example(CPP 14.5.5.2-5):
	template<class T> struct A { A(); };
	template<class T> void f(T);
	template<class T> void f(T*);
	template<class T> void f(const T*);
	template<class T> void g(T);
	template<class T> void g(T&);
	template<class T> void h(const T&);
	template<class T> void h(A<T>&);
	void m() {
	const int *p;
	f(p); // f(const T*) is more specialized than f(T) or f(T*)
	float x;
	g(x); //Ambiguous: g(T) or g(T&)
	A<int> z;
	h(z); //overload resolution selects h(A<T>&)
	const A<int> z2;
	h(z2); // h(const T&) is called because h(A<T>&) is not callable
	}
	 --End Example]
	 */
	public void test14_5_5_2s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A { A(); };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T);\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T*);\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(const T*);\n"); //$NON-NLS-1$
		buffer.append("template<class T> void g(T);\n"); //$NON-NLS-1$
		buffer.append("template<class T> void g(T&);\n"); //$NON-NLS-1$
		buffer.append("template<class T> void h(const T&);\n"); //$NON-NLS-1$
		buffer.append("template<class T> void h(A<T>&);\n"); //$NON-NLS-1$
		buffer.append("void m() {\n"); //$NON-NLS-1$
		buffer.append("const int *p;\n"); //$NON-NLS-1$
		buffer.append("f(p); // f(const T*) is more specialized than f(T) or f(T*)\n"); //$NON-NLS-1$
		buffer.append("float x;\n"); //$NON-NLS-1$
		buffer.append("g(x); //Ambiguous: g(T) or g(T&)\n"); //$NON-NLS-1$
		buffer.append("A<int> z;\n"); //$NON-NLS-1$
		buffer.append("h(z); //overload resolution selects h(A<T>&)\n"); //$NON-NLS-1$
		buffer.append("const A<int> z2;\n"); //$NON-NLS-1$
		buffer.append("h(z2); // h(const T&) is called because h(A<T>&) is not callable\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
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
	public void test14_5_5_2s6() throws Exception {
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

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6-2):
	// no B declared here
	class X;
	template<class T> class Y {
	class Z; // forward declaration of member class
	void f() {
	X* a1; // declare pointer to X
	T* a2; // declare pointer to T
	Y* a3; // declare pointer to Y<T>
	Z* a4; // declare pointer to Z
	typedef typename T::A TA;
	TA* a5; // declare pointer to T’s A
	typename T::A* a6; // declare pointer to T’s A
	T::A* a7; // T::A is not a type name:
	// multiply T::A by a7; illformed,
	// no visible declaration of a7
	B* a8; // B is not a type name:
	// multiply B by a8; illformed,
	// no visible declarations of B and a8
	}
	};
	 --End Example]
	 */
	public void test14_6s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("// no B declared here\n"); //$NON-NLS-1$
		buffer.append("class X;\n"); //$NON-NLS-1$
		buffer.append("template<class T> class Y {\n"); //$NON-NLS-1$
		buffer.append("class Z; // forward declaration of member class\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("X* a1; // declare pointer to X\n"); //$NON-NLS-1$
		buffer.append("T* a2; // declare pointer to T\n"); //$NON-NLS-1$
		buffer.append("Y* a3; // declare pointer to Y<T>\n"); //$NON-NLS-1$
		buffer.append("Z* a4; // declare pointer to Z\n"); //$NON-NLS-1$
		buffer.append("typedef typename T::A TA;\n"); //$NON-NLS-1$
		buffer.append("TA* a5; // declare pointer to T’s A\n"); //$NON-NLS-1$
		buffer.append("typename T::A* a6; // declare pointer to T’s A\n"); //$NON-NLS-1$
		buffer.append("T::A* a7; // T::A is not a type name:\n"); //$NON-NLS-1$
		buffer.append("// multiply T::A by a7; illformed,\n"); //$NON-NLS-1$
		buffer.append("// no visible declaration of a7\n"); //$NON-NLS-1$
		buffer.append("B* a8; // B is not a type name:\n"); //$NON-NLS-1$
		buffer.append("// multiply B by a8; illformed,\n"); //$NON-NLS-1$
		buffer.append("// no visible declarations of B and a8\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6-4):
	struct A {
	struct X { };
	int X;
	};
	template<class T> void f(T t) {
	typename T::X x; // illformed: finds the data member X
	// not the member type X
	}
	 --End Example]
	 */
	public void test14_6s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("struct X { };\n"); //$NON-NLS-1$
		buffer.append("int X;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T t) {\n"); //$NON-NLS-1$
		buffer.append("typename T::X x; // illformed: finds the data member X\n"); //$NON-NLS-1$
		buffer.append("// not the member type X\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6-6):
	template<class T> struct A {
	typedef int B;
	A::B b; // illformed: typename required before A::B
	void f(A<T>::B); // illformed: typename required before A<T>::B
	typename A::B g(); // OK
	};
	 --End Example]
	 */
	public void test14_6s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("typedef int B;\n"); //$NON-NLS-1$
		buffer.append("A::B b; // illformed: typename required before A::B\n"); //$NON-NLS-1$
		buffer.append("void f(A<T>::B); // illformed: typename required before A<T>::B\n"); //$NON-NLS-1$
		buffer.append("typename A::B g(); // OK\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6-7):
	int j;
	template<class T> class X {
	// ...
	void f(T t, int i, char* p)
	{
	t = i; // diagnosed if X::f is instantiated
	// and the assignment to t is an error
	p = i; // may be diagnosed even if X::f is
	// not instantiated
	p = j; // may be diagnosed even if X::f is
	// not instantiated
	}
	void g(T t) {
	// +; //may be diagnosed even if X::g is
	// not instantiated
	}
	};
	 --End Example]
	 */
	public void test14_6s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int j;\n"); //$NON-NLS-1$
		buffer.append("template<class T> class X {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("void f(T t, int i, char* p)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("t = i; // diagnosed if X::f is instantiated\n"); //$NON-NLS-1$
		buffer.append("// and the assignment to t is an error\n"); //$NON-NLS-1$
		buffer.append("p = i; // may be diagnosed even if X::f is\n"); //$NON-NLS-1$
		buffer.append("// not instantiated\n"); //$NON-NLS-1$
		buffer.append("p = j; // may be diagnosed even if X::f is\n"); //$NON-NLS-1$
		buffer.append("// not instantiated\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void g(T t) {\n"); //$NON-NLS-1$
		buffer.append("// +; //may be diagnosed even if X::g is\n"); //$NON-NLS-1$
		buffer.append("// not instantiated\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6-8):
	// #include <iostream>
	using namespace std;
	template<class T> class Set {
	T* p;
	int cnt;
	public:
	Set();
	Set<T>(const Set<T>&);
	void printall()
	{
	for (int i = 0; i<cnt; i++)
	cout << p[i] << '\n';
	}
	// ...
	};
	 --End Example]
	 */
	public void test14_6s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("//#include <iostream>\n"); //$NON-NLS-1$
		buffer.append("using namespace std;\n"); //$NON-NLS-1$
		buffer.append("template<class T> class Set {\n"); //$NON-NLS-1$
		buffer.append("T* p;\n"); //$NON-NLS-1$
		buffer.append("int cnt;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("Set();\n"); //$NON-NLS-1$
		buffer.append("Set<T>(const Set<T>&);\n"); //$NON-NLS-1$
		buffer.append("void printall()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("for (int i = 0; i<cnt; i++)\n"); //$NON-NLS-1$
		buffer.append("cout << p[i] << '\n';\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6-9):
	void f(char);
	template<class T> void g(T t)
	{
	f(1); // f(char)
	f(T(1)); //dependent
	f(t); //dependent
	dd++; //not dependent
	// error: declaration for dd not found
	}
	void f(int);
	double dd;
	void h()
	{
	g(2); //will cause one call of f(char) followed
	// by two calls of f(int)
	g('a'); //will cause three calls of f(char)
	}
	 --End Example]
	 */
	public void test14_6s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f(char);\n"); //$NON-NLS-1$
		buffer.append("template<class T> void g(T t)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(1); // f(char)\n"); //$NON-NLS-1$
		buffer.append("f(T(1)); //dependent\n"); //$NON-NLS-1$
		buffer.append("f(t); //dependent\n"); //$NON-NLS-1$
		buffer.append("dd++; //not dependent\n"); //$NON-NLS-1$
		buffer.append("// error: declaration for dd not found\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("double dd;\n"); //$NON-NLS-1$
		buffer.append("void h()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("g(2); //will cause one call of f(char) followed\n"); //$NON-NLS-1$
		buffer.append("// by two calls of f(int)\n"); //$NON-NLS-1$
		buffer.append("g('a'); //will cause three calls of f(char)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6.1-3a):
	template<class T, T* p, class U = T> class X {  };
	template<class T> void f(T* p = new T);
	 --End Example]
	 */
	public void test14_6_1s3a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T, T* p, class U = T> class X {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T* p = new T);\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_6_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X {\n"); //$NON-NLS-1$
		buffer.append("X* p; // meaning X<T>\n"); //$NON-NLS-1$
		buffer.append("X<T>* p2;\n"); //$NON-NLS-1$
		buffer.append("X<int>* p3;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6.1-2):
	template<class T> class Y;
	template<> class Y<int> {
	Y* p; // meaning Y<int>
	Y<char>* q; // meaning Y<char>
	};
	 --End Example]
	 */
	public void test14_6_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Y;\n"); //$NON-NLS-1$
		buffer.append("template<> class Y<int> {\n"); //$NON-NLS-1$
		buffer.append("Y* p; // meaning Y<int>\n"); //$NON-NLS-1$
		buffer.append("Y<char>* q; // meaning Y<char>\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6.1-3b):
	template<class T> class X : public Array<T> {  };
	template<class T> class Y : public T { };
	 --End Example]
	 */
	public void test14_6_1s3b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X : public Array<T> {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> class Y : public T { };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6.1-4):
	template<class T, int i> class Y {
	int T; // error: templateparameter redeclared
	void f() {
	char T; // error: templateparameter redeclared
	}
	};
	template<class X> class X; // error: templateparameter redeclared
	 --End Example]
	 */
	public void test14_6_1s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T, int i> class Y {\n"); //$NON-NLS-1$
		buffer.append("int T; // error: templateparameter redeclared\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("char T; // error: templateparameter redeclared\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class X> class X; // error: templateparameter redeclared\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6.1-5):
	template<class T> struct A {
	struct B {  };
	void f();
	};
	template<class B> void A<B>::f() {
	B b; // A’s B, not the template parameter
	}
	 --End Example]
	 */
	public void test14_6_1s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("struct B {  };\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class B> void A<B>::f() {\n"); //$NON-NLS-1$
		buffer.append("B b; // A’s B, not the template parameter\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_6_1s6() throws Exception { 
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("int C;\n"); //$NON-NLS-1$
		buffer.append("template<class T> class B {\n"); //$NON-NLS-1$
		buffer.append("void f(T);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("template<class C> void N::B<C>::f(C) {\n"); //$NON-NLS-1$
		buffer.append("C b; // C is the template parameter, not N::C\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6.1-7):
	struct A {
	struct B {  };
	int a;
	int Y;
	};
	template<class B, class a> struct X : A {
	B b; // A’s B
	a b; // error: A’s a isn’t a type name
	};
	 --End Example]
	 */
	public void test14_6_1s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("struct B {  };\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("int Y;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class B, class a> struct X : A {\n"); //$NON-NLS-1$
		buffer.append("B b; // A’s B\n"); //$NON-NLS-1$
		buffer.append("a b; // error: A’s a isn’t a type name\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6.2-2):
	template<class T> struct X : B<T> {
	typename T::A* pa;
	void f(B<T>* pb) {
	static int i = B<T>::i;
	pb->j++;
	}
	};
	 --End Example]
	 */
	public void test14_6_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct X : B<T> {\n"); //$NON-NLS-1$
		buffer.append("typename T::A* pa;\n"); //$NON-NLS-1$
		buffer.append("void f(B<T>* pb) {\n"); //$NON-NLS-1$
		buffer.append("static int i = B<T>::i;\n"); //$NON-NLS-1$
		buffer.append("pb->j++;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6.2-4):
	struct A {
	struct B {  };
	int a;
	int Y;
	};
	int a;
	template<class T> struct Y : T {
	struct B {  };
	B b; // The B defined in Y
	void f(int i) { a = i; } // ::a
	Y* p; // Y<T>
	};
	Y<A> ya;
	 --End Example]
	 */
	public void test14_6_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("struct B {  };\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("int Y;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("template<class T> struct Y : T {\n"); //$NON-NLS-1$
		buffer.append("struct B {  };\n"); //$NON-NLS-1$
		buffer.append("B b; // The B defined in Y\n"); //$NON-NLS-1$
		buffer.append("void f(int i) { a = i; } // ::a\n"); //$NON-NLS-1$
		buffer.append("Y* p; // Y<T>\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("Y<A> ya;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6.3-1):
	void g(double);
	void h();
	template<class T> class Z {
	public:
	void f() {
	g(1); //calls g(double)
	h++; //illformed:
	cannot increment function;
	// this could be diagnosed either here or
	// at the point of instantiation
	}
	};
	void g(int); // not in scope at the point of the template
	// definition, not considered for the call g(1)
	 --End Example]
	 */
	public void test14_6_3s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void g(double);\n"); //$NON-NLS-1$
		buffer.append("void h();\n"); //$NON-NLS-1$
		buffer.append("template<class T> class Z {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("g(1); //calls g(double)\n"); //$NON-NLS-1$
		buffer.append("h++; //illformed:\n"); //$NON-NLS-1$
		buffer.append("// cannot increment function;\n"); //$NON-NLS-1$
		buffer.append("// this could be diagnosed either here or\n"); //$NON-NLS-1$
		buffer.append("// at the point of instantiation\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void g(int); // not in scope at the point of the template\n"); //$NON-NLS-1$
		buffer.append("// definition, not considered for the call g(1)\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.6.5-2):
	template<typename T> class number {
	number(int);
	//...
	friend number gcd(number& x, number& y) {  }
	//...
	};
	void g()
	{
	number<double> a(3), b(4);
	//...
	a = gcd(a,b); // finds gcd because number<double> is an
	// associated class, making gcd visible
	// in its namespace (global scope)
	b = gcd(3,4); // illformed; gcd is not visible
	}
	 --End Example]
	 */
	public void test14_6_5s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<typename T> class number {\n"); //$NON-NLS-1$
		buffer.append("number(int);\n"); //$NON-NLS-1$
		buffer.append("//...\n"); //$NON-NLS-1$
		buffer.append("friend number gcd(number& x, number& y) {  }\n"); //$NON-NLS-1$
		buffer.append("//...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("number<double> a(3), b(4);\n"); //$NON-NLS-1$
		buffer.append("//...\n"); //$NON-NLS-1$
		buffer.append("a = gcd(a,b); // finds gcd because number<double> is an\n"); //$NON-NLS-1$
		buffer.append("// associated class, making gcd visible\n"); //$NON-NLS-1$
		buffer.append("// in its namespace (global scope)\n"); //$NON-NLS-1$
		buffer.append("b = gcd(3,4); // illformed; gcd is not visible\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
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
	public void test14_7s6() throws Exception { 
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X {\n"); //$NON-NLS-1$
		buffer.append("static T s;\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> T X<T>::s = 0;\n"); //$NON-NLS-1$
		buffer.append("X<int> aa;\n"); //$NON-NLS-1$
		buffer.append("X<char*> bb;\n"); //$NON-NLS-1$
		
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.1-3):
	template<class T> class Z {
	public:
	void f();
	void g();
	};
	void h()
	{
	Z<int> a; // instantiation of class Z<int> required
	Z<char>* p; // instantiation of class Z<char> not
	// required
	Z<double>* q; // instantiation of class Z<double>
	// not required
	a.f(); //instantiation of Z<int>::f() required
	p->g(); //instantiation of class Z<char> required, and
	// instantiation of Z<char>::g() required
	}
	 --End Example]
	 */
	public void test14_7_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Z {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("void g();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void h()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("Z<int> a; // instantiation of class Z<int> required\n"); //$NON-NLS-1$
		buffer.append("Z<char>* p; // instantiation of class Z<char> not\n"); //$NON-NLS-1$
		buffer.append("// required\n"); //$NON-NLS-1$
		buffer.append("Z<double>* q; // instantiation of class Z<double>\n"); //$NON-NLS-1$
		buffer.append("// not required\n"); //$NON-NLS-1$
		buffer.append("a.f(); //instantiation of Z<int>::f() required\n"); //$NON-NLS-1$
		buffer.append("p->g(); //instantiation of class Z<char> required, and\n"); //$NON-NLS-1$
		buffer.append("// instantiation of Z<char>::g() required\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.1-4):
	template<class T> class B {  };
	template<class T> class D : public B<T> {  };
	void f(void*);
	void f(B<int>*);
	void g(D<int>* p, D<char>* pp, D<double> ppp)
	{
	f(p); //instantiation of D<int> required: call f(B<int>*)
	B<char>* q = pp; // instantiation of D<char> required:
	// convert D<char>* to B<char>*
	delete ppp; // instantiation of D<double> required
	}
	 --End Example]
	 */
	public void test14_7_1s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class B {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> class D : public B<T> {  };\n"); //$NON-NLS-1$
		buffer.append("void f(void*);\n"); //$NON-NLS-1$
		buffer.append("void f(B<int>*);\n"); //$NON-NLS-1$
		buffer.append("void g(D<int>* p, D<char>* pp, D<double> ppp)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(p); //instantiation of D<int> required: call f(B<int>*)\n"); //$NON-NLS-1$
		buffer.append("B<char>* q = pp; // instantiation of D<char> required:\n"); //$NON-NLS-1$
		buffer.append("// convert D<char>* to B<char>*\n"); //$NON-NLS-1$
		buffer.append("delete ppp; // instantiation of D<double> required\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.1-6):
	template<class T> class X;
	X<char> ch; // error: definition of X required
	 --End Example]
	 */
	public void test14_7_1s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X;\n"); //$NON-NLS-1$
		buffer.append("X<char> ch; // error: definition of X required\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7-3):
	template<class T = int> struct A {
	static int x;
	};
	template<class U> void g(U) { }
	template<> struct A<double> { }; // specialize for T == double
	template<> struct A<> { }; // specialize for T == int
	template<> void g(char) { } // specialize for U == char
	// U is deduced from the parameter type
	template<> void g<int>(int) { } // specialize for U == int
	template<> int A<char>::x = 0; // specialize for T == char
	template<class T = int> struct B {
	static int x;
	};
	template<> int B<>::x = 1; // specialize for T == int
	 --End Example]
	 */
	public void test14_7s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T = int> struct A {\n"); //$NON-NLS-1$
		buffer.append("static int x;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class U> void g(U) { }\n"); //$NON-NLS-1$
		buffer.append("template<> struct A<double> { }; // specialize for T == double\n"); //$NON-NLS-1$
		buffer.append("template<> struct A<> { }; // specialize for T == int\n"); //$NON-NLS-1$
		buffer.append("template<> void g(char) { } // specialize for U == char\n"); //$NON-NLS-1$
		buffer.append("// U is deduced from the parameter type\n"); //$NON-NLS-1$
		buffer.append("template<> void g<int>(int) { } // specialize for U == int\n"); //$NON-NLS-1$
		buffer.append("template<> int A<char>::x = 0; // specialize for T == char\n"); //$NON-NLS-1$
		buffer.append("template<class T = int> struct B {\n"); //$NON-NLS-1$
		buffer.append("static int x;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<> int B<>::x = 1; // specialize for T == int\n"); //$NON-NLS-1$
		
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_7_1s5() throws Exception {
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

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_7_1s10() throws Exception { 
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
		
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}

	/**
	 [--Start Example(CPP 14.7.1-12):
	template<class T> void f(T x, T y = ydef(T()), T z = zdef(T()));
	class A { };
	A zdef(A);
	void g(A a, A b, A c) {
	f(a, b, c); // no default argument instantiation
	f(a, b); // default argument z = zdef(T()) instantiated
	f(a); //illformed; ydef is not declared
	}
	 --End Example]
	 */
	public void test14_7_1s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T x, T y = ydef(T()), T z = zdef(T()));\n"); //$NON-NLS-1$
		buffer.append("class A { };\n"); //$NON-NLS-1$
		buffer.append("A zdef(A);\n"); //$NON-NLS-1$
		buffer.append("void g(A a, A b, A c) {\n"); //$NON-NLS-1$
		buffer.append("f(a, b, c); // no default argument instantiation\n"); //$NON-NLS-1$
		buffer.append("f(a, b); // default argument z = zdef(T()) instantiated\n"); //$NON-NLS-1$
		buffer.append("f(a); //illformed; ydef is not declared\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
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
	public void test14_7_1s14() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X {\n"); //$NON-NLS-1$
		buffer.append("X<T>* p; // OK\n"); //$NON-NLS-1$
		buffer.append("X<T*> a; // implicit generation of X<T> requires\n"); //$NON-NLS-1$
		buffer.append("// the implicit instantiation of X<T*> which requires\n"); //$NON-NLS-1$
		buffer.append("// the implicit instantiation of X<T**> which ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
	
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.2-2):
	template<class T> class Array { void mf(); };
	template class Array<char>;
	template void Array<int>::mf();
	template<class T> void sort(Array<T>& v) {  }
	template void sort(Array<char>&); // argument is deduced here
	namespace N {
	template<class T> void f(T&) { }
	}
	template void N::f<int>(int&);
	 --End Example]
	 */
	public void test14_7_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Array { void mf(); };\n"); //$NON-NLS-1$
		buffer.append("template class Array<char>;\n"); //$NON-NLS-1$
		buffer.append("template void Array<int>::mf();\n"); //$NON-NLS-1$
		buffer.append("template<class T> void sort(Array<T>& v) {  }\n"); //$NON-NLS-1$
		buffer.append("template void sort(Array<char>&); // argument is deduced here\n"); //$NON-NLS-1$
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T&) { }\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("template void N::f<int>(int&);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.2-5):
	namespace N {
	template<class T> class Y { void mf() { } };
	}
	template class Y<int>; // error: class template Y not visible
	// in the global namespace
	using N::Y;
	template class Y<int>; // OK: explicit instantiation in namespace N
	template class N::Y<char*>; // OK: explicit instantiation in namespace N
	template void N::Y<double>::mf(); // OK: explicit instantiation
	// in namespace N
	 --End Example]
	 */
	public void test14_7_2s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("template<class T> class Y { void mf() { } };\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("template class Y<int>; // error: class template Y not visible\n"); //$NON-NLS-1$
		buffer.append("// in the global namespace\n"); //$NON-NLS-1$
		buffer.append("using N::Y;\n"); //$NON-NLS-1$
		buffer.append("template class Y<int>; // OK: explicit instantiation in namespace N\n"); //$NON-NLS-1$
		buffer.append("template class N::Y<char*>; // OK: explicit instantiation in namespace N\n"); //$NON-NLS-1$
		buffer.append("template void N::Y<double>::mf(); // OK: explicit instantiation\n"); //$NON-NLS-1$
		buffer.append("// in namespace N\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.2-6):
	template<class T> class Array {  };
	template<class T> void sort(Array<T>& v);
	// instantiate sort(Array<int>&) - templateargument deduced
	template void sort<>(Array<int>&);
	 --End Example]
	 */
	public void test14_7_2s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Array {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void sort(Array<T>& v);\n"); //$NON-NLS-1$
		buffer.append("// instantiate sort(Array<int>&) - templateargument deduced\n"); //$NON-NLS-1$
		buffer.append("template void sort<>(Array<int>&);\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.2-9):
	char* p = 0;
	template<class T> T g(T = &p);
	template int g<int>(int); // OK even though &p isn’t an int.
	 --End Example]
	 */
	public void test14_7_2s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("char* p = 0;\n"); //$NON-NLS-1$
		buffer.append("template<class T> T g(T = &p);\n"); //$NON-NLS-1$
		buffer.append("template int g<int>(int); // OK even though &p isn’t an int.\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_7_3s1() throws Exception { 
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class stream;\n"); //$NON-NLS-1$
		buffer.append("template<> class stream<char> {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> class Array {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void sort(Array<T>& v) {  }\n"); //$NON-NLS-1$
		buffer.append("template<> void sort<char*>(Array<char*>&) ;\n"); //$NON-NLS-1$
	
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.3-3):
	template<> class X<int> {  }; // error: X not a template
	template<class T> class X;
	template<> class X<char*> {  }; // OK: X is a template
	 --End Example]
	 */
	public void test14_7_3s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<> class X<int> {  }; // error: X not a template\n"); //$NON-NLS-1$
		buffer.append("template<class T> class X;\n"); //$NON-NLS-1$
		buffer.append("template<> class X<char*> {  }; // OK: X is a template\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.3-5):
	template<class T> struct A {
	void f(T) {  }
	};
	template<> struct A<int> {
	void f(int);
	};
	void h()
	{
	A<int> a;
	a.f(16); // A<int>::f must be defined somewhere
	}
	// explicit specialization syntax not used for a member of
	// explicitly specialized class template specialization
	void A<int>::f(int) {  }
	 --End Example]
	 */
	public void test14_7_3s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("void f(T) {  }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<> struct A<int> {\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void h()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("A<int> a;\n"); //$NON-NLS-1$
		buffer.append("a.f(16); // A<int>::f must be defined somewhere\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("// explicit specialization syntax not used for a member of\n"); //$NON-NLS-1$
		buffer.append("// explicitly specialized class template specialization\n"); //$NON-NLS-1$
		buffer.append("void A<int>::f(int) {  }\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.3-6):
	template<class T> class Array {  };
	template<class T> void sort(Array<T>& v) {  }
	void f(Array<String>& v)
	{
	sort(v); //use primary template
	// sort(Array<T>&), T is String
	}
	template<> void sort<String>(Array<String>& v); // error: specialization
	// after use of primary template
	template<> void sort<>(Array<char*>& v); // OK: sort<char*> not yet used
	 --End Example]
	 */
	public void test14_7_3s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Array {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void sort(Array<T>& v) {  }\n"); //$NON-NLS-1$
		buffer.append("void f(Array<String>& v)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("sort(v); //use primary template\n"); //$NON-NLS-1$
		buffer.append("// sort(Array<T>&), T is String\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("template<> void sort<String>(Array<String>& v); // error: specialization\n"); //$NON-NLS-1$
		buffer.append("// after use of primary template\n"); //$NON-NLS-1$
		buffer.append("template<> void sort<>(Array<char*>& v); // OK: sort<char*> not yet used\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.3-9):
	namespace N {
	template<class T> class X {  };
	template<class T> class Y {  };
	template<> class X<int> {  }; // OK: specialization
	// in same namespace
	template<> class Y<double>; // forward declare intent to
	// specialize for double
	}
	template<> class N::Y<double> {  }; // OK: specialization
	// in same namespace
	 --End Example]
	 */
	public void test14_7_3s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace N {\n"); //$NON-NLS-1$
		buffer.append("template<class T> class X {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> class Y {  };\n"); //$NON-NLS-1$
		buffer.append("template<> class X<int> {  }; // OK: specialization\n"); //$NON-NLS-1$
		buffer.append("// in same namespace\n"); //$NON-NLS-1$
		buffer.append("template<> class Y<double>; // forward declare intent to\n"); //$NON-NLS-1$
		buffer.append("// specialize for double\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("template<> class N::Y<double> {  }; // OK: specialization\n"); //$NON-NLS-1$
		buffer.append("// in same namespace\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.3-11):
	template<class T> class Array {  };
	template<class T> void sort(Array<T>& v);
	// explicit specialization for sort(Array<int>&)
	// with deduces templateargument of type int
	template<> void sort(Array<int>&);
	 --End Example]
	 */
	public void test14_7_3s11() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Array {  };\n"); //$NON-NLS-1$
		buffer.append("template<class T> void sort(Array<T>& v);\n"); //$NON-NLS-1$
		buffer.append("// explicit specialization for sort(Array<int>&)\n"); //$NON-NLS-1$
		buffer.append("// with deduces templateargument of type int\n"); //$NON-NLS-1$
		buffer.append("template<> void sort(Array<int>&);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0 );
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
	public void test14_7_3s17() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T1> class A {\n"); //$NON-NLS-1$
		buffer.append("template<class T2> class B {\n"); //$NON-NLS-1$
		buffer.append("void mf();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<> template<> class A<int>::B<double> { };\n"); //$NON-NLS-1$
		buffer.append("template<> template<> void A<char>::B<char>::mf() { };\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	/**
	 [--Start Example(CPP 14.7.3-10):
	template<class T> class X; // X is a class template
	template<> class X<int>;
	X<int>* p; // OK: pointer to declared class X<int>
	X<int> x; // error: object of incomplete class X<int>
	 --End Example]
	 */
	public void test14_7_3s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X; // X is a class template\n"); //$NON-NLS-1$
		buffer.append("template<> class X<int>;\n"); //$NON-NLS-1$
		buffer.append("X<int>* p; // OK: pointer to declared class X<int>\n"); //$NON-NLS-1$
		buffer.append("X<int> x; // error: object of incomplete class X<int>\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.3-12):
	template <class T> void f(T);
	template <class T> void f(T*);
	template <> void f(int*); // Ambiguous
	template <> void f<int>(int*); // OK
	template <> void f(int); // OK
	 --End Example]
	 */
	public void test14_7_3s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> void f(T);\n"); //$NON-NLS-1$
		buffer.append("template <class T> void f(T*);\n"); //$NON-NLS-1$
		buffer.append("template <> void f(int*); // Ambiguous\n"); //$NON-NLS-1$
		buffer.append("template <> void f<int>(int*); // OK\n"); //$NON-NLS-1$
		buffer.append("template <> void f(int); // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 1);
	}
	
	/**
	 [--Start Example(CPP 14.7.3-14):
	template<class T> void f(T) {  }
	template<class T> inline T g(T) {  }
	template<> inline void f<>(int) {  } // OK: inline
	template<> int g<>(int) {  } // OK: not inline
	 --End Example]
	 */
	public void test14_7_3s14() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T) {  }\n"); //$NON-NLS-1$
		buffer.append("template<class T> inline T g(T) {  }\n"); //$NON-NLS-1$
		buffer.append("template<> inline void f<>(int) {  } // OK: inline\n"); //$NON-NLS-1$
		buffer.append("template<> int g<>(int) {  } // OK: not inline\n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_7_3s16() throws Exception {
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
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.7.3-18):
	template<class T1> class A {
	template<class T2> class B {
	template<class T3> void mf1(T3);
	void mf2();
	};
	};
	template<> template<class X>
	class A<int>::B { };
	template<> template<> template<class T>
	void A<int>::B<double>::mf1(T t) { };
	template<class Y> template<>
	void A<Y>::B<double>::mf2() { }; // illformed; B<double> is specialized but
	// its enclosing class template A is not
	 --End Example]
	 */
	public void test14_7_3s18() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T1> class A {\n"); //$NON-NLS-1$
		buffer.append("template<class T2> class B {\n"); //$NON-NLS-1$
		buffer.append("template<class T3> void mf1(T3);\n"); //$NON-NLS-1$
		buffer.append("void mf2();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<> template<class X>\n"); //$NON-NLS-1$
		buffer.append("class A<int>::B { };\n"); //$NON-NLS-1$
		buffer.append("template<> template<> template<class T>\n"); //$NON-NLS-1$
		buffer.append("void A<int>::B<double>::mf1(T t) { };\n"); //$NON-NLS-1$
		buffer.append("template<class Y> template<>\n"); //$NON-NLS-1$
		buffer.append("void A<Y>::B<double>::mf2() { }; // illformed; B<double> is specialized but\n"); //$NON-NLS-1$
		buffer.append("// its enclosing class template A is not\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
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
	public void test14_8s2() throws Exception {
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

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.1-1):
	 template<class T> void sort(Array<T>& v);
	 void f(Array<dcomplex>& cv, Array<int>& ci)
	 {
	 sort<dcomplex>(cv); // sort(Array<dcomplex>&)
	 sort<int>(ci); // sort(Array<int>&)
	 }
	 template<class U, class V> U convert(V v);
	 void g(double d)
	 {
	 int i = convert<int,double>(d); // int convert(double)
	 char c = convert<char,double>(d); // char convert(double)
	 }
	 --End Example]
	 */
	public void test14_8_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		 buffer.append("template<class T> void sort(Array<T>& v);\n"); //$NON-NLS-1$
		 buffer.append("void f(Array<dcomplex>& cv, Array<int>& ci)\n"); //$NON-NLS-1$
		 buffer.append("{\n"); //$NON-NLS-1$
		 buffer.append("sort<dcomplex>(cv); // sort(Array<dcomplex>&)\n"); //$NON-NLS-1$
		 buffer.append("sort<int>(ci); // sort(Array<int>&)\n"); //$NON-NLS-1$
		 buffer.append("}\n"); //$NON-NLS-1$
		 buffer.append("template<class U, class V> U convert(V v);\n"); //$NON-NLS-1$
		 buffer.append("void g(double d)\n"); //$NON-NLS-1$
		 buffer.append("{\n"); //$NON-NLS-1$
		 buffer.append("int i = convert<int,double>(d); // int convert(double)\n"); //$NON-NLS-1$
		 buffer.append("char c = convert<char,double>(d); // char convert(double)\n"); //$NON-NLS-1$
		 buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.1-2):
	template<class X, class Y> X f(Y);
	void g()
	{
	int i = f<int>(5.6); // Y is deduced to be double
	int j = f(5.6); // illformed: X cannot be deduced
	}
	 --End Example]
	 */
	public void test14_8_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class X, class Y> X f(Y);\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int i = f<int>(5.6); // Y is deduced to be double\n"); //$NON-NLS-1$
		buffer.append("int j = f(5.6); // illformed: X cannot be deduced\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.1-3):
	template<class X, class Y, class Z> X f(Y,Z);
	void g()
	{
	f<int,char*,double>("aa",3.0);
	f<int,char*>("aa",3.0); // Z is deduced to be double
	f<int>("aa",3.0); // Y is deduced to be char*, and
	// Z is deduced to be double
	f("aa",3.0); //error: X cannot be deduced
	}
	 --End Example]
	 */
	public void test14_8_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class X, class Y, class Z> X f(Y,Z);\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f<int,char*,double>(\"aa\",3.0);\n"); //$NON-NLS-1$
		buffer.append("f<int,char*>(\"aa\",3.0); // Z is deduced to be double\n"); //$NON-NLS-1$
		buffer.append("f<int>(\"aa\",3.0); // Y is deduced to be char*, and\n"); //$NON-NLS-1$
		buffer.append("// Z is deduced to be double\n"); //$NON-NLS-1$
		buffer.append("f(\"aa\",3.0); //error: X cannot be deduced\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.1-6):
	namespace A {
	struct B { };
	template<int X> void f();
	}
	namespace C {
	template<class T> void f(T t);
	}
	void g(A::B b) {
	f<3>(b); //illformed: not a function call
	A::f<3>(b); //wellformed
	C::f<3>(b); //illformed; argument dependent lookup
	// only applies to unqualified names
	using C::f;
	f<3>(b); //wellformed because C::f is visible; then
	// A::f is found by argument dependent lookup
	}
	 --End Example]
	 */
	public void test14_8_1s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("struct B { };\n"); //$NON-NLS-1$
		buffer.append("template<int X> void f();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace C {\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T t);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void g(A::B b) {\n"); //$NON-NLS-1$
		buffer.append("f<3>(b); //illformed: not a function call\n"); //$NON-NLS-1$
		buffer.append("A::f<3>(b); //wellformed\n"); //$NON-NLS-1$
		buffer.append("C::f<3>(b); //illformed; argument dependent lookup\n"); //$NON-NLS-1$
		buffer.append("// only applies to unqualified names\n"); //$NON-NLS-1$
		buffer.append("using C::f;\n"); //$NON-NLS-1$
		buffer.append("f<3>(b); //wellformed because C::f is visible; then\n"); //$NON-NLS-1$
		buffer.append("// A::f is found by argument dependent lookup\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.2-1):
	void f(Array<dcomplex>& cv, Array<int>& ci)
	{
	sort(cv); //call sort(Array<dcomplex>&)
	sort(ci); //call sort(Array<int>&)
	}
	void g(double d)
	{
	int i = convert<int>(d); // call convert<int,double>(double)
	int c = convert<char>(d); // call convert<char,double>(double)
	}
	 --End Example]
	 */
	public void test14_8_2s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f(Array<dcomplex>& cv, Array<int>& ci)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("sort(cv); //call sort(Array<dcomplex>&)\n"); //$NON-NLS-1$
		buffer.append("sort(ci); //call sort(Array<int>&)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void g(double d)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int i = convert<int>(d); // call convert<int,double>(double)\n"); //$NON-NLS-1$
		buffer.append("int c = convert<char>(d); // call convert<char,double>(double)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.2-2a):
	template <class T> int f(T[5]);
	int I = f<int>(0);
	int j = f<void>(0); // invalid array
	 --End Example]
	 */
	public void test14_8_2s2a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> int f(T[5]);\n"); //$NON-NLS-1$
		buffer.append("int I = f<int>(0);\n"); //$NON-NLS-1$
		buffer.append("int j = f<void>(0); // invalid array\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.2-2d):
	template <class T> int f(int T::*);
	int i = f<int>(0);
	 --End Example]
	 */
	public void test14_8_2s2d() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> int f(int T::*);\n"); //$NON-NLS-1$
		buffer.append("int i = f<int>(0);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 2);
	}
	
	/**
	 [--Start Example(CPP 14.8.2-2e):
	template <class T, T*> int f(int);
	int i2 = f<int,1>(0); // can’t conv 1 to int*
	 --End Example]
	 */
	public void test14_8_2s2e() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T, T*> int f(int);\n"); //$NON-NLS-1$
		buffer.append("int i2 = f<int,1>(0); // can’t conv 1 to int*\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 1);
	}
	
	/**
	 [--Start Example(CPP 14.8.2-5):
	template <int> int f(int);
	template <signed char> int f(int);
	int i1 = f<1>(0); // ambiguous
	int i2 = f<1000>(0); // ambiguous
	 --End Example]
	 */
	public void test14_8_2s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <int> int f(int);\n"); //$NON-NLS-1$
		buffer.append("template <signed char> int f(int);\n"); //$NON-NLS-1$
		buffer.append("int i1 = f<1>(0); // ambiguous\n"); //$NON-NLS-1$
		buffer.append("int i2 = f<1000>(0); // ambiguous\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.2.4-5):
	template<class T> void f(T x, T y) {  }
	struct A {  };
	struct B : A { };
	int g(A a, B b)
	{
	f(a,b); //error: T could be A or B
	f(b,a); //error: T could be A or B
	f(a,a); //OK: T is A
	f(b,b); //OK: T is B
	}
	 --End Example]
	 */
	public void test14_8_2_4s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T x, T y) {  }\n"); //$NON-NLS-1$
		buffer.append("struct A {  };\n"); //$NON-NLS-1$
		buffer.append("struct B : A { };\n"); //$NON-NLS-1$
		buffer.append("int g(A a, B b)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(a,b); //error: T could be A or B\n"); //$NON-NLS-1$
		buffer.append("f(b,a); //error: T could be A or B\n"); //$NON-NLS-1$
		buffer.append("f(a,a); //OK: T is A\n"); //$NON-NLS-1$
		buffer.append("f(b,b); //OK: T is B\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.2.4-6):
	template <class T, class U> void f( T (*)( T, U, U ) );
	int g1( int, float, float);
	char g2( int, float, float);
	int g3( int, char, float);
	void r()
	{
	f(g1); //OK: T is int and U is float
	f(g2); //error: T could be char or int
	f(g3); //error: U could be char or float
	}
	 --End Example]
	 */
	public void test14_8_2_4s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T, class U> void f( T (*)( T, U, U ) );\n"); //$NON-NLS-1$
		buffer.append("int g1( int, float, float);\n"); //$NON-NLS-1$
		buffer.append("char g2( int, float, float);\n"); //$NON-NLS-1$
		buffer.append("int g3( int, char, float);\n"); //$NON-NLS-1$
		buffer.append("void r()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(g1); //OK: T is int and U is float\n"); //$NON-NLS-1$
		buffer.append("f(g2); //error: T could be char or int\n"); //$NON-NLS-1$
		buffer.append("f(g3); //error: U could be char or float\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.2.4-12):
	template<class T, T i> void f(double a[10][i]);
	int v[10][20];
	int foo() {
	f(v); //error: argument for templateparameter
	//T cannot be deduced
	}
	 --End Example]
	 */
	public void test14_8_2_4s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T, T i> void f(double a[10][i]);\n"); //$NON-NLS-1$
		buffer.append("int v[10][20];\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("f(v); //error: argument for templateparameter\n"); //$NON-NLS-1$
		buffer.append("//T cannot be deduced\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.2.4-13):
	template<int i> void f1(int a[10][i]);
	template<int i> void f2(int a[i][20]);
	template<int i> void f3(int (&a)[i][20]);
	void g()
	{
	int v[10][20];
	f1(v); //OK: i deduced to be 20
	f1<20>(v); //OK
	f2(v); //error: cannot deduce templateargument 	i
	f2<10>(v); //OK
	f3(v); //OK: i deduced to be 10
	}
	 --End Example]
	 */
	public void test14_8_2_4s13() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<int i> void f1(int a[10][i]);\n"); //$NON-NLS-1$
		buffer.append("template<int i> void f2(int a[i][20]);\n"); //$NON-NLS-1$
		buffer.append("template<int i> void f3(int (&a)[i][20]);\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int v[10][20];\n"); //$NON-NLS-1$
		buffer.append("f1(v); //OK: i deduced to be 20\n"); //$NON-NLS-1$
		buffer.append("f1<20>(v); //OK\n"); //$NON-NLS-1$
		buffer.append("f2(v); //error: cannot deduce templateargument i\n"); //$NON-NLS-1$
		buffer.append("f2<10>(v); //OK\n"); //$NON-NLS-1$
		buffer.append("f3(v); //OK: i deduced to be 10\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.2.4-15):
	template<int i> class A {  };
	template<short s> void f(A<s>);
	void k1() {
	A<1> a;
	f(a); //error: deduction fails for conversion from int to short
	f<1>(a); //OK
	}
	template<const short cs> class B { };
	template<short s> void h(B<s>);
	void k2() {
	B<1> b;
	g(b); //OK: cvqualifiers are ignored on template parameter types
	}
	 --End Example]
	 */
	public void test14_8_2_4s15() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<int i> class A { /* ... */ };\n"); //$NON-NLS-1$
		buffer.append("template<short s> void f(A<s>);\n"); //$NON-NLS-1$
		buffer.append("void k1() {\n"); //$NON-NLS-1$
		buffer.append("A<1> a;\n"); //$NON-NLS-1$
		buffer.append("f(a); //error: deduction fails for conversion from int to short\n"); //$NON-NLS-1$
		buffer.append("f<1>(a); //OK\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("template<const short cs> class B { };\n"); //$NON-NLS-1$
		buffer.append("template<short s> void h(B<s>);\n"); //$NON-NLS-1$
		buffer.append("void k2() {\n"); //$NON-NLS-1$
		buffer.append("B<1> b;\n"); //$NON-NLS-1$
		buffer.append("g(b); //OK: cvqualifiers are ignored on template parameter types\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.2.4-16):
	template<class T> void f(void(*)(T,int));
	template<class T> void foo(T,int);
	void g(int,int);
	void g(char,int);
	void h(int,int,int);
	void h(char,int);
	int m()
	{
	f(&g); //error: ambiguous
	f(&h); //OK: void h(char,int) is a unique match
	f(&foo); //error: type deduction fails because foo is a template
	}
	 --End Example]
	 */
	public void test14_8_2_4s16() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(void(*)(T,int));\n"); //$NON-NLS-1$
		buffer.append("template<class T> void foo(T,int);\n"); //$NON-NLS-1$
		buffer.append("void g(int,int);\n"); //$NON-NLS-1$
		buffer.append("void g(char,int);\n"); //$NON-NLS-1$
		buffer.append("void h(int,int,int);\n"); //$NON-NLS-1$
		buffer.append("void h(char,int);\n"); //$NON-NLS-1$
		buffer.append("int m()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(&g); //error: ambiguous\n"); //$NON-NLS-1$
		buffer.append("f(&h); //OK: void h(char,int) is a unique match\n"); //$NON-NLS-1$
		buffer.append("f(&foo); //error: type deduction fails because foo is a template\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.2.4-17):
	template <class T> void f(T = 5, T = 7);
	void g()
	{
	f(1); //OK: call f<int>(1,7)
	f(); //error: cannot deduce T
	f<int>(); //OK: call f<int>(5,7)
	}
	 --End Example]
	 */
	public void test14_8_2_4s17() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> void f(T = 5, T = 7);\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(1); //OK: call f<int>(1,7)\n"); //$NON-NLS-1$
		buffer.append("f(); //error: cannot deduce T\n"); //$NON-NLS-1$
		buffer.append("f<int>(); //OK: call f<int>(5,7)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.3-2):
	template<class T> T max(T a, T b) { return a>b?a:b; }
	void f(int a, int b, char c, char d)
	{
	int m1 = max(a,b); // max(int a, int b)
	char m2 = max(c,d); // max(char a, char b)
	int m3 = max(a,c); // error: cannot generate max(int,char)
	}
	 --End Example]
	 */
	public void test14_8_3s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> T max(T a, T b) { return a>b?a:b; }\n"); //$NON-NLS-1$
		buffer.append("void f(int a, int b, char c, char d)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int m1 = max(a,b); // max(int a, int b)\n"); //$NON-NLS-1$
		buffer.append("char m2 = max(c,d); // max(char a, char b)\n"); //$NON-NLS-1$
		buffer.append("int m3 = max(a,c); // error: cannot generate max(int,char)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.3-3):
	template<class T> T max(T a, T b) { return a>b?a:b; }
	int max(int,int);
	void f(int a, int b, char c, char d)
	{
	int m1 = max(a,b); // max(int a, int b)
	char m2 = max(c,d); // max(char a, char b)
	int m3 = max(a,c); // resolved
	}
	 --End Example]
	 */
	public void test14_8_3s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> T max(T a, T b) { return a>b?a:b; }\n"); //$NON-NLS-1$
		buffer.append("int max(int,int);\n"); //$NON-NLS-1$
		buffer.append("void f(int a, int b, char c, char d)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int m1 = max(a,b); // max(int a, int b)\n"); //$NON-NLS-1$
		buffer.append("char m2 = max(c,d); // max(char a, char b)\n"); //$NON-NLS-1$
		buffer.append("int m3 = max(a,c); // resolved\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_8_3s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T); // declaration    \n"); //$NON-NLS-1$
		buffer.append("void g() {                                     \n"); //$NON-NLS-1$
		buffer.append("   f(\"Annemarie\"); // call of f<const char*> \n"); //$NON-NLS-1$
		buffer.append("}                                              \n"); //$NON-NLS-1$

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 15-2):
	int foo() {
	lab: try {
	int t1;
	try {
	int t2;
	if (1)
	goto lab;
	} catch(...) { // handler 2 
	}
	} catch(...) { // handler 1
	}
	}
	 --End Example]
	 */
	public void test15s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("lab: try {\n"); //$NON-NLS-1$
		buffer.append("int t1;\n"); //$NON-NLS-1$
		buffer.append("try {\n"); //$NON-NLS-1$
		buffer.append("int t2;\n"); //$NON-NLS-1$
		buffer.append("if (1)\n"); //$NON-NLS-1$
		buffer.append("goto lab;\n"); //$NON-NLS-1$
		buffer.append("} catch(...) { // handler 2 \n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("} catch(...) { // handler 1\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14-3):
	int f(int);
	class C {
	int i;
	double d;
	public:
	C(int, double);
	};
	C::C(int ii, double id)
	try
	: i(f(ii)), d(id)
	{
	// constructor function body
	}
	catch (...)
	{
	// handles exceptions thrown from the ctorinitializer
	// and from the constructor function body
	}
	 --End Example]
	 */
	public void test15s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("double d;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("C(int, double);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("C::C(int ii, double id)\n"); //$NON-NLS-1$
		buffer.append("try\n"); //$NON-NLS-1$
		buffer.append(": i(f(ii)), d(id)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("// constructor function body\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("catch (...)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("// handles exceptions thrown from the ctorinitializer\n"); //$NON-NLS-1$
		buffer.append("// and from the constructor function body\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 15.1-1):
	class Overflow {
	// ...
	public:
	Overflow(char,double,double);
	};
	void f(double x)
	{
	// ...
	throw Overflow('+',x,3.45e107);
	}
	int foo() {
	try {
	// ...
	f(1.2);
	// ...
	}
	catch(Overflow& oo) {
	// handle exceptions of type Overflow here
	}
	}
	 --End Example]
	 */
	public void test15_1s1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class Overflow {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("Overflow(char,double,double);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f(double x)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("throw Overflow('+',x,3.45e107);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("try {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("f(1.2);\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("catch(Overflow& oo) {\n"); //$NON-NLS-1$
		buffer.append("// handle exceptions of type Overflow here\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 15.1-6):
	int foo() {
	try {
	// ...
	}
	catch (...) { // catch all exceptions
	// respond (partially) to exception
	throw; //pass the exception to some
	// other handler
	}
	}
	 --End Example]
	 */
	public void test15_1s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("try {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("catch (...) { // catch all exceptions\n"); //$NON-NLS-1$
		buffer.append("// respond (partially) to exception\n"); //$NON-NLS-1$
		buffer.append("throw; //pass the exception to some\n"); //$NON-NLS-1$
		buffer.append("// other handler\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 15.3-4):
	class Matherr {  virtual vf(); };
	class Overflow: public Matherr {  };
	class Underflow: public Matherr {  };
	class Zerodivide: public Matherr {  };
	void f()
	{
	try {
	}
	catch (Overflow oo) {
	// ...
	}
	catch (Matherr mm) {
	// ...
	}
	}
	 --End Example]
	 */
	public void test15_3s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class Matherr {  virtual vf(); };\n"); //$NON-NLS-1$
		buffer.append("class Overflow: public Matherr {  };\n"); //$NON-NLS-1$
		buffer.append("class Underflow: public Matherr {  };\n"); //$NON-NLS-1$
		buffer.append("class Zerodivide: public Matherr {  };\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("try {\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("catch (Overflow oo) {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("catch (Matherr mm) {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 15.4-1a):
	void f() throw(int); // OK
	void (*fp)() throw (int); // OK
	void g(void pfa() throw(int)); // OK
	 --End Example]
	 */
	public void test15_4s1a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f() throw(int); // OK\n"); //$NON-NLS-1$
		buffer.append("void (*fp)() throw (int); // OK\n"); //$NON-NLS-1$
		buffer.append("void g(void pfa() throw(int)); // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 15.4-1b):
	typedef int (*pf)() throw(int); // illformed
	 --End Example]
	 */
	public void test15_4s1b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int (*pf)() throw(int); // illformed\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 15.4-3a):
	struct B {
	virtual void f() throw (int, double);
	virtual void g();
	};
	struct D: B {
	void f(); // illformed
	void g() throw (int); // OK
	};
	 --End Example]
	 */
	public void test15_4s3a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("virtual void f() throw (int, double);\n"); //$NON-NLS-1$
		buffer.append("virtual void g();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D: B {\n"); //$NON-NLS-1$
		buffer.append("void f(); // illformed\n"); //$NON-NLS-1$
		buffer.append("void g() throw (int); // OK\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 15.4-3b):
	class A {  };
	void (*pf1)(); // no exception specification
	void (*pf2)() throw(A);
	void f()
	{
	pf1 = pf2; // OK: pf1 is less restrictive
	pf2 = pf1; // error: pf2 is more restrictive
	}
	 --End Example]
	 */
	public void test15_4s3b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class A {  };\n"); //$NON-NLS-1$
		buffer.append("void (*pf1)(); // no exception specification\n"); //$NON-NLS-1$
		buffer.append("void (*pf2)() throw(A);\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("pf1 = pf2; // OK: pf1 is less restrictive\n"); //$NON-NLS-1$
		buffer.append("pf2 = pf1; // error: pf2 is more restrictive\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 15.4-8):
	class X { };
	class Y { };
	class Z: public X { };
	class W { };
	void f() throw (X, Y)
	{
	int n = 0;
	if (n) throw X(); // OK
	if (n) throw Z(); // also OK
	throw W(); // will call unexpected()
	}
	 --End Example]
	 */
	public void test15_4s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class X { };\n"); //$NON-NLS-1$
		buffer.append("class Y { };\n"); //$NON-NLS-1$
		buffer.append("class Z: public X { };\n"); //$NON-NLS-1$
		buffer.append("class W { };\n"); //$NON-NLS-1$
		buffer.append("void f() throw (X, Y)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int n = 0;\n"); //$NON-NLS-1$
		buffer.append("if (n) throw X(); // OK\n"); //$NON-NLS-1$
		buffer.append("if (n) throw Z(); // also OK\n"); //$NON-NLS-1$
		buffer.append("throw W(); // will call unexpected()\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 15.4-10):
	 extern void f() throw(X, Y);
	 void g() throw(X)
	 {
	  f(); //OK
	 }
	 --End Example]
	 */
	public void test15_4s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		 buffer.append("extern void f() throw(X, Y);\n"); //$NON-NLS-1$
		 buffer.append("void g() throw(X)\n"); //$NON-NLS-1$
		 buffer.append("{\n"); //$NON-NLS-1$
		 buffer.append("// f(); //OK\n"); //$NON-NLS-1$
		 buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 15.4-13):
	struct A {
	A();
	A(const A&) throw();
	~A() throw(X);
	};
	struct B {
	B() throw();
	B(const B&) throw();
	~B() throw(Y);
	};
	struct D : public A, public B {
	// Implicit declaration of D::D();
	// Implicit declaration of D::D(const D&) throw();
	// Implicit declaration of D::~D() throw (X,Y);
	};
	 --End Example]
	 */
	public void test15_4s13() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("A();\n"); //$NON-NLS-1$
		buffer.append("A(const A&) throw();\n"); //$NON-NLS-1$
		buffer.append("~A() throw(X);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("B() throw();\n"); //$NON-NLS-1$
		buffer.append("B(const B&) throw();\n"); //$NON-NLS-1$
		buffer.append("~B() throw(Y);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D : public A, public B {\n"); //$NON-NLS-1$
		buffer.append("// Implicit declaration of D::D();\n"); //$NON-NLS-1$
		buffer.append("// Implicit declaration of D::D(const D&) throw();\n"); //$NON-NLS-1$
		buffer.append("// Implicit declaration of D::~D() throw (X,Y);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 16.2-8):
	#if VERSION = = 1
	#define INCFILE "vers1.h"
	#elif VERSION = = 2
	#define INCFILE "vers2.h" // and so on
	#else
	#define INCFILE "versN.h"
	#endif
	 --End Example]
	 */
	public void test16_2s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#if VERSION = = 1\n"); //$NON-NLS-1$
		buffer.append("#define INCFILE \"vers1.h\"\n"); //$NON-NLS-1$
		buffer.append("#elif VERSION = = 2\n"); //$NON-NLS-1$
		buffer.append("#define INCFILE \"vers2.h\" // and so on\n"); //$NON-NLS-1$
		buffer.append("#else\n"); //$NON-NLS-1$
		buffer.append("#define INCFILE \"versN.h\"\n"); //$NON-NLS-1$
		buffer.append("#endif\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	// second pass of C++ spec is to get [Note: ]
	/**
	 [--Start Example(CPP 1.9-15):
	 int f() {
	 int a, b;
	 a = a + 32760 + b + 5;
	 a = (((a + 32760) + b) + 5);
	 a = ((a + b) + 32765);
	 a = ((a + 32765) + b);
	 a = (a + (b + 32765));
	 }
	 --End Example]
	 */
	public void test18_2_1_5s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		 buffer.append("int f() {\n"); //$NON-NLS-1$
		 buffer.append("int a, b;\n"); //$NON-NLS-1$
		 buffer.append("/*...*/\n"); //$NON-NLS-1$
		 buffer.append("a = a + 32760 + b + 5;\n"); //$NON-NLS-1$
		 buffer.append("a = (((a + 32760) + b) + 5);\n"); //$NON-NLS-1$
		 buffer.append("a = ((a + b) + 32765);\n"); //$NON-NLS-1$
		 buffer.append("a = ((a + 32765) + b);\n"); //$NON-NLS-1$
		 buffer.append("a = (a + (b + 32765));\n"); //$NON-NLS-1$
		 buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.3.1-4):
	struct X {
	enum E { z = 16 };
	int b[X::z]; // OK
	};
	 --End Example]
	 */
	public void test3_3_1s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct X {\n"); //$NON-NLS-1$
		buffer.append("enum E { z = 16 };\n"); //$NON-NLS-1$
		buffer.append("int b[X::z]; // OK\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.4.1-3):
	typedef int f;
	struct A {
	friend void f(A &);
	operator int();
	void g(A a) {
	f(a);
	}
	};
	 --End Example]
	 */
	public void test3_4_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int f;\n"); //$NON-NLS-1$
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("friend void f(A &);\n"); //$NON-NLS-1$
		buffer.append("operator int();\n"); //$NON-NLS-1$
		buffer.append("void g(A a) {\n"); //$NON-NLS-1$
		buffer.append("f(a);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.4.5-4):
	struct A {
	int a;
	};
	struct B: virtual A { };
	struct C: B { };
	struct D: B { };
	struct E: public C, public D { };
	struct F: public A { };
	void f() {
	E e;
	e.B::a = 0; // OK, only one A::a in E
	F f;
	f.A::a = 1; // OK, A::a is a member of F
	}
	 --End Example]
	 */
	public void test3_4_5s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct B: virtual A { };\n"); //$NON-NLS-1$
		buffer.append("struct C: B { };\n"); //$NON-NLS-1$
		buffer.append("struct D: B { };\n"); //$NON-NLS-1$
		buffer.append("struct E: public C, public D { };\n"); //$NON-NLS-1$
		buffer.append("struct F: public A { };\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("E e;\n"); //$NON-NLS-1$
		buffer.append("e.B::a = 0; // OK, only one A::a in E\n"); //$NON-NLS-1$
		buffer.append("F f;\n"); //$NON-NLS-1$
		buffer.append("f.A::a = 1; // OK, A::a is a member of F\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 3.6.2-2):
	inline double fd() { return 1.0; }
	extern double d1;
	double d2 = d1; // unspecified:
	// may be statically initialized to 0.0 or
	// dynamically initialized to 1.0
	double d1 = fd(); // may be initialized statically to 1.0
	 --End Example]
	 */
	public void test3_6_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("inline double fd() { return 1.0; }\n"); //$NON-NLS-1$
		buffer.append("extern double d1;\n"); //$NON-NLS-1$
		buffer.append("double d2 = d1; // unspecified:\n"); //$NON-NLS-1$
		buffer.append("// may be statically initialized to 0.0 or\n"); //$NON-NLS-1$
		buffer.append("// dynamically initialized to 1.0\n"); //$NON-NLS-1$
		buffer.append("double d1 = fd(); // may be initialized statically to 1.0\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 4.4-4):
	int main() {
	const char c = 'c';
	char* pc;
	const char** pcc = &pc; //1: not allowed
	*pcc = &c;
	*pc = 'C'; //2: modifies a const object
	}
	 --End Example]
	 */
	public void test4_4s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int main() {\n"); //$NON-NLS-1$
		buffer.append("const char c = 'c';\n"); //$NON-NLS-1$
		buffer.append("char* pc;\n"); //$NON-NLS-1$
		buffer.append("const char** pcc = &pc; //1: not allowed\n"); //$NON-NLS-1$
		buffer.append("*pcc = &c;\n"); //$NON-NLS-1$
		buffer.append("*pc = 'C'; //2: modifies a const object\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 5.5-5):
	struct S {
	mutable int i;
	};
	int f() {
	const S cs;
	int S::* pm = &S::i; // pm refers to mutable member S::i
	cs.*pm = 88; // illformed: cs is a const object
	}
	 --End Example]
	 */
	public void test5_5s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct S {\n"); //$NON-NLS-1$
		buffer.append("mutable int i;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("const S cs;\n"); //$NON-NLS-1$
		buffer.append("int S::* pm = &S::i; // pm refers to mutable member S::i\n"); //$NON-NLS-1$
		buffer.append("cs.*pm = 88; // illformed: cs is a const object\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 7.3.4-4):
	namespace A {
	class X { };
	extern "C" int g();
	extern "C++" int h();
	}
	namespace B {
	void X(int);
	extern "C" int g();
	extern "C++" int h();
	}
	using namespace A;
	using namespace B;
	void f() {
	X(1); //error: name X found in two namespaces
	g(); //okay: name g refers to the same entity
	h(); //error: name h found in two namespaces
	}
	 --End Example]
	 */
	public void test7_3_4s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace A {\n"); //$NON-NLS-1$
		buffer.append("class X { };\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" int g();\n"); //$NON-NLS-1$
		buffer.append("extern \"C++\" int h();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace B {\n"); //$NON-NLS-1$
		buffer.append("void X(int);\n"); //$NON-NLS-1$
		buffer.append("extern \"C\" int g();\n"); //$NON-NLS-1$
		buffer.append("extern \"C++\" int h();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("using namespace A;\n"); //$NON-NLS-1$
		buffer.append("using namespace B;\n"); //$NON-NLS-1$
		buffer.append("void f() {\n"); //$NON-NLS-1$
		buffer.append("X(1); //error: name X found in two namespaces\n"); //$NON-NLS-1$
		buffer.append("g(); //okay: name g refers to the same entity\n"); //$NON-NLS-1$
		buffer.append("h(); //error: name h found in two namespaces\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
		
	/**
	 [--Start Example(CPP 8.4-5):
	void print(int a, int)
	{
	//printf("a = %d\n",a);
	}
	 --End Example]
	 */
	public void test8_4s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void print(int a, int)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("//printf(\"a = %d\",a);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 8.5-14):
	int a;
	const int b = a;
	int c = b;
	 --End Example]
	 */
	public void test8_5s14() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int a;\n"); //$NON-NLS-1$
		buffer.append("const int b = a;\n"); //$NON-NLS-1$
		buffer.append("int c = b;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 10.3-3):
	struct B {
	virtual void f();
	};
	struct D : B {
	void f(int);
	};
	struct D2 : D {
	void f();
	};
	 --End Example]
	 */
	public void test10_3s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("virtual void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D : B {\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D2 : D {\n"); //$NON-NLS-1$
		buffer.append("void f();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 12.5-7a):
	struct B {
	virtual ~B();
	void operator delete(void*, size_t);
	};
	struct D : B {
	void operator delete(void*);
	};
	void f()
	{
	B* bp = new D;
	delete bp; //1: uses D::operator delete(void*)
	}
	 --End Example]
	 */
	public void test12_5s7a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("virtual ~B();\n"); //$NON-NLS-1$
		buffer.append("void operator delete(void*, size_t);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D : B {\n"); //$NON-NLS-1$
		buffer.append("void operator delete(void*);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("B* bp = new D;\n"); //$NON-NLS-1$
		buffer.append("delete bp; //1: uses D::operator delete(void*)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 12.5-7b):
	struct B {
	virtual ~B();
	void operator delete[](void*, size_t);
	};
	struct D : B {
	void operator delete[](void*, size_t);
	};
	void f(int i)
	{
	D* dp = new D[i];
	delete [] dp; // uses D::operator delete[](void*, size_t)
	B* bp = new D[i];
	delete[] bp; // undefined behavior
	}
	 --End Example]
	 */
	public void test12_5s7b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("virtual ~B();\n"); //$NON-NLS-1$
		buffer.append("void operator delete[](void*, size_t);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D : B {\n"); //$NON-NLS-1$
		buffer.append("void operator delete[](void*, size_t);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void f(int i)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("D* dp = new D[i];\n"); //$NON-NLS-1$
		buffer.append("delete [] dp; // uses D::operator delete[](void*, size_t)\n"); //$NON-NLS-1$
		buffer.append("B* bp = new D[i];\n"); //$NON-NLS-1$
		buffer.append("delete[] bp; // undefined behavior\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 13.3.1.2-10):
	struct A { };
	void operator + (A, A);
	struct B {
	void operator + (B);
	void f ();
	};
	A a;
	void B::f() {
	operator+ (a,a); // ERROR - global operator hidden by member
	a + a; // OK - calls global operator+
	}
	 --End Example]
	 */
	public void test13_3_1_2s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct A { };\n"); //$NON-NLS-1$
		buffer.append("void operator + (A, A);\n"); //$NON-NLS-1$
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("void operator + (B);\n"); //$NON-NLS-1$
		buffer.append("void f ();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("A a;\n"); //$NON-NLS-1$
		buffer.append("void B::f() {\n"); //$NON-NLS-1$
		buffer.append("operator+ (a,a); // ERROR - global operator hidden by member\n"); //$NON-NLS-1$
		buffer.append("a + a; // OK - calls global operator+\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.1-2):
	template<class T> class myarray {  };
	template<class K, class V, template<class T> class C = myarray>
	class Map {
	C<K> key;
	C<V> value;
	// ...
	};
	 --End Example]
	 */
	public void test14_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class myarray {  };\n"); //$NON-NLS-1$
		buffer.append("template<class K, class V, template<class T> class C = myarray>\n"); //$NON-NLS-1$
		buffer.append("class Map {\n"); //$NON-NLS-1$
		buffer.append("C<K> key;\n"); //$NON-NLS-1$
		buffer.append("C<V> value;\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.1-3):
	class T {  };
	int i;
	template<class T, T i> void f(T t)
	{
	T t1 = i; // templateparameters T and i
	::T t2 = ::i; // global namespace members T and i
	}
	 --End Example]
	 */
	public void test14_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class T {  };\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("template<class T, T i> void f(T t)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("T t1 = i; // templateparameters T and i\n"); //$NON-NLS-1$
		buffer.append("::T t2 = ::i; // global namespace members T and i\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.3.2-3):
	template<int* p> class X { };
	int a[10];
	struct S { int m; static int s; } s;
	X<&a[2]> x3; // error: address of array element
	X<&s.m> x4; // error: address of nonstatic member
	X<&s.s> x5; // error: &S::s must be used
	X<&S::s> x6; // OK: address of static member
	 --End Example]
	 */
	public void test14_3_2s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<int* p> class X { };\n"); //$NON-NLS-1$
		buffer.append("int a[10];\n"); //$NON-NLS-1$
		buffer.append("struct S { int m; static int s; } s;\n"); //$NON-NLS-1$
		buffer.append("X<&a[2]> x3; // error: address of array element\n"); //$NON-NLS-1$
		buffer.append("X<&s.m> x4; // error: address of nonstatic member\n"); //$NON-NLS-1$
		buffer.append("X<&s.s> x5; // error: &S::s must be used\n"); //$NON-NLS-1$
		buffer.append("X<&S::s> x6; // OK: address of static member\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.3.2-2):
	template<class T, char* p> class X {
	// ...
	X();
	X(const char* q) {  }
	};
	X<int,"Studebaker"> x1; // error: string literal as template argument
	char p[] = "Vivisectionist";
	X<int,p> x2; // OK
	 --End Example]
	 */
	public void test14_3_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T, char* p> class X {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("X();\n"); //$NON-NLS-1$
		buffer.append("X(const char* q) {  }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("X<int,\"Studebaker\"> x1; // error: string literal as template argument\n"); //$NON-NLS-1$
		buffer.append("char p[] = \"Vivisectionist\";\n"); //$NON-NLS-1$
		buffer.append("X<int,p> x2; // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
	}
	
	/**
	 [--Start Example(CPP 14.3.2-4):
	template<const int& CRI> struct B {  };
	B<1> b2; // error: temporary would be required for template argument
	int c = 1;
	B<c> b1; // OK
	 --End Example]
	 */
	public void test14_3_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<const int& CRI> struct B { /* ... */ };\n"); //$NON-NLS-1$
		buffer.append("B<1> b2; // error: temporary would be required for template argument\n"); //$NON-NLS-1$
		buffer.append("int c = 1;\n"); //$NON-NLS-1$
		buffer.append("B<c> b1; // OK\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
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
	public void test14_3_2s5() throws Exception  {
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

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.2-2):
	template <class T> struct A {
	void f(int);
	template <class T2> void f(T2);
	};
	template <> void A<int>::f(int) { } // nontemplate member
	template <> template <> void A<int>::f<>(int) { } // template member
	int main()
	{
	A<char> ac;
	ac.f(1); //nontemplate
	ac.f('c'); //template
	ac.f<>(1); //template
	}
	 --End Example]
	 */
	public void test14_5_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> struct A {\n"); //$NON-NLS-1$
		buffer.append("void f(int);\n"); //$NON-NLS-1$
		buffer.append("template <class T2> void f(T2);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template <> void A<int>::f(int) { } // nontemplate member\n"); //$NON-NLS-1$
		buffer.append("template <> template <> void A<int>::f<>(int) { } // template member\n"); //$NON-NLS-1$
		buffer.append("int main()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("A<char> ac;\n"); //$NON-NLS-1$
		buffer.append("ac.f(1); //nontemplate\n"); //$NON-NLS-1$
		buffer.append("ac.f('c'); //template\n"); //$NON-NLS-1$
		buffer.append("ac.f<>(1); //template\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);  //should be 0
	}
	
	/**
	 [--Start Example(CPP 14.5.4-5):
	template<class T1, class T2, int I> class A<T1, T2, I> { }; // error
	 --End Example]
	 */
	public void test14_5_4s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T1, class T2, int I> class A<T1, T2, I> { }; // error\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, false, 0);
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
	public void test14_5_4s6() throws Exception {
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

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
	public void test14_5_4s7() throws Exception {
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

		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.5.5.1-4):
	template<class T> void f();
	template<int I> void f(); // OK: overloads the first template
	// distinguishable with an explicit template argument list
	 --End Example]
	 */
	public void test14_5_5_1s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f();\n"); //$NON-NLS-1$
		buffer.append("template<int I> void f(); // OK: overloads the first template\n"); //$NON-NLS-1$
		buffer.append("// distinguishable with an explicit template argument list\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 14.8.1-2b):
	template <class T> int f(T); // #1
	int f(int); // #2
	int k = f(1); // uses #2
	int l = f<>(1); // uses #1
	 --End Example]
	 */
	public void test14_8_1s2b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> int f(T); // #1\n"); //$NON-NLS-1$
		buffer.append("int f(int); // #2\n"); //$NON-NLS-1$
		buffer.append("int k = f(1); // uses #2\n"); //$NON-NLS-1$
		buffer.append("int l = f<>(1); // uses #1\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
	}
	
	/**
	 [--Start Example(CPP 16.3.5-3):
	#define TABSIZE 100
	int table[TABSIZE];
	 --End Example]
	 */
	public void test15_3_5s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define TABSIZE 100\n"); //$NON-NLS-1$
		buffer.append("int table[TABSIZE];\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
    public void test8_5_3s1()  throws Exception { // TODO raised bug 90648
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
        parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
     * @throws ParserException 
     */
    public void test12s1() throws ParserException  { 
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A { }; // implicitlydeclared A::operator=\n"); //$NON-NLS-1$
        buffer.append("struct B : A {\n"); //$NON-NLS-1$
        buffer.append("B& operator=(const B &);\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("B& B::operator=(const B& s) {\n"); //$NON-NLS-1$
        buffer.append("this->A::operator=(s); // wellformed\n"); //$NON-NLS-1$
        buffer.append("return *this;\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
     * @throws ParserException 
     */
    public void test12_7s2() throws ParserException  { 
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
        parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
    public void test3_3_6s5() throws Exception { // 90606
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
        parse(buffer.toString(), ParserLanguage.CPP, true, 0);
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
     * @throws ParserException 
     */
    public void test13_4s5a() throws ParserException  { // bug 90674
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
        parse(buffer.toString(), ParserLanguage.CPP, false, 0);
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
        parse(buffer.toString(), ParserLanguage.CPP, true, 0);
    }   
}
