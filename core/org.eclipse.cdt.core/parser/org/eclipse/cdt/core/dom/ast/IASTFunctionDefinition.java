/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is a function definition, i.e. it has a body.
 * 
 * @author Doug Schaefer
 */
public interface IASTFunctionDefinition extends IASTDeclaration {

	/**
	 * <code>DECL_SPECIFIER</code> represents the relationship between a
	 * <code>IASTFunctionDefinition</code> and its
	 * <code>IASTDeclSpecifier</code>.
	 */
	public static final ASTNodeProperty DECL_SPECIFIER = new ASTNodeProperty(
			"Decl Specifier"); //$NON-NLS-1$

	/**
	 * <code>DECLARATOR</code> represents the relationship between a
	 * <code>IASTFunctionDefinition</code> and its
	 * <code>IASTFunctionDeclarator</code>.
	 */
	public static final ASTNodeProperty DECLARATOR = new ASTNodeProperty(
			"Declarator"); //$NON-NLS-1$

	/**
	 * <code>FUNCTION_BODY</code> represents the relationship between a
	 * <code>IASTFunctionDefinition</code> and its <code>IASTStatement</code>.
	 */
	public static final ASTNodeProperty FUNCTION_BODY = new ASTNodeProperty(
			"Function Body"); //$NON-NLS-1$

	/**
	 * Get the decl specifier for the function.
	 * 
	 * @return
	 */
	public IASTDeclSpecifier getDeclSpecifier();

	/**
	 * Set the decl specifier for the function.
	 * 
	 * @param declSpec
	 */
	public void setDeclSpecifier(IASTDeclSpecifier declSpec);

	/**
	 * Get the declarator for the function.
	 * 
	 * @return
	 */
	public IASTFunctionDeclarator getDeclarator();

	/**
	 * Set the declarator for the function.
	 * 
	 * @param declarator
	 */
	public void setDeclarator(IASTFunctionDeclarator declarator);

	/**
	 * Get the body of the function. This is usually a compound statement but
	 * C++ also has a function try block.
	 * 
	 * @return
	 */
	public IASTStatement getBody();

	/**
	 * Set the body of the function.
	 * 
	 * @param statement
	 */
	public void setBody(IASTStatement statement);

	/**
	 * Get the logical IScope that the function definition body represents.
	 * 
	 * @return <code>IScope</code> representing function body.
	 */
	public IScope getScope();

}
