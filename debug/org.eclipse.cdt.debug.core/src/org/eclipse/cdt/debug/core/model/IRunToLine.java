/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.resources.IFile;
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
	 * Returns whether this operation is currently available for this file and line number.
	 *
	 * @return whether this operation is currently available
	 */
	public boolean canRunToLine( IFile file, int lineNumber );

	/**
	 * Causes this element to run to specified location.
	 *
	 * @exception DebugException on failure. Reasons include:
	 */
	public void runToLine( IFile file, int lineNumber ) throws DebugException;

	/**
	 * Returns whether this operation is currently available for this file and line number.
	 *
	 * @return whether this operation is currently available
	 */
	public boolean canRunToLine( String fileName, int lineNumber );

	/**
	 * Causes this element to run to specified location.
	 *
	 * @exception DebugException on failure. Reasons include:
	 */
	public void runToLine( String fileName, int lineNumber ) throws DebugException;
}
