/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * This interface represents a namespace definition in C++.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTNamespaceDefinition
		extends IASTDeclaration, IASTNameOwner, IASTDeclarationListOwner, IASTAttributeOwner {
	/**
	 * {@code OWNED_DECLARATION} is the role served by all the nested declarations.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"ICPPASTNamespaceDefinition.OWNED_DECLARATION - Role served by nested declarations"); //$NON-NLS-1$

	/**
	 * {@code NAMESPACE_NAME} is the role served by the name in this interface.
	 */
	public static final ASTNodeProperty NAMESPACE_NAME = new ASTNodeProperty(
			"ICPPASTNamespaceDefinition.NAMESPACE_NAME - Role served by name"); //$NON-NLS-1$

	/**
	 * Returns the name of the namespace.
	 *
	 * @return {@code IASTName}
	 */
	public IASTName getName();

	/**
	 * Sets the name.
	 *
	 * @param name the name to be set
	 */
	public void setName(IASTName name);

	/**
	 * Specifies whether the namespace definition is inline.
	 * @since 5.3
	 */
	public void setIsInline(boolean isInline);

	/**
	 * Returns whether this namespace definition is inline.
	 * @since 5.3
	 */
	public boolean isInline();

	/**
	 * A namespace contains an ordered sequence of declarations.
	 *
	 * @return an array of declarations contained in the namespace
	 */
	public IASTDeclaration[] getDeclarations();

	/**
	 * Adds a declaration to the namespace.
	 *
	 * @param declaration {@code IASTDeclaration}
	 */
	@Override
	public void addDeclaration(IASTDeclaration declaration);

	/**
	 * Returns the scope object represented by this construct.
	 *
	 * @return {@code IScope}
	 */
	public IScope getScope();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTNamespaceDefinition copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTNamespaceDefinition copy(CopyStyle style);
}
