/*******************************************************************************
 * Copyright (c) 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.executables;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Base viewer used by both the executables viewers and the source files viewer.
 */
abstract class BaseViewer extends TreeViewer {

	// Common columns
	protected TreeColumn nameColumn;
	protected TreeColumn locationColumn;
	protected TreeColumn sizeColumn;
	protected TreeColumn modifiedColumn;
	protected TreeColumn typeColumn;

	private static final int NUM_COLUMNS = 7;
	int column_sort_order[] = new int[NUM_COLUMNS];

	private ExecutablesView executablesView;

	class ColumnSelectionAdapter extends SelectionAdapter {

		private int selector;

		public ColumnSelectionAdapter(int selector) {
			this.selector = selector;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			setComparator(getViewerComparator(selector));
			getTree().setSortColumn((TreeColumn) e.getSource());
			getTree().setSortDirection(column_sort_order[selector] == ExecutablesView.ASCENDING ? SWT.UP : SWT.DOWN);
			column_sort_order[selector]  *= -1;
		}

	}

	public BaseViewer(ExecutablesView view, Composite parent, int style) {
		super(parent, style);
		executablesView = view;
		
		// default all columns sort order to ascending
		for (int i=0; i<NUM_COLUMNS; i++) {
			column_sort_order[i] = ExecutablesView.ASCENDING;
		}
	}

	public ExecutablesView getExecutablesView() {
		return executablesView;
	}

	protected void packColumns() {
		TreeColumn[] columns = getTree().getColumns();
		for (TreeColumn treeColumn : columns) {
			if (treeColumn.getWidth() > 0) {
				treeColumn.pack();
				treeColumn.setWidth(treeColumn.getWidth() + ExecutablesView.COLUMN_WIDTH_PADDING);
			}
		}
	}

	protected void saveColumnSettings(Preferences preferences) {
		Tree tree = getTree();
		
		// save the column order
		StringBuilder columnOrder = new StringBuilder(); 
		for (int index : tree.getColumnOrder()) {
			columnOrder.append(","); //$NON-NLS-1$
			columnOrder.append(Integer.toString(index));
		}
		// trim the leading comma
		columnOrder.deleteCharAt(0);
		preferences.setValue(getColumnOrderKey(), columnOrder.toString());

		// save which column was sorted and in which direction
		TreeColumn sortedColumn = tree.getSortColumn();
		for (int i=0; i<tree.getColumnCount(); i++) {
			if (sortedColumn.equals(tree.getColumn(i))) {
				preferences.setValue(getSortedColumnIndexKey(), i);
				preferences.setValue(getSortedColumnDirectionKey(), tree.getSortDirection());
				break;
			}
		}
		
		// save the visible state of each columns (1 is visible, 0 is not)
		String visibleColumns = ""; //$NON-NLS-1$
		for (int i=0; i<tree.getColumnCount(); i++) {
			if (tree.getColumn(i).getWidth() > 0) {
				visibleColumns += ",1"; //$NON-NLS-1$
			} else {
				visibleColumns += ",0"; //$NON-NLS-1$
			}
		}
		// trim the leading comma
		visibleColumns = visibleColumns.substring(1);
		preferences.setValue(getVisibleColumnsKey(), visibleColumns);
	}
	
	protected void restoreColumnSettings(Preferences preferences) {
		Tree tree = getTree();

		// restore the column order
		String columnOrder = preferences.getString(getColumnOrderKey());
		if (columnOrder.length() > 0) {
			String[] columns = columnOrder.split(","); //$NON-NLS-1$
			int[] columnNumbers = new int[columns.length];
			for (int i=0; i<columns.length; i++) {
				columnNumbers[i] = Integer.parseInt(columns[i]);
			}
			tree.setColumnOrder(columnNumbers);
		}
		
		// restore the sorted column
		int sortedColumnIndex = preferences.getInt(getSortedColumnIndexKey());
		int sortedColumnDirection = preferences.getInt(getSortedColumnDirectionKey());
		tree.setSortColumn(tree.getColumn(sortedColumnIndex));
		tree.setSortDirection(sortedColumnDirection == 0 ? SWT.UP : sortedColumnDirection);

		setComparator(getViewerComparator(sortedColumnIndex));

		// remember the sort order for the column
		column_sort_order[sortedColumnIndex] = sortedColumnDirection == SWT.UP ? ExecutablesView.ASCENDING : ExecutablesView.DESCENDING;
		
		// restore the visible state of each columns (1 is visible, 0 is not)
		String visibleColumns = preferences.getString(getVisibleColumnsKey());
		if (visibleColumns.length() <= 0) {
			visibleColumns = getDefaultVisibleColumnsValue();
		}
		String[] columns = visibleColumns.split(","); //$NON-NLS-1$
		for (int i=0; i<columns.length; i++) {
			if (columns[i].equals("0")) { //$NON-NLS-1$
				tree.getColumn(i).setWidth(0);
			}
		}
	}
	
	abstract protected ViewerComparator getViewerComparator(int sortType);
	
	abstract protected String getColumnOrderKey();
	
	abstract protected String getSortedColumnIndexKey();
	
	abstract protected String getSortedColumnDirectionKey();
	
	abstract protected String getVisibleColumnsKey();

	abstract protected String getDefaultVisibleColumnsValue();
}
