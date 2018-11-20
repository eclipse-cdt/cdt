/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * This is the declarator for a K&R C Function.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTKnRFunctionDeclarator extends IASTFunctionDeclarator {
	/**
	 * <code>PARAMETER_NAME</code> refers to the names qualified in a K&R C
	 * function definition.
	 */
	public static final ASTNodeProperty PARAMETER_NAME = new ASTNodeProperty(
			"ICASTKnRFunctionDeclarator.PARAMETER_NAME - K&R Parameter Name"); //$NON-NLS-1$

	/**
	 * <code>FUNCTION_PARAMETER</code> represents the relationship between an
	 * K&R function declarator and the full parameter declarations.
	 */
	public static final ASTNodeProperty FUNCTION_PARAMETER = new ASTNodeProperty(
			"ICASTKnRFunctionDeclarator.FUNCTION_PARAMETER - Full K&R Parameter Declaration"); //$NON-NLS-1$

	/**
	 * Sets the parameter names. TODO - this should change to add
	 *
	 * @param names
	 *            <code>IASTName []</code>
	 */
	public void setParameterNames(IASTName[] names);

	/**
	 * Returns parameter names.
	 *
	 * @return <code>IASTName []</code>
	 */
	public IASTName[] getParameterNames();

	/**
	 * Sets the parameter lists.
	 *
	 * @param decls
	 *            TODO - replace w/zadd
	 */
	public void setParameterDeclarations(IASTDeclaration[] decls);

	/**
	 * Get parameters declarations.
	 *
	 * @return <code>IASTDeclaration []</code>
	 */
	public IASTDeclaration[] getParameterDeclarations();

	/**
	 * Map declarator to IASTName.
	 *
	 * @param name <code>IASTName</code>
	 */
	public IASTDeclarator getDeclaratorForParameterName(IASTName name);

	/**
	 * @since 5.1
	 */
	@Override
	public ICASTKnRFunctionDeclarator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICASTKnRFunctionDeclarator copy(CopyStyle style);
}
