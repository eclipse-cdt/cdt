/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @author jcamelon
 */
public interface IASTTypeId extends IASTNode {

	/**
	 * Constant.
	 */
	public static final IASTTypeId[] EMPTY_TYPEID_ARRAY = new IASTTypeId[0];

	/**
	 * <code>DECL_SPECIFIER</code> represents the relationship between an <code>IASTTypeId</code> and
	 * it's nested <code>IASTDeclSpecifier</code>.
	 */
	public static final ASTNodeProperty DECL_SPECIFIER = new ASTNodeProperty(
			"IASTTypeId.DECL_SPECIFIER - IASTDeclSpecifier for IASTTypeId"); //$NON-NLS-1$

	/**
	 * <code>ABSTRACT_DECLARATOR</code> represents the relationship between an <code>IASTTypeId</code> and
	 * it's nested <code>IASTDeclarator</code>.
	 */
	public static final ASTNodeProperty ABSTRACT_DECLARATOR = new ASTNodeProperty(
			"IASTTypeId.ABSTRACT_DECLARATOR - IASTDeclarator for IASTTypeId"); //$NON-NLS-1$

	/**
	 * Get the decl specifier.
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
	 * Set the abstract declarator.
	 * @param abstractDeclarator <code>IASTDeclarator</code>
	 */
	public void setAbstractDeclarator(IASTDeclarator abstractDeclarator);
}
