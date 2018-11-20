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
 * This interface represents expressions that access a field reference. e.g. a.b => a
 * is the expression, b is the field name. e.g. a()->def => a() is the
 * expression, def is the field name.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTFieldReference extends IASTExpression, IASTNameOwner {
	/**
	 * <code>FIELD_OWNER</code> represents the relationship between a
	 * <code>IASTFieldReference</code> and its <code>IASTExpression</code>
	 * field owner.
	 */
	public static final ASTNodeProperty FIELD_OWNER = new ASTNodeProperty(
			"IASTFieldReference.FIELD_OWNER - IASTFieldReference's Owner"); //$NON-NLS-1$

	/**
	 * <code>FIELD_NAME</code> represents the relationship between a
	 * <code>IASTFieldReference</code> and its <code>IASTName</code> field
	 * name.
	 */
	public static final ASTNodeProperty FIELD_NAME = new ASTNodeProperty(
			"IASTFieldReference.FIELD_NAME - IASTName for IASTFieldReference"); //$NON-NLS-1$

	/**
	 * Returns an expression for the object containing the field.
	 *
	 * @return the field owner
	 */
	public IASTExpression getFieldOwner();

	/**
	 * Sets the expression for the object containing the field.
	 *
	 * @param expression
	 */
	public void setFieldOwner(IASTExpression expression);

	/**
	 * Returns the name of the field being dereferenced.
	 *
	 * @return the name of the field (<code>IASTName</code>)
	 */
	public IASTName getFieldName();

	/**
	 * Sets the name of the field.
	 *
	 * @param name the new name
	 */
	public void setFieldName(IASTName name);

	/**
	 * Returns true of this is the arrow operator and not the dot operator.
	 *
	 * @return is this a pointer dereference
	 */
	public boolean isPointerDereference();

	/**
	 * Sets whether or not this is a pointer dereference (default == no).
	 *
	 * @param value the new value
	 */
	public void setIsPointerDereference(boolean value);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTFieldReference copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTFieldReference copy(CopyStyle style);
}
