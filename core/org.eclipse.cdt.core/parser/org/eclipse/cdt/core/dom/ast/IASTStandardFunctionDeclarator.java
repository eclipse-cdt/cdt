/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
 * This is a declarator for a non K&R C function.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTStandardFunctionDeclarator extends IASTFunctionDeclarator {
	/**
	 * <code>FUNCTION_PARAMETER</code> represents the relationship between an
	 * <code>IASTStandardFunctionDeclarator</code> and it's nested
	 * <code>IASTParameterDeclaration</code>.
	 */
	public final static ASTNodeProperty FUNCTION_PARAMETER = new ASTNodeProperty(
			"IASTStandardFunctionDeclarator.FUNCTION_PARAMETER - IASTParameterDeclaration for IASTStandardFunctionDeclarator"); //$NON-NLS-1$

	/**
	 * Get the scope for this declarator. Returns <code>null</code>, if this declarator does not
	 * declare a function-prototype or function-definition.
	 * @since 5.1
	 */
	public IScope getFunctionScope();

	/**
	 * Gets the parameter declarations for the function
	 *
	 * @return array of IASTParameterDeclaration
	 */
	public IASTParameterDeclaration[] getParameters();

	/**
	 * Add a parameter.
	 *
	 * @param parameter
	 *            <code>IASTParameterDeclaration</code>
	 */
	public void addParameterDeclaration(IASTParameterDeclaration parameter);

	/**
	 * Does this function take a variable number of arguments?
	 *
	 * @return boolean
	 */
	public boolean takesVarArgs();

	/**
	 * Set whether or not this function takes a variable number or arguments.
	 *
	 * @param value
	 *            boolean
	 */
	public void setVarArgs(boolean value);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTStandardFunctionDeclarator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTStandardFunctionDeclarator copy(CopyStyle style);
}
