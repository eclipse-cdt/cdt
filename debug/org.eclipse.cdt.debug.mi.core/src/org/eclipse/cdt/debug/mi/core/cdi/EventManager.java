/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIFunctionFinishedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MILocationReachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;
import org.eclipse.cdt.debug.mi.core.event.MISteppingRangeEvent;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointEvent;

/**
 */
public class EventManager extends SessionObject implements ICDIEventManager, Observer {

	List list = Collections.synchronizedList(new ArrayList(1));

	/**
	 * Process the event from MI and do any state work on the CDI.
	 */
	public void update(Observable o, Object arg) {
		MIEvent miEvent = (MIEvent)arg;
		CSession session = getCSession();
		ICDIEvent cdiEvent = null;

		if (miEvent instanceof MIBreakpointEvent) {
			cdiEvent = new SuspendedEvent(session, miEvent);
		} else if (miEvent instanceof MIFunctionFinishedEvent) {
			cdiEvent = new SuspendedEvent(session, miEvent);
		} else if (miEvent instanceof MILocationReachedEvent) {
			cdiEvent = new SuspendedEvent(session, miEvent);
		} else if (miEvent instanceof MISignalEvent) {
			cdiEvent = new SuspendedEvent(session, miEvent);
		} else if (miEvent instanceof MISteppingRangeEvent) {
			cdiEvent = new SuspendedEvent(session, miEvent);
		} else if (miEvent instanceof MIWatchpointEvent) {
			cdiEvent = new SuspendedEvent(session, miEvent);
		} else if (miEvent instanceof MIRunningEvent) {
			cdiEvent = new ResumedEvent(session, (MIRunningEvent)miEvent);
		} else if (miEvent instanceof MIInferiorExitEvent) {
			cdiEvent = new ExitedEvent(session, (MIInferiorExitEvent)miEvent);
		} else if (miEvent instanceof MIExitEvent) {
			cdiEvent = new DestroyedEvent(session, null);
		}

		// Fire the event;
		fireEvent(cdiEvent);
	}

	public void fireEvent(ICDIEvent cdiEvent) {
		if (cdiEvent != null) {
			ICDIEventListener[] listeners =
				(ICDIEventListener[])list.toArray(new ICDIEventListener[0]);
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].handleDebugEvent(cdiEvent);
			}
		}
	}

	public EventManager(CSession session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIEventManager#addEventListener(ICDIEventListener)
	 */
	public void addEventListener(ICDIEventListener listener) {
		list.add(listener);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIEventManager#removeEventListener(ICDIEventListener)
	 */
	public void removeEventListener(ICDIEventListener listener) {
		list.remove(listener);
	}
}
