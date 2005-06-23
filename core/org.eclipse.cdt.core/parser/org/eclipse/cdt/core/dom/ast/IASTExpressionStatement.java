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
 * Expression statement.
 * 
 * @author Doug Schaefer
 */
public interface IASTExpressionStatement extends IASTStatement {

	/**
	 * <code>EXPRESSION</code> is the relationship between an
	 * <code>IASTExpressionStatement</code> and an <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty EXPFRESSION = new ASTNodeProperty(
			"IASTExpressionStatement.IASTStatement - IASTExpression for IASTExpressionStatement"); //$NON-NLS-1$

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
}
