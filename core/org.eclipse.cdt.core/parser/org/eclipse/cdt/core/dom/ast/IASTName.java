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
 * This class represents a name in the program that represents a semantic
 * object in the program.
 * 
 * The toString method produces a string representation of the name as
 * appropriate for the language.
 * 
 * @author Doug Schaefer
 */
public interface IASTName extends IASTNode {
	
	/**
	 * Return the semantic object this name is referring to.
	 * 
	 * @return binding
	 */
	public IBinding resolveBinding();
	
	public char[] toCharArray();
}
