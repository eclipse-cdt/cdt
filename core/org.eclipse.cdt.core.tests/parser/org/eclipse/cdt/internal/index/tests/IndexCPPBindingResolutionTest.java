/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * For testing PDOM binding CPP language resolution
 */
/*
 * aftodo - once we have non-problem bindings working, each test should
 * additionally check that the binding obtained has characteristics as
 * expected (type,name,etc..)
 */
public abstract class IndexCPPBindingResolutionTest extends IndexBindingResolutionTestBase {

	public static class SingleProject extends IndexCPPBindingResolutionTest {
		public SingleProject() {setStrategy(new SinglePDOMTestStrategy(true));}
		public static TestSuite suite() {return suite(SingleProject.class);}
	}
	public static class ProjectWithDepProj extends IndexCPPBindingResolutionTest {
		public ProjectWithDepProj() {setStrategy(new ReferencedProject(true));}
		public static TestSuite suite() {return suite(ProjectWithDepProj.class);}
	}
	
	public static void addTests(TestSuite suite) {		
		suite.addTest(SingleProject.suite());
		suite.addTest(ProjectWithDepProj.suite());
	}
	
	//	namespace ns { class A; enum E {E1}; typedef int T; }
	//
	//	class B {
	//    public:
	//	  void m(ns::A* a);
	//    void n(ns::E* a);
	// 	  void o(ns::T* a);
	//    void p(ns::E a);
	//	};
	
	//	namespace ns {
	//	  class A {};
	//    typedef int T;
	//	}
	//
	//	using ns::A;
	//	using ns::E;
	//	using ns::T;
	//  using ns::E1;
	// 
	//	void B::m(A* a) {}
	//	void B::n(E* a) {}
	//	void B::o(T* a) {}
    //  void B::p(E a) {}
	//
	//  void usage() {
	//    B b;
	//    b.p(E1);
	//  }
	public void testUsingTypeDirective_201177() {
		IBinding b0= getBindingFromASTName("B::m", 4);
		IBinding b1= getBindingFromASTName("B::n", 4);
		IBinding b2= getBindingFromASTName("B::o", 4);
		IBinding b3= getBindingFromASTName("p(E1)", 1);
		assertInstance(b0, ICPPMethod.class);
		assertInstance(b1, ICPPMethod.class);
		assertInstance(b2, ICPPMethod.class);
	}
	
	// namespace n { class A{}; class B{}; class C{}; }

	// namespace m {
	//    using namespace n;
	//    class D{};
	// }
	// m::C c;
	// m::D d;
	public void testUsingNamingDirective_177917_1a() {
		IBinding b0= getBindingFromASTName("C c", 1);
		IBinding b1= getBindingFromASTName("D d", 1);
	}
	
	// namespace n { class A{}; }
	// namespace m {
	// using namespace n;
	//     class B {};
	// }

	// #include "header.h"
	// namespace n { class C{}; }
	// m::C c;
	public void testUsingNamingDirective_177917_1b() {
		IBinding b0= getBindingFromASTName("C c", 1);
	}

	// int ff(int x) { return x; }
	// namespace n { class A {}; }
	// namespace m { class B {}; enum C{CE1,CE2}; }
	// namespace o { int (*f)(int)= ff; }

	// using n::A;
	// A a;
	// using namespace m;
	// B b;
	// C c= CE1;
	// using o::f;
	// int g(int x) {return 4;}
	// int g(char x) {return 2;}
	// int nn= g(f(2));
	public void testUsingTypeDirective_177917_1() {
		IBinding b1= getBindingFromASTName("A a", 1);
		IBinding b2= getBindingFromASTName("B b", 1);
		IBinding b3= getBindingFromASTName("C c", 1);
		IBinding b4= getBindingFromASTName("CE1", 3);
		IBinding b5= getBindingFromASTName("f(2", 1);
	}
	
	// namespace a { class A {}; }
	// namespace b {
	//     using a::A;
	//     class B {};
	// }

	// #include "header.h"
	// b::A aa;
	// b::B bb;
	public void testUsingTypeDirective_177917_2() {
		IBinding b0= getBindingFromASTName("A aa", 1);
		IBinding b1= getBindingFromASTName("B bb", 1);
	}
	
	//	namespace header {
	//		class clh {
	//		};
	//		void fh();
	//		void fh(int a);
	//		
	//		class cl {
	//		};
	//		void f();
	//		void f(int a);
	//	}
	//	using header::clh;
	//	using header::fh;
		
	//	#include "header.h"
	//	namespace source {
	//		class cls {
	//		};
	//		void fs();
	//		void fs(int a);
	//		
	//	}
	//	using header::cl;
	//	using header::f;
	//
	//
	//	using source::cls;
	//	using source::fs;
	//
	//	void test() {
	//		fh();
	//		fh(1);  
	//		      
	//		clh c;
	//		
	//		f();
	//		f(1);
	//		cl c1;
	//
	//		fs();
	//		fs(1);
	//		cls c2;
	//	}
	public void testUsingOverloadedFunctionDirective() {
		IBinding b;
		b= getBindingFromASTName("fh()", 2);
		b= getBindingFromASTName("fh(1)", 2);
		b= getBindingFromASTName("clh c", 3);
		b= getBindingFromASTName("f()", 1);
		b= getBindingFromASTName("f(1)", 1);
		b= getBindingFromASTName("cl c1", 2);
		b= getBindingFromASTName("fs()", 2);
		b= getBindingFromASTName("fs(1)", 2);
		b= getBindingFromASTName("cls c2", 3);
	}

	
	// int (*f)(int);
	// int g(int n){return n;}
	// int g(int n, int m){ return n+m; }
	
	// void foo() {
	//    f= g;
	// }
	public void testPointerToFunction() throws Exception {
		IBinding b0 = getBindingFromASTName("f= g;", 1);		
		IBinding b1 = getBindingFromASTName("g;", 1);
		
		assertInstance(b0, ICPPVariable.class);
		ICPPVariable v0= (ICPPVariable) b0;
		assertInstance(v0.getType(), IPointerType.class);
		IPointerType p0= (IPointerType) v0.getType();
		assertInstance(p0.getType(), ICPPFunctionType.class);
		ICPPFunctionType f0= (ICPPFunctionType) p0.getType();
		assertInstance(f0.getReturnType(), ICPPBasicType.class);
		assertEquals(1, f0.getParameterTypes().length);
		assertInstance(f0.getParameterTypes()[0], ICPPBasicType.class);
		
		assertInstance(b1, ICPPFunction.class);
		ICPPFunctionType f1= ((ICPPFunction)b1).getType();
		assertInstance(f1.getReturnType(), ICPPBasicType.class);
		assertEquals(1, f1.getParameterTypes().length);
		assertInstance(f1.getParameterTypes()[0], ICPPBasicType.class);
	}

