/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointScope;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointScopeEvent;

/**
 */
public class WatchpointScope extends SessionObject implements ICDIWatchpointScope {

	MIWatchpointScopeEvent watchEvent;

	public WatchpointScope(CSession session, MIWatchpointScopeEvent e) {
		super(session);
		watchEvent = e;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpointScope#getWatchpoint()
	 */
	public ICDIWatchpoint getWatchpoint() {
		int number = watchEvent.getNumber();
		// Ask the breakpointManager for the breakpoint
		BreakpointManager mgr = (BreakpointManager)getCSession().getBreakpointManager();
		// We need to return the same object as the reason.
		Watchpoint point = mgr.getWatchpoint(number);
		// FIXME: if point ==null ??? Create a new breakpoint ?
		return point;
	}

}
