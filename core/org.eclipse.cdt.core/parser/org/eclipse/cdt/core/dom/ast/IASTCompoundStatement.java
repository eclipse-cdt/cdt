/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This represents a block of statements.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTCompoundStatement extends IASTStatement {
	/**
	 * <code>NESTED_STATEMENT</code> represents the relationship between an
	 * <code>IASTCompoundStatement</code> and its nested
	 * <code>IASTStatement</code>
	 */
	public static final ASTNodeProperty NESTED_STATEMENT = new ASTNodeProperty(
			"IASTCompoundStatement.NESTED_STATEMENT - nested IASTStatement for IASTCompoundStatement"); //$NON-NLS-1$

	/**
	 * Gets the statements in this block.
	 * 
	 * @return Array of IASTStatement
	 */
	public IASTStatement[] getStatements();

	/**
	 * Add a statement to the compound block.
	 * 
	 * @param statement
	 *            statement to be added
	 */
	public void addStatement(IASTStatement statement);

	/**
	 * Get <code>IScope</code> node that this node eludes to in the logical
	 * tree.
	 * 
	 * @return the <code>IScope</code>
	 */
	public IScope getScope();
	
	/**
	 * @since 5.1
	 */
	@Override
	public IASTCompoundStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTCompoundStatement copy(CopyStyle style);
}
