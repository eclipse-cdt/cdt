/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * Tests for exercising resolution of template bindings against IIndex
 */
public class IndexCPPTemplateResolutionTest extends IndexBindingResolutionTestBase {
	public static class SingleProject extends IndexCPPTemplateResolutionTest {
		public SingleProject() {setStrategy(new SinglePDOMTestStrategy(true));}
	}
	public static class ProjectWithDepProj extends IndexCPPTemplateResolutionTest {
		public ProjectWithDepProj() {setStrategy(new ReferencedProject(true));}
	}
	
	public static void addTests(TestSuite suite) {		
		suite.addTest(suite(SingleProject.class));
		suite.addTest(suite(ProjectWithDepProj.class));
	}

	public IndexCPPTemplateResolutionTest() {
		setStrategy(new ReferencedProject(true));
	}
	
	// // Bryan W.'s example from bugzilla#167098
	//    template<class K>
	//    class D { //CPPClassTemplate
	//    public:
	//            template<class T, class X>
	//            D(T t, X x) {} // CPPConstructorTemplate
	//
	//            template<class T, class X>
	//            void foo(T t, X x) {} // CPPMethodTemplate
	//    };

	//    void bar() {
	//            D<int> *var = new D<int>(5, 6);
	//            // First D<int>: CPPClassInstance
	//            // Second D<int>: CPPConstructorInstance
	//            // Now, getting the instance's specialized binding should
	//            // result in a CPPConstructorTemplateSpecialization
	//            var->foo<int,int>(7, 8);
	//            // foo -> CPPMethodTemplateSpecialization
	//            // foo<int,int> -> CPPMethodInstance
	//    }
    public void testCPPConstructorTemplateSpecialization() throws Exception {
    	IBinding b0= getBindingFromASTName("D<int>(", 1);
    	IBinding b1= getBindingFromASTName("D<int>(", 6);
    	
    	assertInstance(b0, ICPPClassTemplate.class); // *D*<int>(5, 6)
    	assertInstance(b0, ICPPClassType.class); // *D*<int>(5, 6)
    	assertInstance(b1, ICPPTemplateInstance.class); // *D<int>*(5, 6)
    	assertInstance(b1, ICPPConstructor.class); // *D<int>*(5, 6)
    	
    	IBinding tidSpc= ((ICPPTemplateInstance)b1).getSpecializedBinding();
    	assertInstance(tidSpc, ICPPConstructor.class);
    	assertInstance(tidSpc, ICPPSpecialization.class);
    	assertInstance(tidSpc, ICPPFunctionTemplate.class);
    }
	
	// class B {};
    //
	//	template<typename T>
	//	class A {
	//	public:
	//		T (*f)(int x);
	//	};
	//
	//	template<typename T> T foo(int x) {return *new T();}
	//	template<typename T> T foo(int x, int y) {return *new T();}

	//	void qux() {
	//		A<B> a;
	//		a.f= foo<B>;
	//	}
	public void _testOverloadedFunctionTemplate() {
		IBinding b0= getBindingFromASTName("foo<B>;", 6);
		assertInstance(b0, ICPPFunction.class);
		assertInstance(b0, ICPPSpecialization.class);
	}
	
	//	template<typename T, template<typename U> class S>
	//	class Foo {
	//	public:
	//		S<T> s;
	//	};
	//
	//	template<typename Z> class X {
	//	public:
	//		void foo(Z z) {}
	//	};
	//
	//	class A {};
	
