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

import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ReflectionSequence;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;

/** 
 * This sequence is used to start debugging a new process.
 *
 * @since 4.0
*/
public class DebugNewProcessSequence extends ReflectionSequence {

	private final static String INVALID = "invalid";   //$NON-NLS-1$

	private IGDBControl fCommandControl;
	private CommandFactory fCommandFactory;
	private IGDBBackend fBackend;
	private IGDBProcesses fProcService;
	private DsfServicesTracker fTracker;

	private IDMContext fContext;
	private String fBinaryName;
	private Map<String, Object> fAttributes;
	private IMIContainerDMContext fContainerCtx;
	
	// Store the dataRM so that we can fill it with the container context that we will be creating
	private DataRequestMonitor<IDMContext> fDataRequestMonitor;

	
	protected IMIContainerDMContext getContainerContext() {
		return fContainerCtx;
	}

	protected void setContainerContext(IMIContainerDMContext ctx) {
		fContainerCtx = ctx;
	}
	
	public DebugNewProcessSequence(DsfExecutor executor, boolean isInitial, IDMContext dmc, String file, Map<String, Object> attributes, DataRequestMonitor<IDMContext> rm) {
		super(executor, rm);
		fContext = dmc;
		fBinaryName = file;
		fAttributes = attributes;
		fDataRequestMonitor = rm;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			return new String[] {
					"stepInitializeBaseSequence",  //$NON-NLS-1$
					"stepSetEnvironmentVariables",   //$NON-NLS-1$
					"stepSetExecutable",   //$NON-NLS-1$
					"stepSetArguments",   //$NON-NLS-1$
					
					// For remote non-attach only
					"stepRemoteConnection",   //$NON-NLS-1$
					// For post-mortem launch only
					"stepSpecifyCoreFile",   //$NON-NLS-1$
					
					"stepStartTrackingBreakpoints", //$NON-NLS-1$
					"stepStartExecution",   //$NON-NLS-1$
					"stepCleanupBaseSequence",   //$NON-NLS-1$
			};
		}
		return null;
	}

	/** 
	 * Initialize the members of the DebugNewProcessSequence class.
	 * This step is mandatory for the rest of the sequence to complete.
	 */
	@Execute
	public void stepInitializeBaseSequence(RequestMonitor rm) {
		fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fContext.getSessionId());
		fBackend = fTracker.getService(IGDBBackend.class);
		fCommandControl = fTracker.getService(IGDBControl.class);
        fCommandFactory = fTracker.getService(IMICommandControl.class).getCommandFactory();		
		fProcService = fTracker.getService(IGDBProcesses.class);

        if (fBackend == null || fCommandControl == null || fCommandFactory == null || fProcService == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot obtain service", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

        // When we are starting to debug a new process, the container is the default process used by GDB.
        // We don't have a pid yet, so we can simply create the container with the UNIQUE_GROUP_ID
        setContainerContext(fProcService.createContainerContextFromGroupId(fCommandControl.getContext(), MIProcesses.UNIQUE_GROUP_ID));
        
        rm.done();
	}
	
	/** 
	 * Rollback method for {@link #stepInitializeBaseSequence()}
	 * @since 4.0 
	 */
	@RollBack("stepInitializeBaseSequence")
	public void rollBackInitializeBaseSequence(RequestMonitor rm) {
		if (fTracker != null) fTracker.dispose();
		fTracker = null;
		rm.done();
	}
	
	/**
	 * Specify environment variables if needed
	 */
	@Execute
	public void stepSetEnvironmentVariables(RequestMonitor rm) {
		boolean clear = false;
		Properties properties = new Properties();
		try {
			// here we need to pass the proper container context
			clear = fBackend.getClearEnvironment();
			properties = fBackend.getEnvironmentVariables();
		} catch (CoreException e) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Cannot get environment information", e)); //$NON-NLS-1$
			rm.done();
			return;
		}

		if (clear == true || properties.size() > 0) {
			// here we need to pass the proper container context
			fCommandControl.setEnvironment(properties, clear, rm);
		} else {
			rm.done();
		}
	}

	/**
	 * Specify the executable file to be debugged and read the symbol table.
	 */
	@Execute
	public void stepSetExecutable(RequestMonitor rm) {
		boolean noFileCommand = CDebugUtils.getAttribute(
				fAttributes, 
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP,
				IGDBLaunchConfigurationConstants.DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP_DEFAULT);

		if (!noFileCommand && fBinaryName != null && fBinaryName.length() > 0) {
			fCommandControl.queueCommand(
					fCommandFactory.createMIFileExecAndSymbols(getContainerContext(), fBinaryName), 
					new ImmediateDataRequestMonitor<MIInfo>(rm));
		} else {
			rm.done();
		}
	}
	
	/**
	 * Specify the arguments to the program that will be run.
	 */
	@Execute
	public void stepSetArguments(RequestMonitor rm) {
		try {
			String args = fBackend.getProgramArguments();

			if (args != null) {
				String[] argArray = args.replaceAll("\n", " ").split(" ");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				fCommandControl.queueCommand(
						fCommandFactory.createMIGDBSetArgs(getContainerContext(), argArray), 
						new ImmediateDataRequestMonitor<MIInfo>(rm));
			} else {
				rm.done();
			}
		} catch (CoreException e) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Cannot get inferior arguments", e)); //$NON-NLS-1$
			rm.done();
		}    		
	}

	/** 
	 * If we are dealing with a remote debugging session, connect to the target.
	 * @since 4.0
	 */
	@Execute
	public void stepRemoteConnection(RequestMonitor rm) {
		// If we are dealing with a non-attach remote session, it is now time to connect
		// to the remote side.  Note that this is the 'target remote' case
		// and not the 'target extended-remote' case (remote attach session)
		// This step is actually global for GDB.  However, we have to do it after
		// we have specified the executable, so we have to do it here.
		// It is safe to do it here because a 'target remote' does not support
		// multi-process so this step will not be executed more than once.
		if (fBackend.getSessionType() == SessionType.REMOTE && !fBackend.getIsAttachSession()) {
			boolean isTcpConnection = CDebugUtils.getAttribute(
					fAttributes,
					IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP,
					false);

			if (isTcpConnection) {
				String remoteTcpHost = CDebugUtils.getAttribute(
						fAttributes,
						IGDBLaunchConfigurationConstants.ATTR_HOST, INVALID);
				String remoteTcpPort = CDebugUtils.getAttribute(
						fAttributes,
						IGDBLaunchConfigurationConstants.ATTR_PORT, INVALID);

				fCommandControl.queueCommand(
						fCommandFactory.createMITargetSelect(fCommandControl.getContext(), 
								remoteTcpHost, remoteTcpPort, false), 
								new ImmediateDataRequestMonitor<MIInfo>(rm));
			} else {
				String serialDevice = CDebugUtils.getAttribute(
						fAttributes,
						IGDBLaunchConfigurationConstants.ATTR_DEV, INVALID);
				fCommandControl.queueCommand(
						fCommandFactory.createMITargetSelect(fCommandControl.getContext(), 
								serialDevice, false), 
								new ImmediateDataRequestMonitor<MIInfo>(rm));
			}
		} else {
			rm.done();
		}
	}

	/** @since 4.0 */
	protected static class PromptForCoreJob extends Job {
		protected DataRequestMonitor<String> fRequestMonitor;

		public PromptForCoreJob(String name, DataRequestMonitor<String> rm) {
			super(name);
			fRequestMonitor = rm;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final IStatus promptStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 200/*STATUS_HANDLER_PROMPT*/, "", null); //$NON-NLS-1$//$NON-NLS-2$
			final IStatus filePrompt = new Status(IStatus.INFO, "org.eclipse.cdt.dsf.gdb.ui", 1001, "", null); //$NON-NLS-1$//$NON-NLS-2$
			// consult a status handler
			final IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);

			final Status NO_CORE_STATUS = new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1,
					LaunchMessages.getString("LocalCDILaunchDelegate.6"), //$NON-NLS-1$
					null);

			if (prompter == null) {
				fRequestMonitor.setStatus(NO_CORE_STATUS);
				fRequestMonitor.done();
				return Status.OK_STATUS;
			} 				

			try {
				Object result = prompter.handleStatus(filePrompt, null);
				 if (result == null) {
						fRequestMonitor.cancel();
				} else if (result instanceof String) {
					fRequestMonitor.setData((String)result);
				} else {
					fRequestMonitor.setStatus(NO_CORE_STATUS);
				}
			} catch (CoreException e) {
				fRequestMonitor.setStatus(NO_CORE_STATUS);
			}
			fRequestMonitor.done();

			return Status.OK_STATUS;
		}
	};
	
	/** 
	 * If we are dealing with a postmortem session, connect to the core/trace file.
	 * @since 4.0
	 */
	@Execute
	public void stepSpecifyCoreFile(final RequestMonitor rm) {
		// If we are dealing with a postmortem session, it is now time to connect
		// to the core/trace file.  We have to do this step after
		// we have specified the executable, so we have to do it here.
		// It is safe to do it here because a postmortem session does not support
		// multi-process so this step will not be executed more than once.
		// Bug 338730
		if (fBackend.getSessionType() == SessionType.CORE) {
			String coreFile = CDebugUtils.getAttribute(
					fAttributes,
					ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, ""); //$NON-NLS-1$
			final String coreType = CDebugUtils.getAttribute(
					fAttributes,
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_POST_MORTEM_TYPE,
					IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TYPE_DEFAULT);
			
				if (coreFile.length() == 0) {
					new PromptForCoreJob(
							"Prompt for post mortem file",  //$NON-NLS-1$
							new DataRequestMonitor<String>(getExecutor(), rm) {
								@Override
								protected void handleCancel() {
									rm.cancel();
									rm.done();
								}
								@Override
								protected void handleSuccess() {
									String newCoreFile = getData();
									if (newCoreFile == null || newCoreFile.length()== 0) {
										rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get post mortem file path", null)); //$NON-NLS-1$
										rm.done();
									} else {
										if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_CORE_FILE)) {
											fCommandControl.queueCommand(
													fCommandFactory.createMITargetSelectCore(fCommandControl.getContext(), newCoreFile), 
													new DataRequestMonitor<MIInfo>(getExecutor(), rm));
										} else if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TRACE_FILE)) {
											IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);
											if (traceControl != null) {
												ITraceTargetDMContext targetDmc = DMContexts.getAncestorOfType(fCommandControl.getContext(), ITraceTargetDMContext.class);
												traceControl.loadTraceData(targetDmc, newCoreFile, rm);
											} else {
												rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Tracing not supported", null));
												rm.done();                                  
											}
										} else {
											rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Invalid post-mortem type", null));
											rm.done();
										}
									}
								}
							}).schedule();
				} else {
					if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_CORE_FILE)) {
						fCommandControl.queueCommand(
								fCommandFactory.createMITargetSelectCore(fCommandControl.getContext(), coreFile),
								new DataRequestMonitor<MIInfo>(getExecutor(), rm));
					} else if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TRACE_FILE)) {
						IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);
						if (traceControl != null) {
							ITraceTargetDMContext targetDmc = DMContexts.getAncestorOfType(fCommandControl.getContext(), ITraceTargetDMContext.class);
							traceControl.loadTraceData(targetDmc, coreFile, rm);
						} else {
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Tracing not supported", null));
							rm.done();
						}
					} else {
						rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Invalid post-mortem type", null));
						rm.done();
					}
				}
		} else {
			rm.done();
		}
	}
	
	/**
	 * Start tracking the breakpoints.  Note that for remote debugging
	 * we should first connect to the target.
	 */
	@Execute
	public void stepStartTrackingBreakpoints(RequestMonitor rm) {
		if (fBackend.getSessionType() != SessionType.CORE) {
			MIBreakpointsManager bpmService = fTracker.getService(MIBreakpointsManager.class);
			IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(getContainerContext(), IBreakpointsTargetDMContext.class);
			bpmService.startTrackingBreakpoints(bpTargetDmc, rm);
		} else {
			rm.done();
		}
	}

	/**
	 * Start executing the program.
	 */
	@Execute
	public void stepStartExecution(final RequestMonitor rm) {
		if (fBackend.getSessionType() != SessionType.CORE) {
			// Overwrite the program name to use the binary name that was specified.
			// This is important for multi-process
			// Bug 342351
			fAttributes.put(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, fBinaryName);
			
			fProcService.start(getContainerContext(), fAttributes, new ImmediateDataRequestMonitor<IContainerDMContext>(rm) {
				@Override
				protected void handleSuccess() {
					assert getData() instanceof IMIContainerDMContext;

					// Set the container that we created
					setContainerContext(DMContexts.getAncestorOfType(getData(), IMIContainerDMContext.class));
					fDataRequestMonitor.setData(getContainerContext());

					// Don't call fDataRequestMonitor.done(), the sequence will
					// automatically do that when it completes;
					rm.done();
				}
			});
		} else {
			fDataRequestMonitor.setData(getContainerContext());
			rm.done();
		}
	}
	
	/**
	 * Cleanup now that the sequence has been run.
	 * @since 4.0
	 */
	@Execute
	public void stepCleanupBaseSequence(RequestMonitor rm) {
		fTracker.dispose();
		fTracker = null;
		rm.done();
	}
}