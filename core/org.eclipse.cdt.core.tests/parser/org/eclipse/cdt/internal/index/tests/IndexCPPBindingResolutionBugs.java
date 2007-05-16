/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * For testing PDOM binding resolution
 */
public class IndexCPPBindingResolutionBugs extends IndexBindingResolutionTestBase {

	public static class SingleProject extends IndexCPPBindingResolutionBugs {
		public SingleProject() {setStrategy(new SinglePDOMTestStrategy(true));}
	}
	public static class ProjectWithDepProj extends IndexCPPBindingResolutionBugs {
		public ProjectWithDepProj() {setStrategy(new ReferencedProject(true));}
	}
	
	public static void addTests(TestSuite suite) {		
		suite.addTest(suite(SingleProject.class));
		suite.addTest(suite(ProjectWithDepProj.class));
	}
	
	public static TestSuite suite() {
		return suite(IndexCPPBindingResolutionBugs.class);
	}
	
	public IndexCPPBindingResolutionBugs() {
		setStrategy(new SinglePDOMTestStrategy(true));
	}
	
	//	 template<typename T>
	//	 class C : public C<T> {};
	
	// 	 void foo() {
	//      C<int>::unresolvable();
	//   };
	public void test185828() throws Exception {
		// Bug 185828 reports a StackOverflowException is thrown before we get here.
		// That the SOE is thrown is detected in BaseTestCase via an Error IStatus
		
		IBinding b0= getBindingFromASTName("C", 1);
		IBinding b1= getBindingFromASTName("C<int>", 6, true);
		IBinding b2= getProblemFromASTName("unresolvable", 12);
		
		assertInstance(b0, ICPPClassType.class);
		assertInstance(b0, ICPPClassTemplate.class);
		
		assertInstance(b1, ICPPClassType.class);
		assertInstance(b1, ICPPSpecialization.class);
	}
	
	//	class MyClass {
	//	public:
	//		template<class T>
	//		T* MopGetObject(T*& aPtr) 
	//			{ return 0; }
	//			
	//		
	//		template<class T>	
	//		T*  MopGetObjectNoChaining(T*& aPtr)
	//		{ return 0; }
	//
	//	};
	
	//	int main() {
	//		MyClass* cls;
	//	}
	public void test184216() throws Exception {
		IBinding b0= getBindingFromASTName("MyClass", 7);
		assertInstance(b0, ICPPClassType.class);
		ICPPClassType ct= (ICPPClassType) b0;
		ICPPMethod[] ms= ct.getDeclaredMethods(); // 184216 reports CCE thrown
		assertEquals(2, ms.length);
		assertInstance(ms[0], ICPPTemplateDefinition.class);
		assertInstance(ms[1], ICPPTemplateDefinition.class);
	}
	
	// // header file
	//  class cl;
	//	typedef cl* t1;
	//  typedef t1 t2;
	
	//// referencing content
	//  void func(t2 a);
	//  void func(int b);
	//  void ref() {
	//     cl* a;
	//     func(a);
	//  }
	public void testBug166954() {
		IBinding b0 = getBindingFromASTName("func(a)", 4);
	}
	
	// // header
	//	class Base { 
	//  public: 
	//     void foo(int i);
	//     int  fooint();
	//     char* fooovr();
	//     char* fooovr(int a);
	//     char* fooovr(char x);
	//  };

	// // references
	// #include "header.h"
	// void Base::foo(int i) {}
	// int Base::fooint() {return 0;}
	// char* Base::fooovr() {return 0;}
	// char* Base::fooovr(int a) {return 0;}
	// char* Base::fooovr(char x) {return 0;}
	//
	// void refs() {
	//   Base b;
	//   b.foo(1);
	//   b.fooint();
	//   b.fooovr();
	//   b.fooovr(1);
	//   b.fooovr('a');
	// }
	public void test168020() {
		getBindingFromASTName("foo(int i)", 3);
		getBindingFromASTName("fooint()", 6);
		getBindingFromASTName("fooovr()", 6);
		getBindingFromASTName("fooovr(int", 6);
		getBindingFromASTName("fooovr(char", 6);

		getBindingFromASTName("foo(1)", 3);
		getBindingFromASTName("fooint();", 6);
		getBindingFromASTName("fooovr();", 6);
		getBindingFromASTName("fooovr(1", 6);
		getBindingFromASTName("fooovr('", 6);
	}

	
	// // header
	//	class Base { 
	//  public: 
	//     void foo(int i);
	//     int  foo2(int i);
	//  };
	//
	//  void func(int k);
	//  void func2(int i);

