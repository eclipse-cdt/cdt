/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * A declaration statement that introduces a declaration.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
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
	
	/**
	 * @since 5.1
	 */
	@Override
	public IASTDeclarationStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTDeclarationStatement copy(CopyStyle style);

}
