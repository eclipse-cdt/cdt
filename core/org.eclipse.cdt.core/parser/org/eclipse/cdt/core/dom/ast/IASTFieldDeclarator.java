/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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
 * This represents a field in a struct. This allows for the specification of
 * size for a bit field.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTFieldDeclarator extends IASTDeclarator {
	/**
	 * <code>FIELD_SIZE</code> represents the relationship between a
	 * <code>IASTFieldDeclarator</code> and its <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty FIELD_SIZE = new ASTNodeProperty(
			"IASTFieldDeclarator.FIELD_SIZE - BitField Size of IASTFieldDeclarator"); //$NON-NLS-1$

	/**
	 * Returns the number of bits if this is a bit field, otherwise {@code null}.
	 *
	 * @return size of bit field or null.
	 */
	public IASTExpression getBitFieldSize();

	/**
	 * Sets the bitfield size.
	 *
	 * @param size
	 *            <code>IASTExpression</code>
	 */
	public void setBitFieldSize(IASTExpression size);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTFieldDeclarator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTFieldDeclarator copy(CopyStyle style);
}
