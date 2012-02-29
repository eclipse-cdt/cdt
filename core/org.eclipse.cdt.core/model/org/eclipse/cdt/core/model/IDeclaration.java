/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * Base interface for any C Model element that could be considered a declaration.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
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
	 * Returns true if the declaration is constant, false otherwise.
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
