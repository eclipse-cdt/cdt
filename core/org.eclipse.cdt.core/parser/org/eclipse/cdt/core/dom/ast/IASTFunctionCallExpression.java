/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents a function call expression, f(x), where f is the function name expression
 * and x is the parameter expression.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTFunctionCallExpression extends IASTExpression {
	public static final ASTNodeProperty FUNCTION_NAME = new ASTNodeProperty(
			"IASTFunctionCallExpression.FUNCTION_NAME [IASTExpression]"); //$NON-NLS-1$

	/**
	 * @since 5.2
	 */
	public static final ASTNodeProperty ARGUMENT = new ASTNodeProperty(
			"IASTFunctionCallExpression.ARGUMENT [IASTInitializerClause]"); //$NON-NLS-1$

	/**
	 * Returns the function name expression.
	 */
	public IASTExpression getFunctionNameExpression();

	/**
	 * Returns the arguments for this function call, never {@code null}.
	 * @since 5.2
	 */
	public IASTInitializerClause[] getArguments();

	/**
	 * @since 5.1
	 */
	@Override
	public IASTFunctionCallExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTFunctionCallExpression copy(CopyStyle style);

	/**
	 * Sets the function name expression. Not allowed on frozen AST.
	 */
	public void setFunctionNameExpression(IASTExpression expression);

	/**
	 * Sets the arguments of the function call. Not allowed on frozen AST.
	 * @since 5.2
	 */
	public void setArguments(IASTInitializerClause[] args);

	@Deprecated
	public static final ASTNodeProperty PARAMETERS = ARGUMENT;

	/**
	 * @deprecated Replaced by {@link #setArguments(IASTInitializerClause[])}.
	 */
	@Deprecated
	public void setParameterExpression(IASTExpression expression);

	/**
	 * @deprecated Replaced by {@link #getArguments()}.
	 */
	@Deprecated
	public IASTExpression getParameterExpression();
}
