/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.mi.core.output.MIBreakPoint;

/**
 */
public class Breakpoint extends CObject implements ICDILocationBreakpoint {

	ICDILocation location;
	ICDICondition condition;
	MIBreakPoint miBreakPoint;
	BreakpointManager mgr;

	public Breakpoint(BreakpointManager m, MIBreakPoint miBreak) {
		super(m.getCSession().getCTarget());
		miBreakPoint = miBreak;
		mgr = m;
	}

	MIBreakPoint getMIBreakPoint() {
		return miBreakPoint;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#getCondition()
	 */
	public ICDICondition getCondition() throws CDIException {
		if (condition == null) {
			condition =  new Condition(miBreakPoint.getIgnoreCount(),
				miBreakPoint.getWhat());
		}
		return condition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#getThreadId()
	 */
	public String getThreadId() throws CDIException {
		return miBreakPoint.getThreadId();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isEnabled()
	 */
	public boolean isEnabled() throws CDIException {
		return miBreakPoint.isEnabled();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isHardware()
	 */
	public boolean isHardware() {
		return miBreakPoint.isHardware();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isTemporary()
	 */
	public boolean isTemporary() {
		return miBreakPoint.isTemporary();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setCondition(ICDICondition)
	 */
	public void setCondition(ICDICondition condition) throws CDIException {
		mgr.setCondition(this, condition);
		this.condition = condition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setEnabled(boolean)
	 */
	public void setEnabled(boolean enable) throws CDIException {
		if (enable == false && isEnabled() == true) { 
				mgr.disableBreakpoint(this);
		} else if (enable == true && isEnabled() == false) {
				mgr.enableBreakpoint(this);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocationBreakpoint#getLocation()
	 */
	public ICDILocation getLocation() throws CDIException {
		if (location == null) {
			location = new Location (miBreakPoint.getFile(),
					miBreakPoint.getFunction(),
					miBreakPoint.getLine(),
					miBreakPoint.getAddress());
		}
		return location;
	}
}
