/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Conditional Expression of the format X ? Y : Z
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTConditionalExpression extends IASTExpression {

	/**
	 * <code>LOGICAL_CONDITION</code> represents the relationship between an
	 * <code>IASTConditionalExpression</code> and its condition
	 * <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty LOGICAL_CONDITION = new ASTNodeProperty(
			"IASTConditionalExpression.LOGICAL_CONDITION - Logical Condition"); //$NON-NLS-1$

	/**
	 * <code>POSITIVE_RESULT</code> represents the relationship between an
	 * <code>IASTConditionalExpression</code> and its positive result
	 * <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty POSITIVE_RESULT = new ASTNodeProperty(
			"IASTConditionalExpression.POSITIVE_RESULT - Positive Result"); //$NON-NLS-1$

	/**
	 * <code>NEGATIVE_RESULT</code> represents the relationship between an
	 * <code>IASTConditionalExpression</code> and its positive result
	 * <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty NEGATIVE_RESULT = new ASTNodeProperty(
			"IASTConditionalExpression.NEGATIVE_RESULT - Negative Result"); //$NON-NLS-1$

	/**
	 * Get the logical condition expression.
	 * 
	 * @return <code>IASTExpression</code> representing the logical condition.
	 */

	public IASTExpression getLogicalConditionExpression();

	/**
	 * Set the logical condition expression.
	 * 
	 * @param expression
	 *            condition to be set
	 */
	public void setLogicalConditionExpression(IASTExpression expression);

	/**
	 * Get the positive result expression, or <code>null</code> in case the positive condition was omitted (this is
	 * a gcc extension).
	 * 
	 * @return <code>IASTExpression</code>, or <code>null</code>.
	 */
	public IASTExpression getPositiveResultExpression();

	/**
	 * Set positive result expression.
	 * 
	 * @param expression
	 */
	public void setPositiveResultExpression(IASTExpression expression);

	/**
	 * Get the negative result expression.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getNegativeResultExpression();

	/**
	 * Set negative result expression.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setNegativeResultExpression(IASTExpression expression);
	
	/**
	 * @since 5.1
	 */
	@Override
	public IASTConditionalExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTConditionalExpression copy(CopyStyle style);

}
