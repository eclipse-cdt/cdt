/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.event.ChangedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.CreatedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.DestroyedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.DisconnectedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.ExitedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.MemoryChangedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.ResumedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.event.SuspendedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.model.Breakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.MemoryBlock;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Thread;
import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIExecContinue;
import org.eclipse.cdt.debug.mi.core.command.MIExecFinish;
import org.eclipse.cdt.debug.mi.core.command.MIStackInfoDepth;
import org.eclipse.cdt.debug.mi.core.command.MIStackSelectFrame;
import org.eclipse.cdt.debug.mi.core.command.MIThreadSelect;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointDeletedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointHitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MICreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIDestroyedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIDetachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIGDBExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorSignalExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibUnloadedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIStoppedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarDeletedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIStackInfoDepthInfo;

/**
 */
public class EventManager extends SessionObject implements ICDIEventManager, Observer {

	List list = Collections.synchronizedList(new ArrayList(1));
	List tokenList = new ArrayList(1); 
	MIRunningEvent lastRunningEvent;
	Command lastUserCommand = null;

	/**
	 * Process the event from MI, do any state work on the CDI,
	 * and fire the corresponding CDI event.
	 */
	public void update(Observable o, Object arg) {
		MIEvent miEvent = (MIEvent)arg;
		Session session = (Session)getSession();
		Target currentTarget = session.getTarget(miEvent.getMISession());
		if (currentTarget == null) {
			return; // bailout; this no concern to us.  But we should Assert.
		}
		List cdiList = new ArrayList(1);

		if (ignoreEventToken(miEvent.getToken())) {
			// Ignore the event if it is on the ignore list.
		} else if (miEvent instanceof MIStoppedEvent) {
			if (processSuspendedEvent((MIStoppedEvent)miEvent)) {
				cdiList.add(new SuspendedEvent(session, miEvent));
			}
		} else if (miEvent instanceof MIRunningEvent) {
			if (processRunningEvent((MIRunningEvent)miEvent))
				cdiList.add(new ResumedEvent(session, (MIRunningEvent)miEvent));
		} else if (miEvent instanceof MIChangedEvent) {
			if (miEvent instanceof MIVarChangedEvent) {
				cdiList.add(new ChangedEvent(session, (MIVarChangedEvent)miEvent));
			} else if (miEvent instanceof MIRegisterChangedEvent) {
				cdiList.add(new ChangedEvent(session, (MIRegisterChangedEvent)miEvent));
			} else if (miEvent instanceof MIMemoryChangedEvent) {
				// We need to fire an event for all the register blocks
				// that may contain the modified addresses.
				MemoryManager mgr = session.getMemoryManager();
				MemoryBlock[] blocks = mgr.getMemoryBlocks(miEvent.getMISession());
				MIMemoryChangedEvent miMem = (MIMemoryChangedEvent)miEvent;
				BigInteger[] addresses = miMem.getAddresses();
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
						session.getBreakpointManager().update(currentTarget);
					} catch (CDIException e) {
					}
				}
			} else if (miEvent instanceof MISharedLibChangedEvent) {
				cdiList.add(new ChangedEvent(session, (MISharedLibChangedEvent)miEvent));
			} else if (miEvent instanceof MISignalChangedEvent) {
				MISignalChangedEvent sig = (MISignalChangedEvent)miEvent;
				String name = sig.getName();
				if (name == null || name.length() == 0) {
					// Something change we do not know what
					// Let the signal manager handle it with an update().
					try {
						SignalManager sMgr = session.getSignalManager();
						sMgr.update(currentTarget);
					} catch (CDIException e) {
					}
				} else {
					cdiList.add(new ChangedEvent(session, sig));
				}
			}
		} else if (miEvent instanceof MIDestroyedEvent) {
			if (miEvent instanceof MIThreadExitEvent) {
				cdiList.add(new DestroyedEvent(session,(MIThreadExitEvent)miEvent)); 
			} else if (miEvent instanceof MIInferiorSignalExitEvent) {
				cdiList.add(new ExitedEvent(session, (MIInferiorSignalExitEvent)miEvent));
			} else if (miEvent instanceof MIInferiorExitEvent) {
				cdiList.add(new ExitedEvent(session, (MIInferiorExitEvent)miEvent));
			} else if (miEvent instanceof MIGDBExitEvent) {
				// Remove the target from the list.
				Target target = session.getTarget(miEvent.getMISession());
				if (target != null) {
					session.removeTargets(new Target[] { target });
				}
			} else if (miEvent instanceof MIDetachedEvent) {
				cdiList.add(new DisconnectedEvent(session, (MIDetachedEvent)miEvent));
			} else if (miEvent instanceof MIBreakpointDeletedEvent) {
				MIBreakpointDeletedEvent bpoint = (MIBreakpointDeletedEvent)miEvent;
				if (bpoint.getNumber() > 0) {
					cdiList.add(new DestroyedEvent(session, bpoint));
				} else {
					// Something was deleted we do not know what
					// Let the breakpoint manager handle it with an update().
					try {
						session.getBreakpointManager().update(currentTarget);
					} catch (CDIException e) {
					}
				}
			} else if (miEvent instanceof MISharedLibUnloadedEvent) {
				processSharedLibUnloadedEvent((MISharedLibUnloadedEvent)miEvent);
				cdiList.add(new DestroyedEvent(session, (MISharedLibUnloadedEvent)miEvent));
			} else if (miEvent instanceof MIVarDeletedEvent) {
				cdiList.add(new DestroyedEvent(session, (MIVarDeletedEvent)miEvent));
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
						session.getBreakpointManager().update(currentTarget);
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
			} else if (miEvent instanceof MIInferiorCreatedEvent) {
				cdiList.add(new CreatedEvent(session, (MIInferiorCreatedEvent)miEvent));
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

	public void removeEventListeners() {
		list.clear();
	}

	/**
	 * Send ICDIEvent to the listeners.
	 */
	public void fireEvents(ICDIEvent[] cdiEvents) {
		if (cdiEvents != null && cdiEvents.length > 0) {
			ICDIEventListener[] listeners = (ICDIEventListener[])list.toArray(new ICDIEventListener[0]);
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].handleDebugEvents(cdiEvents);
			}			
		}
	}

	/**
	 * When suspended arrives, reset managers and target.
	 * Alse the variable and the memory needs to be updated and events
	 * fired for changes.
	 */
	boolean processSuspendedEvent(MIStoppedEvent stopped) {
		Session session = (Session)getSession();
		MISession miSession = stopped.getMISession();
		Target currentTarget = session.getTarget(miSession);
		if (processSharedLibEvent(stopped)) {
			// Event was consumed by the shared lib processing bailout
			return false;
		}

		if (processBreakpointHitEvent(stopped)) {
			// Event was consumed, i.e. it was not the right exception.
			return false;
		}

		int threadId = threadId = stopped.getThreadId();
		currentTarget.updateState(threadId);
		try {
			Thread cthread = (Thread)currentTarget.getCurrentThread();
			if (cthread != null) {
				cthread.getCurrentStackFrame();
			} else {
				return true;
			}
		} catch (CDIException e1) {
			//e1.printStackTrace();
			return true;
		}

		// Update the managers.
		// For the Variable/Expression Managers call only the updateManager.
		VariableManager varMgr = session.getVariableManager();
		ExpressionManager expMgr  = session.getExpressionManager();		
		RegisterManager regMgr = session.getRegisterManager();
		MemoryManager memMgr = session.getMemoryManager();
		BreakpointManager bpMgr = session.getBreakpointManager();
		SignalManager sigMgr = session.getSignalManager();
		SourceManager srcMgr = session.getSourceManager();
		SharedLibraryManager libMgr = session.getSharedLibraryManager();
		try {
			if (varMgr.isAutoUpdate()) {
				varMgr.update(currentTarget);
			}
			if (expMgr.isAutoUpdate()) { 
				expMgr.update(currentTarget);
			}
			if (regMgr.isAutoUpdate()) {
				regMgr.update(currentTarget);
			}
			if (memMgr.isAutoUpdate()) {
				memMgr.update(currentTarget);
			}
			if (bpMgr.isAutoUpdate()) {
				bpMgr.update(currentTarget);
			}
			if (sigMgr.isAutoUpdate()) {
				sigMgr.update(currentTarget);
			}
			if (libMgr.isAutoUpdate()) {
				libMgr.update(currentTarget);
			}
			if (srcMgr.isAutoUpdate()) {
				srcMgr.update(currentTarget);
			}
		} catch (CDIException e) {
			//System.out.println(e);
		}
		return true;
	}

	/**
	 * When a shared library is unloading we could possibly have stale libraries.
	 * GDB does no react well to this: see PR
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74496
	 * @param unLoaded
	 * @return
	 */
	boolean processSharedLibUnloadedEvent(MISharedLibUnloadedEvent unLoaded) {
		Session session = (Session)getSession();
		MISession miSession = unLoaded.getMISession();
		Target target = session.getTarget(miSession);
		ExpressionManager expMgr = session.getExpressionManager();
		VariableManager varMgr = session.getVariableManager();
		try {
			expMgr.destroyAllExpressions(target);
			varMgr.destroyAllVariables(target);
		} catch (CDIException e) {
		}
		return false;
	}

	/**
	 * If the deferredBreakpoint processing is set
	 * catch the shared-lib-event go to the last known
	 * stackframe and try to finish.
	 * Save the last user command and issue it again.
	 * @param stopped
	 * @return
	 */
	boolean processSharedLibEvent(MIStoppedEvent stopped) {
		Session session = (Session)getSession();
		MISession miSession = stopped.getMISession();

		Target currentTarget = session.getTarget(miSession);
		SharedLibraryManager mgr = session.getSharedLibraryManager();

		if (mgr.isDeferredBreakpoint()) {
			if (stopped instanceof MISharedLibEvent) {
				// Check if we have a new library loaded
				List eventList = null;
				try {
					eventList = mgr.updateState(currentTarget);
				} catch (CDIException e3) {
					eventList = Collections.EMPTY_LIST;
				}
				// A new Libraries loaded, try to set the breakpoints.
				if (eventList.size() > 0) {
					BreakpointManager bpMgr = session.getBreakpointManager();
					ICDIBreakpoint bpoints[] = null;
					try {
						bpoints = bpMgr.getDeferredBreakpoints(currentTarget);
					} catch (CDIException e) {
						bpoints = new ICDIBreakpoint[0];
					}
					for (int i = 0; i < bpoints.length; i++) {
						if (bpoints[i] instanceof Breakpoint) {
							Breakpoint bkpt = (Breakpoint)bpoints[i];
							try {
								boolean enable = bkpt.isEnabled();
								bpMgr.setLocationBreakpoint(bkpt);
								bpMgr.deleteFromDeferredList(bkpt);
								bpMgr.addToBreakpointList(bkpt);
								// If the breakpoint was disable in the IDE
								// install it but keep it disable
								if (!enable) {
									bpMgr.disableBreakpoint(bkpt);
								}
								MIBreakpoint[] miBreakpoints = bkpt.getMIBreakpoints();
								if (miBreakpoints != null && miBreakpoints.length > 0) {
									eventList.add(new MIBreakpointCreatedEvent(miSession, miBreakpoints[0].getNumber()));
								}
							} catch (CDIException e) {
								// ignore
							}
						}
					}
					MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
					miSession.fireEvents(events);
				}
				CommandFactory factory = miSession.getCommandFactory();
				int type = (lastRunningEvent == null) ? MIRunningEvent.CONTINUE : lastRunningEvent.getType();
				if (lastUserCommand == null) {
					switch (type) {
						case MIRunningEvent.NEXT:
							lastUserCommand = factory.createMIExecNext(1);
							break;
						case MIRunningEvent.NEXTI:
							lastUserCommand = factory.createMIExecNextInstruction(1);
							break;
						case MIRunningEvent.STEP:
							lastUserCommand = factory.createMIExecStep(1);
							break;
						case MIRunningEvent.STEPI:
							lastUserCommand = factory.createMIExecStepInstruction(1);
							break;
						case MIRunningEvent.FINISH:
							lastUserCommand = factory.createMIExecFinish();
							break;
						case MIRunningEvent.RETURN:
							lastUserCommand = factory.createMIExecReturn();
						break;
						case MIRunningEvent.CONTINUE: {
							MIExecContinue cont = factory.createMIExecContinue();
							try {
								miSession.postCommand(cont);
								MIInfo info = cont.getMIInfo();
								if (info == null) {
									// throw new CDIException("Target is not responding");
								}
							} catch (MIException e) {
								// throw new MI2CDIException(e);
							}
							return true; // for the continue bailout early no need to the stuff below
						}
					}
				}

				int miLevel = 0;
				int tid = 0;
				Thread currentThread = null;
				try {
					currentThread = (Thread)currentTarget.getCurrentThread();
				} catch (CDIException e1) {
				}
				tid = currentThread.getId();
				// Select the old thread now.
				if (tid > 0) {
					MIThreadSelect selectThread = factory.createMIThreadSelect(tid);
					try {
						miSession.postCommand(selectThread);
					} catch (MIException e) {
						// ignore
					}
				}
				ICDIStackFrame frame = null;
				try {
					frame = currentThread.getCurrentStackFrame();
				} catch (CDIException e2) {
				}
				int count = 0;
				try {
					MIStackInfoDepth depth = factory.createMIStackInfoDepth();
					miSession.postCommand(depth);
					MIStackInfoDepthInfo info = depth.getMIStackInfoDepthInfo();
					if (info == null) {
						//throw new CDIException("No answer");
					}
					count = info.getDepth();
				} catch (MIException e) {
					//throw new MI2CDIException(e);
					//System.out.println(e);
				}
				if (frame != null) {
					// Fortunately the ICDIStackFrame store the level
					// in ascending level the higher the stack the higher the level
					// GDB does the opposite the highest stack is 0.
					// This allow us to do some calculation, in figure out the
					// level of the old stack.  The -1 is because gdb level is zero-based
					miLevel = count - frame.getLevel() - 1;
				}
				if (miLevel >= 0) {
					MIStackSelectFrame selectFrame = factory.createMIStackSelectFrame(miLevel);
					MIExecFinish finish = factory.createMIExecFinish();
					try {
						miSession.postCommand(selectFrame);
						miSession.postCommand(finish);
					} catch (MIException e) {
						// ignore
					}
				} else {
					// if we are still at the same level in the backtrace
					// for example the StopEventLib was on a different thread
					// redo the last command.
					Command cmd = lastUserCommand;
					lastUserCommand = null;
					try {
						miSession.postCommand(cmd);
					} catch (MIException e) {
						// ignore
					}					
				}
				return true;
			} else if (lastUserCommand != null) {
				Command cmd = lastUserCommand;
				lastUserCommand = null;
				try {
					miSession.postCommand(cmd);
				} catch (MIException e) {
				}
				return true;
			}
		}
		return false;
	}

	boolean processBreakpointHitEvent(MIStoppedEvent stopped) {
		Session session = (Session)getSession();
		if (stopped instanceof MIBreakpointHitEvent) {
			MIBreakpointHitEvent bpEvent = (MIBreakpointHitEvent)stopped;
			BreakpointManager bpMgr = session.getBreakpointManager();
			int bpNo = bpEvent.getNumber();
		}
		return false;
	}

	/**
	 * Do any processing of before a running event.
	 */
	boolean processRunningEvent(MIRunningEvent running) {
		lastRunningEvent = running;
		return true;
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
