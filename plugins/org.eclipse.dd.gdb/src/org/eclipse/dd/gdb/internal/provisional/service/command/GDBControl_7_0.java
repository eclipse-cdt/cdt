/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for additional features in DSF Reference implementation
 *     Ericsson           - New version for 7_0
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.provisional.service.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;
import org.eclipse.dd.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.dd.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.command.ICommandControl;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.internal.GdbPlugin;
import org.eclipse.dd.gdb.internal.provisional.launching.GdbLaunch;
import org.eclipse.dd.gdb.internal.provisional.launching.LaunchUtils;
import org.eclipse.dd.gdb.internal.provisional.service.SessionType;
import org.eclipse.dd.mi.service.IMIProcesses;
import org.eclipse.dd.mi.service.MIProcesses;
import org.eclipse.dd.mi.service.command.AbstractCLIProcess;
import org.eclipse.dd.mi.service.command.AbstractMIControl;
import org.eclipse.dd.mi.service.command.CLIEventProcessor_7_0;
import org.eclipse.dd.mi.service.command.MIControlDMContext;
import org.eclipse.dd.mi.service.command.MIInferiorProcess;
import org.eclipse.dd.mi.service.command.MIRunControlEventProcessor_7_0;
import org.eclipse.dd.mi.service.command.commands.MIBreakInsert;
import org.eclipse.dd.mi.service.command.commands.MICommand;
import org.eclipse.dd.mi.service.command.commands.MIExecContinue;
import org.eclipse.dd.mi.service.command.commands.MIExecRun;
import org.eclipse.dd.mi.service.command.commands.MIGDBExit;
import org.eclipse.dd.mi.service.command.commands.MIInferiorTTYSet;
import org.eclipse.dd.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.dd.mi.service.command.output.MIInfo;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.osgi.framework.BundleContext;

/**
 * GDB Debugger control implementation.  This implementation extends the 
 * base MI control implementation to provide the GDB-specific debugger 
 * features.  This includes:<br * - Launching and monitoring the GDB process,<br>
 * - CLI console support,<br>
 * - inferior process status tracking.<br>
 */
public class GDBControl_7_0 extends AbstractMIControl implements IGDBControl {

    /**
     * Event indicating that the back end process has started.
     */
    private static class GDBControlInitializedDMEvent extends AbstractDMEvent<ICommandControlDMContext> 
        implements ICommandControlInitializedDMEvent
    {
        public GDBControlInitializedDMEvent(ICommandControlDMContext context) {
            super(context);
        }
    }
    
    /**
     * Event indicating that the back end process has terminated.
     */
    private static class GDBControlShutdownDMEvent extends AbstractDMEvent<ICommandControlDMContext> 
        implements ICommandControlShutdownDMEvent
    {
        public GDBControlShutdownDMEvent(ICommandControlDMContext context) {
            super(context);
        }
    }

    private static int fgInstanceCounter = 0;
    private final MIControlDMContext fControlDmc;

    private SessionType fSessionType;
    
    private boolean fAttach;
    
    private boolean fConnected = true;
    
    private MonitorJob fMonitorJob;
    private IPath fGdbPath; 
    private IPath fExecPath; 
    private Process fProcess;
    private int fGDBExitValue;
    private int fGDBLaunchTimeout = 30;
    
    private MIRunControlEventProcessor_7_0 fMIEventProcessor;
    private CLIEventProcessor_7_0 fCLICommandProcessor;
    private AbstractCLIProcess fCLIProcess;
    private MIInferiorProcess fInferiorProcess = null;
    
    private PTY fPty;

    public GDBControl_7_0(DsfSession session, ILaunchConfiguration config) { 
        super(session, "gdbcontrol[" + ++fgInstanceCounter + "]", true); //$NON-NLS-1$ //$NON-NLS-2$
        fSessionType = LaunchUtils.getSessionType(config);
        fAttach = LaunchUtils.getIsAttach(config);
        fGdbPath = LaunchUtils.getGDBPath(config);
        try {
			fExecPath = LaunchUtils.verifyProgramPath(config, LaunchUtils.getCProject(config));
		} catch (CoreException e) {
			fExecPath = new Path(""); //$NON-NLS-1$
		}
        fControlDmc = new MIControlDMContext(session.getId(), getId()); 
    }