	//	void qux() {
	//		Foo<A,X> f;
	//		f.s.foo(*new A());
	//	}
	public void _testTemplateTemplateParameter() throws Exception {
		IBinding b0= getBindingFromASTName("Foo<A,X>", 3);
		IBinding b1= getBindingFromASTName("Foo<A,X>", 8);
		IBinding b2= getBindingFromASTName("f.s.foo", 1);
		IBinding b3= getBindingFromASTName("s.foo", 1);
		IBinding b4= getBindingFromASTName("foo(*", 3);
		
		assertInstance(b0, ICPPClassTemplate.class);
		assertInstance(b0, ICPPClassType.class);
		ICPPTemplateParameter[] ps= ((ICPPClassTemplate)b0).getTemplateParameters();
		assertEquals(2, ps.length);
		assertInstance(ps[0], ICPPTemplateTypeParameter.class);
		assertInstance(ps[1], ICPPTemplateTemplateParameter.class);
		
		assertInstance(b1, ICPPTemplateInstance.class);
		assertInstance(b1, ICPPClassType.class);
		
		IType[] type= ((ICPPTemplateInstance)b1).getArguments();
		assertInstance(type[0], ICPPClassType.class);
		assertInstance(type[1], ICPPClassTemplate.class);
		assertInstance(type[1], ICPPClassType.class);
		
		ObjectMap om= ((ICPPTemplateInstance)b1).getArgumentMap();
		assertEquals(2, om.size());
		assertInstance(om.keyAt(0), ICPPTemplateTypeParameter.class);
		assertInstance(om.getAt(0), ICPPClassType.class);
		assertInstance(om.keyAt(1), ICPPTemplateTemplateParameter.class);
		assertInstance(om.getAt(1), ICPPClassType.class);
		assertInstance(om.getAt(1), ICPPClassTemplate.class);
		
		IBinding b1_spcd= ((ICPPTemplateInstance)b1).getSpecializedBinding();
		assertInstance(b1_spcd, ICPPClassTemplate.class);
		assertInstance(b1_spcd, ICPPClassType.class);
		assertTrue(((IType)b1_spcd).isSameType((IType)b0));
	}
	
	
	// template<typename T1, typename T2>
	// class Foo {
	// public:
	//  T1* foo (T2 t) {
	//   return 0;
	//  }
	// };
	//
	// class A {};
	// class B {};
	//
	// class X : public Foo<A,B> {};

	// class Y : public Foo<B,A> {};
	//
	// class AA {};
	// class BB {};
	// 
	// class Z : public Foo<AA,BB> {};
	//
	// X x;
	// Y y;
	// Z z;
	public void testInstanceInheritance() throws Exception {
		IBinding[] bs= {
				getBindingFromASTName("X x;", 1),
				getBindingFromASTName("Y y;", 1),
				getBindingFromASTName("Z z;", 1)
		};
 
		for(int i=0; i<bs.length; i++) {
			IBinding b= bs[i];
 			assertInstance(b, ICPPClassType.class);
 			ICPPClassType C= (ICPPClassType) b;
 			assertEquals(1, C.getBases().length);
 			ICPPClassType xb= (ICPPClassType) C.getBases()[0].getBaseClass();
 			assertInstance(xb, ICPPTemplateInstance.class);
 			ObjectMap args= ((ICPPTemplateInstance) xb).getArgumentMap();
 			assertInstance(args.keyAt(0), ICPPTemplateTypeParameter.class);
 			assertInstance(args.keyAt(1), ICPPTemplateTypeParameter.class);
 			assertInstance(args.getAt(0), ICPPClassType.class);
 			assertInstance(args.getAt(1), ICPPClassType.class);
 		}
 	}
 	
	// class A {}; class B {}; class C {};
	// template<typename T1, typename T2>
	// class D {};
	// 
	// template<typename T3>
	// class D<A, T3> {};
	
	// template<typename T3> class D<A, T3>; // harmless declaration for test purposes
	// template<typename T3>
	// class D<B, T3> {};
	// template<typename T3>
	// class D<C, T3> {};
	public void _testClassPartialSpecializations() throws Exception {
		IBinding b0= getBindingFromASTName("D<A, T3>", 8);
		IBinding b1= getBindingFromASTName("D<B, T3>", 8);
		IBinding b2= getBindingFromASTName("D<C, T3>", 8);
 		IBinding b3= getBindingFromASTName("D<B", 1);
 		
 		List spBindings= new ArrayList();
 		assertInstance(b0, ICPPSpecialization.class);
 		assertInstance(b0, ICPPClassTemplate.class);
 		spBindings.add(((ICPPSpecialization)b0).getSpecializedBinding());
 		
 		assertInstance(b1, ICPPSpecialization.class);
 		assertInstance(b1, ICPPClassTemplate.class);
 		spBindings.add(((ICPPSpecialization)b1).getSpecializedBinding());
 		
 		assertInstance(b2, ICPPSpecialization.class);
 		assertInstance(b2, ICPPClassTemplate.class);
 		spBindings.add(((ICPPSpecialization)b2).getSpecializedBinding());
 		
 		for(int i=0; i<spBindings.size(); i++) {
 			for(int j=0; j<spBindings.size(); j++) {
 	 			IType ty1= (IType) spBindings.get(i);
 	 			IType ty2= (IType) spBindings.get(j);
 	 			assertTrue(ty1.isSameType(ty2));
 	 		}
 		}
 		
 		assertInstance(b3, ICPPClassTemplate.class);
 		ICPPClassTemplate ct= (ICPPClassTemplate) b3;
 		assertEquals(3, ct.getPartialSpecializations().length);
	}
	
