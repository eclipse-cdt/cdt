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
package org.eclipse.cdt.core.parser.ast2.c;

/**
 * @author Doug Schaefer
 */
public interface ICASTFunctionDefinition extends ICASTDeclaration {

	/**
	 * @return the decl-specifiers for the return type of this function
	 */
	public ICASTDeclSpecifier getReturnDeclSpecifiers();
	
	/**
	 * @return the declarator for this function
	 */
	public ICASTDeclarator getDeclarator();

	/**
	 * The body of a function is a compound statement in C. In C++ it
	 * can also be a function-try-block.
	 * 
	 * @return the statement that represents the body of the function
	 */
	public ICASTStatement getBody();
	
}
