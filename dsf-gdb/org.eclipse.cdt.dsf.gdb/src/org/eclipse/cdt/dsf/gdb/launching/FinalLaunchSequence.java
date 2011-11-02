/*******************************************************************************
 * Copyright (c) 2008, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation          
 *     Nokia - create and use backend service. 
 *     IBM Corporation 
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ReflectionSequence;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.actions.IConnect;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.CSourceLookup;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;

public class FinalLaunchSequence extends ReflectionSequence {
	// The launchConfiguration attributes
	private Map<String, Object> fAttributes;

	private IGDBControl fCommandControl;
	private IGDBBackend	fGDBBackend;
	private IMIProcesses fProcService;
	private CommandFactory fCommandFactory;

	private DsfServicesTracker fTracker;
	private DsfSession fSession;

	/**
	 * @since 4.0
	 */
	public FinalLaunchSequence(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		super(session.getExecutor(), rm, LaunchMessages.getString("FinalLaunchSequence.0"), LaunchMessages.getString("FinalLaunchSequence.1"));     //$NON-NLS-1$ //$NON-NLS-2$
		fSession = session;
		fAttributes = attributes;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			return new String[] {
					"stepInitializeFinalLaunchSequence",   //$NON-NLS-1$
					// Global GDB settings
					"stepSetEnvironmentDirectory",   //$NON-NLS-1$
					"stepSetBreakpointPending",    //$NON-NLS-1$
					"stepEnablePrettyPrinting",    //$NON-NLS-1$
					"stepSourceGDBInitFile",   //$NON-NLS-1$
					"stepSetNonStop",   //$NON-NLS-1$
					"stepSetAutoLoadSharedLibrarySymbols",   //$NON-NLS-1$
					"stepSetSharedLibraryPaths",   //$NON-NLS-1$
					
					// -environment-directory with a lot of paths could
					// make setting breakpoint incredibly slow, which makes
					// the debug session un-workable.  We simply stop
					// using it because it's usefulness is unclear.
					// Bug 225805
					//
					// "stepSetSourceLookupPath",   //$NON-NLS-1$
					
					// For remote-attach launch only
					"stepRemoteConnection",   //$NON-NLS-1$
					// For all launches except attach ones
					"stepNewProcess", //$NON-NLS-1$
					// For local attach launch only
					"stepAttachToProcess",   //$NON-NLS-1$
					// Global
					"stepDataModelInitializationComplete",   //$NON-NLS-1$
					"stepCleanup",   //$NON-NLS-1$
			};
		}
		return null;
	}

	/** 
	 * Initialize the members of the FinalLaunchSequence class.
	 * This step is mandatory for the rest of the sequence to complete.
	 * @since 4.0 
	 */
	@Execute
	public void stepInitializeFinalLaunchSequence(RequestMonitor requestMonitor) {
		fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSession.getId());
		fGDBBackend = fTracker.getService(IGDBBackend.class);
		if (fGDBBackend == null) {
			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain GDBBackend service", null)); //$NON-NLS-1$
			requestMonitor.done();
			return;
		}

		fCommandControl = fTracker.getService(IGDBControl.class);
		if (fCommandControl == null) {
			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain control service", null)); //$NON-NLS-1$
			requestMonitor.done();
			return;
		}

		fCommandFactory = fCommandControl.getCommandFactory();

		fProcService = fTracker.getService(IMIProcesses.class);
		if (fProcService == null) {
			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain process service", null)); //$NON-NLS-1$
			requestMonitor.done();
			return;
		}

		requestMonitor.done();
	}

	/** 
	 * Rollback method for {@link #stepInitializeFinalLaunchSequence()}
	 * @since 4.0 
	 */
	@RollBack("stepInitializeFinalLaunchSequence")
	public void rollBackInitializeFinalLaunchSequence(RequestMonitor requestMonitor) {
		if (fTracker != null) fTracker.dispose();
		fTracker = null;
		requestMonitor.done();
	}

	/**
	 * Specify GDB's working directory. 
	 * @since 4.0 
	 */
	@Execute
	public void stepSetEnvironmentDirectory(final RequestMonitor requestMonitor) {
		IPath dir = null;
		try {
			dir = fGDBBackend.getGDBWorkingDirectory();
		} catch (CoreException e) {
			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get working directory", e)); //$NON-NLS-1$
			requestMonitor.done();
			return;
		}

		if (dir != null) {
			fCommandControl.queueCommand(
					fCommandFactory.createMIEnvironmentCD(fCommandControl.getContext(), dir.toPortableString()), 
					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
		} else {
			requestMonitor.done();
		}
	}
	
    /**
     * Allow breakpoints/tracepoints to be set as pending when using the gdb console 
     * or a CLI command to create them.
     * @since 4.0
     */
	@Execute
	public void stepSetBreakpointPending(final RequestMonitor requestMonitor) {
		if (fGDBBackend.getSessionType() != SessionType.CORE) {
			fCommandControl.queueCommand(
					fCommandFactory.createMIGDBSetBreakpointPending(fCommandControl.getContext(), true),
					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
		} else {
			requestMonitor.done();
		}
	}

	/**
	 * Turn on pretty printers for MI variable objects, if enabled in preferences.
	 * Also, turn off error messages from python, all the time.
	 * @since 4.0
	 */
	@Execute
	public void stepEnablePrettyPrinting(final RequestMonitor requestMonitor) {
		if (Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_ENABLE_PRETTY_PRINTING,
				false, null)) {

			fCommandControl.enablePrettyPrintingForMIVariableObjects(
					new RequestMonitor(getExecutor(), requestMonitor) {
						@Override
						protected void handleCompleted() {
							fCommandControl.setPrintPythonErrors(false, requestMonitor);
						}
					});
		} else {
			fCommandControl.setPrintPythonErrors(false, requestMonitor);
		}
	}

	/**
	 * Source the gdbinit file specified in the launch.
	 * @since 4.0 
	 */
	@Execute
	public void stepSourceGDBInitFile(final RequestMonitor requestMonitor) {
		try {
			final String gdbinitFile = fGDBBackend.getGDBInitFile();

			if (gdbinitFile != null && gdbinitFile.length() > 0) {
				fCommandControl.queueCommand(
						fCommandFactory.createCLISource(fCommandControl.getContext(), gdbinitFile), 
						new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
							@Override
							protected void handleCompleted() {
								// If the gdbinitFile is the default, then it may not exist and we
								// should not consider this an error.
								// If it is not the default, then the user must have specified it and
								// we want to warn the user if we can't find it.
								if (!gdbinitFile.equals(IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT )) {
									requestMonitor.setStatus(getStatus());
								}
								requestMonitor.done();
							}
						});
			} else {
				requestMonitor.done();
			}
		} catch (CoreException e) {
			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get gdbinit option", e)); //$NON-NLS-1$
			requestMonitor.done();
		}
	}

	/**
	 * Enable non-stop mode if requested.
	 * @since 4.0 
	 */
	@Execute
	public void stepSetNonStop(final RequestMonitor requestMonitor) {
		boolean isNonStop = CDebugUtils.getAttribute(
				fAttributes,
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
				LaunchUtils.getIsNonStopModeDefault());

		// GDBs that don't support non-stop don't allow you to set it to false.
		// We really should set it to false when GDB supports it though.
		// Something to fix later.
		if (isNonStop) {
			fCommandControl.queueCommand(
					fCommandFactory.createMIGDBSetTargetAsync(fCommandControl.getContext(), true),
					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
						@Override
						protected void handleSuccess() {
							fCommandControl.queueCommand(
									fCommandFactory.createMIGDBSetPagination(fCommandControl.getContext(), false), 
									new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
										@Override
										protected void handleSuccess() {
											fCommandControl.queueCommand(
													fCommandFactory.createMIGDBSetNonStop(fCommandControl.getContext(), true), 
													new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
										}
									});
						}
					});
		} else {
			requestMonitor.done();
		}
	}

	/**
	 * Tell GDB to automatically load or not the shared library symbols
	 * @since 4.0 
	 */
	@Execute
	public void stepSetAutoLoadSharedLibrarySymbols(RequestMonitor requestMonitor) {
		boolean autolib = CDebugUtils.getAttribute(
				fAttributes,
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB,
				IGDBLaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT);

		fCommandControl.queueCommand(
				fCommandFactory.createMIGDBSetAutoSolib(fCommandControl.getContext(), autolib), 
				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
	}

	/**
	 * Set the shared library paths.
	 * @since 4.0 
	 */
	@Execute
	public void stepSetSharedLibraryPaths(final RequestMonitor requestMonitor) {
		try {
			List<String> p = fGDBBackend.getSharedLibraryPaths();

			if (p.size() > 0) {
				String[] paths = p.toArray(new String[p.size()]);
				fCommandControl.queueCommand(
						fCommandFactory.createMIGDBSetSolibSearchPath(fCommandControl.getContext(), paths), 
						new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
							@Override
							protected void handleSuccess() {
								// Sysroot is not available in GDB6.6 and will make the launch fail in that case.
								// Let's remove it for now
								requestMonitor.done();
								//   	                			// If we are able to set the solib-search-path,
								//   	                			// we should disable the sysroot variable, as indicated
								//   	                			// in the GDB documentation.  This is to avoid the sysroot
								//   	                			// variable finding libraries that were not meant to be found.
								//   	        	                fCommandControl.queueCommand(
								//   	        	   	                	new MIGDBSetSysroot(fCommandControl.getContext()), 
								//   	        	   	                	new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
							};
						});
			} else {
				requestMonitor.done();
			}
		} catch (CoreException e) {
			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot set share library paths", e)); //$NON-NLS-1$
			requestMonitor.done();
		}
	}

	/**
	 * Setup the source paths.
	 * @since 4.0 
	 */
	@Execute
	public void stepSetSourceLookupPath(RequestMonitor requestMonitor) {
		CSourceLookup sourceLookup = fTracker.getService(CSourceLookup.class);
		ILaunch launch = (ILaunch)fSession.getModelAdapter(ILaunch.class);
		CSourceLookupDirector locator = (CSourceLookupDirector)launch.getSourceLocator();
		ISourceLookupDMContext sourceLookupDmc = (ISourceLookupDMContext)fCommandControl.getContext();

		sourceLookup.setSourceLookupPath(sourceLookupDmc, locator.getSourceContainers(), requestMonitor);
	}

	private final static String INVALID = "invalid";   //$NON-NLS-1$
	/** 
	 * If we are dealing with a remote-attach debugging session, connect to the target.
	 * @since 4.0
	 */
	@Execute
	public void stepRemoteConnection(final RequestMonitor rm) {
		if (fGDBBackend.getSessionType() == SessionType.REMOTE && fGDBBackend.getIsAttachSession()) {
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
								remoteTcpHost, remoteTcpPort, true), 
								new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm));
			} else {
				String serialDevice = CDebugUtils.getAttribute(
						fAttributes,
						IGDBLaunchConfigurationConstants.ATTR_DEV, INVALID);

				fCommandControl.queueCommand(
						fCommandFactory.createMITargetSelect(fCommandControl.getContext(), 
								serialDevice, true), 
								new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm));
			}
		} else {
			rm.done();
		}
	}

	/**
	 * Start a new process if we are not dealing with an attach session
	 * i.e., a local session, a remote session or a post-mortem (core) session.
	 * @since 4.0
	 */
	@Execute
	public void stepNewProcess(final RequestMonitor rm) {
		if (!fGDBBackend.getIsAttachSession()) {

			boolean noBinarySpecified = CDebugUtils.getAttribute(
					fAttributes,
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP,
					IGDBLaunchConfigurationConstants.DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP_DEFAULT);

			String binary = null;
			final IPath execPath = fGDBBackend.getProgramPath();
			if (!noBinarySpecified && execPath != null && !execPath.isEmpty()) {
				binary = execPath.toPortableString();
			}

			// Even if binary is null, we must call this to do all the other steps
			// necessary to create a process.  It is possible that the binary is not needed
			fProcService.debugNewProcess(fCommandControl.getContext(), binary, fAttributes, 
					new DataRequestMonitor<IDMContext>(getExecutor(), rm) {
				@Override
				protected void handleCancel() {
					// If this step is cancelled, cancel the current sequence.
					// This is to allow the user to press the cancel button
					// when prompted for a post-mortem file.
					// Bug 362105
					rm.cancel();
        			rm.done();
				}
			});
		} else {
			rm.done();
		}
	}

	/**
	 * If we are dealing with an local attach session, perform the attach.
     * For a remote attach session, we don't attach during the launch; instead
     * we wait for the user to manually do the attach.
	 * @since 4.0 
	 */
	@Execute
	public void stepAttachToProcess(final RequestMonitor requestMonitor) {
		if (fGDBBackend.getIsAttachSession() && fGDBBackend.getSessionType() != SessionType.REMOTE) {
			// Is the process id already stored in the launch?
			int pid = CDebugUtils.getAttribute(
					fAttributes,
					ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, -1);

			if (pid != -1) {
				fProcService.attachDebuggerToProcess(
						fProcService.createProcessContext(fCommandControl.getContext(), Integer.toString(pid)),
						new DataRequestMonitor<IDMContext>(getExecutor(), requestMonitor));
			} else {
				IConnect connectCommand = (IConnect)fSession.getModelAdapter(IConnect.class);
				if (connectCommand != null) {
					connectCommand.connect(requestMonitor);
				} else {
					requestMonitor.done();
				}
			}
		} else {
			requestMonitor.done();
		}
	}
	
	/**
	 * Indicate that the Data Model has been filled.  This will trigger the Debug view to expand.
	 * @since 4.0
	 */
	@Execute
	public void stepDataModelInitializationComplete(final RequestMonitor requestMonitor) {
		fSession.dispatchEvent(new DataModelInitializedEvent(fCommandControl.getContext()),
				fCommandControl.getProperties());
		requestMonitor.done();
	}
	
	/**
	 * Cleanup now that the sequence has been run.
	 * @since 4.0
	 */
	@Execute
	public void stepCleanup(final RequestMonitor requestMonitor) {
		fTracker.dispose();
		fTracker = null;
		requestMonitor.done();
	}
}

