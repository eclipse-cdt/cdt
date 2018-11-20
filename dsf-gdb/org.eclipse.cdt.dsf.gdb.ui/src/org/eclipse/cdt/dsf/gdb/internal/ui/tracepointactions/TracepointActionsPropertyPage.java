/*******************************************************************************
 * Copyright (c) 2010, 2015 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepointactions;

import org.eclipse.cdt.debug.core.breakpointactions.BreakpointActionManager;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.ITracepointAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
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

/**
 * Property page for Tracepoint actions
 *
 * @since 2.1
 */
public class TracepointActionsPropertyPage extends PropertyPage {

	private TracepointActionsList actionsList;
	private IMarker tracepointMarker;
	private TracepointGlobalActionsList globalActionsList;
	private String savedActionNames;

	public TracepointActionsPropertyPage() {
		super();
	}

	@Override
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		IBreakpoint tracepoint = this.getElement().getAdapter(org.eclipse.debug.core.model.IBreakpoint.class);
		tracepointMarker = tracepoint.getMarker();
		savedActionNames = tracepointMarker.getAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, ""); //$NON-NLS-1$

		final Label actionsTriggeredWhenLabel = new Label(container, SWT.NONE);
		final GridData gridData_2 = new GridData();
		gridData_2.horizontalSpan = 2;
		actionsTriggeredWhenLabel.setLayoutData(gridData_2);
		actionsTriggeredWhenLabel.setText(MessagesForTracepointActions.TracepointActions_Actions_for_this_tracepoint);

		actionsList = new TracepointActionsList(container, SWT.NONE);
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
		allAvailableActionsLabel.setText(MessagesForTracepointActions.TracepointActions_Available_actions);

		globalActionsList = new TracepointGlobalActionsList(container, SWT.NONE, true, null, false);
		final GridData gridData_1 = new GridData(GridData.FILL_BOTH);
		gridData_1.horizontalSpan = 2;
		globalActionsList.setLayoutData(gridData_1);

		String actionNames = tracepointMarker.getAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, ""); //$NON-NLS-1$
		actionsList.setNames(actionNames);

		// connect attached actions list to global list
		globalActionsList.setClientList(actionsList);

		globalActionsList.getAttachButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HandleAttachButton();
			}
		});

		globalActionsList.getDeleteButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HandleDeleteButton();
			}
		});

		return container;
	}

	protected void HandleAttachButton() {

		ITracepointAction[] selectedActions = globalActionsList.getSelectedActions();
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
		// attached actions are now handled by the GlobalActionsList

		globalActionsList.HandleDeleteButton();
	}

	@Override
	protected void performDefaults() {
		try {
			tracepointMarker.setAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, ""); //$NON-NLS-1$
			actionsList.setNames(""); //$NON-NLS-1$
		} catch (CoreException e) {
		}
		super.performDefaults();
	}

	@Override
	public boolean performCancel() {
		try {
			tracepointMarker.setAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, savedActionNames);
			TracepointActionManager.getInstance().revertActionData();
		} catch (CoreException e) {
		}
		return super.performCancel();
	}

	@Override
	public boolean performOk() {
		try {
			TracepointActionManager.getInstance().saveActionData();
			tracepointMarker.setAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE,
					actionsList.getActionNames());
		} catch (CoreException e) {
		}
		return super.performOk();
	}
}