	// // references
	// #include "header.h"
	// void Base::foo(int i) {
	//   i=2;
	// }
	// void Base::foo2(int j) {
	//   j=2;
	// }
	// void func(int k) {
	//  k=2;
	// }
	// void func2(int l) {
	//  l=2;
	// }
	public void test168054() {
		getBindingFromASTName("i=2", 1);
		getBindingFromASTName("j=2", 1);
		getBindingFromASTName("k=2", 1);
		getBindingFromASTName("l=2", 1);
	}
	
	// namespace X {}
	
	// namespace Y {
	//    class Ambiguity {};
	//    enum Ambiguity {A1,A2,A3};
	//    void foo() {
	//       Ambiguity problem;
	//    }
	// }
	public void testBug176708_CCE() throws Exception {
		IBinding binding= getBindingFromASTName("Y {", 1);
		assertTrue(binding instanceof ICPPNamespace);
		ICPPNamespace adapted= (ICPPNamespace) strategy.getIndex().adaptBinding(binding);
		IASTName[] names= findNames("Ambiguity problem", 9);
		assertEquals(1, names.length);
		IBinding binding2= adapted.getNamespaceScope().getBinding(names[0], true);
	}
	
	// namespace X {int i;}
	
	// // references
	// #include "header.h"
	// int a= X::i;
	public void testBug176708_NPE() throws Exception {
		IBinding binding= getBindingFromASTName("i;", 1);
		assertTrue(binding instanceof ICPPVariable);
		IScope scope= binding.getScope();
	}
	
	//	template<class T, class U, class V>
	//	class A {};
	
	//	template<>
	//	class A<int, bool, double> {};
	public void testBug180784() throws Exception {
		IBinding b0= getBindingFromASTName("A<int, bool, double> {};", 20);
		assertInstance(b0, ICPPSpecialization.class);
		ICPPSpecialization s= (ICPPSpecialization) b0;
		ObjectMap map= s.getArgumentMap();
		IBinding b1= s.getSpecializedBinding();
		assertInstance(b1, ICPPClassTemplate.class);
		ICPPClassTemplate t= (ICPPClassTemplate) b1;
		ICPPTemplateParameter[] ps = t.getTemplateParameters();
		assertNotNull(ps);
		assertEquals(3, ps.length);
		assertNotNull(map.get(ps[0]));
		assertNotNull(map.get(ps[1]));
		assertNotNull(map.get(ps[2]));
	}
	
	//	class A{};
	//
	//	template<typename T>
	//	T id (T t) {return t;}
	//
	//	template<>
	//	A id (A a) {return a;}
	//
	//	int id(int x) {return x;}
	
	//	void foo() {
	//		id(*new A());
	//		id(6);
	//	}
	public void testBug180948() throws Exception {
		// Main check occurs in BaseTestCase - that no ClassCastException
		// is thrown during indexing
		IBinding b0= getBindingFromASTName("id(*", 2);
		IBinding b1= getBindingFromASTName("id(6", 2);
	}
	
	
	// void func1(void);
	
	//  #include "header.h"
	//
	//	int main(void)
	//	{
	//      void* v= func1;
	//	}
	public void testBug181735() throws DOMException {
		IBinding b0 = getBindingFromASTName("func1;", 5);
		assertTrue(b0 instanceof IFunction);
	}
	
	//	class B {
	//  public:
	//		class BB {
	//		public:
	//			int field;
	//		};
	//	};
	//
	//	class A : public B::BB {};
	
	//  #include "header.h"
	//	
	//  void foo() {
	//		A c;
	//		c.field;//comment
	//	}
	public void testBug183843() throws DOMException {
		IBinding b0 = getBindingFromASTName("field;//", 5);
		assertTrue(b0 instanceof ICPPField);
	}
}
