/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 * This is a function definition, i.e. it has a body.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTFunctionDefinition extends IASTDeclaration {
	/**
	 * {@code DECL_SPECIFIER} represents the relationship between a
	 * {@code IASTFunctionDefinition} and its
	 * {@code IASTDeclSpecifier}.
	 */
	public static final ASTNodeProperty DECL_SPECIFIER = new ASTNodeProperty(
			"IASTFunctionDefinition.DECL_SPECIFIER - IASTDeclSpecifier for IASTFunctionDefinition"); //$NON-NLS-1$

	/**
	 * {@code DECLARATOR} represents the relationship between a
	 * {@code IASTFunctionDefinition} and its
	 * {@code IASTFunctionDeclarator}.
	 */
	public static final ASTNodeProperty DECLARATOR = new ASTNodeProperty(
			"IASTFunctionDefinition.DECLARATOR - IASTFunctionDeclarator for IASTFunctionDefinition"); //$NON-NLS-1$

	/**
	 * {@code FUNCTION_BODY} represents the relationship between a
	 * {@code IASTFunctionDefinition} and its {@code IASTStatement}.
	 */
	public static final ASTNodeProperty FUNCTION_BODY = new ASTNodeProperty(
			"IASTFunctionDefinition.FUNCTION_BODY - Function Body for IASTFunctionDefinition"); //$NON-NLS-1$

	/**
	 * Returns the decl specifier for the function.
	 */
	public IASTDeclSpecifier getDeclSpecifier();

	/**
	 * Set the decl specifier for the function.
	 *
	 * @param declSpec
	 */
	public void setDeclSpecifier(IASTDeclSpecifier declSpec);

	/**
	 * Returns the function declarator of the function.
	 * Note, that the function declarator may contain nested declarators and may also nest within
	 * another declarator. In the latter case this function definition is always the parent of the
	 * outermost declarator.
	 * <pre>
	 * void (f)(int a); // has nested declarator
	 * void (f(int a)); // is nested in another declarator
	 * </pre>
	 */
	public IASTFunctionDeclarator getDeclarator();

	/**
	 * Sets the declarator for the function.
	 * Note, that the function declarator may contain nested declarators and may also nest within
	 * another declarator. In the latter case this function definition is set to be the parent of the
	 * outermost declarator.
	 * <pre>
	 * void (f)(int a); // has nested declarator
	 * void (f(int a)); // is nested in another declarator
	 * </pre>
	 *
	 * @param declarator
	 */
	public void setDeclarator(IASTFunctionDeclarator declarator);

	/**
	 * Returns the body of the function. This is usually a compound statement but
	 * C++ also has a function try block.
	 */
	public IASTStatement getBody();

	/**
	 * Sets the body of the function.
	 *
	 * @param statement
	 */
	public void setBody(IASTStatement statement);

	/**
	 * Get the logical IScope that the function definition body represents.
	 *
	 * @return {@code IScope} representing function body.
	 */
	public IScope getScope();

	/**
	 * @since 5.1
	 */
	@Override
	public IASTFunctionDefinition copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTFunctionDefinition copy(CopyStyle style);
}
