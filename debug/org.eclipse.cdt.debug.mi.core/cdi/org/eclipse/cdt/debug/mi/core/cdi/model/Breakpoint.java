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
package org.eclipse.cdt.debug.mi.core.cdi.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint2;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.Condition;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;

/**
 */
public abstract class Breakpoint extends CObject implements ICDIBreakpoint2 {

	ICDICondition condition;
	MIBreakpoint[] miBreakpoints;
	
	/**
	 * One of the type constants in ICBreakpointType 
	 */
	int type;
	
	boolean enabled;

	public Breakpoint(Target target, int type, ICDICondition condition, boolean enabled) {
		super(target);
		this.type = type;
		this.condition = condition;
		this.enabled = enabled;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint2#getType()
	 */
	public int getType() {
		return type;
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
		return enabled;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint#isHardware()
	 * 
	 * CDT 5.0 won't call this deprecated method (since we implement
	 * ICDIBreakpoint2), but we use it ourselves.
	 */
	public boolean isHardware() {
		// ignore the TEMPORARY bit qualifier
		return ((type & ~ICBreakpointType.TEMPORARY) == ICBreakpointType.HARDWARE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint#isTemporary()
	 * 
	 * CDT 5.0 won't call this deprecated method (since we implement
	 * ICDIBreakpoint2), but we use it ourselves.
	 */
	public boolean isTemporary() {
		return (type & ICBreakpointType.TEMPORARY) != 0;
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
	}

	public void setEnabled0(boolean on) {
		enabled = on;
	}

}
