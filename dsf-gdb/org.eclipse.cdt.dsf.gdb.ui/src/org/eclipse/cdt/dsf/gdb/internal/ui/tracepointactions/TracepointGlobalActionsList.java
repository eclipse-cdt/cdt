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

import java.util.ArrayList;

import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.ITracepointAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.WhileSteppingAction;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 2.1
 */
public class TracepointGlobalActionsList extends Composite {

	private Button attachButton;
	private Button deleteButton;
	private Button editButton;
	private Button newButton;
	private Table table;
	private boolean isSubAction;

	public TracepointGlobalActionsList(Composite parent, int style, boolean useAttachButton, boolean isSub) {
		super(parent, style);
		isSubAction = isSub;
		
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		setLayout(gridLayout);

		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				HandleEditButton();
			}
		});

		final GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 5;
		table.setLayoutData(gridData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		nameTableColumn.setWidth(120);
		nameTableColumn.setText(MessagesForTracepointActions.TracepointActions_Name);

		final TableColumn typeTableColumn = new TableColumn(table, SWT.NONE);
		typeTableColumn.setWidth(120);
		typeTableColumn.setText(MessagesForTracepointActions.TracepointActions_Type);

		final TableColumn summaryTableColumn = new TableColumn(table, SWT.NONE);
		summaryTableColumn.setWidth(120);
		summaryTableColumn.setText(MessagesForTracepointActions.TracepointActions_Summary);

		ArrayList<ITracepointAction> actions = TracepointActionManager.getInstance().getActions();

		for (ITracepointAction element : actions) {
			if (isSubAction && element instanceof WhileSteppingAction) continue;
			final TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(0, element.getName());
			tableItem.setText(1, element.getTypeName());
			tableItem.setText(2, element.getSummary());
			tableItem.setData(element);
		}

		if (useAttachButton) {
			attachButton = new Button(this, SWT.NONE);
			attachButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
			attachButton.setText(MessagesForTracepointActions.TracepointActions_Attach);
		}

		newButton = new Button(this, SWT.NONE);
		newButton.setLayoutData(new GridData());
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					HandleNewButton();
				} catch (CoreException e1) {
				}
			}
		});
		newButton.setText(MessagesForTracepointActions.TracepointActions_New);
		newButton.setEnabled(true);

		editButton = new Button(this, SWT.NONE);
		editButton.setText(MessagesForTracepointActions.TracepointActions_Edit);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HandleEditButton();
			}
		});
		if (!useAttachButton)
			editButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL + GridData.HORIZONTAL_ALIGN_END));

		deleteButton = new Button(this, SWT.NONE);
		deleteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		deleteButton.setText(MessagesForTracepointActions.TracepointActions_Delete);
		
		updateButtons();
	}

	public Button getAttachButton() {
		return attachButton;
	}

	public Button getDeleteButton() {
		return deleteButton;
	}

	public ITracepointAction[] getSelectedActions() {
		TableItem[] selectedItems = table.getSelection();
		ITracepointAction[] actionList = new ITracepointAction[selectedItems.length];
		int actionCount = 0;
		for (int i = 0; i < selectedItems.length; i++) {
			actionList[actionCount++] = (ITracepointAction) selectedItems[i].getData();
		}
		return actionList;
	}

	protected void HandleDeleteButton() {
		TableItem[] selectedItems = table.getSelection();
		for (int i = 0; i < selectedItems.length; i++) {
			ITracepointAction action = (ITracepointAction) selectedItems[i].getData();
			TracepointActionManager.getInstance().deleteAction(action);
		}
		table.remove(table.getSelectionIndices());
		if (table.getItemCount() > 0) {
			table.select(table.getItemCount() - 1);
		}
		updateButtons();
	}

	protected void HandleEditButton() {

		TableItem[] selectedItems = table.getSelection();
		ITracepointAction action = (ITracepointAction) selectedItems[0].getData();

		TracepointActionDialog dialog = new TracepointActionDialog(this.getShell(), action, isSubAction);
		int result = dialog.open();
		if (result == Window.OK) {
			action.setName(dialog.getActionName());
			selectedItems[0].setText(0, action.getName());
			selectedItems[0].setText(1, action.getTypeName());
			selectedItems[0].setText(2, action.getSummary());
		}

	}

	protected void HandleNewButton() throws CoreException {

		TracepointActionDialog dialog = new TracepointActionDialog(this.getShell(), null, isSubAction);
		int result = dialog.open();
		if (result == Window.OK) {
			ITracepointAction action = (ITracepointAction)dialog.getTracepointAction();
			action.setName(dialog.getActionName());
			TracepointActionManager.getInstance().addAction(action);
			final TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(0, action.getName());
			tableItem.setText(1, action.getTypeName());
			tableItem.setText(2, action.getSummary());
			tableItem.setData(action);

		}

	}

	public void updateButtons() {
		TableItem[] selectedItems = table.getSelection();
		if (attachButton != null)
			attachButton.setEnabled(selectedItems.length > 0);
		deleteButton.setEnabled(selectedItems.length > 0);
		editButton.setEnabled(selectedItems.length == 1);
	}

}
