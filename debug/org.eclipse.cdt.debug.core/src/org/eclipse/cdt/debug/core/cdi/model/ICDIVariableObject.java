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
	 * @return
	 */
	int sizeof() throws CDIException;
}
