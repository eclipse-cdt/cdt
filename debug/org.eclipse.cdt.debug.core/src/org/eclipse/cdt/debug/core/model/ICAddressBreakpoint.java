/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * A breakpoint that suspend the execution when a particular address is reached.
 */
public interface ICAddressBreakpoint extends ICLineBreakpoint {

	/**
	 * Breakpoint attribute storing the address this breakpoint suspends
	 * execution at (value <code>"org.eclipse.cdt.debug.core.address"</code>).
	 * This attribute is a <code>String</code>.
	 */
	public static final String ADDRESS = "org.eclipse.cdt.debug.core.address"; //$NON-NLS-1$	

	/**
	 * Returns the address this breakpoint suspends execution at.
	 * 
	 * @return the address this breakpoint suspends execution at
	 * @exception CoreException if unable to access the property on this breakpoint's
	 * underlying marker
	 */
	public String getAddress() throws CoreException;

	/**
	 * Sets the address this breakpoint suspends execution at.
	 * 
	 * @param address the address this breakpoint suspends execution at
	 * @exception CoreException if unable to access the property on this breakpoint's
	 * underlying marker
	 */
	public void setAddress( String address ) throws CoreException;
}