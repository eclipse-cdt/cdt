/*******************************************************************************
 * Copyright (c) 2006 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.IOException;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.core.runtime.Path;

/**
 * For testing PDOM binding CPP language resolution
 */
/*
 * aftodo - once we have non-problem bindings working, each test should
 * additionally check that the binding obtained has characteristics as
 * expected (type,name,etc..)
 */
public class IndexCPPBindingResolutionTest extends IndexBindingResolutionTestBase {

	public static TestSuite suite() {
		return suite(IndexCPPBindingResolutionTest.class);
	}
	
	protected void setUp() throws Exception {
		cproject= CProjectHelper.createCCProject("ResolveBindingTestsCPP", "bin", IPDOMManager.ID_NO_INDEXER);
		header = new Path("header.h");
		references = new Path("references.cpp");
		super.setUp();
	}
	
	// // header file
	//	class C {
	//	public:
	//	int field;
	//	struct CS { long* l; C *method(CS *); };
	//	CS cs;
	//	long * CS::* ouch;
	//	long * CS::* autsch;
	//  C * CS:: method(CS *);
	//	};

	// // referencing file
	//  #include "header.h"
	//	void references() {
	//	C *cp = new C();
	//	long l = 5, *lp;
	//	lp = &l;
	//	cp->cs.*cp->ouch = lp = cp->cs.*cp->autsch;
	//	&(cp->cs)->*cp->autsch = lp = &(cp->cs)->*cp->ouch;
	//  cp->cs.*method(cp);/*1*/ &(cp->cs)->*method(cp);/*2*/
	//	}
	public void _testPointerToMemberFields() throws IOException {
		IBinding b0 = getBindingFromASTName("cs.*cp->o", 2);
		IBinding b1 = getBindingFromASTName("ouch = lp;", 4);
		IBinding b2 = getBindingFromASTName("autsch;", 6);
		IBinding b3 = getBindingFromASTName("cs)->*c->a", 2);
		IBinding b4 = getBindingFromASTName("autsch = lp", 6);
		IBinding b5 = getBindingFromASTName("ouch;", 4);
		IBinding b6 = getBindingFromASTName("method(cp);/*1*/", 6);
		IBinding b7 = getBindingFromASTName("method(cp);/*2*/", 6);
	}

	// // header file
	// class C {}; struct S {}; union U {}; enum E {ER1,ER2,ER3};
	// int var1; C var2; S *var3; void func(E); void func(C);
	// namespace ns {}
	// typedef int Int; typedef int *IntPtr;

	// // referencing file
	// #include "header.h"
	// void references() {
	// 	C c; /*c*/ S s; /*s*/ U u; /*u*/ E e; /*e*/
	//  var1 = 1; /*var1*/ var2 = c; /*var2*/ var3 = &s; /*var3*/
	//  func(e); /*func1*/ func(var1); /*func2*/ func(c); /*func3*/
	//  Int a; /*a*/
	//  IntPtr b = &a; /*b*/
	//  func(*b); /*func4*/ func(a); /*func5*/
	// }
	// class C2 : public C {}; /*base*/
	// struct S2 : public S {}; /*base*/
	// union U2 : public U {}; /*base*/
	public void testSimpleGlobalBindings() throws IOException {
		IBinding b0 = getBindingFromASTName("C c; ", 1);
		IBinding b1 = getBindingFromASTName("c; ", 1);
		IBinding b2 = getBindingFromASTName("S s;", 1);
		IBinding b3 = getBindingFromASTName("s;", 1);
		IBinding b4 = getBindingFromASTName("U u;", 1);
		IBinding b5 = getBindingFromASTName("u; ", 1);
		IBinding b7 = getBindingFromASTName("e; ", 1);
		IBinding b8 = getBindingFromASTName("var1 = 1;", 4);
		IBinding b9 = getBindingFromASTName("var2 = c;", 4);
		IBinding b10 = getBindingFromASTName("var3 = &s;", 4);
		IBinding b11 = getBindingFromASTName("func(e);", 4);
		IBinding b13 = getBindingFromASTName("func(c);", 4);
		IBinding b15 = getBindingFromASTName("a; ", 1);
		IBinding b17 = getBindingFromASTName("b = &a; /*b*/", 1);
		IBinding b19 = getBindingFromASTName("b); /*func4*/", 1);
		IBinding b20 = getBindingFromASTName("func(a);", 4);
		IBinding b21 = getBindingFromASTName("a); /*func5*/", 1);
		IBinding b22 = getBindingFromASTName("C2 : public", 2);
		IBinding b24 = getBindingFromASTName("S2 : public", 2);
		IBinding b26 = getBindingFromASTName("U2 : public", 2);
	}
	
