/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Examples taken from the c++-specification.
 */
public class AST2CPPSpecTest extends AST2SpecBaseTest {

	public AST2CPPSpecTest() {
	}

	public AST2CPPSpecTest(String name) {
		super(name);
	}
	
	public static TestSuite suite() {
		return suite(AST2CPPSpecTest.class);
	}

	// int x=x+++++y;
	public void test2_4s5() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// int a=12, b=014, c=0XC;
	public void test2_13_1s1() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// ??=define arraycheck(a,b) a??(b??) ??!??! b??(a??)
	// // becomes
	// #define arraycheck(a,b) a[b] || b[a]
	public void test2_3s2() throws Exception { // TODO exists bug 64993
		parseCandCPP(getAboveComment(), true, 0);
	}

	// int a; // defines a
	// extern const int c = 1; // defines c
	// int f(int x) { return x+a; } // defines f and defines x
	// struct S { int a; int b; }; // defines S, S::a, and S::b
	// struct X { // defines X
	// int x; // defines nonstatic data member x
	// static int y; // declares static data member y
	// X(): x(0) { } // defines a constructor of X
	// };
	// int X::y = 1; // defines X::y
	// enum { up, down }; // defines up and down
	// namespace N { int d; } // defines N and N::d
	// namespace N1 = N; // defines N1
	// X anX; // defines anX
	// // whereas these are just declarations:
	// extern int a; // declares a
	// extern const int c; // declares c
	// int f(int); // declares f
	// struct S; // declares S
	// typedef int Int; // declares Int
	// extern X anotherX; // declares anotherX
	// using N::d; // declares N::d
	public void test3_1s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct C {
	// string s; // string is the standard library class (clause 21)
	// };
	// int main()
	// {
	// C a;
	// C b = a;
	// b = a;
	// }
	public void test3_1s4a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct C {
	// string s;
	// C(): s() { }
	// C(const C& x): s(x.s) { }
	// C& operator=(const C& x) { s = x.s; return *this; }
	// ~C() { }
	// };
	// 
	public void test3_1s4b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct X; // declare X as a struct type
	// struct X* x1; // use X in pointer formation
	// X* x2; // use X in pointer formation
	public void test3_2s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// // translation unit 1:
	// struct X {
	// X(int);
	// X(int, int);
	// };
	// X::X(int = 0) { }
	// class D: public X { };
	// D d2; // X(int) called by D()
	public void test3_2s5_a() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}
	
	// // translation unit 2:
	// struct X {
	// X(int);
	// X(int, int);
	// };
	// X::X(int = 0, int = 0) { }
	// class D: public X { }; // X(int, int) called by D();
	// // D()'s implicit definition
	// // violates the ODR
	public void test3_2s5_b() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int j = 24;
	// int main()
	// {
	// int i = j, j;
	// j = 42;
	// }
	public void test3_3s2() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// int foo() {
	// int x = 12;
	// { int x = x; }
	// }
	public void test3_3_1s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// const int i = 2;
	// { int i[i]; }
	// }
	public void test3_3_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// const int x = 12;
	// { enum { x = x }; }
	// }
	public void test3_3_1s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace N {
	// int i;
	// int g(int a) { return a; }
	// int j();
	// void q();
	// }
	// namespace { int l=1; }
	// // the potential scope of l is from its point of declaration
	// // to the end of the translation unit
	// namespace N {
	// int g(char a) // overloads N::g(int)
	// {
	// return l+a; // l is from unnamed namespace
	// }
	// int i; // error: duplicate definition
	// int j(); // OK: duplicate function declaration
	// int j() // OK: definition of N::j()
	// {
	// return g(i); // calls N::g(int)
	// }
	// int q(); // error: different return type
	// }
	public void test3_3_5s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace A {
	// namespace N {
	// void f();
	// }
	// }
	// void A::N::f() {
	// int i = 5;
	// // The following scopes are searched for a declaration of i:
	// // 1) outermost block scope of A::N::f, before the use of i
	// // 2) scope of namespace N
	// // 3) scope of namespace A
	// // 4) global scope, before the definition of A::N::f
	// }
	public void test3_4_1s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace M {
	// class B { };
	// }
	public void test3_4_1s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class B { };
	// namespace M {
	// namespace N {
	// class X : public B {
	// void f();
	// };
	// }
	// }
	// void M::N::X::f() {
	// int i = 16;
	// }
	// // The following scopes are searched for a declaration of i:
	// // 1) outermost block scope of M::N::X::f, before the use of i
	// // 2) scope of class M::N::X
	// // 3) scope of M::N::X's base class B
	// // 4) scope of namespace M::N
	// // 5) scope of namespace M
	// // 6) global scope, before the definition of M::N::X::f
	public void test3_4_1s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// typedef int AT;
	// void f1(AT);
	// void f2(float);
	// };
	// struct B {
	// typedef float BT;
	// friend void A::f1(AT); // parameter type is A::AT
	// friend void A::f2(BT); // parameter type is B::BT
	// };
	public void test3_4_1s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	//	class A {
	//	public:
	//	static int n;
	//	};
	//	int main()
	//	{
	//	int A;
	//	A::n = 42; // OK
	//	A b; // illformed: A does not name a type
	//	}
	public void test3_4_3s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 1);
	}
	
	// namespace NS {
	// class T { };
	// void f(T);
	// }
	// NS::T parm;
	// int main() {
	// f(parm); //OK: calls NS::f
	// }
	public void test3_4_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X { };
	// class C {
	// class X { };
	// static const int number = 50;
	// static X arr[number];
	// };
	// X C::arr[number]; // illformed:
	// // equivalent to: ::X C::arr[C::number];
	// // not to: C::X C::arr[C::number];
	public void test3_4_3s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct C {
	// typedef int I;
	// };
	// typedef int I1, I2;
	// int foo() {
	// extern int* p;
	// extern int* q;
	// p->C::I::~I(); // I is looked up in the scope of C
	// q->I1::~I2(); // I2 is looked up in the scope of
	// };
	// // the postfixexpression
	// struct A {
	// ~A();
	// };
	// typedef A AB;
	// int main()
	// {
	// AB *p;
	// p->AB::~AB(); // explicitly calls the destructor for A
	// }
	public void test3_4_3s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// int x;
	// namespace Y {
	// void f(float);
	// void h(int);
	// }
	// namespace Z {
	// void h(double);
	// }
	// namespace A {
	// using namespace Y;
	// void f(int);
	// void g(int);
	// int i;
	// }
	// namespace B {
	// using namespace Z;
	// void f(char);
	// int i;
	// }
	// namespace AB {
	// using namespace A;
	// using namespace B;
	// void g();
	// }
	// void h()
	// {
	// AB::g(); // g is declared directly in AB,
	// // therefore S is { AB::g() } and AB::g() is chosen
	// AB::f(1); // f is not declared directly in AB so the rules are
	// // applied recursively to A and B;
	// // namespace Y is not searched and Y::f(float)
	// // is not considered;
	// // S is { A::f(int), B::f(char) } and overload
	// // resolution chooses A::f(int)
	// AB::f('c'); //as above but resolution chooses B::f(char)
	// AB::x++; // x is not declared directly in AB, and
	// // is not declared in A or B, so the rules are
	// // applied recursively to Y and Z,
	// // S is { } so the program is illformed
	// AB::i++; // i is not declared directly in AB so the rules are
	// // applied recursively to A and B,
	// // S is { A::i, B::i } so the use is ambiguous
	// // and the program is illformed
	// AB::h(16.8); // h is not declared directly in AB and
	// // not declared directly in A or B so the rules are
	// // applied recursively to Y and Z,
	// // S is { Y::h(int), Z::h(double) } and overload
	// // resolution chooses Z::h(double)
	// }
	public void test3_4_3_2s2() throws Exception {
		String[] problems= {"AB::x", "x", "AB::i", "i"}; 
		parse(getAboveComment(), ParserLanguage.CPP, problems);  // qualified names are counted double, so 4 
	}

	// namespace A {
	// int a;
	// }
	// namespace B {
	// using namespace A;
	// }
	// namespace C {
	// using namespace A;
	// }
	// namespace BC {
	// using namespace B;
	// using namespace C;
	// }
	// void f()
	// {
	// BC::a++; //OK: S is { A::a, A::a }
	// }
	// namespace D {
	// using A::a;
	// }
	// namespace BD {
	// using namespace B;
	// using namespace D;
	// }
	// void g()
	// {
	// BD::a++; //OK: S is { A::a, A::a }
	// }
	public void test3_4_3_2s3() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace B {
	// int b;
	// }
	// namespace A {
	// using namespace B;
	// int a;
	// }
	// namespace B {
	// using namespace A;
	// }
	// void f()
	// {
	// A::a++; //OK: a declared directly in A, S is { A::a }
	// B::a++; //OK: both A and B searched (once), S is { A::a }
	// A::b++; //OK: both A and B searched (once), S is { B::b }
	// B::b++; //OK: b declared directly in B, S is { B::b }
	// }
	public void test3_4_3_2s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace A {
	// struct x { };
	// int x;
	// int y;
	// }
	// namespace B {
	// struct y {};
	// }
	// namespace C {
	// using namespace A;
	// using namespace B;
	// int i = C::x; // OK, A::x (of type int)
	// int j = C::y; // ambiguous, A::y or B::y
	// }
	public void test3_4_3_2s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace A {
	// namespace B {
	// void f1(int);
	// }
	// using namespace B;
	// }
	// void A::f1(int) { } // illformed,
	// // f1 is not a member of A
	public void test3_4_3_2s6a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace A {
	// namespace B {
	// void f1(int);
	// }
	// }
	// namespace C {
	// namespace D {
	// void f1(int);
	// }
	// }
	// using namespace A;
	// using namespace C::D;
	// void B::f1(int){} // OK, defines A::B::f1(int)
	public void test3_4_3_2s6b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct Node {
	// struct Node* Next; // OK: Refers to Node at global scope
	// struct Data* Data; // OK: Declares type Data
	// // at global scope and member Data
	// };
	// struct Data {
	// struct Node* Node; // OK: Refers to Node at global scope
	// friend struct ::Glob; // error: Glob is not declared
	// // cannot introduce a qualified type (7.1.5.3)
	// friend struct Glob; // OK: Refers to (as yet) undeclared Glob
	// // at global scope.
	// 
	// };
	// struct Base {
	// struct Data; // OK: Declares nested Data
	// struct ::Data* thatData; // OK: Refers to ::Data
	// struct Base::Data* thisData; // OK: Refers to nested Data
	// friend class ::Data; // OK: global Data is a friend
	// friend class Data; // OK: nested Data is a friend
	// struct Data { //
	// }; // Defines nested Data
	// struct Data; // OK: Redeclares nested Data
	// };
	// struct Data; // OK: Redeclares Data at global scope
	// struct ::Data; // error: cannot introduce a qualified type (7.1.5.3)
	// struct Base::Data; // error: cannot introduce a qualified type (7.1.5.3)
	// struct Base::Datum; // error: Datum undefined
	// struct Base::Data* pBase; // OK: refers to nested Data
	public void test3_4_4s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// static void f();
	// static int i = 0; //1
	// void g() {
	// extern void f(); // internal linkage
	// int i; //2: i has no linkage
	// {
	// extern void f(); // internal linkage
	// extern int i; //3: external linkage
	// }
	// }
	public void test3_5s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace X {
	// void p()
	// {
	// q(); //error: q not yet declared
	// extern void q(); // q is a member of namespace X
	// }
	// void middle()
	// {
	// q(); //error: q not yet declared
	// }
	// void q() { //
	// } // definition of X::q
	// }
	// void q() { //
	// } // some other, unrelated q
	public void test3_5s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// void f()
	// {
	// struct A { int x; }; // no linkage
	// extern A a; // illformed
	// typedef A B;
	// extern B b; // illformed
	// }
	public void test3_5s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int main(int argc, char* argv[]) { //
	// }
	public void test3_6_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct B {
	// virtual void f();
	// void mutate();
	// virtual ~B();
	// };
	// struct D1 : B { void f(); };
	// struct D2 : B { void f(); };
	// void B::mutate() {
	// new (this) D2; // reuses storage - ends the lifetime of *this
	// f(); //undefined behavior
	// this; // OK, this points to valid memory
	// }
	// void g() {
	// void* p = malloc(sizeof(D1) + sizeof(D2));
	// B* pb = new (p) D1;
	// pb->mutate();
	// &pb; //OK: pb points to valid memory
	// void* q = pb; // OK: pb points to valid memory
	// pb->f(); //undefined behavior, lifetime of *pb has ended
	// }
	public void test3_8s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct C {
	// int i;
	// void f();
	// const C& operator=( const C& );
	// };
	// const C& C::operator=( const C& other)
	// {
	// if ( this != &other ) {
	// this->~C(); //lifetime of *this ends
	// new (this) C(other); // new object of type C created
	// f(); //welldefined
	// }
	// return *this;
	// }
	// int foo() {
	// C c1;
	// C c2;
	// c1 = c2; // welldefined
	// c1.f(); //welldefined; c1 refers to a new object of type C
	// }
	public void test3_8s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class T { };
	// struct B {
	// ~B();
	// };
	// void h() {
	// B b;
	// new (&b) T;
	// } //undefined behavior at block exit
	public void test3_8s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct B {
	// B();
	// ~B();
	// };
	// const B b;
	// void h() {
	// b.~B();
	// new (&b) const B; // undefined behavior
	// }
	public void test3_8s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// #define N sizeof(T)
	// void test() {
	// char buf[N];
	// T obj; // obj initialized to its original value
	// memcpy(buf, &obj, N); // between these two calls to memcpy,
	// // obj might be modified
	// memcpy(&obj, buf, N); // at this point, each subobject of obj of scalar type
	// // holds its original value
	// }
	public void test3_9s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// void test() {
	// T* t1p;
	// T* t2p;
	// // provided that t2p points to an initialized object ...
	// memcpy(t1p, t2p, sizeof(T)); // at this point, every subobject of POD type in *t1p contains
	// // the same value as the corresponding subobject in *t2p
	// }
	public void test3_9s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	//	class X; // X is an incomplete type
	//	extern X* xp; // xp is a pointer to an incomplete type
	//	extern int arr[]; // the type of arr is incomplete
	//	typedef int UNKA[]; // UNKA is an incomplete type
	//	UNKA* arrp; // arrp is a pointer to an incomplete type
	//	UNKA** arrpp;
	//	void foo() {
	//		xp++; //ill-formed: X is incomplete
	//		arrp++; //ill-formed: incomplete type
	//		arrpp++; //OK: sizeof UNKA* is known
	//	}
	//	struct X {
	//		int i; 
	//	}; // now X is a complete type
	//	int arr[10]; // now the type of arr is complete
	//	X x;
	//	void bar() {
	//		xp = &x; // OK; type is ''pointer to X''
	//		arrp = &arr; // ill-formed: different types
	//		xp++; //OK: X is complete
	//		arrp++; //ill-formed: UNKA can't be completed
	//	}
	public void test3_9s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int& f();
	public void test3_10s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// i = v[i++]; // the behavior is unspecified
	// i = 7, i++, i++; // i becomes 9
	// i = ++i + 1; // the behavior is unspecified
	// i = i + 1; // the value of i is incremented
	// }
	public void test5s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct B {};
	// struct D : B {};
	// void foo(D* dp)
	// {
	// B* bp = dynamic_cast<B*>(dp); // equivalent to B* bp = dp;
	// }
	public void test5_2_7s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A { virtual void f(); };
	// class B { virtual void g(); };
	// class D : public virtual A, private B {};
	// void g()
	// {
	// D d;
	// B* bp = (B*)&d; // cast needed to break protection
	// A* ap = &d; // public derivation, no cast needed
	// D& dr = dynamic_cast<D&>(*bp); // fails
	// ap = dynamic_cast<A*>(bp); // fails
	// bp = dynamic_cast<B*>(ap); // fails
	// ap = dynamic_cast<A*>(&d); // succeeds
	// bp = dynamic_cast<B*>(&d); // fails
	// }
	// class E : public D, public B {};
	// class F : public E, public D {};
	// void h()
	// {
	// F f;
	// A* ap = &f; // succeeds: finds unique A
	// D* dp = dynamic_cast<D*>(ap); // fails: yields 0
	// // f has two D subobjects
	// E* ep = (E*)ap; // illformed:
	// // cast from virtual base
	// E* ep1 = dynamic_cast<E*>(ap); // succeeds
	// }
	public void test5_2_7s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class D { // ... 
	// };
	// D d1;
	// const D d2;
	// int foo() {
	// typeid(d1) == typeid(d2); // yields true
	// typeid(D) == typeid(const D); // yields true
	// typeid(D) == typeid(d2); // yields true
	// typeid(D) == typeid(const D&); // yields true
	// }
	public void test5_2_8s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct B {};
	// struct D : public B {};
	// D d;
	// B &br = d;
	// int foo() {
	// static_cast<D&>(br); // produces lvalue to the original d object
	// }
	public void test5_2_9s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A { int i; };
	// struct B : A { };
	// int foo() {
	// &B::i; // has type int A::*
	// }
	public void test5_3_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void test() {
	//    new (int (*[10])());
	// };
	public void test5_3_4s3() throws Exception {
		IASTTranslationUnit tu= parse(getAboveComment(), ParserLanguage.CPP, true, 0);
		IASTFunctionDefinition fdef= getDeclaration(tu, 0);
		IASTExpression expr= getExpressionOfStatement(fdef, 0);
		assertInstance(expr, ICPPASTNewExpression.class);
		ICPPASTNewExpression newExpr= (ICPPASTNewExpression) expr;
		
		assertNull(newExpr.getNewPlacement());
		assertNull(newExpr.getNewInitializer());
		IASTTypeId typeid= newExpr.getTypeId();
		isTypeEqual(CPPVisitor.createType(typeid), "int (* [10])()");
	}

	// typedef int T;
	// void test(int f) {
	//	  new T;
	//    new(2,f) T;
	//    new T[5];
	//    new (2,f) T[5];
	// };
	public void test5_3_4s12() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=236856
		
		IASTTranslationUnit tu= parse(getAboveComment(), ParserLanguage.CPP, true, 0);
		IASTFunctionDefinition fdef= getDeclaration(tu, 1);

		// new T;
		IASTExpression expr= getExpressionOfStatement(fdef, 0);
		assertInstance(expr, ICPPASTNewExpression.class);
		ICPPASTNewExpression newExpr= (ICPPASTNewExpression) expr;
		assertNull(newExpr.getNewPlacement());
		assertNull(newExpr.getNewInitializer());
		isTypeEqual(CPPVisitor.createType(newExpr.getTypeId()), "int");
		
		// new(2,f) T;
		expr= getExpressionOfStatement(fdef, 1);
		assertInstance(expr, ICPPASTNewExpression.class);
		newExpr= (ICPPASTNewExpression) expr;
		assertInstance(newExpr.getNewPlacement(), IASTExpressionList.class);
		assertNull(newExpr.getNewInitializer());
		isTypeEqual(CPPVisitor.createType(newExpr.getTypeId()), "int");

		// new T[5];
		expr= getExpressionOfStatement(fdef, 2);
		assertInstance(expr, ICPPASTNewExpression.class);
		newExpr= (ICPPASTNewExpression) expr;
		assertNull(newExpr.getNewPlacement());
		assertNull(newExpr.getNewInitializer());
		isTypeEqual(CPPVisitor.createType(newExpr.getTypeId()), "int [5]");

		// new (2,f) T[5];
		expr= getExpressionOfStatement(fdef, 3);
		assertInstance(expr, ICPPASTNewExpression.class);
		newExpr= (ICPPASTNewExpression) expr;
		assertInstance(newExpr.getNewPlacement(), IASTExpressionList.class);
		assertNull(newExpr.getNewInitializer());
		isTypeEqual(CPPVisitor.createType(newExpr.getTypeId()), "int [5]");
	}

	// int n=2;
	// int x=new float[n][5];
	// int y=new float[5][n];
	public void test5_3_4s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {};
	// struct I1 : A {};
	// struct I2 : A {};
	// struct D : I1, I2 {};
	// A *foo( D *p ) {
	// return (A*)( p ); // illformed
	// // static_cast interpretation
	// }
	public void test5_4s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// (ptr_to_obj->*ptr_to_mfct)(10);
	// }
	public void test5_5s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// void *p;
	// const int *q;
	// int **pi;
	// const int *const *pci;
	// void ct()
	// {
	// p <= q; // Both converted to const void * before comparison
	// pi <= pci; // Both converted to const int *const * before comparison
	// }
	public void test5_9s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct B {
	// int f();
	// };
	// struct L : B { };
	// struct R : B { };
	// struct D : L, R { };
	// int (B::*pb)() = &B::f;
	// int (L::*pl)() = pb;
	// int (R::*pr)() = pb;
	// int (D::*pdl)() = pl;
	// int (D::*pdr)() = pr;
	// bool x = (pdl == pdr); // false
	public void test5_10s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int f(int, int, int) {}
	// int foo() {
	// int a=0, t=1, c=2;
	// f(a, (t=3, t+2), c);
	// }
	public void test5_18s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// int x=0;
	// if (x)
	// int i;
	// 
	// if (x) {
	// int i;
	// }
	// }
	public void test6_4s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// if (int x = 1) {
	// int x; // illformed,redeclaration of x
	// }
	// else {
	// int x; // illformed,redeclaration of x
	// }
	// }
	public void test6_4s3() throws Exception { 
		// raised bug 90618
		// gcc does not report an error, either, so leave it as it is.
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// int x=5;
	// while (--x >= 0)
	// int i;
	// //can be equivalently rewritten as
	// while (--x >= 0) {
	// int i;
	// }
	// }
	public void test6_5s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// int val;
	// A(int i) : val(i) { }
	// ~A() { }
	// operator bool() { return val != 0; }
	// };
	// 
	// int foo() {
	// int i = 1;
	// while (A a = i) {
	// //...
	// i = 0;
	// }
	// }
	public void test6_5_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// int i = 42;
	// int a[10];
	// for (int i = 0; i < 10; i++)
	// a[i] = i;
	// int j = i; // j = 42
	// }
	public void test6_5_3s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	//	void f()
	//	{
	//	// ...
	//	goto lx; // illformed: jump into scope of a
	//	// ...
	//	ly:
	//	X a = 1; // X is undefined
	//	// ...
	//	lx:
	//	goto ly; // OK, jump implies destructor
	//	// call for a followed by construction
	//	// again immediately following label ly
	//	} 
	public void test6_7s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 1);
	}

	// int foo(int i)
	// {
	// static int s = foo(2*i); // recursive call - undefined
	// return i+1;
	// }
	public void test6_7s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// T(a)->m = 7; // expressionstatement
	// T(a)++; //expressionstatement
	// T(a,5)<<c; //expressionstatement
	// T(*d)(int); //declaration
	// T(e)[5]; //declaration
	// T(f) = { 1, 2 }; // declaration
	// T(*g)(double(3)); // declaration
	// }
	public void test6_8s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class T {
	// // ...
	// public:
	// T();
	// T(int);
	// T(int, int);
	// };
	// T(a); //declaration
	// T(*b)(); //declaration
	// T(c)=7; //declaration
	// T(d),e,f=3; //declaration
	// extern int h;
	// T(g)(h,2); //declaration
	public void test6_8s2() throws Exception { // TODO raised bug 90622
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct T1 {
	// T1 operator()(int x) { return T1(x); }
	// int operator=(int x) { return x; }
	// T1(int) { }
	// };
	// struct T2 { T2(int){ } };
	// int a, (*(*b)(T2))(int), c, d;
	// void f() {
	// // disambiguation requires this to be parsed
	// // as a declaration
	// T1(a) = 3,
	// T2(4), // T2 will be declared as
	// (*(*b)(T2(c)))(int(d)); // a variable of type T1
	// // but this will not allow
	// // the last part of the
	// // declaration to parse
	// // properly since it depends
	// // on T2 being a typename
	// }
	public void test6_8s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef char* Pc;
	// void f(const Pc); // void f(char* const) (not const char*)
	// void g(const int Pc); // void g(const int)
	public void test7_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void h(unsigned Pc); // void h(unsigned int)
	// void k(unsigned int Pc); // void k(unsigned int)
	public void test7_1s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// static char* f(); // f() has internal linkage
	// char* f() // f() still has internal linkage
	// { //
	// }
	// char* g(); // g() has external linkage
	// static char* g() // error: inconsistent linkage
	// { // 
	// }
	// void h();
	// inline void h(); // external linkage
	// inline void l();
	// void l(); // external linkage
	// inline void m();
	// extern void m(); // external linkage
	// static void n();
	// inline void n(); // internal linkage
	// static int a; // a has internal linkage
	// int a; // error: two definitions
	// static int b; // b has internal linkage
	// extern int b; // b still has internal linkage
	// int c; // c has external linkage
	// static int c; // error: inconsistent linkage
	// extern int d; // d has external linkage
	// static int d; // error: inconsistent linkage
	public void test7_1_1s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct S;
	// extern S a;
	// extern S f();
	// extern void g(S);
	// void h()
	// {
	// g(a); //error: S is incomplete
	// f(); //error: S is incomplete
	// }
	public void test7_1_1s8a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// mutable const int* p; // OK
	// mutable int* const q; // illformed
	// };
	public void test7_1_1s8b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef int MILES, *KLICKSP;
	// MILES distance;
	// extern KLICKSP metricp;
	public void test7_1_3s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef struct s { //
	// } s;
	// typedef int I;
	// typedef int I;
	// typedef I I;
	public void test7_1_3s2() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}


	// class complex { // 
	// };
	// typedef int complex; // error: redefinition
	public void test7_1_3s3a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef int complex;
	// class complex { // 
	// }; // error: redefinition
	public void test7_1_3s3b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct S {
	// S();
	// ~S();
	// };
	// typedef struct S T;
	// S a = T(); // OK
	// struct T * p; // error
	public void test7_1_3s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef struct { } *ps, S; // S is the class name for linkage purposes
	public void test7_1_3s5a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef struct {
	// S(); //error: requires a return type because S is
	// // an ordinary member function, not a constructor
	// } S;
	public void test7_1_3s5b() throws Exception {
		IASTTranslationUnit tu= parseWithErrors(getAboveComment(), ParserLanguage.CPP);
		IASTCompositeTypeSpecifier comp= getCompositeType(tu, 0);
		IASTDeclaration d= getDeclaration(comp, 0);
		assertInstance(d, IASTProblemDeclaration.class);
	}

	// int foo() {
	// const int ci = 3; // cvqualified (initialized as required)
	// ci = 4; // illformed: attempt to modify const
	// int i = 2; // not cvqualified
	// const int* cip; // pointer to const int
	// cip = &i; // OK: cvqualified access path to unqualified
	// *cip = 4; // illformed: attempt to modify through ptr to const
	// int* ip;
	// ip = const_cast<int*>(cip); // cast needed to convert const int* to int*
	// *ip = 4; // defined: *ip points to i, a nonconst object
	// const int* ciq = new const int (3); // initialized as required
	// int* iq = const_cast<int*>(ciq); // cast required
	// *iq = 4; // undefined: modifies a const object
	// }
	public void test7_1_5_1s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// public:
	// mutable int i;
	// int j;
	// };
	// class Y {
	// public:
	// X x;
	// Y();
	// };
	// 
	// int foo() {
	// const Y y;
	// y.x.i++; //wellformed: mutable member can be modified
	// y.x.j++; //illformed: constqualified member modified
	// Y* p = const_cast<Y*>(&y); // cast away constness of y
	// p->x.i = 99; // wellformed: mutable member can be modified
	// p->x.j = 99; // undefined: modifies a const member
	// }
	public void test7_1_5_1s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// enum { a, b, c=0 };
	// enum { d, e, f=e+2 };
	public void test7_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// const int x = 12;
	// { enum { x = x }; }
	// }
	public void test7_2s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// enum color { red, yellow, green=20, blue };
	// color col = red;
	// color* cp = &col;
	// if (*cp == blue) // ...
	// return 0;
	// }
	public void test7_2s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// public:
	// enum direction { left='l', right='r' };
	// int f(int i)
	// { return i==left ? 0 : i==right ? 1 : 2; }
	// };
	// void g(X* p)
	// {
	// direction d; // error: direction not in scope
	// int i;
	// i = p->f(left); // error: left not in scope
	// i = p>
	// f(X::right); // OK
	// i = p>
	// f(p->left); // OK
	// // ...
	// }
	public void test7_2s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// namespace Outer {
	// int i;
	// namespace Inner {
	// void f() { i++; } // Outer::i
	// int i;
	// void g() { i++; } // Inner::i
	// }
	// }
	public void test7_3_1s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace { int i; } // unique::i
	// void f() { i++; } // unique::i++
	// namespace A {
	// namespace {
	// int i; // A::unique::i
	// int j; // A::unique::j
	// }
	// void g() { i++; } // A::unique::i++
	// }
	// using namespace A;
	// void h() {
	// i++; //error: unique::i or A::unique::i
	// A::i++; // A::unique::i
	// j++; // A::unique::j
	// }
	public void test7_3_1_1s1() throws Exception {
		String[] problems= {"i"};
		parse(getAboveComment(), ParserLanguage.CPP, problems);
	}

	// namespace X {
	// void f() { //
	// }
	// }
	public void test7_3_1_2s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace Q {
	// namespace V {
	// void f();
	// }
	// void V::f() { } // OK
	// void V::g() { } // error: g() is not yet a member of V
	// namespace V {
	// void g();
	// }
	// }
	// namespace R {
	// void Q::V::g() {  } // error: R doesn't enclose Q
	// }
	public void test7_3_1_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 2);
	}

	// // Assume f and g have not yet been defined.
	// void h(int);
	// namespace A {
	// class X {
	// friend void f(X); // A::f is a friend
	// class Y {
	// friend void g(); // A::g is a friend
	// friend void h(int); // A::h is a friend
	// // ::h not considered
	// };
	// };
	// // A::f, A::g and A::h are not visible here
	// X x;
	// void g() { f(x); } // definition of A::g
	// void f(X) { } // definition of A::f
	// void h(int) { } // definition of A::h
	// // A::f, A::g and A::h are visible here and known to be friends
	// }
	// using A::x;
	// void h()
	// {
	// A::f(x);
	// A::X::f(x); //error: f is not a member of A::X
	// A::X::Y::g(); // error: g is not a member of A::X::Y
	// }
	public void test7_3_1_2s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 4);
	}

	// namespace Company_with_very_long_name {  }
	// namespace CWVLN = Company_with_very_long_name;
	// namespace CWVLN = Company_with_very_long_name; // OK: duplicate
	// namespace CWVLN = CWVLN;
	public void test7_3_2s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct B {
	// void f(char);
	// void g(char);
	// enum E { e };
	// union { int x; };
	// };
	// struct D : B {
	// using B::f;
	// void f(int) { f('c'); } // calls B::f(char)
	// void g(int) { g('c'); } // recursively calls D::g(int)
	// };
	public void test7_3_3s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class C {
	// int g();
	// };
	// class D2 : public B {
	// using B::f; // OK: B is a base of D2
	// using B::e; // OK: e is an enumerator of base B
	// using B::x; // OK: x is a union member of base B
	// using C::g; // error: C isn't a base of D2
	// };
	public void test7_3_3s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class A {
	// public:
	// template <class T> void f(T);
	// template <class T> struct X { };
	// };
	// class B : public A {
	// public:
	// using A::f<double>; // illformed
	// using A::X<int>; // illformed
	// };
	public void test7_3_3s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct X {
	// int i;
	// static int s;
	// };
	// void f()
	// {
	// using X::i; // error: X::i is a class member
	// // and this is not a member declaration.
	// using X::s; // error: X::s is a class member
	// // and this is not a member declaration.
	// }
	public void test7_3_3s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void f();
	// namespace A {
	// void g();
	// }
	// namespace X {
	// using ::f; // global f
	// using A::g; // A's g
	// }
	// void h()
	// {
	// X::f(); //calls ::f
	// X::g(); //calls A::g
	// }
	public void test7_3_3s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace A {
	// int i;
	// }
	// namespace A1 {
	// using A::i;
	// using A::i; // OK: double declaration
	// }
	// void f()
	// {
	// using A::i;
	// using A::i; // error: double declaration
	// }
	// class B {
	// public:
	// int i;
	// };
	// class X : public B {
	// using B::i;
	// using B::i; // error: double member declaration
	// };
	public void test7_3_3s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace A {
	// void f(int);
	// }
	// using A::f; // f is a synonym for A::f;
	// // that is, for A::f(int).
	// namespace A {
	// void f(char);
	// }
	// void foo()
	// {
	// f('a'); //calls f(int),
	// } //even though f(char) exists.
	// void bar()
	// {
	// using A::f; // f is a synonym for A::f;
	// // that is, for A::f(int) and A::f(char).
	// f('a'); //calls f(char)
	// }
	public void test7_3_3s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
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
	public void test7_3_3s12() throws Exception { // raised bug 161562 for that
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace A {
	// int x;
	// }
	// namespace B {
	// int i;
	// struct g { };
	// struct x { };
	// void f(int);
	// void f(double);
	// void g(char); // OK: hides struct g
	// }
	// void func()
	// {
	// int i;
	// //using B::i; // error: i declared twice
	// void f(char);
	// using B::f; // OK: each f is a function
	// f(3.5); //calls B::f(double)
	// using B::g;
	// g('a'); //calls B::g(char)
	// struct g g1; // g1 has class type B::g
	// using B::x;
	// using A::x; // OK: hides struct B::x
	// x = 99; // assigns to A::x
	// struct x x1; // x1 has class type B::x
	// }
	public void test7_3_3s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace B {
	// void f(int);
	// void f(double);
	// }
	// namespace C {
	// void f(int);
	// void f(double);
	// void f(char);
	// }
	// void h()
	// {
	// using B::f; // B::f(int) and B::f(double)
	// using C::f; // C::f(int), C::f(double), and C::f(char)
	// f('h'); //calls C::f(char)
	// f(1); //error: ambiguous: B::f(int) or C::f(int) ?
	// void f(int); // error:
	// // f(int) conflicts with C::f(int) and B::f(int)
	// }
	public void test7_3_3s11() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}	

	// struct A { int x(); };
	// struct B : A { };
	// struct C : A {
	// using A::x;
	// int x(int);
	// };
	// struct D : B, C {
	// using C::x;
	// int x(double);
	// };
	// int f(D* d) {
	// return d>
	// x(); // ambiguous: B::x or C::x
	// }
	public void test7_3_3s14() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class A {
	// private:
	// void f(char);
	// public:
	// void f(int);
	// protected:
	// void g();
	// };
	// class B : public A {
	// using A::f; // error: A::f(char) is inaccessible
	// public:
	// using A::g; // B::g is a public synonym for A::g
	// };
	public void test7_3_3s15() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace A {
	// int i;
	// namespace B {
	// namespace C {
	// int i;
	// }
	// using namespace A::B::C;
	// void f1() {
	// i = 5; // OK, C::i visible in B and hides A::i
	// }
	// }
	// namespace D {
	// using namespace B;
	// using namespace C;
	// void f2() {
	// i = 5; // ambiguous, B::C::i or A::i?
	// }
	// }
	// void f3() {
	// i = 5; // uses A::i
	// }
	// }
	// void f4() {
	// i = 5; // illformed; neither i is visible
	// }
	public void test7_3_4s1() throws Exception {
		String[] problems= {"i", "i"};
		parse(getAboveComment(), ParserLanguage.CPP, problems);
	}

	// namespace M {
	// int i;
	// }
	// namespace N {
	// int i;
	// using namespace M;
	// }
	// void f()
	// {
	// using namespace N;
	// i = 7; // error: both M::i and N::i are visible
	// }
	public void test7_3_4s2a() throws Exception {
		String[] problems= {"i"};
		parse(getAboveComment(), ParserLanguage.CPP, problems);
	}

	// namespace A {
	// int i;
	// }
	// namespace B {
	// int i;
	// int j;
	// namespace C {
	// namespace D {
	// using namespace A;
	// int j;
	// int k;
	// int a = i; // B::i hides A::i
	// }
	// using namespace D;
	// int k = 89; // no problem yet
	// int l = k; // ambiguous: C::k or D::k
	// int m = i; // B::i hides A::i
	// int n = j; // D::j hides B::j
	// }
	// }
	public void test7_3_4s2b() throws Exception {
		String[] problems= {"k"};
		parse(getAboveComment(), ParserLanguage.CPP, problems);
	}

	// namespace D {
	// int d1;
	// void f(char);
	// }
	// using namespace D;
	// int d1; // OK: no conflict with D::d1
	// namespace E {
	// int e;
	// void f(int);
	// }
	// namespace D { // namespace extension
	// int d2;
	// using namespace E;
	// void f(int);
	// }
	// void f()
	// {
	// d1++; //error: ambiguous ::d1 or D::d1?
	// ::d1++; //OK
	// D::d1++; //OK
	// d2++; //OK: D::d2
	// e++; //OK: E::e
	// f(1); //error: ambiguous: D::f(int) or E::f(int)?
	// f('a'); //OK: D::f(char)
	// }
	public void test7_3_4s5() throws Exception {
		String[] problems= {"d1", "f"};
		parse(getAboveComment(), ParserLanguage.CPP, problems);
	}

	// complex sqrt(complex); // C++ linkage by default
	// extern "C" {
	// double sqrt(double); // C linkage
	// }
	public void test7_5s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// extern "C" void f1(void(*pf)(int));
	// // the name f1 and its function type have C language
	// // linkage; pf is a pointer to a C function
	// extern "C" typedef void FUNC();
	// FUNC f2; // the name f2 has C++ language linkage and the
	// // function's type has C language linkage
	// extern "C" FUNC f3; // the name of function f3 and the function's type
	// // have C language linkage
	// void (*pf2)(FUNC*); // the name of the variable pf2 has C++ linkage and
	// // the type of pf2 is pointer to C++ function that
	// // takes one parameter of type pointer to C function
	public void test7_5s4a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// extern "C" typedef void FUNC_c();
	// class C {
	// void mf1(FUNC_c*); // the name of the function mf1 and the member
	// // function's type have C++ language linkage; the
	// // parameter has type pointer to C function
	// FUNC_c mf2; // the name of the function mf2 and the member
	// // function's type have C++ language linkage
	// static FUNC_c* q; // the name of the data member q has C++ language
	// // linkage and the data member's type is pointer to
	// // C function
	// };
	// extern "C" {
	// class X {
	// void mf(); // the name of the function mf and the member
	// // function's type have C++ language linkage
	// void mf2(void(*)()); // the name of the function mf2 has C++ language
	// // linkage; the parameter has type pointer to
	// // C function
	// };
	// }
	public void test7_5s4b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace A {
	// extern "C" int f();
	// extern "C" int g() { return 1; }
	// extern "C" int h();
	// }
	// namespace B {
	// extern "C" int f(); // A::f and B::f refer
	// // to the same function
	// extern "C" int g() { return 1; } // illformed,
	// // the function g
	// // with C language linkage
	// // has two definitions
	// }
	// int A::f() { return 98; } // definition for the function f
	// // with C language linkage
	// extern "C" int h() { return 97; }
	// // definition for the function h
	// // with C language linkage
	// // A::h and ::h refer to the same function
	public void test7_5s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// extern "C" double f();
	// static double f(); // error
	public void test7_5s7a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// extern "C" int i; // declaration
	// extern "C" {
	// int i; // definition
	// }
	public void test7_5s7b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// extern "C" static void f(); // error
	public void test7_5s7c() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int i;
	// int *pi;
	// int *p[3];
	// int (*p3i)[3];
	// int *f();
	// int (*pf)(double);
	public void test8_1s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct S {
	// S(int);
	// };
	// void foo(double a)
	// {
	// S w(int(a)); // function declaration
	// S x(int()); // function declaration
	// S y((int)a); // object declaration
	// S z = int(a); // object declaration
	// }
	public void test8_2s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <class T>
	// struct S {
	// T *p;
	// };
	// S<int()> x; // typeid
	// S<int(1)> y; // expression (illformed)
	public void test8_2s4() throws Exception {
		IASTTranslationUnit tu= parse(getAboveComment(), ParserLanguage.CPP, true, 1);
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);
		
		assertInstance(col.getName(4), ICPPASTTemplateId.class);
		assertInstance(((ICPPASTTemplateId)col.getName(4)).getTemplateArguments()[0], IASTTypeId.class);
		
		final IASTName S_int_1 = col.getName(7);
		assertInstance(S_int_1, ICPPASTTemplateId.class);
		assertInstance(((ICPPASTTemplateId)S_int_1).getTemplateArguments()[0], IASTExpression.class);
		assertInstance(S_int_1.getBinding(), IProblemBinding.class);
	}

	// void foo()
	// {
	// sizeof(int(1)); // expression
	// // sizeof(int()); // typeid (illformed)
	// }
	public void test8_2s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void foo()
	// {
	// (int(1)); //expression
	// // (int())1; //typeid (illformed)
	// }
	public void test8_2s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class C { };
	// void f(int(C)) { } // void f(int (*fp)(C c)) { }
	// // not: void f(int C);
	// int g(C);
	// void foo() {
	// f(1); //error: cannot convert 1 to function pointer
	// f(g); //OK
	// }
	public void test8_2s7a() throws Exception { // TODO raised bug 90633
		final String code = getAboveComment();
		parse(code, ParserLanguage.CPP, true, 1);
		
		BindingAssertionHelper ba= new BindingAssertionHelper(code, true);
		IFunction f= ba.assertNonProblem("f", 1, IFunction.class);
		isTypeEqual(f.getType(), "void (int (*)(C))");
	}

	// class C { };
	// void h(int *(C[10])); // void h(int *(*_fp)(C _parm[10]));
	// // not: void h(int *C[10]);
	public void test8_2s7b() throws Exception {
		final String code = getAboveComment();
		parse(code, ParserLanguage.CPP, true, 0);
		BindingAssertionHelper ba= new BindingAssertionHelper(code, true);
		IFunction f= ba.assertNonProblem("h", 1, IFunction.class);
		isTypeEqual(f.getType(), "void (int * (*)(C *))");
	}

	// namespace A {
	// struct B {
	// void f();
	// };
	// void A::B::f() { } // illformed: the declarator must not be
	// // qualified with A::
	// }
	public void test8_3s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int unsigned i;
	public void test8_3s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// const int ci = 10, *pc = &ci, *const cpc = pc, **ppc;
	// int i, *p, *const cp = &i;
	// 
	// int f() {
	// i = ci;
	// *cp = ci;
	// pc++;
	// pc = cpc;
	// pc = p;
	// ppc = &pc;
	// }
	public void test8_3_1s2a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// const int ci = 10, *pc = &ci, *const cpc = pc, **ppc;
	// int i, *p, *const cp = &i;
	// int f() {
	// ci = 1; // error
	// ci++; //error
	// *pc = 2; // error
	// cp = &ci; // error
	// cpc++; //error
	// p = pc; // error
	// ppc = &p; // error
	// }
	public void test8_3_1s2b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// const int ci = 10, *pc = &ci, *const cpc = pc, **ppc;
	// int i, *p, *const cp = &i;
	// int f() {
	// *ppc = &ci; // OK, but would make p point to ci ...
	// *p = 5; // clobber ci
	// }
	public void test8_3_1s2c() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef int& A;
	// const A aref = 3; // illformed;
	// // nonconst reference initialized with rvalue
	public void test8_3_2s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void f(double& a) { a += 3.14; }
	// // ...
	// int foo() {
	// double d = 0;
	// f(d);
	// int v[20];
	// // ...
	// int& g(int i) { return v[i]; }
	// // ...
	// g(3) = 7;
	// }
	// struct link {
	// link* next;
	// };
	// link* first;
	// void h(link*& p) // p is a reference to pointer
	// {
	// p->next = first;
	// first = p;
	// p = 0;
	// }
	// void k()
	// {
	// link* q = new link;
	// h(q);
	// }
	public void test8_3_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// public:
	// void f(int);
	// int a;
	// };
	// class Y;
	// 
	// void f() {
	// int X::* pmi = &X::a;
	// void (X::* pmf)(int) = &X::f;
	// double X::* pmd;
	// char Y::* pmc;
	// X obj;
	// //...
	// obj.*pmi = 7; // assign 7 to an integer
	// // member of obj
	// (obj.*pmf)(7); //call a function member of obj
	// // with the argument 7
	// }
	public void test8_3_3s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef int A[5], AA[2][3];
	// typedef const A CA; // type is ''array of 5 const int''
	// typedef const AA CAA; // type is ''array of 2 array of 3 const int''
	public void test8_3_4s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// float fa[17], *afp[17];
	// static int x3d[3][5][7];
	public void test8_3_4s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int x[3][5];
	public void test8_3_4s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int printf(const char*, ...);
	// int f() {
	// int a=1, b=0;
	// printf("hello world");
	// printf("a=%d b=%d", a, b);
	// }
	public void test8_3_5s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef void F();
	// struct S {
	// const F f; // illformed:
	// // not equivalent to: void f() const;
	// };
	public void test8_3_5s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// #define FILE int
	// int fseek(FILE*, long, int);
	public void test8_3_5s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef void F();
	// F fv; // OK: equivalent to void fv();
	// // F fv { } // illformed
	// void fv() { } // OK: definition of fv
	public void test8_3_5s7a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef int FIC(int) const;
	// FIC f; // illformed:
	// //does not declare a member function
	// struct S {
	// FIC f; // OK
	// };
	// FIC S::*pm = &S::f; // OK
	public void test8_3_5s7b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int i,
	// *pi,
	// f(),
	// *fpi(int),
	// (*pif)(const char*, const char*),
	// (*fpif(int))(int);
	public void test8_3_5s9a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef int IFUNC(int);
	// IFUNC* fpif(int);
	public void test8_3_5s9b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void point(int = 3, int = 4);
	// void f() {
	// point(1,2); point(1); point();
	// }
	public void test8_3_6s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void f(int, int);
	// void f(int, int = 7);
	// void h()
	// {
	// f(3); //OK, calls f(3, 7)
	// void f(int = 1, int); // error: does not use default
	// // from surrounding scope
	// }
	// void m()
	// {
	// void f(int, int); // has no defaults
	// f(4); //error: wrong number of arguments
	// void f(int, int = 5); // OK
	// f(4); //OK, calls f(4, 5);
	// void f(int, int = 5); // error: cannot redefine, even to
	// // same value
	// }
	// void n()
	// {
	// f(6); //OK, calls f(6, 7)
	// }
	public void test8_3_6s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// int a = 1;
	// int f(int);
	// int g(int x = f(a)); // default argument: f(::a)
	// void h() {
	// a = 2;
	// {
	// int a = 3;
	// g(); // g(f(::a))
	// }
	// }
	public void test8_3_6s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class C {
	// void f(int i = 3);
	// void g(int i, int j = 99);
	// };
	// void C::f(int i = 3) // error: default argument already
	// { } // specified in class scope
	// void C::g(int i = 88, int j) // in this translation unit,
	// { }
	public void test8_3_6s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void f()
	// {
	// int i;
	// extern void g(int x = i); // error
	// // ...
	// }
	public void test8_3_6s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A {
	// void f(A* p = this) { } // error
	// };
	public void test8_3_6s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int a;
	// int f(int a, int b = a); // error: parameter a
	// // used as default argument
	// typedef int I;
	// int g(float I, int b = I(2)); // error: parameter I found
	// int h(int a, int b = sizeof(a)); // error, parameter a used
	public void test8_3_6s9a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// int b;
	// class X {
	// int a;
	// int mem1(int i = a); // error: nonstatic member a
	// // used as default argument
	// int mem2(int i = b); // OK; use X::b
	// static int b;
	// };
	public void test8_3_6s9b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int f(int = 0);
	// void h()
	// {
	// int j = f(1);
	// int k = f(); // OK, means f(0)
	// }
	// int (*p1)(int) = &f;
	// int (*p2)() = &f; // error: type mismatch
	public void test8_3_6s9c() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// virtual void f(int a = 7);
	// };
	// struct B : public A {
	// void f(int a);
	// };
	// void m()
	// {
	// B* pb = new B;
	// A* pa = pb;
	// pa->f(); //OK, calls pa->B::f(7)
	// pb->f(); //error: wrong number of arguments for B::f()
	// }
	public void test8_3_6s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// int max(int a, int b, int c)
	// {
	// int m = (a > b) ? a : b;
	// return (m > c) ? m : c;
	// }
	public void test8_4s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int a;
	// struct X {
	// static int a;
	// static int b;
	// };
	// int X::a = 1;
	// int X::b = a; // X::b = X::a
	public void test8_5s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// int x;
	// struct B {
	// int i;
	// int j;
	// } b;
	// } a = { 1, { 2, 3 } };
	public void test8_5_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int x[] = { 1, 3, 5 };
	public void test8_5_1s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// int i;
	// static int s;
	// int j;
	// } a = { 1, 2 };
	public void test8_5_1s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// char cv[4] = { 'a', 's', 'd', 'f', 0 }; // error
	public void test8_5_1s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct S { int a; char* b; int c; };
	// S ss = { 1, "asdf" };
	public void test8_5_1s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct S { };
	// struct A {
	// S s;
	// int i;
	// } a = { { } , 3 };
	public void test8_5_1s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int x[2][2] = { 3, 1, 4, 2 };
	// float y[4][3] = {
	// { 1 }, { 2 }, { 3 }, { 4 }
	// };
	public void test8_5_1s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// float y[4][3] = {
	// { 1, 3, 5 },
	// { 2, 4, 6 },
	// { 3, 5, 7 },
	// };
	public void test8_5_1s11a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// float y[4][3] = {
	// 1, 3, 5, 2, 4, 6, 3, 5, 7
	// };
	public void test8_5_1s11b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// int i;
	// operator int();
	// };
	// struct B {
	// A a1, a2;
	// int z;
	// };
	// A a;
	// B b = { 4, a, a };
	public void test8_5_1s12() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// union u { int a; char* b; };
	// u a = { 1 };
	// u b = a;
	// u c = 1; // error
	// u d = { 0, "asdf" }; // error
	// u e = { "asdf" }; // error
	public void test8_5_1s15() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// char msg[] = "Syntax error on line %s";
	public void test8_5_2s1() throws Exception { 
		// raised bug 90647
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// 	char cv[4] = "asdf"; // error
	public void test8_5_2s2() throws Exception {
		// we do not check the size of an array, which is ok.
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int& r1; // error: initializer missing
	// extern int& r2; // OK
	public void test8_5_3s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// double d = 2.0;
	// double& rd = d; // rd refers to d
	// const double& rcd = d; // rcd refers to d
	// struct A { };
	// struct B : public A { } b;
	// A& ra = b; // ra refers to A subobject in b
	// const A& rca = b; // rca refers to A subobject in b
	public void test8_5_3s5a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// double& rd2 = 2.0; // error: not an lvalue and reference not const
	// int i = 2;
	// double& rd3 = i; // error: type mismatch and reference not const
	public void test8_5_3s5b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A { };
	// struct B : public A { } b;
	// extern B f();
	// const A& rca = f(); // Either bound to the A subobject of the B rvalue,
	// // or the entire B object is copied and the reference
	// // is bound to the A subobject of the copy
	public void test8_5_3s5c() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// const double& rcd2 = 2; // rcd2 refers to temporary with value 2.0
	// const volatile int cvi = 1;
	// const int& r = cvi; // error: type qualifiers dropped
	public void test8_5_3s5d() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct X { int a; };
	// struct Y { int a; };
	// X a1;
	// Y a2;
	// int a3;
	// void test() {
	// a1 = a2; // error: Y assigned to X
	// a1 = a3; // error: int assigned to X
	// }
	// int f(X);
	// int f(Y);
	// struct S { int a; };
	// struct S { int a; }; // error, double definition
	public void test9_1s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct stat {
	// // ...
	// };
	// stat gstat; // use plain stat to
	// // define variable
	// int stat(struct stat*); // redeclare stat as function
	// void f()
	// {
	// struct stat* ps; // struct prefix needed
	// // to name struct stat
	// // ...
	// stat(ps); //call stat()
	// // ...
	// }
	public void test9_1s2a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct s { int a; };
	// void g()
	// {
	// struct s; // hide global struct s
	// // with a local declaration
	// s* p; // refer to local struct s
	// struct s { char* p; }; // define local struct s
	// struct s; // redeclaration, has no effect
	// }
	public void test9_1s2b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class Vector;
	// class Matrix {
	// // ...
	// friend Vector operator*(Matrix&, Vector&);
	// };
	// class Vector {
	// // ...
	// friend Vector operator*(Matrix&, Vector&);
	// };
	public void test9_1s2c() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct s { int a; };
	// void g(int s)
	// {
	// struct s* p = new struct s; // global s
	// p->a = s; // local s
	// }
	public void test9_1s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A * A;
	public void test9_1s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct tnode {
	// char tword[20];
	// int count;
	// tnode *left;
	// tnode *right;
	// };
	// tnode s, *sp;
	public void test9_2s11() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct X {
	// typedef int T;
	// static T count;
	// void f(T);
	// };
	// void X::f(T t = count) { }
	public void test9_3s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef void fv(void);
	// typedef void fvc(void) const;
	// struct S {
	// fv memfunc1; // equivalent to: void memfunc1(void);
	// void memfunc2();
	// fvc memfunc3; // equivalent to: void memfunc3(void) const;
	// };
	// fv S::* pmfv1 = &S::memfunc1;
	// fv S::* pmfv2 = &S::memfunc2;
	// fvc S::* pmfv3 = &S::memfunc3;
	public void test9_3s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct tnode {
	// char tword[20];
	// int count;
	// tnode *left;
	// tnode *right;
	// void set(char*, tnode* l, tnode* r);
	// };
	// void tnode::set(char* w, tnode* l, tnode* r)
	// {
	// count = strlen(w)+1;
	// if (sizeof(tword)<=count)
	// perror("tnode string too long");
	// strcpy(tword,w);
	// left = l;
	// right = r;
	// }
	// void f(tnode n1, tnode n2)
	// {
	// n1.set("abc",&n2,0);
	// n2.set("def",0,0);
	// }
	public void test9_3_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct X {
	// void g() const;
	// void h() const volatile;
	// };
	public void test9_3_1s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct s {
	// int a;
	// int f() const;
	// int g() { return a++; }
	// int h() const { return a++; } // error
	// };
	// int s::f() const { return a; }
	public void test9_3_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct s {
	// int a;
	// int f() const;
	// int g() { return a++; }
	// };
	// int s::f() const { return a; }
	// 
	// void k(s& x, const s& y)
	// {
	// x.f();
	// x.g();
	// y.f();
	// y.g(); //error
	// }
	public void test9_3_2s4() throws Exception {
		String[] problems= {"g"};
		final String code = getAboveComment();
		IASTTranslationUnit tu= parse(code, ParserLanguage.CPP, problems);
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		bh.assertNonProblem("g();", 1); 
		bh.assertProblem("g(); //error", 1); 
	}

	// class process {
	// public:
	// static void reschedule();
	// };
	// process& g();
	// void f()
	// {
	// process::reschedule(); // OK: no object necessary
	// g().reschedule(); // g() is called
	// }
	public void test9_4s2a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int g();
	// struct X {
	// static int g();
	// };
	// struct Y : X {
	// static int i;
	// };
	// int Y::i = g(); // equivalent to Y::g();
	public void test9_4s2b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class process {
	// static process* run_chain;
	// static process* running;
	// };
	// process* process::running = get_main();
	// process* process::run_chain = running;
	public void test9_4_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// void f()
	// {
	// union { int a; char* p; };
	// a = 1;
	// // ...
	// p = "Jennifer";
	// // ...
	// }
	public void test9_5s2() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// union { int aa; char* p; } obj, *ptr = &obj;
	// aa = 1; // error
	// ptr->aa = 1; // OK
	// }
	public void test9_5s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// enum BOOL { f=0, t=1 };
	// struct A {
	// BOOL b:1;
	// };
	// A a;
	// void f() {
	// a.b = t;
	// if (a.b == t) // shall yield true
	// {  }
	// }
	public void test9_6s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int x;
	// int y;
	// class enclose {
	// public:
	// int x;
	// static int s;
	// class inner {
	// void f(int i)
	// {
	// int a = sizeof(x); // error: refers to enclose::x
	// x = i; // error: assign to enclose::x
	// s = i; // OK: assign to enclose::s
	// ::x = i; // OK: assign to global x
	// y = i; // OK: assign to global y
	// }
	// void g(enclose* p, int i)
	// {
	// p>
	// x = i; // OK: assign to enclose::x
	// }
	// };
	// };
	// inner* p = 0; // error: inner not in scope
	public void test9_7s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class enclose {
	// public:
	// class inner {
	// static int x;
	// void f(int i);
	// };
	// };
	// int enclose::inner::x = 1;
	// void enclose::inner::f(int i) {  }
	public void test9_7s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class E {
	// class I1; // forward declaration of nested class
	// class I2;
	// class I1 {}; // definition of nested class
	// };
	// class E::I2 {}; // definition of nested class
	public void test9_7s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int x;
	// void f()
	// {
	// static int s ;
	// int x;
	// extern int g();
	// struct local {
	// int g() { return x; } // error: x is auto
	// int h() { return s; } // OK
	// int k() { return ::x; } // OK
	// int l() { return g(); } // OK
	// };
	// // ...
	// }
	// local* p = 0; // error: local not in scope
	public void test9_8s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class X {
	// public:
	// typedef int I;
	// class Y { /* ... */ };
	// I a;
	// };
	// I b; // error
	// Y c; // error
	// X::Y d; // OK
	// X::I e; // OK
	public void test9_9s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class Base {
	// public:
	// int a, b, c;
	// };
	// class Derived : public Base {
	// public:
	// int b;
	// };
	// class Derived2 : public Derived {
	// public:
	// int c;
	// };
	public void test10s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A {  };
	// class B {  };
	// class C {  };
	// class D : public A, public B, public C {  };
	public void test10_1s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {  };
	// class Y : public X, public X {  }; // illformed
	public void test10_1s3a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class L { public: int next;  };
	// class A : public L {  };
	// class B : public L {  };
	// class C : public A, public B { void f();  }; // wellformed
	// class D : public A, public L { void f();  }; // wellformed
	// 
	public void test10_1s3b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A {
	// public:
	// int a;
	// int (*b)();
	// int f();
	// int f(int);
	// int g();
	// };
	// class B {
	// int a;
	// int b();
	// public:
	// int f();
	// int g;
	// int h();
	// int h(int);
	// };
	// class C : public A, public B {};
	// void g(C* pc)
	// {
	// pc->a = 1; // error: ambiguous: A::a or B::a
	// pc->b(); //error: ambiguous: A::b or B::b
	// pc->f(); //error: ambiguous: A::f or B::f
	// pc->f(1); //error: ambiguous: A::f or B::f
	// pc->g(); //error: ambiguous: A::g or B::g
	// pc->g = 1; // error: ambiguous: A::g or B::g
	// pc->h(); //OK
	// pc->h(1); //OK
	// }
	public void test10_2s3a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct U { static int i; };
	// struct V : U { };
	// struct W : U { using U::i; };
	// struct X : V, W { void foo(); };
	// void X::foo() {
	// i; //finds U::i in two ways: as W::i and U::i in V
	// // no ambiguity because U::i is static
	// }
	public void test10_2s3b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A {
	// public:
	// int f();
	// };
	// class B {
	// public:
	// int f();
	// };
	// class C : public A, public B {
	// int f() { return A::f() + B::f(); }
	// };
	public void test10_2s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class V { public: int v; };
	// class A {
	// public:
	// int a;
	// static int s;
	// enum { e };
	// };
	// class B : public A, public virtual V {};
	// class C : public A, public virtual V {};
	// class D : public B, public C { };
	// void f(D* pd)
	// {
	// pd->v++; //OK: only one v (virtual)
	// pd->s++; //OK: only one s (static)
	// int i = pd>
	// e; // OK: only one e (enumerator)
	// pd->a++; //error, ambiguous: two as in D
	// }
	public void test10_2s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class V { public: int f(); int x; };
	// class W { public: int g(); int y; };
	// class B : public virtual V, public W
	// {
	// public:
	// int f(); int x;
	// int g(); int y;
	// };
	// class C : public virtual V, public W { };
	// class D : public B, public C { void glorp(); };
	public void test10_2s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class V { };
	// class A { };
	// class B : public A, public virtual V { };
	// class C : public A, public virtual V { };
	// class D : public B, public C { };
	// void g()
	// {
	// D d;
	// B* pb = &d;
	// A* pa = &d; // error, ambiguous: C's A or B's A?
	// V* pv = &d; // OK: only one V subobject
	// }
	public void test10_2s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// virtual void f();
	// };
	// struct B : virtual A {
	// virtual void f();
	// };
	// struct C : B , virtual A {
	// using A::f;
	// };
	// void foo() {
	// C c;
	// c.f(); //calls B::f, the final overrider
	// c.C::f(); //calls A::f because of the usingdeclaration
	// }
	public void test10_3s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class B {};
	// class D : private B { friend class Derived; };
	// struct Base {
	// virtual void vf1();
	// virtual void vf2();
	// virtual void vf3();
	// virtual B* vf4();
	// virtual B* vf5();
	// void f();
	// };
	// struct No_good : public Base {
	// D* vf4(); // error: B (base class of D) inaccessible
	// };
	// class A;
	// struct Derived : public Base {
	// void vf1(); // virtual and overrides Base::vf1()
	// void vf2(int); // not virtual, hides Base::vf2()
	// char vf3(); // error: invalid difference in return type only
	// D* vf4(); // OK: returns pointer to derived class
	// A* vf5(); // error: returns pointer to incomplete class
	// void f();
	// };
	// void g()
	// {
	// Derived d;
	// Base* bp = &d; // standard conversion:
	// // Derived* to Base*
	// bp->vf1(); //calls Derived::vf1()
	// bp->vf2(); //calls Base::vf2()
	// bp->f(); //calls Base::f() (not virtual)
	// B* p = bp->vf4(); // calls Derived::pf() and converts the
	// // result to B*
	// Derived* dp = &d;
	// D* q = dp->vf4(); // calls Derived::pf() and does not
	// // convert the result to B*
	// dp->vf2(); //illformed: argument mismatch
	// }
	public void test10_3s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct A {
	// virtual void f();
	// };
	// struct B1 : A { // note nonvirtual derivation
	// void f();
	// };
	// struct B2 : A {
	// void f();
	// };
	// struct D : B1, B2 { // D has two separate A subobjects
	// };
	// void foo()
	// {
	// D d;
	// // A* ap = &d; // would be illformed:ambiguous
	// B1* b1p = &d;
	// A* ap = b1p;
	// D* dp = &d;
	// ap->f(); //calls D::B1::f
	// dp->f(); //illformed: ambiguous
	// }
	public void test10_3s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct A {
	// virtual void f();
	// };
	// struct VB1 : virtual A { // note virtual derivation
	// void f();
	// };
	// struct VB2 : virtual A {
	// void f();
	// };
	// struct Error : VB1, VB2 { // illformed
	// };
	// struct Okay : VB1, VB2 {
	// void f();
	// };
	public void test10_3s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct VB1a : virtual A { // does not declare f
	// };
	// struct Da : VB1a, VB2 {
	// };
	// void foe()
	// {
	// VB1a* vb1ap = new Da;
	// vb1ap->f(); //calls VB2::f
	// }
	public void test10_3s11() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class B { public: virtual void f(); };
	// class D : public B { public: void f(); };
	// void D::f() { B::f(); }
	public void test10_3s12() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class point {  };
	// class shape { // abstract class
	// point center;
	// // ...
	// public:
	// point where() { return center; }
	// void move(point p) { center=p; draw(); }
	// virtual void rotate(int) = 0; // pure virtual
	// virtual void draw() = 0; // pure virtual
	// // ...
	// };
	public void test10_4s2a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// Not legal C++
	//	//	public void test10_4s2b() throws Exception {
	//		StringBuffer buffer = new StringBuffer();
	//		buffer.append("struct C {\n"); //$NON-NLS-1$
	//		buffer.append("virtual void f() { }=0; // illformed\n"); //$NON-NLS-1$
	//		buffer.append("};\n"); //$NON-NLS-1$
	//		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	//	}

	// shape x; // error: object of abstract class
	// shape* p; // OK
	// shape f(); // error
	// void g(shape); // error
	// shape& h(shape&); // OK
	public void test10_4s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class ab_circle : public shape {
	// int radius;
	// public:
	// void rotate(int) {}
	// // ab_circle::draw() is a pure virtual
	// };
	public void test10_4s4a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class circle : public shape {
	// int radius;
	// public:
	// void rotate(int) {}
	// void draw(); // a definition is required somewhere
	// };
	public void test10_4s4b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class X {
	// int a; // X::a is private by default
	// };
	// struct S {
	// int a; // S::a is public by default
	// };
	public void test11s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A
	// {
	// class B { };
	// public:
	// typedef B BB;
	// };
	// void f()
	// {
	// A::BB x; // OK, typedef name A::BB is public
	// A::B y; // access error, A::B is private
	// }
	public void test11s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A {
	// typedef int I; // private member
	// I f();
	// friend I g(I);
	// static I x;
	// };
	// A::I A::f() { return 0; }
	// A::I g(A::I p = A::x);
	// A::I g(A::I p) { return 0; }
	// A::I A::x = 0;
	public void test11s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class D {
	// class E {
	// static int m;
	// };
	// };
	// int D::E::m = 1; // OK, no access error on private E
	public void test11s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// int a; // X::a is private by default: class used
	// public:
	// int b; // X::b is public
	// int c; // X::c is public
	// };
	public void test11_1s1a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct S {
	// int a; // S::a is public by default: struct used
	// protected:
	// int b; // S::b is protected
	// private:
	// int c; // S::c is private
	// public:
	// int d; // S::d is public
	// };
	public void test11s1b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct S {
	// class A;
	// private:
	// class A { }; // error: cannot change access
	// };
	public void test11_1s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class B {  };
	// class D1 : private B {  };
	// class D2 : public B {  };
	// class D3 : B {  }; // B private by default
	// struct D4 : public B {  };
	// struct D5 : private B {  };
	// struct D6 : B {  }; // B public by default
	// class D7 : protected B {  };
	// struct D8 : protected B {  };
	public void test11_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class B {
	// public:
	// int mi; // nonstatic member
	// static int si; // static member
	// };
	// class D : private B {
	// };
	// class DD : public D {
	// void f();
	// };
	// void DD::f() {
	// mi = 3; // error: mi is private in D
	// si = 3; // error: si is private in D
	// B b;
	// b.mi = 3; // OK (b.mi is different from this->mi)
	// b.si = 3; // OK (b.si is different from this->si)
	// B::si = 3; // OK
	// B* bp1 = this; // error: B is a private base class
	// B* bp2 = (B*)this; // OK with cast
	// bp2->mi = 3; // OK: access through a pointer to B.
	// }
	public void test11_2s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class B;
	// class A {
	// private:
	// int i;
	// friend void f(B*);
	// };
	// class B : public A { };
	// void f(B* p) {
	// p->i = 1; // OK: B* can be implicitly cast to A*,
	// // and f has access to i in A
	// }
	public void test11_2s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// int a;
	// friend void friend_set(X*, int);
	// public:
	// void member_set(int);
	// };
	// void friend_set(X* p, int i) { p->a = i; }
	// void X::member_set(int i) { a = i; }
	// void f()
	// {
	// X obj;
	// friend_set(&obj,10);
	// obj.member_set(10);
	// }
	public void test11_4s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A {
	// class B { };
	// friend class X;
	// };
	// class X : A::B { // illformed:
	// //A::B cannot be accessed
	// // in the baseclause for X
	// A::B mx; // OK: A::B used to declare member of X
	// class Y : A::B { // OK: A::B used to declare member of X
	// A::B my; // illformed: A::B cannot be accessed
	// // to declare members of nested class of X
	// };
	// };
	public void test11_4s2a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// enum { a=100 };
	// friend class Y;
	// };
	// class Y {
	// int v[X::a]; // OK, Y is a friend of X
	// };
	// class Z {
	// int v[X::a]; // error: X::a is private
	// };
	public void test11_4s2b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class Y {
	// friend char* X::foo(int);
	// // ...
	// };
	public void test11_4s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class M {
	// friend void f() { } // definition of global f, a friend of M,
	// // not the definition of a member function
	// };
	public void test11_4s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A {
	// friend class B;
	// int a;
	// };
	// class B {
	// friend class C;
	// };
	// class C {
	// void f(A* p)
	// {
	// p->a++; //error: C is not a friend of A
	// // despite being a friend of a friend
	// }
	// };
	// class D : public B {
	// void f(A* p)
	// {
	// p->a++; //error: D is not a friend of A
	// // despite being derived from a friend
	// }
	// };
	public void test11_4s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X;
	// void a();
	// void f() {
	// class Y;
	// extern void b();
	// class A {
	// friend class X; // OK, but X is a local class, not ::X
	// friend class Y; // OK
	// friend class Z; // OK, introduces local class Z
	// friend void a(); // error, ::a is not considered
	// friend void b(); // OK
	// friend void c(); // error
	// };
	// X *px; // OK, but ::X is found
	// Z *pz; // error, no Z is found
	// }
	public void test11_4s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class B {
	// protected:
	// int i;
	// static int j;
	// };
	// class D1 : public B {
	// };
	// class D2 : public B {
	// friend void fr(B*,D1*,D2*);
	// void mem(B*,D1*);
	// };
	// void fr(B* pb, D1* p1, D2* p2)
	// {
	// pb->i = 1; // illformed
	// p1->i = 2; // illformed
	// p2->i = 3; // OK (access through a D2)
	// p2->B::i = 4; // OK (access through a D2, even though
	// // naming class is B)
	// int B::* pmi_B = &B::i; // illformed
	// int B::* pmi_B2 = &D2::i; // OK (type of &D2::i is int B::*)
	// B::j = 5; // OK (because refers to static member)
	// D2::j =6; // OK (because refers to static member)
	// }
	// void D2::mem(B* pb, D1* p1)
	// {
	// pb->i = 1; // illformed
	// p1->i = 2; // illformed
	// i = 3; // OK (access through this)
	// B::i = 4; // OK (access through this, qualification ignored)
	// int B::* pmi_B = &B::i; // illformed
	// int B::* pmi_B2 = &D2::i; // OK
	// j = 5; // OK (because j refers to static member)
	// B::j = 6; // OK (because B::j refers to static member)
	// }
	// void g(B* pb, D1* p1, D2* p2)
	// {
	// pb->i = 1; // illformed
	// p1->i = 2; // illformed
	// p2->i = 3; // illformed
	// }
	public void test11_5s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class B {
	// public:
	// virtual int f();
	// };
	// class D : public B {
	// private:
	// int f();
	// };
	// void f()
	// {
	// D d;
	// B* pb = &d;
	// D* pd = &d;
	// pb->f(); //OK: B::f() is public,
	// // D::f() is invoked
	// pd->f(); //error: D::f() is private
	// }
	public void test11_6s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class W { public: void f(); };
	// class A : private virtual W { };
	// class B : public virtual W { };
	// class C : public A, public B {
	// void f() { W::f(); } // OK
	// };
	public void test11_7s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class E {
	// int x;
	// class B { };
	// class I {
	// B b; // error: E::B is private
	// int y;
	// void f(E* p, int i)
	// {
	// p->x = i; // error: E::x is private
	// }
	// };
	// int g(I* p)
	// {
	// return p->y; // error: I::y is private
	// }
	// };
	public void test11_8s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class C {
	// class A { };
	// A *p; // OK
	// class B : A // OK
	// {
	// A *q; // OK because of injection of name A in A
	// C::A *r; // error, C::A is inaccessible
	// B *s; // OK because of injection of name B in B
	// C::B *t; // error, C::B is inaccessible
	// };
	// };
	public void test11_8s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class C {
	// public:
	// C(); //declares the constructor
	// };
	// C::C() { } // defines the constructor
	public void test12_1s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct C;
	// void no_opt(C*);
	// struct C {
	// int c;
	// C() : c(0) { no_opt(this); }
	// };
	// const C cobj;
	// void no_opt(C* cptr) {
	// int i = cobj.c * 100; // value of cobj.c is unspecified
	// cptr->c = 1;
	// cout << cobj.c * 100 // value of cobj.c is unspecified
	// << '\
	// ';
	// }
	public void test12_1s15() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class X {
	// // ...
	// public:
	// // ...
	// X(int);
	// X(const X&);
	// ~X();
	// };
	// X f(X);
	// void g()
	// {
	// X a(1);
	// X b = f(X(2));
	// a = f(a);
	// }
	public void test12_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class C {
	// // ...
	// public:
	// C();
	// C(int);
	// friend C operator+(const C&, const C&);
	// ~C();
	// };
	// C obj1;
	// const C& cr = C(16)+C(23);
	// C obj2;
	public void test12_2s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// // ...
	// public:
	// operator int();
	// };
	// class Y {
	// // ...
	// public:
	// operator X();
	// };
	// Y a;
	// int b = a; // error:
	// // a.operator X().operator int() not tried
	// int c = X(a); // OK: a.operator X().operator int()
	public void test12_3s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// public:
	// // ...
	// operator int();
	// };
	// class Y : public X {
	// public:
	// // ...
	// operator char();
	// };
	// void f(Y& a)
	// {
	// if (a) { // illformed:
	// // X::operator int() or Y::operator char()
	// // ...
	// }
	// }
	public void test12_3s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// // ...
	// public:
	// X(int);
	// X(const char*, int =0);
	// };
	// void f(X arg)
	// {
	// X a = 1; // a = X(1)
	// X b = "Jessie"; // b = X("Jessie",0)
	// a = 2; // a = X(2)
	// f(3); // f(X(3))
	// }
	public void test12_3_1s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class Z {
	// public:
	// explicit Z();
	// explicit Z(int);
	// // ...
	// };
	// Z a; // OK: defaultinitialization performed
	// Z a1 = 1; // error: no implicit conversion
	// Z a3 = Z(1); // OK: direct initialization syntax used
	// Z a2(1); // OK: direct initialization syntax used
	// Z* p = new Z(1); // OK: direct initialization syntax used
	// Z a4 = (Z)1; // OK: explicit cast used
	// Z a5 = static_cast<Z>(1); // OK: explicit cast used
	public void test12_3_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// // ...
	// public:
	// operator int();
	// };
	// void f(X a)
	// {
	// int i = int(a);
	// i = (int)a;
	// i = a;
	// }
	public void test12_3_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void g(X a, X b)
	// {
	// int i = (a) ? 1+a : 0;
	// int j = (a&&b) ? a+b : i;
	// if (a) { // ...
	// }
	// }
	public void test12_3_2s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	//	struct B {
	//		virtual ~B() { }
	//	};
	//	struct D : B {
	//		~D() { }
	//	};
	//	D D_object;
	//	typedef B B_alias;
	//	B* B_ptr = &D_object;
	//	void f() {
	//		D_object.B::~B(); //1            // calls B's destructor
	//		B_ptr->~B(); //2                 // calls D's destructor
	//		B_ptr->~B_alias(); //3           // calls D's destructor
	//		B_ptr->B_alias::~B(); //4        // calls B's destructor
	//		B_ptr->B_alias::~B_alias(); //5  // error, no B_alias in class B
	//	}
	public void test12_4s12() throws Exception {
		final String code = getAboveComment();
		parse(code, ParserLanguage.CPP, false, 0);
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPFunction dtor= bh.assertNonProblem("~B() {", 2);
		
		ICPPFunction d= bh.assertNonProblem("~B(); //1", 2);
		assertSame(dtor, d);
		d= bh.assertNonProblem("~B(); //2", 2);
		assertSame(dtor, d);
		d= bh.assertNonProblem("~B_alias(); //3", 8);
		assertSame(dtor, d);
		d= bh.assertNonProblem("~B(); //4", 2);
		assertSame(dtor, d);
		
		bh.assertProblem("~B_alias(); //5", 8);
	}

	// void* operator new(size_t, void* p) { return p; }
	// struct X {
	// // ...
	// X(int);
	// ~X();
	// };
	// void f(X* p);
	// void g() // rare, specialized use:
	// {
	// char* buf = new char[sizeof(X)];
	// X* p = new(buf) X(222); // use buf[] and initialize
	// f(p);
	// p->X::~X(); //cleanup
	// }
	public void test12_4s13() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class Arena;
	// struct B {
	// void* operator new(size_t, Arena*);
	// };
	// struct D1 : B {
	// };
	// Arena* ap;
	// void foo(int i)
	// {
	// new (ap) D1; // calls B::operator new(size_t, Arena*)
	// new D1[i]; // calls ::operator new[](size_t)
	// new D1; // illformed: ::operator new(size_t) hidden
	// }
	public void test12_5s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class X {
	// // ...
	// void operator delete(void*);
	// void operator delete[](void*, size_t);
	// };
	// class Y {
	// // ...
	// void operator delete(void*, size_t);
	// void operator delete[](void*);
	// };
	public void test12_5s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class complex {
	// // ...
	// public:
	// complex();
	// complex(double);
	// complex(double,double);
	// // ...
	// };
	// complex sqrt(complex,complex);
	// int foo() {
	// complex a(1); // initialize by a call of
	// // complex(double)
	// complex b = a; // initialize by a copy of a
	// complex c = complex(1,2); // construct complex(1,2)
	// // using complex(double,double)
	// // copy it into c
	// complex d = sqrt(b,c); // call sqrt(complex,complex)
	// // and copy the result into d
	// complex e; // initialize by a call of
	// // complex()
	// complex f = 3; // construct complex(3) using
	// // complex(double)
	// // copy it into f
	// complex g = { 1, 2 }; // error; constructor is required
	// }
	public void test12_6_1s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class complex {
	// // ...
	// public:
	// complex();
	// complex(double);
	// complex(double,double);
	// // ...
	// };
	// complex v[6] = { 1,complex(1,2),complex(),2 };
	// class X {
	// public:
	// int i;
	// float f;
	// complex c;
	// } x = { 99, 88.8, 77.7 };
	public void test12_6_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A { A(); };
	// typedef A global_A;
	// struct B { };
	// struct C: public A, public B { C(); };
	// C::C(): global_A() { } // meminitializer for base A
	public void test12_6_2s2a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A { A(); };
	// struct B: public virtual A { };
	// struct C: public A, public B { C(); };
	// C::C(): A() { } // illformed: which A?
	public void test12_6_2s2b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct B1 { B1(int); };
	// struct B2 { B2(int); };
	// struct D : B1, B2 {
	// D(int);
	// B1 b;
	// const int c;
	// };
	// D::D(int a) : B2(a+1), B1(a+2), c(a+3), b(a+4)
	// {  }
	// D d(10);
	public void test12_6_2s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class V {
	// public:
	// V();
	// V(int);
	// // ...
	// };
	// class A : public virtual V {
	// public:
	// A();
	// A(int);
	// // ...
	// };
	// class B : public virtual V {
	// public:
	// B();
	// B(int);
	// // ...
	// };
	// class C : public A, public B, private virtual V {
	// public:
	// C();
	// C(int);
	// // ...
	// };
	// A::A(int i) : V(i) { }
	// B::B(int i) { }
	// C::C(int i) { }
	// V v(1); // use V(int)
	// A a(2); // use V(int)
	// B b(3); // use V()
	// C c(4); // use V()
	public void test12_6_2s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// int a;
	// int b;
	// int i;
	// int j;
	// public:
	// const int& r;
	// X(int i): r(a), b(i), i(i), j(this->i) {}
	// };
	public void test12_6_2s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A {
	// public:
	// A(int);
	// };
	// class B : public A {
	// int j;
	// public:
	// int f();
	// B() : A(f()), // undefined: calls member function
	// // but base A not yet initialized
	// j(f()) { } // welldefined: bases are all initialized
	// };
	// class C {
	// public:
	// C(int);
	// };
	// class D : public B, C {
	// int i;
	// public:
	// D() : C(f()), // undefined: calls member function
	// // but base C not yet initialized
	// i(f()) {} // welldefined: bases are all initialized
	// };
	public void test12_6_2s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct X { int i; };
	// struct Y : X { };
	// struct A { int a; };
	// struct B : public A { int j; Y y; };
	// extern B bobj;
	// B* pb = &bobj; // OK
	// int* p1 = &bobj.a; // undefined, refers to base class member
	// int* p2 = &bobj.y.i; // undefined, refers to member's member
	// A* pa = &bobj; // undefined, upcast to a base class type
	// B bobj; // definition of bobj
	// extern X xobj;
	// int* p3 = &xobj.i; // OK, X is a POD class
	// X xobj;
	public void test12_7s1_a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct W { int j; };
	// struct X : public virtual W { };
	// struct Y {
	// int *p;
	// X x;
	// Y() : p(&x.j) // undefined, x is not yet constructed
	// { }
	// };
	public void test12_7s1_b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class V {
	// public:
	// virtual void f();
	// virtual void g();
	// };
	// class A : public virtual V {
	// public:
	// virtual void f();
	// };
	// class B : public virtual V {
	// public:
	// virtual void g();
	// B(V*, A*);
	// };
	// class D : public A, B {
	// public:
	// virtual void f();
	// virtual void g();
	// D() : B((A*)this, this) { }
	// };
	// B::B(V* v, A* a) {
	// f(); //calls V::f, not A::f
	// g(); //calls B::g, not D::g
	// v->g(); // v is base of B, the call is welldefined, calls B::g
	// a->f(); //undefined behavior, a's type not a base of B
	// }
	public void test12_7s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class V {
	// public:
	// virtual void f();
	// };
	// class A : public virtual V { };
	// class B : public virtual V {
	// public:
	// B(V*, A*);
	// };
	// class D : public A, B {
	// public:
	// D() : B((A*)this, this) { }
	// };
	// B::B(V* v, A* a) {
	// typeid(*this); // type_info for B
	// typeid(*v); //welldefined: *v has type V, a base of B
	// // yields type_info for B
	// typeid(*a); //undefined behavior: type A not a base of B
	// dynamic_cast<B*>(v); // welldefined: v of type V*, V base of B
	// // results in B*
	// dynamic_cast<B*>(a); // undefined behavior,
	// // a has type A*, A not a base of B
	// }
	public void test12_7s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// // ...
	// public:
	// X(int);
	// X(const X&, int = 1);
	// };
	// X a(1); // calls X(int);
	// X b(a, 0); // calls X(const X&, int);
	// X c = b; // calls X(const X&, int);
	public void test12_8s2a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// // ...
	// public:
	// X(const X&);
	// X(X&); //OK
	// };
	public void test12_8s2b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct X {
	// X(); //default constructor
	// X(X&); //copy constructor with a nonconst parameter
	// };
	// const X cx;
	// X x = cx; // error - X::X(X&) cannot copy cx into x
	public void test12_8s2c() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct S {
	// template<typename T> S(T);
	// };
	// S f();
	// void g() {
	// S a( f() ); // does not instantiate member template
	// }
	public void test12_8s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void h(int());
	// void h(int (*)()); // redeclaration of h(int())
	// void h(int x()) { } // definition of h(int())
	// void h(int (*x)()) { } // illformed: redefinition of h(int())
	public void test12_8s3d() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 1);
	}

	// struct X {
	// X(const X&, int);
	// };
	public void test12_8s4a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct X {
	// X(const X&, int);
	// };
	// X::X(const X& x, int i =0) {  }
	public void test12_8s4b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct X {
	// X();
	// X& operator=(X&);
	// };
	// const X cx;
	// X x;
	// void f() {
	// x = cx; // error:
	// // X::operator=(X&) cannot assign cx into x
	// }
	public void test12_8s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct V { };
	// struct A : virtual V { };
	// struct B : virtual V { };
	// struct C : B, A { };
	public void test12_8s13() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class Thing {
	// public:
	// Thing();
	// ~Thing();
	// Thing(const Thing&);
	// Thing operator=(const Thing&);
	// void fun();
	// };
	// Thing f() {
	// Thing t;
	// return t;
	// }
	// Thing t2 = f();
	public void test12_8s15() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// double abs(double);
	// int abs(int);
	// int foo() {
	// abs(1); //call abs(int);
	// abs(1.0); //call abs(double);
	// }
	public void test13s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// static void f();
	// void f(); // illformed
	// void f() const; // illformed
	// void f() const volatile; // illformed
	// void g();
	// void g() const; // OK: no static g
	// void g() const volatile; // OK: no static g
	// };
	public void test12_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef int Int;
	// void f(int i);
	// void f(Int i); // OK: redeclaration of f(int)
	// void f(int i) {  }
	// void f(Int i) {  } // error: redefinition of f(int)
	public void test12_1s3a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 1);
	}

	// enum E { a };
	// void f(int i) { }
	// void f(E i) {  }
	public void test12_1s3b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int f(char*);
	// int f(char[]); // same as f(char*);
	// int f(char[7]); // same as f(char*);
	// int f(char[9]); // same as f(char*);
	// int g(char(*)[10]);
	// int g(char[5][10]); // same as g(char(*)[10]);
	// int g(char[7][10]); // same as g(char(*)[10]);
	// int g(char(*)[20]); // different from g(char(*)[10]);
	public void test12_1s3c() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef const int cInt;
	// int f (int);
	// int f (const int); // redeclaration of f(int)
	// int f (int) {  } // definition of f(int)
	// int f (cInt) {  } // error: redefinition of f(int)
	public void test12_8s3e() throws Exception {
		String[] problems= {"f"};
		parse(getAboveComment(), ParserLanguage.CPP, problems);
	}

	// void f (int i, int j);
	// void f (int i, int j = 99); // OK: redeclaration of f(int, int)
	// void f (int i = 88, int j); // OK: redeclaration of f(int, int)
	// void f (); // OK: overloaded declaration of f
	// void prog ()
	// {
	// f (1, 2); // OK: call f(int, int)
	// f (1); // OK: call f(int, int)
	// f (); // Error: f(int, int) or f()?
	// }
	public void test12_8s3f() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class B {
	// public:
	// int f(int);
	// };
	// class D : public B {
	// public:
	// int f(char*);
	// };
	public void test13_2s1a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class B {
	// public:
	// int f(int);
	// };
	// class D : public B {
	// public:
	// int f(char*);
	// };
	// void h(D* pd)
	// {
	// pd->f(1); //error:
	// // D::f(char*) hides B::f(int)
	// pd->B::f(1); //OK
	// pd->f("Ben"); //OK, calls D::f
	// }
	public void test13_2s1b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// int f(char*);
	// void g()
	// {
	// extern int f(int);
	// f("asdf"); //error: f(int) hides f(char*)
	// // so there is no f(char*) in this scope
	// }
	// void caller ()
	// {
	// extern void callee(int, int);
	// {
	// extern void callee(int); // hides callee(int, int)
	// callee(88, 99); // error: only callee(int) in scope
	// }
	// }
	public void test13_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class buffer {
	// private:
	// char* p;
	// int size;
	// protected:
	// buffer(int s, char* store) { size = s; p = store; }
	// // ...
	// public:
	// buffer(int s) { p = new char[size = s]; }
	// // ...
	// };
	public void test13_2s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class T {
	// public:
	// T();
	// // ...
	// };
	// class C : T {
	// public:
	// C(int);
	// // ...
	// };
	// T a = 1; // illformed: T(C(1)) not tried
	public void test13_3_1s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int f1(int);
	// int f2(float);
	// typedef int (*fp1)(int);
	// typedef int (*fp2)(float);
	// struct A {
	// operator fp1() { return f1; }
	// operator fp2() { return f2; }
	// } a;
	// int i = a(1); // Calls f1 via pointer returned from
	// // conversion function
	public void test13_3_1_1_2s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class String {
	// public:
	// String (const String&);
	// String (char*);
	// operator char* ();
	// };
	// String operator + (const String&, const String&);
	// void f(void)
	// {
	// char* p= "one" + "two"; // illformed because neither
	// // operand has user defined type
	// int I = 1 + 1; // Always evaluates to 2 even if
	// // user defined types exist which
	// // would perform the operation.
	// }
	public void test13_3_1_2s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// operator int();
	// };
	// A operator+(const A&, const A&);
	// void m() {
	// A a, b;
	// a + b; // operator+(a,b) chosen over int(a) + int(b)
	// }
	public void test13_3_1_2s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// A();
	// operator int();
	// operator double();
	// } a;
	// int i = a; // a.operator int() followed by no conversion
	// // is better than a.operator double() followed by
	// // a conversion to int
	// float x = a; // ambiguous: both possibilities require conversions,
	// // and neither is better than the other
	public void test13_3_3s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void Fcn(const int*, short);
	// void Fcn(int*, int);
	// int i;
	// short s = 0;
	// void f() {
	// Fcn(&i, s); // is ambiguous because
	// // &i -> int* is better than &i -> const int*
	// // but s -> short is also better than s -> int
	// Fcn(&i, 1L); // calls Fcn(int*, int), because
	// // &i -> int* is better than &i -> const int*
	// // and 1L -> short and 1L -> int are indistinguishable
	// Fcn(&i,'c'); //callsFcn(int*, int), because
	// // &i -> int* is better than &i -> const int*
	// // and c -> int is better than c -> short
	// }
	public void test13_3_3s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class B;
	// class A { A (B&); };
	// class B { operator A (); };
	// class C { C (B&); };
	// void f(A) { }
	// void f(C) { }
	// B b;
	// f(b); //ambiguous because b -> C via constructor and
	// // b -> A via constructor or conversion function.
	public void test13_3_3_1_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct A {};
	// struct B : public A {} b;
	// int f(A&);
	// int f(B&);
	// int i = f(b); // Calls f(B&), an exact match, rather than
	// // f(A&), a conversion
	public void test13_3_3_1_4s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int f(const int *);
	// int f(int *);
	// int i;
	// int j = f(&i); // Calls f(int *)
	public void test13_3_3_2s3a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int f(const int &);
	// int f(int &);
	// int g(const int &);
	// int g(int);
	// int i;
	// int j = f(i); // Calls f(int &)
	// int k = g(i); // ambiguous
	// class X {
	// public:
	// void f() const;
	// void f();
	// };
	// void g(const X& a, X b)
	// {
	// a.f(); //CallsX::f() const
	// b.f(); //Calls X::f()
	// }
	public void test13_3_3_2s3b() throws Exception {
		String[] problems= {"g"};
		parse(getAboveComment(), ParserLanguage.CPP, problems);
	}

	// struct A {
	// operator short();
	// } a;
	// int f(int);
	// int f(float);
	// int i = f(a); // Calls f(int), because short -> int is
	// // better than short -> float.
	public void test13_3_3_2s3c() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {};
	// struct B : public A {};
	// struct C : public B {};
	// C *pc;
	// int f(A *);
	// int f(B *);
	// int i = f(pc); // Calls f(B *)
	public void test13_3_3_2s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct X {
	// int f(int);
	// static int f(long);
	// };
	// int (X::*p1)(int) = &X::f; // OK
	// int (*p2)(int) = &X::f; // error: mismatch
	// int (*p3)(long) = &X::f; // OK
	// int (X::*p4)(long) = &X::f; // error: mismatch
	// int (*p6)(long) = &(X::f); // OK
	public void test13_4s5b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class complex {
	// public:
	// complex operator+(complex a) {}
	// };
	// int n;
	// int foo(complex &a, complex &b) {
	// complex z = a.operator+(b); // complex z = a+b;
	// void* p = operator new(sizeof(int)*n);
	// }
	public void test13_5s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct B {
	// virtual int operator= (int);
	// virtual B& operator= (const B&);
	// };
	// struct D : B {
	// virtual int operator= (int);
	// virtual D& operator= (const B&);
	// };
	// D dobj1;
	// D dobj2;
	// B* bptr = &dobj1;
	// void f() {
	// bptr->operator=(99); // calls D::operator=(int)
	// *bptr = 99; // ditto
	// bptr->operator=(dobj2); // calls D::operator=(const B&)
	// *bptr = dobj2; // ditto
	// dobj1 = dobj2; // calls implicitlydeclared
	// // D::operator=(const D&)
	// }
	public void test13_5_3s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// public:
	// X& operator++(); // prefix ++a
	// X operator++(int); // postfix a++
	// };
	// class Y { };
	// Y& operator++(Y&); // prefix ++b
	// Y operator++(Y&, int); // postfix b++
	// void f(X a, Y b) {
	// ++a; // a.operator++();
	// a++; // a.operator++(0);
	// ++b; // operator++(b);
	// b++; // operator++(b, 0);
	// a.operator++(); // explicit call: like ++a;
	// a.operator++(0); // explicit call: like a++;
	// operator++(b); //explicit call: like ++b;
	// operator++(b, 0); // explicit call: like b++;
	// }
	public void test13_5_7s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<const X& x, int i> void f()
	// {
	// i++; //error: change of template parameter
	// value
	// &x; //OK
	// &i; //error: address of nonreference template parameter
	// int& ri = i; // error: nonconst reference bound to temporary
	// const int& cri = i; // OK: const reference bound to temporary
	// }
	public void test14_1s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<double d> class X; // error
	// template<double* pd> class Y; // OK
	// template<double& rd> class Z; // OK
	public void test14_1s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<int *a> struct R {  };
	// template<int b[5]> struct S {  };
	// int *p;
	// R<p> w; // OK
	// S<p> x; // OK due to parameter adjustment
	// int v[5];
	// R<v> y; // OK due to implicit argument conversion
	// S<v> z; // OK due to both adjustment and conversion
	public void test14_1s8() throws Exception { // TODO raised bug 90668
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T1, class T2 = int> class A;
	// template<class T1 = int, class T2> class A;
	public void test14_1s10a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T1 = int, class T2 = int> class A;
	public void test14_1s10b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T1 = int, class T2> class B; // error
	public void test14_1s11() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T = int> class X;
	// template<class T = int> class X {  }; // error
	public void test14_1s12() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T, T* p, class U = T> class X {  };
	// template<class T> void f(T* p = new T);
	public void test14_1s13() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<int i = (3 > 4) > // OK
	// class Y {  };
	public void test14_1s15() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<int i> class X {  };
	// X<(1>2)> x2; // OK
	// template<class T> class Y {  };
	// Y< X<1> > x3; // OK
	// // with C++0x this is no longer valid:
	// // Y<X<6>> 1> > x4; // OK: Y< X< (6>>1) > >
	public void test14_2s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// public:
	// template<size_t> X* alloc();
	// template<size_t> static X* adjust();
	// };
	// template<class T> void f(T* p)
	// {
	// T* p1 = p>
	// alloc<200>(); // illformed: < means less than
	// T* p2 = p->template alloc<200>();
	// // OK: < starts template argument list
	// T::adjust<100>();
	// // illformed: < means less than
	// T::template adjust<100>();
	// // OK: < starts explicit qualification
	// }
	public void test14_2s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<typename T> class complex {
	//   	complex(T,T) {}
	// };
	// template<class T> class Array {
	// T* v;
	// int sz;
	// public:
	// explicit Array(int);
	// T& operator[](int);
	// T& elem(int i) { return v[i]; }
	// // ...
	// };
	// Array<int> v1(20);
	// typedef complex<double> dcomplex; 
	// Array<dcomplex> v2(30);
	// Array<dcomplex> v3(40);
	// void bar() {
	// v1[3] = 7;
	// v2[3] = v3.elem(4) = dcomplex(7,8);
	// }
	public void test14_3s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class X {
	// static T t;
	// };
	// class Y {
	// private:
	// struct S {  };
	// X<S> x; // OK: S is accessible
	// // X<Y::S> has a static member of type Y::S
	// // OK: even though Y::S is private
	// };
	// X<Y::S> y; // error: S not accessible
	public void test14_3s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> struct A {
	// ~A();
	// };
	// void f(A<int>* p, A<int>* q) {
	// p->A<int>::~A(); // OK: destructor call
	// q->A<int>::~A<int>(); // OK: destructor call
	// }
	public void test14_3s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <class T> class X {  };
	// void f()
	// {
	// struct S {  };
	// X<S> x3; // error: local type used as templateargument
	// X<S*> x4; // error: pointer to local type used as templateargument
	// }
	public void test14_3_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> struct A {
	// static T t;
	// };
	// typedef int function();
	// A<function> a; // illformed: would declare A<function>::t
	// // as a static member function
	public void test14_3_1s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class A { // primary template
	// int x;
	// };
	// template<class T> class A<T*> { // partial specialization
	// long x;
	// };
	// template<template<class U> class V> class C {
	// V<int> y;
	// V<int*> z;
	// };
	// C<A> c; // V<int> within C<A> uses the primary template,
	// // so c.y.x has type int
	// // V<int*> within C<A> uses the partial specialization,
	// // so c.z.x has type long
	public void test14_3_3s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class E, int size> class buffer {  };
	// buffer<char,2*512> x;
	// buffer<char,1024> y;
	public void test14_2s1a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T, void(*err_fct)()> class list {  };
	// list<int,&error_handler1> x1;
	// list<int,&error_handler2> x2;
	// list<int,&error_handler2> x3;
	// list<char,&error_handler2> x4;
	public void test14_4s1b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> class Array {
	// T* v;
	// int sz;
	// public:
	// explicit Array(int);
	// T& operator[](int);
	// T& elem(int i) { return v[i]; }
	// // ...
	// };
	public void test14_5_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T1, class T2> struct A {
	// void f1();
	// void f2();
	// };
	// template<class T2, class T1> void A<T2,T1>::f1() { } // OK
	public void test14_5_1s3a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}


	// void error(const char*);
	// template<class T> class Array {
	// T* v;
	// int sz;
	// public:
	// explicit Array(int);
	// T& operator[](int);
	// T& elem(int i) { return v[i]; }
	// // ...
	// };
	// template<class T> T& Array<T>::operator[](int i)
	// {
	// if (i<0 || sz<=i) error("Array: range error");
	// return v[i];
	// }
	public void test14_5_1_1s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void test() {
	// Array<int> v1(20);
	// Array<dcomplex> v2(30);
	// v1[3] = 7; // Array<int>::operator[]()
	// v2[3] = dcomplex(7,8); // Array<dcomplex>::operator[]()
	// }
	public void test14_5_1_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> struct A {
	// class B;
	// };
	// A<int>::B* b1; // OK: requires A to be defined but not A::B
	// template<class T> class A<T>::B { };
	// A<int>::B b2; // OK: requires A::B to be defined
	public void test14_5_1_2s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class string {
	// public:
	// template<class T2> int compare(const T2&);
	// template<class T2> string(const string<T2>& s) {  }
	// // ...
	// };
	// template<class T> template<class T2> int string<T>::compare(const T2& s)
	// {
	// // ...
	// }
	public void test14_5_2s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}



	// class B {
	// virtual void f(int);
	// };
	// class D : public B {
	// template <class T> void f(T); // does not override B::f(int)
	// void f(int i) { f<>(i); } // overriding function that calls
	// // the template instantiation
	// };
	public void test14_5_2s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// template <class T> operator T*();
	// };
	// template <class T> A::operator T*(){ return 0; }
	// template <> A::operator char*(){ return 0; } // specialization
	// template A::operator void*(); // explicit instantiation
	// int main()
	// {
	// A a;
	// int* ip;
	// ip = a.operator int*(); // explicit call to template operator
	// // A::operator int*()
	// }
	public void test14_5_2s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class X {
	// static T s;
	// };
	// template<class T> T X<T>::s = 0;
	public void test14_5_1_3s1() throws Exception { 
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
	public void test14_5_4s1() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace N {
	// template <class T> void f(T);
	// void g(int);
	// namespace M {
	// template <class T> void h(T);
	// template <class T> void i(T);
	// struct A {
	// friend void f<>(int); // illformed- N::f
	// friend void h<>(int); // OK - M::h
	// friend void g(int); // OK - new decl of M::g
	// template <class T> void i(T);
	// friend void i<>(int); // illformed- A::i
	// };
	// }
	// }
	public void test14_5_4s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class A {
	// template<class T> friend class B; // OK
	// template<class T> friend void f(T){  } // OK
	// };
	public void test14_5_4s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X {
	// template<class T> friend struct A;
	// class Y { };
	// };
	// template<class T> struct A { X::Y ab; }; // OK
	// template<class T> struct A<T*> { X::Y ab; }; // OK
	public void test14_5_4s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> struct A {
	// struct B { };
	// void f();
	// };
	// class C {
	// template<class T> friend struct A<T>::B;
	// template<class T> friend void A<T>::f();
	// };
	public void test14_5_4s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> struct A {
	// template<class T2> struct B {}; // #1
	// template<class T2> struct B<T2*> {}; // #2
	// };
	// template<> template<class T2> struct A<short>::B {}; // #3
	// A<char>::B<int*> abcip; // uses #2
	// A<short>::B<int*> absip; // uses #3
	// A<char>::B<int> abci; // uses #1
	public void test14_5_5_3s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T1, class T2, int I> class A { }; // #1
	// template<class T, int I> class A<T, T*, I> { }; // #2
	// template<class T1, class T2, int I> class A<T1*, T2, I> { }; // #3
	// template<class T> class A<int, T*, 5> { }; // #4
	// template<class T1, class T2, int I> class A<T1, T2*, I> { }; // #5
	public void test14_5_5s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <int I, int J> struct B {};
	// template <int I> struct B<I, I> {}; // OK
	public void test14_5_5s9b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T1, class T2, int I> class A { }; // #1
	// template<class T, int I> class A<T, T*, I> { }; // #2
	// template<class T1, class T2, int I> class A<T1*, T2, I> { }; // #3
	// template<class T> class A<int, T*, 5> { }; // #4
	// template<class T1, class T2, int I> class A<T1, T2*, I> { }; // #5
	// A<int, int, 1> a1; // uses #1
	// A<int, int*, 1> a2; // uses #2, T is int, I is 1
	// A<int, char*, 5> a3; // uses #4, T is char
	// A<int, char*, 1> a4; // uses #5, T1 is int, T2 is char, I is 1
	public void test14_5_5_1s2a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T1, class T2, int I> class A { };             // #1
	// template<class T, int I>            class A<T, T*, I> { };   // #2
	// template<class T1, class T2, int I> class A<T1*, T2, I> { }; // #3
	// template<class T>                   class A<int, T*, 5> { }; // #4
	// template<class T1, class T2, int I> class A<T1, T2*, I> { }; // #5
	// A<int*, int*, 2> a5; // ambiguous: matches #3 and #5 : expect problem 
	public void test14_5_5_1s2b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 1);
	}

	// template<int I, int J, class T> class X { };
	// template<int I, int J>          class X<I, J, int> { }; // #1
	// template<int I>                 class X<I, I, int> { }; // #2
	// template<int I, int J> void f(X<I, J, int>); // #A
	// template<int I>        void f(X<I, I, int>); // #B
	public void test14_5_5_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// // primary template
	// template<class T, int I> struct A {
	// void f();
	// };
	// template<class T, int I> void A<T,I>::f() { }
	// // class template partial specialization
	// template<class T> struct A<T,2> {
	// void f();
	// void g();
	// void h();
	// };
	// // member of class template partial specialization
	// template<class T> void A<T,2>::g() { }
	// // explicit specialization
	// template<> void A<char,2>::h() { }
	// int main()
	// {
	// A<char,0> a0;
	// A<char,2> a2;
	// a0.f(); //OK, uses definition of primary template's member
	// a2.g(); //OK, uses definition of
	// // partial specialization's member
	// a2.h(); //OK, uses definition of
	// // explicit specialization's member
	// a2.f(); //illformed, no definition of f for A<T,2>
	// // the primary template is not used here
	// }
	public void test14_5_5_3s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> class Array { };
	// template<class T> void sort(Array<T>&);
	public void test14_5_6s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// // file1.c 
	// template<class T>
	// void f(T*);
	// void g(int* p) { 
	// f(p); // call 
	// // f<int>(int*) 
	// }
	public void test14_5_6_1s1a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}
	// // file2.c
	// template<class T>
	// void f(T);
	// void h(int* p) {
	// f(p); // call
	// // f<int*>(int*)
	// }
	public void test14_5_6_1s1b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// // Guaranteed to be the same
	// template <int T> class A;
	// template <int I> void f/*1*/(A<I>, A<I+10>);
	// template <int I> void f/*2*/(A<I>, A<I+10>);
	// // Guaranteed to be different
	// template <int I> void f/*3*/(A<I>, A<I+11>);
	public void test14_5_6_1s8a() throws Exception {
		final String content= getAboveComment();
		IASTTranslationUnit tu= parse(content, ParserLanguage.CPP, true, 0);
		BindingAssertionHelper bh= new BindingAssertionHelper(content, true);
		ICPPFunctionTemplate f1= bh.assertNonProblem("f/*1*/", 1);
		ICPPFunctionTemplate f2= bh.assertNonProblem("f/*2*/", 1);
		ICPPFunctionTemplate f3= bh.assertNonProblem("f/*3*/", 1);
		assertSame(f1, f2);
		assertNotSame(f1, f3);
	}

	// // Illformed, no diagnostic required
	// template <int I> void f(A<I>, A<I+10>);
	// template <int I> void f(A<I>, A<I+1+2+3+4>);
	public void test14_5_6_1s8b() throws Exception {
		//test is only for syntax, semantics are not checked here.
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	//	struct A { };
	//	template<class T> struct B {
	//		template<class R> int operator*(R&); // #1
	//	};
	//	template<class T, class R> int operator*(T&, R&); // #2
	//	// The declaration of B::operator* is transformed into the equivalent of
	//	// template<class R> int operator*(B<A>&, R&); // #1a
	//	int main() {
	//		A a;
	//		B<A> b;
	//		b * a; // calls #1a
	//	}
	public void test14_5_6_2s3() throws Exception {
		String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		IBinding op1= bh.assertNonProblem("operator*(R&)", -4);
		IASTImplicitName name= bh.assertImplicitName("* a", 1, ICPPFunction.class);
		ICPPTemplateInstance inst= (ICPPTemplateInstance) name.resolveBinding();
		ICPPSpecialization templateSpecialization = (ICPPSpecialization) inst.getTemplateDefinition();
		assertSame(op1, templateSpecialization.getSpecializedBinding());
	}
	
	// template<class T> struct A { A(); };
	// template<class T> void f(T);
	// template<class T> void f(T*);
	// template<class T> void f(const T*);
	// template<class T> void g(T);
	// template<class T> void g(T&);
	// template<class T> void h(const T&);
	// template<class T> void h(A<T>&);
	// void m() {
	// const int *p;
	// f(p); // f(const T*) is more specialized than f(T) or f(T*)
	// float x;
	// g(x); //Ambiguous: g(T) or g(T&)
	// A<int> z;
	// h(z); //overload resolution selects h(A<T>&)
	// const A<int> z2;
	// h(z2); // h(const T&) is called because h(A<T>&) is not callable
	// }
	public void test14_5_6_2s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> void f(T); // #1
	// template<class T> void f(T*, int=1); // #2
	// template<class T> void g(T); // #3
	// template<class T> void g(T*, ...); // #4
	// int main() {
	// int* ip;
	// f(ip); //calls #2
	// g(ip); //calls #4
	// }
	public void test14_5_6_2s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// // no B declared here
	// class X;
	// template<class T> class Y {
	// class Z; // forward declaration of member class
	// void f() {
	// X* a1; // declare pointer to X
	// T* a2; // declare pointer to T
	// Y* a3; // declare pointer to Y<T>
	// Z* a4; // declare pointer to Z
	// typedef typename T::A TA;
	// TA* a5; // declare pointer to T's A
	// typename T::A* a6; // declare pointer to T's A
	// T::A* a7; // T::A is not a type name:
	// // multiply T::A by a7; illformed,
	// // no visible declaration of a7
	// B* a8; // B is not a type name:
	// // multiply B by a8; illformed,
	// // no visible declarations of B and a8
	// }
	// };
	public void test14_6s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct A {
	// struct X { };
	// int X;
	// };
	// template<class T> void f(T t) {
	// typename T::X x; // illformed: finds the data member X
	// // not the member type X
	// }
	public void test14_6s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> struct A {
	// typedef int B;
	// A::B b; // illformed: typename required before A::B
	// void f(A<T>::B); // illformed: typename required before A<T>::B
	// typename A::B g(); // OK
	// };
	public void test14_6s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// int j;
	// template<class T> class X {
	// // ...
	// void f(T t, int i, char* p)
	// {
	// t = i; // diagnosed if X::f is instantiated
	// // and the assignment to t is an error
	// p = i; // may be diagnosed even if X::f is
	// // not instantiated
	// p = j; // may be diagnosed even if X::f is
	// // not instantiated
	// }
	// void g(T t) {
	// // +; //may be diagnosed even if X::g is
	// // not instantiated
	// }
	// };
	public void test14_6s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// //#include <iostream>
	// using namespace std;
	// template<class T> class Set {
	// T* p;
	// int cnt;
	// public:
	// Set();
	// Set<T>(const Set<T>&);
	// void printall()
	// {
	// for (int i = 0; i<cnt; i++)
	// cout << p[i] << '\
	// ';
	// }
	// // ...
	// };
	public void test14_6s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// void f(char);
	// template<class T> void g(T t)
	// {
	// f(1); // f(char)
	// f(T(1)); //dependent
	// f(t); //dependent
	// dd++; //not dependent
	// // error: declaration for dd not found
	// }
	// void f(int);
	// double dd;
	// void h()
	// {
	// g(2); //will cause one call of f(char) followed
	// // by two calls of f(int)
	// g('a'); //will cause three calls of f(char)
	// }
	public void test14_6s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T, T* p, class U = T> class X {  };
	// template<class T> void f(T* p = new T);
	public void test14_6_1s3a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class X {
	// X* p; // meaning X<T>
	// X<T>* p2;
	// X<int>* p3;
	// };
	public void test14_6_1s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class Y;
	// template<> class Y<int> {
	// Y* p; // meaning Y<int>
	// Y<char>* q; // meaning Y<char>
	// };
	public void test14_6_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<typename T> class Array {};
	// template<class T> class X : public Array<T> {  };
	// template<class T> class Y : public T { };
	public void test14_6_1s3b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T, int i> class Y {
	// int T; // error: templateparameter redeclared
	// void f() {
	// char T; // error: templateparameter redeclared
	// }
	// };
	// template<class X> class X; // error: templateparameter redeclared
	public void test14_6_1s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> struct A {
	// struct B {  };
	// void f();
	// };
	// template<class B> void A<B>::f() {
	// B b; // A's B, not the template parameter
	// }
	public void test14_6_1s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace N {
	// int C;
	// template<class T> class B {
	// void f(T);
	// };
	// }
	// template<class C> void N::B<C>::f(C) {
	// C b; // C is the template parameter, not N::C
	// }
	public void test14_6_1s6() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// struct B {  };
	// int a;
	// int Y;
	// };
	// template<class B, class a> struct X : A {
	// B b; // A's B
	// a b; // error: A's a isn't a type name
	// };
	public void test14_6_1s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template <typename T> class B {};
	// template<class T> struct X : B<T> {
	// typename T::A* pa;
	// void f(B<T>* pb) {
	// static int i = B<T>::i;
	// pb->j++;
	// }
	// };
	public void test14_6_2s2() throws Exception {
		final String code = getAboveComment();
		parse(code, ParserLanguage.CPP, true, 0);
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPUnknownBinding unknown= bh.assertNonProblem("B<T>", 4);
		unknown= bh.assertNonProblem("T::A", 4);
		unknown= bh.assertNonProblem("B<T>::i", 7);
		unknown= bh.assertNonProblem("j", 1);
	}

	// typedef double A;
	// template<class T> class B {
	// typedef int A;
	// };
	// template<class T> struct X : B<T> {
	// A a; // a has type double
	// };
	public void test14_6_2s3() throws Exception { 
		final String content= getAboveComment();
		IASTTranslationUnit tu= parse(content, ParserLanguage.CPP, true, 0);
		BindingAssertionHelper bh= new BindingAssertionHelper(content, true);
		IVariable v= bh.assertNonProblem("a;", 1);
		IType t= v.getType();
		assertInstance(t, ITypedef.class);
		t= ((ITypedef) t).getType();
		assertInstance(t, IBasicType.class);
		assertEquals(IBasicType.t_double, ((IBasicType) t).getType());
	}

	// struct A {
	// struct B {  };
	// int a;
	// int Y;
	// };
	// int a;
	// template<class T> struct Y : T {
	// struct B {  };
	// B b; // The B defined in Y
	// void f(int i) { a = i; } // ::a
	// Y* p; // Y<T>
	// };
	// Y<A> ya;
	public void test14_6_2s4() throws Exception {
		final String content= getAboveComment();
		parse(content, ParserLanguage.CPP, true, 0);
		BindingAssertionHelper bh= new BindingAssertionHelper(content, true);
		IBinding b= bh.assertNonProblem("b;", 1);
		assertEquals("Y", b.getOwner().getName());
		b= bh.assertNonProblem("a = i", 1);
		assertNull(b.getOwner());
	}

	// void g(double);
	// void h();
	// template<class T> class Z {
	// public:
	// void f() {
	// g(1); //calls g(double)
	// h++; //illformed:
	// // cannot increment function;
	// // this could be diagnosed either here or
	// // at the point of instantiation
	// }
	// };
	// void g(int); // not in scope at the point of the template
	// // definition, not considered for the call g(1)
	public void test14_6_3s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<typename T> class number {
	// number(int);
	// //...
	// friend number gcd(number& x, number& y) {  }
	// //...
	// };
	// void g()
	// {
	// number<double> a(3), b(4);
	// //...
	// a = gcd(a,b); // finds gcd because number<double> is an
	// // associated class, making gcd visible
	// // in its namespace (global scope)
	// b = gcd(3,4); // illformed; gcd is not visible
	// }
	public void test14_6_5s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> class X {
	// static T s;
	// // ...
	// };
	// template<class T> T X<T>::s = 0;
	// X<int> aa;
	// X<char*> bb;
	public void test14_7s6() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class Z {
	// public:
	// void f();
	// void g();
	// };
	// void h()
	// {
	// Z<int> a; // instantiation of class Z<int> required
	// Z<char>* p; // instantiation of class Z<char> not
	// // required
	// Z<double>* q; // instantiation of class Z<double>
	// // not required
	// a.f(); //instantiation of Z<int>::f() required
	// p->g(); //instantiation of class Z<char> required, and
	// // instantiation of Z<char>::g() required
	// }
	public void test14_7_1s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class B {  };
	// template<class T> class D : public B<T> {  };
	// void f(void*);
	// void f(B<int>*);
	// void g(D<int>* p, D<char>* pp, D<double> ppp)
	// {
	// f(p); //instantiation of D<int> required: call f(B<int>*)
	// B<char>* q = pp; // instantiation of D<char> required:
	// // convert D<char>* to B<char>*
	// delete ppp; // instantiation of D<double> required
	// }
	public void test14_7_1s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class X;
	// X<char> ch; // error: definition of X required
	public void test14_7_1s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T = int> struct A {
	// static int x;
	// };
	// template<class U> void g(U) { }
	// template<> struct A<double> { }; // specialize for T == double
	// template<> struct A<> { }; // specialize for T == int
	// template<> void g(char) { } // specialize for U == char
	// // U is deduced from the parameter type
	// template<> void g<int>(int) { } // specialize for U == int
	// template<> int A<char>::x = 0; // specialize for T == char
	// template<class T = int> struct B {
	// static int x;
	// };
	// template<> int B<>::x = 1; // specialize for T == int
	public void test14_7s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <class T> struct S {
	// operator int();
	// };
	// void f(int);
	// void f(S<int>&);
	// void f(S<float>);
	// void g(S<int>& sr) {
	// f(sr); //instantiation of S<int> allowed but not required
	// // instantiation of S<float> allowed but not required
	// };
	public void test14_7_1s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace N {
	// template<class T> class List {
	// public:
	// T* get();
	// // ...
	// };
	// }
	// template<class K, class V> class Map {
	// N::List<V> lt;
	// V get(K);
	// // ...
	// };
	// void g(Map<char*,int>& m)
	// {
	// int i = m.get("Nicholas");
	// // ...
	// }
	public void test14_7_1s10() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> void f(T x, T y = ydef(T()), T z = zdef(T()));
	// class A { };
	// A zdef(A);
	// void g(A a, A b, A c) {
	// f(a, b, c); // no default argument instantiation
	// f(a, b); // default argument z = zdef(T()) instantiated
	// f(a); //illformed; ydef is not declared
	// }
	public void test14_7_1s12() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> class X {
	// X<T>* p; // OK
	// X<T*> a; // implicit generation of X<T> requires
	// // the implicit instantiation of X<T*> which requires
	// // the implicit instantiation of X<T**> which ...
	// };
	public void test14_7_1s14() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class Array { void mf(); };
	// template class Array<char>;
	// template void Array<int>::mf();
	// template<class T> void sort(Array<T>& v) {  }
	// template void sort(Array<char>&); // argument is deduced here
	// namespace N {
	// template<class T> void f(T&) { }
	// }
	// template void N::f<int>(int&);
	public void test14_7_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace N {
	// template<class T> class Y { void mf() { } };
	// }
	// template class Y<int>; // error: class template Y not visible
	// // in the global namespace
	// using N::Y;
	// template class Y<int>; // OK: explicit instantiation in namespace N
	// template class N::Y<char*>; // OK: explicit instantiation in namespace N
	// template void N::Y<double>::mf(); // OK: explicit instantiation
	// // in namespace N
	public void test14_7_2s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> class Array {  };
	// template<class T> void sort(Array<T>& v);
	// // instantiate sort(Array<int>&) - templateargument deduced
	// template void sort<>(Array<int>&);
	public void test14_7_2s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// char* p = 0;
	// template<class T> T g(T = &p);
	// template int g<int>(int); // OK even though &p isn't an int.
	public void test14_7_2s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class stream;
	// template<> class stream<char> {  };
	// template<class T> class Array {  };
	// template<class T> void sort(Array<T>& v) {  }
	// template<> void sort<char*>(Array<char*>&) ;
	public void test14_7_3s1() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<> class X<int> {  }; // error: X not a template
	// template<class T> class X;
	// template<> class X<char*> {  }; // OK: X is a template
	public void test14_7_3s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> struct A {
	// void f(T) {  }
	// };
	// template<> struct A<int> {
	// void f(int);
	// };
	// void h()
	// {
	// A<int> a;
	// a.f(16); // A<int>::f must be defined somewhere
	// }
	// // explicit specialization syntax not used for a member of
	// // explicitly specialized class template specialization
	// void A<int>::f(int) {  }
	public void test14_7_3s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class Array {  };
	// template<class T> void sort(Array<T>& v) {  }
	// void f(Array<String>& v)
	// {
	// sort(v); //use primary template
	// // sort(Array<T>&), T is String
	// }
	// template<> void sort<String>(Array<String>& v); // error: specialization
	// // after use of primary template
	// template<> void sort<>(Array<char*>& v); // OK: sort<char*> not yet used
	public void test14_7_3s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// namespace N {
	// template<class T> class X {  };
	// template<class T> class Y {  };
	// template<> class X<int> {  }; // OK: specialization
	// // in same namespace
	// template<> class Y<double>; // forward declare intent to
	// // specialize for double
	// }
	// template<> class N::Y<double> {  }; // OK: specialization
	// // in same namespace
	public void test14_7_3s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class Array {  };
	// template<class T> void sort(Array<T>& v);
	// // explicit specialization for sort(Array<int>&)
	// // with deduces templateargument of type int
	// template<> void sort(Array<int>&);
	public void test14_7_3s11() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0 );
	}

	// template<class T1> class A {
	// template<class T2> class B {
	// void mf();
	// };
	// };
	// template<> template<> class A<int>::B<double> { };
	// template<> template<> void A<char>::B<char>::mf() { };
	public void test14_7_3s17() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}
	// template<class T> class X; // X is a class template
	// template<> class X<int>;
	// X<int>* p; // OK: pointer to declared class X<int>
	// X<int> x; // error: object of incomplete class X<int>
	public void test14_7_3s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <class T> void f(T);
	// template <class T> void f(T*);
	// template <> void f(int*); // Ambiguous
	// template <> void f<int>(int*); // OK
	// template <> void f(int); // OK
	public void test14_7_3s12() throws Exception {
		// gcc does not report the explicit instantiation as ambiguous, so we accept it as well.
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> void f(T) {  }
	// template<class T> inline T g(T) {  }
	// template<> inline void f<>(int) {  } // OK: inline
	// template<> int g<>(int) {  } // OK: not inline
	public void test14_7_3s14() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> struct A {
	// void f(T);
	// template<class X> void g(T,X);
	// void h(T) { }
	// };
	// // specialization
	// template<> void A<int>::f(int);
	// // out of class member template definition
	// template<class T> template<class X> void A<T>::g(T,X) { }
	// // member template partial specialization
	// template<> template<class X> void A<int>::g(int,X);
	// // member template specialization
	// template<> template<>
	// void A<int>::g(int,char); // X deduced as char
	// template<> template<>
	// void A<int>::g<char>(int,char); // X specified as char
	// // member specialization even if defined in class definition
	// template<> void A<int>::h(int) { }
	public void test14_7_3s16() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T1> class A {
	// template<class T2> class B {
	// template<class T3> void mf1(T3);
	// void mf2();
	// };
	// };
	// template<> template<class X>
	// class A<int>::B { };
	// template<> template<> template<class T>
	// void A<int>::B<double>::mf1(T t) { };
	// template<class Y> template<>
	// void A<Y>::B<double>::mf2() { }; // illformed; B<double> is specialized but
	// // its enclosing class template A is not
	public void test14_7_3s18() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> void f(T* p)
	// {
	// static T s;
	// // ...
	// };
	// void g(int a, char* b)
	// {
	// f(&a); //call f<int>(int*)
	// f(&b); //call f<char*>(char**)
	// }
	public void test14_8s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> void sort(Array<T>& v);
	// void f(Array<dcomplex>& cv, Array<int>& ci)
	// {
	// sort<dcomplex>(cv); // sort(Array<dcomplex>&)
	// sort<int>(ci); // sort(Array<int>&)
	// }
	// template<class U, class V> U convert(V v);
	// void g(double d)
	// {
	// int i = convert<int,double>(d); // int convert(double)
	// char c = convert<char,double>(d); // char convert(double)
	// }
	public void test14_8_1s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class X, class Y> X f(Y);
	// void g()
	// {
	// int i = f<int>(5.6); // Y is deduced to be double
	// int j = f(5.6); // illformed: X cannot be deduced
	// }
	public void test14_8_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class X, class Y, class Z> X f(Y,Z);
	// void g()
	// {
	// f<int,char*,double>("aa",3.0);
	// f<int,char*>("aa",3.0); // Z is deduced to be double
	// f<int>("aa",3.0); // Y is deduced to be char*, and
	// // Z is deduced to be double
	// f("aa",3.0); //error: X cannot be deduced
	// }
	public void test14_8_1s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
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
	public void test14_8_1s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace A {
	// struct B { };
	// template<int X> void f();
	// }
	// namespace C {
	// template<class T> void f(T t);
	// }
	// void g(A::B b) {
	// f<3>(b); //illformed: not a function call
	// A::f<3>(b); //wellformed
	// C::f<3>(b); //illformed; argument dependent lookup
	// // only applies to unqualified names
	// using C::f;
	// f<3>(b); //wellformed because C::f is visible; then
	// // A::f is found by argument dependent lookup
	// }
	public void test14_8_1s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// void f(Array<dcomplex>& cv, Array<int>& ci)
	// {
	// sort(cv); //call sort(Array<dcomplex>&)
	// sort(ci); //call sort(Array<int>&)
	// }
	// void g(double d)
	// {
	// int i = convert<int>(d); // call convert<int,double>(double)
	// int c = convert<char>(d); // call convert<char,double>(double)
	// }
	public void test14_8_2s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
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
	public void test14_8_2s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	//	template <class T, class U = double>
	//	void f(T t = 0, U u = 0);
	//	void g() {
	//		f(1, 'c'); // f<int,char>(1,'c')
	//		f(1); // f<int,double>(1,0)
	//		f(); // error: T cannot be deduced
	//		f<int>(); // f<int,double>(0,0)
	//		f<int,char>(); // f<int,char>(0,0)
	//	}
	public void test14_8_2s5() throws Exception {
		final String content= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(content, true);
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f(1, 'c')", 1);
		assertEquals("<int,char>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		inst= bh.assertNonProblem("f(1)", 1);
		assertEquals("<int,double>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		bh.assertProblem("f()", 1);
		inst= bh.assertNonProblem("f<int>()", -2);
		assertEquals("<int,double>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		inst= bh.assertNonProblem("f<int,char>()", -2);
		assertEquals("<int,char>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
	}
	
	//	struct X { };
	//	struct Y {
	//		Y(X){}
	//	};
	//	template <class T> auto f(T t1, T t2) -> decltype(t1 + t2); // #1
	//	X f(Y, Y); // #2
	//	X x1, x2;
	//	X x3 = f(x1, x2); // deduction fails on #1 (cannot add X+X), calls #2
	public void test14_8_2s8a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}		
	
	// template <class T> int f(T[5]);
	// int I = f<int>(0);
	// int j = f<void>(0); // invalid array // also no error with gcc
	public void _test14_8_2s8b() throws Exception {
		final String content= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(content, true);
		bh.assertNonProblem("f<int>(0)", -3);
		bh.assertProblem("f<void>(0)", -3);
	}

	// template <class T> int f(typename T::B*);
	// int i = f<int>(0);
	public void test14_8_2s8c() throws Exception {
		final String content= getAboveComment();
		IASTTranslationUnit tu= parse(content, ParserLanguage.CPP, true, 2);
		BindingAssertionHelper bh= new BindingAssertionHelper(content, true);
		bh.assertProblem("f<", 1);
		bh.assertProblem("f<int>", 6);
	}

	//	template <int I> struct X { };
	//	template <template <class T> class> struct Z { };
	//	template <class T> void f(typename T::Y*){}
	//	template <class T> void g(X<T::N>*){}
	//	template <class T> void h(Z<T::template TT>*){}
	//	struct A {};
	//	struct B { int Y; };
	//	struct C {
	//		typedef int N;
	//	};
	//	struct D {
	//		typedef int TT;
	//	};
	//	int main() {
	//		// Deduction fails in each of these cases:
	//		f<A>(0); // A does not contain a member Y
	//		f<B>(0); // The Y member of B is not a type
	//		g<C>(0); // The N member of C is not a non-type
	//		h<D>(0); // The TT member of D is not a template
	//	}
	public void _test14_8_2s8d() throws Exception {
		final String content= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(content, true);
		bh.assertProblem("f<A>", 0);
		bh.assertProblem("f<B>", 0);
		bh.assertProblem("g<C>", 0);
		bh.assertProblem("h<D>", 0);
	}

	// template <class T> int f(int T::*);
	// int i = f<int>(0);
	public void test14_8_2s8e() throws Exception {
		final String code = getAboveComment();
		parse(code, ParserLanguage.CPP, true, 2);
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		bh.assertProblem("f<int>", 0);
	}

	//	template <class T, T> struct S {};
	//	template <class T> int f(S<T, T()>*);
	//	struct X {};
	//	int i0 = f<X>(0);
	public void test14_8_2s8f() throws Exception {
		final String code = getAboveComment();
		parse(code, ParserLanguage.CPP, true, 2);
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		bh.assertProblem("f<X>", 0);
	}
	
	// template <class T, T*> int f(int);
	// int i2 = f<int,1>(0); // can't conv 1 to int*
	public void test14_8_2s8g() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 1);
	}

	// template <int> int f(int);
	// template <signed char> int f(int);
	// int i1 = f<1>(0); // ambiguous
	// int i2 = f<1000>(0); // ambiguous
	public void test14_8_2s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	//	namespace std {
	//		template<typename T> class initializer_list;
	//	}
	//	template<class T> void f(std::initializer_list<T>);
	//	template<class T> void g(T);
	//	void test() {
	//		f({1,2,3}); // T deduced to int
	//		f({1,"asdf"}); // error: T deduced to both int and const char*
	//		g({1,2,3}); // error: no argument deduced for T
	//	}
	public void test14_8_2_1s1a() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
	
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f({1,2,3})", 1);
		assertEquals("<int>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		bh.assertProblem("f({1,\"asdf\"})", 1);
		bh.assertProblem("g({1,2,3})", 1);
	}
	
	//	template<class ... Types> void f(Types& ...);
	//	template<class T1, class ... Types> void g(T1, Types ...);
	//	void h(int x, float& y) {
	//		const int z = x;
	//		f(x, y, z); // Types is deduced to int, float, const int
	//		g(x, y, z); // T1 is deduced to int; Types is deduced to float, int
	//	}
	public void test14_8_2_1s1b() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
	
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f(x, y, z)", 1);
		assertEquals("<int,float,const int>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		inst= bh.assertNonProblem("g(x, y, z)", 1);
		assertEquals("<int,float,int>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
	}
	
	//	template <class T> int f(T&&);
	//	template <class T> int g(const T&&);
	//	int i;
	//	int n1 = f(i); // calls f<int&>(int&)
	//	int n2 = f(0); // calls f<int>(int&&)
	//	int n3 = g(i); // error: would call g<int>(const int&&), which
	//	               // would bind an rvalue reference to an lvalue
	public void test14_8_2_1s3() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f(i)", 1);
		assertEquals("<int &>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		inst= bh.assertNonProblem("f(0)", 1);
		assertEquals("<int>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		bh.assertProblem("g(i)", 1);
	}

	//	// Only one function of an overload set matches the call so the function
	//	// parameter is a deduced context.
	//	template <class T> int f(T (*p)(T));
	//	int g(int);
	//	int g(char);
	//	int i = f(g); // calls f(int (*)(int))
	public void test14_8_2_1s7() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f(g)", 1);
		assertEquals("<int>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
	}
		
	//	// Ambiguous deduction causes the second function parameter to be a
	//	// non-deduced context.
	//	template <class T> int f(T, T (*p)(T));
	//	int g(int);
	//	char g(char);
	//	int i = f(1, g); // calls f(int, int (*)(int))
	public void test14_8_2_1s8() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f(1, g)", 1);
		assertEquals("<int>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
	}

	//	// The overload set contains a template, causing the second function
	//	// parameter to be a non-deduced context.
	//	template <class T> int f(T, T (*p)(T));
	//	char g(char);
	//	template <class T> T g(T);
	//	int i = f(1, g); // calls f(int, int (*)(int))
	public void test14_8_2_1s9() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f(1, g)", 1);
		assertEquals("<int>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
	}
	
	//	struct A {
	//		template <class T> operator T***();
	//	};
	//	A a;
	//	void test(const int * const * const * p1) {
	//     test(a); // T is deduced as int, not const int
	//  }
	public void test14_8_2_3s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}
	
	//	template <class T> T f(int); // #1
	//	template <class T, class U> T f(U); // #2
	//	void g() {
	//		f<int>(1); // calls #1
	//	}
	public void test14_8_2_4s11() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}
		
	//	template<typename...> struct Tuple { };
	//	template<typename... Types> void g(Tuple<Types...>); // #1
	//	template<typename T1, typename... Types> void g(Tuple<T1, Types...>); // #2
	//	template<typename T1, typename... Types> void g(Tuple<T1, Types&...>); // #3
	//  void test() {
	//	  g(Tuple<>()); // calls #1
	//	  g(Tuple<int, float>()); // calls #2
	//	  g(Tuple<int, float&>()); // calls #3
	//	  g(Tuple<int>()); // calls #3	
	// }
	public void test14_8_2_4s12() throws Exception {
		final String code= getAboveComment();
		parse(code, ParserLanguage.CPP, true, 0);
		
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPFunction g1= bh.assertNonProblem("g(Tuple<Types...>)", 1);
		ICPPFunction g2= bh.assertNonProblem("g(Tuple<T1, Types...>)", 1);
		ICPPFunction g3= bh.assertNonProblem("g(Tuple<T1, Types&...>)", 1);
		
		ICPPTemplateInstance x= bh.assertNonProblem("g(Tuple<>())", 1);
		assertSame(g1, x.getTemplateDefinition());
		x= bh.assertNonProblem("g(Tuple<int, float>())", 1);
		assertSame(g2, x.getTemplateDefinition());
		x= bh.assertNonProblem("g(Tuple<int, float&>())", 1);
		assertSame(g3, x.getTemplateDefinition());
		x= bh.assertNonProblem("g(Tuple<int>())", 1);
		assertSame(g3, x.getTemplateDefinition());
	}		
	
	//	template<class T> void g(T);
	//  void test() {
	//	   g({1,2,3}); // error: no argument deduced for T
	//  }
	public void test14_8_2_5s5() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		bh.assertProblem("g({1,2,3})", 1);
	}

	// template<class T> void f(T x, T y) {  }
	// struct A {  };
	// struct B : A { };
	// int g(A a, B b)
	// {
	// f(a,b); //error: T could be A or B
	// f(b,a); //error: T could be A or B
	// f(a,a); //OK: T is A
	// f(b,b); //OK: T is B
	// }
	public void test14_8_2_5s7a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	//	template <class T, class U> void f( T (*)( T, U, U ) );
	//	int g1( int, float, float);
	//	char g2( int, float, float);
	//	int g3( int, char, float);
	//	void r()
	//	{
	//		f(g1); //OK: T is int and U is float
	//		f(g2); //error: T could be char or int
	//		f(g3); //error: U could be char or float
	//	}
	public void test14_8_2_5s7b() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f(g1)", 1);
		assertEquals("<int,float>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		bh.assertProblem("f(g2)", 1);
		bh.assertProblem("f(g3)", 1);
	}

	// template<class T> void f(const T*) {}
	// int *p;
	// void s()
	// {
	// f(p); // f(const int *)
	// }
	public void test14_8_2_5s7c() throws Exception {
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
	public void test14_8_2_5s7d() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}
	
	//	template <class T> void f(T&&);
	//	template <> void f(int&) { } // #1
	//	template <> void f(int&&) { } // #2
	//	void g(int i) {
	//		f(i); // calls f<int&>(int&), i.e., #1
	//		f(0); // calls f<int>(int&&), i.e., #2
	//	}
	public void test14_8_2_5s10() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f(i)", 1);
		assertEquals("<int &>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		inst= bh.assertNonProblem("f(0)", 1);
		assertEquals("<int>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
	}

	// template<class T, T i> void f(double a[10][i]);
	// int v[10][20];
	// int foo() {
	// f(v); //error: argument for templateparameter
	// //T cannot be deduced
	// }
	public void test14_8_2_5s14() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		bh.assertProblem("f(v)", 1);
	}

	// template<int i> void f1(int a[10][i]);
	// template<int i> void f2(int a[i][20]);
	// template<int i> void f3(int (&a)[i][20]);
	// void g()
	// {
	// int v[10][20];
	// f1(v); //OK: i deduced to be 20
	// f1<20>(v); //OK
	// f2(v); //error: cannot deduce templateargument i
	// f2<10>(v); //OK
	// f3(v); //OK: i deduced to be 10
	// }
	public void test14_8_2_5s15() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f1(v)", 2);
		assertEquals("<20>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		inst= bh.assertNonProblem("f1<20>(v)", -3);
		assertEquals("<20>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		bh.assertProblem("f2(v)", 2);
		inst= bh.assertNonProblem("f2<10>(v)", -3);
		assertEquals("<10>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		inst= bh.assertNonProblem("f3(v)", 2);
		assertEquals("<10>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
	}

	//	template <int i> class A { };
	//	template <int i> void g(A<i+1>);
	//	template <int i> void f(A<i>, A<i+1>);
	//	void k() {
	//		A<1> a1;
	//		A<2> a2;
	//		g(a1); // error: deduction fails for expression i+1
	//		g<0>(a1); // OK
	//		f(a1, a2); // OK
	//	}
	public void test14_8_2_5s16a() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPTemplateInstance inst;
		bh.assertProblem("g(a1)", 1);
		inst= bh.assertNonProblem("g<0>(a1)", -4);
		assertEquals("<0>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		inst= bh.assertNonProblem("f(a1, a2)", 1);
		assertEquals("<1>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
	}
	
	//	template<typename T> class A {
	//  public:
	//		typedef int X;
	//	    X xm;
	//	};
	//	template<int I> class B {
	//	public:
	//		typedef int* Y;
	//	    Y ym;
	//	};
	//	template<int i, typename T>
	//	T deduce(typename A<T>::X x, // T is not deduced here
	//			T t,                 // but T is deduced here
	//			typename B<i>::Y y); // i is not deduced here
	//	void test() {
	//		A<int> a;
	//		B<77> b;
	//		deduce<77> (a.xm, 62, b.ym);
	//	}
	public void test14_8_2_5s16b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	
	//	template<int i> class A {};
	//	template<short s> void f(A<s>);
	//	void k1() {
	//		A<1> a;
	//		f(a); // error: deduction fails for conversion from int to short
	//		f<1>(a); // OK
	//	}
	//	template<const short cs> class B { };
	//	template<short s> void g(B<s>);
	//	void k2() {
	//		B<1> b;
	//		g(b); // OK: cv-qualifiers are ignored on template parameter types
	//	}
	public void test14_8_2_5s17() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPTemplateInstance inst;
		bh.assertProblem("f(a)", 1);
		inst= bh.assertNonProblem("f<1>(a)", -3);
		assertEquals("<1>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		inst= bh.assertNonProblem("g(b)", 1);
		assertEquals("<1>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
	}

	//	template<class T> void f(void(*)(T,int));
	//	template<class T> void foo(T,int);
	//	void g(int,int);
	//	void g(char,int);
	//	void h(int,int,int);
	//	void h(char,int);
	//	int m() {
	//		f(&g); // error: ambiguous
	//		f(&h); // OK: void h(char,int) is a unique match
	//		f(&foo); // error: type deduction fails because foo is a template
	//	}
	public void test14_8_2_5s18() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPTemplateInstance inst;
		bh.assertProblem("f(&g)", 1);
		inst= bh.assertNonProblem("f(&h)", 1);
		assertEquals("<char>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		bh.assertProblem("f(&foo)", 1);
	}

	//	template <class T> void f(T = 5, T = 7);
	//	void g() {
	//		f(1); // OK: call f<int>(1,7)
	//		f(); // error: cannot deduce T
	//		f<int>(); // OK: call f<int>(5,7)
	//	}
	public void test14_8_2_5s19() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f(1)", 1);
		assertEquals("<int>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
		bh.assertProblem("f()", 1);
		inst= bh.assertNonProblem("f<int>()", -2);
		assertEquals("<int>", ASTTypeUtil.getArgumentListString(inst.getTemplateArguments(), true));
	}
	
	//	template <template <class T> class X> struct A { };
	//	template <template <class T> class X> void f(A<X>) { }
	//	template<class T> struct B { };
	//	int foo() {
	//		A<B> ab;
	//		f(ab); //calls f(A<B>)
	//	}
	public void test14_8_2_4s20() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	//	template<class> struct X { };
	//	template<class R, class ... ArgTypes> struct X<R(int, ArgTypes ...)> { };
	//	template<class ... Types> struct Y { };
	//	template<class T, class ... Types> struct Y<T, Types& ...> { };
	//	template<class ... Types> int f(void (*)(Types ...));
	//	void g(int, float);
	//	X<int> x1; // uses primary template
	//	X<int(int, float, double)> x2; // uses partial specialization; ArgTypes contains float, double
	//	X<int(float, int)> x3; // uses primary template
	//	Y<> y1; // use primary template; Types is empty
	//	Y<int&, float&, double&> y2; // uses partial specialization; T is int&, Types contains float, double
	//	Y<int, float, double> y3; // uses primary template; Types contains int, float, double
	//	int fv = f(g); // OK; Types contains int, float
	public void test14_8_2_4s21() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}
	
	//	template<class ... Args> void f(Args ... args); // #1
	//	template<class T1, class ... Args> void f(T1 a1, Args ... args); // #2
	//	template<class T1, class T2> void f(T1 a1, T2 a2); // #3
	//	void test() {
	//		f(); // calls #1
	//		f(1, 2, 3); // calls #2
	//		f(1, 2); // calls #3; non-variadic template #3 is more
	//		// specialized than the variadic templates #1 and #2
	//	}
	public void test14_8_2_5s22() throws Exception {
		final String code= getAboveComment();
		BindingAssertionHelper bh= new BindingAssertionHelper(code, true);
		ICPPFunctionTemplate f1= bh.assertNonProblem("f(Args ... args)", 1);
		ICPPFunctionTemplate f2= bh.assertNonProblem("f(T1 a1, Args ... args)", 1);
		ICPPFunctionTemplate f3= bh.assertNonProblem("f(T1 a1, T2 a2)", 1);
		
		ICPPTemplateInstance inst;
		inst= bh.assertNonProblem("f()", 1);
		assertSame(f1, inst.getTemplateDefinition());
		inst= bh.assertNonProblem("f(1, 2, 3)", 1);
		assertSame(f2, inst.getTemplateDefinition());
		inst= bh.assertNonProblem("f(1, 2)", 1);
		assertSame(f3, inst.getTemplateDefinition());
	}

	// template<class T> T max(T a, T b) { return a>b?a:b; }
	// void f(int a, int b, char c, char d)
	// {
	// int m1 = max(a,b); // max(int a, int b)
	// char m2 = max(c,d); // max(char a, char b)
	// int m3 = max(a,c); // error: cannot generate max(int,char)
	// }
	public void test14_8_3s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> T max(T a, T b) { return a>b?a:b; }
	// int max(int,int);
	// void f(int a, int b, char c, char d)
	// {
	// int m1 = max(a,b); // max(int a, int b)
	// char m2 = max(c,d); // max(char a, char b)
	// int m3 = max(a,c); // resolved
	// }
	public void test14_8_3s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> struct B {  };
	// template<class T> struct D : public B<T> {  };
	// template<class T> void f(B<T>&);
	// void g(B<int>& bi, D<int>& di)
	// {
	// f(bi); // f(bi)
	// f(di); // f( (B<int>&)di )
	// }
	public void test14_8_3s4() throws Exception {
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
	public void test14_8_3s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> void f(T); // declaration    
	// void g() {                                     
	//    f("Annemarie"); // call of f<const char*> 
	// }                                              
	public void test14_8_3s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// lab: try {
	// int t1;
	// try {
	// int t2;
	// if (1)
	// goto lab;
	// } catch(...) { // handler 2 
	// }
	// } catch(...) { // handler 1
	// }
	// }
	public void test15s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int f(int);
	// class C {
	// int i;
	// double d;
	// public:
	// C(int, double);
	// };
	// C::C(int ii, double id)
	// try
	// : i(f(ii)), d(id)
	// {
	// // constructor function body
	// }
	// catch (...)
	// {
	// // handles exceptions thrown from the ctorinitializer
	// // and from the constructor function body
	// }
	public void test15s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class Overflow {
	// // ...
	// public:
	// Overflow(char,double,double);
	// };
	// void f(double x)
	// {
	// // ...
	// throw Overflow('+',x,3.45e107);
	// }
	// int foo() {
	// try {
	// // ...
	// f(1.2);
	// // ...
	// }
	// catch(Overflow& oo) {
	// // handle exceptions of type Overflow here
	// }
	// }
	public void test15_1s1() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int foo() {
	// try {
	// // ...
	// }
	// catch (...) { // catch all exceptions
	// // respond (partially) to exception
	// throw; //pass the exception to some
	// // other handler
	// }
	// }
	public void test15_1s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class Matherr {  virtual void vf(); };
	// class Overflow: public Matherr {  };
	// class Underflow: public Matherr {  };
	// class Zerodivide: public Matherr {  };
	// void f()
	// {
	// try {
	// }
	// catch (Overflow oo) {
	// // ...
	// }
	// catch (Matherr mm) {
	// // ...
	// }
	// }
	public void test15_3s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// void f() throw(int); // OK
	// void (*fp)() throw (int); // OK
	// void g(void pfa() throw(int)); // OK
	public void test15_4s1a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef int (*pf)() throw(int); // illformed
	public void test15_4s1b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct B {
	// virtual void f() throw (int, double);
	// virtual void g();
	// };
	// struct D: B {
	// void f(); // illformed
	// void g() throw (int); // OK
	// };
	public void test15_4s3a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class A {  };
	// void (*pf1)(); // no exception specification
	// void (*pf2)() throw(A);
	// void f()
	// {
	// pf1 = pf2; // OK: pf1 is less restrictive
	// pf2 = pf1; // error: pf2 is more restrictive
	// }
	public void test15_4s3b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class X { };
	// class Y { };
	// class Z: public X { };
	// class W { };
	// void f() throw (X, Y)
	// {
	// int n = 0;
	// if (n) throw X(); // OK
	// if (n) throw Z(); // also OK
	// throw W(); // will call unexpected()
	// }
	public void test15_4s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// extern void f() throw(X, Y);
	// void g() throw(X)
	// {
	// // f(); //OK
	// }
	public void test15_4s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct A {
	// A();
	// A(const A&) throw();
	// ~A() throw(X);
	// };
	// struct B {
	// B() throw();
	// B(const B&) throw();
	// ~B() throw(Y);
	// };
	// struct D : public A, public B {
	// // Implicit declaration of D::D();
	// // Implicit declaration of D::D(const D&) throw();
	// // Implicit declaration of D::~D() throw (X,Y);
	// };
	public void test15_4s13() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// #if VERSION == 1
	// #define INCFILE "vers1.h"
	// #elif VERSION == 2
	// #define INCFILE "vers2.h" // and so on
	// #else
	// #define INCFILE "versN.h"
	// #endif
	public void test16_2s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// // second pass of C++ spec is to get [Note: ]
	// int f() {
	// int a, b;
	// /*...*/
	// a = a + 32760 + b + 5;
	// a = (((a + 32760) + b) + 5);
	// a = ((a + b) + 32765);
	// a = ((a + 32765) + b);
	// a = (a + (b + 32765));
	// }
	public void test18_2_1_5s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct X {
	// enum E { z = 16 };
	// int b[X::z]; // OK
	// };
	public void test3_3_1s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef int f;
	// struct A {
	// friend void f(A &);
	// operator int();
	// void g(A a) {
	// f(a);
	// }
	// };
	public void test3_4_1s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A {
	// int a;
	// };
	// struct B: virtual A { };
	// struct C: B { };
	// struct D: B { };
	// struct E: public C, public D { };
	// struct F: public A { };
	// void f() {
	// E e;
	// e.B::a = 0; // OK, only one A::a in E
	// F f;
	// f.A::a = 1; // OK, A::a is a member of F
	// }
	public void test3_4_5s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// inline double fd() { return 1.0; }
	// extern double d1;
	// double d2 = d1; // unspecified:
	// // may be statically initialized to 0.0 or
	// // dynamically initialized to 1.0
	// double d1 = fd(); // may be initialized statically to 1.0
	public void test3_6_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int main() {
	// const char c = 'c';
	// char* pc;
	// const char** pcc = &pc; //1: not allowed
	// *pcc = &c;
	// *pc = 'C'; //2: modifies a const object
	// }
	public void test4_4s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct S {
	// mutable int i;
	// };
	// int f() {
	// const S cs;
	// int S::* pm = &S::i; // pm refers to mutable member S::i
	// cs.*pm = 88; // illformed: cs is a const object
	// }
	public void test5_5s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace A {
	// class X { };
	// extern "C" int g();
	// extern "C++" int h();
	// }
	// namespace B {
	// void X(int);
	// extern "C" int g();
	// extern "C++" int h();
	// }
	// using namespace A;
	// using namespace B;
	// void f() {
	// X(1); //error: name X found in two namespaces
	// g(); //okay: name g refers to the same entity
	// h(); //error: name h found in two namespaces
	// }
	public void test7_3_4s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// void print(int a, int)
	// {
	// //printf("a = %d",a);
	// }
	public void test8_4s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int a;
	// const int b = a;
	// int c = b;
	public void test8_5s14() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct B {
	// virtual void f();
	// };
	// struct D : B {
	// void f(int);
	// };
	// struct D2 : D {
	// void f();
	// };
	public void test10_3s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct B {
	// virtual ~B();
	// void operator delete(void*, size_t);
	// };
	// struct D : B {
	// void operator delete(void*);
	// };
	// void f()
	// {
	// B* bp = new D;
	// delete bp; //1: uses D::operator delete(void*)
	// }
	public void test12_5s7a() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct B {
	// virtual ~B();
	// void operator delete[](void*, size_t);
	// };
	// struct D : B {
	// void operator delete[](void*, size_t);
	// };
	// void f(int i)
	// {
	// D* dp = new D[i];
	// delete [] dp; // uses D::operator delete[](void*, size_t)
	// B* bp = new D[i];
	// delete[] bp; // undefined behavior
	// }
	public void test12_5s7b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// struct A { };
	// void operator + (A, A);
	// struct B {
	// void operator + (B);
	// void f ();
	// };
	// A a;
	// void B::f() {
	// //operator+ (a,a); // ERROR - global operator hidden by member
	// a + a; // OK - calls global operator+
	// }
	public void test13_3_1_2s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> class myarray {  };
	// template<class K, class V, template<class T> class C = myarray>
	// class Map {
	// C<K> key;
	// C<V> value;
	// // ...
	// };
	public void test14_1s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// class T {  };
	// int i;
	// template<class T, T i> void f(T t)
	// {
	// T t1 = i; // templateparameters T and i
	// ::T t2 = ::i; // global namespace members T and i
	// }
	public void test14_1s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<int* p> class X { };
	// int a[10];
	// struct S { int m; static int s; } s;
	// X<&a[2]> x3; // error: address of array element
	// X<&s.m> x4; // error: address of nonstatic member
	// X<&s.s> x5; // error: &S::s must be used
	// X<&S::s> x6; // OK: address of static member
	public void test14_3_2s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T, char* p> class X {
	// // ...
	// X();
	// X(const char* q) {  }
	// };
	// X<int,"Studebaker"> x1; // error: string literal as template argument
	// char p[] = "Vivisectionist";
	// X<int,p> x2; // OK
	public void test14_3_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<const int& CRI> struct B { /* ... */ };
	// B<1> b2; // error: temporary would be required for template argument
	// int c = 1;
	// B<c> b1; // OK
	public void test14_3_2s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<const int* pci> struct X {  };
	// int ai[10];
	// X<ai> xi; // array to pointer and qualification conversions
	// struct Y {  };
	// template<const Y& b> struct Z {  };
	// Y y;
	// Z<y> z; // no conversion, but note extra cvqualification
	// template<int (&pa)[5]> struct W {  };
	// int b[5];
	// W<b> w; // no conversion
	// void f(char);
	// void f(int);
	// template<void (*pf)(int)> struct A {  };
	// A<&f> a; // selects f(int)
	public void test14_3_2s5() throws Exception  {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <class T> struct A {
	// void f(int);
	// template <class T2> void f(T2);
	// };
	// template <> void A<int>::f(int) { } // nontemplate member
	// template <> template <> void A<int>::f<>(int) { } // template member
	// int main()
	// {
	// A<char> ac;
	// ac.f(1); //nontemplate
	// ac.f('c'); //template
	// ac.f<>(1); //template
	// }
	public void test14_5_2s2() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);  //should be 0
	}

	// template<class T1, class T2, int I> class A<T1, T2, I> { }; // error
	public void test14_5_5s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> struct A {
	// class C {
	// template<class T2> struct B { };
	// };
	// };
	// // partial specialization of A<T>::C::B<T2>
	// template<class T> template<class T2>
	// struct A<T>::C::B<T2*> { };
	// A<short>::C::B<int*> absip; // uses partial specialization
	public void test14_5_5s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// namespace N {
	// template<class T1, class T2> class A { }; // primary template
	// }
	// using N::A; // refers to the primary template
	// namespace N {
	// template<class T> class A<T, T*> { }; // partial specialization
	// }
	// A<int,int*> a; // uses the partial specialization, which is found through
	// // the using declaration which refers to the primary template
	public void test14_5_5s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template<class T> void f();
	// template<int I> void f(); // OK: overloads the first template
	// // distinguishable with an explicit template argument list
	public void test14_5_6_1s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// template <int I> class A;
	// template <int I, int J> A<I+J> f/*1*/(A<I>, A<J>); // #1
	// template <int K, int L> A<K+L> f/*2*/(A<K>, A<L>); // same as #1
	// template <int I, int J> A<I-J> f/*3*/(A<I>, A<J>); // different from #1
	public void test14_5_6_1s5() throws Exception { 
		final String content= getAboveComment();
		IASTTranslationUnit tu= parse(content, ParserLanguage.CPP, true, 0);
		BindingAssertionHelper bh= new BindingAssertionHelper(content, true);
		ICPPFunctionTemplate f1= bh.assertNonProblem("f/*1*/", 1);
		ICPPFunctionTemplate f2= bh.assertNonProblem("f/*2*/", 1);
		ICPPFunctionTemplate f3= bh.assertNonProblem("f/*3*/", 1);
		assertSame(f1, f2);
		assertNotSame(f1, f3);
	}

	// template <int I> class A;
	// template <int I, int J> void f/*1*/(A<I+J>); // #1
	// template <int K, int L> void f/*2*/(A<K+L>); // same as #1
	public void test14_5_6_1s6() throws Exception { 
		final String content= getAboveComment();
		IASTTranslationUnit tu= parse(content, ParserLanguage.CPP, true, 0);
		BindingAssertionHelper bh= new BindingAssertionHelper(content, true);
		ICPPFunctionTemplate f1= bh.assertNonProblem("f/*1*/", 1);
		ICPPFunctionTemplate f2= bh.assertNonProblem("f/*2*/", 1);
		assertSame(f1, f2);
	}

	// template <class T> int f(T); // #1
	// int f(int); // #2
	// int k = f(1); // uses #2
	// int l = f<>(1); // uses #1
	public void test14_8_1s2b() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// #define TABSIZE 100
	// int table[TABSIZE];
	public void test15_3_5s3() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int g(int);
	// void f()
	// {
	// int i;
	// int& r = i; // r refers to i
	// r = 1; // the value of i becomes 1
	// int* p = &r; // p points to i
	// int& rr = r; // rr refers to what r refers to, that is, to i
	// int (&rg)(int) = g; // rg refers to the function g
	// rg(i); //calls function g
	// int a[3];
	// int (&ra)[3] = a; // ra refers to the array a
	// ra[1] = i; // modifies a[1]
	// }
	public void test8_5_3s1()  throws Exception { // TODO raised bug 90648
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A { }; // implicitlydeclared A::operator=
	// struct B : A {
	// B& operator=(const B &);
	// };
	// B& B::operator=(const B& s) {
	// this->A::operator=(s); // wellformed
	// return *this;
	// }
	public void test12s1() throws Exception  { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// struct A { };
	// struct B : virtual A { };
	// struct C : B { };
	// struct D : virtual A { D(A*); };
	// struct X { X(A*); };
	// struct E : C, D, X {
	// E() : D(this), // undefined: upcast from E* to A*
	// // might use path E* -> D* -> A*
	// // but D is not constructed
	// // D((C*)this), // defined:
	// // E* -> C* defined because E() has started
	// // and C* -> A* defined because
	// // C fully constructed
	// X(this) //defined: upon construction of X,
	// // C/B/D/A sublattice is fully constructed
	// { }
	// };
	public void test12_7s2() throws Exception  { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// typedef int c;
	// enum { i = 1 };
	// class X {
	// int i=3;
	// char v[i];
	// int f() { return sizeof(c); } // OK: X::c
	// char c;
	// enum { i = 2 };
	// };
	// typedef char* T;
	// struct Y {
	// typedef long T;
	// T b;
	// };
	// typedef int I;
	// class D {
	// typedef I I; // error, even though no reordering involved
	// };
	public void test3_3_6s5() throws Exception { // 90606
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// int f(double);
	// int f(int);
	// int (*pfd)(double) = &f; // selects f(double)
	// int (*pfi)(int) = &f; // selects f(int)
	// int (*pfe)(...) = &f; // error: type mismatch
	// int (&rfi)(int) = f; // selects f(int)
	// int (&rfd)(double) = f; // selects f(double)
	// void g() {
	// (int (*)(int))&f; // cast expression as selector
	// }
	public void test13_4s5a() throws Exception  { // bug 90674
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// class A {
	// public:
	// int z;
	// int z1;
	// };
	// class B : public A {
	// int a;
	// public:
	// int b, c;
	// int bf();
	// protected:
	// int x;
	// int y;
	// };
	// class D : private B {
	// int d;
	// public:
	// B::c; //adjust access to B::c
	// B::z; //adjust access to A::z
	// A::z1; //adjust access to A::z1
	// int e;
	// int df();
	// protected:
	// B::x; //adjust access to B::x
	// int g;
	// };
	// class X : public D {
	// int xf();
	// };
	// int ef(D&);
	// int ff(X&);
	public void test11_3s2() throws Exception { //bug 92793
		IASTTranslationUnit tu= parse(getAboveComment(), ParserLanguage.CPP, true, 0);
		IASTCompositeTypeSpecifier D= getCompositeType(tu, 2);
		IASTDeclaration accessDecl= getDeclaration(D, 2);
		assertInstance(accessDecl, ICPPASTUsingDeclaration.class);
	}

	// int z() {
	// int f(int);
	// int a = 2;
	// int b = f(a);
	// int c(b);
	// }
	public void test8_5s2() throws Exception  { // 90641
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}

	// // #include <cstddef>
	// char *p;
	// void *operator new(size_t, int);
	// void foo() {
	// const int x = 63;
	// new (int(*p)) int; // newplacement expression
	// new (int(*[x])); // new typeid
	// }
	public void test8_2s3() throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, false, 0);
	}

	// template<class T> void f();
	// template<int I> void f();
	// void g()
	// {
	// f<int()>(); // int() is a typeid:call the first f()
	// }
	public void test14_3s2()  throws Exception { 
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}
}
