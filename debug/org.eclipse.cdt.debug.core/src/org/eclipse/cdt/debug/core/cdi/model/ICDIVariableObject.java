/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;


/**
 * 
 */
public interface ICDIVariableObject extends ICDIObject {

	/**
	 * Returns the name of this variable.
	 * 
	 * @return String the name of this variable
	 */
	String getName();
	
	/**
	 * Returns the stackframe where the variable was found
	 * may return null.
	 * 
	 * @return the stackframe
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIStackFrame getStackFrame() throws CDIException;

	/**
	 * Returns the type of data this variable is declared.
	 * 
	 * @return the type of data this variable is declared
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIType getType() throws CDIException;
	
	/**
	 * Returns the type of data this variable is declared.
	 * 
	 * @return the type of data this variable is declared
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getTypeName() throws CDIException;

	/**
	 * Returns the size of this variable.
	 * 
	 * @return the size of this variable
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	int sizeof() throws CDIException;

	/**
	 * Returns true if the value of this variable could be changed.
	 * 
	 * @return true if the value of this variable could be changed
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	boolean isEditable() throws CDIException;

	/**
	 * Returns the qualified name of this variable.
	 * 
	 * @return the qualified name of this variable
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getQualifiedName() throws CDIException;

	/**
	 * Returns true if the variable Object are the same,
	 * For example event if the name is the same because of
	 * casting this may return false;
	 * @return true if the same
	 */
	boolean equals(ICDIVariableObject varObject);

}
