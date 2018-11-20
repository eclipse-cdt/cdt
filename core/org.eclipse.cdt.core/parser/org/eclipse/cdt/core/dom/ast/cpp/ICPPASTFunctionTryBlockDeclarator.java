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
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;

/**
 * @deprecated, use {@link ICPPASTFunctionWithTryBlock}, instead.
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface ICPPASTFunctionTryBlockDeclarator extends ICPPASTFunctionDeclarator {
	/**
	 * A <code>CATCH_HANDLER</code> is the role of an ICPPASTCatchHandler in
	 * this interface.
	 */
	public static final ASTNodeProperty CATCH_HANDLER = new ASTNodeProperty(
			"ICPPASTFunctionTryBlockDeclarator.CATCH_HANDLER - role of an ICPPASTCatchHandler"); //$NON-NLS-1$

	/**
	 * Add a catch handler.
	 *
	 * @param statement
	 *            <code>ICPPASTCatchHandler</code>
	 */
	public void addCatchHandler(ICPPASTCatchHandler statement);

	/**
	 * Get catch handlers.
	 *
	 * @return <code>ICPPASTCatchHandler</code>
	 */
	public ICPPASTCatchHandler[] getCatchHandlers();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTFunctionTryBlockDeclarator copy();
}
