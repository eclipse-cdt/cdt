/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.internal.core.dom.NamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.SimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;

/**
 * @author jcamelon
 *
 */
public class CrossReferenceTests extends BaseDOMTest {

	public CrossReferenceTests( String arg )
	{
		super( arg );
	}


	public void testMultipleNamespaceDefinition() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "namespace A { int a; }\n" );
		code.write( "namespace A { int k; }\n" );
		TranslationUnit tu = parse( code.toString() );
		assertEquals( tu.getDeclarations().size(), 1 );
		assertEquals( ((NamespaceDefinition)tu.getDeclarations().get(0)).getDeclarations().size(), 2 ); 
	}
	
	public void testElaboratedTypeReference() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class A { int x; }; \n");
		code.write( "class A myA;");
		TranslationUnit tu = parse( code.toString() ); 
		assertEquals( tu.getDeclarations().size(), 2 ); 
		SimpleDeclaration first = (SimpleDeclaration)tu.getDeclarations().get(0);
		SimpleDeclaration second = (SimpleDeclaration)tu.getDeclarations().get(1);
	}
}
