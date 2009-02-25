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

import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;

/**
 * @since 6.0
 */
public class EventBreakpoint extends Breakpoint implements ICDIEventBreakpoint {
	
	public static final String CATCH = "org.eclipse.cdt.debug.gdb.catch";
	public static final String THROW = "org.eclipse.cdt.debug.gdb.throw";
	public static final String SIGNAL_CATCH = "org.eclipse.cdt.debug.gdb.signal";
	public static final String STOP_ON_FORK = "org.eclipse.cdt.debug.gdb.catch_fork";
	public static final String STOP_ON_VFORK = "org.eclipse.cdt.debug.gdb.catch_vfork";
	public static final String STOP_ON_EXEC = "org.eclipse.cdt.debug.gdb.catch_exec";
	/**
	 * @since 6.0
	 */
	public static final String CATCH_EXIT = "org.eclipse.cdt.debug.gdb.catch_exit";
	/**
	 * @since 6.0
	 */
	public static final String CATCH_START = "org.eclipse.cdt.debug.gdb.catch_start";
	/**
	 * @since 6.0
	 */
	public static final String CATCH_STOP = "org.eclipse.cdt.debug.gdb.catch_stop";
	/**
	 * @since 6.0
	 */
	public static final String CATCH_THREAD_START = "org.eclipse.cdt.debug.gdb.catch_thread_start";
	/**
	 * @since 6.0
	 */
	public static final String CATCH_THREAD_EXIT = "org.eclipse.cdt.debug.gdb.catch_thread_exit";
	/**
	 * @since 6.0
	 */
	public static final String CATCH_THREAD_JOIN = "org.eclipse.cdt.debug.gdb.catch_thread_join";
	/**
	 * @since 6.0
	 */
	public static final String CATCH_LOAD = "org.eclipse.cdt.debug.gdb.catch_load";
	/**
	 * @since 6.0
	 */
	public static final String CATCH_UNLOAD = "org.eclipse.cdt.debug.gdb.catch_unload";

	private String eventType;
	private String arg;
	private static final HashMap<String, String> idToKeyword = new HashMap<String, String>();
	static {
		// these Ids are also referenced in mi.ui plugin as contribution
		// to event breakpoints selector
		idToKeyword.put(CATCH, "catch");
		idToKeyword.put(THROW, "throw");
		idToKeyword.put(SIGNAL_CATCH, "signal");
		idToKeyword.put(STOP_ON_EXEC, "exec");
		idToKeyword.put(STOP_ON_FORK, "fork");
		idToKeyword.put(STOP_ON_VFORK, "vfork");
		idToKeyword.put(CATCH_EXIT, "exit");
		idToKeyword.put(CATCH_START, "start");
		idToKeyword.put(CATCH_STOP, "stop");
		idToKeyword.put(CATCH_THREAD_START, "thread_start");
		idToKeyword.put(CATCH_THREAD_EXIT, "thread_exit");
		idToKeyword.put(CATCH_THREAD_JOIN, "thread_join");
		idToKeyword.put(CATCH_LOAD, "load");
		idToKeyword.put(CATCH_UNLOAD, "unload");
	}

	public EventBreakpoint(Target target, String event, String arg, ICDICondition cond, boolean enabled) {
		super(target, ICBreakpointType.REGULAR, cond, enabled);
		this.eventType = event;
		this.arg = arg==null?"":arg;
	}

	public String getEventType()  {
		return eventType;
	}

	public String getExtraArgument() {
		return arg;
	}


	public String getGdbEvent() {
		String etype = getEventType();
		String key= idToKeyword.get(etype);
		if (key!=null) return key;
		return "unknown";
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
		if (miBreakpoint.getWhat().equals("exception catch")) {
			return  EventBreakpoint.CATCH;
		} else if (miBreakpoint.getWhat().equals("exception throw")) {
			return  EventBreakpoint.THROW;
		} else if (miBreakpoint.getType().equals("catch signal")) {
			// catch signal does not work in gdb 
			return  EventBreakpoint.SIGNAL_CATCH;
		} 
		String miType = miBreakpoint.getType();
		String prefix = "catch "; 
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
		return "";
	}

}
