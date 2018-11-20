/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * A dynamic printf specific to the C/C++ debug model.
 *
 * @since 7.5
 */
public interface ICDynamicPrintf extends ICLineBreakpoint {
	/**
	 * Breakpoint marker type for this breakpoint type.
	 */
	public static final String C_DYNAMICPRINTF_MARKER = "org.eclipse.cdt.debug.core.cDynamicPrintfMarker"; //$NON-NLS-1$

	/**
	 * Breakpoint marker type for this breakpoint type.
	 */
	public static final String C_LINE_DYNAMICPRINTF_MARKER = "org.eclipse.cdt.debug.core.cLineDynamicPrintfMarker"; //$NON-NLS-1$

	/**
	 * Breakpoint marker type for this breakpoint type.
	 */
	public static final String C_ADDRESS_DYNAMICPRINTF_MARKER = "org.eclipse.cdt.debug.core.cAddressDynamicPrintfMarker"; //$NON-NLS-1$

	/**
	 * Breakpoint marker type for this breakpoint type.
	 */
	public static final String C_FUNCTION_DYNAMICPRINTF_MARKER = "org.eclipse.cdt.debug.core.cFunctionDynamicPrintfMarker"; //$NON-NLS-1$

	/**
	 * Dynamic printf attribute storing the string to be printed (value
	 * <code>"org.eclipse.cdt.debug.core.printf_string"</code>). This attribute
	 * is a <code>String</code>.
	 */
	public static final String PRINTF_STRING = "org.eclipse.cdt.debug.core.printf_string"; //$NON-NLS-1$

	/**
	 * Returns the string used by this dynamic printf.
	 *
	 * @return the string used by this dynamic printf
	 * @exception CoreException if unable to access the property on this dynamic printf's
	 *  underlying marker
	 */
	public String getPrintfString() throws CoreException;

	/**
	 * Sets the string attribute for this dynamic printf
	 *
	 * @param message The new string
	 * @exception CoreException if unable to access the property on this dynamic printf's
	 *  underlying marker
	 */
	public void setPrintfString(String str) throws CoreException;
}
