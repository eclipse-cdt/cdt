/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is a function definition, i.e. it has a body.
 * 
 * @author Doug Schaefer
 */
public interface IASTFunctionDefinition extends IASTDeclaration {

	ASTNodeProperty DECL_SPECIFIER = new ASTNodeProperty( "Decl Specifier"); //$NON-NLS-1$
	ASTNodeProperty DECLARATOR = new ASTNodeProperty( "Declarator"); //$NON-NLS-1$
	ASTNodeProperty FUNCTION_BODY = new ASTNodeProperty( "Function Body"); //$NON-NLS-1$

    /**
	 * The decl specifier for the function.
	 * 
	 * @return
	 */
	public IASTDeclSpecifier getDeclSpecifier();

	public void setDeclSpecifier( IASTDeclSpecifier declSpec );
	
	/**
	 * The declarator for the function.
	 * 
	 * @return
	 */
	public IASTFunctionDeclarator getDeclarator();
	
	public void setDeclarator( IASTFunctionDeclarator declarator );
	
	/**
	 * This is the body of the function. This is usually a compound statement
	 * but C++ also has a function try block.
	 * 
	 * @return
	 */
	public IASTStatement getBody();
	
	public void setBody( IASTStatement statement );
	
	public IScope getScope();
	
}
