/*******************************************************************************
 * Copyright (c) 2013, 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dmitry Kozlov (Mentor Graphics) - Trace control view enhancements (Bug 390827)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints;

import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints.TraceControlView.FailedTraceVariableCreationException;
import org.eclipse.cdt.dsf.gdb.service.GDBTraceControl_7_2.TraceRecordSelectedChangedEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData2;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceVariableDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingStartedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingStoppedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingSupportedChangeDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl2;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This class is a bridge between the TraceControl view and the TraceControl service.
 * It performs the necessary requests to the service on behalf of the view.
 * Those request must be done on the DSF Executor thread.
 * Note that this class will have a single instance which will deal with
 * all DSF debug sessions at the same time.
 */
public class TraceControlModel {

	private String fDebugSessionId;
	private DsfServicesTracker fServicesTracker;
	private volatile IGDBTraceControl fGDBTraceControl;
	private volatile ITraceTargetDMContext fTargetContext;
	private TraceControlView fTraceControlView;

	private IDebugContextListener fDebugContextListener = event -> {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) != 0) {
			updateDebugContext();
		}
	};

	TraceControlModel(TraceControlView view) {
		fTraceControlView = view;

		IWorkbenchWindow window = fTraceControlView.getSite().getWorkbenchWindow();
		DebugUITools.getDebugContextManager().getContextService(window).addDebugContextListener(fDebugContextListener);
		updateDebugContext();
	}

	protected void updateContent() {
		if (getSession() == null) {
			notifyUI(TracepointsMessages.TraceControlView_trace_status_no_debug_session);
			return;
		}

		if (fTargetContext == null || fGDBTraceControl == null) {
			notifyUI(TracepointsMessages.TraceControlView_trace_status_not_supported);
			return;
		}

		getSession().getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				if (fTargetContext != null && fGDBTraceControl != null) {
					fGDBTraceControl.getTraceStatus(fTargetContext,
							new DataRequestMonitor<ITraceStatusDMData>(getSession().getExecutor(), null) {
								@Override
								protected void handleCompleted() {
									if (isSuccess() && getData() != null) {
										notifyUI((ITraceStatusDMData2) getData());
									} else {
										notifyUI((ITraceStatusDMData2) null);
									}
								}
							});
				} else {
					notifyUI((ITraceStatusDMData2) null);
				}
			}
		});
	}

	public void init() {
		if (fDebugSessionId != null) {
			debugSessionChanged();
		} else {
			updateDebugContext();
		}
	}

	public void dispose() {
		IWorkbenchWindow window = fTraceControlView.getSite().getWorkbenchWindow();
		DebugUITools.getDebugContextManager().getContextService(window)
				.removeDebugContextListener(fDebugContextListener);
		setDebugContext(null);
	}

	protected void updateDebugContext() {
		IAdaptable debugContext = DebugUITools.getDebugContext();
		if (debugContext instanceof IDMVMContext) {
			setDebugContext((IDMVMContext) debugContext);
		} else {
			setDebugContext(null);
		}
	}

	protected void setDebugContext(IDMVMContext vmContext) {
		if (vmContext != null) {
			IDMContext dmContext = vmContext.getDMContext();
			String sessionId = dmContext.getSessionId();
			fTargetContext = DMContexts.getAncestorOfType(dmContext, ITraceTargetDMContext.class);
			if (!sessionId.equals(fDebugSessionId)) {
				if (getSession() != null) {
					try {
						final DsfSession session = getSession();
						session.getExecutor().execute(new DsfRunnable() {
							@Override
							public void run() {
								session.removeServiceEventListener(TraceControlModel.this);
							}
						});
					} catch (RejectedExecutionException e) {
						// Session is shut down.
					}
				}
				fDebugSessionId = sessionId;
				if (fServicesTracker != null) {
					fServicesTracker.dispose();
				}
				fServicesTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), sessionId);
				getGDBTraceControl();
				debugSessionChanged();
			}
		} else if (fDebugSessionId != null) {
			if (getSession() != null) {
				try {
					final DsfSession session = getSession();
					session.getExecutor().execute(new DsfRunnable() {
						@Override
						public void run() {
							session.removeServiceEventListener(TraceControlModel.this);
						}
					});
				} catch (RejectedExecutionException e) {
					// Session is shut down.
				}
			}
			fDebugSessionId = null;
			fTargetContext = null;
			if (fServicesTracker != null) {
				fServicesTracker.dispose();
				fServicesTracker = null;
			}
			debugSessionChanged();
		}
	}

	private void debugSessionChanged() {
		if (getSession() != null) {
			try {
				final DsfSession session = getSession();
				session.getExecutor().execute(new DsfRunnable() {
					@Override
					public void run() {
						session.addServiceEventListener(TraceControlModel.this, null);
					}
				});
			} catch (RejectedExecutionException e) {
				// Session is shut down.
			}
		}

		updateContent();
	}

	public void exitVisualizationMode() {
		if (getSession() == null) {
			return;
		}

		getSession().getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				if (fTargetContext != null && fGDBTraceControl != null) {
					if (fGDBTraceControl instanceof IGDBTraceControl2) {
						((IGDBTraceControl2) fGDBTraceControl).stopTraceVisualization(fTargetContext,
								new ImmediateRequestMonitor());
					} else {
						// Legacy way of stopping visualization of trace data
						ITraceRecordDMContext emptyDmc = fGDBTraceControl.createTraceRecordContext(fTargetContext,
								"-1"); //$NON-NLS-1$
						fGDBTraceControl.selectTraceRecord(emptyDmc, new ImmediateRequestMonitor());
					}
				}
			}
		});
	}

	/**
	 * Get the list of trace variables from the backend.
	 *
	 * @return null when the list cannot be obtained.
	 */
	public ITraceVariableDMData[] getTraceVarList() {
		if (getSession() == null) {
			return null;
		}

		Query<ITraceVariableDMData[]> query = new Query<ITraceVariableDMData[]>() {
			@Override
			protected void execute(final DataRequestMonitor<ITraceVariableDMData[]> rm) {

				if (fTargetContext != null && fGDBTraceControl != null) {
					fGDBTraceControl.getTraceVariables(fTargetContext,
							new DataRequestMonitor<ITraceVariableDMData[]>(getSession().getExecutor(), rm) {
								@Override
								protected void handleCompleted() {
									if (isSuccess()) {
										rm.setData(getData());
									} else {
										rm.setData(null);
									}
									rm.done();
								}

							});
				} else {
					rm.setData(null);
					rm.done();
				}
			}
		};
		try {
			getSession().getExecutor().execute(query);
			return query.get(1, TimeUnit.SECONDS);
		} catch (InterruptedException exc) {
		} catch (ExecutionException exc) {
		} catch (TimeoutException e) {
		}

		return null;
	}

	/**
	 * Create a new trace variable in the backend.
	 *
	 * @throws FailedTraceVariableCreationException when the creation fails.  The exception
	 *         will contain the error message to display to the user.
	 */
	public void createVariable(final String name, final String value) throws FailedTraceVariableCreationException {
		if (getSession() == null) {
			throw new TraceControlView.FailedTraceVariableCreationException(
					TracepointsMessages.TraceControlView_create_variable_error);
		}

		Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {

				if (fTargetContext != null && fGDBTraceControl != null) {
					fGDBTraceControl.createTraceVariable(fTargetContext, name, value,
							new RequestMonitor(getSession().getExecutor(), rm) {
								@Override
								protected void handleFailure() {
									String message = TracepointsMessages.TraceControlView_create_variable_error;
									Throwable t = getStatus().getException();
									if (t != null) {
										message = t.getMessage();
									}
									FailedTraceVariableCreationException e = new FailedTraceVariableCreationException(
											message);
									rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID,
											IDsfStatusConstants.INVALID_STATE, "Backend error", e)); //$NON-NLS-1$
									rm.done();
								}
							});
				} else {
					FailedTraceVariableCreationException e = new FailedTraceVariableCreationException(
							TracepointsMessages.TraceControlView_trace_variable_tracing_unavailable);
					rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE,
							"Tracing unavailable", e)); //$NON-NLS-1$
					rm.done();
				}
			}
		};
		try {
			getSession().getExecutor().execute(query);
			query.get();
		} catch (InterruptedException e) {
			// Session terminated
		} catch (ExecutionException e) {
			Throwable t = e.getCause();
			if (t instanceof CoreException) {
				t = ((CoreException) t).getStatus().getException();
				if (t instanceof FailedTraceVariableCreationException) {
					throw (FailedTraceVariableCreationException) t;
				}
			}
			throw new FailedTraceVariableCreationException(TracepointsMessages.TraceControlView_create_variable_error);
		}
	}

	public void setCurrentTraceRecord(final String traceRecordId) {
		if (getSession() == null) {
			return;
		}

		getSession().getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				if (fTargetContext != null && fGDBTraceControl != null) {
					fGDBTraceControl.getCurrentTraceRecordContext(fTargetContext,
							new ImmediateDataRequestMonitor<ITraceRecordDMContext>() {
								@Override
								protected void handleSuccess() {
									final ITraceRecordDMContext previousDmc = getData();
									ITraceRecordDMContext nextRecord = fGDBTraceControl
											.createTraceRecordContext(fTargetContext, traceRecordId);

									// Must send the event right away to tell the services we are starting visualization
									// If we don't, the services won't behave accordingly soon enough
									// Bug 347514
									getSession().dispatchEvent(new TraceRecordSelectedChangedEvent(nextRecord),
											new Hashtable<String, String>());

									fGDBTraceControl.selectTraceRecord(nextRecord, new ImmediateRequestMonitor() {
										@Override
										protected void handleError() {
											// If we weren't able to select the next record, we must notify that we are still on the previous one
											// since we have already sent a TraceRecordSelectedChangedEvent early, but it didn't happen.
											getSession().dispatchEvent(new TraceRecordSelectedChangedEvent(previousDmc),
													new Hashtable<String, String>());
										}
									});
								}
							});

				}
			}
		});
	}

	public void setCircularBuffer(final boolean useCircularBuffer) {
		if (getSession() == null) {
			return;
		}

		getSession().getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				if (fTargetContext != null && fGDBTraceControl != null
						&& fGDBTraceControl instanceof IGDBTraceControl2) {
					((IGDBTraceControl2) fGDBTraceControl).setCircularTraceBuffer(fTargetContext, useCircularBuffer,
							new ImmediateRequestMonitor());
				}
			}
		});
	}

	public void setDisconnectedTracing(final boolean disconnected) {
		if (getSession() == null) {
			return;
		}

		getSession().getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				if (fTargetContext != null && fGDBTraceControl != null
						&& fGDBTraceControl instanceof IGDBTraceControl2) {
					((IGDBTraceControl2) fGDBTraceControl).setDisconnectedTracing(fTargetContext, disconnected,
							new ImmediateRequestMonitor());
				}
			}
		});
	}

	public void setTraceNotes(final String notes) {
		if (getSession() == null) {
			return;
		}

		getSession().getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				if (fTargetContext != null && fGDBTraceControl != null
						&& fGDBTraceControl instanceof IGDBTraceControl2) {
					((IGDBTraceControl2) fGDBTraceControl).setTraceNotes(fTargetContext, notes,
							new ImmediateRequestMonitor());
				}
			}
		});
	}

	private void getGDBTraceControl() {
		if (getSession() == null) {
			fGDBTraceControl = null;
			return;
		}

		getSession().getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				fGDBTraceControl = getService(IGDBTraceControl.class);
			}
		});
	}

	private DsfSession getSession() {
		return DsfSession.getSession(fDebugSessionId);
	}

	private <V> V getService(Class<V> serviceClass) {
		if (fServicesTracker != null) {
			return fServicesTracker.getService(serviceClass);
		}
		return null;
	}

	private void notifyUI(final ITraceStatusDMData2 data) {
		final TraceControlView v = fTraceControlView;
		if (v != null) {
			Display.getDefault().asyncExec(() -> {
				if (v != null) {
					v.fLastRefreshTime = System.currentTimeMillis();
					v.updateUI(data);
				}
			});
		}
	}

	private void notifyUI(final String message) {
		final TraceControlView v = fTraceControlView;
		if (v != null) {
			Display.getDefault().asyncExec(() -> {
				if (v != null) {
					v.updateUI(message);
				}
			});
		}
	}

	/*
	 * When tracing starts, we know the status has changed
	 */
	@DsfServiceEventHandler
	public void handleEvent(ITracingStartedDMEvent event) {
		updateContent();
	}

	/*
	 * When tracing stops, we know the status has changed
	 */
	@DsfServiceEventHandler
	public void handleEvent(ITracingStoppedDMEvent event) {
		updateContent();
	}

	@DsfServiceEventHandler
	public void handleEvent(ITraceRecordSelectedChangedDMEvent event) {
		updateContent();
	}

	/*
	 * Since something suspended, might as well refresh our status
	 * to show the latest.
	 */
	@DsfServiceEventHandler
	public void handleEvent(ISuspendedDMEvent event) {
		updateContent();
	}

	/*
	 * Tracing support has changed, update view
	 */
	@DsfServiceEventHandler
	public void handleEvent(ITracingSupportedChangeDMEvent event) {
		updateContent();
	}
}
