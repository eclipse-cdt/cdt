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
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTTypeId extends IASTNode {
	public static final IASTTypeId[] EMPTY_TYPEID_ARRAY = {};

	/**
	 * <code>DECL_SPECIFIER</code> represents the relationship between an <code>IASTTypeId</code>
	 * and it's nested <code>IASTDeclSpecifier</code>.
	 */
	public static final ASTNodeProperty DECL_SPECIFIER = new ASTNodeProperty(
			"IASTTypeId.DECL_SPECIFIER - IASTDeclSpecifier for IASTTypeId"); //$NON-NLS-1$

	/**
	 * <code>ABSTRACT_DECLARATOR</code> represents the relationship between an <code>IASTTypeId</code>
	 * and it's nested <code>IASTDeclarator</code>.
	 */
	public static final ASTNodeProperty ABSTRACT_DECLARATOR = new ASTNodeProperty(
			"IASTTypeId.ABSTRACT_DECLARATOR - IASTDeclarator for IASTTypeId"); //$NON-NLS-1$

	/**
	 * Returns the decl specifier.
	 * @return <code>IASTDeclSpecifier</code>
	 */
	public IASTDeclSpecifier getDeclSpecifier();

	/**
	 * Set the decl specifier.
	 * @param declSpec <code>IASTDeclSpecifier</code>
	 */
	public void setDeclSpecifier(IASTDeclSpecifier declSpec);

	/**
	 * Get the abstract declarator.
	 *
	 * @return <code>IASTDeclarator</code>
	 */
	public IASTDeclarator getAbstractDeclarator();

	/**
	 * Sets the abstract declarator.
	 * @param abstractDeclarator <code>IASTDeclarator</code>
	 */
	public void setAbstractDeclarator(IASTDeclarator abstractDeclarator);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTTypeId copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTTypeId copy(CopyStyle style);
}
