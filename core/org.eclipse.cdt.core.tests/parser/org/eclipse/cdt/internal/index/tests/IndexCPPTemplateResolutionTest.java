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
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.core.runtime.CoreException;


/**
 * Tests for exercising resolution of template bindings against IIndex
 */
public class IndexCPPTemplateResolutionTest extends IndexBindingResolutionTestBase {
	public static class SingleProject extends IndexCPPTemplateResolutionTest {
		public SingleProject() {setStrategy(new SinglePDOMTestStrategy(true));}
		public static TestSuite suite() {return suite(SingleProject.class);}
	}
	
	public static class ProjectWithDepProj extends IndexCPPTemplateResolutionTest {
		public ProjectWithDepProj() {setStrategy(new ReferencedProject(true));}
		public static TestSuite suite() {return suite(ProjectWithDepProj.class);}
		
		// template <typename T= int> class XT;
		
	    // #include "header.h"
		// template <typename T> class XT {};
		// void test() {
		//    XT<> x;
		// };		
		@Override
		public void testDefaultTemplateArgInHeader_264988() throws Exception {
			// Not supported across projects (the composite index does not merge
			// default values of template parameters).
		}
	}
	
	public static void addTests(TestSuite suite) {		
		suite.addTest(SingleProject.suite());
		suite.addTest(ProjectWithDepProj.suite());
	}

	public IndexCPPTemplateResolutionTest() {
		setStrategy(new ReferencedProject(true));
	}
	
    // template<typename _TpAllocator>
    // struct Allocator {
    //   typedef _TpAllocator& alloc_reference;
    //   template<typename _TpRebind>
    //   struct rebind {
    //     typedef Allocator<_TpRebind> other;
    //   };
    // };
    //
    // template<typename _Tp, typename _Alloc = Allocator<_Tp> >
    // struct Vec {
    //   typedef typename _Alloc::template rebind<_Tp>::other::alloc_reference reference;
    // };

	// void f(Vec<int>::reference r) {}
    public void testRebindPattern_214017_1() throws Exception {
        IBinding b0= getBindingFromASTName("r)", 1);
        assertInstance(b0, ICPPVariable.class);
		IType type = ((ICPPVariable) b0).getType();
		type = SemanticUtil.getUltimateType(type, false);
		assertInstance(type, IBasicType.class);
		assertEquals("int", ASTTypeUtil.getType(type));
    }

    // template<typename _TpAllocator>
    // struct Allocator {
    //   typedef _TpAllocator& alloc_reference;
    //   template<typename _TpRebind>
    //   struct rebind {
    //     typedef Allocator<_TpRebind> other;
    //   };
    // };
    //
    // template<typename _TpBase, typename _AllocBase>
    // struct VecBase {
    //   typedef typename _AllocBase::template rebind<_TpBase>::other _Tp_alloc_type;
    // };
    //
    // template<typename _Tp, typename _Alloc = Allocator<_Tp> >
    // struct Vec : public VecBase<_Tp, _Alloc> {
    //   typedef typename VecBase<_Tp, _Alloc>::_Tp_alloc_type::alloc_reference reference;
    // };

    // void f(Vec<int>::reference r) {}
    public void testRebindPattern_214017_2() throws Exception {
        IBinding b0= getBindingFromASTName("r)", 1);
        assertInstance(b0, ICPPVariable.class);
		IType type = ((ICPPVariable) b0).getType();
		type = SemanticUtil.getUltimateType(type, false);
		assertInstance(type, IBasicType.class);
		assertEquals("int", ASTTypeUtil.getType(type));
    }
	
    // template<typename _TpAllocatorForward>
    // class Allocator;
    //
	// template<>
	// struct Allocator<void> {
	//   template<typename _TpRebind>
	//   struct rebind {
	//	   typedef Allocator<_TpRebind> other;
	//   };
	// };
    //
    // template<typename _TpAllocator>
    // struct Allocator {
    //   typedef _TpAllocator& alloc_reference;
    //   template<typename _TpRebind>
    //   struct rebind {
    //     typedef Allocator<_TpRebind> other;
    //   };
    // };
    //
    // template<typename _TpBase, typename _AllocBase>
    // struct VecBase {
    //   typedef typename _AllocBase::template rebind<_TpBase>::other _Tp_alloc_type;
    // };
    //
    // template<typename _Tp, typename _Alloc = Allocator<_Tp> >
    // struct Vec : public VecBase<_Tp, _Alloc> {
    //   typedef typename VecBase<_Tp, _Alloc>::_Tp_alloc_type::alloc_reference reference;
    // };

    // void f(Vec<int>::reference r) {}
    public void testRebindPattern_214017_3() throws Exception {
        IBinding b0= getBindingFromASTName("r)", 1);
        assertInstance(b0, ICPPVariable.class);
		IType type = ((ICPPVariable) b0).getType();
		type = SemanticUtil.getUltimateType(type, false);
		assertInstance(type, IBasicType.class);
		assertEquals("int", ASTTypeUtil.getType(type));
    }

    // template<typename _TpAllocator>
    // struct Allocator {
    //   typedef _TpAllocator& alloc_reference;
    //   template<typename _TpRebind>
    //   struct rebind {
    //     typedef Allocator<_TpRebind> other;
    //   };
    // };
    //
    // template<typename _TpBase, typename _AllocBase>
    // struct VecBase {
    //   typedef typename _AllocBase::template rebind<_TpBase*>::other unreferenced;
    //   typedef typename _AllocBase::template rebind<_TpBase>::other _Tp_alloc_type;
    // };
    //
    // template<typename _Tp, typename _Alloc = Allocator<_Tp> >
    // struct Vec : public VecBase<_Tp, _Alloc> {
    //   typedef typename VecBase<_Tp, _Alloc>::_Tp_alloc_type::alloc_reference reference;
    // };

