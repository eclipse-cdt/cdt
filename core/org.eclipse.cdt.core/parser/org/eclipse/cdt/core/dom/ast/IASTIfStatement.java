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
 * The if statement including the optional else clause.
 * 
 * @author Doug Schaefer
 */
public interface IASTIfStatement extends IASTStatement {

	/**
	 * <code>CONDITION</code> represents the relationship between an
	 * <code>IASTIfStatement</code> and its nested <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty CONDITION = new ASTNodeProperty(
			"IASTIfStatement.CONDITION - IASTExpression condition for IASTIfStatement"); //$NON-NLS-1$

	/**
	 * <code>THEN</code> represents the relationship between an
	 * <code>IASTIfStatement</code> and its nested <code>IASTStatement</code>
	 * (then).
	 */
	public static final ASTNodeProperty THEN = new ASTNodeProperty("IASTIfStatement.THEN - IASTStatement (then) for IASTIfStatement"); //$NON-NLS-1$

	/**
	 * <code>ELSE</code> represents the relationship between an
	 * <code>IASTIfStatement</code> and its nested <code>IASTStatement</code>
	 * (else).
	 */
	public static final ASTNodeProperty ELSE = new ASTNodeProperty("IASTIfStatement.ELSE - IASTStatement (else) for IASTIfStatement"); //$NON-NLS-1$

	/**
	 * Get the condition in the if statement.
	 * 
	 * @return the condition <code>IASTExpression</code>
	 */
	public IASTExpression getConditionExpression();

	/**
	 * Set the condition in the if statement.
	 * 
	 * @param condition
	 *            <code>IASTExpression</code>
	 */
	public void setConditionExpression(IASTExpression condition);

	/**
	 * Get the statement that is executed if the condition is true.
	 * 
	 * @return the then clause <code>IASTStatement</code>
	 */
	public IASTStatement getThenClause();

	/**
	 * Set the statement that is executed if the condition is true.
	 * 
	 * @param thenClause
	 *            <code>IASTStatement</code>
	 */
	public void setThenClause(IASTStatement thenClause);

	/**
	 * Get the statement that is executed if the condition is false. This clause
	 * is optional and returns null if there is none.
	 * 
	 * @return the else clause or null <code>IASTStatement</code>
	 */
	public IASTStatement getElseClause();

	/**
	 * Set the else clause.
	 * 
	 * @param elseClause
	 *            <code>IASTStatement</code>
	 */
	public void setElseClause(IASTStatement elseClause);

}
