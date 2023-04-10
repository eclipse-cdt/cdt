/*******************************************************************************
 * Copyright (c) 2008, 2018 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Nokia - create and use backend service.
 *     IBM Corporation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Sergey Prigogin (Google)
 *     Marc Khouzam (Ericsson) - No longer call method to check non-stop for GDB < 7.0 (Bug 365471)
 *     Mathias Kunter - Support for different charsets (bug 370462)
 *     Anton Gorenkov - A preference to use RTTI for variable types determination (Bug 377536)
 *     Xavier Raynaud (Kalray) - Avoid duplicating fields in sub-classes (add protected accessors)
 *     Marc Khouzam (Ericsson) - Output the version of GDB at startup (Bug 455408)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.IConnectHandler;
import org.eclipse.cdt.debug.internal.core.DebugStringVariableSubstitutor;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ReflectionSequence;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.actions.IConnect;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.gdb.service.IGDBSourceLookup;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.CSourceLookup;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBVersionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;

/**
 * Final launch sequence for GDB < 7.0, extended by subclasses for newer versions.
 *
 * @see FinalLaunchSequence_7_0
 */
public class FinalLaunchSequence extends ReflectionSequence {
	// The launchConfiguration attributes
	private Map<String, Object> fAttributes;

	private IGDBControl fCommandControl;
	private IGDBBackend fGDBBackend;
	private IGDBProcesses fProcService;
	private CommandFactory fCommandFactory;

	private DsfServicesTracker fTracker;
	private DsfSession fSession;

	/**
	 * @since 4.0
	 */
	public FinalLaunchSequence(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		super(session.getExecutor(), rm, LaunchMessages.getString("FinalLaunchSequence.0"), //$NON-NLS-1$
				LaunchMessages.getString("FinalLaunchSequence.1")); //$NON-NLS-1$
		fSession = session;
		fAttributes = attributes;
	}

	/**
	 * Gets the DsfSession of this launch sequence.
	 * @return the {@link DsfSession}
	 * @since 4.3
	 */
	protected DsfSession getSession() {
		return fSession;
	}

	/**
	 * Gets the launch configuration attributes, as a {@link Map}.
	 * @return the launch configuration attributes
	 * @since 4.3
	 */
	protected Map<String, Object> getAttributes() {
		return fAttributes;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			return new String[] { "stepInitializeFinalLaunchSequence", //$NON-NLS-1$
					// Global GDB settings
					"stepGDBVersion", //$NON-NLS-1$
					"stepSetEnvironmentDirectory", //$NON-NLS-1$
					"stepSetBreakpointPending", //$NON-NLS-1$
					"stepEnablePrettyPrinting", //$NON-NLS-1$
					"stepSetPrintObject", //$NON-NLS-1$
					"stepSetCharset", //$NON-NLS-1$
					"stepSourceGDBInitFile", //$NON-NLS-1$
					"stepSetAutoLoadSharedLibrarySymbols", //$NON-NLS-1$
					"stepSetSharedLibraryPaths", //$NON-NLS-1$
					"stepSetSourceSubstitutePath", //$NON-NLS-1$
					"stepSetRemoteTimeout", //$NON-NLS-1$

					// -environment-directory with a lot of paths could
					// make setting breakpoint incredibly slow, which makes
					// the debug session un-workable.  We simply stop
					// using it because it's usefulness is unclear.
					// Bug 225805
					//
					// "stepSetSourceLookupPath",   //$NON-NLS-1$

					// For remote-attach launch only (deprecated, see javadocs)
					"stepRemoteConnection", //$NON-NLS-1$
					// For all launches except attach ones
					"stepNewProcess", //$NON-NLS-1$
					// For all attach launch only
					"stepAttachToProcess", //$NON-NLS-1$
					// For remote attach launch only (deprecated, see javadocs)
					"stepAttachRemoteToDebugger", //$NON-NLS-1$
					// Global
					"stepDataModelInitializationComplete", //$NON-NLS-1$
					"stepCleanup", //$NON-NLS-1$
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
			requestMonitor.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain GDBBackend service", null)); //$NON-NLS-1$
			requestMonitor.done();
			return;
		}

		fCommandControl = fTracker.getService(IGDBControl.class);
		if (fCommandControl == null) {
			requestMonitor.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain control service", null)); //$NON-NLS-1$
			requestMonitor.done();
			return;
		}

		fCommandFactory = fCommandControl.getCommandFactory();

		fProcService = fTracker.getService(IGDBProcesses.class);
		if (fProcService == null) {
			requestMonitor.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain process service", null)); //$NON-NLS-1$
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
		if (fTracker != null) {
			fTracker.dispose();
			fTracker = null;
		}
		requestMonitor.done();
	}

	/**
	 * Print the version of GDB.
	 * @since 4.6
	 */
	@Execute
	public void stepGDBVersion(final RequestMonitor requestMonitor) {
		fCommandControl.queueCommand(fCommandFactory.createMIGDBVersion(fCommandControl.getContext()),
				new DataRequestMonitor<MIGDBVersionInfo>(getExecutor(), requestMonitor) {
					@Override
					protected void handleCompleted() {
						// Accept failures
						requestMonitor.done();
					}
				});
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
			requestMonitor
					.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get working directory", e)); //$NON-NLS-1$
			requestMonitor.done();
			return;
		}

		if (dir != null) {
			fCommandControl.queueCommand(
					fCommandFactory.createMIEnvironmentCD(fCommandControl.getContext(), dir.toString()),
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
				IGdbDebugPreferenceConstants.PREF_ENABLE_PRETTY_PRINTING, false, null)) {

			fCommandControl.enablePrettyPrintingForMIVariableObjects(new RequestMonitor(getExecutor(), requestMonitor) {
				@Override
				protected void handleCompleted() {
					fCommandControl.setPrintPythonErrors(false, new ImmediateRequestMonitor() {
						@Override
						protected void handleCompleted() {
							// Ignore this error
							// Bug 402988
							requestMonitor.done();
						}
					});
				}
			});
		} else {
			fCommandControl.setPrintPythonErrors(false, new ImmediateRequestMonitor() {
				@Override
				protected void handleCompleted() {
					// Ignore this error
					// Bug 402988
					requestMonitor.done();
				}
			});
		}
	}

