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
 * Ye ol' do statement.
 * 
 * @author Doug Schaefer
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

}