	public void _testSimpleGlobalBindings() throws IOException {
		/* note the test data from above follows through to this test */
		IBinding b6 = getBindingFromASTName("E e; ", 1);
		IBinding b12 = getBindingFromASTName("func(var1);", 4);
		IBinding b14 = getBindingFromASTName("Int a; ", 3);
		IBinding b16 = getBindingFromASTName("IntPtr b = &a; ", 6);
		IBinding b18 = getBindingFromASTName("func(*b);", 4);
		IBinding b23 = getBindingFromASTName("C {}; /*base*/", 1);		
		IBinding b25 = getBindingFromASTName("S {}; /*base*/", 1);
		IBinding b27 = getBindingFromASTName("U {}; /*base*/", 1);
	}
	
	
	//// header content
	//class TopC {}; struct TopS {}; union TopU {}; enum TopE {TopER1,TopER2};
	//short topBasic; void *topPtr; TopC *topCPtr; TopU topFunc(){return *new TopU();}

	//// referencing content
	//namespace n1 {
	//   class TopC {}; struct TopS {}; union TopU {}; enum TopE {TopER1,TopER2};
	//   short topBasic; void *topPtr; TopC *topCPtr; TopU topFunc(){return *new TopU();}   
	//   class C {
	//      class TopC {}; struct TopS {}; union TopU {}; enum TopE {TopER1,TopER2};
	//      short topBasic; void *topPtr; TopC *topCPtr; TopU topFunc(){return *new TopU();}
	//      void references() {
	//         ::TopC c; ::TopS s; ::TopU u; ::TopE e = ::TopER1;
	//         ::topBasic++; ::topPtr = &::topBasic; ::topCPtr = &c; ::topFunc();
	//      } 
	//   };
	//}
	public void _testSingletonQualifiedName() {
		IBinding b0 = getBindingFromASTName("TopC c", 4);
		IBinding b1 = getBindingFromASTName("TopS s", 4);
		IBinding b2 = getBindingFromASTName("TopU u", 4);
		IBinding b3 = getBindingFromASTName("TopE e", 4);
		IBinding b4 = getBindingFromASTName("TopER1;", 6);
		IBinding b5 = getBindingFromASTName("topBasic++", 8);
		IBinding b6 = getBindingFromASTName("topPtr", 6);
		IBinding b7 = getBindingFromASTName("topBasic", 8);
		IBinding b8 = getBindingFromASTName("topCPtr", 7);
		IBinding b9 = getBindingFromASTName("topFunc", 7);
	}
	
	public void _testMultiVirtualBaseClassLookup() {fail("aftodo");}
	public void _testMultiNonVirtualBaseClassLookup() {fail("aftodo");}
	
	public void _testQualifiedNamesForNamespaceAliases() {fail("aftodo");}
	public void _testQualifiedNamesForNamespaces() {fail("aftodo");}
	
	//	// header content
	// namespace n1 { namespace n2 { struct S {}; } }
	// class c1 { class c2 { struct S {}; };
	// struct s1 { struct s2 { struct S {}; }; };
	// union u1 { struct u2 { struct S {}; }; };
	// namespace n3 { class c3 { struct s3 { union u3 { struct S {}; }; }; }; }
	
