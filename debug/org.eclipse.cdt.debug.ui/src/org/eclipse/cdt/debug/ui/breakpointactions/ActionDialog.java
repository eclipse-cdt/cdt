/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
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

public class ActionDialog extends Dialog {

	public static final String BREAKPOINT_ACTION_PAGE_EXTENSION_POINT_ID = "BreakpointActionPage"; //$NON-NLS-1$
	
	public static final String ACTION_PAGE_ELEMENT = "actionPage"; //$NON-NLS-1$

	private static final String ACTION_DIALOG_LAST_SELECTED = "ActionDialog.lastSelectedAction"; //$NON-NLS-1$

	private Composite actionArea;
	private Composite[] actionComposites;
	private IBreakpointAction breakpointAction;
	private IBreakpointActionPage actionPage;
	private IBreakpointAction[] breakpointActions;
	private IBreakpointActionPage[] actionPages;
	private String actionName;
	private Text actionNameTextWidget;
	private Combo combo;
	private Composite dialogArea;
	private int lastSelectedActionTypeIndex;
	private IBreakpointAction originalAction;

	private IExtension[] breakpointActionPageExtensions;

	/**
	 * Create the dialog
	 * 
	 * @param parentShell
	 */
	public ActionDialog(Shell parentShell, IBreakpointAction action) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
		this.originalAction = action;
		this.breakpointAction = action;
		lastSelectedActionTypeIndex = 0;
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
			newShell.setText(Messages.getString("ActionDialog.0")); //$NON-NLS-1$
		else
			newShell.setText(originalAction.getName());
	}

	/**
	 * Create contents of the button bar
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Create contents of the dialog
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		dialogArea = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		dialogArea.setLayout(gridLayout);

		final Label actionNameLabel = new Label(dialogArea, SWT.NONE);
		actionNameLabel.setText(Messages.getString("ActionDialog.1")); //$NON-NLS-1$

		actionNameTextWidget = new Text(dialogArea, SWT.BORDER);
		actionNameTextWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label breakpointActionTypeLabel = new Label(dialogArea, SWT.NONE);
		breakpointActionTypeLabel.setText(Messages.getString("ActionDialog.2")); //$NON-NLS-1$

		combo = new Combo(dialogArea, SWT.READ_ONLY);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				try {
					showActionComposite();
				} catch (CoreException e1) {
				}
			}
		});
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		//

		IExtension[] actionExtensions = CDebugCorePlugin.getDefault().getBreakpointActionManager().getBreakpointActionExtensions();

		breakpointActions = new IBreakpointAction[actionExtensions.length];
		actionPages = new IBreakpointActionPage[actionExtensions.length];
		actionComposites = new Composite[actionExtensions.length];

		if (actionExtensions.length > 0) {

			String lastTypeName = CDebugUIPlugin.getDefault().getPreferenceStore().getString(ACTION_DIALOG_LAST_SELECTED);

			if (breakpointAction != null) {
				lastTypeName = breakpointAction.getTypeName();
				actionName = breakpointAction.getName();
			}

			for (int i = 0; i < actionExtensions.length; i++) {
				IConfigurationElement[] elements = actionExtensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					IConfigurationElement element = elements[j];
					if (element.getName().equals(CDebugCorePlugin.ACTION_TYPE_ELEMENT)) {
						String actionTypeName = element.getAttribute("name"); //$NON-NLS-1$
						combo.add(actionTypeName);
						if (actionTypeName.equals(lastTypeName))
							lastSelectedActionTypeIndex = i;
					}
				}
			}

			combo.select(lastSelectedActionTypeIndex);
			if (originalAction != null)
				combo.setEnabled(false);

			breakpointActions[combo.getSelectionIndex()] = breakpointAction;

			actionArea = new Composite(dialogArea, SWT.NONE);
			actionArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			actionArea.setLayout(new StackLayout());
			try {
				showActionComposite();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return dialogArea;
	}

	public IBreakpointAction getBreakpointAction() {
		return breakpointAction;
	}

	public String getActionName() {
		return actionName;
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 375);
	}

	@Override
	protected void okPressed() {
		if (originalAction == null)
			CDebugUIPlugin.getDefault().getPreferenceStore().setValue(ACTION_DIALOG_LAST_SELECTED, breakpointAction.getTypeName());
		String newName = actionNameTextWidget.getText();
		if (originalAction == null || !originalAction.getName().equals(newName)) {
			actionName = CDebugCorePlugin.getDefault().getBreakpointActionManager().makeUniqueActionName(newName);
			breakpointAction.setName(actionName);
		}
		actionPage.actionDialogOK();
		super.okPressed();
	}

	void showActionComposite() throws CoreException {
		// Find the selected extension
		int selectedTypeIndex = combo.getSelectionIndex();
		lastSelectedActionTypeIndex = selectedTypeIndex;
		breakpointAction = breakpointActions[selectedTypeIndex];
		if (breakpointAction == null) {
			int elementCount = 0;
			IConfigurationElement selectedElement = null;

			IExtension[] actionExtensions = CDebugCorePlugin.getDefault().getBreakpointActionManager().getBreakpointActionExtensions();

			for (int i = 0; i < actionExtensions.length && selectedElement == null; i++) {
				IConfigurationElement[] elements = actionExtensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length && selectedElement == null; j++) {
					IConfigurationElement element = elements[j];
					if (element.getName().equals(CDebugCorePlugin.ACTION_TYPE_ELEMENT)) {
						if (elementCount == selectedTypeIndex)
							selectedElement = element;
						elementCount++;
					}
				}
			}

			breakpointAction = (IBreakpointAction) selectedElement.createExecutableExtension("class"); //$NON-NLS-1$
			breakpointAction.setName(breakpointAction.getDefaultName());
			breakpointActions[selectedTypeIndex] = breakpointAction;
		}
		actionPage = actionPages[selectedTypeIndex];
		if (actionPage == null) {
			actionPages[selectedTypeIndex] = getActionPage(breakpointActions[selectedTypeIndex]);
			actionPage = actionPages[selectedTypeIndex];
		}
		if (actionComposites[selectedTypeIndex] == null) {
			Composite actionComposite = actionPages[selectedTypeIndex].createComposite(breakpointAction, actionArea, SWT.NONE);
			actionComposites[selectedTypeIndex] = actionComposite;
		}
		actionName = breakpointAction.getName();

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return actionPageResult;
	}

}
