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

import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
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

public class ActionsList extends Composite {

	private Button removeButton;
	private Table table;

	public ActionsList(Composite parent, int style) {
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
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});

		final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		nameTableColumn.setWidth(120);
		nameTableColumn.setText(Messages.getString("ActionsList.0")); //$NON-NLS-1$

		final TableColumn typeTableColumn = new TableColumn(table, SWT.NONE);
		typeTableColumn.setWidth(120);
		typeTableColumn.setText(Messages.getString("ActionsList.1")); //$NON-NLS-1$

		final TableColumn summaryTableColumn = new TableColumn(table, SWT.NONE);
		summaryTableColumn.setWidth(120);
		summaryTableColumn.setText(Messages.getString("ActionsList.2")); //$NON-NLS-1$

		removeButton = new Button(this, SWT.NONE);
		removeButton.setText(Messages.getString("ActionsList.3")); //$NON-NLS-1$

		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				HandleRemoveButton();
			}
		});

		final Button upButton = new Button(this, SWT.NONE);
		upButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				HandleUpButton();
			}
		});
		upButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
		upButton.setText(Messages.getString("ActionsList.4")); //$NON-NLS-1$

		final Button downButton = new Button(this, SWT.NONE);
		downButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				HandleDownButton();
			}
		});
		downButton.setText(Messages.getString("ActionsList.5")); //$NON-NLS-1$
		//

		updateButtons();
	}

	public void addAction(IBreakpointAction action) {
		TableItem[] currentItems = table.getItems();
		boolean alreadyInList = false;
		for (int i = 0; i < currentItems.length && !alreadyInList; i++) {
			alreadyInList = ((IBreakpointAction) currentItems[i].getData()).equals(action);
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

	/**
	 * Remove an action from the list
	 * 
	 * @since 7.0
	 */
	public void removeAction(IBreakpointAction action) {
		TableItem[] currentItems = table.getItems();
		for (int i = 0; i < currentItems.length; i++) {
			if (((IBreakpointAction) currentItems[i].getData()).equals(action)) {
				table.remove(i);
				break;
			}
		}
		updateButtons();
	}

	public String getActionNames() {
		StringBuffer result = new StringBuffer();
		TableItem[] currentItems = table.getItems();
		for (int i = 0; i < currentItems.length; i++) {
			if (i > 0)
				result.append(',');
			result.append(((IBreakpointAction) currentItems[i].getData()).getName());
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
		StringTokenizer tok = new StringTokenizer(actionNames, ","); //$NON-NLS-1$

		while (tok.hasMoreTokens()) {
			String actionName = tok.nextToken();
			IBreakpointAction action = CDebugCorePlugin.getDefault().getBreakpointActionManager().findBreakpointAction(actionName);
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
		TableItem[] selectedItems = table.getSelection();
		removeButton.setEnabled(selectedItems.length > 0);
	}

}
