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
 * This is a simple declaration which contains a sequence of declSpecifiers
 * followed by a list of declarators.
 * 
 * @author Doug Schaefer
 */
public interface IASTSimpleDeclaration extends IASTDeclaration, IASTNode {

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

}
