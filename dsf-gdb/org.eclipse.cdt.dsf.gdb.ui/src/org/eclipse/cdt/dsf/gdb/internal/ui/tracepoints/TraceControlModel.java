/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Dmitry Kozlov (Mentor Graphics) - trace control view enhancements (Bug 390827)
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
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints.TraceControlView.FailedTraceVariableCreationException;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl2;
import org.eclipse.cdt.dsf.gdb.service.GDBTraceControl_7_2.TraceRecordSelectedChangedEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceVariableDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingStartedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingStoppedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingSupportedChangeDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl2.ITraceStatusDMData2;
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
import org.eclipse.debug.ui.contexts.IDebugContextManager;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.swt.widgets.Display;

public class TraceControlModel implements DsfSession.SessionEndedListener {
	
	protected String fDebugSessionId;
	protected DsfServicesTracker fServicesTracker;
	protected IGDBTraceControl2 fGDBTraceControl;
	protected ITraceStatusDMData2 fTraceStatusData;
	protected volatile ITraceTargetDMContext fTargetContext;
	protected IDebugContextListener fDebugContextListener;
	protected TraceControlView fTraceControlView;
	
	TraceControlModel(TraceControlView view) {
		fTraceControlView = view;
		DsfSession.addSessionEndedListener(this);

		setupContextListener();
		updateDebugContext();
	}
	
	/** Value object to pass trace status data to UI thread */
	class TraceStatusData {
		
		public TraceStatusData(ITraceStatusDMData2 tData) {
			this.tData = tData;
		}
		
		ITraceStatusDMData2 tData;
		String currentTraceFrame = null;
		int tracepointIndexForCurrentTraceRecord = -1;
	}

