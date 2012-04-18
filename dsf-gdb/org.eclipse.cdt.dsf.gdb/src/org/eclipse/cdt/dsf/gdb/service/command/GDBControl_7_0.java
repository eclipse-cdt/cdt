/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems and others.
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
 *     Mikhail Khodjaiants (Mentor Graphics) - Refactor common code in GDBControl* classes (bug 372795)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence_7_0;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.AbstractMIControl;
import org.eclipse.cdt.dsf.mi.service.command.CLIEventProcessor_7_0;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.IEventProcessor;
import org.eclipse.cdt.dsf.mi.service.command.MIRunControlEventProcessor_7_0;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListFeaturesInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * GDB Debugger control implementation.  This implementation extends the 
 * base MI control implementation to provide the GDB-specific debugger 
 * features.  This includes:<br>
 * - CLI console support,<br>
 * - inferior process status tracking.<br>
 */
public class GDBControl_7_0 extends GDBControl {

	/**
	 * @deprecated use {@link GDBControl.InitializationShutdownStep}
	 */
	@Deprecated
    public static class InitializationShutdownStep extends Sequence.Step {
		
		/**
		 * @deprecated use {@link GDBControl.InitializationShutdownStep.Direction}
		 */
		@Deprecated
        public enum Direction { INITIALIZING, SHUTTING_DOWN }
        
		private GDBControl.InitializationShutdownStep.Direction fDirection;
		private GDBControl.InitializationShutdownStep fDelegate;
		
        public InitializationShutdownStep(Direction direction) {
        	fDirection = (direction == Direction.INITIALIZING) ? 
        			GDBControl.InitializationShutdownStep.Direction.INITIALIZING :
        			GDBControl.InitializationShutdownStep.Direction.SHUTTING_DOWN;

        	fDelegate = new GDBControl.InitializationShutdownStep(fDirection);
        }

        private InitializationShutdownStep(
        		GDBControl.InitializationShutdownStep.Direction direction, 
        		GDBControl.InitializationShutdownStep delegate) {
        	fDirection = direction;
        	fDelegate = delegate;
        }

        @Override
        final public void execute(RequestMonitor requestMonitor) {
            if (fDirection == GDBControl.InitializationShutdownStep.Direction.INITIALIZING) {
                initialize(requestMonitor);
            } else {
                shutdown(requestMonitor);
            }
        }
        
        @Override
        final public void rollBack(RequestMonitor requestMonitor) {
            if (fDirection == GDBControl.InitializationShutdownStep.Direction.INITIALIZING) {
                shutdown(requestMonitor);
            } else {
                super.rollBack(requestMonitor);
            }
        }
        
        protected void initialize(RequestMonitor requestMonitor) {
            fDelegate.initialize(requestMonitor);
        }
        
        protected void shutdown(RequestMonitor requestMonitor) {
            fDelegate.shutdown(requestMonitor);
        }
    }

	/**
	 * @deprecated use {@link GDBControl.CommandMonitoringStep}
	 */
	@Deprecated
    protected class CommandMonitoringStep extends InitializationShutdownStep {
		
		CommandMonitoringStep(GDBControl.InitializationShutdownStep.Direction direction) {
			super(direction, new GDBControl.CommandMonitoringStep(direction));
		}
    }

	/**
	 * @deprecated use {@link GDBControl.CommandProcessorsStep}.
	 */
	@Deprecated
    protected class CommandProcessorsStep extends InitializationShutdownStep {

		CommandProcessorsStep(GDBControl.InitializationShutdownStep.Direction direction) {
			super(direction, new GDBControl.CommandProcessorsStep(direction));
		}
    }

	/**
	 * @deprecated use {@link GDBControl.RegisterStep}.
	 */
	@Deprecated
    protected class RegisterStep extends InitializationShutdownStep {

		RegisterStep(GDBControl.InitializationShutdownStep.Direction direction) {
			super(direction, new GDBControl.RegisterStep(direction));
		}
    }

    /**
     * @since 3.0
     */
    public GDBControl_7_0(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
    	super(session, true, config, factory);
    }

