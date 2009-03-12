/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;

/**
 * This interface represents a new expression.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTNewExpression extends IASTExpression, IASTImplicitNameOwner {

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
			"ICPPASTNewExpression.NEW_PLACEMENT - Location where memory should be allocated"); //$NON-NLS-1$

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
			"ICPPASTNewExpression.NEW_INITIALIZER - New Initializer"); //$NON-NLS-1$

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
	public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty("ICPPASTNewExpression.TYPE_ID - The type being 'newed'"); //$NON-NLS-1$

	/**
	 * Get the type Id. The type-id includes the optional array modifications.
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
	 * Returns whether the the typeID a new type ID, which is the case when
	 * the type-id is provided without parenthesis.
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
	 * Returns true if this expression is allocating an array.
	 * @since 5.1
	 */
	public boolean isArrayAllocation();
	
	
	/**
	 * Expressions that go inside array brackets.
	 */
	public static final ASTNodeProperty NEW_TYPEID_ARRAY_EXPRESSION = new ASTNodeProperty(
			"ICPPASTNewExpression.NEW_TYPEID_ARRAY_EXPRESSION - Expressions inside array brackets"); //$NON-NLS-1$

	/**
	 * @deprecated the id-expressions are part of the type-id.
	 */
	@Deprecated
	public IASTExpression[] getNewTypeIdArrayExpressions();

	/**
	 * @deprecated the id-expressions are part of the type-id
	 */
	@Deprecated
	public void addNewTypeIdArrayExpression(IASTExpression expression);
	
	
	/**
	 * @since 5.1
	 */
	public ICPPASTNewExpression copy();

}
