/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to resume execution without giving a signal. 
 * This is useful when the program stopped on account of a signal and would 
 * ordinary see the signal when resumed.
 * 
 * @since: Feb 4, 2003
 */
public interface IResumeWithoutSignal
{
	/**
	 * Causes this element to resume its execution ignoring a signal.
	 * Has no effect on an element that is not suspended because of a signal.
	 * 
	 * @exception DebugException on failure. Reasons include:
	 */
	public void resumeWithoutSignal() throws DebugException;

	/**
	 * Returns whether this element can currently be resumed without signal.
	 *
	 * @return whether this element can currently be resumed without signal
	 */
	boolean canResumeWithoutSignal();
}
