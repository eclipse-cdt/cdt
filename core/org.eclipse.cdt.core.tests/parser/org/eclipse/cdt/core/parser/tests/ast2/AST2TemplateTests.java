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

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.parser.ParserLanguage;

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
        assertSame( ((ICPPTemplateInstance)A_int).getOriginalBinding(), A );
        
        ICPPClassScope A_int_Scope = (ICPPClassScope) A_int.getCompositeScope();
        assertNotSame( A_int_Scope, ((ICompositeType) A).getCompositeScope() );
        
        ICPPField t = (ICPPField) col.getName(11).resolveBinding();
        assertTrue( t instanceof ICPPTemplateInstance );
        assertSame( ((ICPPTemplateInstance)t).getOriginalBinding(), t1 );
        assertSame( t.getScope(), A_int_Scope );
        IType type = t.getType();
        assertTrue( type instanceof IBasicType );
        assertEquals( ((IBasicType)type).getType(), IBasicType.t_int );
        
        t = (ICPPField) col.getName(13).resolveBinding();
        assertTrue( t instanceof ICPPTemplateInstance );
        assertSame( ((ICPPTemplateInstance)t).getOriginalBinding(), t2 );
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
		assertSame( ((ICPPTemplateInstance)A_int).getOriginalBinding(), A );
		
		ICPPMethod f_int = (ICPPMethod) col.getName(11).resolveBinding();
		assertTrue( f_int instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)f_int).getOriginalBinding(), f );
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
        assertSame( ((ICPPTemplateInstance)f3).getOriginalBinding(), f );
        
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
        assertSame( pi.getOriginalBinding(), pair );
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
		ICPPClassTemplate A2 = (ICPPClassTemplate) col.getName(3).resolveBinding();
		ICPPTemplateParameter T3 = (ICPPTemplateParameter) col.getName(5).resolveBinding();
		ICPPClassTemplate A3 = (ICPPClassTemplate) col.getName(7).resolveBinding();
		ICPPTemplateParameter T4 = (ICPPTemplateParameter) col.getName(6).resolveBinding();
		
		assertTrue( A2 instanceof ICPPTemplateSpecialization );
		assertTrue( ((ICPPTemplateSpecialization)A2).isPartialSpecialization() );
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
		
		//TODO this isn't right, but its close enough for now
		assertTrue( f2 instanceof ICPPTemplateInstance );
		assertSame( ((ICPPTemplateInstance)f2).getOriginalBinding(), f1 );
	}
}