	// template<typename T1>
	// class A {};
	// 
	// template<typename T2>
	// class B : public A<T2> {
	//    public:
	//       static void foo() {}
	// };
	//
	// B<int> bb; // make sure the instance is in the pdom
	
	// template<typename T3>
	// class X : public B<T3> {};
	//
	// void qux() {
	//    B<int>::foo();
	//    B<long>::foo(); // instance not in the referenced pdom
	//    X<int> x;
	// }
	public void testClassImplicitInstantiations_188274() throws Exception {
		IBinding b2= getBindingFromASTName("X<int>", 6);
		assertInstance(b2, ICPPClassType.class);
		assertInstance(b2, ICPPTemplateInstance.class);
		ICPPClassType ct2= (ICPPClassType) b2;
		ICPPBase[] bss2= ct2.getBases();
		assertEquals(1, bss2.length);
		assertInstance(bss2[0].getBaseClass(), ICPPClassType.class);
		ICPPClassType ct2b= (ICPPClassType) bss2[0].getBaseClass();
		assertInstance(ct2b, ICPPTemplateInstance.class);
		
		IBinding b0= getBindingFromASTName("B<int>", 6);
		assertInstance(b0, ICPPClassType.class);
		ICPPClassType ct= (ICPPClassType) b0;
		ICPPBase[] bss= ct.getBases();
		assertEquals(1, bss.length);
		assertInstance(bss[0].getBaseClass(), ICPPClassType.class);	
		
		IBinding b1= getBindingFromASTName("B<long>", 7);
		assertInstance(b1, ICPPClassType.class);
		ICPPClassType ct1= (ICPPClassType) b1;
		ICPPBase[] bss1= ct1.getBases();
		assertEquals(1, bss1.length);
		assertInstance(bss1[0].getBaseClass(), ICPPClassType.class);
	}
	
	//	class B {};
	//
	//	template<typename T>
	//	class A {
	//      T t;
	//      int x;
	//		T foo(T t) { return t; }
	//      void bar(T t, int& x) {}
	//	};
	//
	//	template<>
	//	class A<B> {
	//      B t;
	//      int x;
	//		B foo(B t) { B x= *new B(); return x; }
 	//      void bar(B t, int& x) { x++; }
 	//	};
 	
 	// A<B> ab;
 	public void testClassSpecializationMethods() throws Exception {
 		IBinding b0= getBindingFromASTName("A<B> ab", 4);
 		assertInstance(b0, ICPPClassType.class);
 		assertInstance(b0, ICPPSpecialization.class);
 		assertFalse(b0 instanceof ICPPTemplateInstance);
 		
 		ICPPClassType ct= (ICPPClassType) b0;
 		ICPPMethod[] dms= ct.getDeclaredMethods();
 		assertEquals(2, dms.length);
 		
 		ICPPMethod foo= dms[0].getName().equals("foo") ? dms[0] : dms[1];
 		ICPPMethod bar= dms[0].getName().equals("bar") ? dms[0] : dms[1];
 		
 		assertEquals(foo.getName(), "foo");
 		assertEquals(bar.getName(), "bar");
 		
 		assertInstance(foo.getType().getReturnType(), ICPPClassType.class);
 		assertEquals(((ICPPClassType)foo.getType().getReturnType()).getName(), "B");
 		assertEquals(foo.getType().getParameterTypes().length, 1);
 		assertInstance(foo.getType().getParameterTypes()[0], ICPPClassType.class);
 		assertEquals(((ICPPClassType)foo.getType().getParameterTypes()[0]).getName(), "B");
 		
 		assertInstance(bar.getType().getReturnType(), ICPPBasicType.class);
 		assertEquals(((ICPPBasicType)bar.getType().getReturnType()).getType(), IBasicType.t_void);
 		
 		ICPPField[] fs= ct.getDeclaredFields();
 		assertEquals(2, fs.length);
 	}
 	
