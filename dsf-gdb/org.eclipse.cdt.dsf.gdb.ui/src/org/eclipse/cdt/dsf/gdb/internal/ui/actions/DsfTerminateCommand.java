/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Nokia - create and use backend service.
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IMultiTerminate;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IProcess;

public class DsfTerminateCommand implements ITerminateHandler {
	private final DsfSession fSession;
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public DsfTerminateCommand(DsfSession session) {
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

		final GdbLaunch launch = getLaunch(request);
		if (launch != null) {
			fExecutor.execute(new DsfRunnable() {
				@Override
				public void run() {
					request.setEnabled(false);
					IGDBControl gdbControl = fTracker.getService(IGDBControl.class);
					if (gdbControl != null && gdbControl.isActive()) {
						request.setEnabled(true);
					} else {
						// The GDB session may be terminated at this moment but if there
						// are processes in this launch that are not controlled by GDB
						// we need to check them as well.
						for (IProcess p : launch.getProcesses()) {
							if (p.canTerminate()) {
								request.setEnabled(true);
								break;
							}
						}
					}
					request.done();
				}
			});
		} else {
			fExecutor.execute(new DsfRunnable() {
				@Override
				public void run() {
					IProcessDMContext[] procDmcs = getProcessDMContexts(request.getElements());
					canTerminate(procDmcs, new DataRequestMonitor<Boolean>(fExecutor, null) {
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
			});
		}
	}

	@Override
	public boolean execute(final IDebugCommandRequest request) {
		if (request.getElements().length == 0) {
			request.done();
			return false;
		}

		final GdbLaunch launch = getLaunch(request);
		if (launch != null) {
			fExecutor.execute(new DsfRunnable() {
				@Override
				public void run() {
					IGDBControl gdbControl = fTracker.getService(IGDBControl.class);
					if (gdbControl != null && gdbControl.isActive()) {
						gdbControl.terminate(new RequestMonitor(fExecutor, null) {
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
					} else {
						terminateRemainingProcesses(launch, request);
					}
				}
			});
		} else {
			fExecutor.execute(new DsfRunnable() {
				@Override
				public void run() {
					IProcessDMContext[] procDmcs = getProcessDMContexts(request.getElements());
					terminate(procDmcs, new RequestMonitor(fExecutor, null) {
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
			});
		}
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
		class ScheduledFutureWrapper {
			ScheduledFuture<?> fFuture;
		}

		final ScheduledFutureWrapper fFutureWrapper = new ScheduledFutureWrapper();

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
					// Cancel the cleanup job since we won't need it
					fFutureWrapper.fFuture.cancel(false);
					GdbLaunch launch = getLaunch(request);
					if (launch != null) {
						terminateRemainingProcesses(launch, request);
					} else {
						request.done();
					}
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
		fFutureWrapper.fFuture = fExecutor.schedule(() -> {
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

	private IProcessDMContext[] getProcessDMContexts(Object[] elements) {
		final Set<IProcessDMContext> procDmcs = new HashSet<>();
		for (Object obj : elements) {
			if (obj instanceof IDMVMContext) {
				IProcessDMContext procDmc = DMContexts.getAncestorOfType(((IDMVMContext) obj).getDMContext(),
						IProcessDMContext.class);
				if (procDmc != null) {
					procDmcs.add(procDmc);
				}
			}
		}
		return procDmcs.toArray(new IProcessDMContext[procDmcs.size()]);
	}

	private void canTerminate(IProcessDMContext[] procDmcs, DataRequestMonitor<Boolean> rm) {
		if (procDmcs.length == 0) {
			IGDBControl gdbControl = fTracker.getService(IGDBControl.class);
			if (gdbControl != null) {
				rm.setData(gdbControl.isActive());
			} else {
				rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, "Service is not available.")); //$NON-NLS-1$
			}
			rm.done();
			return;
		}

		IMultiTerminate multiTerminate = fTracker.getService(IMultiTerminate.class);
		if (multiTerminate != null) {
			multiTerminate.canTerminateSome(procDmcs, rm);
		} else {
			IProcesses procService = fTracker.getService(IProcesses.class);
			if (procService != null && procDmcs.length == 1) {
				procService.canTerminate(procDmcs[0], rm);
			} else {
				rm.setData(false);
				rm.done();
			}
		}
	}

	private void terminate(IProcessDMContext[] procDmcs, RequestMonitor rm) {
		if (procDmcs.length == 0) {
			IGDBControl gdbControl = fTracker.getService(IGDBControl.class);
			if (gdbControl != null) {
				gdbControl.terminate(rm);
			} else {
				rm.done();
			}
			return;
		}

		IMultiTerminate multiTerminate = fTracker.getService(IMultiTerminate.class);
		if (multiTerminate != null) {
			multiTerminate.terminate(procDmcs, rm);
		} else {
			IProcesses procService = fTracker.getService(IProcesses.class);
			if (procService != null && procDmcs.length == 1) {
				procService.terminate(procDmcs[0], rm);
			} else {
				rm.done();
			}
		}
	}

	private GdbLaunch getLaunch(IDebugCommandRequest request) {
		for (Object el : request.getElements()) {
			if (el instanceof GdbLaunch) {
				return (GdbLaunch) el;
			}
		}
		return null;
	}

	private void terminateRemainingProcesses(final GdbLaunch launch, final IDebugCommandRequest request) {
		// Run this in a separate job since this method is called from
		// the executor thread. The job is scheduled with a delay to make
		// sure that MIInferiorProcess is terminated. See MIInferiorProcess.waitForSync()
		new Job("Terminate Job") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				MultiStatus status = new MultiStatus(GdbUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED,
						Messages.DsfTerminateCommand_Terminate_failed, null);
				for (IProcess p : launch.getProcesses()) {
					if (p.canTerminate()) {
						try {
							p.terminate();
						} catch (DebugException e) {
							status.merge(e.getStatus());
						}
					}
				}
				if (!status.isOK()) {
					request.setStatus(status);
				}
				request.done();
				return Status.OK_STATUS;
			}
		}.schedule(100);
	}
}
