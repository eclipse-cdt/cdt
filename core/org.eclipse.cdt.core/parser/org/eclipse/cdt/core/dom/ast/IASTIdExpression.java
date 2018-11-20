/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is a name used in an expression.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTIdExpression extends IASTExpression, IASTNameOwner {
	/**
	 * <code>ID_NAME</code> represents the relationship between an
	 * <code>IASTIdExpression</code> and a <code>IASTName</code>.
	 */
	public static final ASTNodeProperty ID_NAME = new ASTNodeProperty(
			"IASTIdExpression.ID_NAME - IASTName for IASTIdExpression"); //$NON-NLS-1$

	/**
	 * Returns the name used in the expression.
	 *
	 * @return the name
	 */
	public IASTName getName();

	/**
	 * Sets the name to be used in the expression.
	 *
	 * @param name
	 */
	public void setName(IASTName name);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTIdExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTIdExpression copy(CopyStyle style);
}
