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
package org.eclipse.cdt.core.parser.ast2;

/**
 * Represents a function definition. A function takes a number of
 * parameters and returns a (possibly void) value. Parameters are
 * always instances of IASTParameter and are declared with
 * IASTVariableDeclarations in the parameter list.
 * 
 * @author Doug Schaefer
 */
public interface IASTFunction extends IASTNode {

	/**
	 * @return the type of the return value of the function
	 */
	public IASTType getReturnType();

	/**
	 * @return the first parameter to the function
	 */
	public IASTDeclaration getFirstParameter();

	/**
	 * @return the first statement of the function
	 */
	public IASTStatement getBody();
	
}
