/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * 
 * A breakpoint that suspend execution when a particular address is reached.
 * 
 * @since Aug 21, 2002
 */
public interface ICAddressBreakpoint extends ICLineBreakpoint
{
	/**
	 * Returns the address this breakpoint suspends execution at.
	 * 
	 * @return the address this breakpoint suspends execution at
	 * @exception CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public String getAddress() throws CoreException;

	/**
	 * Sets the address this breakpoint suspends execution at.
	 * 
	 * @param address the address this breakpoint suspends execution at
	 * @exception CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public void setAddress( String address ) throws CoreException;
}
