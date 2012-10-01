/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Dmitry Kozlov (Mentor Graphics) - trace control view enhancements
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints;

import java.text.DateFormat;
import java.util.Date;
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
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
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
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.STOP_REASON_ENUM;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl2;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl2.ITraceStatusDMData2;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextManager;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

/**
 * TraceControlView Part
 * 
 * This view is used to control Tracing.
 * 
 * @since 2.1
 */
public class TraceControlView extends ViewPart implements IViewPart, SessionEndedListener {

	private static final int EXTRA_SPACE = 20;
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final int UI_REFRESH_DELAY = 1000; // milliseconds

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
	
	private String fDebugSessionId;
	private DsfServicesTracker fServicesTracker;
	IGDBTraceControl2 fGDBTraceControl;
	private volatile ITraceTargetDMContext fTargetContext;

	private Label fStatusLabel;
	protected Action fActionRefreshView;
	protected Action fOpenTraceVarDetails;
	protected Action fActionExitVisualization;
	private boolean fTracingSupported;

	private boolean fTraceVisualization;
	
	protected Label fBufferLabel;
	protected Composite fBufferComposite;
	protected ProgressBar fBufferProgressBar;
	protected Label fBufferNumberLabel;
	protected Button fSetCircularBufferButton;

	protected Composite fFrameComposite;
	protected Label fFrameNumberLabel;
	protected Slider fFrameSlider;
	
	protected Label fUserLabel;
	protected Composite fUserComposite; 
	protected Label fUserNameLabel;
	protected Button fSetUserNameButton;
	protected Text fUserNameText;
	
	protected Label fNotesLabel;
	protected Composite fNotesComposite;
	protected Label fNotesContentLabel;
	protected Text fNotesContentText;
	protected Button fSetNotesButton;
	
	protected Label fStopLabel;
	protected Label fStopNotesLabel;
	protected Composite fStopNotesComposite;
	protected Label fStopNotesContentLabel;
	protected Text fStopNotesContentText;
	protected Button fSetStopNotesButton;
	
	protected Composite composite;
	
	protected Job refreshUIJob;
	private IDebugContextListener fDebugContextListener;
	
	public TraceControlView() {
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		setupContextListener();
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
	}

	@Override
	public void createPartControl(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		composite = new Composite(sc, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setAlwaysShowScrollBars(false);

		// Tracing status line
		createStatusLine();
		
		// User name
		createUserLine(composite);
		setUserNameLineVisible(false, null, false);
		
		// Start notes
		createNotesLine(composite);
		setNotesLineVisible(false, null, false);
		
		// Stop reason 
		createStopLine(composite);
		setStopLineVisible(false, null);
		
		// Stop notes
		createStopNotesLine(composite);
		setStopNotesLineVisible(false, null, false);
		
		// Frame line
		createFrameLine(composite);
		setFrameLineVisible(false, null);
		
		// Buffer line
		createBufferLine(composite);
		setBufferLineVisible(false, null, false);
		
		createActions();

		Point size = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		
		// We need some extra space in composite to prevent scrollComposite's 
		// scrollbar to overlap with inner composite contents. Without this
		// UI looks bad.
		size.x += EXTRA_SPACE;
		size.y += EXTRA_SPACE;
		sc.setMinSize(size);

		if (fDebugSessionId != null) {
			debugSessionChanged();
		} else {
			updateDebugContext();
		}
		DsfSession.addSessionEndedListener(this);
	}

	private void createStatusLine() {
		fStatusLabel = new Label(composite, SWT.WRAP);
		GridData gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		gd.horizontalSpan = 2;
		fStatusLabel.setLayoutData(gd);
		fStatusLabel.setText(TracepointsMessages.TraceControlView_trace_status_inactive);
	}

	private void createUserLine(final Composite parent) {
		fUserLabel = new Label(parent, SWT.NONE);
		fUserLabel.setText(TracepointsMessages.TraceControlView_trace_user_label);
		fUserLabel.setLayoutData(new GridData());
		
		fUserComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		fUserComposite.setLayout(layout);
		fUserComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		fUserNameLabel = new Label(fUserComposite, SWT.NONE);
		fUserNameLabel.setText(TracepointsMessages.TraceControlView_trace_user_not_set);
		fUserNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));		

