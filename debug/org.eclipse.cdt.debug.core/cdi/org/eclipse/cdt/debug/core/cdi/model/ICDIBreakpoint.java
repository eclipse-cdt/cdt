/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
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
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;

/**
 * 
 * A breakpoint is capable of suspending the execution of a program 
 * whenever a certain point in the program is reached. Provides a 
 * basic functionality for the location breakpoints, watchpoints,
 * and event breakpoints
 * 
 * @see ICDILineBreakpoint
 * @see ICDIFunctionBreakpoint
 * @see ICDIAddressBreakpoint
 * @see ICDIWatchpoint
 * @see ICDIEventBreakpoint
 * 
 * @since Jul 9, 2002
 */
public interface ICDIBreakpoint extends ICDIObject {
	/** @deprecated use ICBreakpointTyped.REGULAR */
	final static public int REGULAR =  ICBreakpointType.REGULAR;
	/** @deprecated use ICBreakpointTyped.TEMPORARY */
	final static public int TEMPORARY = ICBreakpointType.TEMPORARY;
	/** @deprecated use ICBreakpointTyped.HARDWARE */
	final static public int HARDWARE = ICBreakpointType.HARDWARE;
	
	/**
	 * @return whether this breakpoint is temporary
	 * @deprecated by {@link ICDIBreakpoint2#getType()} 
	 */
	boolean isTemporary();
	
	/**
	 * @return whether this breakpoint is hardware-assisted
	 * @deprecated by {@link ICDIBreakpoint2#getType()} 
	 */
	boolean isHardware();

	/**
	 * Returns whether this breakpoint is enabled.
	 * 
	 * @return whether this breakpoint is enabled
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	boolean isEnabled() throws CDIException;
	
	/**
	 * Sets the enabled state of this breakpoint. This has no effect 
	 * if the current enabled state is the same as specified by 
	 * the enabled parameter.
	 * 
	 * @param enabled - whether this breakpoint should be enabled 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void setEnabled(boolean enabled) throws CDIException;
	
	/**
	 * Returns the condition of this breakpoint or <code>null</code>
	 * if the breakpoint's condition is not set.
	 * 
	 * @return the condition of this breakpoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDICondition getCondition() throws CDIException;
	
	/**
	 * Sets the condition of this breakpoint.
	 * 
	 * @param the condition to set
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void setCondition(ICDICondition condition) throws CDIException;
	
}