	// // header file
	//  class Base {public: int field; void foo() {}};
	//	class C : public Base {
	//		public:
	//			struct CS { long* l; C *method(CS **); };
	//			CS cs;
	//			CS **cspp;
	//			long * CS::* ouch;
	//			long * CS::* autsch;
	//			C* (CS::*method)(CS **);
	//		};

	// // referencing file
	//	#include "header.h"
	//  
	//  C *cp = new C(); /*b0, b1*/
	//	void references() {
	//		long l = 5, *lp;
	//		lp = &l;
	//		cp->cs.*cp->ouch = lp = cp->cs.*cp->autsch; /*b2, b3, b4*/
	//		&(cp->cs)->*cp->autsch = lp = &(cp->cs)->*cp->ouch;
	//		(cp->cs).method(cp->cspp);/*1*/ (&(cp->cs))->method(cp->cspp);/*2*/
	//		((cp->cs).*(cp->method))(cp->cspp);/*3*/
	//		((&(cp->cs))->*(cp->method))(cp->cspp);/*4*/
	//	}
	public void testPointerToMemberFields() throws IOException, DOMException {
		IBinding b0 = getBindingFromASTName("C *cp", 1);
		assertClassType((ICPPClassType)b0, "C", ICPPClassType.k_class, 1, 6, 5, 9, 0, 1, 0, 2, 1);
		
		IBinding b1 = getBindingFromASTName("cp = new C()", 2);
		assertVariable(b1, "cp", IPointerType.class, null);
		IPointerType b1type = (IPointerType) ((ICPPVariable)b1).getType();
		assertClassType(b1type.getType(), "C", ICPPClassType.k_class, 1, 6, 5, 9, 0, 1, 0, 2, 1);
		
		IBinding b2 = getBindingFromASTName("cs.*cp->o", 2);
		ICPPField field0 = (ICPPField) b2;
		assertTrue(field0.getType() instanceof ICPPClassType);

		IBinding b3 = getBindingFromASTName("ouch = lp", 4);
		assertField(b3, "C::ouch", ICPPPointerToMemberType.class, null);
		assertPTM(((ICPPField)b3).getType(), "C::CS", null);

		IBinding b4 = getBindingFromASTName("autsch;", 6);
		assertField(b4, "C::autsch", ICPPPointerToMemberType.class, null);
		assertPTM(((ICPPField)b4).getType(), "C::CS", null);

		IBinding b5 = getBindingFromASTName("cs)->*cp->a", 2);
		assertField(b5, "C::cs", ICPPClassType.class, "C::CS");
		assertClassType(((ICPPField)b5).getType(), "C::CS", ICompositeType.k_struct, 0, 1, 1, 5, 1, 1, 0, 2, 0);

		IBinding b6 = getBindingFromASTName("autsch = lp", 6);
		assertField(b4, "C::autsch", ICPPPointerToMemberType.class, null);
		assertPTM(((ICPPField)b4).getType(), "C::CS", null);

		IBinding b7 = getBindingFromASTName("ouch;", 4);
		assertField(b3, "C::ouch", ICPPPointerToMemberType.class, null);
		assertPTM(((ICPPField)b3).getType(), "C::CS", null);
	}

	// // header file
	// class C {}; struct S {}; union U {}; enum E {ER1,ER2,ER3};
	// int var1; C var2; S *var3; void func(E); void func(C);
	// namespace ns {}
	// typedef int Int; typedef int *IntPtr;
	// void func(int*); void func(int);

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
	public void testSimpleGlobalBindings() throws IOException, DOMException {
		{
 			IBinding b0 = getBindingFromASTName("C c; ", 1);
			assertClassTypeBinding(b0, "C", ICPPClassType.k_class, 0, 0, 0, 4, 0, 0, 0, 2, 0);

			IBinding b1 = getBindingFromASTName("c; ", 1);
			assertVariable(b1, "c", ICPPClassType.class, "C");
			ICPPClassType b1type = (ICPPClassType)((ICPPVariable)b1).getType();
			assertClassTypeBinding(b1type, "C", ICPPClassType.k_class, 0, 0, 0, 4, 0, 0, 0, 2, 0);
			assertTrue(b1type.getScope() == null);
			assertTrue(b1type.getCompositeScope() instanceof ICPPClassScope);
			assertClassTypeBinding(((ICPPClassScope)b1type.getCompositeScope()).getClassType(), "C", ICPPClassType.k_class, 0, 0, 0, 4, 0, 0, 0, 2, 0);
		}
		{
			IBinding b2 = getBindingFromASTName("S s;", 1);
			assertClassTypeBinding(b2, "S", ICompositeType.k_struct, 0, 0, 0, 4, 0, 0, 0, 2, 0);

			IBinding b3 = getBindingFromASTName("s;", 1);
			assertVariable(b3, "s", ICPPClassType.class, "S");
			ICPPClassType b3type = (ICPPClassType)((ICPPVariable)b3).getType();
			assertClassTypeBinding(b3type, "S", ICompositeType.k_struct, 0, 0, 0, 4, 0, 0, 0, 2, 0);
		}
		{
			IBinding b4 = getBindingFromASTName("U u;", 1);
			assertClassTypeBinding(b4, "U", ICompositeType.k_union, 0, 0, 0, 4, 0, 0, 0, 2, 0);

			IBinding b5 = getBindingFromASTName("u; ", 1);
			assertVariable(b5, "u", ICPPClassType.class, "U");
			ICPPClassType b5type = (ICPPClassType)((ICPPVariable)b5).getType();
			assertClassTypeBinding(b5type, "U", ICompositeType.k_union, 0, 0, 0, 4, 0, 0, 0, 2, 0);
		}
		{
			IBinding b6 = getBindingFromASTName("E e; ", 1);
			assertEnumeration(b6, "E", new String[] {"ER1","ER2","ER3"});

			IBinding b7 = getBindingFromASTName("e; ", 1);
			assertVariable(b7, "e", IEnumeration.class, "E");
			IEnumeration b5type = (IEnumeration)((ICPPVariable)b7).getType();
			assertEnumeration(b5type, "E", new String[] {"ER1","ER2","ER3"});
			assertTrue(b5type.getScope() == null);
		}
		{
			IBinding b8 = getBindingFromASTName("var1 = 1;", 4);
			assertVariable(b8, "var1", ICPPBasicType.class, null);
		}
		{
			IBinding b9 = getBindingFromASTName("var2 = c;", 4);
			assertVariable(b9, "var2", ICPPClassType.class, "C");
			ICPPClassType b9type = (ICPPClassType)((ICPPVariable)b9).getType();
			assertClassTypeBinding(b9type, "C", ICPPClassType.k_class, 0, 0, 0, 4, 0, 0, 0, 2, 0);
		}
		{
			IBinding b10 = getBindingFromASTName("var3 = &s;", 4);
			assertVariable(b10, "var3", IPointerType.class, null);
			IPointerType b10type = (IPointerType) ((ICPPVariable)b10).getType();
			assertClassTypeBinding((ICPPClassType) b10type.getType(), "S", ICompositeType.k_struct, 0, 0, 0, 4, 0, 0, 0, 2, 0);
		}
		{
			IBinding b11 = getBindingFromASTName("func(e);", 4);
		}
		IBinding b12 = getBindingFromASTName("func(var1);", 4);
		IBinding b13 = getBindingFromASTName("func(c);", 4);
		IBinding b14 = getBindingFromASTName("Int a; ", 3);
		IBinding b15 = getBindingFromASTName("a; ", 1);
		IBinding b16 = getBindingFromASTName("IntPtr b = &a; ", 6);
		IBinding b17 = getBindingFromASTName("b = &a; /*b*/", 1);
		IBinding b18 = getBindingFromASTName("func(*b);", 4);
		IBinding b19 = getBindingFromASTName("b); /*func4*/", 1);
		IBinding b20 = getBindingFromASTName("func(a);", 4);
		IBinding b21 = getBindingFromASTName("a); /*func5*/", 1);
		IBinding b22 = getBindingFromASTName("C2 : public", 2);
		IBinding b23 = getBindingFromASTName("C {}; /*base*/", 1);		
		IBinding b24 = getBindingFromASTName("S2 : public", 2);
		IBinding b25 = getBindingFromASTName("S {}; /*base*/", 1);		
	}


