/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson and others.
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
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.model.IStartTracingHandler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl2;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

/**
 * Command to start the tracing experiment
 *
 * @since 2.1
 */
public class GdbStartTracingCommand extends AbstractDebugCommand implements IStartTracingHandler {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public GdbStartTracingCommand(DsfSession session) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
	}

	public void dispose() {
		fTracker.dispose();
	}

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {
		if (targets.length != 1) {
			return;
		}

		final ITraceTargetDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext) targets[0]).getDMContext(),
				ITraceTargetDMContext.class);
		if (dmc == null) {
			return;
		}

		Query<Object> startTracingQuery = new Query<Object>() {
			@Override
			public void execute(final DataRequestMonitor<Object> rm) {
				final IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);
				if (traceControl != null) {
					String user = System.getProperty("user.name"); //$NON-NLS-1$
					if (user != null && !user.isEmpty() && traceControl instanceof IGDBTraceControl2) {
						((IGDBTraceControl2) traceControl).setTraceUser(dmc, user, new ImmediateRequestMonitor() {
							@Override
							protected void handleCompleted() {
								traceControl.startTracing(dmc, rm);
							}
						});
					} else {
						traceControl.startTracing(dmc, rm);
					}
				} else {
					rm.done();
				}
			}
		};
		try {
			fExecutor.execute(startTracingQuery);
			startTracingQuery.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		}
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
			throws CoreException {
		if (targets.length != 1) {
			return false;
		}

		final ITraceTargetDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext) targets[0]).getDMContext(),
				ITraceTargetDMContext.class);
		if (dmc == null) {
			return false;
		}

		Query<Boolean> canStartTracingQuery = new Query<Boolean>() {
			@Override
			public void execute(DataRequestMonitor<Boolean> rm) {
				IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);

				if (traceControl != null) {
					traceControl.canStartTracing(dmc, rm);
				} else {
					rm.setData(false);
					rm.done();
				}
			}
		};
		try {
			fExecutor.execute(canStartTracingQuery);
			return canStartTracingQuery.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		}

		return false;
	}

	@Override
	protected Object getTarget(Object element) {
		if (element instanceof IDMVMContext) {
			return element;
		}
		return null;
	}

	/*
	 * Re-selection of the debug context will be forced by the Debug Model through a StartTracing event.
	 * Therefore, the enablement of this command will be refreshed, so we don't need to keep it enabled.
	 * In fact, it is better to have it disabled right after selection to avoid a double-click
	 *
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.commands.AbstractDebugCommand#isRemainEnabled(org.eclipse.debug.core.commands.IDebugCommandRequest)
	 */
	@Override
	protected boolean isRemainEnabled(IDebugCommandRequest request) {
		return false;
	}
}
