/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepointactions;

import java.util.Vector;

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.breakpointactions.IBreakpointActionPage;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.CollectAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.EvaluateAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.ITracepointAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.WhileSteppingAction;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @since 2.1
 */
public class TracepointActionDialog extends Dialog {

	public static final String BREAKPOINT_ACTION_PAGE_EXTENSION_POINT_ID = "BreakpointActionPage"; //$NON-NLS-1$

	private static final String ACTION_PAGE_ELEMENT = "actionPage"; //$NON-NLS-1$

	private static final String TRACEPOINT_ACTION_DIALOG_LAST_SELECTED = "TracepointActionDialog.lastSelectedAction"; //$NON-NLS-1$

	private static final int TRACEPOINT_ACTIONS_COUNT = 3;
	
	private Composite actionArea; 	
	private Composite[] actionComposites;
	private ITracepointAction tracepointAction;
	private IBreakpointActionPage actionPage;
	private Vector<ITracepointAction> tracepointActions;
	private IBreakpointActionPage[] actionPages;
	private String actionName;
	private Text actionNameTextWidget;
	private Combo combo;
	private Composite dialogArea;
	private int lastSelectedActionTypeIndex;
	private IBreakpointAction originalAction;
	private boolean isSubAction;

	private IExtension[] breakpointActionPageExtensions;
	
	private static final Point MINIMUM_SIZE = new Point(440, 540);

