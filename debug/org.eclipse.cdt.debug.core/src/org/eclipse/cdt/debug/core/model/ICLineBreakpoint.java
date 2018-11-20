/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * A breakpoint that suspends the execution when a particular location of code
 * is reached.
 */
public interface ICLineBreakpoint extends ICBreakpoint, ILineBreakpoint {

	/**
	 * Breakpoint marker type for this breakpoint type.
	 * @since 7.2
	 */
	public static final String C_LINE_BREAKPOINT_MARKER = "org.eclipse.cdt.debug.core.cLineBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing the function this breakpoint suspends
	 * execution at (value <code>"org.eclipse.cdt.debug.core.function"</code>).
	 * This attribute is a <code>String</code>.
	 */
	public static final String FUNCTION = "org.eclipse.cdt.debug.core.function"; //$NON-NLS-1$

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
	public void setAddress(String address) throws CoreException;

	/**
	 * Returns the function this breakpoint suspends execution in.
	 *
	 * @return the function this breakpoint suspends execution in
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public String getFunction() throws CoreException;

	/**
	 * Sets the function this breakpoint suspends execution in.
	 *
	 * @param function the function this breakpoint suspends execution in
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public void setFunction(String function) throws CoreException;

	/**
	 * Returns the source file (if available) of this breakpoint.
	 *
	 * @return the source file of this breakpoint
	 * @throws CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public String getFileName() throws CoreException;

}