	//// header content
	//class TopC {}; struct TopS {}; union TopU {}; enum TopE {TopER1,TopER2};
	//short topBasic; void *topPtr; TopC *topCPtr; TopU topFunc(){return *new TopU();}

	//// referencing content
	//namespace n1 {
	//   class TopC {}; struct TopS {}; union TopU {}; enum TopE {TopER1,TopER2};
	//   short topBasic; void *topPtr;/*A*/ TopC *topCPtr;/*A*/ TopU topFunc(){return *new TopU();}   
	//   class C {
	//      class TopC {}; struct TopS {}; union TopU {}; enum TopE {TopER1,TopER2};
	//      short topBasic; void *topPtr;/*B*/ TopC *topCPtr;/*B*/ TopU topFunc(){return *new TopU();}
	//      void references() {
	//         ::TopC c; ::TopS s; ::TopU u; ::TopE e = ::TopER1;
	//         ::topBasic++; ::topPtr = &::topBasic; ::topCPtr = &c; ::topFunc();
	//      } 
	//   };
	//}
	public void testSingletonQualifiedName() {
		IBinding b0 = getBindingFromASTName("TopC c", 4);
		IBinding b1 = getBindingFromASTName("TopS s", 4);
		IBinding b2 = getBindingFromASTName("TopU u", 4);
		IBinding b3 = getBindingFromASTName("TopE e", 4);
		IBinding b4 = getBindingFromASTName("TopER1;", 6);
		IBinding b5 = getBindingFromASTName("topBasic++", 8);
		IBinding b6 = getBindingFromASTName("topPtr = &", 6);
		IBinding b7 = getBindingFromASTName("topBasic; ::", 8);
		IBinding b8 = getBindingFromASTName("topCPtr = &", 7);
		IBinding b9 = getBindingFromASTName("topFunc();", 7);

		IBinding _b5 = getBindingFromASTName("topBasic; v", 8);
		IBinding _b6 = getBindingFromASTName("topPtr;/*A*/", 6);
		IBinding _b7 = getBindingFromASTName("topPtr;/*B*/", 6);
		IBinding _b8 = getBindingFromASTName("topCPtr;/*A*/", 7);
		IBinding _b9 = getBindingFromASTName("topCPtr;/*B*/", 7);
		IBinding _b10 = getBindingFromASTName("topFunc(){", 7);
	}

	//	// header content
	// namespace n1 { namespace n2 { struct S {}; } }
	// class c1 { public: class c2 { public: struct S {}; }; };
	// struct s1 { struct s2 { struct S {}; }; };
	// union u1 { struct u2 { struct S {}; }; };
	// namespace n3 { class c3 { public: struct s3 { union u3 { struct S {}; }; }; }; }

