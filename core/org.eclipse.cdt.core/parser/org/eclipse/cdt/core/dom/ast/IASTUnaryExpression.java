/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface is used to represent a unary expression in the AST.
 * 
 * @author jcamelon
 */
public interface IASTUnaryExpression extends IASTExpression {

	/**
	 * Prefix increment.
	 * <code>op_prefixIncr</code> ++exp
	 */
	public static final int op_prefixIncr = 0;

	/**
	 * Prefix decrement.
	 * <code>op_prefixDecr</code> --exp
	 */
	public static final int op_prefixDecr = 1;

	/**
	 * Operator plus.
	 * <code>op_plus</code> ==> + exp
	 */
	public static final int op_plus = 2;

	/**
	 * Operator minus.
	 * <code>op_minux</code> ==> -exp
	 */
	public static final int op_minus = 3;

	/**
	 *  Operator star.
	 *  <code>op_star</code> ==> *exp
	 */
	public static final int op_star = 4;

	/**
	 * Operator ampersand.
	 * <code>op_amper</code> ==> &exp
	 */
	public static final int op_amper = 5;

	/**
	 * Operator tilde.
	 * <code>op_tilde</code> ==> ~exp
	 */
	public static final int op_tilde = 6;

	/**
	 * not.
	 * <code>op_not</code> ==> ! exp
	 */
	public static final int op_not = 7;

	/**
	 * sizeof.
	 * <code>op_sizeof</code> ==> sizeof exp  
	 */
	public static final int op_sizeof = 8;

	/**
	 * Postfix increment.
	 * <code>op_postFixIncr</code> ==> exp++
	 */
	public static final int op_postFixIncr = 9;

	/**
	 * Postfix decrement.
	 * <code>op_bracketedPrimary</code> ==> exp--
	 */
	public static final int op_postFixDecr = 10;

	/**
	 * A bracketed expression.
	 * <code>op_bracketedPrimary</code> ==> ( exp )
	 */
	public static final int op_bracketedPrimary = 11;

	/**
	 * <code>op_last</code> is made available for subclasses.
	 */
	public static final int op_last = op_bracketedPrimary;

	/**
	 * Get the operator/kind.
	 * 
	 * @return (int)
	 */
	public int getOperator();

	/**
	 * Set the operator/kind.
	 * 
	 * @param value (int) value
	 */
	public void setOperator(int value);

	/**
	 * <code>OPERAND</code> represents the relationship between an <code>IASTUnaryExpression</code> and
	 * it's nested <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty OPERAND = new ASTNodeProperty("IASTUnaryExpression.OPERAND - IASTExpression (operand) for IASTUnaryExpression"); //$NON-NLS-1$

	/**
	 * Get the operand.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getOperand();

	/**
	 * Set the operand.
	 * 
	 * @param expression <code>IASTExpression</code>
	 */
	public void setOperand(IASTExpression expression);

}
