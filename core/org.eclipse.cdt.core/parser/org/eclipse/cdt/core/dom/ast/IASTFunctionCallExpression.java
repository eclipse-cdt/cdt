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
 * This interface represents a function call expression.
 * f( x ) : f is the function name expression, x is the parameter expression.
 * 
 * @author jcamelon
 */
public interface IASTFunctionCallExpression extends IASTExpression  {
    
	
    /**
     * <code>FUNCTION_NAME</code> represents the relationship between a <code>IASTFunctionCallExpression</code> and its <code>IASTExpression</code> (function name).
     */
    public static final ASTNodeProperty FUNCTION_NAME = new ASTNodeProperty( "Function Name"); //$NON-NLS-1$
    /**
     * Set the function name expression. 
     * @param expression <code>IASTExpression</code> representing the function name
     */
    public void setFunctionNameExpression( IASTExpression expression );
    /**
     * Get the function name expression.
     * @return <code>IASTExpression</code> representing the function name
     */
    public IASTExpression getFunctionNameExpression();
    
    /**
     * <code>PARAMETERS</code> represents the relationship between a <code>IASTFunctionCallExpression</code> and its <code>IASTExpression</code> (parameters). 
     */
    public static final ASTNodeProperty PARAMETERS = new ASTNodeProperty( "Parameters"); //$NON-NLS-1$
    /**
     * Set the parameters expression.  
     * @param expression <code>IASTExpression</code> representing the parameters
     */
    public void setParameterExpression( IASTExpression expression );
    /**
     * Get the parameter expression.
     * @return <code>IASTExpression</code> representing the parameters
     */
    public IASTExpression getParameterExpression();
    
}
