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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.CSourceNotFoundEditorInput;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

/**
 * Displays the list of source files for the executable selected in the
 * ExecutablesViewer.
 */
public class SourceFilesViewer extends BaseViewer implements ISourceLookupParticipant {

	TreeColumn originalLocationColumn;
	private Tree sourceFilesTree;

	public SourceFilesViewer(ExecutablesView view, Composite parent, int style) {
		super(view, parent, style);

		setContentProvider(new SourceFilesContentProvider(this));
		setLabelProvider(new SourceFilesLabelProvider(this));
		sourceFilesTree = getTree();
		sourceFilesTree.setHeaderVisible(true);
		sourceFilesTree.setLinesVisible(true);

		createColumns();

		this.addOpenListener(new IOpenListener() {

			public void open(OpenEvent event) {
				openSourceFile(event);
			}
		});

		initializeSorter();

		// We implement ISourceLookupParticipant so we can listen for changes to
		// source lookup as this viewer shows both original and remapped
		// locations
		CDebugCorePlugin.getDefault().getCommonSourceLookupDirector().addParticipants(new ISourceLookupParticipant[] { this });
		sourceFilesTree.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				CDebugCorePlugin.getDefault().getCommonSourceLookupDirector().removeParticipants(
						new ISourceLookupParticipant[] { SourceFilesViewer.this });
			}
		});
	}

	private void openSourceFile(OpenEvent event) {
		boolean opened = false;
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();
		if (element instanceof ICElement) {
			if (element instanceof ITranslationUnit) {
				ITranslationUnit tu = (ITranslationUnit) element;
				IPath path = tu.getLocation();
				if (path != null && !path.toFile().exists()) {
					// Open the source not found editor
					IWorkbenchPage p = CUIPlugin.getActivePage();
					if (p != null) {
						try {
							String editorID = ICDebugUIConstants.CSOURCENOTFOUND_EDITOR_ID;
							p.openEditor(new CSourceNotFoundEditorInput(tu), editorID, true);
							opened = true;
						} catch (PartInitException e) {
						}
					}

				}

			}
			if (!opened) {
				try {
					IEditorPart part = EditorUtility.openInEditor(element);
					if (part != null) {
						IWorkbenchPage page = getExecutablesView().getSite().getPage();
						page.bringToTop(part);
						if (element instanceof ISourceReference) {
							EditorUtility.revealInEditor(part, (ICElement) element);
						}
					}
				} catch (Exception e) {
				}
			}
		}
	}

	private void createColumns() {
		nameColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		nameColumn.setWidth(100);
		nameColumn.setText("Source File Name");
		nameColumn.setMoveable(true);
		nameColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.NAME));

		locationColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		locationColumn.setWidth(100);
		locationColumn.setText("Location");
		locationColumn.setMoveable(true);
		locationColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.LOCATION));

		originalLocationColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		originalLocationColumn.setWidth(100);
		originalLocationColumn.setText("Original");
		originalLocationColumn.setMoveable(true);
		originalLocationColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.ORG_LOCATION));

		sizeColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		sizeColumn.setWidth(100);
		sizeColumn.setText("Size");
		sizeColumn.setMoveable(true);
		sizeColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.SIZE));

		modifiedColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		modifiedColumn.setWidth(100);
		modifiedColumn.setText("Modified");
		modifiedColumn.setMoveable(true);
		modifiedColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.MODIFIED));

		typeColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		typeColumn.setWidth(100);
		typeColumn.setText("Type");
		typeColumn.setMoveable(true);
		typeColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.TYPE));
	}

	protected ViewerComparator getViewerComparator(int sortType) {
		if (sortType == ExecutablesView.ORG_LOCATION) {
			return new ExecutablesViewerComparator(sortType, column_order[ExecutablesView.ORG_LOCATION]) {

				@SuppressWarnings("unchecked")
				public int compare(Viewer viewer, Object e1, Object e2) {
					if (e1 instanceof ITranslationUnit && e2 instanceof ITranslationUnit) {
						ITranslationUnit entry1 = (ITranslationUnit) e1;
						ITranslationUnit entry2 = (ITranslationUnit) e2;
						Executable exe = (Executable) getInput();
						String originalLocation1 = exe.getOriginalLocation(entry1);
						String originalLocation2 = exe.getOriginalLocation(entry2);
						return getComparator().compare(originalLocation1, originalLocation2) * column_order[ExecutablesView.ORG_LOCATION];
					}
					return super.compare(viewer, e1, e2);
				}
			};
		} else
			return new ExecutablesViewerComparator(sortType, column_order[sortType]);
	}

	private void initializeSorter() {
		byte orderType = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_TYPE_SF).byteValue();
		switch (orderType) {
		case ExecutablesView.NAME:
			column_order[ExecutablesView.NAME] = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_VALUE_SF).intValue();
			column_order[ExecutablesView.LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.SIZE] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.ORG_LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.MODIFIED] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
			break;
		case ExecutablesView.LOCATION:
			column_order[ExecutablesView.LOCATION] = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_VALUE_SF).intValue();
			column_order[ExecutablesView.NAME] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.SIZE] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.ORG_LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.MODIFIED] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
			break;
		case ExecutablesView.ORG_LOCATION:
			column_order[ExecutablesView.ORG_LOCATION] = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_VALUE_SF).intValue();
			column_order[ExecutablesView.NAME] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.SIZE] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.MODIFIED] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
			break;
		case ExecutablesView.SIZE:
			column_order[ExecutablesView.SIZE] = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_VALUE_SF).intValue();
			column_order[ExecutablesView.NAME] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.MODIFIED] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.ORG_LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
			break;
		case ExecutablesView.MODIFIED:
			column_order[ExecutablesView.MODIFIED] = getExecutablesView().getMemento().getInteger(ExecutablesView.P_ORDER_VALUE_SF).intValue();
			column_order[ExecutablesView.NAME] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.SIZE] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.ORG_LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
			break;
		default:
			column_order[ExecutablesView.MODIFIED] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.NAME] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.SIZE] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.ORG_LOCATION] = ExecutablesView.DESCENDING;
			column_order[ExecutablesView.TYPE] = ExecutablesView.DESCENDING;
		}

		ViewerComparator comparator = getViewerComparator(orderType);
		setComparator(comparator);
		if (orderType == ExecutablesView.NAME)
			setColumnSorting(nameColumn, column_order[ExecutablesView.NAME]);
		else if (orderType == ExecutablesView.LOCATION)
			setColumnSorting(locationColumn, column_order[ExecutablesView.LOCATION]);
		else if (orderType == ExecutablesView.ORG_LOCATION)
			setColumnSorting(originalLocationColumn, column_order[ExecutablesView.ORG_LOCATION]);
	}

	public void dispose() {
	}

	public Object[] findSourceElements(Object object) throws CoreException {
		return new Object[0];
	}

	public String getSourceName(Object object) throws CoreException {
		return "";
	}

	public void init(ISourceLookupDirector director) {
	}

	public void sourceContainersChanged(ISourceLookupDirector director) {
		UIJob refreshJob = new UIJob("Refresh Source Files") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				Object input = getInput();
				if (input != null && input instanceof Executable) {
					refresh(true);
				}
				return Status.OK_STATUS;
			}
		};
		refreshJob.schedule();
	}

}