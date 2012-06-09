/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc Khouzam (Ericsson) - Support setting the path in which the core file 
 *                               dialog should start (Bug 362039)
 *     Sergey Prigogin (Google) - Bug 381804 
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.io.File;
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
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.VariablesPlugin;
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
				String[] argArray = CommandLineUtil.argumentsToArray(args);
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
		/**
		 * The initial path that should be used in the prompt for the core file
		 * @since 4.1 
		 */
		protected String fInitialPath;
		protected DataRequestMonitor<String> fRequestMonitor;

		public PromptForCoreJob(String name, DataRequestMonitor<String> rm) {
			this(name, null, null, rm);
		}

		/** @since 4.1 */
		public PromptForCoreJob(String name, String coreType, String initialPath, DataRequestMonitor<String> rm) {
			super(name);
			fInitialPath = initialPath;
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
				Object result = prompter.handleStatus(filePrompt, fInitialPath);
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
			
			try {
				// Support variable substitution for the core file path
				// Bug 362039
				coreFile = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(coreFile, false);
			} catch (CoreException e) {
				// Ignore and use core file string as is.
				// This should not happen because the dialog will
				// prevent the user from making such mistakes
			}

			final String coreType = CDebugUtils.getAttribute(
					fAttributes,
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_POST_MORTEM_TYPE,
					IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TYPE_DEFAULT);
			
			// We handle three cases:
			// 1- Core file specified, in which case we use it
			// 2- Nothing specified, in which case we prompt for a core file path
			// 3- Path to a directory, in which case we prompt for a core file starting at the specified path
			boolean shouldPrompt = false;
			coreFile = coreFile.trim();
			if (coreFile.length() == 0) {
				shouldPrompt = true;
			} else {
				File filePath = new File(coreFile);
				if (filePath.isDirectory()) {
					// The user provided a directory.  We need to prompt for an actual
					// core file, but we'll start off in the specified directory
					// Bug 362039
					shouldPrompt = true;
				}
				// else not a directory but an actual core file: use it.
			}
			
			if (shouldPrompt) {
					new PromptForCoreJob(
							"Prompt for post mortem file",  //$NON-NLS-1$
							coreType,
							coreFile,
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
										rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, Messages.Cannot_get_post_mortem_file_path_error, null)); 
										rm.done();
									} else {
										targetSelectFile(coreType, newCoreFile, rm);
									}
								}
							}).schedule();
				} else {
					// The user specified something that was not a directory,
					// it therefore should be the core file itself.  Let's use it.
					
					// First convert to absolute path so that things work even if the user
					// specifies a relative path.  The reason we do this is that GDB
					// may not be using the same root path as Eclipse.
					String absoluteCoreFile = new Path(coreFile).toFile().getAbsolutePath();
					targetSelectFile(coreType, absoluteCoreFile, rm);
				}
		} else {
			rm.done();
		}
	}
	
	private void targetSelectFile(String coreType, String file, RequestMonitor rm) {
		if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_CORE_FILE)) {
			fCommandControl.queueCommand(
					fCommandFactory.createMITargetSelectCore(fCommandControl.getContext(), file),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm));
		} else if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TRACE_FILE)) {
			IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);
			if (traceControl != null) {
				ITraceTargetDMContext targetDmc = DMContexts.getAncestorOfType(fCommandControl.getContext(), ITraceTargetDMContext.class);
				traceControl.loadTraceData(targetDmc, file, rm);
			} else {
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, Messages.Tracing_not_supported_error, null));
				rm.done();
			}
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, Messages.Invalid_post_mortem_type_error, null));
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
