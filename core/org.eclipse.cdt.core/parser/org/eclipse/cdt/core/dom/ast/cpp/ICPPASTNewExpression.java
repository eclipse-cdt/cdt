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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * This interface represents a new expression.
 * 
 * @author jcamelon
 */
public interface ICPPASTNewExpression extends IASTExpression {

	/**
	 * Is this a ::new expression?
	 * 
	 * @return boolean
	 */
	public boolean isGlobal();

	/**
	 * Set this expression to bea global ::new expression (or not).
	 * 
	 * @param value
	 *            boolean
	 */
	public void setIsGlobal(boolean value);

	/**
	 * NEW_PLACEMENT is a role for an expression to represent the location of
	 * where the memory should be allocated.
	 */
	public static final ASTNodeProperty NEW_PLACEMENT = new ASTNodeProperty(
			"New Placement"); //$NON-NLS-1$

	/**
	 * Get the new placement (optional).
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getNewPlacement();

	/**
	 * Set the new placement expression.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setNewPlacement(IASTExpression expression);

	/**
	 * <code>NEW_INITIALIZER</code>
	 */
	public static final ASTNodeProperty NEW_INITIALIZER = new ASTNodeProperty(
			"New Initializer"); //$NON-NLS-1$

	/**
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getNewInitializer();

	/**
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setNewInitializer(IASTExpression expression);

	/**
	 * TYPE_ID is the type being 'newed'.
	 */
	public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty("Type Id"); //$NON-NLS-1$

	/**
	 * Get the type Id.
	 * 
	 * @return <code>IASTTypeId</code>
	 */
	public IASTTypeId getTypeId();

	/**
	 * Set the type Id.
	 * 
	 * @param typeId
	 *            <code>IASTTypeId</code>
	 */
	public void setTypeId(IASTTypeId typeId);

	/**
	 * Is the typeID a new type ID?
	 * 
	 * @return boolean
	 */
	public boolean isNewTypeId();

	/**
	 * Set the type ID to be a new type ID.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setIsNewTypeId(boolean value);

	/**
	 * Expressions that go inside array brackets.
	 */
	public static final ASTNodeProperty NEW_TYPEID_ARRAY_EXPRESSION = new ASTNodeProperty(
			"Array Size Expression"); //$NON-NLS-1$

	/**
	 * Get the new array size expressions.
	 * 
	 * @return <code>IASTExpression []</code>
	 */
	public IASTExpression[] getNewTypeIdArrayExpressions();

	/**
	 * Add another array size expression.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void addNewTypeIdArrayExpression(IASTExpression expression);

}
