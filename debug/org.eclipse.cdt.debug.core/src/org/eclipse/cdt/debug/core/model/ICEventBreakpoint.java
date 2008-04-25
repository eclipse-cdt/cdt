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
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * Interface for debugger event breakpoints. Example of event breakpoint
 * is break on raising exception in C++, or break on receiving signal.
 * 
 * @sinse 5.0
 */
public interface ICEventBreakpoint extends ICBreakpoint {
	/**
	 * Breakpoint attribute storing the event breakpoint event id
	 * This attribute is a <code>String</code>.
	 * 
	 */
	public static final String EVENT_TYPE_ID = "org.eclipse.cdt.debug.core.eventbreakpoint_event_id"; //$NON-NLS-1$	
	/**
	 * Breakpoint attribute storing the event breakpoint argument
	 * This attribute is a <code>String</code>.
	 * 
	 */
	public static final String EVENT_ARG = "org.eclipse.cdt.debug.core.eventbreakpoint_event_arg"; //$NON-NLS-1$	
	
	/**
	 * Get event breakpoint type. This is usually id in reverse web notation. 
	 * This type is interpreted by underlying debugger implementation.
	 * Use extension point <code>org.eclipse.cdt.debug.ui.breakpointContribution</code> to define user visible label for this event type.
	 * @return event breakpoint type id (not null)
	 * @throws CoreException 
	 */
	String getEventType() throws CoreException;

	/**
	 * Get extra event argument. For example name of the exception or number of a signal.
	 * Use extension point <code>org.eclipse.cdt.debug.ui.breakpointContribution</code> to define UI control to edit/view this argument
	 * @return event argument (not null)
	 * @throws CoreException 
	 */
	String getEventArgument() throws CoreException;
}
