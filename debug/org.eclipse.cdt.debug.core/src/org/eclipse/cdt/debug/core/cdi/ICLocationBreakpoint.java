/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

/**
 * 
 * Represents a line, function or address breakpoint.
 * 
 * @since Jul 9, 2002
 */
public interface ICLocationBreakpoint extends ICBreakpoint
{
	/**
	 * Returns the location of this breakpoint.
	 * 
	 * @return the location of this breakpoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICLocation getLocation() throws CDIException;
}
