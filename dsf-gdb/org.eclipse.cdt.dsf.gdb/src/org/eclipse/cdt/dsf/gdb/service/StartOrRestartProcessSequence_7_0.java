/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ReflectionSequence;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class causes a process to start (run for the first time), or to
 * be restarted.  The complexity is due to the handling of reverse debugging,
 * which this class transparently enables if necessary.
 * 
 * This sequence is used for GDB >= 7.0 which supports reverse debugging.
 * 
 * @since 4.0
 */
public class StartOrRestartProcessSequence_7_0 extends ReflectionSequence {
	
	private IGDBControl fCommandControl;
	private CommandFactory fCommandFactory;
	private IGDBProcesses fProcService;
	private IReverseRunControl fReverseService;
	
	private DsfServicesTracker fTracker;

	// This variable will be used to store the original container context,
	// but once the new process is start (restarted), it will contain the new
	// container context.  This new container context has for parent the process
	// context, which holds the new pid.
	private IContainerDMContext fContainerDmc;
	
	// If the user requested a stop_on_main, this variable will hold the breakpoint
	private MIBreakpoint fUserBreakpoint;
	// Since the stop_on_main option allows the user to set the breakpoint on any
	// symbol, we use this variable to know if the stop_on_main breakpoint was really
	// on the main() method.
	private boolean fUserBreakpointIsOnMain;
	
	private boolean fReverseEnabled;
	private final Map<String, Object> fAttributes;
	
	// Indicates if the sequence is being used for a restart or a start
	private final boolean fRestart;
	
	// Store the dataRM so that we can fill it with the new container context, which we must return
	// Although we can access this through Sequence.getRequestMonitor(), we would loose the type-checking.
	// Therefore, doing it like this is more future-proof.
	private final DataRequestMonitor<IContainerDMContext> fDataRequestMonitor;

	
	protected IContainerDMContext getContainerContext() {
		return fContainerDmc;
	}
	
	protected MIBreakpoint getUserBreakpoint() {
		return fUserBreakpoint;
	}
	
