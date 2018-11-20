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
 *     Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * A composite type specifier represents a composite structure (contains declarations).
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTCompositeTypeSpecifier extends IASTDeclSpecifier, IASTNameOwner, IASTDeclarationListOwner {
	/**
	 * {@code k_struct} represents 'struct' in C and C++.
	 */
	public static final int k_struct = 1;

	/**
	 * {@code k_union} represents 'union' in C and C++.
	 */
	public static final int k_union = 2;

	/**
	 * {@code k_last} allows for subinterfaces to continue enumerating keys.
	 */
	public static final int k_last = k_union;

	/**
	 * {@code TYPE_NAME} represents the relationship between an
	 * {@code IASTCompositeTypeSpecifier} and its {@code IASTName}.
	 */
	public static final ASTNodeProperty TYPE_NAME = new ASTNodeProperty(
			"IASTCompositeTypeSpecifier.TYPE_NAME - IASTName for IASTCompositeTypeSpecifier"); //$NON-NLS-1$

	/**
	 * {@code MEMBER_DECLARATION} represents the relationship between an
	 * {@code IASTCompositeTypeSpecifier} and its nested{@code IASTDeclaration}s.
	 */
	public static final ASTNodeProperty MEMBER_DECLARATION = new ASTNodeProperty(
			"IASTCompositeTypeSpecifier.MEMBER_DECLARATION - Nested IASTDeclaration for IASTCompositeTypeSpecifier"); //$NON-NLS-1$

	/**
	 * Returns the type (key) of this composite specifier.
	 *
	 * @return key for this type
	 * @see #k_struct
	 * @see #k_union
	 */
	public int getKey();

	/**
	 * Sets the type (key) of this composite specifier.
	 *
	 * @param key
	 * @see #k_struct
	 * @see #k_union
	 */
	public void setKey(int key);

	/**
	 * Returns the name for this composite type. If this is an anonymous type,
	 * this will return an empty name.
	 *
	 * @return the name of the type
	 */
	public IASTName getName();

	/**
	 * Sets the name for this composite type.
	 *
	 * @param name
	 */
	public void setName(IASTName name);

	/**
	 * Returns a list of member declarations.
	 *
	 * @return List of IASTDeclaration
	 */
	public IASTDeclaration[] getMembers();

	/**
	 * Adds a member declaration.
	 *
	 * @param declaration
	 */
	public void addMemberDeclaration(IASTDeclaration declaration);

	/**
	 * Returns the scope that this interface eludes to in the logical tree.
	 */
	public IScope getScope();

	/**
	 * @since 5.1
	 */
	@Override
	public IASTCompositeTypeSpecifier copy();
}
