/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.Condition;
import org.eclipse.cdt.debug.mi.core.cdi.Location;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;

/**
 */
public class Breakpoint extends CObject implements ICDILocationBreakpoint {

	ICDILocation location;
	ICDICondition condition;
	MIBreakpoint miBreakpoint;
	BreakpointManager mgr;

	public Breakpoint(BreakpointManager m, MIBreakpoint miBreak) {
		super(m.getSession().getCurrentTarget());
		miBreakpoint = miBreak;
		mgr = m;
	}

	public MIBreakpoint getMIBreakpoint() {
		return miBreakpoint;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#getCondition()
	 */
	public ICDICondition getCondition() throws CDIException {
		if (condition == null) {
			condition =  new Condition(miBreakpoint.getIgnoreCount(),
				miBreakpoint.getWhat());
		}
		return condition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#getThreadId()
	 */
	public String getThreadId() throws CDIException {
		return miBreakpoint.getThreadId();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isEnabled()
	 */
	public boolean isEnabled() throws CDIException {
		return miBreakpoint.isEnabled();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isHardware()
	 */
	public boolean isHardware() {
		return miBreakpoint.isHardware();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isTemporary()
	 */
	public boolean isTemporary() {
		return miBreakpoint.isTemporary();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setCondition(ICDICondition)
	 */
	public void setCondition(ICDICondition condition) throws CDIException {
		if (isEnabled()) {
			mgr.setCondition(this, condition);
		}
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
			location = new Location (miBreakpoint.getFile(),
					miBreakpoint.getFunction(),
					miBreakpoint.getLine(),
					miBreakpoint.getAddress());
		}
		return location;
	}
}
