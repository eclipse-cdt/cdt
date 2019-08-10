/*******************************************************************************
 * Copyright (c) 2008, 2014 Ericsson and others.
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
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IMultiDetach;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

public class GdbDisconnectCommand implements IDisconnectHandler {
	private final DsfSession fSession;
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public GdbDisconnectCommand(DsfSession session) {
		super();
		fSession = session;
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
	}

	public void dispose() {
		fTracker.dispose();
	}

	@Override
	public void canExecute(final IEnabledStateRequest request) {
		if (request.getElements().length == 0) {
			request.setEnabled(false);
			request.done();
			return;
		}

		getContainerDMContexts(request.getElements(), new DataRequestMonitor<IContainerDMContext[]>(fExecutor, null) {
			@Override
			protected void handleCompleted() {
				if (!isSuccess()) {
					request.setEnabled(false);
					request.done();
				} else {
					canDisconnect(getData(), new ImmediateDataRequestMonitor<Boolean>() {
						@Override
						protected void handleCompleted() {
							if (!isSuccess()) {
								request.setEnabled(false);
							} else {
								request.setEnabled(getData());

							}
							request.done();
						}
					});
				}
			}
		});
	}

	@Override
	public boolean execute(final IDebugCommandRequest request) {
		if (request.getElements().length == 0) {
			request.done();
			return false;
		}

		getContainerDMContexts(request.getElements(), new DataRequestMonitor<IContainerDMContext[]>(fExecutor, null) {
			@Override
			protected void handleCompleted() {
				if (!isSuccess()) {
					request.setStatus(getStatus());
					request.done();
				} else {
					disconnect(getData(), new ImmediateRequestMonitor() {
						@Override
						protected void handleCompleted() {
							if (!isSuccess()) {
								request.setStatus(getStatus());
								request.done();
							} else {
								waitForTermination(request);
							}
						}
					});
				}
			}
		});

		return false;
	}

	/**
	 * Wait for the debug session to be fully shutdown before reporting
	 * that the terminate was completed.  This is important for the
	 * 'Terminate and remove' operation.
	 * The wait time is limited with a timeout so as to eventually complete the
	 * request in the case of termination error, or when terminating
	 * a single process in a multi-process session.
	 * See bug 377447
	 */
	private void waitForTermination(final IDebugCommandRequest request) {
		// It is possible that the session already had time to terminate
		if (!DsfSession.isSessionActive(fSession.getId())) {
			request.done();
			return;
		}

		// Listener that will indicate when the shutdown is complete
		final SessionEndedListener endedListener = new SessionEndedListener() {
			@Override
			public void sessionEnded(DsfSession session) {
				if (fSession.equals(session)) {
					DsfSession.removeSessionEndedListener(this);
					request.done();
				}
			}
		};

		DsfSession.addSessionEndedListener(endedListener);

		// Create the timeout
		// For a multi-process session, if a single process is
		// terminated, this timeout will always hit (unless the
		// session is also terminated before the timeout).
		// We haven't found a problem with delaying the completion
		// of the request that way.
		// Note that this timeout is not removed even if we don't
		// need it anymore, once the session has terminated;
		// instead, we let it timeout and ignore it if the session
		// is already terminated.
		fExecutor.schedule(() -> {
			// Check that the session is still active when the timeout hits.
			// If it is not, then everything has been cleaned up already.
			if (DsfSession.isSessionActive(fSession.getId())) {
				DsfSession.removeSessionEndedListener(endedListener);

				// Marking the request as cancelled will prevent the removal of
				// the launch from the Debug view in case of "Terminate and Remove".
				// This is important for multi-process sessions when "Terminate and Remove"
				// is applied to one of the running processes. In this case the selected
				// process will be terminated but the associated launch will not be removed
				// from the Debug view.
				request.setStatus(Status.CANCEL_STATUS);
				request.done();
			}
		}, 1, TimeUnit.MINUTES);
	}

	private void getContainerDMContexts(Object[] elements, final DataRequestMonitor<IContainerDMContext[]> rm) {
		GdbLaunch launch = null;
		final Set<IContainerDMContext> contDmcs = new HashSet<>();
		for (Object obj : elements) {
			if (obj instanceof GdbLaunch) {
				launch = (GdbLaunch) obj;
				break;
			}
			if (obj instanceof IDMVMContext) {
				IContainerDMContext contDmc = DMContexts.getAncestorOfType(((IDMVMContext) obj).getDMContext(),
						IContainerDMContext.class);
				if (contDmc != null) {
					contDmcs.add(contDmc);
				}
			}
		}
		if (launch == null) {
			rm.setData(contDmcs.toArray(new IContainerDMContext[contDmcs.size()]));
			rm.done();
		} else {
			try {
				fExecutor.execute(new DsfRunnable() {
					@Override
					public void run() {
						ICommandControlService commandControl = fTracker.getService(ICommandControlService.class);
						final IProcesses procService = fTracker.getService(IProcesses.class);
						if (commandControl != null && procService != null) {
							procService.getProcessesBeingDebugged(commandControl.getContext(),
									new ImmediateDataRequestMonitor<IDMContext[]>() {
										@Override
										protected void handleCompleted() {
											if (!isSuccess()) {
												rm.setStatus(getStatus());
											} else {
												for (IDMContext ctx : getData()) {
													IContainerDMContext contDmc = DMContexts.getAncestorOfType(ctx,
															IContainerDMContext.class);
													if (contDmc != null) {
														contDmcs.add(contDmc);
													}
												}
												rm.setData(contDmcs.toArray(new IContainerDMContext[contDmcs.size()]));
											}
											rm.done();
										}
									});
						} else {
							rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, "Service is not available.")); //$NON-NLS-1$
							rm.done();
						}
					}
				});
			} catch (RejectedExecutionException e) {
				rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, e.getLocalizedMessage()));
				rm.done();
			}
		}
	}

	private void canDisconnect(IContainerDMContext[] contDmcs, DataRequestMonitor<Boolean> rm) {
		if (contDmcs.length == 0) {
			rm.setData(false);
			rm.done();
			return;
		}

		IMultiDetach multiDetach = fTracker.getService(IMultiDetach.class);
		if (multiDetach != null) {
			multiDetach.canDetachDebuggerFromSomeProcesses(contDmcs, rm);
		} else {
			IProcesses procService = fTracker.getService(IProcesses.class);
			if (procService != null && contDmcs.length == 1) {
				procService.canDetachDebuggerFromProcess(contDmcs[0], rm);
			} else {
				rm.setData(false);
				rm.done();
			}
		}
	}

	private void disconnect(IContainerDMContext[] contDmcs, RequestMonitor rm) {
		if (contDmcs.length == 0) {
			rm.done();
			return;
		}

		IMultiDetach multiDetach = fTracker.getService(IMultiDetach.class);
		if (multiDetach != null) {
			multiDetach.detachDebuggerFromProcesses(contDmcs, rm);
		} else {
			IProcesses procService = fTracker.getService(IProcesses.class);
			if (procService != null && contDmcs.length == 1) {
				procService.detachDebuggerFromProcess(contDmcs[0], rm);
			} else {
				rm.done();
			}
		}
	}
}
