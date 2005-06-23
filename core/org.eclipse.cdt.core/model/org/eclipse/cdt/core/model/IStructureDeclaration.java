/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

/**
 * IStructureDeclaration
 */
public interface IStructureDeclaration extends IDeclaration {

	/**
	 * Checks if the structure is a Union
	 * @return boolean
	 * @throws CModelException
	 */
	public boolean isUnion() throws CModelException;

	/**
	 * Checks if the structure is a class
	 * @return boolean
	 * @throws CModelException
	 */
	public boolean isClass() throws CModelException;

	/**
	 * Checks if the structure is a struct
	 * @return boolean
	 * @throws CModelException
	 */
	public boolean isStruct() throws CModelException;

	/**
	 * Return "class", "struct", "union"
	 * @deprecated use isUnion(), isClass(), isStruct()
	 */
	String getTypeName() throws CModelException;
}
