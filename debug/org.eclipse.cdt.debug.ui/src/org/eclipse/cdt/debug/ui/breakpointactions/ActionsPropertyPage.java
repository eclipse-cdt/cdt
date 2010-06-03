/*******************************************************************************
 * Copyright (c) 2007, 2010 Nokia and others.
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
import org.eclipse.cdt.debug.core.breakpointactions.BreakpointActionManager;
import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

public class ActionsPropertyPage extends PropertyPage {

	private ActionsList actionsList;
	private IMarker breakpointMarker;
	private GlobalActionsList globalActionsList;
	private String savedActionNames;

	public ActionsPropertyPage() {
		super();
	}

	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		IBreakpoint breakpoint = (IBreakpoint) this.getElement().getAdapter(org.eclipse.debug.core.model.IBreakpoint.class);
		breakpointMarker = breakpoint.getMarker();
		savedActionNames = breakpointMarker.getAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, ""); //$NON-NLS-1$

		final Label actionsTriggeredWhenLabel = new Label(container, SWT.NONE);
		final GridData gridData_2 = new GridData();
		gridData_2.horizontalSpan = 2;
		actionsTriggeredWhenLabel.setLayoutData(gridData_2);
		actionsTriggeredWhenLabel.setText(Messages.getString("ActionsPropertyPage.1")); //$NON-NLS-1$

		actionsList = new ActionsList(container, SWT.NONE);
		final GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		actionsList.setLayoutData(gridData);

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		final GridData gridData_4 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData_4.horizontalSpan = 2;
		label.setLayoutData(gridData_4);

		final Label allAvailableActionsLabel = new Label(container, SWT.NONE);
		final GridData gridData_3 = new GridData();
		gridData_3.horizontalSpan = 2;
		allAvailableActionsLabel.setLayoutData(gridData_3);
		allAvailableActionsLabel.setText(Messages.getString("ActionsPropertyPage.2")); //$NON-NLS-1$

		globalActionsList = new GlobalActionsList(container, SWT.NONE, true);
		final GridData gridData_1 = new GridData(GridData.FILL_BOTH);
		gridData_1.horizontalSpan = 2;
		globalActionsList.setLayoutData(gridData_1);
		//

		String actionNames = breakpointMarker.getAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, ""); //$NON-NLS-1$
		actionsList.setNames(actionNames);

		globalActionsList.getAttachButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				HandleAttachButton();
			}
		});

		globalActionsList.getDeleteButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				HandleDeleteButton();
			}
		});

		return container;
	}

	protected void HandleAttachButton() {

		IBreakpointAction[] selectedActions = globalActionsList.getSelectedActions();
		for (int i = 0; i < selectedActions.length; i++) {
			actionsList.addAction(selectedActions[i]);
		}
	}

	/**
	 * Clean up attached actions that were just deleted from the GlobalActionList
	 * 
	 * @since 7.0
	 */
	protected void HandleDeleteButton() {

		// First remove any attached action that was just deleted
		IBreakpointAction[] selectedActions = globalActionsList.getSelectedActions();
		for (int i = 0; i < selectedActions.length; i++) {
			actionsList.removeAction(selectedActions[i]);
		}
		// Now cleanup the global action list
		globalActionsList.HandleDeleteButton();
	}

	protected void performDefaults() {
		try {
			breakpointMarker.setAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, ""); //$NON-NLS-1$
			actionsList.setNames(""); //$NON-NLS-1$
		} catch (CoreException e) {
		}
		super.performDefaults();
	}

	public boolean performCancel() {
		try {
			breakpointMarker.setAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, savedActionNames);
			CDebugCorePlugin.getDefault().getBreakpointActionManager().revertActionData();
		} catch (CoreException e) {
		}
		return super.performCancel();
	}

	public boolean performOk() {
		try {
			CDebugCorePlugin.getDefault().getBreakpointActionManager().saveActionData();
			breakpointMarker.setAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, actionsList.getActionNames());
		} catch (CoreException e) {
		}
		return super.performOk();
	}

}
