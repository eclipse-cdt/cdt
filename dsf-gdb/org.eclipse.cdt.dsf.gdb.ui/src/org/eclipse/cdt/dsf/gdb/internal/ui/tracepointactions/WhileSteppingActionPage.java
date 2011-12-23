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
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepointactions;

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.cdt.debug.ui.breakpointactions.IBreakpointActionPage;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.ITracepointAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.WhileSteppingAction;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @since 2.1
 */
public class WhileSteppingActionPage extends PlatformObject implements IBreakpointActionPage {

	private WhileSteppingAction fWhileSteppingAction;
	private Text fStepCountText;
	private TracepointActionsList actionsList;
	private TracepointGlobalActionsList globalActionsList;

	/**
	 * Create the composite
	 */
	private Composite createWhileSteppingActionComposite(Composite parent, int style) {
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));

		final Label stepCountLabel = new Label(composite, SWT.NONE);
		stepCountLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		stepCountLabel.setText(MessagesForTracepointActions.TracepointActions_Step_Count);

		fStepCountText = new Text(composite, SWT.BORDER);
		fStepCountText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fStepCountText.setText(Integer.toString(fWhileSteppingAction.getStepCount()));

		final Label actionsTriggeredWhenLabel = new Label(composite, SWT.NONE);
		actionsTriggeredWhenLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		actionsTriggeredWhenLabel.setText(MessagesForTracepointActions.TracepointActions_WhileStepping_Sub_Actions);		

		actionsList = new TracepointActionsList(composite, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		actionsList.setLayoutData(gridData);

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);

		final Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);

		final Label allAvailableActionsLabel = new Label(composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		allAvailableActionsLabel.setLayoutData(gridData);
		allAvailableActionsLabel.setText(MessagesForTracepointActions.TracepointActions_Available_actions);

		globalActionsList = new TracepointGlobalActionsList(composite, SWT.NONE, true, true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		globalActionsList.setLayoutData(gridData);

		String actionNames = fWhileSteppingAction.getSubActionsNames();
		actionsList.setNames(actionNames);

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

		return composite;
	}

	protected void HandleAttachButton() {
		ITracepointAction[] selectedActions = globalActionsList.getSelectedActions();
		for (ITracepointAction action : selectedActions) {
			actionsList.addAction(action);
		}
	}

	/**
	 * Clean up attached actions that were just deleted from the GlobalActionList
	 * 
	 * @since 7.0
	 */
	protected void HandleDeleteButton() {
		// First remove any attached action that was just deleted
		ITracepointAction[] selectedActions = globalActionsList.getSelectedActions();
		for (ITracepointAction action : selectedActions) {
			actionsList.removeAction(action);
		}
		// Now cleanup the global action list
		globalActionsList.HandleDeleteButton();
	}
	
	public WhileSteppingAction getWhileSteppingAction() {
		return fWhileSteppingAction;
	}

    @Override
	public void actionDialogCanceled() {
	}

    @Override
	public void actionDialogOK() {
		// Make sure we are dealing with an int
		int count = 1;
		try {
			count = Integer.parseInt(fStepCountText.getText());
		} catch (NumberFormatException e) {
		}
		fWhileSteppingAction.setStepCount(count);

		fWhileSteppingAction.setSubActionsNames(actionsList.getActionNames());
		fWhileSteppingAction.setSubActionsContent(actionsList.getActionNames());
	}

    @Override
	public Composite createComposite(IBreakpointAction action, Composite composite, int style) {
		fWhileSteppingAction = (WhileSteppingAction)action;
		return createWhileSteppingActionComposite(composite, style);
	}
}
