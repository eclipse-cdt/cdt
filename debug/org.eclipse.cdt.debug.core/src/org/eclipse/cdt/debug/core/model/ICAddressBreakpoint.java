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

/**
 * A breakpoint that suspend the execution when a particular address is reached.
 */
public interface ICAddressBreakpoint extends ICLineBreakpoint {

	/**
	 * Breakpoint marker type for this breakpoint type.
	 * @since 7.2
	 */
	public static final String C_ADDRESS_BREAKPOINT_MARKER = "org.eclipse.cdt.debug.core.cAddressBreakpointMarker"; //$NON-NLS-1$

}
