/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Interface for nodes that can potentially be pack-expansions.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.2
 */
public interface ICPPASTPackExpandable {
	/**
	 * Returns whether this base specifier is a pack expansion.
	 * @since 5.2
	 */
	public boolean isPackExpansion();

	/**
	 * Sets whether this base specifier is a pack expansion. Not allowed on frozen AST.
	 * @since 5.2
	 */
	public void setIsPackExpansion(boolean val);
}
