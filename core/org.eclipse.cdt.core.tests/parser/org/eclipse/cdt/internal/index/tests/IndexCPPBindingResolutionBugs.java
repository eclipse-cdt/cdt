/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
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

import java.util.Arrays;
import java.util.regex.Pattern;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.core.runtime.CoreException;

/**
 * For testing PDOM binding resolution
 */
public class IndexCPPBindingResolutionBugs extends IndexBindingResolutionTestBase {

	public static class SingleProject extends IndexCPPBindingResolutionBugs {
		public SingleProject() {setStrategy(new SinglePDOMTestStrategy(true));}
		public static TestSuite suite() {return suite(SingleProject.class);}
	}

	public static class ProjectWithDepProj extends IndexCPPBindingResolutionBugs {
		public ProjectWithDepProj() {setStrategy(new ReferencedProject(true));}
		public static TestSuite suite() {return suite(ProjectWithDepProj.class);}
	}
	
	public static void addTests(TestSuite suite) {		
		suite.addTest(IndexCPPBindingResolutionBugsSingleProjectFirstAST.suite());
		suite.addTest(SingleProject.suite());
		suite.addTest(ProjectWithDepProj.suite());
	}
	
	public static TestSuite suite() {
		return suite(IndexCPPBindingResolutionBugs.class);
	}
	
	public IndexCPPBindingResolutionBugs() {
		setStrategy(new SinglePDOMTestStrategy(true));
	}
	
	// #define OBJ void foo()
	// #define FUNC() void bar()
	// #define FUNC2(A) void baz()
	

	// #include "header.h"
	//
	// OBJ {}
	// FUNC() {}
	// FUNC2(1) {}
	public void testBug208558() throws CoreException {
		IIndex index= getIndex();
		
		IIndexMacro[] macrosA= index.findMacros("OBJ".toCharArray(), IndexFilter.ALL, npm());
		IIndexMacro[] macrosB= index.findMacros("FUNC".toCharArray(), IndexFilter.ALL, npm());
		IIndexMacro[] macrosC= index.findMacros("FUNC2".toCharArray(), IndexFilter.ALL, npm());
		
		assertEquals(1, macrosA.length);
		assertEquals(1, macrosB.length);
		assertEquals(1, macrosC.length);
		IIndexMacro obj= macrosA[0];
		IIndexMacro func= macrosB[0];
		IIndexMacro func2= macrosC[0];
		
		assertEquals("OBJ", new String(obj.getName()));
		assertEquals("FUNC", new String(func.getName()));
		assertEquals("FUNC2", new String(func2.getName()));
		
		assertEquals("void foo()", new String(obj.getExpansionImage()));
		assertEquals("void bar()", new String(func.getExpansionImage()));
		assertEquals("void baz()", new String(func2.getExpansionImage()));
		
		assertEquals("OBJ", new String(obj.getName()));
		assertNull(obj.getParameterList());
		
		assertEquals("FUNC", new String(func.getName()));
		assertEquals(0, func.getParameterList().length);

		assertEquals("FUNC2", new String(func2.getName()));
		assertEquals(1, func2.getParameterList().length);
		assertEquals("A", new String(func2.getParameterList()[0]));
		
		IIndexBinding[] bindings= index.findBindings(Pattern.compile(".*"), false, IndexFilter.ALL, npm());
		assertEquals(3, bindings.length);
		
		IIndexBinding foo= index.findBindings("foo".toCharArray(), IndexFilter.ALL, npm())[0];
		IIndexBinding bar= index.findBindings("bar".toCharArray(), IndexFilter.ALL, npm())[0];
		IIndexBinding baz= index.findBindings("baz".toCharArray(), IndexFilter.ALL, npm())[0];
		
		assertEquals("foo", foo.getName());
		assertEquals("bar", bar.getName());
		assertEquals("baz", baz.getName());
		assertInstance(foo, ICPPFunction.class);
		assertInstance(bar, ICPPFunction.class);
		assertInstance(baz, ICPPFunction.class);
	}
	
	//	template <class T>
	//	inline void testTemplate(T& aRef);
	//
	//	class Temp {
	//	};

	//	void main(void) {
	//	    Temp testFile;
	//	    testTemplate(testFile);
	//	}
	public void testBug207320() {
		IBinding b0= getBindingFromASTName("testTemplate(", 12);
		assertInstance(b0, ICPPFunction.class);
		assertInstance(b0, ICPPTemplateInstance.class);
	}
	
	//	class testdef{
	//
	//	public:
	//		void testagain();
	//	};
	//
	//	typedef void TAny;
	//
	//	inline void testCall(TAny* aExpected){}
	//
	//  testdef*  global_cBase;
	//  testdef*& global_cBaseRef = global_cBase;
	
