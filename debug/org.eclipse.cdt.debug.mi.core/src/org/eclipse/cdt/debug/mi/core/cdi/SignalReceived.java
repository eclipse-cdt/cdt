/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */

package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISignalReceived;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.mi.core.cdi.model.Signal;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;

/**
 */
public class SignalReceived extends SessionObject implements ICDISignalReceived {

	ICDISignal signal;
	public SignalReceived(CSession session, MISignalEvent event) {
		super(session);
		signal = new Signal(session.getCTarget(), event);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalReceived#getSignal()
	 */
	public ICDISignal getSignal() {
		return signal;
	}

}