	// template<typename T> class A {
	//    public:
    //       typedef T TD;
	// };
	// 
	// class B {};
	// A<B>::TD foo;
	
	// class C {};
	// A<C>::TD bar;
	// 
	// void qux() {
	//   A<B>::TD foo2= foo;
	//   A<C>::TD bar2= bar;
	// }
	public void testTypedefSpecialization() {
		IBinding b0= getBindingFromASTName("TD foo2", 2);
		IBinding b1= getBindingFromASTName("TD bar2", 2);
		assertInstance(b0, ITypedef.class);
		assertInstance(b1, ITypedef.class);
		assertInstance(b0, ICPPSpecialization.class);
		assertInstance(b1, ICPPSpecialization.class);
		ObjectMap om0= ((ICPPSpecialization)b0).getArgumentMap();
		ObjectMap om1= ((ICPPSpecialization)b1).getArgumentMap();
		assertEquals(1, om0.size());
		assertEquals(1, om1.size());
		assertInstance(om0.keyAt(0), ICPPTemplateTypeParameter.class);
		assertInstance(om0.getAt(0), ICPPClassType.class);
		assertInstance(om1.keyAt(0), ICPPTemplateTypeParameter.class);
		assertInstance(om1.getAt(0), ICPPClassType.class);
		assertEquals("B", ((ICPPClassType)om0.getAt(0)).getName());
		assertEquals("C", ((ICPPClassType)om1.getAt(0)).getName());
	}
	
	//	template<typename X>
	//	void foo(X x) {}
	//
	//	template<typename A, typename B>
	//	void foo(A a, B b) {}
	//
	//	class C1 {}; class C2 {}; class C3 {};

	//	void bar() {
	//		foo<C1>(*new C1());
	//		foo<C2>(*new C2());
	//		foo<C3>(*new C3());
	//      foo<C1,C2>(*new C1(), *new C2());
	//      foo<C2,C3>(*new C2(), *new C3());
	//      foo<C3,C1>(*new C3(), *new C1());
	//      foo<C2,C1>(*new C2(), *new C1());
	//      foo<C3,C2>(*new C3(), *new C2());
	//      foo<C1,C3>(*new C1(), *new C3());
	//	}
	public void testFunctionTemplateSpecializations() throws Exception {
		IBinding b0= getBindingFromASTName("foo<C1>(", 3);
		IBinding b1= getBindingFromASTName("foo<C2>(", 3);
		IBinding b2= getBindingFromASTName("foo<C3>(", 3);
		IBinding b3= getBindingFromASTName("foo<C1,C2>(", 3);
		IBinding b4= getBindingFromASTName("foo<C2,C3>(", 3);
		IBinding b5= getBindingFromASTName("foo<C3,C1>(", 3);
		IBinding b6= getBindingFromASTName("foo<C2,C1>(", 3);
		IBinding b7= getBindingFromASTName("foo<C3,C2>(", 3);
		IBinding b8= getBindingFromASTName("foo<C1,C3>(", 3);
	}
	
	//	class A {}; class B{}; class C {};
	//
	//	template<typename T1, typename T2>
	//	void foo(T1 t1, T2 t2) {} // (0)	
	//
	//	template<>
	//	void foo(C c, A a) {} // (1)
	
