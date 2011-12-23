/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems and others.
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
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Marc Khouzam (Ericsson) - Call new FinalLaunchSequence_7_0 (Bug 365471)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence_7_0;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend.BackendStateChangedEvent;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.AbstractCLIProcess;
import org.eclipse.cdt.dsf.mi.service.command.AbstractMIControl;
import org.eclipse.cdt.dsf.mi.service.command.CLIEventProcessor_7_0;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.MIControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.MIRunControlEventProcessor_7_0;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListFeaturesInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
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
    
    private MIRunControlEventProcessor_7_0 fMIEventProcessor;
    private CLIEventProcessor_7_0 fCLICommandProcessor;
    private AbstractCLIProcess fCLIProcess;
    
    private List<String> fFeatures = new ArrayList<String>();

    private boolean fTerminated;
    
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
        super.initialize(new ImmediateRequestMonitor(requestMonitor) {
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

        final Sequence.Step[] initializeSteps = new Sequence.Step[] {
                new CommandMonitoringStep(InitializationShutdownStep.Direction.INITIALIZING),
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
	
	/**
	 * @since 4.0
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void completeInitialization(final RequestMonitor rm) {
		// We take the attributes from the launchConfiguration
		ILaunch launch = (ILaunch)getSession().getModelAdapter(ILaunch.class);
    	Map<String, Object> attributes = null;
		try {
			attributes = launch.getLaunchConfiguration().getAttributes();
		} catch (CoreException e) {}

		// We need a RequestMonitorWithProgress, if we don't have one, we create one.
		RequestMonitorWithProgress progressRm;
		if (rm instanceof RequestMonitorWithProgress) {
			progressRm = (RequestMonitorWithProgress)rm;
		} else {
			progressRm = new RequestMonitorWithProgress(getExecutor(), new NullProgressMonitor()) {
				@Override
				protected void handleCompleted() {
       				rm.setStatus(getStatus());
        			rm.done();
				}
			};
		}

		ImmediateExecutor.getInstance().execute(getCompleteInitializationSequence(attributes, progressRm));
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
		return new FinalLaunchSequence_7_0(getSession(), attributes, rm);
	}
	
	/**@since 4.0 */
	@Override
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

    /** @since 3.0 */
    @DsfServiceEventHandler 
    public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
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
            
            fCLICommandProcessor = new CLIEventProcessor_7_0(GDBControl_7_0.this, fControlDmc);
            fMIEventProcessor = new MIRunControlEventProcessor_7_0(GDBControl_7_0.this, fControlDmc);

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
    
	/**
	 * @since 4.0
	 */
	@Override
	public void enablePrettyPrintingForMIVariableObjects(RequestMonitor rm) {
		queueCommand(
				getCommandFactory().createMIEnablePrettyPrinting(fControlDmc),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}

	/**
	 * @since 4.0
	 */
	@Override
	public void setPrintPythonErrors(boolean enabled, RequestMonitor rm) {
		
		String subCommand = "set python print-stack " + (enabled ? "on" : "off");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		
		queueCommand(
				getCommandFactory().createCLIMaintenance(fControlDmc, subCommand),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}
}
