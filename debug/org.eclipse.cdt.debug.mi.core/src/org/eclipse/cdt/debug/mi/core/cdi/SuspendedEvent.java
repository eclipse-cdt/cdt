package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIStepEvent;
import org.eclipse.cdt.debug.mi.core.output.MIBreakPoint;

/**
 *
 */
public class SuspendedEvent implements ICDISuspendedEvent {

	MIEvent event;
	CSession session;

	public SuspendedEvent(CSession s, MIEvent e) {
		session = s;
		event = e;
	}
		

	public ICDISessionObject getReason() {
		if (event instanceof MIBreakpointEvent) {
			MIBreakpointEvent breakEvent = (MIBreakpointEvent)event;
			int number = breakEvent.getNumber();
			ICDIBreakpointManager mgr = session.getBreakpointManager();
			try {
				ICDIBreakpoint[] bkpts= mgr.getBreakpoints();
				for (int i = 0; i < bkpts.length; i++) {
					if (bkpts[i] instanceof Breakpoint) {
						Breakpoint point = (Breakpoint)bkpts[i];
						MIBreakPoint miBreak = point.getMIBreakPoint();
						if (miBreak.getNumber() == number) {
							return point;
						}
					}
				}
			} catch (CDIException e) {
			}
		} else if (event instanceof MIStepEvent) {
			return new EndSteppingRange(session);
		}
		return session;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return new CThread(session.getCTarget(), "");
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent#getStackFrame()
	 */
	public ICDIStackFrame getStackFrame() {
		if (event instanceof MIBreakpointEvent) {
			MIBreakpointEvent breakEvent = (MIBreakpointEvent)event;
			return new StackFrame(session.getCTarget(), breakEvent.getMIFrame());
		} else if (event instanceof MIStepEvent) {
			MIStepEvent stepEvent = (MIStepEvent)event;
			return new StackFrame(session.getCTarget(), stepEvent.getMIFrame());
		}
		return null;
	}

}
