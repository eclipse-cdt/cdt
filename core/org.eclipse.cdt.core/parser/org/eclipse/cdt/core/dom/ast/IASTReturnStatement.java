/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTReturnStatement extends IASTStatement {
	public static final ASTNodeProperty RETURNVALUE = new ASTNodeProperty(
			"IASTReturnValue.RETURNVALUE - [IASTInitializerClause]"); //$NON-NLS-1$

	/**
	 * This is the optional return value for this function.
	 *
	 * @return the return expression or {@code null}.
	 */
	public IASTExpression getReturnValue();

	/**
	 * Returns the return value as {@link IASTInitializerClause}, or {@code null}.
	 * In C++ this can be an braced initializer list.
	 * @since 5.2
	 */
	public IASTInitializerClause getReturnArgument();

	/**
	 * Not allowed on frozen AST.
	 * @since 5.2
	 */
	public void setReturnArgument(IASTInitializerClause returnValue);

	/**
	 * Not allowed on frozen AST.
	 */
	public void setReturnValue(IASTExpression returnValue);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTReturnStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTReturnStatement copy(CopyStyle style);
}
