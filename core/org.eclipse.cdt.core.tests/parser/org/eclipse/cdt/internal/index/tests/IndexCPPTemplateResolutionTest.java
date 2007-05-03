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

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
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
	public void _testClassSpecializationInHeader() {
		IBinding b1a = getBindingFromASTName("Foo<B> b1;", 3);
		IBinding b1b = getBindingFromASTNameWithRawSignature("Foo<B> b1;", "Foo<B>");
		
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
		IBinding b2b = getBindingFromASTNameWithRawSignature("Foo<B> b2;", "Foo<B>");
		
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
	
	//  void foo() { sanity(); }
	//	class Int {};
	//	Int a,b;
	//	Int c= left(a,b);
	public void testSimpleFunctionTemplate() {
		IBinding b0 = getBindingFromASTName("sanity();", 6);
		IBinding b1 = getBindingFromASTName("a,b;", 1);
		IBinding b2 = getBindingFromASTName("left(a,b)", 4);
	}
}
