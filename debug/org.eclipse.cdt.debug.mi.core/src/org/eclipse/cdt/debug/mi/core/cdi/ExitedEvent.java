/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIExitInfo;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;

/**
 */
public class ExitedEvent implements ICDIExitedEvent {

	MIInferiorExitEvent event;
	CSession session;
	
	public ExitedEvent(CSession s, MIInferiorExitEvent e) {
		session = s;
		event = e;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent#getExitInfo()
	 */
	public ICDIExitInfo getExitInfo() {
		return new ExitInfo(session, event);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return session.getCurrentTarget();
	}

}
