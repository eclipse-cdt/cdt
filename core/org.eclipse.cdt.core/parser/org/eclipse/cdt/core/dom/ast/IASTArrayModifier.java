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
 * This is the portion of the node that represents the portions when someone declares a 
 * variable/type which is an array.
 * 
 * @author jcamelon
 */
public interface IASTArrayModifier extends IASTNode {
    
    /**
     * Node property that describes the relationship between an <code>IASTArrayModifier</code> and an <code>IASTExpression</code>.
     */
    public static final ASTNodeProperty CONSTANT_EXPRESSION = new ASTNodeProperty( "Constant Expression");  //$NON-NLS-1$
    /**
     * <code>EMPTY_ARRAY</code> is referred to in implementations
     */
    public static final IASTArrayModifier[] EMPTY_ARRAY = new IASTArrayModifier[0];
    /**
     * Get the constant expression that represents the size of the array.
     * 
     * @return <code>IASTExpression</code>
     */
    public IASTExpression getConstantExpression();
    /**
     * Set the constant expression that represents the size of the array.
     * 
     * @param expression <code>IASTExpression</code> 
     */
    public void setConstantExpression( IASTExpression expression );

}
