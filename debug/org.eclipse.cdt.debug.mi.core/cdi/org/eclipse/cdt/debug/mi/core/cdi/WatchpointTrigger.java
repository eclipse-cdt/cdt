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
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.Watchpoint;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointTriggerEvent;

/**
 */
public class WatchpointTrigger extends SessionObject implements ICDIWatchpointTrigger {

	MIWatchpointTriggerEvent watchEvent;

	public WatchpointTrigger(Session session, MIWatchpointTriggerEvent e) {
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
		BreakpointManager mgr = ((Session)getSession()).getBreakpointManager();
		// We need to return the same object as the reason.
		Watchpoint point = mgr.getWatchpoint(watchEvent.getMISession(), number);
		// FIXME: if point ==null ??? Create a new breakpoint ?
		return point;
	}

}
