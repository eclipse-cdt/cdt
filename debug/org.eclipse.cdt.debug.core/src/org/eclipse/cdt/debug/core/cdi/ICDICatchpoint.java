/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

/**
 * 
 * Represents a catchpoint.
 * 
 * @since Jul 9, 2002
 */
public interface ICDICatchpoint extends ICDIBreakpoint
{
	/**
	 * Returns the catch event for this catchpoint.
	 * 
	 * @return the catch event for this catchpoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDICatchEvent getEvent() throws CDIException;
}