	protected boolean getUserBreakpointIsOnMain() {
		return fUserBreakpointIsOnMain;
	}

	    
	public StartOrRestartProcessSequence_7_0(DsfExecutor executor, IContainerDMContext containerDmc, Map<String, Object> attributes, 
										 boolean restart, DataRequestMonitor<IContainerDMContext> rm) {
		super(executor, rm);
		
		assert executor != null;
		assert containerDmc != null;
		if (attributes == null) {
			// If no attributes are specified, simply use an empty map.
			attributes = new HashMap<String, Object>();
		}
		
		fContainerDmc = containerDmc;
		fAttributes = attributes;
		fRestart = restart;
		fDataRequestMonitor = rm;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {

			DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fContainerDmc.getSessionId());
			IGDBBackend backend = tracker.getService(IGDBBackend.class);
			tracker.dispose();
			
			if (backend.getIsAttachSession()) {
	   			// Restart does not apply to attach sessions, so we are only dealing with the
				// Start case.
	   			//
	   			// When attaching to a running process, we do not need to set a breakpoint or
	   			// start the program; it is left up to the user.
	   			// We only need to turn on Reverse Debugging if requested.

				return new String[] {
						"stepInitializeBaseSequence",  //$NON-NLS-1$
						"stepEnableReverse",   //$NON-NLS-1$
						"stepCleanupBaseSequence",   //$NON-NLS-1$
				};
			} else {
				return new String[] {
						"stepInitializeBaseSequence",  //$NON-NLS-1$
						"stepInsertStopOnMainBreakpoint",  //$NON-NLS-1$
						"stepSetBreakpointForReverse",   //$NON-NLS-1$
						"stepRunProgram",   //$NON-NLS-1$
						"stepSetReverseOff",   //$NON-NLS-1$
						"stepEnableReverse",   //$NON-NLS-1$
						"stepContinue",   //$NON-NLS-1$
						"stepCleanupBaseSequence",   //$NON-NLS-1$
				};
			}
		}
		return null;
	}
	
	/** 
	 * Initialize the members of the StartOrRestartProcessSequence_7_0 class.
	 * This step is mandatory for the rest of the sequence to complete.
	 */
	@Execute
	public void stepInitializeBaseSequence(RequestMonitor rm) {
		fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fContainerDmc.getSessionId());
		fCommandControl = fTracker.getService(IGDBControl.class);
        fCommandFactory = fTracker.getService(IMICommandControl.class).getCommandFactory();		
		fProcService = fTracker.getService(IGDBProcesses.class);

        if (fCommandControl == null || fCommandFactory == null || fProcService == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot obtain service", null)); //$NON-NLS-1$
			rm.done();
			return;
		}
        
		fReverseService = fTracker.getService(IReverseRunControl.class);
		if (fReverseService != null) {
			// Although the option to use reverse debugging could be on, we only check
			// it if we actually have a reverse debugging service.  There is no point
			// in trying to handle reverse debugging if it is not available.
			fReverseEnabled = CDebugUtils.getAttribute(fAttributes, 
													   IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
													   IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT);
		}

		rm.done();
	}
	
	/** 
	 * Rollback method for {@link #stepInitializeBaseSequence()}
	 */
	@RollBack("stepInitializeBaseSequence")
	public void rollBackInitializeBaseSequence(RequestMonitor rm) {
		if (fTracker != null) fTracker.dispose();
		fTracker = null;
		rm.done();
	}
	
	/**
	 * If the user requested a 'stopOnMain', let's set the temporary breakpoint
	 * where the user specified.
	 */
	@Execute
	public void stepInsertStopOnMainBreakpoint(final RequestMonitor rm) {
		boolean userRequestedStop = CDebugUtils.getAttribute(fAttributes, 
															 ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
															 false);

		if (userRequestedStop) {
			String userStopSymbol = CDebugUtils.getAttribute(fAttributes, 
															 ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
															 ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);

			IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(getContainerContext(), IBreakpointsTargetDMContext.class);

			fCommandControl.queueCommand(
					fCommandFactory.createMIBreakInsert(bpTargetDmc, true, false, null, 0, userStopSymbol, 0),
					new DataRequestMonitor<MIBreakInsertInfo>(ImmediateExecutor.getInstance(), rm) {
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
	}

	/**
	 * If reverse debugging, set a breakpoint on main to be able to enable reverse
	 * as early as possible.
	 * If the user has requested a stop at the same point, we could skip this breakpoint
	 * however, we have to first set it to find out!  So, we just leave it.
	 */
	@Execute
	public void stepSetBreakpointForReverse(final RequestMonitor rm) {
		if (fReverseEnabled) {
			IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(getContainerContext(), IBreakpointsTargetDMContext.class);

			fCommandControl.queueCommand(
					fCommandFactory.createMIBreakInsert(bpTargetDmc, true, false, null, 0, 
							                            ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT, 0),
					new DataRequestMonitor<MIBreakInsertInfo>(ImmediateExecutor.getInstance(), rm) {
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
	}
	
	/**
	 * Now, run the program.
	 */
	@Execute
	public void stepRunProgram(final RequestMonitor rm) {
		ICommand<MIInfo> command;
		if (useContinueCommand()) {
			command = fCommandFactory.createMIExecContinue(fContainerDmc);
		} else {
			command = fCommandFactory.createMIExecRun(fContainerDmc);	
		}
		fCommandControl.queueCommand(command, new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm) {
			@Override
			protected void handleSuccess() {
				// Now that the process is started, the pid has been allocated
				// so we need to fetch the proper container context
				// We replace our current context which does not have the pid, with one that has the pid.
				
				if (fContainerDmc instanceof IMIContainerDMContext) {	
					fContainerDmc = fProcService.createContainerContextFromGroupId(fCommandControl.getContext(), ((IMIContainerDMContext)fContainerDmc).getGroupId());
					// This is the container context that this sequence is supposed to return: set the dataRm
					fDataRequestMonitor.setData(fContainerDmc);					
				} else {
					assert false : "Container context was not an IMIContainerDMContext"; //$NON-NLS-1$
				}
				rm.done();
			}
		});
	}
	
	/**
	 * In case of a restart, we must mark reverse debugging as disabled because
	 * GDB has turned it off. We may have to turn it back on after.
	 */
	@Execute
	public void stepSetReverseOff(RequestMonitor rm) {
		if (fRestart) {
			GDBRunControl_7_0 reverseService = fTracker.getService(GDBRunControl_7_0.class);
			if (reverseService != null) {
				reverseService.setReverseModeEnabled(false);
			}
		}
		rm.done();
	}
	
	/**
	 * Since we have started the program, we can turn on reverse debugging if needed.
	 * We know the program will stop since we set a breakpoint on main, to enable reverse.
	 */
	@Execute
	public void stepEnableReverse(RequestMonitor rm) {
		if (fReverseEnabled) {
			fReverseService.enableReverseMode(fCommandControl.getContext(), true, rm);
		} else {
			rm.done();
		}
	}
	
	/**
	 * Finally, if we are enabling reverse, and the userSymbolStop is not on main,
	 * we should do a continue because we are currently stopped on main but that 
	 * is not what the user requested
	 */
	@Execute
	public void stepContinue(RequestMonitor rm) {
		if (fReverseEnabled && !fUserBreakpointIsOnMain) {
			fCommandControl.queueCommand(fCommandFactory.createMIExecContinue(fContainerDmc),
					new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm));
		} else {
			rm.done();
		}
	}
	
	/**
	 * Cleanup now that the sequence has been run.
	 */
	@Execute
	public void stepCleanupBaseSequence(final RequestMonitor rm) {
		fTracker.dispose();
		fTracker = null;
		rm.done();
	}
	
    /**
     * This method indicates if we should use the -exec-continue command
     * instead of the -exec-run command.
     * This method can be overridden to allow for customization.
     */
    protected boolean useContinueCommand() {
    	// When doing remote debugging, we use -exec-continue instead of -exec-run
    	// Restart does not apply to remote sessions
    	IGDBBackend backend = fTracker.getService(IGDBBackend.class);
		if (backend == null) {
			return false;
		}
    	return backend.getSessionType() == SessionType.REMOTE;
    }

}