/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Ye ol' do statement.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTDoStatement extends IASTStatement {

	/**
	 * <code>BODY</code> represents the relationship between a
	 * <code>IASTDoStatement</code> and its nested body
	 * <code>IASTStatement</code>.
	 */
	public static final ASTNodeProperty BODY = new ASTNodeProperty("IASTDoStatement.BODY - nested body for IASTDoStatement"); //$NON-NLS-1$

	/**
	 * <code>CONDITION</code> represents the relationship between a
	 * <code>IASTDoStatement</code> and its condition
	 * <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty CONDITION = new ASTNodeProperty(
			"IASTDoStatement.CONDITION - IASTExpression condition for IASTDoStatement"); //$NON-NLS-1$

	/**
	 * Get the body of the loop.
	 * 
	 * @return <code>IASTStatement</code> loop code body
	 */
	public IASTStatement getBody();

	/**
	 * Set the body of the loop.
	 * 
	 * @param body
	 *            an <code>IASTStatement</code>
	 */
	public void setBody(IASTStatement body);

	/**
	 * The condition on the loop.
	 * 
	 * @return the expression for the condition
	 */
	public IASTExpression getCondition();

	/**
	 * Set the condition for the loop.
	 * 
	 * @param condition
	 *            an IASTExpression
	 */
	public void setCondition(IASTExpression condition);
	
	/**
	 * @since 5.1
	 */
	@Override
	public IASTDoStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTDoStatement copy(CopyStyle style);

}
