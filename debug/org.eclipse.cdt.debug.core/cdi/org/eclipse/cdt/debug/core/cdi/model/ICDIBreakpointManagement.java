/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;

/**
 * Breapoints action on the Target.
 */
public interface ICDIBreakpointManagement {
	
	/**
	 * Set a line breakpoint.
	 * 
	 * @param type
	 * @param location
	 * @param condition
	 * @param deferred
	 * @return
	 * @throws CDIException
	 */
	ICDILineBreakpoint setLineBreakpoint(int type, ICDILineLocation location,
			ICDICondition condition, boolean deferred) throws CDIException;		
	
	/**
	 * Set a function breakpoint.
	 * 
	 * @param type
	 * @param location
	 * @param condition
	 * @param deferred
	 * @return
	 * @throws CDIException
	 */
	ICDIFunctionBreakpoint setFunctionBreakpoint(int type, ICDIFunctionLocation location,
			ICDICondition condition, boolean deferred) throws CDIException;
	
	/**
	 * Set an address Breakpoint
	 * 
	 * @param type
	 * @param location
	 * @param condition
	 * @param deferred
	 * @return
	 * @throws CDIException
	 */
	ICDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location,
		ICDICondition condition, boolean deferred) throws CDIException;		

	/**
	 * Set a watchpoint.
	 * 
	 * @param type
	 * @param watchType
	 * @param expression
	 * @param condition
	 * @return
	 * @throws CDIException
	 */
	ICDIWatchpoint setWatchpoint(int type, int watchType, String expression,
		ICDICondition condition) throws CDIException;

	/**
	 * Set an exception point.
	 * 
	 * @param clazz
	 * @param stopOnThrow
	 * @param stopOnCatch
	 * @return
	 * @throws CDIException
	 */
	ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws CDIException;

	/**
	 * Return all the breakpoints
	 * 
	 * @return
	 * @throws CDIException
	 */
	ICDIBreakpoint[] getBreakpoints() throws CDIException;

	/**
	 * Remove the breakpoints
	 * @param breakpoints
	 * @throws CDIException
	 */
	void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException;

	/**
	 * Remove all the breakpoints
	 * 
	 * @throws CDIException
	 */
	void deleteAllBreakpoints() throws CDIException;


}
