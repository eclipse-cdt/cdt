/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author aniefer
 */

public class AST2CPPTests extends AST2BaseTest {

	public void testSimpleClass() throws Exception {
		StringBuffer buffer = new StringBuffer( "class A { } a;" ); //$NON-NLS-1$

		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTCompositeTypeSpecifier compTypeSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A = compTypeSpec.getName();
		
		IASTDeclarator dtor = decl.getDeclarators()[0];
		IASTName name_a = dtor.getName();
		
		ICompositeType A = (ICompositeType) name_A.resolveBinding();
		IVariable a = (IVariable) name_a.resolveBinding();
		ICompositeType A_2 = (ICompositeType) a.getType();
		assertNotNull( A );
		assertNotNull( a );
		assertSame( A, A_2 );
	}
	
	public void testClassForwardDecl() throws Exception {
		StringBuffer buffer = new StringBuffer( "class A; class A {};" ); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals( decl.getDeclarators().length, 0 );
		IASTElaboratedTypeSpecifier elabSpec = (IASTElaboratedTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_elab = elabSpec.getName();
		
		decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		assertEquals( decl.getDeclarators().length, 0 );
		IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_comp = compSpec.getName();
		
		ICompositeType A = (ICompositeType) name_elab.resolveBinding();
		ICompositeType A_2 = (ICompositeType) name_comp.resolveBinding();
		
		assertNotNull( A );
		assertSame( A, A_2 );
	}
	
	public void testVariable() throws Exception {
		StringBuffer buffer = new StringBuffer( "class A {};  A a;" ); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals( decl.getDeclarators().length, 0 );
		IASTCompositeTypeSpecifier compType = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A = compType.getName();
		
		decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		IASTDeclarator dtor = decl.getDeclarators()[0];
		IASTName name_a = dtor.getName();
		IASTNamedTypeSpecifier namedSpec = (IASTNamedTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A2 = namedSpec.getName();
		
		IVariable a = (IVariable) name_a.resolveBinding();
		ICompositeType A1 = (ICompositeType) a.getType();
		ICompositeType A2 = (ICompositeType) name_A2.resolveBinding();
		ICompositeType A = (ICompositeType) name_A.resolveBinding();
	
		assertNotNull( a );
		assertNotNull( A );
		assertSame( A, A1 );
		assertSame( A1, A2 );
	}
}