		fUserNameText = new Text(fUserComposite, SWT.BORDER);
		fUserNameText.setVisible(false);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.exclude = true;
		fUserNameText.setLayoutData(gd);
		fUserNameText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				fSetUserNameButton.setSelection(false);
				handleEditUserButtonPressed();
			}
		});
		
		fSetUserNameButton = new Button(fUserComposite, SWT.TOGGLE);
		fSetUserNameButton.setImage(TracepointImageRegistry.getImageDescriptor(
				TracepointImageRegistry.ICON_Edit_Enabled).createImage());
		fSetUserNameButton.setSelection(false);
		fSetUserNameButton.setToolTipText(TracepointsMessages.TraceControlView_trace_user_edit);
		fSetUserNameButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		fSetUserNameButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleEditUserButtonPressed();
			}
		});
	}

	protected void handleEditUserButtonPressed() {
		boolean isEditMode = fSetUserNameButton.getSelection();
		fUserNameLabel.setVisible(!isEditMode);
		((GridData)fUserNameLabel.getLayoutData()).exclude = isEditMode;
		fUserNameText.setVisible(isEditMode);
		((GridData)fUserNameText.getLayoutData()).exclude = !isEditMode;
		fUserNameText.setFocus();

		if (isEditMode) {
			String txt = fUserNameLabel.getText();
			txt = TracepointsMessages.TraceControlView_trace_user_not_set.equals(txt) ? EMPTY_STRING : txt;
			fUserNameText.setText(txt);
			fSetUserNameButton.setToolTipText(TracepointsMessages.TraceControlView_trace_user_save);
		} else {
			fSetUserNameButton.setToolTipText(TracepointsMessages.TraceControlView_trace_user_edit);
			final String userName = fUserNameText.getText();
			fUserNameLabel.setText(userName);
			final IGDBTraceControl2 traceControl = fGDBTraceControl;
			if (traceControl != null) {
				final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
				if (ctx != null) {
	    			getSession().getExecutor().execute(
	    					new DsfRunnable() {	
	    						@Override
	    						public void run() {
	    							traceControl.setTraceUser(ctx, userName, new ImmediateRequestMonitor());
	    						}
	    					});
				}
			}
		}
		fUserComposite.layout();
		fUserComposite.redraw();
	}
	
	private void setUserNameLineVisible(boolean visible, ITraceStatusDMData2 tData, boolean readonly) {
		fUserLabel.setVisible(visible);
		((GridData)fUserLabel.getLayoutData()).exclude = !visible;
		fUserComposite.setVisible(visible);
		((GridData)fUserComposite.getLayoutData()).exclude = !visible;

		
		if (visible) {
			if (tData.getUserName().length() > 0) {
				fUserNameLabel.setText(removeQuotes(tData.getUserName()));
			} else {
				fUserNameLabel.setText(TracepointsMessages.TraceControlView_trace_user_not_set);
			}
			fSetUserNameButton.setEnabled(!readonly);
		}
	}
	
	private void createNotesLine(final Composite parent) {
		fNotesLabel = new Label(parent, SWT.NONE);
		fNotesLabel.setText(TracepointsMessages.TraceControlView_trace_notes_label);
		fNotesLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		fNotesComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		fNotesComposite.setLayout(layout);
		fNotesComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		fNotesContentLabel = new Label(fNotesComposite, SWT.WRAP);
		fNotesContentLabel.setText(TracepointsMessages.TraceControlView_trace_notes_not_set);
		fNotesContentLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fNotesContentText = new Text(fNotesComposite, SWT.BORDER);
		fNotesContentText.setVisible(false);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.exclude = true;
		fNotesContentText.setLayoutData(gd);
		fNotesContentText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				fSetNotesButton.setSelection(false);
				handleEditNotesButtonPressed();
			}
		});		
		
		fSetNotesButton = new Button(fNotesComposite, SWT.TOGGLE);
		fSetNotesButton.setImage(TracepointImageRegistry.getImageDescriptor(
				TracepointImageRegistry.ICON_Edit_Enabled).createImage());
		fSetNotesButton.setSelection(false);
		fSetNotesButton.setToolTipText(TracepointsMessages.TraceControlView_trace_notes_edit_tooltip);
		fSetNotesButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		fSetNotesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleEditNotesButtonPressed();
			}
		});
	}

	protected void handleEditNotesButtonPressed() {
		boolean isEditMode = fSetNotesButton.getSelection();
		fNotesContentLabel.setVisible(!isEditMode);
		((GridData)fNotesContentLabel.getLayoutData()).exclude = isEditMode;
		fNotesContentText.setVisible(isEditMode);
		((GridData)fNotesContentText.getLayoutData()).exclude = !isEditMode;
		fNotesContentText.setFocus();

		if (isEditMode) {
			String txt = fNotesContentLabel.getText();
			txt = TracepointsMessages.TraceControlView_trace_notes_not_set.equals(txt) ? EMPTY_STRING : txt;
			fNotesContentText.setText(txt);
			fSetNotesButton.setToolTipText(TracepointsMessages.TraceControlView_trace_notes_save_tooltip);
		} else {
			fSetNotesButton.setToolTipText(TracepointsMessages.TraceControlView_trace_notes_edit_tooltip);
			final String notes = fNotesContentText.getText();
			fNotesContentLabel.setText(notes);
			final IGDBTraceControl2 traceControl = fGDBTraceControl;
			if (traceControl != null) {
				final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
				if (ctx != null) {
	    			getSession().getExecutor().execute(
	    					new DsfRunnable() {	
	    						@Override
	    						public void run() {
	    							traceControl.setTraceNotes(ctx, notes, new ImmediateRequestMonitor());
	    						}
	    					});
				}
			}
		}
		fNotesComposite.layout();
		fNotesComposite.redraw();
	}
	
	private void setNotesLineVisible(boolean visible, ITraceStatusDMData2 tData, boolean readonly) {
		fNotesLabel.setVisible(visible);
		((GridData)fNotesLabel.getLayoutData()).exclude = !visible;
		fNotesComposite.setVisible(visible);
		((GridData)fNotesComposite.getLayoutData()).exclude = !visible;

		if (visible) {
			if (tData.getStartNotes() != null && tData.getStartNotes().length() > 0) {
				fNotesContentLabel.setText(removeQuotes(tData.getStartNotes()));
			} else {
				fNotesContentLabel.setText(TracepointsMessages.TraceControlView_trace_notes_not_set);
			}
			fSetNotesButton.setEnabled(!readonly);
		}
	}
	
	private void createStopLine(final Composite parent) {
		fStopLabel = new Label(parent, SWT.WRAP);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 2;
		fStopLabel.setLayoutData(gd);
	}
	
	private void setStopLineVisible(boolean visible, ITraceStatusDMData tData) {
		fStopLabel.setVisible(visible);
		((GridData)fStopLabel.getLayoutData()).exclude = !visible;
	}
	
	private void createStopNotesLine(final Composite parent) {
		fStopNotesLabel = new Label(parent, SWT.NONE);
		fStopNotesLabel.setText(TracepointsMessages.TraceControlView_trace_stop_notes_label);
		fStopNotesLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		fStopNotesComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		fStopNotesComposite.setLayout(layout);
		fStopNotesComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		fStopNotesContentLabel = new Label(fStopNotesComposite, SWT.WRAP);
		fStopNotesContentLabel.setText(TracepointsMessages.TraceControlView_trace_notes_not_set);
		fStopNotesContentLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		fStopNotesContentText = new Text(fStopNotesComposite, SWT.BORDER);
		fStopNotesContentText.setVisible(false);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.exclude = true;
		fStopNotesContentText.setLayoutData(gd);
		fStopNotesContentText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				fSetStopNotesButton.setSelection(false);
				handleEditStopNotesButtonPressed();
			}
		});
		
		fSetStopNotesButton = new Button(fStopNotesComposite, SWT.TOGGLE);
		fSetStopNotesButton.setImage(TracepointImageRegistry.getImageDescriptor(
				TracepointImageRegistry.ICON_Edit_Enabled).createImage());
		fSetStopNotesButton.setSelection(false);
		fSetStopNotesButton.setToolTipText(TracepointsMessages.TraceControlView_trace_stop_notes_edit_tooltip);
		fSetStopNotesButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		fSetStopNotesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleEditStopNotesButtonPressed();
			}	
		});
	}

	protected void handleEditStopNotesButtonPressed() {
		boolean isEditMode = fSetStopNotesButton.getSelection();
		fStopNotesContentLabel.setVisible(!isEditMode);
		((GridData)fStopNotesContentLabel.getLayoutData()).exclude = isEditMode;
		fStopNotesContentText.setVisible(isEditMode);
		((GridData)fStopNotesContentText.getLayoutData()).exclude = !isEditMode;
		fStopNotesContentText.setFocus();

		if (isEditMode) {
			String txt = fStopNotesContentLabel.getText();
			txt = TracepointsMessages.TraceControlView_trace_stop_notes_not_set.equals(txt) ? EMPTY_STRING : txt;
			fStopNotesContentText.setText(txt);
			fSetStopNotesButton.setToolTipText(TracepointsMessages.TraceControlView_trace_stop_notes_save_tooltip);
		} else {
			fSetStopNotesButton.setToolTipText(TracepointsMessages.TraceControlView_trace_stop_notes_edit_tooltip);
			final String notes = fStopNotesContentText.getText();
			fStopNotesContentLabel.setText(notes);
			final IGDBTraceControl2 traceControl = fGDBTraceControl;
			if (traceControl != null) {
				final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
				if (ctx != null) {
	    			getSession().getExecutor().execute(
	    					new DsfRunnable() {	
	    						@Override
	    						public void run() {
	    							traceControl.setTraceStopNotes(ctx, notes, new ImmediateRequestMonitor());
	    						}
	    					});
				}
			}
		}
		fStopNotesComposite.layout();
		fStopNotesComposite.redraw();
	}

	private void setStopNotesLineVisible(boolean visible, ITraceStatusDMData2 tData, boolean readonly) {
		fStopNotesLabel.setVisible(visible);
		((GridData)fStopNotesLabel.getLayoutData()).exclude = !visible;
		fStopNotesComposite.setVisible(visible);
		((GridData)fStopNotesComposite.getLayoutData()).exclude = !visible;

		if (visible) {
			if (tData.getStopNotes() != null && tData.getStopNotes().length() > 0) {
				fStopNotesContentLabel.setText(removeQuotes(tData.getStopNotes()));
			} else {
				fStopNotesContentLabel.setText(TracepointsMessages.TraceControlView_trace_notes_not_set);
			}
			fSetStopNotesButton.setEnabled(!readonly);
			fStopLabel.setText(getStopMessage(tData));
		}
	}
	
	private void createFrameLine(Composite parent) {
		fFrameNumberLabel = new Label(parent, SWT.NONE);
		fFrameNumberLabel.setText(TracepointsMessages.TraceControlView_frame_not_looking);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 2;
		fFrameNumberLabel.setLayoutData(gd);

		fFrameSlider = new Slider(parent, SWT.HORIZONTAL | SWT.BORDER);
		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd2.horizontalSpan = 2;
		fFrameSlider.setLayoutData(gd2);
		fFrameSlider.setValues(0, 0, 100, 1, 1, 10);
		fFrameSlider.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
    			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
    			if (e.detail == SWT.ARROW_DOWN) {
        			try {
    					handlerService.executeCommand("org.eclipse.cdt.dsf.gdb.ui.command.selectNextTraceRecord", null); //$NON-NLS-1$
    				} catch (Exception ex) {
    				}
    			} else if (e.detail == SWT.ARROW_UP) {
        			try {
    					handlerService.executeCommand("org.eclipse.cdt.dsf.gdb.ui.command.selectPreviousTraceRecord", null); //$NON-NLS-1$
    				} catch (Exception ex) {
    				}
    			} else {
    				final String traceRecordId = Integer.toString(fFrameSlider.getSelection());
    				
    				if (fDebugSessionId != null && getSession() != null) {
    					final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
        				if (ctx == null) {
        					return;
        				}
        				
        				getSession().getExecutor().execute(
        						new DsfRunnable() {	
        							@Override
        							public void run() {
        								final IGDBTraceControl2 traceControl = fGDBTraceControl;
        								if (traceControl != null) {
        									ITraceRecordDMContext nextRecord = traceControl.createTraceRecordContext(ctx, traceRecordId);
        									traceControl.selectTraceRecord(nextRecord, new ImmediateRequestMonitor());
        								}
        							}
        						});
        			}
    				
    			}
			}
			
		});
	}

	private void setFrameLineVisible(boolean visible, ITraceStatusDMData2 tData) {
		fFrameNumberLabel.setVisible(visible);
		((GridData)fFrameNumberLabel.getLayoutData()).exclude = !visible;
		fFrameSlider.setVisible(visible);
		((GridData)fFrameSlider.getLayoutData()).exclude = !visible;
		
		if (visible) {
			fFrameSlider.setMinimum(0);
			fFrameSlider.setMaximum(tData.getNumberOfCollectedFrame());
			int inc = tData.getNumberOfCollectedFrame() / 20;
			fFrameSlider.setPageIncrement(inc <= 1 ? 2 : inc);
	
			String fl = EMPTY_STRING;
			if ( tData.getCurrentTraceFrame() != null ) {
				fl += TracepointsMessages.bind(TracepointsMessages.TraceControlView_frame_looking,
						new Object[] { tData.getCurrentTraceFrame(), tData.getNumberOfCollectedFrame(), tData.getTracepointIndexForCurrentTraceRecord()} );

				fFrameSlider.setSelection(Integer.parseInt(tData.getCurrentTraceFrame()));
			} else {
				fl += TracepointsMessages.bind(TracepointsMessages.TraceControlView_frame_not_looking, tData.getNumberOfCollectedFrame());
				fFrameSlider.setSelection(0);
			}
			fFrameNumberLabel.setText(fl);
		}
	}
	
	private void createBufferLine(final Composite parent) {
		fBufferComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		fBufferComposite.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 2;
		fBufferComposite.setLayoutData(gd);

		fBufferNumberLabel = new Label(fBufferComposite, SWT.NONE);
		fBufferNumberLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));		
		
		fSetCircularBufferButton = new Button(fBufferComposite, SWT.TOGGLE);
		fSetCircularBufferButton.setImage(TracepointImageRegistry.getImageDescriptor(
				TracepointImageRegistry.ICON_Refresh_enabled).createImage());
		fSetCircularBufferButton.setSelection(false);
		fSetCircularBufferButton.setEnabled(true);
		fSetCircularBufferButton.setToolTipText(TracepointsMessages.TraceControlView_circular_buffer_off_tooltip);
		fSetCircularBufferButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		fSetCircularBufferButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

					final boolean useCircularBuffer = fSetCircularBufferButton.getSelection();
					final IGDBTraceControl2 traceControl = fGDBTraceControl;
					if (traceControl != null) {
						final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
						if (ctx != null) {
		    				getSession().getExecutor().execute(
		    						new DsfRunnable() {	
		    							@Override
		    							public void run() {
											traceControl.setCircularTraceBuffer(ctx, useCircularBuffer, new ImmediateRequestMonitor());
		    							}
		    						});
						}
					}
					fSetCircularBufferButton.setToolTipText( useCircularBuffer ? 
							TracepointsMessages.TraceControlView_circular_buffer_on_tooltip : 
							TracepointsMessages.TraceControlView_circular_buffer_off_tooltip);
					updateContent();
				}
		});

		fBufferProgressBar = new ProgressBar(parent, SWT.SMOOTH);
		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd2.horizontalSpan = 2;
		fBufferProgressBar.setLayoutData(gd2);
		fBufferProgressBar.setMinimum(0);
		fBufferProgressBar.setMaximum(100);
		fBufferProgressBar.setSelection(90);		
	}

	private void setBufferLineVisible(boolean visible, ITraceStatusDMData2 tData, boolean readonly) {
		fBufferComposite.setVisible(visible);
		((GridData)fBufferComposite.getLayoutData()).exclude = !visible;
		fBufferProgressBar.setVisible(visible);
		((GridData)fBufferProgressBar.getLayoutData()).exclude = !visible;

		if (visible){
			fBufferProgressBar.setMaximum(tData.getTotalBufferSize());
			fBufferProgressBar.setSelection(tData.getTotalBufferSize() - tData.getFreeBufferSize());
			if (tData.isCircularBuffer()) {
				fBufferNumberLabel.setText(TracepointsMessages.bind(
						TracepointsMessages.TraceControlView_buffer_number_label_circular, tData.getTotalBufferSize()));
			} else {
				fBufferNumberLabel.setText(TracepointsMessages.bind(
						TracepointsMessages.TraceControlView_buffer_number_label_linear, 
						tData.getTotalBufferSize() - tData.getFreeBufferSize(), tData.getTotalBufferSize()));
			}
			fSetCircularBufferButton.setSelection(tData.isCircularBuffer());
			fSetCircularBufferButton.setEnabled(!readonly);
		}
	}
	
	protected void createActions() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		
		// Create the action to refresh the view
		fActionRefreshView = new ActionRefreshView();
		bars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fActionRefreshView);
		manager.add(fActionRefreshView);
		fActionRefreshView.setEnabled(true);

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
		IDebugContextManager contextManager = DebugUITools.getDebugContextManager();
		IDebugContextService contextService = contextManager.getContextService(getSite().getWorkbenchWindow());
		contextService.removeDebugContextListener(fDebugContextListener);
		DsfSession.removeSessionEndedListener(this);
		setDebugContext(null);

		fStatusLabel = null;  // Indicate that we have been disposed
		if (refreshUIJob != null) {
			refreshUIJob.cancel();
		}
		super.dispose();
	}
	
	private void setupContextListener() {
		IDebugContextManager contextManager = DebugUITools.getDebugContextManager();
		IDebugContextService contextService = contextManager.getContextService(getSite().getWorkbenchWindow());

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

	protected void updateContent() {			
		if (fDebugSessionId == null || getSession() == null) {
			updateUI();
			return;
		}
		
		final ITraceTargetDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ITraceTargetDMContext.class);
		if (ctx == null) {
			updateUI();
			return;
		}

		getSession().getExecutor().execute(
			new DsfRunnable() {	
				@Override
				public void run() {
					final IGDBTraceControl2 traceControl = fGDBTraceControl;
					if (traceControl != null) {
						traceControl.getTraceStatus(
							ctx, new DataRequestMonitor<ITraceStatusDMData>(getSession().getExecutor(), null) {
								@Override
								protected void handleCompleted() {
									if (isSuccess() && getData() != null) {
										fTracingSupported = getData().isTracingSupported();
										if (fTracingSupported) {
											updateUI((ITraceStatusDMData2)getData());
										} else {
											updateUI();
										}
									} else {
										fTracingSupported = false;
										updateUI();
									}													
								}
							});
					} else {
						fTracingSupported = false;
						updateUI();
					}

				}
		});
	}
	
	protected void updateUI() {
		asyncExec(new Runnable() {
			@Override
			public void run() {
            	try {
	            	setNotesLineVisible(false, null, false);
	            	setUserNameLineVisible(false, null, false);
	            	setFrameLineVisible(false, null);
	            	setBufferLineVisible(false, null, false);
	            	setStopLineVisible(false, null);
	            	setStopNotesLineVisible(false, null, false);
					fStatusLabel.setText(TracepointsMessages.TraceControlView_trace_status_inactive);
					updateActionEnablement();
					composite.layout();
            	} catch ( SWTException ex) {
            	}
			}});
	}

	protected void updateUI(ITraceStatusDMData2 traceData) {	
		if (traceData == null || !traceData.isTracingSupported()) {
			// should not happen, but still process it correctly
			updateUI();
		} else if (traceData.isOfflineTracing()) {
			// Off-line tracing from data file
			final ITraceStatusDMData2 tData = traceData;
			asyncExec(new Runnable() {
	            @Override
				public void run() {
	        		String s = TracepointsMessages.TraceControlView_trace_status_offline;
	    			
	    			fStatusLabel.setText(s);  				    			

					/* 
					 * It seems that gdb doesn't save time, user name, notes, stop-notes to a file, 
					 * so they are not useful in offline mode untis gdb is fixed
					 */
	    			setUserNameLineVisible(true, tData, true);
	    			setNotesLineVisible(true, tData, true);			
	        		
	    			// Stop reason and note. Warning: in off-line mode stop time is unavailable
	    			setStopLineVisible(true, tData);
	    			setStopNotesLineVisible(false, tData, true);
	    			
	    			// In offline mode we need frames slider
	    			setFrameLineVisible(true, tData);	    			
	    			
	    			updateActionEnablement();
	    			composite.layout();
				}});
			
		} else if (!traceData.isTracingActive() && traceData.getStopReason() == null){
			// Tracing is not started yet
			final ITraceStatusDMData2 tData = traceData;
			asyncExec(new Runnable() {
	            @Override
				public void run() {
	    			fStatusLabel.setText(TracepointsMessages.TraceControlView_trace_status_not_started);

	    			setUserNameLineVisible(true, tData, false);
	    			setNotesLineVisible(true, tData, false);  			
	    			setFrameLineVisible(false, tData);
	    			setBufferLineVisible(true, tData, false);

	    			updateActionEnablement();
	    			composite.layout();
				}});			
			
		} else {
			// Live execution tracing started and running or started and stopped
			final ITraceStatusDMData2 tData = traceData;
			asyncExec(new Runnable() {
	            @Override
				public void run() {
	    			fStatusLabel.setText(TracepointsMessages.TraceControlView_trace_status_was_started + formatTime(tData.getStartTime()));
	    			
	    			setUserNameLineVisible(true, tData, false);
	    			setNotesLineVisible(true, tData, false);
	    			setBufferLineVisible(true, tData, true);
	    			
	    			// If stopped, stop reason, time and note.
	    			IGDBTraceControl.STOP_REASON_ENUM fStopReason = tData.getStopReason();
	    			if (fStopReason != null) {
		    			setStopLineVisible(true, tData);
		    			setStopNotesLineVisible(true, tData, false);
		    			// When tracing has stopped we need frames slider
		    			setFrameLineVisible(true, tData);
	    			} else {
    					refreshUIJob = new Job("Refresh Trace Control view UI") { //$NON-NLS-1$
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								updateContent();
								return Status.OK_STATUS;
							}
    						
    					};
	    				refreshUIJob.schedule(UI_REFRESH_DELAY);
	    			}

	    			updateActionEnablement();
	    			composite.layout();
				}});			
		}
	}

	protected String getStopMessage(ITraceStatusDMData2 tData) {
		IGDBTraceControl.STOP_REASON_ENUM fStopReason = tData.getStopReason();

		String stopMessage = TracepointsMessages.bind(TracepointsMessages.TraceControlView_tracing_stopped_at, formatTime(tData.getStopTime()));
		
		if (fStopReason == STOP_REASON_ENUM.REQUEST) {
			stopMessage += TracepointsMessages.TraceControlView_tracing_stopped_user_request;
		} else if (fStopReason == STOP_REASON_ENUM.PASSCOUNT) {
			stopMessage += TracepointsMessages.TraceControlView_tracing_stopped_passcount;
			if (tData.getStoppingTracepoint() != null) {
				stopMessage += TracepointsMessages.bind(TracepointsMessages.TraceControlView_tracing_stopped_tracepoint_number, tData.getStoppingTracepoint());
			} else {
				stopMessage += TracepointsMessages.TraceControlView_tracing_stopped_passcount;
			}
		} else if (fStopReason == STOP_REASON_ENUM.OVERFLOW) {
			stopMessage += TracepointsMessages.TraceControlView_tracing_stopped_buffer_full;
		} else if (fStopReason == STOP_REASON_ENUM.DISCONNECTION) {
			stopMessage += TracepointsMessages.TraceControlView_tracing_stopped_disconnection;
		} else if (fStopReason == STOP_REASON_ENUM.ERROR) {
			stopMessage += TracepointsMessages.TraceControlView_tracing_stopped_error;
		} else {
			stopMessage += TracepointsMessages.TraceControlView_tracing_stopped_unknown;
		}

		return stopMessage;
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
						final IGDBTraceControl2 traceControl = fGDBTraceControl;
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
				fGDBTraceControl = (IGDBTraceControl2) getService(IGDBTraceControl.class);
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
		
		// This hack is to avoid adding an API late in the release.
		// For the next release, we should have a proper call to know if 
		// we can stop visualization or not
		if (fStatusLabel != null && fStatusLabel.getText().toLowerCase().indexOf("off") != -1) { //$NON-NLS-1$
			fActionExitVisualization.setEnabled(false);
		} else {
			fActionExitVisualization.setEnabled(fTraceVisualization);
		}
	}
	
	private void asyncExec(Runnable runnable) {
		if (fStatusLabel != null) {
			fStatusLabel.getDisplay().asyncExec(runnable);
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
		if (fStatusLabel != null) {
			fStatusLabel.setFocus();
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
				final IGDBTraceControl2 traceControl = fGDBTraceControl;
				
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
	
	/** 
	 * Format time from gdb presentation into user-understandable form
	 * @param time in gd presentation
	 * @return 
	 */
	protected String formatTime(String time) {
		long microseconds = 0;
		try { 
			String[] times = time.split("\\.");  //$NON-NLS-1$
			microseconds += Long.parseLong(times[0]) * 1000000;
			microseconds += Long.parseLong(times[1]);
		} catch (NumberFormatException ex) {
			GdbPlugin.log(ex);
		}
		Date date = new Date(microseconds / 1000);
		
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
	}
	
	protected String removeQuotes(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {  //$NON-NLS-1$//$NON-NLS-2$
			return s.substring(1, s.length()-1);
		} else {
			return s;
		}
	}
}