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
 * This is an initializer that is simply an expression.
 * 
 * @author Doug Schaefer
 */
public interface IASTInitializerExpression extends IASTInitializer {

    /**
     * <code>INITIALIZER_EXPRESSION</code> represents the relationship between an <code>IASTInitializerExpression</code>. and its <code></code>IASTExpression</code>. 
     */
    public static final ASTNodeProperty INITIALIZER_EXPRESSION = new ASTNodeProperty( "Initializer Expression"); //$NON-NLS-1$
	/**
	 * Get the expression for the initializer.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getExpression();
	
	/**
	 * Set the initializer's expression. 
	 * 
	 * @param expression <code>IASTExpression</code>
	 */
	public void setExpression( IASTExpression expression );
}
