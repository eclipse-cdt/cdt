/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * A tracepoint specific to the C/C++ debug model.
 *
 * @since 7.0
 */
public interface ICTracepoint extends ICLineBreakpoint {

	/**
	 * Breakpoint marker type for this breakpoint type.
	 * @since 7.2
	 */
	public static final String C_TRACEPOINT_MARKER = "org.eclipse.cdt.debug.core.cTracepointMarker"; //$NON-NLS-1$

	/**
	 * Breakpoint marker type for this breakpoint type.
	 * @since 7.2
	 */
	public static final String C_LINE_TRACEPOINT_MARKER = "org.eclipse.cdt.debug.core.cLineTracepointMarker"; //$NON-NLS-1$

	/**
	 * Breakpoint marker type for this breakpoint type.
	 * @since 7.2
	 */
	public static final String C_ADDRESS_TRACEPOINT_MARKER = "org.eclipse.cdt.debug.core.cAddressTracepointMarker"; //$NON-NLS-1$

	/**
	 * Breakpoint marker type for this breakpoint type.
	 * @since 7.2
	 */
	public static final String C_FUNCTION_TRACEPOINT_MARKER = "org.eclipse.cdt.debug.core.cFunctionTracepointMarker"; //$NON-NLS-1$

	/**
	 * Tracepoint attribute storing a tracepoint's pass count value (value
	 * <code>"org.eclipse.cdt.debug.core.passCount"</code>). This attribute
	 * is an <code>int</code>.
	 */
	public static final String PASS_COUNT = "org.eclipse.cdt.debug.core.passCount"; //$NON-NLS-1$

	/**
	 * Returns the pass count used by this tracepoint.
	 *
	 * @return the pass count used by this breakpoint
	 * @exception CoreException if unable to access the property on this tracepoint's
	 *  underlying marker
	 */
	public int getPassCount() throws CoreException;

	/**
	 * Sets the pass count attribute for this tracepoint.
	 *
	 * @param passCount the new pass count
	 * @exception CoreException if unable to access the property on this tracepoint's
	 *  underlying marker
	 */
	public void setPassCount(int passCount) throws CoreException;
}
