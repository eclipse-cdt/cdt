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

import java.util.List;

/**
 * This represents a function in the program. A function is also a scope
 * for other bindings.
 * 
 * @author Doug Schaefer
 */
public interface IFunction extends IBinding, IScope {

	/**
	 * This gets the parameters to the function which are IVariables.
	 * 
	 * @return List of IVariables
	 */
	public List getParameters();
	
}
