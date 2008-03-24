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

import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.cdt.debug.core.executables.IExecutablesChangeEvent;
import org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.progress.UIJob;

/**
 * Displays the list of executables gathered by the ExecutablesManager
 */
public class ExecutablesViewer extends BaseViewer implements IExecutablesChangeListener {

	/**
	 * Handles dropping executable files into the view
	 */
	public class ExecutablesDropAdapter extends ViewerDropAdapter {

		protected ExecutablesDropAdapter(Viewer viewer) {
			super(viewer);
		}

		@Override
		public boolean performDrop(Object data) {
			final String[] fileNames = (String[]) data;
			ExecutablesViewer.this.getExecutablesView().importExecutables(fileNames);
			return true;
		}

		@Override
		public boolean validateDrop(Object target, int operation, TransferData transferType) {
			return FileTransfer.getInstance().isSupportedType(transferType);
		}

	}

	public TreeColumn projectColumn;

	public ExecutablesViewer(ExecutablesView executablesView, Composite parent, int style) {
		super(executablesView, parent, style);

		// Setup D&D support
		int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT;
		Transfer[] transfers = new Transfer[] { FileTransfer.getInstance() };
		ExecutablesDropAdapter adapter = new ExecutablesDropAdapter(this);
		adapter.setFeedbackEnabled(false);
		addDropSupport(ops | DND.DROP_DEFAULT, transfers, adapter);

		// Setup content provider
		ExecutablesContentProvider exeContentProvider = new ExecutablesContentProvider(this);
		setContentProvider(exeContentProvider);
		setLabelProvider(exeContentProvider);

		getTree().setHeaderVisible(true);
		getTree().setLinesVisible(true);
		executablesView.getSite().setSelectionProvider(this);

		createColumns();
		initializeSorter();

		setInput(ExecutablesManager.getExecutablesManager());

		MenuManager popupMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		IMenuListener listener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(ExecutablesViewer.this.getExecutablesView().refreshAction);
				manager.add(ExecutablesViewer.this.getExecutablesView().importAction);
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		};
		popupMenuManager.addMenuListener(listener);
		popupMenuManager.setRemoveAllWhenShown(true);
		getExecutablesView().getSite().registerContextMenu(popupMenuManager, this.getExecutablesView().getSite().getSelectionProvider());
		Menu menu = popupMenuManager.createContextMenu(getTree());
		getTree().setMenu(menu);
	}

	private void createColumns() {
		nameColumn = new TreeColumn(getTree(), SWT.NONE);
		nameColumn.setWidth(100);
		nameColumn.setText("Executable Name");
		nameColumn.setMoveable(true);
		nameColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.NAME));

		projectColumn = new TreeColumn(getTree(), SWT.NONE);
		projectColumn.setWidth(100);
		projectColumn.setMoveable(true);
		projectColumn.setText("Project");
		projectColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.PROJECT));

		locationColumn = new TreeColumn(getTree(), SWT.NONE);
		locationColumn.setWidth(100);
		locationColumn.setText("Location");
		locationColumn.setMoveable(true);
		locationColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.LOCATION));

		sizeColumn = new TreeColumn(getTree(), SWT.NONE);
		sizeColumn.setWidth(100);
		sizeColumn.setText("Size");
		sizeColumn.setMoveable(true);
		sizeColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.SIZE));

		modifiedColumn = new TreeColumn(getTree(), SWT.NONE);
		modifiedColumn.setWidth(100);
		modifiedColumn.setText("Modified");
		modifiedColumn.setMoveable(true);
		modifiedColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.MODIFIED));

		typeColumn = new TreeColumn(getTree(), SWT.NONE);
		typeColumn.setWidth(100);
		typeColumn.setText("Type");
		typeColumn.setMoveable(true);
		typeColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.TYPE));

	}

	/**
	 * Initialize column ordering and sorting
	 */
	private void initializeSorter() {
		byte orderType = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_TYPE_EXE).byteValue();
		switch (orderType) {
		case ExecutablesView.NAME:
			column_order[ExecutablesView.NAME] = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_VALUE_EXE).intValue();
			column_order[ExecutablesView.PROJECT] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.SIZE] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.MODIFIED] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
			break;
		case ExecutablesView.PROJECT:
			column_order[ExecutablesView.PROJECT] = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_VALUE_EXE).intValue();
			column_order[ExecutablesView.NAME] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.SIZE] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.MODIFIED] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
			break;
		case ExecutablesView.LOCATION:
			column_order[ExecutablesView.LOCATION] = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_VALUE_EXE).intValue();
			column_order[ExecutablesView.NAME] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.PROJECT] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.SIZE] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.MODIFIED] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
			break;
		case ExecutablesView.SIZE:
			column_order[ExecutablesView.SIZE] = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_VALUE_EXE).intValue();
			column_order[ExecutablesView.NAME] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.PROJECT] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.MODIFIED] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
			break;
		case ExecutablesView.MODIFIED:
			column_order[ExecutablesView.MODIFIED] = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_VALUE_EXE).intValue();
			column_order[ExecutablesView.NAME] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.PROJECT] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.SIZE] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
			break;
		default:
			column_order[ExecutablesView.NAME] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.PROJECT] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.SIZE] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.MODIFIED] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
		}

		ViewerComparator comparator = getViewerComparator(orderType);
		setComparator(comparator);
		if (orderType == ExecutablesView.NAME)
			setColumnSorting(nameColumn, column_order[ExecutablesView.NAME]);
		else if (orderType == ExecutablesView.PROJECT)
			setColumnSorting(projectColumn, column_order[ExecutablesView.PROJECT]);
		else if (orderType == ExecutablesView.LOCATION)
			setColumnSorting(locationColumn, column_order[ExecutablesView.LOCATION]);
		else if (orderType == ExecutablesView.SIZE)
			setColumnSorting(projectColumn, column_order[ExecutablesView.SIZE]);
		else if (orderType == ExecutablesView.MODIFIED)
			setColumnSorting(locationColumn, column_order[ExecutablesView.MODIFIED]);
	}

	@Override
	protected ViewerComparator getViewerComparator(int sortType) {
		if (sortType == ExecutablesView.PROJECT) {
			return new ExecutablesViewerComparator(sortType, column_order[ExecutablesView.PROJECT]) {
				@SuppressWarnings("unchecked")
				public int compare(Viewer viewer, Object e1, Object e2) {
					Executable entry1 = (Executable) e1;
					Executable entry2 = (Executable) e2;
					return getComparator().compare(entry1.getProject().getName(), entry2.getProject().getName())
							* column_order[ExecutablesView.PROJECT];
				}
			};
		}
		return new ExecutablesViewerComparator(sortType, column_order[sortType]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener#executablesChanged(org.eclipse.cdt.debug.core.executables.IExecutablesChangeEvent)
	 */
	public void executablesChanged(IExecutablesChangeEvent event) {
		// Executables have changed so refresh the view.
		final ExecutablesViewer viewer = this;
		UIJob refreshJob = new UIJob("Refresh Executables View") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				viewer.refresh(null);
				viewer.packColumns();
				return Status.OK_STATUS;
			}
		};
		refreshJob.schedule();
	}

}