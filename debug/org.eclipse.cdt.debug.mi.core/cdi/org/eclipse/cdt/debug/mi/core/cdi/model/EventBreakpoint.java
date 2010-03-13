/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;

/**
 * @since 6.0
 */
public class EventBreakpoint extends Breakpoint implements ICDIEventBreakpoint {
	

	private String eventType;
	private String arg;

	/**
	 * A mapping of ICEventBreakpoint event types to their corresponding gdb
	 * catchpoint keyword
	 */
	private static final Map<String, String> idToKeyword = new HashMap<String, String>();
	static {
		// these Ids are also referenced in mi.ui plugin as contribution
		// to event breakpoints selector
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_CATCH, "catch"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_THROW, "throw"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_SIGNAL_CATCH, "signal"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_EXEC, "exec"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_FORK, "fork"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_VFORK, "vfork"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_EXIT, "exit"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_PROCESS_START, "start"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_PROCESS_STOP, "stop"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_THREAD_START, "thread_start"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_THREAD_EXIT, "thread_exit"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_THREAD_JOIN, "thread_join"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_LIBRARY_LOAD, "load"); //$NON-NLS-1$
		idToKeyword.put(ICEventBreakpoint.EVENT_TYPE_LIBRARY_UNLOAD, "unload"); //$NON-NLS-1$
	}

	public EventBreakpoint(Target target, String event, String arg, ICDICondition cond, boolean enabled) {
		super(target, ICBreakpointType.REGULAR, cond, enabled);
		this.eventType = event;
		this.arg = arg==null?"":arg; //$NON-NLS-1$
	}

	public String getEventType()  {
		return eventType;
	}

	public String getExtraArgument() {
		return arg;
	}

	/**
	 * Returns the gdb catchpoint keyword associated with this event breakpoint
	 * (e.g., "signal", "throw")
	 */
	public String getGdbEvent() {
		return getGdbEventFromId(getEventType());
	}

	/**
	 * Returns the gdb catchpoint keyword associated with the given event point
	 * type id (e.g., "signal", "throw")
	 * 
	 * @param eventTypeId
	 *            one of the EVENT_TYPE_XXXXX constants from
	 *            {@link ICEventBreakpoint}
	 * 
	 * @since 7.0
	 */
	public static String getGdbEventFromId(String eventTypeId) {
		String key= idToKeyword.get(eventTypeId);
		if (key!=null) return key;
		assert false : "Unexpected even breakpoint type ID: " + eventTypeId; //$NON-NLS-1$
		return "unknown"; //$NON-NLS-1$
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
		if (miBreakpoint.getWhat().equals("exception catch")) { //$NON-NLS-1$
			return  ICEventBreakpoint.EVENT_TYPE_CATCH;
		} else if (miBreakpoint.getWhat().equals("exception throw")) { //$NON-NLS-1$
			return ICEventBreakpoint.EVENT_TYPE_THROW;
		} else if (miBreakpoint.getType().equals("catch signal")) { //$NON-NLS-1$
			// catch signal does not work in gdb 
			return  ICEventBreakpoint.EVENT_TYPE_SIGNAL_CATCH;
		} 
		String miType = miBreakpoint.getType();
		String prefix = "catch ";  //$NON-NLS-1$
		if (miType.startsWith(prefix)) {
			String key = miType.substring(prefix.length());
			for (String id : idToKeyword.keySet()) {
				String etype = idToKeyword.get(id);
				if (key.equals(etype)) {
					return id;
				}
			}
		}
		return null; // not known/supported
	}
	
	public static String getEventArgumentFromMI(MIBreakpoint miBreakpoint) {
		// need a working gdb command command that support catch event argument test test
		return ""; //$NON-NLS-1$
	}

}