	// // reference content
	// void reference() {
	//  ::n1::n2::S s0; n1::n2::S s1;
	//  ::c1::c2::S s2; c1::c2::S s3;
	//  ::s1::s2::S s4; s1::s2::S s5;
	//  ::u1::u2::S s6; u1::u2::S s7;
	//  ::n3::c3::s3::u3::S s8;
	//    n3::c3::s3::u3::S s9;
	// }
	// namespace n3 { c3::s3::u3::S s10; }
	// namespace n1 { n2::S s11; }
	// namespace n1 { namespace n2 { S s12; }} 
	public void _testQualifiedNamesForStruct() throws DOMException {
		IBinding b0 = getBindingFromASTName("S s0;", 1);
		assertQNEquals("n1::n2::S", b0);
		IBinding b1 = getBindingFromASTName("S s1;", 1);
		assertQNEquals("n1::n2::S", b1);
		IBinding b2 = getBindingFromASTName("S s2;", 1);
		assertQNEquals("c1::c2::S", b2);
		IBinding b3 = getBindingFromASTName("S s3;", 1);
		assertQNEquals("c1::c2::S", b3);
		IBinding b4 = getBindingFromASTName("S s4;", 1);
		assertQNEquals("s1::s2::S", b4);
		IBinding b5 = getBindingFromASTName("S s5;", 1);
		assertQNEquals("s1::s2::S", b5);
		IBinding b6 = getBindingFromASTName("S s6;", 1);
		assertQNEquals("u1::u2::S", b6);
		IBinding b7 = getBindingFromASTName("S s7;", 1);
		assertQNEquals("u1::u2::S", b7);
		IBinding b8 = getBindingFromASTName("S s8;", 1);
		assertQNEquals("n3::c3::s3::u3::S", b8);
		IBinding b9 = getBindingFromASTName("S s9;", 1);
		assertQNEquals("n3::c3::s3::u3::S", b9);
		IBinding b10 = getBindingFromASTName("S s10;", 1);
		assertQNEquals("n3::c3::s3::u3::S", b10);
		IBinding b11 = getBindingFromASTName("S s11;", 1);
		assertQNEquals("n1::n2::S", b11);
		IBinding b12 = getBindingFromASTName("S s12;", 1);
		assertQNEquals("n1::n2::S", b12);
	}
	
	// // header content
	// namespace n1 { namespace n2 { union U {}; } }
	// class c1 { class c2 { union U {}; };
	// struct s1 { struct s2 { union U {}; }; };
	// union u1 { struct u2 { union U {}; }; };
	// namespace n3 { class c3 { struct s3 { union u3 { union U {}; }; }; }; }
	
	// // reference content
	// void reference() {
	//  ::n1::n2::U u0; n1::n2::U u1;
	//  ::c1::c2::U u2; c1::c2::U u3;
	//  ::s1::s2::U u4; s1::s2::U u5;
	//  ::u1::u2::U u6; u1::u2::U u7;
	//  ::n3::c3::s3::u3::U u8;
	//    n3::c3::s3::u3::U u9;
	// }
	// namespace n3 { c3::s3::u3::U u10; }
	// namespace n1 { n2::U u11; }
	// namespace n1 { namespace n2 { U u12; }} 
	public void _testQualifiedNamesForUnion() throws DOMException {
		IBinding b0 = getBindingFromASTName("S s0;", 1);
		assertQNEquals("n1::n2::S", b0);
		IBinding b1 = getBindingFromASTName("S s1;", 1);
		assertQNEquals("n1::n2::S", b1);
		IBinding b2 = getBindingFromASTName("S s2;", 1);
		assertQNEquals("c1::c2::S", b2);
		IBinding b3 = getBindingFromASTName("S s3;", 1);
		assertQNEquals("c1::c2::S", b3);
		IBinding b4 = getBindingFromASTName("S s4;", 1);
		assertQNEquals("s1::s2::S", b4);
		IBinding b5 = getBindingFromASTName("S s5;", 1);
		assertQNEquals("s1::s2::S", b5);
		IBinding b6 = getBindingFromASTName("S s6;", 1);
		assertQNEquals("u1::u2::S", b6);
		IBinding b7 = getBindingFromASTName("S s7;", 1);
		assertQNEquals("u1::u2::S", b7);
		IBinding b8 = getBindingFromASTName("S s8;", 1);
		assertQNEquals("n3::c3::s3::u3::S", b8);
		IBinding b9 = getBindingFromASTName("S s9;", 1);
		assertQNEquals("n3::c3::s3::u3::S", b9);
		IBinding b10 = getBindingFromASTName("S s10;", 1);
		assertQNEquals("n3::c3::s3::u3::S", b10);
		IBinding b11 = getBindingFromASTName("S s11;", 1);
		assertQNEquals("n1::n2::S", b11);
		IBinding b12 = getBindingFromASTName("C s12;", 1);
		assertQNEquals("n1::n2::S", b12);
	}
	
