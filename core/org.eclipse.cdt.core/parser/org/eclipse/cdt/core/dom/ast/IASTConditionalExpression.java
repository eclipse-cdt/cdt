/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast;

/**
 * Conditional Expression of the format X ? Y : Z
 * 
 * @author jcamelon
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
	 * Get the positive result expression.
	 * 
	 * @return <code>IASTExpression</code>
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

}
