/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * 
 * Provides the ability to run a debug target to the given address.
 * 
 * @since Jan 13, 2003
 */
public interface IRunToAddress
{
	/**
	 * Returns whether this operation is currently available for this element.
	 *
	 * @return whether this operation is currently available
	 */
	public boolean canRunToAddress( long address );

	/**
	 * Causes this element to run to specified address.
	 *
	 * @exception DebugException on failure. Reasons include:
	 */
	public void runToAddress( long address ) throws DebugException;
}
