/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Anders Dahlberg (Ericsson) - bug 84144
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface is used to represent a unary expression in the AST.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTUnaryExpression extends IASTExpression {
	/**
	 * Prefix increment.
	 * {@code op_prefixIncr}: ++exp
	 */
	public static final int op_prefixIncr = 0;

	/**
	 * Prefix decrement.
	 * {@code op_prefixDecr}: --exp
	 */
	public static final int op_prefixDecr = 1;

	/**
	 * Operator plus.
	 * {@code op_plus}: +exp
	 */
	public static final int op_plus = 2;

	/**
	 * Operator minus.
	 * {@code op_minux}: -exp
	 */
	public static final int op_minus = 3;

	/**
	 *  Operator star.
	 *  {@code op_star}: *exp
	 */
	public static final int op_star = 4;

	/**
	 * Operator ampersand.
	 * {@code op_amper}: &exp
	 */
	public static final int op_amper = 5;

	/**
	 * Operator tilde.
	 * {@code op_tilde}: ~exp
	 */
	public static final int op_tilde = 6;

	/**
	 * not.
	 * {@code op_not}: !exp
	 */
	public static final int op_not = 7;

	/**
	 * sizeof.
	 * {@code op_sizeof}: sizeof exp
	 */
	public static final int op_sizeof = 8;

	/**
	 * Postfix increment.
	 * {@code op_postFixIncr}: exp++
	 */
	public static final int op_postFixIncr = 9;

	/**
	 * Postfix decrement.
	 * {@code op_postFixDecr}: exp--
	 */
	public static final int op_postFixDecr = 10;

	/**
	 * A bracketed expression.
	 * {@code op_bracketedPrimary}: ( exp )
	 */
	public static final int op_bracketedPrimary = 11;

	/**
	 * For C++, only. {@code op_throw}: throw exp
	 */
	public static final int op_throw = 12;

	/**
	 * For C++, only. {@code op_typeid}: typeid( exp )
	 */
	public static final int op_typeid = 13;

	/**
	 * @deprecated Shall not be used, 'typeof something' is not an expression, it's a declaration specifier.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int op_typeof = 14;

	/**
	 * For GCC parsers, only. {@code op_alignOf} is used for __alignOf( unaryExpression ) type
	 * expressions.
	 */
	public static final int op_alignOf = 15;

	/**
	 * For C++, only: 'sizeof... ( parameterPack )'
	 * @since 5.2
	 */
	public static final int op_sizeofParameterPack = 16;

	/**
	 * For C++, only: noexcept ( expression )
	 * @since 5.5
	 */
	public static final int op_noexcept = 17;

	/**
	 * For GCC parsers, only. {@code op_labelReference} is used for &&label type expressions.
	 * @since 5.8
	 */
	public static final int op_labelReference = 18;

	/**
	 * {@code OPERAND} represents the relationship between an {@code IASTUnaryExpression} and
	 * it's nested {@code IASTExpression}.
	 */
	public static final ASTNodeProperty OPERAND = new ASTNodeProperty(
			"IASTUnaryExpression.OPERAND - IASTExpression (operand) for IASTUnaryExpression"); //$NON-NLS-1$

	/**
	 * Returns the operator/kind.
	 *
	 * @return the operator, one of {@code op_*} constants defined in this interface.
	 */
	public int getOperator();

	/**
	 * Sets the operator/kind.
	 *
	 * @param operator the operator, one of {@code op_*} constants defined in this interface.
	 */
	public void setOperator(int operator);

	/**
	 * Returns the operand.
	 *
	 * @return {@code IASTExpression}
	 */
	public IASTExpression getOperand();

	/**
	 * Sets the operand.
	 *
	 * @param expression {@code IASTExpression}
	 */
	public void setOperand(IASTExpression expression);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTUnaryExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTUnaryExpression copy(CopyStyle style);
}