	// void f(int s);
	//
	// void test(Vec<int>::reference p) {
	//   f(p);
	// }
    public void testRebindPattern_276610() throws Exception {
        getBindingFromASTName("f(p)", 1, ICPPFunction.class);
    }

    //	class Str1 {
	//	public:
	//	   Str1(const char* s) {
	//	      s_ = s;
	//     }
	//
	//	   const char* s_;
	//	};
	//
	//	template<typename T>
	//	class StrT {
	//	public:
	//	   StrT(const T* s) {
	//	      s_ = s;
	//     }
	//
	//     const T* s_;
	//	};
	//
	//  template<typename T>
	//	class C1 {
	//	public:
	//	  void m1(const Str1& s) {}
	//	  void m2(const StrT<T> s) {}
	//	};

	//  void main() {
	//     C1<char> c1;
	//	   c1.m1("aaa");  // OK
	//	   c1.m2("aaa");  // problem
	//  }
	public void testUnindexedConstructorInstanceImplicitReferenceToDeferred() throws Exception {
		IBinding b0= getBindingFromASTName("C1<char> c1", 8);
		IBinding b1= getBindingFromASTName("m1(\"aaa\")", 2);
    	IBinding b2= getBindingFromASTName("m2(\"aaa\")", 2);
    	
    	assertEquals(1, getIndex().findNames(b1, IIndex.FIND_REFERENCES).length);
    	assertEquals(1, getIndex().findNames(b2, IIndex.FIND_REFERENCES).length);
	}
	
	
	// template<typename T>
	// class X {
	//    public: static void foo() {}
	// };
	
	// class A{};
	// void bar() {
	//   X<A>::foo();
	// }
	public void testUnindexedMethodInstance() {
		IBinding b0= getBindingFromASTName("foo()", 3);
		assertInstance(b0, ICPPMethod.class);
	}
	
	// template<typename T>
	// class StrT {
	//   public: void assign(const T* s) {}
	// };
	
	// void main() {
	//   StrT<char> x;
	//   x.assign("aaa");
	// }
	public void testUnindexedMethodInstance2() throws Exception {
		IBinding b0= getBindingFromASTName("assign(\"aaa\")", 6);
		assertInstance(b0, ICPPMethod.class);
    	assertEquals(1, getIndex().findNames(b0, IIndex.FIND_REFERENCES).length);
		IParameter[] parameters = ((ICPPMethod) b0).getParameters();
		System.out.println(String.valueOf(parameters));
		IFunctionType type = ((ICPPMethod) b0).getType();
		System.out.println(String.valueOf(type));
	}

	// template<typename T>
	// class X {};
	
	// class A{};
	// void bar() {
	//   X<A> xa= new X<A>();
	// }
	public void testUnindexedConstructorInstance() {
		IBinding b0= getBindingFromASTName("X<A>()", 4);
		assertInstance(b0, ICPPConstructor.class);
	}

	//	template<typename T>
	//	class StrT {
	//	public:
	//	   StrT(const T* s) {
	//	      s_ = s;
	//     }
	//
	//     const T* s_;
	//	};
	//
	//  template<typename T>
	//	class C1 {
	//	public:
	//	  void m2(T t) {}
	//	};
	
	//  class A {};
	//  void foo() {
	//     C1< StrT<A> > c1a;
	//     c1a.m2(*new StrT<A>(new A()));
	//  }
	public void testUnindexedConstructorInstanceImplicitReference3() throws Exception {
		IBinding b0= getBindingFromASTName("C1< StrT<A> >", 2);
		IBinding b1= getBindingFromASTName("StrT<A> > c1a", 7);
		IBinding b2= getBindingFromASTName("StrT<A>(", 7);
		IBinding b3= getBindingFromASTName("c1a;", 3);
		IBinding b4= getBindingFromASTName("m2(*", 2);
	}
	
	//	class Str1 {
	//	public:
	//	   Str1(const char* s) {
	//	      s_ = s;
	//     }
	//
	//	   const char* s_;
	//	};
	//
	//	template<typename T>
	//	class StrT {
	//	public:
	//	   StrT(const T* s) {
	//	      s_ = s;
	//     }
	//
	//     const T* s_;
	//	};
	//
	//	typedef StrT<char> Str2;
	//
	//	class C1 {
	//	public:
	//	  void m1(const Str1& s) {}
	//	  void m2(const Str2& s) {}
	//	  void m3();
	//	};

