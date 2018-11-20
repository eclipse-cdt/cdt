/*******************************************************************************
 * Copyright (c) 2008, 2016 Ericsson and others.
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
 *     Marc Khouzam (Ericsson) - Add support for multi-attach (Bug 293679)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.model.IConnectHandler;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateInDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.actions.IConnect;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.actions.ProcessInfo;
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.LaunchUIMessages;
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.ProcessPrompter;
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.ProcessPrompter.PrompterInfo;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.IProcessExtendedInfo;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData2;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

public class GdbConnectCommand extends RefreshableDebugCommand implements IConnectHandler, IConnect {

	private final ILaunch fLaunch;
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	// A map of processName to path, that allows us to remember the path to the binary file
	// for a process with a particular name.  We can then re-use the same binary for another
	// process with the same name.  This allows a user to connect to multiple processes
	// with the same name without having to be prompted each time for a path.
	// This map is associated to the current debug session only, therefore the user can
	// reset it by using a new debug session.
	// This map is only needed for remote sessions, since we don't need to specify
	// the binary location for a local attach session.
	private Map<String, String> fProcessNameToBinaryMap = new HashMap<>();

	public GdbConnectCommand(DsfSession session, ILaunch launch) {
		fLaunch = launch;
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
	}

	public void dispose() {
		fTracker.dispose();
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
			throws CoreException {
		return canConnect();
	}

	/*
	 * This method should not be called from the UI thread.
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.gdb.actions.IConnect#canConnect()
	 */
	@Override
	public boolean canConnect() {
		Query<Boolean> canConnectQuery = new Query<Boolean>() {
			@Override
			public void execute(DataRequestMonitor<Boolean> rm) {
				IProcesses procService = fTracker.getService(IProcesses.class);
				ICommandControlService commandControl = fTracker.getService(ICommandControlService.class);

				if (procService != null && commandControl != null) {
					procService.isDebuggerAttachSupported(commandControl.getContext(), rm);
				} else {
					rm.setData(false);
					rm.done();
				}
			}
		};
		try {
			fExecutor.execute(canConnectQuery);
			return canConnectQuery.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		}

		return false;
	}

	/**
	 * This job will prompt the user to select a set of processes
	 * to attach too.
	 * We need a job because the ProcessPrompter will block and
	 * we don't want to block the executor.
	 */
	protected class PromptForPidJob extends UIJob {

		// The list of processes used in the case of an ATTACH session
		IProcessExtendedInfo[] fProcessList = null;
		DataRequestMonitor<Object> fRequestMonitor;
		private List<String> fDebuggedProcesses;

		public PromptForPidJob(String name, IProcessExtendedInfo[] procs, List<String> debuggedProcesses,
				DataRequestMonitor<Object> rm) {
			super(name);
			fProcessList = procs;
			fRequestMonitor = rm;
			fDebuggedProcesses = debuggedProcesses;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			final Status NO_PID_STATUS = new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, -1,
					LaunchMessages.getString("LocalAttachLaunchDelegate.No_Process_ID_selected"), //$NON-NLS-1$
					null);

			try {
				PrompterInfo info = new PrompterInfo(fProcessList, fDebuggedProcesses);
				Object result = new ProcessPrompter().handleStatus(null, info);
				if (result == null) {
					fRequestMonitor.cancel();
				} else if (result instanceof IProcessExtendedInfo[]) {
					fRequestMonitor.setData(result);
				} else if (result instanceof Integer) {
					// This is the case where the user typed in a pid number directly
					fRequestMonitor.setData(new IProcessExtendedInfo[] { new ProcessInfo((Integer) result, "") }); //$NON-NLS-1$
				} else {
					fRequestMonitor.setStatus(NO_PID_STATUS);
				}
			} catch (CoreException e) {
				fRequestMonitor.setStatus(NO_PID_STATUS);
			}
			fRequestMonitor.done();

			return Status.OK_STATUS;
		}
	}

	/**
	 * This job will prompt the user for a path to the binary to use,
	 * and then will attach to the process.
	 * We need a job to free the executor while we prompt the user for
	 * a binary path. Bug 344892
	 */
	private class PromptAndAttachToProcessJob extends UIJob {
		private final String fPid;
		private final RequestMonitor fRm;
		private final String fTitle;
		private final String fProcName;

		public PromptAndAttachToProcessJob(String pid, String title, String procName, RequestMonitor rm) {
			super(""); //$NON-NLS-1$
			fPid = pid;
			fTitle = title;
			fProcName = procName;
			fRm = rm;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {

			// Have we already see the binary for a process with this name?
			String binaryPath = fProcessNameToBinaryMap.get(fProcName);

			if (binaryPath == null) {
				// prompt for the binary path
				Shell shell = GdbUIPlugin.getShell();

				if (shell != null) {
					FileDialog fd = new FileDialog(shell, SWT.NONE);
					fd.setText(fTitle);
					binaryPath = fd.open();
				}
			}

			if (binaryPath == null) {
				// The user pressed the cancel button, so we cancel the attach gracefully
				fRm.done();
			} else {

				final String finalBinaryPath = binaryPath;
				fExecutor.execute(new DsfRunnable() {
					@Override
					public void run() {
						IGDBProcesses procService = fTracker.getService(IGDBProcesses.class);
						ICommandControlService commandControl = fTracker.getService(ICommandControlService.class);

						if (procService != null && commandControl != null) {
							IProcessDMContext procDmc = procService.createProcessContext(commandControl.getContext(),
									fPid);
							procService.attachDebuggerToProcess(procDmc, finalBinaryPath,
									new DataRequestMonitor<IDMContext>(fExecutor, fRm) {
										@Override
										protected void handleSuccess() {
											// Store the path of the binary so we can use it again for another process
											// with the same name.  Only do this on success, to avoid being stuck with
											// a path that is invalid.
											if (fProcName != null && !fProcName.isEmpty()) {
												fProcessNameToBinaryMap.put(fProcName, finalBinaryPath);
											}
											fRm.done();
										}
									});

						} else {
							fRm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID,
									IDsfStatusConstants.INTERNAL_ERROR, "Cannot find services", null)); //$NON-NLS-1$
							fRm.done();
						}
					}
				});
			}

			return Status.OK_STATUS;
		}
	}

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, final IRequest request) throws CoreException {
		Query<Boolean> connectQuery = new Query<Boolean>() {
			@Override
			public void execute(final DataRequestMonitor<Boolean> rm) {
				connect(new RequestMonitor(fExecutor, rm) {
					@Override
					protected void handleCompleted() {
						// pass any error to the caller
						if (!isSuccess()) {
							request.setStatus(getStatus());
						}
						rm.done();
					}
				});
			}
		};
		try {
			fExecutor.execute(connectQuery);
			connectQuery.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (CancellationException e) {
			// Nothing to do, just ignore the command since the user
			// cancelled it.
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		} finally {
			updateEnablement();
		}
	}

	/**
	 * Get already debugged processes from all compatible sessions.
	 * "compatible" in current implementation means all sessions on local machine.
	 *
	 * @param currentCtx current session context
	 * @param allSessions true if all session to be queried, false to return result only for current execution session context
	 * @param drm where result to be returned
	 */
	private void getAllDebuggedProcesses(final IDMContext currentCtx, boolean allSessions,
			final DataRequestMonitor<List<String>> drm) {
		SessionType sessionType = fTracker.getService(IGDBBackend.class).getSessionType();

		final List<String> result = new LinkedList<>();
		final List<DsfSession> sessions = new LinkedList<>();
		// Only for local session types search in all debug sessions
		if (allSessions && sessionType == SessionType.LOCAL) {
			sessions.addAll(Arrays.asList(DsfSession.getActiveSessions()));
		} else {
			// For remote session just query current context.
			//
			// cannot reliably match two remote debug session that are connected to same target machine.
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=486408#c7

			sessions.add(DsfSession.getSession(currentCtx.getSessionId()));
		}

		// Query each sessions for existing processes in a sequential fashion.
		// We must do this as each session will require different executor.
		final class ProcessRequestMonitor extends DataRequestMonitor<IDMContext[]> {
			public ProcessRequestMonitor(Executor executor) {
				super(executor, null);
			}

			public ProcessRequestMonitor(DsfExecutor executor) {
				super(new ImmediateInDsfExecutor(executor), null);
			}

			@Override
			protected void handleCompleted() {
				// if succeeded and has data, add process ids to result,
				// otherwise proceed to next debug session (aka DsfSession)
				if (isSuccess() && getData() != null) {
					for (IDMContext dmc : getData()) {
						IMIProcessDMContext procDmc = DMContexts.getAncestorOfType(dmc, IMIProcessDMContext.class);
						if (procDmc != null) {
							result.add(procDmc.getProcId());
						}
					}
				}
				if (!sessions.isEmpty()) {
					final DsfSession nextSession = sessions.remove(0);
					final boolean sameSession = currentCtx.getSessionId().equals(nextSession.getId());
					nextSession.getExecutor().execute(new DsfRunnable() {
						@Override
						public void run() {
							DsfServicesTracker nextTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(),
									nextSession.getId());
							IGDBBackend nextSessionBackend = nextTracker.getService(IGDBBackend.class);
							if (sameSession || nextSessionBackend.getSessionType() == SessionType.LOCAL) {
								ICommandControlService nextCommandControl = nextTracker
										.getService(ICommandControlService.class);
								IProcesses nextProcService = nextTracker.getService(IProcesses.class);
								nextProcService.getProcessesBeingDebugged(nextCommandControl.getContext(),
										new ProcessRequestMonitor(nextSession.getExecutor()));
							} else {
								// proceed to next session context query passing an error (that will be ignored)
								new ProcessRequestMonitor(nextSession.getExecutor())
										.done(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID,
												IDsfStatusConstants.NOT_SUPPORTED, "Only local session", null)); //$NON-NLS-1$
							}
							nextTracker.dispose();
						}
					});
				} else {
					// done with querying all session. Copy the result
					drm.done(result);
				}
			}
		}
		// Trigger the first query
		new ProcessRequestMonitor(ImmediateExecutor.getInstance()).done();
	}

	/*
	 * This method should not be called from the UI thread.
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.gdb.actions.IConnect#canConnect()
	 */
	@Override
	public void connect(final RequestMonitor rm) {
		fExecutor.execute(new DsfRunnable() {
			@Override
			public void run() {
				final IProcesses procService = fTracker.getService(IProcesses.class);
				ICommandControlService commandControl = fTracker.getService(ICommandControlService.class);
				IGDBBackend backend = fTracker.getService(IGDBBackend.class);

				if (procService != null && commandControl != null && backend != null) {
					final ICommandControlDMContext controlCtx = commandControl.getContext();

					// Now get the list of all processes
					procService.getRunningProcesses(controlCtx,
							new DataRequestMonitor<IProcessDMContext[]>(fExecutor, rm) {
								@Override
								protected void handleSuccess() {

									final List<IProcessExtendedInfo> procInfoList = new ArrayList<>();

									final CountingRequestMonitor countingRm = new CountingRequestMonitor(fExecutor,
											rm) {
										@Override
										protected void handleSuccess() {
											getAllDebuggedProcesses(controlCtx, true,
													new ImmediateDataRequestMonitor<List<String>>(rm) {
														@Override
														protected void handleSuccess() {
															List<String> dbgPids = getData();

															// Prompt the user to choose one or more processes
															new PromptForPidJob(
																	LaunchUIMessages
																			.getString("ProcessPrompter.PromptJob"), //$NON-NLS-1$
																	procInfoList.toArray(
																			new IProcessExtendedInfo[procInfoList
																					.size()]),
																	dbgPids,
																	new DataRequestMonitor<Object>(fExecutor, rm) {
																		@Override
																		protected void handleCancel() {
																			rm.cancel();
																			rm.done();
																		}

																		@Override
																		protected void handleSuccess() {
																			Object data = getData();
																			if (data instanceof IProcessExtendedInfo[]) {
																				attachToProcesses(controlCtx,
																						(IProcessExtendedInfo[]) data,
																						rm);
																			} else {
																				rm.done(new Status(IStatus.ERROR,
																						GdbUIPlugin.PLUGIN_ID,
																						IDsfStatusConstants.INTERNAL_ERROR,
																						"Invalid return type for process prompter", //$NON-NLS-1$
																						null));
																			}
																		}
																	}).schedule();
														}
													});
										}
									};

									if (getData().length > 0 && getData()[0] instanceof IThreadDMData) {
										// The list of running processes also contains the name of the processes
										// This is much more efficient.  Let's use it.
										for (IProcessDMContext processCtx : getData()) {
											IThreadDMData processData = (IThreadDMData) processCtx;
											int pid = 0;
											try {
												pid = Integer.parseInt(processData.getId());
											} catch (NumberFormatException e) {
											}
											String[] cores = null;
											String owner = null;
											if (processData instanceof IGdbThreadDMData) {
												cores = ((IGdbThreadDMData) processData).getCores();
												owner = ((IGdbThreadDMData) processData).getOwner();
											}
											String description = null;
											if (processData instanceof IGdbThreadDMData2) {
												description = ((IGdbThreadDMData2) processData).getDescription();
											}
											procInfoList.add(new ProcessInfo(pid, processData.getName(), cores, owner,
													description));
										}

										// Re-use the counting monitor and trigger it right away.
										// No need to call done() in this case.
										countingRm.setDoneCount(0);
									} else {
										// The list of running processes does not contain the names, so
										// we must obtain it individually

										// For each process, obtain its name
										// Once all the names are obtained, prompt the user for the pid to use

										// New cycle, look for service again
										final IProcesses procService = fTracker.getService(IProcesses.class);

										if (procService != null) {
											countingRm.setDoneCount(getData().length);

											for (IProcessDMContext processCtx : getData()) {
												procService.getExecutionData(processCtx,
														new DataRequestMonitor<IThreadDMData>(fExecutor, countingRm) {
															@Override
															protected void handleSuccess() {
																IThreadDMData processData = getData();
																int pid = 0;
																try {
																	pid = Integer.parseInt(processData.getId());
																} catch (NumberFormatException e) {
																}
																String[] cores = null;
																String owner = null;
																if (processData instanceof IGdbThreadDMData) {
																	cores = ((IGdbThreadDMData) processData).getCores();
																	owner = ((IGdbThreadDMData) processData).getOwner();
																}
																String description = null;
																if (processData instanceof IGdbThreadDMData2) {
																	description = ((IGdbThreadDMData2) processData)
																			.getDescription();
																}
																procInfoList
																		.add(new ProcessInfo(pid, processData.getName(),
																				cores, owner, description));
																countingRm.done();
															}
														});
											}
										} else {
											// Trigger right away.  No need to call done() in this case.
											countingRm.setDoneCount(0);
										}
									}
								}
							});
				} else {
					rm.done();
				}
			}
		});
	}

	private void attachToProcesses(final ICommandControlDMContext controlDmc, IProcessExtendedInfo[] processes,
			final RequestMonitor rm) {

		// For a local attach, GDB can figure out the binary automatically,
		// so we don't need to prompt for it.
		final IGDBProcesses procService = fTracker.getService(IGDBProcesses.class);
		final IGDBBackend backend = fTracker.getService(IGDBBackend.class);
		final StringBuilder errors = new StringBuilder();

		if (procService != null && backend != null) {
			// Attach to each process in a sequential fashion.  We must do this
			// to be able to check if we are allowed to attach to the next process.
			// Attaching to all of them in parallel would assume that all attach are supported.

			// Create a list of all our processes so we can attach to one at a time.
			// We need to create a new list so that we can remove elements from it.
			final List<IProcessExtendedInfo> procList = new ArrayList<>(Arrays.asList(processes));
			// Create a one element array to remember what process we are trying to attach to, so that we can
			// use it in case of error.
			final IProcessExtendedInfo[] previousProcAttempt = new IProcessExtendedInfo[1];

			class AttachToProcessRequestMonitor extends ImmediateDataRequestMonitor<IDMContext> {
				public AttachToProcessRequestMonitor() {
					super();
				}

				@Override
				protected void handleCompleted() {
					// Failed to attach to a process.  Remember the error message.
					if (!isSuccess()) {
						formatErrorMessage(errors, previousProcAttempt[0], getStatus().getMessage());
					}

					// Check that we have a process to attach to
					if (!procList.isEmpty()) {

						// Check that we can actually attach to the process.
						// This is because some backends may not support multi-process.
						// If the backend does not support multi-process, we only attach to the first process.
						procService.isDebuggerAttachSupported(controlDmc, new ImmediateDataRequestMonitor<Boolean>() {
							@Override
							protected void handleCompleted() {
								if (isSuccess() && getData()) {
									// Can attach to process

									// Remove process from list and attach to it.
									IProcessExtendedInfo process = procList.remove(0);
									// Store process in case of error
									previousProcAttempt[0] = process;
									String pidStr = Integer.toString(process.getPid());

									if (backend.getSessionType() == SessionType.REMOTE) {
										// For remote attach, we must set the binary first so we need to prompt the user.

										// If this is the very first attach of a remote session, check if the user
										// specified the binary in the launch.  If so, let's add it to our map to
										// avoid having to prompt the user for that binary.
										// This would be particularly annoying since we didn't use to have
										// to do that before we supported multi-process.
										// Must do this here to be in the executor
										// Bug 350365
										if (fProcessNameToBinaryMap.isEmpty()) {
											IPath binaryPath = backend.getProgramPath();
											if (binaryPath != null && !binaryPath.isEmpty()) {
												fProcessNameToBinaryMap.put(binaryPath.lastSegment(),
														binaryPath.toOSString());
											}
										}

										// Because the prompt is a very long operation, we need to run outside the
										// executor, so we don't lock it.
										// Bug 344892
										IPath processPath = new Path(process.getName());
										String processShortName = processPath.lastSegment();
										new PromptAndAttachToProcessJob(pidStr,
												LaunchUIMessages.getString("ProcessPrompterDialog.TitlePrefix") //$NON-NLS-1$
														+ process.getName(),
												processShortName, new AttachToProcessRequestMonitor()).schedule();
									} else {
										// For a local attach, we can attach directly without looking for the binary
										// since GDB will figure it out by itself
										IProcessDMContext procDmc = procService.createProcessContext(controlDmc,
												pidStr);
										procService.attachDebuggerToProcess(procDmc, null,
												new AttachToProcessRequestMonitor());
									}
								} else {
									// Not allowed to attach to another process.  Just stop.
									rm.done();
								}
							}
						});
					} else {
						// If there were errors, pass them-on to the caller
						if (errors.length() != 0) {
							rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, errors.toString()));
						}
						// No other process to attach to
						rm.done();
					}
				}
			}

			// Trigger the first attach.
			new AttachToProcessRequestMonitor().done();

		} else {
			rm.done(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Cannot find service", null)); //$NON-NLS-1$
		}

	}

	@Override
	protected Object getTarget(Object element) {
		if (element instanceof GdbLaunch || element instanceof IDMVMContext) {
			return element;
		}
		return null;
	}

	@Override
	protected boolean isRemainEnabled(IDebugCommandRequest request) {
		return false;
	}

	private void formatErrorMessage(StringBuilder errors, IProcessExtendedInfo process, String errorMsg) {
		// Extract process name from full path.
		// On windows host, paths of style "sendmail:", "udisk-daemon:"
		// is treated as device id with no path segments
		String name;
		IPath path = new Path(process.getName());
		if (path.lastSegment() == null) {
			name = process.getName();
		} else {
			name = path.lastSegment();
		}

		if (errors.length() != 0) {
			errors.append(System.lineSeparator()).append(System.lineSeparator());
		}

		errors.append(Messages.GdbConnectCommand_FailureMessage).append(" ") //$NON-NLS-1$
				.append(name).append(" [").append(process.getPid()).append("]") //$NON-NLS-1$ //$NON-NLS-2$
				.append(System.lineSeparator()).append(Messages.GdbConnectCommand_Error).append(System.lineSeparator())
				.append(errorMsg);
	}
}