	// // header content
	// namespace n1 { namespace n2 { class C {}; } }
	// class c1 { public: class c2 { public: class C {}; }; };
	// struct s1 { struct s2 { class C {}; }; };
	// union u1 { union u2 { class C {}; }; };
	// namespace n3 { class c3 { public: struct s3 { union u3 { class C {}; }; }; }; }
	//
	// // reference content
	// void reference() {
	//  ::n1::n2::C c0; n1::n2::C c1;
	//  ::c1::c2::C c2; c1::c2::C c3;
	//  ::s1::s2::C c4; s1::s2::C c5;
	//  ::u1::u2::C c6; u1::u2::C c7;
	//  ::n3::c3::s3::u3::C c8;
	//    n3::c3::s3::u3::C c9;
	// }
	// namespace n3 { c3::s3::u3::C c10; }
	// namespace n1 { n2::C c11; }
	// namespace n1 { namespace n2 { C c12; }} 
	public void _testQualifiedNamesForClass() throws DOMException {
		IBinding b0 = getBindingFromASTName("C c0;", 1);
		assertQNEquals("n1::n2::C", b0);
		IBinding b1 = getBindingFromASTName("C c1;", 1);
		assertQNEquals("n1::n2::C", b1);
		IBinding b2 = getBindingFromASTName("C c2;", 1);
		assertQNEquals("c1::c2::C", b2);
		IBinding b3 = getBindingFromASTName("C c3;", 1);
		assertQNEquals("c1::c2::C", b3);
		IBinding b4 = getBindingFromASTName("C c4;", 1);
		assertQNEquals("s1::s2::C", b4);
		IBinding b5 = getBindingFromASTName("C c5;", 1);
		assertQNEquals("s1::s2::C", b5);
		IBinding b6 = getBindingFromASTName("C c6;", 1);
		assertQNEquals("u1::u2::C", b6);
		IBinding b7 = getBindingFromASTName("C c7;", 1);
		assertQNEquals("u1::u2::C", b7);
		IBinding b8 = getBindingFromASTName("C c8;", 1);
		assertQNEquals("n3::c3::s3::u3::C", b8);
		IBinding b9 = getBindingFromASTName("C c9;", 1);
		assertQNEquals("n3::c3::s3::u3::C", b9);
		IBinding b10 = getBindingFromASTName("C c10;", 1);
		assertQNEquals("n3::c3::s3::u3::C", b10);
		IBinding b11 = getBindingFromASTName("C c11;", 1);
		assertQNEquals("n1::n2::C", b11);
		IBinding b12 = getBindingFromASTName("C c12;", 1);
		assertQNEquals("n1::n2::C", b12);
	}
	
	 // // header content
	 // namespace n1 { namespace n2 { typedef int Int; } }
	 // class c1 { public: class c2 { public: typedef int Int; }; };
	 // struct s1 { struct s2 { typedef int Int; }; };
	 // union u1 { struct u2 { typedef int Int; }; };
	 // namespace n3 { class c3 { public: struct s3 { union u3 { typedef int Int; }; }; }; }
	
	 // // reference content
	 // void reference() {
	 //  ::n1::n2::Int i0; n1::n2::Int i1;
	 //  ::c1::c2::Int i2; c1::c2::Int i3;
	 //  ::s1::s2::Int i4; s1::s2::Int i5;
	 //  ::u1::u2::Int i6; u1::u2::Int i7;
	 //  ::n3::c3::s3::u3::Int i8;
	 //    n3::c3::s3::u3::Int i9;
	 // }
	 // namespace n3 { c3::s3::u3::Int i10; }
	 // namespace n1 { n2::Int i11; }
	 // namespace n1 { namespace n2 { Int i12; }} 
	public void _testQualifiedNamesForTypedef() throws DOMException {
		IBinding b0 = getBindingFromASTName("Int i0;", 3);
		assertQNEquals("n1::n2::Int", b0);
		IBinding b1= getBindingFromASTName("Int i1;", 3);
		assertQNEquals("n1::n2::Int", b1);
		
		IBinding b2 = getBindingFromASTName("Int i2;", 3);
		assertQNEquals("c1::c2::Int", b2);
		IBinding b3 = getBindingFromASTName("Int i3;", 3);
		assertQNEquals("c1::c2::Int", b3);
		
		IBinding b4 = getBindingFromASTName("Int i4;", 3);
		assertQNEquals("s1::s2::Int", b4);
		IBinding b5 = getBindingFromASTName("Int i5;", 3);
		assertQNEquals("s1::s2::Int", b5);
		
		IBinding b6 = getBindingFromASTName("Int i6;", 3);
		assertQNEquals("u1::u2::Int", b6);
		IBinding b7 = getBindingFromASTName("Int i7;", 3);
		assertQNEquals("u1::u2::Int", b7);
		
		IBinding b8 = getBindingFromASTName("Int i8;", 3);
		assertQNEquals("n3::c3::s3::u3::Int", b8);
		IBinding b9 = getBindingFromASTName("Int i9;", 3);
		assertQNEquals("n3::c3::s3::u3::Int", b9);
		IBinding b10 = getBindingFromASTName("Int i10;", 3);
		assertQNEquals("n3::c3::s3::u3::Int", b10);
		IBinding b11 = getBindingFromASTName("Int i11;", 3);
		assertQNEquals("n1::n2::Int", b11);
		IBinding b12 = getBindingFromASTName("Int i12;", 3);
		assertQNEquals("n1::n2::Int", b12);
	}
	
