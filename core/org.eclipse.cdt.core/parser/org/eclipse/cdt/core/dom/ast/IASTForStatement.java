/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * The 'for' statement. The initialization clause can be an expression
 * or a declaration but not both.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTForStatement extends IASTStatement {
	/**
	 * {@code CONDITION} represents the relationship between a {@code IASTForStatement} and
	 * its {@code IASTExpression} condition.
	 */
	public static final ASTNodeProperty CONDITION = new ASTNodeProperty(
			"IASTForStatement.CONDITION - IASTExpression condition of IASTForStatement"); //$NON-NLS-1$

	/**
	 * {@code ITERATION} represents the relationship between a {@code IASTForStatement} and
	 * its {@code IASTExpression} iteration expression.
	 */
	public static final ASTNodeProperty ITERATION = new ASTNodeProperty(
			"IASTForStatement.ITERATION - IASTExpression iteration of IASTForStatement"); //$NON-NLS-1$

	/**
	 * {@code BODY} represents the relationship between a {@code IASTForStatement} and
	 * its {@code IASTStatement} body.
	 */
	public static final ASTNodeProperty BODY = new ASTNodeProperty(
			"IASTForStatement.BODY - IASTStatement body of IASTForStatement"); //$NON-NLS-1$

	/**
	 * {@code INITIALIZER} represents the relationship between a {@code IASTForStatement} and
	 * its {@code IASTDeclaration} initializer.
	 */
	public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty(
			"IASTForStatement.INITIALIZER - initializer for IASTForStatement"); //$NON-NLS-1$

	/**
	 * Returns the initializer statement.
	 */
	public IASTStatement getInitializerStatement();

	/**
	 * @param statement
	 */
	public void setInitializerStatement(IASTStatement statement);

	/**
	 * Returns the condition expression for the loop.
	 *
	 * @return {@code IASTExpression}
	 */
	public IASTExpression getConditionExpression();

	/**
	 * Sets the condition expression for the loop.
	 *
	 * @param condition {@code IASTExpression}
	 */
	public void setConditionExpression(IASTExpression condition);

	/**
	 * Returns the expression that is evaluated after the completion of an iteration of the loop.
	 *
	 * @return {@code IASTExpression}
	 */
	public IASTExpression getIterationExpression();

	/**
	 * Sets the expression that is evaluated after the completion of an iteration of the loop.
	 *
	 * @param iterator {@code IASTExpression}
	 */
	public void setIterationExpression(IASTExpression iterator);

	/**
	 * Returns the statements that this for loop controls.
	 *
	 * @return {@code IASTStatement}
	 */
	public IASTStatement getBody();

	/**
	 * Sets the body of the for loop.
	 *
	 * @param statement {@code IASTStatement}
	 */
	public void setBody(IASTStatement statement);

	/**
	 * Returns the {@code IScope} represented by this for loop.
	 *
	 * @return {@code IScope}
	 */
	public IScope getScope();

	/**
	 * @since 5.1
	 */
	@Override
	public IASTForStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTForStatement copy(CopyStyle style);
}
