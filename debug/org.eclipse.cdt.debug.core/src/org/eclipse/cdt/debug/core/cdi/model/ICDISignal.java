/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

/**
 * 
 * Represents a signal.
 * 
 * @since Jul 10, 2002
 */
public interface ICDISignal extends ICDIObject {

	/**
	 * Returns the name of this signal.
	 * 
	 * @return the name of this signal
	 */
	String getName();
	
	/**
	 * Returns the meaning of this signal.
	 * 
	 * @return the meaning of this signal
	 */
	String getDescription();

	/**
	 * if false means program will see the signal.
	 * Otherwise program does not know.
	 * 
	 * @return boolean
	 */
	boolean isIgnore();

	/**
	 * Means reenter debugger if this signal happens
	 * 
	 * Method  isStopSet.
	 * @return boolean
	 */
	boolean isStopSet();

	/**
	 * Continue program giving it this signal.
	 * 
	 * Method signal.
	 */
	void signal();
}