	// // reference content
	// void reference() {
	//  ::n1::n2::S _s0; n1::n2::S _s1;
	//  ::c1::c2::S _s2; c1::c2::S _s3;
	//  ::s1::s2::S _s4; s1::s2::S _s5;
	//  ::u1::u2::S _s6; u1::u2::S _s7;
	//  ::n3::c3::s3::u3::S _s8;
	//    n3::c3::s3::u3::S _s9;
	// }
	// namespace n3 { c3::s3::u3::S _s10; }
	// namespace n1 { n2::S _s11; }
	// namespace n1 { namespace n2 { S _s12; }} 
	public void testQualifiedNamesForStruct() throws DOMException {
		IBinding b0 = getBindingFromASTName("S _s0;", 1);
		assertTrue(b0.getScope() instanceof ICPPNamespaceScope);
		assertTrue(b0.getScope().getParent() instanceof ICPPNamespaceScope);
		assertTrue(b0.getScope().getParent().getParent() == null);
		assertQNEquals("n1::n2::S", b0);
		
		IBinding b1 = getBindingFromASTName("S _s1;", 1);
		assertTrue(b1.getScope() instanceof ICPPNamespaceScope);
		assertTrue(b1.getScope().getParent() instanceof ICPPNamespaceScope);
		assertTrue(b1.getScope().getParent().getParent() == null);
		assertQNEquals("n1::n2::S", b1);
		
		IBinding b2 = getBindingFromASTName("S _s2;", 1);
		assertTrue(b2.getScope() instanceof ICPPClassScope);
		assertTrue(b2.getScope().getParent() instanceof ICPPClassScope);
		assertTrue(b2.getScope().getParent().getParent() == null);
		assertQNEquals("c1::c2::S", b2);
		
		IBinding b3 = getBindingFromASTName("S _s3;", 1);
		assertQNEquals("c1::c2::S", b3);
		IBinding b4 = getBindingFromASTName("S _s4;", 1);
		assertQNEquals("s1::s2::S", b4);
		IBinding b5 = getBindingFromASTName("S _s5;", 1);
		assertQNEquals("s1::s2::S", b5);
		IBinding b6 = getBindingFromASTName("S _s6;", 1);
		assertQNEquals("u1::u2::S", b6);
		IBinding b7 = getBindingFromASTName("S _s7;", 1);
		assertQNEquals("u1::u2::S", b7);
		IBinding b8 = getBindingFromASTName("S _s8;", 1);
		assertQNEquals("n3::c3::s3::u3::S", b8);
		IBinding b9 = getBindingFromASTName("S _s9;", 1);
		assertQNEquals("n3::c3::s3::u3::S", b9);
		
		IBinding b10 = getBindingFromASTName("S _s10;", 1);
		assertTrue(b10.getScope() instanceof ICPPClassScope);
		assertTrue(b10.getScope().getParent() instanceof ICPPClassScope);
		assertTrue(b10.getScope().getParent().getParent() instanceof ICPPClassScope);
		assertTrue(b10.getScope().getParent().getParent().getParent() instanceof ICPPNamespaceScope);
		assertTrue(b10.getScope().getParent().getParent().getParent().getParent() == null);
		assertQNEquals("n3::c3::s3::u3::S", b10);
		
		IBinding b11 = getBindingFromASTName("S _s11;", 1);
		assertQNEquals("n1::n2::S", b11);
		IBinding b12 = getBindingFromASTName("S _s12;", 1);
		assertQNEquals("n1::n2::S", b12);
	}

	// // header content
	// namespace n1 { namespace n2 { union U {}; } }
	// class c1 { public: class c2 { public: union U {}; }; }; 
	// struct s1 { struct s2 { union U {}; }; };
	// union u1 { struct u2 { union U {}; }; };
	// namespace n3 { class c3 { public: struct s3 { union u3 { union U {}; }; }; }; }

	// // reference content
	// void reference() {
	//  ::n1::n2::U _u0; n1::n2::U _u1;
	//  ::c1::c2::U _u2; c1::c2::U _u3;
	//  ::s1::s2::U _u4; s1::s2::U _u5;
	//  ::u1::u2::U _u6; u1::u2::U _u7;
	//  ::n3::c3::s3::u3::U _u8;
	//    n3::c3::s3::u3::U _u9;
	// }
	// namespace n3 { c3::s3::u3::U _u10; }
	// namespace n1 { n2::U _u11; }
	// namespace n1 { namespace n2 { U _u12; }} 
	public void testQualifiedNamesForUnion() throws DOMException {
		IBinding b0 = getBindingFromASTName("U _u0;", 1);
		assertQNEquals("n1::n2::U", b0);
		IBinding b1 = getBindingFromASTName("U _u1;", 1);
		assertQNEquals("n1::n2::U", b1);
		IBinding b2 = getBindingFromASTName("U _u2;", 1);
		assertQNEquals("c1::c2::U", b2);
		IBinding b3 = getBindingFromASTName("U _u3;", 1);
		assertQNEquals("c1::c2::U", b3);
		IBinding b4 = getBindingFromASTName("U _u4;", 1);
		assertQNEquals("s1::s2::U", b4);
		IBinding b5 = getBindingFromASTName("U _u5;", 1);
		assertQNEquals("s1::s2::U", b5);
		IBinding b6 = getBindingFromASTName("U _u6;", 1);
		assertQNEquals("u1::u2::U", b6);
		IBinding b7 = getBindingFromASTName("U _u7;", 1);
		assertQNEquals("u1::u2::U", b7);
		IBinding b8 = getBindingFromASTName("U _u8;", 1);
		assertQNEquals("n3::c3::s3::u3::U", b8);
		IBinding b9 = getBindingFromASTName("U _u9;", 1);
		assertQNEquals("n3::c3::s3::u3::U", b9);
		IBinding b10 = getBindingFromASTName("U _u10;", 1);
		assertQNEquals("n3::c3::s3::u3::U", b10);
		IBinding b11 = getBindingFromASTName("U _u11;", 1);
		assertQNEquals("n1::n2::U", b11);
		IBinding b12 = getBindingFromASTName("U _u12;", 1);
		assertQNEquals("n1::n2::U", b12);
	}

	// // header content
	// namespace n1 { namespace n2 { class C {}; } }
	// class c1 { public: class c2 { public: class C {}; }; };
	// struct s1 { struct s2 { class C {}; }; };
	// union u1 { union u2 { class C {}; }; };
	// namespace n3 { class c3 { public: struct s3 { union u3 { class C {}; }; }; }; }

