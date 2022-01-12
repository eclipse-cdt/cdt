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
 * Represents a goto statement.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTGotoStatement extends IASTStatement, IASTNameOwner {
	public static final ASTNodeProperty NAME = new ASTNodeProperty(
			"IASTGotoStatement.NAME - Name for IASTGotoStatement"); //$NON-NLS-1$

	/**
	 * Returns the name of the label. The name resolves to a ILabel binding.
	 *
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * Set the name for a goto statement label.
	 *
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTGotoStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTGotoStatement copy(CopyStyle style);
}
