/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Dmitry Kozlov (Mentor Graphics) - Trace control view enhancements (Bug 390827)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData2;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.STOP_REASON_ENUM;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

/**
 * TraceControlView Part
 * 
 * This view is used to control Tracing.
 * 
 * @since 2.1
 */
public class TraceControlView extends ViewPart implements IViewPart {

	private static final int ACTION_BUTTON_INDENTATION = 10;
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected static final int UI_REFRESH_DELAY = 1000; // milliseconds

	public static class FailedTraceVariableCreationException extends Exception {
	    private static final long serialVersionUID = -3042693455630687285L;

		FailedTraceVariableCreationException() {}
		
		FailedTraceVariableCreationException(String errorMessage) {
			super(errorMessage);
		}
	}

	/**
	 * Action to refresh the content of the view.
	 */
	private final class RefreshViewAction extends Action {
		public RefreshViewAction() {
			setText(TracepointsMessages.TraceControlView_action_Refresh_label);
			setImageDescriptor(TracepointImageRegistry.getImageDescriptor(TracepointImageRegistry.ICON_Refresh_enabled));
			setDisabledImageDescriptor(TracepointImageRegistry.getImageDescriptor(TracepointImageRegistry.ICON_Refresh_disabled));
		}
		@Override
		public void run() {
			fLastRefreshTime = System.currentTimeMillis();
			fTraceControlModel.updateContent();
		}
	}

	/**
	 * Action to automatically refresh the content of the view by polling trace-status.
	 */
	protected final class AutoRefreshAction extends Action {
		public AutoRefreshAction() {
			super(TracepointsMessages.TraceControlView_auto_refresh_action_label, AS_CHECK_BOX);
			setImageDescriptor(TracepointImageRegistry.getImageDescriptor(TracepointImageRegistry.ICON_Refresh_Auto));
		}
		@Override
		public void run() {
			if (isChecked()) {
				// Call to updateContent which starts refreshUI job only if necessary
				// (when tracing is running)
				fAutoRefreshEnabled = true;
				fRefreshViewAction.setEnabled(false);
				fTraceControlModel.updateContent();
			} else {
				fAutoRefreshEnabled = false;
				fRefreshViewAction.setEnabled(true);
			}
		}
	}

	/**
	 * Action to refresh the content of the view.
	 */
	protected final class DisconnectedTracingAction extends Action {
		public DisconnectedTracingAction() {
			super(TracepointsMessages.TraceControlView_action_Disconnected_tracing_label, AS_CHECK_BOX);
			setImageDescriptor(TracepointImageRegistry.getImageDescriptor(TracepointImageRegistry.ICON_Disconnected_Tracing));
		}
		@Override
		public void run() {
			fTraceControlModel.setDisconnectedTracing(isChecked());
		}
	}

