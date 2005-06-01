/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 11, 2005
 */
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;

/**
 * @author aniefer
 */
public class AST2TemplateTests extends AST2BaseTest {
	
	public void testBasicClassTemplate() throws Exception {
		IASTTranslationUnit tu = parse( "template <class T> class A{ T t; };", ParserLanguage.CPP ); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        assertEquals( col.size(), 4 );
        ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
        ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
        
        
        ICPPTemplateScope scope = (ICPPTemplateScope) T.getScope();
        IScope s2 = A.getScope();
        assertSame( scope, s2 );
        
        ICPPField t = (ICPPField) col.getName(3).resolveBinding();
        ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(2).resolveBinding();
        
        assertSame( T, T2 );
        IType type = t.getType();
        assertSame( type, T );
        
        assertNotNull( T );
		assertNotNull( A );
	}
	
	public void testBasicTemplateInstance_1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template < class T > class A {             \n"); //$NON-NLS-1$
		buffer.append("   T t1;                                   \n"); //$NON-NLS-1$
		buffer.append("   T * t2;                                 \n"); //$NON-NLS-1$
		buffer.append("};                                         \n"); //$NON-NLS-1$
		buffer.append("void f(){                                  \n"); //$NON-NLS-1$
		buffer.append("   A<int> a;                               \n"); //$NON-NLS-1$
		buffer.append("   a.t1; a.t2;                             \n"); //$NON-NLS-1$
		buffer.append("}                                          \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        assertEquals( col.size(), 14 );
        
        ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
        ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
        ICPPField t1 = (ICPPField) col.getName(3).resolveBinding();
        ICPPField t2 = (ICPPField) col.getName(5).resolveBinding();
        
        assertSame( t1.getType(), T );
        assertSame( ((IPointerType)t2.getType()).getType(), T );
        
        ICPPVariable a = (ICPPVariable) col.getName(9).resolveBinding();
        
        ICPPClassType A_int = (ICPPClassType) col.getName(7).resolveBinding();
        assertSame( A_int, a.getType() );
        
        assertTrue( A_int instanceof ICPPTemplateInstance );
        assertSame( ((ICPPTemplateInstance)A_int).getTemplateDefinition(), A );
        
        ICPPClassScope A_int_Scope = (ICPPClassScope) A_int.getCompositeScope();
        assertNotSame( A_int_Scope, ((ICompositeType) A).getCompositeScope() );
        
        ICPPField t = (ICPPField) col.getName(11).resolveBinding();
        assertTrue( t instanceof ICPPSpecialization );
        assertSame( ((ICPPSpecialization)t).getSpecializedBinding(), t1 );
        assertSame( t.getScope(), A_int_Scope );
        IType type = t.getType();
        assertTrue( type instanceof IBasicType );
        assertEquals( ((IBasicType)type).getType(), IBasicType.t_int );
        
        t = (ICPPField) col.getName(13).resolveBinding();
        assertTrue( t instanceof ICPPSpecialization );
        assertSame( ((ICPPSpecialization)t).getSpecializedBinding(), t2 );
        assertSame( t.getScope(), A_int_Scope );
        type = t.getType();
        assertTrue( type instanceof IPointerType );
        assertTrue( ((IPointerType)type).getType() instanceof IBasicType );
        assertEquals( ((IBasicType)((IPointerType)type).getType()).getType(), IBasicType.t_int );
	}
	
	public void testBasicTemplateInstance_2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template < class T > class A {             \n"); //$NON-NLS-1$
		buffer.append("   T f( T *);                              \n"); //$NON-NLS-1$
		buffer.append("};                                         \n"); //$NON-NLS-1$
		buffer.append("void g(){                                  \n"); //$NON-NLS-1$
		buffer.append("   A<int> a;                               \n"); //$NON-NLS-1$
		buffer.append("   a.f( (int*)0 );                         \n"); //$NON-NLS-1$
		buffer.append("}                                          \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();
        ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
        ICPPMethod f = (ICPPMethod) col.getName(3).resolveBinding();
        IFunctionType ft = f.getType();
        
        assertSame( ft.getReturnType(), T );
        assertSame( ((IPointerType)ft.getParameterTypes()[0]).getType(), T ); 
		
		ICPPClassType A_int = (ICPPClassType) col.getName(7).resolveBinding();
		assertTrue( A_int instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)A_int).getTemplateDefinition(), A );
		
		ICPPMethod f_int = (ICPPMethod) col.getName(11).resolveBinding();
		assertTrue( f_int instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)f_int).getSpecializedBinding(), f );
		ft = f_int.getType();
		assertTrue( ft.getReturnType() instanceof IBasicType );
		assertTrue( ((IPointerType)ft.getParameterTypes()[0]).getType() instanceof IBasicType );
	}
	
	public void testBasicTemplateFunction() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T > void f( T );          \n"); //$NON-NLS-1$
		buffer.append("template <class T > void f( T ) {         \n"); //$NON-NLS-1$
		buffer.append("   T * d;                                 \n"); //$NON-NLS-1$
		buffer.append("}                                         \n"); //$NON-NLS-1$
		buffer.append("void foo() {                              \n"); //$NON-NLS-1$
		buffer.append("   f<int>( 0 );                           \n"); //$NON-NLS-1$
		buffer.append("}                                         \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
        ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
        
        IParameter p1 = (IParameter) col.getName(3).resolveBinding();
        
        ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(4).resolveBinding();
        ICPPFunction f2 = (ICPPFunction) col.getName(5).resolveBinding();
        IParameter p2 = (IParameter) col.getName(7).resolveBinding();
        
        assertSame( T, T2 );
        assertSame( f, f2 );
        assertSame( p1, p2 );
        assertSame( p1.getType(), T );
        
        ICPPFunction f3 = (ICPPFunction) col.getName(11).resolveBinding();
        assertTrue( f3 instanceof ICPPTemplateInstance );
        assertSame( ((ICPPTemplateInstance)f3).getTemplateDefinition(), f );
        
        assertInstances( col, T, 5 );
	}
	
	public void testStackOverflow() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template < class T > class pair {                \n"); //$NON-NLS-1$
		buffer.append("   template < class U > pair( const pair<U> & ); \n"); //$NON-NLS-1$
		buffer.append("};                                               \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        assertTrue( col.getName(0).resolveBinding() instanceof ICPPTemplateParameter );
        ICPPClassTemplate pair = (ICPPClassTemplate) col.getName(1).resolveBinding();
        ICPPTemplateParameter U = (ICPPTemplateParameter) col.getName(2).resolveBinding();
        assertTrue( col.getName(3).resolveBinding() instanceof ICPPFunctionTemplate );
        ICPPTemplateInstance pi = (ICPPTemplateInstance) col.getName(4).resolveBinding();
        ICPPClassTemplate p = (ICPPClassTemplate) col.getName(5).resolveBinding();
        ICPPTemplateParameter U2 = (ICPPTemplateParameter) col.getName(6).resolveBinding();

        assertSame( U, U2 );
        assertSame( pair, p );
        assertSame( pi.getTemplateDefinition(), pair );
	}
	
	public void testBasicClassPartialSpecialization() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "template < class T > class A {};       \n "); //$NON-NLS-1$
		buffer.append( "template < class T > class A< T* > {}; \n "); //$NON-NLS-1$
		buffer.append( "template < class T > class A< T** > {}; \n "); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateParameter T1 = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(2).resolveBinding();
		ICPPClassTemplatePartialSpecialization A2 = (ICPPClassTemplatePartialSpecialization) col.getName(3).resolveBinding();
		ICPPTemplateParameter T3 = (ICPPTemplateParameter) col.getName(5).resolveBinding();
		ICPPClassTemplatePartialSpecialization A3 = (ICPPClassTemplatePartialSpecialization) col.getName(7).resolveBinding();
		ICPPTemplateParameter T4 = (ICPPTemplateParameter) col.getName(6).resolveBinding();
		
		assertSame( A2.getPrimaryClassTemplate(), A1 );
		assertSame( A3.getPrimaryClassTemplate(), A1 );
		assertNotSame( T1, T2 );
		assertNotSame( A1, A2 );
		assertNotSame( A1, A3 );
		assertNotSame( A2, A3 );
		assertSame( T2, T3 );
		assertNotSame( T2, T4 );
	}
	
	public void testStackOverflow_2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "template < class T > class A { typedef int TYPE; };  \n"); //$NON-NLS-1$
		buffer.append( "template < class T > A<T>::TYPE foo( T );            \n"); //$NON-NLS-1$
		buffer.append( "template < class T > A<T>::TYPE foo( T );            \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateParameter T0 = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPTemplateParameter T1 = (ICPPTemplateParameter) col.getName(3).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(12).resolveBinding();
		
		assertNotSame( T0, T1 );
		assertSame( T1, T2 );
		
		ICPPFunctionTemplate foo1 = (ICPPFunctionTemplate) col.getName(9).resolveBinding();
		ICPPFunctionTemplate foo2 = (ICPPFunctionTemplate) col.getName(18).resolveBinding();
		assertSame( foo1, foo2 );
		
		ITypedef TYPE = (ITypedef) col.getName(2).resolveBinding();
		assertSame( TYPE, col.getName(8).resolveBinding() );
		assertSame( TYPE, col.getName(17).resolveBinding() );
		
		assertInstances( col, T1, 6 );
	}
	
	public void testTemplateMemberDef() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template < class T > class A {                       \n"); //$NON-NLS-1$
		buffer.append("   void f();                                         \n"); //$NON-NLS-1$
		buffer.append("};                                                   \n"); //$NON-NLS-1$
		buffer.append("template < class T > void A<T>::f() { }              \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPMethod f1 = (ICPPMethod) col.getName(2).resolveBinding();
		ICPPMethod f2 = (ICPPMethod) col.getName(8).resolveBinding();
		
		assertSame( f2, f1 );
	}
	
	public void testTemplateFunctionImplicitInstantiation() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template < class T > void f ( T );           \n"); //$NON-NLS-1$
		buffer.append("void main() {                                \n"); //$NON-NLS-1$
		buffer.append("   f( 1 );                                   \n"); //$NON-NLS-1$
		buffer.append("}                                            \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		IFunction f2 = (IFunction) col.getName(5).resolveBinding();
		
		assertTrue( f2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)f2).getTemplateDefinition(), f1 );
	}
	
	/**
	 * template< class T > void f( T );			//#1
	 * template< class T > void f( T* );		//#2
	 * template< class T > void f( const T* );	//#3
	 * 
	 * const int *p;
	 * f( p );	//calls f( const T * ) , 3 is more specialized than 1 or 2
	 *
	 * @throws Exception 
	 * 
	 */
	public void test_14_5_5_2s5_OrderingFunctionTemplates_1() throws Exception{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "template < class T > void f( T );        \n"); //$NON-NLS-1$
		buffer.append( "template < class T > void f( T* );       \n"); //$NON-NLS-1$
		buffer.append( "template < class T > void f( const T* ); \n"); //$NON-NLS-1$
		buffer.append( "void main() {                            \n"); //$NON-NLS-1$
		buffer.append( "   const int *p;                         \n"); //$NON-NLS-1$
		buffer.append( "   f( p );                               \n"); //$NON-NLS-1$
		buffer.append( "}                                        \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunctionTemplate f2 = (ICPPFunctionTemplate) col.getName(5).resolveBinding();
		ICPPFunctionTemplate f3 = (ICPPFunctionTemplate) col.getName(9).resolveBinding();
		
		assertNotSame( f1, f2 );
		assertNotSame( f2, f3 );
		assertNotSame( f3, f1 );
		
		IFunction f = (IFunction) col.getName(14).resolveBinding();
		assertTrue( f instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)f).getTemplateDefinition(), f3 );
	}
	
	/**
	 * template< class T > void g( T );			//#1
	 * template< class T > void g( T& );		//#2

	 * float x;
	 * g( x );  //ambiguous 1 or 2
	 * 
	 * @throws Exception
	 */
	public void test_14_5_5_2s5_OrderingFunctionTemplates_2() throws Exception{
	  	StringBuffer buffer = new StringBuffer();
		buffer.append( "template < class T > void f( T );        \n"); //$NON-NLS-1$
		buffer.append( "template < class T > void f( T& );       \n"); //$NON-NLS-1$
		buffer.append( "void main() {                            \n"); //$NON-NLS-1$
		buffer.append( "   float x;                              \n"); //$NON-NLS-1$
		buffer.append( "   f( x );                               \n"); //$NON-NLS-1$
		buffer.append( "}                                        \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunctionTemplate f2 = (ICPPFunctionTemplate) col.getName(5).resolveBinding();
		
		assertNotSame( f1, f2 );
		
		IProblemBinding f = (IProblemBinding) col.getName(10).resolveBinding();
		assertEquals( f.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP );
	}
	
	public void testTemplateParameters() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "template < class T, template < class X > class U, T *pT > class A {   \n"); //$NON-NLS-1$
		buffer.append( "};                                                                    \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateTypeParameter T = (ICPPTemplateTypeParameter) col.getName(0).resolveBinding();
		ICPPTemplateTemplateParameter U = (ICPPTemplateTemplateParameter) col.getName(2).resolveBinding();
		ICPPTemplateNonTypeParameter pT = (ICPPTemplateNonTypeParameter) col.getName(4).resolveBinding();
		
		ICPPTemplateTypeParameter X = (ICPPTemplateTypeParameter) col.getName(1).resolveBinding();
		
		ICPPTemplateParameter [] ps = U.getTemplateParameters();
		assertEquals( ps.length, 1 );
		assertSame( ps[0], X );
		
		IPointerType ptype = (IPointerType) pT.getType();
		assertSame( ptype.getType(), T );		
	}
	
	public void testDeferredInstances() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "template <class T> class A {        \n"); //$NON-NLS-1$
		buffer.append( "   A<T>* a;                         \n"); //$NON-NLS-1$
		buffer.append( "   A<T>* a2;                        \n"); //$NON-NLS-1$
		buffer.append( "};                                  \n"); //$NON-NLS-1$
		buffer.append( "void f(){                           \n"); //$NON-NLS-1$
		buffer.append( "   A<int> * b;                      \n"); //$NON-NLS-1$
		buffer.append( "   b->a;                            \n"); //$NON-NLS-1$
		buffer.append( "}                                   \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPTemplateInstance A_T = (ICPPTemplateInstance) col.getName(2).resolveBinding();
		assertSame( A_T.getTemplateDefinition(), A );
		
		ICPPTemplateInstance A_T2 = (ICPPTemplateInstance) col.getName(6).resolveBinding();
		assertSame( A_T, A_T2 );
		
		ICPPVariable a = (ICPPVariable) col.getName(5).resolveBinding();
		IPointerType pt = (IPointerType) a.getType();
		assertSame( pt.getType(), A_T );
		
		ICPPVariable b = (ICPPVariable) col.getName(13).resolveBinding();
		IType bt = b.getType();
		assertTrue( bt instanceof IPointerType );
		
		ICPPVariable a2 = (ICPPVariable) col.getName(15).resolveBinding();
		assertTrue( a2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)a2).getSpecializedBinding(), a );
		IType at = a2.getType();
		assertTrue( at instanceof IPointerType );
		
		assertSame( ((IPointerType)at).getType(), ((IPointerType)bt).getType() );
	}
	
	public void test_14_5_4_1s2_MatchingTemplateSpecializations() throws Exception{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "template < class T1, class T2, int I > class A                {}; //#1   \n"); //$NON-NLS-1$
		buffer.append( "template < class T, int I >            class A < T, T*, I >   {}; //#2   \n"); //$NON-NLS-1$
		buffer.append( "template < class T1, class T2, int I > class A < T1*, T2, I > {}; //#3   \n"); //$NON-NLS-1$
		buffer.append( "template < class T >                   class A < int, T*, 5 > {}; //#4   \n"); //$NON-NLS-1$
		buffer.append( "template < class T1, class T2, int I > class A < T1, T2*, I > {}; //#5   \n"); //$NON-NLS-1$
				 
		buffer.append( "A <int, int, 1>   a1;		//uses #1                                    \n"); //$NON-NLS-1$ 
		buffer.append( "A <int, int*, 1>  a2;		//uses #2, T is int, I is 1                  \n"); //$NON-NLS-1$
		buffer.append( "A <int, char*, 5> a3;		//uses #4, T is char                         \n"); //$NON-NLS-1$
		buffer.append( "A <int, char*, 1> a4;		//uses #5, T is int, T2 is char, I is1       \n"); //$NON-NLS-1$
		buffer.append( "A <int*, int*, 2> a5;		//ambiguous, matches #3 & #5.                \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(3).resolveBinding();
		ICPPClassTemplate A2 = (ICPPClassTemplate) col.getName(6).resolveBinding();
		ICPPClassTemplate A3 = (ICPPClassTemplate) col.getName(14).resolveBinding();
		ICPPClassTemplate A4 = (ICPPClassTemplate) col.getName(20).resolveBinding();
		ICPPClassTemplate A5 = (ICPPClassTemplate) col.getName(26).resolveBinding();
		
		assertTrue( A3 instanceof ICPPClassTemplatePartialSpecialization );
		assertSame( ((ICPPClassTemplatePartialSpecialization)A3).getPrimaryClassTemplate(), A1 );
		
		ICPPTemplateTypeParameter T1 = (ICPPTemplateTypeParameter) col.getName(11).resolveBinding();
		ICPPTemplateTypeParameter T2 = (ICPPTemplateTypeParameter) col.getName(12).resolveBinding();
		ICPPTemplateNonTypeParameter I = (ICPPTemplateNonTypeParameter) col.getName(13).resolveBinding();
		
		ICPPTemplateParameter TR1 = (ICPPTemplateParameter) col.getName(16).resolveBinding();
		ICPPTemplateParameter TR2 = (ICPPTemplateParameter) col.getName(17).resolveBinding();
		ICPPTemplateParameter TR3 = (ICPPTemplateParameter) col.getName(18).resolveBinding();
		
		assertSame( T1, TR1 );
		assertSame( T2, TR2 );
		assertSame( I, TR3 );
		
		ICPPTemplateInstance R1 = (ICPPTemplateInstance) col.getName(31).resolveBinding();
		ICPPTemplateInstance R2 = (ICPPTemplateInstance) col.getName(34).resolveBinding();
		ICPPTemplateInstance R3 = (ICPPTemplateInstance) col.getName(37).resolveBinding();
		ICPPTemplateInstance R4 = (ICPPTemplateInstance) col.getName(40).resolveBinding();
		IProblemBinding R5 = (IProblemBinding) col.getName(43).resolveBinding();
		assertEquals( R5.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP );
		
		assertSame( R1.getTemplateDefinition(), A1 );
		assertSame( R2.getTemplateDefinition(), A2 );
		assertSame( R4.getTemplateDefinition(), A5 );
		assertSame( R3.getTemplateDefinition(), A4 );
	}
	
	public void test14_7_3_FunctionExplicitSpecialization() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> void f(T);                  \n"); //$NON-NLS-1$
		buffer.append("template <class T> void f(T*);                 \n"); //$NON-NLS-1$
		buffer.append("template <> void f(int);       //ok            \n"); //$NON-NLS-1$
		buffer.append("template <> void f<int>(int*); //ok            \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate fT1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunctionTemplate fT2 = (ICPPFunctionTemplate) col.getName(5).resolveBinding();
		
		ICPPSpecialization f1 = (ICPPSpecialization) col.getName(8).resolveBinding();
		ICPPSpecialization f2 = (ICPPSpecialization) col.getName(10).resolveBinding();
		
		assertSame( f1.getSpecializedBinding(), fT1 );
		assertSame( f2.getSpecializedBinding(), fT2 );
	}
	
	public void test_14_5_5_1_FunctionTemplates_1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T*);        \n"); //$NON-NLS-1$
		buffer.append("void g(int* p) { f(p); }             \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		
		ICPPFunction ref = (ICPPFunction) col.getName(6).resolveBinding();
		assertTrue( ref instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)ref).getTemplateDefinition(), f );
	}
	
	public void test_14_5_5_1_FunctionTemplates_2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T);        \n"); //$NON-NLS-1$
		buffer.append("void g(int* p) { f(p); }            \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		
		ICPPFunction ref = (ICPPFunction) col.getName(6).resolveBinding();
		assertTrue( ref instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)ref).getTemplateDefinition(), f );
	}
	
	public void test_14_8_1s2_FunctionTemplates() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class X, class Y> X f(Y);                      \n"); //$NON-NLS-1$
		buffer.append("void g(){                                               \n"); //$NON-NLS-1$
		buffer.append("   int i = f<int>(5); // Y is int                       \n"); //$NON-NLS-1$
		buffer.append("}                                                       \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(3).resolveBinding();
		ICPPFunction ref1 = (ICPPFunction) col.getName(8).resolveBinding();
		
		assertTrue( ref1 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance) ref1).getTemplateDefinition(), f );
	}
	
	public void test14_8_3s6_FunctionTemplates() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T);  \n"); //$NON-NLS-1$
		buffer.append("void g(){                     \n"); //$NON-NLS-1$
		buffer.append("   f(\"Annemarie\");          \n"); //$NON-NLS-1$
		buffer.append("}                             \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunction ref = (ICPPFunction) col.getName(5).resolveBinding();
		assertTrue( ref instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)ref).getTemplateDefinition(), f );
	}
	
	public void test14_5_5_2s6_FunctionTemplates() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T);         // #1\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T*, int=1); // #2\n"); //$NON-NLS-1$
		buffer.append("template<class T> void g(T);         // #3\n"); //$NON-NLS-1$
		buffer.append("template<class T> void g(T*, ...);   // #4\n"); //$NON-NLS-1$
		buffer.append("int main() {                              \n"); //$NON-NLS-1$
		buffer.append("   int* ip;                               \n"); //$NON-NLS-1$
		buffer.append("   f(ip);                       //calls #2\n"); //$NON-NLS-1$
		buffer.append("   g(ip);                       //calls #4\n"); //$NON-NLS-1$
		buffer.append("}                                         \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunctionTemplate f2 = (ICPPFunctionTemplate) col.getName(5).resolveBinding();
		assertNotSame( f1, f2 );
		
		ICPPFunctionTemplate g1 = (ICPPFunctionTemplate) col.getName(10).resolveBinding();
		ICPPFunctionTemplate g2 = (ICPPFunctionTemplate) col.getName(14).resolveBinding();
		assertNotSame( g1, g2 );
		
		ICPPFunction ref1 = (ICPPFunction) col.getName(19).resolveBinding();
		ICPPFunction ref2 = (ICPPFunction) col.getName(21).resolveBinding();
		
		assertTrue( ref1 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance) ref1).getTemplateDefinition(), f2 );
		
		assertTrue( ref2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance) ref2).getTemplateDefinition(), g2 );
	}
	
	public void test14_6_1s1_LocalNames() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X {           \n"); //$NON-NLS-1$
		buffer.append("   X* p;               // meaning X<T>\n"); //$NON-NLS-1$
		buffer.append("   X<T>* p2;                          \n"); //$NON-NLS-1$
		buffer.append("};                                    \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate X = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPClassType x1 = (ICPPClassType) col.getName(2).resolveBinding();
		ICPPClassType x2 = (ICPPClassType) col.getName(4).resolveBinding();
		
		assertTrue( x1 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)x1).getTemplateDefinition(), X );
		
		assertSame( x1, x2 );
	}
	
	public void test14_8s2_() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> T f(T* p){                  \n"); //$NON-NLS-1$
		buffer.append("};                                            \n"); //$NON-NLS-1$
		buffer.append("void g(int a, char* b){                       \n"); //$NON-NLS-1$
		buffer.append("   f(&a);              //call f<int>(int*)    \n"); //$NON-NLS-1$
		buffer.append("   f(&b);              //call f<char*>(char**)\n"); //$NON-NLS-1$
		buffer.append("}                                             \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(2).resolveBinding();
		
		ICPPFunction f1 = (ICPPFunction) col.getName(8).resolveBinding();
		ICPPFunction f2 = (ICPPFunction) col.getName(10).resolveBinding();
		
		assertNotSame( f1, f2 );
		assertTrue( f1 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)f1).getTemplateDefinition(), f );
		assertTrue( f2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)f2).getTemplateDefinition(), f );
		
		IType fr1 = f1.getType().getReturnType();
		IType fr2 = f2.getType().getReturnType();
		
		assertTrue( fr1 instanceof IBasicType );
		assertEquals( ((IBasicType)fr1).getType(), IBasicType.t_int );
		
		assertTrue( fr2 instanceof IPointerType );
		assertTrue( ((IPointerType)fr2).getType() instanceof IBasicType );
		assertEquals( ((IBasicType) ((IPointerType)fr2).getType()).getType(), IBasicType.t_char );
	}
	
	public void test14_7_3s14() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T) {  }                  \n"); //$NON-NLS-1$
		buffer.append("template<class T> inline T g(T) {  }              \n"); //$NON-NLS-1$
		buffer.append("template<> inline void f<>(int) {  } //OK: inline \n"); //$NON-NLS-1$
		buffer.append("template<> int g<>(int) {  }     // OK: not inline\n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunctionTemplate g1 = (ICPPFunctionTemplate) col.getName(6).resolveBinding();
		
		ICPPSpecialization f2 = (ICPPSpecialization) col.getName(9).resolveBinding();
		ICPPSpecialization g2 = (ICPPSpecialization) col.getName(12).resolveBinding();
		
		assertSame( f2.getSpecializedBinding(), f1 );
		assertSame( g2.getSpecializedBinding(), g1 );
		
		assertFalse( ((ICPPFunction)f1).isInline() );
		assertTrue( ((ICPPFunction)g1).isInline() );
		assertTrue( ((ICPPFunction)f2).isInline() );
		assertFalse( ((ICPPFunction)g2).isInline() );
	}
	
	public void test14_7_1s14_InfiniteInstantiation() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class X {                                       \n"); //$NON-NLS-1$
		buffer.append("   X<T*> a; // implicit generation of X<T> requires               \n"); //$NON-NLS-1$
		buffer.append("            // the implicit instantiation of X<T*> which requires \n"); //$NON-NLS-1$
		buffer.append("            // the implicit instantiation of X<T**> which ...     \n"); //$NON-NLS-1$
		buffer.append("};                                                                \n"); //$NON-NLS-1$
		buffer.append("void f() {                                                        \n"); //$NON-NLS-1$
		buffer.append("   X<int> x;                                                      \n"); //$NON-NLS-1$
		buffer.append("   x.a.a.a.a;                                                     \n"); //$NON-NLS-1$
		buffer.append("}                                                                 \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate X = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPVariable x = (ICPPVariable) col.getName(9).resolveBinding();
		IType t = x.getType();
		assertTrue( t instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance) t).getTemplateDefinition(), X );
		
		ICPPField a = (ICPPField) col.getName(5).resolveBinding();
		ICPPField a1 = (ICPPField) col.getName(11).resolveBinding();
		ICPPField a2 = (ICPPField) col.getName(12).resolveBinding();
		ICPPField a3 = (ICPPField) col.getName(13).resolveBinding();
		ICPPField a4 = (ICPPField) col.getName(14).resolveBinding();
		
		assertTrue( a1 instanceof ICPPSpecialization );
		assertTrue( a2 instanceof ICPPSpecialization );
		assertTrue( a3 instanceof ICPPSpecialization );
		assertTrue( a4 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)a1).getSpecializedBinding(), a );
		assertSame( ((ICPPSpecialization)a2).getSpecializedBinding(), a );
		assertSame( ((ICPPSpecialization)a3).getSpecializedBinding(), a );
		assertSame( ((ICPPSpecialization)a4).getSpecializedBinding(), a );
	}
	
	public void test14_6_1s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Y;         \n"); //$NON-NLS-1$
		buffer.append("template<> class Y<int> {          \n"); //$NON-NLS-1$
		buffer.append("   Y* p; // meaning Y<int>         \n"); //$NON-NLS-1$
		buffer.append("   Y<char>* q; // meaning Y<char>  \n"); //$NON-NLS-1$
		buffer.append("};                                 \n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate Y = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPSpecialization Yspec = (ICPPSpecialization) col.getName(2).resolveBinding();
		
		assertTrue( Yspec instanceof ICPPClassType );
		assertSame( Yspec.getSpecializedBinding(), Y );
		
		ICPPClassType y1 = (ICPPClassType) col.getName(4).resolveBinding();
		assertSame( y1, Yspec );
		
		ICPPClassType y2 = (ICPPClassType) col.getName(6).resolveBinding();
		assertTrue( y2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)y2).getTemplateDefinition(), Y );
	}
	
	public void testBug45129() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("template < class T, class U > void f ( T (*) ( T, U ) );   \n"); //$NON-NLS-1$
	    buffer.append("int g ( int, char );                                       \n"); //$NON-NLS-1$
	    buffer.append("void foo () {                                              \n"); //$NON-NLS-1$
	    buffer.append("   f( g );                                                 \n"); //$NON-NLS-1$
	    buffer.append("}                                                          \n"); //$NON-NLS-1$
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
	    ICPPFunction f1 = (ICPPFunction) col.getName(2).resolveBinding();
    	ICPPFunction g1 = (ICPPFunction) col.getName(9).resolveBinding();
    	
    	IBinding f2 = col.getName(13).resolveBinding();
    	IBinding g2 = col.getName(14).resolveBinding();
    	
    	assertTrue( f2 instanceof ICPPTemplateInstance );
    	assertSame( ((ICPPTemplateInstance)f2).getTemplateDefinition(), f1 );
    	assertSame( g1, g2 );
	}
	
	public void testBug76951_1() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("template <class T, class U = T > U f( T );   \n"); //$NON-NLS-1$
	    buffer.append("void g() {                                   \n"); //$NON-NLS-1$
	    buffer.append("   f( 1 );                                   \n"); //$NON-NLS-1$
	    buffer.append("}                                            \n"); //$NON-NLS-1$
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(2).resolveBinding();
		assertSame( T, T2 );
		
		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(4).resolveBinding();
		ICPPFunction f2 = (ICPPFunction) col.getName(8).resolveBinding();
		
		assertTrue( f2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)f2).getTemplateDefinition(), f1 );
		
		IFunctionType ft = f2.getType();
		assertTrue( ft.getReturnType() instanceof IBasicType );
		assertEquals( ((IBasicType)ft.getReturnType()).getType(), IBasicType.t_int );
	}
	
	public void testBug76951_2() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("template <class T, class U = T > class A {   \n"); //$NON-NLS-1$
	    buffer.append("   U u;                                      \n"); //$NON-NLS-1$
	    buffer.append("};                                           \n"); //$NON-NLS-1$
	    buffer.append("void f() {                                   \n"); //$NON-NLS-1$
	    buffer.append("   A<int> a;                                 \n"); //$NON-NLS-1$
	    buffer.append("   a.u;                                      \n"); //$NON-NLS-1$
	    buffer.append("}                                            \n"); //$NON-NLS-1$
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPTemplateTypeParameter U = (ICPPTemplateTypeParameter) col.getName(1).resolveBinding();
		assertSame( U.getDefault(), T );
		
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(3).resolveBinding();
		ICPPField u1 = (ICPPField) col.getName(5).resolveBinding();
		assertSame( u1.getType(), U );
		
		ICPPClassType A1 = (ICPPClassType) col.getName(7).resolveBinding();
		assertTrue( A1 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)A1).getTemplateDefinition(), A );
		
		ICPPField u2 = (ICPPField) col.getName(11).resolveBinding();
		assertTrue( u2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)u2).getSpecializedBinding(), u1 );
		
		IType type = u2.getType();
		assertTrue( type instanceof IBasicType );
		assertEquals( ((IBasicType)type).getType(), IBasicType.t_int );
	}
	
	public void testInstances() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("template < class T > class A {               \n"); //$NON-NLS-1$
	    buffer.append("   A< int > a;                               \n"); //$NON-NLS-1$
	    buffer.append("};                                           \n"); //$NON-NLS-1$
	    buffer.append("void f( A<int> p ) { }                       \n"); //$NON-NLS-1$
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPClassType A1 = (ICPPClassType) col.getName(2).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) col.getName(6).resolveBinding();
		
		assertSame( A1, A2 );
		assertTrue( A1 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)A1).getTemplateDefinition(), A );
	}
	
	public void testTemplateParameterDeclarations() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "template <class T> void f( T );      \n"); //$NON-NLS-1$
		buffer.append( "template <class T> void f( T ) {}    \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateParameter T1 = (ICPPTemplateParameter) col.getName(4).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(2).resolveBinding();
			
		assertSame( T1, T2 );
		
		assertInstances( col, T1, 4 );
	}
	
	public void testDeferredInstantiation() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template < class T > class A {                                \n"); //$NON-NLS-1$
		buffer.append("   int f( A * );                                              \n"); //$NON-NLS-1$
		buffer.append("   A < T > *pA;                                               \n"); //$NON-NLS-1$
		buffer.append("};                                                            \n"); //$NON-NLS-1$
		buffer.append("void f () {                                                   \n"); //$NON-NLS-1$
		buffer.append("   A< int > *a;                                               \n"); //$NON-NLS-1$
		buffer.append("   a->f( a );                                                 \n"); //$NON-NLS-1$
		buffer.append("   a->pA;                                                     \n"); //$NON-NLS-1$
		buffer.append("};                                                            \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(2).resolveBinding();
		ICPPClassType A1 = (ICPPClassType) col.getName(3).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) col.getName(5).resolveBinding();
		ICPPField pA = (ICPPField) col.getName(8).resolveBinding();
		
		assertSame( A1, A2 );
		assertTrue( A1 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)A1).getTemplateDefinition(), A );
		
		ICPPClassType AI = (ICPPClassType) col.getName(10).resolveBinding();
		ICPPMethod f2 = (ICPPMethod) col.getName(14).resolveBinding();
		ICPPField pA2 = (ICPPField) col.getName(17).resolveBinding();
		
		assertTrue( f2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)f2).getSpecializedBinding(), f );
		assertTrue( pA2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)pA2).getSpecializedBinding(), pA );
		
		IType paT = pA2.getType();
		assertTrue( paT instanceof IPointerType );
		assertSame( ((IPointerType)paT).getType(), AI );
		
		IParameter p = f2.getParameters()[0];
		IType pT = p.getType();
		assertTrue( pT instanceof IPointerType );
		assertSame( ((IPointerType)pT).getType(), AI );
	}
	
	public void test14_5_2s2_MemberSpecializations() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> struct A {                                 \n"); //$NON-NLS-1$
		buffer.append("   void f(int);                                               \n"); //$NON-NLS-1$
		buffer.append("   template <class T2> void f(T2);                            \n"); //$NON-NLS-1$
		buffer.append("};                                                            \n"); //$NON-NLS-1$
		buffer.append("template <> void A<int>::f(int) { } //nontemplate             \n"); //$NON-NLS-1$
		buffer.append("template <> template <> void A<int>::f<>(int) { } //template  \n"); //$NON-NLS-1$
		buffer.append("int main() {                                                  \n"); //$NON-NLS-1$
		buffer.append("   A<int> ac;                                                 \n"); //$NON-NLS-1$
		buffer.append("   ac.f(1);   //nontemplate                                   \n"); //$NON-NLS-1$
		buffer.append("   ac.f('c'); //template                                      \n"); //$NON-NLS-1$
		buffer.append("   ac.f<>(1); //template                                      \n"); //$NON-NLS-1$
		buffer.append("}                                                             \n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPMethod f1 = (ICPPMethod) col.getName(2).resolveBinding();
		ICPPMethod f2 = (ICPPMethod) col.getName(5).resolveBinding();
		
		ICPPMethod f1_2 = (ICPPMethod) col.getName(11).resolveBinding();
		assertNotSame( f1, f1_2 );
		assertTrue( f1_2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)f1_2).getSpecializedBinding(), f1 );
		
		ICPPClassType A2 = (ICPPClassType) col.getName(9).resolveBinding();
		assertTrue( A2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)A2).getTemplateDefinition(), A );
		
		ICPPMethod f2_2 = (ICPPMethod) col.getName(16).resolveBinding();
		assertTrue( f2_2 instanceof ICPPSpecialization );
		IBinding speced = ((ICPPSpecialization)f2_2).getSpecializedBinding();
		assertTrue( speced instanceof ICPPFunctionTemplate && speced instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)speced).getSpecializedBinding(), f2 );
		
		ICPPClassType A3 = (ICPPClassType) col.getName(14).resolveBinding();
		assertSame( A2, A3 );
		
		ICPPClassType A4 = (ICPPClassType) col.getName(20).resolveBinding();
		assertSame( A2, A4 );
		
		IFunction r1 = (IFunction) col.getName(24).resolveBinding();
		IFunction r2 = (IFunction) col.getName(26).resolveBinding();
		IFunction r3 = (IFunction) col.getName(28).resolveBinding();
		
		assertSame( r1, f1_2 );
		assertTrue( r2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)r2).getTemplateDefinition(), speced );
		assertSame( r3, f2_2 );
	}
	
	public void testClassSpecializations() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> class A { };       \n"); //$NON-NLS-1$
		buffer.append("template <> class A<int> {};          \n"); //$NON-NLS-1$
		buffer.append("A<char> ac;                           \n"); //$NON-NLS-1$
		buffer.append("A<int> ai;                            \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) col.getName(2).resolveBinding();
		
		assertTrue( A2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)A2).getSpecializedBinding(), A1 );
		
		ICPPClassType r1 = (ICPPClassType) col.getName(4).resolveBinding();
		ICPPClassType r2 = (ICPPClassType) col.getName(7).resolveBinding();
		
		assertTrue( r1 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)r1).getTemplateDefinition(), A1 );
		assertSame( r2, A2 );
	}
	
	public void test14_7_3s5_SpecializationMemberDefinition() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {                                           \n"); //$NON-NLS-1$
		buffer.append("   void f(T) {  }                                                      \n"); //$NON-NLS-1$
		buffer.append("};                                                                     \n"); //$NON-NLS-1$
		buffer.append("template<> struct A<int> {                                             \n"); //$NON-NLS-1$
		buffer.append("   void f(int);                                                        \n"); //$NON-NLS-1$
		buffer.append("};                                                                     \n"); //$NON-NLS-1$
		buffer.append("void h(){                                                              \n"); //$NON-NLS-1$
		buffer.append("   A<int> a;                                                           \n"); //$NON-NLS-1$
		buffer.append("   a.f(16);   // A<int>::f must be defined somewhere                   \n"); //$NON-NLS-1$
		buffer.append("}                                                                      \n"); //$NON-NLS-1$
		buffer.append("// explicit specialization syntax not used for a member of             \n"); //$NON-NLS-1$
		buffer.append("// explicitly specialized class template specialization                \n"); //$NON-NLS-1$
		buffer.append("void A<int>::f(int) {  }                                               \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPMethod f1 = (ICPPMethod) col.getName(2).resolveBinding();
		
		ICPPClassType A2 = (ICPPClassType) col.getName(5).resolveBinding();
		assertTrue( A2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)A2).getSpecializedBinding(), A1 );
		
		ICPPMethod f2 = (ICPPMethod) col.getName(7).resolveBinding();
		assertNotSame( f1, f2 );
		
		ICPPClassType A3 = (ICPPClassType) col.getName(10).resolveBinding();
		assertSame( A3, A2 );
		ICPPMethod f3 = (ICPPMethod) col.getName(14).resolveBinding();
		assertSame( f3, f2 );
		
		ICPPClassType A4 = (ICPPClassType) col.getName(16).resolveBinding();
		assertSame( A4, A2 );
		ICPPMethod f4 = (ICPPMethod) col.getName(18).resolveBinding();
		assertSame( f4, f3 );
	}
	
	public void testNestedSpecializations() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("class C{};                                                     \n"); //$NON-NLS-1$
		buffer.append("template <class T> class A {                                   \n"); //$NON-NLS-1$
		buffer.append("   template <class T2> class B {                               \n"); //$NON-NLS-1$
		buffer.append("      T f( T2 );                                               \n"); //$NON-NLS-1$
		buffer.append("   };                                                          \n"); //$NON-NLS-1$
		buffer.append("};                                                             \n"); //$NON-NLS-1$
		buffer.append("void g(){                                                      \n"); //$NON-NLS-1$
		buffer.append("   A<int>::B<C> b;                                             \n"); //$NON-NLS-1$
		buffer.append("   C c;                                                        \n"); //$NON-NLS-1$
		buffer.append("   b.f( c );                                                   \n"); //$NON-NLS-1$
		buffer.append("}                                                              \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassType C = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(2).resolveBinding();
		ICPPClassTemplate B = (ICPPClassTemplate) col.getName(4).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(6).resolveBinding();
		
		ICPPClassType A1 = (ICPPClassType) col.getName(11).resolveBinding();
		assertTrue( A1 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)A1).getTemplateDefinition(), A );
		
		ICPPClassType B1 = (ICPPClassType) col.getName(13).resolveBinding();
		assertTrue( B1 instanceof ICPPTemplateInstance );
		ICPPClassType B2 = (ICPPClassType) ((ICPPTemplateInstance)B1).getTemplateDefinition();
		assertTrue( B2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)B2).getSpecializedBinding(), B );
		
		//we might want this to be a specialization of a specialization, but for now, this is easier
		ICPPMethod f1 = (ICPPMethod) col.getName(20).resolveBinding();
		assertTrue( f1 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)f1).getSpecializedBinding(), f );
		
		IFunctionType ft = f1.getType();
		assertTrue( ft.getReturnType() instanceof IBasicType );
		assertEquals( ((IBasicType)ft.getReturnType()).getType(), IBasicType.t_int );
		
		assertSame( ft.getParameterTypes()[0], C ); 
	}
	
	public void test14_5_4s7_UsingClassTemplate() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace N {                                                \n"); //$NON-NLS-1$
		buffer.append("   template<class T1, class T2> class A { };                 \n"); //$NON-NLS-1$
		buffer.append("}                                                            \n"); //$NON-NLS-1$
		buffer.append("using N::A;                                                  \n"); //$NON-NLS-1$
		buffer.append("namespace N {                                                \n"); //$NON-NLS-1$
		buffer.append("   template<class T> class A<T, T*> { };                     \n"); //$NON-NLS-1$
		buffer.append("}                                                            \n"); //$NON-NLS-1$
		buffer.append("A<int,int*> a;                                               \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(3).resolveBinding();
		ICPPClassTemplatePartialSpecialization A2 = (ICPPClassTemplatePartialSpecialization) col.getName(9).resolveBinding();
		
		ICPPClassType A3 = (ICPPClassType) col.getName(13).resolveBinding();
		assertTrue( A3 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)A3).getTemplateDefinition(), A2 );
		
		ICPPClassTemplate A4 = (ICPPClassTemplate) col.getName(14).resolveBinding();
		assertTrue( A4 instanceof ICPPDelegate );
		assertSame( ((ICPPDelegate)A4).getBinding(), A1 );
	}

	public void testTemplateTemplateParameter() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class A {                      \n"); //$NON-NLS-1$
		buffer.append("   int x;                                        \n"); //$NON-NLS-1$
		buffer.append("};                                               \n"); //$NON-NLS-1$
		buffer.append("template<class T> class A<T*> {                  \n"); //$NON-NLS-1$
		buffer.append("   char x;                                       \n"); //$NON-NLS-1$
		buffer.append("};                                               \n"); //$NON-NLS-1$
		buffer.append("template<template<class U> class V> class C {    \n"); //$NON-NLS-1$
		buffer.append("   V<int> y;                                     \n"); //$NON-NLS-1$
		buffer.append("   V<int*> z;                                    \n"); //$NON-NLS-1$
		buffer.append("};                                               \n"); //$NON-NLS-1$
		buffer.append("void f() {                                       \n"); //$NON-NLS-1$
		buffer.append("   C<A> c;                                       \n"); //$NON-NLS-1$
		buffer.append("   c.y.x;   c.z.x;                               \n"); //$NON-NLS-1$
		buffer.append("}                                                \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPField x1 = (ICPPField) col.getName(2).resolveBinding();
		ICPPClassTemplatePartialSpecialization A2 = (ICPPClassTemplatePartialSpecialization) col.getName(4).resolveBinding();
		ICPPField x2 = (ICPPField) col.getName(7).resolveBinding();
		
		ICPPClassTemplate C = (ICPPClassTemplate) col.getName(10).resolveBinding();
		ICPPField y = (ICPPField) col.getName(13).resolveBinding();
		ICPPField z = (ICPPField) col.getName(16).resolveBinding();
		
		ICPPClassType C1 = (ICPPClassType) col.getName(18).resolveBinding();
		assertTrue( C1 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)C1).getTemplateDefinition(), C );
		
		ICPPField y2 = (ICPPField) col.getName(23).resolveBinding();
		assertTrue( y2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)y2).getSpecializedBinding(), y );
		IType t = y2.getType();
		assertTrue( t instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)t).getTemplateDefinition(), A1 );
		ICPPField x3 = (ICPPField) col.getName(24).resolveBinding();
		assertTrue( x3 instanceof ICPPSpecialization );
		assertEquals( ((ICPPSpecialization)x3).getSpecializedBinding(), x1 );         
		
		ICPPField z2 = (ICPPField) col.getName(26).resolveBinding();
		assertTrue( z2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)z2).getSpecializedBinding(), z );
		t = z2.getType();
		assertTrue( t instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)t).getTemplateDefinition(), A2 );
		ICPPField x4 = (ICPPField) col.getName(27).resolveBinding();
		assertTrue( x4 instanceof ICPPSpecialization );
		assertEquals( ((ICPPSpecialization)x4).getSpecializedBinding(), x2 );
	}
	public void _testNestedTypeSpecializations() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> class A {               \n"); //$NON-NLS-1$
		buffer.append("   typedef T _T;                           \n"); //$NON-NLS-1$
		buffer.append("  _T t;                                    \n"); //$NON-NLS-1$
		buffer.append("};                                         \n"); //$NON-NLS-1$
		buffer.append("void f() {                                 \n"); //$NON-NLS-1$
		buffer.append("   A<int> a;                               \n"); //$NON-NLS-1$
		buffer.append("   a.t;                                    \n"); //$NON-NLS-1$
		buffer.append("}                                          \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ITypedef _T = (ITypedef) col.getName(3).resolveBinding();
		assertSame( _T.getType(), T );
		
		ICPPField t = (ICPPField) col.getName(5).resolveBinding();
		assertSame( t.getType(), _T );
		
		ICPPField t2 = (ICPPField) col.getName(11).resolveBinding();
		assertTrue( t2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)t2).getSpecializedBinding(), t );
		
		IType type = t2.getType();
		assertTrue( type instanceof ITypedef );
		assertTrue( type instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)type).getSpecializedBinding(), _T );
		
		type = ((ITypedef)type).getType();
		assertTrue( type instanceof IBasicType );
		assertEquals( ((IBasicType)type).getType(), IBasicType.t_int );
	}
	
	public void _testNestedClassTypeSpecializations() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> class A {                          \n"); //$NON-NLS-1$
		buffer.append("   class B { T t; };                                  \n"); //$NON-NLS-1$
		buffer.append("   B b;                                               \n"); //$NON-NLS-1$
		buffer.append("};                                                    \n"); //$NON-NLS-1$
		buffer.append("void f() {                                            \n"); //$NON-NLS-1$
		buffer.append("   A<int> a;                                          \n"); //$NON-NLS-1$
		buffer.append("   a.b.t;                                             \n"); //$NON-NLS-1$
		buffer.append("}                                                     \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
		ICPPField t = (ICPPField) col.getName(4).resolveBinding();
		assertSame( t.getType(), T );
		ICPPField b = (ICPPField) col.getName(6).resolveBinding();
		assertSame( b.getType(), B );
		
		ICPPField b2 = (ICPPField) col.getName(12).resolveBinding();
		ICPPField t2 = (ICPPField) col.getName(13).resolveBinding();
		
		assertTrue( b2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)b2).getSpecializedBinding(), b );
		
		IType type = b2.getType();
		assertTrue( type instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)type).getSpecializedBinding(), B );
		
		assertTrue( t2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)t2).getSpecializedBinding(), t );
		assertTrue( t2.getType() instanceof IBasicType );
		assertEquals( ((IBasicType)t2.getType()).getType(), IBasicType.t_int );
	}
	
	public void testTemplateParameterQualifiedType_1() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("template <class T> class A {                     \n"); //$NON-NLS-1$
	    buffer.append("   typedef typename T::X _xx;                    \n"); //$NON-NLS-1$
	    buffer.append("   _xx s;                                        \n"); //$NON-NLS-1$
	    buffer.append("};                                               \n"); //$NON-NLS-1$
	    buffer.append("class B {};                                      \n"); //$NON-NLS-1$
	    buffer.append("template < class T > class C {                   \n"); //$NON-NLS-1$
	    buffer.append("   typedef T X;                                  \n"); //$NON-NLS-1$
	    buffer.append("};                                               \n"); //$NON-NLS-1$
	    buffer.append("void f() {                                       \n"); //$NON-NLS-1$
	    buffer.append("   A< C<B> > a; a.s;                             \n"); //$NON-NLS-1$
	    buffer.append("};                                               \n"); //$NON-NLS-1$
	    
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateTypeParameter T = (ICPPTemplateTypeParameter) col.getName(0).resolveBinding();
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		
		IBinding T1 = col.getName(3).resolveBinding();
		assertSame( T1, T );
		
		ICPPClassType X = (ICPPClassType) col.getName(4).resolveBinding();
		
		ITypedef _xx = (ITypedef) col.getName(5).resolveBinding();
		
		IBinding _xx2 = col.getName(6).resolveBinding();
		assertSame( _xx, _xx2 );
		assertSame( _xx.getType(), X );
		
		ICPPField s = (ICPPField) col.getName(7).resolveBinding();
		
		ICPPClassType B = (ICPPClassType) col.getName(8).resolveBinding();
		ITypedef X2 = (ITypedef) col.getName(12).resolveBinding();
		
		ICPPClassType Acb = (ICPPClassType) col.getName(14).resolveBinding();
		assertTrue( Acb instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)Acb).getTemplateDefinition(), A );
		
		ICPPField  s2 = (ICPPField) col.getName(21).resolveBinding();
		assertTrue( s2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)s2).getSpecializedBinding(), s );
		
		IType t = s2.getType();
