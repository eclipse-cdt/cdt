/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;

/**
 * 
 * Notifies that the program has exited.
 * The originators:
 * <ul>
 * <li>target (ICDITarget)
 * </ul>
 * 
 * @since Jul 10, 2002
 */
public interface ICDIExitedEvent extends ICDIDestroyedEvent {
	/**
	 * Returns the information provided by the session when program 
	 * is exited.
	 * 
	 * @return the exit information
	 */
	ICDISessionObject getReason();
}
