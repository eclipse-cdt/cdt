/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDICatchpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;

/**
 * 
 * The breakpoint manager manages the collection of breakpoints 
 * in the debug session.
 * Auto update is off by default. 
 * @since Jul 9, 2002
 */
public interface ICDIBreakpointManager extends ICDIManager {

	/**
	 * Returns a collection of all breakpoints set for this session. 
	 * Returns an empty array if no breakpoints are set.
	 * 
	 * @return a collection of all breakpoints set for this session
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIBreakpoint[] getBreakpoints() throws CDIException;

	/**
	 * Deletes the given breakpoint.
	 * 
	 * @param breakpoint - a breakpoint to be deleted
	 * @throws CDIException on failure. Reasons include:
	 */
	void deleteBreakpoint(ICDIBreakpoint breakpoint) throws CDIException;

	/**
	 * Deletes the given array of breakpoints.
	 * 
	 * @param breakpoints - the array of breakpoints to be deleted
	 * @throws CDIException on failure. Reasons include:
	 */
	void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException;

	/**
	 * Deletes all breakpoints.
	 * 
	 * @throws CDIException on failure. Reasons include:
	 */
	void deleteAllBreakpoints() throws CDIException;

	/**
	 * Sets a breakpoint at the given location.
	 * The breakpoint is set acording to the choices:
	 * <pre>
	 * if location.getFile() != null then
	 *    if location.getFunction() != null then
	 *       breakpoint = file:function
	 *    else
	 *       breakpoint = file:line
	 * else if (location.getFuntion() != null) then
	 *    breakpoint = function
	 * else if (location.getLineNumber() != 0 then
	 *    breakpoint = line
	 * else
	 *    breakpoint = address
	 * end
	 * </pre> 
	 * @see ICDIBreakpoint.TEMPORARY
	 * @see ICDIBreakpoint.HARDWARE
	 * @see ICDIBreakpoint.REGULAR
	 * 
	 * @param type - a combination of TEMPORARY and HARDWARE or REGULAR
	 * @param location - the location 
	 * @param condition - the condition or <code>null</code>
	 * @param threadId - the thread identifier if this is 
	 * a thread-specific breakpoint or <code>null</code>
	 * @param deferred - when set to <code>true</code>, if the breakpoint fails
	 * to be set, it is put a deferred list and the debugger will retry to set
	 * it when a new Shared library is loaded.
	 * @return a breakpoint
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDILocationBreakpoint setLocationBreakpoint(
		int type,
		ICDILocation location,
		ICDICondition condition,
		String threadId, boolean deferred)
		throws CDIException;

	/**
	 * Equivalent to :
	 * setLocationBreakpoint(type, location, condition, threadID, false);
	 * The breakpoint is not deferred.
	 * 
	 * @param type - a combination of TEMPORARY and HARDWARE or REGULAR
	 * @param location - the location 
	 * @param condition - the condition or <code>null</code>
	 * @param threadId - the thread identifier if this is 
	 * a thread-specific breakpoint or <code>null</code>
	 * @return a breakpoint
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDILocationBreakpoint setLocationBreakpoint(
		int type,
		ICDILocation location,
		ICDICondition condition,
		String threadId)
		throws CDIException;

	/**
	 * Sets a watchpoint for the given expression.
	 * @param type - a combination of TEMPORARY and HARDWARE or 0
	 * @param watchType - a combination of READ and WRITE
	 * @param expression - the expression to watch
	 * @param condition - the condition or <code>null</code>
	 * @return a watchpoint
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIWatchpoint setWatchpoint(
		int type,
		int watchType,
		String expression,
		ICDICondition condition)
		throws CDIException;

	/**
	 * Sets a catchpoint for the given catch event.
	 * @param type - a combination of TEMPORARY and HARDWARE or 0
	 * @param event - the event to catch
	 * @param condition - the condition or <code>null</code>
	 * @return a catchpoint
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDICatchpoint setCatchpoint(
		int type,
		ICDICatchEvent event,
		String expression,
		ICDICondition condition)
		throws CDIException;

	/**
	 * Allow the manager to interrupt the target
	 * if when setting the breakopoint the program was running.
	 */
	void allowProgramInterruption(boolean allow);

	/**
	 * Return a ICDICondition
	 */
	ICDICondition createCondition(int ignoreCount, String expression);

	/**
	 * Returns a ICDILocation
	 */
	ICDILocation createLocation(String file, String function, int line);

	/**
	 * Returns a ICDILocation
	 */
	ICDILocation createLocation(long address);

}
