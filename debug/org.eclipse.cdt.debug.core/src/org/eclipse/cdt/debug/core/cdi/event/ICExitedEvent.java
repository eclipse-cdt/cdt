/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.ICExitInfo;

/**
 * 
 * Notifies that the program has exited.
 * The originators:
 * <ul>
 * <li>target (ICTarget)
 * </ul>
 * 
 * @since Jul 10, 2002
 */
public interface ICExitedEvent extends ICEvent
{
	/**
	 * Returns the information provided by the session when program 
	 * is exited.
	 * 
	 * @return the exit information
	 */
	ICExitInfo getExitInfo();
}