	//	void bar() {
	//		A a;
	//		B b;
	//		C c;
	//		foo(a,b); // function instance of function template (0) 
	//		foo(c,a); // function specialization (1)
	//	}
	public void testFunctionInstanceSpecializationsParameters() throws Exception {
		IBinding b0= getBindingFromASTName("foo(a,b)", 3);
		assertInstance(b0, ICPPFunction.class);
		assertInstance(b0, ICPPTemplateInstance.class);
		ICPPFunctionType b0type= (ICPPFunctionType) ((ICPPFunction)b0).getType();
		assertInstance(b0type.getReturnType(), ICPPBasicType.class);
		IType[] b0_ptypes= b0type.getParameterTypes();
		assertEquals(2, b0_ptypes.length);
		assertInstance(b0_ptypes[0], ICPPClassType.class);
		assertInstance(b0_ptypes[1], ICPPClassType.class);
		assertEquals("A", ((ICPPClassType)b0_ptypes[0]).getName());
		assertEquals("B", ((ICPPClassType)b0_ptypes[1]).getName());
		
		IParameter[] b0_pms= ((ICPPFunction)b0).getParameters();
		assertEquals(2, b0_pms.length);
		assertInstance(b0_pms[0].getType(), ICPPClassType.class);
		assertInstance(b0_pms[1].getType(), ICPPClassType.class);
		assertEquals("A", ((ICPPClassType)b0_pms[0].getType()).getName());
		assertEquals("B", ((ICPPClassType)b0_pms[1].getType()).getName());
		
		IBinding b0_spcd= ((ICPPTemplateInstance)b0).getSpecializedBinding();
		assertInstance(b0_spcd, ICPPFunction.class);
		assertInstance(b0_spcd, ICPPTemplateDefinition.class);
		
		IParameter[] b0_spcd_pms= ((ICPPFunction)b0_spcd).getParameters();
		assertEquals(2, b0_spcd_pms.length);
		assertInstance(b0_spcd_pms[0].getType(), ICPPTemplateTypeParameter.class);
		assertInstance(b0_spcd_pms[1].getType(), ICPPTemplateTypeParameter.class);
		assertEquals("T1", ((ICPPTemplateTypeParameter)b0_spcd_pms[0].getType()).getName());
		assertEquals("T2", ((ICPPTemplateTypeParameter)b0_spcd_pms[1].getType()).getName());
		
		ObjectMap b0_am= ((ICPPSpecialization)b0).getArgumentMap();
		assertEquals(2, b0_am.size());
		assertInstance(b0_am.getAt(0), ICPPClassType.class);
		assertInstance(b0_am.getAt(1), ICPPClassType.class);
		assertInstance(b0_am.keyAt(0), ICPPTemplateTypeParameter.class);
		assertInstance(b0_am.keyAt(1), ICPPTemplateTypeParameter.class);
		assertEquals("T1", ((ICPPTemplateTypeParameter)b0_am.keyAt(0)).getName());
		assertEquals("T2", ((ICPPTemplateTypeParameter)b0_am.keyAt(1)).getName());
		assertEquals("A", ((ICPPClassType)b0_am.getAt(0)).getName());
		assertEquals("B", ((ICPPClassType)b0_am.getAt(1)).getName());
		
		ICPPFunctionType b0_spcd_type= (ICPPFunctionType) ((ICPPFunction)b0_spcd).getType();
		assertInstance(b0_spcd_type.getReturnType(), ICPPBasicType.class);
		IType[] b0_spcd_ptypes= b0_spcd_type.getParameterTypes();
		assertEquals(2, b0_spcd_ptypes.length);
		assertInstance(b0_spcd_ptypes[0], ICPPTemplateTypeParameter.class);
		assertInstance(b0_spcd_ptypes[1], ICPPTemplateTypeParameter.class);
		assertEquals("T1", ((ICPPTemplateTypeParameter)b0_spcd_ptypes[0]).getName());
		assertEquals("T2", ((ICPPTemplateTypeParameter)b0_spcd_ptypes[1]).getName());
		
		IBinding b1= getBindingFromASTName("foo(c,a)", 3);
		assertInstance(b1, ICPPFunction.class);
		ICPPFunctionType b1type= (ICPPFunctionType) ((ICPPFunction)b1).getType();
		assertInstance(b1type.getReturnType(), ICPPBasicType.class);
		IType[] b1_ptypes= b1type.getParameterTypes();
		assertEquals(2, b1_ptypes.length);
		assertInstance(b1_ptypes[0], ICPPClassType.class);
		assertInstance(b1_ptypes[1], ICPPClassType.class);
		assertEquals("C", ((ICPPClassType)b1_ptypes[0]).getName());
		assertEquals("A", ((ICPPClassType)b1_ptypes[1]).getName());
		
		IParameter[] b1_pms= ((ICPPFunction)b1).getParameters();
		assertEquals(2, b1_pms.length);
		assertInstance(b1_pms[0].getType(), ICPPClassType.class);
		assertInstance(b1_pms[1].getType(), ICPPClassType.class);
		assertEquals("C", ((ICPPClassType)b1_pms[0].getType()).getName());
		assertEquals("A", ((ICPPClassType)b1_pms[1].getType()).getName());
		
		assertInstance(b1, ICPPSpecialization.class);
		ICPPSpecialization b1s= (ICPPSpecialization) b1;
		IBinding b1_spcd= b1s.getSpecializedBinding();
		assertInstance(b1_spcd, ICPPFunction.class);
		assertInstance(b1_spcd, ICPPTemplateDefinition.class);
		
		ICPPFunctionType b1_spcd_type= (ICPPFunctionType) ((ICPPFunction)b1_spcd).getType();
		assertInstance(b1_spcd_type.getReturnType(), ICPPBasicType.class);
		IType[] b1_spcd_ptypes= b1_spcd_type.getParameterTypes();
		assertEquals(2, b1_spcd_ptypes.length);
		assertInstance(b1_spcd_ptypes[0], ICPPTemplateTypeParameter.class);
		assertInstance(b1_spcd_ptypes[1], ICPPTemplateTypeParameter.class);
		assertEquals("T1", ((ICPPTemplateTypeParameter)b1_spcd_ptypes[0]).getName());
		assertEquals("T2", ((ICPPTemplateTypeParameter)b1_spcd_ptypes[1]).getName());
		
		IParameter[] b1_spcd_pms= ((ICPPFunction)b1_spcd).getParameters();
		assertEquals(2, b1_spcd_pms.length);
		assertInstance(b1_spcd_pms[0].getType(), ICPPTemplateTypeParameter.class);
		assertInstance(b1_spcd_pms[1].getType(), ICPPTemplateTypeParameter.class);
		assertEquals("T1", ((ICPPTemplateTypeParameter)b1_spcd_pms[0].getType()).getName());
		assertEquals("T2", ((ICPPTemplateTypeParameter)b1_spcd_pms[1].getType()).getName());
		
		ObjectMap b1_am= b1s.getArgumentMap();
		assertEquals(2, b1_am.size());
		assertInstance(b1_am.keyAt(0), ICPPTemplateTypeParameter.class);
		assertInstance(b1_am.keyAt(1), ICPPTemplateTypeParameter.class);
		assertInstance(b1_am.getAt(0), ICPPClassType.class);
		assertInstance(b1_am.getAt(1), ICPPClassType.class);
		assertEquals("T1", ((ICPPTemplateTypeParameter)b1_am.keyAt(0)).getName());
		assertEquals("T2", ((ICPPTemplateTypeParameter)b1_am.keyAt(1)).getName());
		assertEquals("C", ((ICPPClassType)b1_am.getAt(0)).getName());
		assertEquals("A", ((ICPPClassType)b1_am.getAt(1)).getName());
	}
	
