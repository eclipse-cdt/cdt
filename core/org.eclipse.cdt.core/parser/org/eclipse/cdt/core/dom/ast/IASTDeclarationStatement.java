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
 * A declaration statement that introduces a declaration.
 * 
 * @author Doug Schaefer
 */
public interface IASTDeclarationStatement extends IASTStatement {

	/**
	 * Gets the declaration introduced by this statement.
	 * 
	 * @return the declaration
	 */
	public IASTDeclaration getDeclaration();
	
	public void setDeclaration( IASTDeclaration declaration );
	
}
