/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi;


/**
 * 
 * Represents a location in the debuggable program.
 * 
 * @since Jul 9, 2002
 */
public interface ICDILocation
{
	/**
	 * Returns the address of this location.
	 * 
	 * @return the address of this location
	 */
	long getAddress();
	
	/**
	 * Returns the source file of this location or <code>null</code>
	 * if the source file is unknown.
	 *  
	 * @return the source file of this location
	 */
	String getFile();

	/**
	 * Returns the function of this location or <code>null</code>
	 * if the function is unknown.
	 *  
	 * @return the function of this location
	 */
	String getFunction();

	/**
	 * Returns the line number of this location or <code>null</code>
	 * if the line number is unknown.
	 *  
	 * @return the line number of this location
	 */
	int getLineNumber();
	
	/**
	 * Return true if the both location refers to the same
	 * place.
	 */
	boolean equals(ICDILocation location);
}
