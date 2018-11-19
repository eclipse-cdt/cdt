/*******************************************************************************
 * Copyright (c) 2002, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
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
	 * @deprecated use isUnion(), isClass(), isStruct()
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	String getTypeName() throws CModelException;
}
