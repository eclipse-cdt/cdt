/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
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
 * Represents a label statement.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTLabelStatement extends IASTStatement, IASTNameOwner {
	/** @since 6.0 */
	public static final IASTLabelStatement[] EMPTY_ARRAY = {};
	/**
	 * @since 5.4
	 * @deprecated use {@link #EMPTY_ARRAY} instead
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final IASTStatement[] EMPTY_LABEL_STATEMENT_ARRAY = {};

	public static final ASTNodeProperty NAME = new ASTNodeProperty(
			"IASTLabelStatement.NAME - name for IASTLabelStatement"); //$NON-NLS-1$
	public static final ASTNodeProperty NESTED_STATEMENT = new ASTNodeProperty(
			"IASTLabelStatement.NESTED_STATEMENT - statement for IASTLabelStatement"); //$NON-NLS-1$

	/**
	 * The name for the label. The name resolves to an ILabel binding.
	 *
	 * @return the name for the label
	 */
	public IASTName getName();

	/**
	 * Set the name for a label.
	 *
	 * @param name
	 */
	public void setName(IASTName name);

	/**
	 * Returns the statement following the label.
	 */
	public IASTStatement getNestedStatement();

	/**
	 * Sets the statement following the label.
	 *
	 * @param statement the statement to set
	 */
	public void setNestedStatement(IASTStatement statement);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTLabelStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTLabelStatement copy(CopyStyle style);
}
