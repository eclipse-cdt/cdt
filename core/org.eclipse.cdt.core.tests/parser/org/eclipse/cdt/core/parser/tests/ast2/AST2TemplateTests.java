/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *     Nathan Ridge
 *     Danny Ferreira
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import static org.eclipse.cdt.core.parser.ParserLanguage.CPP;
import static org.eclipse.cdt.core.parser.tests.VisibilityAsserts.assertVisibility;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateType;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

import junit.framework.TestSuite;

public class AST2TemplateTests extends AST2CPPTestBase {

	public AST2TemplateTests() {
	}

	public AST2TemplateTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(AST2TemplateTests.class);
	}

	private NameCollector getNameCollector(IASTTranslationUnit ast) {
		NameCollector collector = new NameCollector();
		ast.accept(collector);
		return collector;
	}

	public void testBasicClassTemplate() throws Exception {
		IASTTranslationUnit tu = parse("template <class T> class A{ T t; };", CPP); //$NON-NLS-1$
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 4);
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();

		ICPPTemplateScope scope = (ICPPTemplateScope) T.getScope();
		IScope s2 = A.getScope();
		assertSame(scope, s2);

		ICPPField t = (ICPPField) col.getName(3).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(2).resolveBinding();

		assertSame(T, T2);
		IType type = t.getType();
		assertSame(type, T);

		assertNotNull(T);
		assertNotNull(A);
	}

	// template < class T > class A {
	//    T t1;
	//    T * t2;
	// };
	// void f(){
	//    A<int> a;
	//    a.t1; a.t2;
	// }
	public void testBasicTemplateInstance_a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 14);

		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPField t1 = (ICPPField) col.getName(3).resolveBinding();
		ICPPField t2 = (ICPPField) col.getName(5).resolveBinding();

		assertSame(t1.getType(), T);
		assertSame(((IPointerType) t2.getType()).getType(), T);

		ICPPVariable a = (ICPPVariable) col.getName(9).resolveBinding();

		ICPPClassType A_int = (ICPPClassType) col.getName(7).resolveBinding();
		assertSame(A_int, a.getType());

		assertTrue(A_int instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A_int).getTemplateDefinition(), A);

		ICPPClassScope A_int_Scope = (ICPPClassScope) A_int.getCompositeScope();
		assertNotSame(A_int_Scope, ((ICompositeType) A).getCompositeScope());

		ICPPField t = (ICPPField) col.getName(11).resolveBinding();
		assertTrue(t instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) t).getSpecializedBinding(), t1);
		assertSame(t.getScope(), A_int_Scope);
		IType type = t.getType();
		assertTrue(type instanceof IBasicType);
		assertEquals(((IBasicType) type).getType(), IBasicType.t_int);

		t = (ICPPField) col.getName(13).resolveBinding();
		assertTrue(t instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) t).getSpecializedBinding(), t2);
		assertSame(t.getScope(), A_int_Scope);
		type = t.getType();
		assertTrue(type instanceof IPointerType);
		assertTrue(((IPointerType) type).getType() instanceof IBasicType);
		assertEquals(((IBasicType) ((IPointerType) type).getType()).getType(), IBasicType.t_int);
	}

	// template < class T > class A {
	//    T f(T *);
	// };
	// void g(){
	//    A<int> a;
	//    a.f((int*)0);
	// }
	public void testBasicTemplateInstance_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(3).resolveBinding();
		IFunctionType ft = f.getType();

		assertSame(ft.getReturnType(), T);
		assertSame(((IPointerType) ft.getParameterTypes()[0]).getType(), T);

		ICPPClassType A_int = (ICPPClassType) col.getName(7).resolveBinding();
		assertTrue(A_int instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A_int).getTemplateDefinition(), A);

		ICPPMethod f_int = (ICPPMethod) col.getName(11).resolveBinding();
		assertTrue(f_int instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) f_int).getSpecializedBinding(), f);
		ft = f_int.getType();
		assertTrue(ft.getReturnType() instanceof IBasicType);
		assertTrue(((IPointerType) ft.getParameterTypes()[0]).getType() instanceof IBasicType);
	}

	// template <class T > void f(T);
	// template <class T > void f(T) {
	//    T * d;
	// }
	// void foo() {
	//    f<int>(0);
	// }
	public void testBasicTemplateFunction() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();

		IParameter p1 = (IParameter) col.getName(3).resolveBinding();

		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(4).resolveBinding();
		ICPPFunction f2 = (ICPPFunction) col.getName(5).resolveBinding();
		IParameter p2 = (IParameter) col.getName(7).resolveBinding();

		assertSame(T, T2);
		assertSame(f, f2);
		assertSame(p1, p2);
		assertSame(p1.getType(), T);

		ICPPFunction f3 = (ICPPFunction) col.getName(11).resolveBinding();
		assertTrue(f3 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) f3).getTemplateDefinition(), f);

		assertInstances(col, T, 5);
	}

	// template < class T > class pair {
	//    template < class U > pair(const pair<U> &);
	// };
	public void testStackOverflow_a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertTrue(col.getName(0).resolveBinding() instanceof ICPPTemplateParameter);
		ICPPClassTemplate pair = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPTemplateParameter U = (ICPPTemplateParameter) col.getName(2).resolveBinding();
		assertTrue(col.getName(3).resolveBinding() instanceof ICPPFunctionTemplate);
		ICPPTemplateInstance pi = (ICPPTemplateInstance) col.getName(4).resolveBinding();
		ICPPClassTemplate p = (ICPPClassTemplate) col.getName(5).resolveBinding();
		ICPPTemplateParameter U2 = (ICPPTemplateParameter) col.getName(6).resolveBinding();

		assertSame(U, U2);
		assertSame(pair, p);
		assertSame(pi.getTemplateDefinition(), pair);
	}

	// template < class T > class A {};
	// template < class T > class A< T* > {};
	// template < class T > class A< T** > {};
	public void testBasicClassPartialSpecialization() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T1 = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(2).resolveBinding();
		ICPPClassTemplatePartialSpecialization A2 = (ICPPClassTemplatePartialSpecialization) col.getName(3)
				.resolveBinding();
		ICPPTemplateParameter T3 = (ICPPTemplateParameter) col.getName(5).resolveBinding();
		ICPPClassTemplatePartialSpecialization A3 = (ICPPClassTemplatePartialSpecialization) col.getName(7)
				.resolveBinding();
		ICPPTemplateParameter T4 = (ICPPTemplateParameter) col.getName(6).resolveBinding();

		assertSame(A2.getPrimaryClassTemplate(), A1);
		assertSame(A3.getPrimaryClassTemplate(), A1);
		assertNotSame(T1, T2);
		assertNotSame(A1, A2);
		assertNotSame(A1, A3);
		assertNotSame(A2, A3);
		assertSame(T2, T3);
		assertNotSame(T2, T4);
	}

	// template < class T > class A { typedef int TYPE; };
	// template < class T > typename A<T>::TYPE foo(T);
	// template < class T > typename A<T>::TYPE foo(T);
	public void testStackOverflow_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T0 = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPTemplateParameter T1 = (ICPPTemplateParameter) col.getName(3).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(12).resolveBinding();

		assertNotSame(T0, T1);
		assertSame(T1, T2);

		ICPPFunctionTemplate foo1 = (ICPPFunctionTemplate) col.getName(9).resolveBinding();
		ICPPFunctionTemplate foo2 = (ICPPFunctionTemplate) col.getName(18).resolveBinding();
		assertSame(foo1, foo2);

		ITypedef TYPE = (ITypedef) col.getName(2).resolveBinding();
		IBinding b0 = col.getName(8).resolveBinding();
		IBinding b1 = col.getName(17).resolveBinding();
		assertSame(b1, b0);

		// the instantiation of A<T> has to be deferred.
		assertInstance(b0, ICPPUnknownBinding.class);
		final IType parent = ((ICPPInternalUnknownScope) b0.getScope()).getScopeType();
		assertInstance(parent, ICPPDeferredClassInstance.class);
		assertSame(((ICPPDeferredClassInstance) parent).getSpecializedBinding(), A);

		assertInstances(col, T1, 6);
	}

	//	template<typename _A_>
	//	struct A : public _A_::member_t {};
	//
	//	struct B : public A<B>{};
	public void testStackOverflowInBaseComputation_418996() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType B = helper.assertNonProblem("A<B>", 4);
		// Check that this line does not cause a StackOverflowError.
		B.getBases();
	}

	// template < class T > class A {
	//    void f();
	// };
	// template < class T > void A<T>::f() { }
	public void testTemplateMemberDef() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPMethod f1 = (ICPPMethod) col.getName(2).resolveBinding();
		ICPPMethod f2 = (ICPPMethod) col.getName(8).resolveBinding();

		assertSame(f2, f1);
	}

	// template < class T > void f(T);         // #1
	// template < class T > void f(T*);        // #2
	// template < class T > void f(const T*);  // #3
	// void main() {
	//    const int *p;
	//    f(p); //calls f(const T *) , 3 is more specialized than 1 or 2
	// }
	public void test14_5_5_2s5_OrderingFunctionTemplates_a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunctionTemplate f2 = (ICPPFunctionTemplate) col.getName(5).resolveBinding();
		ICPPFunctionTemplate f3 = (ICPPFunctionTemplate) col.getName(9).resolveBinding();

		assertNotSame(f1, f2);
		assertNotSame(f2, f3);
		assertNotSame(f3, f1);

		IFunction f = (IFunction) col.getName(14).resolveBinding();
		assertTrue(f instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) f).getTemplateDefinition(), f3);
	}

	// template < class T > void f(T);    // #1
	// template < class T > void f(T&);   // #2
	// void main() {
	//    float x;
	//    f(x); //ambiguous 1 or 2
	// }
	public void test14_5_5_2s5_OrderingFunctionTemplates_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunctionTemplate f2 = (ICPPFunctionTemplate) col.getName(5).resolveBinding();

		assertNotSame(f1, f2);

		IProblemBinding f = (IProblemBinding) col.getName(10).resolveBinding();
		assertEquals(f.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP);
	}

	// template < int N > void f(const int (&v)[N]);
	// void main() {
	//     f({1,2,3});
	// }
	public void test_dr1591_DeduceArrayFromInitializerList_a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		assertEquals("f", col.getName(5).toString());

		IBinding fCall = col.getName(5).resolveBinding();
		assertInstance(fCall, IFunction.class);
		IFunction f2 = (IFunction) fCall;
		assertInstance(f2, ICPPTemplateInstance.class);
		assertSame(f, ((ICPPTemplateInstance) f2).getTemplateDefinition());
	}

	// template < typename T, int N > void f(const T (&v)[N]);
	// void main() {
	//     f({1,2,3});
	// }
	public void test_dr1591_DeduceArrayFromInitializerList_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(2).resolveBinding();
		assertEquals("f", col.getName(7).toString());

		IBinding fCall = col.getName(7).resolveBinding();
		assertInstance(fCall, IFunction.class);
		IFunction f2 = (IFunction) fCall;
		assertInstance(f2, ICPPTemplateInstance.class);
		assertSame(f, ((ICPPTemplateInstance) f2).getTemplateDefinition());
	}

	// template < typename T, int N > void f(const T (&v)[N]);
	// void main() {
	//     f({1,2.0,3});
	// }
	public void test_dr1591_DeduceArrayFromInitializerList_c() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(2).resolveBinding();
		assertEquals("f", col.getName(7).toString());

		IBinding fCall = col.getName(7).resolveBinding();
		assertInstance(fCall, IProblemBinding.class);

		IProblemBinding fCallPB = (IProblemBinding) fCall;
		assertEquals(IProblemBinding.SEMANTIC_NAME_NOT_FOUND, fCallPB.getID());
	}

	// template < class T, template < class X > class U, T *pT > class A {
	// };
	public void testTemplateParameters() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateTypeParameter T = (ICPPTemplateTypeParameter) col.getName(0).resolveBinding();
		ICPPTemplateTemplateParameter U = (ICPPTemplateTemplateParameter) col.getName(2).resolveBinding();
		ICPPTemplateNonTypeParameter pT = (ICPPTemplateNonTypeParameter) col.getName(4).resolveBinding();

		ICPPTemplateTypeParameter X = (ICPPTemplateTypeParameter) col.getName(1).resolveBinding();

		ICPPTemplateParameter[] ps = U.getTemplateParameters();
		assertEquals(ps.length, 1);
		assertSame(ps[0], X);

		IPointerType ptype = (IPointerType) pT.getType();
		assertSame(ptype.getType(), T);
	}

	// template <class T> class A {
	//    A<T>* a;
	//    A<T>* a2;
	// };
	// void f(){
	//    A<int> * b;
	//    b->a;
	// }
	public void testDeferredInstances() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPTemplateInstance A_T = (ICPPTemplateInstance) col.getName(2).resolveBinding();
		assertSame(A_T.getTemplateDefinition(), A);

		ICPPTemplateInstance A_T2 = (ICPPTemplateInstance) col.getName(6).resolveBinding();
		assertSame(A_T, A_T2);

		ICPPVariable a = (ICPPVariable) col.getName(5).resolveBinding();
		IPointerType pt = (IPointerType) a.getType();
		assertSame(pt.getType(), A_T);

		ICPPVariable b = (ICPPVariable) col.getName(13).resolveBinding();
		IType bt = b.getType();
		assertTrue(bt instanceof IPointerType);

		ICPPVariable a2 = (ICPPVariable) col.getName(15).resolveBinding();
		assertTrue(a2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) a2).getSpecializedBinding(), a);
		IType at = a2.getType();
		assertTrue(at instanceof IPointerType);

		assertSame(((IPointerType) at).getType(), ((IPointerType) bt).getType());
	}

	// template < class T1, class T2, int I > class A                {}; //#1
	// template < class T, int I >            class A < T, T*, I >   {}; //#2
	// template < class T1, class T2, int I > class A < T1*, T2, I > {}; //#3
	// template < class T >                   class A < int, T*, 5 > {}; //#4
	// template < class T1, class T2, int I > class A < T1, T2*, I > {}; //#5
	//
	// A <int, int, 1>   a1;		//uses #1
	// A <int, int*, 1>  a2;		//uses #2, T is int, I is 1
	// A <int, char*, 5> a3;		//uses #4, T is char
	// A <int, char*, 1> a4;		//uses #5, T is int, T2 is char, I is1
	// A <int*, int*, 2> a5;		//ambiguous, matches #3 & #5.
	public void test14_5_4_1s2_MatchingTemplateSpecializations() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(3).resolveBinding();
		ICPPClassTemplate A2 = (ICPPClassTemplate) col.getName(6).resolveBinding();
		ICPPClassTemplate A3 = (ICPPClassTemplate) col.getName(14).resolveBinding();
		ICPPClassTemplate A4 = (ICPPClassTemplate) col.getName(20).resolveBinding();
		ICPPClassTemplate A5 = (ICPPClassTemplate) col.getName(26).resolveBinding();

		assertTrue(A3 instanceof ICPPClassTemplatePartialSpecialization);
		assertSame(((ICPPClassTemplatePartialSpecialization) A3).getPrimaryClassTemplate(), A1);

		ICPPTemplateTypeParameter T1 = (ICPPTemplateTypeParameter) col.getName(11).resolveBinding();
		ICPPTemplateTypeParameter T2 = (ICPPTemplateTypeParameter) col.getName(12).resolveBinding();
		ICPPTemplateNonTypeParameter I = (ICPPTemplateNonTypeParameter) col.getName(13).resolveBinding();

		ICPPTemplateParameter TR1 = (ICPPTemplateParameter) col.getName(16).resolveBinding();
		ICPPTemplateParameter TR2 = (ICPPTemplateParameter) col.getName(17).resolveBinding();
		ICPPTemplateParameter TR3 = (ICPPTemplateParameter) col.getName(18).resolveBinding();

		assertSame(T1, TR1);
		assertSame(T2, TR2);
		assertSame(I, TR3);

		ICPPTemplateInstance R1 = (ICPPTemplateInstance) col.getName(31).resolveBinding();
		ICPPTemplateInstance R2 = (ICPPTemplateInstance) col.getName(34).resolveBinding();
		ICPPTemplateInstance R3 = (ICPPTemplateInstance) col.getName(37).resolveBinding();
		ICPPTemplateInstance R4 = (ICPPTemplateInstance) col.getName(40).resolveBinding();
		IProblemBinding R5 = (IProblemBinding) col.getName(43).resolveBinding();
		assertEquals(R5.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP);

		assertSame(R1.getTemplateDefinition(), A1);
		assertSame(R2.getTemplateDefinition(), A2);
		assertSame(R4.getTemplateDefinition(), A5);
		assertSame(R3.getTemplateDefinition(), A4);
	}

	// template <class T> void f(T);
	// template <class T> void f(T*);
	// template <> void f(int);       //ok
	// template <> void f<int>(int*); //ok
	public void test14_7_3_FunctionExplicitSpecialization() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate fT1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunctionTemplate fT2 = (ICPPFunctionTemplate) col.getName(5).resolveBinding();

		ICPPSpecialization f1 = (ICPPSpecialization) col.getName(8).resolveBinding();
		ICPPSpecialization f2 = (ICPPSpecialization) col.getName(10).resolveBinding();

		assertSame(f1.getSpecializedBinding(), fT1);
		assertSame(f2.getSpecializedBinding(), fT2);
	}

	// template<class T> void f(T*);
	// void g(int* p) { f(p); }
	public void test14_5_5_1_FunctionTemplates_a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(1).resolveBinding();

		ICPPFunction ref = (ICPPFunction) col.getName(6).resolveBinding();
		assertTrue(ref instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) ref).getTemplateDefinition(), f);
	}

	// template<class T> void f(T);
	// void g(int* p) { f(p); }
	public void test14_5_5_1_FunctionTemplates_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(1).resolveBinding();

		ICPPFunction ref = (ICPPFunction) col.getName(6).resolveBinding();
		assertTrue(ref instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) ref).getTemplateDefinition(), f);
	}

	// template<class X, class Y> X f(Y);
	// void g(){
	//    int i = f<int>(5); // Y is int
	// }
	public void test14_8_1s2_FunctionTemplates() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(3).resolveBinding();
		ICPPFunction ref1 = (ICPPFunction) col.getName(8).resolveBinding();

		assertTrue(ref1 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) ref1).getTemplateDefinition(), f);
	}

	// template<class T> void f(T);
	// void g(){
	//    f("Annemarie");
	// }
	public void test14_8_3s6_FunctionTemplates() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunction ref = (ICPPFunction) col.getName(5).resolveBinding();
		assertTrue(ref instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) ref).getTemplateDefinition(), f);
	}

	// template<class T> void f(T);         // #1
	// template<class T> void f(T*, int=1); // #2
	// template<class T> void g(T);         // #3
	// template<class T> void g(T*, ...);   // #4
	// int main() {
	//    int* ip;
	//    f(ip);                       //calls #2
	//    g(ip);                       //calls #4
	// }
	public void test14_5_5_2s6_FunctionTemplates() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunctionTemplate f2 = (ICPPFunctionTemplate) col.getName(5).resolveBinding();
		assertNotSame(f1, f2);

		ICPPFunctionTemplate g1 = (ICPPFunctionTemplate) col.getName(10).resolveBinding();
		ICPPFunctionTemplate g2 = (ICPPFunctionTemplate) col.getName(14).resolveBinding();
		assertNotSame(g1, g2);

		ICPPFunction ref1 = (ICPPFunction) col.getName(19).resolveBinding();
		ICPPFunction ref2 = (ICPPFunction) col.getName(21).resolveBinding();

		assertTrue(ref1 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) ref1).getTemplateDefinition(), f2);

		assertTrue(ref2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) ref2).getTemplateDefinition(), g2);
	}

	// template<class T> class X {
	//    X* p;               // meaning X<T>
	//    X<T>* p2;
	// };
	public void test14_6_1s1_LocalNames() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate X = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPClassType x1 = (ICPPClassType) col.getName(2).resolveBinding();
		ICPPClassType x2 = (ICPPClassType) col.getName(4).resolveBinding();

		assertTrue(x1 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) x1).getTemplateDefinition(), X);

		assertSame(x1, x2);
	}

	// template<class T> T f(T* p){
	// };
	// void g(int a, char* b){
	//    f(&a);              //call f<int>(int*)
	//    f(&b);              //call f<char*>(char**)
	// }
	public void test14_8s2() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(2).resolveBinding();

		ICPPFunction f1 = (ICPPFunction) col.getName(8).resolveBinding();
		ICPPFunction f2 = (ICPPFunction) col.getName(10).resolveBinding();

		assertNotSame(f1, f2);
		assertTrue(f1 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) f1).getTemplateDefinition(), f);
		assertTrue(f2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) f2).getTemplateDefinition(), f);

		IType fr1 = f1.getType().getReturnType();
		IType fr2 = f2.getType().getReturnType();

		assertTrue(fr1 instanceof IBasicType);
		assertEquals(((IBasicType) fr1).getType(), IBasicType.t_int);

		assertTrue(fr2 instanceof IPointerType);
		assertTrue(((IPointerType) fr2).getType() instanceof IBasicType);
		assertEquals(((IBasicType) ((IPointerType) fr2).getType()).getType(), IBasicType.t_char);
	}

	// template<class T> void f(T) {  }
	// template<class T> inline T g(T) {  }
	// template<> inline void f<>(int) {  } //OK: inline
	// template<> int g<>(int) {  }     // OK: not inline
	public void test14_7_3s14() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunctionTemplate g1 = (ICPPFunctionTemplate) col.getName(6).resolveBinding();

		ICPPSpecialization f2 = (ICPPSpecialization) col.getName(9).resolveBinding();
		ICPPSpecialization g2 = (ICPPSpecialization) col.getName(12).resolveBinding();

		assertSame(f2.getSpecializedBinding(), f1);
		assertSame(g2.getSpecializedBinding(), g1);

		assertFalse(((ICPPFunction) f1).isInline());
		assertTrue(((ICPPFunction) g1).isInline());
		assertTrue(((ICPPFunction) f2).isInline());
		assertFalse(((ICPPFunction) g2).isInline());
	}

	// template<class T> class X {
	//    X<T*> a; // implicit generation of X<T> requires
	//             // the implicit instantiation of X<T*> which requires
	//             // the implicit instantiation of X<T**> which ...
	// };
	// void f() {
	//    X<int> x;
	//    x.a.a.a.a;
	// }
	public void test14_7_1s14_InfiniteInstantiation() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate X = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPVariable x = (ICPPVariable) col.getName(9).resolveBinding();
		IType t = x.getType();
		assertTrue(t instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) t).getTemplateDefinition(), X);

		ICPPField a = (ICPPField) col.getName(5).resolveBinding();
		ICPPField a1 = (ICPPField) col.getName(11).resolveBinding();
		ICPPField a2 = (ICPPField) col.getName(12).resolveBinding();
		ICPPField a3 = (ICPPField) col.getName(13).resolveBinding();
		ICPPField a4 = (ICPPField) col.getName(14).resolveBinding();

		assertTrue(a1 instanceof ICPPSpecialization);
		assertTrue(a2 instanceof ICPPSpecialization);
		assertTrue(a3 instanceof ICPPSpecialization);
		assertTrue(a4 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) a1).getSpecializedBinding(), a);
		assertSame(((ICPPSpecialization) a2).getSpecializedBinding(), a);
		assertSame(((ICPPSpecialization) a3).getSpecializedBinding(), a);
		assertSame(((ICPPSpecialization) a4).getSpecializedBinding(), a);
	}

	// template<class T> class Y;
	// template<> class Y<int> {
	//    Y* p; // meaning Y<int>
	//    Y<char>* q; // meaning Y<char>
	// };
	public void test14_6_1s2() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate Y = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPSpecialization Yspec = (ICPPSpecialization) col.getName(2).resolveBinding();

		assertTrue(Yspec instanceof ICPPClassType);
		assertSame(Yspec.getSpecializedBinding(), Y);

		ICPPClassType y1 = (ICPPClassType) col.getName(4).resolveBinding();
		assertSame(y1, Yspec);

		ICPPClassType y2 = (ICPPClassType) col.getName(6).resolveBinding();
		assertTrue(y2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) y2).getTemplateDefinition(), Y);
	}

	// template < class T, class U > void f (T (*) (T, U));
	// int g (int, char);
	// void foo () {
	//    f(g);
	// }
	public void testBug45129() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction f1 = (ICPPFunction) col.getName(2).resolveBinding();
		ICPPFunction g1 = (ICPPFunction) col.getName(9).resolveBinding();

		IBinding f2 = col.getName(13).resolveBinding();
		IBinding g2 = col.getName(14).resolveBinding();

		assertTrue(f2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) f2).getTemplateDefinition(), f1);
		assertSame(g1, g2);
	}

	// template <class T, class U = T > class A {
	//    U u;
	// };
	// void f() {
	//    A<int> a;
	//    a.u;
	// }
	public void testBug76951() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPTemplateTypeParameter U = (ICPPTemplateTypeParameter) col.getName(1).resolveBinding();
		assertSame(U.getDefault(), T);

		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(3).resolveBinding();
		ICPPField u1 = (ICPPField) col.getName(5).resolveBinding();
		assertSame(u1.getType(), U);

		ICPPClassType A1 = (ICPPClassType) col.getName(7).resolveBinding();
		assertTrue(A1 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A1).getTemplateDefinition(), A);

		ICPPField u2 = (ICPPField) col.getName(11).resolveBinding();
		assertTrue(u2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) u2).getSpecializedBinding(), u1);

		IType type = u2.getType();
		assertTrue(type instanceof IBasicType);
		assertEquals(((IBasicType) type).getType(), IBasicType.t_int);
	}

	// template < class T > class A {
	//    A< int > a;
	// };
	// void f(A<int> p) { }
	public void testInstances() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPClassType A1 = (ICPPClassType) col.getName(2).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) col.getName(6).resolveBinding();

		assertSame(A1, A2);
		assertTrue(A1 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A1).getTemplateDefinition(), A);
	}

	// template <class T> void f(T);
	// template <class T> void f(T) {}
	public void testTemplateParameterDeclarations() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T1 = (ICPPTemplateParameter) col.getName(4).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(2).resolveBinding();

		assertSame(T1, T2);

		assertInstances(col, T1, 4);
	}

	// template < class T > class A {
	//    int f(A *);
	//    A < T > *pA;
	// };
	// void f () {
	//    A< int > *a;
	//    a->f(a);
	//    a->pA;
	// };
	public void testDeferredInstantiation() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(2).resolveBinding();
		ICPPClassType A1 = (ICPPClassType) col.getName(3).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) col.getName(5).resolveBinding();
		ICPPField pA = (ICPPField) col.getName(8).resolveBinding();

		assertSame(A1, A2);
		assertTrue(A1 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A1).getTemplateDefinition(), A);

		ICPPClassType AI = (ICPPClassType) col.getName(10).resolveBinding();
		ICPPMethod f2 = (ICPPMethod) col.getName(14).resolveBinding();
		ICPPField pA2 = (ICPPField) col.getName(17).resolveBinding();

		assertTrue(f2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) f2).getSpecializedBinding(), f);
		assertTrue(pA2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) pA2).getSpecializedBinding(), pA);

		IType paT = pA2.getType();
		assertTrue(paT instanceof IPointerType);
		assertSame(((IPointerType) paT).getType(), AI);

		IParameter p = f2.getParameters()[0];
		IType pT = p.getType();
		assertTrue(pT instanceof IPointerType);
		assertSame(((IPointerType) pT).getType(), AI);
	}

	// template <class T> struct A {
	//    void f(int);
	//    template <class T2> void f(T2);
	// };
	// template <> void A<int>::f(int) { } //nontemplate
	// template <> template <> void A<int>::f<>(int) { } //template
	// int main() {
	//    A<int> ac;
	//    ac.f(1);   //nontemplate
	//    ac.f('c'); //template
	//    ac.f<>(1); //template
	// }
	public void test14_5_2s2_MemberSpecializations() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPMethod f1 = (ICPPMethod) col.getName(2).resolveBinding();
		ICPPMethod f2 = (ICPPMethod) col.getName(5).resolveBinding();

		ICPPMethod f1_2 = (ICPPMethod) col.getName(11).resolveBinding();
		assertNotSame(f1, f1_2);
		assertTrue(f1_2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) f1_2).getSpecializedBinding(), f1);

		ICPPClassType A2 = (ICPPClassType) col.getName(9).resolveBinding();
		assertTrue(A2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A2).getTemplateDefinition(), A);

		ICPPMethod f2_2 = (ICPPMethod) col.getName(16).resolveBinding();
		assertTrue(f2_2 instanceof ICPPSpecialization);
		IBinding speced = ((ICPPSpecialization) f2_2).getSpecializedBinding();
		assertTrue(speced instanceof ICPPFunctionTemplate && speced instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) speced).getSpecializedBinding(), f2);

		ICPPClassType A3 = (ICPPClassType) col.getName(14).resolveBinding();
		assertSame(A2, A3);

		ICPPClassType A4 = (ICPPClassType) col.getName(20).resolveBinding();
		assertSame(A2, A4);

		IFunction r1 = (IFunction) col.getName(24).resolveBinding();
		IFunction r2 = (IFunction) col.getName(26).resolveBinding();
		IFunction r3 = (IFunction) col.getName(28).resolveBinding();

		assertSame(r1, f1_2);
		assertTrue(r2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) r2).getTemplateDefinition(), speced);
		assertSame(r3, f2_2);
	}

	// template <class T> class A { };
	// template <> class A<int> {};
	// A<char> ac;
	// A<int> ai;
	public void testClassSpecializations() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) col.getName(2).resolveBinding();

		assertTrue(A2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) A2).getSpecializedBinding(), A1);

		ICPPClassType r1 = (ICPPClassType) col.getName(4).resolveBinding();
		ICPPClassType r2 = (ICPPClassType) col.getName(7).resolveBinding();

		assertTrue(r1 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) r1).getTemplateDefinition(), A1);
		assertSame(r2, A2);
	}

	// template<class T> struct A {
	//    void f(T) {  }
	// };
	// template<> struct A<int> {
	//    void f(int);
	// };
	// void h(){
	//    A<int> a;
	//    a.f(16);   // A<int>::f must be defined somewhere
	// }
	// // explicit specialization syntax not used for a member of
	// // explicitly specialized class template specialization
	// void A<int>::f(int) {  }
	public void test14_7_3s5_SpecializationMemberDefinition() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPMethod f1 = (ICPPMethod) col.getName(2).resolveBinding();

		ICPPClassType A2 = (ICPPClassType) col.getName(5).resolveBinding();
		assertTrue(A2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) A2).getSpecializedBinding(), A1);

		ICPPMethod f2 = (ICPPMethod) col.getName(7).resolveBinding();
		assertNotSame(f1, f2);

		ICPPClassType A3 = (ICPPClassType) col.getName(10).resolveBinding();
		assertSame(A3, A2);
		ICPPMethod f3 = (ICPPMethod) col.getName(14).resolveBinding();
		assertSame(f3, f2);

		ICPPClassType A4 = (ICPPClassType) col.getName(16).resolveBinding();
		assertSame(A4, A2);
		ICPPMethod f4 = (ICPPMethod) col.getName(18).resolveBinding();
		assertSame(f4, f3);
	}

	// class C{};
	// template <class T> class A {
	//    template <class T2> class B {
	//       T f(T2);
	//    };
	// };
	// void g(){
	//    A<int>::B<C> b;
	//    C c;
	//    b.f(c);
	// }
	public void testNestedSpecializations() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType C = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(2).resolveBinding();
		ICPPClassTemplate B = (ICPPClassTemplate) col.getName(4).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(6).resolveBinding();

		ICPPClassType A1 = (ICPPClassType) col.getName(11).resolveBinding();
		assertTrue(A1 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A1).getTemplateDefinition(), A);

		ICPPClassType B1 = (ICPPClassType) col.getName(13).resolveBinding();
		assertTrue(B1 instanceof ICPPTemplateInstance);
		ICPPClassType B2 = (ICPPClassType) ((ICPPTemplateInstance) B1).getTemplateDefinition();
		assertTrue(B2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) B2).getSpecializedBinding(), B);

		ICPPMethod f1 = (ICPPMethod) col.getName(20).resolveBinding();
		assertTrue(f1 instanceof ICPPSpecialization);
		assertTrue(((ICPPSpecialization) f1).getSpecializedBinding() instanceof ICPPMethod);
		ICPPMethod f2 = (ICPPMethod) ((ICPPSpecialization) f1).getSpecializedBinding();
		assertTrue(f2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) f2).getSpecializedBinding(), f);

		IFunctionType ft = f1.getType();
		assertTrue(ft.getReturnType() instanceof IBasicType);
		assertEquals(((IBasicType) ft.getReturnType()).getType(), IBasicType.t_int);

		assertSame(ft.getParameterTypes()[0], C);
	}

	// namespace N {
	//    template<class T1, class T2> class A { };
	// }
	// using N::A;
	// namespace N {
	//    template<class T> class A<T, T*> { };
	// }
	// A<int,int*> a;
	public void test14_5_4s7_UsingClassTemplate() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(3).resolveBinding();
		ICPPClassTemplatePartialSpecialization A2 = (ICPPClassTemplatePartialSpecialization) col.getName(9)
				.resolveBinding();

		ICPPClassType A3 = (ICPPClassType) col.getName(13).resolveBinding();
		assertTrue(A3 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A3).getTemplateDefinition(), A2);

		ICPPClassTemplate A4 = (ICPPClassTemplate) col.getName(14).resolveBinding();
		assertSame(A4, A1);
	}

	// template<class T> class A {
	//    int x;
	// };
	// template<class T> class A<T*> {
	//    char x;
	// };
	// template<template<class U> class V> class C {
	//    V<int> y;
	//    V<int*> z;
	// };
	// void f() {
	//    C<A> c;
	//    c.y.x;   c.z.x;
	// }
	public void testTemplateTemplateParameter() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPField x1 = (ICPPField) col.getName(2).resolveBinding();
		ICPPClassTemplatePartialSpecialization A2 = (ICPPClassTemplatePartialSpecialization) col.getName(4)
				.resolveBinding();
		ICPPField x2 = (ICPPField) col.getName(7).resolveBinding();

		ICPPClassTemplate C = (ICPPClassTemplate) col.getName(10).resolveBinding();
		ICPPField y = (ICPPField) col.getName(13).resolveBinding();
		ICPPField z = (ICPPField) col.getName(16).resolveBinding();

		ICPPClassType C1 = (ICPPClassType) col.getName(18).resolveBinding();
		assertTrue(C1 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) C1).getTemplateDefinition(), C);

		ICPPField y2 = (ICPPField) col.getName(23).resolveBinding();
		assertTrue(y2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) y2).getSpecializedBinding(), y);
		IType t = y2.getType();
		assertTrue(t instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) t).getTemplateDefinition(), A1);
		ICPPField x3 = (ICPPField) col.getName(24).resolveBinding();
		assertTrue(x3 instanceof ICPPSpecialization);
		assertEquals(((ICPPSpecialization) x3).getSpecializedBinding(), x1);

		ICPPField z2 = (ICPPField) col.getName(26).resolveBinding();
		assertTrue(z2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) z2).getSpecializedBinding(), z);
		t = z2.getType();
		assertTrue(t instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) t).getTemplateDefinition(), A2);
		ICPPField x4 = (ICPPField) col.getName(27).resolveBinding();
		assertTrue(x4 instanceof ICPPSpecialization);
		assertEquals(((ICPPSpecialization) x4).getSpecializedBinding(), x2);
	}

	// template <class T> class A {
	//    typedef T _T;
	//   _T t;
	// };
	// void f() {
	//    A<int> a;
	//    a.t;
	// }
	public void testNestedTypeSpecializations() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ITypedef _T = (ITypedef) col.getName(3).resolveBinding();
		assertSame(_T.getType(), T);

		ICPPField t = (ICPPField) col.getName(5).resolveBinding();
		assertSame(t.getType(), _T);

		ICPPField t2 = (ICPPField) col.getName(11).resolveBinding();
		assertTrue(t2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) t2).getSpecializedBinding(), t);

		IType type = t2.getType();
		assertTrue(type instanceof ITypedef);
		assertTrue(type instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) type).getSpecializedBinding(), _T);

		type = ((ITypedef) type).getType();
		assertTrue(type instanceof IBasicType);
		assertEquals(((IBasicType) type).getType(), IBasicType.t_int);
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
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
		ICPPField t = (ICPPField) col.getName(4).resolveBinding();
		assertSame(t.getType(), T);
		ICPPField b = (ICPPField) col.getName(6).resolveBinding();
		assertSame(b.getType(), B);

		ICPPField b2 = (ICPPField) col.getName(12).resolveBinding();
		ICPPField t2 = (ICPPField) col.getName(13).resolveBinding();

		assertTrue(b2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) b2).getSpecializedBinding(), b);

		IType type = b2.getType();
		assertTrue(type instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) type).getSpecializedBinding(), B);

		assertTrue(t2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) t2).getSpecializedBinding(), t);
		assertTrue(t2.getType() instanceof IBasicType);
		assertEquals(((IBasicType) t2.getType()).getType(), IBasicType.t_int);
	}

	// template <class T> class A {
	//    typedef typename T::X _xx;
	//    _xx s;
	// };
	// class B {};
	// template < class T > class C {
	//    typedef T X;
	// };
	// void f() {
	//    A< C<B> > a; a.s;
	// };
	public void testTemplateParameterQualifiedType() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateTypeParameter T = (ICPPTemplateTypeParameter) col.getName(0).resolveBinding();
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();

		IBinding T1 = col.getName(3).resolveBinding();
		assertSame(T1, T);

		ICPPClassType X = (ICPPClassType) col.getName(4).resolveBinding();

		ITypedef _xx = (ITypedef) col.getName(5).resolveBinding();

		IBinding _xx2 = col.getName(6).resolveBinding();
		assertSame(_xx, _xx2);
		assertSame(_xx.getType(), X);

		ICPPField s = (ICPPField) col.getName(7).resolveBinding();

		ICPPClassType B = (ICPPClassType) col.getName(8).resolveBinding();
		ITypedef X2 = (ITypedef) col.getName(12).resolveBinding();

		ICPPClassType Acb = (ICPPClassType) col.getName(14).resolveBinding();
		assertTrue(Acb instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) Acb).getTemplateDefinition(), A);

		ICPPField s2 = (ICPPField) col.getName(21).resolveBinding();
		assertTrue(s2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) s2).getSpecializedBinding(), s);

		IType t = s2.getType();
		//		assertTrue(t instanceof ITypedef);
		//		assertTrue(t instanceof ICPPSpecialization);
		//		assertSame(((ICPPSpecialization) t).getSpecializedBinding(), _xx);

		t = ((ITypedef) t).getType();
		assertTrue(t instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) t).getSpecializedBinding(), X2);

		t = ((ITypedef) t).getType();
		assertSame(t, B);
	}

	// template <class T> class A {
	//    A<T> a;
	//    void f();
	// };
	// template <class U> void A<U>::f(){
	//    U u;
	// }
	public void testTemplateScopes_a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) col.getName(2).resolveBinding();

		ICPPTemplateParameter U = (ICPPTemplateParameter) col.getName(7).resolveBinding();
		assertSame(U, T);
		ICPPClassType A3 = (ICPPClassType) col.getName(9).resolveBinding();
		assertSame(A, A3);

		ICPPTemplateParameter U2 = (ICPPTemplateParameter) col.getName(13).resolveBinding();
		assertSame(U, U2);
		assertSame(T, U);
	}

	// class A {
	//    template < class T > void f(T);
	// };
	// template <class U> void A::f<>(U){}
	public void testTemplateScopes_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(1).resolveBinding();
		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(2).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(3).resolveBinding();
		assertSame(T, T2);

		ICPPTemplateParameter U = (ICPPTemplateParameter) col.getName(5).resolveBinding();
		assertSame(T, U);
		ICPPClassType A2 = (ICPPClassType) col.getName(7).resolveBinding();
		assertSame(A, A2);
		ICPPMethod f2 = (ICPPMethod) col.getName(8).resolveBinding();
		IBinding U2 = col.getName(10).resolveBinding();
		assertSame(U, U2);

		assertSame(f1, f2);
	}

	// template<typename T>
	// class A {};
	//
	// class B {};
	//
	// template<>
	// class A<B> {};
	//
	// class C {};
	//
	// A<B> ab;
	// A<C> ac;
	public void testEnclosingScopes_a() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);

		ICPPSpecialization b0 = ba.assertNonProblem("A<B>", 4, ICPPSpecialization.class, ICPPClassType.class);
		ICPPTemplateInstance b1 = ba.assertNonProblem("A<C>", 4, ICPPTemplateInstance.class, ICPPClassType.class);

		ICPPClassType sc0 = assertInstance(b0.getSpecializedBinding(), ICPPClassType.class);
		ICPPClassType sc1 = assertInstance(b1.getSpecializedBinding(), ICPPClassType.class);
		assertTrue(sc0.isSameType(sc1));

		assertInstance(b0, ICPPSpecialization.class);
		assertInstance(b1, ICPPTemplateInstance.class);

		assertInstance(b0.getScope(), ICPPTemplateScope.class);

		IScope ts0 = ((ICPPClassType) b0.getSpecializedBinding()).getScope();
		IScope ts1 = ((ICPPClassType) b1.getSpecializedBinding()).getScope();

		assertInstance(ts0, ICPPTemplateScope.class);

		assertSame(ts0, ts1);
		assertNotSame(ts0, b0.getScope());
		assertSame(ts1, b1.getScope()); // a class instance exists in the same scope as the template its defined from
	}

	// template<typename T>
	// struct A {
	//    class B {};
	// };
	//
	// class C {}; class D {};
	//
	// template<>
	// struct A<C> {
	//   class B {};
	// };
	//
	// void refs() {
	//    A<C>::B acb;
	//    A<D>::B adb;
	// }
	public void testEnclosingScopes_b() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);

		ICPPClassType b0 = ba.assertNonProblem("B acb", 1, ICPPClassType.class);
		ICPPClassType b1 = ba.assertNonProblem("B adb", 1, ICPPClassType.class, ICPPSpecialization.class);
		ICPPClassType b2 = ba.assertNonProblem("A<C>", 4, ICPPClassType.class, ICPPSpecialization.class);
		ICPPClassType b3 = ba.assertNonProblem("A {", 1, ICPPClassType.class, ICPPTemplateDefinition.class);
		ICPPClassType b4 = ba.assertNonProblem("B {}", 1, ICPPClassType.class);

		assertFalse(b0 instanceof ICPPSpecialization);

		assertSame(b0.getScope(), b2.getCompositeScope());
		ICPPClassScope cs1 = assertInstance(b1.getScope(), ICPPClassScope.class);
		assertInstance(cs1.getClassType(), ICPPTemplateInstance.class);
		assertSame(b4.getScope(), b3.getCompositeScope());
	}

	// class A {};
	//
	// template<typename T>
	// struct X {
	//    struct Y {
	//       class Z {};
	//    };
	// };
	//
	// X<A>::Y::Z xayz;
	public void testEnclosingScopes_c() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);

		ICPPClassType b0 = ba.assertNonProblem("Y::Z x", 1, ICPPClassType.class);
		ICPPClassType b1 = ba.assertNonProblem("Z xayz", 1, ICPPClassType.class);

		ICPPClassScope cs0 = assertInstance(b0.getScope(), ICPPClassScope.class);
		assertInstance(cs0.getClassType(), ICPPSpecialization.class);

		ICPPClassScope cs1 = assertInstance(b1.getScope(), ICPPClassScope.class);
		assertInstance(cs1.getClassType(), ICPPSpecialization.class);
	}

	// class A {}; class B {};
	//
	// template<typename T1, typename T2>
	// class X {};
	//
	// template<typename T3>
	// struct X<T3, A> {
	//     class N {};
	// };
	//
	// X<B,A>::N n;
	public void testEnclosingScopes_d() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);

		ICPPClassType b0 = ba.assertNonProblem("N n", 1, ICPPClassType.class);
		ICPPClassType b1 = ba.assertNonProblem("N {", 1, ICPPClassType.class);

		ICPPClassScope s0 = assertInstance(b0.getScope(), ICPPClassScope.class);
		assertInstance(s0.getClassType(), ICPPTemplateInstance.class);

		ICPPClassScope s1 = assertInstance(b1.getScope(), ICPPClassScope.class);
		assertInstance(s1.getClassType(), ICPPTemplateDefinition.class);

		ICPPTemplateScope s2 = assertInstance(s1.getClassType().getScope(), ICPPTemplateScope.class);
	}

	// template<class T> struct A {
	//    void f(T);
	//    template<class X> void g(T,X);
	//    void h(T) { }
	// };
	// template<> void A<int>::f(int);
	// template<class T> template<class X> void A<T>::g(T,X) { }
	// template<> template<class X> void A<int>::g(int,X);
	// template<> template<> void A<int>::g(int,char);
	// template<> template<> void A<int>::g<char>(int,char);
	// template<> void A<int>::h(int) { }
	public void test14_7_3s16() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(2).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(3).resolveBinding();
		assertSame(T, T2);

		ICPPTemplateParameter X = (ICPPTemplateParameter) col.getName(5).resolveBinding();
		ICPPFunctionTemplate g = (ICPPFunctionTemplate) col.getName(6).resolveBinding();
		ICPPTemplateParameter T3 = (ICPPTemplateParameter) col.getName(7).resolveBinding();
		assertSame(T, T3);
		ICPPTemplateParameter X2 = (ICPPTemplateParameter) col.getName(9).resolveBinding();
		assertSame(X, X2);

		ICPPMethod h = (ICPPMethod) col.getName(11).resolveBinding();
		ICPPTemplateParameter T4 = (ICPPTemplateParameter) col.getName(12).resolveBinding();
		assertSame(T, T4);

		ICPPClassType A2 = (ICPPClassType) col.getName(15).resolveBinding();
		assertTrue(A2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A2).getTemplateDefinition(), A);
		ICPPMethod f2 = (ICPPMethod) col.getName(17).resolveBinding();
		assertTrue(f2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) f2).getSpecializedBinding(), f);

		ICPPTemplateParameter TR = (ICPPTemplateParameter) col.getName(19).resolveBinding();
		assertSame(T, TR);
		ICPPTemplateParameter XR = (ICPPTemplateParameter) col.getName(20).resolveBinding();
		assertSame(X, XR);
		ICPPClassType A3 = (ICPPClassType) col.getName(22).resolveBinding();
		assertSame(A3, A);

		ICPPMethod g2 = (ICPPMethod) col.getName(25).resolveBinding();
		assertSame(g2, g);
		TR = (ICPPTemplateParameter) col.getName(26).resolveBinding();
		assertSame(T, TR);
		XR = (ICPPTemplateParameter) col.getName(28).resolveBinding();
		assertSame(X, XR);

		assertSame(col.getName(32).resolveBinding(), A2);
		assertSame(col.getName(39).resolveBinding(), A2);
		assertSame(col.getName(45).resolveBinding(), A2);
		assertSame(col.getName(52).resolveBinding(), A2);

		ICPPMethod h2 = (ICPPMethod) col.getName(54).resolveBinding();
		assertTrue(h2 instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) h2).getSpecializedBinding(), h);
	}

	// namespace N {
	//    int C;
	//    template<class T> class B {
	//       void f(T);
	//    };
	// }
	// template<class C> void N::B<C>::f(C) {
	//    C b; // C is the template parameter, not N::C
	// }
	public void test14_6_1s6() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(2).resolveBinding();
		ICPPClassTemplate B = (ICPPClassTemplate) col.getName(3).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(4).resolveBinding();
		ICPPTemplateParameter TR = (ICPPTemplateParameter) col.getName(5).resolveBinding();
		assertSame(T, TR);

		ICPPTemplateParameter C = (ICPPTemplateParameter) col.getName(7).resolveBinding();
		assertSame(C, T);

		ICPPClassType B2 = (ICPPClassType) col.getName(10).resolveBinding();
		assertSame(B2, B);

		ICPPTemplateParameter CR = (ICPPTemplateParameter) col.getName(12).resolveBinding();
		assertSame(CR, T);

		ICPPMethod f2 = (ICPPMethod) col.getName(13).resolveBinding();
		assertSame(f2, f);

		CR = (ICPPTemplateParameter) col.getName(14).resolveBinding();
		assertSame(CR, T);

		CR = (ICPPTemplateParameter) col.getName(16).resolveBinding();
		assertSame(CR, T);
	}

	// template <class T> class Array {};
	// template <class T> void sort(Array<T> &);
	// template void sort<>(Array<int> &);
	public void testBug90689_ExplicitInstantiation() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPFunctionTemplate s = (ICPPFunctionTemplate) col.getName(3).resolveBinding();

		ICPPClassType A2 = (ICPPClassType) col.getName(4).resolveBinding();
		assertTrue(A2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A2).getTemplateDefinition(), A);

		ICPPFunction s2 = (ICPPFunction) col.getName(8).resolveBinding();
		assertTrue(s2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) s2).getTemplateDefinition(), s);

		ICPPClassType A3 = (ICPPClassType) col.getName(10).resolveBinding();
		assertTrue(A3 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A3).getTemplateDefinition(), A);
		assertNotSame(A2, A3);
	}

	// template<class T> class Array { };
	// template class Array<char>;
	// template<class T> void sort(Array<T>& v) {  }
	// template void sort(Array<char>&); // argument is deduced here
	public void test14_7_2s2_ExplicitInstantiation() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) col.getName(2).resolveBinding();
		assertTrue(A2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A2).getTemplateDefinition(), A1);

		ICPPFunctionTemplate s1 = (ICPPFunctionTemplate) col.getName(5).resolveBinding();
		ICPPFunction s2 = (ICPPFunction) col.getName(10).resolveBinding();
		assertTrue(s2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) s2).getTemplateDefinition(), s1);

		ICPPClassType A3 = (ICPPClassType) col.getName(11).resolveBinding();
		assertSame(A2, A3);
	}

	// template <class T> class A {
	//    A<T>* p;
	//    void f() { this; }
	// };
	public void testBug74204() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IField p = (IField) col.getName(5).resolveBinding();

		IASTName f = col.getName(6);
		IASTFunctionDefinition fdef = (IASTFunctionDefinition) f.getParent().getParent();
		IASTExpressionStatement statement = (IASTExpressionStatement) ((IASTCompoundStatement) fdef.getBody())
				.getStatements()[0];
		IType type = statement.getExpression().getExpressionType();

		assertTrue(type.isSameType(p.getType()));
	}

	// template <class T > void f(T);
	// template <class T > void g(T t){
	//    f(t);
	// }
	public void testDeferredFunctionTemplates() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunction f2 = (ICPPFunction) col.getName(8).resolveBinding();
		assertTrue(f2 instanceof ICPPUnknownBinding);
	}

	// template < class T > class A {};
	// template < class T > class B {
	//    void init(A<T> *);
	// };
	// template < class T > class C : public B<T> {
	//    C(A<T> * a) {
	//       init(a);
	//    }
	// };
	public void testRelaxationForTemplateInheritance() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPMethod init = (ICPPMethod) col.getName(4).resolveBinding();
		// the instantiation of B<T> has to be deferred, therefore 'init' is an unknown binding.
		assertInstance(col.getName(19).resolveBinding(), ICPPUnknownBinding.class);
	}

	// template <class Tp, class Tr > class iter {
	//    Tp operator -> () const;
	//    Tr operator [] (int) const;
	// };
	// template <class T> class list {
	//    typedef iter< T*, T& > iterator;
	//    iterator begin();
	//    iterator end();
	// };
	// struct Bar { int foo; };
	// void f() {
	//    list<Bar> bar;
	//    for(list<Bar>::iterator i = bar.begin(); i != bar.end(); ++i){
	//       i->foo;  i[0].foo;
	//    }
	// }
	public void testBug91707() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPMethod begin = (ICPPMethod) col.getName(16).resolveBinding();
		ICPPMethod end = (ICPPMethod) col.getName(18).resolveBinding();

		ICPPField foo = (ICPPField) col.getName(20).resolveBinding();

		IBinding r = col.getName(33).resolveBinding();
		assertTrue(r instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) r).getSpecializedBinding(), begin);

		r = col.getName(36).resolveBinding();
		assertTrue(r instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) r).getSpecializedBinding(), end);

		assertSame(foo, col.getName(39).resolveBinding());
		assertSame(foo, col.getName(41).resolveBinding());
	}

	// class B { int i; };
	// template <class T > class A {
	//    typedef T* _T;
	// };
	// void f(){
	//    A<B>::_T t;
	//    (*t).i;
	// }
	public void testBug98961() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType B = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPField i = (ICPPField) col.getName(1).resolveBinding();
		ITypedef _T = (ITypedef) col.getName(5).resolveBinding();
		ICPPVariable t = (ICPPVariable) col.getName(12).resolveBinding();

		IType type = t.getType();
		assertTrue(type instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) type).getSpecializedBinding(), _T);
		assertSame(((IPointerType) ((ITypedef) type).getType()).getType(), B);
		assertSame(i, col.getName(14).resolveBinding());
	}

	// class A {
	//    template <class T > void f(T) {
	//       begin();
	//    }
	//    void begin();
	// };
	public void testBug98784() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertSame(col.getName(5).resolveBinding(), col.getName(6).resolveBinding());
	}

	// template <class T> class A {
	//    A(T t);
	// };
	// void f(A<int> a);
	// void m(){
	//    f(A<int>(1));
	// }
	public void testBug99254a() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPConstructor ctor = bh.assertNonProblem("A(T t)", "A", ICPPConstructor.class);
		ICPPSpecialization spec = bh.assertNonProblem("A<int>(1)", "A<int>", ICPPSpecialization.class);
		assertSame(ctor.getOwner(), spec.getSpecializedBinding());
		IASTName name = bh.findName("A<int>(1)", "A<int>");
		IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) name.getParent().getParent()).getImplicitNames();
		assertEquals(1, implicitNames.length);
		ICPPSpecialization ctor2 = (ICPPSpecialization) implicitNames[0].getBinding();
		assertSame(ctor, ctor2.getSpecializedBinding());
		ICPPFunction f = bh.assertNonProblem("f(A<int> a)", "f", ICPPFunction.class);
		ICPPFunction f2 = bh.assertNonProblem("f(A<int>(1))", "f", ICPPFunction.class);
		assertSame(f, f2);
	}

	// namespace core {
	//    template<class T> class A {
	//       A(T x, T y);
	//    };
	// }
	// class B {
	//    int add(const core::A<int>& rect);
	// };
	// void f(B* b){
	//    b->add(core::A<int>(10, 2));
	// }
	public void testBug99254b() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPConstructor ctor = bh.assertNonProblem("A(T x, T y)", "A", ICPPConstructor.class);
		ICPPSpecialization spec = bh.assertNonProblem("A<int>(10, 2)", "A<int>", ICPPSpecialization.class);
		assertSame(ctor.getOwner(), spec.getSpecializedBinding());
		IASTName name = bh.findName("A<int>(10, 2)", "A<int>");
		IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) name.getParent().getParent().getParent())
				.getImplicitNames();
		assertEquals(1, implicitNames.length);
		ICPPSpecialization ctor2 = (ICPPSpecialization) implicitNames[0].getBinding();
		assertSame(ctor, ctor2.getSpecializedBinding());
		ICPPMethod add = bh.assertNonProblem("add(const core::A<int>& rect)", "add", ICPPMethod.class);
		ICPPMethod add2 = bh.assertNonProblem("b->add(core::A<int>(10, 2))", "add", ICPPMethod.class);
		assertSame(add, add2);
	}

	// template <class T> class A { A(T); };
	// typedef signed int s32;
	// class B {
	//    int add(const A<s32>& rect);
	// };
	// void f(B* b){
	//    b->add(A<int>(10));
	// }
	public void testBug99254c() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPConstructor ctor = bh.assertNonProblem("A(T)", "A", ICPPConstructor.class);
		ICPPSpecialization spec = bh.assertNonProblem("A<int>(10)", "A<int>", ICPPSpecialization.class);
		assertSame(ctor.getOwner(), spec.getSpecializedBinding());
		IASTName name = bh.findName("A<int>(10)", "A<int>");
		IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) name.getParent().getParent()).getImplicitNames();
		assertEquals(1, implicitNames.length);
		ICPPSpecialization ctor2 = (ICPPSpecialization) implicitNames[0].getBinding();
		assertSame(ctor, ctor2.getSpecializedBinding());
		ICPPMethod add = bh.assertNonProblem("add(const A<s32>& rect)", "add", ICPPMethod.class);
		ICPPMethod add2 = bh.assertNonProblem("b->add(A<int>(10))", "add", ICPPMethod.class);
		assertSame(add, add2);
	}

	public void testBug98666() throws Exception {
		CPPASTNameBase.sAllowNameComputation = true;
		IASTTranslationUnit tu = parse("A::template B<T> b;", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPASTQualifiedName qn = (ICPPASTQualifiedName) col.getName(0);
		IASTName lastName = qn.getLastName();
		assertTrue(lastName instanceof ICPPASTTemplateId);
		assertEquals(lastName.toString(), "B<T>");
	}

	// template <class T> struct A{
	//    class C {
	//       template <class T2> struct B {};
	//    };
	// };
	// template <class T> template <class T2>
	// struct A<T>::C::B<T2*>{};
	// A<short>::C::B<int*> ab;
	public void testBug90678() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(3).resolveBinding();

		ICPPClassTemplate B = (ICPPClassTemplate) col.getName(4).resolveBinding();

		assertSame(T, col.getName(5).resolveBinding());
		final IBinding T2ofPartialSpec = col.getName(6).resolveBinding();
		assertNotSame(T2, T2ofPartialSpec); // partial spec has its own template params
		assertSame(T, col.getName(10).resolveBinding());
		assertSame(T2ofPartialSpec, col.getName(14).resolveBinding());

		ICPPClassTemplatePartialSpecialization spec = (ICPPClassTemplatePartialSpecialization) col.getName(12)
				.resolveBinding();
		assertSame(spec.getPrimaryClassTemplate(), B);

		ICPPClassType BI = (ICPPClassType) col.getName(19).resolveBinding();
		assertTrue(BI instanceof ICPPTemplateInstance);
		final IBinding partialSpecSpec = ((ICPPTemplateInstance) BI).getSpecializedBinding();
		assertTrue(partialSpecSpec instanceof ICPPSpecialization);
		IBinding partialSpec = ((ICPPSpecialization) partialSpecSpec).getSpecializedBinding();
		assertSame(partialSpec, spec);
	}

	// template <class T> int f(T); // #1
	// int f(int);                  // #2
	// int k = f(1);           // uses #2
	// int l = f<>(1);         // uses #1
	public void testBug95208() throws Exception {
		String content = getAboveComment();
		IASTTranslationUnit tu = parse(content, CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunction f2 = (ICPPFunction) col.getName(4).resolveBinding();

		assertSame(f2, col.getName(7).resolveBinding());

		IBinding b = col.getName(9).resolveBinding(); // resolve the binding of the ICPPASTTemplateId first
		assertTrue(b instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) b).getSpecializedBinding(), f1);
		assertSame(f1, col.getName(10).resolveBinding());

		tu = parse(content, CPP);
		col = new NameCollector();
		tu.accept(col);

		f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		assertSame(f1, col.getName(10).resolveBinding());
	}

	// template <class T, int someConst = 0 >  class A {};
	// int f() {
	//    const int local = 10;
	//    A<int, local> broken;
	// };
	public void testBug103578() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(2).resolveBinding();
		IVariable local = (IVariable) col.getName(4).resolveBinding();

		ICPPClassType a = (ICPPClassType) col.getName(5).resolveBinding();
		assertTrue(a instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) a).getTemplateDefinition(), A);
		assertSame(local, col.getName(7).resolveBinding());
	}

	// template <class T> class A : public T {};
	// class B { int base; };
	// void f() {
	//    A< B > a;
	//    a.base;
	// }
	public void testBug103715() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField base = (ICPPField) col.getName(4).resolveBinding();
		assertSame(base, col.getName(11).resolveBinding());

		ICPPClassType B = (ICPPClassType) col.getName(3).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(6).resolveBinding();

		ICPPBase[] bases = A.getBases();
		assertEquals(bases.length, 1);
		assertSame(bases[0].getBaseClass(), B);
	}

	// template < class T > class complex;
	// template <> class complex <float>;
	// template < class T > class complex{
	// };
	// template <> class complex< float > {
	//    void f(float);
	// };
	// void complex<float>::f(float){
	// }
	public void testBug74276() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassTemplate complex = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPClassType cspec = (ICPPClassType) col.getName(2).resolveBinding();
		assertTrue(cspec instanceof ICPPSpecialization);
		assertSame(((ICPPSpecialization) cspec).getSpecializedBinding(), complex);

		assertSame(complex, col.getName(5).resolveBinding());
		assertSame(cspec, col.getName(6).resolveBinding());

		ICPPMethod f = (ICPPMethod) col.getName(8).resolveBinding();
		assertSame(f, col.getName(10).resolveBinding());
	}

	// template< class T1, int q > class C {};
	// template< class T1, class T2> class A {};
	// template< class T1, class T2, int q1, int q2>
	// class A< C<T1, q1>, C<T2, q2> > {};
	// class N {};
	// typedef A<C<N,1>, C<N,1> > myType;
	// void m(){
	//    myType t;
	// }
	public void testBug105852() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ITypedef myType = (ITypedef) col.getName(31).resolveBinding();
		ICPPClassType A = (ICPPClassType) myType.getType();

		ICPPClassTemplatePartialSpecialization Aspec = (ICPPClassTemplatePartialSpecialization) col.getName(10)
				.resolveBinding();

		assertTrue(A instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) A).getTemplateDefinition(), Aspec);
	}

	//	template<class T>
	//	class A : public T {};
	//	class C { public: int c; };
	//	class B : public A<C> { };
	//	void main() {
	//    B k;
	//    k.c;
	//	}
	public void testBug105769() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPVariable c = (ICPPVariable) col.getName(13).resolveBinding();
		assertSame(c, col.getName(4).resolveBinding());
	}

	//	template<class T>
	//	class C {
	//    public: void * blah;
	//    template<typename G> C(G* g) : blah(g) {}
	//    template <> C(char * c) : blah(c) {}
	//    template <> C(wchar_t * c) : blah(c) {}
	//	};
	public void testBug162230() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPClassTemplate C = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPField blah = (ICPPField) col.getName(2).resolveBinding();
		ICPPTemplateTypeParameter G = (ICPPTemplateTypeParameter) col.getName(3).resolveBinding();
		ICPPFunctionTemplate ctor = (ICPPFunctionTemplate) col.getName(4).resolveBinding();

		assertSame(G, col.getName(5).resolveBinding());
		ICPPParameter g = (ICPPParameter) col.getName(6).resolveBinding();
		assertSame(blah, col.getName(7).resolveBinding());
		assertSame(g, col.getName(8).resolveBinding());

		ICPPSpecialization spec = (ICPPSpecialization) col.getName(9).resolveBinding();
		assertSame(spec.getSpecializedBinding(), ctor);

		ICPPParameter c = (ICPPParameter) col.getName(10).resolveBinding();

		assertSame(blah, col.getName(11).resolveBinding());
		assertSame(c, col.getName(12).resolveBinding());

		ICPPSpecialization spec2 = (ICPPSpecialization) col.getName(13).resolveBinding();
		assertSame(spec.getSpecializedBinding(), ctor);

		ICPPParameter c2 = (ICPPParameter) col.getName(14).resolveBinding();

		assertSame(blah, col.getName(15).resolveBinding());
		assertSame(c2, col.getName(16).resolveBinding());
	}

	//	template< class T > class C {};
	//	typedef struct C<int> CInt;
	public void testBug169628() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertTrue(col.getName(2).resolveBinding() instanceof ICPPSpecialization);
	}

	// template<class T1>
	// struct Closure {
	//   Closure(T1* obj1, void (T1::*method1)()) {}
	// };
	//
	// template<class T2>
	// Closure<T2>* makeClosure(T2* obj2, void (T2::*method2)()) {
	//   return new Closure<T2>(obj2, method2);
	// }
	//
	// struct A {
	//   void m1() {}
	//   void m2() {
	//     makeClosure(this, &A::m1);
	//   }
	// };
	public void testBug201204() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPFunction fn = bh.assertNonProblem("makeClosure(this", 11, ICPPFunction.class);
	}

	// template <class R, class T, class P1, class P2, class P3, class P4>
	// class A {};
	//
	// template <class R, class T, class P1, class P2, class P3, class P4>
	// A<R, T, P1, P2, P3, P4>* func(const T* obj, R (T::*m)(P1, P2, P3, P4) const);
	//
	// template <class R, class T, class P1, class P2, class P3, class P4>
	// class B {};
	//
	// template <class R, class T, class P1, class P2, class P3, class P4>
	// B<R, T, P1, P2, P3, P4>* func(T* obj, R (T::*m)(P1, P2, P3, P4));
	//
	// struct C {
	//	 int m1(int a1, int a2, int a3, int a4);
	//	 int m2(int a1, int a2, int a3, int a4) const;
	// };
	//
	// void f(C* c, const C* d) {
	//	 func(c, &C::m1);
	//	 func(d, &C::m2);
	// }
	public void testBug233889() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPFunction fn1 = bh.assertNonProblem("func(c", 4, ICPPFunction.class);
		ICPPFunction fn2 = bh.assertNonProblem("func(d", 4, ICPPFunction.class);
		assertNotSame(fn1, fn2);
	}

	// template<class _T1, class _T2>
	// struct pair {
	//   typedef _T1 first_type;
	// };
	//
	// template <typename _Key, typename _Tp>
	// struct map {
	//   typedef pair<_Key, _Tp> value_type;
	// };
	//
	// template <class _C>
	// typename _C::value_type GetPair(_C& collection, typename _C::value_type::first_type key);
	//
	// int main(map<int, int> x) {
	//   GetPair(x, 1);
	// }
	public void testBug229917a() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPFunction fn = bh.assertNonProblem("GetPair(x", 7, ICPPFunction.class);
	}

	// template<class _T1, class _T2>
	// struct pair {
	//   typedef _T1 first_type;
	// };
	//
	// template <typename _Key, typename _Tp>
	// struct map {
	//   typedef pair<_Key, _Tp> value_type;
	// };
	//
	// template <class _C>
	// typename _C::value_type GetPair(_C& collection, typename _C::value_type::first_type key);
	public void testBug229917b() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		IBinding b0 = bh.assertNonProblem("value_type GetPair", 10, IBinding.class);
	}

	// template<typename _T1>
	// class A {};
	//
	// template<typename _T2, template<typename> class _Base = A>
	// struct B {
	//   const _T2* m() const { return 0; }
	// };
	//
	// template<typename _T3>
	// class C : public B<_T3> {};
	//
	// void f(C<char>& str) {
	//   str.m();
	// }
	public void testBug232086() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPFunction b0 = bh.assertNonProblem("m();", 1, ICPPFunction.class);
	}

	//    class A {};
	//
	//    template <class T> class C {
	//    public:
	//    	inline C(T& aRef) {}
	//    	inline operator T&() {}
	//    };
	//
	//    void foo(A a) {}
	//    void bar(C<const A> ca) {}
	//
	//    void main2() {
	//    	const A a= *new A();
	//    	const C<const A> ca= *new C<const A>(*new A());
	//
	//    	foo(a);
	//    	bar(ca);
	//    }
	public void testBug214646() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);

		IBinding b0 = bh.assertNonProblem("foo(a)", 3);
		IBinding b1 = bh.assertNonProblem("bar(ca)", 3);

		assertInstance(b0, ICPPFunction.class);
		assertInstance(b1, ICPPFunction.class);

		ICPPFunction f0 = (ICPPFunction) b0, f1 = (ICPPFunction) b1;
		assertEquals(1, f0.getParameters().length);
		assertEquals(1, f1.getParameters().length);

		assertInstance(f0.getParameters()[0].getType(), ICPPClassType.class);
		assertFalse(f0 instanceof ICPPTemplateInstance);
		assertFalse(f0 instanceof ICPPTemplateDefinition);
		assertInstance(f1.getParameters()[0].getType(), ICPPClassType.class);
		assertInstance(f1.getParameters()[0].getType(), ICPPTemplateInstance.class);
	}

	//	struct A {};
	//
	//	template <class T1>
	//	void func(const T1& p) {
	//	}
	//
	//	void test() {
	//	  A a1;
	//	  const A a2;
	//	  func(a1);
	//	  func(a2);
	//	}
	public void testFunctionTemplate_245049a() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPFunction b0 = bh.assertNonProblem("func(a1)", 4, ICPPFunction.class);
		assertInstance(b0, ICPPTemplateInstance.class);
		ICPPFunction b1 = bh.assertNonProblem("func(a2)", 4, ICPPFunction.class);
		assertSame(b0, b1);
	}

	//	struct A {};
	//
	//	template <class T1>
	//	void func(const T1& p) {
	//	}
	//	template <class T2>
	//	void func(T2& p) {
	//	}
	//
	//	void test() {
	//	  A a1;
	//	  const A a2;
	//	  func(a1);
	//	  func(a2);
	//	}
	public void testFunctionTemplate_245049b() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPFunction b0 = bh.assertNonProblem("func(a1)", 4, ICPPFunction.class);
		assertInstance(b0, ICPPTemplateInstance.class);
		ICPPFunction b1 = bh.assertNonProblem("func(a2)", 4, ICPPFunction.class);
		assertNotSame(b0, b1);
	}

	// namespace ns {
	//
	// template<class _M1, class _M2>
	// struct pair {
	//   pair(const _M1& _a, const _M2& _b) {}
	// };
	//
	// template<class _T1, class _T2>
	// pair<_T1, _T2> make_pair(_T1 _x, _T2 _y) { return pair<_T1, _T2>(_x, _y); }
	//
	// }
	//
	// using ns::pair;
	// using ns::make_pair;
	// pair<int, int> p = make_pair(1, 2);
	public void testFunctionTemplateWithUsing() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertNonProblem("make_pair(1", 9, ICPPFunction.class);
	}

	// template < class T > void f (T);
	// void main() {
	//    f(1);
	// }
	public void testFunctionTemplateImplicitInstantiation() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		IFunction f2 = (IFunction) col.getName(5).resolveBinding();

		assertTrue(f2 instanceof ICPPTemplateInstance);
		assertSame(((ICPPTemplateInstance) f2).getTemplateDefinition(), f1);
	}

	//	template <class T>
	//	int waldo(T (*function)());
	//
	//	template <class T, class U>
	//	int waldo(T (*function)(U));
	//
	//	void test() {
	//	  waldo(+[]() {});
	//	}
	public void testFunctionTemplateWithLambdaArgument_443361() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class T>
	//	struct A {};
	//
	//	template<class T>
	//	A<T> a(T p);
	//
	//	void f(A<int> p);
	//
	//	typedef int& B;
	//
	//	void test(B x) {
	//	  f(a(x));
	//	}
	public void testFunctionTemplate_264963() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertNonProblem("f(a(x));", 1, ICPPFunction.class);
	}

	//	template <class T, class P>
	//	void f(void (T::*member)(P));
	//
	//	struct A {
	//	  void m(int& p);
	//	};
	//
	//	void test() {
	//	  f(&A::m);
	//	}
	public void testFunctionTemplate_266532() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertNonProblem("f(&A::m);", 1, ICPPFunction.class);
	}

	//	template<typename T, typename U = int>
	//	class A {};
	//
	//	template <typename P>
	//	void f(A<P> p);
	//
	//	class B {};
	//
	//	void test(A<B> p) {
	//	  f(p);
	//	}
	public void testFunctionTemplate_272848a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename S>
	//	class B {};
	//
	//	template <typename T, typename U = B<T> >
	//	class A {};
	//
	//	template <typename P>
	//	void f(A<P*> p);
	//
	//	void test(A<int*> p) {
	//	  f(p);
	//	}
	public void testFunctionTemplate_272848b() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T, typename U>
	//	T f(U* f) {}
	//
	//	template<typename T, typename U>
	//	T f(U& f) {}
	//
	//	void test(int* x) {
	//	  f<int>(x);
	//	}
	public void testFunctionTemplate_309564() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class U> void f1(void(*f)(const U&)) {}
	//	void f2(const int& b){}
	//	void test() {
	//	  f1(&f2);
	//	}
	public void testSimplifiedFunctionTemplateWithFunctionPointer_281783() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class T>
	//	class A {};
	//
	//	class B {};
	//
	//	template <class U>
	//	void f1(const A<U>& a, void (*f)(const U&));
	//
	//	void f2(const B& b);
	//
	//	void test(A<B> x) {
	//	  f1(x, &f2);
	//	}
	public void testFunctionTemplateWithFunctionPointer_281783() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertNonProblem("f1(x, &f2);", 2, ICPPFunction.class);
	}

	// // Brian W.'s example from bugzilla#167098
	//    template<class K>
	//    class D { //CPPClassTemplate
	//    public:
	//            template<class T, class X>
	//            D(T t, X x) {} // CPPConstructorTemplate
	//
	//            template<class T, class X>
	//            void foo(T t, X x) {} // CPPMethodTemplate
	//    };
	//
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
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);

		NameCollector col = new NameCollector(true);
		tu.accept(col);

		IASTImplicitName tid = (IASTImplicitName) col.getName(20);
		IASTName cn = col.getName(22);
		assertInstance(cn.resolveBinding(), ICPPClassTemplate.class); // *D*<int>(5, 6)
		assertInstance(cn.resolveBinding(), ICPPClassType.class); // *D*<int>(5, 6)
		assertInstance(tid.resolveBinding(), ICPPTemplateInstance.class); // *D<int>*(5, 6)
		assertInstance(tid.resolveBinding(), ICPPConstructor.class); // *D<int>*(5, 6)

		IBinding tidSpc = ((ICPPTemplateInstance) tid.resolveBinding()).getSpecializedBinding();
		assertInstance(tidSpc, ICPPConstructor.class);
		assertInstance(tidSpc, ICPPSpecialization.class);
		assertInstance(tidSpc, ICPPFunctionTemplate.class);
	}

	// template<class T> const T& (max)(const T& lhs, const T& rhs) {
	//    return (lhs < rhs ? rhs : lhs);
	// }
	public void testNestedFuncTemplatedDeclarator_bug190241() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);

		NameCollector col = new NameCollector();
		tu.accept(col);

		IASTName name;
		for (Object element : col.nameList) {
			name = (IASTName) element;
			assertFalse(name.resolveBinding() instanceof IProblemBinding);
		}

		name = col.nameList.get(0);
		assertTrue(name.resolveBinding() instanceof ICPPTemplateParameter);
		name = col.nameList.get(1);
		assertTrue(name.resolveBinding() instanceof ICPPTemplateParameter);
		name = col.nameList.get(2);
		assertTrue(name.resolveBinding() instanceof ICPPFunction);
		name = col.nameList.get(3);
		assertTrue(name.resolveBinding() instanceof ICPPTemplateParameter);
		name = col.nameList.get(4);
		assertTrue(name.resolveBinding() instanceof IParameter);
		name = col.nameList.get(5);
		assertTrue(name.resolveBinding() instanceof ICPPTemplateParameter);
		name = col.nameList.get(6);
		assertTrue(name.resolveBinding() instanceof IParameter);
		name = col.nameList.get(7);
		assertTrue(name.resolveBinding() instanceof IParameter);
		name = col.nameList.get(8);
		assertTrue(name.resolveBinding() instanceof IParameter);
		name = col.nameList.get(9);
		assertTrue(name.resolveBinding() instanceof IParameter);
		name = col.nameList.get(10);
		assertTrue(name.resolveBinding() instanceof IParameter);
	}

	// template<typename TpA>
	// class A {
	// public:
	//   typedef TpA ta;
	// };
	//
	// template<typename TpB>
	// class B {
	// public:
	//   typedef typename A<TpB>::ta tb;
	// };
	//
	// void f(B<int>::tb r) {}
	public void testTemplateTypedef_214447() throws Exception {
		CPPASTNameBase.sAllowNameComputation = true;
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);

		NameCollector col = new NameCollector();
		tu.accept(col);
		for (IASTName name : col.nameList) {
			if ("r".equals(String.valueOf(name))) {
				IBinding b0 = name.resolveBinding();
				IType type = ((ICPPVariable) b0).getType();
				type = getUltimateType(type, false);
				assertInstance(type, IBasicType.class);
				assertEquals("int", ASTTypeUtil.getType(type));
			}
		}
	}

	// template<typename _TpAllocator>
	// class Allocator {
	// public:
	//   typedef _TpAllocator& alloc_reference;
	//   template<typename _TpRebind>
	//   struct rebind {
	//     typedef Allocator<_TpRebind> other;
	//   };
	// };
	//
	// template<typename _Tp, typename _Alloc = Allocator<_Tp> >
	// class Vec {
	// public:
	//   typedef typename _Alloc::template rebind<_Tp>::other::alloc_reference reference;
	// };
	//
	// void f(Vec<int>::reference r) {}
	public void testRebindPattern_214447a() throws Exception {
		CPPASTNameBase.sAllowNameComputation = true;
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);

		NameCollector col = new NameCollector();
		tu.accept(col);
		for (IASTName name : col.nameList) {
			if ("r".equals(String.valueOf(name))) {
				IBinding b0 = name.resolveBinding();
				IType type = ((ICPPVariable) b0).getType();
				type = getUltimateType(type, false);
				assertInstance(type, IBasicType.class);
				assertEquals("int", ASTTypeUtil.getType(type));
			}
		}
	}

	// template<typename _TpAllocator>
	// class Allocator {
	// public:
	//   typedef _TpAllocator& alloc_reference;
	//   template<typename _TpRebind>
	//   struct rebind {
	//     typedef Allocator<_TpRebind> other;
	//   };
	// };
	//
	// template<typename _TpBase, typename _AllocBase>
	// class VecBase {
	// public:
	//   typedef typename _AllocBase::template rebind<_TpBase>::other _Tp_alloc_type;
	// };
	//
	// template<typename _Tp, typename _Alloc = Allocator<_Tp> >
	// class Vec : protected VecBase<_Tp, _Alloc> {
	// public:
	//   typedef typename VecBase<_Tp, _Alloc>::_Tp_alloc_type::alloc_reference reference;
	// };
	//
	// void f(Vec<int>::reference r) {}
	public void testRebindPattern_214447b() throws Exception {
		CPPASTNameBase.sAllowNameComputation = true;
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);

		NameCollector col = new NameCollector();
		tu.accept(col);
		for (IASTName name : col.nameList) {
			if ("r".equals(String.valueOf(name))) {
				IBinding b0 = name.resolveBinding();
				IType type = ((ICPPVariable) b0).getType();
				type = getUltimateType(type, false);
				assertInstance(type, IBasicType.class);
				assertEquals("int", ASTTypeUtil.getType(type));
			}
		}
	}

	// template<typename _Tp>
	// struct allocator {
	//   template<typename _Tp1>
	//   struct rebind {
	//     typedef allocator<_Tp1> other;
	//   };
	// };
	//
	// template<typename _Val1, typename _Alloc1 = allocator<_Val1> >
	// struct _Rb_tree {
	//   typedef _Val1 value_type1;
	// };
	//
	// template <typename _Val2, typename _Alloc2 = allocator<_Val2> >
	// struct map {
	//   typedef _Val2 value_type2;
	//   typedef typename _Alloc2::template rebind<value_type2>::other _Val_alloc_type;
	//   typedef _Rb_tree<_Val2, _Val_alloc_type> _Rep_type;
	//   typedef typename _Rep_type::value_type1 value_type;
	// };
	//
	// void f(map<int>::value_type r) {}
	public void testRebindPattern_236197() throws Exception {
		CPPASTNameBase.sAllowNameComputation = true;
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);
		for (IASTName name : col.nameList) {
			if ("r".equals(String.valueOf(name))) {
				IBinding b0 = name.resolveBinding();
				IType type = ((ICPPVariable) b0).getType();
				type = getUltimateType(type, false);
				assertInstance(type, IBasicType.class);
				assertEquals("int", ASTTypeUtil.getType(type));
			}
		}
	}

	// template<typename _Iterator>
	// struct IterTraits {
	//   typedef typename _Iterator::iter_reference traits_reference;
	// };
	//
	// template<typename _Tp>
	// struct IterTraits<_Tp*> {
	//   typedef _Tp& traits_reference;
	// };
	//
	// template<typename _Pointer>
	// struct Iter {
	//   typedef typename IterTraits<_Pointer>::traits_reference iter_reference;
	// };
	//
	// void main(Iter<int*>::iter_reference r);
	public void testSpecializationSelection_229218() throws Exception {
		CPPASTNameBase.sAllowNameComputation = true;
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);
		for (IASTName name : col.nameList) {
			if ("r".equals(String.valueOf(name))) {
				IBinding b0 = name.resolveBinding();
				IType type = ((ICPPVariable) b0).getType();
				type = getUltimateType(type, false);
				assertInstance(type, IBasicType.class);
				assertEquals("int", ASTTypeUtil.getType(type));
			}
		}
	}

	//	template<typename T>
	//	struct A { typedef char type; };
	//
	//	template <typename T, typename U>
	//	struct B { typedef int type; };
	//
	//	template <typename T>
	//	struct B<T, typename A<T>::type> {};
	//
	//	typename B<A<int>, int>::type a;
	public void testSpecializationSelection_509255a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {
	//	  typedef T type;
	//	};
	//
	//	template <typename T>
	//	void waldo(T t, typename T::type u);
	//
	//	void test() {
	//	  A<char> a;
	//	  waldo(a, 1);
	//	}
	public void testSpecializationSelection_509255b() throws Exception {
		parseAndCheckBindings();
	}

	//	struct true_type {
	//	  static constexpr bool v = true;
	//	};
	//
	//	struct false_type {
	//	  static constexpr bool v = false;
	//	};
	//
	//	template<typename T>
	//	T d();
	//
	//	template<typename T, typename U>
	//	struct E : public true_type {};
	//
	//	template<bool, typename T = void>
	//	struct enable_if {};
	//
	//	template<typename T>
	//	struct enable_if<true, T> {
	//	  typedef T type;
	//	};
	//
	//	template <typename T>
	//	struct D {
	//	  template <typename F>
	//	  struct S;
	//
	//	  template <typename R, typename U>
	//	  struct S<R(U::*)()> {
	//	    using type = R();
	//	  };
	//
	//	  using type = typename S<decltype(&T::m)>::type;
	//	};
	//
	//	template <typename F, typename S, typename T = void>
	//	struct C : false_type {};
	//
	//	template <typename F, typename R, typename... U>
	//	struct C<
	//	    F, R(U...),
	//	    typename enable_if<E<decltype(d<F>()(d<U>()...)), R>::v>::type>
	//	    : true_type {};
	//
	//	template <typename F, typename S>
	//	constexpr bool g() { return C<F, S>::v; }
	//
	//	template <typename F>
	//	struct B {
	//	  template <
	//	  typename T,
	//	  typename = typename enable_if<g<F, typename D<T>::type>()>::type>
	//	  operator T*();
	//	};
	//
	//	template <typename F>
	//	B<F> f(F p);
	//
	//	struct A {
	//	  void m();
	//	};
	//
	//	void waldo(A* a);
	//
	//	void test() {
	//	  waldo(f([]() {}));
	//	}
	public void testSpecializationSelection_509255c() throws Exception {
		parseAndCheckBindings();
	}

	// template<typename _Tp>
	// class A {
	// public:
	//   typedef _Tp a;
	// };
	//
	// template<typename _Tp1, typename _Tp2 = A<_Tp1> >
	// class B {
	// public:
	//   typedef _Tp2 b;
	// };
	//
	// B<int>::b::a x;
	public void testDefaultTemplateParameter() throws Exception {
		CPPASTNameBase.sAllowNameComputation = true;
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);

		NameCollector col = new NameCollector();
		tu.accept(col);
		for (IASTName name : col.nameList) {
			if ("x".equals(String.valueOf(name))) {
				IBinding b0 = name.resolveBinding();
				IType type = ((ICPPVariable) b0).getType();
				type = getUltimateType(type, false);
				assertInstance(type, IBasicType.class);
				assertEquals("int", ASTTypeUtil.getType(type));
			}
		}
	}

	//	template<class T, class U> class A;
	//  template<class T, int U> class AI;
	//  template<class T, template<typename V1, typename V2> class U> class AT;
	//
	//	class B {};
	//
	//	template<class T, class U = B> class A {};
	//  template<class T, int U=1> class AI {};
	//  template<class T, template<typename V1, typename V2> class U=A> class AT {};
	//
	//	A<char> x;
	//  AI<char> y;
	//  AT<char> z;
	public void testDefaultTemplateParameter_281781() throws Exception {
		parseAndCheckBindings();
	}

	//    class A {};
	//    class B {};
	//    template<typename T>
	//    class C {
	//    public:
	//    	T t;
	//    	operator B() {B b; return b;}
	//    };
	//    template<typename T>
	//    class D : public C<T> {};
	//    void foo(B b) {}
	//
	//    void refs() {
	//    	D<A> d;
	//    	foo(d);
	//    }
	public void testUserDefinedConversions_224364() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPFunction fn = bh.assertNonProblem("foo(d)", 3, ICPPFunction.class);
	}

	//    class B {};
	//    template<typename T>
	//    class C {
	//    public:
	//    	T t;
	//    	operator T() {return t;}
	//    };
	//    template<typename T>
	//    class D : public C<T> {};
	//    void foo(B b) {}
	//
	//    void refs() {
	//    	D<B> d;
	//    	foo(d);
	//    }
	public void testUserDefinedConversions_224364a() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPFunction fn = bh.assertNonProblem("foo(d)", 3, ICPPFunction.class);
	}

	//    class Z {};
	//    template<typename TA>
	//    class A {
	//    	public:
	//    		TA ta;
	//          operator TA() {return ta;}
	//    };
	//    template<typename TB>
	//    class B : public A<TB> {};
	//    template<typename TC>
	//    class C : public B<TC> {};
	//    template<typename TD>
	//    class D : public C<TD> {};
	//    template<typename TE>
	//    class E : public D<TE> {};
	//    Z foo(Z z) {return z;}
	//
	//    Z z= foo(*new E<Z>());
	public void testUserDefinedConversions_224364b() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPFunction fn = bh.assertNonProblem("foo(*new", 3, ICPPFunction.class);
	}

	//    class X {}; class B {};
	//    template<typename T>
	//    class C {
	//    	public:
	//    		T t;
	//          operator T() {return t;}
	//    };
	//    template<>
	//    class C<X> {
	//    	public:
	//    		X t;
	//          operator B() {B b; return b;}
	//    };
	//    void foo(B b) {}
	//
	//    void refs() {
	//    	C<X> cx;
	//    	foo(cx);
	//    }
	public void testUserDefinedConversions_226231() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPFunction fn = bh.assertNonProblem("foo(cx", 3, ICPPFunction.class);
	}

	//	class A;
	//
	//	int foo(A a);
	//
	//	template <class T>
	//	class C {
	//	public:
	//		inline operator A();
	//	};
	//
	//	template<typename T>
	//	void ref(C<T> c) {
	//	 return foo(c);
	//	}
	public void testUserDefinedConversions_239023() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ba.assertNonProblem("foo(c);", 3);
	}

	//	template<int x>
	//	class A {};
	//
	//	const int i= 1;
	//	A<i> a1;
	public void testNonTypeArgumentIsIDExpression_229942a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertInstance(col.getName(4).getParent(), ICPPASTTemplateId.class);
		assertInstance(col.getName(5).getParent(), IASTIdExpression.class);
	}

	//  class X {
	//	   template<int x>
	//	   class A {};
	//
	//	   void foo() {
	//	      A<i> a1;
	//     }
	//
	//     const int i= 1;
	//  };
	public void testNonTypeArgumentIsIDExpression_229942b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertInstance(col.getName(5).getParent(), ICPPASTTemplateId.class);
		assertInstance(col.getName(6).getParent(), IASTIdExpression.class);
	}

	//	template<int x>
	//	class A {};
	//
	//	const int i= 1;
	//	A<i+1> a1;
	public void testExpressionArgumentIsExpression_229942() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertInstance(col.getName(4).getParent(), ICPPASTTemplateId.class);
		assertInstance(col.getName(5).getParent(), IASTIdExpression.class);
		assertInstance(col.getName(5).getParent().getParent(), IASTBinaryExpression.class);
	}

	//	template<int x>
	//	class A {};
	//
	//	const int i= 1;
	//	A<typeid(1)> a1;
	public void testTypeIdOperatorArgumentIsUnaryExpression_229942() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertInstance(col.getName(3), ICPPASTTemplateId.class);
		assertInstance(((ICPPASTTemplateId) col.getName(3)).getTemplateArguments()[0], ICPPASTUnaryExpression.class);
	}

	// template<class T1, int q> class C {};
	// template<class T1, class T2> class A {};
	// template< class T1, class T2, int q1, int q2>
	// class A< C<T1, q1>, C<T2, q2> > {};
	public void testTemplateIdAsTemplateArgumentIsTypeId_229942() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		// 12 is template-id: C<T1, q1>
		assertInstance(col.getName(12), ICPPASTTemplateId.class);
		assertInstance(col.getName(12).getParent(), ICPPASTNamedTypeSpecifier.class);
		assertInstance(col.getName(12).getParent().getParent(), IASTTypeId.class);

		// 16 is template-id: C<T2, q2>
		assertInstance(col.getName(16), ICPPASTTemplateId.class);
		assertInstance(col.getName(16).getParent(), ICPPASTNamedTypeSpecifier.class);
		assertInstance(col.getName(16).getParent().getParent(), IASTTypeId.class);
	}

	//	template <class T>
	//	struct A {
	//		A(T* t) {}
	//	};
	//
	//	template <class T>
	//	inline const A<T> foo(T* t) {
	//		return A<T>(t);
	//	}
	//
	//	template <class T>
	//	inline const A<T> foo(const A<T> at) {
	//		return at;
	//	}
	public void testTypeIdAsTemplateArgumentIsTypeId_229942a() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ba.assertNonProblem("T> at) {", 1);

		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertInstance(col.getName(23).getParent().getParent(), IASTTypeId.class);
		assertInstance(col.getName(23).resolveBinding(), ICPPTemplateTypeParameter.class);
	}

	//	template <class T>
	//	struct A {};
	//
	//	template <class T>
	//	inline const void foo(void (*f)(A<T>), T t) {
	//	}
	//
	//	const int i= 5;
	//	template <class T>
	//	inline const void foo(void (*f)(A<i>), T* t) { // disallowed, but we're testing the AST
	//	}
	public void testTypeIdAsTemplateArgumentIsTypeId_229942b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		// 7 is T in A<T>
		assertInstance(col.getName(7).getParent(), ICPPASTNamedTypeSpecifier.class);
		assertInstance(col.getName(7).getParent().getParent(), IASTTypeId.class);

		// 17 is i in A<i>
		assertInstance(col.getName(17).getParent(), IASTIdExpression.class);
	}

	//	typedef int td;
	//	template<> class Alias<td const *> {
	//	};
	public void testNonAmbiguityCase_229942() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		// 2 is Alias
		ICPPASTTemplateId tid = assertInstance(col.getName(2).getParent(), ICPPASTTemplateId.class);
		IASTNode[] args = tid.getTemplateArguments();
		assertEquals(1, args.length);
		assertInstance(args[0], IASTTypeId.class);
	}

	//  // From discussion in 207840. See 14.3.4.
	//	class A {};
	//
	//	template<typename T>
	//	class B {};
	//
	//	template<typename T = A>
	//	class C {};
	//
	//	B b1;
	//	B<> b2; // error - no default args
	//
	//	C c1;
	//	C<> c2; // ok - default args
	public void testMissingTemplateArgumentLists() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ba.assertProblem("B b1", 1);
		ba.assertNonProblem("B<> b2", 1, ICPPTemplateDefinition.class, ICPPClassType.class);
		ba.assertProblem("B<> b2", 3);
		ba.assertProblem("C c1", 1);
		ba.assertNonProblem("C<> c2", 1, ICPPTemplateDefinition.class, ICPPClassType.class);
		ba.assertNonProblem("C<> c2", 3, ICPPTemplateInstance.class, ICPPClassType.class);
	}

	//	template<class T1, int N> class TestClass {
	//		int member1;
	//		void fun1(void);
	//	};
	//	template<class T1,int N> inline void TestClass<T1,N>::fun1(void) {
	//		member1 = 0;
	//	}
	public void testDefinitionOfClassTemplateWithNonTypeParameter() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPMethod f1 = ba.assertNonProblem("fun1(void);", 4, ICPPMethod.class);
		ICPPField m1 = ba.assertNonProblem("member1;", 7, ICPPField.class);
		ICPPMethod f2 = ba.assertNonProblem("fun1(void) {", 4, ICPPMethod.class);
		ICPPField m2 = ba.assertNonProblem("member1 =", 7, ICPPField.class);
		assertSame(m1, m2);
		assertSame(f1, f2);
	}

	//	class Z {};
	//
	//	template<typename T1>
	//	class A {
	//		public:
	//			template<typename T2 = Z> class B;
	//	};
	//
	//	template<> template<typename T3> class A<short>::B {
	//		public:
	//			T3 foo() { return (T3) 0; }
	//	};
	//
	//	void ref() {
	//		A<short>::B<> b;
	//	}
	public void testNestedTemplateDefinitionParameter() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPTemplateTypeParameter T3a = ba.assertNonProblem("T3 f", 2, ICPPTemplateTypeParameter.class);
		ICPPTemplateTypeParameter T3b = ba.assertNonProblem("T3)", 2, ICPPTemplateTypeParameter.class);
		ICPPClassType b = ba.assertNonProblem("B<>", 3, ICPPClassType.class, ICPPTemplateInstance.class);
	}

	//	template<class T, int x> class A {public: class X {};};
	//	template<class T1> class A<T1,1> {public: class Y {};};
	//	template<class T2> class A<T2,2> {public: class Z {};};
	//
	//	class B {};
	//
	//	A<B, 0>::X x;
	//	A<B, 1>::Y y;
	//	A<B, 2>::Z z;
	public void testNonTypeArgumentDisambiguation_233460() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPClassType b2 = ba.assertNonProblem("A<B, 0>", 7, ICPPClassType.class, ICPPTemplateInstance.class);
		ICPPClassType b3 = ba.assertNonProblem("A<B, 1>", 7, ICPPClassType.class, ICPPTemplateInstance.class);
		ICPPClassType b4 = ba.assertNonProblem("A<B, 2>", 7, ICPPClassType.class, ICPPTemplateInstance.class);

		assertTrue(!b2.isSameType(b3));
		assertTrue(!b3.isSameType(b4));
		assertTrue(!b4.isSameType(b2));

		ICPPClassType X = ba.assertNonProblem("X x", 1, ICPPClassType.class);
		ICPPClassType Y = ba.assertNonProblem("Y y", 1, ICPPClassType.class);
		ICPPClassType Z = ba.assertNonProblem("Z z", 1, ICPPClassType.class);

		assertTrue(!X.isSameType(Y));
		assertTrue(!Y.isSameType(Z));
		assertTrue(!Z.isSameType(X));
	}

	//	template<class T, bool b> class A {public: class X {};};
	//	template<class T1> class A<T1,true> {public: class Y {};};
	//
	//	class B {};
	//
	//	A<B, false>::X x; //1
	//	A<B, true>::Y y; //2
	//
	//	A<B, true>::X x; //3 should be an error
	//	A<B, false>::Y y; //4 should be an error
	public void testNonTypeBooleanArgumentDisambiguation() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);

		ICPPClassType X = ba.assertNonProblem("X x; //1", 1, ICPPClassType.class);
		ICPPClassType Y = ba.assertNonProblem("Y y; //2", 1, ICPPClassType.class);
		ba.assertProblem("X x; //3", 1);
		ba.assertProblem("Y y; //4", 1);

		assertTrue(!X.isSameType(Y));
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
	//
	//	int main (void) {
	//		C<256> t;
	//		foo(t);
	//		bar(t);
	//		baz();
	//	}
	public void testBug207871() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);

		ICPPVariable _256 = ba.assertNonProblem("_256=0x100", 4, ICPPVariable.class);
		IQualifierType qt1 = assertInstance(_256.getType(), IQualifierType.class);
		ICPPBasicType bt1 = assertInstance(qt1.getType(), ICPPBasicType.class);
		assertConstantValue(256, _256);

		ICPPVariable t = ba.assertNonProblem("t;", 1, ICPPVariable.class);
		ICPPTemplateInstance ci1 = assertInstance(t.getType(), ICPPTemplateInstance.class, ICPPClassType.class);
		ICPPTemplateParameterMap args1 = ci1.getTemplateParameterMap();
		assertEquals(1, args1.getAllParameterPositions().length);
		assertEquals(256, args1.getArgument(0).getNonTypeValue().numberValue().intValue());

		ICPPTemplateInstance ct = ba.assertNonProblem("C<_256> ", 7, ICPPTemplateInstance.class, ICPPClassType.class);
		ICPPTemplateParameterMap args = ct.getTemplateParameterMap();
		assertEquals(1, args.getAllParameterPositions().length);
		assertEquals(256, args.getArgument(0).getNonTypeValue().numberValue().intValue());

		ba.assertNonProblem("foo(t)", 3);
		ba.assertNonProblem("bar(t)", 3);
	}

	//	template<int x>
	//	class C {};
	//
	//	template<int y>
	//	class D {
	//	public:
	//		C<y> go();
	//	};
	public void testDeferredNonTypeArgument() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPDeferredClassInstance ci = ba.assertNonProblem("C<y>", 4, ICPPDeferredClassInstance.class);
		ICPPTemplateArgument[] args = ci.getTemplateArguments();
		assertEquals(1, args.length);
		assertEquals(0, IntegralValue.isTemplateParameter(args[0].getNonTypeValue()));
	}

	//	template<int x>
	//	class A {};
	//
	//	A<int> aint; // should be an error
	public void testTypeArgumentToNonTypeParameter() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ba.assertProblem("A<int>", 6);
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
	//
	//	template <int I>
	//	inline This<I>::This() : That<I>(I) {
	//	}
	public void testParameterReferenceInChainInitializer_a() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);

		// These intermediate assertions will not hold until deferred non-type arguments are
		// correctly modelled
		ICPPClassType tid = ba.assertNonProblem("This<I>::T", 7, ICPPClassType.class);
		assertFalse(tid instanceof ICPPSpecialization);
		ICPPConstructor th1sCtor = ba.assertNonProblem("This() :", 4, ICPPConstructor.class);
		assertFalse(th1sCtor instanceof ICPPSpecialization);

		ICPPTemplateNonTypeParameter np = ba.assertNonProblem("I>(I)", 1, ICPPTemplateNonTypeParameter.class);
		ICPPConstructor clazz = ba.assertNonProblem("That<I>(I)", 4, ICPPConstructor.class);
		ICPPConstructor ctor = ba.assertNonProblem("That<I>(I)", 7, ICPPConstructor.class);

		ICPPTemplateNonTypeParameter np1 = ba.assertNonProblem("I)", 1, ICPPTemplateNonTypeParameter.class);
		assertSame(np, np1);
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
	//
	//	template <typename I>
	//	inline This<I>::This() : That<I>() {
	//	}
	public void testParameterReferenceInChainInitializer_b() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);

		ICPPClassType tid = ba.assertNonProblem("This<I>::T", 7, ICPPClassType.class);
		assertFalse(tid instanceof ICPPSpecialization);
		ICPPConstructor th1sCtor = ba.assertNonProblem("This() :", 4, ICPPConstructor.class);
		assertFalse(th1sCtor instanceof ICPPSpecialization);

		ICPPTemplateTypeParameter np = ba.assertNonProblem("I>()", 1, ICPPTemplateTypeParameter.class);
		ICPPConstructor clazz = ba.assertNonProblem("That<I>()", 4, ICPPConstructor.class);
		ICPPConstructor ctor = ba.assertNonProblem("That<I>()", 7, ICPPConstructor.class);
	}

	// template<typename T, int I>
	// class C {};
	//
	// template<typename T>
	// class C<T, 5> {};
	//
	// class A {};
	//
	// C<A,5L> ca5L;
	public void testIntegralConversionInPartialSpecializationMatching_237914() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPTemplateInstance ctps = ba.assertNonProblem("C<A,5L>", 7, ICPPTemplateInstance.class, ICPPClassType.class);
		assertInstance(ctps.getTemplateDefinition(), ICPPClassTemplatePartialSpecialization.class);
	}

	// template<typename T, int I>
	// class C {};
	//
	// class A {};
	//
	// template<>
	// class C<A, 5> {
	// public: int test;
	// };
	//
	// C<A,5L> ca5L;
	// void xx() {
	//    ca5L.test= 0;
	// }
	public void testIntegralConversionInSpecializationMatching_237914() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPSpecialization ctps = ba.assertNonProblem("C<A,5L>", 7, ICPPSpecialization.class, ICPPClassType.class);
		ba.assertNonProblem("test=", 4, ICPPField.class);
	}

	//	template<typename T, typename U>
	//	struct is_same {};
	//
	//	template<typename T>
	//	struct is_same<T, T> {
	//	  constexpr operator bool() { return true; }
	//	};
	//
	//	template<bool>
	//	struct enable_if {};
	//
	//	template<>
	//	struct enable_if<true> {
	//	  typedef void type;
	//	};
	//
	//	template <typename T>
	//	typename enable_if<is_same<T, T>{}>::type waldo(T p);
	//
	//	void test() {
	//	  waldo(1);
	//	}
	public void testIntegralConversionOperator_495091a() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T, typename U>
	//	struct is_same {};
	//
	//	template<typename T>
	//	struct is_same<T, T> {
	//	  constexpr operator bool() { return true; }
	//	};
	//
	//	template<bool>
	//	struct enable_if {};
	//
	//	template<>
	//	struct enable_if<true> {
	//	  typedef void type;
	//	};
	//
	//	template <typename T>
	//	typename enable_if<is_same<T, T>{} && true>::type waldo(T p);
	//
	//	void test() {
	//	  waldo(1);
	//	}
	public void testIntegralConversionOperator_495091b() throws Exception {
		parseAndCheckBindings();
	}

	//	class A {
	//		public:
	//			A(const A& a) {}
	//	};
	//
	//	template<typename T>
	//	class B : A {
	//		public:
	//			B(const B<T>& other) : A(other) {}
	//	};
	public void testChainInitializerLookupThroughDeferredClassBase() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ba.assertNonProblem("A(other", 1);
	}

	//	template<class T>
	//	struct A {};
	//
	//	template <typename U>
	//	A<int> waldo(U p);
	//
	//	void test() {
	//	  typedef int INT;
	//	  A<INT> x = waldo([](int data) { return false; });
	//	}
	public void testLambda_430428() throws Exception {
		parseAndCheckBindings();
	}

	//	class A {};
	//
	//	class B {
	//	public:
	//		void foo(const A* b);
	//	};
	//
	//	template<typename T>
	//	class C : public B {
	//	public:
	//		void foo(T *t) {
	//			B::foo(static_cast<A*>(t));
	//		}
	//	};
	public void testMemberLookupThroughDeferredClassBase() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ba.assertNonProblem("foo(s", 3);
	}

	//	template<typename U>
	//	struct result : U {
	//	    typedef typename result::result_type type;
	//	};
	//
	//	struct B {
	//	    typedef int result_type;
	//	};
	//
	//	typedef result<B>::type waldo;
	public void testDependentBaseLookup_408314a() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ITypedef waldo = bh.assertNonProblem("waldo");
		assertSameType(waldo.getType(), CommonCPPTypes.int_);
	}

	//	template <typename T>
	//	struct A {
	//		template <typename U>
	//		struct result;
	//
	//		template <typename V>
	//		struct result<V*> : T {
	//	    	typedef typename result::result_type type;
	//		};
	//	};
	//
	//	struct B {
	//	    typedef int result_type;
	//	};
	//
	//	typedef A<B>::result<int*>::type waldo;
	public void testDependentBaseLookup_408314b() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ITypedef waldo = bh.assertNonProblem("waldo");
		assertSameType(waldo.getType(), CommonCPPTypes.int_);
	}

	//	template <typename T>
	//	struct A {
	//		template <typename U>
	//		struct result;
	//
	//		template <typename V>
	//		struct result<V*> : T {
	//	    	typedef typename result::result_type type;
	//		};
	//	};
	//
	//	struct B {
	//	    typedef int result_type;
	//	};
	//
	//	typedef A<B>::result<B*>::type waldo;
	public void testDependentBaseLookup_408314c() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ITypedef waldo = bh.assertNonProblem("waldo");
		assertSameType(waldo.getType(), CommonCPPTypes.int_);
	}

	//	template <class T>
	//	class A {
	//	public:
	//		inline int foo() const;
	//		inline int bar() const;
	//	};
	//
	//	template <class T>
	//	inline int A<T>::bar() const {
	//		return foo();
	//	}
	public void testMemberReferenceFromTemplatedMethodDefinition_238232() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ba.assertNonProblem("foo();", 3);
	}

	//	namespace result_of {
	//		template <typename Sequence, typename T, bool is_associative_sequence = false>
	//		struct find;
	//
	//		template <typename Sequence, typename T>
	//		struct find<Sequence, T, false> {
	//			typedef
	//			detail::static_seq_find_if<
	//			typename result_of::begin<Sequence>::type
	//			, typename result_of::end<Sequence>::type
	//			, is_same<mpl::_, T>
	//			>
	//			filter;
	//		};
	//
	//		template <typename Sequence, typename T>
	//		struct find<Sequence, T, true> {
	//			typedef detail::assoc_find<Sequence, T> filter;
	//		};
	//	}
	public void testBug238180_ArrayOutOfBounds() throws Exception {
		// The code above used to trigger an ArrayOutOfBoundsException
		parse(getAboveComment(), CPP);
	}

	//	namespace detail {
	//		template<bool AtoB, bool BtoA, bool SameType, class A, class B>
	//		struct str;
	//		template<class A, class B>
	//		struct str<true, true, false, A, B> {
	//			typedef
	//			detail::return_type_deduction_failure<str> type;
	//			// ambiguous type in conditional expression
	//		};
	//		template<class A, class B>
	//		struct str<true, true, true, A, B> {
	//			typedef A type;
	//		};
	//	} // detail
	public void testBug238180_ClassCast() throws Exception {
		// the code above used to trigger a ClassCastException
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPClassType p = ba.assertNonProblem("str<true, true, false, A, B>", 0, ICPPClassType.class);
		ICPPConstructor con = p.getConstructors()[1];
		ICPPReferenceType reftype = (ICPPReferenceType) con.getType().getParameterTypes()[0];
		IQualifierType qt = (IQualifierType) reftype.getType();
		ICPPDeferredClassInstance dcl = (ICPPDeferredClassInstance) qt.getType();
		ICPPClassTemplatePartialSpecialization spec = (ICPPClassTemplatePartialSpecialization) dcl
				.getSpecializedBinding();
		ICPPTemplateTypeParameter tp = (ICPPTemplateTypeParameter) spec.getTemplateParameters()[0];
		assertNull(tp.getDefault());
	}

	//	class X {
	//		template <typename S> X(S s);
	//	};
	//
	//	void test(X* a);
	//	void bla(int g) {
	//		test(new X(g));
	//	}
	public void testBug239586_ClassCast() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP);
	}

	//	template<typename T1> class CT {
	//		static int x;
	//	};
	//	template<typename T> int CT<T>::x = sizeof(T);
	public void testUsingTemplParamInInitializerOfStaticField() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPTemplateTypeParameter t = ba.assertNonProblem("T)", 1, ICPPTemplateTypeParameter.class);
	}

	//	template <typename T>
	//	constexpr T id(T a) {
	//	    return a;
	//	}
	//
	//	template <int N>
	//	struct ratio {
	//	    static const int num = N;
	//	};
	//
	//	template <typename factor>
	//	struct ratioRoundUp : ratio<id(factor::num)> {};
	//
	//	typedef ratioRoundUp<ratio<42>> rounded;
	//
	//	template <int> struct Waldo;
	//	template <> struct Waldo<42> { typedef int type; };
	//
	//	Waldo<rounded::num>::type foo();  // ERROR
	public void testDependentIdExprNamingStaticMember_508254() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class T1, T1 v1>
	//	struct integral_constant {
	//	  static const T1 value = v1;
	//	  typedef T1 value_type;
	//	  typedef integral_constant<T1, v1> type;
	//	};
	//	template <class T2, T2 v2> const T2 integral_constant<T2, v2>::value;
	//	typedef integral_constant<bool, true>  true_type;
	//	typedef integral_constant<bool, false> false_type;
	//
	//	template<bool cond, typename A1, typename B1>
	//	struct if_{
	//	  typedef A1 type;
	//	};
	//	template<typename A2, typename B2>
	//	struct if_<false, A2, B2> {
	//	  typedef B2 type;
	//	};
	//
	//	struct AA {
	//	  int a;
	//	  void method() {}
	//	};
	//	void func(AA oa) {}
	//
	//	struct BB {
	//	  int b;
	//	  void method() {}
	//	};
	//	void func(BB ob) {}
	//
	//	template<class T>
	//	struct CC : public if_<T::value, AA, BB>::type {};
	//
	//	void test() {
	//	  CC<true_type> ca;
	//	  CC<false_type> cb;
	//	  ca.method();
	//	  ca.a = 5;
	//	  cb.b = 6;
	//	  func(cb);
	//	}
	public void testTemplateMetaProgramming_245027() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPMethod method = ba.assertNonProblem("method();", 6, ICPPMethod.class);
		ICPPVariable a = ba.assertNonProblem("a =", 1, ICPPVariable.class);
		ICPPVariable b = ba.assertNonProblem("b =", 1, ICPPVariable.class);
		ICPPFunction func = ba.assertNonProblem("func(cb)", 4, ICPPFunction.class);
	}

	//	class Incomplete;
	//
	//	char probe(Incomplete* p);
	//	char (&probe(...))[2];
	//
	//	namespace ns1 {
	//
	//	template<bool VAL>
	//	class A {
	//	 public:
	//	  static bool m(int a) {}
	//	};
	//	}
	//
	//	void test() {
	//	  int x;
	//	  ns1::A<(sizeof(probe(x)) == 1)>::m(x);
	//	}
	public void testNonTypeTemplateParameter_252108() throws Exception {
		BindingAssertionHelper ba = new AST2AssertionHelper(getAboveComment(), CPP);
		ba.assertNonProblem("x))", 1, ICPPVariable.class);
	}

	//    template<typename T, typename U> class TL {};
	//    typedef int T;
	//    typedef
	//    TL<T, TL< T, TL< T, TL< T, TL<T,
	//    TL<T, TL< T, TL< T, TL< T, TL<T,
	//    TL<T, TL< T, TL< T, TL< T, TL<T,
	//    TL<T, TL< T, TL< T, TL< T, TL<T,
	//    TL<T, TL< T, TL< T, TL< T, TL<T,
	//    T
	//    > > > > >
	//    > > > > >
	//    > > > > >
	//    > > > > >
	//    > > > > >
	//    type;
	public void testNestedArguments_246079() throws Throwable {
		final Throwable[] th = { null };
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					parseAndCheckBindings(getAboveComment(), CPP);
				} catch (Throwable e) {
					th[0] = e;
				}
			}
		};

		t.start();
		t.join(4000);
		assertFalse(t.isAlive());
		if (th[0] != null)
			throw th[0];
	}

	//	template<class T, class U> class A {};
	//	template<class T> class A<T, int> {
	//	   void foo(T t);
	//	};
	//	template<class T> void A<T, int>::foo(T t) {}
	public void testBug177418() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);

		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPTemplateParameter T1 = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPTemplateParameter U = (ICPPTemplateParameter) col.getName(1).resolveBinding();
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(2).resolveBinding();

		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(3).resolveBinding();
		assertNotSame(T1, T2);

		ICPPClassTemplatePartialSpecialization A2 = (ICPPClassTemplatePartialSpecialization) col.getName(4)
				.resolveBinding();
		assertSame(A2.getPrimaryClassTemplate(), A);
		assertSame(A, col.getName(5).resolveBinding());
		assertSame(T2, col.getName(6).resolveBinding());

		ICPPMethod foo = (ICPPMethod) col.getName(7).resolveBinding();
		assertSame(T2, col.getName(8).resolveBinding());
		assertSame(T2, col.getName(10).resolveBinding());
		ICPPParameter t = (ICPPParameter) col.getName(9).resolveBinding();

		assertSame(A2, col.getName(12).resolveBinding());
		assertSame(A, col.getName(13).resolveBinding());
		assertSame(T2, col.getName(14).resolveBinding());
		assertSame(foo, col.getName(15).resolveBinding());
		assertSame(T2, col.getName(16).resolveBinding());
		assertSame(t, col.getName(17).resolveBinding());
	}

	//    template <typename T, typename U> class CT {
	//    	T* instance(void);
	//    };
	//    template <class T, class U> T * CT<T, U>::instance (void) {
	//    	return new CT<T, U>;
	//    }
	public void testNewOfThisTemplate() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP);
	}

	//    template <class T> void f(T);
	//    class X {
	//    	friend void f<>(int);
	//    };
	public void testFunctionSpecializationAsFriend() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPFunctionTemplate f = bh.assertNonProblem("f(T)", 1);
		IFunction fref1 = bh.assertNonProblem("f<>", 1);
		assertSame(fref1, f);
		IFunction fref2 = bh.assertNonProblem("f<>", 3);
		assertInstance(fref2, ICPPTemplateInstance.class);
		assertSame(f, ((ICPPTemplateInstance) fref2).getSpecializedBinding());
	}

	//    template <typename T> class XT {
	//    	typedef int mytype1;
	//    	mytype1 m1();
	//    };
	//    template <typename T> class XT<T*> {
	//    	typedef int mytype2;
	//    	mytype2 m2();
	//    };
	//    template <> class XT<int> {
	//    	typedef int mytype3;
	//    	mytype3 m3();
	//    };
	//    template <typename T> typename XT<T>::mytype1 XT<T>::m1() {}
	//    template <typename T> typename XT<T*>::mytype2 XT<T*>::m2() {}
	//    XT<int>::mytype3 XT<int>::m3() {}
	public void testMethodImplWithNonDeferredType() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPMethod m1 = bh.assertNonProblem("m1();", 2);
		ICPPMethod m2 = bh.assertNonProblem("m1() ", 2);
		assertSame(m1, m2);
		m1 = bh.assertNonProblem("m2();", 2);
		m2 = bh.assertNonProblem("m2() ", 2);
		assertSame(m1, m2);
		m1 = bh.assertNonProblem("m3();", 2);
		m2 = bh.assertNonProblem("m3() ", 2);
		assertSame(m1, m2);
	}

	//    template<typename S> class A1 {
	//        template<typename T> void f1(T);
	//    };
	//    template<> template<typename T> void A1<float>::f1(T){}
	//
	//    template<typename T> class A {};
	//    template<> class A<float> {
	//    	  template<typename T> void f(T);
	//    };
	//    template<typename T> void A<float>::f(T){}
	public void testClassTemplateMemberFunctionTemplate_104262() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		ICPPClassTemplate A1 = bh.assertNonProblem("A1", 2);
		ICPPMethod method = bh.assertNonProblem("A1<float>::f1", 13);
		IBinding owner = method.getOwner();
		assertInstance(owner, ICPPClassSpecialization.class);
		assertSame(A1, ((ICPPClassSpecialization) owner).getSpecializedBinding());

		ICPPClassSpecialization special = bh.assertNonProblem("A<float>", 8);
		method = bh.assertNonProblem("A<float>::f", 11);
		assertSame(method.getOwner(), special);
	}

	//    template<typename T> class XT {
	//    	class Nested {
	//    		template<typename V> void Nested::m(V);
	//    	};
	//    };
	//    template<typename T> template <typename V> void XT<T>::Nested::m(V) {
	//    }
	public void testQualifiedMethodTemplate() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		ICPPMethod mt1 = bh.assertNonProblem("m(V);", 1);
		ICPPMethod mt2 = bh.assertNonProblem("m(V) ", 1);
		assertSame(mt1, mt2);
		assertInstance(mt1, ICPPFunctionTemplate.class);
	}

	//	template <typename T>
	//	struct A {
	//	  template <typename U>
	//	  static U m();
	//	};
	//
	//	template <typename T, typename U = decltype(A<T>::template m<char>())>
	//	class B {};
	//
	//	template <typename T>
	//	void waldo(T p);
	//
	//	template <typename T>
	//	typename B<T>::type waldo(T p);
	//
	//	void test() {
	//	  waldo(1);
	//	}
	public void testMethodTemplate_497535a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {
	//	  template <typename U>
	//	  static U m();
	//	};
	//
	//	template <typename T, typename U = decltype(A<T>::template m())>
	//	class B {};
	//
	//	template <typename T>
	//	void waldo(T p);
	//
	//	template <typename T>
	//	typename B<T>::type waldo(T p);
	//
	//	void test() {
	//	  waldo(1);
	//	}
	public void testMethodTemplate_497535b() throws Exception {
		parseAndCheckBindings();
	}

	//    template <typename T, typename U=T> class XT {};
	//    template <typename T> class XT<T,T> {public: int partial;};
	//    void test() {
	//       XT<int> xt;
	//       xt.partial;
	//    }
	public void testDefaultArgsWithPartialSpecialization() throws Exception {
		parseAndCheckBindings();
	}

	//    template <typename T> class XT {
	//   	public:
	//   		int a;
	//    	void m() {
	//    		this->a= 1;
	//    	}
	//    };
	public void testFieldReference_257186() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		IBinding a1 = bh.assertNonProblem("a;", 1);
		IBinding a2 = bh.assertNonProblem("a=", 1);
		assertInstance(a1, ICPPField.class);
		assertSame(a1, a2);
	}

	//    void f(int); void f(char);
	//    void g(int);
	//    template<typename T> void h(T);
	//    template<typename T> struct A  {
	//      void m(int); void m(char);
	//    	void m() {
	//    		typename T::B b;
	//    		b.func(); b.var;
	//    		f(b); g(b); h(b); m(b);
	//    	}
	//    };
	public void testUnknownReferences_257194() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		bh.assertNonProblem("func();", 4, ICPPUnknownBinding.class);
		bh.assertNonProblem("var;", 3, ICPPUnknownBinding.class);
		bh.assertNonProblem("f(b)", 1, ICPPUnknownBinding.class, IFunction.class);
		bh.assertNonProblem("h(b)", 1, ICPPUnknownBinding.class, IFunction.class);
		bh.assertNonProblem("m(b)", 1, ICPPUnknownBinding.class, IFunction.class);
		bh.assertNonProblem("g(b)", 1, ICPPUnknownBinding.class, IFunction.class);
	}

	//    template<typename T> struct A  {
	//    	void m() {
	//    		T::b.c;
	//	        T::b.f();
	//    		T::b.f().d;
	//          T::f1();
	//          T v;
	//			v.x; v.y();
	//    	}
	//    };
	public void testTypeOfUnknownReferences_257194a() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		bh.assertNonProblem("b.c", 1, ICPPUnknownBinding.class);
		bh.assertNonProblem("c;", 1, ICPPUnknownBinding.class);
		bh.assertNonProblem("f();", 1, ICPPUnknownBinding.class, IFunction.class);
		bh.assertNonProblem("f().", 1, ICPPUnknownBinding.class, IFunction.class);
		bh.assertNonProblem("d;", 1, ICPPUnknownBinding.class);
		bh.assertNonProblem("f1();", 2, ICPPUnknownBinding.class, IFunction.class);
		bh.assertNonProblem("x;", 1, ICPPUnknownBinding.class);
		bh.assertNonProblem("y();", 1, ICPPUnknownBinding.class, IFunction.class);
	}

	//    template<typename T> struct A  {
	//    	void m() {
	//    		T::b->c;
	//	        T::b->f();
	//    		T::b->f()->d;
	//          T::f1();
	//          T v;
	//          v->x; v->y();
	//    	}
	//    };
	public void testTypeOfUnknownReferences_257194b() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		bh.assertNonProblem("b->c", 1, ICPPUnknownBinding.class);
		bh.assertNonProblem("c;", 1, ICPPUnknownBinding.class);
		bh.assertNonProblem("f();", 1, ICPPUnknownBinding.class, IFunction.class);
		bh.assertNonProblem("f()->", 1, ICPPUnknownBinding.class, IFunction.class);
		bh.assertNonProblem("d;", 1, ICPPUnknownBinding.class);
		bh.assertNonProblem("f1();", 2, ICPPUnknownBinding.class, IFunction.class);
		bh.assertNonProblem("x;", 1, ICPPUnknownBinding.class);
		bh.assertNonProblem("y();", 1, ICPPUnknownBinding.class, IFunction.class);
	}

	//    template<typename T> class XT {
	//    	typename T::template type<T::a> x;
	//    	typename T::template type<typename T::A> y;
	//      using T::b;
	//      using typename T::B;
	//      void m() {
	//         T::f();
	//         typename T::F();
	//      }
	//    };
	public void testTypeVsExpressionInArgsOfDependentTemplateID_257194() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		ICPPUnknownBinding b = bh.assertNonProblem("a>", 1);
		assertFalse(b instanceof IType);
		b = bh.assertNonProblem("A>", 1);
		assertTrue(b instanceof IType);

		ICPPUsingDeclaration ud = bh.assertNonProblem("b;", 1);
		b = (ICPPUnknownBinding) ud.getDelegates()[0];
		assertFalse(b instanceof IType);
		ud = bh.assertNonProblem("B;", 1);
		b = (ICPPUnknownBinding) ud.getDelegates()[0];
		assertTrue(b instanceof IType);

		b = bh.assertNonProblem("f();", 1);
		assertFalse(b instanceof IType);
		b = bh.assertNonProblem("F();", 1);
		assertTrue(b instanceof IType);
	}

	//  template <typename Val>
	//  struct A {
	//    typedef const Val value;
	//  };
	//
	//  template<typename T>
	//  struct B {
	//    typedef typename T::value& reference;
	//  };
	//
	//  void func(int a);
	//
	//  void test(B<A<int> >::reference p) {
	//    func(p);
	//  }
	public void testTypedefReference_259871() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertNonProblem("func(p)", 4, ICPPFunction.class);
	}

	//	template <class T>
	//	struct C {
	//	  typedef void (T::*PMF)();
	//	  C(PMF member);
	//	};
	//
	//	struct A {
	//	  void m();
	//	};
	//
	//	typedef A B;
	//
	//	void test() {
	//	  new C<B>(&B::m);
	//	}
	public void testTypedef() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	struct basic_string {
	//	  basic_string& operator+=(const T* s);
	//	  basic_string& append(const T* s);
	//	};
	//
	//	template<typename T>
	//	basic_string<T> operator+(const T* cs, const basic_string<T>& s);
	//
	//	template<typename T>
	//	basic_string<T> operator+(const basic_string<T>& s, const T* cs);
	//
	//	typedef basic_string<char> string;
	//
	//	void test(const string& s) {
	//	  auto s1 = "" + s + "";
	//	  auto s2 = s1 += "";
	//	  auto s3 = s2.append("foo");
	//	}
	public void testTypedefPreservation_380498a() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ICPPVariable s1 = ba.assertNonProblem("s1");
		assertTrue(s1.getType() instanceof ITypedef);
		assertEquals("string", ((ITypedef) s1.getType()).getName());
		ICPPVariable s2 = ba.assertNonProblem("s2");
		assertTrue(s2.getType() instanceof ITypedef);
		assertEquals("string", ((ITypedef) s2.getType()).getName());
		ICPPVariable s3 = ba.assertNonProblem("s3");
		assertTrue(s3.getType() instanceof ITypedef);
		assertEquals("string", ((ITypedef) s3.getType()).getName());
	}

	//	template <typename T>
	//	struct vector {
	//	  typedef T* const_iterator;
	//	  const_iterator begin() const;
	//	};
	//
	//	typedef int Element;
	//
	//	void test(const vector<Element>& v) {
	//	  auto it = v.begin();
	//	}
	public void testTypedefPreservation_380498b() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ICPPVariable it = ba.assertNonProblem("it =", "it", ICPPVariable.class);
		assertTrue(it.getType() instanceof ITypedef);
		assertEquals("vector<Element>::const_iterator", ASTTypeUtil.getType(it.getType(), false));
	}

	//	template <typename T> class char_traits {};
	//	template <typename C, typename T = char_traits<C>> class basic_string {};
	//
	//	template<typename _Iterator>
	//	struct iterator_traits {
	//	  typedef typename _Iterator::reference reference;
	//	};
	//
	//	template<typename _Tp>
	//	struct iterator_traits<_Tp*> {
	//	  typedef _Tp& reference;
	//	};
	//
	//	template<typename _Iterator, typename _Container>
	//	struct normal_iterator {
	//	  typedef iterator_traits<_Iterator> traits_type;
	//	  typedef typename traits_type::reference reference;
	//	  reference operator*() const;
	//	};
	//
	//	template <typename T> struct vector {
	//	  typedef T* pointer;
	//	  typedef normal_iterator<pointer, vector> iterator;
	//	  iterator begin();
	//	  iterator end();
	//	};
	//
	//	typedef basic_string<char> string;
	//
	//	void test() {
	//	  vector<string> v;
	//	  for (auto s : v) {
	//	  }
	//	}
	public void testTypedefPreservation_380498c() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ICPPVariable s = ba.assertNonProblem("s :", "s", ICPPVariable.class);
		assertInstance(s.getType(), ITypedef.class);
		assertEquals("string", ASTTypeUtil.getType(s.getType(), false));
	}

	//	template<typename T, bool b = __is_class(T)>
	//	class A {};
	//
	//	template<typename T>
	//	class A<T, true> {
	//	  void waldo();
	//	};
	//
	//	class B {};
	//
	//	void test(A<const B> p) {
	//	  p.waldo();
	//	}
	public void testTypeTraitWithQualifier_511143() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//  template <typename CL, typename T>
	//  struct A {
	//    template<typename U> struct C {
	//      typedef T (U::*method1)() const;
	//    };
	//    typedef typename C<CL>::method1 method2;
	//
	//    A(method2 p);
	//  };
	//
	//  struct B {
	//    int m() const;
	//
	//    void test() {
	//      new A<B, int>(&B::m);
	//    }
	//  };
	public void testNestedTemplates_259872a() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertNonProblem("A<B, int>", 9, ICPPClassType.class);
	}

	//  template <typename CL, typename T>
	//  struct A {
	//    template<typename U> struct C {
	//      typedef T (U::*method1)();
	//    };
	//	  template<typename U> struct C<const U> {
	//	    typedef T (U::*method1)();
	//	  };
	//    typedef typename C<CL>::method1 method2;
	//
	//    A(method2 p);
	//  };
	//
	//  struct B {
	//    int m();
	//
	//    void test() {
	//      new A<B, int>(&B::m);
	//    }
	//  };
	public void testNestedTemplates_259872b() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertNonProblem("A<B, int>", 9, ICPPClassType.class);
	}

	//    template <class T>
	//    class DumbPtr {
	//    public:
	//    	DumbPtr<T> (const DumbPtr<T>& aObj);
	//    	~DumbPtr<T> ();
	//    };
	//    template <class T>
	//    DumbPtr<T>::DumbPtr/**/ (const DumbPtr<T>& aObj) {
	//    }
	//    template <class T>
	//    DumbPtr<T>::~DumbPtr/**/ () {
	//    }
	public void testCtorWithTemplateID_259600() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPConstructor ctor = bh.assertNonProblem("DumbPtr/**/", 7);
		ICPPMethod dtor = bh.assertNonProblem("~DumbPtr/**/", 8);
	}

	//    template <class T> class XT {
	//    public:
	//       template<typename X> XT(X*);
	//       template<typename X> XT(X&);
	//    };
	//    template <class T> template <class X> XT<T>::XT/**/(X* a) {}
	//    template <class T> template <class X> XT<T>::XT<T>/**/(X& a) {}
	public void testCtorTemplateWithTemplateID_259600() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPConstructor ctor = bh.assertNonProblem("XT/**/", 2);
		ctor = bh.assertNonProblem("XT<T>/**/", 5);
	}

	//    template <typename T> class XT {
	//    	public:
	//    		typedef typename T::Nested TD;
	//    };
	//
	//    class Base {
	//    	public:
	//    		typedef int Nested;
	//    };
	//
	//    class Derived : public Base {
	//    };
	//
	//    void test() {
	//    	XT<Derived>::TD x;
	//    }
	public void testResolutionOfUnknownBindings_262163() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		IVariable x = bh.assertNonProblem("x;", 1);
		ITypedef Nested = bh.assertNonProblem("Nested;", 6);
		IType t = x.getType();
		assertInstance(t, ITypedef.class);
		t = ((ITypedef) t).getType();
		assertSame(t, Nested);
	}

	//  template<typename _CharT>
	//  struct StringBase {
	//    typedef int size_type;
	//  };
	//
	//  template<typename _CharT, template<typename> class _Base = StringBase>
	//  struct VersaString;
	//
	//  template<typename _CharT, template<typename> class _Base>
	//  struct VersaString : private _Base<_CharT> {
	//    typedef typename _Base<_CharT>::size_type size_type;
	//  };
	//
	//  template<typename _CharT>
	//  struct BasicString : public VersaString<_CharT> {
	//    typedef typename VersaString<_CharT>::size_type size_type;
	//    BasicString substr(size_type pos) const;
	//  };
	//
	//  void test(BasicString<char> s) {
	//    s.substr(0);
	//  }
	public void testResolutionOfUnknownBindings_262328() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertNonProblem("substr(0)", 6, ICPPMethod.class);
	}

	// class C {};
	// template<typename T> class XT {
	//    T field;
	//    void bla() {
	//       C c;
	//       field.m(c);
	//    }
	// };
	public void testResolutionOfUnknownFunctions() throws Exception {
		parseAndCheckBindings();
	}

	// class C {};
	// template<typename T> class XT {
	//    T field;
	//    void bla() {
	//       C c;
	//       field[0].m(c);
	//    }
	// };
	public void testResolutionOfUnknownArrayAccess() throws Exception {
		parseAndCheckBindings();
	}

	// template <typename T> class CT {
	// public:
	//    void append(unsigned int __n, T __c) {}
	//    template<class P> void append(P __first, P __last) {}
	// };
	// void test() {
	//    CT<char> x;
	//    x.append(3, 'c');
	// }
	public void testConflictInTemplateArgumentDeduction() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPMethod m = bh.assertNonProblem("append(3", 6);
		assertFalse(m instanceof ICPPTemplateInstance);
	}

	//	struct A {
	//	  void m() const;
	//	};
	//
	//	template<typename T>
	//	struct B : public A {
	//	};
	//
	//	typedef B<char> C;
	//
	//	void test(const C& p) {
	//	  p.m();
	//	}
	public void testConversionSequence_263159() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPMethod m = bh.assertNonProblem("m();", 1, ICPPMethod.class);
	}

	//	template <class C> class A;
	//
	//	template <class C>
	//	A<C> make_A(C* p);
	//
	//	template <class C>
	//	struct A {
	//	  A(C* p);
	//	  friend A<C> make_A<C>(C* p);
	//	};
	//
	//	template <class C>
	//	A<C> make_A(C* p) {
	//	  return A<C>(p);
	//	}
	public void testForwardDeclarations_264109() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertNonProblem("A<C> make_A(C* p) {", 4, ICPPTemplateInstance.class);
		parseAndCheckBindings(getAboveComment());
	}

	//	template <typename T> class CT {
	//		public:
	//			template <typename U> CT(U u) {}
	//	};
	//	template <typename T> void any(T t) {}
	//	void test() {
	//		int* iptr;
	//		any(CT<int>(iptr));
	//	}
	public void testConstructorTemplateInClassTemplate_264314() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T> class XT {};
	//	template <typename T> void func(T t, XT<typename T::A> a) {}
	//	template <typename T, typename S> void func(S s, XT<typename S::A> a, T t) {}
	//
	//	class X {typedef int A;};
	//	class Y {typedef X A;};
	//
	//	void test() {
	//	    X x; Y y;
	//	    XT<int> xint; XT<X> xy;
	//	    func(x, xint);
	//	    func(y, xy, xint);
	//	}
	public void testDistinctDeferredInstances_264367() throws Exception {
		parseAndCheckBindings();
	}

	// template <typename T> class XT {
	//    void m(T t) {
	//       m(0); // ok with a conversion from 0 to T
	//    }
	// };
	public void testUnknownParameter_264988() throws Exception {
		parseAndCheckBindings();
	}

	//	template<int V>
	//	struct A {
	//	  enum E { e };
	//	};
	//
	//	int x = A<0>::e;
	//	A<0>::E y;
	public void testEnumeratorInTemplateInstance_265070() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T> class CT {};
	//	template<class T> CT<T>& getline1(CT<T>& __in);
	//	template<class T> CT<T>& getline2(CT<T>& __in);
	//	void test() {
	//		CT<int> i;
	//		getline2(i);
	//	}
	public void testAmbiguousDeclaratorInFunctionTemplate_265342() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertNonProblem("getline2(i)", 8, ICPPTemplateInstance.class);
		parseAndCheckBindings(getAboveComment());
	}

	// class C {
	//   friend int f1(int);
	// };
	// template <typename T> class CT {
	//   template <typename S> friend int f2(S);
	// };
	// template <typename T1> class C1 {
	//   template <typename T2> class C2 {
	//      template<typename T3> class C3 {
	//      };
	//   };
	// };
	public void testOwnerOfFriendTemplate_265671() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		IFunction f = bh.assertNonProblem("f1(", 2, IFunction.class);
		IBinding owner = f.getOwner();
		assertNull(owner);
		ICPPFunctionTemplate ft = bh.assertNonProblem("f2(", 2, ICPPFunctionTemplate.class);
		owner = f.getOwner();
		assertNull(owner);
		ICPPTemplateParameter tpar = ft.getTemplateParameters()[0];
		assertEquals(0, tpar.getTemplateNestingLevel());

		tpar = bh.assertNonProblem("T1", 2, ICPPTemplateParameter.class);
		assertEquals(0, tpar.getTemplateNestingLevel());
		tpar = bh.assertNonProblem("T2", 2, ICPPTemplateParameter.class);
		assertEquals(1, tpar.getTemplateNestingLevel());
		tpar = bh.assertNonProblem("T3", 2, ICPPTemplateParameter.class);
		assertEquals(2, tpar.getTemplateNestingLevel());

		parseAndCheckBindings(getAboveComment());
	}

	//	template<typename T>
	//	struct A {
	//	  template<typename U>
	//	  friend void f(const A<U>& p) {}
	//	  template<typename U>
	//	  void m(const A<U>& p) const;
	//	};
	//
	//	void test(const A<int>& a) {
	//	  f(a);
	//	  a.m(a);
	//	}
	public void testOwnerOfFriendTemplateFunction_408181() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPFunction f = bh.assertNonProblemOnFirstIdentifier("f(a)");
		assertNull(f.getOwner());
		ICPPClassType A = bh.assertNonProblem("A<int>");
		assertEquals(A, bh.assertNonProblemOnFirstIdentifier("m(a);").getOwner());
	}

	// template <typename T> void f(T t) {
	//     g(t);
	// }
	// template <typename T> void g(T t) {}
	public void testDependentNameReferencingLaterDeclaration_265926a() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		IFunction gref = bh.assertNonProblem("g(t)", 1);
		assertInstance(gref, ICPPUnknownBinding.class);
		IFunction gdecl = bh.assertNonProblem("g(T t)", 1);

		parseAndCheckBindings(getAboveComment());
	}

	//	class C;
	//	C* c(void*) {return 0;}
	//
	//	template <typename T> class XT {
	//		void m();
	//		C* ptr() {return 0;}
	//	};
	//
	//	template <typename T> void XT<T>::m() {
	//		c(this)->a();
	//		ptr()->a();
	//	};
	//
	//	class C {
	//		void a() {};
	//	};
	public void testDependentNameReferencingLaterDeclaration_265926b() throws Exception {
		parseAndCheckBindings();
	}

	// template<typename T> class XT {
	//    operator T() {return 0;}
	//    void m() {
	//       XT<T*> xt;
	//       xt.operator T*()->something();
	//    }
	// };
	public void testDeferredConversionOperator() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T> class X {};
	//	template <typename T> class X1 {
	//		friend class X<T>;
	//	};
	//	template <typename T> class Y : X1<int> {
	//		void test() {
	//			X<int> x;
	//		}
	//	};
	public void testFriendClassTemplate_266992() throws Exception {
		parseAndCheckBindings();
	}

	//	template <int N>
	//	void S(int (&array)[N]);
	//
	//	int a[1];
	//	void test() {
	//	  S(a);
	//	}
	public void testFunctionTemplateWithArrayReferenceParameter_269926() throws Exception {
		parseAndCheckBindings();
	}

	//	typedef unsigned int uint;
	//	template <uint N>
	//	void S(int (&array)[N]);
	//
	//	int a[1];
	//	void test() {
	//	  S(a);
	//	}
	public void testFunctionTemplateWithArrayReferenceParameter_394024() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {};
	//
	//	struct B {
	//	  template <typename T>
	//	  operator A<T>();
	//	};
	//
	//	void f(A<int> p);
	//
	//	void test(B p) {
	//	  f(p);
	//	}
	public void testTemplateConversionOperator_271948a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {
	//	};
	//
	//	template <class U>
	//	struct B {
	//	  template <typename T>
	//	  operator A<T>();
	//	};
	//
	//	void f(const A<char*>& p);
	//
	//	void test(B<int> x) {
	//	  f(x);
	//	}
	public void testTemplateConversionOperator_271948b() throws Exception {
		parseAndCheckBindings();
	}

	//	class Mat {};
	//
	//	template <typename T>
	//	class Mat_ {};
	//
	//	class MatExpr {
	//	public:
	//	    operator Mat();
	//
	//	    template <typename T>
	//	    operator Mat_<T>();
	//	};
	//
	//	Mat x = MatExpr();
	public void testOverloadedConversionOperators_550397() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//	template<unsigned int> struct ST{};
	//	template<template<unsigned int> class T> class CT {};
	//	typedef CT<ST> TDef;
	public void testUsingTemplateTemplateParameter_279619() throws Exception {
		parseAndCheckBindings();
	}

	//	template <int N> void T(int (&array)[N]) {};
	//	void test() {
	//	  int a[2];
	//	  T<2>(a);
	//	}
	public void testInstantiationOfArraySize_269926() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T> class CT {
	//		void init();
	//	};
	//	void CT<int>::init(void) {
	//	}
	public void testMethodSpecialization_322988() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename... Ts>
	//	struct E {};
	//
	//	template<int I, typename T>
	//	struct D;
	//
	//	template<int I, typename T, typename... Us>
	//	struct D<I, E<T, Us...>> : D<I - 1, E<Us...>> {};
	//
	//	template<typename T, typename... Us>
	//	struct D<0, E<T, Us...>> {
	//	  typedef T type;
	//	};
	//
	//	template <typename... Ts>
	//	class A;
	//
	//	template <typename T>
	//	struct F {
	//	  using type = T;
	//	};
	//
	//	template <int N, typename T>
	//	struct C;
	//
	//	template <int N, typename... Ts>
	//	struct C<N, A<Ts...>> {
	//	  using type = typename D<N, E<F<Ts>...>>::type::type;
	//	};
	//
	//	template <int I, typename T>
	//	struct B : public B<I - 1, T> {
	//	  using U = typename C<I, T>::type;
	//	  using Base = B<I - 1, T>;
	//	  using Base::Base;
	//
	//	  B(const U& value);
	//	};
	//
	//	template <typename... Ts>
	//	struct B<-1, A<Ts...>> {};
	//
	//	template <typename... Ts>
	//	struct A : public B<sizeof...(Ts) - 1, A<Ts...>> {
	//	  using B = B<sizeof...(Ts) - 1, A>;
	//	  using B::B;
	//	};
	//
	//	struct X {};
	//	struct Y {};
	//
	//	void waldo(const A<X, Y>& p);
	//
	//	void test() {
	//	  waldo(X());
	//	  waldo(Y());
	//	}
	public void testInheritedConstructor_489710() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {
	//	  typedef A<T> Self;
	//	  friend Self f(Self p) { return Self(); }
	//	};
	//
	//	void test(A<int> x) {
	//	  f(x);
	//	}
	public void testInlineFriendFunction_284690() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {
	//	  typedef A<T> Self;
	//	  friend void f(Self p) {}
	//	};
	//	template <typename U>
	//	struct B {
	//	  typedef B<U> Self;
	//	  friend void f(Self p) {}
	//	};
	//
	//	void test(A<int> x) {
	//	  f(x);
	//	}
	public void testInlineFriendFunction_287409() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPFunction func = bh.assertNonProblem("f(x)", 1, ICPPFunction.class);
		assertFalse(func instanceof ICPPUnknownBinding);
	}

	//	class NullType {};
	//	template <typename T, typename U> struct TypeList {
	//	   typedef T Head;
	//	   typedef U Tail;
	//	};
	//
	//	template <typename T1 = NullType, typename T2 = NullType> struct CreateTL {
	//	    typedef TypeList<T1, typename CreateTL<T2>::Type> Type;
	//	};
	//
	//	template<> struct CreateTL<NullType, NullType> {
	//	   typedef NullType Type;
	//	};
	public void testDefaultArgument_289132() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T> class XT {
	//		void n() {
	//			m(); // ok
	//		}
	//		void m() const {
	//			n();  // must be a problem
	//		}
	//	};
	public void testResolutionOfNonDependentNames_293052() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPFunction func = bh.assertNonProblem("m();", 1, ICPPFunction.class);
		assertFalse(func instanceof ICPPUnknownBinding);
		bh.assertProblem("n();", 1);
	}

	//	template<class T> struct CT {};
	//	class D : public CT<char> {};
	//	template<typename S> void f1(const CT<S> &) {}
	//	template<typename S> void f2(const CT<S> *) {}
	//	template<typename S> void f3(CT<S> *) {}
	//	template<typename S> void f4(volatile S*) {}
	//	void t() {
	//		D d;
	//		const volatile int *i= 0;
	//		const D cd= *new D();
	//		f1(d);
	//		f2(&d);
	//		f2(&cd);
	//		f3(&d);
	//		f4(i);
	//		f3(&cd);  // must be a problem, cd is const
	//	}
	public void testArgumentDeduction_293409() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		bh.assertNonProblem("f1(d);", 2, ICPPFunction.class);
		bh.assertNonProblem("f2(&d);", 2, ICPPFunction.class);
		bh.assertNonProblem("f2(&cd);", 2, ICPPFunction.class);
		bh.assertNonProblem("f3(&d);", 2, ICPPFunction.class);
		bh.assertNonProblem("f4(i);", 2, ICPPFunction.class);
		bh.assertProblem("f3(&cd);", 2);
	}

	//	template<typename T> struct C {};
	//	template<typename T, typename V> void f(T, C<V>) {}
	//	template<typename T> void f(T, C<int>) {}
	//
	//	void test() {
	//		char ch;
	//		C<int> cint;
	//		C<char> cchar;
	//		f(ch, cchar);
	//		f(ch, cint);
	//	}
	public void testFunctionTemplateOrdering_293468() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T> void func(T* t) {};
	//	template <typename T> void func(T& t) {};
	//	void test() {
	//	  int* a;
	//	  func(a);
	//	}
	//
	//	template <typename T> void func1(const T* const t) {};
	//	template <typename T> void func1(T* const t) {};
	//	void test2() {
	//	  const int* a;
	//	  func1  (a);
	//	}
	public void testFunctionTemplateOrdering_294539() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename A>
	//	void foo(A);
	//	template <typename A, typename... B>
	//	void foo(A, B...);
	//	int main() {
	//	    foo(0);
	//	}
	public void testFunctionTemplateOrdering_DR1395_388805() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		ICPPFunctionTemplate f1 = bh.assertNonProblem("foo(A)", 3);
		ICPPFunctionTemplate f2 = bh.assertNonProblem("foo(A, B...)", 3);

		ICPPTemplateInstance t;
		t = bh.assertNonProblem("foo(0)", 3);
		assertSame(f1, t.getTemplateDefinition());
	}

	//	template <typename T>
	//	struct identity {
	//	    typedef T type;
	//	};
	//
	//	template <typename T>
	//	void foo(typename identity<T>::type);
	//
	//	template <typename T>
	//	void foo(T);
	//
	//	int main() {
	//	    foo<int>(0);  // ERROR HERE
	//	}
	public void testFunctionTemplateOrdering_409094a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct identity {
	//	    typedef T type;
	//	};
	//
	//	template <typename> struct W;
	//
	//	template <typename T>
	//	struct A {
	//	    typedef typename identity<T>::type type1;
	//	    typedef W<type1> type2;
	//	};
	//
	//	template<typename T>
	//	void foo(typename identity<T>::type);
	//
	//	template <class T>
	//	void foo(T);
	//
	//	struct waldo {};
	//
	//	int main() {
	//	    waldo w;
	//	    foo<waldo>(w);  // ERROR HERE
	//	}
	public void testFunctionTemplateOrdering_409094b() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T> class CT {};
	//	template<int I> class CTI {};
	//
	//	int test() {
	//		int a;
	//		CT<CT<int>> x;
	//		a= 1 >> 2;
	//		return a;
	//	}
	public void testClosingAngleBrackets1_261268() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T> class CT {};
	//	template<int I> class CTI {};
	//
	//	int test() {
	//		int a;
	//		a= 1 > > 3;         // must be syntax error
	//		return a;
	//	}
	public void testClosingAngleBrackets2_261268() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parse(code, CPP, true, false);
		IASTFunctionDefinition fdef = getDeclaration(tu, 2);
		IASTProblemStatement p1 = getStatement(fdef, 1);
	}

	//	template<typename T> class CT {};
	//  typedef int TInt;
	//	int test() {
	//		int a;
	//		CT<CT<TInt>> x; // declaration
	//      int y= a<a<a>> a;      // binary expression
	//		a<a<a>> a;      		// binary expression via ambiguity
	//      y= a < a >> (1+2);	    // binary expression
	//      a < a >> (1+2);	   		// binary expression via ambiguity
	//	}
	public void testClosingAngleBracketsAmbiguity_261268() throws Exception {
		parseAndCheckBindings();
	}

	//	#define OPASSIGN(x) x##=
	//	int test() {
	//		int a=1;
	//      a OPASSIGN(>>) 1;
	//	}
	public void testTokenPasteShiftROperator_261268() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class T> class X {
	//	    void f(const T&);
	//	    void g(T&&);
	//	};
	//	X<int&> x1;         // X<int&>::f has the parameter type int&
	//	                    // X<int&>::g has the parameter type int&
	//	X<const int&&> x2;  // X<const int&&>::f has the parameter type const int&
	//	                    // X<const int&&>::g has the parameter type const int&&
	public void testRValueReferences_294730() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		ICPPClassType type = bh.assertNonProblem("X<int&>", 7);
		ICPPMethod[] ms = ClassTypeHelper.getMethods(type);
		int i = ms[0].getName().equals("f") ? 0 : 1;
		ICPPMethod m = ms[i];
		assertEquals("int &", ASTTypeUtil.getType(m.getType().getParameterTypes()[0]));
		m = ms[1 - i];
		assertEquals("int &", ASTTypeUtil.getType(m.getType().getParameterTypes()[0]));

		type = bh.assertNonProblem("X<const int&&>", 14);
		ms = ClassTypeHelper.getMethods(type);
		i = ms[0].getName().equals("f") ? 0 : 1;
		m = ms[i];
		assertEquals("const int &", ASTTypeUtil.getType(m.getType().getParameterTypes()[0]));
		m = ms[1 - i];
		assertEquals("const int &&", ASTTypeUtil.getType(m.getType().getParameterTypes()[0]));
	}

	//	template<typename... Pack> void f1(int (* p)(Pack ...a));
	//	template<typename... Pack> void f2(int (* ...p)(Pack a, int));
	//	template<typename... Pack> void f3(Pack (* ...p)());
	//  template<int... ipack> void f4(int (&...p)[ipack]);
	//  template<typename... Pack> void f5(Pack ...);
	//  template<typename NonPack> void f6(NonPack ...);
	//  template<typename... T> void f7() throw(T...);
	public void testFunctionParameterPacks_280909() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPFunctionTemplate f = bh.assertNonProblem("f1", 2);
		assertEquals("void (int (*)(#0(...) ...))", ASTTypeUtil.getType(f.getType(), true));
		assertFalse(f.getParameters()[0].isParameterPack());
		f = bh.assertNonProblem("f2", 2);
		assertEquals("void (int (* ...)(#0(...), int))", ASTTypeUtil.getType(f.getType(), true));
		assertTrue(f.getParameters()[0].isParameterPack());
		f = bh.assertNonProblem("f3", 2);
		assertEquals("void (#0(...) (* ...)())", ASTTypeUtil.getType(f.getType(), true));
		assertTrue(f.getParameters()[0].isParameterPack());
		f = bh.assertNonProblem("f4", 2);
		assertEquals("void (int (& ...)[3 *0 0])", ASTTypeUtil.getType(f.getType(), true));
		assertTrue(f.getParameters()[0].isParameterPack());
		f = bh.assertNonProblem("f5", 2);
		assertEquals("void (#0(...) ...)", ASTTypeUtil.getType(f.getType(), true));
		assertTrue(f.getParameters()[0].isParameterPack());
		f = bh.assertNonProblem("f6", 2);
		assertEquals("void (#0, ...)", ASTTypeUtil.getType(f.getType(), true));
		assertFalse(f.getParameters()[0].isParameterPack());
		f = bh.assertNonProblem("f7", 2);
		assertEquals("#0(...) ...", ASTTypeUtil.getType(f.getExceptionSpecification()[0], true));
	}

	//	template<typename... Pack> class C1 {};
	//	template<template<typename... NP> class... Pack> class C2 {};
	//	template<int... Pack> class C3 {};
	public void testTemplateParameterPacks_280909() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPClassTemplate ct = bh.assertNonProblem("C1", 2);
		ICPPTemplateParameter tp = ct.getTemplateParameters()[0];
		assertTrue(tp.isParameterPack());

		ct = bh.assertNonProblem("C2", 2);
		tp = ct.getTemplateParameters()[0];
		assertTrue(tp.isParameterPack());

		ct = bh.assertNonProblem("C3", 2);
		tp = ct.getTemplateParameters()[0];
		assertTrue(tp.isParameterPack());
	}

	//	template <typename... Pack> class CT : public Pack... {
	//		void mem() throw(Pack...);
	//	};
	//	struct A {int a;};
	//	struct B {int b;};
	//
	//	void test() {
	//		CT<A,B> c;
	//		c.a= 1;
	//		c.b= 1;
	//		c.mem();
	//	}
	public void testParameterPackExpansions_280909() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPField field = bh.assertNonProblem("a= 1", 1);
		field = bh.assertNonProblem("b= 1", 1);

		ICPPMethod meth = bh.assertNonProblem("mem();", 3);
		IType[] spec = meth.getExceptionSpecification();
		assertEquals(2, spec.length);
		assertEquals("A", ASTTypeUtil.getType(spec[0]));
		assertEquals("B", ASTTypeUtil.getType(spec[1]));
	}

	//	template<typename... T> void f1(T*...);
	//	template<typename T> void f2(T*...);
	public void testTemplateParameterPacksAmbiguity_280909() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPFunctionTemplate ft = bh.assertNonProblem("f1", 2);
		ICPPTemplateParameter tp = ft.getTemplateParameters()[0];
		assertTrue(tp.isParameterPack());

		ft = bh.assertNonProblem("f2", 2);
		tp = ft.getTemplateParameters()[0];
		assertFalse(tp.isParameterPack());
	}

	//	template <int ...I> struct CTx {};
	//	void test() {
	//		CTx<> a;
	//		CTx<1> b;
	//		CTx<1,2> c;
	//	}
	public void testNonTypeTemplateParameterPack_280909() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename... Types>
	//	struct count { static const int value = sizeof...(Types);
	//	};
	public void testVariadicTemplateExamples_280909a() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename... T> void f(T (* ...t)(int, int));
	//	int add(int, int);
	//	float subtract(int, int);
	//	void g() {
	//		f(add, subtract);
	//	}
	public void testVariadicTemplateExamples_280909b() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename... Mixins>
	//	class X : public Mixins...
	//	{public:
	//	X(const Mixins&... mixins) : Mixins(mixins)... { }
	//	};
	public void testVariadicTemplateExamples_280909c() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class... Types> class Tuple; // Types is a template type parameter pack
	//	template<class T, int... Dims> struct multi array; // Dims is a non-type template parameter pack
	public void testVariadicTemplateExamples_280909d() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class T = char> class String;
	//	String<>* p; // OK: String<char>
	//	String* q; // syntax error
	//	template<typename ... Elements> class Tuple;
	//	Tuple<>* t; // OK: Elements is empty
	//	Tuple* u; // syntax error
	public void testVariadicTemplateExamples_280909e() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		bh.assertNonProblem("String<>", 6);
		bh.assertProblem("String*", 6);
		bh.assertNonProblem("Tuple<>", 5);
		bh.assertProblem("Tuple*", 5);
	}

	//	template<class T> class A {};
	//	template<class T, class U = T> class B {};
	//	template<class... Types> class C {};
	//	template<template<class> class P> class X {};
	//	template<template<class...> class Q> class Y {};
	//	X<A> xa; // okay
	//	X<B> xb; // ill-formed: default arguments for the parameters of a template template argument are ignored
	//	X<C> xc; // ill-formed: a template parameter pack does not match a template parameter
	//	Y<A> ya; // okay
	//	Y<B> yb; // okay
	//	Y<C> yc; // okay
	public void testVariadicTemplateExamples_280909f() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		bh.assertNonProblem("X<A>", 4);
		bh.assertProblem("X<B>", 4);
		bh.assertProblem("X<C>", 4);
		bh.assertNonProblem("Y<A>", 4);
		bh.assertNonProblem("Y<B>", 4);
		bh.assertNonProblem("Y<C>", 4);
	}

	//	template<class T1, class T2> struct A {
	//     void f1();
	//	   void f2();
	//	};
	//	template<class... Types> struct B {
	//    void f3();
	//	  void f4();
	//	};
	//	template<class T2, class T1> void A<T2,T1>::f1() {} // OK
	//	template<class T2, class T1> void A<T1,T2>::f2() {} // error
	//	template<class... Types> void B<Types...>::f3() {} // OK
	//	template<class... Types> void B<Types>::f4() {} // error
	public void testVariadicTemplateExamples_280909g() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		bh.assertNonProblem("f1() {}", 2);
		bh.assertProblem("f2() {}", 2);
		bh.assertNonProblem("f3() {}", 2);
		bh.assertProblem("f4() {}", 2);
	}

	//	template<class X, class Y> X f(Y);
	//	template<class X, class Y, class... Z> X g(Y);
	//	void gh() {
	//	  int i = f<int>(5.6); 		// Y is deduced to be double
	//	  int j = f(5.6); 			// ill-formed: X cannot be deduced
	//	  f<void>(f<int, bool>); 	// Y for outer f deduced to be
	//								// int (*)(bool)
	//	  f<void>(f<int>); 			// ill-formed: f<int> does not denote a
	//								// single function template specialization
	//	  int k = g<int>(5.6); 		// Y is deduced to be double, Z is deduced to an empty sequence
	//	  f<void>(g<int, bool>); 	// Y for outer f deduced to be
	//  }							// int (*)(bool), Z is deduced to an empty sequence
	public void testVariadicTemplateExamples_280909h() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		bh.assertNonProblem("f<int>(5.6)", 6);
		bh.assertProblem("f(5.6)", 1);
		bh.assertNonProblem("f<void>(f<int, bool>)", 7);
		bh.assertProblem("f<void>(f<int>)", 7);
		bh.assertNonProblem("g<int>(5.6)", 6);
		bh.assertNonProblem("f<void>(g<int, bool>)", 7);
	}

	// template<class X, class Y, class Z> X f(Y,Z);
	// template<class... Args> void f2();
	// void g() {
	//   f<int,char*,double>("aa",3.0);
	//   f<int,char*>("aa",3.0); 		// Z is deduced to be double
	//   f<int>("aa",3.0); 				// Y is deduced to be const char*, and
	// 									// Z is deduced to be double
	//   f("aa",3.0); 					// error: X cannot be deduced
	//   f2<char, short, int, long>(); 	// okay
	// }
	public void testVariadicTemplateExamples_280909i() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		bh.assertNonProblem("f<int,char*,double>", 0);
		bh.assertNonProblem("f<int,char*>", 0);
		bh.assertNonProblem("f<int>", 0);
		bh.assertProblem("f(\"aa\",3.0)", 1);
		bh.assertNonProblem("f2<char, short, int, long>", 0);
	}

	//	template<typename... Types> void f(Types... values);
	//	void g() {
	//		f<int*, float*>(0, 0, 0); // Types is the sequence int*, float*, int
	//	}
	public void testVariadicTemplateExamples_280909j() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class... Types> void f(Types&...);
	//	template<class T1, class... Types> void g(T1, Types...);
	//	void h(int x, float& y) {
	//	  const int z = x;
	//	  f(x, y, z); // Types is deduced to int, float, const int
	//	  g(x, y, z); // T1 is deduced to int, Types is deduced to float, int
	//	}
	public void testVariadicTemplateExamples_280909k() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class> struct X { };
	//	template<class R, class... ArgTypes> struct X<R(int, ArgTypes...)> { };
	//	template<class... Types> struct Y { };
	//	template<class T, class... Types> struct Y<T, Types&...> { };
	//	template <class... Types> int f (void (*)(Types...));
	//	void g(int, float);
	//	X<int> x1; // uses primary template
	//	X<int(int, float, double)> x2; // uses partial specialization, ArgTypes contains float, double
	//	X<int(float, int)> x3; // uses primary template
	//	Y<> y1; // uses primary template, Types is empty
	//	Y<int&, float&, double&> y2; // uses partial specialization. T is int&, Types contains float, double
	//	Y<int, float, double> y3; // uses primary template, Types contains int, float, double
	//	int fv = f(g); // okay, Types contains int, float
	public void testVariadicTemplateExamples_280909n() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename... Types> struct Tuple { };
	//  void test() {
	//	  Tuple<> t0; // Types contains no arguments
	//	  Tuple<int> t1; // Types contains one argument: int
	//	  Tuple<int, float> t2; // Types contains two arguments: int and float
	//	  Tuple<0> error; // Error: 0 is not a type
	// }
	public void testVariadicTemplateExamples_280909p() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		bh.assertNonProblem("Tuple<>", 0);
		bh.assertNonProblem("Tuple<int>", 0);
		bh.assertNonProblem("Tuple<int, float>", 0);
		bh.assertProblem("Tuple<0>", 0);
	}

	//	template<typename... Types> void f(Types... args);
	//  void test() {
	//	  f(); // okay: args contains no arguments
	//	  f(1); // okay: args contains one int argument
	//	  f(2, 1.0); // okay: args contains two arguments, an int and a double
	// }
	public void testVariadicTemplateExamples_280909q() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename... Types>	void f(Types... rest);
	//	template<typename... Types> void g(Types... rest) {
	//	   f(&rest...); // '&rest...' is a pack expansion, '&rest' is its pattern
	//	}
	public void testVariadicTemplateExamples_280909r() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename...> struct Tuple {};
	//	template<typename T1, typename T2> struct Pair {};
	//	template<typename... Args1>	struct zip {
	//     template<typename... Args2> struct with {
	//        typedef Tuple<Pair<Args1, Args2>...> type;
	//	   };
	//	};
	//	typedef zip<short, int>::with<unsigned short, unsigned>::type T1;
	//			// T1 is Tuple<Pair<short, unsigned short>, Pair<int, unsigned> >
	//	typedef zip<short>::with<unsigned short, unsigned>::type T2;
	//			// error: different number of arguments specified
	//			// for Args1 and Args2
	//	template<typename... Args> void f(Args... args) {}
	//	template<typename... Args> void h(Args... args) {}
	//	template<typename... Args> void g(Args... args) {
	//	  f(const_cast<const Args*>(&args)...); // okay: 'Args' and 'args' are expanded
	//	  f(5 ...); // error: pattern does not contain any parameter packs
	//	  f(args); // error: parameter pack 'args' is not expanded
	//	  f(h(args...) + args...); // okay: first 'args' expanded within h, second 'args' expanded within f.
	//	}
	public void testVariadicTemplateExamples_280909s() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ITypedef td = bh.assertNonProblem("T1;", 2);
		IType type = getNestedType(td, TDEF);
		assertEquals("Tuple<Pair<short int,unsigned short int>,Pair<int,unsigned int>>",
				ASTTypeUtil.getType(type, false));
		td = bh.assertNonProblem("zip<short>::with<unsigned short, unsigned>::type", 0);
		type = getNestedType(td, TDEF);
		assertTrue(type instanceof IProblemBinding);

		ICPPUnknownBinding ub;
		ub = bh.assertNonProblem("f(const_cast<const Args*>(&args)...)", 1);
		ub = bh.assertNonProblem("f(5 ...)", 1); // no diagnostics in CDT, treated as unknown function.
		ub = bh.assertNonProblem("f(args)", 1); // no diagnostics in CDT
		ub = bh.assertNonProblem("f(h(args...) + args...)", 1);
	}

	//	template <typename... Args>
	//	struct contains_waldo;
	//	template <>
	//	struct contains_waldo<> {
	//	    static const bool value = false;
	//	};
	//	template <typename First, typename... Rest>
	//	struct contains_waldo<First, Rest...> {
	//	    static const bool value = contains_waldo<Rest...>::value;
	//	};
	//	int main() {
	//	    bool b1 = contains_waldo<int>::value;
	//	    bool b2 = contains_waldo<int, int>::value;
	//	    bool b2 = contains_waldo<int, int, int>::value;
	//	}
	public void testRecursiveVariadicTemplate_397828() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename... T>
	//	struct A {
	//	  static int waldo(T... p, int q);
	//	};
	//
	//	int x = A<>::waldo(0);
	public void testVariadicTemplateWithNoArguments_422700() throws Exception {
		parseAndCheckBindings();
	}

	//	struct Test {
	//		void Update() {}
	//	};
	//	template<class R, class T> void bind(R (T::*f) ()) {}
	//	template<class R, class T> void bind(R T::*f) {}
	//
	//	void test() {
	//		bind(&Test::Update);
	//	}
	public void testFunctionOrdering_299608() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class T, class U = double> void f(T t = 0, U u = 0);
	//    void g() {
	//        f(1, 'c');         // f<int,char>(1,'c')
	//        f(1);              // f<int,double>(1,0)
	//        f();               // error: T cannot be deduced
	//        f<int>();          // f<int,double>(0,0)
	//        f<int,char>();     // f<int,char>(0,0)
	//    }
	public void testDefaultTemplateArgsForFunctionTemplates_294730() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		ICPPTemplateInstance f = bh.assertNonProblem("f(1, 'c');", 1);
		assertEquals("<int,char>", ASTTypeUtil.getArgumentListString(f.getTemplateArguments(), true));
		f = bh.assertNonProblem("f(1);", 1);
		assertEquals("<int,double>", ASTTypeUtil.getArgumentListString(f.getTemplateArguments(), true));
		bh.assertProblem("f();", 1);
		f = bh.assertNonProblem("f<int>();", -3);
		assertEquals("<int,double>", ASTTypeUtil.getArgumentListString(f.getTemplateArguments(), true));
		f = bh.assertNonProblem("f<int,char>();", -3);
		assertEquals("<int,char>", ASTTypeUtil.getArgumentListString(f.getTemplateArguments(), true));
	}

	//	template<typename T> class CT {};
	//	extern template class CT<int>;
	public void testExternTemplates_294730() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code);
		ICPPASTExplicitTemplateInstantiation ti = getDeclaration(tu, 1);
		assertEquals(ICPPASTExplicitTemplateInstantiation.EXTERN, ti.getModifier());
	}

	//	template <class T> struct eval;
	//	template <template <class, class...> class TT, class T1, class... Rest>
	//	struct eval<TT<T1, Rest...>> { };
	//	template <class T1> struct A;
	//	template <class T1, class T2> struct B;
	//	template <int N> struct C;
	//	template <class T1, int N> struct D;
	//	template <class T1, class T2, int N = 17> struct E;
	//
	//	eval<A<int>> eA; // OK: matches partial specialization of eval
	//	eval<B<int, float>> eB; // OK: matches partial specialization of eval
	//	eval<C<17>> eC; // error: C does not match TT in partial specialization
	//	eval<D<int, 17>> eD; // error: D does not match TT in partial specialization
	//	eval<E<int, float>> eE; // error: E does not match TT in partial specialization
	public void testExtendingVariadicTemplateTemplateParameters_302282() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPClassTemplate ct = bh.assertNonProblem("eval;", -1);
		ICPPClassTemplatePartialSpecialization pspec = bh.assertNonProblem("eval<TT<T1, Rest...>>", 0);

		ICPPTemplateInstance inst = bh.assertNonProblem("eval<A<int>>", 0);
		assertSame(pspec, inst.getSpecializedBinding());

		inst = bh.assertNonProblem("eval<B<int, float>>", 0);
		assertSame(pspec, inst.getSpecializedBinding());

		inst = bh.assertNonProblem("eval<C<17>>", 0);
		assertSame(ct, inst.getSpecializedBinding());

		inst = bh.assertNonProblem("eval<D<int, 17>>", 0);
		assertSame(ct, inst.getSpecializedBinding());

		inst = bh.assertNonProblem("eval<E<int, float>>", 0);
		assertSame(ct, inst.getSpecializedBinding());
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
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		ICPPTemplateInstance inst;
		inst = bh.assertNonProblem("X<int>", 0);
		assertFalse(inst.isExplicitSpecialization());
		inst = bh.assertNonProblem("Y<int> y;", 6);
		assertTrue(inst.isExplicitSpecialization());

		inst = bh.assertNonProblem("f(1)", 1);
		assertFalse(inst.isExplicitSpecialization());
		inst = bh.assertNonProblem("g(1)", 1);
		assertTrue(inst.isExplicitSpecialization());
	}

	//	template <typename T> struct CT {
	//		CT ();
	//	};
	//	template<> struct CT<int> {
	//		CT ();
	//		int value_;
	//	};
	//	CT<int>::CT() :	value_(0) {
	//	}
	public void testConstructorOfExplicitSpecialization() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T> struct CT;
	//	template<> struct CT<int> {typedef int Type;};
	//	template <typename T> struct CT <const T> {
	//	  typedef const typename CT<T>::Type Type;
	//	};
	//	template <typename T> void func(typename CT<T>::Type unit) {
	//	}
	//	void test() {
	//	  func<int>(1);
	//	}
	public void testBug306213a() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		bh.assertNonProblem("func<int>", 0);
		parseAndCheckBindings(code);
	}

	//	template <typename T> struct CT;
	//	template <typename T> struct CT <T*> {
	//	  typedef const typename CT<T**>::Type Type;
	//	};
	//	template <typename T> void func(typename CT<T>::Type unit) {
	//	}
	//	void test() {
	//	  func<int*>(1);
	//	}
	public void testBug306213b() throws Exception {
		CPPASTNameBase.sAllowRecursionBindings = true;
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		bh.assertProblem("func<int*>", 0);
	}

	//	template <typename T> struct CT {
	//		typedef int T1;
	//	};
	//	template <typename T> struct CT <const T> {
	//	  typedef int T2;
	//	};
	//
	//	void test() {
	//		CT<int>::T1 a;
	//		CT<const int>::T2 b;
	//	}
	public void testBug306213c() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T1, typename T2> class CT {};
	//	template<> class CT<int,char> {};
	//	template<> class CT<char,char> {};
	public void testBug311164() throws Exception {
		CPPASTNameBase.sAllowNameComputation = true;
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		final IASTTranslationUnit tu = bh.getTranslationUnit();

		IBinding b = bh.assertNonProblem("CT {", 2);
		IName[] names = tu.getDeclarationsInAST(b);
		assertEquals(1, names.length);
		assertEquals("CT", names[0].toString());
		names = tu.getReferences(b);
		assertEquals(2, names.length);
		assertEquals("CT", names[0].toString());
		assertEquals("CT", names[1].toString());

		b = bh.assertNonProblem("CT<int,char>", 0);
		names = tu.getDeclarationsInAST(b);
		assertEquals(1, names.length);
		assertEquals("CT<int, char>", names[0].toString());

		b = bh.assertNonProblem("CT<char,char>", 0);
		names = tu.getDeclarationsInAST(b);
		assertEquals(1, names.length);
		assertEquals("CT<char, char>", names[0].toString());
	}

	// NOTE: If, after refactoring some AST code, this test hangs, check
	//		 if any methods that were added during the refactoring need
	//		 to be added to ASTComparer.methodsToIgnore.
	public void testBug316704() throws Exception {
		StringBuilder code = new StringBuilder("typedef if_< bool,");
		for (int i = 0; i < 50; i++) {
			code.append('\n').append("if_<bool,");
		}
		code.append("int_<0>,");
		for (int i = 0; i < 50; i++) {
			code.append('\n').append("int_<0> >::type,");
		}
		code.append("int_<0> >::type tdef;");
		IASTTranslationUnit tu = parse(code.toString(), CPP, true, true);
		tu = validateCopy(tu);
		assertEquals(1, tu.getDeclarations().length);
	}

	//	namespace A {
	//
	//	template <typename T>
	//	struct A {
	//	  A();
	//	};
	//
	//	template <typename U>
	//	A<U>::A() {}
	//
	//	A<int> a;
	//
	//	}
	public void testBug377838() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace N {
	//		inline namespace M {
	//			template<class T> void f(T&) { }
	//
	//		}
	//		template void f<char>(char&);
	//		template<> void f<short>(short&) {}
	//	}
	//
	//	template void N::f<int>(int&);
	//	template<> void N::f<long>(long&) {}
	public void testInlineNamespaces_305980() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPFunctionTemplate ft = bh.assertNonProblem("f(T&)", 1);
		ICPPNamespace M = (ICPPNamespace) ft.getOwner();

		ICPPTemplateInstance inst;
		inst = bh.assertNonProblem("f<char>", 0);
		assertSame(ft, inst.getTemplateDefinition());
		assertSame(M, inst.getOwner());

		inst = bh.assertNonProblem("f<short>", 0);
		assertSame(ft, inst.getTemplateDefinition());
		assertSame(M, inst.getOwner());

		inst = bh.assertNonProblem("f<int>", 0);
		assertSame(ft, inst.getTemplateDefinition());
		assertSame(M, inst.getOwner());

		inst = bh.assertNonProblem("f<long>", 0);
		assertSame(ft, inst.getTemplateDefinition());
		assertSame(M, inst.getOwner());
	}

	//	template <class T> struct A {
	//		friend void f(A, T){}
	//	};
	//	template <class T> void g(T t) {
	//		A<T> at;
	//		f(at, t);
	//	}
	//	int main() {
	//		class X {} x;
	//		g(x);
	//	}
	public void testUnnamedTypesAsTemplateArgument_316317a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class T> class X { };
	//	template <class T> void f(T t) { }
	//	struct {} unnamed_obj;
	//	void f() {
	//		struct A { };
	//		enum { e1 };
	//		typedef struct {} B;
	//		B b;
	//		X<A>  x1; // OK
	//		X<A*> x2; // OK
	//		X<B>  x3; // OK
	//		f(e1); // OK
	//		f(unnamed_obj); // OK
	//		f(b); // OK
	//	}
	public void testUnnamedTypesAsTemplateArgument_316317b() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//		int s;
	//	};
	//	struct X {
	//		template<typename T> S* operator+(T t) const {return 0;}
	//	};
	//	int* operator+(const X&, int *) {return 0;}
	//
	//	void test() {
	//		X x;
	//		(x + 1)->s;
	//	}
	public void testOverloadResolutionBetweenMethodTemplateAndFunction() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T, int N>
	//	char (&f(T (&a)[N]))[N];
	//
	//	template <typename T, int N>
	//	char (&f(const T (&a)[N]))[N];
	//
	//	const char c[] = "";
	//	int x = sizeof(f(c));
	//  const int d[] = { 0 };
	//	int y = sizeof(f(d));
	public void testOverloadedFunctionTemplate_407579() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename ...T> void f(T..., T...);
	//	void test() {
	//	  f(1,1);
	//	}
	public void testFunctionParameterPacksInNonFinalPosition_324096() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename _CharT> struct OutStream {
	//		OutStream& operator<<(OutStream& (*__pf)(OutStream&));
	//	};
	//	template<typename _CharT> OutStream<_CharT>& endl(OutStream<_CharT>& __os);
	//
	//	void test() {
	//		OutStream<char> out;
	//		out << endl;
	//	}
	public void testInstantiationOfEndl_297457() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code);
		final IASTNodeSelector nodeSelector = tu.getNodeSelector(null);

		IASTName methodName = nodeSelector.findEnclosingName(code.indexOf("operator<<"), 1);
		IASTImplicitName name = nodeSelector.findImplicitName(code.indexOf("<< endl"), 2);

		final IBinding method = methodName.resolveBinding();
		final IBinding reference = name.resolveBinding();
		assertSame(method, ((ICPPSpecialization) reference).getSpecializedBinding());
	}

	//	template <typename CharT>
	//	struct ostream {
	//	    template <typename T>
	//	    ostream& operator<<(T);
	//
	//	    ostream& operator<<(ostream&(*)(ostream&));
	//	};
	//
	//	template <typename CharT>
	//	ostream<CharT>& endl(ostream<CharT>&);
	//
	//	template <typename T>
	//	void test(T t) {
	//	    ostream<char> out;
	//	    out << t << endl;
	//	}
	public void testInstantiationOfEndlInTemplate_417700() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T> bool MySort(const T& a);
	//	bool MySort(const int& a);
	//	template<typename V> void sort(V __comp);
	//	void test() {
	//	    sort(MySort<int>);
	//	}
	public void testAdressOfUniqueTemplateInst_326076() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T> void f(T (*)(int), char);
	//	template <typename T> void f(int (*)(T), int);
	//	template <typename T> void f(T, int);
	//
	//	int g(char);
	//	void g(int);
	//
	//	void b() {
	//	  f(g, '1');
	//	  f(g, 1);
	//	}
	public void testInstantiationOfFunctionTemplateWithOverloadedFunctionSetArgument_326492() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPFunctionTemplate f1 = bh.assertNonProblem("f(T (*)(int), char)", 1);
		ICPPFunctionTemplate f2 = bh.assertNonProblem("f(int (*)(T), int)", 1);
		IFunction g1 = bh.assertNonProblem("g(char)", 1);
		IFunction g2 = bh.assertNonProblem("g(int)", 1);

		ICPPTemplateInstance t;
		t = bh.assertNonProblem("f(g, '1')", 1);
		assertSame(f1, t.getTemplateDefinition());
		t = bh.assertNonProblem("f(g, 1)", 1);
		assertSame(f2, t.getTemplateDefinition());

		ICPPFunction g;
		g = bh.assertNonProblem("g, '1')", 1);
		assertSame(g2, g);
		g = bh.assertNonProblem("g, 1)", 1);
		assertSame(g1, g);
	}

	//	template <class T> class Ptr{};
	//	namespace ns {
	//	  class T {};
	//	  void f(Ptr<T>);
	//	}
	//	void test() {
	//	  Ptr<ns::T> parm;
	//	  f(parm);
	//	}
	public void testADLForTemplateSpecializations_327069() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T, typename V> T* f(V*);
	//	template<typename T, typename V> T f(V*);
	//	template<typename T, typename V> T* f(V);
	//	void x(int* (*) (int*)) {
	//	  x(f);
	//	}
	public void testPartialOrderingInNonCallContext_326900() throws Exception {
		parseAndCheckBindings();
	}

	//	struct X {
	//	  template<typename T> operator T();
	//	  template<typename T> operator T*();
	//	};
	//	void y(int *) {
	//	  X x;
	//	  y(x);
	//	}
	public void testPartialOrderingForConversions_326900() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S { int foo; };
	//	template<typename T> struct L {
	//		  typedef T& CR;
	//		  template<bool> struct _CI {
	//			    CR m();
	//		  };
	//		  typedef _CI<true> CI;
	//	};
	//	void test() {
	//		  L<S>::CI l;
	//		  l.m().foo = 1;
	//	}
	public void testNestedTypedefSpecialization_329795() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);
		ICPPField f1 = bh.assertNonProblem("foo;", 3);
		IBinding f2 = bh.assertNonProblem("foo =", 3);
		assertSame(f1, f2);
	}

	//	template <class T> struct TestTmpl {
	//	  struct Inner1;
	//	  struct Inner2{
	//	    Inner1* ptr1;
	//	  };
	//	  struct Inner1{
	//	    Inner2* ptr2;
	//	  };
	//	};
	//	struct TestImpl:TestTmpl<int>{};
	//	void func(TestImpl::Inner1* ptr1) {
	//	  TestImpl::Inner2* ptr2=ptr1->ptr2;
	//	  func(ptr2->ptr1);
	//	}
	public void testSpecializationViaNotDirectlyEnclosingTemplate_333186() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T> struct A {
	//	    typedef T type;
	//	};
	//	template <typename T> struct X {
	//	    template <typename A<T>::type x> struct Y {};
	//	};
	//
	//	struct C {};
	//	template <class C& c> class Z{};
	public void testNonTypeTemplateParameterWithTypenameKeyword_333186() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T, typename U = int> void f() {
	//	    f<int>();
	//	}
	public void testDefaultTmplArgumentOfFunctionTemplate_333325() throws Exception {
		parseAndCheckBindings();
	}

	//	template <void (*Func)()> class X {};
	//	template <typename T> void Y();
	//	X< Y<int> > x;
	public void testFunctionInstanceAsTemplateArg_333529() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	class M {};
	//
	//	template<typename U>
	//	U F();
	//
	//	template <M<int> (*Func)()>
	//	struct G {
	//	  M<int> operator()();
	//	};
	//
	//	template <typename U>
	//	struct H : public G<F<U> > {};
	//
	//	H<M<int> > C;
	//
	//	template <typename T>
	//	void P(M<T> a);
	//
	//	void test() {
	//	  P(C());
	//	}
	public void testFunctionInstanceAsTemplateArg_334472() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T> void g() {}
	//	template <typename T, typename U> void g()  {}
	//	void test() {
	//	    g<int>();
	//	    g<int, int>();
	//	}
	public void testFunctionTemplateSignatures_335062() throws Exception {
		parseAndCheckBindings();
	}

	//	template <bool B, class T = void> struct enable_if {
	//		typedef T type;
	//	};
	//	template <class T> struct enable_if<false, T> {};
	//
	//	template <typename T> struct is_int {
	//		static const bool value = false;
	//	};
	//	template <> struct is_int<int> {
	//		static const bool value = true;
	//	};
	//
	//	template <typename T> typename enable_if<!is_int<T>::value>::type function(T);
	//	template <typename T> typename enable_if<is_int<T>::value>::type function(T);
	//
	//	void g() {
	//		function(0);  // ERROR HERE
	//	}
	public void testSyntaxErrorInReturnTypeOfFunctionInstance_336426() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T> struct A {};
	//	template <typename Functor> void f(Functor functor) {
	//	    A<decltype(functor())> a;
	//	}
	public void testFunctionCallOnDependentName_337686() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	void test(T* a) {
	//	  auto* b = a->f;
	//	  b->g;
	//	}
	public void testDependentNameWithAuto_407480() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {};
	//	template <typename... Args> void h(S s, Args... args) {}
	//	void g() {
	//	    S s;
	//	    h(s);
	//	    h(s, 1);
	//	    h(s, 1, 2);
	//	}
	public void testVariadicFunctionTemplate_333389() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T> void f(T(*)());
	//	template<typename T> void g(T(*)(void));
	//	void v1();
	//	void v2(void);
	//	void test() {
	//	  f(v1);
	//	  f(v2);
	//	  g(v1);
	//	  g(v2);
	//	}
	public void testFunctionWithVoidParamInTypeDeduction() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class T1, class T2, class U>
	//	void A(T1* obj, void (T2::*member)(U));
	//
	//	template <class T1, class T2>
	//	void A(T1* obj, void (T2::*member)());
	//
	//	class B {
	//	  void m1(void);
	//
	//	  void m2() {
	//	    A(this, &B::m1);
	//	  }
	//	};
	public void testFunctionWithVoidParamInTypeDeduction_423127() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A {
	//	  template <typename U>
	//	  void m(U);
	//	  int m(int);
	//	};
	//
	//	void foo(void(A::*)(int));
	//	void foo(int);
	//
	//	template <typename T>
	//	decltype(foo(&T::m)) waldo(T);
	//
	//	int main() {
	//	  waldo(A());
	//	}
	public void testAddressOfMethodTargeted_509396() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A {
	//	  template <typename U>
	//	  void m(U t);
	//	};
	//
	//	template <typename T>
	//	void waldo(T t);
	//
	//	template <typename T>
	//	decltype(&T::m) waldo(T t);
	//
	//	void test() {
	//	  waldo(A());
	//	}
	public void testAddressOfMethodUntargeted_509396() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T, unsigned length> struct Templ {
	//		Templ(){}
	//	};
	//
	//	template<> struct Templ<int, 5> {
	//		Templ(){}
	//      int e;
	//	};
	//
	//	template<unsigned length> struct Templ<int, length> {
	//		Templ(){}
	//	};
	//
	//	int main() {
	//		Templ<int, 5> iFive;
	//      iFive.e= 0;
	//		return 0;
	//	}
	public void testPartialSpecAfterExplicitInst_339475() throws Exception {
		parseAndCheckBindings();
	}

	//	template<bool> struct S {
	//		static int m();
	//	};
	//	template<int> void g(int);
	//	int f();
	//	int s;
	//
	//	void test() {
	//		f < 0 > (1);  // Function pointer
	//		g<0>(1);      // Function call
	//	    S<1 && 2>::m();        // m is member of S
	//	    s<1 && 2>::f();        // f is global
	//	}
	public void testTemplateIDAmbiguity_341747a() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		IASTFunctionDefinition fdef = getDeclaration(tu, 4);

		IASTExpressionStatement stmt;
		stmt = getStatement(fdef, 0);
		assertTrue(stmt.getExpression() instanceof IASTBinaryExpression);

		stmt = getStatement(fdef, 1);
		assertTrue(stmt.getExpression() instanceof IASTFunctionCallExpression);

		stmt = getStatement(fdef, 2);
		assertTrue(stmt.getExpression() instanceof IASTFunctionCallExpression);

		stmt = getStatement(fdef, 0);
		assertTrue(stmt.getExpression() instanceof IASTBinaryExpression);
	}

	//	const int a=0, b=1;
	//	template<int> struct A{};
	//
	//	template<bool B= a<b> struct S {};
	//	struct X : S<a<b> {};
	//
	//	template<typename B= A<b>> struct T {};
	//	struct Y : T<A<b>> {};
	public void testTemplateIDAmbiguity_341747b() throws Exception {
		parseAndCheckBindings();
	}

	//	int a=0, b=1;
	//	bool bl= false;
	//	template<bool B> struct S {
	//		int a;
	//	};
	//	void test() {
	//		S< a<b >::a;
	//		a < S<bl>::a;
	//	}
	public void testTemplateIDAmbiguity_341747c() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//		int B;
	//	};
	//	template<typename T> struct B {};
	//	int c;
	//	void test() {
	//		S* a=0;
	//		a->B<c && c>::c;
	//	}
	public void testTemplateIDAmbiguity_341747d() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T> void ft(void (T::* function)()) {}
	//	struct Bar {
	//	    template<typename T> Bar(void (T::*function)()) {}
	//	};
	//	struct Foo {
	//	    void function() {}
	//	    void function(int) {}
	//	};
	//	int test2() {
	//	    Bar test(&Foo::function); // Invalid overload of 'Foo::func tion'
	//	    ft(&Foo::function);
	//	    return 0;
	//	}
	public void testAddressOfMethodForInstantiation_344310() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename Arg> struct Callback {
	//	    Callback(void (*function)(Arg arg)) {}
	//	};
	//
	//	void Subscribe(const Callback<const int>& callback){}
	//	void CallMe(const int){}
	//
	//	int test() {
	//	    Subscribe(Callback<const int>(&CallMe)); // invalid arguments, symbol not
	//	}
	public void testParameterAdjustementInInstantiatedFunctionType_351609() throws Exception {
		parseAndCheckBindings();
	}

	// template<typename T> struct CT {
	//   int g;
	// };
	// template<typename T> struct CT<T&> {
	//    int ref;
	// };
	// template<typename T> struct CT<T&&> {
	//    int rref;
	// };
	// void test() {
	//    CT<int>::g;
	//    CT<int&>::ref;
	//    CT<int&&>::rref;
	// }
	public void testRRefVsRef_351927() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename = int> class A {};
	public void testTemplateParameterWithoutName_352266() throws Exception {
		parseAndCheckBindings();
	}

	//	template<template<typename, typename...> class T> struct CTTP{ };
	//
	//	template<typename T> struct CT1{ };
	//	template<typename T1, typename T2> struct CT2{ };
	//	template<typename T1, typename T2, typename T3> struct CT3{ };
	//	template<typename T1, typename T2, typename T3, typename... T4> struct CT4{ };
	//
	//	typedef CTTP<CT1> a;
	//	typedef CTTP<CT2> b;
	//	typedef CTTP<CT3> c;
	//	typedef CTTP<CT4> d;
	public void testTemplateTemplateParameterMatching_352859() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T> T f();
	//	template<> int f() {
	//	    return 0;
	//	}
	public void testArgumentDeductionFromReturnTypeOfExplicitSpecialization_355304() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPFunctionTemplate template = bh.assertNonProblem("f();", 1);
		ICPPTemplateInstance inst = bh.assertNonProblem("f() {", 1);
		assertSame(template, inst.getTemplateDefinition());
	}

	//	template<typename T1, typename T2> class A {};
	//	template<typename T1> class A<T1, int> {};
	//	template<typename T2> class A<int, T2> {};
	//	template<> class A<int, int>;
	//  A<int, int> fooA();
	//
	//	template<typename T1, typename T2> class B {};
	//	template<typename T1> class B<T1, int> {};
	//	template<typename T2> class B<int, T2> {};
	//	template<> class B<int, int> {};
	//  B<int, int> fooB();
	public void testExplicitSpecializationOfForbiddenAsImplicit_356818() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A {
	//		void f() { }
	//	};
	//	template <typename T> struct B : A {
	//		using A::f;
	//		void f(int) { }
	//	};
	//	template <typename T> struct C : B<T> {
	//		using B<T>::f;
	//		void f(int, int);
	//	};
	//
	//	void test() {
	//		B<float> b;
	//		C<float> c;
	//		b.f();
	//		b.f(1);
	//		c.f();
	//		c.f(1);
	//		c.f(1,1);
	//	}
	public void testSpecializationOfUsingDeclaration_357293() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T> struct SS {};
	//	template<template<typename T, typename S = SS<T> > class Cont>
	//   	   Cont<int> f() {}
	public void testReferenceToParameterOfTemplateTemplateParameter_357308() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename...> void f() {}
	//	void test() {
	//	     f();
	//	     f<>();
	//	}
	public void testTemplateArgumentDeductionWithoutParameters_358654() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	T d();
	//
	//	template <typename U>
	//	static decltype(&U::operator()) c(U* p);
	//
	//	template <typename F>
	//	decltype(c<F>(d<F*>()))* waldo(F f);
	//
	//	template <typename T, typename U = decltype(&T::m)>
	//	struct B {};
	//
	//	template <typename T, typename R, typename P>
	//	struct B<T, R (*)(P)> {
	//	  R operator()(P p);
	//	};
	//
	//	struct A {
	//	  static void m(int p);
	//	};
	//
	//	void test() {
	//	  waldo([]() { return B<A>(); }());
	//	}
	public void testTemplateArgumentDeductionWithFunctionSet_501549() throws Exception {
		parseAndCheckBindings();
	}

	//	template<bool V, typename T>
	//	struct C {
	//	  typedef int s;
	//	};
	//
	//	template<typename T>
	//	struct C<false, T> {
	//	  typedef T s;
	//	};
	//
	//	struct B {
	//	  typedef B u;
	//	};
	//
	//  struct C8 { char c[8]; };
	//
	//	typedef C<sizeof(char) == sizeof(C8), B> r;
	//	typedef r::s t;
	//	t::u x;
	public void testBoolExpressionAsTemplateArgument_361604() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T> struct B {
	//		void m();
	//	};
	//	template<typename T> struct C : B<T> {
	//		using B<T*>::m;
	//		void m();
	//	};
	//	template<typename T> void C<T>::m() {}
	public void testDependentUsingDeclaration() throws Exception {
		parseAndCheckBindings();
	}

	//	class A;
	//	class B;
	//
	//	template <bool bool_value>
	//	struct bool_constant {
	//	  static const bool value = bool_value;
	//	};
	//
	//	template <typename From, typename To>
	//	struct ImplicitlyConvertible {
	//	  static From MakeFrom();
	//
	//	  static char Helper(To);
	//	  static char (&Helper(...))[2];
	//
	//	  static const bool value = sizeof(Helper(ImplicitlyConvertible::MakeFrom())) == 1;
	//	};
	//
	//	template <typename T>
	//	struct IsAorB
	//	    : public bool_constant<
	//	  ImplicitlyConvertible<const T*, const A*>::value ||
	//	  ImplicitlyConvertible<const T*, const B*>::value> {
	//	};
	//
	//	namespace ns {
	//
	//	template <bool U>
	//	class C {
	//	};
	//
	//	template <typename V>
	//	void f(V a);
	//
	//	} // namespace ns
	//
	//	void test() {
	//	  ns::C<IsAorB<int>::value> a;
	//	  f(a);
	//	};
	public void testDependentExpressions_a() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	struct A {
	//	  typedef T type;
	//	};
	//
	//	template <typename T>
	//	struct B {
	//	  struct C {
	//	    template<typename V>
	//	    static typename V::pointer test(typename V::pointer*);
	//	    template<typename V>
	//	    static T* test(...);
	//
	//	    typedef typename A<T>::type D;
	//	    typedef decltype(test<D>(0)) type;
	//	  };
	//
	//	  typedef typename C::type pointer;
	//	};
	//
	//	B<int>::pointer a;
	public void testDependentExpressions_b() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPVariable var = bh.assertNonProblem("a;", 1, ICPPVariable.class);
		IType type = var.getType();
		type = SemanticUtil.getNestedType(type, TDEF);
		assertEquals("int *", type.toString());
	}

	//	template <int> void* foo(int);
	//	template <typename T> void f(T t) {
	//	    if (T* i = foo<0>(0))
	//	        return;
	//	}
	public void testDirectlyNestedAmbiguity_362976() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T, T p1, T p2, T p3=T(), T p4=T(), T p5=T(),
	//			T p6=T(),  T p7=T(),  T p8=T(),  T p9=T(),  T p10=T(),
	//			T p11=T(), T p12=T(), T p13=T(), T p14=T(), T p15=T(),
	//			T p16=T(), T p17=T(), T p18=T(), T p19=T(), T p20=T()
	//			>
	//	struct MaxOfN {
	//		template<typename X, X x1, X x2> struct Max2 {
	//			static const X result = (x1>x2)?x1:x2;
	//		};
	//		static const T result = Max2<T,(Max2<T,(Max2<T,(Max2<T,(Max2<T,(Max2<T,(Max2<T,(Max2<T,(Max2<T,
	//				(Max2<T,(Max2<T,(Max2<T,(Max2<T,(Max2<T,(Max2<T,(Max2<T,(Max2<T,(Max2<T,(Max2<T,p1,p2>::result),
	//						p3>::result),p4>::result),p5>::result),p6>::result),p7>::result),p8>::result),
	//						p9>::result),p10>::result),p11>::result),p12>::result),p13>::result),p14>::result),
	//						p15>::result),p16>::result),p17>::result),p18>::result),p19>::result),p20>::result;
	//	};
	//	int main(){
	//		return MaxOfN<int,1,2>::result;
	//	}
	public void testNestedTemplateAmbiguity_363609() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A {
	//	    void m() {}
	//	};
	//	template <class T, void (T::*m)() = &T::m> struct B {};
	//	void test() {
	//		B<A> b1;
	//	}
	public void testDefaultArgForNonTypeTemplateParameter_363743() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class T> struct A {
	//		bool b;
	//	};
	//	class B {
	//	};
	//	template<class T> T * func();
	//	void test1() {
	//		delete func<A<B>>(); // This line causes the NPE
	//	}
	//
	//	template<bool> struct C {
	//		int* ptr;
	//	};
	//	void test2() {
	//		int a = 0, b = 1;
	//		delete C< a<b >::ptr;
	//		delete C< A<B>::b >::ptr;
	//	}
	public void testTemplateAmbiguityInDeleteExpression_364225() throws Exception {
		parseAndCheckBindings();
	}

	//	template<int, int> struct a {};
	//	const int b = 0, c = 1;
	//	int a<b<c,b<c>::*mp6;  // syntax error here
	public void testTemplateIDAmbiguity_445177() throws Exception {
		parseAndCheckBindings();
	}

	//	template <bool, typename>
	//	struct EnableIf;
	//
	//	template <typename T>
	//	struct EnableIf<true, T> {
	//	    typedef T type;
	//	};
	//
	//	template <typename...> struct Tuple;
	//
	//	template <typename> struct TupleSize;
	//
	//	template<typename... E>
	//	struct TupleSize<Tuple<E...>> {
	//	  static constexpr int value = sizeof...(E);
	//	};
	//
	//	template<int I>
	//	using W = typename EnableIf<(I < TupleSize<Tuple<int>>::value), int>::type;
	public void testTemplateIDAmbiguity_497668() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T> void foo(T);
	//	template <typename T> void foo(T, typename T::type* = 0);
	//	int main() {
	//		foo(0);
	//	}
	public void testSyntaxFailureInstantiatingFunctionTemplate_365981a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T> bool bar(T);
	//	template <typename T> bool bar(T, void(T::*)() = 0);
	//	void test() {
	//	    bar(0);
	//	}
	public void testSyntaxFailureInstantiatingFunctionTemplate_365981b() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename _Tp> class vector {};
	//	template<typename T> struct bar {
	//	    void foo() {
	//	        vector<T> index;
	//	        for (const auto& entry : index) {
	//	        }
	//	    }
	//	};
	public void testResolvingAutoTypeWithDependentExpression_367472() throws Exception {
		parseAndCheckBindings();
	}

	//	struct vector {
	//	    int* begin();
	//	};
	//
	//	template <class Container>
	//	auto begin1(Container cont) -> decltype(cont.begin());
	//
	//	template <class Container>
	//	auto begin2(Container& cont) -> decltype(cont.begin());
	//
	//	vector v;
	//	auto x1 = begin1(v);
	//	auto x2 = begin2(v);
	public void testResolvingAutoTypeWithDependentExpression_402409a() throws Exception {
		BindingAssertionHelper helper = new AST2AssertionHelper(getAboveComment(), true);
		helper.assertVariableType("x1", CommonCPPTypes.pointerToInt);
		helper.assertVariableType("x2", CommonCPPTypes.pointerToInt);
	}

	//	struct vector {
	//	    int* begin();
	//	    const int* begin() const;
	//	};
	//
	//	template<class Container>
	//	auto begin1(Container cont) -> decltype(cont.begin());
	//
	//	template<class Container>
	//	auto begin2(Container& cont) -> decltype(cont.begin());
	//
	//	int main() {
	//	    vector v;
	//	    begin1(v);
	//	    begin2(v);
	//	}
	public void testResolvingAutoTypeWithDependentExpression_402409b() throws Exception {
		parseAndCheckBindings();
	}

	//	void foo(int, int);
	//	template <typename... Args> void bar(Args... args) {
	//	    foo(1,2,args...);
	//	    foo(args...);
	//	}
	public void testPackExpansionsAsArguments_367560() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename> class A;
	//	template <typename T> class A<void (T::*)()> {};
	//	template <typename T> class A<void (T::*)() const> {};
	//
	//	struct S {};
	//	int main()  {
	//	    A<void (S::*)()> m;
	//	}
	public void testDeductionForConstFunctionType_367562() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename> struct base {
	//	    typedef int type;
	//	};
	//	template <typename A, typename B> struct derived;
	//	template <typename B> struct derived<int, B> : public base<B> {
	//	    typedef typename derived::type type;  // ERROR HERE
	//	};
	public void testTemplateShortNameInQualifiedName_367607() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPDeferredClassInstance shortHand = bh.assertNonProblem("derived:", -1);
		assertTrue(shortHand.getClassTemplate() instanceof ICPPClassTemplatePartialSpecialization);
	}

	//	template <typename> class A {};
	//	template <typename T, typename=void> struct B {};
	//	template <typename T> struct B<A<T> > {
	//	    typedef int type;
	//	};
	//	typedef B<A<int> >::type type;  // ERROR HERE
	public void testPartialClassTemplateSpecUsingDefaultArgument_367997() throws Exception {
		parseAndCheckBindings();
	}

	//	struct two { char x[2]; };
	//	two check(...);
	//	char check(int);
	//	template <int> struct foo {};
	//	template <> struct foo<1> { typedef int type; };
	//	typedef foo<sizeof(check(0))>::type t;  // ERROR HERE
	public void testValueForSizeofExpression_368309() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class Value> struct iterator {
	//	    Value operator*();
	//	};
	//	template <typename Iterator> struct range {
	//	    Iterator begin();
	//	};
	//	template <typename T> struct A {
	//	    struct iterator_t : public iterator<T> {};
	//	    typedef range<iterator_t> range_t;
	//	};
	//	struct S {
	//	    int x;
	//	};
	//
	//	void test() {
	//	    A<S>::range_t r;
	//	    auto cur = r.begin(); // A<S>::iterator_t
	//	    A<S>::iterator_t cur;
	//	    auto e = *cur;
	//	    e.x;            // ERROR HERE: "Field 'x' could not be resolved"
	//	}
	public void testAutoTypeWithTypedef_368311() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		IVariable v = bh.assertNonProblem("cur = r.begin()", 3);
		assertEquals("A<S>::iterator_t", ASTTypeUtil.getType(v.getType(), true));
		parseAndCheckBindings();
	}

	//	struct S {
	//	    int x;
	//	};
	//	template <typename> struct iterator_base {
	//	    S operator*();
	//	};
	//	template <typename> struct A {
	//	    struct iterator : public iterator_base<iterator> {};
	//	};
	//	void test() {
	//	    A<int>::iterator it;
	//	    auto s = *it;
	//	    s.x;  // ERROR HERE: "Field 'x' could not be resolved"
	//	}
	public void testSpecializationOfClassType_368610a() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//	    int x;
	//	};
	//	template <typename> struct iterator_base {
	//	    S operator*();
	//	};
	//	template <typename> struct A {
	//	    template<typename T> struct iterator : public iterator_base<iterator> {};
	//	};
	//	void test() {
	//	    A<int>::iterator<int> it;
	//	    auto s = *it;
	//	    s.x;  // ERROR HERE: "Field 'x' could not be resolved"
	//	}
	public void testSpecializationOfClassType_368610b() throws Exception {
		parseAndCheckBindings();
	}

	//	template <template<typename T> class TT> struct CTT {
	//		int y;
	//	};
	//	template <typename T> struct CT {
	//		CTT<CT> someFunc();
	//	};
	//	void test2() {
	//		CT<int> x;
	//		x.someFunc().y;
	//	}
	public void testSpecializationOfClassType_368610c() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct id {
	//	    typedef T type;
	//	};
	//
	//	template <typename T>
	//	struct B {
	//	    struct base : id<id<T>> {};
	//
	//	    typedef typename base::type base2;
	//
	//	    struct result : base2 {};
	//	};
	//
	//	typedef B<int>::result::type waldo;
	public void testSpecializationOfBaseClass_409078() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ITypedef waldo = bh.assertNonProblem("waldo");
		assertSameType(waldo.getType(), CommonCPPTypes.int_);
	}

	//	struct A {
	//	  int m(int i) const;
	//	  void m() const;
	//	};
	//
	//	template<typename T> struct B {
	//	  typedef int (T::*Method)(int) const;
	//	  B(Method p) {}
	//	};
	//
	//	B<const A> a(&A::m);
	public void testConstInTypeParameter_377223() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T, T v>
	//	struct integral_constant {
	//	  static constexpr T value = v;
	//	  typedef T value_type;
	//	  typedef integral_constant<T, v> type;
	//	};
	//
	//	typedef integral_constant<bool, true> true_type;
	//
	//	typedef integral_constant<bool, false> false_type;
	//
	//	template<typename T>
	//	class helper {
	//	  typedef char one;
	//	  typedef struct { char arr[2]; } two;
	//	  template<typename U> struct Wrap_type {};
	//	  template<typename U> static one test(Wrap_type<typename U::category>*);
	//	  template<typename U> static two test(...);
	//	  public: static const bool value = sizeof(test<T>(0)) == 1;
	//	};
	//
	//	template<typename T>
	//	struct has_category : integral_constant<bool, helper<T>::value> {};
	//
	//	template<typename Iterator, bool = has_category<Iterator>::value>
	//	struct traits {};
	//
	//	template<typename Iterator>
	//	struct traits<Iterator, true> {
	//	  typedef typename Iterator::value_type value_type;
	//	};
	//
	//	struct tag {};
	//
	//	struct C {
	//	  typedef int value_type;
	//	  typedef tag category;
	//	};
	//
	//	template<typename It, typename Val = typename traits<It>::value_type>
	//	class A {
	//	};
	//
	//	typedef A<C> type;
	public void testSfinae_a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <bool B, typename T = void> struct enable_if { typedef T type; };
	//	template <typename T> struct enable_if<false, T> {};
	//
	//	template <typename T> struct is_int { static const bool value = false; };
	//	template <> struct is_int<int> { static const bool value = true; };
	//
	//	template <typename T> struct is_double { static const bool value = false; };
	//	template <> struct is_double<double> { static const bool value = true; };
	//
	//	template <typename T, typename Enabled = void>
	//	struct A {
	//	  static int get() { return 0; }
	//	};
	//
	//	template<typename T>
	//	struct A<T, typename enable_if<is_double<T>::value>::type> {
	//	  static int get() { return 1; }
	//	};
	//
	//	template <typename T>
	//	struct A<T, typename enable_if<is_int<T>::value>::type> {
	//	  static int get() { return 2; }
	//	};
	//
	//	void test() {
	//	  A<double>::get();
	//	  A<int>::get();
	//	}
	public void testSfinae_b() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	struct is_pod {
	//	  static const bool value = __is_pod(T);
	//	};
	//
	//	template <bool, typename = void>
	//	struct enable_if {};
	//
	//	template <typename T>
	//	struct enable_if<true, T> {
	//	  typedef T type;
	//	};
	//
	//	template <typename T>
	//	void f(typename enable_if<is_pod<T>::value>::type* = 0);
	//
	//	void test() {
	//	  f<int>();
	//	}
	public void testIsPOD_367993() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	template<typename T, void (T::*M)()> class A {
	//	public:
	//		static void Delegate(void* thiz) { ((T*)thiz->*M)(); }
	//	};
	//	class B {
	//	public:
	//		void Method() {}
	//	};
	//	class C {
	//	public:
	//		template<typename T, void (T::*M)()>
	//		void callDelegate(A<T, M>& thiz) { A<T, M>::Delegate(&thiz); }
	//	};
	//	void Run() {
	//		C c;
	//		B b;
	//		A<B, &B::Method> a; /* no error this line */
	//		c.callDelegate(a); /* Invalid arguments 'Candidates are: void callDelegate(A<#0,#1> &)' */
	//	}
	public void testDeductionOfNonTypeTemplateArg_372587() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	template<typename _Functor> void b(_Functor __f) {}
	//	template<typename T, typename V> void f(T __first, T __last, const V& __val) {}
	//	template<typename T> void f(T __first, T __last, const T& __val) {}
	//	void test() {
	//		b(f<int*, int>);
	//	}
	public void testFunctionSetWithNonMatchingTemplateArgs_379604() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	template <typename T> struct C {
	//		typedef decltype(&T::m) dtm;
	//	};
	//	struct X {
	//		int m() {return 0;}
	//	};
	//	void f(int (X::*)()) {}
	//	void test() {
	//		f(&X::m);
	//		C<X>::dtm v;
	//		f(v);
	//	}
	public void testPointerToMemberAsDependentExpression_391001() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	template<typename>
	//	struct A {
	//	    char x;
	//	};
	//
	//	typedef A<int> B;
	//
	//	template <char B::*PtrToMember>
	//	struct C {};
	//
	//	typedef C<&B::x> T;
	public void testPointerToMemberOfTemplateClass_402861() throws Exception {
		parseAndCheckBindings();
	}

	//	struct N {
	//	    int node;
	//	};
	//
	//	template <typename T>
	//	struct List {
	//	    template <int T::*>
	//	    struct Base {};
	//	};
	//
	//	List<N>::Base<&N::node> base;
	public void testDependentTemplateParameterInNestedTemplate_407497() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct enclosing {
	//	    template <typename U = T>
	//	    struct nested {
	//	        typedef U type;
	//	    };
	//	};
	//
	//	typedef enclosing<int>::nested<>::type waldo;
	public void testDependentTemplateParameterInNestedTemplate_399454() throws Exception {
		parseAndCheckBindings();
	}

	//	class Memory { };
	//	Memory memory;
	//	template<Memory* m> struct Container {
	//	    struct iterator {
	//	        int test;
	//	    };
	//	};
	//	int main() {
	//	    Container<&memory>::iterator it;
	//	    it.test;  // Field 'test' could not be resolved
	//	}
	public void testAddressAsTemplateArgument_391190() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	template <typename T> struct CT {
	//		const static int const_min= 1;
	//	};
	//	void test(int off) {
	//	    off < CT<int>::const_min || off > CT<int>::const_min;
	//	}
	public void testTemplateIDAmbiguity_393959() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	// template<typename T> class CT {
	//     void m() {
	//         template<typename T> using Alias= T;   // nesting level 1
	//         Alias<int> x;
	//     }
	// };
	public void testNestedAliasDeclarationNestingLevel() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		ICPPAliasTemplate templateParameterAlias = bh.assertNonProblem("Alias=", "Alias", ICPPAliasTemplate.class);
		ICPPTemplateParameter aliasParameterT = templateParameterAlias.getTemplateParameters()[0];
		assertEquals(1, aliasParameterT.getTemplateNestingLevel());

		ICPPAliasTemplateInstance aliasIntInstance = bh.assertNonProblem("Alias<int>");
		IType typeOfAliasIntInstance = aliasIntInstance.getType();
		assertTrue(typeOfAliasIntInstance instanceof ICPPBasicType);
		assertEquals(((ICPPBasicType) typeOfAliasIntInstance).getKind(), IBasicType.Kind.eInt);

		parseAndCheckBindings(code);
	}

	// template<typename T> class CT;
	// template<typename T> using Alias= CT<T>;  // nesting level 0
	// template<typename T> class CT {           // nesting level 0
	//     typedef Alias<T> TYPE;
	// };
	public void testAliasDeclarationNestingLevel() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		ICPPAliasTemplate templateParameterAlias = bh.assertNonProblem("Alias=", "Alias", ICPPAliasTemplate.class);
		ICPPTemplateParameter aliasParameterT = templateParameterAlias.getTemplateParameters()[0];
		assertEquals(0, aliasParameterT.getTemplateNestingLevel());

		ICPPTemplateDefinition templateCT = bh.assertNonProblem("CT {", "CT", ICPPTemplateDefinition.class);
		ICPPTemplateParameter templateParameterT = templateCT.getTemplateParameters()[0];
		assertEquals(0, templateParameterT.getTemplateNestingLevel());

		parseAndCheckBindings(code);
	}

	// struct S {
	//     int x;
	// };
	// using Alias = S;
	// void foo() {
	//     Alias myA;
	//     myA.x = 42;
	// }
	public void testSimpleAliasDeclaration() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();
		NameCollector collector = getNameCollector(assertionHelper.getTranslationUnit());

		ICPPClassType S = assertionHelper.assertNonProblem("struct S {", "S", ICPPClassType.class);
		ICPPField x = assertionHelper.assertNonProblem("int x", "x", ICPPField.class);
		ITypedef Alias = assertionHelper.assertNonProblem("using Alias = S", "Alias", ITypedef.class);
		IFunction foo = assertionHelper.assertNonProblem("void foo() {", "foo", IFunction.class);
		IVariable myA = assertionHelper.assertNonProblem("Alias myA", "myA", IVariable.class);

		assertInstances(collector, S, 2);
		assertInstances(collector, x, 2);
		assertInstances(collector, Alias, 2);
		assertInstances(collector, foo, 1);
		assertInstances(collector, myA, 2);
	}

	// template<typename T>
	// struct S {
	//     T x;
	// };
	// using Alias = S<int>;
	// void foo() {
	//     Alias myA;
	//     myA.x = 42;
	// }
	public void testSpecifiedTemplateAliasDeclaration() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();
		NameCollector collector = getNameCollector(assertionHelper.getTranslationUnit());

		ICPPClassType S = assertionHelper.assertNonProblem("struct S", "S", ICPPClassType.class);
		ICPPField x = assertionHelper.assertNonProblem("T x;", "x", ICPPField.class);
		ITypedef Alias = assertionHelper.assertNonProblem("using Alias = S<int>;", "Alias", ITypedef.class);
		IVariable myA = assertionHelper.assertNonProblem("Alias myA;", "myA", IVariable.class);
		ICPPSpecialization xRef = assertionHelper.assertNonProblem("myA.x = 42;", "x", ICPPSpecialization.class);

		assertInstances(collector, S, 2);
		assertInstances(collector, Alias, 2);
		assertInstances(collector, myA, 2);
		assertEquals(x, xRef.getSpecializedBinding());
	}

	// template<typename T>
	// using Alias = int;
	// void foo() {
	//     Alias<float> myA;
	//     myA = 42;
	// }
	public void testTemplatedAliasBasicType() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();
		NameCollector collector = getNameCollector(assertionHelper.getTranslationUnit());

		ICPPAliasTemplate Alias = assertionHelper.assertNonProblem("using Alias = int;", "Alias",
				ICPPAliasTemplate.class);
		ICPPAliasTemplateInstance aliasFloatInstance = assertionHelper.assertNonProblem("Alias<float> myA;",
				"Alias<float>", ICPPAliasTemplateInstance.class);

		assertInstances(collector, Alias, 2);
		assertSameType(aliasFloatInstance, new CPPBasicType(IBasicType.Kind.eInt, 0));
	}

	// template<typename T>
	// struct S {
	//     T t;
	// };
	// template<typename _T>
	// using TAlias = S<_T>;
	// void foo() {
	//     TAlias<int> myA;
	//     myA.t = 42;
	// }
	public void testTemplatedAliasDeclaration() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();
		NameCollector collector = getNameCollector(assertionHelper.getTranslationUnit());

		ICPPClassType S = assertionHelper.assertNonProblem("struct S {", "S", ICPPClassType.class);
		ICPPField t = assertionHelper.assertNonProblem("T t;", "t", ICPPField.class);
		ICPPTemplateParameter T = assertionHelper.assertNonProblem("template<typename _T>", "_T",
				ICPPTemplateParameter.class);
		ICPPTemplateParameter TRef = assertionHelper.assertNonProblem("using TAlias = S<_T>;", "_T",
				ICPPTemplateParameter.class);
		ICPPAliasTemplate TAlias = assertionHelper.assertNonProblem("using TAlias = S<_T>;", "TAlias",
				ICPPAliasTemplate.class);
		ICPPVariable myA = assertionHelper.assertNonProblem("TAlias<int> myA;", "myA", ICPPVariable.class);
		ICPPSpecialization tRef = assertionHelper.assertNonProblem("myA.t = 42;", "t", ICPPSpecialization.class);

		assertInstances(collector, S, 2);
		assertInstances(collector, T, 2);
		assertEquals(T, TRef);
		assertInstances(collector, TAlias, 2);
		assertInstances(collector, myA, 2);
		assertEquals(t, tRef.getSpecializedBinding());
		ICPPDeferredClassInstance aliasedType = (ICPPDeferredClassInstance) TAlias.getType();
		assertEquals(S, aliasedType.getClassTemplate());
	}

	// template<typename T1, typename T2, typename T3>
	// struct S {
	//     T1 t1;
	//     T2 t2;
	//     T3 t3;
	// };
	// template<typename P1, typename P2>
	// using TAlias = S<int, P2, P1>;
	// void foo() {
	//     TAlias<bool, float> myA;
	//     myA.t1 = 42;
	//     myA.t2 = 42.0f;
	//     myA.t3 = true;
	// }
	public void testTemplatedAliasDeclarationMultipleParameters() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();

		ICPPField t1 = assertionHelper.assertNonProblem("T1 t1;", "t1", ICPPField.class);
		ICPPField t2 = assertionHelper.assertNonProblem("T2 t2;", "t2", ICPPField.class);
		ICPPField t3 = assertionHelper.assertNonProblem("T3 t3;", "t3", ICPPField.class);

		ICPPTemplateParameter P1 = assertionHelper.assertNonProblem("template<typename P1, typename P2>", "P1",
				ICPPTemplateParameter.class);
		ICPPTemplateParameter P2 = assertionHelper.assertNonProblem("template<typename P1, typename P2>", "P2",
				ICPPTemplateParameter.class);

		ICPPTemplateParameter P1Ref = assertionHelper.assertNonProblem("using TAlias = S<int, P2, P1>;", "P1",
				ICPPTemplateParameter.class);
		ICPPTemplateParameter P2Ref = assertionHelper.assertNonProblem("using TAlias = S<int, P2, P1>;", "P2",
				ICPPTemplateParameter.class);

		ICPPAliasTemplateInstance TAliasInstance = assertionHelper.assertNonProblem("TAlias<bool, float> myA;",
				"TAlias<bool, float>", ICPPAliasTemplateInstance.class);
		ICPPTemplateInstance aliasedTypeInstance = (ICPPTemplateInstance) TAliasInstance.getType();

		ICPPSpecialization t1Ref = assertionHelper.assertNonProblem("myA.t1 = 42;", "t1", ICPPSpecialization.class);
		ICPPSpecialization t2Ref = assertionHelper.assertNonProblem("myA.t2 = 42.0f;", "t2", ICPPSpecialization.class);
		ICPPSpecialization t3Ref = assertionHelper.assertNonProblem("myA.t3 = true;", "t3", ICPPSpecialization.class);

		assertEquals(P1, P1Ref);
		assertEquals(P2, P2Ref);

		assertEquals(t1, t1Ref.getSpecializedBinding());
		assertEquals(t2, t2Ref.getSpecializedBinding());
		assertEquals(t3, t3Ref.getSpecializedBinding());
		assertSameType(new CPPBasicType(IBasicType.Kind.eInt, 0),
				aliasedTypeInstance.getTemplateArguments()[0].getTypeValue());
		assertSameType(new CPPBasicType(IBasicType.Kind.eFloat, 0),
				aliasedTypeInstance.getTemplateArguments()[1].getTypeValue());
		assertSameType(new CPPBasicType(IBasicType.Kind.eBoolean, 0),
				aliasedTypeInstance.getTemplateArguments()[2].getTypeValue());
	}

	// template<typename T>
	// struct S {
	//     T t;
	// };
	// template<typename P>
	// using TAlias = S<P>;
	// void foo() {
	//     TAlias<S<int>> myA;
	//     myA.t = S<int>();
	// }
	public void testTemplatedAliasDeclarationTemplateArgument() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();

		ICPPField t = assertionHelper.assertNonProblem("T t;", "t", ICPPField.class);
		ICPPAliasTemplateInstance TAliasSInt = assertionHelper.assertNonProblem("TAlias<S<int>> myA;", "TAlias<S<int>>",
				ICPPAliasTemplateInstance.class);
		ICPPSpecialization tRef = assertionHelper.assertNonProblem("myA.t = S<int>()", "t", ICPPSpecialization.class);

		assertEquals(t, tRef.getSpecializedBinding());
		assertSameType(TAliasSInt, (IType) tRef.getOwner());
	}

	// template<typename T>
	// struct S {
	//     T t;
	// };
	// template<typename P>
	// using TAlias = S<P>;
	// void foo() {
	//     S<TAlias<int>> myA;
	//     myA.t = S<int>();
	// }
	public void testTemplatedAliasAsTemplateArgument() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();

		ICPPField t = assertionHelper.assertNonProblem("T t;", "t", ICPPField.class);
		ICPPTemplateInstance STAliasInt = assertionHelper.assertNonProblem("S<TAlias<int>> myA;", "S<TAlias<int>>",
				ICPPTemplateInstance.class);
		ICPPSpecialization tRef = assertionHelper.assertNonProblem("myA.t = S<int>();", "t", ICPPSpecialization.class);

		assertEquals(t, tRef.getSpecializedBinding());
		assertEquals(STAliasInt, tRef.getOwner());
	}

	// template<int Size, int Size2>
	// struct S {
	//     int buff [Size];
	// };
	// template<int SizeArg, int SizeArg2>
	// using TAlias = S<SizeArg2, SizeArg>;
	// void foo() {
	//     TAlias<5, 4> myA;
	//     myA.buff[0] = 1;
	// }
	public void testTemplatedAliasDeclarationValueArgument() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();

		ICPPField buff = assertionHelper.assertNonProblem("int buff [Size];", "buff", ICPPField.class);
		ICPPSpecialization buffRef = assertionHelper.assertNonProblem("myA.buff[0] = 1;", "buff",
				ICPPSpecialization.class);

		assertEquals(buff, buffRef.getSpecializedBinding());
		assertEquals(Long.valueOf(4), buffRef.getTemplateParameterMap().getArgument(0).getNonTypeValue().numberValue());
		assertEquals(Long.valueOf(5), buffRef.getTemplateParameterMap().getArgument(1).getNonTypeValue().numberValue());
	}

	// template<typename T, int Size>
	// struct S {
	//     T buff [Size];
	// };
	// template<typename Type = int, int Items = 5>
	// using TAlias = S<Type, Items>;
	// void foo() {
	//     TAlias<> myA;
	//     myA.buff[0] = 1;
	// }
	public void testTemplatedAliasDefaultArguments() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();

		ICPPField buff = assertionHelper.assertNonProblem("T buff [Size];", "buff", ICPPField.class);
		ICPPAliasTemplateInstance myA = assertionHelper.assertNonProblem("TAlias<> myA;", "TAlias<>",
				ICPPAliasTemplateInstance.class);
		ICPPSpecialization buffRef = assertionHelper.assertNonProblem("myA.buff[0] = 1;", "buff",
				ICPPSpecialization.class);

		assertEquals(buff, buffRef.getSpecializedBinding());
		assertSameType(buffRef.getTemplateParameterMap().getArgument(0).getTypeValue(),
				new CPPBasicType(IBasicType.Kind.eInt, 0));
		assertEquals(Long.valueOf(5), buffRef.getTemplateParameterMap().getArgument(1).getNonTypeValue().numberValue());
	}

	// template<typename T>
	// struct S {
	//     T t;
	// };
	// template<typename A, typename A2>
	// using TAlias = S<S<A2> >;
	// void foo() {
	//     TAlias<float, int> myA;
	//     myA.t = S<int>();
	// }
	public void testTemplatedAliasTemplateArgument() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();

		ICPPField t = assertionHelper.assertNonProblem("T t;", "t", ICPPField.class);
		ICPPSpecialization tRef = assertionHelper.assertNonProblem(" myA.t = S<int>();", "t", ICPPSpecialization.class);
		ICPPClassSpecialization Sint = assertionHelper.assertNonProblem("myA.t = S<int>();", "S<int>",
				ICPPClassSpecialization.class);

		assertEquals(t, tRef.getSpecializedBinding());
		assertSameType(tRef.getTemplateParameterMap().getArgument(0).getTypeValue(), Sint);
	}

	// template<typename T>
	// struct S {
	//     T t;
	// };
	// template<typename A>
	// using TAlias = S<A>;
	// void bar(TAlias<int> arg){
	// }
	// void foo() {
	//     TAlias<int> myA;
	//     bar(myA);
	//     S<int> myS;
	//     bar(myS);
	// }
	public void testTemplatedAliasAsFunctionParameter() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();

		ICPPFunction bar = assertionHelper.assertNonProblem("void bar(TAlias<int> arg){", "bar", ICPPFunction.class);
		ICPPFunction barRefAlias = assertionHelper.assertNonProblem("bar(myA);", "bar", ICPPFunction.class);
		ICPPFunction barRefSInt = assertionHelper.assertNonProblem("bar(myS);", "bar", ICPPFunction.class);

		assertEquals(bar, barRefAlias);
		assertEquals(bar, barRefSInt);
	}

	// template<typename T>
	// struct S {
	//     T t;
	// };
	// template<typename A>
	// using TAlias = S<A>;
	// void bar(S<int> arg){
	// }
	// void foo() {
	//     TAlias<int> myA;
	//     bar(myA);
	// }
	public void testTemplatedAliasAsFunctionArgument() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();

		ICPPFunction bar = assertionHelper.assertNonProblem("void bar(S<int> arg){", "bar", ICPPFunction.class);
		ICPPFunction barRefAlias = assertionHelper.assertNonProblem("bar(myA);", "bar", ICPPFunction.class);

		assertEquals(bar, barRefAlias);
	}

	// template<typename T>
	// struct S {
	//     T t;
	// };
	// template<typename A>
	// using TAlias = S<A>;
	// void bar(S<int> arg){
	// }
	// void bar(TAlias<int> arg){
	// }
	public void testTemplatedAliasRedefinitionOfSameFunction() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertNonProblem("bar(S", "bar", ICPPFunction.class);
		bh.assertProblem("bar(TAlias", "bar", ISemanticProblem.BINDING_INVALID_REDEFINITION);
	}

	// template<typename VT, typename Allocator> struct vector{};
	// template<typename AT> struct Alloc{};
	// template<typename T> using Vec = vector<T, Alloc<T> >;
	// template<template<typename> class TT>
	// void f(TT<int>);
	// template<template<typename, typename> class TT>
	// void g(TT<int, Alloc<int> >);
	// void foo(){
	//     Vec<int> v;
	//     g(v);
	//     f(v);
	// }
	public void testTemplatedAliasDeduction() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertNonProblem("g(v)", "g", ICPPFunction.class);
		bh.assertProblem("f(v)", "f", ISemanticProblem.BINDING_NOT_FOUND);
	}

	// using function = void (&)(int);
	// void foo(int) {
	//     function f = &foo;
	// }
	public void testSimpleFunctionAliasDeclaration() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();
		NameCollector collector = getNameCollector(assertionHelper.getTranslationUnit());

		ITypedef function = assertionHelper.assertNonProblem("using function = void (&)(int)", "function",
				ITypedef.class);
		ICPPFunction foo = assertionHelper.assertNonProblem("void foo(int)", "foo", ICPPFunction.class);

		assertInstances(collector, function, 2);
		assertInstances(collector, foo, 2);
		assertSameType(((ICPPReferenceType) function.getType()).getType(), foo.getType());
	}

	// template<typename T>
	// struct S {
	//     T t;
	// };
	// template<typename T>
	// using TAlias = S<T>&;
	// void foo() {
	//     S<int> myS;
	//     TAlias<int> myA = myS;
	//     myA.t = 42;
	// }
	public void testTemplatedAliasForTemplateReference() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();

		ICPPClassSpecialization SInt = assertionHelper.assertNonProblem("S<int> myS;", "S<int>",
				ICPPClassSpecialization.class);
		ICPPAliasTemplateInstance TAliasInt = assertionHelper.assertNonProblem("TAlias<int> myA = myS;", "TAlias<int>",
				ICPPAliasTemplateInstance.class);
		assertSameType(new CPPReferenceType(SInt, false), TAliasInt);

	}

	// template<typename T>
	// using function = void (int);
	// void foo(int) {
	//     function<int> f = &foo;
	// }
	public void testSimpleFunctionTemplateAliasDeclaration() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();
		NameCollector collector = getNameCollector(assertionHelper.getTranslationUnit());

		ICPPAliasTemplate function = assertionHelper.assertNonProblem("using function = void (int)", "function",
				ICPPAliasTemplate.class);
		ICPPFunction foo = assertionHelper.assertNonProblem("void foo(int) {", "foo", ICPPFunction.class);
		ICPPAliasTemplateInstance functionInt = assertionHelper.assertNonProblem("function<int> f = &foo;",
				"function<int>", ICPPAliasTemplateInstance.class);

		assertInstances(collector, function, 2);
		assertInstances(collector, foo, 2);
		assertSameType(foo.getType(), functionInt);
	}

	// template<typename T>
	// using function = void (&)(int);
	// void foo(int) {
	//     function<int> f = &foo;
	// }
	public void testSimpleFunctionReferenceTemplateAliasDeclaration() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();
		NameCollector collector = getNameCollector(assertionHelper.getTranslationUnit());

		ICPPAliasTemplate function = assertionHelper.assertNonProblem("using function = void (&)(int)", "function",
				ICPPAliasTemplate.class);
		ICPPFunction foo = assertionHelper.assertNonProblem("void foo(int) {", "foo", ICPPFunction.class);
		ICPPAliasTemplateInstance functionInt = assertionHelper.assertNonProblem("function<int> f = &foo;",
				"function<int>", ICPPAliasTemplateInstance.class);

		assertInstances(collector, function, 2);
		assertInstances(collector, foo, 2);
		assertSameType(new CPPReferenceType(foo.getType(), false), functionInt.getType());
	}

	// template<typename T>
	// struct S {
	//     T t;
	// };
	// template<template<typename> class TA>
	// using TAlias = S<TA>;
	// void foo() {
	//     TAlias<S<int> > myA;
	//     myA.t = S<int>();
	// }
	public void testTemplatedAliasTemplateParameter() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();

		ICPPField t = assertionHelper.assertNonProblem("T t;", "t", ICPPField.class);
		ICPPSpecialization tRef = assertionHelper.assertNonProblem("myA.t = S<int>();", "t", ICPPSpecialization.class);
		ICPPClassSpecialization Sint = assertionHelper.assertNonProblem("myA.t = S<int>();", "S<int>",
				ICPPClassSpecialization.class);

		assertEquals(t, tRef.getSpecializedBinding());
		assertSameType(tRef.getTemplateParameterMap().getArgument(0).getTypeValue(), Sint);
	}

	//	template <typename T>
	//	struct A {
	//	  typedef void (T::*func)();
	//	};
	//
	//	template <typename T>
	//	struct B {
	//	  template <typename A<T>::func U>
	//	  class C {};
	//
	//	  template <typename A<T>::func U>
	//	  using Waldo = C<U>;
	//	};
	//
	//	struct D {
	//	  void m();
	//	};
	//
	//	void test() {
	//	  B<D>::Waldo<&D::m>();
	//	}
	public void testTemplatedAliasWithPointerToMember_448785() throws Exception {
		parseAndCheckBindings();
	}

	// namespace NS {
	//     template<typename T>
	//     struct S {
	//         T t;
	//     };
	//     template<typename T>
	//     using Alias = S<T>;
	// }
	// using namespace NS;
	// Alias<int> intAlias;
	public void testAliasDeclarationContext() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper assertionHelper = getAssertionHelper();

		ICPPAliasTemplateInstance AliasInt = assertionHelper.assertNonProblem("Alias<int> intAlias;", "Alias<int>",
				ICPPAliasTemplateInstance.class);
		assertEquals("Alias", AliasInt.getName());
		assertEquals("NS", AliasInt.getQualifiedName()[0]);
		assertEquals("Alias", AliasInt.getQualifiedName()[1]);
		IType aliasedType = AliasInt.getType();
		assertInstance(aliasedType, ICPPTemplateInstance.class);
		ICPPTemplateArgument[] args = ((ICPPTemplateInstance) aliasedType).getTemplateArguments();
		assertEquals(1, args.length);
		assertSameType(CommonCPPTypes.int_, args[0].getTypeValue());

		ICPPNamespace namespaceNS = assertionHelper.assertNonProblem("using namespace NS;", "NS", ICPPNamespace.class);
		assertEquals(namespaceNS, AliasInt.getOwner());

		assertTrue(AliasInt.getScope() instanceof ICPPTemplateScope);
	}

	//	template<typename U>
	//	struct A {
	//	  template<typename V>
	//	  struct rebind {
	//	    typedef A<V> other;
	//	  };
	//	};
	//
	//	template<typename T, typename U>
	//	struct B {
	//	  typedef typename T::template rebind<U>::other type1;
	//	};
	//
	//	template<typename T>
	//	struct C {
	//	  template<typename U>
	//	  using rebind2 = typename B<T, U>::type1;
	//	};
	//
	//	template<typename T>
	//	struct D : C<T> {
	//	  typedef int type0;
	//
	//	  template<typename U>
	//	  struct rebind {
	//	    typedef typename C<T>::template rebind2<U> other2;
	//	  };
	//	};
	//
	//	template<typename T>
	//	struct E {
	//	  typedef typename D<T>::template rebind<int>::other2 type2;
	//	  typedef D<type2> type3;
	//	  typedef typename type3::type0 type;
	//	};
	//
	//	void f(int x);
	//
	//	void test(E<A<int>>::type v) {
	//	  f(v);
	//	}
	public void testAliasTemplate_395026a() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename U>
	//	struct A {
	//	  template<typename V>
	//	  struct rebind {
	//	    typedef A<V> other;
	//	  };
	//	};
	//
	//	template<typename T, typename U>
	//	struct B {
	//	  typedef typename T::template rebind<U>::other type1;
	//	};
	//
	//	template<typename T>
	//	struct C {
	//	  template<typename U>
	//	  using rebind2 = typename B<T, U>::type1;
	//	};
	//
	//	template<typename T>
	//	struct D : C<T> {
	//	  typedef int* type0;
	//	  template<typename U>
	//	  struct rebind {
	//	    typedef typename C<T>::template rebind2<U> other;
	//	  };
	//	};
	//
	//	template<typename U>
	//	struct E {
	//	  typedef typename D<A<U>>::template rebind<U>::other type2;
	//	  typedef typename D<type2>::type0 type;
	//	  type operator[](int n);
	//	};
	//
	//	void f(int);
	//
	//	void test() {
	//	  E<int*> v;
	//	  f(*v[0]);
	//	}
	public void testAliasTemplate_395026b() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	struct A {};
	//
	//	template<typename T>
	//	using B = A<T>;
	//
	//	template<typename T>
	//	void f(B<T>* p);
	//
	//	void test(A<int>* c) {
	//	  f(c);
	//	}
	public void testAliasTemplate_416280_1() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	struct C {};
	//
	//	template<typename U>
	//	struct A {
	//	  template<typename V>
	//	  using B = C<V>;
	//	};
	//
	//	struct D : public A<char> {
	//	  B<int> b;
	//	};
	public void testAliasTemplate_416280_2() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A {
	//	  static constexpr bool val = true;
	//	};
	//
	//	struct C {
	//	  template <typename T>
	//	  using AC = A;
	//	};
	//
	//	template <class T>
	//	struct D {
	//	  template <class U>
	//	  struct AD : T::template AC<U> {};
	//	};
	//
	//	bool b = D<C>::template AD<int>::val;
	public void testAliasTemplate_486618() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	struct B {
	//	  typedef T type;
	//	};
	//
	//	template <class... T>
	//	class C {};
	//
	//	template <class... T>
	//	using D = C<typename B<T>::type...>;
	//
	//	template <class... T>
	//	class Group {};
	//
	//	template <class... U>
	//	D<U...> waldo1(Group<U...>);
	//
	//	template <class U, class... V>
	//	D<U, V...> waldo2(Group<U>, Group<V...>);
	//
	//	template <class... U, class V>
	//	D<U..., V> waldo3(Group<U...>, Group<V>);
	//
	//	template <class... U, class... V>
	//	D<U..., V...> waldo4(Group<U...>, Group<V...>);
	//
	//	void test() {
	//		Group<int> one;
	//		Group<int, int> two;
	//	    waldo1(two);
	//		waldo2(one, two);
	//		waldo3(two, one);
	//		waldo4(two, two);
	//	}
	public void testAliasTemplate_486971() throws Exception {
		parseAndCheckBindings();
	}

	//	struct true_type {
	//	  constexpr operator bool() const { return true; }
	//	};
	//
	//	struct false_type {
	//	  constexpr operator bool() const { return false; }
	//	};
	//
	//	template<bool, typename T = void>
	//	struct enable_if {};
	//
	//	template<typename T>
	//	struct enable_if<true, T> {
	//	  typedef T type;
	//	};
	//
	//	template<typename T>
	//	struct C {
	//	  T* ptr();
	//	};
	//
	//	struct A {};
	//
	//	struct B {
	//	  B(A a);
	//	};
	//
	//	template<class T>
	//	auto ptr1(T t) -> decltype(t.ptr());
	//
	//	template <typename T>
	//	struct E {
	//	  template <typename U>
	//	  using G = decltype(ptr1(U()));
	//	  template <typename U>
	//	  using H = decltype(*U());
	//
	//	  using type1 = H<G<T>>;
	//	};
	//
	//	template<typename T>
	//	struct D {
	//	  typedef true_type type2;
	//	};
	//
	//	template <typename T>
	//	using F = typename D<typename E<T>::type1>::type2;
	//
	//	template <typename T, typename = typename enable_if<F<T>{}>::type>
	//	void waldo(T p);
	//
	//	void test() {
	//	  C<A> a;
	//	  waldo(a);
	//	}
	public void testAliasTemplate_502109() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct Struct {};
	//
	//	template <typename T> using Alias = Struct<T>;
	//
	//	void waldo(Struct<int>);
	//
	//	int main() {
	//	    waldo(Alias<int>());
	//	}
	public void testTemplateIdNamingAliasTemplateInExpression_472615() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class>
	//	struct Traits {
	//	    template <class U>
	//	    using rebind = U;
	//	};
	//	template <class T>
	//	struct Meta {
	//	    typedef typename Traits<T>::template rebind<int> type;
	//	};
	//	typedef Meta<int>::type Waldo;
	public void testNestedAliasTemplate_488456() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ITypedef waldo = helper.assertNonProblem("Waldo");
		assertSameType(waldo, CommonCPPTypes.int_);
	}

	//	template<typename U>
	//	struct A {
	//	  typedef U type1;
	//
	//	  template<typename V>
	//	  struct rebind {
	//	    typedef A<V> other;
	//	  };
	//	};
	//
	//	template<typename T, typename U>
	//	struct B {
	//	  template<typename T2, typename U2>
	//	  static constexpr bool test(typename T2::template rebind<U2>::other*) {
	//	    return true;
	//	  }
	//
	//	  template<typename, typename>
	//	  static constexpr bool test(...) {
	//	    return false;
	//	  }
	//
	//	  static const bool value = test<T, U>(nullptr);
	//	};
	//
	//	template<typename T, typename U, bool = B<T, U>::value>
	//	struct C;
	//
	//	template<typename T, typename U>
	//	struct C<T, U, true> {
	//	  typedef typename T::template rebind<U>::other type2;
	//	};
	//
	//	template<typename T>
	//	struct D {
	//	  typedef typename T::type1 type3;
	//
	//	  template<typename U>
	//	  using rebind2 = typename C<T, U>::type2;
	//	};
	//
	//	template<typename T>
	//	struct E : D<T> {
	//	  typedef D<T> Base;
	//	  typedef typename Base::type3& type4;
	//
	//	  template<typename U>
	//	  struct rebind {
	//	    typedef typename Base::template rebind2<U> other;
	//	  };
	//	};
	//
	//	template<typename U, typename T = A<U>>
	//	struct F {
	//	  typedef typename E<T>::template rebind<U>::other type5;
	//	  typedef typename E<type5>::type4 type6;
	//	  type6 operator[](int n);
	//	};
	//
	//	void f(int);
	//
	//	void test() {
	//	  F<int*> a;
	//	  f(*a[0]);
	//	}
	public void testConstexprFunction_395238a() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	struct A {
	//	  template<typename U>
	//	  static constexpr U test(U v) {
	//	    return v;
	//	  }
	//
	//	  template<typename>
	//	  static constexpr bool test(...) {
	//	    return false;
	//	  }
	//
	//	  static const bool value = test<T>(true);
	//	};
	//
	//	template<typename T, bool = A<T>::value>
	//	struct B;
	//
	//	template<typename T>
	//	struct B<T, true> {
	//	  typedef T type;
	//	};
	//
	//	B<bool>::type x;
	//	B<int*>::type y;
	public void testConstexprFunction_395238b() throws Exception {
		BindingAssertionHelper ah = getAssertionHelper();
		ITypedef td = ah.assertNonProblem("B<bool>::type", "type", ITypedef.class);
		assertEquals("bool", ASTTypeUtil.getType(td.getType()));
		ah.assertProblem("B<int*>::type", "type");
	}

	//	constexpr int f() { return 1; }
	//
	//	template <int>
	//	struct A {
	//	    static void g() {}
	//	};
	//
	//	void bar() {
	//	    A<f()>::g();
	//	}
	public void testConstexprFunctionCallInTemplateArgument_332829() throws Exception {
		parseAndCheckBindings();
	}

	//	struct IntConvertible {
	//	    constexpr operator int() const { return 42; }
	//	};
	//
	//	template <int>
	//	struct Waldo {};
	//
	//	Waldo<IntConvertible{}> w;  // Syntax error
	public void testUniformInitializationInTemplateArgument_510010() throws Exception {
		parseAndCheckBindings();
	}

	//	int f() {
	//		int i = 0;
	//		if(i < 1){
	//			++i;
	//		}
	//	}
	public void testRegression_510010() throws Exception {
		parseAndCheckBindings();
	}

	//	template<bool, typename T = void>
	//	struct C {};
	//
	//	template<typename T>
	//	struct C<true, T> {
	//	  typedef T type;
	//	};
	//
	//	template <typename T>
	//	struct B {
	//	  static constexpr bool b() {
	//	    return true;
	//	  }
	//	};
	//
	//	struct A {
	//	  template <typename T, typename = typename C<B<T>::b()>::type>
	//	  A(T v);
	//	};
	//
	//	void waldo(A p);
	//
	//	void test(int x) {
	//	  waldo(x);
	//	}
	public void testConstexprMethod_489987() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename From>
	//	struct is_convertible {
	//	    static char check(From);
	//	    static From from;
	//	    static const int value = sizeof(check(from));
	//	};
	//	template <int>
	//	struct S {
	//	    typedef int type;
	//	};
	//	struct Cat {};
	//	typedef S<is_convertible<Cat>::value>::type T;
	public void testDependentExpressionInvolvingField_388623() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//	    typedef int a_type;
	//	};
	//	template <typename T, typename = typename T::a_type> int foo(T);
	//	template <typename T, typename = typename T::b_type> void foo(T);
	//	int main() {
	//	    foo(S());
	//	}
	public void testSfinaeInDefaultArgument() throws Exception {
		parseAndCheckBindings();
	}

	//	typedef char (&no_tag)[1];
	//	typedef char (&yes_tag)[2];
	//
	//	template <typename T>
	//	struct type_wrapper {};
	//
	//	template <typename T>
	//	struct has_type {
	//	    template <typename U>
	//	    static yes_tag test(type_wrapper<U> const volatile*, type_wrapper<typename U::type>* = 0);
	//
	//	    static no_tag test(...);
	//
	//	    static const bool value = sizeof(test(static_cast<type_wrapper<T>*>(0))) == sizeof(yes_tag);
	//	};
	//
	//	const bool B = has_type<int>::value;
	public void testSfinaeInNestedTypeInTemplateArgument_402257() throws Exception {
		BindingAssertionHelper helper = new AST2AssertionHelper(getAboveComment(), true);
		ICPPVariable B = helper.assertNonProblem("B");
		assertConstantValue(0 /* false */, B);
	}

	//	struct S {
	//	    S(int);
	//	};
	//
	//	template <typename>
	//	struct meta {};
	//
	//	template <>
	//	struct meta<S> {
	//	    typedef void type;
	//	};
	//
	//	struct B {
	//	    template <typename T, typename = typename meta<T>::type>
	//	    operator T() const;
	//	};
	//
	//	struct  A {
	//	    S waldo;
	//	    A() : waldo(B{}) {}
	//	};
	public void testSfinaeInTemplatedConversionOperator_409056() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//	template<typename T>
	//	struct A {
	//	  static constexpr bool value = false;
	//	};
	//
	//	template<bool, typename T = void>
	//	struct enable_if {};
	//
	//	template<typename T>
	//	struct enable_if<true, T> {
	//	  typedef T type;
	//	};
	//
	//	template <class U>
	//	void waldo();
	//
	//	template <class U>
	//	typename enable_if<A<U>::value>::type waldo();
	//
	//	auto x = waldo<int>;
	public void testSfinaeWhenResolvingAddressOfFunction_429928() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	struct A {};
	//
	//	template<bool, typename T = void>
	//	struct enable_if {};
	//
	//	template<typename T>
	//	struct enable_if<true, T> {
	//	  typedef T type;
	//	};
	//
	//	template <class U>
	//	void waldo();
	//
	//	template <class U>
	//	typename enable_if<A<U>::value>::type waldo();
	//
	//	auto x = waldo<int>;
	public void testSfinaeInNonTypeTemplateParameter_429928() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T, typename = decltype(T(0))>
	//	static void test(int);
	//
	//	template<typename>
	//	static int test(...);
	//
	//	struct A {};
	//
	//	int waldo(int p);
	//
	//	int x = waldo(test<A>(0));
	public void testSfinaeInConstructorCall_430230() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T, typename = decltype(new T(0))>
	//	static void test(int);
	//
	//	template<typename>
	//	static int test(...);
	//
	//	struct A {};
	//
	//	int waldo(int p);
	//
	//	int x = waldo(test<A>(0));
	public void testSfinaeInNewExpression_430230a() throws Exception {
		parseAndCheckBindings();
	}

	//	template<bool __v>
	//	struct bool_constant {
	//	  static constexpr bool value = __v;
	//	};
	//
	//	typedef bool_constant<true> true_type;
	//	typedef bool_constant<false> false_type;
	//
	//	struct B {
	//	  template<typename T, typename Arg, typename = decltype(::new T(Arg()))>
	//	  static true_type test(int);
	//
	//	  template<typename, typename>
	//	  static false_type test(...);
	//	};
	//
	//	template<typename T, typename Arg>
	//	struct C : public B {
	//	  typedef decltype(test<T, Arg>(0)) type;
	//	};
	//
	//	template<typename T, typename Arg>
	//	struct D : public C<T, Arg>::type {};
	//
	//	template<typename T, typename Arg>
	//	struct E : public bool_constant<D<T, Arg>::value> {};
	//
	//	template<bool, typename T = void>
	//	struct enable_if {};
	//
	//	template<typename T>
	//	struct enable_if<true, T> {
	//	  typedef T type;
	//	};
	//
	//	struct A {};
	//
	//	template <class F>
	//	typename enable_if<true>::type
	//	waldo();
	//
	//	template <class F>
	//	typename enable_if<E<F, int>::value>::type
	//	waldo();
	//
	//	auto x = waldo<A>;
	public void testSfinaeInNewExpression_430230b() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T, typename = decltype(new T)>
	//	static void test(int);
	//
	//	template<typename>
	//	static int test(...);
	//
	//	struct A {
	//	  A() = delete;
	//	};
	//
	//	int waldo(int p);
	//
	//	int x = waldo(test<A>(0));
	public void testSfinaeInNewExpressionWithDeletedConstructor_430230() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename _From>
	//	struct is_convertible {};
	//
	//	class function {
	//	public:
	//	  template<typename _Functor, bool = is_convertible<_Functor>::type::value>
	//	  function(_Functor);
	//	};
	//
	//	class A {};
	//
	//	struct B {
	//	  B(const char* s);
	//	};
	//
	//	template <class T> void waldo(const B& b);
	//	template <class T> void waldo(function f);
	//
	//	void test() {
	//	    waldo<A>("");
	//	}
	public void testSfinaeInIdExpression_459940() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	auto waldo(T p) -> decltype(undeclared(p));
	//
	//	template <typename T>
	//	void waldo(T p);
	//
	//	void test() {
	//	  waldo(1);
	//	}
	public void testSfinaeInTrailingReturnType_495845() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	T a();
	//
	//	template <class T>
	//	struct A {};
	//
	//	template <class T>
	//	A<T> b(T t);
	//
	//	template <class T, class U>
	//	void c(U u);
	//
	//	template <class T, class U, class W>
	//	decltype(c<T>(1)) d(W w, U u);
	//
	//	template <class T, class U>
	//	auto d(U u, T t) -> decltype(d<typename A<T>::type>(u, t));
	//
	//	template <class T, class U>
	//	auto e(U u, T t) -> decltype(d(b(u), t));
	//
	//	template <typename T, typename U = decltype(e(1, a<T>()))>
	//	class B {};
	//
	//	template <typename T>
	//	typename B<T>::type waldo(T p);
	//
	//	template <typename T>
	//	int waldo(T p);
	//
	//	void test() {
	//	  waldo(1);
	//	}
	public void testSfinaeInTrailingReturnType_495952() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename>
	//	struct M {
	//	    template <typename... Args>
	//	    M(Args...);
	//	};
	//	void foo() {
	//	    new M<int>((int*)0, 0);
	//	}
	public void testVariadicConstructor_395247() throws Exception {
		parseAndCheckBindings();
	}

	//	template <int> struct Int {};
	//	template<typename T>
	//	struct identity {
	//	    typedef T type;
	//	};
	//	template <typename T>
	//	char waldo(T);
	//	template<typename F = int>
	//	struct S {
	//	    F f;
	//	    static const int value = sizeof(waldo(f));
	//	};
	//	typedef identity<Int<S<>::value>>::type reference;
	public void testDependentExpressions_395243a() throws Exception {
		parseAndCheckBindings();
	}

	//	typedef char one;
	//	typedef struct {
	//		char arr[2];
	//	} two;
	//	template <typename T>
	//	struct has_foo_type {
	//		template <typename _Up>
	//		struct wrap_type { };
	//		template <typename U>
	//		static one test(wrap_type<typename U::foo_type>*);
	//		template <typename U>
	//		static two test(...);
	//		static const bool value = sizeof(test<T>(0)) == 1;
	//	};
	//	template <bool>
	//	struct traits;
	//	template <>
	//	struct traits<true> {
	//		typedef int bar_type;
	//	};
	//	struct S {
	//		typedef int foo_type;
	//	};
	//	traits<has_foo_type<S>::value>::bar_type a;
	public void testDependentExpressions_395243b() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename U> U bar(U);
	//	template <typename T> auto waldo(T t) -> decltype(bar(t));
	//	struct S {
	//	    void foo() const;
	//	};
	//	struct V {
	//	    S arr[5];
	//	};
	//	int main() {
	//	    V e;
	//	    auto v = waldo(e);
	//	    for (auto s : v.arr)
	//	        s.foo();
	//	}
	public void testDependentExpressions_395243c() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename> class C {};
	//	template <typename T> int begin(C<T>);
	//	template <typename>
	//	struct A {
	//	    class B {
	//	        void m();
	//	    };
	//	    void test() {
	//	        B* v[5];
	//	        for (auto x : v)
	//	            x->m();
	//	    }
	//	};
	public void testDependentExpressions_395243d() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct B {
	//	  enum { value = 1 };
	//	};
	//
	//	template <typename T>
	//	struct C {
	//	  enum { id = B<T>::value };
	//	};
	//
	//	void test() {
	//	  int x = C<bool>::id;
	//	}
	public void testDependentEnumValue_389009() throws Exception {
		BindingAssertionHelper ah = getAssertionHelper();
		IEnumerator binding = ah.assertNonProblem("C<bool>::id", "id");
		IValue value = binding.getValue();
		Number num = value.numberValue();
		assertNotNull(num);
		assertEquals(1, num.longValue());
	}

	//	template <int>
	//	struct A {
	//	    void waldo();
	//	};
	//
	//	template <typename>
	//	struct traits {
	//	  enum {
	//	    E = 1,
	//	  };
	//	};
	//
	//	template <typename T>
	//	struct L {
	//	  enum {
	//	      X = traits<T>::E & 1
	//	  };
	//	};
	//
	//	template <typename T>
	//	struct B : A<L<T>::X> {
	//	    using A<L<T>::X>::waldo;
	//	};
	//
	//	class M : public B<M> {};
	//
	//	void bar(B<M>& v) {
	//	    v.waldo();
	//	}
	public void testArgumentDependentLookupForEnumeration_506170() throws Exception {
		parseAndCheckBindings();
	}

	//	template <int...> struct A {};
	//	template <int... I> void foo(A<I...>);
	//	int main() {
	//		foo(A<0>());
	//	}
	public void testVariadicNonTypeTemplateParameter_382074() throws Exception {
		parseAndCheckBindings();
	}

	//	template <bool...>
	//	struct ice_or {
	//	    static const bool value = false;
	//	};
	//	template <typename T>
	//	struct is_foo {
	//	    static const bool value = false;
	//	};
	//	template <typename... Args>
	//	struct contains_foo {
	//	    static const bool value = ice_or<is_foo<Args>::value...>::value;
	//	};
	//	template <bool>
	//	struct meta;
	//	struct S { void bar(); };
	//	template <>
	//	struct meta<false> {
	//	    typedef S type;
	//	};
	//	int main() {
	//	    meta<contains_foo<>::value>::type t;
	//	    t.bar();
	//	}
	public void testVariadicNonTypeTemplateParameter_399039() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename...>
	//	struct common_type;
	//	template <typename T>
	//	struct common_type<T> {
	//	    typedef int type;
	//	};
	//	template <typename T, typename... U>
	//	struct common_type<T, U...> {
	//	    typedef int type;
	//	};
	//	typedef common_type<int>::type type;
	public void testClassTemplateSpecializationPartialOrdering_398044a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename>
	//	class A;
	//	template <typename R, typename... Args>
	//	class A<R(*)(Args...)> {
	//	};
	//	template <typename R>
	//	class A<R*> {
	//	};
	//	int main() {
	//	    A<bool(*)()> mf;
	//	}
	public void testClassTemplateSpecializationPartialOrdering_398044b() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct waldo {
	//	    typedef int type;
	//	};
	//
	//	template <typename R>
	//	struct waldo<R (...)>;
	//
	//	typedef waldo<int ()>::type Type;
	public void testPartialSpecializationForVarargFunctionType_402807() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct waldo {
	//		typedef int type;
	//	};
	//
	//	template <typename R>
	//	struct waldo<R () &>;
	//
	//	typedef waldo<int ()>::type Type;
	public void testPartialSpecializationForRefQualifiedFunctionType_485888() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	struct term_traits;
	//
	//	template<typename T>
	//	struct term_traits<T const &> {
	//	    typedef T value_type;
	//	};
	//
	//	template<typename T, int N>
	//	struct term_traits<T const (&)[N]> {
	//	    typedef T value_type[N];
	//	};
	//
	//	using T = const char(&)[4];
	//	using ActualType = term_traits<T const &>::value_type;
	//
	//	using ExpectedType = char[4];
	public void testQualifierTypeThatCollapsesAfterTypedefSubstitution_487698() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ITypedef actualType = helper.assertNonProblem("ActualType");
		ITypedef expectedType = helper.assertNonProblem("ExpectedType");
		assertSameType(actualType, expectedType);
	}

	//	template <typename>
	//	struct meta {
	//	    static const bool value = 1;
	//	};
	//	template <bool>
	//	struct enable_if {};
	//	template <>
	//	struct enable_if<true> {
	//	    typedef void type;
	//	};
	//	template <class T>
	//	struct pair {
	//	    template <typename = typename enable_if<meta<T>::value>::type>
	//	    pair(int);
	//	};
	//	void push_back(pair<long>&&);
	//	void push_back(const pair<long>&);
	//	void test() {
	//	    push_back(0);
	//	}
	public void testRegression_399142() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class T>
	//	struct A {
	//	    struct impl {
	//	        static T x;
	//	    };
	//	    static const int value = sizeof(impl::x);
	//	};
	//	template <int> struct W {};
	//	template <> struct W<1> { typedef int type; };
	//	int main() {
	//	    W<A<char>::value>::type w;
	//	}
	public void testDependentExpressionInvolvingFieldInNestedClass_399362() throws Exception {
		parseAndCheckBindings();
	}

	//	struct foo {
	//	    int operator()() const;
	//	};
	//
	//	template <typename F>
	//	struct W {
	//	    F f;
	//
	//	    auto operator()() const -> decltype(f()) {
	//	        return f();
	//	    }
	//	};
	//
	//	typedef decltype(W<foo>()()) waldo;
	public void testInstantiationOfConstMemberAccess_409107() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		IType waldo = bh.assertNonProblem("waldo");
		assertSameType(waldo, CommonCPPTypes.int_);
	}

	//    template <typename _Tp>
	//    struct remove_reference {
	//        typedef _Tp type;
	//    };
	//    template <typename>
	//    struct A {};
	//    template <typename From, typename To>
	//    struct waldo {
	//        typedef typename remove_reference<From>::type src_t;
	//        typedef A<src_t> type;
	//    };
	//    template <bool First>
	//    struct ice_or {
	//        static const bool value = First;
	//    };
	//    template <typename T>
	//    struct is_waldo {
	//        static const bool value = false;
	//    };
	//    template <typename... Args>
	//    struct contains_waldo {
	//        static const bool value = ice_or<is_waldo<typename remove_reference<Args>::type>::value...>::value;
	//    };
	//    template <bool>
	//    struct S {};
	//    struct Cat {
	//        void meow();
	//    };
	//    template <>
	//    struct S<false> {
	//        typedef Cat type;
	//    };
	//    int main() {
	//        S<contains_waldo<int>::value>::type t;
	//    }
	public void testVariadicTemplates_401024() throws Exception {
		parseAndCheckBindings();
	}

	//    int fn(int);
	//    struct S {
	//        template <typename... Args>
	//        auto operator()(Args... args) -> decltype(fn(args...));
	//    };
	//    template <typename F>
	//    int foo(F);
	//    template <typename T>
	//    void bar(T);
	//    int main() {
	//        S s;
	//        bar(foo(s(0)));
	//    }
	public void testVariadicTemplatesAndFunctionObjects_401479() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename _Tp>
	//	_Tp declval() noexcept;
	//
	//	template<typename _From>
	//	struct is_convertible {};
	//
	//	template<typename _Signature>
	//	class function;
	//
	//	template<typename _Res, typename... _ArgTypes>
	//	class function<_Res(_ArgTypes...)> {
	//	  template<typename _Functor>
	//	  using _Invoke = decltype(declval<_Functor&>()(declval<_ArgTypes>()...));
	//
	//	public:
	//	  template<typename _Functor, typename = typename is_convertible<_Invoke<_Functor>>::type>
	//	  function(_Functor);
	//	};
	//
	//	class A {};
	//
	//	struct B {
	//	  B(const char* s);
	//	};
	//
	//	template <class T> void waldo(const B& response);
	//	template <class T> void waldo(function<T()> generator);
	//
	//	void test() {
	//	    waldo<A>("");
	//	}
	public void testPackExpansionInNestedTemplate_459844() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {};
	//
	//	template <typename... T>
	//	struct C : A<T>... {};
	//
	//	constexpr bool answer = __is_base_of(A<int>, C<int>);
	public void testPackExpansionInBaseSpecifier_487703() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IVariable answer = helper.assertNonProblem("answer");
		assertVariableValue(answer, 1);
	}

	//	template <template <class> class ... Mixins>
	//	struct C : Mixins<int>... {};
	//
	//	template <typename>
	//	struct SpecificMixin {};
	//
	//	constexpr bool answer = __is_base_of(SpecificMixin<int>, C<SpecificMixin>);
	public void testTemplateTemplateParameterPack_487703a() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IVariable answer = helper.assertNonProblem("answer");
		assertVariableValue(answer, 1);
	}

	//  template <template <class> class ... Mixins>
	//  struct C : Mixins<C<Mixins...>>... {};
	//
	//  template <typename>
	//  struct SpecificMixin {};
	//
	//  typedef C<SpecificMixin> Waldo;
	//  constexpr bool answer = __is_base_of(SpecificMixin<Waldo>, Waldo);
	public void testTemplateTemplateParameterPack_487703b() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IVariable answer = helper.assertNonProblem("answer");
		assertVariableValue(answer, 1);
	}

	// struct S {
	//     void kind();
	// };
	// struct T {};
	// namespace N {
	//     S operator++(T);
	//     template <class T>
	//     struct impl {
	//         static T x;
	//         typedef decltype(++x) type;
	//     };
	// }
	// void test() {
	//     N::impl<T>::type operand;
	//     operand.kind();
	// }
	public void testNameLookupInDependentExpression_399829a() throws Exception {
		parseAndCheckBindings();
	}

	// struct S {
	//     void kind();
	// };
	// namespace N {
	//   struct tag {};
	//   struct any { template <class T> any(T); };
	//   tag operator++(any);
	//   tag operator,(tag,int);
	//   S check(tag);
	//   int check(int);
	//   template <class T>
	//   struct impl {
	//       static T& x;
	//       typedef decltype(N::check((++x,0))) type;
	//   };
	// }
	// void test() {
	//     N::impl<S>::type operand;
	//     operand.kind();
	// }
	public void testNameLookupInDependentExpression_399829b() throws Exception {
		parseAndCheckBindings();
	}

	//    template <bool> int assertion_failed(void*);
	//    struct assert_ {};
	//    assert_ arg;
	//    char operator==(assert_, assert_);
	//    template <unsigned> struct assert_relation {};
	//    template<class>
	//    struct concept {
	//        typedef decltype(assertion_failed<true>((assert_relation<sizeof(arg == arg) >*)0)) type;
	//    };
	//    template <bool> struct S {};
	//    template <typename>
	//    struct is_int
	//    {
	//        static const bool value = false;
	//    };
	//    template<typename T>
	//    S<true> operator==(T, T*);
	//    template<typename T>
	//    S<(is_int<T>::value)> operator==(T, T);
	public void testRegression_399829() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//		int foo;
	//	};
	//
	//	template <typename T>
	//	auto bar(T t) -> decltype(t->foo);
	//
	//	int main() {
	//		S s;
	//		auto waldo = bar(&s);
	//	}
	public void testDependentFieldReference_472436a() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("waldo", CommonCPPTypes.int_);
	}

	//	struct T {
	//		int foo;
	//	};
	//	struct S {
	//		T* other;
	//	};
	//
	//	template <typename T>
	//	auto bar(T t) -> decltype(t->other->foo);
	//
	//	int main() {
	//		S s;
	//		auto waldo = bar(&s);
	//	}
	public void testDependentFieldReference_472436b() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("waldo", CommonCPPTypes.int_);
	}

	//	template <typename>
	//	struct Bind {};
	//	template <typename Func, typename ... BoundArgs>
	//	struct Bind_helper {
	//	    typedef Bind<Func(BoundArgs...)> type;
	//	};
	//	template <typename Func, typename ... BoundArgs>
	//	typename Bind_helper<Func, BoundArgs...>::type
	//	bind(Func, BoundArgs...);
	//	struct S {
	//	    template <typename T, typename U>
	//	    void operator()(T, U);
	//	};
	//	int main() {
	//	    S s;
	//	    bind(s, 0, foo);
	//	}
	public void testNPE_401140() throws Exception {
		BindingAssertionHelper helper = new AST2AssertionHelper(getAboveComment(), true);
		helper.assertProblem("bind(s, 0, foo)", "bind");
	}

	//	struct a3 {
	//	    int xxx;
	//	};
	//
	//	template <template <typename> class V>
	//	struct S {};
	//
	//	template <typename V>
	//	int foo(...);
	//
	//	template <typename V>
	//	int foo(void*, S<V::template xxx>* = 0);
	//
	//	int value = sizeof(foo<a3>(0));
	public void testNPE_395074() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	T forward(T);
	//	template <typename, typename S1, typename S2>
	//	int combine(S1&& r1, S2&& r2);
	//	template <typename S1, typename S2>
	//	auto combine(S1 r1, S2 r2) -> decltype(combine<int>(forward<S1>(r1), forward<S2>(r2)));
	public void testUnsupportedOperationExceptionInASTAmbiguousNode_402085() throws Exception {
		parseAndCheckBindings();
	}

	//	template <bool... Args>
	//	struct ice_or;
	//	template <bool First>
	//	struct ice_or<First> {
	//	    static const bool value = First;
	//	};
	//	template <bool>
	//	struct S {};
	//	template <>
	//	struct S<false> {
	//	    typedef int type;
	//	};
	//	int main() {
	//	    S<ice_or<false>::value>::type t;
	//	}
	public void testVariadicNonTypeTemplateParameter_401142() throws Exception {
		parseAndCheckBindings();
	}

	//	template <bool... Args>
	//	struct ice_or;
	//	template <>
	//	struct ice_or<> {
	//	    static const bool value = false;
	//	};
	//	template <bool First, bool... Rest>
	//	struct ice_or<First, Rest...> {
	//	    static const bool value = ice_or<Rest...>::value;
	//	};
	//	template <bool> struct S {};
	//	template <>
	//	struct S<false> {
	//	    typedef int type;
	//	};
	//	int main() {
	//	    S<ice_or<false, false>::value>::type t;
	//	}
	public void testVariadicNonTypeTemplateParameter_401400() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename... Args>
	//	struct foo {
	//		static constexpr int i = sizeof...(Args);
	//	};
	//	constexpr int bar = foo<int, double>::i;
	public void testSizeofParameterPackOnTypeid_401973() throws Exception {
		BindingAssertionHelper helper = new AST2AssertionHelper(getAboveComment(), true);
		ICPPVariable bar = helper.assertNonProblem("bar");
		assertConstantValue(2, bar);
	}

	//	template <int...> struct tuple_indices {};
	//	template <int Sp, class IntTuple, int Ep>
	//	struct make_indices_imp;
	//	template <int Sp, int ...Indices, int Ep>
	//	struct make_indices_imp<Sp, tuple_indices<Indices...>, Ep> {
	//	    typedef typename make_indices_imp<Sp + 1, tuple_indices<Indices..., Sp>, Ep>::type type;
	//	};
	//	template <int Ep, int ...Indices>
	//	struct make_indices_imp<Ep, tuple_indices<Indices...>, Ep> {
	//	    typedef tuple_indices<Indices...> type;
	//	};
	//	template <int Ep, int Sp = 0>
	//	struct make_tuple_indices {
	//	    typedef typename make_indices_imp<Sp, tuple_indices<>, Ep>::type type;
	//	};
	//	template <class ... Args>
	//	class async_func {
	//	    void operator()() {
	//	        typedef typename make_tuple_indices<1 + sizeof...(Args), 1>::type Index;
	//	    }
	//	};
	public void testVariadicTemplatesNPE_401743() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename = int>
	//	struct S {
	//	    typedef int A;
	//
	//	    template <typename... Args>
	//	    void waldo(A, Args...);
	//	};
	//
	//	int main() {
	//	    S<> s;
	//	    s.waldo(0, 0);  // ERROR HERE
	//	}
	public void testParameterPackInNestedTemplate_441028() throws Exception {
		parseAndCheckBindings();
	}

	//	template <int... Is> struct A {
	//	  typedef A t;
	//	};
	//
	//	template <class P, int I> struct B;
	//
	//	template <int... Is, int I>
	//	struct B<A<Is...>, I> : A<Is..., I> {};
	//
	//	template <typename, typename = void>
	//	struct prober {};
	//
	//	template <typename T>
	//	struct prober<A<0>, T> {
	//	  typedef T t;
	//	};
	//
	//	prober<B<A<>, 0>::t>::t g();
	public void testParameterPack_485806() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename... Args>
	//	void waldo(Args...);
	//
	//	int main() {
	//	    waldo<int>();
	//	}
	public void testExplicitArgumentsForParameterPack_404245() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertProblem("waldo<int>()", "waldo<int>");
	}

	//	// Example 1
	//	template <typename... T>
	//	void foo1(T... t) {
	//		bar(t.waldo...);
	//	}
	//
	//	// Example 2
	//	template <typename> struct A {};
	//	template <typename... T>
	//	void foo2(A<T>... t) {
	//		bar(t.waldo...);
	//	}
	public void testMemberAccessInPackExpansion_442213() throws Exception {
		parseAndCheckBindings();
	}

	//	// Example 1
	//	template <typename... T>
	//	void foo1(T&... t) {
	//		bar(t.waldo...);
	//	}
	//
	//	// Example 2
	//	template <typename> struct A {};
	//	template <typename... T>
	//	void foo2(A<T>&... t) {
	//		bar(t.waldo...);
	//	}
	//
	//	// Example 3
	//	template <typename... T>
	//	void foo1(T&&... t) {
	//		bar(t.waldo...);
	//	}
	//
	//	// Example 4
	//	template <typename... T>
	//	void foo1(const T&... t) {
	//		bar(t.waldo...);
	//	}
	public void testMemberAccessViaReferenceInPackExpansion_466845() throws Exception {
		parseAndCheckBindings();
	}

	//	template <int... I>
	//	struct C {};
	//
	//	template <class... T>
	//	struct B {
	//	  typedef void type;
	//	};
	//
	//	template <class T, int... I>
	//	typename B<decltype(T::template operator()<I>())...>::type
	//	waldo(T f, C<I...>);
	//
	//	struct A {};
	//
	//	void test() {
	//	  A a;
	//	  waldo(a, C<>());
	//	}
	public void testDecltypeInPackExpansion_486425a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <int... I>
	//	struct C {};
	//
	//	template <class... T>
	//	struct B {
	//	  typedef void type;
	//	};
	//
	//	template <class T, int... I>
	//	typename B<decltype(T::template undefined<I>())...>::type
	//	waldo(T f, C<I...>);
	//
	//	struct A {};
	//
	//	void test() {
	//	  A a;
	//	  waldo(a, C<>());
	//	}
	public void testDecltypeInPackExpansion_486425b() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	T __declval();
	//
	//	template <typename T>
	//	decltype(__declval<T>()) declval();
	//
	//	template <typename T>
	//	decltype(__declval<T>()) declval();
	//
	//	using T = decltype(declval<int>());
	public void testDeclvalDeclaration_540957() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	class meta {
	//	    typedef T type;
	//	};
	//
	//	template <typename... Ts>
	//	using Alias = void(typename meta<Ts>::type...);
	//
	//	template <typename... Ts>
	//	Alias<Ts...>* async(Ts...);
	//
	//	int main() {
	//	    async();  // ERROR: Invalid arguments
	//	}
	public void testDependentPackExpansionInFunctionType_526684() throws Exception {
		parseAndCheckBindings();
	}

	//	template <int> struct __make;
	//	template <> struct __make<2> { typedef int type; };
	//
	//	template <typename... T>
	//	using type_pack_element = typename __make<sizeof...(T)>::type;
	//
	//	template <typename... T>
	//	struct tuple_element {
	//	    typedef type_pack_element<T...> type;
	//	};
	//
	//	typedef tuple_element<int, int>::type Waldo;
	public void testSizeofParameterPack_527697() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ITypedef waldo = helper.assertNonProblem("Waldo");
		assertSameType(CommonCPPTypes.int_, waldo);
	}

	//	template <int, class>
	//	struct A {};
	//
	//	struct B : A<0, int>, A<1, int> {};
	//
	//	template <class T>
	//	void waldo(A<1, T>);
	//
	//	void foo() {
	//	    B b;
	//	    waldo(b);
	//	}
	public void testTemplateArgumentDeduction_MultipleInheritance_527697() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {};
	//
	//	template <typename T>
	//	struct B {
	//	  typedef int type;
	//	};
	//
	//	template <class T, const T& V>
	//	struct C {};
	//
	//	extern const char* const K = "";
	//
	//	typedef A<C<const char*, K>> D;
	//
	//	typedef B<D>::type E;
	public void testRegression_401743a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {};
	//
	//	template <typename T>
	//	struct B {
	//	  typedef int type;
	//	};
	//
	//	template <class T, const T& V>
	//	struct C {};
	//
	//	class F {};
	//
	//	extern F K;
	//
	//	typedef A<C<F, K>> D;
	//
	//	typedef B<D>::type E;
	public void testRegression_401743b() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class T>
	//	struct A {
	//	  T a;
	//	};
	//
	//	template<typename T>
	//	struct B {
	//	  typedef T* pointer;
	//	};
	//
	//	template <class U>
	//	struct C : public B<A<U>> {
	//	  typedef typename C::pointer pointer;
	//	};
	//
	//	void test() {
	//	  C<int>::pointer p;
	//	  p->a = 0;
	//	}
	public void testPseudoRecursiveTypedef_408314() throws Exception {
		CPPASTNameBase.sAllowRecursionBindings = true;
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	void foo(T t) {
	//	    bar(t);
	//	}
	public void testUnqualifiedFunctionCallInTemplate_402498a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	auto foo(T t) -> decltype(bar(t));
	//
	//	namespace N {
	//	    class A {};
	//	    int bar(A);
	//	}
	//
	//	int main() {
	//	    auto x = foo(N::A());
	//	}
	public void testUnqualifiedFunctionCallInTemplate_402498b() throws Exception {
		new AST2AssertionHelper(getAboveComment(), true).assertVariableType("x", CommonCPPTypes.int_);
	}

	//	template <typename T>
	//	auto foo(T t) -> decltype(bar(t));
	//
	//	namespace N {
	//	    class A {};
	//	}
	//
	//	int bar(N::A);
	//
	//	int main() {
	//	    auto x = foo(N::A());
	//	}
	public void testUnqualifiedFunctionCallInTemplate_402498c() throws Exception {
		BindingAssertionHelper helper = new AST2AssertionHelper(getAboveComment(), true);
		ICPPVariable x = helper.assertNonProblem("x");
		// We really should assert that x's type is a ProblemType, but the semantic
		// analyzer is too lenient and makes it a TypeOfDependentExpression if it
		// can't instantiate the return type of foo() properly.
		// That's another bug for another day.
		assertFalse(x.getType().isSameType(CommonCPPTypes.int_));
	}

	//	struct Cat { void meow(); };
	//	struct Dog { void woof(); };
	//
	//	template <typename T>
	//	Dog bar(T);
	//
	//	template <typename T>
	//	auto foo(T t) -> decltype(bar(t));
	//
	//	namespace N {
	//		class A {};
	//	}
	//
	//	Cat bar(N::A);
	//
	//	int main() {
	//		auto x = foo(N::A());
	//		x.woof();
	//	}
	public void testUnqualifiedFunctionCallInTemplate_402498d() throws Exception {
		parseAndCheckBindings();
	}

	//	void bar();
	//
	//	template <typename T>
	//	void foo(T t) {
	//	    bar(t);
	//	}
	public void testUnqualifiedFunctionCallInTemplate_458316a() throws Exception {
		parseAndCheckBindings();
	}

	//	void bar();
	//
	//	template <typename T>
	//	auto foo(T t) -> decltype(bar(t));
	//
	//	struct Cat { void meow(); };
	//
	//	namespace N {
	//	    struct S {};
	//
	//	    Cat bar(S);
	//	}
	//
	//	int main() {
	//	    foo(N::S()).meow();
	//	}
	public void testUnqualifiedFunctionCallInTemplate_458316b() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename>
	//	struct no_type {};
	//
	//	struct type {};
	//
	//	template <typename T>
	//	struct A {};
	//
	//	template <typename T>
	//	int foo(T);
	//
	//	template <typename T>
	//	typename no_type<T>::type const foo(A<T>);
	//
	//	A<int> a;
	//	auto b = foo(a);
	public void testQualifiedNameLookupInTemplate_402854() throws Exception {
		BindingAssertionHelper helper = new AST2AssertionHelper(getAboveComment(), true);
		helper.assertVariableType("b", CommonCPPTypes.int_);
	}

	//	template<typename T> struct A {
	//	  A(int c);
	//	};
	//
	//	struct B : public A<int> {
	//	  B(int c) : A(c) {}
	//	};
	public void testTemplateBaseClassConstructorCall_402602() throws Exception {
		parseAndCheckBindings();
	}

	//  template<typename T>
	//  class A {
	//    int defaultMemberVariable;
	//  public:
	//    int publicMemberVariable;
	//  protected:
	//    int protectedMemberVariable;
	//  private:
	//    int privateMemberVariable;
	//  };
	public void testTemplateMemberAccessibility() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();

		ICPPClassTemplate aTemplate = bh.assertNonProblem("A");

		ICPPField defaultMemberVariable = bh.assertNonProblem("defaultMemberVariable");
		assertVisibility(ICPPClassType.v_private, aTemplate.getVisibility(defaultMemberVariable));

		ICPPField publicMemberVariable = bh.assertNonProblem("publicMemberVariable");
		assertVisibility(ICPPClassType.v_public, aTemplate.getVisibility(publicMemberVariable));

		ICPPField protectedMemberVariable = bh.assertNonProblem("protectedMemberVariable");
		assertVisibility(ICPPClassType.v_protected, aTemplate.getVisibility(protectedMemberVariable));

		ICPPField privateMemberVariable = bh.assertNonProblem("privateMemberVariable");
		assertVisibility(ICPPClassType.v_private, aTemplate.getVisibility(privateMemberVariable));
	}

	//  template<typename T>
	//  class A {};
	//
	//  template<>
	//  class A<int> {
	//    int specializedDefaultVariable;
	//  public:
	//    int specializedPublicVariable;
	//  protected:
	//    int specializedProtectedVariable;
	//  private:
	//    int specializedPrivateVariable;
	//  };
	public void testTemplateSpecializationMemberAccessibility() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();

		ICPPClassSpecialization aTemplateSpecialization = bh.assertNonProblem("A<int>");

		ICPPField defaultMemberVariable = bh.assertNonProblem("specializedDefaultVariable");
		assertVisibility(ICPPClassType.v_private, aTemplateSpecialization.getVisibility(defaultMemberVariable));

		ICPPField publicMemberVariable = bh.assertNonProblem("specializedPublicVariable");
		assertVisibility(ICPPClassType.v_public, aTemplateSpecialization.getVisibility(publicMemberVariable));

		ICPPField protectedMemberVariable = bh.assertNonProblem("specializedProtectedVariable");
		assertVisibility(ICPPClassType.v_protected, aTemplateSpecialization.getVisibility(protectedMemberVariable));

		ICPPField privateMemberVariable = bh.assertNonProblem("specializedPrivateVariable");
		assertVisibility(ICPPClassType.v_private, aTemplateSpecialization.getVisibility(privateMemberVariable));
	}

	//  template<typename T>
	//  class A {
	//    int defaultMemberVariable;
	//  public:
	//    int publicMemberVariable;
	//  protected:
	//    int protectedMemberVariable;
	//  private:
	//    int privateMemberVariable;
	//  };
	//
	//	void test(A<int>* a) {
	//	  a->defaultMemberVariable = 0;
	//	  a->publicMemberVariable = 0;
	//	  a->protectedMemberVariable = 0;
	//	  a->privateMemberVariable = 0;
	//	}
	public void testInstanceMemberAccessibility() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();

		ICPPClassType aTemplate = bh.assertNonProblem("A<int>");

		ICPPField defaultMemberVariable = bh.assertNonProblemOnFirstIdentifier("defaultMemberVariable =");
		assertVisibility(ICPPClassType.v_private, aTemplate.getVisibility(defaultMemberVariable));

		ICPPField publicMemberVariable = bh.assertNonProblemOnFirstIdentifier("publicMemberVariable =");
		assertVisibility(ICPPClassType.v_public, aTemplate.getVisibility(publicMemberVariable));

		ICPPField protectedMemberVariable = bh.assertNonProblemOnFirstIdentifier("protectedMemberVariable =");
		assertVisibility(ICPPClassType.v_protected, aTemplate.getVisibility(protectedMemberVariable));

		ICPPField privateMemberVariable = bh.assertNonProblemOnFirstIdentifier("privateMemberVariable =");
		assertVisibility(ICPPClassType.v_private, aTemplate.getVisibility(privateMemberVariable));
	}

	//	template<bool B, class T = void>
	//	struct enable_if_c {
	//	  typedef T type;
	//	};
	//
	//	template<class T>
	//	struct enable_if_c<false, T> {
	//	};
	//
	//	template<class Cond, class T = void>
	//	struct enable_if: public enable_if_c<Cond::value, T> {
	//	};
	//
	//	template<typename T, typename = void>
	//	struct some_trait {
	//	  static const bool value = true;
	//	};
	//
	//	template<typename T>
	//	struct some_trait<T, typename enable_if_c<T::some_trait_value>::type> {
	//	  static const bool value = true;
	//	};
	//
	//	template<typename T>
	//	inline typename enable_if_c<some_trait<T>::value>::type foo() {
	//	}
	//
	//	typedef int myInt;
	//	int main() {
	//	  foo<myInt>();
	//	}
	public void testInstantiationOfTypedef_412555() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class T>
	//	struct B {};
	//
	//	template <class Abstract, class T>
	//	struct A;
	//
	//	template <class T>
	//	struct A<B<T>, T> {
	//	    void method();
	//	};
	//
	//	template <class T>
	//	void A<B<T>, T>::method() {}
	public void testOutOfLineMethodOfPartialSpecialization_401152() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	T foo(T);
	//
	//	template <typename T>
	//	struct U {
	//	    typedef typename decltype(foo(T()))::type type;
	//	};
	//
	//	struct S {
	//		typedef int type;
	//	};
	//
	//	int main() {
	//		U<S>::type x;
	//	}
	public void testDependentDecltypeInNameQualifier_415198() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertNonProblem("decltype(foo(T()))::type");
		assertSameType((ITypedef) helper.assertNonProblem("U<S>::type"), CommonCPPTypes.int_);
	}

	//	template <typename T>
	//	struct A {
	//	  typedef T type;
	//	};
	//
	//	struct B {
	//	  static const A<int> c;
	//	};
	//
	//	decltype(B::c)::type x;
	public void testDependentDecltypeInNameQualifier_429837() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		assertSameType((ITypedef) helper.assertNonProblem("decltype(B::c)::type"), CommonCPPTypes.int_);
	}

	//	namespace N {
	//	    template <typename>
	//	    struct C;
	//
	//	    template <typename T>
	//	    struct C<T*> {
	//	        C();
	//	        void waldo();
	//	    };
	//
	//	    template <typename T>
	//	    C<T*>::C() {}
	//
	//	    template <typename T>
	//	    void C<T*>::waldo() {}
	//	}
	public void testMemberOfPartialSpecialization_416788() throws Exception {
		parseAndCheckBindings();
	}

	//	template<bool>
	//	struct enable_if {
	//		typedef void type;
	//	};
	//
	//	template<int I>
	//	struct MyClass {
	//		enum {
	//			K
	//		};
	//
	//		template<int J>
	//		void method(typename enable_if<J == K>::type* = 0) {
	//		}
	//	};
	//
	//	int main() {
	//		MyClass<0> myObject;
	//		myObject.method<0>();
	//		return 0;
	//	}
	public void testSpecializedEnumerator_418770() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	class A;
	//
	//	namespace ns {
	//	    template <typename T>
	//	    int waldo(const A<T>&);
	//	}
	//
	//	template <typename T>
	//	class A {
	//	    friend int ns::waldo<T>(const A<T>&);
	//	};
	public void testDependentSpecializationOfFunctionTemplateAsFriend_422505a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	class A;
	//
	//	template <typename T>
	//	int waldo(const A<T>&);
	//
	//	template <typename T>
	//	class A {
	//	    friend int waldo<T>(const A<T>&);
	//	};
	public void testDependentSpecializationOfFunctionTemplateAsFriend_422505b() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertNonProblem("waldo<T>", ICPPDeferredFunction.class);
	}

	//	template<bool, typename T = void>
	//	struct enable_if {};
	//
	//	template<typename T>
	//	struct enable_if<true, T> {
	//	  typedef T type;
	//	};
	//
	//	template <typename T>
	//	constexpr bool F() {
	//	  return false;
	//	}
	//
	//	template <typename T>
	//	typename enable_if<!F<T>(), void>::type waldo(T p);
	//
	//	struct A {};
	//
	//	void test() {
	//	  A a;
	//	  waldo(a);
	//	}
	public void testDependentFunctionSet_485985() throws Exception {
		parseAndCheckBindings();
	}

	//	template<bool, typename T = void>
	//	struct enable_if {};
	//
	//	template<typename T>
	//	struct enable_if<true, T> {
	//	  typedef T type;
	//	};
	//
	//	template<typename>
	//	struct A {
	//	  constexpr operator int() const { return false; }
	//	};
	//
	//	template <class T>
	//	typename enable_if<!A<T>()>::type waldo(T a);
	//
	//	void test() {
	//	  waldo(0);
	//	}
	public void testDependentConversionOperator_486149() throws Exception {
		parseAndCheckBindings();
	}

	//	constexpr bool negate(bool arg) {
	//	  return !arg;
	//	}
	//
	//	template <bool B>
	//	struct boolean {
	//	  constexpr operator bool() { return B; }
	//	};
	//
	//	constexpr bool waldo = negate(boolean<true>());
	public void testDependentConversionOperator_486426() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPVariable waldo = helper.assertNonProblem("waldo");
		assertConstantValue(0, waldo);
	}

	//	template <typename T>
	//	struct traits {
	//	    static constexpr int Flags = 1;
	//	};
	//
	//	template <typename T>
	//	struct S {
	//	    static constexpr int a = traits<T>::Flags;
	//	    static constexpr int b = a ? 42 : 0;
	//	};
	//
	//	constexpr int waldo = S<int>::b;
	public void testDependentConditionalExpression_506170() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 42);
	}

	//	template <typename>
	//	struct C {
	//	    friend bool operator==(C, C);
	//	    friend bool operator!=(C, C);
	//	};
	//
	//	template <typename U>
	//	void waldo(U, U);
	//
	//	void test() {
	//	  C<int> x;
	//	  waldo(x, x);
	//	}
	public void testStrayFriends_419301() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename>
	//	struct A {
	//	  struct B {
	//	      friend B foo(B, long);
	//	      friend long foo(B, B);
	//	  };
	//	};
	//
	//	template <typename T>
	//	void waldo(T);
	//
	//	A<int>::B c;
	//	A<int>::B d;
	//
	//	void test() {
	//	  waldo(foo(c, d));
	//	}
	public void testInstantiationOfFriendOfNestedClassInsideTemplate_484162() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	constexpr T t(T) {
	//	    return 0;
	//	}
	//
	//	template <>
	//	constexpr unsigned t<unsigned>(unsigned) {
	//	    return 1 + 1;
	//	}
	//
	//	constexpr unsigned waldo = t(0u);
	public void testSpecializationOfConstexprFunction_420995() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPVariable waldo = helper.assertNonProblem("waldo");
		assertConstantValue(2, waldo);
	}

	//	struct Test {
	//        static constexpr unsigned calc_sig(const char *s, unsigned n) {
	//                return (n == 0 || *s == '\0' ? 0 :
	//                                n > 1 && *s == '%' && s[1] == '%' ?
	//                                                calc_sig(s + 2, n - 2) :
	//                                                calc_sig(s + 1, n - 1));
	//        }
	//
	//        template<unsigned sig, class ... T>
	//        static void validate_sig();
	//
	//        template<class ... T>
	//        static inline constexpr bool validate(const char *s, unsigned n) {
	//                constexpr auto sig = calc_sig(s, n);
	//                validate_sig<sig, T...>();
	//                return true;
	//        }
	//
	//	};
	public void testConstexprFunctionCallWithNonConstexprArguments_429891() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename>
	//	struct S;
	//
	//	template <>
	//	struct S<int> {
	//		static const int value = 42;
	//	};
	//
	//	template <typename T>
	//	constexpr int foo() {
	//		return S<T>::value;
	//	}
	//
	//	constexpr int waldo = foo<int>();
	public void testInstantiationOfReturnExpression_484959() throws Exception {
		getAssertionHelper().assertVariableValue("waldo", 42);
	}

	//	template <typename> class A {};
	//	template <int>      class B {};
	//	const int D = 4;
	//
	//	// Type template parameter
	//	template <typename A = A<int>>
	//	struct C1 {};
	//	C1<> c1;
	//
	//	// Template template parameter
	//	template <template <typename> class A = A>
	//	struct C2 { typedef A<int> type; };
	//	C2<>::type c2;
	//
	//	// Non-type template parameter
	//	template <int D = D>
	//	struct C3 { typedef B<D> type; };
	//	C3<>::type c3;
	public void testNameLookupInDefaultTemplateArgument_399145() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {
	//	  typedef T t;
	//	};
	//
	//	void f(char*);
	//	void g(int);
	//
	//	void test() {
	//	  {
	//	    struct B {
	//	      typedef char* b;
	//	    };
	//	    A<B>::t::b a;
	//	    f(a);
	//	  }
	//	  {
	//	    struct B {
	//	      typedef int b;
	//	    };
	//	    A<B>::t::b a;
	//	    g(a);
	//	  }
	//	}
	public void testLocalTypeAsTemplateArgument_442832() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct Bar {};
	//
	//	template <typename T>
	//	auto foo(T t) -> Bar<decltype(t.foo)> {
	//	    Bar<decltype(t.foo)> bar; // bogus `invalid template arguments` error here
	//		return bar;
	//	}
	//
	//	struct S {
	//		int foo;
	//	};
	//
	//	int main() {
	//		Bar<int> var1;
	//		auto var2 = foo(S());
	//	}
	public void testTypeOfUnknownMember_447728() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IVariable var1 = helper.assertNonProblem("var1");
		IVariable var2 = helper.assertNonProblem("var2");
		assertSameType(var1.getType(), var2.getType());
	}

	//	template <typename T>
	//	void foo() {
	//	    typedef decltype(T::member) C;
	//	    typedef decltype(C::member) D;
	//	}
	public void testScopeOfUnkownMemberType_525982() throws Exception {
		parseAndCheckBindings();
	}

	//	template <bool>
	//	struct integral_constant {
	//	    static const bool value = true;
	//	};
	//
	//	template <class>
	//	struct meta2 {
	//	    struct Test {};
	//
	//	    enum {
	//	        value = sizeof((Test()))
	//	    };
	//	};
	//
	//	struct meta : integral_constant<meta2<int>::value> {};
	//
	//	template <int>
	//	struct base {
	//	    int waldo;
	//	};
	//
	//	template <typename>
	//	struct S : base<meta::value> {
	//	    using base<meta::value>::waldo;
	//	};
	//
	//	int main() {
	//	    S<int> s;
	//	    s.waldo = 42;
	//	}
	public void testClassSpecializationInEnumerator_457511() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename> struct TypeTemplate {};
	//	template <int> struct Size_tTemplate {};
	//
	//	template <typename... ParameterPack> struct Test {
	//	  static constexpr int packSize() { return sizeof...(ParameterPack); }
	//
	//	  using type = TypeTemplate<Size_tTemplate<packSize()>>;
	//	};
	public void testAmbiguityResolutionOrder_462348a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename> struct TypeTemplate {};
	//	template <int> struct Size_tTemplate {};
	//
	//	template <typename... ParameterPack> struct Test {
	//	  static constexpr int packSize() { return sizeof...(ParameterPack); }
	//
	//	  struct nested {
	//	    using type = TypeTemplate<Size_tTemplate<packSize()>>;
	//	  };
	//	};
	public void testAmbiguityResolutionOrder_462348b() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	struct remove_reference {
	//	  typedef T type;
	//	};
	//
	//	template<typename T>
	//	struct remove_reference<T&> {
	//	  typedef T type;
	//	};
	//
	//	template<typename T>
	//	struct remove_reference<T&&> {
	//	  typedef T type;
	//	};
	//
	//	template<typename T>
	//	T&& waldo(typename remove_reference<T>::type& t);
	//
	//	template <class T>
	//	struct D {
	//	  D(T);
	//	  T t;
	//	};
	//
	//	template <class T, class U>
	//	T f(U p);
	//
	//	template <class T, class U>
	//	auto g(U&& t) -> decltype(f<T, D<U>>(D<U>{waldo<U>(t)})) {
	//	  return f<T, D<U>>(D<U>{waldo<U>(t)});
	//	}
	//
	//	struct A {};
	//
	//	template <typename T>
	//	struct B {
	//	  A a;
	//
	//	  void method() {
	//	    g<A>(a);
	//	  }
	//	};
	//
	//	void test() {
	//	  B<int> b;
	//	  b.method();
	//	}
	public void testAmbiguityResolution_469788() throws Exception {
		parseAndCheckBindings();
	}

	//	template <int I, typename T>
	//	constexpr int waldo(T v) {
	//	  return v < I ? 1 : 1 + waldo<I, T>(v / I);
	//	}
	public void _testAmbiguityResolution_497931() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename> struct S {};
	//	struct U {};
	//
	//	struct outer {
	//		struct inner {
	//			S<U> foo() {
	//				return waldo<42>(0);
	//			}
	//		};
	//
	//		template <int>
	//		static S<U> waldo(int);
	//	};
	public void testAmbiguityResolutionInNestedClassMethodBody_485388() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename...>
	//	struct Voider {
	//	  using type = void;
	//	};
	//
	//	template <typename... Args>
	//	using void_t = typename Voider<Args...>::type;
	//
	//	template <typename, template <typename...> class Op, typename... Args>
	//	struct IsDetectedImpl;
	//
	//	template <template <typename...> class Op, typename... Args>
	//	struct IsDetectedImpl<void_t<Op<Args...>>, Op, Args...> {};
	public void testAmbiguityResolution_515453() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T, T v>
	//	struct F {
	//	  static constexpr T val = v;
	//	};
	//
	//	template<typename T>
	//	struct E : public F<bool, __is_class(T)> {};
	//
	//	template<bool, typename T = void>
	//	struct D {};
	//
	//	template<typename T>
	//	struct D<true, T> {
	//	  typedef T type;
	//	};
	//
	//	template<typename T> struct C {
	//	  static constexpr int c = 0;
	//	};
	//
	//	template<typename T, T a>
	//	struct B {
	//	  template<typename U>
	//	  typename D<E<U>::val>::type waldo(U);
	//	};
	//
	//	template<typename T, T a>
	//	template<typename U>
	//	typename D<E<U>::val>::type
	//	B<T, a>::waldo(U) { // problems on B<T, a>::waldo and on U
	//	  C<T>::c; // problems on C, T and ::c
	//	}
	public void testRegression_485388a() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	template <typename T>
	//	struct A {
	//	  void ma(T);
	//	};
	//
	//	template <typename T>
	//	struct B {
	//	  void mb() {
	//	    class C {
	//	      void mc() {
	//	        return A<T>::ma(b->waldo()); // problem on waldo
	//	      }
	//
	//	      B<T>* b; // problem on B<T>
	//	    };
	//	  }
	//
	//	  int waldo();
	//	};
	public void testRegression_485388b() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename>
	//	struct Base {
	//	    template <typename>
	//	    void method(int);
	//	};
	//
	//	template <typename V>
	//	struct C : Base<V> {
	//	  typedef int WALDO;
	//
	//	  C() {
	//	    this->template method<WALDO>(0);
	//	  }
	//	};
	public void testRegression_421823() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename E>
	//	class G {};
	//
	//	template<typename E>
	//	void waldo(G<E>);
	//
	//	template <typename T>
	//	struct A {
	//	  typedef G<T> type;
	//	};
	//
	//	template <typename... T>
	//	using B = typename A<T...>::type;
	//
	//	template <typename T>
	//	class C : public B<T> {
	//	};
	//
	//	void test() {
	//	  C<int> a;
	//	  waldo(a);
	//	}
	public void testRecursiveTemplateClass_484786() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct S {
	//		static const bool value = true;
	//	};
	//
	//	typedef int Int;
	//
	//	void waldo() noexcept(S<Int>::value) {}
	public void testDisambiguationInNoexceptSpecifier_467332() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct C {
	//	    T field;
	//	    void meow();
	//	};
	//	struct S {
	//	    template <typename U>
	//	    auto operator()(U u) -> decltype(C<U>{u});
	//	};
	//	int main() {
	//	    S()(0).meow();  // ERROR: Method 'meow' could not be resolved
	//	}
	public void testBraceInitialization_490475a() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//	    int x;
	//	    int y;
	//	};
	//
	//	constexpr int foo(S a, S b) {
	//	    return a.x - b.x;
	//	}
	//
	//	constexpr S a = S{8, 0};
	//	constexpr S b = S{21, 0};
	//
	//	constexpr int waldo = foo(a, b);
	public void testBraceInitialization_490475b() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IVariable waldo = helper.assertNonProblem("waldo");
		helper.assertVariableValue("waldo", -13);
	}

	//	template<int P, int Q>
	//	struct gcd : gcd<Q, P % Q> {};
	//
	//	template<int P>
	//	struct gcd<P, 0> {
	//	    static constexpr int value = P;
	//	};
	//
	//	template<int Q>
	//	struct gcd<0, Q> {
	//	    static constexpr int value = Q;
	//	};
	//
	//	template<int N, int D = 1>
	//	struct ratio {
	//	    static constexpr int den = D / gcd<N, D>::value;
	//	};
	//
	//	constexpr int foo(int) {
	//	    return 42;
	//	}
	//
	//	struct S : public ratio<foo("")> {};
	//
	//	template<typename R1, typename R2>
	//	struct ratio_multiply {
	//	    static constexpr int div = gcd<1, R1::den>::value;
	//	    typedef ratio<1, R1::den / div> type;
	//	};
	//
	//	typedef ratio_multiply<S, ratio<1>>::type waldo;
	public void testOOM_508254() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		// Just check that resolution does not throw an exception.
		helper.findName("waldo").resolveBinding();
	}

	//	template <typename Ty>
	//	struct has_rbegin_impl {
	//	    typedef char yes[1];
	//	    typedef char no[2];
	//	    template <typename Inner>
	//	    static yes& test(Inner *I, decltype(I->rbegin()) * = nullptr);
	//	    template <typename >
	//	    static no& test(...);
	//	    static const bool value = sizeof(test<Ty>(nullptr)) == sizeof(yes);
	//	};
	//
	//	template <bool, typename _Tp = void>
	//	struct enable_if {};
	//
	//	template <typename _Tp>
	//	struct enable_if<true, _Tp> {
	//	    typedef _Tp type;
	//	};
	//
	//	template <typename Container>
	//	void reverse(Container&& C, typename enable_if<has_rbegin_impl<Container>::value>::type * = nullptr);
	//
	//	template <typename Container>
	//	void reverse(Container&& C, typename enable_if<!has_rbegin_impl<Container>::value>::type * = nullptr);
	//
	//	class MyContainer{};
	//
	//	int main() {
	//	    MyContainer c;
	//	    reverse(c);   // Ambiguous
	//	}
	public void testSFINAEInEvalIdWithFieldOwner_510834() throws Exception {
		parseAndCheckBindings();
	}

	//	class C {};
	//
	//	void aux(C);
	//
	//	template<typename T>
	//	decltype(aux(T())) foo(T);
	//
	//	int foo(...);
	//
	//	void waldo(int);
	//
	//	int main() {
	//	    waldo(foo(0));  // Error here
	//	}
	public void testSFINAEInDecltype_516291a() throws Exception {
		parseAndCheckBindings();
	}

	//	class C {};
	//
	//	void aux(C);
	//
	//	template<typename T>
	//	decltype(aux(T()), C()) foo(T);
	//
	//	int foo(...);
	//
	//	void waldo(int);
	//
	//	int main() {
	//	    waldo(foo(0));  // Error here
	//	}
	public void testSFINAEInDecltype_516291b() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename>
	//	using void_t = void;
	//
	//	template <typename T, typename = void>
	//	struct Waldo {
	//	    using type = T;
	//	};
	//
	//	template <typename T>
	//	struct Waldo<T, void_t<typename T::type>> {};
	//
	//	Waldo<int>::type foo();
	public void testSFINAEInAliasTemplateArgs_516338() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename, typename>
	//	struct is_same {
	//	    static constexpr bool value = false;
	//	};
	//
	//	template <typename T>
	//	struct is_same<T, T> {
	//	    static constexpr bool value = true;
	//	};
	//
	//	template <bool C, typename>
	//	struct enable_if {};
	//
	//	template <typename R>
	//	struct enable_if<true, R> {
	//	    typedef R type;
	//	};
	//
	//	template <typename, typename>
	//	struct arg {};
	//
	//	template <typename>
	//	struct param {
	//	    template<typename I>
	//	    param(arg<I, typename enable_if<is_same<I, int>::value, int>::type>&) {}
	//	};
	//
	//	void foo(param<int>);
	//
	//	void bar(arg<int, int>& x) {
	//	    foo(x);
	//	}
	public void testInstantiationOfEvalIdWithFieldOwner_511108() throws Exception {
		parseAndCheckBindings();
	}

	//	class C {};
	//	typedef C D;
	//
	//	template <typename T, typename = decltype(T().~T())>
	//	void test();
	//
	//	void foo() {
	//	    test<C>();
	//		test<const C>();
	//		test<D>();
	//		test<const D>();
	//	}
	public void testDependentDestructorName_511122() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class T>
	//	using alias = T;
	//
	//	struct A {};
	//
	//	void foo() {
	//	    A a;
	//	    a.~alias<A>();
	//	}
	public void testDestructorCallViaAliasedTemplateName_511658() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename> struct Waldo {};
	//	Waldo<void() noexcept> var;
	public void testNoexceptSpecifierInTypeTemplateArgument_511186() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace ns {
	//
	//	template <typename T>
	//	class A {
	//	  friend void waldo(A<int> flag);
	//	};
	//
	//	void waldo(A<int> flag);
	//
	//	}
	//
	//	ns::A<int> a;
	//
	//	void func() {
	//	  waldo(a);
	//	}
	public void testFriendFunctionDeclarationInNamespace_513681() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class T, unsigned long Size = sizeof(T)>
	//	class foobar {};
	//
	//	template<class T>
	//	void waldo(foobar<T>) {}
	//
	//	void bar() {
	//	    foobar<int> obj;
	//	    waldo(obj);         // Error: Invalid arguments
	//	}
	public void testDependentSizeofInDefaultArgument_513430() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//	    int& foo();
	//	};
	//
	//	template<typename T>
	//	decltype(S().foo()) bar();
	//
	//	void waldo(int&);
	//
	//	int main() {
	//	    waldo(bar<S>());
	//	}
	public void testDependentMemberAccess_516290() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename>
	//	struct A;
	//
	//	template <typename T>
	//	struct A<T*> {
	//	    A(int);
	//	    A() : A(5) {}
	//	};
	public void testDelegatingConstructorInPartialSpecialization_512932() throws Exception {
		parseAndCheckBindings();
	}

	//	enum class E { F };
	//
	//	template <unsigned char>
	//	void foo();
	//
	//	template <E>
	//	void foo();
	//
	//	int main() {
	//	    foo<E::F>();  // error here
	//	}
	public void testOverloadingOnTypeOfNonTypeTemplateParameter_512932() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	void waldo(T&);
	//
	//	class A {};
	//	typedef const A CA;
	//
	//	int main() {
	//	    waldo(CA());
	//	}
	public void testReferenceBinding_Regression_516284() throws Exception {
		parseAndCheckBindings();
	}

	//	// Declare a constexpr function that turns int and int& into different values.
	//	template <typename T> constexpr int foo();
	//	template <> constexpr int foo<int>() { return 42; };
	//	template <> constexpr int foo<int&>() { return 43; };
	//
	//	template <typename T>
	//	constexpr int bar(T arg) {
	//		// Bind a TypeOfDependentExpression to an 'auto'.
	//	    auto local = *&arg;
	//
	//		// Leak the deduced type via the return value.
	//	    return foo<decltype(local)>();
	//	}
	//
	//	// Instantiate with [T = int] and capture the return value.
	//	constexpr int waldo = bar(0);
	public void testDependentTypeBindingToAuto_408470() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		// Check that the TypeOfDependentExpression instantiated to the correct type.
		helper.assertVariableValue("waldo", 42);
	}

	//	template <int N>
	//	struct Model {
	//	    static constexpr int getFamily() {
	//	        if (N < 1350)
	//	            return 1300;
	//	        else
	//	            return 1400;
	//	    }
	//	    static constexpr int res = getFamily();
	//	};
	//
	//	constexpr int waldo = Model<1302>::res;
	public void testStaticConstexprFunctionWithDependentBody_521274a() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 1300);
	}

	//	template <int N>
	//	struct constant {
	//		static constexpr int value = N;
	//	};
	//	template <int N>
	//	struct Model {
	//	    static constexpr int getFamily() {
	//	        if (N < 1350)
	//	            return 1300;
	//	        else
	//	            return 1400;
	//	    }
	//		using family_t = constant<getFamily()>;
	//	};
	//
	//	constexpr int waldo = Model<1302>::family_t::value;
	public void testStaticConstexprFunctionWithDependentBody_521274b() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 1300);
	}

	//	template <class>
	//	struct A {
	//	    template <class>
	//	    struct B {
	//	        enum { val = 0 };
	//	    };
	//	};
	//
	//	template <class X>
	//	struct C {
	//	    struct D : A<X> {};
	//	    enum { val = D::template B<X>::val };
	//	};
	public void testMemberOfUnknownMemberClass_519819() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class> class any {};
	//	typedef any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//			    any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<any<any<any<any<any<any<
	//				any<any<any<any<any<any<any<any<any<int>>>>>>>>>>>>>>>>>>>>>
	//			   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	//			   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	//			   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	//			   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	//			   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	//			   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	//			   >>>>>>>>>>>>> parser_killer_type;
	public void testTemplateArgumentNestingDepthLimit_512297() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IASTTranslationUnit tu = helper.getTranslationUnit();
		IASTDeclaration[] declarations = tu.getDeclarations();
		assertEquals(2, declarations.length);
		assertInstance(declarations[1], IASTProblemDeclaration.class);
		IASTProblemDeclaration problemDecl = (IASTProblemDeclaration) declarations[1];
		IASTProblem problem = problemDecl.getProblem().getOriginalProblem();
		assertNotNull(problem);
		assertEquals(IProblem.TEMPLATE_ARGUMENT_NESTING_DEPTH_LIMIT_EXCEEDED, problem.getID());
	}

	//	template<class... Ts>
	//	struct infinite;
	//
	//	template<class Infinite, int N>
	//	struct infinite_generator {
	//	  typedef infinite<typename infinite_generator<Infinite, N-1>::type> type;
	//	};
	//
	//	template<class Infinite>
	//	struct infinite_generator<Infinite, 0> {
	//	  typedef Infinite type;
	//	};
	//
	//	template<class... Ts>
	//	struct infinite {
	//	  typedef infinite<Ts...> self_type;
	//
	//	  template<int N>
	//	  static typename infinite_generator<self_type, N>::type generate() {
	//	    return typename infinite_generator<self_type, N>::type();
	//	  }
	//	};
	//
	//	auto parser_killer_2 = infinite<int>::generate<400>();
	public void testTemplateInstantiationDepthLimit_512297() throws Exception {
		CPPASTNameBase.sAllowRecursionBindings = true;
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertProblem("generate<400>", "generate<400>");
	}

	//	template <typename T> T declval();
	//
	//	template <class T>
	//	using destructor_expr_t = decltype(declval<T>().~T());
	//
	//	typedef destructor_expr_t<int> Waldo;
	public void testDestructorExpressionType_528846() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IType waldo = helper.assertNonProblem("Waldo");
		assertSameType(CommonCPPTypes.void_, waldo);
	}

	//	template <int, int, int, int, int, int, int, int> int constant8f();
	//
	//	template <int i0, int i1, int i2, int i3>
	//	void foo() {
	//	    constant8f<
	//	      i0 < 0, i0 < 0,
	//	      i1 < 0, i1 < 0,
	//	      i2 < 0, i2 < 0,
	//	      i3 < 0, i3 < 0>();
	//	}
	public void testTemplateIdAmbiguity_529696() throws Exception {
		parseAndCheckBindings();
	}

	//	template <int...>
	//	using index_sequence = int;
	//
	//	template <int... I>
	//	void foo(index_sequence<I...>);
	public void testTemplateAliasWithVariadicNonTypeArgs_530086a() throws Exception {
		parseAndCheckBindings();
	}

	//	template <int...>
	//	struct integer_sequence {};
	//
	//	template <int... I>
	//	using index_sequence = integer_sequence<I...>;
	//
	//	template <typename, int... I>
	//	void bar(index_sequence<I...>);
	//
	//	void foo() {
	//	    bar<int>(integer_sequence<0>{});
	//	}
	public void testTemplateAliasWithVariadicArgs_530086b() throws Exception {
		parseAndCheckBindings();
	}

	//	template<bool, typename _Tp = void>
	//	struct enable_if {};
	//
	//	template<typename _Tp>
	//	struct enable_if<true, _Tp> { typedef _Tp type; };
	//
	//	template<typename _Tp> _Tp&& declval();
	//
	//	template<typename _Signature> class function;
	//
	//	template<typename _Res, typename... _ArgTypes>
	//	class function<_Res(_ArgTypes...)>
	//	{
	//	  template<typename _Func,
	//	           typename _Res2 = decltype(declval<_Func&>()(declval<_ArgTypes>()...))>
	//	  struct _Callable { };
	//
	//	public:
	//	  template<typename _Functor,
	//	           typename = typename enable_if<_Callable<_Functor>::value, void>::type>
	//	    function(_Functor);
	//	};
	//
	//	void do_with_cql_env(function<void(int&)> func);
	//
	//	void test_range_queries() {
	//	   do_with_cql_env([] (auto& e) {
	//	        return e.create_table([](auto ks_name) {
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        }).then([&e] {
	//	            return e.foo();
	//	        });
	//	    });
	//	}
	public void testLongDependentFunctionCallChain_530692() throws Exception {
		parseAndCheckBindings();
	}

	//	template<int Idx>
	//	struct get_from_variadic_pack {
	//		template<typename First, typename ... Accessors>
	//		static constexpr int apply(First first, Accessors... args) {
	//			return get_from_variadic_pack<Idx - 1>::apply(args...);
	//		}
	//	};
	//
	//	template<>
	//	struct get_from_variadic_pack<0> {
	//		template<typename First, typename ... Accessors>
	//		static constexpr int apply(First first, Accessors ... args) {
	//			return first;
	//		}
	//	};
	//
	//	template<int N>
	//	struct static_int{
	//		static constexpr int value = N;
	//	};
	//
	//	constexpr int tmp = get_from_variadic_pack<1>::apply(1,2);
	//	constexpr int result = static_int<tmp>::value;
	public void testInstantiationOfPackInNestedTemplate_540758() throws Exception {
		parseAndCheckBindings();
	}

	//	// A metafunction that loops infinitely on odd inputs.
	//	template <int N>
	//	struct meta {
	//	    static constexpr int value = 1 + meta<N - 2>::value;
	//	};
	//	template <>
	//	struct meta<0> {
	//	    static constexpr int value = 0;
	//	};
	//
	//	// A constexpr function that calls 'meta' on an odd input
	//  // but only in the uninstantiated branch of a constexpr if.
	//	template <int N>
	//	constexpr int foo() {
	//	    if constexpr (N % 2 != 0) {
	//	        return meta<N - 1>::value;
	//	    } else {
	//	        return meta<N>::value;
	//	    }
	//	}
	//
	//	// Call the function
	//	constexpr int waldo = foo<7>();
	public void testConditionalInstantiationOfConstexprIfTrueBranch_527427() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 3);
	}

	//	// A metafunction that loops infinitely on odd inputs.
	//	template <int N>
	//	struct meta {
	//	    static constexpr int value = 1 + meta<N - 2>::value;
	//	};
	//	template <>
	//	struct meta<0> {
	//	    static constexpr int value = 0;
	//	};
	//
	//	// A constexpr function that calls 'meta' on an odd input
	//  // but only in the uninstantiated branch of a constexpr if.
	//	template <int N>
	//	constexpr int foo() {
	//	    if constexpr (N % 2 == 0) {
	//	        return meta<N>::value;
	//	    } else {
	//	        return meta<N - 1>::value;
	//	    }
	//	}
	//
	//	// Call the function
	//	constexpr int waldo = foo<7>();
	public void testConditionalInstantiationOfConstexprIfFalseBranch_527427() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 3);
	}

	//	template <int N>
	//	constexpr int fib() {
	//	    if constexpr (N == 0) {
	//	        return 0;
	//	    } else if constexpr (N == 1) {
	//	        return 1;
	//	    } else {
	//	        return fib<N - 1>() + fib<N - 2>();
	//	    }
	//	}
	//
	//	// Call the function
	//	constexpr int waldo = fib<7>();
	public void testConstexprFibonacciConstexprIf_527427() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 13);
	}

	//	constexpr int g(int x) {
	//	    return x * 2;
	//	}
	//
	//	template <int N>
	//	constexpr int foo() {
	//	    if constexpr (constexpr auto x = g(N)) {
	//	        return 14 / x;
	//	    } else {
	//	        return 0;
	//	    }
	//	}
	//
	//	// Call the function
	//	constexpr int waldo = foo<2>();
	public void testConstexprIfDeclarationTrueBranch_527427() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 3);
	}

	//	constexpr int g(int x) {
	//	    return x * 2;
	//	}
	//
	//	template <int N>
	//	constexpr int foo() {
	//	    if constexpr (constexpr auto x = g(N)) {
	//	        return 14 / x;
	//	    } else {
	//	        return 42;
	//	    }
	//	}
	//
	//	// Call the function
	//	constexpr int waldo = foo<0>();
	public void testConstexprIfDeclarationFalseBranch_527427() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 42);
	}

	//	constexpr auto foo() {
	//	    if constexpr (false) {
	//	        return "Error";
	//	    } else {
	//	        return 42;
	//	    }
	//	}
	//
	//	// Call the function
	//	constexpr auto waldo = foo();
	public void testReturnAutoConstexprIfDeclarationFalseBranchValueExpression_527427() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 42);
	}

	//	constexpr auto foo() {
	//	    if constexpr (true) {
	//	        return 42;
	//	    } else {
	//	        return "Error";
	//	    }
	//	}
	//
	//	// Call the function
	//	constexpr auto waldo = foo();
	public void testReturnAutoConstexprIfDeclarationTrueBranchValueExpression_527427() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 42);
	}

	//	namespace std {
	//	    template <class E>
	//	    struct initializer_list {
	//	        E* array;
	//	        unsigned long len;
	//	    };
	//	}
	//
	//	template <typename T>
	//	struct vector {
	//	    vector(std::initializer_list<T>);
	//
	//	    template <typename InputIterator>
	//	    vector(InputIterator, InputIterator);
	//	};
	//
	//	struct mystring {
	//	    mystring(const char*);
	//	};
	//
	//	void foo(vector<mystring>);
	//
	//	int main() {
	//	    char** begin;
	//	    char** end;
	//	    foo({begin, end});
	//	}
	public void testOverloadResolutionWithInitializerList_531322() throws Exception {
		parseAndCheckBindings();
	}

	//	using size_t = decltype(sizeof(int));
	//
	//	template <class Fn, class... Ts>
	//	using MetaApply = typename Fn::template apply<Ts...>;
	//
	//	template <class... Ts>
	//	struct TypeList {
	//		using type = TypeList;
	//
	//		template <class Fn>
	//		using apply = MetaApply<Fn, Ts...>;
	//	};
	//
	//	struct Empty {};
	//
	//	namespace impl {
	//		template <bool B>
	//		struct If_ {
	//			template <class T, class U>
	//			using apply = T;
	//		};
	//		template <>
	//		struct If_<false> {
	//			template <class T, class U>
	//			using apply = U;
	//		};
	//	}
	//
	//	template <bool If_, class Then, class Else>
	//	using If = MetaApply<impl::If_<If_>, Then, Else>;
	//
	//	template <template <class...> class C, class... Ts>
	//	class MetaDefer {
	//		template <template <class...> class D = C, class = D<Ts...>>
	//		static char(&try_(int))[1];
	//		static char(&try_(long))[2];
	//		struct Result {
	//			using type = C<Ts...>;
	//		};
	//
	//	public:
	//		template <class... Us>
	//		using apply = typename If<sizeof(try_(0)) - 1 || sizeof...(Us), Empty, Result>::type;
	//	};
	//
	//	struct MetaIdentity {
	//		template <class T>
	//		using apply = T;
	//	};
	//
	//	template <template <class...> class C>
	//	struct MetaQuote {
	//		template <class... Ts>
	//		using apply = MetaApply<MetaDefer<C, Ts...>>;
	//	};
	//
	//	template <>
	//	struct MetaQuote<TypeList> {
	//		template <class... Ts>
	//		using apply = TypeList<Ts...>;
	//	};
	//
	//	template <class Fn>
	//	struct MetaFlip {
	//		template <class A, class B>
	//		using apply = MetaApply<Fn, B, A>;
	//	};
	//
	//	namespace impl {
	//		template <class Fn>
	//		struct FoldL_ {
	//			template <class... Ts>
	//			struct Lambda : MetaIdentity {};
	//			template <class A, class... Ts>
	//			struct Lambda<A, Ts...> {
	//				template <class State>
	//				using apply = MetaApply<Lambda<Ts...>, MetaApply<Fn, State, A>>;
	//			};
	//			template <class... Ts>
	//			using apply = Lambda<Ts...>;
	//		};
	//	}
	//
	//	template <class List, class State, class Fn>
	//	using TypeReverseFold = MetaApply<MetaApply<List, impl::FoldL_<Fn>>, State>;
	//
	//	template <class Car, class Cdr = Empty>
	//	struct Cons {};
	//	using Fn = MetaQuote<Cons>;
	//	using T4 = TypeReverseFold<
	//      // Make it long enough to be sure that if the runtime is exponential
	//      // in the length of the list, the test suite times out.
	//		TypeList<int, short, void, float, double, long, char
	//               int*, short*, void*, float*, double*, long*, char*>,
	//		Empty,
	//		MetaFlip<Fn>>;
	//
	//	template <class T>
	//	struct Dummy {
	//		static const bool value = true;
	//	};
	//	int main() {
	//		static_assert(Dummy<T4>::value, "");
	//	}
	public void testMetaprogrammingWithAliasTemplates_534126() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class>
	//	struct hhh {
	//	    using type = int;
	//	};
	//
	//	template <template <class> class TT>
	//	struct iii {
	//	    using type = typename TT<int>::type;
	//	};
	//
	//	template <class A>
	//	using hhh_d = hhh<A>;
	//
	//	using waldo = typename iii<hhh_d>::type;
	public void testAliasTemplateAsTemplateTemplateArg_539076() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IType waldo = helper.assertNonProblem("waldo");
		assertSameType(SemanticUtil.getSimplifiedType(waldo), CommonCPPTypes.int_);
	}

	//	template <bool...>
	//	struct list {};
	//
	//	template <class... Ts>
	//	using foo = list<Ts::value...>;
	//
	//	template<class T> struct trigger{};
	//
	//	template <class T>
	//	using evaluate = trigger<foo<T>>;
	public void testNonTypePackExpansion_540538() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class>
	//	struct foo{
	//	    template<class>
	//	    struct apply{
	//	    };
	//	};
	//	template <template <class> class F> struct capture {};
	//	template <class F> using forward = capture<foo<F>::template apply>;
	//	struct dummy1 {};
	//	using bar = forward<dummy1>;
	//	template <class> struct dummy2 {};
	//	using trigger = dummy2<bar>;
	public void testDependentTemplateTemplateArgument_540450() throws Exception {
		parseAndCheckBindings();
	}

	//	struct type{};
	//
	//	template <template <class> class T1>
	//	using template_template_alias = type;
	//
	//	template<typename T2>
	//	struct foo{
	//		template <typename T3>
	//		struct apply{};
	//	};
	//
	//	template <class T4>
	//	using trigger = template_template_alias<foo<T4>::template apply>;
	public void testAliasTemplateWithTemplateTemplateParameter_540676() throws Exception {
		parseAndCheckBindings();
	}

	//	struct type{};
	//
	//	template <template <class, class> class T1> // number of arguments doesn't match
	//	using template_template_alias = type;
	//
	//	template<typename T2>
	//	struct foo{
	//		template <typename T3>
	//		struct apply{};
	//	};
	//
	//	template <class T4>
	//	using trigger = template_template_alias<foo<T4>::template apply>;
	//
	//	using A = trigger<type>;
	//
	//	template <typename> struct B;
	//	using C = B<A>;
	public void testInvalidAliasTemplateWithTemplateTemplateParameter_540676() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertProblem("B<A>", 4);
	}

	//	struct my_type {};
	//
	//	template <class>
	//	using my_type_alias = my_type;
	//
	//	template <class... Ts>
	//	struct foo {
	//	    template <class... Vs>
	//	    static int select(my_type_alias<Ts>..., Vs...);
	//
	//	    using type = decltype(select(Ts()...));
	//	};
	//
	//	template <class> struct trigger{};
	//
	//	using A = trigger<foo<my_type>::type>;
	public void testParameterPackInAliasTemplateArgs_540741() throws Exception {
		parseAndCheckBindings();
	}

	//	void foo(int);
	//
	//	template <typename... T>
	//	using Res = decltype(foo(T()...));
	//
	//	template <typename... T>
	//	struct Bind {
	//	    using Type = Res<T...>;
	//	};
	//
	//	using Waldo = Bind<int>::Type;
	public void testPackExpansionExprInAliasTemplate_541549() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IType waldo = helper.assertNonProblem("Waldo");
		assertSameType(waldo, CommonCPPTypes.void_);
	}

	//	template <class T>
	//	void foo(T = {});
	//
	//	template <class U>
	//	void foo(U*);  // more specialized
	//
	//	int main() {
	//	    int* p;
	//	    foo(p);
	//	}
	public void testDisambiguateFunctionWithDefaultArgument_541474() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class T>
	//	void foo(T = {});
	//
	//	template <class U>
	//	void foo(U*);  // more specialized
	//
	//	// Which one is this an explicit spec. of?
	//	template <>
	//	void foo(int*);
	public void testDisambiguateFunctionWithDefaultArgumentExplicitInstantiation_541474() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A {
	//	    template <typename T>
	//	    A(T = {});
	//
	//	    template <typename U>
	//	    A(U*);  // more specialized
	//	};
	//
	//	void bar(A);
	//
	//	void foo() {
	//	    int* p;
	//      // Which constructor is used for the conversion?
	//	    bar(p);
	//	}
	public void testDisambiguateFunctionWithDefaultArgumentConversion_541474() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class T>
	//	void foo(T = {});
	//
	//	template <class U>
	//	void foo(U*);  // more specialized
	//
	//	int main() {
	//	    using FPtr = void(*)(int*);
	//	    // Which one are we taking the address of?
	//	    FPtr x = &foo;
	//	}
	public void testDisambiguateFunctionWithDefaultArgumentDeclaration_541474() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	void info(T a) {
	//	}
	//	template<typename... Args>
	//	void info(int a, Args... args) {
	//		// this is more specialized
	//	}
	//	int main( ) {
	//		info(1);
	//	}
	public void testDisambiguateFunctionUnusedPack_541474() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename A, typename B = int>
	//	void foo(A, B=0); // this overload is taken
	//
	//	template<typename A, typename... B>
	//	void foo(A, B...);
	//
	//	int main() {
	//		foo(0);
	//	}
	public void testDisambiguateFunctionUnusedPackVsDefault_541474() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, CPP);

		ICPPFunctionTemplate f1 = bh.assertNonProblem("foo(A, B=0)", 3);
		ICPPFunctionTemplate f2 = bh.assertNonProblem("foo(A, B...)", 3);

		ICPPTemplateInstance t;
		t = bh.assertNonProblem("foo(0)", 3);
		assertSame(f1, t.getTemplateDefinition());
	}

	//	template<typename A, typename B = int, typename... C>
	//	void foo(A, B=0, C...);
	//
	//	template<typename A, typename... B>
	//	void foo(A, B...);
	//
	//	int main() {
	//		foo(0);
	//	}
	public void testDisambiguateFunctionUnusedPackVsDefault2_541474() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		// clang (7.0.0) and gcc (7.3.1) disagree, clang thinks this is ambiguous
		// which seems correct according to [temp.deduct.partial] p11
		bh.assertProblem("foo(0)", 3);
	}

	//  template <auto First, auto...>
	//  struct getFirst {
	//      static constexpr auto value = First;
	//  };
	//  template <auto, auto Second, auto...>
	//  struct getSecond {
	//      static constexpr auto value = Second;
	//  };
	//	template <auto... T>
	//	struct A {
	//	    static constexpr auto first = getFirst<T...>::value;
	//	    static constexpr auto second = getSecond<T...>::value;
	//	};
	//
	//	typedef A<42,43> B;
	//  static constexpr auto val1 = B::first;
	//  static constexpr auto val2 = B::second;
	public void testVariadicTemplateAuto_544681() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("val1", 42);
		helper.assertVariableValue("val2", 43);
	}

	//  template <typename T>
	//  constexpr T id(T a) {
	//      return a;
	//  }
	//
	//  template <int> struct Waldo {using type = int;};
	//
	//  const int forty_two = 42;
	//  using const_int_ref = int const&;
	//  const_int_ref ref_forty_two = forty_two;
	//
	//  Waldo<id(ref_forty_two)>::type a;
	public void testGlobalConstWorksAsConstExpression_545756() throws Exception {
		parseAndCheckBindings();
	}
}