	/**
	 * Turn on RTTI usage, if enabled in preferences.
	 * @since 4.1
	 */
	@Execute
	public void stepSetPrintObject(final RequestMonitor requestMonitor) {
		// Enable or disable variables type determination based on RTTI.
		// See bug 377536 for details.
		boolean useRtti = Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_USE_RTTI, false, null);
		fCommandControl.queueCommand(
				fCommandControl.getCommandFactory().createMIGDBSetPrintObject(fCommandControl.getContext(), useRtti),
				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
					@Override
					protected void handleCompleted() {
						// Not an essential command, so accept errors
						requestMonitor.done();
					}
				});
	}

	/**
	 * Set the charsets.
	 * @since 4.1
	 */
	@Execute
	public void stepSetCharset(final RequestMonitor requestMonitor) {
		// Enable printing of sevenbit-strings. This is required to avoid charset issues.
		// See bug 307311 for details.
		fCommandControl.queueCommand(
				fCommandFactory.createMIGDBSetPrintSevenbitStrings(fCommandControl.getContext(), true),
				new ImmediateDataRequestMonitor<MIInfo>(requestMonitor) {
					@Override
					protected void handleCompleted() {
						// Set the charset to ISO-8859-1. We have to do this here because GDB earlier than
						// 7.0 has no proper Unicode support. Note that we can still handle UTF-8 though, as
						// we can determine and decode UTF-8 encoded strings on our own. This makes ISO-8859-1
						// the most suitable option here. See the MIStringHandler class and bug 307311 for
						// details.
						fCommandControl.queueCommand(
								fCommandFactory.createMIGDBSetCharset(fCommandControl.getContext(), "ISO-8859-1"), //$NON-NLS-1$
								new ImmediateDataRequestMonitor<MIInfo>(requestMonitor) {
									@Override
									protected void handleCompleted() {
										// Not an essential command, so accept errors
										requestMonitor.done();
									}
								});
					}
				});
	}

	/**
	 * Source the gdbinit file specified in the launch.
	 * @since 4.0
	 */
	@Execute
	public void stepSourceGDBInitFile(final RequestMonitor requestMonitor) {
		try {
			String gdbinitFile = fGDBBackend.getGDBInitFile();

			if (gdbinitFile != null && !gdbinitFile.isEmpty()) {
				String projectName = (String) fAttributes.get(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME);
				final String expandedGDBInitFile = new DebugStringVariableSubstitutor(projectName)
						.performStringSubstitution(gdbinitFile);

				fCommandControl.queueCommand(
						fCommandFactory.createCLISource(fCommandControl.getContext(), expandedGDBInitFile),
						new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
							@Override
							protected void handleCompleted() {
								// If the gdbinitFile is the default, then it may not exist and we
								// should not consider this an error.
								// If it is not the default, then the user must have specified it and
								// we want to warn the user if we can't find it.
								if (!expandedGDBInitFile
										.equals(IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT)) {
									requestMonitor.setStatus(getStatus());
								}
								requestMonitor.done();
							}
						});
			} else {
				requestMonitor.done();
			}
		} catch (CoreException e) {
			requestMonitor
					.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get gdbinit option", e)); //$NON-NLS-1$
			requestMonitor.done();
		}
	}

	/**
	 * Enable non-stop mode if requested.
	 * @since 4.0
	 */
	// Keep this method in this class for backwards-compatibility, although
	// it is called only by sub-classes.
	// It could be moved to FinalLaunchSequence_7_0, otherwise.
	@Execute
	public void stepSetNonStop(final RequestMonitor requestMonitor) {
		boolean isNonStop = CDebugUtils.getAttribute(fAttributes,
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, LaunchUtils.getIsNonStopModeDefault());

		// GDBs that don't support non-stop don't allow you to set it to false.
		// We really should set it to false when GDB supports it though.
		// Something to fix later.
		if (isNonStop) {
			fCommandControl.queueCommand(fCommandFactory.createMIGDBSetTargetAsync(fCommandControl.getContext(), true),
					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
						@Override
						protected void handleSuccess() {
							fCommandControl.queueCommand(
									fCommandFactory.createMIGDBSetPagination(fCommandControl.getContext(), false),
									new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
										@Override
										protected void handleSuccess() {
											fCommandControl.queueCommand(
													fCommandFactory.createMIGDBSetNonStop(fCommandControl.getContext(),
															true),
													new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
										}
									});
						}
					});
		} else {
			// Explicitly set target-async to off for all-stop mode.
			fCommandControl.queueCommand(fCommandFactory.createMIGDBSetTargetAsync(fCommandControl.getContext(), false),
					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
						@Override
						protected void handleError() {
							// We should only be calling this for GDB >= 7.0,
							// but just in case, accept errors for older GDBs
							requestMonitor.done();
						}
					});
		}
	}

	/**
	 * Tell GDB to automatically load or not the shared library symbols
	 * @since 4.0
	 */
	@Execute
	public void stepSetAutoLoadSharedLibrarySymbols(RequestMonitor requestMonitor) {
		boolean autolib = CDebugUtils.getAttribute(fAttributes,
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB,
				IGDBLaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT);

		fCommandControl.queueCommand(fCommandFactory.createMIGDBSetAutoSolib(fCommandControl.getContext(), autolib),
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

			if (!p.isEmpty()) {
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
							}
						});
			} else {
				requestMonitor.done();
			}
		} catch (CoreException e) {
			requestMonitor
					.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot set share library paths", e)); //$NON-NLS-1$
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
		ILaunch launch = (ILaunch) fSession.getModelAdapter(ILaunch.class);
		CSourceLookupDirector locator = (CSourceLookupDirector) launch.getSourceLocator();
		ISourceLookupDMContext sourceLookupDmc = (ISourceLookupDMContext) fCommandControl.getContext();

		sourceLookup.setSourceLookupPath(sourceLookupDmc, locator.getSourceContainers(), requestMonitor);
	}

	/**
	 * Setup the source substitute paths.
	 *
	 * This step tells GDB to to handle all the path re-writing using
	 * "set substitute-path"
	 *
	 * @since 5.0
	 */
	@Execute
	public void stepSetSourceSubstitutePath(RequestMonitor rm) {
		IGDBSourceLookup sourceSubPath = fTracker.getService(IGDBSourceLookup.class);
		ICommandControlDMContext context = fCommandControl.getContext();
		if (sourceSubPath == null || !(context instanceof ISourceLookupDMContext)) {
			rm.done();
		} else {
			ISourceLookupDMContext sourceLookupCtx = (ISourceLookupDMContext) context;
			sourceSubPath.initializeSourceSubstitutions(sourceLookupCtx, rm);
		}
	}

	/**
	 * Before starting a remote connection, set the gdb remotetimeout to the user
	 * specified value.
	 *
	 * @since 5.5
	 */
	@Execute
	public void stepSetRemoteTimeout(RequestMonitor rm) {
		if (fGDBBackend.getSessionType() == SessionType.REMOTE) {
			boolean remoteTimeoutEnabled = CDebugUtils.getAttribute(fAttributes,
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_ENABLED,
					LaunchUtils.getRemoteTimeoutEnabledDefault());
			String remoteTimeoutValue = CDebugUtils.getAttribute(fAttributes,
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_TIMEOUT_VALUE,
					LaunchUtils.getRemoteTimeoutValueDefault());
			if (remoteTimeoutEnabled && remoteTimeoutValue != null && !remoteTimeoutValue.isEmpty()) {
				fCommandControl.queueCommand(
						fCommandFactory.createMIGDBSetRemoteTimeout(fCommandControl.getContext(), remoteTimeoutValue),
						new ImmediateDataRequestMonitor<MIInfo>(rm) {
							@Override
							protected void handleError() {
								IStatus status = getStatus();
								MultiStatus ms = new MultiStatus(GdbPlugin.PLUGIN_ID, -1, new IStatus[] { status },
										LaunchMessages.getString("FinalLaunchSequence.2"), null); //$NON-NLS-1$
								rm.done(ms);
							}
						});
				return;
			}
		}
		rm.done();
	}

	@Deprecated(forRemoval = true)
	private static final String INVALID = "invalid"; //$NON-NLS-1$

	/**
	 * If we are dealing with a remote-attach debugging session, connect to the target.
	 * @since 4.0
	 *
	 * When removing, revive/uncomment code in implementations of IGDBProcesses.attachDebuggerToProcess()
	 */
	@Execute
	@Deprecated(forRemoval = true)
	public void stepRemoteConnection(final RequestMonitor rm) {
		if (fGDBBackend.getSessionType() == SessionType.REMOTE && fGDBBackend.getIsAttachSession()) {
			boolean isTcpConnection = CDebugUtils.getAttribute(fAttributes,
					IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, false);

			if (isTcpConnection) {
				String remoteTcpHost = CDebugUtils.getAttribute(fAttributes, IGDBLaunchConfigurationConstants.ATTR_HOST,
						INVALID);
				String remoteTcpPort = CDebugUtils.getAttribute(fAttributes, IGDBLaunchConfigurationConstants.ATTR_PORT,
						INVALID);

				fCommandControl.queueCommand(fCommandFactory.createMITargetSelect(fCommandControl.getContext(),
						remoteTcpHost, remoteTcpPort, true), new ImmediateDataRequestMonitor<MIInfo>(rm));
			} else {
				String serialDevice = CDebugUtils.getAttribute(fAttributes, IGDBLaunchConfigurationConstants.ATTR_DEV,
						INVALID);

				fCommandControl.queueCommand(
						fCommandFactory.createMITargetSelect(fCommandControl.getContext(), serialDevice, true),
						new ImmediateDataRequestMonitor<MIInfo>(rm));
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
			// Even if binary is null, we must call this to do all the other steps
			// necessary to create a process.  It is possible that the binary is not needed
			fProcService.debugNewProcess(fCommandControl.getContext(), getBinary(), fAttributes,
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
	 * @since 7.1
	 */
	protected String getBinary() {
		boolean noBinarySpecified = CDebugUtils.getAttribute(fAttributes,
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP,
				IGDBLaunchConfigurationConstants.DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP_DEFAULT);

		String binary = null;
		final IPath execPath = fGDBBackend.getProgramPath();
		if (!noBinarySpecified && execPath != null && !execPath.isEmpty()) {
			binary = execPath.toString();
		}
		return binary;
	}

	/**
	 * If we are dealing with an attach session, perform the attach.
	 * @since 4.0
	 */
	@Execute
	public void stepAttachToProcess(final RequestMonitor requestMonitor) {
		if (fGDBBackend.getIsAttachSession()) {
			// Is the process id already stored in the launch?
			int pid = CDebugUtils.getAttribute(fAttributes, ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID,
					-1);

			if (pid != -1) {
				fProcService.attachDebuggerToProcess(
						fProcService.createProcessContext(fCommandControl.getContext(), Integer.toString(pid)),
						new DataRequestMonitor<IDMContext>(getExecutor(), requestMonitor));
			} else if (fGDBBackend.getSessionType() == SessionType.REMOTE) {
				// Inline following and remove requestMonitor.done() once FinalLaunchSequence.stepAttachRemoteToDebugger() is removed
				// stepAttachRemoteToDebugger(requestMonitor);
				requestMonitor.done();
			} else {
				IConnectHandler connectCommand = (IConnectHandler) fSession.getModelAdapter(IConnectHandler.class);
				if (connectCommand instanceof IConnect) {
					((IConnect) connectCommand).connect(requestMonitor);
				} else {
					requestMonitor.done();
				}
			}
		} else {
			requestMonitor.done();
		}
	}

	/**
	 * If we are dealing with an remote attach session, perform the attach to debugger.
	 * Bug 528145
	 * @since 6.6
	 *
	 * When removing, revive/uncomment code in implementations in FinalLaunchSequence.stepAttachToProcess(RequestMonitor)
	 */
	@Deprecated(forRemoval = true)
	@Execute
	public void stepAttachRemoteToDebugger(final RequestMonitor requestMonitor) {
		if (fGDBBackend.getIsAttachSession() && fGDBBackend.getSessionType() == SessionType.REMOTE) {
			fProcService.attachDebuggerToProcess(
					fProcService.createProcessContext(fCommandControl.getContext(), MIProcesses.UNKNOWN_PROCESS_ID),
					getBinary(), new DataRequestMonitor<IDMContext>(getExecutor(), requestMonitor));
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