	protected void updateContent() {
		
		if (fDebugSessionId == null || getSession() == null) {
			notifyUI(TracepointsMessages.TraceControlView_trace_status_no_debug_session);
			return;
		}
		
		final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
		final IGDBTraceControl2 traceControl = fGDBTraceControl;
		if (ctx == null || traceControl == null) {
			notifyUI(TracepointsMessages.TraceControlView_trace_status_not_supported);
			return;
		}

		getSession().getExecutor().execute(
			new DsfRunnable() {	
				@Override
				public void run() {
					if (ctx != null && traceControl != null) {
						traceControl.getTraceStatus(
							ctx, new DataRequestMonitor<ITraceStatusDMData>(getSession().getExecutor(), null) {
								@Override
								protected void handleCompleted() {
									if (isSuccess() && getData() != null) {
											retrieveCurrentTraceRecordData();
											TraceStatusData traceData = new TraceStatusData((ITraceStatusDMData2)getData());
											if (fGDBTraceControl != null) {
												traceData.currentTraceFrame = fGDBTraceControl.getCurrentTraceFrame();
												traceData.tracepointIndexForCurrentTraceRecord = fGDBTraceControl.getTracepointIndexForCurrentTraceRecord();
											}
											notifyUI(traceData);
									} else {
										notifyUI((TraceStatusData)null);
									}
								}
							});
					} else {
						notifyUI((TraceStatusData)null);
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
		IDebugContextManager contextManager = DebugUITools.getDebugContextManager();
		IDebugContextService contextService = contextManager.getContextService(fTraceControlView.getSite().getWorkbenchWindow());
		contextService.removeDebugContextListener(fDebugContextListener);
		DsfSession.removeSessionEndedListener(this);
		setDebugContext(null);
	}
	
	protected void updateDebugContext() {
		IAdaptable debugContext = DebugUITools.getDebugContext();
		if (debugContext instanceof IDMVMContext) {
			setDebugContext((IDMVMContext)debugContext);
		} else {
			setDebugContext(null);
		}
	}
	
	protected void setupContextListener() {
		IDebugContextManager contextManager = DebugUITools.getDebugContextManager();
		IDebugContextService contextService = contextManager.getContextService(fTraceControlView.getSite().getWorkbenchWindow());

		fDebugContextListener = new IDebugContextListener() {
			@Override
			public void debugContextChanged(DebugContextEvent event) {
				if ((event.getFlags() & DebugContextEvent.ACTIVATED) != 0) {
					updateDebugContext();
				}
			}
		};
		contextService.addDebugContextListener(fDebugContextListener);
		updateDebugContext();
	}
	
	protected void setDebugContext(IDMVMContext vmContext) {
		if (vmContext != null) {
			IDMContext dmContext = vmContext.getDMContext();
			String sessionId = dmContext.getSessionId();
			fTargetContext = DMContexts.getAncestorOfType(dmContext, ITraceTargetDMContext.class);
			if (!sessionId.equals(fDebugSessionId)) {
				if (fDebugSessionId != null && getSession() != null) {
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
		// When dealing with a new debug session, assume tracing is not supported.
		// updateContent() will fix it
		
		if (fDebugSessionId != null && getSession() != null) {
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

	@Override
	public void sessionEnded(DsfSession session) {
		if (session.getId().equals(fDebugSessionId)) {
			setDebugContext(null);
		}
	}

	protected void exitVisualizationMode() {
		if (fDebugSessionId == null || getSession() == null) {
			return;
		}
		
		final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
		final IGDBTraceControl2 traceControl = fGDBTraceControl;

		if (ctx == null || traceControl == null)
			return;
		
		getSession().getExecutor().execute(
				new DsfRunnable() {	
					@Override
					public void run() {
						if (ctx != null && traceControl != null) {
							ITraceRecordDMContext emptyDmc = traceControl.createTraceRecordContext(ctx, "-1"); //$NON-NLS-1$
							traceControl.selectTraceRecord(emptyDmc, new ImmediateRequestMonitor());
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
		if (fDebugSessionId == null || getSession() == null) {
			return null;
		}
		
		final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
		final IGDBTraceControl2 traceControl = fGDBTraceControl;

		if (ctx == null || traceControl == null)
			return null;
		
		Query<ITraceVariableDMData[]> query = new Query<ITraceVariableDMData[]>() {
			@Override
			protected void execute(final DataRequestMonitor<ITraceVariableDMData[]> rm) {
				
				if (ctx != null && traceControl != null) {
					traceControl.getTraceVariables(ctx,
							new DataRequestMonitor<ITraceVariableDMData[]>(getSession().getExecutor(), rm) {
						@Override
						protected void handleCompleted() {
							if (isSuccess()) {
								rm.setData(getData());
							} else {
								rm.setData(null);
							}
							rm.done();
						};

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
	protected void createVariable(final String name, final String value) throws FailedTraceVariableCreationException {
		if (fDebugSessionId == null || getSession() == null) {
			throw new TraceControlView.FailedTraceVariableCreationException(TracepointsMessages.TraceControlView_create_variable_error);
		}
		
		final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
		if (ctx == null) {
			throw new TraceControlView.FailedTraceVariableCreationException(TracepointsMessages.TraceControlView_create_variable_error);
		}

		Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				final IGDBTraceControl2 traceControl = fGDBTraceControl;
				
				if (traceControl != null) {
					traceControl.createTraceVariable(ctx, name, value, 
							new RequestMonitor(getSession().getExecutor(), rm) {
						@Override
						protected void handleFailure() {
							String message = TracepointsMessages.TraceControlView_create_variable_error;
							Throwable t = getStatus().getException();
							if (t != null) {
								message = t.getMessage();
							}
							FailedTraceVariableCreationException e = 
								new FailedTraceVariableCreationException(message);
							rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Backend error", e)); //$NON-NLS-1$
							rm.done();
						};
					});
				} else {
					FailedTraceVariableCreationException e = 
						new FailedTraceVariableCreationException(TracepointsMessages.TraceControlView_trace_variable_tracing_unavailable);
					rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Tracing unavailable", e)); //$NON-NLS-1$
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
				t = ((CoreException)t).getStatus().getException();
				if (t instanceof FailedTraceVariableCreationException) {
					throw (FailedTraceVariableCreationException)t;
				}
			}
			throw new FailedTraceVariableCreationException(TracepointsMessages.TraceControlView_create_variable_error);
		}
	}
	
	public void setDisconnectedTracing(final boolean disconnected) {
		final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
		final IGDBTraceControl2 traceControl = fGDBTraceControl;
		if (ctx == null || traceControl == null)
			return;

		getSession().getExecutor().execute(
				new DsfRunnable() {	
					@Override
					public void run() {
						traceControl.setDisconnectedTracing(ctx, disconnected, new ImmediateRequestMonitor());
					}
				});
	}
	
	public void setCurrentTraceRecord(final String traceRecordId) {
		if (fDebugSessionId != null && getSession() != null) {
			final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
			final IGDBTraceControl2 traceControl = fGDBTraceControl;
			if (ctx == null || traceControl == null)
				return;
			
			getSession().getExecutor().execute(
					new DsfRunnable() {	
						@Override
						public void run() {
							if (ctx != null && traceControl != null) {
								final ITraceRecordDMContext nextRecord = traceControl.createTraceRecordContext(ctx, traceRecordId);

								traceControl.selectTraceRecord(nextRecord, new ImmediateRequestMonitor() {
									@Override
									protected void handleSuccess() {
										getSession().dispatchEvent(new TraceRecordSelectedChangedEvent(nextRecord), new Hashtable<String, String>());
									}
								});
							}
						}
					});
		}
	}
	
	public void setTraceNotes(final String notes) {
		final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
		final IGDBTraceControl2 traceControl = fGDBTraceControl;
		if (ctx == null || traceControl == null)
			return;

		getSession().getExecutor().execute(
				new DsfRunnable() {	
					@Override
					public void run() {
						if (ctx != null && traceControl != null) {
							traceControl.setTraceNotes(ctx, notes, new ImmediateRequestMonitor());
						}
					}
				});
	}
	
	public void setCircularBuffer(final boolean useCircularBuffer) {
		final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
		final IGDBTraceControl2 traceControl = fGDBTraceControl;
		if (ctx == null || traceControl == null)
			return;
		
		getSession().getExecutor().execute(
				new DsfRunnable() {	
					@Override
					public void run() {
						traceControl.setCircularTraceBuffer(ctx, useCircularBuffer, new ImmediateRequestMonitor());
					}
				});
		updateContent();
	}

	protected void retrieveCurrentTraceRecordData() {
		final ITraceRecordDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceRecordDMContext.class);
		final IGDBTraceControl2 traceControl = fGDBTraceControl;
		
		if (ctx == null || traceControl == null)
			return;
		
		getSession().getExecutor().execute(
				new DsfRunnable() {	
					@Override
					public void run() {
						if (ctx != null && traceControl != null) {
							traceControl.getTraceRecordData(
								ctx, new DataRequestMonitor<ITraceRecordDMData>(getSession().getExecutor(), null) {
									@Override
									protected void handleCompleted() {
										if (isSuccess()) {
											getData();
										}												
									}
								});
						}

					}
			});
	}

	void getGDBTraceControl() {
		getSession().getExecutor().execute(
				new DsfRunnable() {	
					@Override
					public void run() {
						fGDBTraceControl = (IGDBTraceControl2) getService(IGDBTraceControl.class);
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

	private void notifyUI(final TraceStatusData data) {
		final TraceControlView v = fTraceControlView;
		if (v != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (v != null) {
						v.updateUI(data);
					}
				}
			});
		}
	}

	private void notifyUI(final String message) {
		final TraceControlView v = fTraceControlView;
		if (v != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (v != null) {
						v.updateUI(message);
					}
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
