/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

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
	 * Breakpoint attribute storing the function this breakpoint suspends 
	 * execution at (value <code>"org.eclipse.cdt.debug.core.function"</code>).
	 * This attribute is a <code>String</code>.
	 */
	public static final String FUNCTION = "org.eclipse.cdt.debug.core.function"; //$NON-NLS-1$	

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
