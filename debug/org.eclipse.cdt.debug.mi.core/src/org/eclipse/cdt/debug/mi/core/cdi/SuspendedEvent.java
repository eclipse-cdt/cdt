package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIFunctionFinishedEvent;
import org.eclipse.cdt.debug.mi.core.event.MILocationReachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;
import org.eclipse.cdt.debug.mi.core.event.MISteppingRangeEvent;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointEvent;
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
		if (event instanceof MIBreakpointEvent || event instanceof MIWatchpointEvent) {
			MIBreakpointEvent breakEvent = (MIBreakpointEvent)event;
			int number = breakEvent.getNumber();
			BreakpointManager mgr = (BreakpointManager)session.getBreakpointManager();
			// Ask the breakpointManager for the breakpoint
			// We need to return the same object as the reason.
			Breakpoint point = mgr.getBreakpoint(number);
			if (point != null) {
				return point;
			}  else {
				// FIXME: Create a new breakpoint.
			}
		} else if (event instanceof MISteppingRangeEvent) {
			return new EndSteppingRange(session);
		} else if (event instanceof MISignalEvent) {
			return new Signal(session, (MISignalEvent) event);
		} else if (event instanceof MILocationReachedEvent) {
			return new EndSteppingRange(session);
		} else if (event instanceof MIFunctionFinishedEvent) {
			return new EndSteppingRange(session);
		}
		return session;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		CTarget target = session.getCTarget();
		int threadId = 0;
		if (event instanceof MIBreakpointEvent) {
			MIBreakpointEvent breakEvent = (MIBreakpointEvent) event;
			threadId = breakEvent.getThreadId();
		} else if (event instanceof MIWatchpointEvent) {
			MIWatchpointEvent watchEvent = (MIWatchpointEvent) event;
			threadId = watchEvent.getThreadId();
		} else if (event instanceof MISteppingRangeEvent) {
			MISteppingRangeEvent rangeEvent = (MISteppingRangeEvent) event;
			threadId = rangeEvent.getThreadId();
		} else if (event instanceof MISignalEvent) {
			MISignalEvent sigEvent = (MISignalEvent) event;
			threadId = sigEvent.getThreadId();
		} else if (event instanceof MILocationReachedEvent) {
			MILocationReachedEvent locEvent = (MILocationReachedEvent) event;
			threadId = locEvent.getThreadId();
		} else if (event instanceof MIFunctionFinishedEvent) {
			MIFunctionFinishedEvent funcEvent = (MIFunctionFinishedEvent) event;
			threadId = funcEvent.getThreadId();
		}

		// If it came from a thread return it as the source.
		CThread[] cthreads = target.getCThreads();
		for (int i = 0; i < cthreads.length; i++) {
			if (cthreads[i].getId() == threadId) {
				return cthreads[i];
			}
		}
		// Not found?? new thread created?
		CThread cthread = new CThread(session.getCTarget(), threadId);
		target.addCThread(cthread);
		return cthread;
	}
}
