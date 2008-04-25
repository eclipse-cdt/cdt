/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * QNX Software Systems - catchpoints - bug 226689
 *******************************************************************************/
package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;

public interface ICDIBreakpointManagement3 extends ICDIBreakpointManagement2{
	/**
	 * Set an event breakpoint
	 * @param type - event breakpoint type, interpreted by backend
	 * @param arg - extra argument, for example signal number
	 * @param cdiBreakpointType - cdi breakpoint type, just in case some inferiors support "hardware" event breakpoints
	 */
	ICDIEventBreakpoint setEventBreakpoint(String type, String arg, int cdiBreakpointType,
			ICDICondition condition, boolean deferred, boolean enabled) throws CDIException;
}
