/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * This interface represents a new expression.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTNewExpression extends IASTExpression, IASTImplicitNameOwner {

	public static final ASTNodeProperty NEW_PLACEMENT = new ASTNodeProperty(
			"ICPPASTNewExpression.NEW_PLACEMENT [IASTExpression]"); //$NON-NLS-1$

	public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty(
			"ICPPASTNewExpression.TYPE_ID - [IASTTypeId]"); //$NON-NLS-1$

	public static final ASTNodeProperty NEW_INITIALIZER = new ASTNodeProperty(
		"ICPPASTNewExpression.NEW_INITIALIZER - [IASTInitializer]"); //$NON-NLS-1$

	/**
	 * Is this a ::new expression?
	 */
	public boolean isGlobal();

	/**
	 * Returns true if this expression is allocating an array.
	 * @since 5.1
	 */
	public boolean isArrayAllocation();

	/**
	 * Returns the additional arguments for the new placement, or <code>null</code>.
	 * A placement argument can be of type {@link ICPPASTInitializerList}.
	 * @since 5.2
	 */
	public IASTInitializerClause[] getPlacementArguments();
	
	/**
	 * Get the type Id. The type-id includes the optional array modifications.
	 */
	public IASTTypeId getTypeId();

	/**
	 * Returns whether the the typeID a new type ID, which is the case when
	 * the type-id is provided without parenthesis.
	 */
	public boolean isNewTypeId();

	/**
	 * Returns the initializer or <code>null</code>.
	 * @since 5.2
	 */
	public IASTInitializer getInitializer();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTNewExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTNewExpression copy(CopyStyle style);

	
	/**
	 * Not allowed on frozen ast.
	 */
	public void setIsGlobal(boolean value);

	/**
	 * Not allowed on frozen ast.
	 * @since 5.2
	 */
	public void setPlacementArguments(IASTInitializerClause[] expression);

	/**
	 * Not allowed on frozen ast.
	 */
	public void setTypeId(IASTTypeId typeId);

	/**
	 * Not allowed on frozen ast.
	 */
	public void setIsNewTypeId(boolean value);

	/**
	 * Not allowed on frozen ast.
	 * @since 5.2
	 */
	public void setInitializer(IASTInitializer init);


	/**
	 * @deprecated the id-expressions are part of the type-id.
	 */
	@Deprecated
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
	 * @deprecated Replaced by {@link #getPlacementArguments()}
	 */
	@Deprecated
	public IASTExpression getNewPlacement();
	
	/**
	 * @deprecated Replaced by {@link #setPlacementArguments(IASTInitializerClause[])}
	 */
	@Deprecated
	public void setNewPlacement(IASTExpression expression);

	/**
	 * @deprecated Replaced by {@link #getInitializer()}
	 */
	@Deprecated
	public IASTExpression getNewInitializer();

	/**
	 * @deprecated Replaced by {@link #setInitializer(IASTInitializer)}
	 */
	@Deprecated
	public void setNewInitializer(IASTExpression expression);
}
