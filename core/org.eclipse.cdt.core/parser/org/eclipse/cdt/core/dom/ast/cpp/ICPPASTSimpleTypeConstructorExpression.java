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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * Simple type constructor postfix expression.
 * 
 * @author jcamelon
 */
public interface ICPPASTSimpleTypeConstructorExpression extends IASTExpression {

	/**
	 * t_unspecified (error)
	 */
	public static final int t_unspecified = 0;

	/**
	 * t_void == void
	 */
	public static final int t_void = 1;

	/**
	 * t_char == char
	 */
	public static final int t_char = 2;

	/**
	 * t_int == int
	 */
	public static final int t_int = 3;

	/**
	 * t_float == float
	 */
	public static final int t_float = 4;

	/**
	 * t_double == double
	 */
	public static final int t_double = 5;

	/**
	 * t_bool = bool
	 */
	public static final int t_bool = 6;

	/**
	 * t_wchar_t = wchar_t
	 */
	public static final int t_wchar_t = 7;

	/**
	 * t_short = short
	 */
	public static final int t_short = 8;

	/**
	 * t_long = long
	 */
	public static final int t_long = 9;

	/**
	 * t_signed = signed
	 */
	public static final int t_signed = 10;

	/**
	 * t_unsigned = unsigned
	 */
	public static final int t_unsigned = 11;

	/**
	 * t_last is provided for subinterfaces.
	 */
	public static final int t_last = t_unsigned;

	/**
	 * Get the simple type.
	 * 
	 * @return int
	 */
	public int getSimpleType();

	/**
	 * Set the simple type.
	 * 
	 * @param value
	 *            int
	 */
	public void setSimpleType(int value);

	/**
	 * INITIALIZER_VALUE is the value passed into the constructor.
	 */
	public static final ASTNodeProperty INITIALIZER_VALUE = new ASTNodeProperty(
			"ICPPASTSimpleTypeConstructorExpression.INITIALIZER_VALUE - Value passed into constructor"); //$NON-NLS-1$

	/**
	 * Get the initial value.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getInitialValue();

	/**
	 * Set the initial value.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setInitialValue(IASTExpression expression);

}
