/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson and others.
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
 *     Marc Khouzam (Ericsson) - Disable button when no trace record is selected
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.gdb.internal.commands.ISelectPrevTraceRecordHandler;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.GDBTraceControl_7_2.TraceRecordSelectedChangedEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData2;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
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
 * Command to select the previous trace record
 *
 * @since 2.1
 */
public class GdbSelectPrevTraceRecordCommand extends AbstractDebugCommand implements ISelectPrevTraceRecordHandler {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;
	private final DsfSession fSession;

	public GdbSelectPrevTraceRecordCommand(DsfSession session) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
		fSession = session;
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

		Query<Object> selectRecordQuery = new Query<Object>() {
			@Override
			public void execute(final DataRequestMonitor<Object> rm) {
				final IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);

				if (traceControl != null) {
					traceControl.getCurrentTraceRecordContext(dmc,
							new DataRequestMonitor<ITraceRecordDMContext>(fExecutor, rm) {
								@Override
								protected void handleSuccess() {
									final ITraceRecordDMContext prevDmc = traceControl
											.createPrevRecordContext(getData());
									traceControl.selectTraceRecord(prevDmc, new ImmediateRequestMonitor(rm) {
										@Override
										protected void handleSuccess() {
											fSession.dispatchEvent(new TraceRecordSelectedChangedEvent(prevDmc),
													new Hashtable<String, String>());
											rm.done();
										}
									});
								}
							});
				} else {
					rm.done();
				}
			}
		};
		try {
			fExecutor.execute(selectRecordQuery);
			selectRecordQuery.get();
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

		Query<Boolean> canSelectRecordQuery = new Query<Boolean>() {
			@Override
			public void execute(final DataRequestMonitor<Boolean> rm) {
				final IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);

				if (traceControl != null) {
					traceControl.getTraceStatus(dmc, new DataRequestMonitor<ITraceStatusDMData>(fExecutor, rm) {
						@Override
						protected void handleSuccess() {
							if (getData().getNumberOfCollectedFrame() <= 0) {
								// No frames to look at.
								rm.done(false);
								return;
							}

							if (getData() instanceof ITraceStatusDMData2) {
								if (((ITraceStatusDMData2) getData()).getCurrentTraceFrameId() == null) {
									// Haven't started looking at frames, so don't enable the "Previous" button
									rm.done(false);
									return;
								}
							}

							traceControl.isTracing(dmc, new DataRequestMonitor<Boolean>(fExecutor, rm) {
								@Override
								protected void handleSuccess() {
									// Can do visualization if we are tracing.
									rm.done(!getData());
								}
							});
						}
					});
				} else {
					rm.done(false);
				}
			}
		};
		try {
			fExecutor.execute(canSelectRecordQuery);
			return canSelectRecordQuery.get();
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

	@Override
	protected boolean isRemainEnabled(IDebugCommandRequest request) {
		return true;
	}
}
