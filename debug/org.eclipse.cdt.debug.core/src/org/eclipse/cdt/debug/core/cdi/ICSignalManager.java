/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.core.cdi;

/**
 * 
 * The signal manager manages the collection of signals defined 
 * for the debug session.
 * 
 * @since Jul 9, 2002
 */
public interface ICSignalManager extends ICSessionObject
{
	/**
	 * Returns the array of signals defined for this session.
	 * 
	 * @return the array of signals
	 * @throws CDIException on failure. Reasons include:
	 */
	ICSignal[] getSignals() throws CDIException;
}
