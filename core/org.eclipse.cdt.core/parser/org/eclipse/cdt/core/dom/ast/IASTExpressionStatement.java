/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Expression statement.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTExpressionStatement extends IASTStatement {

	/**
	 * <code>EXPRESSION</code> is the relationship between an
	 * <code>IASTExpressionStatement</code> and an <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty EXPRESSION = new ASTNodeProperty(
			"IASTExpressionStatement.EXPRESSION - IASTExpression for IASTExpressionStatement"); //$NON-NLS-1$

	/**
	 * Get the expression in this statement.
	 * 
	 * @return the expression
	 */
	public IASTExpression getExpression();

	/**
	 * Set the expression statement.
	 * 
	 * @param expression
	 */
	public void setExpression(IASTExpression expression);
	
	/**
	 * @since 5.1
	 */
	@Override
	public IASTExpressionStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTExpressionStatement copy(CopyStyle style);
}
