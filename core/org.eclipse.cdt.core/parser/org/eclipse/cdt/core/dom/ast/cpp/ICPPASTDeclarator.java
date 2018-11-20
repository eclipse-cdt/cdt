/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;

/**
 * Declarator for c++.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.2
 */
public interface ICPPASTDeclarator extends IASTDeclarator {
	/**
	 * Returns whether the declarator contains an ellipsis, in which case it declares
	 * a parameter pack.
	 */
	public boolean declaresParameterPack();

	/**
	 * Set whether the declarator contains an ellipsis, denoting a pack expansion.
	 * Not allowed on a frozen AST.
	 */
	public void setDeclaresParameterPack(boolean val);
}
