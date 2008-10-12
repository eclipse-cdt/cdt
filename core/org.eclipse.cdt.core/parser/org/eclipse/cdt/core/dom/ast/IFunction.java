/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;


/**
 * This represents a function in the program. A function is also a scope
 * for other bindings.
 * 
 * @author Doug Schaefer
 */
public interface IFunction extends IBinding {

	/**
	 * Returns the formal parameters of the function.
	 * 
	 * @return array of IParameter
	 * @throws DOMException if this is a problem binding.
	 */
	public IParameter[] getParameters() throws DOMException;
	
	/**
	 * Get the function scope
	 * 
	 * @throws DOMException if this is a problem binding.
	 */
	public IScope getFunctionScope() throws DOMException;
	
	/**
	 * Get the IFunctionType for this function
	 * @throws DOMException if this is a problem binding.
	 */
	public IFunctionType getType() throws DOMException;
	
	/**
	 * Returns {@code true} if the function has the static storage-class specifier
	 * similarly for extern, auto, register.
	 * @throws DOMException if this is a problem binding.
	 */
	public boolean isStatic() throws DOMException;
	public boolean isExtern() throws DOMException;
	public boolean isAuto() throws DOMException;
	public boolean isRegister() throws DOMException;

	/**
	 * Returns {@code true} if the function is inline.
	 * @throws DOMException if this is a problem binding.
	 */
	public boolean isInline() throws DOMException;
	
	/**
	 * Returns {@code true} if this function takes variable arguments.
	 * @throws DOMException if this is a problem binding.
	 */
	public boolean takesVarArgs() throws DOMException;
}
