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
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
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
import org.eclipse.cdt.debug.mi.core.event.MIChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MICreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIDestroyedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIDetachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIGDBExitEvent;
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
						session.getBreakpointManager().update();
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
						session.getSignalManager().update();
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
						session.getBreakpointManager().update();
					} catch (CDIException e) {
					}
				}
			} else if (miEvent instanceof MISharedLibUnloadedEvent) {
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
						session.getBreakpointManager().update();
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
		ICDITarget currentTarget = session.getCurrentTarget();

		if (processSharedLibEvent(stopped)) {
			// Event was consumed by the shared lib processing bailout
			return false;
		}
	
		int threadId = threadId = stopped.getThreadId();
		if (currentTarget instanceof Target) {
			Target cTarget = (Target)currentTarget;
			cTarget.updateState(threadId);
			try {
				ICDIThread cthread = cTarget.getCurrentThread();
					if (cthread != null) {
						cthread.getCurrentStackFrame();
					} else {
						return true;
					}
			} catch (CDIException e1) {
				//e1.printStackTrace();
				return true;
			}
		}

		// Update the managers.
		// For the Variable/Expression Managers call only the updateManager.
		ICDIVariableManager varMgr = session.getVariableManager();
		ICDIExpressionManager expMgr  = session.getExpressionManager();		
		ICDIRegisterManager regMgr = session.getRegisterManager();
		ICDIMemoryManager memMgr = session.getMemoryManager();
		ICDIBreakpointManager bpMgr = session.getBreakpointManager();
		ICDISignalManager sigMgr = session.getSignalManager();
		ICDISourceManager srcMgr = session.getSourceManager();
		ICDISharedLibraryManager libMgr = session.getSharedLibraryManager();
		try {
			if (varMgr.isAutoUpdate()) {
				varMgr.update();
			}
			if (expMgr.isAutoUpdate()) { 
				expMgr.update();
			}
			if (regMgr.isAutoUpdate()) {
				regMgr.update();
			}
			if (memMgr.isAutoUpdate()) {
				memMgr.update();
			}
			if (bpMgr.isAutoUpdate()) {
				bpMgr.update();
			}
			if (sigMgr.isAutoUpdate()) {
				sigMgr.update();
			}
			if (libMgr.isAutoUpdate()) {
				   libMgr.update();
			}
			if (srcMgr.isAutoUpdate()) {
				srcMgr.update();
			}
		} catch (CDIException e) {
			//System.out.println(e);
		}
		return true;
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
		MISession mi = session.getMISession();

		ICDITarget currentTarget = session.getCurrentTarget();
		ICDISharedLibraryManager libMgr = session.getSharedLibraryManager();
		SharedLibraryManager mgr = null;

		if (libMgr instanceof SharedLibraryManager) {
			mgr = (SharedLibraryManager)libMgr;
		}

		if (mgr !=null &&  mgr.isDeferredBreakpoint()) {
			if (stopped instanceof MISharedLibEvent) {
				// Check if we have a new library loaded
				List eventList = null;
				try {
					eventList = mgr.updateState();
				} catch (CDIException e3) {
					eventList = Collections.EMPTY_LIST;
				}
				// A new Libraries loaded, try to set the breakpoints.
				if (eventList.size() > 0) {
					ICDIBreakpointManager manager = session.getBreakpointManager();
					if (manager instanceof BreakpointManager) {
						BreakpointManager bpMgr = (BreakpointManager)manager;
						ICDIBreakpoint bpoints[] = null;
						try {
							bpoints = bpMgr.getDeferredBreakpoints();
						} catch (CDIException e) {
							bpoints = new ICDIBreakpoint[0];
						}
						for (int i = 0; i < bpoints.length; i++) {
							if (bpoints[i] instanceof Breakpoint) {
								Breakpoint bkpt = (Breakpoint)bpoints[i];
								try {
									bpMgr.setLocationBreakpoint(bkpt);
									bpMgr.deleteFromDeferredList(bkpt);
									bpMgr.addToBreakpointList(bkpt);
									// If the breakpoint was disable
									// install it but keep it disable
									if (!bkpt.isEnabled()) {
										bpMgr.disableBreakpoint(bkpt);
									}
									eventList.add(new MIBreakpointCreatedEvent(bkpt.getMIBreakpoint().getNumber()));
								} catch (CDIException e) {
									// ignore
								}
							}
						}
					}
					MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
					mi.fireEvents(events);
				}
				CommandFactory factory = mi.getCommandFactory();
				int type = (lastRunningEvent == null) ? MIRunningEvent.CONTINUE : lastRunningEvent.getType();
				if (lastUserCommand == null) {
					switch (type) {
						case MIRunningEvent.NEXT:
							lastUserCommand = factory.createMIExecNext();
							break;
						case MIRunningEvent.NEXTI:
							lastUserCommand = factory.createMIExecNextInstruction();
							break;
						case MIRunningEvent.STEP:
							lastUserCommand = factory.createMIExecStep();
							break;
						case MIRunningEvent.STEPI:
							lastUserCommand = factory.createMIExecStepInstruction();
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
								mi.postCommand(cont);
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
				ICDIThread currentThread = null;
				try {
					currentThread = currentTarget.getCurrentThread();
				} catch (CDIException e1) {
				}
				if (currentThread instanceof Thread) {
					tid = ((Thread)currentThread).getId();
				}
				ICDIStackFrame frame = null;
				try {
					frame = currentThread.getCurrentStackFrame();
				} catch (CDIException e2) {
				}
				int count = 0;
				try {
					MIStackInfoDepth depth = factory.createMIStackInfoDepth();
					mi.postCommand(depth);
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
				if (tid > 0) {
					MIThreadSelect selectThread = factory.createMIThreadSelect(tid);
					try {
						mi.postCommand(selectThread);
					} catch (MIException e) {
					}
				}
				if (miLevel >= 0) {
					MIStackSelectFrame selectFrame = factory.createMIStackSelectFrame(miLevel);
					MIExecFinish finish = factory.createMIExecFinish();
					try {
						mi.postCommand(selectFrame);
						mi.postCommand(finish);
					} catch (MIException e) {
					}
				}
				return true;
			} else if (lastUserCommand != null) {
				Command cmd = lastUserCommand;
				lastUserCommand = null;
				try {
					mi.postCommand(cmd);
				} catch (MIException e) {
				}
				return true;
			}
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
