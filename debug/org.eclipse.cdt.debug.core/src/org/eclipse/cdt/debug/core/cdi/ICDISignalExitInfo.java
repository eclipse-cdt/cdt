/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi;

/**
 * Represents information provided by the session when the program exited.
 * 
 */
public interface ICDISignalExitInfo extends ICDISessionObject {

	/**
	 * Method getName.
	 * @return String
	 */
	String getName();
	
	/**
	 * Method getDescription.
	 * @return String
	 */
	String getDescription();

}
