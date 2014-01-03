/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for additional features in DSF Reference implementation
 *     Nokia - create and use backend service.
 *     Vladimir Prus (CodeSourcery) - Support for -data-read-memory-bytes (bug 322658)
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Mikhail Khodjaiants (Mentor Graphics) - Refactor common code in GDBControl* classes (bug 372795)
 *     Marc Khouzam (Ericsson) - Pass errorStream to startCommandProcessing() (Bug 350837)
 *     Mikhail Khodjaiants (Mentor Graphics) - Terminate should cancel the initialization sequence 
 *                                             if it is still running (bug 373845)
 *     Marc Khouzam (Ericsson) - Terminate the session if we loose the connection to the remote target (bug 422586)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.Messages;
import org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.gdb.service.command.GdbCommandTimeoutManager.ICommandTimeoutListener;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend.BackendStateChangedEvent;
import org.eclipse.cdt.dsf.mi.service.IMIBackend2;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses.ContainerExitedDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.AbstractCLIProcess;
import org.eclipse.cdt.dsf.mi.service.command.AbstractMIControl;
import org.eclipse.cdt.dsf.mi.service.command.CLIEventProcessor;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.IEventProcessor;
import org.eclipse.cdt.dsf.mi.service.command.MIControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.MIRunControlEventProcessor;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConsoleStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MILogStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResultRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.osgi.framework.BundleContext;

/**
 * GDB Debugger control implementation.  This implementation extends the 
 * base MI control implementation to provide the GDB-specific debugger 
 * features.  This includes:<br>
 * - CLI console support,<br>
 * - inferior process status tracking.<br>
 */
public class GDBControl extends AbstractMIControl implements IGDBControl, IEventListener, ICommandListener {

	private static final int STATUS_CODE_COMMAND_TIMED_OUT = 20100;

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

	private class TimeoutListener implements ICommandTimeoutListener {

		@Override
		public void commandTimedOut(final ICommandToken token) {
			getExecutor().execute(new DsfRunnable() {
				
				@Override
				public void run() {
					GDBControl.this.commandTimedOut(token);
				}
			});
		}		
	}

    private GDBControlDMContext fControlDmc;

    private IGDBBackend fMIBackend;
        
    private IEventProcessor fMIEventProcessor;
    private IEventProcessor fCLICommandProcessor;
    private AbstractCLIProcess fCLIProcess;

    private GdbCommandTimeoutManager fCommandTimeoutManager;

    private ICommandTimeoutListener fTimeoutListener = new TimeoutListener();

    /**
     * GDBControl is only used for GDB earlier that 7.0. Although -list-features
     * is available in 6.8, it does not report anything we care about, so
     * return empty list.
     */
	private final List<String> fFeatures = new ArrayList<String>();

	private Sequence fInitializationSequence;
	
	/**
	 * Indicator to distinguish whether this service is initialized.
	 * <code>fInitializationSequence</code> can not be used for this 
	 * purpose because there is a period of time when the service is already
	 * initializing but the initialization sequence has not created yet.   
	 */
	private boolean fInitialized = false;

    private boolean fTerminated;

    /**
     * @since 3.0
     */
    public GDBControl(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
    	this(session, false, config, factory);
    }

    /**
	 * @since 4.1
	 */
    protected GDBControl(DsfSession session, boolean useThreadAndFrameOptions, ILaunchConfiguration config, CommandFactory factory) {
    	super(session, useThreadAndFrameOptions, factory);
    }

