/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointEvent;

/**
 */
public class WatchpointTrigger extends WatchpointScope implements ICDIWatchpointTrigger {

	public WatchpointTrigger(CSession session, MIWatchpointEvent e) {
		super(session, e);
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

}
