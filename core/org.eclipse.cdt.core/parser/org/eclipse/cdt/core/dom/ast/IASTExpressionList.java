/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation */
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Expression List (Comma separated list of expressions).
 * 
 * @author jcamelon
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
}