	// // header content
	// enum E { ER1, ER2 };
		
	// // referencing content
	// class C {
	//	 E e1;
	//	 static E e2;
	//	 void m1() { e1 = ER1; }
	//	 static void m2() { e2 = ER2; }
	// };
	public void _testEnumeratorInClassScope() {
		IBinding b0 = getBindingFromASTName("E e1", 1);
		IBinding b1 = getBindingFromASTName("ER1; }", 1);
		IBinding b2 = getBindingFromASTName("ER2; }", 1);
	}
	
	// // header content
	// enum E { ER1, ER2 };
		
	// // referencing content
	// struct S {
	//	 E e1;
	//	 static E e2;
	//	 void m1() { e1 = ER1; }
	//	 static void m2() { e2 = ER2; }
	// };
	public void _testEnumeratorInStructScope() {
		IBinding b0 = getBindingFromASTName("E e1", 1);
		IBinding b1 = getBindingFromASTName("ER1; }", 1);
		IBinding b2 = getBindingFromASTName("ER2; }", 1);
	}
	
	//	 // header content
	// enum E { ER1, ER2 };
		
	// // referencing content
	// union U {
	//	 E e1;
	//	 static E e2;
	//	 void m1() { e1 = ER1; }
	//	 static void m2() { e2 = ER2; }
	// };
	public void _testEnumeratorInUnionScope() {
		IBinding b0 = getBindingFromASTName("E e1", 1);
		IBinding b1 = getBindingFromASTName("ER1; }", 1);
		IBinding b2 = getBindingFromASTName("ER2; }", 1);
	}
	
	//	 // header content
	// enum E { ER1, ER2 };
		
	// // referencing content
	// namespace n1 {
	//	 E e1;
	//	 static E e2;
	//	 void f1() { e1 = ER1; }
	//	 static void f2() { e2 = ER2; }
	// };
	public void _testEnumeratorInNamespaceScope() {
		IBinding b0 = getBindingFromASTName("E e1", 1);
		IBinding b1 = getBindingFromASTName("ER1; }", 1);
		IBinding b2 = getBindingFromASTName("ER2; }", 1);
	}
	
	// // teh header
	// void foo(int a=2, int b=3);
	
	// #include "header.h"
	// void ref() { foo(); }
	public void _testFunctionDefaultArguments() {
		IBinding b0 = getBindingFromASTName("foo();", 3);
	}

	// // header
	//	class Base { public: void foo(int i) {} };
	//	class Derived : public Base { public: void foo(long l) {} };

	// // references
	// #include "header.h"
	// void references() {
	//    Derived d; /*d*/
	//    d.foo(55L); // calls long version
	//    d.foo(4); // also calls long version (int version is hidden)
	//    // aftodo - does this test make sense?
	// }
	public void _testMethodHidingInInheritance() {
		IBinding b0 = getBindingFromASTName("d; /*d*/", 1);
		IBinding b1 = getBindingFromASTName("foo(55L);", 3);
		IBinding b2 = getBindingFromASTName("foo(4);", 3);
	}
	
	// // header content
	// namespace x { namespace y { int i; } }
	
	// // the references
	// #include "header.h"
	// class C { public:
	//    class x { public:
	//       class y { public:
	//          static int j;
	//       };
	//    }; 
	//    void method() {
	//       ::x::y::i++;
	//       x::y::j++;
	//    }
	// };
	public void testGQualifiedReference() {
		IBinding b0 = getBindingFromASTName("x::y::i++", 1);
		assertTrue(ICPPNamespace.class.isInstance(b0));
		IBinding b1 = getBindingFromASTName("y::i++", 1);
		assertTrue(ICPPNamespace.class.isInstance(b1));
		IBinding b2 = getBindingFromASTName("i++", 1);
		assertTrue(ICPPVariable.class.isInstance(b2));
		IBinding b3 = getBindingFromASTName("x::y::j++", 1);
		assertTrue(ICPPClassType.class.isInstance(b3));
		IBinding b4 = getBindingFromASTName("y::j++", 1);
		assertTrue(ICPPClassType.class.isInstance(b4));
		IBinding b5 = getBindingFromASTName("j++", 1);
		assertTrue(ICPPVariable.class.isInstance(b5));
	}
	
		
	////header content
	//struct S {int i;};
	//struct SS { S s, *sp; };
	//
	//S* retsptr() {return 0;}
	//S rets() { return *new S(); }
	//S s, *sp;
	//SS ss, *ssp;
	//S *a[3];
	
