/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;


/**
 * 
 */
public interface ICDIVariableDescriptor extends ICDIObject {

	/**
	 * Returns the name of this variable.
	 * 
	 * @return String the name of this variable
	 */
	String getName();
	
	/**
	 * Returns the type of this variable descriptor.
	 * 
	 * @return the type of data this variable is declared
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIType getType() throws CDIException;
	
	/**
	 * Returns the type name of this variable descriptor.
	 * 
	 * @return the type of data this variable is declared
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getTypeName() throws CDIException;

	/**
	 * Returns the size of this variable descriptor.
	 * 
	 * @return the size of this variable
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	int sizeof() throws CDIException;

	/**
	 * Returns the qualified name of this variable.
	 * 
	 * @return the qualified name of this variable
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getQualifiedName() throws CDIException;

	/**
	 * Consider the variable object as an Array of type and range[start, start + length - 1]
	 * @param stack
	 * @param name
	 * @return ICDIVariableDescriptor
	 * @throws CDIException
	 */
	ICDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws CDIException;

	/**
	 * Consider the variable descritor as type.
	 * 
	 * @param stack
	 * @param name
	 * @return ICDIVariableDescriptor
	 * @throws CDIException
	 */
	ICDIVariableDescriptor getVariableDescriptorAsType(String type) throws CDIException;

	/**
	 * Returns true if the variable Object are the same,
	 * For example event if the name is the same because of
	 * casting this may return false;
	 * @return true if the same
	 */
	boolean equals(ICDIVariableDescriptor varDesc);

}
