/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointTriggerEvent;

/**
 */
public class WatchpointTrigger extends SessionObject implements ICDIWatchpointTrigger {

	MIWatchpointTriggerEvent watchEvent;

	public WatchpointTrigger(CSession session, MIWatchpointTriggerEvent e) {
		super(session);
		watchEvent = e;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger#getNewValue()
	 */
	public String getNewValue() {
		return watchEvent.getNewValue();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger#getOldValue()
	 */
	public String getOldValue() {
		return watchEvent.getOldValue();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger#getWatchpoint()
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
