/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.CoreException;

/**
 * 
 * A breakpoint that suspend execution when a function is entered.
 * 
 * @since Aug 21, 2002
 */
public interface ICFunctionBreakpoint extends ICLineBreakpoint
{
	/**
	 * Returns the function this breakpoint suspends execution in.
	 * 
	 * @return the function this breakpoint suspends execution in
	 * @exception CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public String getFunction() throws CoreException;

	/**
	 * Sets the function this breakpoint suspends execution in.
	 * 
	 * @param function the function this breakpoint suspends execution in
	 * @exception CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public void setFunction( String function ) throws CoreException;
}
