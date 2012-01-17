/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import java.util.Arrays;

import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;
import org.eclipse.cdt.gdb.eventbkpts.IEventBreakpointConstants;
import org.eclipse.cdt.gdb.internal.eventbkpts.GdbCatchpoints;

/**
 * @since 6.0
 */
public class EventBreakpoint extends Breakpoint implements ICDIEventBreakpoint {
	

	private String eventType;
	private String arg;

	public EventBreakpoint(Target target, String event, String arg, ICDICondition cond, boolean enabled) {
		super(target, ICBreakpointType.REGULAR, cond, enabled);
		this.eventType = event;
		this.arg = arg==null?"":arg; //$NON-NLS-1$
	}

	@Override
	public String getEventType()  {
		return eventType;
	}

	@Override
	public String getExtraArgument() {
		return arg;
	}

	/**
	 * Returns the gdb catchpoint keyword associated with this event breakpoint
	 * (e.g., "signal", "throw")
	 */
	public String getGdbEvent() {
		return GdbCatchpoints.eventToGdbCatchpointKeyword(getEventType());
	}
	
	public String getGdbArg() {
		return getExtraArgument();
	}
	
	@Override
	public int hashCode() {
		return eventType.hashCode();
	}
	@Override
	public boolean equals(Object arg0) {
		if (this == arg0) return true;
		if (!(arg0 instanceof EventBreakpoint)) return false;
		MIBreakpoint[] breakpoints = getMIBreakpoints();
		if (breakpoints==null || breakpoints.length==0) {
			return super.equals(arg0);
		}
		return Arrays.equals(breakpoints, ((EventBreakpoint)arg0).getMIBreakpoints());
	}
	/**
	 * Returns event type by using miBreakpoint parameters
	 * @param miBreakpoint
	 * @return null if unknown type, null cannot be used to create valid EventBreakpoint
	 */
	public static String getEventTypeFromMI(MIBreakpoint miBreakpoint) {
		// Two exceptions to how the message is typically formatted
		if (miBreakpoint.getWhat().equals("exception catch")) { //$NON-NLS-1$
			return  IEventBreakpointConstants.EVENT_TYPE_CATCH;
		} else if (miBreakpoint.getWhat().equals("exception throw")) { //$NON-NLS-1$
			return IEventBreakpointConstants.EVENT_TYPE_THROW;
		}
		
		String miType = miBreakpoint.getType();
		final String PREFIX = "catch ";  //$NON-NLS-1$
		if (miType.startsWith(PREFIX)) {
			String keyword = miType.substring(PREFIX.length());
			return GdbCatchpoints.gdbCatchpointKeywordToEvent(keyword); 
		}
		return null; // not known/supported
	}
	
	public static String getEventArgumentFromMI(MIBreakpoint miBreakpoint) {
		// need a working gdb command command that support catch event argument test test
		return ""; //$NON-NLS-1$
	}

}
