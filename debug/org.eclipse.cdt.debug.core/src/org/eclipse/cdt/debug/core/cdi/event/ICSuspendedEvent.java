/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.ICSessionObject;
import org.eclipse.cdt.debug.core.cdi.model.ICStackFrame;

/**
 * 
 * Notifies that the originator has been suspended. 
 * The originators:
 * <ul>
 * <li>target (ICTarget)
 * <li>thread (ICThread)
 * </ul>
 * The reason of the suspension can be one of the following session 
 * objects:
 * <ul>
 * <li>breakpoint (ICBreakpoint)
 * <li>signal (ICSignal)
 * <li>end of the stepping range (ICEndSteppingRange)
 * </ul>
 * 
 * @since Jul 10, 2002
 */
public interface ICSuspendedEvent extends ICEvent
{
	/**
	 * Returns the session object that caused the suspension.
	 * 
	 * @return ICObject
	 */
	ICSessionObject getReason();
	
	/**
	 * Returns the current stack frame.
	 * 
	 * @return the current stack frame
	 */
	ICStackFrame getStackFrame();
}
