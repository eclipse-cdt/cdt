/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to resume a debug target at the given line.
 * 
 * @since: Feb 5, 2003
 */
public interface IJumpToLine
{
	/**
	 * Returns whether this operation is currently available for this element.
	 *
	 * @return whether this operation is currently available
	 */
	public boolean canJumpToLine( IResource resource, int lineNumber );

	/**
	 * Causes this element to resume the execution at the specified line.
	 *
	 * @exception DebugException on failure. Reasons include:
	 */
	public void jumpToLine( IResource resource, int lineNumber ) throws DebugException;
}
