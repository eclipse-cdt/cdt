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
 * @author Doug Schaefer
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

}
