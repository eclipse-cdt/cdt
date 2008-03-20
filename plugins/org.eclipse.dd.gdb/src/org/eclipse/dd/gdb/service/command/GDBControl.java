/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for additional features in DSF Reference implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.service.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;
import org.eclipse.dd.dsf.debug.service.command.ICommandControl;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.gdb.internal.GdbPlugin;
import org.eclipse.dd.mi.service.command.AbstractCLIProcess;
import org.eclipse.dd.mi.service.command.AbstractMIControl;
import org.eclipse.dd.mi.service.command.CLIEventProcessor;
import org.eclipse.dd.mi.service.command.MIControlDMContext;
import org.eclipse.dd.mi.service.command.MIInferiorProcess;
import org.eclipse.dd.mi.service.command.MIRunControlEventProcessor;
import org.eclipse.dd.mi.service.command.commands.MIGDBExit;
import org.eclipse.dd.mi.service.command.commands.MIInterpreterExecConsole;
import org.eclipse.dd.mi.service.command.output.MIInfo;
import org.eclipse.debug.core.DebugException;
import org.osgi.framework.BundleContext;

/**
 * GDB Debugger control implementation.  This implementation extends the 
 * base MI control implementation to provide the GDB-specific debugger 
 * features.  This includes:<br * - Launching and monitoring the GDB process,<br>
 * - CLI console support,<br>
 * - inferior process status tracking.<br>
 */
public class GDBControl extends AbstractMIControl {

    /**
     * Event indicating that the back end process process has started.
     */
    public static class StartedEvent extends AbstractDMEvent<GDBControlDMContext> {
        public StartedEvent(GDBControlDMContext context) {
            super(context);
        }
    }

    
    /**
     * Event indicating that the back end process has terminated.
     */
    public static class ExitedEvent extends AbstractDMEvent<GDBControlDMContext> {
        public ExitedEvent(GDBControlDMContext context) {
            super(context);
        }
    }

    private static int fgInstanceCounter = 0;
    private final GDBControlDMContext fControlDmc;

    public enum SessionType { RUN, ATTACH, CORE, REMOTE }
    private SessionType fSessionType;
    
    private boolean fConnected = false;
    private boolean fUseInterpreterConsole;
    
    private MonitorJob fMonitorJob;
    private IPath fGdbPath; 
    private IPath fExecPath; 
    private Process fProcess;
    private int fGDBExitValue;
    final private int fGDBLaunchTimeout;
    
    private MIRunControlEventProcessor fMIEventProcessor;
    private CLIEventProcessor fCLICommandProcessor;
    private AbstractCLIProcess fCLIProcess;
    private MIInferiorProcess fInferiorProcess;
    
    public GDBControl(DsfSession session, IPath gdbPath, IPath execPath, SessionType type, int gdbLaunchTimeout) {
        super(session);
        fSessionType = type;
        fGdbPath = gdbPath;
        fExecPath = execPath;
        fGDBLaunchTimeout = gdbLaunchTimeout;
        fControlDmc = new GDBControlDMContext(session.getId(), getClass().getName() + ":" + ++fgInstanceCounter); //$NON-NLS-1$

    }

