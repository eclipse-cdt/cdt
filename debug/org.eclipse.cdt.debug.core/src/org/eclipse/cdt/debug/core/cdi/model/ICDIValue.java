/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * 
 * Represents the value of a variable. A value representing 
 * a complex data structure contains variables.
 * 
 * @since Jul 9, 2002
 */
public interface ICDIValue extends ICDIObject {
	/**
	 * Returns a description of the type of data this value contains.
	 * 
	 * @return  the name of this value's data type
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getTypeName() throws CDIException;
	
	/**
	 * Returns this value as a <code>String</code>.
	 *
	 * @return a String representation of this value
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getValueString() throws CDIException;

	/**
	 * Return the number of children.
	 * @return int children count
	 */
	int getChildrenNumber() throws CDIException;
	
	/**
	 * @return true if value is a container like structure.
	 */
	boolean hasChildren() throws CDIException;
	
	/**
	 * Returns the variables in this value. An empty collection 
	 * is returned if there are no variables.
	 * 
	 * @return an array of variables
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIVariable[] getVariables() throws CDIException;	
}
