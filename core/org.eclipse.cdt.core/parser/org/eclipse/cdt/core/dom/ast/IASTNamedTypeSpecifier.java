/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
 * Represents the use of a typedef name in an decl specifier in C. Also used for
 * class/struct/union names in C.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTNamedTypeSpecifier extends IASTDeclSpecifier, IASTNameOwner {

	/**
	 * <code>NAME</code> describes the relationship between an
	 * <code>IASTNamedTypeSpecifier</code> and its nested
	 * <code>IASTName</code>.
	 */
	public static final ASTNodeProperty NAME = new ASTNodeProperty("IASTNamedTypeSpecifier.NAME - IASTName for IASTNamedTypeSpecifier"); //$NON-NLS-1$

	/**
	 * Get the name.
	 * 
	 * @return the typedef name.
	 */
	public IASTName getName();

	/**
	 * Set the name.
	 * 
	 * @param name
	 */
	public void setName(IASTName name);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTNamedTypeSpecifier copy();
}
