/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson			  - Version 7.0	
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecContinue;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecFinish;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecNext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecNextInstruction;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReturn;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecStep;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecStepInstruction;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecUntil;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIFunctionFinishedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIInferiorExitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIInferiorSignalExitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MILocationReachedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISignalEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISteppingRangeEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadCreatedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadExitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupCreatedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupExitedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIWatchpointScopeEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIWatchpointTriggerEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIExecAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResultRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * MI debugger output listener that listens for the parsed MI output, and
 * generates corresponding MI events.  The generated MI events are then
 * received by other services and clients.
 * @since 1.1
 */
public class MIRunControlEventProcessor_7_0
    implements IEventListener, ICommandListener
{
	private static final String STOPPED_REASON = "stopped"; //$NON-NLS-1$
	private static final String RUNNING_REASON = "running"; //$NON-NLS-1$
	   
	private Integer fLastRunningCmdType = null;
	/**
     * The connection service that this event processor is registered with.
     */
    private final AbstractMIControl fCommandControl;
 
    /**
     * Container context used as the context for the run control events generated
     * by this processor.
     */
    private final ICommandControlDMContext fControlDmc; 
    
    private final DsfServicesTracker fServicesTracker;
    
    /**
     * Creates the event processor and registers it as listener with the debugger
     * control.
     * @param connection
     * @param inferior
     */
    public MIRunControlEventProcessor_7_0(AbstractMIControl connection, ICommandControlDMContext controlDmc) {
        fCommandControl = connection;
        fControlDmc = controlDmc;
        fServicesTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fCommandControl.getSession().getId());
        connection.addEventListener(this);
        connection.addCommandListener(this);
    }

    /**
     * This processor must be disposed before the control service is un-registered. 
     */
    public void dispose() {
        fCommandControl.removeEventListener(this);        
        fCommandControl.removeCommandListener(this);
        fServicesTracker.dispose();
    }
    
    public void eventReceived(Object output) {
    	for (MIOOBRecord oobr : ((MIOutput)output).getMIOOBRecords()) {
			List<MIEvent<?>> events = new LinkedList<MIEvent<?>>();
    		if (oobr instanceof MIExecAsyncOutput) {
    			MIExecAsyncOutput exec = (MIExecAsyncOutput) oobr;
    			// Change of state.
    			String state = exec.getAsyncClass();
    			if (STOPPED_REASON.equals(state)) {
    				// Re-set the thread and stack level to -1 when stopped event is recvd. 
    				// This is to synchronize the state between GDB back-end and AbstractMIControl. 
    				fCommandControl.resetCurrentThreadLevel();
    				fCommandControl.resetCurrentStackLevel();

    				MIResult[] results = exec.getMIResults();
    				for (int i = 0; i < results.length; i++) {
    					String var = results[i].getVariable();
    					MIValue val = results[i].getMIValue();
    					if (var.equals("reason")) { //$NON-NLS-1$
    						if (val instanceof MIConst) {
    							String reason = ((MIConst) val).getString();
    							MIEvent<?> e = createEvent(reason, exec);
    							if (e != null) {
    								events.add(e);
    								continue;
    							}
    						}
    					}
    				}
        			// We were stopped for some unknown reason, for example
        			// GDB for temporary breakpoints will not send the
        			// "reason" ??? still fire a stopped event.
        			if (events.isEmpty()) {
        				MIEvent<?> e = createEvent(STOPPED_REASON, exec);
						if (e != null) {
							events.add(e);
						}
        			}

        			for (MIEvent<?> event : events) {
        				fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
        			}
    			}
    			else if (RUNNING_REASON.equals(state)) {
					MIEvent<?> event = createEvent(RUNNING_REASON, exec);
					if (event != null) {
						fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
					}
    			}
    		} else if (oobr instanceof MINotifyAsyncOutput) {
    			// Parse the string and dispatch the corresponding event
    			MINotifyAsyncOutput exec = (MINotifyAsyncOutput) oobr;
    			String miEvent = exec.getAsyncClass();
    			if ("thread-created".equals(miEvent) || "thread-exited".equals(miEvent)) { //$NON-NLS-1$ //$NON-NLS-2$
    				String threadId = null;
    				String groupId = null;

    				MIResult[] results = exec.getMIResults();
    				for (int i = 0; i < results.length; i++) {
    					String var = results[i].getVariable();
    					MIValue val = results[i].getMIValue();
    					if (var.equals("group-id")) { //$NON-NLS-1$
    						if (val instanceof MIConst) {
    							groupId = ((MIConst) val).getString();
    						}
    					} else if (var.equals("id")) { //$NON-NLS-1$
    		    			if (val instanceof MIConst) {
    							threadId = ((MIConst) val).getString();
    		    			}
    		    		}
    				}
    		    	
   		    		// Until GDB is officially supporting multi-process, we may not get
		    		// a groupId.  In this case, we are running single process and we'll
		    		// need its groupId
		    		if (groupId == null) {
		    			groupId = MIProcesses.UNIQUE_GROUP_ID;
		    		}

		    		// Here, threads are created and removed.  We cannot use the IMIProcesses service
		    		// to map a threadId to a groupId, because there would be a race condition.
		    		// Since we have the groupId anyway, we have no problems.
    		    	IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);

    		    	if (procService != null) {
    		    		IProcessDMContext procDmc = procService.createProcessContext(fControlDmc, groupId);
    		    		IContainerDMContext processContainerDmc = procService.createContainerContext(procDmc, groupId);

    		    		MIEvent<?> event = null;
    		    		if ("thread-created".equals(miEvent)) { //$NON-NLS-1$
    		    			event = new MIThreadCreatedEvent(processContainerDmc, exec.getToken(), threadId);
    		    		} else if ("thread-exited".equals(miEvent)) { //$NON-NLS-1$
    		    			event = new MIThreadExitEvent(processContainerDmc, exec.getToken(), threadId);
    		    		}
    		    		else {
    		    			assert false;	// earlier check should have guaranteed this isn't possible
    		    		}

    		    		fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
    		    	}
    			} else if ("thread-group-created".equals(miEvent) || "thread-group-exited".equals(miEvent)) { //$NON-NLS-1$ //$NON-NLS-2$
    				
    				String groupId = null;

    				MIResult[] results = exec.getMIResults();
    				for (int i = 0; i < results.length; i++) {
    					String var = results[i].getVariable();
    					MIValue val = results[i].getMIValue();
    					if (var.equals("id")) { //$NON-NLS-1$
    						if (val instanceof MIConst) {
    							groupId = ((MIConst) val).getString().trim();
    						}
    					}
    				}

					IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
    				if (groupId != null && procService != null) {
    					IProcessDMContext procDmc = procService.createProcessContext(fControlDmc, groupId);

    					MIEvent<?> event = null;
    					if ("thread-group-created".equals(miEvent)) { //$NON-NLS-1$
    						event = new MIThreadGroupCreatedEvent(procDmc, exec.getToken(), groupId);
    					} else if ("thread-group-exited".equals(miEvent)) { //$NON-NLS-1$
    						event = new MIThreadGroupExitedEvent(procDmc, exec.getToken(), groupId);
    					}

   						fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
    				}
    			}
    		}
    	}
    }

    @ConfinedToDsfExecutor("")     
    protected MIEvent<?> createEvent(String reason, MIExecAsyncOutput exec) {
    	MIEvent<?> event = null;

    	if ("exited-normally".equals(reason) || "exited".equals(reason)) { //$NON-NLS-1$ //$NON-NLS-2$
    		event = MIInferiorExitEvent.parse(fCommandControl.getContext(), exec.getToken(), exec.getMIResults());
    	} else if ("exited-signalled".equals(reason)) { //$NON-NLS-1$
    		event = MIInferiorSignalExitEvent.parse(fCommandControl.getContext(), exec.getToken(), exec.getMIResults());
    	} else {

    		String threadId = null; 
    		String groupId = null;

    		MIResult[] results = exec.getMIResults();
    		for (int i = 0; i < results.length; i++) {
    			String var = results[i].getVariable();
    			MIValue val = results[i].getMIValue();

    			if (var.equals("thread-id")) { //$NON-NLS-1$
    				if (val instanceof MIConst) {
    					threadId = ((MIConst)val).getString();
    				}
    			} else if (var.equals("group-id")) { //$NON-NLS-1$
    				if (val instanceof MIConst) {
    					groupId = ((MIConst)val).getString();
    				}
    			}
    		}

    		IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
    		if (procService == null) {
    			return null;
    		}

    		IProcessDMContext procDmc = null;
    		IContainerDMContext containerDmc = null;
    		if (groupId == null) {
    			// MI does not currently provide the group-id in these events
    			
    			// In some cases, gdb sends a bare stopped event. Likely a bug, but 
    			// we need to react to it all the same. See bug 311118.
    			if (threadId == null) {
    				threadId = "all"; //$NON-NLS-1$
    			}
    			
   				containerDmc = procService.createContainerContextFromThreadId(fControlDmc, threadId);
   				procDmc = DMContexts.getAncestorOfType(containerDmc, IProcessDMContext.class);
    		} else {
    			// This code would only trigger if the groupId was provided by MI
    			procDmc = procService.createProcessContext(fControlDmc, groupId);
    			containerDmc = procService.createContainerContext(procDmc, groupId);
    		}

    		IExecutionDMContext execDmc = containerDmc;
    		if (threadId != null && !threadId.equals("all")) { //$NON-NLS-1$
    			IThreadDMContext threadDmc = procService.createThreadContext(procDmc, threadId);
    			execDmc = procService.createExecutionContext(containerDmc, threadDmc, threadId);
    		}

    		if (execDmc == null) {
    			// Badly formatted event
    			return null;
    		}

    		if ("breakpoint-hit".equals(reason)) { //$NON-NLS-1$
    			event = MIBreakpointHitEvent.parse(execDmc, exec.getToken(), exec.getMIResults());
    		} else if (
    				"watchpoint-trigger".equals(reason) //$NON-NLS-1$
    				|| "read-watchpoint-trigger".equals(reason) //$NON-NLS-1$
    				|| "access-watchpoint-trigger".equals(reason)) { //$NON-NLS-1$
    			event = MIWatchpointTriggerEvent.parse(execDmc, exec.getToken(), exec.getMIResults());
    		} else if ("watchpoint-scope".equals(reason)) { //$NON-NLS-1$
    			event = MIWatchpointScopeEvent.parse(execDmc, exec.getToken(), exec.getMIResults());
    		} else if ("end-stepping-range".equals(reason)) { //$NON-NLS-1$
    			event = MISteppingRangeEvent.parse(execDmc, exec.getToken(), exec.getMIResults());
    		} else if ("signal-received".equals(reason)) { //$NON-NLS-1$
    			event = MISignalEvent.parse(execDmc, exec.getToken(), exec.getMIResults());
    		} else if ("location-reached".equals(reason)) { //$NON-NLS-1$
    			event = MILocationReachedEvent.parse(execDmc, exec.getToken(), exec.getMIResults());
    		} else if ("function-finished".equals(reason)) { //$NON-NLS-1$
    			event = MIFunctionFinishedEvent.parse(execDmc, exec.getToken(), exec.getMIResults());
    		} else if (STOPPED_REASON.equals(reason)) {
    			event = MIStoppedEvent.parse(execDmc, exec.getToken(), exec.getMIResults());
    		} else if (RUNNING_REASON.equals(reason)) {
    			// Retrieve the type of command from what we last stored
    			int type = MIRunningEvent.CONTINUE;
    			if (fLastRunningCmdType != null) {
    				type = fLastRunningCmdType;
    				fLastRunningCmdType = null;
    			}
    			event = new MIRunningEvent(execDmc, exec.getToken(), type);
    		}
    	}
    	return event;
    }
    
    public void commandQueued(ICommandToken token) {
        // Do nothing.
    }
    
    public void commandSent(ICommandToken token) {
        // Do nothing.
    }
    
    public void commandRemoved(ICommandToken token) {
        // Do nothing.
    }
    
    public void commandDone(ICommandToken token, ICommandResult result) {
        ICommand<?> cmd = token.getCommand();
    	MIInfo cmdResult = (MIInfo) result ;
    	MIOutput output =  cmdResult.getMIOutput();
    	MIResultRecord rr = output.getMIResultRecord();
        if (rr != null) {
            // Check if the state changed.
            String state = rr.getResultClass();
            if (RUNNING_REASON.equals(state)) {
            	// Store the type of command that is the trigger for the coming
            	// *running event
                     if (cmd instanceof MIExecNext)            { fLastRunningCmdType = MIRunningEvent.NEXT; }
                else if (cmd instanceof MIExecNextInstruction) { fLastRunningCmdType = MIRunningEvent.NEXTI; }
                else if (cmd instanceof MIExecStep)            { fLastRunningCmdType = MIRunningEvent.STEP; }
                else if (cmd instanceof MIExecStepInstruction) { fLastRunningCmdType = MIRunningEvent.STEPI; }
                else if (cmd instanceof MIExecUntil)           { fLastRunningCmdType = MIRunningEvent.UNTIL; }
                else if (cmd instanceof MIExecFinish)          { fLastRunningCmdType = MIRunningEvent.FINISH; }
                else if (cmd instanceof MIExecReturn)          { fLastRunningCmdType = MIRunningEvent.RETURN; }
                else if (cmd instanceof MIExecContinue)        { fLastRunningCmdType = MIRunningEvent.CONTINUE; }
                else                                           { fLastRunningCmdType = MIRunningEvent.CONTINUE; }
            } 
        }
    }
}
