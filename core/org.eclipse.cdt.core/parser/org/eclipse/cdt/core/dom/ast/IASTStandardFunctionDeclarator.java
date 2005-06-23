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
 * This is a declarator for a non K&R C function.
 * 
 * @author Doug Schaefer
 */
public interface IASTStandardFunctionDeclarator extends IASTFunctionDeclarator {

	/**
	 * <code>FUNCTION_PARAMETER</code> represents the relationship between an
	 * <code>IASTStandardFunctionDeclarator</code> and it's nested
	 * <code>IASTParameterDeclaration</code>.
	 */
	public final static ASTNodeProperty FUNCTION_PARAMETER = new ASTNodeProperty(
			"IASTStandardFunctionDeclarator.FUNCTION_PARAMETER - IASTParameterDeclaration for IASTStandardFunctionDeclarator"); //$NON-NLS-1$

	/**
	 * Gets the parameter declarations for the function
	 * 
	 * @return array of IASTParameterDeclaration
	 */
	public IASTParameterDeclaration[] getParameters();

	/**
	 * Add a parameter.
	 * 
	 * @param parameter
	 *            <code>IASTParameterDeclaration</code>
	 */
	public void addParameterDeclaration(IASTParameterDeclaration parameter);

	/**
	 * Does this function take a variable number of arguments?
	 * 
	 * @return boolean
	 */
	public boolean takesVarArgs();

	/**
	 * Set whether or not this function takes a variable number or arguments.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setVarArgs(boolean value);
}
