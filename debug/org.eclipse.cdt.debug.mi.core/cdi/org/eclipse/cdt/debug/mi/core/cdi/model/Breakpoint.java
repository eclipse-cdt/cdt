/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.Condition;
import org.eclipse.cdt.debug.mi.core.cdi.Location;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;

/**
 */
public class Breakpoint extends CObject implements ICDILocationBreakpoint {

	ICDILocation fLocation;
	ICDICondition condition;
	MIBreakpoint[] miBreakpoints;
	int type;
	boolean enable;

	public Breakpoint(Target target, int kind, ICDILocation loc, ICDICondition cond) {
		super(target);
		type = kind;
		fLocation = loc;
		condition = cond;
		enable = true;
	}

	public MIBreakpoint[] getMIBreakpoints() {
		return miBreakpoints;
	}

	public void setMIBreakpoints(MIBreakpoint[] newMIBreakpoints) {
		miBreakpoints = newMIBreakpoints;
	}

	public boolean isDeferred() {
		return (miBreakpoints == null || miBreakpoints.length == 0);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#getCondition()
	 */
	public ICDICondition getCondition() throws CDIException {
		if (condition == null) {
			if (miBreakpoints != null && miBreakpoints.length > 0) {
				List list = new ArrayList(miBreakpoints.length);
				for (int i = 0; i < miBreakpoints.length; i++) {
					String tid = miBreakpoints[i].getThreadId();
					if (tid != null && tid.length() > 0) {
						list.add(miBreakpoints[i].getThreadId());
					}
				}
				String[] tids = (String[]) list.toArray(new String[list.size()]);
				int icount = miBreakpoints[0].getIgnoreCount();
				String exp = miBreakpoints[0].getCondition();
				condition = new Condition(icount, exp, tids);
			} else {
				condition =  new Condition(0, new String(), null);
			}
		}
		return condition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isEnabled()
	 */
	public boolean isEnabled() throws CDIException {
		return enable;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isHardware()
	 */
	public boolean isHardware() {
		return (type == ICDIBreakpoint.HARDWARE);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isTemporary()
	 */
	public boolean isTemporary() {
		return (type == ICDIBreakpoint.TEMPORARY);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setCondition(ICDICondition)
	 */
	public void setCondition(ICDICondition newCondition) throws CDIException {
		Session session = (Session)getTarget().getSession();
		BreakpointManager mgr = session.getBreakpointManager();
		mgr.setCondition(this, newCondition);
		setCondition0(newCondition);
	}

	public void setCondition0(ICDICondition newCondition) {
		condition = newCondition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setEnabled(boolean)
	 */
	public void setEnabled(boolean on) throws CDIException {
		Session session = (Session)getTarget().getSession();
		BreakpointManager mgr = session.getBreakpointManager();
		if (on == false && isEnabled() == true) { 
			mgr.disableBreakpoint(this);
		} else if (on == true && isEnabled() == false) {
			mgr.enableBreakpoint(this);
		}
		setEnabled0(on);
	}

	public void setEnabled0(boolean on) {
		enable = on;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocationBreakpoint#getLocation()
	 */
	public ICDILocation getLocation() throws CDIException {
		if (fLocation == null) {
			if (miBreakpoints != null && miBreakpoints.length > 0) {
				BigInteger addr = BigInteger.ZERO;
				String a = miBreakpoints[0].getAddress();
				if (a != null) {
					addr = MIFormat.getBigInteger(a);
				}
				fLocation = new Location (miBreakpoints[0].getFile(),
					miBreakpoints[0].getFunction(),
					miBreakpoints[0].getLine(),
					addr);
			}
		}
		return fLocation;
	}

	public void setLocation(ICDILocation loc) {
		fLocation = loc;
	}
}
