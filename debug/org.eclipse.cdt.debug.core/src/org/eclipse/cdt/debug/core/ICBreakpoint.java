/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * 
 * A breakpoint specific to the C/C++ debug model. A C/C++ breakpoint supports:
 * <ul>
 * <li>a condition</li>
 * <li>an ignore count</li>
 * <li>a thread filter to restrict a breakpoin to a specific thread</li>
 * <li>an installed property that indicates a breakpoint was successfully
 *  installed in debug target</li>
 * </ul>
 * 
 * @since Aug 21, 2002
 */
public interface ICBreakpoint extends IBreakpoint
{
	/**
	 * Returns whether this breakpoint is installed in at least
	 * one debug target.
	 * 
	 * @return whether this breakpoint is installed
	 * @exception CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public boolean isInstalled() throws CoreException;

	/**
	 * Returns the conditional expression associated with this breakpoint.
	 * 
	 * @return this breakpoint's conditional expression
	 * @exception CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public String getCondition() throws CoreException;

	/**
	 * Sets the condition associated with this breakpoint.
	 * 
	 * @param condition the conditional expression
	 * @exception CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public void setCondition( String condition ) throws CoreException;

	/**
	 * Returns the ignore count used by this breakpoint.
	 * 
	 * @return the ignore count used by this breakpoint
	 * @exception CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public int getIgnoreCount() throws CoreException;

	/**
	 * Sets the ignore count attribute for this breakpoint.
	 * 
	 * @param ignoreCount the new ignore count
	 * @exception CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public void setIgnoreCount( int ignoreCount ) throws CoreException;

	/**
	 * Returns the identifier of the thread this breakpoint is restricted in.
	 * 
	 * @return the thread identifier
	 * @exception CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public String getThreadId() throws CoreException;

	/**
	 * Restricts this breakpoint to suspend only in the given thread
	 * when encounterd in the given thread's target.
	 * 
	 * @param threadId the thread identifier
	 * @exception CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public void setThreadId( String threadId ) throws CoreException;
}
