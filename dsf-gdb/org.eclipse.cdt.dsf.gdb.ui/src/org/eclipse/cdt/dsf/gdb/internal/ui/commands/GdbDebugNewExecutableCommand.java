/*******************************************************************************
 * Copyright (c) 2012, 2015 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.model.IDebugNewExecutableHandler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.NewExecutableDialog;
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.NewExecutableInfo;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.progress.UIJob;

public class GdbDebugNewExecutableCommand extends RefreshableDebugCommand implements IDebugNewExecutableHandler {

	private class PromptJob extends UIJob {

		private DataRequestMonitor<NewExecutableInfo> fRequestMonitor;
		final private SessionType fSessionType;

		private PromptJob(SessionType sessionType, DataRequestMonitor<NewExecutableInfo> rm) {
			super(Messages.GdbDebugNewExecutableCommand_New_Executable_Prompt_Job);
			fSessionType = sessionType;
			fRequestMonitor = rm;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			final NewExecutableInfo info = new NewExecutableInfo(fSessionType);
			NewExecutableDialog dialog = new NewExecutableDialog(GdbUIPlugin.getShell(), info);
			final boolean canceled = dialog.open() == Window.CANCEL;
			fExecutor.execute(new DsfRunnable() {

				@Override
				public void run() {
					if (canceled)
						fRequestMonitor.cancel();
					else
						fRequestMonitor.setData(info);
					fRequestMonitor.done();
				}
			});
			return Status.OK_STATUS;
		}
	}

	private final ILaunch fLaunch;
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public GdbDebugNewExecutableCommand(DsfSession session, ILaunch launch) {
		super();
		fLaunch = launch;
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
	}

	public boolean canDebugNewExecutable() {

		Query<Boolean> canDebugQuery = new Query<Boolean>() {
			@Override
			public void execute(DataRequestMonitor<Boolean> rm) {
				IProcesses procService = fTracker.getService(IProcesses.class);
				ICommandControlService commandControl = fTracker.getService(ICommandControlService.class);

				if (procService == null || commandControl == null) {
					rm.setData(false);
					rm.done();
					return;
				}
				procService.isDebugNewProcessSupported(commandControl.getContext(), rm);
			}
		};
		try {
			fExecutor.execute(canDebugQuery);
			return canDebugQuery.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		}
		return false;
	}

	public void debugNewExecutable(final RequestMonitor rm) {
		IGDBBackend backend = fTracker.getService(IGDBBackend.class);
		final IProcesses procService = fTracker.getService(IProcesses.class);
		final ICommandControlService commandControl = fTracker.getService(ICommandControlService.class);
		if (backend == null || procService == null || commandControl == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, "Service is not available")); //$NON-NLS-1$
			rm.done();
			return;
		}

		PromptJob job = new PromptJob(backend.getSessionType(),
				new DataRequestMonitor<NewExecutableInfo>(fExecutor, rm) {

					@Override
					protected void handleCancel() {
						rm.cancel();
						rm.done();
					}

					@Override
					protected void handleSuccess() {
						try {
							Map<String, Object> attributes = getLaunchConfiguration().getAttributes();
							attributes.putAll(getData().getAttributes());
							procService.debugNewProcess(commandControl.getContext(), getData().getHostPath(),
									attributes, new ImmediateDataRequestMonitor<IDMContext>(rm));
						} catch (CoreException e) {
							rm.setStatus(e.getStatus());
							rm.done();
						}
					}
				});
		job.schedule();
	}

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {
		Query<Boolean> query = new Query<Boolean>() {

			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				debugNewExecutable(rm);
			}
		};
		try {
			fExecutor.execute(query);
			query.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
			// There was an error.  Propagate it to the user
			String errorMessage;
			if (e.getCause() != null) {
				errorMessage = e.getCause().getMessage();
			} else {
				errorMessage = e.getMessage();
			}
			request.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, errorMessage));
		} catch (CancellationException e) {
			// Nothing to do, just ignore the command since the user
			// cancelled it.
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		} finally {
			updateEnablement();
		}
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
			throws CoreException {
		return canDebugNewExecutable();
	}

	@Override
	protected Object getTarget(Object element) {
		if (element instanceof GdbLaunch || element instanceof IDMVMContext)
			return element;
		return null;
	}

	public void dispose() {
		fTracker.dispose();
	}

	private ILaunchConfiguration getLaunchConfiguration() {
		return fLaunch.getLaunchConfiguration();
	}
}