	//  void C1::m3() {
	//	   m1("aaa");  // OK
	//	   m2("aaa");  // problem
	//  }
	public void testUnindexedConstructorInstanceImplicitReference() throws Exception {
		IBinding b0= getBindingFromASTName("m1(\"aaa\")", 2);
    	IBinding b1= getBindingFromASTName("m2(\"aaa\")", 2);
    	
    	assertEquals(1, getIndex().findNames(b0, IIndex.FIND_REFERENCES).length);
    	assertEquals(1, getIndex().findNames(b1, IIndex.FIND_REFERENCES).length);
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
    	IBinding b0= getBindingFromASTName("D<int> *var", 1);
    	IBinding b1= getBindingFromASTName("D<int> *var", 6);
    	
    	assertInstance(b0, ICPPClassTemplate.class); 
    	assertInstance(b0, ICPPClassType.class); 
    	assertInstance(b1, ICPPTemplateInstance.class);
    	assertInstance(b1, ICPPClassType.class); 
    	
    	// ICPPClassType _ct= (ICPPClassType) b1;
    	// ICPPConstructor[] _ctcs= _ct.getConstructors();
    	// assertEquals(3, _ctcs.length); // two implicit plus the constructor template
    	
    	IBinding b2= getBindingFromASTName("D<int>(", 1);
    	IBinding b3= getBindingFromASTName("D<int>(", 6);
    	
    	assertInstance(b2, ICPPClassTemplate.class); // *D*<int>(5, 6)
    	assertInstance(b2, ICPPClassType.class); // *D*<int>(5, 6)
    	assertInstance(b3, ICPPTemplateInstance.class); // *D<int>*(5, 6)
    	assertInstance(b3, ICPPConstructor.class); // *D<int>*(5, 6)
    	
    	// 
    	// ICPPClassType ct= (ICPPClassType) b2;
    	// ICPPConstructor[] ctcs= ct.getConstructors();
    	// assertEquals(3, ctcs.length); // two implicit plus the constructor template
    	
    	IBinding tidSpc= ((ICPPTemplateInstance)b3).getSpecializedBinding();
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
	public void testTemplateTemplateParameter() throws Exception {
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
 
		for (IBinding b : bs) {
			assertInstance(b, ICPPClassType.class);
 			ICPPClassType c= (ICPPClassType) b;
 			assertEquals(1, c.getBases().length);
 			ICPPClassType xb= (ICPPClassType) c.getBases()[0].getBaseClass();
 			assertInstance(xb, ICPPTemplateInstance.class);
 			ICPPTemplateParameter[] templateParameters =
 				((ICPPTemplateInstance) xb).getTemplateDefinition().getTemplateParameters();
 			assertInstance(templateParameters[0], ICPPTemplateTypeParameter.class);
 			assertInstance(templateParameters[1], ICPPTemplateTypeParameter.class);
 			ICPPTemplateParameterMap args= ((ICPPTemplateInstance) xb).getTemplateParameterMap();
 			assertInstance(args.getArgument(0).getTypeValue(), ICPPClassType.class);
 			assertInstance(args.getArgument(1).getTypeValue(), ICPPClassType.class);
 		}
 	}

	//  namespace ns {
	//	template<typename T1>
	//	struct A {
	//	  static int a;
	//	};
	//	}
	//
	//	template<typename T2>
	//	struct B : public ns::A<T2> {};

	//	void test() {
	//	  B<int>::a;
	//	}
	public void testInstanceInheritance_258745() throws Exception {
		getBindingFromASTName("a", 1, ICPPField.class);
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
	public void testClassPartialSpecializations() throws Exception {
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
 		
 		ICPPClassType ct= (ICPPClassType) b0;
 		ICPPMethod[] dms= ct.getDeclaredMethods();
 		assertEquals(2, dms.length);

 		// if the specialization was used, we have 2 fields.
 		ICPPField[] fs= ct.getDeclaredFields();
 		assertEquals(2, fs.length);

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

	// template<typename T>
	// class C {
	// public:
	//   typedef T value_type;
	//   void m(value_type v) {}
	// };

	// void main() {
	//   C<int> x;
	//   x.m(1);
	// }
	public void testTypedefSpecialization_213861() throws Exception {
		IBinding b0= getBindingFromASTName("m(1)", 1);
		assertInstance(b0, ICPPMethod.class);
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
		ICPPFunctionType b0type= ((ICPPFunction)b0).getType();
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
		
		ICPPFunctionType b0_spcd_type= ((ICPPFunction)b0_spcd).getType();
		assertInstance(b0_spcd_type.getReturnType(), ICPPBasicType.class);
		IType[] b0_spcd_ptypes= b0_spcd_type.getParameterTypes();
		assertEquals(2, b0_spcd_ptypes.length);
		assertInstance(b0_spcd_ptypes[0], ICPPTemplateTypeParameter.class);
		assertInstance(b0_spcd_ptypes[1], ICPPTemplateTypeParameter.class);
		assertEquals("T1", ((ICPPTemplateTypeParameter)b0_spcd_ptypes[0]).getName());
		assertEquals("T2", ((ICPPTemplateTypeParameter)b0_spcd_ptypes[1]).getName());
		
		IBinding b1= getBindingFromASTName("foo(c,a)", 3);
		assertInstance(b1, ICPPFunction.class);
		ICPPFunctionType b1type= ((ICPPFunction)b1).getType();
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
		
		ICPPFunctionType b1_spcd_type= ((ICPPFunction)b1_spcd).getType();
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
		ICPPFunctionType type= f.getType();
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
	
	//	template <class T1, class T2, class R>
	//	void func(T1* obj, R (T2::*member)()) {
	//	}
	//
	//	struct A {
	//	  void m();
	//	};
	
	//	void test() {
	//	  A a;
	//	  func(&a, &A::m);
	//	}
	public void testFunctionTemplate_245030() throws Exception {
		ICPPFunction f= getBindingFromASTName("func(&a, &A::m)", 4, ICPPFunction.class);
		assertInstance(f, ICPPTemplateInstance.class);
	}

	//	template <class U>
	//	void func(const U& u, const typename U::t& v) {
	//	}
	//
	//	template <class U>
	//	void func(U& u, const typename U::t& v) {
	//	}
	
	//	template <typename T> class A {
	//	  typedef T t;
	//	};
	//
	//	void test() {
	//	  const A<int>& a;
	//	  int b;
	//	  func(a, b);
	//	}
	public void _testFunctionTemplate_319498() throws Exception {
		ICPPFunction f= getBindingFromASTName("func(a, b)", 4, ICPPFunction.class);
		assertInstance(f, ICPPTemplateInstance.class);
	}

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
	public void testClassSpecializations_180738() {
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

    // template<typename _TpAllocator>
    // class Allocator {
    // public:
    //   typedef _TpAllocator& alloc_reference;
    // };
	//
    // template<typename _TpRebind>
    // struct rebind {
    //   typedef Allocator<_TpRebind> other;
    // };
	//
    // template<typename _Tp, typename _Alloc = Allocator<_Tp> >
    // class Vec {
    // public:
    //   typedef typename rebind<_Tp>::other::alloc_reference reference;
    // };

	// void f(Vec<int>::reference r) {}
    public void testTemplateTypedef_214447() throws Exception {
        IBinding b0= getBindingFromASTName("r)", 1);
        assertInstance(b0, ICPPVariable.class);
		IType type = ((ICPPVariable) b0).getType();
		type = SemanticUtil.getUltimateType(type, false);
		assertInstance(type, IBasicType.class);
		assertEquals("int", ASTTypeUtil.getType(type));
    }

	//	class A {}; class B {}; class X {};	
	//	template<typename T>
	//	class C {
	//	public:
	//		T t;
	//		operator B() {B b; return b;}
	//	};
	//	template<typename T>
	//	class D : public C<T> {};
	//	class E : public C<A> {};
	//	void foo(B b) {}
    //  template<>
    //  class C<X> {
    //		public:
	//		X t;
	//		operator B() {B b; return b;}
    //  };
	
    //  class F : public C<A> {};
	//	void refs() {
	//		C<A> c;
	//		foo(c);
	//		D<A> d;
	//		foo(d);
	//		E e;
	//		foo(e);
    //      F f;
	//		foo(f);
    //      C<X> cx;
    //      foo(cx);
	//	}
    public void testUserDefinedConversionOperator_224364() throws Exception {
    	IBinding ca=   getBindingFromASTName("C<A>", 4);
    	assertInstance(ca, ICPPClassType.class);
    	assertInstance(ca, ICPPTemplateInstance.class);
    	
    	IBinding foo1= getBindingFromASTName("foo(c)", 3);
    	
    	IBinding da=   getBindingFromASTName("D<A>", 4);
    	assertInstance(da, ICPPClassType.class);
    	assertInstance(da, ICPPTemplateInstance.class);
    	
		IBinding foo2= getBindingFromASTName("foo(d)", 3);
		IBinding foo3= getBindingFromASTName("foo(e)", 3);
		IBinding foo4= getBindingFromASTName("foo(cx)", 3);
		
		assertEquals(foo1, foo2); assertEquals(foo2, foo3);
		assertEquals(foo3, foo4);
    }
    
    // template<typename T>
    // class A {};
    //
    // class B {};
    // 
    // template<>
    // class A<B> {};
    
    // class C {};
    //
    // A<B> ab;
    // A<C> ac;
    public void testEnclosingScopes_a() throws Exception {
    	ICPPSpecialization   b0= getBindingFromASTName("A<B>", 4, ICPPSpecialization.class, ICPPClassType.class);
    	ICPPTemplateInstance b1= getBindingFromASTName("A<C>", 4, ICPPTemplateInstance.class, ICPPClassType.class);
    	
    	ICPPClassType sc0= assertInstance(b0.getSpecializedBinding(), ICPPClassType.class);
    	ICPPClassType sc1= assertInstance(b1.getSpecializedBinding(), ICPPClassType.class);
    	assertTrue(sc0.isSameType(sc1));
    	
    	assertNull(sc0.getScope());
    	assertNull(b0.getScope());
    }
    
    // template<typename T>
    // class A {
    //    public:
    //    class B {};
    // };
    //
    // class C {}; class D {};
    //
    // template<>
    // class A<C> {
    //   public:
    //   class B {};
    // };
    
    // void refs() {
    //    A<C>::B acb;
    //    A<D>::B adb;
    // }
    public void testEnclosingScopes_b() throws Exception {
    	ICPPClassType b0= getBindingFromASTName("B acb", 1, ICPPClassType.class);
    	ICPPClassType b1= getBindingFromASTName("B adb", 1, ICPPClassType.class, ICPPSpecialization.class);
    	ICPPClassType b2= getBindingFromASTName("A<C>", 4, ICPPClassType.class, ICPPSpecialization.class);
    	
    	ICPPClassType b3= (ICPPClassType) getIndex().findBindings("A".toCharArray(), new IndexFilter() {
    		@Override
    		public boolean acceptBinding(IBinding binding) throws CoreException {
    			return !(binding instanceof ICPPSpecialization);
    		}
    	}, npm())[0];
    	
    	ICPPClassType b4= (ICPPClassType) getIndex().findBindings(new char[][] {"A".toCharArray(), "B".toCharArray()}, new IndexFilter() {
    		@Override
    		public boolean acceptBinding(IBinding binding) throws CoreException {
    			try {
    				return !(binding.getScope() instanceof CPPClassSpecializationScope); //
    			} catch(DOMException de) {
    				CCorePlugin.log(de);
    				return false;
    			}
    		}
    	}, npm())[0];
    	
    	assertFalse(b0 instanceof ICPPSpecialization);
    	
    	IIndexScope s0= (IIndexScope) b0.getScope(), s4= (IIndexScope) b4.getScope();
    	IScope s1= b1.getScope();
    	
    	assertTrue(((IType)s0.getScopeBinding()).isSameType((IType)((IIndexScope)b2.getCompositeScope()).getScopeBinding()));
    	ICPPClassScope cs1= assertInstance(s1, ICPPClassScope.class);
    	assertInstance(cs1.getClassType(), ICPPClassType.class);
    	assertInstance(cs1.getClassType(), ICPPTemplateInstance.class);
    	assertTrue(((IType)s4.getScopeBinding()).isSameType( (IType) ((IIndexScope)b3.getCompositeScope()).getScopeBinding() ));
    }
    
	// class A {};
	// 
	// template<typename T>
	// class X {
	// public:
	//    class Y {
	//    public:
	//       class Z {};
	//    };
	// };
	
	// X<A>::Y::Z xayz;
    public void testEnclosingScopes_c() throws Exception {
    	ICPPClassType b0= getBindingFromASTName("Y::Z x", 1, ICPPClassType.class);
    	ICPPClassType b1= getBindingFromASTName("Z xayz", 1, ICPPClassType.class);
    	
    	IScope s0= b0.getScope(), s1= b1.getScope();
    	
    	ICPPClassScope cs0= assertInstance(s0, ICPPClassScope.class);
    	assertInstance(cs0.getClassType(), ICPPClassType.class);
    	assertInstance(cs0.getClassType(), ICPPSpecialization.class);
    	
    	ICPPClassScope cs1= assertInstance(s1, ICPPClassScope.class);
    	assertInstance(cs1.getClassType(), ICPPClassType.class);
    	assertInstance(cs1.getClassType(), ICPPSpecialization.class);    	
    }
    
    // class A {}; class B {};
    //
    // template<typename T1, typename T2>
    // class X {};
    //
    // template<typename T3>
    // class X<T3, A> {
    // public:
    //     class N {};
    // };
    
    // X<B,A>::N n;
    public void testEnclosingScopes_d() throws Exception {
    	ICPPClassType b0= getBindingFromASTName("N n", 1, ICPPClassType.class, ICPPSpecialization.class);
    	ICPPClassType b1= assertInstance(((ICPPSpecialization) b0).getSpecializedBinding(), ICPPClassType.class);
    	
    	ICPPClassScope s0= assertInstance(b0.getScope(), ICPPClassScope.class);
    	assertInstance(s0.getClassType(), ICPPTemplateInstance.class);
    	
    	ICPPClassScope s1= assertInstance(b1.getScope(), ICPPClassScope.class);
    	assertInstance(s1.getClassType(), ICPPTemplateDefinition.class);
    	
    	assertNull(s1.getClassType().getScope());
    }
    
	//    typedef signed int SI;
	//
	//    template <SI x>
	//    class A {};

	//    const SI y= 99;
	//    A<y> ay;
    public void testNonTypeTemplateParameter_207840() {
    	ICPPVariable b0= getBindingFromASTName("y>", 1, ICPPVariable.class);
    	ICPPClassType b1= getBindingFromASTName("A<y>", 1, ICPPClassType.class, ICPPTemplateDefinition.class);
    	ICPPTemplateInstance b2= getBindingFromASTName("A<y>", 4, ICPPTemplateInstance.class, ICPPClassType.class);
    	ObjectMap args= b2.getArgumentMap();
    	assertInstance(args.keyAt(0), ICPPTemplateNonTypeParameter.class);
    	assertEquals(1, args.size());
    }
    
	// template <class T> class A {                          
	//    class B { T t; };                                  
	//    B b;                                               
	// };      
    
	// void f() {                                            
	//    A<int> a;                                          
	//    a.b.t;                                             
	// }  
	public void testNestedClassTypeSpecializations() throws Exception {
		ICPPField t2 = getBindingFromASTName("t;", 1, ICPPField.class);

		assertTrue(t2 instanceof ICPPSpecialization);
		final IType type = t2.getType();
		assertTrue(type instanceof IBasicType);
		assertEquals(((IBasicType)type).getType(), IBasicType.t_int);
	}
	
	//	template<typename _Iterator> struct iterator_traits {
	//		typedef typename _Iterator::pointer           pointer;
	//	};
	//
	//	template<typename _Tp> struct iterator_traits<_Tp*> {
	//	    typedef _Tp*                        pointer;
	//	};
	//
	//	template<typename _Iterator, typename _Container> class normal_iterator {
	//	    protected:
	//		_Iterator _M_current;
	//	    
	//      public:
	//		typedef typename iterator_traits<_Iterator>::pointer   pointer;
	//		normal_iterator() : _M_current(_Iterator()) { }
	//
	//		pointer operator->() const {
	//			return _M_current;
	//		}
	//	};
	//
	//	template<typename _Tp> class allocator {
	//		public:
	//			typedef _Tp*       pointer;
	//	};
	//
	//	template<typename _Tp, typename _Alloc = allocator<_Tp> >
	//	class vector {
	//		typedef vector<_Tp, _Alloc> vector_type;
	//
	//		public:
	//	    typedef typename _Alloc::pointer pointer;
	//		typedef normal_iterator<pointer, vector_type> iterator;
	//	};
	//

	//	struct MyStruct {
	//		int member;
	//	};
	//	typedef vector<MyStruct> VS1;
	//	void test() {
	//		VS1::iterator it;
	//		it->member; // it->member
	//	}
	public void testVectorIterator() throws Exception {
		ICPPField t2 = getBindingFromASTName("member; // it->member", 6, ICPPField.class);
		ICPPClassType ct= t2.getClassOwner();
		assertEquals("MyStruct", ct.getName());
		
		final IType type = t2.getType();
		assertTrue(type instanceof IBasicType);
		assertEquals(((IBasicType)type).getType(), IBasicType.t_int);
	}
	
	//	template <int x>
	//	class C {
	//	public:
	//		inline C() {};
	//	};
	//
	//	const int _256=0x100;
	//
	//	typedef C<_256> aRef;
	//
	//	void foo(aRef& aRefence) {}
	//	void bar(C<_256>& aRefence) {}
	//	void baz(void) {}
	
	//	int main (void) {
	//		C<256> t;
	//		foo(t);
	//		bar(t);
	//		baz();
	//	}
	public void testClassInstanceWithNonTypeArgument_207871() throws Exception {
		ICPPTemplateInstance c256 = getBindingFromASTName("C<256>", 6, ICPPTemplateInstance.class, ICPPClassType.class);
		ObjectMap args= c256.getArgumentMap();
		assertEquals(1, args.size());
		assertInstance(args.keyAt(0), ICPPTemplateNonTypeParameter.class);
		ICPPBasicType bt= assertInstance(args.getAt(0), ICPPBasicType.class);
		IASTExpression val= bt.getValue();
		
		ICPPFunction foo = getBindingFromASTName("foo(t)", 3, ICPPFunction.class);
		ICPPFunction bar = getBindingFromASTName("bar(t)", 3, ICPPFunction.class);
	}
	
	//	template<class T, int x> class A {public: class X {};};
	//	template<class T1> class A<T1,'y'> {public: class Y {};};
	//	template<class T2> class A<T2,'z'> {public: class Z {};};
	//
	//	class B {};
	
	//	A<B, 'x'>::X x;
	//	A<B, 'y'>::Y y;
	//	A<B, 'z'>::Z z;
	public void testNonTypeCharArgumentDisambiguation() throws Exception {
		ICPPClassType b2= getBindingFromASTName("A<B, 'x'>", 9, ICPPClassType.class, ICPPTemplateInstance.class);
		ICPPClassType b3= getBindingFromASTName("A<B, 'y'>", 9, ICPPClassType.class, ICPPTemplateInstance.class);
		ICPPClassType b4= getBindingFromASTName("A<B, 'z'>", 9, ICPPClassType.class, ICPPTemplateInstance.class);
		
		assertTrue(!b2.isSameType(b3));
		assertTrue(!b3.isSameType(b4));
		assertTrue(!b4.isSameType(b2));
		
		ICPPClassType X= getBindingFromASTName("X x", 1, ICPPClassType.class);
		ICPPClassType Y= getBindingFromASTName("Y y", 1, ICPPClassType.class);
		ICPPClassType Z= getBindingFromASTName("Z z", 1, ICPPClassType.class);
		
		assertTrue(!X.isSameType(Y));
		assertTrue(!Y.isSameType(Z));
		assertTrue(!Z.isSameType(X));
	}
	
	//	template<class T, bool b> class A {public: class X {};};
	//	template<class T1> class A<T1,true> {public: class Y {};};
	//
	//	class B {};
	
	//	A<B, false>::X x; //1
	//	A<B, true>::Y y; //2
	//
	//	A<B, true>::X x; //3 should be an error
	//	A<B, false>::Y y; //4 should be an error
	public void testNonTypeBooleanArgumentDisambiguation() throws Exception {
		ICPPClassType X= getBindingFromASTName("X x; //1", 1, ICPPClassType.class);
		ICPPClassType Y= getBindingFromASTName("Y y; //2", 1, ICPPClassType.class);
		getProblemFromASTName("X x; //3", 1);
		getProblemFromASTName("Y y; //4", 1);
		
		assertTrue(!X.isSameType(Y));
	}
	
	// template<int x> class A {};
	// template<> class A<5> {public: class B{};};
	//
	// const int FIVE= 5;
	// const int CINQ= FIVE;

	// const int FUNF= CINQ;
	// void refs() {
	//    A<FIVE> a5a;
	//    A<CINQ> a5b;
	//    A<FUNF> a5c;
	//    A<5> a5d;
	//    A<1> a1;
	// }
	public void testConstantPropagationFromHeader() throws Exception {
		ICPPClassType a5a= getBindingFromASTName("A<FIVE>", 7, ICPPClassType.class, ICPPSpecialization.class);
		ICPPClassType a5b= getBindingFromASTName("A<CINQ>", 7, ICPPClassType.class, ICPPSpecialization.class);
		ICPPClassType a5c= getBindingFromASTName("A<FUNF>", 7, ICPPClassType.class, ICPPSpecialization.class);
		ICPPClassType a5d= getBindingFromASTName("A<5>", 4, ICPPClassType.class, ICPPSpecialization.class);
		ICPPClassType a1= getBindingFromASTName("A<1>", 4, ICPPClassType.class, ICPPTemplateInstance.class);
		
		assertTrue(a5a.isSameType(a5b));
		assertTrue(a5b.isSameType(a5c));
		assertTrue(a5c.isSameType(a5d));
		assertTrue(a5d.isSameType(a5a));
		
		assertTrue(!a1.isSameType(a5a));
		assertTrue(!a1.isSameType(a5b));
		assertTrue(!a1.isSameType(a5c));
		assertTrue(!a1.isSameType(a5d));
	}
	
    //	template<int I>
	//	class That {
	//	public:
	//		That(int x) {}
	//	};
	//
	//	template<int T>
	//	class This : public That<T> {
	//	public:
	//		inline This();
	//	};
	
	//	template <int I>
	//	inline This<I>::This() : That<I>(I) {
	//  }
	public void testParameterReferenceInChainInitializer_a() throws Exception {
		// These intermediate assertions will not hold until deferred non-type arguments are
		// correctly modelled
		/*
		ICPPClassType tid= ba.assertNonProblem("This<I>::T", 7, ICPPClassType.class);
		assertFalse(tid instanceof ICPPSpecialization);
		ICPPConstructor th1sCtor= ba.assertNonProblem("This() :", 4, ICPPConstructor.class);
		assertFalse(th1sCtor instanceof ICPPSpecialization);ICPPTemplateNonTypeParameter np= ba.assertNonProblem("I)", 1, ICPPTemplateNonTypeParameter.class);
		*/
		
		ICPPTemplateNonTypeParameter np= getBindingFromASTName("I>(I)", 1, ICPPTemplateNonTypeParameter.class);
		ICPPConstructor clazz= getBindingFromASTName("That<I>(I)", 4, ICPPConstructor.class);
		ICPPConstructor ctor= getBindingFromASTName("That<I>(I)", 7, ICPPConstructor.class);
	}
	
	//	template<typename I>
	//	class That {
	//		public:
	//			That() {}
	//	};
	//
	//	template<typename T>
	//	class This : public That<T> {
	//		public:
	//			inline This();
	//	};
	
	//	template <typename I>
	//	inline This<I>::This() : That<I>() {
	//	}
	public void testParameterReferenceInChainInitializer_b() throws Exception {
		ICPPClassType tid= getBindingFromASTName("This<I>::T", 7, ICPPClassType.class);
		assertFalse(tid instanceof ICPPSpecialization);
		ICPPConstructor th1sCtor= getBindingFromASTName("This() :", 4, ICPPConstructor.class);
		assertFalse(th1sCtor instanceof ICPPSpecialization);
		
		ICPPTemplateTypeParameter np= getBindingFromASTName("I>()", 1, ICPPTemplateTypeParameter.class);
		ICPPConstructor clazz= getBindingFromASTName("That<I>()", 4, ICPPConstructor.class);
		ICPPConstructor ctor= getBindingFromASTName("That<I>()", 7, ICPPConstructor.class);
	}
	
	
	// template<typename T> class CT {
	//    public: int field;
	// };
	
	// CT<int> v1;
	public void testUniqueSpecializations_Bug241641() throws Exception {
		ICPPVariable v1= getBindingFromASTName("v1", 2, ICPPVariable.class);
		ICPPVariable v2= getBindingFromASTName("v1", 2, ICPPVariable.class);
		
		IType t1= v1.getType();
		assertInstance(t1, ICPPClassType.class);
		
		ICPPClassType ct= (ICPPClassType) t1;
		IBinding f1= ct.getCompositeScope().find("field")[0];
		IBinding f2= ct.getCompositeScope().find("field")[0];
		
		assertSame(f1, f2);
	}
	
	// template<typename T> class CT {
	//    public: int field;
	// };
	
	// CT<int> v1;
	public void testUniqueInstance_Bug241641() throws Exception {
		ICPPVariable v1= getBindingFromASTName("v1", 2, ICPPVariable.class);
		ICPPVariable v2= getBindingFromASTName("v1", 2, ICPPVariable.class);
		
		IType t1= v1.getType();
		assertInstance(t1, ICPPTemplateInstance.class);
		
		ICPPTemplateInstance inst= (ICPPTemplateInstance) t1;
		final ICPPClassTemplate tmplDef = (ICPPClassTemplate) inst.getTemplateDefinition();
		IBinding inst2= CPPTemplates.instantiate(tmplDef, inst.getTemplateArguments(), false);
		assertSame(inst, inst2);
		
		IBinding charInst1= CPPTemplates.instantiate(tmplDef, new ICPPTemplateArgument[] {new CPPTemplateArgument(new CPPBasicType(Kind.eChar, 0))}, false);
		IBinding charInst2= CPPTemplates.instantiate(tmplDef, new ICPPTemplateArgument[] {new CPPTemplateArgument(new CPPBasicType(Kind.eChar, 0))}, false);
		assertSame(charInst1, charInst2);
	}
	
	//	template<typename T> class XT {
	//     public: void method() {};
	//  };
	//  XT<int> x;
	
	//	void test() {
	//     x.method();
	//  }
	public void testMethodSpecialization_Bug248927() throws Exception {
		ICPPMethod m= getBindingFromASTName("method", 6, ICPPMethod.class);
		assertInstance(m, ICPPSpecialization.class);
		ICPPClassType ct= m.getClassOwner();
		assertInstance(ct, ICPPTemplateInstance.class);
		ICPPMethod[] ms= ct.getDeclaredMethods();
		assertEquals(1, ms.length);
		assertEquals(m, ms[0]);
	}
	
    //	template<class T, class U> class A {};
    //	template<class T> class A<T, int> {   
    //	   void foo(T t);                     
    //	};                                    

	//	template<class T> void A<T, int>::foo(T t) {} 
    public void testBug177418() throws Exception {
		ICPPMethod m= getBindingFromASTName("foo", 3, ICPPMethod.class);
		ICPPClassType owner= m.getClassOwner();
		assertInstance(owner, ICPPClassTemplatePartialSpecialization.class);
    }
    
    
    // template<typename T> class XT {
    //    int f; 
    //    void m();
    // };

    // template<typename T> void XT<T>::m() {
    //    m(); // 1
    //    f; // 1
    //    this->m(); // 2
    //    this->f; // 2
    // };
    public void testUnknownBindings_Bug264988() throws Exception { 
		ICPPMethod m= getBindingFromASTName("m(); // 1", 1, ICPPMethod.class);
		assertFalse(m instanceof ICPPUnknownBinding);
		m= getBindingFromASTName("m(); // 2", 1, ICPPMethod.class);
		assertFalse(m instanceof ICPPUnknownBinding);
		
		ICPPField f= getBindingFromASTName("f; // 1", 1, ICPPField.class);
		assertFalse(f instanceof ICPPUnknownBinding);
		f= getBindingFromASTName("f; // 2", 1, ICPPField.class);
		assertFalse(f instanceof ICPPUnknownBinding);
    }
    
	// template <typename T= int> class XT;
	
    // #include "header.h"
	// template <typename T> class XT {};
	// void test() {
	//    XT<> x;
	// };
	public void testDefaultTemplateArgInHeader_264988() throws Exception { 
		ICPPTemplateInstance ti= getBindingFromASTName("XT<>", 4, ICPPTemplateInstance.class);
	}

	// typedef int TInt;
	// template <typename T> class XT {
	//    void m();
	// };
	
	// template<> void XT<int>::m() {
	//    TInt t;
	// }
	public void testParentScopeOfSpecialization_267013() throws Exception { 
		ITypedef ti= getBindingFromASTName("TInt", 4, ITypedef.class);
	}

	//	struct __true_type {};
	//	struct __false_type {};
	//
	//	template<typename, typename>
	//	struct __are_same {
	//	  enum { __value = 0 };
	//	  typedef __false_type __type;
	//	};
	//
	//	template<typename _Tp>
	//	struct __are_same<_Tp, _Tp> {
	//	  enum { __value = 1 };
	//	  typedef __true_type __type;
	//	};
	//
	//	template<bool, typename>
	//	struct __enable_if {};
	//
	//	template<typename _Tp>
	//	struct __enable_if<true, _Tp> {
	//	  typedef _Tp __type;
	//	};
	//
	//	template<typename _Iterator, typename _Container>
	//	struct __normal_iterator {
	//	  template<typename _Iter>
	//	    __normal_iterator(
	//	        const __normal_iterator<
	//	            _Iter,
	//	            typename __enable_if<
	//	                __are_same<_Iter, typename _Container::pointer>::__value,
	//	                _Container
	//	            >::__type
	//	        >& __i);
	//	};
	//
	//	template<typename _Tp>
	//	struct allocator {
	//	  typedef _Tp* pointer;
	//	  typedef const _Tp* const_pointer;
	//
	//	  template<typename _Tp1>
	//	  struct rebind
	//	  { typedef allocator<_Tp1> other; };
	//	};
	//
	//	template<typename _Tp, typename _Alloc = allocator<_Tp> >
	//	struct vector {
	//	  typedef vector<_Tp, _Alloc> vector_type;
	//	  typedef typename _Alloc::template rebind<_Tp>::other _Tp_alloc_type;
	//
	//	  typedef typename _Tp_alloc_type::pointer pointer;
	//	  typedef typename _Tp_alloc_type::const_pointer const_pointer;
	//	  typedef __normal_iterator<pointer, vector_type> iterator;
	//	  typedef __normal_iterator<const_pointer, vector_type> const_iterator;
	//
	//	  iterator begin();
	//	  const_iterator begin() const;
	//	};

	//	void f(vector<int>::const_iterator p);
	//
	//	void test() {
	//	  vector<int> v;
	//	  f(v.begin());
	//	}
	public void testTemplateMetaprogramming_284686() throws Exception { 
		getBindingFromASTName("f(v.begin())", 1, ICPPFunction.class);
	}
	
	//	template<typename T> class op {
	//  public:
	//		inline static int DO(T key, T key2) {
	//			return false;
	//	    }
	//	};
	//
	//	template<typename T, int KVT_KeyCompareProc(T key, T key2)=op<T>::DO> class Noder1 {};

	//	template<typename T, int KVT_KeyCompareProc(T key, T key2)=op<T>::DO> class Noder2 {};
	//
	//	void test() {
	//		Noder1<int> f;
	//		Noder2<int> g;
	//	}
	public void testInstantiationOfValue_284683() throws Exception { 
		getBindingFromASTName("Noder1<int>", 11, ICPPClassSpecialization.class);
		getBindingFromASTName("Noder2<int>", 11, ICPPClassSpecialization.class);
	}
	

	//	template <typename> struct CT;
	//	template <typename T> struct CT {
	//		T f;
	//	};
	//	struct X {
	//		int x;
	//	};

	//	void test() {
	//		CT<X> p;
	//		p.f.x; 
	//	}
	public void testTemplateParameterWithoutName_300978() throws Exception { 
		getBindingFromASTName("x;", 1, ICPPField.class);
		ICPPClassSpecialization ctx = getBindingFromASTName("CT<X>", 5, ICPPClassSpecialization.class);
		ICPPClassTemplate ct= (ICPPClassTemplate) ctx.getSpecializedBinding();
		assertEquals("T", ct.getTemplateParameters()[0].getName());
	}

	// template<typename T> class X {};
	// template<typename T> class Y {};
	// template<> class Y<int> {};
	// template<typename T> void f(T t) {}
	// template<typename T> void g(T t) {}
	// template<> void g(int t) {}

	// void test() {
	//    X<int> x;
	//    Y<int> y;
	//    f(1);
	//    g(1);
	// }
	public void testExplicitSpecializations_296427() throws Exception { 
		ICPPTemplateInstance inst;
		inst= getBindingFromASTName("X<int>", 0);
		assertFalse(inst.isExplicitSpecialization());
		inst = getBindingFromASTName("Y<int>", 0);
		assertTrue(inst.isExplicitSpecialization());
		
		inst = getBindingFromASTName("f(1)", 1);
		assertFalse(inst.isExplicitSpecialization());
		inst = getBindingFromASTName("g(1)", 1);
		assertTrue(inst.isExplicitSpecialization());
	}
}
