/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTTypeIdExpression extends IASTExpression {
	/**
	 * <code>op_sizeof</code> sizeof (typeId) expression
	 */
	public static final int op_sizeof = 0;

	/**
	 * For c++, only.
	 */
	public static final int op_typeid = 1;

	/**
	 * For gnu-parsers, only.
	 * <code>op_alignOf</code> is used for __alignOf( typeId ) type expressions.
	 */
	public static final int op_alignof = 2;

	/**
	 * For gnu-parsers, only.
	 * <code>op_typeof</code> is used for typeof( typeId ) type expressions.
	 */
	public static final int op_typeof = 3;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_has_nothrow_assign = 4;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_has_nothrow_copy = 5;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_has_nothrow_constructor = 6;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_has_trivial_assign = 7;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_has_trivial_copy = 8;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_has_trivial_constructor = 9;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_has_trivial_destructor = 10;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_has_virtual_destructor = 11;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_is_abstract = 12;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_is_class = 13;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_is_empty = 14;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_is_enum = 15;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_is_pod = 16;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_is_polymorphic = 17;

	/**
	 * Built-in type trait of g++.
	 * @since 5.3
	 */
	public static final int op_is_union = 18;

	/**
	 * Built-in type trait of g++.
	 * @since 5.5
	 */
	public static final int op_is_literal_type = 19;

	/**
	 * Built-in type trait of g++.
	 * @since 5.5
	 */
	public static final int op_is_standard_layout = 20;

	/**
	 * Built-in type trait of g++.
	 * @since 5.5
	 */
	public static final int op_is_trivial = 21;

	/**
	 * <code>op_sizeofParameterPack</code> is used for sizeof... ( typeid ) expressions.
	 * @since 5.5
	 */
	public static final int op_sizeofParameterPack = 22;

	/**
	 * Built-in type trait of g++.
	 * @since 5.6
	 */
	public static final int op_is_final = 23;

	/**
	 * Built-in type trait of g++.
	 * @since 6.0
	 */
	public static final int op_is_trivially_copyable = 24;

	/**
	 * Returns the operator for the expression.
	 *
	 * @return int
	 */
	public int getOperator();

	/**
	 * Sets the operator for the expression.
	 * @param value int
	 */
	public void setOperator(int value);

	/**
	 * <code>TYPEID</code> represents the relationship between an <code>IASTTypeIdExpression</code> and
	 * it's nested <code>IASTTypeId</code>.
	 */
	public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty(
			"IASTTypeIdExpression.TYPE_ID - IASTTypeId for IASTTypeIdExpression"); //$NON-NLS-1$

	/**
	 * Set the type Id.
	 * @param typeId
	 */
	public void setTypeId(IASTTypeId typeId);

	/**
	 * Get the type Id.
	 */
	public IASTTypeId getTypeId();

	/**
	 * @since 5.1
	 */
	@Override
	public IASTTypeIdExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTTypeIdExpression copy(CopyStyle style);
}
