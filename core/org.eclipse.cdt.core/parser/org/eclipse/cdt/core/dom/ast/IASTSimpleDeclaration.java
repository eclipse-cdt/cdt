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
 * This is a simple declaration which contains a sequence of declSpecifiers
 * followed by a list of declarators.
 * 
 * @author Doug Schaefer
 */
public interface IASTSimpleDeclaration extends IASTDeclaration, IASTNode {

	ASTNodeProperty DECL_SPECIFIER = new ASTNodeProperty( "Decl Specifier"); //$NON-NLS-1$
    ASTNodeProperty DECLARATOR = new ASTNodeProperty( "Declarator"); //$NON-NLS-1$

    /**
	 * This returns the object representing the declSpecifiers for this
	 * declaration.
	 * 
	 * @return the declSpecifier object
	 */
	public IASTDeclSpecifier getDeclSpecifier();
	
	public void setDeclSpecifier( IASTDeclSpecifier declSpec );

	/**
	 * This returns the list of declarators in this declaration.
	 * 
	 * @return list of IASTDeclarator
	 */
	public IASTDeclarator[] getDeclarators();
	
	public void addDeclarator( IASTDeclarator declarator );
	
}
