/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast;

/**
 * @author jcamelon
 */
public interface IASTTypeId extends IASTNode {
    
	public static final IASTTypeId [] EMPTY_TYPEID_ARRAY = new IASTTypeId[0];
	
    public static final ASTNodeProperty DECL_SPECIFIER = new ASTNodeProperty( "Decl Specifier"); //$NON-NLS-1$
    public static final ASTNodeProperty ABSTRACT_DECLARATOR = new ASTNodeProperty( "Abstract Declarator"); //$NON-NLS-1$
    
    public IASTDeclSpecifier getDeclSpecifier();
    public void setDeclSpecifier( IASTDeclSpecifier declSpec );
    public IASTDeclarator    getAbstractDeclarator();
    public void setAbstractDeclarator( IASTDeclarator abstractDeclarator );
}
