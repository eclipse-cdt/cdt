/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model.type;


/**
 * 
 * Represents the type of a variable.
 * 
 * @since Apr 15, 2003
 */
public interface ICDIType {

	/**
	 * Returns the name.
	 * 
	 * @return  the name of the data type
	 * @throws CDIException if this method fails.
	 */
	String getTypeName();

	/**
	 * Returns a more desciptive name.
	 * @return
	 */
	String getDetailTypeName();	
}
