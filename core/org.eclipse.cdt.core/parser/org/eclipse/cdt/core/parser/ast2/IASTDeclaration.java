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
 * Introduces a type, function, or variable name into scope. This
 * interface is extended for each one of these.
 * 
 * @author Doug Schaefer
 */
public interface IASTDeclaration extends IASTNode {

	/**
	 * @return the scope for the declaration
	 */
	public IASTScope getScope();
	
	public void setScope(IASTScope scope);

	/**
	 * @return the identifier being introduced into the scope
	 */
	public IASTIdentifier getName();
	
	public void setName(IASTIdentifier name);

	/**
	 * @return the next declaration in this scope
	 */
	public IASTDeclaration getNextDeclaration();
	
	public void setNextDeclaration(IASTDeclaration next);

	/**
	 * @return is this a forward declaration, i.e., the thing
	 * it is declaring is not defined at the same point.
	 */
	public boolean isForward();
	
	public void setIsForward(boolean isForward);
	
}
