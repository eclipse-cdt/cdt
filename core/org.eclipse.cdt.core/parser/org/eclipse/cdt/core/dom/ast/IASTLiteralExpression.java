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
	 * An integer literal e.g. {@code 5}
	 */
	public static final int lk_integer_constant = 0;

	/**
	 * A floating point literal e.g. {@code 6.0}
	 */
	public static final int lk_float_constant = 1;

	/**
	 * A character literal e.g. {@code 'a'}
	 */
	public static final int lk_char_constant = 2;

	/**
	 * A string literal e.g. {@code "a literal"}
	 */
	public static final int lk_string_literal = 3;

	/**
	 * {@code lk_this} represents the '{@code this}' keyword for C++ only.
	 * @since 5.1
	 */
	public static final int lk_this = 4;

	/**
	 * {@code lk_true} represents the '{@code true}' keyword.
	 * @since 5.1
	 */
	public static final int lk_true = 5;

	/**
	 * {@code lk_false} represents the '{@code false}' keyword.
	 * @since 5.1
	 */
	public static final int lk_false = 6;

	/**
	 * {@code lk_nullptr} represents the '{@code nullptr}' keyword.
	 * @since 5.4
	 */
	public static final int lk_nullptr = 7;

	/**
	 * Returns the kind of the literal expression kind, which can be one of the {@code lk_*}
	 * constants defined above.
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
	 * Sets the kind of the literal expression.
	 */
	public void setKind(int value);

	/**
	 * Sets the value for the expression.
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
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void setValue(String value);
}