	////reference content
	//void references() {
	//	a[0]->i/*0*/++; (*a[0]).i/*1*/++;                    // IASTArraySubscriptExpression
	//	/* not applicable */ ??                              // IASTBinaryExpression
	//	((S*)sp)->i/*3*/++; ((S)s).i/*4*/++; //aftodo-valid? // IASTCastExpression
	//	(true ? sp : sp)->i/*5*/++; (true ? s : s).i/*6*/++; // IASTConditionalExpression
	//	(sp,sp)->i/*7*/++; (s,s).i/*8*/++;                   // IASTExpressionList
	//	ss.sp->i/*9*/++; ss.s.i/*10*/++;                     // IASTFieldReference
	//	ssp->sp->i/*11*/++; ssp->s.i/*12*/++;                // IASTFieldReference
	//	retsptr()->i/*13*/++; rets().i/*14*/++;              // IASTFunctionCallExpression     
	//	sp->i/*15*/++; s.i/*16*/++;                          // IASTIdExpression
	//	/* not applicable */                                 // IASTLiteralExpression
	//	/* not applicable */                                 // IASTTypeIdExpression
	//	(*sp).i/*17*/++;                                     // IASTUnaryExpression
	//	/* not applicable */                                 // ICPPASTDeleteExpression
	//	(new S())->i/*18*/++;                                // ICPPASTNewExpression
	//}
	public void _testFieldReference() {
		IBinding b0 = getBindingFromASTName("i/*0*/", 1);
		IBinding b1 = getBindingFromASTName("i/*1*/", 1);
		// IBinding b2 = getBindingFromASTName(ast, "i/*2*/", 1);
		IBinding b3 = getBindingFromASTName("i/*3*/", 1);
		IBinding b4 = getBindingFromASTName("i/*4*/", 1);
		IBinding b5 = getBindingFromASTName("i/*5*/", 1);
		IBinding b6 = getBindingFromASTName("i/*6*/", 1);
		IBinding b7 = getBindingFromASTName("i/*7*/", 1);
		IBinding b8 = getBindingFromASTName("i/*8*/", 1);
		IBinding b9 = getBindingFromASTName("i/*9*/", 1);
		IBinding b10 = getBindingFromASTName("i/*10*/", 1);
		IBinding b11 = getBindingFromASTName("i/*11*/", 1);
		IBinding b12 = getBindingFromASTName("i/*12*/", 1);
		IBinding b13 = getBindingFromASTName("i/*13*/", 1);
		IBinding b14 = getBindingFromASTName("i/*14*/", 1);
		IBinding b15 = getBindingFromASTName("i/*15*/", 1);
		IBinding b16 = getBindingFromASTName("i/*16*/", 1);
		IBinding b17 = getBindingFromASTName("i/*17*/", 1);
		IBinding b18 = getBindingFromASTName("i/*18*/", 1);
	}
	
	
	// // header file
	//	class C {C* cp;};
	//	C foo(C c);
	//	C* foo(C* c);
	//	int foo(int i);
	//	int foo(int i, C c);
	