    @Override
    protected BundleContext getBundleContext() {
        return GdbPlugin.getBundleContext();
    }
    
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize( new RequestMonitor(getExecutor(), requestMonitor) {
            @Override
            protected void handleOK() {
                doInitialize(requestMonitor);
            }
        });
    }

    public void doInitialize(final RequestMonitor requestMonitor) {
        final Sequence.Step[] initializeSteps = new Sequence.Step[] {
                new GDBProcessStep(InitializationShutdownStep.Direction.INITIALIZING),
                new MonitorJobStep(InitializationShutdownStep.Direction.INITIALIZING),
                new CommandMonitoringStep(InitializationShutdownStep.Direction.INITIALIZING),
                new CheckInterpreterConsoleStep(InitializationShutdownStep.Direction.INITIALIZING),
                new CommandProcessorsStep(InitializationShutdownStep.Direction.INITIALIZING),
                new RegisterStep(InitializationShutdownStep.Direction.INITIALIZING),
            };

        Sequence startupSequence = new Sequence(getExecutor(), requestMonitor) {
            @Override public Step[] getSteps() { return initializeSteps; }
        };
        getExecutor().execute(startupSequence);
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        final Sequence.Step[] shutdownSteps = new Sequence.Step[] {
                new RegisterStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new CommandProcessorsStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new CheckInterpreterConsoleStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new CommandMonitoringStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new MonitorJobStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new GDBProcessStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
            };
        Sequence shutdownSequence = new Sequence(getExecutor(), requestMonitor) {
            @Override public Step[] getSteps() { return shutdownSteps; }
        };
        getExecutor().execute(shutdownSequence);
        
    }        

    @Override
    public MIControlDMContext getControlDMContext() {
        return fControlDmc;
    }
    
    /**
     * More strongly typed version of {@link #getControlDMContext()}.
     */
    public GDBControlDMContext getGDBDMContext() {
        return (GDBControlDMContext)getControlDMContext();
    }

    public SessionType getSessionType() { 
        return fSessionType; 
    }

    public boolean canInterrupt() {
        return fProcess instanceof Spawner;
    }

    public void interrupt() {
        if (fProcess instanceof Spawner) {
            Spawner gdbSpawner = (Spawner) fProcess;
            gdbSpawner.interrupt();
        }
    }

    public void destroy() {
        if (fProcess instanceof Spawner) {
            Spawner gdbSpawner = (Spawner) fProcess;
            gdbSpawner.destroy();
        }
    }

    public void terminate(final RequestMonitor rm) {
        // Schedule a runnable to be executed 2 seconds from now.
        // If we don't get a response to the quit command, this 
        // runnable will kill the task.
        final Future<?> quitTimeoutFuture = getExecutor().schedule(
            new DsfRunnable() {
                public void run() {
                    if (!isGDBExited())
                    destroy();
                    rm.done();
                }
                
                @Override
                protected boolean isExecutionRequired() {
                    return false;
                }
            }, 
            2, TimeUnit.SECONDS);
        
        MIGDBExit cmd = new MIGDBExit(fControlDmc);
        queueCommand(
            cmd,
            new DataRequestMonitor<MIInfo>(getExecutor(), rm) { 
                @Override
                public void handleCompleted() {
                    // Cancel the time out runnable (if it hasn't run yet).
                    quitTimeoutFuture.cancel(false);
                    if (!getStatus().isOK() && !isGDBExited()) {
                        destroy();
                    }
                    rm.done();
                }
            }
        );
    }

    public boolean isConnected() {
        return fInferiorProcess.getState() != MIInferiorProcess.State.TERMINATED && fConnected;
    }
    
    void setConnected(boolean connected) {
        fConnected = connected;
    }
    
    public Process getGDBProcess() { 
        return fProcess; 
    }

    public AbstractCLIProcess getCLIProcess() { 
        return fCLIProcess; 
    }

    public MIInferiorProcess getInferiorProcess() {
        return fInferiorProcess;
    }
    
    public boolean isGDBExited() { 
        return fMonitorJob != null && fMonitorJob.fExited; 
    }
    
    public int getGDBExitCode() { 
        return fGDBExitValue;
    }
    
    public IPath getExecutablePath() { return fExecPath; }
        
    public void getInferiorProcessId(DataRequestMonitor<Integer> rm) {
    }
        
    @DsfServiceEventHandler 
    public void eventDispatched(ExitedEvent e) {
        // Handle our "GDB Exited" event and stop processing commands.
        stopCommandProcessing();
    }
    
    /**
     * Monitors a system process, waiting for it to terminate, and
     * then notifies the associated runtime process.
     */
    private class MonitorJob extends Job {
        boolean fExited = false;
        DsfRunnable fMonitorStarted;
        Process fMonProcess;

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            synchronized(fMonProcess) {
                getExecutor().submit(fMonitorStarted);
                while (!fExited) {
                    try {
                        fMonProcess.waitFor();
                        fGDBExitValue = fMonProcess.exitValue();
                    } catch (InterruptedException ie) {
                        // clear interrupted state
                        Thread.interrupted();
                    } finally {
                        fExited = true;
                        getSession().dispatchEvent(new ExitedEvent(fControlDmc) {}, getProperties());
                    }
                }
            }
            return Status.OK_STATUS;
        }

        MonitorJob(Process process, DsfRunnable monitorStarted) {
            super("GDB process monitor job.");  //$NON-NLS-1$
            fMonProcess = process; 
            fMonitorStarted = monitorStarted;
            setSystem(true);
        }

        void kill() {
            synchronized(fMonProcess) {
                if (!fExited) {
                    getThread().interrupt();
                }
            }
        }
    }   

    public static class InitializationShutdownStep extends Sequence.Step {
        public enum Direction { INITIALIZING, SHUTTING_DOWN }
        
        private Direction fDirection;
        InitializationShutdownStep(Direction direction) { fDirection = direction; }
        
        @Override
        final public void execute(RequestMonitor requestMonitor) {
            if (fDirection == Direction.INITIALIZING) {
                initialize(requestMonitor);
            } else {
                shutdown(requestMonitor);
            }
        }
        
        @Override
        final public void rollBack(RequestMonitor requestMonitor) {
            if (fDirection == Direction.INITIALIZING) {
                shutdown(requestMonitor);
            } else {
                super.rollBack(requestMonitor);
            }
        }
        
        protected void initialize(RequestMonitor requestMonitor) {
            requestMonitor.done();
        }
        protected void shutdown(RequestMonitor requestMonitor) {
            requestMonitor.done();
        }
    }

    protected class GDBProcessStep extends InitializationShutdownStep {
        GDBProcessStep(Direction direction) { super(direction); }
        
        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            class GDBLaunchMonitor {
                boolean fLaunched = false;
                boolean fTimedOut = false;
            }
            final GDBLaunchMonitor fGDBLaunchMonitor = new GDBLaunchMonitor(); 

            final RequestMonitor gdbLaunchRequestMonitor = new RequestMonitor(getExecutor(), null) {
                @Override
                protected void handleCompleted() {
                    if (!fGDBLaunchMonitor.fTimedOut) {
                        fGDBLaunchMonitor.fLaunched = true;
                        if (!getStatus().isOK()) {
                            requestMonitor.setStatus(getStatus());
                        }
                        requestMonitor.done();
                    }
                }
            };
            
            final Job startGdbJob = new Job("Start GDB Process Job") { //$NON-NLS-1$
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    List<String> commandList = new ArrayList<String>();
                    
                    commandList.add(fGdbPath.toOSString());
                    if (fExecPath != null) {
                        commandList.add("--interpreter"); //$NON-NLS-1$
                        commandList.add("mi"); //$NON-NLS-1$
                        commandList.add(fExecPath.toOSString());
                    }
                    
                    String[] commandLine = commandList.toArray(new String[commandList.size()]);
        
                    try {
                        fProcess = ProcessFactory.getFactory().exec(commandLine);
                    } catch(IOException e) {
                        String message = MessageFormat.format("Error while launching command",   //$NON-NLS-1$
                                                              new Object[]{commandList.toString()});
                        gdbLaunchRequestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, message, e));
                        gdbLaunchRequestMonitor.done();
                        return Status.OK_STATUS;
                    }
                    
                    try {
                        InputStream stream = fProcess.getInputStream();
                        Reader r = new InputStreamReader(stream);
                        BufferedReader reader = new BufferedReader(r);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            //System.out.println("GDB " + line);
                            if (line.endsWith("(gdb)")) { //$NON-NLS-1$
                                break;
                            }
                        }
                    } catch (IOException e) {
                        gdbLaunchRequestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Error reading GDB STDOUT", e)); //$NON-NLS-1$
                        gdbLaunchRequestMonitor.done();
                        return Status.OK_STATUS;
                    }

                    gdbLaunchRequestMonitor.done();
                    return Status.OK_STATUS;
                }
            };
            startGdbJob.schedule();
                
            getExecutor().schedule(new Runnable() { 
                public void run() {
                    // Only process the event if we have not finished yet (hit the breakpoint).
                    if (!fGDBLaunchMonitor.fLaunched) {
                        fGDBLaunchMonitor.fTimedOut = true;
                        Thread jobThread = startGdbJob.getThread();
                        if (jobThread != null) {
                            jobThread.interrupt();
                        }
                        requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.TARGET_REQUEST_FAILED, "Timed out trying to launch GDB.", null)); //$NON-NLS-1$
                        requestMonitor.done();
                    }
                }},
                fGDBLaunchTimeout, TimeUnit.SECONDS);

        }
        
        @Override
        protected void shutdown(final RequestMonitor requestMonitor) {
            new Job("Terminating GDB process.") {  //$NON-NLS-1$
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    fProcess.destroy();
        
                    int attempts = 0;
                    while (attempts < 10) {
                        try {
                            // Don't know if we really need the exit value... but what the hell.
                            fGDBExitValue = fProcess.exitValue(); // throws exception if process not exited
        
                            requestMonitor.done();
                            return Status.OK_STATUS;
                        } catch (IllegalThreadStateException ie) {
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                        attempts++;
                    }
                    requestMonitor.setStatus(new Status(
                        IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfService.REQUEST_FAILED, "Process terminate failed", null));      //$NON-NLS-1$
                    requestMonitor.done();
                    return Status.OK_STATUS;
                }
            }.schedule();
        }
    }
    
    protected class MonitorJobStep extends InitializationShutdownStep {
        MonitorJobStep(Direction direction) { super(direction); }

        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            fMonitorJob = new MonitorJob(
                fProcess, 
                new DsfRunnable() {
                    public void run() {
                        requestMonitor.done();
                    }
                });
            fMonitorJob.schedule();
        }

        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            if (!fMonitorJob.fExited) {
                fMonitorJob.kill();
            }
            requestMonitor.done();
        }
    }

    protected class CommandMonitoringStep extends InitializationShutdownStep {
        CommandMonitoringStep(Direction direction) { super(direction); }

        @Override
        protected void initialize(final RequestMonitor requestMonitor) {
            startCommandProcessing(fProcess.getInputStream(), fProcess.getOutputStream());
            requestMonitor.done();
        }

        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            stopCommandProcessing();
            requestMonitor.done();
        }
    }

    protected class CheckInterpreterConsoleStep extends InitializationShutdownStep {
        CheckInterpreterConsoleStep(Direction direction) { super(direction); }

        @Override
        public void initialize(final RequestMonitor requestMonitor) {
        	MIInterpreterExecConsole<MIInfo> cmd = new MIInterpreterExecConsole<MIInfo>(fControlDmc, "echo"); //$NON-NLS-1$
        	GDBControl.this.queueCommand(
            	cmd,
            	new DataRequestMonitor<MIInfo>(getExecutor(), null) { 
                    @Override
                    protected void handleCompleted() {
                    	fUseInterpreterConsole = getStatus().isOK();
                        requestMonitor.done();
                    }
            	}
            );
        }
    }

    protected class CommandProcessorsStep extends InitializationShutdownStep {
        CommandProcessorsStep(Direction direction) { super(direction); }

        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            try {
                fCLIProcess = new GDBCLIProcess(GDBControl.this, fUseInterpreterConsole);
            }
            catch(IOException e) {
                requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfService.REQUEST_FAILED, "Failed to create CLI Process", e)); //$NON-NLS-1$
                requestMonitor.done();
                return;
            }
            
            fInferiorProcess = new GDBInferiorProcess(GDBControl.this, fProcess.getOutputStream());
            fCLICommandProcessor = new CLIEventProcessor(GDBControl.this, fControlDmc, fInferiorProcess);
            fMIEventProcessor = new MIRunControlEventProcessor(GDBControl.this, fControlDmc);

            requestMonitor.done();
        }
        
        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            fCLICommandProcessor.dispose();
            fMIEventProcessor.dispose();
            fCLIProcess.dispose();
            fInferiorProcess.dispose();

            requestMonitor.done();
        }
    }
    
    protected class RegisterStep extends InitializationShutdownStep {
        RegisterStep(Direction direction) { super(direction); }
        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            getSession().addServiceEventListener(GDBControl.this, null);
            register(new String[]{ ICommandControl.class.getName(), AbstractMIControl.class.getName() }, new Hashtable<String,String>());
            getSession().dispatchEvent(new StartedEvent(getGDBDMContext()), getProperties());
            requestMonitor.done();
        }

        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            unregister();
            getSession().removeServiceEventListener(GDBControl.this);
            requestMonitor.done();
        }
    }
}
