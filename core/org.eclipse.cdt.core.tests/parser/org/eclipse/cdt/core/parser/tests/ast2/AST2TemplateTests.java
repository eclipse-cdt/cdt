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
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
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
        ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
        ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();
        
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
        
        ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();
        ICPPTemplateParameter T = (ICPPTemplateParameter) col.getName(0).resolveBinding();
        ICPPField t1 = (ICPPField) col.getName(3).resolveBinding();
        ICPPField t2 = (ICPPField) col.getName(5).resolveBinding();
        
        assertSame( t1.getType(), T );
        assertSame( ((IPointerType)t2.getType()).getType(), T );
        
        ICPPVariable a = (ICPPVariable) col.getName(9).resolveBinding();
        
        ICPPClassType A_int = (ICPPClassType) col.getName(7).resolveBinding();
        assertSame( A_int, a.getType() );
        
        assertTrue( A_int instanceof ICPPInstance );
        assertSame( ((ICPPInstance)A_int).getOriginalBinding(), A );
        
        ICPPClassScope A_int_Scope = (ICPPClassScope) A_int.getCompositeScope();
        assertNotSame( A_int_Scope, A.getCompositeScope() );
        
        ICPPField t = (ICPPField) col.getName(11).resolveBinding();
        assertTrue( t instanceof ICPPInstance );
        assertSame( ((ICPPInstance)t).getOriginalBinding(), t1 );
        assertSame( t.getScope(), A_int_Scope );
        IType type = t.getType();
        assertTrue( type instanceof IBasicType );
        assertEquals( ((IBasicType)type).getType(), IBasicType.t_int );
        
        t = (ICPPField) col.getName(13).resolveBinding();
        assertTrue( t instanceof ICPPInstance );
        assertSame( ((ICPPInstance)t).getOriginalBinding(), t2 );
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
		assertTrue( A_int instanceof ICPPInstance );
		assertSame( ((ICPPInstance)A_int).getOriginalBinding(), A );
		
		ICPPMethod f_int = (ICPPMethod) col.getName(11).resolveBinding();
		assertTrue( f_int instanceof ICPPInstance );
		assertSame( ((ICPPInstance)f_int).getOriginalBinding(), f );
		ft = f_int.getType();
		assertTrue( ft.getReturnType() instanceof IBasicType );
		assertTrue( ((IPointerType)ft.getParameterTypes()[0]).getType() instanceof IBasicType );
	}
}
