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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;

/**
 * Represents a potentially empty list of initializers in parenthesis: ( initializer-list? )
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTConstructorInitializer extends IASTInitializer {
	/**
	 * @since 5.2
	 */
	public static final ASTNodeProperty ARGUMENT = new ASTNodeProperty(
			"ICPPASTConstructorInitializer.ARGUMENT - [IASTInitializerClause]"); //$NON-NLS-1$

	/**
	 * Returns the arguments of this initializer, never <code>null</code>.
	 * An argument can be of type {@link IASTInitializerList}.
	 *
	 * @since 5.2
	 */
	public IASTInitializerClause[] getArguments();

	/**
	 * Not allowed on frozen ast.
	 * @since 5.2
	 */
	public void setArguments(IASTInitializerClause[] args);

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTConstructorInitializer copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTConstructorInitializer copy(CopyStyle style);

	/**
	 * @deprecated Replaced by {@link #getArguments()}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IASTExpression getExpression();

	/**
	 * @deprecated Replaced by {@link #setArguments(IASTInitializerClause[])}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void setExpression(IASTExpression expression);

	/**
	 * @deprecated Use {@link #ARGUMENT} instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final ASTNodeProperty EXPRESSION = ARGUMENT;
}
