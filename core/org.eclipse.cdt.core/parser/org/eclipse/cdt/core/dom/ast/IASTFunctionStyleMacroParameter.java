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
 * This interface represents the name of a function style macro parameter.
 * This is not an IASTName, as there are not any bindings for 
 * 
 * @author jcamelon
 */
public interface IASTFunctionStyleMacroParameter extends IASTNode {

	/**
	 * Constant <code>EMPTY_PARAMETER_ARRAY</code> is used to return anempty array.
	 */
	public static final IASTFunctionStyleMacroParameter[] EMPTY_PARAMETER_ARRAY = new IASTFunctionStyleMacroParameter[0];

	/**
	 * Get the parameter name. 
	 * 
	 * @return String name
	 */
	public String getParameter();

	/**
	 * Set the parameter name.
	 * 
	 * @param value String
	 */
	public void setParameter(String value);

}
