/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISignal;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;

/**
 */
public class Signal extends SessionObject implements ICDISignal {

	MISignalEvent event;
	public Signal(CSession session, MISignalEvent e) {
		super(session);
		event = e;
	}
		
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#getMeaning()
	 */
	public String getMeaning() {
		return event.getMeaning();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#getName()
	 */
	public String getName() {
		return event.getName();
	}

}
