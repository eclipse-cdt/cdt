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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
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

public class GlobalActionsList extends Composite {

	private Button attachButton = null;
	private Button deleteButton = null;
	private Button editButton = null;
	private Button newButton = null;
	private Table table = null;

	public GlobalActionsList(Composite parent, int style, boolean useAttachButton) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		setLayout(gridLayout);

		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}

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
		nameTableColumn.setText(Messages.getString("GlobalActionsList.0")); //$NON-NLS-1$

		final TableColumn typeTableColumn = new TableColumn(table, SWT.NONE);
		typeTableColumn.setWidth(120);
		typeTableColumn.setText(Messages.getString("GlobalActionsList.1")); //$NON-NLS-1$

		final TableColumn summaryTableColumn = new TableColumn(table, SWT.NONE);
		summaryTableColumn.setWidth(120);
		summaryTableColumn.setText(Messages.getString("GlobalActionsList.2")); //$NON-NLS-1$

		ArrayList actions = CDebugCorePlugin.getDefault().getBreakpointActionManager().getBreakpointActions();

		for (Iterator iter = CDebugCorePlugin.getDefault().getBreakpointActionManager().getBreakpointActions().iterator(); iter.hasNext();) {
			IBreakpointAction element = (IBreakpointAction) iter.next();
			final TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(0, element.getName());
			tableItem.setText(1, element.getTypeName());
			tableItem.setText(2, element.getSummary());
			tableItem.setData(element);
		}

		if (useAttachButton) {
			attachButton = new Button(this, SWT.NONE);
			attachButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
			attachButton.setText(Messages.getString("GlobalActionsList.3")); //$NON-NLS-1$
		}

		newButton = new Button(this, SWT.NONE);
		newButton.setLayoutData(new GridData());
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				try {
					HandleNewButton();
				} catch (CoreException e1) {
				}
			}
		});
		newButton.setText(Messages.getString("GlobalActionsList.4")); //$NON-NLS-1$
		newButton.setEnabled(CDebugCorePlugin.getDefault().getBreakpointActionManager().getBreakpointActionExtensions().length > 0);

		editButton = new Button(this, SWT.NONE);
		editButton.setText(Messages.getString("GlobalActionsList.5")); //$NON-NLS-1$
		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				HandleEditButton();
			}
		});
		if (!useAttachButton)
			editButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL + GridData.HORIZONTAL_ALIGN_END));

		deleteButton = new Button(this, SWT.NONE);
		deleteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		deleteButton.setText(Messages.getString("GlobalActionsList.6")); //$NON-NLS-1$

		updateButtons();
	}

	public Button getAttachButton() {
		return attachButton;
	}

	/**
	 * @since 7.0
	 */
	public Button getDeleteButton() {
		return deleteButton;
	}

	public IBreakpointAction[] getSelectedActions() {
		TableItem[] selectedItems = table.getSelection();
		IBreakpointAction[] actionList = new IBreakpointAction[selectedItems.length];
		int actionCount = 0;
		for (int i = 0; i < selectedItems.length; i++) {
			actionList[actionCount++] = (IBreakpointAction) selectedItems[i].getData();
		}
		return actionList;
	}

	protected void HandleDeleteButton() {
		TableItem[] selectedItems = table.getSelection();
		for (int i = 0; i < selectedItems.length; i++) {
			IBreakpointAction action = (IBreakpointAction) selectedItems[i].getData();
			CDebugCorePlugin.getDefault().getBreakpointActionManager().deleteAction(action);
		}
		table.remove(table.getSelectionIndices());
		if (table.getItemCount() > 0) {
			table.select(table.getItemCount() - 1);
		}
		updateButtons();
	}

	protected void HandleEditButton() {

		TableItem[] selectedItems = table.getSelection();
		IBreakpointAction action = (IBreakpointAction) selectedItems[0].getData();

		ActionDialog dialog = new ActionDialog(this.getShell(), action);
		int result = dialog.open();
		if (result == Window.OK) {
			action.setName(dialog.getActionName());
			selectedItems[0].setText(0, action.getName());
			selectedItems[0].setText(1, action.getTypeName());
			selectedItems[0].setText(2, action.getSummary());
		}

	}

	protected void HandleNewButton() throws CoreException {

		ActionDialog dialog = new ActionDialog(this.getShell(), null);
		int result = dialog.open();
		if (result == Window.OK) {
			IBreakpointAction action = dialog.getBreakpointAction();
			action.setName(dialog.getActionName());
			CDebugCorePlugin.getDefault().getBreakpointActionManager().addAction(action);
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
		editButton.setEnabled(selectedItems.length > 0);
	}

}
