/*******************************************************************************
 * Copyright (c) 2016 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Teodor Madan (Freescale Semiconductor) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.osview;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.IResourcesInformation;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class AttachProcessHandler extends AbstractHandler implements IHandler {

	private static final String PROCESSES_CLASS = "processes"; //$NON-NLS-1$
	private static final String THREADS_CLASS = "threads"; //$NON-NLS-1$

	@Override
	public void setEnabled(Object evaluationContext) {
		boolean state = false;
		if (evaluationContext instanceof IEvaluationContext) {
			Object p = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_PART_NAME);
			Object s = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
			if (p instanceof OSResourcesView && s instanceof IStructuredSelection) {
				IStructuredSelection sel2 = (IStructuredSelection) s;
				if (!sel2.isEmpty() && sel2.getFirstElement() instanceof IResourcesInformation) {
					OSResourcesView rview = (OSResourcesView) p;
					// check that processes class resources is selected.
					ICommandControlDMContext sessionContext = rview.getSessionContext();
					if (sessionContext != null && (PROCESSES_CLASS.equals(rview.getResourceClass())
							|| THREADS_CLASS.equals(rview.getResourceClass()))) {
						// check that attach is supported"
						state = isAttachSupported(sessionContext);
					}
				}
			}
		}
		setBaseEnabled(state);

	}

	private boolean isAttachSupported(final ICommandControlDMContext context) {
		DsfSession session = DsfSession.getSession(context.getSessionId());
		if (session == null) {
			// can be null while terminating
			return false;
		}

		Query<Boolean> canConnectQuery = new Query<Boolean>() {
			@Override
			public void execute(DataRequestMonitor<Boolean> rm) {
				BundleContext c = GdbUIPlugin.getDefault().getBundle().getBundleContext();
				DsfServicesTracker tracker = new DsfServicesTracker(c, context.getSessionId());

				IProcesses procService = tracker.getService(IProcesses.class);
				ICommandControlService commandControl = tracker.getService(ICommandControlService.class);

				if (procService != null && commandControl != null) {
					procService.isDebuggerAttachSupported(commandControl.getContext(), rm);
				} else {
					rm.done(false);
				}
			}
		};
		try {
			session.getExecutor().execute(canConnectQuery);
			return canConnectQuery.get();
		} catch (InterruptedException | java.util.concurrent.ExecutionException e) {
			// Can be thrown if the session is shutdown
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (selection instanceof IStructuredSelection && part instanceof OSResourcesView) {
			Set<String> pid_ids = new LinkedHashSet<String>();
			for (Object sel : ((IStructuredSelection) selection).toList()) {
				IResourcesInformation ri = (IResourcesInformation) sel;
				int pidColumn = -1;
				for (int i = 0; i < ri.getColumnNames().length; i++) {
					if ("pid".equalsIgnoreCase(ri.getColumnNames()[i])) { //$NON-NLS-1$
						pidColumn = i;
						break;
					}
				}
				if (pidColumn >= 0) {
					String pid = ri.getContent()[0][pidColumn];
					try {
						Integer.parseInt(pid); // test for integer value
						pid_ids.add(pid);
					} catch (NumberFormatException e) {
						GdbUIPlugin.logErrorMessage("Non-integer pid " + pid); //$NON-NLS-1$
					}

				}
			}

			final ICommandControlDMContext context = ((OSResourcesView) part).getSessionContext();
			final List<Sequence.Step> attach_steps = new ArrayList<Sequence.Step>(pid_ids.size());
			for (final String p : pid_ids) {
				attach_steps.add(new Sequence.Step() {

					@Override
					public void execute(RequestMonitor rm) {
						attachToProcess(context, p, rm);
					}

					@Override
					public String getTaskName() {
						return MessageFormat.format(Messages.OSView_AttachTask, p);
					}
				});
			}
			if (!attach_steps.isEmpty()) {
				Job.create(Messages.OSView_AttachJobName, new ICoreRunnable() {
					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						DsfSession session = DsfSession.getSession(context.getSessionId());
						Sequence sequence = new Sequence(session.getExecutor(), monitor, Messages.OSView_AttachJobName,
								"") { //$NON-NLS-1$
							@Override
							public Step[] getSteps() {
								return attach_steps.toArray(new Step[attach_steps.size()]);
							}
						};
						session.getExecutor().execute(sequence);
						try {
							sequence.get();
						} catch (InterruptedException | java.util.concurrent.ExecutionException e) {
							// ignore here.
						}
					}
				}).schedule();
			}
		}
		return null;
	}

	private void attachToProcess(final ICommandControlDMContext context, final String pid, RequestMonitor rm) {
		BundleContext c = GdbUIPlugin.getDefault().getBundle().getBundleContext();
		DsfServicesTracker tracker = new DsfServicesTracker(c, context.getSessionId());
		try {
			final IGDBProcesses procService = tracker.getService(IGDBProcesses.class);
			final IGDBBackend backend = tracker.getService(IGDBBackend.class);

			if (procService != null && backend != null) {
				// For a local attach, we can attach directly without
				// looking for the binary
				// since GDB will figure it out by itself
				IProcessDMContext procDmc = procService.createProcessContext(context, pid);
				procService.attachDebuggerToProcess(procDmc, new DataRequestMonitor<IDMContext>(
						DsfSession.getSession(context.getSessionId()).getExecutor(), rm) {
					@Override
					protected void handleErrorOrWarning() {
						IStatus status = getStatus();
						if (status != null && !status.isOK()) {
							IStatusHandler statusHandler = DebugPlugin.getDefault().getStatusHandler(status);
							if (statusHandler != null) {
								try {
									statusHandler.handleStatus(status, null);
								} catch (CoreException ex) {
									GdbUIPlugin.getDefault().getLog().log(ex.getStatus());
								}
							} else {
								GdbUIPlugin.getDefault().getLog().log(status);
							}
						}
						handleSuccess(); // mark as success even if fail.
					}

				});
			} else {
				rm.done();
				GdbUIPlugin.logErrorMessage("Could not retreive process service for context" + context); //$NON-NLS-1$
			}
		} finally {
			tracker.dispose();
		}
	}
}
