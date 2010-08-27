/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Interface for all sorts of variables: local, parameter, global, field.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IVariable extends IBinding {

	/**
	 * Returns the type of the variable
	 */
	public IType getType() throws DOMException;
	
	/**
	 * Returns the value for a variable with an initializer,
	 * or <code>null</code> otherwise.
	 * @since 5.1
	 */
	public IValue getInitialValue();
	
	/**
	 * Returns whether this variable is declared static.
	 */
	public boolean isStatic();

	/**
	 * Returns whether this variable is declared extern.
	 */
	public boolean isExtern();
	
	/**
	 * Returns whether this variable is an automatic variable.
	 */
	public boolean isAuto();
	
	/**
	 * Returns whether this variable is declared register.
	 */
	public boolean isRegister();
}