	// // referencing content
	//	void references() {
	//		C c, *cp;
	//		foo/*a*/(cp[1]);                        // IASTArraySubscriptExpression
	//		foo/*b*/(cp+1);                         // IASTBinaryExpression
	//		foo/*c*/((C*) cp);/*1*/                 // IASTCastExpression
	//		foo/*d*/(true ? c : c);/*2*/            // IASTConditionalExpression
	//		foo/*e*/(5, c);/*3*/                    // IASTExpressionList
	//		foo/*f*/(c.cp);/*4*/ foo(cp->cp);/*5*/  // IASTFieldReference
	//		foo/*g*/(foo(c));/*6*/ foo(foo(1));/*7*/// IASTFunctionCallExpression
	//		foo/*h*/(c);/*8*/                       // IASTIdExpression
	//		foo/*i*/(23489);                        // IASTLiteralExpression
	//		foo/*j*/(sizeof(C));/*9*/               // IASTTypeIdExpression
	//		foo/*k*/(*cp);/*10*/                    // IASTUnaryExpression
	//		foo/*l*/(delete cp);/*11*/              // ICPPASTDeleteExpression
	//		foo/*m*/(new C());/*12*/                // ICPPASTNewExpression
	//		// ?? foo/*n*/();                       // ICPPASTSimpleTypeConstructorExpression
	//		// ?? foo/*o*/();                       // ICPPASTTypenameExprssion
	//		// foo/*p*/(MADE_UP_SYMBOL);            // ICPPASTTypenameExprssion
	//	}
	public void _testExpressionKindForFunctionCalls() {
		IBinding b0 = getBindingFromASTName("foo/*a*/", 3);
		IBinding b0a = getBindingFromASTName("cp[1]", 2);
		// assertCompositeTypeParam(0, ICPPClassType.k_class, b0, "C");
		
		IBinding b1 = getBindingFromASTName("foo/*b*/", 3);
		IBinding b1a = getBindingFromASTName("cp+1", 2);
		
		IBinding b2 = getBindingFromASTName("foo/*c*/", 3);
		IBinding b2a = getBindingFromASTName("cp);/*1*/", 2);
		
		IBinding b3 = getBindingFromASTName("foo/*d*/", 3);
		IBinding b3a = getBindingFromASTName("c : c", 1);
		IBinding b3b = getBindingFromASTName("c);/*2*/", 1);
		
		IBinding b4 = getBindingFromASTName("foo/*e*/", 3);
		IBinding b4a = getBindingFromASTName("c);/*3*/", 1);
		
		IBinding b5 = getBindingFromASTName("foo/*f*/", 3);
		IBinding b5a = getBindingFromASTName("cp);/*4*/", 2);
		IBinding b5b = getBindingFromASTName("cp->cp);/*5*/", 2);
		IBinding b5c = getBindingFromASTName("cp);/*5*/", 2);
		
		IBinding b6 = getBindingFromASTName("foo/*g*/", 3);
		IBinding b6a = getBindingFromASTName("foo(c));/*6*/", 3);
		IBinding b6b = getBindingFromASTName("c));/*6*/", 1);
		IBinding b6c = getBindingFromASTName("foo(foo(1));/*7*/", 3);
		IBinding b6d = getBindingFromASTName("foo(1));/*7*/", 3);
		
		IBinding b7 = getBindingFromASTName("foo/*h*/", 3);
		IBinding b7a = getBindingFromASTName("c);/*8*/", 1);
		
		IBinding b8 = getBindingFromASTName("foo/*i*/", 3);
		
		IBinding b9 = getBindingFromASTName("foo/*j*/", 3);
		IBinding b9a = getBindingFromASTName("C));/*9*/", 1);
		
		IBinding b10 = getBindingFromASTName("foo/*k*/", 3);
		IBinding b10a = getBindingFromASTName("cp);/*10*/", 2);
		
		IBinding b11 = getBindingFromASTName("foo/*l*/", 3);
		IBinding b11a = getBindingFromASTName("cp);/*11*/", 2);
		
		IBinding b12 = getBindingFromASTName("foo/*m*/", 3);
		IBinding b12a = getBindingFromASTName("C());/*12*/", 1);
		// IBinding b13 = getBindingFromASTName(ast, "foo/*n*/", 3);
	}

	// // header content
	//	class C { public:
	//	typedef int i1;	typedef long *lp1;
	//	class C1 {}; struct S1 {}; union U1 {}; enum E1 {A1};
	//	};
	//	struct S { public:
	//	typedef int i2; typedef long *lp2;
	//	class C2 {}; struct S2 {}; union U2 {}; enum E2 {A2};
	//	};
	//	union U { public:
	//	typedef int i3; typedef long *lp3;
	//	class C3 {}; struct S3 {}; union U3 {}; enum E3 {A3};
	//	};
	//	enum E {A};
	//	namespace n {
	//		typedef int i4;	typedef long *lp4;
	//		class C4 {}; struct S4 {}; union U4 {}; enum E4 {A4};
	//	}
	//	void f(int);
	//	void f(long);
	//	void f(C); void f(C::i1); void f(C::lp1); void f(C::S1); void f(C::U1); void f(C::E1);
	//	void f(S); void f(S::i2); void f(S::lp2); void f(S::S2); void f(S::U2); void f(S::E2);	
	//	void f(U); void f(U::i3); void f(U::lp3); void f(U::S3); void f(U::U3); void f(U::E3);
	//	void f(n::i4); void f(n::lp4); void f(n::S4); void f(n::U4); void f(n::E4);
	//	void f(E);
	
