/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;

/**
 * 
 * Represents an information provided by the session when the program 
 * stopped by a breakpoint.
 * 
 * @since Aug 27, 2002
 */
public interface ICDIBreakpointHit extends ICDISessionObject
{
	/**
	 * Returns the breakpoint that stopped the program.
	 * 
	 * @return the breakpoint that stopped the program
	 */
	ICDIBreakpoint getBreakpoint();
}