	//	#include "typedefHeader.h"
	//
	//
	//	int main(void)
	//		{
	//			testdef*  local_cBase;
	//          testdef*& local_cBaseRef = local_cBase;
	//		
	//			testCall( /*1*/ (void *) local_cBase);
	//          testCall( /*2*/ local_cBase);
	//
	//			testCall( /*3*/ (void *) local_cBaseRef);
	//          testCall( /*4*/ local_cBaseRef);
	//
	//			testCall( /*5*/ (void *) global_cBase);
	//          testCall( /*6*/ global_cBase);
	//
	//			testCall( /*7*/ (void *)global_cBaseRef);
	//          testCall( /*8*/ global_cBaseRef);
	//		}
	public void testBug206187() throws Exception {
		IBinding b1= getBindingFromASTName("testCall( /*1*/", 8);
		IBinding b2= getBindingFromASTName("testCall( /*2*/", 8);
		IBinding b3= getBindingFromASTName("testCall( /*3*/", 8);
		IBinding b4= getBindingFromASTName("testCall( /*4*/", 8);
		IBinding b5= getBindingFromASTName("testCall( /*5*/", 8);
		IBinding b6= getBindingFromASTName("testCall( /*6*/", 8);
		IBinding b7= getBindingFromASTName("testCall( /*7*/", 8);
		IBinding b8= getBindingFromASTName("testCall( /*8*/", 8);
	}
	
	
	// template<typename T1>
    // class A {};
    // 
    // template<typename T2>
    // class B : public A<T2> {};
    // 
    // class C {};
    //
    // B<C> b;
    
    // void foo() {C c; B<int> b;}
    public void testBug188274() throws Exception {
        IBinding b0= getBindingFromASTName("C", 1);
        IBinding b1= getBindingFromASTName("B", 1);
        assertInstance(b0, ICPPClassType.class);
        assertInstance(b1, ICPPClassType.class);
        assertInstance(b1, ICPPClassTemplate.class);
        assertInstance(b1, ICPPInstanceCache.class);
        
        ICPPInstanceCache ct= (ICPPInstanceCache) b1;
        ICPPSpecialization inst= ct.getInstance(new ICPPTemplateArgument[]{new CPPTemplateArgument((IType)b0)});
        assertInstance(inst, ICPPClassType.class);
        ICPPClassType c2t= (ICPPClassType) inst;
        ICPPBase[] bases= c2t.getBases();
        assertEquals(1, bases.length);
        assertInstance(bases[0].getBaseClass(), ICPPClassType.class);
    }
	
	// namespace ns {class A{};}
	
	// ns::A a;
	// class B {};
	public void testBug188324() throws Exception {
		IASTName name= findName("B", 1);
		IBinding b0= getBindingFromASTName("ns::A", 2);
		assertInstance(b0, ICPPNamespace.class);
		ICPPNamespace ns= (ICPPNamespace) b0;
		assertEquals(0, ns.getNamespaceScope().getBindings(name, false, false).length);
	}
	
	//	 template<typename T>
	//	 class C : public C<T> {};
	
