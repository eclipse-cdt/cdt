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
 * Represents the use of a typedef name in an decl specifier in C. Also used for
 * class/struct/union names in C.
 * 
 * @author Doug Schaefer
 */
public interface IASTNamedTypeSpecifier extends IASTDeclSpecifier, IASTNameOwner {

	/**
	 * <code>NAME</code> describes the relationship between an
	 * <code>IASTNamedTypeSpecifier</code> and its nested
	 * <code>IASTName</code>.
	 */
	public static final ASTNodeProperty NAME = new ASTNodeProperty("Name"); //$NON-NLS-1$

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

}