	/**
	 * Create the dialog
	 */
	public TracepointActionDialog(Shell parentShell, ITracepointAction action, boolean isSub) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
		originalAction = action;
		tracepointAction = action;
		lastSelectedActionTypeIndex = 0;
		isSubAction = isSub;
	}

	@Override
	protected void cancelPressed() {
		actionPage.actionDialogCanceled();
		super.cancelPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (originalAction == null)
			newShell.setText(MessagesForTracepointActions.TracepointActions_ActionDialog_New);
		else
			newShell.setText(originalAction.getName());
	}

	/**
	 * Create contents of the button bar
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Create contents of the dialog
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		dialogArea = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		dialogArea.setLayout(gridLayout);

		final Label actionNameLabel = new Label(dialogArea, SWT.NONE);
		actionNameLabel.setText(MessagesForTracepointActions.TracepointActions_ActionDialog_Name);

		actionNameTextWidget = new Text(dialogArea, SWT.BORDER);
		actionNameTextWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label breakpointActionTypeLabel = new Label(dialogArea, SWT.NONE);
		breakpointActionTypeLabel.setText(MessagesForTracepointActions.TracepointActions_ActionDialog_Type);

		combo = new Combo(dialogArea, SWT.READ_ONLY);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				try {
					showActionComposite();
					Point p = parent.computeSize(MINIMUM_SIZE.x, MINIMUM_SIZE.y);
					getShell().setSize(getShell().computeSize(p.x, p.y));
				} catch (CoreException e1) {
				}
			}
		});
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		tracepointActions = new Vector<ITracepointAction>(TRACEPOINT_ACTIONS_COUNT);
		tracepointActions.add(new CollectAction());
		tracepointActions.add(new EvaluateAction());
		// Sub actions of whileStepping cannot be whileStepping
		if (!isSubAction) tracepointActions.add(new WhileSteppingAction());
		
		actionPages = new IBreakpointActionPage[TRACEPOINT_ACTIONS_COUNT];
		actionComposites = new Composite[TRACEPOINT_ACTIONS_COUNT];

		if (tracepointActions.size() > 0) {

			String lastTypeName = GdbUIPlugin.getDefault().getPreferenceStore().getString(TRACEPOINT_ACTION_DIALOG_LAST_SELECTED);

			if (tracepointAction != null) {
				lastTypeName = tracepointAction.getTypeName();
				actionName = tracepointAction.getName();
			}

			for (int i = 0; i < tracepointActions.size(); i++) {
				tracepointActions.get(i).setName(tracepointActions.get(i).getDefaultName());
				String actionTypeName =  tracepointActions.get(i).getTypeName();
				combo.add(actionTypeName);
				if (actionTypeName.equals(lastTypeName)) {
					lastSelectedActionTypeIndex = i;
					if (tracepointAction != null) {
						tracepointActions.add(i, tracepointAction);
						tracepointActions.remove(i+1);
					}
				}
			}
			
			combo.select(lastSelectedActionTypeIndex);
			if (originalAction != null)
				combo.setEnabled(false);

			actionArea = new Composite(dialogArea, SWT.NONE);
			actionArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			actionArea.setLayout(new StackLayout());
			try {
				showActionComposite();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return dialogArea;
	}

	public IBreakpointAction getTracepointAction() {
		return tracepointAction;
	}

	public String getActionName() {
		return actionName;
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return MINIMUM_SIZE;
	}

	@Override
	protected void okPressed() {
		if (originalAction == null)
			GdbUIPlugin.getDefault().getPreferenceStore().setValue(TRACEPOINT_ACTION_DIALOG_LAST_SELECTED, tracepointAction.getTypeName());
		String newName = actionNameTextWidget.getText();
		if (originalAction == null || !originalAction.getName().equals(newName)) {
			actionName = TracepointActionManager.getInstance().makeUniqueActionName(newName);
			tracepointAction.setName(actionName);
		}
		actionPage.actionDialogOK();
		super.okPressed();
	}

	void showActionComposite() throws CoreException {
		// Find the selected extension
		int selectedTypeIndex = combo.getSelectionIndex();
		lastSelectedActionTypeIndex = selectedTypeIndex;
		tracepointAction = tracepointActions.get(selectedTypeIndex);

		actionPage = actionPages[selectedTypeIndex];
		if (actionPage == null) {
			actionPages[selectedTypeIndex] = getActionPage(tracepointActions.get(selectedTypeIndex));
			actionPage = actionPages[selectedTypeIndex];
		}
		if (actionComposites[selectedTypeIndex] == null) {
			Composite actionComposite = actionPages[selectedTypeIndex].createComposite(tracepointAction, actionArea, SWT.NONE);
			actionComposites[selectedTypeIndex] = actionComposite;
		}
		actionName = tracepointAction.getName();

		actionNameTextWidget.setText(actionName);
		StackLayout stacklayout = (StackLayout) actionArea.getLayout();
		stacklayout.topControl = actionComposites[selectedTypeIndex];
		actionArea.layout();
	}

	public IExtension[] getBreakpointActionPageExtensions() {
		if (breakpointActionPageExtensions == null) {
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CDebugUIPlugin.PLUGIN_ID, BREAKPOINT_ACTION_PAGE_EXTENSION_POINT_ID);
			if (point == null)
				breakpointActionPageExtensions = new IExtension[0];
			else {
				breakpointActionPageExtensions = point.getExtensions();
			}
		}

		return breakpointActionPageExtensions;
	}

	private IBreakpointActionPage getActionPage(IBreakpointAction breakpointAction) {
		IExtension[] actionExtensions = getBreakpointActionPageExtensions();

		IBreakpointActionPage actionPageResult = null;
		try {

			for (int i = 0; i < actionExtensions.length && actionPageResult == null; i++) {
				IConfigurationElement[] elements = actionExtensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length && actionPageResult == null; j++) {
					IConfigurationElement element = elements[j];
					if (element.getName().equals(ACTION_PAGE_ELEMENT)) {
						if (element.getAttribute("actionType").equals(breakpointAction.getIdentifier())) { //$NON-NLS-1$
							actionPageResult = (IBreakpointActionPage) element.createExecutableExtension("class"); //$NON-NLS-1$
						}
					}
				}
			}

		} catch (CoreException e) {
		}
		return actionPageResult;
	}

}
