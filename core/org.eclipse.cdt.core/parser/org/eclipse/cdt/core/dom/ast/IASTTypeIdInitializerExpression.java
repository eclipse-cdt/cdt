/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;


/**
 * Compound literal: type-id { initializer }
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.1
 */
public interface IASTTypeIdInitializerExpression extends IASTExpression {

	/**
	 * <code>TYPE_ID</code> represents the relationship between an
	 * <code>IASTTypeIdInitializerExpression</code> and
	 * <code>IASTTypeId</code>.
	 */
	public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty("IASTTypeIdInitializerExpression.TYPE_ID - IASTTypeId for IASTTypeIdInitializerExpression"); //$NON-NLS-1$

	/**
	 * <code>INITIALIZER</code> represents the relationship between an
	 * <code>ICASTTypeIdInitializerExpression</code> and
	 * <code>IASTInitializer</code>.
	 */
	public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty(
			"IASTTypeIdInitializerExpression.INITIALIZER - IASTInitializer for IASTTypeIdInitializerExpression"); //$NON-NLS-1$

	/**
	 * Returns the type id of the compound literal.
	 */
	public IASTTypeId getTypeId();

	/**
	 * Sets the type id of the compound literal, must not be called on frozen ast.
	 */
	public void setTypeId(IASTTypeId typeId);

	/**
	 * Returns the initializer for the compound literal.
	 */
	public IASTInitializer getInitializer();

	/**
	 * Sets the initializer, must not be called on frozen ast.
	 */
	public void setInitializer(IASTInitializer initializer);
	
	@Override
	public IASTTypeIdInitializerExpression copy();
}