	//	class A {};
	//
	//	template<typename T>
	//	void foo(T t) {}
	
	//  void bar() {
	//		A a;
	//		foo(a);
	//  }
	public void testFunctionInstanceParameters() throws Exception {
		IBinding b0= getBindingFromASTName("foo(a)", 3);
		assertInstance(b0, ICPPTemplateInstance.class);
		assertInstance(b0, ICPPFunction.class);
		
		ICPPFunction f= (ICPPFunction) b0;
		ICPPFunctionType type= (ICPPFunctionType) f.getType();
		IType rt= type.getReturnType();
		IType[] pts= type.getParameterTypes();
		
		IParameter[] ps= f.getParameters();
		assertEquals(1, ps.length);
		ICPPParameter param= (ICPPParameter) ps[0];
		assertInstance(param, ICPPSpecialization.class);
		
		IType paramType= param.getType();
		assertInstance(paramType, ICPPClassType.class);
		ICPPParameter paramSpec= (ICPPParameter) ((ICPPSpecialization) param).getSpecializedBinding();
		assertInstance(paramSpec.getType(), ICPPTemplateTypeParameter.class);
		ICPPTemplateTypeParameter ttp= (ICPPTemplateTypeParameter) paramSpec.getType();
		assertEquals("T", ttp.getName());
		assertNull(ttp.getDefault());
		
		ICPPTemplateInstance inst= (ICPPTemplateInstance) b0;
		IBinding sp= inst.getSpecializedBinding();
		assertInstance(sp, ICPPFunction.class);
		assertInstance(sp, ICPPTemplateDefinition.class);
	}
	
