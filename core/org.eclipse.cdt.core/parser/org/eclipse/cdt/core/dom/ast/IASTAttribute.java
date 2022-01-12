/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents a C++11 (ISO/IEC 14882:2011 7.6)
 * or a GCC attribute (http://gcc.gnu.org/onlinedocs/gcc/Attribute-Syntax.html).
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.4
 */
public interface IASTAttribute extends IASTNode {
	public static final IASTAttribute[] EMPTY_ATTRIBUTE_ARRAY = {};

	/**
	 * <code>ATTRIBUTE_ARGUMENT</code> represents the relationship between an
	 * <code>IASTAttribute</code> and an <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty ARGUMENT_CLAUSE = new ASTNodeProperty(
			"IASTAttribute.ARGUMENT_CLAUSE - IASTToken, argument clause for IASTAttribute"); //$NON-NLS-1$

	/**
	 * Returns the name of the attribute.
	 */
	public char[] getName();

	/**
	 * Returns arguments of this attribute, or {@code null} if the attribute doesn't have arguments.
	 */
	public IASTToken getArgumentClause();

	/**
	 * Sets the argument clause.
	 */
	public void setArgumentClause(IASTToken argumentClause);

	@Override
	public IASTAttribute copy();

	@Override
	public IASTAttribute copy(CopyStyle style);
}
