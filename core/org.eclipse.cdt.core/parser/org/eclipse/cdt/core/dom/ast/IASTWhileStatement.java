/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Ye ol' while statement.
 * 
 * @author Doug Schaefer
 */
public interface IASTWhileStatement extends IASTStatement {

	/**
	 * <code>CONDITIONEXPRESSION</code> represents the relationship between an <code>IASTWhileStatement</code> and
	 * it's nested <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty CONDITIONEXPRESSION = new ASTNodeProperty(
			"IASTWhileStatement.CONDITIONEXPRESSION - IASTExpression (condition) for IASTWhileStatement"); //$NON-NLS-1$

	/**
	 * <code>BODY</code> represents the relationship between an <code>IASTWhileStatement</code> and
	 * it's nested <code>IASTStatement</code>.
	 */
	public static final ASTNodeProperty BODY = new ASTNodeProperty("IASTWhileStatement.BODY - IASTStatement (body) for IASTWhileStatement"); //$NON-NLS-1$

	/**
	 * Get the condition on the while loop
	 * 
	 * @return expression for the condition
	 */
	public IASTExpression getCondition();

	/**
	 * Set the condition of the while loop.
	 * 
	 * @param condition
	 */
	public void setCondition(IASTExpression condition);

	/**
	 * The body of the loop.
	 * 
	 * @return the body
	 */
	public IASTStatement getBody();

	/**
	 * Set the body of the while loop.
	 * 
	 * @param body
	 */
	public void setBody(IASTStatement body);

}
