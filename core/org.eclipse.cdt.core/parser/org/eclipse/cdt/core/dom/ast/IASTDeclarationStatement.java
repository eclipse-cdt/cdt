/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * A declaration statement that introduces a declaration.
 * 
 * @author Doug Schaefer
 */
public interface IASTDeclarationStatement extends IASTStatement {

	/**
	 * <code>DECLARATION</code> represents the relationship between a
	 * declaration statement and the declaration it wraps.
	 */
	public static final ASTNodeProperty DECLARATION = new ASTNodeProperty(
			"IASTDeclarationStatement.DECLARATION - Declaration for DeclarationStatement"); //$NON-NLS-1$

	/**
	 * Gets the declaration introduced by this statement.
	 * 
	 * @return the declaration
	 */
	public IASTDeclaration getDeclaration();

	/**
	 * Set the declaration for this statement.
	 * 
	 * @param declaration
	 */
	public void setDeclaration(IASTDeclaration declaration);

}
