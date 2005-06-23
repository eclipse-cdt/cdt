/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
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

/**
 * 
 * Represents a data structure in the program. Each variable has 
 * a value which may in turn contain more variables.
 * 
 * @since Jul 9, 2002
 */
public interface ICDIVariable extends ICDIVariableDescriptor {

	/**
	 * Returns true if the value of this variable could be changed.
	 * 
	 * @return true if the value of this variable could be changed
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	boolean isEditable() throws CDIException;

	/**
	 * Returns the value of this variable.
	 * 
	 * @return the value of this variable
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIValue getValue() throws CDIException;

	/**
	 * Attempts to set the value of this variable to the value of 
	 * the given expression.
	 * 
	 * @param expression - an expression to generate a new value
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void setValue(String expression) throws CDIException;

	/**
	 * Sets the value of this variable to the given value.
	 * 
	 * @param value - a new value
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void setValue(ICDIValue value) throws CDIException;
	
	/**
	 * Set the format of the variable.
	 * 
	 * @param format - @see ICDIFormat
	 * @throws CDIException if this method fails.
	 */
	void setFormat(int format) throws CDIException;

	/**
	 * Remove the variable from the manager list.
	 * 
	 * @param var
	 * @return ICDIArgument
	 * @throws CDIException
	 */
	void dispose() throws CDIException;

	boolean equals(ICDIVariable variable);
}
