/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * The for statement. The initialization clause can be an expression or a
 * declaration but not both.
 * 
 * @author Doug Schaefer
 */
public interface IASTForStatement extends IASTStatement {
    
	/**
	 * <code>CONDITION</code> represents the relationship between a
	 * <code>IASTForStatement</code> and its <code>IASTExpression</code>
	 * condition.
	 */
	public static final ASTNodeProperty CONDITION = new ASTNodeProperty(
			"IASTForStatement.CONDITION - IASTExpression condition of IASTForStatement"); //$NON-NLS-1$

	/**
	 * <code>ITERATION</code> represents the relationship between a
	 * <code>IASTForStatement</code> and its <code>IASTExpression</code>
	 * iteration expression.
	 */
	public static final ASTNodeProperty ITERATION = new ASTNodeProperty(
			"IASTForStatement.ITERATION - IASTExpression iteration of IASTForStatement"); //$NON-NLS-1$

	/**
	 * <code>BODY</code> represents the relationship between a
	 * <code>IASTForStatement</code> and its <code>IASTStatement</code>
	 * body.
	 */
	public static final ASTNodeProperty BODY = new ASTNodeProperty("IASTForStatement.BODY - IASTStatement body of IASTForStatement"); //$NON-NLS-1$

    /**
     * <code>INITIALIZER</code> represents the relationship between a
     * <code>IASTForStatement</code> and its <code>IASTDeclaration</code>
     * initializer.
     */
    public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty(
            "IASTForStatement.INITIALIZER - initializer for IASTForStatement"); //$NON-NLS-1$

    /**
     * @return
     */
    public IASTStatement getInitializerStatement();
    /**
     * @param statement
     */
    public void setInitializerStatement( IASTStatement statement );
    
    
	/**
	 * Get the condition expression for the loop.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getConditionExpression();

	/**
	 * Set the condition expression for the loop.
	 * 
	 * @param condition
	 *            <code>IASTExpression</code>
	 */
	public void setConditionExpression(IASTExpression condition);

	/**
	 * Get the expression that is evaluated after the completion of an iteration
	 * of the loop.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getIterationExpression();

	/**
	 * Set the expression that is evaluated after the completion of an iteration
	 * of the loop.
	 * 
	 * @param iterator
	 *            <code>IASTExpression</code>
	 */
	public void setIterationExpression(IASTExpression iterator);

	/**
	 * Get the statements that this for loop controls.
	 * 
	 * @return <code>IASTStatement</code>
	 */
	public IASTStatement getBody();

	/**
	 * Set the body of the for loop.
	 * 
	 * @param statement
	 *            <code>IASTStatement</code>
	 */
	public void setBody(IASTStatement statement);

	/**
	 * Get the <code>IScope</code> represented by this for loop.
	 * 
	 * @return <code>IScope</code>
	 */
	public IScope getScope();
}
