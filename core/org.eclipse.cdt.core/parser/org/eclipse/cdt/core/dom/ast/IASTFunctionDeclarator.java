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
 * This is a declarator for a function.
 * 
 * @author Doug Schaefer
 */
public interface IASTFunctionDeclarator extends IASTDeclarator {

	/**
	 * Gets the parameter declarations for the function
	 * 
	 * @return List of IASTParameterDeclaration
	 */
	public List getParameters();
	
	// TODO handle varargs
	
}
