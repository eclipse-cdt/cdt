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
 * @author Doug Schaefer
 */
public interface IASTScope {

	/**
	 * @return the first declaration in the scope.
	 */
	public IASTDeclaration getFirstDeclaration();
	
	public void setFirstDeclaration(IASTDeclaration declaration);

	/**
	 * @return the container scope of this scope
	 */
	public IASTScope getParentScope();
	
	public void setParentScope(IASTScope parentScope);

	/**
	 * This method searches this scope and then it's parent scope
	 * recursively to find the declaration with the given name.
	 * 
	 * @param name the name of the declaration
	 * @return the declaration for the given name
	 */
	public IASTDeclaration findDeclaration(IASTIdentifier name);
	
}
