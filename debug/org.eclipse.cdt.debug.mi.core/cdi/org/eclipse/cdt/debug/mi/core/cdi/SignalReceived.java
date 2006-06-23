/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

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
		SignalManager mgr = session.getSignalManager();
		signal = mgr.getSignal(event.getMISession(), event.getName());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalReceived#getSignal()
	 */
	public ICDISignal getSignal() {
		return signal;
	}

}
