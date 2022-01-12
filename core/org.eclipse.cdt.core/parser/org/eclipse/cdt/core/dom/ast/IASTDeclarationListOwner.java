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
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * Common interface for parents of declaration lists.
 * @since 5.1
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTDeclarationListOwner extends IASTNode {
	/**
	 * Adds a declaration to the owner. May only be called as long as the ast is not frozen.
	 */
	void addDeclaration(IASTDeclaration declaration);

	/**
	 * Returns the array of declarations.
	 * @param includeInactive whether to include declarations from inactive code branches.
	 * @see ITranslationUnit#AST_PARSE_INACTIVE_CODE
	 * @since 5.1
	 */
	IASTDeclaration[] getDeclarations(boolean includeInactive);
}