	// //header file
	//
	// template<typename T>
	// class Foo {};
	//
	// class B {};
	//
	// template<>	
	// class Foo<B> {};
	
	// Foo<B> b1; 
	//
	// class A {};
	//
	// template<>
	// class Foo<A> {};
	//
	// Foo<B> b2;
	public void _testClassSpecializations_180738() {
		IBinding b1a = getBindingFromASTName("Foo<B> b1;", 3);
		IBinding b1b = getBindingFromASTName("Foo<B> b1;", 6);
		
		assertInstance(b1a, ICPPClassType.class);
		assertInstance(b1a, ICPPClassTemplate.class);
		
		assertInstance(b1b, ICPPClassType.class);
		assertInstance(b1b, ICPPSpecialization.class);
		ICPPSpecialization b1spc= (ICPPSpecialization) b1b;
		ObjectMap b1om= b1spc.getArgumentMap();
		assertEquals(1, b1om.keyArray().length);
		assertInstance(b1om.getAt(0), ICPPClassType.class);
		ICPPClassType b1pct= (ICPPClassType) b1om.getAt(0);
		assertEquals("B", b1pct.getName());
		
		IBinding b2a = getBindingFromASTName("Foo<B> b2;", 3);
		IBinding b2b = getBindingFromASTName("Foo<B> b2;", 6);
		
		assertInstance(b2a, ICPPClassType.class);
		assertInstance(b2a, ICPPClassTemplate.class);
		
		assertInstance(b2b, ICPPClassType.class);
		assertInstance(b2b, ICPPSpecialization.class);
		ICPPSpecialization b2spc= (ICPPSpecialization) b2b;
		ObjectMap b2om= b2spc.getArgumentMap();
		assertEquals(1, b2om.keyArray().length);
		assertInstance(b2om.getAt(0), ICPPClassType.class);
		ICPPClassType b2pct= (ICPPClassType) b2om.getAt(0);
		assertEquals("B", b2pct.getName());
	}
	
	// // header file
	//	template <class T>
	//	T left(T a, T b) {
	//	   	return a;
	//	}
	//  void sanity() {}
	//  int d;
	
	//  void foo() { sanity(); }
	//	class Int {};
	//	Int a,b;
	//	Int c= left(a,b);
	//  Int c= left(a,d);
	public void testSimpleFunctionTemplate() {
		IBinding b0 = getBindingFromASTName("sanity();", 6);
		IBinding b1 = getBindingFromASTName("a,b;", 1);
		IBinding b2 = getBindingFromASTName("a,b)", 1);
		IBinding b3 = getBindingFromASTName("b)", 1);
		IBinding b4 = getBindingFromASTName("d)", 1);
		IBinding b5 = getBindingFromASTName("left(a,b)", 4);
		IBinding b6 = getBindingFromASTName("left(a,b)", 4);
	}
	
	// class A {};
	// template<typename T1, typename T2> class D {};
	// template<typename X1> class D<X1,X1> {};
	
	// D<A,A> daa;
	public void testClassPartialSpecializations_199572() throws Exception {
		IBinding b0= getBindingFromASTName("D<A,A>", 6);
		assertInstance(b0, ICPPTemplateInstance.class);
		assertInstance(b0, ICPPClassType.class);
		IBinding b1= getBindingFromASTName("D<A,A>", 1);
		assertInstance(b1, ICPPTemplateDefinition.class);
		assertInstance(b1, ICPPClassType.class);
	}
}
