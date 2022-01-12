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
 *     John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;

/**
 * This interface represents a linkage specification. e.g. extern "C" { ... }
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTLinkageSpecification extends IASTDeclaration, IASTDeclarationListOwner {
	/**
	 * {@code OWNED_DECLARATION} is the owned declaration role for linkages.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"ICPPASTLinkageSpecification.OWNED_DECLARATION - Owned Declaration role for linkages"); //$NON-NLS-1$

	/**
	 * Returns the "literal" that represents the linkage.
	 *
	 * @return String
	 */
	public String getLiteral();

	/**
	 * Sets the "literal" that represents the linkage.
	 *
	 * @param value the "literal" that represents the linkage
	 */
	public void setLiteral(String value);

	/**
	 * Returns all of the declarations.
	 *
	 * @return {@code IASTDeclaration[]}
	 */
	public IASTDeclaration[] getDeclarations();

	/**
	 * Adds another declaration to the linkage.
	 *
	 * @param declaration the declaration to add
	 */
	@Override
	public void addDeclaration(IASTDeclaration declaration);

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTLinkageSpecification copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTLinkageSpecification copy(CopyStyle style);
}
