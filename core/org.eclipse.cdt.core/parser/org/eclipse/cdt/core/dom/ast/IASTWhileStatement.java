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
 * Ye ol' while statement.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTWhileStatement extends IASTStatement {
	/**
	 * {@code CONDITIONEXPRESSION} represents the relationship between an {@code IASTWhileStatement} and
	 * it's nested {@code IASTExpression}.
	 */
	public static final ASTNodeProperty CONDITIONEXPRESSION = new ASTNodeProperty(
			"IASTWhileStatement.CONDITIONEXPRESSION - IASTExpression (condition) for IASTWhileStatement"); //$NON-NLS-1$

	/**
	 * {@code BODY} represents the relationship between an {@code IASTWhileStatement} and
	 * it's nested {@code IASTStatement}.
	 */
	public static final ASTNodeProperty BODY = new ASTNodeProperty(
			"IASTWhileStatement.BODY - IASTStatement (body) for IASTWhileStatement"); //$NON-NLS-1$

	/**
	 * Returns the condition on the while loop
	 *
	 * @return expression for the condition
	 */
	public IASTExpression getCondition();

	/**
	 * Sets the condition of the while loop.
	 *
	 * @param condition
	 */
	public void setCondition(IASTExpression condition);

	/**
	 * The body of the loop.
	 *
	 * @return the body
	 */
	public IASTStatement getBody();

	/**
	 * Sets the body of the while loop.
	 *
	 * @param body
	 */
	public void setBody(IASTStatement body);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTWhileStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTWhileStatement copy(CopyStyle style);
}
