/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugException;

/**
 * 
 * Provides the ability to run a debug target to the given line.
 * 
 * @since Sep 19, 2002
 */
public interface IRunToLine
{
	/**
	 * Returns whether this operation is currently available for this element.
	 *
	 * @return whether this operation is currently available
	 */
	public boolean canRunToLine( IResource resource, int lineNumber );

	/**
	 * Causes this element to run to specified location.
	 *
	 * @exception DebugException on failure. Reasons include:
	 */
	public void runToLine( IResource resource, int lineNumber ) throws DebugException;
}
