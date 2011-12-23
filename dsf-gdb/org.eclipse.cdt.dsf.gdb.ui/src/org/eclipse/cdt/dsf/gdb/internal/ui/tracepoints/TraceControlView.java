/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceVariableDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingStartedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingStoppedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingSupportedChangeDMEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

/**
 * TraceControlView Part
 * 
 * This view is used to control Tracing.
 * 
 * @since 2.1
 */
public class TraceControlView extends ViewPart implements IViewPart, SessionEndedListener {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public class FailedTraceVariableCreationException extends Exception {
	    private static final long serialVersionUID = -3042693455630687285L;

		FailedTraceVariableCreationException() {}
		
		FailedTraceVariableCreationException(String errorMessage) {
			super(errorMessage);
		}
	}
	
	/**
	 * Action to refresh the content of the view.
	 */
	private final class ActionRefreshView extends Action {
		public ActionRefreshView() {
			setText(TracepointsMessages.TraceControlView_action_Refresh_label);
			setImageDescriptor(TracepointImageRegistry.getImageDescriptor(TracepointImageRegistry.ICON_Refresh_enabled));
			setDisabledImageDescriptor(TracepointImageRegistry.getImageDescriptor(TracepointImageRegistry.ICON_Refresh_disabled));
		}
		@Override
		public void run() {
			updateContent();
		}
	}

	private final class ActionOpenTraceVarDetails extends Action {
		public ActionOpenTraceVarDetails() {
			setText(TracepointsMessages.TraceControlView_action_trace_variable_details);
			setImageDescriptor(TracepointImageRegistry.getImageDescriptor(TracepointImageRegistry.ICON_Trace_Variables));
		}
		@Override
		public void run() {
			Shell shell = Display.getDefault().getActiveShell();
			TraceVarDetailsDialog dialog = new TraceVarDetailsDialog(shell, TraceControlView.this);
			dialog.open();
		}
	}

	private final class ActionExitVisualizationModeDetails extends Action {
		public ActionExitVisualizationModeDetails() {
			setText(TracepointsMessages.TraceControlView_action_exit_visualization_mode);
			setImageDescriptor(TracepointImageRegistry.getImageDescriptor(TracepointImageRegistry.ICON_Exit_Visualization));
		}
		@Override
		public void run() {
			asyncExec(new Runnable() {
                @Override
				public void run() {
					exitVisualizationMode();
					updateActionEnablement();
				}});
		}
	}
	
	private ISelectionListener fDebugViewListener;
	private String fDebugSessionId;
	private DsfServicesTracker fServicesTracker;
	private volatile ITraceTargetDMContext fTargetContext;

	private StyledText fStatusText;
	protected Action fActionRefreshView;
	protected Action fOpenTraceVarDetails;
	protected Action fActionExitVisualization;
	private boolean fTracingSupported;

	private boolean fTraceVisualization;
	
