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
 * This is a function definition, i.e. it has a body.
 * 
 * @author Doug Schaefer
 */
public interface IASTFunctionDefinition extends IASTDeclaration {

	/**
	 * The decl specifier for the function.
	 * 
	 * @return
	 */
	public IASTDeclSpecifier getDeclSpecifier();

	/**
	 * The declarator for the function.
	 * 
	 * @return
	 */
	public IASTFunctionDeclarator getDeclarator();
	
	/**
	 * This is the body of the function. This is usually a compound statement
	 * but C++ also has a function try block.
	 * 
	 * @return
	 */
	public IASTStatement getBody();
	
}
