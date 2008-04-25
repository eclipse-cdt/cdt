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

import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDICatchpoint;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;

public class Catchpoint extends Breakpoint implements ICDICatchpoint {

	public static final String CATCH = "org.eclipse.cdt.debug.gdb.catch";
	public static final String THROW = "org.eclipse.cdt.debug.gdb.throw";
	public static final String SIGNAL_CATCH = "org.eclipse.cdt.debug.gdb.signal";
	private String eventType;
	private String arg;

	public Catchpoint(Target target, String event, String arg, ICDICondition cond, boolean enabled) {
		super(target, ICDIBreakpoint.REGULAR, cond, enabled);
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
		if (getEventType().equals(CATCH)) return "catch";
		if (getEventType().equals(THROW)) return "throw";
		if (getEventType().equals(SIGNAL_CATCH)) return "signal";
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
		if (!(arg0 instanceof Catchpoint)) return false;
		MIBreakpoint[] breakpoints = getMIBreakpoints();
		if (breakpoints==null || breakpoints.length==0) {
			return super.equals(arg0);
		}
		return Arrays.equals(breakpoints, ((Catchpoint)arg0).getMIBreakpoints());
	}
	/**
	 * Returns event type by using miBreakpoint parameters
	 * @param miBreakpoint
	 * @return null if unknown type, null cannot be used to create valid Catchpoint
	 */
	public static String getEventTypeFromMI(MIBreakpoint miBreakpoint) {
		if (miBreakpoint.getWhat().equals("exception catch")) {
			return 
			 Catchpoint.CATCH;
		} else if (miBreakpoint.getWhat().equals("exception throw")) {
			return  Catchpoint.THROW;
		}
		return null; // not known/supported
	}
	
	public static String getEventArgumentFromMI(MIBreakpoint miBreakpoint) {
		// need a working gdb command command that support catch event argument test test
		return "";
	}

}