	@Override
	protected Sequence getStartupSequence(RequestMonitor requestMonitor) {
        final Sequence.Step[] initializeSteps = new Sequence.Step[] {
                new GDBControl.CommandMonitoringStep(GDBControl.InitializationShutdownStep.Direction.INITIALIZING),
                new GDBControl.CommandProcessorsStep(GDBControl.InitializationShutdownStep.Direction.INITIALIZING),
                new CommandTimeoutStep(GDBControl.InitializationShutdownStep.Direction.INITIALIZING),
                new ListFeaturesStep(GDBControl.InitializationShutdownStep.Direction.INITIALIZING),
                new GDBControl.RegisterStep(GDBControl.InitializationShutdownStep.Direction.INITIALIZING),
            };

        return new Sequence(getExecutor(), requestMonitor) {
            @Override public Step[] getSteps() { return initializeSteps; }
        };
	}

	@Override
	protected Sequence getShutdownSequence(RequestMonitor requestMonitor) {
        final Sequence.Step[] shutdownSteps = new Sequence.Step[] {
                new GDBControl.RegisterStep(GDBControl.InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new ListFeaturesStep(GDBControl.InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new CommandTimeoutStep(GDBControl.InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new GDBControl.CommandProcessorsStep(GDBControl.InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new GDBControl.CommandMonitoringStep(GDBControl.InitializationShutdownStep.Direction.SHUTTING_DOWN),
            };
        return new Sequence(getExecutor(), requestMonitor) {
            @Override public Step[] getSteps() { return shutdownSteps; }
        };
	}

	@Override
	protected IEventProcessor createCLIEventProcessor(ICommandControlService connection, ICommandControlDMContext controlDmc) {
		return new CLIEventProcessor_7_0(connection, controlDmc);
	}

	@Override
	protected IEventProcessor createMIRunControlEventProcessor(AbstractMIControl connection, ICommandControlDMContext controlDmc) {
		return new MIRunControlEventProcessor_7_0(connection, controlDmc);
	}
    
    private void listFeatures(final RequestMonitor requestMonitor) {
    	queueCommand(
    			getCommandFactory().createMIListFeatures(getControlDMContext()), 
    			new DataRequestMonitor<MIListFeaturesInfo>(getExecutor(), requestMonitor) {
    				@Override
    				public void handleSuccess() {
    					setFeatures(getData().getFeatures());					
    					super.handleSuccess();
    				}
    			});
    }

	@Override
	protected Sequence getCompleteInitializationSequence(Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		return new FinalLaunchSequence_7_0(getSession(), attributes, rm);
	}
	
    @Override
	@DsfServiceEventHandler 
    public void eventDispatched(ICommandControlShutdownDMEvent e) {
        // Handle our "GDB Exited" event and stop processing commands.
        stopCommandProcessing();
    }

    /** @since 3.0 */
    @DsfServiceEventHandler 
    public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
    }

    
    /** @since 4.0 */
    protected class ListFeaturesStep extends InitializationShutdownStep {
    	
    	ListFeaturesStep(GDBControl.InitializationShutdownStep.Direction direction) { 
			super(direction, new GDBControl.InitializationShutdownStep(direction));
    	}

    	@Override
    	protected void initialize(final RequestMonitor requestMonitor) {
    		listFeatures(requestMonitor);
    	}

    	@Override
    	protected void shutdown(RequestMonitor requestMonitor) {            
    		requestMonitor.done();
    	}
    }

    /**
	 * @since 4.0
	 */
	@Override
	public void enablePrettyPrintingForMIVariableObjects(RequestMonitor rm) {
		queueCommand(
				getCommandFactory().createMIEnablePrettyPrinting(getControlDMContext()),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}
	
	/**
	 * @since 4.0
	 */
	@Override
	public void setPrintPythonErrors(boolean enabled, RequestMonitor rm) {
		
		String subCommand = "set python print-stack " + (enabled ? "on" : "off");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		
		queueCommand(
				getCommandFactory().createCLIMaintenance(getControlDMContext(), subCommand),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}
}
