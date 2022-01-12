/*******************************************************************************
 * Copyright (c) 2007, 2016 Nokia and others.
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

import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.ITracepointAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
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
public class TracepointActionsList extends Composite {

	private Button removeButton;
	private Button upButton;
	private Button downButton;
	private Table table;

	public TracepointActionsList(Composite parent, int style) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		setLayout(gridLayout);

		table = new Table(this, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
		final GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gridData.heightHint = 60;
		gridData.horizontalSpan = 4;
		table.setLayoutData(gridData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});

		final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		nameTableColumn.setWidth(120);
		nameTableColumn.setText(MessagesForTracepointActions.TracepointActions_Name);

		final TableColumn typeTableColumn = new TableColumn(table, SWT.NONE);
		typeTableColumn.setWidth(120);
		typeTableColumn.setText(MessagesForTracepointActions.TracepointActions_Type);

		final TableColumn summaryTableColumn = new TableColumn(table, SWT.NONE);
		summaryTableColumn.setWidth(120);
		summaryTableColumn.setText(MessagesForTracepointActions.TracepointActions_Summary);

		removeButton = new Button(this, SWT.NONE);
		removeButton.setText(MessagesForTracepointActions.TracepointActions_Remove);

		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HandleRemoveButton();
			}
		});

		upButton = new Button(this, SWT.NONE);
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				HandleUpButton();
			}
		});
		upButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
		upButton.setText(MessagesForTracepointActions.TracepointActions_Up);

		downButton = new Button(this, SWT.NONE);
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				HandleDownButton();
			}
		});
		downButton.setText(MessagesForTracepointActions.TracepointActions_Down);

		updateButtons();
	}

	public void addAction(ITracepointAction action) {
		TableItem[] currentItems = table.getItems();
		boolean alreadyInList = false;
		for (TableItem currentItem : currentItems) {
			if (((ITracepointAction) currentItem.getData()).equals(action)) {
				alreadyInList = true;
				break;
			}
		}
		if (!alreadyInList) {
			final TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(0, action.getName());
			tableItem.setText(1, action.getTypeName());
			tableItem.setText(2, action.getSummary());
			tableItem.setData(action);
		}
		updateButtons();
	}

	public void removeAction(ITracepointAction action) {
		TableItem[] currentItems = table.getItems();
		for (int i = 0; i < currentItems.length; i++) {
			if (((ITracepointAction) currentItems[i].getData()).equals(action)) {
				table.remove(i);
				break;
			}
		}
		updateButtons();
	}

	public String getActionNames() {
		StringBuilder result = new StringBuilder();
		TableItem[] currentItems = table.getItems();
		for (int i = 0; i < currentItems.length; i++) {
			if (i > 0) {
				// Keep a delimiter between the different action strings
				// so we can separate them again.
				result.append(TracepointActionManager.TRACEPOINT_ACTION_DELIMITER);
			}
			result.append(((ITracepointAction) currentItems[i].getData()).getName());
		}
		return result.toString();
	}

	private void swapItems(TableItem item, TableItem item2) {
		String[] item2Text = { item2.getText(0), item2.getText(1), item2.getText(2) };
		Object item2Data = item2.getData();

		item2.setText(0, item.getText(0));
		item2.setText(1, item.getText(1));
		item2.setText(2, item.getText(2));
		item2.setData(item.getData());

		item.setText(0, item2Text[0]);
		item.setText(1, item2Text[1]);
		item.setText(2, item2Text[2]);
		item.setData(item2Data);
	}

	protected void HandleUpButton() {
		int[] selection = table.getSelectionIndices();
		if (selection.length == 1 && selection[0] > 0) {
			swapItems(table.getItem(selection[0]), table.getItem(selection[0] - 1));
		}
	}

	protected void HandleDownButton() {
		int[] selection = table.getSelectionIndices();
		if (selection.length == 1 && selection[0] < (table.getItemCount() - 1)) {
			swapItems(table.getItem(selection[0]), table.getItem(selection[0] + 1));
		}
	}

	protected void HandleRemoveButton() {
		table.remove(table.getSelectionIndices());
		if (table.getItemCount() > 0) {
			table.select(table.getItemCount() - 1);
		}
		updateButtons();
	}

	public void setNames(String actionNames) {

		table.removeAll();
		String[] names = actionNames.split(TracepointActionManager.TRACEPOINT_ACTION_DELIMITER);

		for (String actionName : names) {
			ITracepointAction action = TracepointActionManager.getInstance().findAction(actionName);
			if (action != null) {
				final TableItem tableItem = new TableItem(table, SWT.NONE);
				tableItem.setText(0, action.getName());
				tableItem.setText(1, action.getTypeName());
				tableItem.setText(2, action.getSummary());
				tableItem.setData(action);
			}
		}

		updateButtons();
	}

	public void updateButtons() {
		int[] selectedItems = table.getSelectionIndices();
		removeButton.setEnabled(selectedItems.length > 0);
		downButton.setEnabled(selectedItems.length == 1 && selectedItems[0] < (table.getItemCount() - 1));
		upButton.setEnabled(selectedItems.length == 1 && selectedItems[0] > 0);
	}

	/**
	 * Update the appearance of given action.
	 * @param action
	 */
	void updateAction(ITracepointAction action) {
		TableItem[] currentItems = table.getItems();
		for (int i = 0; i < currentItems.length; i++) {
			if (((ITracepointAction) currentItems[i].getData()).equals(action)) {
				TableItem tableItem = currentItems[i];
				tableItem.setText(0, action.getName());
				tableItem.setText(1, action.getTypeName());
				tableItem.setText(2, action.getSummary());
				break;
			}
		}
		updateButtons();
	}
}
