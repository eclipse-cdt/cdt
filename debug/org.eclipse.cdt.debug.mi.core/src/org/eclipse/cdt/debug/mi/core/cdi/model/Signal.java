/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;

/**
 */
public class Signal extends CObject implements ICDISignal {

	MISignalEvent event;
	public Signal(ICDITarget target, MISignalEvent e) {
		super(target);
		event = e;
	}
		
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalReceived#getMeaning()
	 */
	public String getMeaning() {
		return event.getMeaning();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalReceived#getName()
	 */
	public String getName() {
		return event.getName();
	}

}
