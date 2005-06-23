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
 * This represents a function in the program. A function is also a scope
 * for other bindings.
 * 
 * @author Doug Schaefer
 */
public interface IFunction extends IBinding {

	/**
	 * This gets the parameters to the function
	 * 
	 * @return array of IParameter
	 * @throws DOMException if this is a problem binding
	 */
	public IParameter [] getParameters() throws DOMException;
	
	/**
	 * Get the function scope
	 * 
	 * @return
	 * @throws DOMException if this is a problem binding
	 */
	public IScope getFunctionScope() throws DOMException;
	
	/**
	 * Get the IFunctionType for this function
	 * @return
	 * @throws DOMException if this is a problem binding
	 */
	public IFunctionType getType() throws DOMException;
	
	/**
	 * Does this function have the static storage-class specifier
	 * similarily for extern, auto, register
	 * @return
	 * @throws DOMException
	 */
	public boolean isStatic() throws DOMException;
	public boolean isExtern() throws DOMException;
	public boolean isAuto() throws DOMException;
	public boolean isRegister() throws DOMException;

	/**
	 * is this function inline
	 * @return
	 * @throws DOMException
	 */
	public boolean isInline() throws DOMException;
	
	/**
	 * Whether or not this function takes variable arguments
	 * @return
	 * @throws DOMException
	 */
	public boolean takesVarArgs()throws DOMException;
}
