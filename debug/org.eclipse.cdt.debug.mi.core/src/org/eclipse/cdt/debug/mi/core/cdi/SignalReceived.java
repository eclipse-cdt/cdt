/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */

package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISignalReceived;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;

/**
 */
public class SignalReceived extends SessionObject implements ICDISignalReceived {

	ICDISignal signal;
	public SignalReceived(Session session, MISignalEvent event) {
		super(session);
		SignalManager mgr = (SignalManager)session.getSignalManager();
		signal = mgr.getSignal(event.getName());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalReceived#getSignal()
	 */
	public ICDISignal getSignal() {
		return signal;
	}

}
