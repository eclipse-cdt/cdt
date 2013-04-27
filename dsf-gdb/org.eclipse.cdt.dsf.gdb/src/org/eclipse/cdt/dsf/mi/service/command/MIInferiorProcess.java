/*******************************************************************************
 * Copyright (c) 2009, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Hewlett-Packard Development Company - fix for bug 109733
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Marc Khouzam (Ericsson) - Display exit code in process console (Bug 402054)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupExitedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowExitCodeInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MITargetStreamOutput;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

/**
 * This Process implementation tracks the process that is being debugged 
 * by GDB.  The process object is displayed in Debug view and is used to
 * channel the STDIO of the interior process to the console view.  
 * 
 * @see org.eclipse.debug.core.model.IProcess 
 */
public class MIInferiorProcess extends Process 
    implements IEventListener, ICommandListener 
{
    
	// Indicates that the inferior has been started
	// This is important for the case of a restart
	// where we need to make sure not to terminate
	// the new inferior, which was not started yet.
	private boolean fStarted;
	
	// Indicates that the inferior has been terminated
    private boolean fTerminated;

    private OutputStream fOutputStream;
    private InputStream fInputStream;

    private PipedOutputStream fInputStreamPiped;

    private PipedInputStream fErrorStream;
    private PipedOutputStream fErrorStreamPiped;

    private final DsfSession fSession;

    private final IMICommandControl fCommandControl;
    private CommandFactory fCommandFactory;

    private IContainerDMContext fContainerDMContext;
    
    @ConfinedToDsfExecutor("fSession#getExecutor")
    private boolean fDisposed = false;
    
    /**
     * Counter for tracking console commands sent by services.  
     * 
     * The CLI 'monitor' command produces target output which should 
     * not be written to the target console, since it is in response to a CLI
     * command.  In fact, CLI commands should never have their output sent
     * to the target console.
     *   
     * This counter is incremented any time a CLI command is seen.  It is 
     * decremented whenever a CLI command is finished.  When counter 
     * value is 0, the inferior process writes the target output. 
     */
    private int fSuppressTargetOutputCounter = 0;

    @ThreadSafe
    Integer fExitCode = null;
 
    /**
     * Creates an inferior process object which uses the given output stream 
     * to write the user standard input into.
     *  
     * @param container The process that this inferior represents
     * @param gdbOutputStream The output stream to use to write user IO into.
     * @since 4.0
     */
    @ConfinedToDsfExecutor("fSession#getExecutor")
    public MIInferiorProcess(IContainerDMContext container, OutputStream gdbOutputStream) {
        this(container, gdbOutputStream, null);
    }
    
    /**
     * Creates an inferior process object which uses the given terminal 
     * to write the user standard input into.
     *  
     * @param container The process that this inferior represents
     * @param p The terminal to use to write user IO into.
     * @since 4.0
     */
    @ConfinedToDsfExecutor("fSession#getExecutor")
    public MIInferiorProcess(IContainerDMContext container, PTY p) {
        this(container, null, p);
    }
    
    @ConfinedToDsfExecutor("fSession#getExecutor")
    private MIInferiorProcess(IContainerDMContext container, final OutputStream gdbOutputStream, PTY pty) {
        fSession = DsfSession.getSession(container.getSessionId());
        fSession.addServiceEventListener(this, null);
        
        fContainerDMContext = container;
        
        DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSession.getId());
        fCommandControl = tracker.getService(IMICommandControl.class);
        tracker.dispose();
        
       	fCommandFactory = fCommandControl.getCommandFactory();

       	fCommandControl.addEventListener(this);
       	fCommandControl.addCommandListener(this);
        
        if (pty != null) {
            fOutputStream = pty.getOutputStream();
            fInputStream = pty.getInputStream();
            fInputStreamPiped = null;
        } else {
            fOutputStream = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    gdbOutputStream.write(b);
                }
            };
            
            fInputStreamPiped = new PipedOutputStream();
            PipedInputStream inputStream = null; 
            try {
                // Using a LargePipedInputStream see https://bugs.eclipse.org/bugs/show_bug.cgi?id=223154
                inputStream = new LargePipedInputStream(fInputStreamPiped);
            } catch (IOException e) {
            }
            fInputStream = inputStream;
            
        }
        
        // Note: We do not have any err stream from gdb/mi so this gdb 
        // err channel instead.
        fErrorStreamPiped = new PipedOutputStream();
        PipedInputStream errorStream = null; 
        try {
            // Using a LargePipedInputStream see https://bugs.eclipse.org/bugs/show_bug.cgi?id=223154
            errorStream = new LargePipedInputStream(fErrorStreamPiped);
        } catch (IOException e) {
        }
        fErrorStream = errorStream;
    }

    @ConfinedToDsfExecutor("fSession#getExecutor")
    public void dispose() {
    	fSession.removeServiceEventListener(this);
    	
        fCommandControl.removeEventListener(this);
        fCommandControl.removeCommandListener(this);
        
        closeIO();

        setTerminated();
        
        fDisposed = true;
    }
        
    @ConfinedToDsfExecutor("fSession#getExecutor")
    protected boolean isDisposed() { return fDisposed; }
    
    @Override
    public OutputStream getOutputStream() {
        return fOutputStream;
    }

    @Override
    public InputStream getInputStream() {
        return fInputStream;
    }

    @Override
    public InputStream getErrorStream() {
        return fErrorStream;
    }

    @ThreadSafeAndProhibitedFromDsfExecutor("fSession#getExecutor")
    public synchronized void waitForSync() throws InterruptedException {
        assert !fSession.getExecutor().isInExecutorThread();
        
        while (!fTerminated) {
            wait(100);
        }        
    }

    /**
     * @see java.lang.Process#waitFor()
     */
    @ThreadSafeAndProhibitedFromDsfExecutor("fSession#getExecutor")
    @Override
    public int waitFor() throws InterruptedException {
        assert !fSession.getExecutor().isInExecutorThread();
        
        waitForSync();
        return exitValue();
    }

    @ThreadSafeAndProhibitedFromDsfExecutor("fSession#getExecutor")
    @Override
    public int exitValue() {
        assert !fSession.getExecutor().isInExecutorThread();
        
        synchronized (this) {
            if (fExitCode != null) {
                return fExitCode;
            }
        }
        
        // The below should only be used for GDB < 7.3 because $_exitcode does not
        // support multi-process properly.  Note that for GDB 7.2, there is no proper
        // solution because although we support multi-process, $_exitcode was still the
        // only way to get the exit code.  For GDB >= 7.3, we can use =thread-group-exited
        // which provides the exit code; in fact, fExitCode will already be set for that case.
        try {
            Query<Integer> exitCodeQuery = new Query<Integer>() {
                @Override
                protected void execute(final DataRequestMonitor<Integer> rm) {
                    // Guard against session disposed.
                    if (!DsfSession.isSessionActive(fSession.getId())) {
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Debug session already shut down.", null)); //$NON-NLS-1$
                        rm.done();
                        return;
                    }

                    if (isDisposed()) {
                        rm.setData(0);
                        rm.done();
                    } else if (!fTerminated) {
                        // This will cause ExecutionException to be thrown with a CoreException, 
                        // which will in turn contain the IllegalThreadStateException.
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "GDB is still running.", new IllegalThreadStateException())); //$NON-NLS-1$
                        rm.done();
                    } else {
                    	// The exitCode from GDB does not seem to be handled for multi-process
                    	// so there is no point is specifying the container
                    	fCommandControl.queueCommand(
                    		fCommandFactory.createMIGDBShowExitCode(fCommandControl.getContext()), 
                            new DataRequestMonitor<MIGDBShowExitCodeInfo>(fSession.getExecutor(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    rm.setData(getData().getCode());
                                    rm.done();
                                }
                            });

                    }
                }
            };
            fSession.getExecutor().execute(exitCodeQuery);

            int exitCode = exitCodeQuery.get(); 
            synchronized(this) {
                fExitCode = exitCode;
            }
            return exitCode;
        } catch (RejectedExecutionException e) {
        } catch (InterruptedException e) {
        } catch (CancellationException e) {
        } catch (ExecutionException e) {
            if (e.getCause() instanceof CoreException && 
                ((CoreException)e.getCause()).getStatus().getException() instanceof RuntimeException ) 
            {
                throw (RuntimeException)((CoreException)e.getCause()).getStatus().getException();
            }
        }
        return 0;
    }

    /**
     * @see java.lang.Process#destroy()
     */
    @Override
    public void destroy() {
        try {
            fSession.getExecutor().execute(new DsfRunnable() {
            	@Override
                public void run() {
                    doDestroy();
                }
            });
        } catch (RejectedExecutionException e) {
            // Session disposed.
        }
        closeIO();
    }
    
    private void closeIO() {
        try {
        	if (fOutputStream != null) fOutputStream.close();
            // Make sure things get GCed
            fOutputStream = null;
        } catch (IOException e) {}
        try {
        	if (fInputStream != null) fInputStream.close();
            // Make sure things get GCed
            fInputStream = null;
        } catch (IOException e) {}
        try {
            if (fInputStreamPiped != null) fInputStreamPiped.close();
            // Make sure things get GCed
            fInputStreamPiped = null;
        } catch (IOException e) {}
        try {
        	if (fErrorStream != null) fErrorStream.close();
            // Make sure things get GCed
            fErrorStream = null;
        } catch (IOException e) {}
        try {
        	if (fErrorStreamPiped != null) fErrorStreamPiped.close();
            // Make sure things get GCed
            fErrorStreamPiped = null;
        } catch (IOException e) {}
    }
    
    @ConfinedToDsfExecutor("fSession#getExecutor")
    private void doDestroy() {
        if (isDisposed() || !fSession.isActive() || fTerminated) return;

        DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSession.getId());
        IProcesses procService = tracker.getService(IProcesses.class);
        tracker.dispose();
        if (procService != null) {
        	IProcessDMContext procDmc = DMContexts.getAncestorOfType(fContainerDMContext, IProcessDMContext.class);
        	procService.terminate(procDmc, new ImmediateRequestMonitor());
        } else {
        	setTerminated();
        }
    }
	
    @ConfinedToDsfExecutor("fSession#getExecutor")
    private synchronized void setTerminated() {
        if (fTerminated) return;
        fTerminated = true;
        closeIO();
        notifyAll();
    }

    public OutputStream getPipedOutputStream() {
        return fInputStreamPiped;
    }

    public OutputStream getPipedErrorStream() {
        return fErrorStreamPiped;
    }
    
	@Override
    public void eventReceived(Object output) {
        for (MIOOBRecord oobr : ((MIOutput)output).getMIOOBRecords()) {
        	if (oobr instanceof MITargetStreamOutput) {
            	if (fSuppressTargetOutputCounter > 0) return;
                MITargetStreamOutput tgtOut = (MITargetStreamOutput)oobr;
                if (fInputStreamPiped != null && tgtOut.getString() != null) {
                    try {
                        fInputStreamPiped.write(tgtOut.getString().getBytes());
                        fInputStreamPiped.flush();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }
    
	@Override
    public void commandQueued(ICommandToken token) {
        // No action 
    }
    
	@Override
    public void commandSent(ICommandToken token) {
        if (token.getCommand() instanceof CLICommand<?>) {
            fSuppressTargetOutputCounter++;
        }
    }
    
	@Override
    public void commandRemoved(ICommandToken token) {
        // No action 
    }
    
	@Override
    public void commandDone(ICommandToken token, ICommandResult result) {
    	if (token.getCommand() instanceof CLICommand<?>) {
            fSuppressTargetOutputCounter--;
        }
    }

    /**
	 * @since 4.0
	 */
    @DsfServiceEventHandler
    public void eventDispatched(IExitedDMEvent e) {
    	if (e.getDMContext() instanceof IMIContainerDMContext) {
    		// For multi-process, make sure the exited event
    		// is actually for this inferior.
    		if (e.getDMContext().equals(fContainerDMContext)) {
    			if (fStarted) {
    				// Only mark this process as terminated if it was already
    				// started.  This is to protect ourselves in the case of
    				// a restart, where the new inferior is already created
    				// and gets the exited event for the old inferior.
    				setTerminated();
    			}
    		}
    	}
    }
    
    /** @since 4.2 */
    @DsfServiceEventHandler
    public void eventDispatched(MIThreadGroupExitedEvent e) {
		if (fContainerDMContext instanceof IMIContainerDMContext) {
			if (((IMIContainerDMContext)fContainerDMContext).getGroupId().equals(e.getGroupId())) {
    			if (fStarted) {
    				// Only handle this event if this process was already
    				// started.  This is to protect ourselves in the case of
    				// a restart, where the new inferior is already created
    				// and gets the exited event for the old inferior.
    				String exitCode = e.getExitCode();
    				if (exitCode != null) {
    					setExitCodeAttribute();
    					try {
    						// Must use 'decode' since GDB returns an octal value
    						Integer decodedExitCode = Integer.decode(exitCode);
			        		synchronized(this) {
    			            	fExitCode = decodedExitCode;
    			            }
    					} catch (NumberFormatException exception) {
    					}    					
    				}
    			}
    		}
    	}
    }
    
    /**
	 * @since 4.0
	 */
    @DsfServiceEventHandler
    public void eventDispatched(IStartedDMEvent e) {
    	if (e.getDMContext() instanceof IMIContainerDMContext) {
    		// Mark the inferior started if the event is for this inferior.
    		// We may get other started events in the cases of a restarts
    		if (!fStarted) {
    			// For multi-process, make sure the started event
    			// is actually for this inferior.
    			// We must compare the groupId and not the full context
    			// because the container that we currently hold is incomplete
    			// because the pid was not determined yet.
    			String inferiorGroup = ((IMIContainerDMContext)fContainerDMContext).getGroupId();

    			if (inferiorGroup == null || inferiorGroup.length() == 0) {
    				// Single process case, so we know we have started
    				fStarted = true;
    				// Store the fully-formed container
    				fContainerDMContext = (IMIContainerDMContext)e.getDMContext();
    			} else {
    				String startedGroup = ((IMIContainerDMContext)e.getDMContext()).getGroupId();
    				if (inferiorGroup.equals(startedGroup)) {
    					fStarted = true;
        				// Store the fully-formed container
        				fContainerDMContext = (IMIContainerDMContext)e.getDMContext();
    				}
    			}
    		}
    	}
    }
    
    /**
	 * @since 4.0
	 */
    @DsfServiceEventHandler
    public void eventDispatched(ICommandControlShutdownDMEvent e) {
    	dispose();
    }

	/**
	 * Set an attribute in the inferior process of the launch to indicate
	 * that the inferior has properly exited and its exit value can be used.
	 */
    @ConfinedToDsfExecutor("fSession#getExecutor")
	private void setExitCodeAttribute() {
		// Update the console label to contain the exit code
		ILaunch launch = (ILaunch)fSession.getModelAdapter(ILaunch.class);
		IProcess[] launchProcesses = launch.getProcesses();
		for (IProcess proc : launchProcesses) {
			if (proc instanceof InferiorRuntimeProcess) {
				InferiorRuntimeProcess process = (InferiorRuntimeProcess)proc;
				String groupAttribute = process.getAttribute(IGdbDebugConstants.INFERIOR_GROUPID_ATTR);

				// if the groupAttribute is not set in the process we know we are dealing
				// with single process debugging so the one process is the one we want.
				// If the groupAttribute is set, then we must make sure it is the proper inferior
				if (fContainerDMContext instanceof IMIContainerDMContext) {
					if (groupAttribute == null || groupAttribute.equals(MIProcesses.UNIQUE_GROUP_ID) ||
							groupAttribute.equals(((IMIContainerDMContext)fContainerDMContext).getGroupId())) {
						// Simply set the attribute that indicates the inferior has properly exited and its
						// exit code can be used.
						process.setAttribute(IGdbDebugConstants.INFERIOR_EXITED_ATTR, ""); //$NON-NLS-1$
						return;
					}
				}
			}
		}
	}
}
