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
 * The for statement. The initialization clause can be an expression or
 * a declaration but not both.
 * 
 * @author Doug Schaefer
 */
public interface IASTForStatement extends IASTStatement {

    /**
     * <code>INITEXPRESSION</code> represents the relationship between a <code>IASTForStatement</code> and its <code>IASTExpression</code> initializer.
     */
    public static final ASTNodeProperty INITEXPRESSION = new ASTNodeProperty("initExpression");  //$NON-NLS-1$
    /**
     * <code>INITDECLARATION</code> represents the relationship between a <code>IASTForStatement</code> and its <code>IASTDeclaration</code> initializer.
     */
    public static final ASTNodeProperty INITDECLARATION = new ASTNodeProperty("initDeclaration");  //$NON-NLS-1$
    /**
     * <code>CONDITION</code> represents the relationship between a <code>IASTForStatement</code> and its <code>IASTExpression</code> condition.
     */
    public static final ASTNodeProperty CONDITION = new ASTNodeProperty("condition");  //$NON-NLS-1$
    /**
     * <code>ITERATION</code> represents the relationship between a <code>IASTForStatement</code> and its <code>IASTExpression</code> iteration expression.
     */
    public static final ASTNodeProperty ITERATION = new ASTNodeProperty("iteration");  //$NON-NLS-1$
    /**
     * <code>BODY</code> represents the relationship between a <code>IASTForStatement</code> and its <code>IASTStatement</code> body. 
     */
    public static final ASTNodeProperty BODY = new ASTNodeProperty("body"); //$NON-NLS-1$

	/**
	 * Get the initial expression for the loop. Returns null if there is
	 * none. You can not have both an initial expression and an initial
	 * declaration.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getInitExpression();
	
	/**
	 * Set the initial expression for the loop.
	 * 
	 * @param expression <code>IASTExpression</code>
	 */
	public void setInit(IASTExpression expression);

	/**
	 * Get the initial declaration for the loop. Returns null if there is
	 * none. You can not have both an initial declaration and an initial
	 * declaration.
	 * 
	 * @return <code>IASTDeclaration</code>
	 */
	public IASTDeclaration getInitDeclaration();	
	/**
	 * Set the intiial declaration for the loop. 
	 * 
	 * @param declaration <code>IASTDeclaration</code>
	 */
	public void setInit(IASTDeclaration declaration);

	/**
	 * Get the condition expression for the loop.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getCondition();
	
	/**
	 * Set the condition expression for the loop.
	 * 
	 * @param condition <code>IASTExpression</code>
	 */
	public void setCondition(IASTExpression condition);

	/**
	 * Get the expression that is evaluated after the completion of an iteration
	 * of the loop.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getIterationExpression();

	/**
	 * Set the expression that is evaluated after the completion of an iteration of the loop.
	 * 
	 * @param iterator <code>IASTExpression</code>
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
	 * @param statement <code>IASTStatement</code>
	 */
	public void setBody( IASTStatement statement );
	
	/**
	 * Get the <code>IScope</code> represented by this for loop.
	 * @return <code>IScope</code>
	 */
	public IScope getScope();	
}
