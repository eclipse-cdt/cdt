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
 * The switch statement.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTSwitchStatement extends IASTStatement {

	/**
	 * <code>CONTROLLER_EXP</code> represents the relationship between an
	 * <code>IASTSwitchStatement</code> and it's nested
	 * <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty CONTROLLER_EXP = new ASTNodeProperty(
			"IASTSwitchStatement.CONTROLLER - IASTExpression (controller) for IASTSwitchExpression"); //$NON-NLS-1$

	/**
	 * <code>BODY</code> represents the relationship between an
	 * <code>IASTSwitchStatement</code> and it's nested
	 * <code>IASTStatement</code>.
	 */
	public static final ASTNodeProperty BODY = new ASTNodeProperty("IASTSwitchStatement.BODY - IASTStatment (body) for IASTSwitchStatement"); //$NON-NLS-1$

	/**
	 * This returns the expression which determines which case to take.
	 * 
	 * @return the controller expression
	 */
	public IASTExpression getControllerExpression();

	/**
	 * Set the controlling expression for the switch.
	 * 
	 * @param controller
	 *            <code>IASTExpression</code>
	 */
	public void setControllerExpression(IASTExpression controller);

	/**
	 * Returns the body of the switch statement.
	 * 
	 * TODO - finding the cases could be a logical thing
	 * 
	 * @return <code>IASTStatement</code>
	 */
	public IASTStatement getBody();

	/**
	 * Set the body for the switch statement.
	 * 
	 * @param body
	 *            <code>IASTStatement</code>
	 */
	public void setBody(IASTStatement body);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTSwitchStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTSwitchStatement copy(CopyStyle style);
}
