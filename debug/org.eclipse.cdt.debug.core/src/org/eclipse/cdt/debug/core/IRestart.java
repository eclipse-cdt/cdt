/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;

import org.eclipse.debug.core.DebugException;
/**
 * Provides the ability to restart a debug target.
 * 
 */
public interface IRestart 
{
	/**
	 * Returns whether this element can currently be restarted.
	 *
	 * @return whether this element can currently be restarted
	 */
	public boolean canRestart();

	/**
	 * Causes this element to restart its execution.
	 *
	 * @exception DebugException on failure. Reasons include:
	 */
	public void restart() throws DebugException;
}

