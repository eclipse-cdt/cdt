/**********************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
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
 * Represents the declaration of a variable.
 */
public interface IVariableDeclaration extends IDeclaration {
	/**
	 * Returns the variable declaration type name.
	 * @return String
	 * @throws CModelException
	 */
	public String getTypeName() throws CModelException;
	
	/**
	 * Sets the variable declaration type name.
	 * @param type
	 * @throws CModelException
	 */
	public void setTypeName(String type) throws CModelException;
}
