package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointEvent;

/**
 */
public class BreakpointHit extends SessionObject implements ICDIBreakpointHit {

	MIBreakpointEvent breakEvent;

	public BreakpointHit(CSession session, MIBreakpointEvent e) {
		super(session);
		breakEvent = e;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit#getBreakpoint()
	 */
	public ICDIBreakpoint getBreakpoint() {
		int number = breakEvent.getNumber();
		// Ask the breakpointManager for the breakpoint
		BreakpointManager mgr = (BreakpointManager)getCSession().getBreakpointManager();
		// We need to return the same object as the breakpoint.
		Breakpoint point = mgr.getBreakpoint(number);
		// FIXME: if point == null ?? Create a new breakpoint ??
		return point;
	}

}
