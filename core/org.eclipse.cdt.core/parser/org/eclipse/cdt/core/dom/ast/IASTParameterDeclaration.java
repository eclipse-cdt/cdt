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
 * This class represents a parameter declaration
 * 
 * @author Doug Schaefer
 */
public interface IASTParameterDeclaration extends IASTNode {

	ASTNodeProperty DECL_SPECIFIER = new ASTNodeProperty( "Decl Specifier"); //$NON-NLS-1$
    ASTNodeProperty DECLARATOR = new ASTNodeProperty( "Declarator"); //$NON-NLS-1$

    public IASTDeclSpecifier getDeclSpecifier();
	
	public IASTDeclarator getDeclarator();

    /**
     * @param declSpec
     */
    public void setDeclSpecifier(IASTDeclSpecifier declSpec);

    /**
     * @param declarator2
     */
    public void setDeclarator(IASTDeclarator declarator);
	
}
