/*******************************************************************************
 * Copyright (c) 2018, Institute for Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Felix Morgner - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;

/**
 * Internal interface to describe the ability of having a parameter list
 * @since 6.6
 */
public interface ICPPASTParameterListOwner {

	/**
	 * Add a parameter to the parameter list of the parameter list owner.
	 */
	public void addParameterDeclaration(IASTParameterDeclaration parameter);

	/**
	 * Gets the parameter declarations for the parameter list owner
	 */
	public IASTParameterDeclaration[] getParameters();

	/**
	 * Set whether or not the parameter list owner takes a variable number of
	 * arguments.
	 */
	public void setVarArgs(boolean value);

	/**
	 * Check if the parameter list owner takes a variable number of arguments.
	 */
	public boolean takesVarArgs();
}
