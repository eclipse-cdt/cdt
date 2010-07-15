/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.executables;

import java.util.List;

import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
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

	private static final String P_COLUMN_ORDER_KEY_EXE = "columnOrderKeyEXE"; //$NON-NLS-1$
	private static final String P_SORTED_COLUMN_INDEX_KEY_EXE = "sortedColumnIndexKeyEXE"; //$NON-NLS-1$
	private static final String P_COLUMN_SORT_DIRECTION_KEY_EXE = "columnSortDirectionKeyEXE"; //$NON-NLS-1$
	private static final String P_VISIBLE_COLUMNS_KEY_EXE = "visibleColumnsKeyEXE"; //$NON-NLS-1$

	
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
		getTree().setData(".uid", "ExecutablesViewerTree"); //$NON-NLS-1$ //$NON-NLS-2$

		createColumns();

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
		nameColumn.setText(Messages.ExecutablesViewer_ExecutableName);
		nameColumn.setMoveable(true);
		nameColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.NAME));

		projectColumn = new TreeColumn(getTree(), SWT.NONE);
		projectColumn.setWidth(100);
		projectColumn.setMoveable(true);
		projectColumn.setText(Messages.ExecutablesViewer_Project);
		projectColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.PROJECT));

		locationColumn = new TreeColumn(getTree(), SWT.NONE);
		locationColumn.setWidth(100);
		locationColumn.setText(Messages.ExecutablesViewer_Location);
		locationColumn.setMoveable(true);
		locationColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.LOCATION));

		sizeColumn = new TreeColumn(getTree(), SWT.NONE);
		sizeColumn.setWidth(100);
		sizeColumn.setText(Messages.ExecutablesViewer_Size);
		sizeColumn.setMoveable(true);
		sizeColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.SIZE));

		modifiedColumn = new TreeColumn(getTree(), SWT.NONE);
		modifiedColumn.setWidth(100);
		modifiedColumn.setText(Messages.ExecutablesViewer_Modified);
		modifiedColumn.setMoveable(true);
		modifiedColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.MODIFIED));

		typeColumn = new TreeColumn(getTree(), SWT.NONE);
		typeColumn.setWidth(100);
		typeColumn.setText(Messages.ExecutablesViewer_Type);
		typeColumn.setMoveable(true);
		typeColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.TYPE));

	}

	@Override
	protected ViewerComparator getViewerComparator(int sortType) {
		if (sortType == ExecutablesView.PROJECT) {
			return new ExecutablesViewerComparator(sortType, column_sort_order[ExecutablesView.PROJECT]) {
				@SuppressWarnings("unchecked")
				public int compare(Viewer viewer, Object e1, Object e2) {
					Executable entry1 = (Executable) e1;
					Executable entry2 = (Executable) e2;
					return getComparator().compare(entry1.getProject().getName(), entry2.getProject().getName())
							* column_sort_order[ExecutablesView.PROJECT];
				}
			};
		}
		return new ExecutablesViewerComparator(sortType, column_sort_order[sortType]);
	}

	@Override
	protected String getColumnOrderKey() {
		return P_COLUMN_ORDER_KEY_EXE;
	}

	@Override
	protected String getSortedColumnIndexKey() {
		return P_SORTED_COLUMN_INDEX_KEY_EXE;
	}

	@Override
	protected String getSortedColumnDirectionKey() {
		return P_COLUMN_SORT_DIRECTION_KEY_EXE;
	}

	@Override
	protected String getVisibleColumnsKey() {
		return P_VISIBLE_COLUMNS_KEY_EXE;
	}

	@Override
	protected String getDefaultVisibleColumnsValue() {
		// default visible columns
		return "1,1,1,0,0,0"; //$NON-NLS-1$
	}

	public void executablesChanged(final List<Executable> executables) {
		// some executables have been updated.  if one of them is currently
		// selected, we need to update the source file list
		UIJob refreshJob = new UIJob(Messages.ExecutablesViewer_RefreshExecutablesView) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				// if the user has selected an executable, they expect its
				// list of source files to be refreshed automatically
				if (getSelection() != null &&
					getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection)getSelection();
					
					Object firstElement = selection.getFirstElement();
					if (firstElement instanceof Executable) {
						Executable executable = (Executable) firstElement;
						if (executables.contains(executable)) {
							executable.setRefreshSourceFiles(true);
							setSelection(selection);
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		refreshJob.schedule();
	}

	public void executablesListChanged() {
		// Executables list has changed so refresh the view.
		UIJob refreshJob = new UIJob(Messages.ExecutablesViewer_RefreshExecutablesView) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				refresh(null);
				packColumns();
				return Status.OK_STATUS;
			}
		};
		refreshJob.schedule();
	}
}