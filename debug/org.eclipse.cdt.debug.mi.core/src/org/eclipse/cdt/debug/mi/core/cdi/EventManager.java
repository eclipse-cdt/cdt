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
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager;
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager;
import org.eclipse.cdt.debug.core.cdi.ICDISignalManager;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.ICDIVariableManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.event.ChangedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.CreatedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.DestroyedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.DisconnectedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.ExitedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.MemoryChangedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.ResumedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.SuspendedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.model.MemoryBlock;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointDeletedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MICreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIDestroyedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIDetachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIGDBExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibUnloadedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIStoppedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarCreatedEvent;

/**
 */
public class EventManager extends SessionObject implements ICDIEventManager, Observer {

	List list = Collections.synchronizedList(new ArrayList(1));
	List tokenList = new ArrayList(1); 

	/**
	 * Process the event from MI, do any state work on the CDI,
	 * and fire the corresponding CDI event.
	 */
	public void update(Observable o, Object arg) {
		MIEvent miEvent = (MIEvent)arg;
		Session session = (Session)getSession();
		List cdiList = new ArrayList(1);

		if (ignoreEventToken(miEvent.getToken())) {
			// Ignore the event if it is on the ignore list.
		} else if (miEvent instanceof MIStoppedEvent) {
			processSuspendedEvent((MIStoppedEvent)miEvent);
			cdiList.add(new SuspendedEvent(session, miEvent));
		} else if (miEvent instanceof MIRunningEvent) {
			cdiList.add(new ResumedEvent(session, (MIRunningEvent)miEvent));
		} else if (miEvent instanceof MIChangedEvent) {
			if (miEvent instanceof MIVarChangedEvent) {
				MIVarChangedEvent eventChanged = (MIVarChangedEvent)miEvent;
				// We will receive a MIVarChangeEvent if the variable is
				// no longer in scope in this case fire up a DestroyEvent
				if (eventChanged.isInScope()) {
					cdiList.add(new ChangedEvent(session, eventChanged));
				} else {
					cdiList.add(new DestroyedEvent(session, eventChanged));
				}
			} else if (miEvent instanceof MIRegisterChangedEvent) {
				cdiList.add(new ChangedEvent(session, (MIRegisterChangedEvent)miEvent));
			} else if (miEvent instanceof MIMemoryChangedEvent) {
				// We need to fire an event for all the register blocks
				// that may contain the modified addresses.
				MemoryManager mgr = (MemoryManager)session.getMemoryManager();
				MemoryBlock[] blocks = mgr.listMemoryBlocks();
				MIMemoryChangedEvent miMem = (MIMemoryChangedEvent)miEvent;
				Long[] addresses = miMem.getAddresses();
				for (int i = 0; i < blocks.length; i++) {
					if (blocks[i].contains(addresses) &&
						(! blocks[i].isFrozen() || blocks[i].isDirty())) {
						cdiList.add(new MemoryChangedEvent(session, blocks[i], miMem));
						blocks[i].setDirty(false);
					}
				}
			} else if (miEvent instanceof MIBreakpointChangedEvent) {
				MIBreakpointChangedEvent bpoint = (MIBreakpointChangedEvent)miEvent;
				if (bpoint.getNumber() > 0) {
					cdiList.add(new ChangedEvent(session, bpoint));
				} else {
					// Something change we do not know what
					// Let the breakpoint manager handle it with an update().
					try {
						((BreakpointManager)(session.getBreakpointManager())).update();
					} catch (CDIException e) {
					}
				}
			} else if (miEvent instanceof MISharedLibChangedEvent) {
				cdiList.add(new ChangedEvent(session, (MISharedLibChangedEvent)miEvent));
			}
		} else if (miEvent instanceof MIDestroyedEvent) {
			if (miEvent instanceof MIThreadExitEvent) {
				cdiList.add(new DestroyedEvent(session,(MIThreadExitEvent)miEvent)); 
			} else if (miEvent instanceof MIInferiorExitEvent) {
				cdiList.add(new ExitedEvent(session, (MIInferiorExitEvent)miEvent));
			} else if (miEvent instanceof MIGDBExitEvent) {
				cdiList.add(new DestroyedEvent(session));
			} else if (miEvent instanceof MIDetachedEvent) {
				cdiList.add(new DisconnectedEvent(session));
			} else if (miEvent instanceof MIBreakpointDeletedEvent) {
				MIBreakpointDeletedEvent bpoint = (MIBreakpointDeletedEvent)miEvent;
				if (bpoint.getNumber() > 0) {
					cdiList.add(new DestroyedEvent(session, bpoint));
				} else {
					// Something was deleted we do not know what
					// Let the breakpoint manager handle it with an update().
					try {
						((BreakpointManager)(session.getBreakpointManager())).update();
					} catch (CDIException e) {
					}
				}
			} else if (miEvent instanceof MISharedLibUnloadedEvent) {
				cdiList.add(new DestroyedEvent(session, (MISharedLibUnloadedEvent)miEvent));
			}
		} else if (miEvent instanceof MICreatedEvent) {
			if (miEvent instanceof MIBreakpointCreatedEvent) {
				MIBreakpointCreatedEvent bpoint = (MIBreakpointCreatedEvent)miEvent;
				if (bpoint.getNumber() > 0) {
					cdiList.add(new CreatedEvent(session, bpoint));
				} else {
					// Something created we do not know what
					// Let the breakpoint manager handle it with an update().
					try {
						((BreakpointManager)(session.getBreakpointManager())).update();
					} catch (CDIException e) {
					}
				}
			} else if (miEvent instanceof MIVarCreatedEvent) {
				cdiList.add(new CreatedEvent(session, (MIVarCreatedEvent)miEvent));
			} else if (miEvent instanceof MIRegisterCreatedEvent) {
				cdiList.add(new CreatedEvent(session, (MIRegisterCreatedEvent)miEvent));
			} else if (miEvent instanceof MIThreadCreatedEvent) {
				cdiList.add(new CreatedEvent(session, (MIThreadCreatedEvent)miEvent));
			} else if (miEvent instanceof MIMemoryCreatedEvent) {
				cdiList.add(new CreatedEvent(session, (MIMemoryCreatedEvent)miEvent));
			} else if (miEvent instanceof MISharedLibCreatedEvent) {
				cdiList.add(new CreatedEvent(session, (MISharedLibCreatedEvent)miEvent));
			}
		}

		// Fire the event;
		ICDIEvent[] cdiEvents = (ICDIEvent[])cdiList.toArray(new ICDIEvent[0]);
		fireEvents(cdiEvents);
	}

