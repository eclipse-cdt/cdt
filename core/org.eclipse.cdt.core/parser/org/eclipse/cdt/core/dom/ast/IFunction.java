/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;


/**
 * This represents a function in the program. A function is also a scope
 * for other bindings.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IFunction extends IBinding {

	/**
	 * Returns the formal parameters of the function.
	 */
	public IParameter[] getParameters();
	
	/**
	 * Get the function scope
	 */
	public IScope getFunctionScope();
	
	/**
	 * Get the IFunctionType for this function
	 */
	public IFunctionType getType();
	
	/**
	 * Returns {@code true} if the function has the static storage-class specifier
	 * similarly for extern, auto, register.
	 */
	public boolean isStatic();
	public boolean isExtern();
	public boolean isAuto();
	public boolean isRegister();

	/**
	 * Returns {@code true} if the function is inline.
	 */
	public boolean isInline();
	
	/**
	 * Returns {@code true} if this function takes variable arguments.
	 */
	public boolean takesVarArgs();
}
