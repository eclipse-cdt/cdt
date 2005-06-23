/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.Breakpoint;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointHitEvent;

/**
 */
public class BreakpointHit extends SessionObject implements ICDIBreakpointHit {

	MIBreakpointHitEvent breakEvent;

	public BreakpointHit(Session session, MIBreakpointHitEvent e) {
		super(session);
		breakEvent = e;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit#getBreakpoint()
	 */
	public ICDIBreakpoint getBreakpoint() {
		int number = breakEvent.getNumber();
		// Ask the breakpointManager for the breakpoint
		BreakpointManager mgr = ((Session)getSession()).getBreakpointManager();
		// We need to return the same object as the breakpoint.
		Breakpoint point = mgr.getBreakpoint(breakEvent.getMISession(), number);
		// FIXME: if point == null ?? Create a new breakpoint ??
		return point;
	}

}