	protected final class OpenTraceVarDetailsAction extends Action {
		public OpenTraceVarDetailsAction() {
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

	protected final class ExitVisualizationModeDetailsAction extends Action {
		public ExitVisualizationModeDetailsAction() {
			setText(TracepointsMessages.TraceControlView_action_exit_visualization_mode);
			setImageDescriptor(TracepointImageRegistry.getImageDescriptor(TracepointImageRegistry.ICON_Exit_Visualization));
		}
		@Override
		public void run() {
			fTraceControlModel.exitVisualizationMode();
			// Content of view will be updated from the event
			// triggered by this asynchronous operation.
		}
	}
	
	protected TraceControlModel fTraceControlModel;

	protected RefreshViewAction fRefreshViewAction;
	protected boolean fAutoRefreshEnabled;
	protected DisconnectedTracingAction fDisconnectedTracingAction;
	protected OpenTraceVarDetailsAction fOpenTraceVarDetails;
	protected ExitVisualizationModeDetailsAction fExitVisualizationAction;
	protected AutoRefreshAction fAutoRefreshAction;
	protected boolean fTraceVisualization;
	protected Job refreshUIJob;
	protected Font cachedBold;
	protected long fLastRefreshTime;
	protected ITraceStatusDMData2 fLastTraceData;

	protected Composite fTopComposite;
	protected Composite fStatusComposite;
	protected Label fStatusLabel;	
	protected Label fSecondaryStatusLabel;	
	protected Composite fSecondaryStatusComposite;
	protected FlatButton fActionButton;

	protected Composite fBufferComposite;
	protected Label fBufferCollectedFramesLabel;
	protected FlatRadioButton fSetCircularBufferButton;
	protected CircularProgress fBufferProgress;

	protected Composite fFrameComposite;
	protected Label fFrameLabel;
	protected Label fFrameNumberLabel;
	protected Slider fFrameSlider;
	
	protected Composite fNotesComposite;
	protected Label fNotesContentLabel;
	protected Text fNotesContentText;
	protected Button fSetNotesButton;


	public TraceControlView() {
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		fTraceControlModel = new TraceControlModel(this);
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
	}

	@Override
	public void createPartControl(Composite parent) {

		createActions();
		
		fTopComposite = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout(1, false);
		topLayout.marginWidth = 0;
		topLayout.marginHeight = 0;
		fTopComposite.setLayout(topLayout);
		fTopComposite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		// Tracing status line
		createStatusLine(fTopComposite);

		// Secondary status: start time, stop time and reason 
		setSecondaryStatusLineVisible(false, null);

		// Buffer line
		createBufferLine(fTopComposite);
		setBufferLineVisible(false, null, false);
		
		// Frame line
		createFrameLine(fTopComposite);
		setFrameLineVisible(false, null);

		// Trace notes
		//createNotesLine(fTopComposite);
		//setNotesLineVisible(false, null, false);
		
		fTraceControlModel.init();
	}

	protected void createStatusLine(Composite parent) {
		fStatusComposite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 2;
		gd.minimumHeight = 22;
		fStatusComposite.setLayoutData(gd);
		GridLayout l = new GridLayout(2,false);
		l.marginBottom = 0;
		fStatusComposite.setLayout(l);
		fStatusComposite.setBackgroundMode(SWT.INHERIT_FORCE);

		fStatusLabel = new Label(fStatusComposite, SWT.NONE);
		if (cachedBold == null) {
			FontData fontData = fStatusLabel.getFont().getFontData()[0];
			fontData.setStyle(SWT.BOLD);
			cachedBold = new Font(fStatusLabel.getDisplay(),fontData);
		}
		fStatusLabel.setFont(cachedBold);
		GridData d = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		fStatusLabel.setLayoutData(d);
		fStatusLabel.setText(TracepointsMessages.TraceControlView_trace_status_no_debug_session);
		fStatusLabel.setBackground(parent.getBackground());
		
		fActionButton = new FlatButton(fStatusComposite, SWT.NONE);
		fActionButton.setText(EMPTY_STRING);
		GridData acGd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		acGd.horizontalIndent = ACTION_BUTTON_INDENTATION;
		fActionButton.setLayoutData(acGd);
		fActionButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Run action
				IHandlerService handlerService = getSite().getService(IHandlerService.class);
				if (handlerService == null) {
		    		GdbUIPlugin.log(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, "Missing command handler service")); //$NON-NLS-1$
		    		return;
				}

				try {
					String text = ((FlatButton)e.getSource()).getText();
					if (TracepointsMessages.TraceControlView_action_start.equals(text) || 
							TracepointsMessages.TraceControlView_action_restart.equals(text)) {
						handlerService.executeCommand("org.eclipse.cdt.debug.ui.command.startTracing", null); //$NON-NLS-1$
					} else if (TracepointsMessages.TraceControlView_action_stop.equals(text)) {
						handlerService.executeCommand("org.eclipse.cdt.debug.ui.command.stopTracing", null); //$NON-NLS-1$
					} else if (TracepointsMessages.TraceControlView_action_finish_visualization.equals(text)) {
						fTraceControlModel.exitVisualizationMode();					
					}
					// Note that the content of the view will be updated due to the event
					// triggered by the above operations.  There is no point in updating the
					// content ourselves since some of the above calls are asynchronous
					// and have not completed yet.
				} catch (Exception ex) {
					GdbUIPlugin.log(ex);
				}
			}

		});
		
		fSecondaryStatusLabel = new Label(fStatusComposite, SWT.NONE | SWT.WRAP);
		GridData sslGd = new GridData(SWT.FILL, SWT.TOP, true, false);
		sslGd.horizontalSpan = 2;
		fSecondaryStatusLabel.setLayoutData(sslGd);
		fSecondaryStatusLabel.setBackground(parent.getBackground());
		
		Label separator = new Label(fStatusComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData sGd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		//sGd.heightHint = 3;
		sGd.horizontalSpan = 2;
		separator.setLayoutData(sGd);
	}
	
	protected void setActionLinkVisible(boolean visible, String text) {
		fActionButton.setVisible(visible);
		fActionButton.setText(visible ? text: EMPTY_STRING);
	}
	
	private void setSecondaryStatusLineVisible(boolean visible, ITraceStatusDMData2 tData) {
		fSecondaryStatusLabel.setVisible(visible);
		((GridData)fSecondaryStatusLabel.getLayoutData()).exclude = !visible;

		if (visible && tData != null) {
			STOP_REASON_ENUM stopReason = tData.getStopReason();
			if (stopReason != null) {
				fSecondaryStatusLabel.setText(getStopMessage(tData));
			} else if (tData.isTracingActive() && tData.getStartTime() != null) {
				String user = EMPTY_STRING;
				String lastRefreshed = EMPTY_STRING;
				// In case autorefresh is disabled, show when view was manually refreshed last time
				if (!fAutoRefreshEnabled) {
					lastRefreshed = TracepointsMessages.bind(
							TracepointsMessages.TraceControlView_trace_status_secondary_refresh_time,
							formatTimeInterval(fLastRefreshTime, System.currentTimeMillis(), true));
				}
				if (tData.getUserName() != null && tData.getUserName().length() > 0) {
					user = TracepointsMessages.bind(TracepointsMessages.TraceControlView_trace_status_secondary_user, tData.getUserName());
				}
				fSecondaryStatusLabel.setText(TracepointsMessages.bind(
						TracepointsMessages.TraceControlView_trace_status_secondary_running, 
						new Object[] {
								formatTime(tData.getStartTime()),
								user,
								lastRefreshed} 
						));
			} else {
				// Should not happen if usage is correct
				fSecondaryStatusLabel.setText(EMPTY_STRING);
			}
		} else {
			fSecondaryStatusLabel.setText(EMPTY_STRING);
		}
	}
	
	protected void createNotesLine(final Composite parent) {
		// Trace notes: notes text and edit notes button
		fNotesComposite = new Composite(parent,  SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		fNotesComposite.setLayout(layout);
		fNotesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fNotesComposite.setBackgroundMode(SWT.INHERIT_FORCE);

		// Separator on the left of whole notes composite
		Label separator = new Label(fNotesComposite, SWT.SEPARATOR | SWT.VERTICAL);
		GridData slGd = new GridData(SWT.RIGHT, SWT.FILL, false, true);
		//slGd.widthHint = 3;
		slGd.verticalSpan = 4;
		separator.setLayoutData(slGd);
		separator.setBackground(fNotesComposite.getBackground());
		
		Label fNotesLabel = new Label(fNotesComposite, SWT.NONE);
		fNotesLabel.setBackground(parent.getBackground());
		fNotesLabel.setText(TracepointsMessages.TraceControlView_trace_notes_label);
		if (cachedBold != null) {
			fNotesLabel.setFont(cachedBold);
		}
		
		fSetNotesButton = new Button(fNotesComposite, SWT.TOGGLE);
		fSetNotesButton.setImage(TracepointImageRegistry.getImageDescriptor(
				TracepointImageRegistry.ICON_Edit_enabled).createImage());
		fSetNotesButton.setSelection(false);
		fSetNotesButton.setToolTipText(TracepointsMessages.TraceControlView_trace_notes_edit_tooltip);
		fSetNotesButton.setLayoutData(new GridData(SWT.END, SWT.TOP, false, false));
		fSetNotesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleEditNotesButtonPressed();
			}
		});
		
		fNotesContentLabel = new Label(fNotesComposite, SWT.WRAP);
		fNotesContentLabel.setBackground(parent.getBackground());
		fNotesContentLabel.setText(TracepointsMessages.TraceControlView_trace_notes_not_set);
		GridData nclGd = new GridData(SWT.FILL, SWT.TOP, true, false);
		nclGd.horizontalSpan = 2;
		fNotesContentLabel.setLayoutData(nclGd);
		
		fNotesContentText = new Text(fNotesComposite, SWT.BORDER);
		fNotesContentText.setVisible(false);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.horizontalSpan = 2;
		gd.exclude = true;
		fNotesContentText.setLayoutData(gd);
		fNotesContentText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				fSetNotesButton.setSelection(false);
				handleEditNotesButtonPressed();
			}
		});		
		fNotesContentText.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 0x1b) {
					// Esc was pressed, cancel editing
					fSetNotesButton.setSelection(false);
					handleEditNotesButtonPressed(true);
				}
			}
		});
		
	}

	protected void handleEditNotesButtonPressed() {
		handleEditNotesButtonPressed(false);
	}
	
	protected void handleEditNotesButtonPressed(boolean cancelEditing) {
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
			if (!cancelEditing) {
				fNotesContentLabel.setText(fNotesContentText.getText());
				fNotesContentLabel.getSize();
				fTraceControlModel.setTraceNotes(fNotesContentText.getText());
			}
		}
		fNotesComposite.layout();
		fNotesComposite.redraw();
		fTraceControlModel.updateContent();
	}


	protected void setNotesLineVisible(boolean visible, ITraceStatusDMData2 tData, boolean readonly) {
		fNotesComposite.setVisible(visible);
		((GridData)fNotesComposite.getLayoutData()).exclude = !visible;

		if (visible) {
			if (tData.getNotes() != null && tData.getNotes().length() > 0) {
				fNotesContentLabel.setText(removeQuotes(tData.getNotes()));
			} else {
				fNotesContentLabel.setText(TracepointsMessages.TraceControlView_trace_notes_not_set);
			}
			if (tData != null && tData.getStartTime() != null)
			fSetNotesButton.setEnabled(!readonly);
		}
	}

	protected void createFrameLine(Composite parent) {		 
		fFrameComposite = new Composite(parent, SWT.NONE);
		GridData fcGd = new GridData(SWT.FILL, SWT.TOP, true, false);
		fcGd.horizontalSpan = 2;
		fFrameComposite.setLayoutData(fcGd);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		fFrameComposite.setLayout(layout);
		
		Label separator = new Label(fFrameComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData sepGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		sepGd.horizontalSpan = 2;
		sepGd.verticalIndent = 2;
		separator.setLayoutData(sepGd);

		fFrameSlider = new Slider(fFrameComposite, SWT.HORIZONTAL | SWT.BORDER);
		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd2.horizontalSpan = 2;
		fFrameSlider.setLayoutData(gd2);
		fFrameSlider.setValues(0, 0, 100, 1, 1, 10);
		fFrameSlider.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
    			IHandlerService handlerService = getSite().getService(IHandlerService.class);
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
    			} else if (e.detail == SWT.DRAG) {
    				// We don't want to query gdb while user drags thumb, just update the label instead 
    				// but postpone actual gdb query to the time thumb is released (e.detail == SWT.NONE)
    				fFrameNumberLabel.setText(TracepointsMessages.bind(
    						TracepointsMessages.TraceControlView_frame_dragging,
    						fFrameSlider.getSelection()));
    			} else {
    				fTraceControlModel.setCurrentTraceRecord(Integer.toString(fFrameSlider.getSelection()));    				
    			}
			}
			
		});
		
		fFrameLabel = new Label(fFrameComposite, SWT.NONE);
		fFrameLabel.setText(TracepointsMessages.TraceControlView_frame_label);
		fFrameLabel.setLayoutData(new GridData());

		fFrameNumberLabel = new Label(fFrameComposite, SWT.NONE);
		fFrameNumberLabel.setText(TracepointsMessages.TraceControlView_frame_not_looking);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		fFrameNumberLabel.setLayoutData(gd);
	}

	protected void setFrameLineVisible(boolean visible, ITraceStatusDMData2 traceData) {
		fFrameComposite.setVisible(visible);
		((GridData)fFrameComposite.getLayoutData()).exclude = !visible;
		
		if (visible) {
			fFrameSlider.setMinimum(0);
			if (traceData.getNumberOfCollectedFrame() == 0) {
				fFrameSlider.setMaximum(1);
			} else {
				fFrameSlider.setMaximum(traceData.getNumberOfCollectedFrame());
			}
			int inc = traceData.getNumberOfCollectedFrame() / 20;
			fFrameSlider.setPageIncrement(inc <= 1 ? 2 : inc);
			
			String fl = EMPTY_STRING;
			if (traceData.getCurrentTraceFrameId() != null) {
				fl += TracepointsMessages.bind(TracepointsMessages.TraceControlView_frame_looking,
						new Object[] { traceData.getCurrentTraceFrameId(), 
										new Integer(traceData.getTracepointNumberForCurrentTraceFrame())} );
				int recId = 0;
				try {
					recId = Integer.parseInt(traceData.getCurrentTraceFrameId());
				} catch (NumberFormatException e) {
				}
				fFrameSlider.setSelection(recId);
			} else {
				fl += TracepointsMessages.bind(TracepointsMessages.TraceControlView_frame_not_looking, traceData.getNumberOfCollectedFrame());
				fFrameSlider.setSelection(0);
			}
			fFrameNumberLabel.setText(fl);
		}
		if (traceData != null && traceData != null) {
			fFrameSlider.setEnabled(traceData.getNumberOfCollectedFrame() != 0);
		}
	}
	
	protected void createBufferLine(final Composite parent) {
		
		fBufferComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		fBufferComposite.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		fBufferComposite.setLayoutData(gd);
		fBufferComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		Label fBufferLabel = new Label(fBufferComposite, SWT.NONE);
		fBufferLabel.setText(TracepointsMessages.TraceControlView_buffer_label);
		if (cachedBold != null) {
			fBufferLabel.setFont(cachedBold);
		}
		GridData gdBL = new GridData(SWT.FILL, SWT.CENTER, true, false);
		fBufferLabel.setLayoutData(gdBL);
		fBufferLabel.setBackground(fBufferComposite.getBackground());

		fBufferProgress = new CircularProgress(fBufferComposite, SWT.NONE);
		GridData bpGd = new GridData(SWT.CENTER, SWT.TOP, false, false);
		bpGd.verticalSpan = 2;
		fBufferProgress.setLayoutData(bpGd);

		fSetCircularBufferButton = new FlatRadioButton(fBufferComposite, SWT.NONE);
		fSetCircularBufferButton.setText(TracepointsMessages.TraceControlView_buffer_circular_button_label);
		fSetCircularBufferButton.setSelection(false);
		fSetCircularBufferButton.setEnabled(true);
		fSetCircularBufferButton.setToolTipText(TracepointsMessages.TraceControlView_buffer_circular_off_tooltip);
		GridData cbbGd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		cbbGd.horizontalIndent = 20;
		fSetCircularBufferButton.setLayoutData(cbbGd);
		fSetCircularBufferButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fTraceControlModel.setCircularBuffer(fSetCircularBufferButton.getSelection());
				fTraceControlModel.updateContent();
			}
		});
		
		fBufferCollectedFramesLabel = new Label(fBufferComposite, SWT.WRAP);
		fBufferCollectedFramesLabel.setText(TracepointsMessages.TraceControlView_buffer_label);
		GridData gd3 = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		fBufferCollectedFramesLabel.setLayoutData(gd3);
	}

	protected void setBufferLineVisible(boolean visible, ITraceStatusDMData2 tData, boolean readonly) {
		fBufferComposite.setVisible(visible);
		((GridData)fBufferComposite.getLayoutData()).exclude = !visible;

		if (visible && tData != null){
			if (tData.getStopReason() != null && tData.getStopReason() == STOP_REASON_ENUM.OVERFLOW){
				// Buffer overflowed, it should be 100% full instead of 99%
				fBufferProgress.setProgress(100);
			} else if (tData.getStopReason() != null && tData.isCircularBuffer() && tData.getNumberOfCreatedFrames() > tData.getNumberOfCollectedFrame()) {
				// Buffer is circular and overflowed once, it should be 100% full instead of 99%
				fBufferProgress.setProgress(100);
			} else if (tData.isCircularBuffer() && tData.isTracingActive() && 
					tData.getNumberOfCreatedFrames() > tData.getNumberOfCollectedFrame()) {
				// If we run with Circular buffer and all buffer was filled in once, we continue displaying progress 100%
				// and showing moving bar that makes a circle every tData.getNumberOfCollectedFrame() because it is buffer size in frames
				// and actual number of collected frames from the start is tData.getNumberOfCreatedFrames(), but only last 
				// tData.getNumberOfCollectedFrame() are stored in the buffer
				int p = (tData.getNumberOfCreatedFrames() % tData.getNumberOfCollectedFrame()) * 100 / tData.getNumberOfCollectedFrame();
				// 100 is an indicator that buffer is already full and should be showed in different manner
				fBufferProgress.setProgress(100 + p);
			} else {
				fBufferProgress.setProgress((tData.getTotalBufferSize() - tData.getFreeBufferSize()) * 100 / tData.getTotalBufferSize());
			}
			
			fSetCircularBufferButton.setSelection(tData.isCircularBuffer());
			fSetCircularBufferButton.setEnabled(!readonly);
			fSetCircularBufferButton.setToolTipText(fSetCircularBufferButton.getSelection() ? 
			                                     		TracepointsMessages.TraceControlView_buffer_circular_on_tooltip : 
					                                    TracepointsMessages.TraceControlView_buffer_circular_off_tooltip);
			fSetCircularBufferButton.redraw();
			fSetCircularBufferButton.update();
			
			fBufferCollectedFramesLabel.setText(TracepointsMessages.bind(
					TracepointsMessages.TraceControlView_buffer_frames_collected, 
					tData.getNumberOfCollectedFrame(),
					(tData.getTotalBufferSize() - tData.getFreeBufferSize())/1000));
			fBufferProgress.redraw();
			fBufferProgress.update();
		}
	}
	
	protected void createActions() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		window.getActivePage().showActionSet("org.eclipse.cdt.debug.ui.tracepointActionSet"); //$NON-NLS-1$
		
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();

		// Create the action to refresh the view
		fRefreshViewAction = new RefreshViewAction();
		bars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fRefreshViewAction);
		manager.add(fRefreshViewAction);
		fRefreshViewAction.setEnabled(false);

		fAutoRefreshAction = new AutoRefreshAction();
		manager.add(fAutoRefreshAction);
		fAutoRefreshAction.setChecked(true);
		fAutoRefreshAction.setEnabled(true);
		fAutoRefreshEnabled = true;
		fLastRefreshTime = System.currentTimeMillis();

		fDisconnectedTracingAction = new DisconnectedTracingAction();
		manager.add(fDisconnectedTracingAction);
		fDisconnectedTracingAction.setEnabled(false);
		
		// Create the action to open the trace variable details
		fOpenTraceVarDetails = new OpenTraceVarDetailsAction();
		manager.add(fOpenTraceVarDetails);
		
		// Create the action to exit visualization mode
		fExitVisualizationAction = new ExitVisualizationModeDetailsAction();
		manager.add(fExitVisualizationAction);

		bars.updateActionBars();
		updateActionEnablement(null);
	}
	
	@Override
	public void dispose() {
		fTraceControlModel.dispose();

		fStatusLabel = null;  // Indicate that we have been disposed
		if (refreshUIJob != null) {
			refreshUIJob.cancel();
		}
		
		if (cachedBold != null) { 
			cachedBold.dispose();
			cachedBold = null;
		}
		
		super.dispose();
	}
	
	protected void updateUI(final String statusMessage) {
		try {
			fLastTraceData = null;
			setSecondaryStatusLineVisible(false, null);
			//setNotesLineVisible(false, null, false);
			setFrameLineVisible(false, null);
			setBufferLineVisible(false, null, false);
			fStatusLabel.setText(statusMessage);
			setActionLinkVisible(false, EMPTY_STRING);
			updateActionEnablement(null);
			updateLayout();
			fDisconnectedTracingAction.setEnabled(false);
		} catch (SWTException ex) {
		}
	}

	protected void updateUI(final ITraceStatusDMData2 traceData) {
		fLastTraceData = traceData;
		if (traceData == null ) {
			// should not happen, but still process it correctly
			updateUI(TracepointsMessages.TraceControlView_trace_status_inactive);
		} else if (!traceData.isTracingSupported()) {
			updateUI(TracepointsMessages.TraceControlView_trace_status_not_supported);
		} else if (traceData.isTracingFromFile()) {
			// Off-line tracing from data file
			fDisconnectedTracingAction.setEnabled(false);
			String s = TracepointsMessages.TraceControlView_trace_status_offline;
			fStatusLabel.setText(s);
			setActionLinkVisible(false, EMPTY_STRING);

			// If start and stop time are not available in trace data file, do not show secondary status line 
			if (getTimeMilliseconds(traceData.getStartTime()) != 0 && getTimeMilliseconds(traceData.getStopTime()) != 0) {
				setSecondaryStatusLineVisible(true, traceData);
			} else {
				setSecondaryStatusLineVisible(false, traceData);
			}
			//setNotesLineVisible(true, traceData, true);			
			setBufferLineVisible(true, traceData, true);
			setFrameLineVisible(true, traceData);	    			
	    			
			updateActionEnablement(traceData);
			updateLayout();
			
		} else if (!traceData.isTracingActive() && traceData.getStopReason() == null){
			// Tracing is not started yet
			fDisconnectedTracingAction.setEnabled(true);
			fStatusLabel.setText(TracepointsMessages.TraceControlView_trace_status_not_started);
			setActionLinkVisible(true,TracepointsMessages.TraceControlView_action_start);

			setSecondaryStatusLineVisible(false, null);
			//setNotesLineVisible(true, traceData, false);  			
			setBufferLineVisible(true, traceData, false);
			setFrameLineVisible(false, null);

			updateActionEnablement(traceData);
	    	updateLayout();
			
		} else {
			// Live execution tracing started and running or started and stopped
			fDisconnectedTracingAction.setEnabled(true);

			// If stopped, stop reason, time and note.
			STOP_REASON_ENUM fStopReason = traceData.getStopReason();
			if (fStopReason != null) {
				// Tracing has stopped we need notes, secondary status line, and frames slider
				setSecondaryStatusLineVisible(true, traceData);
				//setNotesLineVisible(true, traceData, false);

				setFrameLineVisible(true, traceData);
				setBufferLineVisible(true, traceData, false);
				if (traceData.getCurrentTraceFrameId() != null) {
					fStatusLabel.setText(TracepointsMessages.TraceControlView_trace_status_visualization);
					setActionLinkVisible(true, TracepointsMessages.TraceControlView_action_finish_visualization);
				} else {
					if (traceData.getNumberOfCollectedFrame() == 0) {
						fStatusLabel.setText(TracepointsMessages.TraceControlView_trace_status_not_started);
					} else {
						fStatusLabel.setText(TracepointsMessages.TraceControlView_trace_status_stopped);						
					}
					setActionLinkVisible(true, TracepointsMessages.TraceControlView_action_restart);
				}
			} else {
				// Tracing is running, don't show stop reason line, stop notes and frames line.
				String s = TracepointsMessages.TraceControlView_trace_status_in_progress;
				fStatusLabel.setText(s);
				setActionLinkVisible(true, TracepointsMessages.TraceControlView_action_stop);
				setSecondaryStatusLineVisible(true, traceData);
				//setNotesLineVisible(true, traceData, false);
				setFrameLineVisible(false, traceData);
				setBufferLineVisible(true, traceData, true);
				startRefreshUIJob();
			}

			updateActionEnablement(traceData);
			updateLayout();
		}
	}
		
	protected void startRefreshUIJob() {
		if (refreshUIJob == null) {
			refreshUIJob = new UIJob("Refresh Trace Control view UI") { //$NON-NLS-1$
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					// Run on the UI thread to avoid synchronization
					if (fAutoRefreshEnabled) {
						fTraceControlModel.updateContent();
					} else {
						// Update the UI to simply say how long ago was the last refresh
						updateUI(fLastTraceData);
					}
					
					return Status.OK_STATUS;
				}
			};
		}
		refreshUIJob.schedule(UI_REFRESH_DELAY);
	}
		
	protected String getStopMessage(ITraceStatusDMData2 tData) {
		String stopMessage;
		STOP_REASON_ENUM fStopReason = tData.getStopReason();
		if (fStopReason == STOP_REASON_ENUM.REQUEST) {
			stopMessage = TracepointsMessages.TraceControlView_tracing_stopped_user_request;
		} else if (fStopReason == STOP_REASON_ENUM.PASSCOUNT) {
			if (tData.getStoppingTracepoint() != null) {
				stopMessage = TracepointsMessages.bind(TracepointsMessages.TraceControlView_tracing_stopped_tracepoint_number, tData.getStoppingTracepoint());
			} else {
				stopMessage = TracepointsMessages.TraceControlView_tracing_stopped_passcount;
			}
		} else if (fStopReason == STOP_REASON_ENUM.OVERFLOW) {
			stopMessage = TracepointsMessages.TraceControlView_tracing_stopped_buffer_full;
		} else if (fStopReason == STOP_REASON_ENUM.DISCONNECTION) {
			stopMessage = TracepointsMessages.TraceControlView_tracing_stopped_disconnection;
		} else if (fStopReason == STOP_REASON_ENUM.ERROR) {
			stopMessage = TracepointsMessages.TraceControlView_tracing_stopped_error;
		} else {
			stopMessage = TracepointsMessages.TraceControlView_tracing_stopped_unknown;
		}

		String user = EMPTY_STRING;
		if (tData.getUserName() != null && tData.getUserName().length() > 0) {
			user = TracepointsMessages.bind(TracepointsMessages.TraceControlView_trace_status_secondary_user, tData.getUserName());
		}
		if (tData.isTracingFromFile()) {
			stopMessage = TracepointsMessages.bind(
					TracepointsMessages.TraceControlView_trace_status_secondary_offline, 
					new Object[] {formatTime(tData.getStartTime()),
					user,
					formatTime(tData.getStopTime()),
					stopMessage
					});	
		} else {
			stopMessage = TracepointsMessages.bind(
					TracepointsMessages.TraceControlView_trace_status_secondary_stopped, 
					new Object[] {formatTimeInterval(tData.getStartTime(),tData.getStopTime()),
					user,
					formatTime(tData.getStopTime()),
					stopMessage
					});	
		}
		
		return stopMessage;
	}

	protected void updateActionEnablement(ITraceStatusDMData2 traceData) {
		fOpenTraceVarDetails.setEnabled(traceData != null && traceData.isTracingSupported());
		fExitVisualizationAction.setEnabled(traceData != null && traceData.getCurrentTraceFrameId() != null && 
				                            !traceData.isTracingFromFile());
		fDisconnectedTracingAction.setChecked(traceData != null && traceData.isDisconnectedTracingEnabled());
	}
		
	@Override
	public void setFocus() {
		if (fStatusLabel != null) {
			fStatusLabel.setFocus();
		}
	}
	
	public void updateLayout() {
		fStatusComposite.layout(true);
		fTopComposite.layout(true);
	}
	
	protected long getTimeMilliseconds(String time) {
		long microseconds = 0;
		try { 
			if (time.length() != 0) {
				String[] times = time.split("\\.");  //$NON-NLS-1$
				microseconds += Long.parseLong(times[0]) * 1000000;
				microseconds += Long.parseLong(times[1]);
			}
		} catch (NumberFormatException ex) {
			GdbPlugin.log(ex);
		}
		return microseconds / 1000;
	}

	/** 
	 * Format time from gdb presentation into user-understandable form
	 * @param time in gd presentation
	 * @return 
	 */
	protected String formatTime(String time) {
		long milliseconds = getTimeMilliseconds(time);
		return formatTime(milliseconds);
	}

	/** 
	 * Format time from standard milliseconds since Epoch into user-understandable form
	 */
	protected String formatTime(long milliseconds) {
		Date date = new Date(milliseconds);
		long currentTime = System.currentTimeMillis();
		long days = TimeUnit.MILLISECONDS.toDays(currentTime - milliseconds);
		if (days == 0) {
			// today
			return TracepointsMessages.bind(TracepointsMessages.TraceControlView_today, DateFormat.getTimeInstance(DateFormat.SHORT).format(date));
		} else if (days == 1) {
			// yesterday
			return TracepointsMessages.bind(TracepointsMessages.TraceControlView_yesterday, DateFormat.getTimeInstance(DateFormat.SHORT).format(date));
		}
		
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
	}

	/**
	 * Format time interval returned by trace status command into human-readable 
	 */
	protected String formatTimeInterval(String startTime, String stopTime) {
		long startMicroseconds = 0;
		long stopMicroseconds = 0;
		try { 
			if (startTime.length() != 0) {
				String[] times = startTime.split("\\.");  //$NON-NLS-1$
				startMicroseconds += Long.parseLong(times[0]) * 1000000;
				startMicroseconds += Long.parseLong(times[1]);
			}
			if (stopTime.length() != 0) {
				String[] times = stopTime.split("\\.");  //$NON-NLS-1$
				stopMicroseconds += Long.parseLong(times[0]) * 1000000;
				stopMicroseconds += Long.parseLong(times[1]);
			}
			return formatTimeInterval(startMicroseconds/1000, stopMicroseconds/1000, true);
		} catch (NumberFormatException ex) {
			GdbPlugin.log(ex);
		}
		return EMPTY_STRING;
	}
	
	/**
	 * Format time interval returned by trace status command into human-readable 
	 */
	protected String formatTimeInterval(long startMilliseconds, long stopMilliseconds, boolean shortForm) {
		long millis = stopMilliseconds - startMilliseconds;
		long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);        
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        
        StringBuilder sb = new StringBuilder(64);
        if (!shortForm) {
	        if (days != 0) sb.append(days + TracepointsMessages.TraceControlView_date_days);
	        if (hours != 0) sb.append(hours + TracepointsMessages.TraceControlView_date_hours);
	        if (minutes != 0) sb.append(minutes + TracepointsMessages.TraceControlView_date_minutes);
	        if (seconds != 0) sb.append(seconds + TracepointsMessages.TraceControlView_date_seconds);
	        if (sb.length() == 0) sb.append(TracepointsMessages.TraceControlView_date_zero);
        } else {
	        if (days != 0) sb.append(days + TracepointsMessages.TraceControlView_date_short_days);
	        if (hours != 0) sb.append(hours + TracepointsMessages.TraceControlView_date_short_hours);
	        if (minutes != 0) sb.append(minutes + TracepointsMessages.TraceControlView_date_short_minutes);
	        if (seconds != 0) sb.append(seconds + TracepointsMessages.TraceControlView_date_short_seconds);
	        if (sb.length() == 0) sb.append(TracepointsMessages.TraceControlView_date_short_zero);

        }
        return(sb.toString());
	}

	/** 
	 * GDB's set trace-user and set trace-notes commands require quotes if argument contains spaces,
	 * but these quotes are returned by trace status, to workaround this we remove quotes on UI side 
	 */
	protected String removeQuotes(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {  //$NON-NLS-1$//$NON-NLS-2$
			return s.substring(1, s.length()-1);
		} else {
			return s;
		}
	}
}
