/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents a GCC attribute (http://gcc.gnu.org/onlinedocs/gcc/Attribute-Syntax.html).
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.4
 */
public interface IASTGCCAttribute extends IASTNode, IASTNameOwner {
	public static final IASTGCCAttribute[] EMPTY_ATTRIBUTE_ARRAY = {};

	/**
	 * <code>ATTRIBUTE_NAME</code> represents the relationship between an
	 * <code>IASTAttribute</code> and an <code>IASTName</code>.
	 */
	public static final ASTNodeProperty ATTRIBUTE_NAME = new ASTNodeProperty(
			"IASTGCCAttribute.ATTRIBUTE_NAME - IASTName, name for IASTGCCAttribute"); //$NON-NLS-1$

	/**
	 * <code>ATTRIBUTE_ARGUMENT</code> represents the relationship between an
	 * <code>IASTAttribute</code> and an <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty ATTRIBUTE_ARGUMENT = new ASTNodeProperty(
			"IASTGCCAttribute.ATTRIBUTE_ARGUMENT - IASTExpression, argument for IASTGCCAttribute"); //$NON-NLS-1$

	/**
	 * Returns the name of the attribute.
	 * 
	 * @return the name of the attribute
	 */
	public IASTName getName();

	/**
	 * Sets the name of the attribute.
	 */
	public void setName(IASTName name);

	/**
	 * Returns arguments of this attribute.
	 * 
	 * @return the array of arguments
	 */
	public IASTExpression[] getArguments();

	/**
	 * Adds an argument to the attribute.
	 */
	public void addArgument(IASTExpression argument);

	@Override
	public IASTGCCAttribute copy();

	@Override
	public IASTGCCAttribute copy(CopyStyle style);
}
