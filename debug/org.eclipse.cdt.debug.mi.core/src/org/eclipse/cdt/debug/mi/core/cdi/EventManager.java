/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIFunctionFinishedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIGDBExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MILocationReachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;
import org.eclipse.cdt.debug.mi.core.event.MISteppingRangeEvent;
import org.eclipse.cdt.debug.mi.core.event.MIStoppedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
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

		if (miEvent instanceof MIStoppedEvent) {
			processSuspendedEvent(miEvent);
			cdiEvent = new SuspendedEvent(session, miEvent);
		} else if (miEvent instanceof MIRunningEvent) {
			cdiEvent = new ResumedEvent(session, (MIRunningEvent)miEvent);
		} else if (miEvent instanceof MIVarChangedEvent) {
			MIVarChangedEvent eventChanged = (MIVarChangedEvent)miEvent;
			if (eventChanged.isInScope()) {
				cdiEvent = new ChangedEvent(session, (MIVarChangedEvent)miEvent);
			} else {
				cdiEvent = new DestroyedEvent(session, (MIVarChangedEvent)miEvent);
			}
		} else if (miEvent instanceof MIThreadExitEvent) {
			cdiEvent = new DestroyedEvent(session,(MIThreadExitEvent)miEvent); 
		} else if (miEvent instanceof MIInferiorExitEvent) {
			cdiEvent = new ExitedEvent(session, (MIInferiorExitEvent)miEvent);
		} else if (miEvent instanceof MIGDBExitEvent) {
			cdiEvent = new DestroyedEvent(session);
		}

		// Fire the event;
		fireEvent(cdiEvent);
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

	private void fireEvent(ICDIEvent cdiEvent) {
		if (cdiEvent != null) {
			ICDIEventListener[] listeners =
				(ICDIEventListener[])list.toArray(new ICDIEventListener[0]);
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].handleDebugEvent(cdiEvent);
			}
		}
	}

	/**
	 * When suspended arrives, reset managers and target.
	 */
	void processSuspendedEvent(MIEvent event) {
		int threadId = 0;
		CTarget target = getCSession().getCTarget();
		// Set the current thread.
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
		target.updateState(threadId);

		VariableManager varMgr = getCSession().getVariableManager();
		try {
			varMgr.update();
		} catch (CDIException e) {
			//System.out.println(e);
		}
	}

	void processRunningEvent() {
		CTarget target = getCSession().getCTarget();
		//target.clearState();
	}
}