	// 	 void foo() {
	//      C<int>::unresolvable();
	//   };
	public void testBug185828() throws Exception {
		// Bug 185828 reports a StackOverflowException is thrown before we get here.
		// That the SOE is thrown is detected in BaseTestCase via an Error IStatus
		
		IBinding b0= getBindingFromASTName("C<int>", 1);
		IBinding b1= getBindingFromASTName("C<int>", 6);
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
	//		MyClass* cls= new MyClass();
	//	}
	public void testBug184216() throws Exception {
		IBinding b0= getBindingFromASTName("MyClass*", 7);
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
	public void testBug168020() {
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
	// int Base::foo2(int j) {
	//   j=2;
	// }
	// void func(int k) {
	//  k=2;
	// }
	// void func2(int l) {
	//  l=2;
	// }
	public void testBug168054() {
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
		IASTName name= findName("Ambiguity problem", 9);
		assertNotNull(name);
		IBinding binding2= adapted.getNamespaceScope().getBinding(name, true);
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
	
    // typedef struct {
    //    int utm;
    // } usertype;
    // void func(usertype t);

	// #include "header.h"
    // void test() {
	//    usertype ut;
	//    func(ut);
    // }
    public void testFuncWithTypedefForAnonymousStruct_190730() throws Exception {
		IBinding b0 = getBindingFromASTName("func(", 4);
		assertTrue(b0 instanceof IFunction);
		IFunction f= (IFunction) b0;
		IParameter[] pars= f.getParameters();
		assertEquals(1, pars.length);
		IType type= pars[0].getType();
		assertTrue(type instanceof ITypedef);
		type= ((ITypedef) type).getType();
		assertTrue(type instanceof ICPPClassType);
    }

    // typedef enum {
    //    eItem
    // } userEnum;
    // void func(userEnum t);

	// #include "header.h"
    // void test() {
	//    userEnum ut;
	//    func(ut);
    // }
    public void testFuncWithTypedefForAnonymousEnum_190730() throws Exception {
		IBinding b0 = getBindingFromASTName("func(", 4);
		assertTrue(b0 instanceof IFunction);
		IFunction f= (IFunction) b0;
		IParameter[] pars= f.getParameters();
		assertEquals(1, pars.length);
		IType type= pars[0].getType();
		assertTrue(type instanceof ITypedef);
		type= ((ITypedef) type).getType();
		assertTrue(type instanceof IEnumeration);
    }
    
    // // no header needed
    
    // typedef class {
    //    int member;
    // } t_class;
    // typedef struct {
    //    int member;
    // } t_struct;
    // typedef union {
    //    int member;
    // } t_union;
    // typedef enum {
    //    ei
    // } t_enum;
	public void testIsSameAnonymousType_Bug193962() throws DOMException {
		// class
		IBinding tdAST = getBindingFromASTName("t_class;", 7);
		assertFalse(tdAST instanceof IIndexBinding);
		IBinding tdIndex= strategy.getIndex().adaptBinding(tdAST);
		assertTrue(tdIndex instanceof IIndexBinding);
		assertTrue(tdAST instanceof ITypedef);
		assertTrue(tdIndex instanceof ITypedef);

		IType tAST= ((ITypedef) tdAST).getType();
		IType tIndex= ((ITypedef) tdIndex).getType();
		assertTrue(tAST instanceof ICompositeType);
		assertTrue(tIndex instanceof ICompositeType);
		assertTrue(tAST.isSameType(tIndex));
		assertTrue(tIndex.isSameType(tAST));

		// struct
		tdAST = getBindingFromASTName("t_struct;", 8);
		assertFalse(tdAST instanceof IIndexBinding);
		tdIndex= strategy.getIndex().adaptBinding(tdAST);
		assertTrue(tdIndex instanceof IIndexBinding);
		assertTrue(tdAST instanceof ITypedef);
		assertTrue(tdIndex instanceof ITypedef);
		
		tAST= ((ITypedef) tdAST).getType();
		tIndex= ((ITypedef) tdIndex).getType();
		assertTrue(tAST instanceof ICompositeType);
		assertTrue(tIndex instanceof ICompositeType);
		assertTrue(tAST.isSameType(tIndex));
		assertTrue(tIndex.isSameType(tAST));

		// union
		tdAST = getBindingFromASTName("t_union;", 7);
		assertFalse(tdAST instanceof IIndexBinding);
		tdIndex= strategy.getIndex().adaptBinding(tdAST);
		assertTrue(tdIndex instanceof IIndexBinding);
		assertTrue(tdAST instanceof ITypedef);
		assertTrue(tdIndex instanceof ITypedef);
		
		tAST= ((ITypedef) tdAST).getType();
		tIndex= ((ITypedef) tdIndex).getType();
		assertTrue(tAST instanceof ICompositeType);
		assertTrue(tIndex instanceof ICompositeType);
		assertTrue(tAST.isSameType(tIndex));
		assertTrue(tIndex.isSameType(tAST));

		// enum
		tdAST = getBindingFromASTName("t_enum;", 6);
		assertFalse(tdAST instanceof IIndexBinding);
		tdIndex= strategy.getIndex().adaptBinding(tdAST);
		assertTrue(tdIndex instanceof IIndexBinding);
		assertTrue(tdAST instanceof ITypedef);
		assertTrue(tdIndex instanceof ITypedef);
		
		tAST= ((ITypedef) tdAST).getType();
		tIndex= ((ITypedef) tdIndex).getType();
		assertTrue(tAST instanceof IEnumeration);
		assertTrue(tIndex instanceof IEnumeration);
		assertTrue(tAST.isSameType(tIndex));
		assertTrue(tIndex.isSameType(tAST));
	}

    // // no header needed
    
	// namespace ns {
    // typedef class {
    //    int member;
    // } t_class;
    // typedef struct {
    //    int member;
    // } t_struct;
    // typedef union {
    //    int member;
    // } t_union;
    // typedef enum {
    //    ei
    // } t_enum;
	// };
	public void testIsSameNestedAnonymousType_Bug193962() throws DOMException {
		// class
		IBinding tdAST = getBindingFromASTName("t_class;", 7);
		assertFalse(tdAST instanceof IIndexBinding);
		IBinding tdIndex= strategy.getIndex().adaptBinding(tdAST);
		assertTrue(tdIndex instanceof IIndexBinding);
		assertTrue(tdAST instanceof ITypedef);
		assertTrue(tdIndex instanceof ITypedef);

		IType tAST= ((ITypedef) tdAST).getType();
		IType tIndex= ((ITypedef) tdIndex).getType();
		assertTrue(tAST instanceof ICompositeType);
		assertTrue(tIndex instanceof ICompositeType);
		assertTrue(tAST.isSameType(tIndex));
		assertTrue(tIndex.isSameType(tAST));

		// struct
		tdAST = getBindingFromASTName("t_struct;", 8);
		assertFalse(tdAST instanceof IIndexBinding);
		tdIndex= strategy.getIndex().adaptBinding(tdAST);
		assertTrue(tdIndex instanceof IIndexBinding);
		assertTrue(tdAST instanceof ITypedef);
		assertTrue(tdIndex instanceof ITypedef);
		
		tAST= ((ITypedef) tdAST).getType();
		tIndex= ((ITypedef) tdIndex).getType();
		assertTrue(tAST instanceof ICompositeType);
		assertTrue(tIndex instanceof ICompositeType);
		assertTrue(tAST.isSameType(tIndex));
		assertTrue(tIndex.isSameType(tAST));

		// union
		tdAST = getBindingFromASTName("t_union;", 7);
		assertFalse(tdAST instanceof IIndexBinding);
		tdIndex= strategy.getIndex().adaptBinding(tdAST);
		assertTrue(tdIndex instanceof IIndexBinding);
		assertTrue(tdAST instanceof ITypedef);
		assertTrue(tdIndex instanceof ITypedef);
		
		tAST= ((ITypedef) tdAST).getType();
		tIndex= ((ITypedef) tdIndex).getType();
		assertTrue(tAST instanceof ICompositeType);
		assertTrue(tIndex instanceof ICompositeType);
		assertTrue(tAST.isSameType(tIndex));
		assertTrue(tIndex.isSameType(tAST));

		// enum
		tdAST = getBindingFromASTName("t_enum;", 6);
		assertFalse(tdAST instanceof IIndexBinding);
		tdIndex= strategy.getIndex().adaptBinding(tdAST);
		assertTrue(tdIndex instanceof IIndexBinding);
		assertTrue(tdAST instanceof ITypedef);
		assertTrue(tdIndex instanceof ITypedef);
		
		tAST= ((ITypedef) tdAST).getType();
		tIndex= ((ITypedef) tdIndex).getType();
		assertTrue(tAST instanceof IEnumeration);
		assertTrue(tIndex instanceof IEnumeration);
		assertTrue(tAST.isSameType(tIndex));
		assertTrue(tIndex.isSameType(tAST));
	}
	
	//	namespace FOO {
	//		namespace BAR {
	//		    class Bar;
	//		}
	//		class Foo {
	//			BAR::Bar * Test(BAR::Bar * bar);
	//		};
	//	}

	//	#include "header.h"
	//	namespace FOO {
	//	    using BAR::Bar;
	//	 
	//	    Bar* Foo::Test(Bar* pBar) {
	//	       return pBar;
	//	    }
	//	}
	public void testAdvanceUsingDeclaration_Bug217102() throws Exception {
		IBinding cl = getBindingFromASTName("Bar* Foo", 3);

		assertEquals("Bar", cl.getName());
		assertTrue(cl instanceof ICPPClassType);
		assertEquals("BAR", cl.getScope().getScopeName().toString());

		cl = getBindingFromASTName("Bar* pBar", 3);
		assertEquals("Bar", cl.getName());
		assertTrue(cl instanceof ICPPClassType);
		assertEquals("BAR", cl.getScope().getScopeName().toString());
	}
	
	// struct outer {
	//    union {
	//       int var1;
	//    };
	// };
	  
	// #include "header.h"
	// void test() {
	//    struct outer x;
	//    x.var1=1;
	// }
	public void testAnonymousUnion_Bug216791() throws DOMException {
		// struct
		IBinding b = getBindingFromASTName("var1=", 4);
		assertTrue(b instanceof IField);
		IField f= (IField) b;
		IScope outer= f.getCompositeTypeOwner().getScope();
		assertTrue(outer instanceof ICPPClassScope);
		assertEquals("outer", outer.getScopeName().toString());
	}

	// union outer {
	//    struct {
	//       int var1;
	//    };
	//    struct {
	//       int var2;
	//    } hide;
	// };
	  
	// #include "header.h"
	// void test() {
	//    union outer x;
	//    x.var1=1;
	//    x.var2= 2; // must be a problem
	// }
	public void testAnonymousStruct_Bug216791() throws DOMException {
		// struct
		IBinding b = getBindingFromASTName("var1=", 4);
		assertTrue(b instanceof IField);
		IField f= (IField) b;
		IScope outer= f.getCompositeTypeOwner().getScope();
		assertTrue(outer instanceof ICPPClassScope);
		assertEquals("outer", outer.getScopeName().toString());
		
		getProblemFromASTName("var2=", 4);
	}
	
	// namespace ns {
	// int v;
	// };
	// using namespace ns;
	
	// #include "header.h"
	// void test() {
	//    v=1;
	// }
	public void testUsingDirective_Bug216527() throws Exception {
		IBinding b = getBindingFromASTName("v=", 1);
		assertTrue(b instanceof IVariable);
		IVariable v= (IVariable) b;
		IScope scope= v.getScope();
		assertTrue(scope instanceof ICPPNamespaceScope);
		assertEquals("ns", scope.getScopeName().toString());
	}	
	
	// namespace NSA {
	// int a;
	// }
	// namespace NSB {
	// int b;
	// }
	// namespace NSAB {
	//    using namespace NSA;
	//    using namespace NSB;
	// }

	// #include "header.h"
	// void f() {
	//   NSAB::a= NSAB::b;
	// }
	public void testNamespaceComposition_Bug200673() throws Exception {
		IBinding a = getBindingFromASTName("a=", 1);
		assertTrue(a instanceof IVariable);
		IVariable v= (IVariable) a;
		IScope scope= v.getScope();
		assertTrue(scope instanceof ICPPNamespaceScope);
		assertEquals("NSA", scope.getScopeName().toString());

		IBinding b = getBindingFromASTName("b;", 1);
		assertTrue(b instanceof IVariable);
		v= (IVariable) b;
		scope= v.getScope();
		assertTrue(scope instanceof ICPPNamespaceScope);
		assertEquals("NSB", scope.getScopeName().toString());
	}
	
	// namespace N { namespace M {}}

	// namespace N {using namespace N::M;}
	// using namespace N;
    // void test() {x;}
	public void testEndlessLoopWithUsingDeclaration_Bug209813() throws DOMException {
		getProblemFromASTName("x;", 1);
	}
	
	// class MyClass {};
	
	// void test(MyClass* ptr);
	// class MyClass;
	public void testClassRedeclarationAfterReference_Bug229571() throws Exception {
		IBinding cl= getBindingFromASTName("MyClass;", 7);
		IFunction fn= getBindingFromASTName("test(", 4, IFunction.class);
		IType type= fn.getType().getParameterTypes()[0];
		assertInstance(type, IPointerType.class);
		type= ((IPointerType) type).getType();
		assertSame(type, cl);
	}
	
    // class A {
    // public:
    //    void foo() const volatile;
    //    void foo() volatile;
    //    void foo() const;
    //    void foo();
    //    void bar() const volatile;
    //    void bar() volatile;
    //    void bar() const;
    //    void bar();
    // };
    
    // void A::foo() const volatile { bar();/*1*/ }
    // void A::foo() volatile       { bar();/*2*/ }
    // void A::foo() const          { bar();/*3*/ }
    // void A::foo()                { bar();/*4*/ }
    // void test() {
    //   A a;
    //   const A ca;
    //   volatile A va;
    //   const volatile A cva;
    //   cva.bar();/*5*/
    //   va.bar();/*6*/
    //   ca.bar();/*7*/
    //   a.bar();/*8*/
    // }
    public void testMemberFunctionDisambiguationByCVness_238409() throws Exception {
    	ICPPMethod bar_cv= getBindingFromASTName("bar();/*1*/", 3, ICPPMethod.class);
    	ICPPMethod bar_v=  getBindingFromASTName("bar();/*2*/", 3, ICPPMethod.class);
    	ICPPMethod bar_c=  getBindingFromASTName("bar();/*3*/", 3, ICPPMethod.class);
    	ICPPMethod bar=    getBindingFromASTName("bar();/*4*/", 3, ICPPMethod.class);
    	ICPPFunctionType bar_cv_ft= bar_cv.getType();
    	ICPPFunctionType bar_v_ft=  bar_v.getType();
    	ICPPFunctionType bar_c_ft=  bar_c.getType();
    	ICPPFunctionType bar_ft=    bar.getType();
    	
    	assertTrue(bar_cv_ft.isConst()); assertTrue(bar_cv_ft.isVolatile());
    	assertTrue(!bar_v_ft.isConst()); assertTrue(bar_v_ft.isVolatile());
    	assertTrue(bar_c_ft.isConst());  assertTrue(!bar_c_ft.isVolatile());
    	assertTrue(!bar_ft.isConst());   assertTrue(!bar_ft.isVolatile());
    	
    	bar_cv= getBindingFromASTName("bar();/*5*/", 3, ICPPMethod.class);
    	bar_v=  getBindingFromASTName("bar();/*6*/", 3, ICPPMethod.class);
    	bar_c=  getBindingFromASTName("bar();/*7*/", 3, ICPPMethod.class);
    	bar=    getBindingFromASTName("bar();/*8*/", 3, ICPPMethod.class);
    	bar_cv_ft= bar_cv.getType();
    	bar_v_ft=  bar_v.getType();
    	bar_c_ft=  bar_c.getType();
    	bar_ft=    bar.getType();
    	
    	assertTrue(bar_cv_ft.isConst()); assertTrue(bar_cv_ft.isVolatile());
    	assertTrue(!bar_v_ft.isConst()); assertTrue(bar_v_ft.isVolatile());
    	assertTrue(bar_c_ft.isConst());  assertTrue(!bar_c_ft.isVolatile());
    	assertTrue(!bar_ft.isConst());   assertTrue(!bar_ft.isVolatile());
    }
    
	//	typedef char t[12];
	//	void test1(char *);
	//	void test2(char []);
	//	void test3(t);
    
	//	void xx() {
	//	   char* x= 0;
	//	   test1(x);
	//	   test2(x); // problem binding here
	//	   test3(x); // problem binding here
	//	}
	public void testAdjustmentOfParameterTypes_Bug239975() throws Exception {
    	getBindingFromASTName("test1(x)", 5, ICPPFunction.class);
    	getBindingFromASTName("test2(x)", 5, ICPPFunction.class);
    	getBindingFromASTName("test3(x)", 5, ICPPFunction.class);
	}
	
	// class A {
	//    A();
	//    void l();
	//    void e;
	//    class M {};
	// };
	// class B {
	//    B();
	//    void m();
	//    void f;
	//    class N {};
	// };
	// class C : B {
	//    C();
	//    void n();
	//    int g;
	//    class O {};
	// };
	// template<typename T> class CT : B {
	//    CT();
	//    void n();
	//    T g;
	//    class O {};
	// };
	// template<> class CT<char> : A {
	//    CT(); CT(int);
	//    void o();
	//    int h;
	//    class P {};
	// };
	// template<typename S> class Container {
	//    class C : B {
	//       C();
	//       void n();
	//       int g;
	//       class O {};
	//    };
	//    template<typename T> class CT : B {
	//       CT();
	//       void n();
	//       T g;
	//       class O {};
	//    };
	// };
	//	template<> class Container<char>::C : A {
	//		C(); C(int);
	//		void o();
	//		int h;
	//		class P {};
	//	};
	//	template<> template<typename T> class Container<char>::CT : A {
	//		CT(); CT(int);
	//		void o();
	//		int h;
	//		class P {};
	//	};
	
	// C c;
	// CT<int> ct;
	// CT<char> ctinst;
	// Container<int>::C spec;
	// Container<int>::CT<int> spect;
	// Container<char>::C espec;
	// Container<char>::CT<int> espect;
	public void testClassTypes_Bug98171() throws Exception {
		// regular class
		ICPPClassType ct= getBindingFromASTName("C", 1);
		assertBindings(new String[] {"B"}, ct.getBases());
		assertBindings(new String[] {"n", "m", "B", "C"}, ct.getAllDeclaredMethods());
		assertBindings(new String[] {"C", "C"}, ct.getConstructors());
		assertBindings(new String[] {"g"}, ct.getDeclaredFields());
		assertBindings(new String[] {"n", "C"}, ct.getDeclaredMethods());
		assertBindings(new String[] {"f", "g"}, ct.getFields());
		assertBindings(new String[] {"m", "n", "C", "C", "~C", "B", "B", "~B", "operator =", "operator ="}, ct.getMethods());
		assertBindings(new String[] {"O"}, ct.getNestedClasses());

		// class template
		ct= getBindingFromASTName("CT<int>", 2);
		assertInstance(ct, ICPPClassTemplate.class);
		assertBindings(new String[] {"B"}, ct.getBases());
		assertBindings(new String[] {"n", "m", "B", "CT"}, ct.getAllDeclaredMethods());
		assertBindings(new String[] {"CT", "CT"}, ct.getConstructors());
		assertBindings(new String[] {"g"}, ct.getDeclaredFields());
		assertBindings(new String[] {"n", "CT"}, ct.getDeclaredMethods());
		assertBindings(new String[] {"f", "g"}, ct.getFields());
		assertBindings(new String[] {"m", "n", "CT", "CT", "~CT", "B", "B", "~B", "operator =", "operator ="}, ct.getMethods());
		assertBindings(new String[] {"O"}, ct.getNestedClasses());

		// class template instance
		ct= getBindingFromASTName("CT<int>", 7);
		assertInstance(ct, ICPPTemplateInstance.class);
		assertBindings(new String[] {"B"}, ct.getBases());
		assertBindings(new String[] {"n", "m", "B", "CT"}, ct.getAllDeclaredMethods());
		assertBindings(new String[] {"CT", "CT"}, ct.getConstructors());
		assertBindings(new String[] {"g"}, ct.getDeclaredFields());
		assertBindings(new String[] {"n", "CT"}, ct.getDeclaredMethods());
		assertBindings(new String[] {"f", "g"}, ct.getFields());
		assertBindings(new String[] {"m", "n", "CT", "CT", "~CT", "B", "B", "~B", "operator =", "operator ="}, ct.getMethods());
		assertBindings(new String[] {"O"}, ct.getNestedClasses());

		// explicit class template instance
		ct= getBindingFromASTName("CT<char>", 8);
		assertInstance(ct, ICPPTemplateInstance.class);
		assertBindings(new String[] {"A"}, ct.getBases());
		assertBindings(new String[] {"o", "l", "A", "CT", "CT"}, ct.getAllDeclaredMethods());
		assertBindings(new String[] {"CT", "CT", "CT"}, ct.getConstructors());
		assertBindings(new String[] {"h"}, ct.getDeclaredFields());
		assertBindings(new String[] {"o", "CT", "CT"}, ct.getDeclaredMethods());
		assertBindings(new String[] {"e", "h"}, ct.getFields());
		assertBindings(new String[] {"l", "o", "CT", "CT", "CT", "~CT", "A", "A", "~A", "operator =", "operator ="}, ct.getMethods());
		assertBindings(new String[] {"P"}, ct.getNestedClasses());

		// class specialization
		ct= getBindingFromASTName("C spec", 1);
		assertInstance(ct, ICPPClassSpecialization.class);
		assertBindings(new String[] {"B"}, ct.getBases());
		assertBindings(new String[] {"n", "m", "B", "C"}, ct.getAllDeclaredMethods());
		assertBindings(new String[] {"C", "C"}, ct.getConstructors());
		assertBindings(new String[] {"g"}, ct.getDeclaredFields());
		assertBindings(new String[] {"n", "C"}, ct.getDeclaredMethods());
		assertBindings(new String[] {"f", "g"}, ct.getFields());
		assertBindings(new String[] {"m", "n", "C", "C", "~C", "B", "B", "~B", "operator =", "operator ="}, ct.getMethods());
		assertBindings(new String[] {"O"}, ct.getNestedClasses());

		// class template specialization
		ct= getBindingFromASTName("CT<int> spect", 2);
		assertInstance(ct, ICPPClassTemplate.class, ICPPClassSpecialization.class);
		assertBindings(new String[] {"B"}, ct.getBases());
		assertBindings(new String[] {"n", "m", "B", "CT"}, ct.getAllDeclaredMethods());
		assertBindings(new String[] {"CT", "CT"}, ct.getConstructors());
		assertBindings(new String[] {"g"}, ct.getDeclaredFields());
		assertBindings(new String[] {"n", "CT"}, ct.getDeclaredMethods());
		assertBindings(new String[] {"f", "g"}, ct.getFields());
		assertBindings(new String[] {"m", "n", "CT", "CT", "~CT", "B", "B", "~B", "operator =", "operator ="}, ct.getMethods());
		assertBindings(new String[] {"O"}, ct.getNestedClasses());
		
		// explicit class specialization
		ct= getBindingFromASTName("C espec", 1);
		assertInstance(ct, ICPPClassSpecialization.class);
		assertBindings(new String[] {"A"}, ct.getBases());
		assertBindings(new String[] {"o", "l", "A", "C", "C"}, ct.getAllDeclaredMethods());
		assertBindings(new String[] {"C", "C", "C"}, ct.getConstructors());
		assertBindings(new String[] {"h"}, ct.getDeclaredFields());
		assertBindings(new String[] {"o", "C", "C"}, ct.getDeclaredMethods());
		assertBindings(new String[] {"e", "h"}, ct.getFields());
		assertBindings(new String[] {"l", "o", "C", "C", "C", "~C", "A", "A", "~A", "operator =", "operator ="}, ct.getMethods());
		assertBindings(new String[] {"P"}, ct.getNestedClasses());

		// explicit class template specialization
		ct= getBindingFromASTName("CT<int> espect", 7);
		assertInstance(ct, ICPPTemplateInstance.class);
		assertBindings(new String[] {"A"}, ct.getBases());
		assertBindings(new String[] {"o", "l", "A", "CT", "CT"}, ct.getAllDeclaredMethods());
		assertBindings(new String[] {"CT", "CT", "CT"}, ct.getConstructors());
		assertBindings(new String[] {"h"}, ct.getDeclaredFields());
		assertBindings(new String[] {"o", "CT", "CT"}, ct.getDeclaredMethods());
		assertBindings(new String[] {"e", "h"}, ct.getFields());
		assertBindings(new String[] {"l", "o", "CT", "CT", "CT", "~CT", "A", "A", "~A", "operator =", "operator ="}, ct.getMethods());
		assertBindings(new String[] {"P"}, ct.getNestedClasses());
	}

	//	void func(const int* x) {}
	
	//	void func(int* p) {
	//      const int* q = p;
	//		func(q);
	//	}
	public void testOverloadedFunctionFromIndex_Bug256240() throws Exception {
    	getBindingFromASTName("func(q", 4, ICPPFunction.class);
	}

	//	class A {
	//	  class B;
	//	};
	//	class A::B {
	//	  void m();
	//	};

	//	void A::B::m() {}
	public void testNestedClasses_Bug259683() throws Exception {
    	getBindingFromASTName("A::B::m", 7, ICPPMethod.class);
	}

	//	namespace ns {
	//		struct S {
	//			int a;
	//		};
	//	}
	//	class A {
	//		public:
	//			template<typename T> operator T*(){return 0;};
	//	};

	//	namespace ns {
	//		void bla() {
	//			A a;
	//			a.operator S *();
	//		}
	//	}
	public void testLookupScopeForConversionNames_267221() throws Exception {
    	getBindingFromASTName("operator S *", 12, ICPPMethod.class);
	}

	private void assertBindings(String[] expected, ICPPBase[] bases) throws DOMException {
		IBinding[] bindings= new IBinding[bases.length];
		for (int i = 0; i < bindings.length; i++) {
			bindings[i]= bases[i].getBaseClass();
		}
		assertBindings(expected, bindings);
	}

	private void assertBindings(String[] expected, IBinding[] binding) {
		String[] actual= new String[binding.length];
		for (int i = 0; i < actual.length; i++) {
			actual[i]= binding[i].getName();
		}		
		Arrays.sort(actual);
		Arrays.sort(expected);
		assertEquals(toString(expected), toString(actual));
	}
	
	private String toString(String[] actual) {
		StringBuilder buf= new StringBuilder();
		buf.append('{');
		boolean isFirst= true;
		for (String val : actual) {
			if (!isFirst) {
				buf.append(',');
			}
			buf.append(val);
			isFirst= false;
		}
		buf.append('}');
		return buf.toString();
	}
	
	//	class Derived;
	//  class X {
	//     Derived* d;
	//  };
	//  class Base {};
	//  void useBase(Base* b);
	
	//  class Derived: Base {};
	//	void test() {
	//      X x;
	//      useBase(x.d);
	//	}
	public void testLateDefinitionOfInheritance_Bug292749() throws Exception {
    	getBindingFromASTName("useBase(x.d", 7, ICPPFunction.class);
	}
	
	// namespace one {
	//   void fx();
	//   void fx(int);
	//   void fx(int, int);
	// }
	// namespace two {
	//   using one::fx;
    // }
	
	// #include "header.h"
	// void test() {
	//    two::fx();
	//    two::fx(1);
	//    two::fx(1,1);
	// }
	public void testUsingDeclaration_Bug300019() throws Exception {
    	getBindingFromASTName("fx();", 2, ICPPFunction.class);
    	getBindingFromASTName("fx(1);", 2, ICPPFunction.class);
    	getBindingFromASTName("fx(1,1);", 2, ICPPFunction.class);
	}
	
	//	struct YetAnotherTest {
	//	  void test();
	//	  friend class InnerClass3;
	//	  class InnerClass3 {
	//	    void f() {
	//	      member=0;
	//	    }
	//	    int member;
	//	  };
	//	  InnerClass3 arr[32];
	//	};

	//	#include "a.h"
	//	void YetAnotherTest::test() {
	//	  arr[0].member=0;
	//	}
	public void testElaboratedTypeSpecifier_Bug303739() throws Exception {
    	getBindingFromASTName("member=0", -2, ICPPField.class);
	}
	
	//	typedef int xxx::* MBR_PTR;
	
	//	void test() {	
	//		MBR_PTR x;
	//	}
	public void testProblemInIndexBinding_Bug317146() throws Exception {
    	ITypedef td= getBindingFromASTName("MBR_PTR", 0, ITypedef.class);
    	ICPPPointerToMemberType ptrMbr= (ICPPPointerToMemberType) td.getType();
    	IType t= ptrMbr.getMemberOfClass();
    	assertInstance(t, IProblemBinding.class);
	}

	//	void f255(
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int);
	//	void f256(
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
	//     int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int);
	
	//	void test() {	
	//     f255(
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
	//     f256(
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	//          0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
	//	}
	public void testFunctionsWithManyParameters_Bug319186() throws Exception {
		getBindingFromASTName("f255", 0);
		getBindingFromASTName("f256", 0);
	}		
}
