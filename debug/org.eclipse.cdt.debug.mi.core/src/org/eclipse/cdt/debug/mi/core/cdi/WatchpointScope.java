package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointScope;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointEvent;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WatchpointScope extends SessionObject implements ICDIWatchpointScope {

	MIWatchpointEvent watchEvent;

	public WatchpointScope(CSession session, MIWatchpointEvent e) {
		super(session);
		watchEvent = e;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpointScope#getWatchpoint()
	 */
	public ICDIWatchpoint getWatchpoint() {
		int number = watchEvent.getNumber();
		// Ask the breakpointManager for the breakpoint
		BreakpointManager mgr = (BreakpointManager)getCSession().getBreakpointManager();
		// We need to return the same object as the reason.
		Watchpoint point = mgr.getWatchpoint(number);
		// FIXME: if point ==null ??? Create a new breakpoint ?
		return point;
	}

}
