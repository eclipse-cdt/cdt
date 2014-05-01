/*******************************************************************************
 * Copyright (c) 2014 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.breakpoints;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;

/**
 * @since 7.4
 */
public class BreakpointUtils {
	
	public static boolean isBreakpointInstalled(ICBreakpoint breakpoint) {
		return ICDebugUIConstants.BREAKPOINT_STATE_INSTALLED.equals(getBreakpointState(breakpoint));
	}
	
	public static boolean isBreakpointPending(ICBreakpoint breakpoint) {
		return ICDebugUIConstants.BREAKPOINT_STATE_PENDING.equals(getBreakpointState(breakpoint));
	}

	private static String getBreakpointState(ICBreakpoint breakpoint) {
		return breakpoint.getMarker().getAttribute(ICDebugUIConstants.BREAKPOINT_ATTR_STATE, ICDebugUIConstants.BREAKPOINT_STATE_NOT_INSTALLED);
	}
}
