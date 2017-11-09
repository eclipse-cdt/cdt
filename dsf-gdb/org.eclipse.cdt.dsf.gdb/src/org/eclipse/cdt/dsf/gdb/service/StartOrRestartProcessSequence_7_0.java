/*******************************************************************************
 * Copyright (c) 2011, 2017 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Anton Gorenkov - Need to use a process factory (Bug 210366)
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ReflectionSequence;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowNewConsoleInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.pty.PersistentPTY;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
	private IGDBBackend fBackend;
	
	private DsfServicesTracker fTracker;

	// This variable will be used to store the original container context,
	// but once the new process is started (restarted), it will contain the new
	// container context.  This new container context has for parent the process
	// context, which holds the new pid.
	private IContainerDMContext fContainerDmc;
	
	// If the user requested a stop_on_main, this variable will hold the breakpoint
	private MIBreakpoint fUserBreakpoint;
	// Since the stop_on_main option allows the user to set the breakpoint on any
	// symbol, we use this variable to know if the stop_on_main breakpoint was really
	// on the main() method.
	private boolean fUserBreakpointIsOnMain;
	
	private MIBreakpoint fBreakPointForReverse;
	private boolean fReverseEnabled;
	private final Map<String, Object> fAttributes;
	
	// Indicates if the sequence is being used for a restart or a start
	private final boolean fRestart;
	
	private PTY fPty;
	
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

	/**
	 * @since 5.2
	 */
	protected MIBreakpoint getBreakPointForReverse() {
		return fBreakPointForReverse;
	}

	protected boolean getUserBreakpointIsOnMain() {
		return fUserBreakpointIsOnMain;
	}

	/** @since 5.0 */
	protected boolean getReverseEnabled() {
		return fReverseEnabled;
	}
	
	public StartOrRestartProcessSequence_7_0(DsfExecutor executor, IContainerDMContext containerDmc,
			Map<String, Object> attributes, boolean restart, DataRequestMonitor<IContainerDMContext> rm) {
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
			return new String[] {
					"stepInitializeBaseSequence",  //$NON-NLS-1$
					"stepInsertStopOnMainBreakpoint",  //$NON-NLS-1$
					"stepSetBreakpointForReverse",   //$NON-NLS-1$
					"stepInitializeInputOutput",   //$NON-NLS-1$
					"stepCreateConsole",    //$NON-NLS-1$
					"stepRunProgram",   //$NON-NLS-1$
					"stepSetReverseOff",   //$NON-NLS-1$
					"stepEnableReverse",   //$NON-NLS-1$
					"stepContinue",   //$NON-NLS-1$
					"stepCleanupBaseSequence",   //$NON-NLS-1$
			};
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
		fBackend = fTracker.getService(IGDBBackend.class);

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
	 * If the user requested a 'stopAtMain', let's set the temporary breakpoint
	 * where the user specified.
	 */
	@Execute
	public void stepInsertStopOnMainBreakpoint(final RequestMonitor rm) {
		boolean userRequestedStop = CDebugUtils.getAttribute(fAttributes, 
				ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
				LaunchUtils.getStopAtMainDefault());

		if (userRequestedStop) {
			String userStopSymbol = CDebugUtils.getAttribute(fAttributes, 
					ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
					LaunchUtils.getStopAtMainSymbolDefault());

			IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(getContainerContext(),
					IBreakpointsTargetDMContext.class);

			fCommandControl.queueCommand(
					fCommandFactory.createMIBreakInsert(bpTargetDmc, true, false, null, 0, userStopSymbol, "0"), //$NON-NLS-1$
					new ImmediateDataRequestMonitor<MIBreakInsertInfo>(rm) {
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
							ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT, "0"), //$NON-NLS-1$
					new ImmediateDataRequestMonitor<MIBreakInsertInfo>(rm) {
						@Override
						public void handleSuccess() {
							if (getData() != null) {
								MIBreakpoint[] breakpoints = getData().getMIBreakpoints();
								if (breakpoints.length > 0) {
									fBreakPointForReverse = breakpoints[0];
									if (fUserBreakpoint != null) {
										fUserBreakpointIsOnMain = fBreakPointForReverse.getAddress().equals(fUserBreakpoint.getAddress());
									}
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
     * This method does the necessary work to setup the input/output streams for the
     * inferior process, by either preparing the PTY to be used, or by simply leaving
     * the PTY null, which indicates that the input/output streams are handled externally;
     * this decision is based on the type of session.
     */
	@Execute
    public void stepInitializeInputOutput(final RequestMonitor rm) {
    	if (fBackend.getSessionType() == SessionType.REMOTE) {
    		// The program input and output for a remote session is handled by gdbserver. 
    		// Therefore, no need to create a pty.
    		fPty = null;
    		rm.done();
    	} else {
    		if (fRestart) {
    			// For a restart we re-use the previous PersistentPTY (or no pty if we couldn't tell GDB about it)
    			assert fPty instanceof PersistentPTY || fPty == null;
    			rm.done();
    			return;
    		}
    
			boolean externalConsoleDefault = Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
					IGdbDebugPreferenceConstants.PREF_EXTERNAL_CONSOLE,
					IGDBLaunchConfigurationConstants.DEBUGGER_EXTERNAL_CONSOLE_DEFAULT, null);

    		boolean externalConsole = CDebugUtils.getAttribute(fAttributes,
    				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_EXTERNAL_CONSOLE,
    				externalConsoleDefault);
    		if (externalConsole) {
	    		initializeExternalConsole(new ImmediateRequestMonitor(rm) {
					@Override
					protected void handleCompleted() {
						if (isSuccess()) {
							fPty = null;
							rm.done();
						} else {
							initializePty(rm);
						}
	    			}
	    		});
    		} else {
				initializePty(rm);
    		}
    	}
    }

	private void initializeExternalConsole(final RequestMonitor rm) {
		fCommandControl.queueCommand(fCommandFactory.createMIGDBShowNewConsole(getContainerContext()),
				new ImmediateDataRequestMonitor<MIGDBShowNewConsoleInfo>(rm) {
					@Override
					protected void handleSuccess() {
						fCommandControl.queueCommand(
								fCommandFactory.createMIGDBSetNewConsole(getContainerContext(), true),
								new ImmediateDataRequestMonitor<MIInfo>(rm));
					}
				});
	}

	private void initializePty(final RequestMonitor rm) {
		// Every other type of session that can get to this code, is starting a new process
		// and requires a pty for it.
		try {
			// Use a PersistentPTY so it can be re-used for restarts.
			// It is possible that the inferior will be restarted by the user from
			// the GDB console, in which case, we are not able to create a new PTY
			// for it; using a persistentPTY allows this to work since the persistentPTY
			// does not need to be replaced but can continue to be used.
			fPty = new PersistentPTY();
			fPty.validateSlaveName();

			// Tell GDB to use this PTY
			fCommandControl.queueCommand(
					fCommandFactory.createMIInferiorTTYSet((IMIContainerDMContext)getContainerContext(), fPty.getSlaveName()), 
					new ImmediateDataRequestMonitor<MIInfo>(rm) {
						@Override
						protected void handleFailure() {
							// We were not able to tell GDB to use the PTY
							// so we won't use it at all.
			    			fPty = null;
			        		rm.done();
						}
					});
		} catch (IOException e) {
			fPty = null;
			rm.done();
		}
	}
	
	/** 
	 * @since 4.7 
	 * @deprecated The creation of MIInferiorProcess has been moved to the IGDBProcesses service
	 */
	@Deprecated
	protected MIInferiorProcess createInferiorProcess(IContainerDMContext container, OutputStream outputStream) {
		return new MIInferiorProcess(container, outputStream);
	}

	/** 
	 * @since 4.7 
	 * @deprecated The creation of MIInferiorProcess has been moved to the IGDBProcesses service
	 */
	@Deprecated
	protected MIInferiorProcess createInferiorProcess(IContainerDMContext container, PTY pty) {
		return new MIInferiorProcess(container, pty);
	}

	/**
	 * Before running the program, we must create its console for IO.
	 */
	@Execute
	public void stepCreateConsole(RequestMonitor rm) {
    	if (fBackend.getSessionType() == SessionType.REMOTE) {
    		// The program output for a remote session is handled by gdbserver. Therefore,
    		// no need to create an inferior process and add it to the launch.
    		rm.done();
    		return;
    	}

		if (fRestart) {
			// For a restart, the IGDBProcesses service already handles creating the new
			// console.  We do it this way because a restart can be triggered by the user
			// on the GDB console, so this class isn't even called in that case.
			rm.done();
			return;
		}

    	// For multi-process, we cannot simply use the name given by the backend service
		// because we may not be starting that process, but another one.
		// Instead, we can look in the attributes for the binary name, which we stored
		// there for this case, specifically.
		// Bug 342351
		IGDBBackend backend = fTracker.getService(IGDBBackend.class);
		String defaultPathName = backend.getProgramPath().lastSegment();
		if (defaultPathName == null) {
			defaultPathName = ""; //$NON-NLS-1$
		}
		String progPathName =
				CDebugUtils.getAttribute(fAttributes,
						ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
						defaultPathName);
		final String pathLabel = new Path(progPathName).lastSegment();    			 

		// Adds the inferior to the launch which will also create the console
		fProcService.addInferiorToLaunch(fContainerDmc, pathLabel, fPty, new ImmediateRequestMonitor() {
			@Override
			protected void handleCompleted() {
				// Accept errors.  The console won't be created but the rest will mostly work
				// This can especially happen if extenders did not implement IGDBProcesses.addInferiorToLaunch()
				rm.done();
			}
		});
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
		fCommandControl.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo>(rm) {
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
					new ImmediateDataRequestMonitor<MIInfo>(rm));
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
    	// Note that restart does not apply to remote sessions
    	IGDBBackend backend = fTracker.getService(IGDBBackend.class);
		if (backend == null) {
			return false;
		}
    	// When doing remote non-attach debugging, we use -exec-continue instead of -exec-run
		// For remote attach, if we get here it is that we are starting a new process
		// (multi-process), so we want to use -exec-run
    	return backend.getSessionType() == SessionType.REMOTE && !backend.getIsAttachSession();
    }
}