    @Override
    protected BundleContext getBundleContext() {
        return GdbPlugin.getBundleContext();
    }
    
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize( new ImmediateRequestMonitor(requestMonitor) {
            @Override
            protected void handleSuccess() {
                doInitialize(requestMonitor);
            }
        });
    }

    private void doInitialize(final RequestMonitor requestMonitor) {

        fMIBackend = getServicesTracker().getService(IGDBBackend.class);
    	
        // getId uses the MIBackend service, which is why we must wait until we
        // have it, before we can create this context.
        fControlDmc = new GDBControlDMContext(getSession().getId(), getId()); 

        addEventListener(this);
        addCommandListener(this);
        getExecutor().execute(getStartupSequence(requestMonitor));
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        getExecutor().execute(getShutdownSequence(new RequestMonitor(getExecutor(), requestMonitor) {

        	@Override
        	protected void handleCompleted() {
        		removeEventListener(GDBControl.this);
        		removeCommandListener(GDBControl.this);
				GDBControl.super.shutdown(requestMonitor);
        	}
        }));
    }        

	@Override
    public String getId() {
        return fMIBackend.getId();
    }
    
    @Override
    public MIControlDMContext getControlDMContext() {
        return fControlDmc;
    }
    
	@Override
    public ICommandControlDMContext getContext() {
        return fControlDmc;
    }
    
	@Override
    public void terminate(final RequestMonitor rm) {
        if (fTerminated) {
            rm.done();
            return;
        }
        fTerminated = true;

        // If the initialization sequence is still running mark it as cancelled,
        // to avoid reporting errors to the user, since we are terminating anyway.
        if (fInitializationSequence != null) {
        	fInitializationSequence.getRequestMonitor().cancel();
        }
        
       // To fix bug 234467:
       // Interrupt GDB in case the inferior is running.
       // That way, the inferior will also be killed when we exit GDB.
       //
		IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
		if (runControl != null && !runControl.isTargetAcceptingCommands()) {
           fMIBackend.interrupt();
       }
       
        // Schedule a runnable to be executed 2 seconds from now.
        // If we don't get a response to the quit command, this 
        // runnable will kill the task.
        final Future<?> forceQuitTask = getExecutor().schedule(
            new DsfRunnable() {
            	@Override
                public void run() {
                    fMIBackend.destroy();
                    rm.done();
                }
                
                @Override
                protected boolean isExecutionRequired() {
                    return false;
                }
            }, 
            getGDBExitWaitTime(), TimeUnit.SECONDS);
        
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

	@Override
    public AbstractCLIProcess getCLIProcess() { 
        return fCLIProcess; 
    }
    
	/**
	 * @since 2.0
	 */
	@Override
	public void setTracingStream(OutputStream tracingStream) {
		setMITracingStream(tracingStream);
	}
	
	/** @since 3.0 */
	@Override
	public void setEnvironment(Properties props, boolean clear, final RequestMonitor rm) {
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
	 
	/**
	 * @since 4.0
	 */
	@Override
	public void completeInitialization(final RequestMonitor rm) {
		// We take the attributes from the launchConfiguration
		ILaunch launch = (ILaunch)getSession().getModelAdapter(ILaunch.class);
    	Map<String, Object> attributes = null;
		try {
			attributes = launch.getLaunchConfiguration().getAttributes();
		} catch (CoreException e) {}

		// We need a RequestMonitorWithProgress, if we don't have one, we create one.
		IProgressMonitor monitor = (rm instanceof RequestMonitorWithProgress) ?				
				((RequestMonitorWithProgress)rm).getProgressMonitor() : new NullProgressMonitor();
		RequestMonitorWithProgress progressRm = new RequestMonitorWithProgress(getExecutor(), monitor) {

			@Override
			protected void handleCompleted() {
				fInitializationSequence = null;
				fInitialized = true;
				if (!isCanceled()) {
					// Only set the status if the user has not cancelled the operation already.
					rm.setStatus(getStatus());
				} else {
					rm.cancel();
				}
    			rm.done();
			}
		};

		fInitializationSequence = getCompleteInitializationSequence(attributes, progressRm);
		ImmediateExecutor.getInstance().execute(fInitializationSequence);
	}
	
	/**
	 * Return the sequence that is to be used to complete the initialization of GDB.
	 * 
	 * @param rm A RequestMonitorWithProgress that will indicate when the sequence is completed, but that
	 *           also contains an IProgressMonitor to be able to cancel the launch.  A NullProgressMonitor
	 *           can be used if cancellation is not required.
	 * 
	 * @since 4.0
	 */
	protected Sequence getCompleteInitializationSequence(Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		return new FinalLaunchSequence(getSession(), attributes, rm);
	}
	
    @DsfServiceEventHandler 
    public void eventDispatched(ICommandControlShutdownDMEvent e) {
    	// Handle our "GDB Exited" event and stop processing commands.
    	stopCommandProcessing();

    	// Before GDB 7.0, we have to send the containerExited event ourselves
    	IGDBProcesses procService = getServicesTracker().getService(IGDBProcesses.class);
        if (procService != null) {
    		IContainerDMContext processContainerDmc = procService.createContainerContextFromGroupId(fControlDmc, MIProcesses.UNIQUE_GROUP_ID);
    		getSession().dispatchEvent(
    				new ContainerExitedDMEvent(processContainerDmc), getProperties());
        }
    }
    
    @DsfServiceEventHandler 
    public void eventDispatched(BackendStateChangedEvent e) {
        if (e.getState() == IMIBackend.State.TERMINATED && e.getBackendId().equals(fMIBackend.getId())) {
            // Handle "GDB Exited" event, just relay to following event.
            getSession().dispatchEvent(new GDBControlShutdownDMEvent(fControlDmc), getProperties());
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
        	InputStream errorStream = null;
        	if (fMIBackend instanceof IMIBackend2) {
        		errorStream = ((IMIBackend2)fMIBackend).getMIErrorStream();
        	}
            startCommandProcessing(fMIBackend.getMIInputStream(), fMIBackend.getMIOutputStream(), errorStream);
            requestMonitor.done();
        }

        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            stopCommandProcessing();
            requestMonitor.done();
        }
    }
    
    protected class CommandProcessorsStep extends InitializationShutdownStep {
        CommandProcessorsStep(Direction direction) { super(direction); }

        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            try {
                fCLIProcess = new GDBBackendCLIProcess(GDBControl.this, fMIBackend);
            }
            catch(IOException e) {
                requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Failed to create CLI Process", e)); //$NON-NLS-1$
                requestMonitor.done();
                return;
            }
            
            fCLICommandProcessor = createCLIEventProcessor(GDBControl.this, fControlDmc);
            fMIEventProcessor = createMIRunControlEventProcessor(GDBControl.this, fControlDmc);

            requestMonitor.done();
        }
        
        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            fCLICommandProcessor.dispose();
            fMIEventProcessor.dispose();
            fCLIProcess.dispose();

            requestMonitor.done();
        }
    }
    
    /**
	 * @since 4.1
	 */
    protected class CommandTimeoutStep extends InitializationShutdownStep {
		CommandTimeoutStep( Direction direction ) {
			super( direction );
		}

		@Override
		public void initialize( final RequestMonitor requestMonitor ) {
			fCommandTimeoutManager = createCommandTimeoutManager( GDBControl.this );
			if (fCommandTimeoutManager != null) {
				fCommandTimeoutManager.addCommandTimeoutListener(fTimeoutListener);
			}
			requestMonitor.done();
		}

		@Override
		protected void shutdown( RequestMonitor requestMonitor ) {
			if ( fCommandTimeoutManager != null ) {
				fCommandTimeoutManager.removeCommandTimeoutListener(fTimeoutListener);
				fCommandTimeoutManager.dispose();
			}
			requestMonitor.done();
		}
	}

    protected class RegisterStep extends InitializationShutdownStep {
        RegisterStep(Direction direction) { super(direction); }
        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            getSession().addServiceEventListener(GDBControl.this, null);
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
            getSession().removeServiceEventListener(GDBControl.this);
            requestMonitor.done();
        }
    }

	/** @since 4.0 */
	@Override
	public List<String> getFeatures() {
		return fFeatures;
	}
	
	/**
	 * @since 4.0
	 */
	@Override
	public void enablePrettyPrintingForMIVariableObjects(RequestMonitor rm) {
		rm.done();
	}
	
	/**
	 * @since 4.0
	 */
	@Override
	public void setPrintPythonErrors(boolean enabled, RequestMonitor rm) {
		rm.done();
	}

	/**
	 * @since 4.1
	 */
	protected Sequence getStartupSequence(final RequestMonitor requestMonitor) {
        final Sequence.Step[] initializeSteps = new Sequence.Step[] {
                new CommandMonitoringStep(InitializationShutdownStep.Direction.INITIALIZING),
                new CommandProcessorsStep(InitializationShutdownStep.Direction.INITIALIZING),
                new CommandTimeoutStep(InitializationShutdownStep.Direction.INITIALIZING),
                new RegisterStep(InitializationShutdownStep.Direction.INITIALIZING),
            };

        return new Sequence(getExecutor(), requestMonitor) {
            @Override public Step[] getSteps() { return initializeSteps; }
        };
	}

	/**
	 * @since 4.1
	 */
	protected Sequence getShutdownSequence(RequestMonitor requestMonitor) {
        final Sequence.Step[] shutdownSteps = new Sequence.Step[] {
                new RegisterStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new CommandTimeoutStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new CommandProcessorsStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new CommandMonitoringStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
            };
        return new Sequence(getExecutor(), requestMonitor) {
            @Override public Step[] getSteps() { return shutdownSteps; }
        };
	}

	/**
	 * @since 4.1
	 */
	protected IEventProcessor createCLIEventProcessor(ICommandControlService connection, ICommandControlDMContext controlDmc) {
		return new CLIEventProcessor(connection, controlDmc);
	}

	/**
	 * @since 4.1
	 */
	protected IEventProcessor createMIRunControlEventProcessor(AbstractMIControl connection, ICommandControlDMContext controlDmc) {
		return new MIRunControlEventProcessor(connection, controlDmc);
	}

	/**
	 * @since 4.1
	 */
	protected void setFeatures(List<String> features) {
		fFeatures.clear();
		fFeatures.addAll(features);
	}
	
	/**
	 * @since 4.1
	 */
	protected GdbCommandTimeoutManager createCommandTimeoutManager(ICommandControl commandControl) {
		GdbCommandTimeoutManager manager = new GdbCommandTimeoutManager(commandControl);
		manager.initialize();
		return manager;
	}

	/**
	 * @since 4.1
	 */
	@ConfinedToDsfExecutor("this.getExecutor()")
	protected void commandTimedOut(ICommandToken token) {
		String commandText = token.getCommand().toString();
		if (commandText.endsWith("\n")) //$NON-NLS-1$
			commandText = commandText.substring(0, commandText.length() - 1);
		final String errorMessage = String.format("Command '%s' is timed out", commandText); //$NON-NLS-1$
		commandFailed(token, STATUS_CODE_COMMAND_TIMED_OUT, errorMessage);
		
		// If the timeout occurs while the launch sequence is running 
		// the error will be reported by the launcher's error reporting mechanism.
		// We need to show the error message only when the session is initialized.
		if (isInitialized()) {
			// The session is terminated if a command is timed out.
			terminate(new RequestMonitor(getExecutor(), null) {
				
				@Override
				protected void handleErrorOrWarning() {
					GdbPlugin.getDefault().getLog().log(getStatus());
					super.handleErrorOrWarning();
				};
			} );

			IStatus status = new Status( 
					IStatus.ERROR, 
					GdbPlugin.PLUGIN_ID, 
					IGdbDebugConstants.STATUS_HANDLER_CODE, 
					String.format( Messages.GDBControl_Session_is_terminated, errorMessage ), 
					null);
			IStatusHandler statusHandler = DebugPlugin.getDefault().getStatusHandler(status);
			if (statusHandler != null) {
				try {
					statusHandler.handleStatus(status, null);
				}
				catch(CoreException e) {
					GdbPlugin.getDefault().getLog().log(e.getStatus());
				}
			}
		}
	}

	/**
	 * @since 4.1
	 */
	protected boolean isInitialized() {
		return fInitialized;
	}

	/**
	 * Returns the time (in seconds) the debugger will wait for "gdb-exit" to complete.
	 * 
	 * @since 4.2
	 */
	protected int getGDBExitWaitTime() {
		return 2;
	}

	/** @since 4.3 */
	@Override
	public void eventReceived(Object output) {
		boolean shouldTerminate = false;
		for (MIOOBRecord oobr : ((MIOutput)output).getMIOOBRecords()) {
			if (oobr instanceof MIConsoleStreamOutput) {
				MIConsoleStreamOutput out = (MIConsoleStreamOutput) oobr;
				String str = out.getString();
				if (str != null && str.startsWith("Ending remote debugging")) { //$NON-NLS-1$
					// This happens if the user types 'disconnect' in the console
					// Note that we don't trigger a terminate in the case where a
					// -target-disconnect is used.  This is a case that CDT handles
					// explicitly as it is an MI command; we shouldn't have to catch it here.
					// In fact, catching it here and terminating the session would break
					// the workaround for GDB 7.2 handled by GDBProcesses_7_2.needFixForGDB72Bug352998()
					shouldTerminate = true;
					break;
				}
			} else if (oobr instanceof MILogStreamOutput) {
				MILogStreamOutput out = (MILogStreamOutput) oobr;
				String str = out.getString().trim();
				if (str != null && str.startsWith("Remote connection closed")) { //$NON-NLS-1$
					// This happens if gdbserver is killed or dies
					shouldTerminate = true;
					break;
				}
			}
		}

		if (!shouldTerminate) {
            // It can happen that GDB sends multiple responses to the same command.  
			// In those cases, the second command result is sent as an event.
			// Let's check if we have such a result.
			// This case can happen for example in all-stop when sending an -exec-continue
			// and then kill gdbserver while connected to a process; a second result
			// to -exec-continue will be sent to indicate the remote connection is closed.
			MIResultRecord rr = ((MIOutput)output).getMIResultRecord();
			if (rr != null) {
				String state = rr.getResultClass();
				if ("error".equals(state)) { //$NON-NLS-1$
					MIResult[] results = rr.getMIResults();
					for (MIResult result : results) {
						String var = result.getVariable();
						if (var.equals("msg")) { //$NON-NLS-1$
							MIValue value = result.getMIValue();
							if (value instanceof MIConst) {
								String str = ((MIConst)value).getCString();
								if (str != null && str.startsWith("Remote connection closed")) { //$NON-NLS-1$
									shouldTerminate = true;
								}
							}
						}
					}
				}
			}
		}
		
    	if (shouldTerminate) {
    		terminate(new ImmediateRequestMonitor() {
    			@Override
    			protected void handleErrorOrWarning() {
    				GdbPlugin.getDefault().getLog().log(getStatus());
    				super.handleErrorOrWarning();
    			};
    		});
    	}
	}

	/** @since 4.3 */
	@Override
    public void commandDone(ICommandToken token, ICommandResult cmdResult) {
    	MIInfo info = (MIInfo) cmdResult ;
    	MIOutput output =  info.getMIOutput();
    	MIResultRecord rr = output.getMIResultRecord();
        if (rr != null) {
            String state = rr.getResultClass();
            if ("error".equals(state)) { //$NON-NLS-1$
    			MIResult[] results = rr.getMIResults();
    			for (MIResult result : results) {
    				String var = result.getVariable();
    				if (var.equals("msg")) { //$NON-NLS-1$
    					MIValue value = result.getMIValue();
    					if (value instanceof MIConst) {
    						String str = ((MIConst)value).getCString();
    						if (str != null && str.startsWith("Remote connection closed")) { //$NON-NLS-1$
    				    		terminate(new ImmediateRequestMonitor() {
    				    			@Override
    				    			protected void handleErrorOrWarning() {
    				    				GdbPlugin.getDefault().getLog().log(getStatus());
    				    				super.handleErrorOrWarning();
    				    			};
    				    		});
    				    	}
    					}
    				}
    			}
            }
        }
    }

	/** @since 4.3 */
	@Override
	public void commandQueued(ICommandToken token) {
	}

	/** @since 4.3 */
	@Override
	public void commandSent(ICommandToken token) {
	}

	/** @since 4.3 */
	@Override
	public void commandRemoved(ICommandToken token) {
	}
}
