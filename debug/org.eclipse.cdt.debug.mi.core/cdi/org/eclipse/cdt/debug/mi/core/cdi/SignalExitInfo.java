/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISignalExitInfo;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorSignalExitEvent;

/**.
 */
public class SignalExitInfo extends SessionObject implements ICDISignalExitInfo {

	MIInferiorSignalExitEvent event;

	public SignalExitInfo(Session session, MIInferiorSignalExitEvent e) {
		super(session);
		event = e;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalExitInfo#getName()
	 */
	public String getName() {
		return event.getName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalExitInfo#getDescription()
	 */
	public String getDescription() {
		return event.getMeaning();
	}

}
