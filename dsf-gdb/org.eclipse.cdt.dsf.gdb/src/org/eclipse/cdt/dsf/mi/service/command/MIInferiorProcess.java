/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Hewlett-Packard Development Company - fix for bug 109733
 *     Wind River Systems   - Modified for new DSF Reference Implementation
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
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.MIProcesses.ContainerExitedDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIExecAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowExitCodeInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResultRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MITargetStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

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
    
    public enum State { RUNNING, STOPPED, TERMINATED }

    private final OutputStream fOutputStream;
    private final InputStream fInputStream;

    private final PipedOutputStream fInputStreamPiped;

    private final PipedInputStream fErrorStream;
    private final PipedOutputStream fErrorStreamPiped;

    private final DsfSession fSession;
    private final PTY fPty;

    private final ICommandControlService fCommandControl;
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

    private State fState = State.RUNNING;
    
    @ConfinedToDsfExecutor("fSession#getExecutor")
    private String fInferiorPid = null;

    /**
     * Creates an inferior process object which uses the given output stream 
     * to write the user standard input into.
     *  
     * @param commandControl Command control that this inferior process belongs to.
     * @param inferiorExecCtx The execution context controlling the execution 
     * state of the inferior process.
     * @param gdbOutputStream The output stream to use to write user IO into.
     * @since 1.1
     */
    @ConfinedToDsfExecutor("fSession#getExecutor")
    public MIInferiorProcess(ICommandControlService commandControl, OutputStream gdbOutputStream) {
        this(commandControl, gdbOutputStream, null);
    }
    
    /**
     * Creates an inferior process object which uses the given terminal 
     * to write the user standard input into.
     *  
     * @param commandControl Command control that this inferior process belongs to.
     * @param inferiorExecCtx The execution context controlling the execution 
     * state of the inferior process.
     * @param p The terminal to use to write user IO into.
     * @since 1.1
     */
    @ConfinedToDsfExecutor("fSession#getExecutor")
    public MIInferiorProcess(ICommandControlService commandControl, PTY p) {
        this(commandControl, null, p);
    }
    
    @ConfinedToDsfExecutor("fSession#getExecutor")
    private MIInferiorProcess(ICommandControlService commandControl, final OutputStream gdbOutputStream, PTY p) {
        fCommandControl = commandControl;
        fSession = commandControl.getSession();
        
        if (fCommandControl instanceof IMICommandControl) {
        	fCommandFactory = ((IMICommandControl)fCommandControl).getCommandFactory();
        } else {
        	// Should not happen
        	fCommandFactory = new CommandFactory();
        }
        
        commandControl.addEventListener(this);
        commandControl.addCommandListener(this);
        
        fPty = p;
        if (fPty != null) {
            fOutputStream = fPty.getOutputStream();
            fInputStream = fPty.getInputStream();
            fInputStreamPiped = null;
        } else {
            fOutputStream = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    // Have to re-dispatch to dispatch thread to check state
                    if (getState() != State.RUNNING) {
                        throw new IOException("Target is not running"); //$NON-NLS-1$
                    }                        
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
        fCommandControl.removeEventListener(this);
        fCommandControl.removeCommandListener(this);
        
        closeIO();

        setState(State.TERMINATED);
        
        fDisposed = true;
    }

    @ThreadSafe
    protected DsfSession getSession() {
        return fSession;
    }
    
    /**
     * @since 1.1
     */
    @ConfinedToDsfExecutor("fSession#getExecutor")
    protected ICommandControlService getCommandControlService() { return fCommandControl; }
    
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
        assert !getSession().getExecutor().isInExecutorThread();
        
        while (getState() != State.TERMINATED) {
            wait(100);
        }        
    }

    /**
     * @see java.lang.Process#waitFor()
     */
    @ThreadSafeAndProhibitedFromDsfExecutor("fSession#getExecutor")
    @Override
    public int waitFor() throws InterruptedException {
        assert !getSession().getExecutor().isInExecutorThread();
        
        waitForSync();
        return exitValue();
    }

    @ThreadSafeAndProhibitedFromDsfExecutor("fSession#getExecutor")
    @Override
    public int exitValue() {
        assert !getSession().getExecutor().isInExecutorThread();
        
        synchronized (this) {
            if (fExitCode != null) {
                return fExitCode;
            }
        }
        
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
                    } else if (getState() != State.TERMINATED) {
                        // This will cause ExecutionException to be thrown with a CoreException, 
                        // which will in turn contain the IllegalThreadStateException.
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "GDB is still running.", new IllegalThreadStateException())); //$NON-NLS-1$
                        rm.done();
                    } else {
                    	getCommandControlService().queueCommand(
                    		fCommandFactory.createMIGDBShowExitCode(getCommandControlService().getContext()), 
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
            fOutputStream.close();
        } catch (IOException e) {}
        try {
            fInputStream.close();
        } catch (IOException e) {}
        try {
            if (fInputStreamPiped != null) fInputStreamPiped.close();
        } catch (IOException e) {}
        try {
            fErrorStream.close();
        } catch (IOException e) {}
        try {
            fErrorStreamPiped.close();
        } catch (IOException e) {}
    }
    
    @ConfinedToDsfExecutor("fSession#getExecutor")
    private void doDestroy() {
        if (isDisposed() || !fSession.isActive() || getState() == State.TERMINATED) return;

        // To avoid a RejectedExecutionException, use an executor that
        // immediately executes in the same dispatch cycle.
        ICommand<MIInfo> cmd = fCommandFactory.createCLIExecAbort(getCommandControlService().getContext());
        getCommandControlService().queueCommand(
            cmd,
            new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), null) { 
                @Override
                protected void handleCompleted() {
                    setState(MIInferiorProcess.State.TERMINATED);
                }
            }
        );         
    }

    @ThreadSafe
    public synchronized State getState() { 
        return fState;
    }

    @ConfinedToDsfExecutor("fSession#getExecutor")
    public IExecutionDMContext getExecutionContext() {
        return fContainerDMContext;
    }
    
    /**
	 * @since 1.1
	 */
    @ConfinedToDsfExecutor("fSession#getExecutor")
    public void setContainerContext(IContainerDMContext containerDmc) {
    	fContainerDMContext = containerDmc;
    }
    
    @ConfinedToDsfExecutor("fSession#getExecutor")
    synchronized void setState(State state) {
        if (fState == State.TERMINATED) return;
        fState = state;
        if (fState == State.TERMINATED) {
            if (fContainerDMContext != null) {
            	// This may not be necessary in 7.0 because of the =thread-group-exited event
                getSession().dispatchEvent(
                    new ContainerExitedDMEvent(fContainerDMContext), 
                    getCommandControlService().getProperties());
            }
            closeIO();
        }
        notifyAll();
    }

    public OutputStream getPipedOutputStream() {
        return fInputStreamPiped;
    }

    public OutputStream getPipedErrorStream() {
        return fErrorStreamPiped;
    }

    public PTY getPTY() {
        return fPty;
    }
    
    /**
     * @since 1.1
     */
    @ConfinedToDsfExecutor("fSession#getExecutor")
    public String getPid() { 
    	return fInferiorPid;
    }
    
    /**
     * @since 1.1
     */
    @ConfinedToDsfExecutor("fSession#getExecutor")
    public void setPid(String pid) { 
    	fInferiorPid = pid;
    }
    
    public void eventReceived(Object output) {
        for (MIOOBRecord oobr : ((MIOutput)output).getMIOOBRecords()) {
            if (oobr instanceof MIExecAsyncOutput) {
                MIExecAsyncOutput async = (MIExecAsyncOutput)oobr;
                
                String state = async.getAsyncClass();
                if ("stopped".equals(state)) { //$NON-NLS-1$
                    boolean handled = false;
                    MIResult[] results = async.getMIResults();
                    for (int i = 0; i < results.length; i++) {
                        String var = results[i].getVariable();
                        if (var.equals("reason")) { //$NON-NLS-1$
                            MIValue value = results[i].getMIValue();
                            if (value instanceof MIConst) {
                                String reason = ((MIConst) value).getString();
                                if ("exited-signalled".equals(reason) || "exited-normally".equals(reason) || "exited".equals(reason)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    setState(State.TERMINATED);
                                } else {
                                    setState(State.STOPPED);
                                }
                                handled = true;
                            }
                        }
                    }            
                    
                    if (!handled) {
                        setState(State.STOPPED);
                    }
                }
            } else if (oobr instanceof MITargetStreamOutput) {
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
    
    public void commandQueued(ICommandToken token) {
        // No action 
    }
    
    public void commandSent(ICommandToken token) {
        if (token.getCommand() instanceof CLICommand<?>) {
            fSuppressTargetOutputCounter++;
        }
    }
    
    public void commandRemoved(ICommandToken token) {
        // No action 
    }
    
    public void commandDone(ICommandToken token, ICommandResult result) {
    	if (token.getCommand() instanceof CLICommand<?>) {
            fSuppressTargetOutputCounter--;
        }

        MIInfo cmdResult = (MIInfo) result ;
        MIOutput output =  cmdResult.getMIOutput();
        MIResultRecord rr = output.getMIResultRecord();
    
        // Check if the state changed.
        String state = rr.getResultClass();
        
             if ("running".equals(state)) { setState(State.RUNNING); }//$NON-NLS-1$            
        else if ("exit".equals(state))    { setState(State.TERMINATED); }//$NON-NLS-1$            
        else if ("error".equals(state))   { setState(State.STOPPED); }//$NON-NLS-1$            
    }
}