    @Override
    protected BundleContext getBundleContext() {
        return GdbPlugin.getBundleContext();
    }
    
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize( new RequestMonitor(getExecutor(), requestMonitor) {
            @Override
            protected void handleSuccess() {
                doInitialize(requestMonitor);
            }
        });
    }

    public void doInitialize(final RequestMonitor requestMonitor) {
        final Sequence.Step[] initializeSteps = new Sequence.Step[] {
                new GDBProcessStep(InitializationShutdownStep.Direction.INITIALIZING),
                new MonitorJobStep(InitializationShutdownStep.Direction.INITIALIZING),
                new CommandMonitoringStep(InitializationShutdownStep.Direction.INITIALIZING),
                new InferiorInputOutputInitStep(InitializationShutdownStep.Direction.INITIALIZING),
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
                new InferiorInputOutputInitStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
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
    
    public ICommandControlDMContext getContext() {
        return fControlDmc;
    }
    
    public SessionType getSessionType() { 
        return fSessionType; 
    }

    public boolean getIsAttachSession() { 
        return fAttach; 
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
                    if (!isGDBExited()) {
                        destroy();
                    }
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
                    if (quitTimeoutFuture.cancel(false)) {
                        if (!isSuccess() && !isGDBExited()) {
                            destroy();
                        }
                        rm.done();
                    }
                }
            }
        );
    }
    
    /*
     * This method does the necessary work to setup the input/output streams for the
     * inferior process, by either preparing the PTY to be used, to simply leaving
     * the PTY null, which indicates that the input/output streams of the CLI should
     * be used instead; this decision is based on the type of session.
     */
    public void initInferiorInputOutput(final RequestMonitor requestMonitor) {
    	if (fSessionType == SessionType.REMOTE || fAttach) {
    		// These types do not use a PTY
    		fPty = null;
    		requestMonitor.done();
    	} else {
    		// These types always use a PTY
    		try {
    			fPty = new PTY();

    			// Tell GDB to use this PTY
    			queueCommand(
    					new MIInferiorTTYSet(fControlDmc, fPty.getSlaveName()), 
    					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
    						@Override
    						protected void handleFailure() {
    							// We were not able to tell GDB to use the PTY
    							// so we won't use it at all.
    			    			fPty = null;
    			        		requestMonitor.done();
    						}
    					});
    		} catch (IOException e) {
    			fPty = null;
        		requestMonitor.done();
    		}
    	}
    }


    public boolean canRestart() {
    	if (fAttach) return false;
    	
    	// Before GDB6.8, the Linux gdbserver would restart a new
    	// process when getting a -exec-run but the communication
    	// with GDB had a bug and everything hung.
    	// with GDB6.8 the program restarts properly one time,
    	// but on a second attempt, gdbserver crashes.
    	// So, lets just turn off the Restart for Remote debugging
    	if (fSessionType == SessionType.REMOTE) return false;
    	
    	return true;
    }

     /*
     * Start the program.
     */
    public void start(GdbLaunch launch, final RequestMonitor requestMonitor) {
    	startOrRestart(launch, false, requestMonitor);
    }

    /*
     * Before restarting the inferior, we must re-initialize its input/output streams
     * and create a new inferior process object.  Then we can restart the inferior.
     */
    public void restart(final GdbLaunch launch, final RequestMonitor requestMonitor) {
   		startOrRestart(launch, true, requestMonitor);
    }

    /*
     * Insert breakpoint at entry if set, and start or restart the program.
     */
    protected void startOrRestart(final GdbLaunch launch, boolean restart, final RequestMonitor requestMonitor) {
    	if (fAttach) {
    		// When attaching to a running process, we do not need to set a breakpoint or
    		// start the program; it is left up to the user.
    		requestMonitor.done();
    		return;
    	}

    	DsfServicesTracker servicesTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), getSession().getId());
    	IMIProcesses procService = servicesTracker.getService(IMIProcesses.class);
    	servicesTracker.dispose();
   		IProcessDMContext procDmc = procService.createProcessContext(fControlDmc, MIProcesses.UNIQUE_GROUP_ID);
   		final IContainerDMContext containerDmc = procService.createContainerContext(procDmc, MIProcesses.UNIQUE_GROUP_ID);

    	final MICommand<MIInfo> execCommand;
    	if (fSessionType == SessionType.REMOTE) {
    		// When doing remote debugging, we use -exec-continue instead of -exec-run 
    		execCommand = new MIExecContinue(containerDmc);
    	} else {
    		execCommand = new MIExecRun(containerDmc, new String[0]);	
    	}

    	boolean stopInMain = false;
    	try {
    		stopInMain = launch.getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false );
    	} catch (CoreException e) {
    		requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot retrieve stop at entry point boolean", e)); //$NON-NLS-1$
    		requestMonitor.done();
    		return;
    	}

    	final DataRequestMonitor<MIInfo> execMonitor = new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor);

    	if (!stopInMain) {
    		// Just start the program.
    		queueCommand(execCommand, execMonitor);
    	} else {
    		String stopSymbol = null;
    		try {
    			stopSymbol = launch.getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT );
    		} catch (CoreException e) {
    			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.CONFIGURATION_INVALID, "Cannot retrieve the entry point symbol", e)); //$NON-NLS-1$
    			requestMonitor.done();
    			return;
    		}

        	final IBreakpointsTargetDMContext breakpointDmc = (IBreakpointsTargetDMContext)containerDmc;

    		// Insert a breakpoint at the requested stop symbol.
    		queueCommand(
    				new MIBreakInsert(breakpointDmc, true, false, null, 0, stopSymbol, 0), 
    				new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), requestMonitor) { 
    					@Override
    					protected void handleSuccess() {
    						// After the break-insert is done, execute the -exec-run or -exec-continue command.
    						queueCommand(execCommand, execMonitor);
    					}
    				});
    	}
    }

    /*
     * This method creates a new inferior process object based on the current Pty or output stream.
     */
    public void createInferiorProcess() {
    	if (fPty == null) {
    		fInferiorProcess = new GDBInferiorProcess(GDBControl_7_0.this, fProcess.getOutputStream());
    	} else {
    		fInferiorProcess = new GDBInferiorProcess(GDBControl_7_0.this, fPty);
    	}
    }

    public boolean isConnected() {
        return fInferiorProcess.getState() != MIInferiorProcess.State.TERMINATED && fConnected;
    }
    
    public void setConnected(boolean connected) {
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
        
    @DsfServiceEventHandler 
    public void eventDispatched(ICommandControlShutdownDMEvent e) {
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
                        getSession().dispatchEvent(new GDBControlShutdownDMEvent(fControlDmc) {}, getProperties());
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
                        if (!isSuccess()) {
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
                    
                    // The goal here is to keep options to an absolute minimum.
                    // All configuration should be done in the launch sequence
                    // to allow for easy overriding.
                    commandList.add(fGdbPath.toOSString());
                    commandList.add("--interpreter"); //$NON-NLS-1$
                    // We currently work with MI version 2
                    commandList.add("mi2"); //$NON-NLS-1$
                    // Don't read the gdbinit file here.  It is read explicitly in
                    // the LaunchSequence to make it easier to customize.
                    commandList.add("--nx"); //$NON-NLS-1$
                    
                    String[] commandLine = commandList.toArray(new String[commandList.size()]);
        
                    try {                        
                        fProcess = ProcessFactory.getFactory().exec(commandLine);
                    } catch(IOException e) {
                        String message = "Error while launching command " + commandList.toString();   //$NON-NLS-1$
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
                        IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Process terminate failed", null));      //$NON-NLS-1$
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
    
    protected class InferiorInputOutputInitStep extends InitializationShutdownStep {
    	InferiorInputOutputInitStep(Direction direction) { super(direction); }

        @Override
        protected void initialize(final RequestMonitor requestMonitor) {
        	initInferiorInputOutput(requestMonitor);
        }

        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            requestMonitor.done();
        }
    }

    protected class CommandProcessorsStep extends InitializationShutdownStep {
        CommandProcessorsStep(Direction direction) { super(direction); }

        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            try {
                fCLIProcess = new GDBCLIProcess(GDBControl_7_0.this);
            }
            catch(IOException e) {
                requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Failed to create CLI Process", e)); //$NON-NLS-1$
                requestMonitor.done();
                return;
            }

            createInferiorProcess();
            
            fCLICommandProcessor = new CLIEventProcessor_7_0(GDBControl_7_0.this, fControlDmc);
            fMIEventProcessor = new MIRunControlEventProcessor_7_0(GDBControl_7_0.this, fControlDmc);

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
            getSession().addServiceEventListener(GDBControl_7_0.this, null);
            register(
                new String[]{ ICommandControl.class.getName(), 
                              ICommandControlService.class.getName(), 
                              AbstractMIControl.class.getName(),
                              IGDBControl.class.getName() }, 
                new Hashtable<String,String>());
            getSession().dispatchEvent(new GDBControlInitializedDMEvent(fControlDmc), getProperties());
            requestMonitor.done();
        }

        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            unregister();
            getSession().removeServiceEventListener(GDBControl_7_0.this);
            requestMonitor.done();
        }
    }
}
