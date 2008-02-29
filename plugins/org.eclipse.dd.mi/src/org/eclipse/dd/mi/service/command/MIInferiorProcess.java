/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
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

package org.eclipse.dd.mi.service.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.ImmediateExecutor;
import org.eclipse.dd.dsf.concurrent.Query;
import org.eclipse.dd.dsf.debug.service.command.ICommand;
import org.eclipse.dd.dsf.debug.service.command.ICommandListener;
import org.eclipse.dd.dsf.debug.service.command.ICommandResult;
import org.eclipse.dd.dsf.debug.service.command.IEventListener;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.mi.internal.MIPlugin;
import org.eclipse.dd.mi.service.command.commands.CLICommand;
import org.eclipse.dd.mi.service.command.commands.CLIExecAbort;
import org.eclipse.dd.mi.service.command.commands.MIGDBShowExitCode;
import org.eclipse.dd.mi.service.command.output.MIConst;
import org.eclipse.dd.mi.service.command.output.MIExecAsyncOutput;
import org.eclipse.dd.mi.service.command.output.MIGDBShowExitCodeInfo;
import org.eclipse.dd.mi.service.command.output.MIInfo;
import org.eclipse.dd.mi.service.command.output.MIOOBRecord;
import org.eclipse.dd.mi.service.command.output.MIOutput;
import org.eclipse.dd.mi.service.command.output.MIResult;
import org.eclipse.dd.mi.service.command.output.MIResultRecord;
import org.eclipse.dd.mi.service.command.output.MITargetStreamOutput;
import org.eclipse.dd.mi.service.command.output.MIValue;

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

    private final AbstractMIControl fCommandControl;

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

    Integer fExitCode = null;

    private State fState = State.RUNNING;
    
    int inferiorPID;

    @ConfinedToDsfExecutor("fSession#getExecutor")
    public MIInferiorProcess(AbstractMIControl commandControl, OutputStream gdbOutputStream) {
        this(commandControl, gdbOutputStream, null);
    }

    @ConfinedToDsfExecutor("fSession#getExecutor")
    public MIInferiorProcess(AbstractMIControl commandControl, PTY p) {
        this(commandControl, null, p);
    }

    @ConfinedToDsfExecutor("fSession#getExecutor")
    private MIInferiorProcess(AbstractMIControl commandControl, final OutputStream gdbOutputStream, PTY p) {
        fCommandControl = commandControl;
        fSession = commandControl.getSession();
        
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
                inputStream = new PipedInputStream(fInputStreamPiped);
            } catch (IOException e) {
            }
            fInputStream = inputStream;
            
        }
        
        // Note: We do not have any err stream from gdb/mi so this gdb 
        // err channel instead.
        fErrorStreamPiped = new PipedOutputStream();
        PipedInputStream errorStream = null; 
        try {
            errorStream = new PipedInputStream(fErrorStreamPiped);
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

    protected DsfSession getSession() {
        return fSession;
    }
    
    protected AbstractMIControl getCommandControl() { return fCommandControl; }
    
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

    public synchronized void waitForSync() throws InterruptedException {
        while (getState() != State.TERMINATED) {
            wait(100);
        }        
    }

    /**
     * @see java.lang.Process#waitFor()
     */
    @Override
    public int waitFor() throws InterruptedException {
        waitForSync();
        return exitValue();
    }

    /**
     * @see java.lang.Process#exitValue()
     */
    @Override
    public int exitValue() {
        if (fExitCode != null) {
            return fExitCode;
        }
        
        try {
            Query<Integer> exitCodeQuery = new Query<Integer>() {
                @Override
                protected void execute(final DataRequestMonitor<Integer> rm) {
                    // Guard against session disposed.
                    if (!DsfSession.isSessionActive(fSession.getId())) {
                        cancel(false);
                        return;
                    }

                    if (isDisposed()) {
                        rm.setData(0);
                        rm.done();
                    } else if (getState() != State.TERMINATED) {
                        // This will cause ExecutionException to be thrown with a CoreException, 
                        // which will in turn contain the IllegalThreadStateException.
                        rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, IDsfService.INVALID_STATE, "GDB is still running.", new IllegalThreadStateException())); //$NON-NLS-1$
                        rm.done();
                    } else {
                        getCommandControl().queueCommand(
                            new MIGDBShowExitCode(getCommandControl().getControlDMContext()), 
                            new DataRequestMonitor<MIGDBShowExitCodeInfo>(fSession.getExecutor(), rm) {
                                @Override
                                protected void handleOK() {
                                    rm.setData(getData().getCode());
                                    rm.done();
                                }
                            });

                    }
                }
            };
            fSession.getExecutor().execute(exitCodeQuery);
            fExitCode = exitCodeQuery.get();
            return fExitCode;
        } catch (RejectedExecutionException e) {
        } catch (InterruptedException e) {
        } catch (CancellationException e) {
        } catch (ExecutionException e) {
            // Che
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
            fInputStreamPiped.close();
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
        CLIExecAbort cmd = new CLIExecAbort(getCommandControl().getControlDMContext());
        getCommandControl().queueCommand(
            cmd,
            new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), null) { 
                @Override
                protected void handleCompleted() {
                    setState(MIInferiorProcess.State.TERMINATED);
                }
            }
        );         
    }

    public State getState() { 
        return fState;
    }
    
    synchronized void setState(State state) {
        if (fState == State.TERMINATED) return;
        fState = state;
        if (fState == State.TERMINATED) {
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
    
    public void commandQueued(ICommand<? extends ICommandResult> command) {
        // No action 
    }
    
    public void commandSent(ICommand<? extends ICommandResult> command) {
        if (command instanceof CLICommand<?>) {
            fSuppressTargetOutputCounter++;
        }
    }
    
    public void commandRemoved(ICommand<? extends ICommandResult> command) {
        // No action 
    }
    
    public void commandDone(ICommand<? extends ICommandResult> command, ICommandResult result) {
    	if (command instanceof CLICommand<?>) {
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
