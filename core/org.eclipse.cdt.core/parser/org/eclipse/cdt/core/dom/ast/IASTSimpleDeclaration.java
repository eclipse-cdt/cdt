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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is a simple declaration which contains a sequence of declSpecifiers
 * followed by a list of declarators.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTSimpleDeclaration extends IASTDeclaration, IASTAttributeOwner {
	/**
	 * <code>DECL_SPECIFIER</code> represents the relationship between an
	 * <code>IASTSimpleDeclaration</code> and it's nested
	 * <code>IASTDeclSpecifier</code>.
	 */
	public static final ASTNodeProperty DECL_SPECIFIER = new ASTNodeProperty(
			"IASTSimpleDeclaration.DECL_SPECIFIER - IASTDeclSpecifier for IASTSimpleDeclaration"); //$NON-NLS-1$

	/**
	 * <code>DECLARATOR</code> represents the relationship between an
	 * <code>IASTSimpleDeclaration</code> and it's nested
	 * <code>IASTDeclarator</code>s.
	 */
	public static final ASTNodeProperty DECLARATOR = new ASTNodeProperty(
			"IASTSimpleDeclaration.DECLARATOR - IASTDeclarator for IASTSimpleDeclaration"); //$NON-NLS-1$

	/**
	 * This returns the object representing the declSpecifiers for this
	 * declaration.
	 *
	 * @return the declSpecifier object
	 */
	public IASTDeclSpecifier getDeclSpecifier();

	/**
	 * Set the decl specifier.
	 *
	 * @param declSpec
	 *            <code>IASTDeclSpecifier</code>
	 */
	public void setDeclSpecifier(IASTDeclSpecifier declSpec);

	/**
	 * This returns the list of declarators in this declaration.
	 *
	 * @return <code>IASTDeclarator []</code>
	 */
	public IASTDeclarator[] getDeclarators();

	/**
	 * Add a declarator.
	 *
	 * @param declarator
	 *            <code>IASTDeclarator</code>
	 */
	public void addDeclarator(IASTDeclarator declarator);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTSimpleDeclaration copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTSimpleDeclaration copy(CopyStyle style);
}
