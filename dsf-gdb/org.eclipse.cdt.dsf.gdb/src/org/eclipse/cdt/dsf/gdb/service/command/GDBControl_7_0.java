/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for additional features in DSF Reference implementation
 *     Ericsson           - New version for 7_0
 *     Vladimir Prus (CodeSourcery) - Support for -data-read-memory-bytes (bug 322658)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.GDBProcesses_7_0.ContainerExitedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.GDBProcesses_7_0.ContainerStartedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.GDBRunControl_7_0;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend.BackendStateChangedEvent;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.AbstractCLIProcess;
import org.eclipse.cdt.dsf.mi.service.command.AbstractMIControl;
import org.eclipse.cdt.dsf.mi.service.command.CLIEventProcessor_7_0;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.MIControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess.State;
import org.eclipse.cdt.dsf.mi.service.command.MIRunControlEventProcessor_7_0;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListFeaturesInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.osgi.framework.BundleContext;

/**
 * GDB Debugger control implementation.  This implementation extends the 
 * base MI control implementation to provide the GDB-specific debugger 
 * features.  This includes:<br>
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
     * Event indicating that the CommandControl (back end process) has terminated.
     */
    private static class GDBControlShutdownDMEvent extends AbstractDMEvent<ICommandControlDMContext> 
        implements ICommandControlShutdownDMEvent
    {
        public GDBControlShutdownDMEvent(ICommandControlDMContext context) {
            super(context);
        }
    }

    private GDBControlDMContext fControlDmc;

    private IGDBBackend fMIBackend;

    private int fConnected = 0;
    
    private MIRunControlEventProcessor_7_0 fMIEventProcessor;
    private CLIEventProcessor_7_0 fCLICommandProcessor;
    private AbstractCLIProcess fCLIProcess;
    private MIInferiorProcess fInferiorProcess = null;
    
    private PTY fPty;
    private List<String> fFeatures = new ArrayList<String>();

    /**
     * @since 3.0
     */
    public GDBControl_7_0(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
    	super(session, true, factory);
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
        fMIBackend = getServicesTracker().getService(IGDBBackend.class);
    	
        // getId uses the MIBackend service, which is why we must wait until we
        // have it, before we can create this context.
        fControlDmc = new GDBControlDMContext(getSession().getId(), getId()); 

        final Sequence.Step[] initializeSteps = new Sequence.Step[] {
                new CommandMonitoringStep(InitializationShutdownStep.Direction.INITIALIZING),
                new InferiorInputOutputInitStep(InitializationShutdownStep.Direction.INITIALIZING),
                new CommandProcessorsStep(InitializationShutdownStep.Direction.INITIALIZING),
                new ListFeaturesStep(InitializationShutdownStep.Direction.INITIALIZING),
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
                new ListFeaturesStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new CommandProcessorsStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new InferiorInputOutputInitStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new CommandMonitoringStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
            };
        Sequence shutdownSequence = 
        	new Sequence(getExecutor(), 
        				 new RequestMonitor(getExecutor(), requestMonitor) {
        					@Override
        					protected void handleCompleted() {
        						GDBControl_7_0.super.shutdown(requestMonitor);
        					}
        				}) {
            @Override public Step[] getSteps() { return shutdownSteps; }
        };
        getExecutor().execute(shutdownSequence);
        
    }        
    
    public String getId() {
        return fMIBackend.getId();
    }

    @Override
    public MIControlDMContext getControlDMContext() {
        return fControlDmc;
    }
    
    public ICommandControlDMContext getContext() {
        return fControlDmc;
    }
    
    public void terminate(final RequestMonitor rm) {
    	// To fix bug 234467:
    	// Interrupt GDB in case the inferior is running.
    	// That way, the inferior will also be killed when we exit GDB.
    	//
    	if (fInferiorProcess.getState() == State.RUNNING) {
    		fMIBackend.interrupt();
    	}

        // Schedule a runnable to be executed 2 seconds from now.
        // If we don't get a response to the quit command, this 
        // runnable will kill the task.
        final Future<?> forceQuitTask = getExecutor().schedule(
            new DsfRunnable() {
                public void run() {
                    fMIBackend.destroy();
                    rm.done();
                }
                
                @Override
                protected boolean isExecutionRequired() {
                    return false;
                }
            }, 
            2, TimeUnit.SECONDS);
        
        queueCommand(
        	getCommandFactory().createMIGDBExit(fControlDmc),
            new DataRequestMonitor<MIInfo>(getExecutor(), rm) { 
                @Override
                public void handleCompleted() {
                    if (isSuccess()) {
                        // Cancel the time out runnable (if it hasn't run yet).
                        forceQuitTask.cancel(false);
                        rm.done();
                    }
                    // else: the forceQuitTask has or will handle it.
                    // It is good to wait for the forceQuitTask to trigger
                    // to leave enough time for the interrupt() to complete.
                }
            }
        );
    }
    
    private void listFeatures(final RequestMonitor requestMonitor) {
    	queueCommand(
    			getCommandFactory().createMIListFeatures(fControlDmc), 
    			new DataRequestMonitor<MIListFeaturesInfo>(getExecutor(), requestMonitor) {
    				@Override
    				public void handleSuccess() {
    					fFeatures = getData().getFeatures();					
    					super.handleSuccess();
    				}
    			});
    }

    /*
     * This method does the necessary work to setup the input/output streams for the
     * inferior process, by either preparing the PTY to be used, to simply leaving
     * the PTY null, which indicates that the input/output streams of the CLI should
     * be used instead; this decision is based on the type of session.
     */
    public void initInferiorInputOutput(final RequestMonitor requestMonitor) {
    	if (fMIBackend.getSessionType() == SessionType.REMOTE || fMIBackend.getIsAttachSession()) {
    		// These types do not use a PTY
    		fPty = null;
    		requestMonitor.done();
    	} else {
    		// These types always use a PTY
    		try {
    			fPty = new PTY();

    			// Tell GDB to use this PTY
    			queueCommand(
    					getCommandFactory().createMIInferiorTTYSet(fControlDmc, fPty.getSlaveName()), 
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
    	if (fMIBackend.getIsAttachSession()|| fMIBackend.getSessionType() == SessionType.CORE) {
    		return false;
    	}
    	
    	// Before GDB6.8, the Linux gdbserver would restart a new
    	// process when getting a -exec-run but the communication
    	// with GDB had a bug and everything hung.
    	// with GDB6.8 the program restarts properly one time,
    	// but on a second attempt, gdbserver crashes.
    	// So, lets just turn off the Restart for Remote debugging
    	if (fMIBackend.getSessionType() == SessionType.REMOTE) return false;
    	
    	return true;
    }

    /**
     * Start the program.
     */
    public void start(GdbLaunch launch, final RequestMonitor requestMonitor) {
    	startOrRestart(launch, false, requestMonitor);
    }

    /**
     * Before restarting the inferior, we must re-initialize its input/output streams
     * and create a new inferior process object.  Then we can restart the inferior.
     */
    public void restart(final GdbLaunch launch, final RequestMonitor requestMonitor) {
   		startOrRestart(launch, true, requestMonitor);
    }

    /**
     * Insert breakpoint at entry if set, and start or restart the program.
     * Note that restart does not apply to remote or attach sessions.
     * 
     * If we want to enable Reverse debugging from the start of the program we do the following:
     * attachSession => enable reverse
     * else => set temp bp on main, run, enable reverse, continue if bp on main was not requested by user 
     */
    protected void startOrRestart(final GdbLaunch launch, final boolean restart, RequestMonitor requestMonitor) {
		boolean tmpReverseEnabled = IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT;
    	try {
    		tmpReverseEnabled = launch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
       																         IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT);
    	} catch (CoreException e) {
    	}
    	final boolean reverseEnabled = tmpReverseEnabled;
    	
   		if (fMIBackend.getIsAttachSession()) {
   			// Restart does not apply to attach sessions.
   			//
   			// When attaching to a running process, we do not need to set a breakpoint or
   			// start the program; it is left up to the user.
   			// We only need to turn on Reverse Debugging if requested.
   			if (reverseEnabled) {
   				IReverseRunControl reverseService = getServicesTracker().getService(IReverseRunControl.class);
   				if (reverseService != null) {
   					reverseService.enableReverseMode(fControlDmc, true, requestMonitor);
   					return;
   				}
   			}
   			requestMonitor.done();
   			return;
   		}

   		// When it is not an attach session, it gets a little more complicated
   		// so let's use a sequence.
   		getExecutor().execute(new Sequence(getExecutor(), requestMonitor) {
        	IContainerDMContext fContainerDmc;        	
        	MIBreakpoint fUserBreakpoint = null;
        	boolean fUserBreakpointIsOnMain = false;
        	
    	    Step[] fSteps = new Step[] {
    	    	/*
    	    	 * If the user requested a 'stopOnMain', let's set the temporary breakpoint
    	    	 * where the user specified.
    	    	 */
    	    	new Step() {
    	    	@Override
    	    	public void execute(final RequestMonitor rm) {
    	        	boolean userRequestedStop = false;
    	        	try {
    	        		userRequestedStop = launch.getLaunchConfiguration().getAttribute(
    	        								ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, 
    	        								false);
    	        	} catch (CoreException e) {
    	        		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot retrieve stop at entry point boolean", e)); //$NON-NLS-1$
    	        		rm.done();
    	        		return;
    	        	}
    	        	
    	           	if (userRequestedStop) {
    	            	String userStopSymbol = null;
    	        		try {
    	        			userStopSymbol = launch.getLaunchConfiguration().getAttribute(
    	        								ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, 
    	        								ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);
    	        		} catch (CoreException e) {
    	        			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.CONFIGURATION_INVALID, "Cannot retrieve the entry point symbol", e)); //$NON-NLS-1$
    	        			rm.done();
    	        			return;
    	        		}
    	        		
    	        		queueCommand(getCommandFactory().createMIBreakInsert(fControlDmc, true, false, null, 0, userStopSymbol, 0),
   	        				         new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), rm) {
    	        			@Override
    	        			public void handleSuccess() {
    	        				if (getData() != null) {
    	        					MIBreakpoint[] breakpoints = getData().getMIBreakpoints();
    	        					if (breakpoints.length > 0) {
    	        						fUserBreakpoint = breakpoints[0];
    	        					}
    	        				}
    	        				rm.done();
    	        			}
    	        		});
    	           	} else {
    	           		rm.done();
    	           	}
    	    	}},
    	    	/*
    	    	 * If reverse debugging, set a breakpoint on main to be able to enable reverse
    	    	 * as early as possible.
    	    	 * If the user has requested a stop at the same point, we could skip this breakpoint
    	    	 * however, we have to first set it to find out!  So, we just leave it.
    	    	 */
      	    	new Step() { 
       	    	@Override
      	    	public void execute(final RequestMonitor rm) {
       	    		if (reverseEnabled) {
     	        		queueCommand(getCommandFactory().createMIBreakInsert(fControlDmc, true, false, null, 0, 
     	        									   ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT, 0),
     							     new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), rm) {
    	        			@Override
    	        			public void handleSuccess() {
    	        				if (getData() != null) {
    	        					MIBreakpoint[] breakpoints = getData().getMIBreakpoints();
    	        					if (breakpoints.length > 0 && fUserBreakpoint != null) {
    	        						fUserBreakpointIsOnMain = breakpoints[0].getAddress().equals(fUserBreakpoint.getAddress());
    	        					}
    	        				}
    	        				rm.done();
    	        			}
    	        		});
       	    		} else {
       	    			rm.done();
					}
       	    	}},
       	    	/*
       	    	 * Now, run the program.  Use either -exec-run or -exec-continue depending
       	    	 * on whether we have remote session or not.
       	    	 */
      	    	new Step() { 
       	    	@Override
      	    	public void execute(RequestMonitor rm) {
               		IMIProcesses procService = getServicesTracker().getService(IMIProcesses.class);
               	    fContainerDmc = procService.createContainerContextFromGroupId(fControlDmc, MIProcesses.UNIQUE_GROUP_ID);
               	    ICommand<MIInfo> command;

       	    		if (useContinueCommand(launch, restart)) {
       	    			command = getCommandFactory().createMIExecContinue(fContainerDmc);
       	    		} else {
       	    			command = getCommandFactory().createMIExecRun(fContainerDmc);	
       	    		}
   	    			queueCommand(command, new DataRequestMonitor<MIInfo>(getExecutor(), rm));
       	    	}},
       	    	/*
       	    	 * In case of a restart, reverse debugging should be marked as off here because
       	    	 * GDB will have turned it off. We may turn it back on after.
       	    	 */
      	    	new Step() { 
       	    	@Override
      	    	public void execute(RequestMonitor rm) {
       	    		// Although it only makes sense for a restart, it doesn't hurt
       	    		// do to it all the time.
          	    	GDBRunControl_7_0 reverseService = getServicesTracker().getService(GDBRunControl_7_0.class);
          	    	if (reverseService != null) {
          	    		reverseService.setReverseModeEnabled(false);
          	    	}
          	    	rm.done();
    	    	}},
    	    	/*
    	    	 * Since we have started the program, we can turn on reverse debugging if needed
    	    	 */
      	    	new Step() { 
      	    	@Override
       	    	public void execute(RequestMonitor rm) {
					if (reverseEnabled) {
						IReverseRunControl reverseService = getServicesTracker().getService(IReverseRunControl.class);
						if (reverseService != null) {
							reverseService.enableReverseMode(fControlDmc, true, rm);
							return;
						}
					}
					rm.done();
       	    	}},
       	    	/*
       	    	 * Finally, if we are enabling reverse, and the userSymbolStop is not on main,
       	    	 * we should do a continue because we are currently stopped on main but that 
       	    	 * is not what the user requested
       	    	 */
      	    	new Step() { 
       	    	@Override
       	    	public void execute(RequestMonitor rm) {
       	    		if (reverseEnabled && !fUserBreakpointIsOnMain) {
       	    			queueCommand(getCommandFactory().createMIExecContinue(fContainerDmc),
       	    					     new DataRequestMonitor<MIInfo>(getExecutor(), rm));
       	    		} else {
       	    			rm.done();
       	    		}
       	    	}},
    	    };
    	    
			@Override
			public Step[] getSteps() {
				return fSteps;
			}
    	});
    }

    /**
     * This method indicates if we should use the -exec-continue method
     * instead of the -exec-run method.
     * This can be overridden to allow for customization.
     * 
     * @since 4.0
     */
    protected boolean useContinueCommand(ILaunch launch, boolean restart) {
    	// When doing remote debugging, we use -exec-continue instead of -exec-run
    	// Restart does not apply to remote sessions
    	return fMIBackend.getSessionType() == SessionType.REMOTE;
    }

    /**
     * This method creates a new inferior process object based on the current Pty or output stream.
     */
    public void createInferiorProcess() {
    	if (fPty == null) {
    		fInferiorProcess = new GDBInferiorProcess(GDBControl_7_0.this, fMIBackend, fMIBackend.getMIOutputStream());
    	} else {
    		fInferiorProcess = new GDBInferiorProcess(GDBControl_7_0.this, fMIBackend, fPty);
    	}
    }

    public boolean isConnected() {
        return fInferiorProcess.getState() != MIInferiorProcess.State.TERMINATED && 
        			(!fMIBackend.getIsAttachSession() || fConnected > 0);
    }
    
    public void setConnected(boolean connected) {
    	if (connected) {
    		fConnected++;
    	} else {
    		if (fConnected > 0) fConnected--;
    	}
   }

    public AbstractCLIProcess getCLIProcess() { 
        return fCLIProcess; 
    }

    public MIInferiorProcess getInferiorProcess() {
        return fInferiorProcess;
    }
    
	/**
	 * @since 2.0
	 */
	public void setTracingStream(OutputStream tracingStream) {
		setMITracingStream(tracingStream);
	}
	
	/** @since 3.0 */
	public void setEnvironment(Properties props, boolean clear, RequestMonitor rm) {
		int count = 0;
		CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm);

		// First clear the environment if requested.
		if (clear) {
			count++;
			queueCommand(
					getCommandFactory().createCLIUnsetEnv(getContext()),
					new DataRequestMonitor<MIInfo>(getExecutor(), countingRm));	
		}
		
		// Now set the new variables
		for (Entry<Object,Object> property : props.entrySet()) {
			count++;
			String name = (String)property.getKey();
			String value = (String)property.getValue();
			queueCommand(
					getCommandFactory().createMIGDBSetEnv(getContext(), name, value),
					new DataRequestMonitor<MIInfo>(getExecutor(), countingRm));	
		}
		countingRm.setDoneCount(count);		
	}
	
	/**@since 4.0 */
	public List<String> getFeatures() {
		return fFeatures;
	}
	
    @DsfServiceEventHandler 
    public void eventDispatched(ICommandControlShutdownDMEvent e) {
        // Handle our "GDB Exited" event and stop processing commands.
        stopCommandProcessing();
    }
    
    @DsfServiceEventHandler 
    public void eventDispatched(BackendStateChangedEvent e) {
        if (e.getState() == IMIBackend.State.TERMINATED && e.getBackendId().equals(fMIBackend.getId())) {
            // Handle "GDB Exited" event, just relay to following event.
            getSession().dispatchEvent(new GDBControlShutdownDMEvent(fControlDmc), getProperties());
        }
    }
 
    /** @since 2.0 */
    @DsfServiceEventHandler 
    public void eventDispatched(ContainerStartedDMEvent e) {
    	setConnected(true);
    }

    /** @since 2.0 */
    @DsfServiceEventHandler 
    public void eventDispatched(ContainerExitedDMEvent e) {
    	setConnected(false);
    	
    	if (Platform.getPreferencesService().getBoolean("org.eclipse.cdt.dsf.gdb.ui",  //$NON-NLS-1$
    													IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB,
    													true, null)) {
    		if (!isConnected() && 
    				!(fMIBackend.getIsAttachSession() && 
    				  fMIBackend.getSessionType() == SessionType.REMOTE)) {
    			// If the last process we are debugging finishes, let's terminate GDB
    			// but not for a remote attach session, since we could request to attach
    			// to another process
    			terminate(new RequestMonitor(ImmediateExecutor.getInstance(), null));
    		}
    	}
    }

    /** @since 3.0 */
    @DsfServiceEventHandler 
    public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
    	if (e.isVisualizationModeEnabled()) {
    		// Once we start looking at trace frames, we should not use
    		// the --thread or --frame options because GDB does not handle
    		// it well, there are no actual threads running.
    		// We only need to do this once, but it won't hurt to do it
    		// every time.
    		setUseThreadAndFrameOptions(false);
    	} else {
    		// We stopped looking at trace frames, so we can start
    		// using --thread and --frame again
    		setUseThreadAndFrameOptions(true);
    	}
    }

    public static class InitializationShutdownStep extends Sequence.Step {
        public enum Direction { INITIALIZING, SHUTTING_DOWN }
        
        private Direction fDirection;
        public InitializationShutdownStep(Direction direction) { fDirection = direction; }
        
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

    protected class CommandMonitoringStep extends InitializationShutdownStep {
        CommandMonitoringStep(Direction direction) { super(direction); }

        @Override
        protected void initialize(final RequestMonitor requestMonitor) {
            startCommandProcessing(fMIBackend.getMIInputStream(), fMIBackend.getMIOutputStream());
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
                fCLIProcess = new GDBBackendCLIProcess(GDBControl_7_0.this, fMIBackend);
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
    
    /** @since 4.0 */
    protected class ListFeaturesStep extends InitializationShutdownStep {
    	ListFeaturesStep(Direction direction) { super(direction); }

    	@Override
    	protected void initialize(final RequestMonitor requestMonitor) {
    		listFeatures(requestMonitor);
    	}

    	@Override
    	protected void shutdown(RequestMonitor requestMonitor) {            
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
                              IMICommandControl.class.getName(),
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
