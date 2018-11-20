/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;

/**
 * Models a function defined with a try block, which is a function definition:
 * <pre> void func() try {
 * } catch (...) {
 * }
 * @since 5.1
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTFunctionWithTryBlock extends ICPPASTFunctionDefinition {
	/**
	 * A <code>CATCH_HANDLER</code> is the role of an ICPPASTCatchHandler in
	 * this interface.
	 */
	public static final ASTNodeProperty CATCH_HANDLER = new ASTNodeProperty(
			"ICPPASTFunctionWithTryBlock.CATCH_HANDLER - role of an ICPPASTCatchHandler"); //$NON-NLS-1$

	/**
	 * Adds a catch handler.
	 */
	public void addCatchHandler(ICPPASTCatchHandler statement);

	/**
	 * Returns an array of catch handlers.
	 */
	public ICPPASTCatchHandler[] getCatchHandlers();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTFunctionWithTryBlock copy();
}
