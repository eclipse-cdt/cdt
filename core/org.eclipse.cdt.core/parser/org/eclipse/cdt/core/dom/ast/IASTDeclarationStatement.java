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
 * A declaration statement that introduces a declaration.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTDeclarationStatement extends IASTStatement {
	/**
	 * <code>DECLARATION</code> represents the relationship between a
	 * declaration statement and the declaration it wraps.
	 */
	public static final ASTNodeProperty DECLARATION = new ASTNodeProperty(
			"IASTDeclarationStatement.DECLARATION - Declaration for DeclarationStatement"); //$NON-NLS-1$

	/**
	 * Gets the declaration introduced by this statement.
	 *
	 * @return the declaration
	 */
	public IASTDeclaration getDeclaration();

	/**
	 * Set the declaration for this statement.
	 *
	 * @param declaration
	 */
	public void setDeclaration(IASTDeclaration declaration);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTDeclarationStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTDeclarationStatement copy(CopyStyle style);
}
