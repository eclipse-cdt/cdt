/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
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
    	IASTNamespaceDefinition namespace = (IASTNamespaceDefinition)assertSoleDeclaration("namespace A { namespace B { int x; } }"); //$NON-NLS-1$
    	assertQualifiedName( namespace.getFullyQualifiedName(), new String [] {"A" } ); //$NON-NLS-1$
    	IASTNamespaceDefinition namespace2 = (IASTNamespaceDefinition)namespace.getDeclarations().next();
		assertQualifiedName( namespace2.getFullyQualifiedName(), new String [] { "A", "B" } ); //$NON-NLS-1$ //$NON-NLS-2$
    	
    }
    
    public void testClass() throws Exception
    {
    	IASTAbstractTypeSpecifierDeclaration abs = (IASTAbstractTypeSpecifierDeclaration)assertSoleDeclaration( "class A { class B { int a; }; };");  //$NON-NLS-1$
    	IASTClassSpecifier classSpec = (IASTClassSpecifier)abs.getTypeSpecifier();
    	assertQualifiedName( classSpec.getFullyQualifiedName(), new String [] { "A" } );    	 //$NON-NLS-1$
    	Iterator subDecls = classSpec.getDeclarations();
    	abs = (IASTAbstractTypeSpecifierDeclaration)subDecls.next(); 
    	assertFalse( subDecls.hasNext() );
		classSpec = (IASTClassSpecifier)abs.getTypeSpecifier();
		assertQualifiedName( classSpec.getFullyQualifiedName(), new String [] { "A", "B" } );  //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void testEnum() throws Exception
    {
    	Iterator declarations = parse( "class A { enum E1 { enumerator = 1 }; };\n namespace N { enum E2 { enumerator = 4 }; }\n" ).getDeclarations(); //$NON-NLS-1$
    	IASTAbstractTypeSpecifierDeclaration abs = (IASTAbstractTypeSpecifierDeclaration)declarations.next(); 
    	IASTClassSpecifier classSpec = (IASTClassSpecifier)abs.getTypeSpecifier();
    	IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier)(((IASTAbstractTypeSpecifierDeclaration)classSpec.getDeclarations().next()).getTypeSpecifier());
    	assertQualifiedName( enumSpec.getFullyQualifiedName(), new String[] { "A", "E1" } ); //$NON-NLS-1$ //$NON-NLS-2$
    	IASTNamespaceDefinition nms = (IASTNamespaceDefinition)declarations.next(); 
		enumSpec = (IASTEnumerationSpecifier)(((IASTAbstractTypeSpecifierDeclaration)nms.getDeclarations().next()).getTypeSpecifier());
		assertQualifiedName( enumSpec.getFullyQualifiedName(), new String[] { "N", "E2" } );		 //$NON-NLS-1$ //$NON-NLS-2$
    	assertFalse( declarations.hasNext() );
    }
    
    public void testVariable() throws Exception
    {
    	IASTNamespaceDefinition topNMS = (IASTNamespaceDefinition)assertSoleDeclaration("namespace A { int x; namespace B { int y; } }"); //$NON-NLS-1$
    	Iterator level1 = topNMS.getDeclarations();
    	IASTVariable var = (IASTVariable)level1.next();
    	assertQualifiedName( var.getFullyQualifiedName(), new String[] {"A","x"}); //$NON-NLS-1$ //$NON-NLS-2$
    	Iterator level2 = ((IASTNamespaceDefinition)level1.next()).getDeclarations();
		assertFalse( level1.hasNext());
    	var = (IASTVariable)level2.next();
		assertQualifiedName( var.getFullyQualifiedName(), new String[] {"A","B","y"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

	public void testTypedef() throws Exception
	{
		IASTNamespaceDefinition topNMS = (IASTNamespaceDefinition)
			assertSoleDeclaration("namespace FLEA { typedef int GODS_INT; class ANTHONY { typedef ANTHONY * tonyPointer; }; }"); //$NON-NLS-1$
		Iterator level1 = topNMS.getDeclarations();
		assertQualifiedName( ((IASTTypedefDeclaration)level1.next()).getFullyQualifiedName(), new String [] { "FLEA", "GODS_INT" } ); //$NON-NLS-1$ //$NON-NLS-2$
		assertQualifiedName( ((IASTTypedefDeclaration)((IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)level1.next()).getTypeSpecifier()).getDeclarations().next()).getFullyQualifiedName(), new String [] { "FLEA", "ANTHONY", "tonyPointer" } );  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
    

	public void testMembers() throws Exception
	{
		IASTNamespaceDefinition topNMS = 
			(IASTNamespaceDefinition)assertSoleDeclaration( "namespace John { class David { int Shannon; void Camelon(); }; } "); //$NON-NLS-1$
		Iterator members = 
			((IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)topNMS.getDeclarations().next()).getTypeSpecifier()).getDeclarations();
		assertQualifiedName( ((IASTField)members.next()).getFullyQualifiedName(), new String[] { "John", "David", "Shannon" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertQualifiedName( ((IASTMethod)members.next()).getFullyQualifiedName(), new String[] { "John", "David", "Camelon" } );  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public void testFunction() throws Exception
	{
		IASTNamespaceDefinition topNMS = 
			(IASTNamespaceDefinition)assertSoleDeclaration( "namespace Bogdan { void Wears(); namespace Fancy { int Pants(); } }" ); //$NON-NLS-1$
		Iterator members = topNMS.getDeclarations(); 
		assertQualifiedName( ((IASTFunction)members.next()).getFullyQualifiedName(), new String[] { "Bogdan", "Wears" } ); //$NON-NLS-1$ //$NON-NLS-2$
		assertQualifiedName( ((IASTFunction)((IASTNamespaceDefinition)members.next()).getDeclarations().next()).getFullyQualifiedName(), new String[] { "Bogdan", "Fancy", "Pants" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
}