	// // reference content
	// void reference() {
	//  ::n1::n2::C _c0; n1::n2::C _c1;
	//  ::c1::c2::C _c2; c1::c2::C _c3;
	//  ::s1::s2::C _c4; s1::s2::C _c5;
	//  ::u1::u2::C _c6; u1::u2::C _c7;
	//  ::n3::c3::s3::u3::C _c8;
	//    n3::c3::s3::u3::C _c9;
	// }
	// namespace n3 { c3::s3::u3::C _c10; }
	// namespace n1 { n2::C _c11; }
	// namespace n1 { namespace n2 { C _c12; }} 
	public void testQualifiedNamesForClass() throws DOMException {
		IBinding b0 = getBindingFromASTName("C _c0;", 1);
		assertQNEquals("n1::n2::C", b0);
		IBinding b1 = getBindingFromASTName("C _c1;", 1);
		assertQNEquals("n1::n2::C", b1);
		IBinding b2 = getBindingFromASTName("C _c2;", 1);
		assertQNEquals("c1::c2::C", b2);
		IBinding b3 = getBindingFromASTName("C _c3;", 1);
		assertQNEquals("c1::c2::C", b3);
		IBinding b4 = getBindingFromASTName("C _c4;", 1);
		assertQNEquals("s1::s2::C", b4);
		IBinding b5 = getBindingFromASTName("C _c5;", 1);
		assertQNEquals("s1::s2::C", b5);
		IBinding b6 = getBindingFromASTName("C _c6;", 1);
		assertQNEquals("u1::u2::C", b6);
		IBinding b7 = getBindingFromASTName("C _c7;", 1);
		assertQNEquals("u1::u2::C", b7);
		IBinding b8 = getBindingFromASTName("C _c8;", 1);
		assertQNEquals("n3::c3::s3::u3::C", b8);
		IBinding b9 = getBindingFromASTName("C _c9;", 1);
		assertQNEquals("n3::c3::s3::u3::C", b9);
		IBinding b10 = getBindingFromASTName("C _c10;", 1);
		assertQNEquals("n3::c3::s3::u3::C", b10);
		IBinding b11 = getBindingFromASTName("C _c11;", 1);
		assertQNEquals("n1::n2::C", b11);
		IBinding b12 = getBindingFromASTName("C _c12;", 1);
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
	public void testQualifiedNamesForTypedef() throws DOMException {
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
	public void testEnumeratorInClassScope() {
		IBinding b0 = getBindingFromASTName("E e1", 1);
		IBinding b1 = getBindingFromASTName("ER1; }", 3);
		IBinding b2 = getBindingFromASTName("ER2; }", 3);
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
	public void testEnumeratorInStructScope() {
		IBinding b0 = getBindingFromASTName("E e1", 1);
		IBinding b1 = getBindingFromASTName("ER1; }", 3);
		IBinding b2 = getBindingFromASTName("ER2; }", 3);
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
	public void testEnumeratorInUnionScope() {
		IBinding b0 = getBindingFromASTName("E e1", 1);
		IBinding b1 = getBindingFromASTName("ER1; }", 3);
		IBinding b2 = getBindingFromASTName("ER2; }", 3);
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
	public void testEnumeratorInNamespaceScope() {
		IBinding b0 = getBindingFromASTName("E e1", 1);
		IBinding b1 = getBindingFromASTName("ER1; }", 3);
		IBinding b2 = getBindingFromASTName("ER2; }", 3);
	}

	// // the header
	// void foo(int a=2, int b=3);

	// #include "header.h"
	// void ref() { foo(); }
	public void testFunctionDefaultArguments() {
		IBinding b0 = getBindingFromASTName("foo();", 3);
	}

	// // the header
	// typedef int TYPE;
	// namespace ns {
	//    const TYPE* foo(int a);
	// };

	// #include "header.h"
	// const TYPE* ns::foo(int a) { return 0; }
	public void testTypeQualifier() {
		IBinding b0 = getBindingFromASTName("foo(", 3);
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
	public void testMethodHidingInInheritance() {
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
	//	/* not applicable ?? */                              // IASTBinaryExpression
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
	public void testFieldReference() {
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

	//	// header file
	//	class C {public: C* cp;};
	//	C foo(C c);
	//	C* foo(C* c);
	//	int foo(int i);
	//	int foo(int i, C c);

	//	// referencing content
	//	#include "header.h"
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
	public void testExpressionKindForFunctionCalls() {
		// depends on bug 164470 because resolution takes place during parse.
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

		IBinding b5 = getBindingFromASTName("cp);/*4*/", 2);
		IBinding b5a = getBindingFromASTName("foo/*f*/", 3);
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
	public void testAddressOfOverloadedFunction() throws DOMException {
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

	// struct C {
	//	 int m1(int a);
	//	 int m2(int a) const;
	// }; 
	//
	// C* func(int (C::*m)(int) const);
	// C* func(int (C::*m)(int));

	// void ref() {
	//	 func(&C::m1);
	//	 func(&C::m2);
	// }
    public void testAddressOfConstMethod_233889() throws Exception {
		IBinding fn1= getBindingFromASTName("func(&C::m1", 4, ICPPFunction.class);
		IBinding fn2= getBindingFromASTName("func(&C::m2", 4, ICPPFunction.class);
		assertNotSame(fn1, fn2);
    }

	// // the header
	// void f_int(int);
	// void f_const_int(const int);
	// void f_int_ptr(int*);

	// #include "header.h"
	// void ref() { 
	// 	 int 			i				= 0;
	//   const int 		const_int		= 0;
	//
	//   f_int(i);				 // ok
	//   f_int(const_int);       // ok (passed as value)
	//   f_const_int(i);		 // ok
	//   f_const_int(const_int); // ok
	// }
	//
	//  void f_const_int(const int const_int) {
	//     f_int_ptr(&const_int); // error
	//  }  
	public void testConstIntParameter() {
		getBindingFromASTName("f_int(i)", 5);
		getBindingFromASTName("f_int(const_int)", 5);
		getBindingFromASTName("f_const_int(i)", 11);
		getBindingFromASTName("f_const_int(const_int)", 11);
		getProblemFromASTName("f_int_ptr(&const_int)", 9);
	}

	// // the header
	// void f_int_ptr(int*);
	// void f_const_int_ptr(const int*);
	// void f_int_const_ptr(int const*);
	// void f_int_ptr_const(int *const);
	// void f_const_int_ptr_const(const int*const);
	// void f_int_const_ptr_const(int const*const);

	// #include "header.h"
	// void ref() { 
	// 	 int* 			int_ptr			= 0;
	//   const int*		const_int_ptr   = 0;
	// 	 int const*     int_const_ptr	= 0;
	// 	 int *const     int_ptr_const	= 0;
	//   const int*const		const_int_ptr_const   = 0;
	//   int const*const		int_const_ptr_const   = 0;
	//
	//   f_int_ptr(int_ptr);				// ok
	//   f_int_ptr(const_int_ptr);			// error
	//   f_int_ptr(int_const_ptr);			// error
	//   f_int_ptr(int_ptr_const);			// ok
	//   f_int_ptr(const_int_ptr_const);	// error
	//   f_int_ptr(int_const_ptr_const);	// error
	//
	//   f_const_int_ptr(int_ptr);				// ok
	//   f_const_int_ptr(const_int_ptr);		// ok
	//   f_const_int_ptr(int_const_ptr);		// ok
	//   f_const_int_ptr(int_ptr_const);		// ok
	//   f_const_int_ptr(const_int_ptr_const);	// ok
	//   f_const_int_ptr(int_const_ptr_const);	// ok
	//
	//   f_int_const_ptr(int_ptr);				// ok
	//   f_int_const_ptr(const_int_ptr);		// ok
	//   f_int_const_ptr(int_const_ptr);		// ok
	//   f_int_const_ptr(int_ptr_const);		// ok
	//   f_int_const_ptr(const_int_ptr_const);	// ok
	//   f_int_const_ptr(int_const_ptr_const);	// ok
	//
	//   f_int_ptr_const(int_ptr);				// ok
	//   f_int_ptr_const(const_int_ptr);		// error
	//   f_int_ptr_const(int_const_ptr);		// error
	//   f_int_ptr_const(int_ptr_const);		// ok
	//   f_int_ptr_const(const_int_ptr_const);	// error
	//   f_int_ptr_const(int_const_ptr_const);	// error
	//
	//   f_const_int_ptr_const(int_ptr);			 // ok
	//   f_const_int_ptr_const(const_int_ptr);		 // ok
	//   f_const_int_ptr_const(int_const_ptr);		 // ok
	//   f_const_int_ptr_const(int_ptr_const);		 // ok
	//   f_const_int_ptr_const(const_int_ptr_const); // ok
	//   f_const_int_ptr_const(int_const_ptr_const); // ok
	//
	//   f_int_const_ptr_const(int_ptr);				// ok
	//   f_int_const_ptr_const(const_int_ptr);			// ok
	//   f_int_const_ptr_const(int_const_ptr);			// ok
	//   f_int_const_ptr_const(int_ptr_const);			// ok
	//   f_int_const_ptr_const(const_int_ptr_const);	// ok
	//   f_int_const_ptr_const(int_const_ptr_const);	// ok
	// }
	public void testConstIntPtrParameter() {
		getBindingFromASTName("f_int_ptr(int_ptr)", 			9);
		getProblemFromASTName("f_int_ptr(const_int_ptr)", 		9);
		getProblemFromASTName("f_int_ptr(int_const_ptr)", 		9);
		getBindingFromASTName("f_int_ptr(int_ptr_const)", 		9);
		getProblemFromASTName("f_int_ptr(const_int_ptr_const)", 9);
		getProblemFromASTName("f_int_ptr(int_const_ptr_const)", 9);

		getBindingFromASTName("f_const_int_ptr(int_ptr)", 				15);
		getBindingFromASTName("f_const_int_ptr(const_int_ptr)", 		15);
		getBindingFromASTName("f_const_int_ptr(int_const_ptr)", 		15);
		getBindingFromASTName("f_const_int_ptr(int_ptr_const)", 		15);
		getBindingFromASTName("f_const_int_ptr(const_int_ptr_const)",	15);
		getBindingFromASTName("f_const_int_ptr(int_const_ptr_const)", 	15);

		getBindingFromASTName("f_int_const_ptr(int_ptr)", 				15);
		getBindingFromASTName("f_int_const_ptr(const_int_ptr)", 		15);
		getBindingFromASTName("f_int_const_ptr(int_const_ptr)", 		15);
		getBindingFromASTName("f_int_const_ptr(int_ptr_const)", 		15);
		getBindingFromASTName("f_int_const_ptr(const_int_ptr_const)",	15);
		getBindingFromASTName("f_int_const_ptr(int_const_ptr_const)", 	15);

		getBindingFromASTName("f_int_ptr_const(int_ptr)", 				15);
		getProblemFromASTName("f_int_ptr_const(const_int_ptr)", 		15);
		getProblemFromASTName("f_int_ptr_const(int_const_ptr)", 		15);
		getBindingFromASTName("f_int_ptr_const(int_ptr_const)", 		15);
		getProblemFromASTName("f_int_ptr_const(const_int_ptr_const)",	15);
		getProblemFromASTName("f_int_ptr_const(int_const_ptr_const)", 	15);

		getBindingFromASTName("f_const_int_ptr_const(int_ptr)", 			21);
		getBindingFromASTName("f_const_int_ptr_const(const_int_ptr)", 		21);
		getBindingFromASTName("f_const_int_ptr_const(int_const_ptr)", 		21);
		getBindingFromASTName("f_const_int_ptr_const(int_ptr_const)", 		21);
		getBindingFromASTName("f_const_int_ptr_const(const_int_ptr_const)",	21);
		getBindingFromASTName("f_const_int_ptr_const(int_const_ptr_const)", 21);

		getBindingFromASTName("f_int_const_ptr_const(int_ptr)", 			21);
		getBindingFromASTName("f_int_const_ptr_const(const_int_ptr)", 		21);
		getBindingFromASTName("f_int_const_ptr_const(int_const_ptr)", 		21);
		getBindingFromASTName("f_int_const_ptr_const(int_ptr_const)", 		21);
		getBindingFromASTName("f_int_const_ptr_const(const_int_ptr_const)",	21);
		getBindingFromASTName("f_int_const_ptr_const(int_const_ptr_const)", 21);
	}

	// // the header

	// void f(int*){}		// b1
	// void f(const int*){}	// b2
	// void f(int const*){}	// b2, redef
	// void f(int *const){}	// b1, redef
	// void f(const int*const){} // b2, redef
	// void f(int const*const){} // b2, redef
	public void testConstIntPtrParameterInDefinitionAST() throws CoreException {
		IBinding binding1= getBindingFromASTName("f(int*){}", 1);
		IBinding binding2= getBindingFromASTName("f(const int*){}", 1);
		getProblemFromASTName("f(int const*){}", 1);
		getProblemFromASTName("f(int *const){}", 1);
		getProblemFromASTName("f(const int*const){}", 1);
		getProblemFromASTName("f(int const*const){}", 1);
	}

	// // the header

	// void f(int&){}		// b1
	// void f(const int&){}	// b2
	// void f(int const&){}	// b2, redef
	public void testConstIntRefParameterInDefinitionAST() throws CoreException {
		IBinding binding1= getBindingFromASTName("f(int&){}", 1);
		IBinding binding2= getBindingFromASTName("f(const int&){}", 1);
		getProblemFromASTName("f(int const&){}", 1);
	}

	// // the header

	// void f(int*);		// b1
	// void f(const int*);	// b2
	// void f(int const*);	// b2
	// void f(int *const);	// b1
	// void f(const int*const);	// b2
	// void f(int const*const); // b2
	//
	// void f(int*){}		// b1
	// void f(const int*){}	// b2
	//
	// void ref() {
	// 	 int* 			int_ptr			= 0;
	//   const int*		const_int_ptr   = 0;
	// 	 int const*     int_const_ptr	= 0;
	// 	 int *const     int_ptr_const	= 0;
	//   const int*const		const_int_ptr_const   = 0;
	//   int const*const		int_const_ptr_const   = 0;
	//
	//   f(int_ptr);				// b1
	//   f(const_int_ptr);			// b2
	//   f(int_const_ptr);			// b2
	//   f(int_ptr_const);			// b1
	//   f(const_int_ptr_const);	// b2
	//   f(int_const_ptr_const);	// b2
	// }
	public void testConstIntPtrParameterInDefinitionAST2() throws CoreException {
		IBinding binding1= getBindingFromASTName("f(int*){}", 1);
		IBinding binding2= getBindingFromASTName("f(const int*){}", 1);

		assertEquals(binding1, getBindingFromASTName("f(int_ptr)", 1));
		assertEquals(binding2, getBindingFromASTName("f(const_int_ptr)", 1));
		assertEquals(binding2, getBindingFromASTName("f(int_const_ptr)", 1));
		assertEquals(binding1, getBindingFromASTName("f(int_ptr_const)", 1));
		assertEquals(binding2, getBindingFromASTName("f(const_int_ptr_const)", 1));
		assertEquals(binding2, getBindingFromASTName("f(int_const_ptr_const)", 1));
	}

	// // the header
	// void f(int*);		// b1
	// void f(const int*);	// b2
	// void f(int const*);	// b2
	// void f(int *const);	// b1
	// void f(const int*const);	// b2
	// void f(int const*const); // b2

	// #include "header.h"
	// void f(int*){}		// b1
	// void f(const int*){}	// b2
	//
	// void ref() {
	// 	 int* 			int_ptr			= 0;
	//   const int*		const_int_ptr   = 0;
	// 	 int const*     int_const_ptr	= 0;
	// 	 int *const     int_ptr_const	= 0;
	//   const int*const		const_int_ptr_const   = 0;
	//   int const*const		int_const_ptr_const   = 0;
	//
	//   f(int_ptr);				// b1
	//   f(const_int_ptr);			// b2
	//   f(int_const_ptr);			// b2
	//   f(int_ptr_const);			// b1
	//   f(const_int_ptr_const);	// b2
	//   f(int_const_ptr_const);	// b2
	// }
	public void testConstIntPtrParameterInDefinition() throws CoreException {
		IBinding binding1= getBindingFromASTName("f(int*){}", 1);
		IBinding binding2= getBindingFromASTName("f(const int*){}", 1);

		assertEquals(binding1, getBindingFromASTName("f(int_ptr)", 1));
		assertEquals(binding2, getBindingFromASTName("f(const_int_ptr)", 1));
		assertEquals(binding2, getBindingFromASTName("f(int_const_ptr)", 1));
		assertEquals(binding1, getBindingFromASTName("f(int_ptr_const)", 1));
		assertEquals(binding2, getBindingFromASTName("f(const_int_ptr_const)", 1));
		assertEquals(binding2, getBindingFromASTName("f(int_const_ptr_const)", 1));

		assertEquals(2, getIndex().findNames(binding1, IIndex.FIND_DECLARATIONS).length);
		assertEquals(4, getIndex().findNames(binding2, IIndex.FIND_DECLARATIONS).length);
		assertEquals(1, getIndex().findNames(binding1, IIndex.FIND_DEFINITIONS).length);
		assertEquals(1, getIndex().findNames(binding2, IIndex.FIND_DEFINITIONS).length);
	}

	// // header file
	// struct myStruct {
	//    int a;
	// };
	// union myUnion {
	//    int b;
	// };

	// // referencing content
	// struct myStruct; 
	// union myUnion;
	// void test() {
	//    struct myStruct* u;
	//    union myUnion* v;
	//    u->a= 1;  // since we include the definition, we may use the type.
	//    v->b= 1;  // since we include the definition, we may use the type.
	// }
	public void testTypeDefinitionWithFwdDeclaration() {
		getBindingFromASTName("a= 1", 1);
		getBindingFromASTName("b= 1", 1);
	}
	
	// namespace x {
	//    int a(int);
	// }
    // using namespace x;
	// using x::a;
	
	// #include "header.h"
	// void test() {
	//    a(1);
	// }
    public void testLegalConflictWithUsingDeclaration() throws Exception {
		getBindingFromASTName("a(1)", 1);
    }
    
	//	class A {};
	//	class B {};
	//	class C {
	//	public:
	//		operator B() {B b; return b;}
	//	};
	//	class D : public C {};
	//	void foo(B b) {}
	
    //  class E : public C {};
	//	void refs() {
	//		C c;
	//		foo(c);
	//		D d;
	//		foo(d);
	//		E e;
	//		foo(e);
	//	}
    public void testUserDefinedConversionOperator_224364() throws Exception {
    	IBinding ca=   getBindingFromASTName("C c;", 1);
    	assertInstance(ca, ICPPClassType.class);
    	    	
    	IBinding foo1= getBindingFromASTName("foo(c)", 3);
    	
    	IBinding da=   getBindingFromASTName("D d", 1);
    	assertInstance(da, ICPPClassType.class);
    	    	
		IBinding foo2= getBindingFromASTName("foo(d)", 3);
		IBinding foo3= getBindingFromASTName("foo(e)", 3);
    }

	// int a= 1+2-3*4+10/2; // -4
	// int b= a+4;
	// int* c= &b;
	// enum X {e0, e4=4, e5, e2=2, e3};

    // void ref() {
    // a; b; c; e0; e2; e3; e4; e5;
    // }
	public void testValues() throws Exception {
		IVariable v= (IVariable) getBindingFromASTName("a;", 1);
		asserValueEquals(v.getInitialValue(), -4);
		v= (IVariable) getBindingFromASTName("b;", 1);
		asserValueEquals(v.getInitialValue(), 0);
		v= (IVariable) getBindingFromASTName("c;", 1);
		assertNull(v.getInitialValue().numericalValue());

		IEnumerator e= (IEnumerator) getBindingFromASTName("e0", 2);
		asserValueEquals(e.getValue(), 0);
		e= (IEnumerator) getBindingFromASTName("e2", 2);
		asserValueEquals(e.getValue(), 2);
		e= (IEnumerator) getBindingFromASTName("e3", 2);
		asserValueEquals(e.getValue(), 3);
		e= (IEnumerator) getBindingFromASTName("e4", 2);
		asserValueEquals(e.getValue(), 4);
		e= (IEnumerator) getBindingFromASTName("e5", 2);
		asserValueEquals(e.getValue(), 5);
	}

	//	namespace ns1 { namespace ns2 {
	//	  class A {};
	//	}}
	//	using namespace ns1::ns2;
	
	//	#include "header.h"
	//	A a;
	public void testUsingDirectiveWithQualifiedName_269727() throws Exception {
    	getBindingFromASTName("A a", 1, ICPPClassType.class);
	}

	// void f(int (&v)[1]);
	// void f(int (&v)[2]);
	
	// void test() {
	//   int a[1], b[2];
	//   f(a); f(b);
	// }
	public void testArrayTypeWithSize_269926() throws Exception {
    	IFunction f1= getBindingFromASTName("f(a)", 1, IFunction.class);
    	IFunction f2= getBindingFromASTName("f(b)", 1, IFunction.class);
    	assertFalse(f1.equals(f2));
	}

	//	class A {
	//	  class B;
	//	  void method();
	//	};

	//	class A::B {
	//	  B(int x);
	//    static void m(int p);
	//	};
	//
	//	void A::method() {
	//	  new B(0);
	//    B::m(0);
	//	}
	public void testNestedClass_284665() throws Exception {
    	ICPPClassType b0 = getBindingFromASTName("B {", 1, ICPPClassType.class);
    	assertFalse(b0 instanceof IIndexBinding);
    	ICPPConstructor b1 = getBindingFromASTName("B(int x)", 1, ICPPConstructor.class);
    	assertFalse(b1 instanceof IIndexBinding);
    	ICPPConstructor b2 = getBindingFromASTName("B(0)", 1, ICPPConstructor.class);
    	assertFalse(b2 instanceof IIndexBinding);
    	assertEquals(b1, b2);
    	ICPPMethod b3 = getBindingFromASTName("m(0)", 1, ICPPMethod.class);
    	assertFalse(b3 instanceof IIndexBinding);
	}

	//	class A {
	//	  friend inline void m(A p) {}
	//	};

	//	void test(A a) {
	//	  m(a);
	//	}
	public void testInlineFriendFunction_284690() throws Exception {
    	getBindingFromASTName("m(a)", 1, IFunction.class);
	}

	/* CPP assertion helpers */
	/* ##################################################################### */

	static protected void assertField(
			IBinding binding,
			String qn,
			Class expType,
			String expTypeQN) {
		try {
			assertTrue(binding instanceof ICPPField);
			ICPPField field = (ICPPField) binding;
			assertQNEquals(qn, field);
			assertTrue(expType.isInstance(field.getType()));
			if (expTypeQN != null) {
				assert(field.getType() instanceof ICPPBinding);
				ICPPBinding tyBinding = (ICPPBinding) field.getType();
				assertQNEquals(expTypeQN, tyBinding);
			}
		} catch (DOMException de) {
			fail(de.getMessage());
		}
	}

	static protected void assertClassTypeBinding(IBinding binding,
			String qn,
			int key,
			int bases,
			int fields,
			int declaredFields,
			int methods,
			int declaredMethods,
			int allDeclaredMethods,
			int friends,
			int constructors,
			int nestedClasses) {
		assertTrue(binding instanceof ICPPClassType);
		assertClassType((ICPPClassType) binding, qn, key, bases, fields, declaredFields, methods,
				declaredMethods, allDeclaredMethods, friends, constructors, nestedClasses);
	}

	static protected void assertClassType(
			IType type,
			String qn,
			int key,
			int bases,
			int fields,
			int declaredFields,
			int methods,
			int declaredMethods,
			int allDeclaredMethods,
			int friends,
			int constructors,
			int nestedClasses) {
		try {
			assertTrue(type instanceof ICPPClassType);
			ICPPClassType classType = (ICPPClassType) type;
			assertQNEquals(qn, classType);
			assertEquals(key, classType.getKey());
			assertEquals(bases, classType.getBases().length);
			assertEquals(fields, classType.getFields().length);
			assertEquals(declaredFields, classType.getDeclaredFields().length);
			assertEquals(methods, classType.getMethods().length);
			assertEquals(declaredMethods, classType.getDeclaredMethods().length);
			assertEquals(allDeclaredMethods, classType.getAllDeclaredMethods().length);
			// assertEquals(friends, classType.getFriends().length); (PDOMNotImplementedError)
			assertEquals(constructors, classType.getConstructors().length);
			assertEquals(nestedClasses, classType.getNestedClasses().length);
		} catch (DOMException de) {
			fail(de.getMessage());
		}
	}

	public void assertEnumeration(IBinding binding, String name, String[] enumerators) throws DOMException {
		assertTrue(binding instanceof IEnumeration);
		assertEquals(name, binding.getName());
		IEnumerator[] aEnumerators = ((IEnumeration)binding).getEnumerators();
		Set expectedEnumerators = new HashSet();
		expectedEnumerators.addAll(Arrays.asList(enumerators));
		Set actualEnumerators = new HashSet();
		for (IEnumerator enumerator : aEnumerators) {
			actualEnumerators.add(enumerator.getName());
 		}
 		assertEquals(expectedEnumerators, actualEnumerators);
 	}

	/**
 	 * @param type
 	 * @param cqn
 	 * @param qn may be null
 	 */
	static protected void assertPTM(IType type, String cqn, String qn) {
		assertTrue(type instanceof ICPPPointerToMemberType);
		ICPPPointerToMemberType ptmt = (ICPPPointerToMemberType) type;
		ICPPClassType classType = (ICPPClassType) ptmt.getMemberOfClass();
		assertQNEquals(cqn, classType);
		if (qn != null) {
			assert(ptmt.getType() instanceof ICPPBinding);
			ICPPBinding tyBinding = (ICPPBinding) ptmt.getType();
			assertQNEquals(qn, tyBinding);
		}
	}

	private void asserValueEquals(IValue initialValue, long i) {
		assertNotNull(initialValue);
		final Long numericalValue = initialValue.numericalValue();
		assertNotNull(numericalValue);
		assertEquals(i, numericalValue.longValue());
	}
}
