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
 * This class represents a parameter declaration
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTParameterDeclaration extends IASTNode {
	/**
	 * Constant/sentinel.
	 */
	public static final IASTParameterDeclaration[] EMPTY_PARAMETERDECLARATION_ARRAY = {};

	/**
	 * <code>DECL_SPECIFIER</code> represents the relationship between an
	 * <code>IASTParameterDeclaration</code> and its nested
	 * <code>IASTDeclSpecifier</code>.
	 */
	public static final ASTNodeProperty DECL_SPECIFIER = new ASTNodeProperty(
			"IASTParameterDeclaration.DECL_SPECIFIER - IASTDeclSpecifier for IASTParameterDeclaration"); //$NON-NLS-1$

	/**
	 * <code>DECLARATOR</code> represents the relationship between an
	 * <code>IASTParameterDeclaration</code> and its nested
	 * <code>IASTDeclarator</code>.
	 */
	public static final ASTNodeProperty DECLARATOR = new ASTNodeProperty(
			"IASTParameterDeclaration.DECLARATOR - IASTDeclarator for IASTParameterDeclaration"); //$NON-NLS-1$

	/**
	 * Returns the decl specifier.
	 *
	 * @return <code>IASTDeclSpecifier</code>
	 */
	public IASTDeclSpecifier getDeclSpecifier();

	/**
	 * Returns the declarator.
	 *
	 * @return <code>IASTDeclarator</code>
	 */
	public IASTDeclarator getDeclarator();

	/**
	 * Sets the decl specifier.
	 *
	 * @param declSpec
	 *            <code>IASTDeclSpecifier</code>.
	 */
	public void setDeclSpecifier(IASTDeclSpecifier declSpec);

	/**
	 * Sets the declarator.
	 *
	 * @param declarator
	 *            <code>IASTDeclarator</code>
	 */
	public void setDeclarator(IASTDeclarator declarator);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTParameterDeclaration copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTParameterDeclaration copy(CopyStyle style);
}
