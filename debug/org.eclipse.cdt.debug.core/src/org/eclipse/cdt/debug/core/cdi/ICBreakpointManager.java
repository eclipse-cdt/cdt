/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.core.cdi;

/**
 * 
 * The breakpoint manager manages the collection of breakpoints 
 * in the debug session.
 * 
 * @since Jul 9, 2002
 */
public interface ICBreakpointManager extends ICSessionObject
{
	/**
	 * Returns a collection of all breakpoints set for this session. 
	 * Returns an empty array if no breakpoints are set.
	 * 
	 * @return a collection of all breakpoints set for this session
	 * @throws CDIException on failure. Reasons include:
	 */
	ICBreakpoint[] getBreakpoints() throws CDIException;

	/**
	 * Deletes the given breakpoint.
	 * 
	 * @param breakpoint - a breakpoint to be deleted
	 * @throws CDIException on failure. Reasons include:
	 */
	void deleteBreakpoint( ICBreakpoint breakpoint ) throws CDIException;

	/**
	 * Deletes the given array of breakpoints.
	 * 
	 * @param breakpoints - the array of breakpoints to be deleted
	 * @throws CDIException on failure. Reasons include:
	 */
	void deleteBreakpoints( ICBreakpoint[] breakpoints ) throws CDIException;

	/**
	 * Deletes all breakpoints.
	 * 
	 * @throws CDIException on failure. Reasons include:
	 */
	void deleteAllBreakpoints() throws CDIException;

	/**
	 * Sets a breakpoint at the given location.
	 * 
	 * @param type - a combination of TEMPORARY and HARDWARE or 0
	 * @param location - the location 
	 * @param condition - the condition or <code>null</code>
	 * @param threadId - the thread identifier if this is 
	 * a thread-specific breakpoint or <code>null</code>
	 * @return a breakpoint
	 * @throws CDIException on failure. Reasons include:
	 */
	ICLocationBreakpoint setLocationBreakpoint( int type,
												ICLocation location,
												ICCondition condition,
												String threadId ) throws CDIException;
	
	/**
	 * Sets a watchpoint for the given expression.
	 * @param type - a combination of TEMPORARY and HARDWARE or 0
	 * @param watchType - a combination of READ and WRITE
	 * @param expression - the expression to watch
	 * @param condition - the condition or <code>null</code>
	 * @return a watchpoint
	 * @throws CDIException on failure. Reasons include:
	 */
	ICWatchpoint setWatchpoint( int type,
								int watchType,
								String expression,
								ICCondition condition ) throws CDIException;
	
	/**
	 * Sets a catchpoint for the given catch event.
	 * @param type - a combination of TEMPORARY and HARDWARE or 0
	 * @param event - the event to catch
	 * @param condition - the condition or <code>null</code>
	 * @return a catchpoint
	 * @throws CDIException on failure. Reasons include:
	 */
	ICCatchpoint setCatchpoint( int type,
								ICCatchEvent event,
								String expression,
								ICCondition condition ) throws CDIException;
}
