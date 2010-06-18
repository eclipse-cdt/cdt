/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM)
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * Tests for classes implementing IASTImplicitNameOwner interface.
 */
public class AST2CPPImplicitNameTests extends AST2BaseTest {

	public AST2CPPImplicitNameTests() {
	}
	
	public AST2CPPImplicitNameTests(String name) {
		super(name);
	}
	
	public static TestSuite suite() {
		return suite(AST2CPPImplicitNameTests.class);
	}
	
	public IASTImplicitName[] getImplicitNames(IASTTranslationUnit tu, String contents, String section, int len) {
		final int offset = contents.indexOf(section);
		assertTrue(offset >= 0);
		IASTNodeSelector selector = tu.getNodeSelector(null);
		IASTImplicitName firstImplicit = selector.findImplicitName(offset, len);
		IASTImplicitNameOwner owner = (IASTImplicitNameOwner) firstImplicit.getParent();
		IASTImplicitName[] implicits = owner.getImplicitNames();
		return implicits;
	}

	//	class point {
	//	  int x, y;
	//	public:
	//	  point operator+(point);
	//	  point operator-(point);
	//	  point operator-();
	//	  point operator+=(int);
	//	};
	//	point operator*(point, point);
	//	point operator/(point, point);
	//
	//	point test(point p) {
	//	  p += 5;
	//	  p + p - p * p / p;
	//	  p << 6;
	//	  -p;
	//	  +p;
	//	}
	public void testBinaryExpressions() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		IASTTranslationUnit tu = ba.getTranslationUnit();
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);
		IASTImplicitName n;
		
		n = ba.assertImplicitName("+= 5", 2, ICPPMethod.class);
		assertSame(n.resolveBinding(), col.getName(14).resolveBinding());
		
		n = ba.assertImplicitName("+ p", 1, ICPPMethod.class);
		assertSame(n.resolveBinding(), col.getName(4).resolveBinding());
		
		n = ba.assertImplicitName("- p", 1, ICPPMethod.class);
		assertSame(n.resolveBinding(), col.getName(8).resolveBinding());
		
		n = ba.assertImplicitName("* p", 1, ICPPFunction.class);
		assertSame(n.resolveBinding(), col.getName(17).resolveBinding());
		
		n = ba.assertImplicitName("/ p", 1, ICPPFunction.class);
		assertSame(n.resolveBinding(), col.getName(23).resolveBinding());
		
		n = ba.assertImplicitName("-p;", 1, ICPPMethod.class);
		assertSame(n.resolveBinding(), col.getName(12).resolveBinding());
		
		ba.assertNoImplicitName("<< 6", 2);
		ba.assertNoImplicitName("+p;", 1);
	}

	//	struct X {};
	//
	//	template <class T> class auto_ptr {
	//	  T* ptr;
	//	public:
	//	  explicit auto_ptr(T* p = 0) : ptr(p) {}
	//	  T& operator*() { return *ptr; }
	//	};
	//
	//	void test() {
	//	  auto_ptr<X> x(new X());
	//	  *x; //1
	//	  int* y;
	//	  *y; //2
	//	}
	public void testPointerDereference() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		ba.assertImplicitName("*x;", 1, ICPPFunction.class);
		ba.assertNoImplicitName("*y; //2", 1);
	}

	//	struct X {};
	//	struct Y {
	//	  X x;
	//	};
	//
	//	X* operator&(X);
	//	X* operator&(Y);
	//
	//	void test(X x, Y y) {
	//	  X (Y::*px1) = &Y::x;  // not the overloaded operator
	//	  X* px2 = &y; // overloaded
	//	}
	public void testPointerToMember() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		IASTTranslationUnit tu = ba.getTranslationUnit();
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);
		
		ba.assertNoImplicitName("&Y::x;", 1);
		
		IASTImplicitName n = ba.assertImplicitName("&y;", 1, ICPPFunction.class);
		assertSame(n.resolveBinding(), col.getName(9).resolveBinding());
	}

	//	class A {
	//	public:
	//	  void doA() {}
	//	};
	//
	//	class FirstLevelProxy {
	//	public:
	//	  A* operator->() {A* a = new A(); return a;} // leaky
	//	  void doFLP() {}
	//	};
	//
	//	class SecondLevelProxy {
	//	public:
	//	  FirstLevelProxy operator->() {FirstLevelProxy p; return p;}
	//	  void doSLP() {}
	//	};
	//
	//	int main(int argc, char** argv) {
	//	  SecondLevelProxy p2;
	//	  p2->doA();
	//	}
	public void testArrowOperator() throws Exception {
		String contents = getAboveComment();		
		IASTTranslationUnit tu = parse(contents, ParserLanguage.CPP);
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);
		
		IASTImplicitName[] implicits = getImplicitNames(tu, contents, "->doA();", 2);
		
		assertNotNull(implicits);
		assertEquals(2, implicits.length);
		
		assertSame(implicits[1].getBinding(), col.getName(4).resolveBinding());
		assertSame(implicits[0].getBinding(), col.getName(12).resolveBinding());
	}
	
	//	struct A {
	//	  int x;
	//	};
	//
	//	struct B {
	//    A& operator++(); // prefix
	//	};
	//	A operator++(B, int); // postfix
	//
	//	void test(B p1, B p2) {
	//	  (p1++).x; //1
	//	  (++p1).x; //2
	//	}
	public void testUnaryPrefixAndPostfix() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		ba.assertImplicitName("++).x; //1", 2, ICPPFunction.class);
		ba.assertImplicitName("++p1).x; //2", 2, ICPPMethod.class);
	}

	//	struct A {};
	//	struct B {};
	//	struct C {};
	//	struct D {};
	//
	//	C operator,(A, B);
	//	D operator,(C, C);
	//
	//	int test(A a, B b, C c, D d) {
	//	  // should be treated like (((a, b), c), d)
	//	  a, b, c, d; // expr
	//	}
	//
	//	int main(int argc, char** argv) {
	//	  A a;
	//	  B b;
	//	  C c;
	//	  D d;
	//	  test(a, b, c, d); // func
	//	}
	public void testCommaOperator1() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		// expression lists are used in function calls but they should not resolve to the comma operator
		ba.assertNoImplicitName(", b, c, d); // func", 1);
		ba.assertNoImplicitName(", c, d); // func", 1);
		ba.assertNoImplicitName(", d); // func", 1);
		
		IASTImplicitName opAB = ba.assertImplicitName(", b, c, d; // expr", 1, ICPPFunction.class);
		IASTImplicitName opCC = ba.assertImplicitName(", c, d; // expr", 1, ICPPFunction.class);
		ba.assertNoImplicitName(", d; // expr", 1);
		
		IASTTranslationUnit tu = ba.getTranslationUnit();
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);
		
		assertSame(opAB.resolveBinding(), col.getName(5).resolveBinding());
		assertSame(opCC.resolveBinding(), col.getName(11).resolveBinding());
	}

	//	struct B {};
	//	struct C {};
	//	struct E {
	//	  int ee;
	//	};
	//	struct A {
	//	  C operator,(B);
	//	};
	//	struct D {
	//	  E operator,(D);
	//	};
	//	D operator,(C,C);
	//
	//	int test(A a, B b, C c, D d) {
	//	  (a, b, c, d).ee; // expr
	//	}
	public void testCommaOperator2() throws Exception {
		BindingAssertionHelper ba = new BindingAssertionHelper(getAboveComment(), true);
		
		IASTImplicitName opAB = ba.assertImplicitName(", b, c, d", 1, ICPPMethod.class);
		IASTImplicitName opCC = ba.assertImplicitName(", c, d", 1, ICPPFunction.class);
		IASTImplicitName opDD = ba.assertImplicitName(", d", 1, ICPPMethod.class);
		
		IASTTranslationUnit tu = ba.getTranslationUnit();
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);
		
		// 6, 11, 15
		assertSame(opAB.resolveBinding(), col.getName(6).resolveBinding());
		assertSame(opCC.resolveBinding(), col.getName(15).resolveBinding());
		assertSame(opDD.resolveBinding(), col.getName(11).resolveBinding());
		
		ba.assertNonProblem("ee;", 2);
	}

	//	struct X {
	//	  int operator()(bool);
	//	  int operator()();
	//	  int operator()(int, int);
	//	};
	//
	//	int test(X x) {
	//	  bool b = true;
	//	  x(b); // 1
	//	  x(); // 2
	//	  x(1, 2); // 3
	//	}
	public void testFunctionCallOperator() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		IASTTranslationUnit tu = ba.getTranslationUnit();
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);
		
		IASTImplicitName n1 = ba.assertImplicitName("(b); // 1", 1, ICPPMethod.class);
		IASTImplicitName n2 = ba.assertImplicitName("); // 1", 1, ICPPMethod.class);
		assertSame(n1.resolveBinding(), n2.resolveBinding());
		assertFalse(n1.isAlternate());
		assertTrue(n2.isAlternate());
		// there should be no overlap
		ba.assertNoImplicitName("b); // 1", 1);
		assertSame(col.getName(1).resolveBinding(), n1.resolveBinding());
		
		n1 = ba.assertImplicitName("(); // 2", 1, ICPPMethod.class);
		n2 = ba.assertImplicitName("); // 2", 1, ICPPMethod.class);
		assertSame(n1.resolveBinding(), n2.resolveBinding());
		assertFalse(n1.isAlternate());
		assertTrue(n2.isAlternate());
		assertSame(col.getName(3).resolveBinding(), n1.resolveBinding());
		
		n1 = ba.assertImplicitName("(1, 2); // 3", 1, ICPPMethod.class);
		n2 = ba.assertImplicitName("); // 3", 1, ICPPMethod.class);
		assertSame(n1.resolveBinding(), n2.resolveBinding());
		assertFalse(n1.isAlternate());
		assertTrue(n2.isAlternate());
		assertSame(col.getName(4).resolveBinding(), n1.resolveBinding());
	}

	//	struct A {};
	//	struct B {};
	//
	//	// Error: operator= must be a non-static member function
	//	A& operator=(const B&, const A&);
	//
	//	int main(int argc, char** argv) {
	//	  A a;
	//	  B b;
	//	  b = a; // should not resolve
	//	}
	public void testCopyAssignmentOperator() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		ba.assertNoImplicitName("= a;", 1);
	}

	//	struct A {
	//	  const char& operator[](int pos) const;
	//	  char& operator[](int pos);
	//	};
	//
	//	void func(const char& c);
	//	void func(char& c);
	//
	//	void test(const A& x, A& y) {
	//	  int q;
	//	  func(x[0]); //1
	//	  func(y[q]); //2
	//	}
	public void testArraySubscript() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		IASTTranslationUnit tu = ba.getTranslationUnit();
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);
		
		IASTImplicitName n1 = ba.assertImplicitName("[0]); //1", 1, ICPPMethod.class);
		ba.assertNoImplicitName("0]); //1", 1);
		IASTImplicitName n2 = ba.assertImplicitName("]); //1", 1, ICPPMethod.class);
		assertSame(n1.resolveBinding(), n2.resolveBinding());
		assertFalse(n1.isAlternate());
		assertTrue(n2.isAlternate());
		assertSame(col.getName(1).resolveBinding(), n1.resolveBinding());
		
		n1 = ba.assertImplicitName("[q]); //2", 1, ICPPMethod.class);
		ba.assertNoImplicitName("q]); //2", 1);
		n2 = ba.assertImplicitName("]); //2", 1, ICPPMethod.class);
		assertSame(n1.resolveBinding(), n2.resolveBinding());
		assertFalse(n1.isAlternate());
		assertTrue(n2.isAlternate());
		assertSame(col.getName(3).resolveBinding(), n1.resolveBinding());
	}

	//	struct X {
	//	  ~X();
	//	  void operator delete();
	//	  void operator delete[]();
	//	};
	//
	//	int test(X* x) {
	//	  delete x;
	//	  X* xs = new X[5];
	//	  delete[] x;
	//    delete 1;
	//	}
	public void testDelete() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		IASTImplicitName[] names = ba.getImplicitNames("delete x;", 6);
		assertEquals(2, names.length);
		IASTImplicitName destructor = names[0];
		IASTImplicitName delete = names[1];
		
		IASTTranslationUnit tu = ba.getTranslationUnit();
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);
		
		assertSame(col.getName(1).resolveBinding(), destructor.resolveBinding());
		assertSame(col.getName(2).resolveBinding(), delete.resolveBinding());
		
		names = ba.getImplicitNames("delete[] x;", 6);
		assertEquals(1, names.length);
		assertSame(col.getName(3).resolveBinding(), names[0].resolveBinding());
		
		ba.assertNoImplicitName("delete 1;", 6);
	}
	
	//	struct X {}
	//	int test(X* x) {
	//	  X* xs = new X[5];
	//	  delete[] x;
	//	}
	public void testImplicitNewAndDelete() throws Exception {
		BindingAssertionHelper ba = new BindingAssertionHelper(getAboveComment(), true);
		ba.assertNoImplicitName("new X", 3);
		ba.assertNoImplicitName("delete[]", 6); 
	}
	
	//  typedef long unsigned int size_t
	//	struct nothrow_t {};
	//	extern const nothrow_t nothrow;
	//	void* operator new(size_t, const nothrow_t&);
	//	void* operator new[](size_t, const nothrow_t&);
	//  void* operator new[](size_t, int, int);
	//	struct X {};
	//
	//	int test() {
	//	  X* fp = new (nothrow) X;
	//	  int* p = new (nothrow) int[5];
	//    int* p2 = new (5, 6) int[5];
	//	}
	public void testNew() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		IASTImplicitName n1 = ba.assertImplicitName("new (nothrow) X", 3, ICPPFunction.class);
		IASTImplicitName n2 = ba.assertImplicitName("new (nothrow) int", 3, ICPPFunction.class);
		IASTImplicitName n3 = ba.assertImplicitName("new (5, 6) int", 3, ICPPFunction.class);

		IASTTranslationUnit tu = ba.getTranslationUnit();
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);

		assertSame(col.getName(4).resolveBinding(), n1.resolveBinding());
		assertSame(col.getName(9).resolveBinding(), n2.resolveBinding());
		assertSame(col.getName(14).resolveBinding(), n3.resolveBinding());
	}
	
	//	int test() {
	//	  throw;
	//	}
	public void testEmptyThrow() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		ba.assertNoImplicitName("throw;", 5);
	}

	//	struct A {
	//	  A() {}
	//    A(int) {}
	//	  template <typename T>
	//    A(T, int) {}
	//	};
	//  typedef A B;
	//
	//	void test() {
	//	  B a;
	//	  B b(1);
	//	  B c = 1;
	//	  B d("", 1);
	//	  extern B e;
	//	}
	//
	//	struct C {
	//	  static B s = 1;
	//    static B t;
	//	  B u;
	//	  B v;
	//	  C(int p) : u(), v(p) {}
	//	};
	//	B C::t = 1;
	public void testConstructorCall() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		IASTTranslationUnit tu = ba.getTranslationUnit();
		ICPPConstructor ctor0 = ba.assertNonProblem("A()", 1, ICPPConstructor.class);
		ICPPConstructor ctor1 = ba.assertNonProblem("A(int)", 1, ICPPConstructor.class);
		ICPPConstructor ctor2 = ba.assertNonProblem("A(T, int)", 1, ICPPConstructor.class);

		IASTImplicitName a = ba.assertImplicitName("a;", 1, ICPPConstructor.class);
		assertSame(ctor0, a.resolveBinding());
		IASTImplicitName b = ba.assertImplicitName("b(", 1, ICPPConstructor.class);
		assertSame(ctor1, b.resolveBinding());
		IASTImplicitName c = ba.assertImplicitName("c =", 1, ICPPConstructor.class);
		assertSame(ctor1, c.resolveBinding());
		IASTImplicitName d = ba.assertImplicitName("d(", 1, ICPPConstructor.class);
		assertSame(ctor2, ((ICPPTemplateInstance) d.resolveBinding()).getTemplateDefinition());
		ba.assertNoImplicitName("e;", 1);
		IASTImplicitName s = ba.assertImplicitName("s =", 1, ICPPConstructor.class);
		assertSame(ctor1, s.resolveBinding());
		ba.assertNoImplicitName("t;", 1);
		IASTImplicitName t = ba.assertImplicitName("t =", 1, ICPPConstructor.class);
		assertSame(ctor1, t.resolveBinding());
		ba.assertNoImplicitName("u;", 1);
		IASTImplicitName u = ba.assertImplicitName("u()", 1, ICPPConstructor.class);
		assertSame(ctor0, u.resolveBinding());
		IASTImplicitName v = ba.assertImplicitName("v(p)", 1, ICPPConstructor.class);
		assertSame(ctor1, v.resolveBinding());
	}
	
	//	enum A {aa};
	//	struct B{ operator A();};
	//	bool operator==(A, A);   // overrides the built-in operator.
	//
	//	void test() {
	//		B b;
	//		if (aa==b) {
	//		}
	//	}
	public void testBuiltinOperators_294543() throws Exception {
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		IASTTranslationUnit tu = ba.getTranslationUnit();
		ICPPFunction op = ba.assertNonProblem("operator==", 0);
		IASTImplicitName a = ba.assertImplicitName("==b", 2, ICPPFunction.class);
		assertSame(op, a.resolveBinding());
	}		
}
