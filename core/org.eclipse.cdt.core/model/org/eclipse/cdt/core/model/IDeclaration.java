/**********************************************************************
Copyright (c) 2002, 2004 IBM Rational Software and others.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
 IBM Rational Software - Initial API and implementation
**********************************************************************/
package org.eclipse.cdt.core.model;

/**
 * IDeclaration is a base interface for any C Model element that could be
 * considered a declaration. 
 */
public interface IDeclaration extends ICElement, ISourceManipulation, ISourceReference {

	/**
	 * Checks if the declaration is static 
	 * Returns true if the declaration is static, false otherwise.
	 * @return boolean
	 * @throws CModelException
	 */
	boolean isStatic() throws CModelException;
	
	/**
	 * Checks if the declaration is constant.
	 * Returns true if the decalration is constant, false otherwise.
	 * @return boolean
	 * @throws CModelException
	 */
	boolean isConst() throws CModelException;
	
	/**
	 * Checks if the declaration is volatile.
	 * Returns true if the declaration is volatile, false otherwise.
	 * @return boolean
	 * @throws CModelException
	 */
	boolean isVolatile() throws CModelException;	
}
