/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIFunctionFinishedEvent;
import org.eclipse.cdt.debug.mi.core.event.MILocationReachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;
import org.eclipse.cdt.debug.mi.core.event.MISteppingRangeEvent;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointEvent;

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
			return new BreakpointHit(session, (MIBreakpointEvent)event);
		} else if (event instanceof MIWatchpointEvent) {
			return new WatchpointTrigger(session, (MIWatchpointEvent)event);
		} else if (event instanceof MISteppingRangeEvent) {
			return new EndSteppingRange(session);
		} else if (event instanceof MISignalEvent) {
			return new Signal(session, (MISignalEvent)event);
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

		// This not necessary, we can send the target as the source
		// it will mean to cdi that all threads are suspended.
		// FIXME: Remove this comment code after testing.
		/*
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

		try {
			// If it came from a thread return it as the source.
			ICDIThread[] cthreads = target.getThreads();
			for (int i = 0; i < cthreads.length; i++) {
				if (((CThread)cthreads[i]).getId() == threadId) {
					return cthreads[i];
				}
			}
		} catch (CDIException e) {
		}
		*/
		return target;
	}
}
