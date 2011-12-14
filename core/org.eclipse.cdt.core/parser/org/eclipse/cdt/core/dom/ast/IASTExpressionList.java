/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Expression List (Comma separated list of expressions).
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTExpressionList extends IASTExpression {

	/**
	 * <code>NESTED_EXPRESSION</code> describes the relationship between
	 * <code>IASTExpressionList</code> and the nested
	 * <code>IASTExpression</code>s.
	 */
	public static final ASTNodeProperty NESTED_EXPRESSION = new ASTNodeProperty(
			"IASTExpressionList.NESTED_EXPRESSION - Nested IASTExpression for IASTExpressionList"); //$NON-NLS-1$

	/**
	 * Get nested expressions.
	 * 
	 * @return <code>IASTExpression [] </code> nested expressions
	 */
	public IASTExpression[] getExpressions();

	/**
	 * Add nested expression.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code> value to be added.
	 */
	public void addExpression(IASTExpression expression);
	
	/**
	 * @since 5.1
	 */
	@Override
	public IASTExpressionList copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTExpressionList copy(CopyStyle style);
}
