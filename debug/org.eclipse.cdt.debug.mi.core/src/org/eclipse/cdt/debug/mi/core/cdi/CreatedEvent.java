/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointCreatedEvent;

/**
 */
public class CreatedEvent implements ICDICreatedEvent {

	CSession session;
	ICDIObject source;

	public CreatedEvent(CSession s, MIBreakpointCreatedEvent bpoint) {
		session = s;
		BreakpointManager mgr = (BreakpointManager)session.getBreakpointManager();
		int number = bpoint.getNumber();
		ICDIBreakpoint breakpoint = mgr.getBreakpoint(number);
		if (breakpoint != null) {
			source = breakpoint;
		} else {
			source = new CObject(session.getCTarget());
		}
	}

	public CreatedEvent(CSession s, ICDIObject src) {
		session = s;
		source = src;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return source;
	}

}
