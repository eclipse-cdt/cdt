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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Braced initializer list, for example as in:
 * <pre> int a[]= {1,2,3}; </pre>
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTInitializerList extends IASTInitializer, IASTInitializerClause {
	public static final ASTNodeProperty NESTED_INITIALIZER = new ASTNodeProperty(
			"IASTInitializerList.NESTED_INITIALIZER [IASTInitializerClause]"); //$NON-NLS-1$

	/**
	 * Returns the size of the initializer list, including trivial initializers. This size may
	 * be larger than the length of the array returned by {@link #getInitializers()}.
	 * @since 5.2
	 */
	public int getSize();

	/**
	 * Returns the list of initializers. Depending on how the ast was created, this may omit
	 * trivial initializers in order to save memory.
	 * @since 5.2
	 */
	public IASTInitializerClause[] getClauses();

	/**
	 * Add an initializer clause to the initializer list. Depending on how the AST is created the
	 * initializer may be <code>null</code>. A <code>null</code> initializer will not be returned
	 * by {@link #getInitializers()}, however it contributes to the actual element count (#getSize()).
	 * @since 5.2
	 */
	public void addClause(IASTInitializerClause clause);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTInitializerList copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTInitializerList copy(CopyStyle style);

	/**
	 * @deprecated Replaced by {@link #getClauses()}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IASTInitializer[] getInitializers();

	/**
	 * @deprecated Replaced by {@link #addClause(IASTInitializerClause)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void addInitializer(IASTInitializer initializer);
}