	public TraceControlView() {
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, 
				                            fDebugViewListener = new ISelectionListener() {
            @Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				updateDebugContext();
			}});
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		
		// Let's just create a place to put a text status for now.
		// A fancier display would be nicer though
		fStatusText = new StyledText(composite, SWT.MULTI);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		fStatusText.setLayoutData(gd);
		fStatusText.setEditable(false);
		fStatusText.setCaret(null);
		
		createActions();

		if (fDebugSessionId != null) {
			debugSessionChanged();
		} else {
			updateDebugContext();
		}
		DsfSession.addSessionEndedListener(this);
	}

	protected void createActions() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
				
		// Create the action to refresh the view
		fActionRefreshView = new ActionRefreshView();
		bars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fActionRefreshView);
		manager.add(fActionRefreshView);

		// Create the action to open the trace variable details
		fOpenTraceVarDetails = new ActionOpenTraceVarDetails();
		manager.add(fOpenTraceVarDetails);
		
		// Create the action to exit visualization mode
		fActionExitVisualization = new ActionExitVisualizationModeDetails();
		manager.add(fActionExitVisualization);

		bars.updateActionBars();
		updateActionEnablement();
	}
	
	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, fDebugViewListener);
		fStatusText = null;  // Indicate that we have been disposed
		setDebugContext(null);
		super.dispose();
	}

	protected void updateContent() {			
		if (fDebugSessionId != null && getSession() != null) {
			final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
			if (ctx != null) {
				getSession().getExecutor().execute(
						new DsfRunnable() {	
			                @Override
							public void run() {
								final IGDBTraceControl traceControl = getService(IGDBTraceControl.class);
								if (traceControl != null) {
									traceControl.getTraceStatus(
											ctx, new DataRequestMonitor<ITraceStatusDMData>(getSession().getExecutor(), null) {
												@Override
												protected void handleCompleted() {
													String traceStatus = EMPTY_STRING;
													if (isSuccess() && getData() != null) {
														fTracingSupported = getData().isTracingSupported();
														if (fTracingSupported) {
															traceStatus = getData().toString();
															if (traceStatus.length() > 0) {
																Calendar cal = Calendar.getInstance();
																SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$

																traceStatus = TracepointsMessages.TraceControlView_trace_view_content_updated_label +
																sdf.format(cal.getTime()) + "\n" + traceStatus;  //$NON-NLS-1$
															}
														}
													} else {
														fTracingSupported = false;
													}
													
													final String finalStatus = traceStatus;
													asyncExec(new Runnable() {
										                @Override
														public void run() {
															fStatusText.setText(finalStatus);
															updateActionEnablement();
														}});
												}
											});
								} else {
									fTracingSupported = false;

									asyncExec(new Runnable() {
						                @Override
										public void run() {
											fStatusText.setText(EMPTY_STRING);
											updateActionEnablement();
										}});
								}

							}
						});
				return;
			}
		}
		
		if (fStatusText != null) {
			fStatusText.setText(EMPTY_STRING);
		}
		updateActionEnablement();
	}
		
	protected void exitVisualizationMode() {
		if (fDebugSessionId == null || getSession() == null) {
			return;
		}
		
		final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
		if (ctx == null) {
			return;
		}
		
		getSession().getExecutor().execute(
				new DsfRunnable() {	
	                @Override
					public void run() {
						final IGDBTraceControl traceControl = getService(IGDBTraceControl.class);
						if (traceControl != null) {
							ITraceRecordDMContext emptyDmc = traceControl.createTraceRecordContext(ctx, "-1"); //$NON-NLS-1$
							traceControl.selectTraceRecord(emptyDmc, new ImmediateRequestMonitor());
						}
					}
				});
	}
	
	protected void updateDebugContext() {
		IAdaptable debugContext = DebugUITools.getDebugContext();
		if (debugContext instanceof IDMVMContext) {
			setDebugContext((IDMVMContext)debugContext);
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
				if (fDebugSessionId != null && getSession() != null) {
					try {
						final DsfSession session = getSession();
						session.getExecutor().execute(new DsfRunnable() {
			                @Override
							public void run() {
								session.removeServiceEventListener(TraceControlView.this);
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
				debugSessionChanged();
			}
		} else if (fDebugSessionId != null) {
			if (getSession() != null) {
				try {
					final DsfSession session = getSession();
					session.getExecutor().execute(new DsfRunnable() {
		                @Override
						public void run() {
							session.removeServiceEventListener(TraceControlView.this);
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
		fTracingSupported = false;
		
		if (fDebugSessionId != null && getSession() != null) {
			try {
				final DsfSession session = getSession();
				session.getExecutor().execute(new DsfRunnable() {
	                @Override
					public void run() {
						session.addServiceEventListener(TraceControlView.this, null);
					}
				});
    		} catch (RejectedExecutionException e) {
                // Session is shut down.
    		}
        }
		
		updateContent();
	}

	protected void updateActionEnablement() {
		fOpenTraceVarDetails.setEnabled(fTracingSupported);
		fActionRefreshView.setEnabled(fTracingSupported);
		
		// This hack is to avoid adding an API late in the release.
		// For the next release, we should have a proper call to know if 
		// we can stop visualization or not
		if (fStatusText != null && fStatusText.getText().toLowerCase().indexOf("off-line") != -1) { //$NON-NLS-1$
			fActionExitVisualization.setEnabled(false);
		} else {
			fActionExitVisualization.setEnabled(fTraceVisualization);
		}
	}
	
	private void asyncExec(Runnable runnable) {
		if (fStatusText != null) {
			fStatusText.getDisplay().asyncExec(runnable);
		}
	}

    @Override
	public void sessionEnded(DsfSession session) {
		if (session.getId().equals(fDebugSessionId)) {
			asyncExec(new Runnable() {
                @Override
				public void run() {
					setDebugContext(null);
				}});
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
    	if (event.isVisualizationModeEnabled()) {
    		fTraceVisualization = true;
    	} else {
    		fTraceVisualization = false;
    	}
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

	
	@Override
	public void setFocus() {
		if (fStatusText != null) {
			fStatusText.setFocus();
		}
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
		if (ctx == null) {
			return null;
		}
		
		Query<ITraceVariableDMData[]> query = new Query<ITraceVariableDMData[]>() {
			@Override
			protected void execute(final DataRequestMonitor<ITraceVariableDMData[]> rm) {
				final IGDBTraceControl traceControl = getService(IGDBTraceControl.class);
				
				if (traceControl != null) {
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
			throw new FailedTraceVariableCreationException(TracepointsMessages.TraceControlView_create_variable_error);
		}
		
		final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
		if (ctx == null) {
			throw new FailedTraceVariableCreationException(TracepointsMessages.TraceControlView_create_variable_error);
		}

		Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				final IGDBTraceControl traceControl = getService(IGDBTraceControl.class);
				
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
}

