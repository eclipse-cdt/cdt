/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

/**
 * 
 * Represents a signal.
 * 
 * @since Jul 10, 2002
 */
public interface ICSignal extends ICSessionObject
{
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
	String getMeaning();
}
