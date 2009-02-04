/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTReturnStatement extends IASTStatement {

	/**
	 * <code>RETURNVALUE</code> represents the relationship between an
	 * <code>IASTReturnStatement</code> and it's nested
	 * <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty RETURNVALUE = new ASTNodeProperty(
			"IASTReturnValue.RETURNVALUE - IASTExpression (returnValue) for IASTReturnStatement"); //$NON-NLS-1$

	/**
	 * This is the optional return value for this function.
	 * 
	 * @return the return expression or null.
	 */
	public IASTExpression getReturnValue();

	/**
	 * Set the return value.
	 * 
	 * @param returnValue
	 *            <code>IASTExpression</code>
	 */
	public void setReturnValue(IASTExpression returnValue);

	/**
	 * @since 5.1
	 */
	public IASTReturnStatement copy();
}
