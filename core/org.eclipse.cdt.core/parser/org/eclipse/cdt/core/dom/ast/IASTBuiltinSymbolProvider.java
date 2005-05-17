/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
 * This is used to IASTName implementations to determine if they are bound to a Built-in Symbol
 * provided by a Built-in Symbol Provider that implements this interface.
 * 
 * @author dsteffle
 */
public interface IASTBuiltinSymbolProvider {
	
	/**
	 * Returns all of the IBindings corresponding to the IASTBuiltinSymbolProvider.
	 * 
	 * @param symbol
	 * @return
	 */
	public IBinding[] getBuiltinBindings();
	
}
