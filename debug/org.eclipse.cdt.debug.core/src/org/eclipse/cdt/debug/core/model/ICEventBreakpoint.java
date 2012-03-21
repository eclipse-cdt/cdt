/*******************************************************************************
 * Copyright (c) 2008, 2010 QNX Software Systems and others.
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
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * Interface for debugger event breakpoints. Example of event breakpoint
 * is break on raising exception in C++, or break on receiving signal.
 * 
 * @sinse 5.0
 * @since 7.0
 */
public interface ICEventBreakpoint extends ICBreakpoint {

    /** 
     * Breakpoint marker type for this breakpoint type.
     * @since 7.2
     */    
    public static final String C_EVENT_BREAKPOINT_MARKER = "org.eclipse.cdt.debug.core.cEventBreakpointMarker"; //$NON-NLS-1$;
    
	/**
	 * Breakpoint attribute storing the event breakpoint event id. Basically,
	 * this indicates what type of event the breakpoint catches--e.g., a C++
	 * exception throw, a library load, a thread exit, etc. Event types are
	 * contributed via the "breakpointContribution" extension point.
	 * 
	 * <p>
	 * This attribute is a <code>String</code>.
	 * 
	 */
	public static final String EVENT_TYPE_ID = "org.eclipse.cdt.debug.core.eventbreakpoint_event_id"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing the event breakpoint argument. This
	 * attribute is a <code>String</code>, though it may be a stringified
	 * representation of another type (it may be a number, for example).
	 * Currently, an event type can have only one argument
	 */
	public static final String EVENT_ARG = "org.eclipse.cdt.debug.core.eventbreakpoint_event_arg"; //$NON-NLS-1$	

	/**
	 * Get the event breakpoint type. Same as querying the property
	 * {@link #EVENT_TYPE_ID}
	 * 
	 * @return event breakpoint type id (not null)
	 * @throws CoreException
	 */
	String getEventType() throws CoreException;

	/**
	 * Get the event argument, if the type has one. Currently, an event type can
	 * have at most one argument. Same as querying the property
	 * {@link #EVENT_ARG}
	 * 
	 * @return event argument, or null if not applicable
	 * @throws CoreException
	 */
	String getEventArgument() throws CoreException;
}
