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
 * This represents a function in the program. A function is also a scope
 * for other bindings.
 * 
 * @author Doug Schaefer
 */
public interface IFunction extends IBinding {

	/**
	 * This gets the parameters to the function which are IVariables.
	 * 
	 * @return List of IParameter
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
	 * 
	 * @return
	 * @throws DOMException if this is a problem binding
	 */
	public IFunctionType getType() throws DOMException;
	
	public boolean isStatic() throws DOMException;
}