//		assertTrue( t instanceof ITypedef );
//		assertTrue( t instanceof ICPPSpecialization );
//		assertSame( ((ICPPSpecialization)t).getSpecializedBinding(), _xx );
		
		t = ((ITypedef)t).getType();
		assertTrue( t instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)t).getSpecializedBinding(), X2 );
		
		t = ((ITypedef)t).getType();
		assertSame( t, B );
	}
	
	public void testTemplateScopes() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("template <class T> class A {               \n"); //$NON-NLS-1$
	    buffer.append("   A<T> a;                                 \n"); //$NON-NLS-1$
	    buffer.append("   void f();                               \n"); //$NON-NLS-1$
	    buffer.append("};                                         \n"); //$NON-NLS-1$
	    buffer.append("template <class U> void A<U>::f(){         \n"); //$NON-NLS-1$
	    buffer.append("   U u;                                    \n"); //$NON-NLS-1$
	    buffer.append("}                                          \n"); //$NON-NLS-1$

	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) col.getName(2).resolveBinding();
		
		ICPPTemplateParameter U = (ICPPTemplateParameter) col.getName(7).resolveBinding();
		assertSame( U, T );
		ICPPClassType A3 = (ICPPClassType) col.getName(9).resolveBinding();
		assertTrue( A3 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance) A3).getTemplateDefinition(), A );
		assertSame( A2, A3 );
		
		
		ICPPTemplateParameter U2 = (ICPPTemplateParameter) col.getName(13).resolveBinding();
		assertSame( U, U2 );
		assertSame( T, U );
	}
	
	public void testTemplateScopes_2() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("class A {                              \n"); //$NON-NLS-1$
	    buffer.append("   template < class T > void f(T);     \n"); //$NON-NLS-1$
	    buffer.append("};                                     \n"); //$NON-NLS-1$
	    buffer.append("template <class U> void A::f<>(U){}    \n"); //$NON-NLS-1$
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(1).resolveBinding();
		ICPPFunctionTemplate f1 = (ICPPFunctionTemplate) col.getName(2).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(3).resolveBinding();
		assertSame( T, T2 );
		
		ICPPTemplateParameter U = (ICPPTemplateParameter) col.getName(5).resolveBinding();
		assertSame( T, U );
		ICPPClassType A2 = (ICPPClassType) col.getName(7).resolveBinding();
		assertSame( A, A2 );
		ICPPMethod f2 = (ICPPMethod) col.getName(8).resolveBinding();
		IBinding U2 = col.getName(10).resolveBinding();
		assertSame( U, U2 );
		
		assertTrue( f2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)f2).getSpecializedBinding(), f1 );
	}
	
	public void test14_7_3s16() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> struct A {                              \n"); //$NON-NLS-1$
		buffer.append("   void f(T);                                             \n"); //$NON-NLS-1$
		buffer.append("   template<class X> void g(T,X);                         \n"); //$NON-NLS-1$
		buffer.append("   void h(T) { }                                          \n"); //$NON-NLS-1$
		buffer.append("};                                                        \n"); //$NON-NLS-1$
		buffer.append("template<> void A<int>::f(int);                           \n"); //$NON-NLS-1$
		buffer.append("template<class T> template<class X> void A<T>::g(T,X) { } \n"); //$NON-NLS-1$
		buffer.append("template<> template<class X> void A<int>::g(int,X);       \n"); //$NON-NLS-1$
		buffer.append("template<> template<> void A<int>::g(int,char);           \n"); //$NON-NLS-1$
		buffer.append("template<> template<> void A<int>::g<char>(int,char);     \n"); //$NON-NLS-1$
		buffer.append("template<> void A<int>::h(int) { }                        \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(2).resolveBinding();
		ICPPTemplateParameter T2 = (ICPPTemplateParameter) col.getName(3).resolveBinding();
		assertSame( T, T2 );
		
		ICPPTemplateParameter X = (ICPPTemplateParameter) col.getName(5).resolveBinding();
		ICPPFunctionTemplate g = (ICPPFunctionTemplate) col.getName(6).resolveBinding();
		ICPPTemplateParameter T3 = (ICPPTemplateParameter) col.getName(7).resolveBinding();
		assertSame( T, T3 );
		ICPPTemplateParameter X2 = (ICPPTemplateParameter) col.getName(9).resolveBinding();
		assertSame( X, X2 );
		
		ICPPMethod h = (ICPPMethod) col.getName(11).resolveBinding();
		ICPPTemplateParameter T4 = (ICPPTemplateParameter) col.getName(12).resolveBinding();
		assertSame( T, T4 );
		
		ICPPClassType A2 = (ICPPClassType) col.getName(15).resolveBinding();
		assertTrue( A2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)A2).getTemplateDefinition(), A );
		ICPPMethod f2 = (ICPPMethod) col.getName(17).resolveBinding();
		assertTrue( f2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)f2).getSpecializedBinding(), f );
		
		ICPPTemplateParameter TR = (ICPPTemplateParameter) col.getName(19).resolveBinding();
		assertSame( T, TR );
		ICPPTemplateParameter XR = (ICPPTemplateParameter) col.getName(20).resolveBinding();
		assertSame( X, XR );
		ICPPClassType A3 = (ICPPClassType) col.getName(22).resolveBinding();
		assertTrue( A3 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)A3).getTemplateDefinition(), A );
		assertNotSame( A2, A3 );
		
		ICPPMethod g2 = (ICPPMethod) col.getName(25).resolveBinding();
		assertSame( g2, g );
		TR = (ICPPTemplateParameter) col.getName(26).resolveBinding();
		assertSame( T, TR );
		XR = (ICPPTemplateParameter) col.getName(28).resolveBinding();
		assertSame( X, XR );
		
		assertSame( col.getName(32).resolveBinding(), A2 );
		assertSame( col.getName(39).resolveBinding(), A2 );
		assertSame( col.getName(45).resolveBinding(), A2 );
		assertSame( col.getName(52).resolveBinding(), A2 );
		
		ICPPMethod h2 = (ICPPMethod) col.getName(54).resolveBinding();
		assertTrue( h2 instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)h2).getSpecializedBinding(), h );
	}
	
	public void test14_6_1s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace N {                                                 \n"); //$NON-NLS-1$
		buffer.append("   int C;                                                     \n"); //$NON-NLS-1$
		buffer.append("   template<class T> class B {                                \n"); //$NON-NLS-1$
		buffer.append("      void f(T);                                              \n"); //$NON-NLS-1$
		buffer.append("   };                                                         \n"); //$NON-NLS-1$
		buffer.append("}                                                             \n"); //$NON-NLS-1$
		buffer.append("template<class C> void N::B<C>::f(C) {                        \n"); //$NON-NLS-1$
		buffer.append("   C b; // C is the template parameter, not N::C              \n"); //$NON-NLS-1$
		buffer.append("}                                                             \n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(2).resolveBinding();
		ICPPClassTemplate B = (ICPPClassTemplate) col.getName(3).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(4).resolveBinding();
		ICPPTemplateParameter TR = (ICPPTemplateParameter) col.getName(5).resolveBinding();
		assertSame( T, TR );
		
		ICPPTemplateParameter C = (ICPPTemplateParameter) col.getName(7).resolveBinding();
		assertSame( C, T );
		
		ICPPClassType B2 = (ICPPClassType) col.getName(10).resolveBinding();
		assertTrue( B2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)B2).getTemplateDefinition(), B );
		
		ICPPTemplateParameter CR = (ICPPTemplateParameter) col.getName(12).resolveBinding();
		assertSame( CR, T );
		
		ICPPMethod f2 = (ICPPMethod) col.getName(13).resolveBinding();
		assertSame( f2, f );
		
		CR = (ICPPTemplateParameter) col.getName(14).resolveBinding();
		assertSame( CR, T );
	}
	
	public void testBug90689_ExplicitInstantiation() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("template <class T> class Array {};                    \n"); //$NON-NLS-1$
	    buffer.append("template <class T> void sort( Array<T> & );           \n"); //$NON-NLS-1$
	    buffer.append("template void sort<>( Array<int> & );                 \n"); //$NON-NLS-1$
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
	    ICPPClassTemplate A = (ICPPClassTemplate) col.getName(1).resolveBinding();
	    ICPPFunctionTemplate s = (ICPPFunctionTemplate) col.getName(3).resolveBinding();
	    
	    ICPPClassType A2 = (ICPPClassType) col.getName(4).resolveBinding();
	    assertTrue( A2 instanceof ICPPTemplateInstance );
	    assertSame( ((ICPPTemplateInstance)A2).getTemplateDefinition(), A );
	    
	    ICPPFunction s2 = (ICPPFunction) col.getName(8).resolveBinding();
	    assertTrue( s2 instanceof ICPPTemplateInstance );
	    assertSame( ((ICPPTemplateInstance)s2).getTemplateDefinition(), s );
	    
	    ICPPClassType A3 = (ICPPClassType) col.getName(10).resolveBinding();
	    assertTrue( A3 instanceof ICPPTemplateInstance );
	    assertSame( ((ICPPTemplateInstance)A3).getTemplateDefinition(), A );
	    assertNotSame( A2, A3 );
	}
	
	public void test14_7_2s2_ExplicitInstantiation() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class Array { };                              \n"); //$NON-NLS-1$
		buffer.append("template class Array<char>;                                     \n"); //$NON-NLS-1$
		buffer.append("template<class T> void sort(Array<T>& v) {  }                   \n"); //$NON-NLS-1$
		buffer.append("template void sort(Array<char>&); // argument is deduced here   \n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );

		ICPPClassTemplate A1 = (ICPPClassTemplate) col.getName(1).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) col.getName(2).resolveBinding();
		assertTrue( A2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)A2).getTemplateDefinition(), A1 );
		
		ICPPFunctionTemplate s1 = (ICPPFunctionTemplate) col.getName(5).resolveBinding();
		ICPPFunction s2 = (ICPPFunction) col.getName(10).resolveBinding();
		assertTrue( s2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)s2).getTemplateDefinition(), s1 );
		
		ICPPClassType A3 = (ICPPClassType) col.getName(11).resolveBinding();
		assertSame( A2, A3 );
	}
	
	public void testBug74204() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("template <class T> class A {       \n"); //$NON-NLS-1$
	    buffer.append("   A<T>* p;                        \n"); //$NON-NLS-1$
	    buffer.append("   void f() { this; }              \n"); //$NON-NLS-1$
	    buffer.append("};                                 \n"); //$NON-NLS-1$
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		IField p = (IField) col.getName(5).resolveBinding();
		
		IASTName f = col.getName(6);
		IASTFunctionDefinition fdef = (IASTFunctionDefinition) f.getParent().getParent();
		IASTExpressionStatement statement = (IASTExpressionStatement) ((IASTCompoundStatement)fdef.getBody()).getStatements()[0];
		IType type = CPPVisitor.getExpressionType( statement.getExpression() );
		
		assertTrue( type.isSameType( p.getType() ) );
	}
	
	public void testDeferredFunctionTemplates() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T > void f( T );               \n"); //$NON-NLS-1$
		buffer.append("template <class T > void g( T t ){             \n"); //$NON-NLS-1$
		buffer.append("   f( t );                                     \n"); //$NON-NLS-1$
		buffer.append("}                                              \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPFunctionTemplate f = (ICPPFunctionTemplate) col.getName(1).resolveBinding();
		ICPPFunction f2 = (ICPPFunction) col.getName(8).resolveBinding();
		assertTrue( f2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)f2).getTemplateDefinition(), f );
	}
	
	public void testRelaxationForTemplateInheritance() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template < class T > class A {};                          \n"); //$NON-NLS-1$
		buffer.append("template < class T > class B {                            \n"); //$NON-NLS-1$
		buffer.append("   void init( A<T> * );                                   \n"); //$NON-NLS-1$
		buffer.append("};                                                        \n"); //$NON-NLS-1$
		buffer.append("template < class T > class C : public B<T> {              \n"); //$NON-NLS-1$
		buffer.append("   C( A<T> * a ) {                                        \n"); //$NON-NLS-1$
		buffer.append("      init( a );                                          \n"); //$NON-NLS-1$
		buffer.append("   }                                                      \n"); //$NON-NLS-1$
		buffer.append("};                                                        \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPMethod init = (ICPPMethod) col.getName(4).resolveBinding();
		assertSame( init, col.getName(19).resolveBinding() );
	}
	
	public void testBug91707() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class Tp, class Tr > class iter {                         \n"); //$NON-NLS-1$
		buffer.append("   Tp operator -> () const;                                         \n"); //$NON-NLS-1$
		buffer.append("   Tr operator [] (int) const;                                      \n"); //$NON-NLS-1$
		buffer.append("};                                                                  \n"); //$NON-NLS-1$
		buffer.append("template <class T> class list {                                     \n"); //$NON-NLS-1$
		buffer.append("   typedef iter< T*, T& > iterator;                                 \n"); //$NON-NLS-1$
		buffer.append("   iterator begin();                                                \n"); //$NON-NLS-1$
		buffer.append("   iterator end();                                                  \n"); //$NON-NLS-1$
		buffer.append("};                                                                  \n"); //$NON-NLS-1$
		buffer.append("class Bar { public: int foo; };                                     \n"); //$NON-NLS-1$
		buffer.append("void f() {                                                          \n"); //$NON-NLS-1$
		buffer.append("   list<Bar> bar;                                                   \n"); //$NON-NLS-1$
		buffer.append("   for( list<Bar>::iterator i = bar.begin(); i != bar.end(); ++i ){ \n"); //$NON-NLS-1$
		buffer.append("      i->foo;  i[0].foo;                                            \n"); //$NON-NLS-1$
		buffer.append("   }                                                                \n"); //$NON-NLS-1$
		buffer.append("}                                                                   \n"); //$NON-NLS-1$
		
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		ICPPMethod begin = (ICPPMethod) col.getName(16).resolveBinding();
		ICPPMethod end = (ICPPMethod) col.getName(18).resolveBinding();

		ICPPField foo = (ICPPField) col.getName(20).resolveBinding();
		
		IBinding r = col.getName(33).resolveBinding();
		assertTrue( r instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)r).getSpecializedBinding(), begin );
		
		r = col.getName(36).resolveBinding();
		assertTrue( r instanceof ICPPSpecialization );
		assertSame( ((ICPPSpecialization)r).getSpecializedBinding(), end );
		
		assertSame( foo, col.getName(39).resolveBinding() );
		assertSame( foo, col.getName(41).resolveBinding() );
	}
}
