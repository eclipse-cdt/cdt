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
 * Base interface for a declarator.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTDeclarator extends IASTNameOwner, IASTAttributeOwner {
	/**
	 * Constant - empty declarator array
	 */
	public static final IASTDeclarator[] EMPTY_DECLARATOR_ARRAY = {};

	/**
	 * <code>POINTER_OPERATOR</code> represents the relationship between an
	 * <code>IASTDeclarator</code> and an <code>IASTPointerOperator</code>.
	 */
	public static final ASTNodeProperty POINTER_OPERATOR = new ASTNodeProperty(
			"IASTDeclarator.POINTER_OPERATOR - IASTPointerOperator for IASTDeclarator"); //$NON-NLS-1$

	/**
	 * <code>INITIALIZER</code> represents the relationship between an
	 * <code>IASTDeclarator</code> and an <code>IASTInitializer</code>.
	 */
	public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty(
			"IASTDeclarator.INITIALIZER - IASTInitializer for IASTDeclarator"); //$NON-NLS-1$

	/**
	 * <code>NESTED_DECLARATOR</code> represents the relationship between an
	 * <code>IASTDeclarator</code> and a nested <code>IASTDeclarator</code>.
	 */
	public static final ASTNodeProperty NESTED_DECLARATOR = new ASTNodeProperty(
			"IASTDeclarator.NESTED_DECLARATOR - Nested IASTDeclarator"); //$NON-NLS-1$

	/**
	 * <code>DECLARATOR_NAME</code> represents the relationship between an
	 * <code>IASTDeclarator</code> and an <code>IASTName</code>.
	 */
	public static final ASTNodeProperty DECLARATOR_NAME = new ASTNodeProperty(
			"IASTDeclarator.DECLARATOR_NAME - IASTName for IASTDeclarator"); //$NON-NLS-1$

	/**
	 * This is the list of pointer operators applied to the type for the declarator.
	 *
	 * @return array of IASTPointerOperator
	 */
	public IASTPointerOperator[] getPointerOperators();

	/**
	 * Adds a pointer operator to the declarator.
	 *
	 * @param operator a <code>IASTPointerOperator</code> to be added.
	 */
	public void addPointerOperator(IASTPointerOperator operator);

	/**
	 * If the declarator is nested in parentheses, returns the declarator
	 * as found in those parentheses.
	 *
	 * @return the nested declarator or null
	 */
	public IASTDeclarator getNestedDeclarator();

	public void setNestedDeclarator(IASTDeclarator nested);

	/**
	 * Returns the name of the declarator. If this is an abstract
	 * declarator, this will return an empty name.
	 *
	 * @return the name of the declarator
	 */
	public IASTName getName();

	/**
	 * Sets the name of he declarator.
	 *
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);

	/**
	 * Returns the optional initializer for this declarator.
	 *
	 * @return the initializer expression or null
	 */
	public IASTInitializer getInitializer();

	/**
	 * Set the optional initializer.
	 *
	 * @param initializer
	 *            <code>IASTInitializer</code>
	 */
	public void setInitializer(IASTInitializer initializer);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTDeclarator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTDeclarator copy(CopyStyle style);
}