	// // reference content
	//  #include "header.h"
	//	void references() {
	//		void (*fintptr)(int), (*flongptr)(long);
	//		void (*fC)(C), (*fCi1)(C::i1), (*fClp1)(C::lp1), (*fCS1)(C::S1), (*fCU1)(C::U1), (*fCE1)(C::E1);
	//		void (*fS)(S), (*fSi2)(S::i2), (*fSlp2)(S::lp2), (*fSS2)(S::S2), (*fSU2)(S::U2), (*fSE2)(S::E2);
	//		void (*fU)(U), (*fUi3)(U::i3), (*fUlp3)(U::lp3), (*fUS3)(U::S3), (*fUU3)(U::U3), (*fUE3)(U::E3);
	//		void           (*fni4)(n::i4), (*fnlp4)(n::lp4), (*fnS4)(n::S4), (*fnU4)(n::U4), (*fnE4)(n::E4);
	//		void (*fE)(E);
	//		fintptr = &f;/*0*/ flongptr = &f;/*1*/
	//		fC = &f;/*2*/ fCi1 = &f;/*3*/ fClp1 = &f;/*4*/ fCS1 = &f;/*5*/ fCU1 = &f;/*6*/ fCE1 = &f;/*7*/
	//		fS = &f;/*8*/ fSi2 = &f;/*9*/ fSlp2 = &f;/*10*/ fSS2 = &f;/*11*/ fSU2 = &f;/*12*/ fSE2 = &f;/*13*/
	//		fU = &f;/*14*/ fUi3 = &f;/*15*/ fUlp3 = &f;/*16*/ fUS3 = &f;/*17*/ fUU3 = &f;/*18*/ fUE3 = &f;/*19*/
	//		         fni4 = &f;/*20*/ fnlp4 = &f;/*21*/ fnS4 = &f;/*22*/ fnU4 = &f;/*23*/ fnE4 = &f;/*24*/
	//		fE = &f;/*25*/
	//	}	
	public void _testAddressOfOverloadedFunction() throws DOMException {
		IBinding b0 = getBindingFromASTName("f;/*0*/", 1);
		IBinding b1 = getBindingFromASTName("f;/*1*/", 1);
		IBinding b2 = getBindingFromASTName("f;/*2*/", 1);
		IBinding b3 = getBindingFromASTName("f;/*3*/", 1);
		IBinding b4 = getBindingFromASTName("f;/*4*/", 1);
		IBinding b5 = getBindingFromASTName("f;/*5*/", 1);
		IBinding b6 = getBindingFromASTName("f;/*6*/", 1);
		IBinding b7 = getBindingFromASTName("f;/*7*/", 1);
		IBinding b8 = getBindingFromASTName("f;/*8*/", 1);
		IBinding b9 = getBindingFromASTName("f;/*9*/", 1);
		IBinding b10= getBindingFromASTName("f;/*10*/", 1);
		IBinding b11 = getBindingFromASTName("f;/*11*/", 1);
		IBinding b12 = getBindingFromASTName("f;/*12*/", 1);
		IBinding b13 = getBindingFromASTName("f;/*13*/", 1);
		IBinding b14 = getBindingFromASTName("f;/*14*/", 1);
		IBinding b15 = getBindingFromASTName("f;/*15*/", 1);
		IBinding b16 = getBindingFromASTName("f;/*16*/", 1);
		IBinding b17 = getBindingFromASTName("f;/*17*/", 1);
		IBinding b18 = getBindingFromASTName("f;/*18*/", 1);
		IBinding b19 = getBindingFromASTName("f;/*19*/", 1);
		IBinding b20 = getBindingFromASTName("f;/*20*/", 1);
		IBinding b21 = getBindingFromASTName("f;/*21*/", 1);
		IBinding b22 = getBindingFromASTName("f;/*22*/", 1);
		IBinding b23 = getBindingFromASTName("f;/*23*/", 1);
		IBinding b24 = getBindingFromASTName("f;/*24*/", 1);
	}
}
