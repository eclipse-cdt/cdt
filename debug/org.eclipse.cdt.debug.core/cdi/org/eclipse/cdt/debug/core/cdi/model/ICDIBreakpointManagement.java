/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 * Breapoints action on the Target.
 * 
 */
public interface ICDIBreakpointManagement {
	
	ICDIBreakpoint[] getBreakpoints() throws CDIException;

	ICDILocationBreakpoint setLocationBreakpoint(int type, ICDILocation location,
		ICDICondition condition, boolean deferred) throws CDIException;		

	ICDIWatchpoint setWatchpoint(int type, int watchType, String expression,
		ICDICondition condition) throws CDIException;

	void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException;

	void deleteAllBreakpoints() throws CDIException;

	ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws CDIException;

}
