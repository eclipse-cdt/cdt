/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson			  - Additional handling of events  	
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses.ContainerExitedDMEvent;
import org.eclipse.cdt.dsf.mi.service.MIProcesses.ContainerStartedDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecContinue;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecFinish;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecNext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecNextInstruction;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReturn;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecStep;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecStepInstruction;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecUntil;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MICatchpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIFunctionFinishedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIInferiorExitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIInferiorSignalExitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MILocationReachedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISignalEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISteppingRangeEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIWatchpointScopeEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIWatchpointTriggerEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIExecAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResultRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStreamRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * MI debugger output listener that listens for the parsed MI output, and
 * generates corresponding MI events.  The generated MI events are then
 * received by other services and clients.
 */
public class MIRunControlEventProcessor 
    implements IEventListener, ICommandListener
{
	private static final String STOPPED_REASON = "stopped"; //$NON-NLS-1$
	   
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
     * @since 1.1
     */
    public MIRunControlEventProcessor(AbstractMIControl connection, ICommandControlDMContext controlDmc) {
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
    
	@Override
    public void eventReceived(Object output) {
    	for (MIOOBRecord oobr : ((MIOutput)output).getMIOOBRecords()) {
			List<MIEvent<?>> events = new LinkedList<MIEvent<?>>();
    		if (oobr instanceof MIExecAsyncOutput) {
    			MIExecAsyncOutput exec = (MIExecAsyncOutput) oobr;
    			// Change of state.
    			String state = exec.getAsyncClass();
    			if ("stopped".equals(state)) { //$NON-NLS-1$
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
    				
					// GDB < 7.0 does not provide a reason when stopping on a
					// catchpoint. However, the reason is contained in the
					// stream records that precede the exec async output one.
					// This is ugly, but we don't really have an alternative.
    				if (events.isEmpty()) {
    					MIStreamRecord[] streamRecords = ((MIOutput)output).getStreamRecords();
    					for (MIStreamRecord streamRecord : streamRecords) {
    						String log = streamRecord.getString();
    						if (log.startsWith("Catchpoint ")) { //$NON-NLS-1$
    							events.add(MICatchpointHitEvent.parse(getExecutionContext(exec), exec.getToken(), results, streamRecord));
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
    		}
    	}
    	
    	// Now check for a oob command result.  This happens on Windows when interrupting GDB.
    	// In this case, GDB before 7.0 does not always send a *stopped event, so we must do it ourselves
    	// Bug 304096 (if you have the patience to go through it :-))
    	MIResultRecord rr = ((MIOutput)output).getMIResultRecord();
    	if (rr != null) {
    		int id = rr.getToken();
    		String state = rr.getResultClass();
    		if ("error".equals(state)) { //$NON-NLS-1$

    			MIResult[] results = rr.getMIResults();
    			for (int i = 0; i < results.length; i++) {
    				String var = results[i].getVariable();
    				MIValue val = results[i].getMIValue();
    				if (var.equals("msg")) { //$NON-NLS-1$
    					if (val instanceof MIConst) {
    						String message = ((MIConst) val).getString();
    						if (message.toLowerCase().startsWith("quit")) { //$NON-NLS-1$
    							IRunControl runControl = fServicesTracker.getService(IRunControl.class);
    							IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
    							if (runControl != null && procService != null) {
    								// We don't know which thread stopped so we simply create a container event.
    								IContainerDMContext processContainerDmc = procService.createContainerContextFromGroupId(fControlDmc, MIProcesses.UNIQUE_GROUP_ID);

    								if (runControl.isSuspended(processContainerDmc) == false) {
    									// Create an MISignalEvent because that is what the *stopped event should have been
    									MIEvent<?> event = MISignalEvent.parse(processContainerDmc, id, rr.getMIResults());
    									fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    }

	/**
	 * Create an execution context given an exec-async-output OOB record 
	 * 
	 * @since 3.0
	 */
    protected IExecutionDMContext getExecutionContext(MIExecAsyncOutput exec) {
    	String threadId = null; 

    	MIResult[] results = exec.getMIResults();
    	for (int i = 0; i < results.length; i++) {
    		String var = results[i].getVariable();
    		MIValue val = results[i].getMIValue();

    		if (var.equals("thread-id")) { //$NON-NLS-1$
    			if (val instanceof MIConst) {
    				threadId = ((MIConst)val).getString();
    			}
    		}
    	}

    	IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
        if (procService == null) {
        	return null;
        }
        
    	IContainerDMContext processContainerDmc = procService.createContainerContextFromGroupId(fControlDmc, MIProcesses.UNIQUE_GROUP_ID);

    	IExecutionDMContext execDmc = processContainerDmc;
    	if (threadId != null) {
    		IProcessDMContext procDmc = DMContexts.getAncestorOfType(processContainerDmc, IProcessDMContext.class);
   			IThreadDMContext threadDmc = procService.createThreadContext(procDmc, threadId);
   			execDmc = procService.createExecutionContext(processContainerDmc, threadDmc, threadId);
    	}
    	
    	return execDmc;
    }

    @ConfinedToDsfExecutor("")
    protected MIEvent<?> createEvent(String reason, MIExecAsyncOutput exec) {
    	IExecutionDMContext execDmc = getExecutionContext(exec);
    	MIEvent<?> event = null;
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
    	} else if ("exited-normally".equals(reason) || "exited".equals(reason)) { //$NON-NLS-1$ //$NON-NLS-2$
    		event = MIInferiorExitEvent.parse(fCommandControl.getContext(), exec.getToken(), exec.getMIResults());
    		// Until we clean up the handling of all these events, we need to send the containerExited event
    		// Only needed GDB < 7.0, because GDB itself does not yet send an MI event about the inferior terminating
    		sendContainerExitedEvent();
    	} else if ("exited-signalled".equals(reason)) { //$NON-NLS-1$
    		event = MIInferiorSignalExitEvent.parse(fCommandControl.getContext(), exec.getToken(), exec.getMIResults());
    		// Until we clean up the handling of all these events, we need to send the containerExited event
    		// Only needed GDB < 7.0, because GDB itself does not yet send an MI event about the inferior terminating
    		sendContainerExitedEvent();
    	} else if (STOPPED_REASON.equals(reason)) {
    		event = MIStoppedEvent.parse(execDmc, exec.getToken(), exec.getMIResults());
    	}
    	return event;
    }
    
    private void sendContainerExitedEvent() {
    	IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
    	if (procService != null) {
    		IContainerDMContext processContainerDmc = procService.createContainerContextFromGroupId(fControlDmc, MIProcesses.UNIQUE_GROUP_ID);

    		fCommandControl.getSession().dispatchEvent(
    				new ContainerExitedDMEvent(processContainerDmc), fCommandControl.getProperties());
    	}
    }
    
	@Override
    public void commandQueued(ICommandToken token) {
        // Do nothing.
    }
    
	@Override
    public void commandSent(ICommandToken token) {
        // Do nothing.
    }
    
	@Override
    public void commandRemoved(ICommandToken token) {
        // Do nothing.
    }
    
	@Override
    public void commandDone(ICommandToken token, ICommandResult result) {
        ICommand<?> cmd = token.getCommand();
    	MIInfo cmdResult = (MIInfo) result ;
    	MIOutput output =  cmdResult.getMIOutput();
    	MIResultRecord rr = output.getMIResultRecord();
        if (rr != null) {
            int id = rr.getToken();
            // Check if the state changed.
            String state = rr.getResultClass();
            if ("running".equals(state)) { //$NON-NLS-1$
                int type = 0;
                // Check the type of command
                // if it was a step instruction set state stepping
                
                     if (cmd instanceof MIExecNext)            { type = MIRunningEvent.NEXT; }
                else if (cmd instanceof MIExecNextInstruction) { type = MIRunningEvent.NEXTI; }
                else if (cmd instanceof MIExecStep)            { type = MIRunningEvent.STEP; }
                else if (cmd instanceof MIExecStepInstruction) { type = MIRunningEvent.STEPI; }
                else if (cmd instanceof MIExecUntil)           { type = MIRunningEvent.UNTIL; }
                else if (cmd instanceof MIExecFinish)          { type = MIRunningEvent.FINISH; }
                else if (cmd instanceof MIExecReturn)          { type = MIRunningEvent.RETURN; }
                else if (cmd instanceof MIExecContinue)        { type = MIRunningEvent.CONTINUE; }
                else                                           { type = MIRunningEvent.CONTINUE; }

                IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
                if (procService != null) {
                	IContainerDMContext processContainerDmc = procService.createContainerContextFromGroupId(fControlDmc, MIProcesses.UNIQUE_GROUP_ID);

                	fCommandControl.getSession().dispatchEvent(
                			new MIRunningEvent(processContainerDmc, id, type), fCommandControl.getProperties());
                }
            } else if ("exit".equals(state)) { //$NON-NLS-1$
                // No need to do anything, terminate() will.
                // Send exited?
            } else if ("connected".equals(state)) { //$NON-NLS-1$
            	// This will happen for a CORE or REMOTE session.
            	// For a CORE session this is the only indication
            	// that the inferior has 'started'.  So we use
            	// it to trigger the ContainerStarted event.
            	// In the case of a REMOTE session, it is a proper
            	// indicator as well but not if it is a remote attach.
            	// For an attach session, it only indicates
            	// that we are connected to a remote node but we still
            	// need to wait until we are attached to the process before
            	// sending the event, which will happen in the attaching code.
                IGDBBackend backendService = fServicesTracker.getService(IGDBBackend.class);
                if (backendService != null && backendService.getIsAttachSession() == false) {
                	IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
                	if (procService != null) {
                		IContainerDMContext processContainerDmc = procService.createContainerContextFromGroupId(fControlDmc, MIProcesses.UNIQUE_GROUP_ID);

                		fCommandControl.getSession().dispatchEvent(
                				new ContainerStartedDMEvent(processContainerDmc), fCommandControl.getProperties());
                	}
                }            	
            } else if ("error".equals(state)) { //$NON-NLS-1$
            } else if ("done".equals(state)) { //$NON-NLS-1$
            	// For GDBs older than 7.0, GDB does not trigger a *stopped event
            	// when it stops due to a CLI command.  We have to trigger the 
            	// MIStoppedEvent ourselves
            	if (cmd instanceof CLICommand<?>) {
            		// It is important to limit this to runControl operations (e.g., 'next', 'continue', 'jump')
            		// There are other CLI commands that we use that could still be sent when the target is considered
            		// running, due to timing issues.
            		boolean isAttachingOperation = CLIEventProcessor.isAttachingOperation(((CLICommand<?>)cmd).getOperation());
            		boolean isSteppingOperation = CLIEventProcessor.isSteppingOperation(((CLICommand<?>)cmd).getOperation());
            		if (isSteppingOperation || isAttachingOperation) {
            			IRunControl runControl = fServicesTracker.getService(IRunControl.class);
            			IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
            			if (runControl != null && procService != null) {
            				// We don't know which thread stopped so we simply create a container event.
            				IContainerDMContext processContainerDmc = procService.createContainerContextFromGroupId(fControlDmc, MIProcesses.UNIQUE_GROUP_ID);

            				// An attaching operation is debugging a new inferior and always stops it.
            				// We should not check that the container is suspended, because at startup, we are considered
            				// suspended, even though we can get a *stopped event.
            				if (isAttachingOperation || runControl.isSuspended(processContainerDmc) == false) {
            					MIEvent<?> event = MIStoppedEvent.parse(processContainerDmc, id, rr.getMIResults());
            					fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
            				}
            			}
            		}
            	}
            }
        }
    }
}
