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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
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
	int column_order[] = new int[NUM_COLUMNS];

	private ExecutablesView executablesView;

	class ColumnSelectionAdapter extends SelectionAdapter {

		private int selector;

		public ColumnSelectionAdapter(int selector) {
			this.selector = selector;
		}

		public void widgetSelected(SelectionEvent e) {
			column_order[selector] *= -1;
			ViewerComparator comparator = getViewerComparator(selector);
			setComparator(comparator);
			executablesView.getMemento().putInteger(ExecutablesView.P_ORDER_VALUE_SF, column_order[selector]);
			executablesView.getMemento().putInteger(ExecutablesView.P_ORDER_TYPE_SF, selector);
			setColumnSorting((TreeColumn) e.getSource(), column_order[selector]);
		}

	}

	public BaseViewer(ExecutablesView view, Composite parent, int style) {
		super(parent, style);
		executablesView = view;
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

	protected void setColumnSorting(TreeColumn column, int order) {
		getTree().setSortColumn(column);
		getTree().setSortDirection(order == ExecutablesView.ASCENDING ? SWT.UP : SWT.DOWN);
	}

	abstract protected ViewerComparator getViewerComparator(int sortType);

}