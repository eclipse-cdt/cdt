/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIExitInfo;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;

/**.
 */
public class ExitInfo extends SessionObject implements ICDIExitInfo {

	MIInferiorExitEvent event;

	public ExitInfo(Session session, MIInferiorExitEvent e) {
		super(session);
		event = e;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExitInfo#getCode()
	 */
	public int getCode() {
		return event.getExitCode();
	}

}
