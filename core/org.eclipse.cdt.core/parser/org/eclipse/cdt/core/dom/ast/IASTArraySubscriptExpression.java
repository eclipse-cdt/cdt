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
 * This interface represents a postfix array subscript expression. x[ 10 ]
 * y.z()[ t * t ]
 * 
 * @author jcamelon
 */
public interface IASTArraySubscriptExpression extends IASTExpression {

	/**
	 * Node property that describes the relationship between an
	 * <code>IASTArraySubscriptExpression</code> and an
	 * <code>IASTExpression</code> representing the subscript.
	 */
	public static final ASTNodeProperty ARRAY = new ASTNodeProperty("IASTArraySubscriptExpression.ARRAY - IASTExpression representing the Array"); //$NON-NLS-1$

	/**
	 * Get the expression that represents the array.
	 * 
	 * @return <code>IASTExpression</code> that represents the array.
	 */
	public IASTExpression getArrayExpression();

	/**
	 * Set the expression that represents the array.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code> to be set.
	 */
	public void setArrayExpression(IASTExpression expression);

	/**
	 * Node property that describes the relationship between an
	 * <code>IASTArraySubscriptExpression</code> and an
	 * <code>IASTExpression</code> representing the array.
	 */
	public static final ASTNodeProperty SUBSCRIPT = new ASTNodeProperty(
			"IASTArraySubscriptExpression.SUBSCRIPT - IASTExpression representing the Subscript"); //$NON-NLS-1$

	/**
	 * Get the subscript expression.
	 * 
	 * @return <code>IASTExpression</code> that represents the subscript.
	 */
	public IASTExpression getSubscriptExpression();

	/**
	 * Set the subscript expression.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code> to be set.
	 */
	public void setSubscriptExpression(IASTExpression expression);

}
