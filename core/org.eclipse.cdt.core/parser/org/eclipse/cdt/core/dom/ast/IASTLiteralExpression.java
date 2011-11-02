/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This expression represents a literal in the program.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IASTLiteralExpression extends IASTExpression {
	/**
	 * An integer literal e.g. 5
	 */
	public static final int lk_integer_constant = 0;

	/**
	 * A floating point literal e.g. 6.0
	 */
	public static final int lk_float_constant = 1;

	/**
	 * A char literal e.g. 'a'
	 */
	public static final int lk_char_constant = 2;

	/**
	 * A string literal e.g. "a literal"
	 */
	public static final int lk_string_literal = 3;

	/**
	 * A constant defined for subclasses to extend from.
	 * @deprecated all possible values must be defined in {@link IASTLiteralExpression}.
	 */
	@Deprecated
	public static final int lk_last = lk_string_literal;

	/**
	 * <code>lk_this</code> represents the 'this' keyword for  c++ only.
	 * @since 5.1
	 */
	public static final int lk_this = 4;

	/**
	 * <code>lk_true</code> represents the 'true' keyword.
	 * @since 5.1
	 */
	public static final int lk_true = 5;

	/**
	 * <code>lk_false</code> represents the 'false' keyword.
	 * @since 5.1
	 */
	public static final int lk_false = 6;

	/**
	 * Get the literal expression kind.
	 */
	public int getKind();

	/**
	 * Returns the value of the literal as char-array.
	 * @since 5.1
	 */
	public char[] getValue();

	/**
	 * Returns the value of the literal as string.
	 * @since 5.1
	 */
	@Override
	public String toString();
	
	/**
	 * Set the literal expression kind.
	 */
	public void setKind(int value);

	/**
	 * Provide the value for the expression.
	 * @since 5.1
	 */
	public void setValue(char[] value);
		
	/**
	 * @since 5.1
	 */
	@Override
	public IASTLiteralExpression copy();
	
	/**
	 * @since 5.3
	 */
	@Override
	public IASTLiteralExpression copy(CopyStyle style);

	/**
	 * @deprecated Replaced by {@link #setValue(char[])}.
	 */
	@Deprecated
	public void setValue(String value);
}