	public EventManager(Session session) {
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


	/**
	 * Send ICDIEvent to the listeners.
	 */
	private void fireEvents(ICDIEvent[] cdiEvents) {
		if (cdiEvents != null) {
			for (int i = 0; i < cdiEvents.length; i++) {
				fireEvent(cdiEvents[i]);
			}
		}
	}

	/**
	 * Send ICDIEvent to the listeners.
	 */
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
	 * Alse the variable and the memory needs to be updated and events
	 * fired for changes.
	 */
	void processSuspendedEvent(MIStoppedEvent stopped) {
		Session session = (Session)getSession();
		ICDITarget currentTarget = session.getCurrentTarget();
		// Set the current thread.
		int threadId = threadId = stopped.getThreadId();
		if (currentTarget instanceof Target) {
			((Target)currentTarget).updateState(threadId);
		}
		// Update the managers.
		// For the Variable/Expression Managers call only the updateManager.
		UpdateManager upMgr = session.getUpdateManager();
		ICDIVariableManager varMgr = session.getVariableManager();
		ICDIExpressionManager expMgr  = session.getExpressionManager();		
		ICDIRegisterManager regMgr = session.getRegisterManager();
		ICDIMemoryManager memMgr = session.getMemoryManager();
		ICDISharedLibraryManager libMgr = session.getSharedLibraryManager();
		ICDIBreakpointManager bpMgr = session.getBreakpointManager();
		ICDISignalManager sigMgr = session.getSignalManager();
		ICDISourceManager srcMgr = session.getSourceManager();
		try {
			if (varMgr.isAutoUpdate() || expMgr.isAutoUpdate()) { 
				upMgr.update();
			}
			if (regMgr.isAutoUpdate()) {
				regMgr.update();
			}
			if (memMgr.isAutoUpdate()) {
				memMgr.update();
			}
			if (libMgr.isAutoUpdate()) {
				libMgr.update();
			}
			if (bpMgr.isAutoUpdate()) {
				bpMgr.update();
			}
			if (sigMgr.isAutoUpdate()) {
				sigMgr.update();
			}
			if (srcMgr.isAutoUpdate()) {
				srcMgr.update();
			}
		} catch (CDIException e) {
			//System.out.println(e);
		}
	}

	/**
	 * Do any processing of before a running event.
	 */
	void processRunningEvent() {
		//Target target = getCSession().getCTarget();
		//target.clearState();
	}


	/**
	 * Ignore Event with token id.
	 */
	void disableEventToken(int token) {
		tokenList.add(new Integer(token));
	}

	/**
	 * Ignore events with token ids.
	 */
	void disableEventTokens(int [] tokens) {
		for (int i = 0; i < tokens.length; i++) {
			disableEventToken(tokens[i]);
		}
	}

	/**
	 * Reenable sending events with this token.
	 */
	void enableEventToken(int token) {
		Integer t = new Integer(token);
		if (tokenList.contains(t)) {
			tokenList.remove(t);
		}
	}

	/**
	 * Reenable sending events with this token.
	 */
	void enableEventTokens(int [] tokens) {
		for (int i = 0; i < tokens.length; i++) {
			enableEventToken(tokens[i]);
		}
	}

	private boolean ignoreEventToken(int token) {
		return tokenList.contains(new Integer(token));
	}
}
