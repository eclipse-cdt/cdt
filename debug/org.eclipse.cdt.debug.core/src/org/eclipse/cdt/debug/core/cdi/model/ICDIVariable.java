/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * 
 * Represents a data structure in the program. Each variable has 
 * a value which may in turn contain more variables.
 * 
 * @since Jul 9, 2002
 */
public interface ICDIVariable extends ICDIObject {
	/**
	 * Returns the name of this variable.
	 * 
	 * @return the name of this variable
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getName() throws CDIException;

	/**
	 * Returns the type of data this variable is declared.
	 * 
	 * @return the type of data this variable is declared
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getTypeName() throws CDIException;

	/**
	 * Returns the value of this variable.
	 * 
	 * @return the value of this variable
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIValue getValue() throws CDIException;

	/**
	 * Returns true if the value could be changed.
	 * @trhows CDIException if the method fails.
	 */
	boolean isEditable() throws CDIException;

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
}
