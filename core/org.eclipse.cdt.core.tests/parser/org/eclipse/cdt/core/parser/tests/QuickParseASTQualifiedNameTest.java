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

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;

/**
 * @author jcamelon
 *
 */
public class QuickParseASTQualifiedNameTest extends BaseASTTest
{
    /**
     * @param a
     */
    public QuickParseASTQualifiedNameTest(String a)
    {
        super(a);
    }
    
    public void testNamespace() throws Exception
    {
    	IASTNamespaceDefinition namespace = (IASTNamespaceDefinition)assertSoleDeclaration("namespace A { namespace B { int x; } }");
    	assertQualifiedName( namespace.getFullyQualifiedName(), new String [] {"A" } );
    	IASTNamespaceDefinition namespace2 = (IASTNamespaceDefinition)namespace.getDeclarations().next();
		assertQualifiedName( namespace2.getFullyQualifiedName(), new String [] { "A", "B" } );
    	
    }
    
    public void testClass() throws Exception
    {
    	IASTAbstractTypeSpecifierDeclaration abs = (IASTAbstractTypeSpecifierDeclaration)assertSoleDeclaration( "class A { class B { int a; }; };"); 
    	IASTClassSpecifier classSpec = (IASTClassSpecifier)abs.getTypeSpecifier();
    	assertQualifiedName( classSpec.getFullyQualifiedName(), new String [] { "A" } );    	
    	Iterator subDecls = classSpec.getDeclarations();
    	abs = (IASTAbstractTypeSpecifierDeclaration)subDecls.next(); 
    	assertFalse( subDecls.hasNext() );
		classSpec = (IASTClassSpecifier)abs.getTypeSpecifier();
		assertQualifiedName( classSpec.getFullyQualifiedName(), new String [] { "A", "B" } ); 
    }
    
    public void testEnum() throws Exception
    {
    	Iterator declarations = parse( "class A { enum E1 { enumerator = 1 }; };\n namespace N { enum E2 { enumerator = 4 }; }\n" ).getDeclarations();
    	IASTAbstractTypeSpecifierDeclaration abs = (IASTAbstractTypeSpecifierDeclaration)declarations.next(); 
    	IASTClassSpecifier classSpec = (IASTClassSpecifier)abs.getTypeSpecifier();
    	IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier)(((IASTAbstractTypeSpecifierDeclaration)classSpec.getDeclarations().next()).getTypeSpecifier());
    	assertQualifiedName( enumSpec.getFullyQualifiedName(), new String[] { "A", "E1" } );
    	IASTNamespaceDefinition nms = (IASTNamespaceDefinition)declarations.next(); 
		enumSpec = (IASTEnumerationSpecifier)(((IASTAbstractTypeSpecifierDeclaration)nms.getDeclarations().next()).getTypeSpecifier());
		assertQualifiedName( enumSpec.getFullyQualifiedName(), new String[] { "N", "E2" } );		
    	assertFalse( declarations.hasNext() );
    }
    
    public void testVariable() throws Exception
    {
    	IASTNamespaceDefinition topNMS = (IASTNamespaceDefinition)assertSoleDeclaration("namespace A { int x; namespace B { int y; } }");
    	Iterator level1 = topNMS.getDeclarations();
    	IASTVariable var = (IASTVariable)level1.next();
    	assertQualifiedName( var.getFullyQualifiedName(), new String[] {"A","x"});
    	Iterator level2 = ((IASTNamespaceDefinition)level1.next()).getDeclarations();
		assertFalse( level1.hasNext());
    	var = (IASTVariable)level2.next();
		assertQualifiedName( var.getFullyQualifiedName(), new String[] {"A","B","y"});
    }

	public void testTypedef() throws Exception
	{
		IASTNamespaceDefinition topNMS = (IASTNamespaceDefinition)
			assertSoleDeclaration("namespace FLEA { typedef int GODS_INT; class ANTHONY { typedef ANTHONY * tonyPointer; }; }");
		Iterator level1 = topNMS.getDeclarations();
		assertQualifiedName( ((IASTTypedefDeclaration)level1.next()).getFullyQualifiedName(), new String [] { "FLEA", "GODS_INT" } );
		assertQualifiedName( ((IASTTypedefDeclaration)((IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)level1.next()).getTypeSpecifier()).getDeclarations().next()).getFullyQualifiedName(), new String [] { "FLEA", "ANTHONY", "tonyPointer" } ); 
	}
    

	public void testMembers() throws Exception
	{
		IASTNamespaceDefinition topNMS = 
			(IASTNamespaceDefinition)assertSoleDeclaration( "namespace John { class David { int Shannon; void Camelon(); }; } ");
		Iterator members = 
			((IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)topNMS.getDeclarations().next()).getTypeSpecifier()).getDeclarations();
		assertQualifiedName( ((IASTField)members.next()).getFullyQualifiedName(), new String[] { "John", "David", "Shannon" } );
		assertQualifiedName( ((IASTMethod)members.next()).getFullyQualifiedName(), new String[] { "John", "David", "Camelon" } ); 
	}
	
	public void testFunction() throws Exception
	{
		IASTNamespaceDefinition topNMS = 
			(IASTNamespaceDefinition)assertSoleDeclaration( "namespace Bogdan { void Wears(); namespace Fancy { int Pants(); } }" );
		Iterator members = topNMS.getDeclarations(); 
		assertQualifiedName( ((IASTFunction)members.next()).getFullyQualifiedName(), new String[] { "Bogdan", "Wears" } );
		assertQualifiedName( ((IASTFunction)((IASTNamespaceDefinition)members.next()).getDeclarations().next()).getFullyQualifiedName(), new String[] { "Bogdan", "Fancy", "Pants" } );
	}
	
}
