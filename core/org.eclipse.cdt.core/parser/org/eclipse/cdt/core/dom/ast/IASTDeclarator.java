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
 * @author Doug Schaefer
 */
public interface IASTDeclarator extends IASTNode {
    public static final IASTDeclarator[] EMPTY_DECLARATOR_ARRAY = new IASTDeclarator[0];

	ASTNodeProperty POINTER_OPERATOR = new ASTNodeProperty( "Pointer Operator"); //$NON-NLS-1$
    ASTNodeProperty INITIALIZER = new ASTNodeProperty( "Initializer"); //$NON-NLS-1$
    ASTNodeProperty NESTED_DECLARATOR = new ASTNodeProperty( "Nested Declarator"); //$NON-NLS-1$
    ASTNodeProperty DECLARATOR_NAME = new ASTNodeProperty( "Declarator Name"); //$NON-NLS-1$

    /**
	 * This is the list of pointer operators applied to the type for
	 * the declarator.
	 * 
	 * @return List of IASTPointerOperator
	 */
	public IASTPointerOperator[] getPointerOperators();
	
	public void addPointerOperator( IASTPointerOperator operator );

	/**
	 * If the declarator is nested in parenthesis, this returns the
	 * declarator as found in those parenethesis.
	 * 
	 * @return the nested declarator or null
	 */
	public IASTDeclarator getNestedDeclarator();
	
	public void setNestedDeclarator( IASTDeclarator nested );
	
	/**
	 * This returns the name of the declarator. If this is an abstract
	 * declarator, this will return null.
	 * 
	 * @return the name of the declarator or null
	 */
	public IASTName getName();
	
	public void setName( IASTName name );
	
	/**
	 * This is the optional initializer for this declarator.
	 * 
	 * @return the initializer expression or null
	 */
	public IASTInitializer getInitializer();
	
	public void setInitializer( IASTInitializer initializer );
	
}
