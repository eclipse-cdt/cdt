/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to resume a debug target at the given address.
 * 
 * @since: Feb 5, 2003
 */
public interface IJumpToAddress
{
	/**
	 * Returns whether this operation is currently available for this element.
	 *
	 * @return whether this operation is currently available
	 */
	public boolean canJumpToAddress( long address );

	/**
	 * Causes this element to resume the execution at the specified address.
	 *
	 * @exception DebugException on failure. Reasons include:
	 */
	public void jumpToAddress( long address ) throws DebugException;
}
