/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * The 'if' statement including the optional else clause.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTIfStatement extends IASTStatement {
	/**
	 * {@code CONDITION} represents the relationship between an
	 * {@code IASTIfStatement} and its nested {@code IASTExpression}.
	 */
	public static final ASTNodeProperty CONDITION = new ASTNodeProperty(
			"IASTIfStatement.CONDITION - IASTExpression condition for IASTIfStatement"); //$NON-NLS-1$

	/**
	 * {@code THEN} represents the relationship between an
	 * {@code IASTIfStatement} and its nested {@code IASTStatement}
	 * (then).
	 */
	public static final ASTNodeProperty THEN = new ASTNodeProperty("IASTIfStatement.THEN - IASTStatement (then) for IASTIfStatement"); //$NON-NLS-1$

	/**
	 * {@code ELSE} represents the relationship between an
	 * {@code IASTIfStatement} and its nested {@code IASTStatement}
	 * (else).
	 */
	public static final ASTNodeProperty ELSE = new ASTNodeProperty("IASTIfStatement.ELSE - IASTStatement (else) for IASTIfStatement"); //$NON-NLS-1$

	/**
	 * Returns the condition in the if statement.
	 * 
	 * @return the condition {@code IASTExpression}. May return {@code null} if the 'if'
	 *     statement has condition declaration instead of condition expression
	 *     (see {@link org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement}).
	 */
	public IASTExpression getConditionExpression();

	/**
	 * Sets the condition in the if statement.
	 * 
	 * @param condition {@code IASTExpression}
	 */
	public void setConditionExpression(IASTExpression condition);

	/**
	 * Returns the statement that is executed if the condition is true.
	 * 
	 * @return the then clause {@code IASTStatement}
	 */
	public IASTStatement getThenClause();

	/**
	 * Sets the statement that is executed if the condition is true.
	 * 
	 * @param thenClause {@code IASTStatement}
	 */
	public void setThenClause(IASTStatement thenClause);

	/**
	 * Returns the statement that is executed if the condition is false. This clause
	 * is optional and returns null if there is none.
	 * 
	 * @return the else clause or {@code null} {@code IASTStatement}
	 */
	public IASTStatement getElseClause();

	/**
	 * Sets the else clause.
	 * 
	 * @param elseClause {@code IASTStatement}
	 */
	public void setElseClause(IASTStatement elseClause);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTIfStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTIfStatement copy(CopyStyle style);
}
