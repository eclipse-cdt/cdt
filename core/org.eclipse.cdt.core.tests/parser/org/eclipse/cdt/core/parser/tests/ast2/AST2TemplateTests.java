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
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
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
	
//	public void testTemplateInstance() throws Exception {
//		StringBuffer buffer = new StringBuffer();
//		buffer.append("template < class T > class A {             \n"); //$NON-NLS-1$
//		buffer.append("   T t;                                    \n"); //$NON-NLS-1$
//		buffer.append("};                                         \n"); //$NON-NLS-1$
//		buffer.append("void f(){                                  \n"); //$NON-NLS-1$
//		buffer.append("   A<int> a;                               \n"); //$NON-NLS-1$
//		buffer.append("   a.t;                                    \n"); //$NON-NLS-1$
//		buffer.append("}                                          \n"); //$NON-NLS-1$
//		
//		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
//        CPPNameCollector col = new CPPNameCollector();
//        tu.accept(col);
//        
//        assertEquals( col.size(), 10 );
//        
//        ICPPClassType A1 = (ICPPClassType) col.getName(1).resolveBinding();
//        ICPPField t1 = (ICPPField) col.getName(3).resolveBinding();
//        
//        ICPPVariable a = (ICPPVariable) col.getName(7).resolveBinding();
//        assertFalse( a.isTemplateInstance() );
//        
//        ICPPClassType A2 = (ICPPClassType) col.getName(5).resolveBinding();
//        ICPPClassType A = (ICPPClassType) a.getType();
//        assertSame( A2, A );
//        ICPPClassScope AScope = (ICPPClassScope) A.getCompositeScope();
//        
//        ICPPField t = (ICPPField) col.getName(9).resolveBinding();
//        IType type = t.getType();
//        assertTrue( type instanceof IBasicType );
//        assertEquals( ((IBasicType)type).getType(), IBasicType.t_int );
//        
//        assertSame( t.getScope(), AScope );
//        
//        assertTrue( A.isTemplateInstance() );
//        assertSame( A.getTemplatedBinding(), A1 );
//        assertTrue( t.isTemplateInstance() );
//        assertSame( t.getTemplatedBinding(), t1 );
//	}

}
