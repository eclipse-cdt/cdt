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
 * This class represents a parameter declaration
 * 
 * @author Doug Schaefer
 */
public interface IASTParameterDeclaration extends IASTNode {
	/**
	 * Constant/sentinel.
	 */
	public static final IASTParameterDeclaration[] EMPTY_PARAMETERDECLARATION_ARRAY = new IASTParameterDeclaration[0];

	/**
	 * <code>DECL_SPECIFIER</code> represents the relationship between an
	 * <code>IASTParameterDeclaration</code> and its nested
	 * <code>IASTDeclSpecifier</code>.
	 */
	public static final ASTNodeProperty DECL_SPECIFIER = new ASTNodeProperty(
			"Decl Specifier"); //$NON-NLS-1$

	/**
	 * <code>DECLARATOR</code> represents the relationship between an
	 * <code>IASTParameterDeclaration</code> and its nested
	 * <code>IASTDeclarator</code>.
	 */
	public static final ASTNodeProperty DECLARATOR = new ASTNodeProperty(
			"Declarator"); //$NON-NLS-1$

	/**
	 * Get the decl specifier.
	 * 
	 * @return <code>IASTDeclSpecifier</code>
	 */
	public IASTDeclSpecifier getDeclSpecifier();

	/**
	 * Get the declarator.
	 * 
	 * @return <code>IASTDeclarator</code>
	 */
	public IASTDeclarator getDeclarator();

	/**
	 * Set the decl specifier.
	 * 
	 * @param declSpec
	 *            <code>IASTDeclSpecifier</code>.
	 */
	public void setDeclSpecifier(IASTDeclSpecifier declSpec);

	/**
	 * Set the declarator.
	 * 
	 * @param declarator
	 *            <code>IASTDeclarator</code>
	 */
	public void setDeclarator(IASTDeclarator declarator);

}
