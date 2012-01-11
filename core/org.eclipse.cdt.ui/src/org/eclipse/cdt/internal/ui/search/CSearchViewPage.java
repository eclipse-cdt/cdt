/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Ed Swartz (Nokia)
 *     Andrey Eremchenko (LEDAS)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.viewsupport.ColoringLabelProvider;

/**
 * Implementation of the search view page for index based searches.
 */
public class CSearchViewPage extends AbstractTextSearchViewPage {
	public static final int LOCATION_COLUMN_INDEX = 0; 
	public static final int DEFINITION_COLUMN_INDEX = 1;
	public static final int MATCH_COLUMN_INDEX = 2;

	private static final String[] fColumnLabels = new String[] { 
		CSearchMessages.PDOMSearchViewPageLocationColumn_label,
		CSearchMessages.PDOMSearchViewPageDefinitionColumn_label,
		CSearchMessages.PDOMSearchViewPageMatchColumn_label
	};
	
	private static final String KEY_LOCATION_COLUMN_WIDTH = "locationColumnWidth"; //$NON-NLS-1$
	private static final String KEY_DEFINITION_COLUMN_WIDTH = "definitionColumnWidth"; //$NON-NLS-1$
	private static final String KEY_MATCH_COLUMN_WIDTH = "matchColumnWidth"; //$NON-NLS-1$
	private static final String KEY_SHOW_ENCLOSING_DEFINITIONS = "showEnclosingDefinitions"; //$NON-NLS-1$

	private IPDOMSearchContentProvider contentProvider;
	private boolean fShowEnclosingDefinitions;
	private ShowEnclosingDefinitionsAction fShowEnclosingDefinitionsAction;
	private final int[] fColumnWidths = { 300, 150, 300 };
	
	private class ShowEnclosingDefinitionsAction extends Action {
		public ShowEnclosingDefinitionsAction() {
			super(CSearchMessages.PDOMSearchViewPage_ShowEnclosingDefinitions_actionLabel, SWT.CHECK);
			setChecked(fShowEnclosingDefinitions);
		}
		
		@Override
		public void run() {
			setShowEnclosingDefinitions(isChecked());
		}
	}
	
	public CSearchViewPage(int supportedLayouts) {
		super(supportedLayouts);
	}

	public CSearchViewPage() {
		super();
	}

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
		fShowEnclosingDefinitionsAction = new ShowEnclosingDefinitionsAction();
		IMenuManager menuManager= pageSite.getActionBars().getMenuManager();
		menuManager.add(fShowEnclosingDefinitionsAction);
		menuManager.updateAll(true);
		pageSite.getActionBars().updateActionBars();
	}

	@Override
	public void restoreState(IMemento memento) {
		super.restoreState(memento);
		IDialogSettings settings = getSettings();
		boolean showEnclosingDefinitions = true;
		if (settings.get(KEY_SHOW_ENCLOSING_DEFINITIONS) != null)
			showEnclosingDefinitions = settings.getBoolean(KEY_SHOW_ENCLOSING_DEFINITIONS);
		if (memento != null) {
			Boolean value = memento.getBoolean(KEY_SHOW_ENCLOSING_DEFINITIONS);
			if (value != null)
				showEnclosingDefinitions = value.booleanValue();
			String[] keys = { KEY_LOCATION_COLUMN_WIDTH, KEY_DEFINITION_COLUMN_WIDTH, KEY_MATCH_COLUMN_WIDTH };
			for (int i = 0; i < keys.length; i++) {
				Integer width = memento.getInteger(keys[i]);
				if (width == null)
					continue;
				if (width > 0)
					fColumnWidths[i] = width;
			}
		}
		setShowEnclosingDefinitions(showEnclosingDefinitions);
	}
	
	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		saveColumnWidths();
		memento.putInteger(KEY_DEFINITION_COLUMN_WIDTH, fColumnWidths[DEFINITION_COLUMN_INDEX]);
		memento.putInteger(KEY_LOCATION_COLUMN_WIDTH, fColumnWidths[LOCATION_COLUMN_INDEX]);
		memento.putInteger(KEY_MATCH_COLUMN_WIDTH, fColumnWidths[MATCH_COLUMN_INDEX]);
		memento.putBoolean(KEY_SHOW_ENCLOSING_DEFINITIONS, fShowEnclosingDefinitions);
	}
	
	public void setShowEnclosingDefinitions(boolean showEnclosingDefinitions) {
		if (fShowEnclosingDefinitions == showEnclosingDefinitions)
			return;
		fShowEnclosingDefinitions = showEnclosingDefinitions;
		getSettings().put(KEY_SHOW_ENCLOSING_DEFINITIONS, fShowEnclosingDefinitions);
		if (fShowEnclosingDefinitionsAction.isChecked() != showEnclosingDefinitions)
			fShowEnclosingDefinitionsAction.setChecked(showEnclosingDefinitions);
		StructuredViewer viewer = getViewer();
		if (viewer instanceof TableViewer) {
			TableViewer tableViewer = (TableViewer) viewer;
			TableColumn tableColumn = tableViewer.getTable().getColumn(DEFINITION_COLUMN_INDEX);
			if (fShowEnclosingDefinitions) {
				tableColumn.setWidth(fColumnWidths[DEFINITION_COLUMN_INDEX]);
				tableColumn.setResizable(true);
			} else {
				fColumnWidths[DEFINITION_COLUMN_INDEX] = tableColumn.getWidth();
				tableColumn.setWidth(0);
				tableColumn.setResizable(false);
			}
		}
		if (viewer != null)
			viewer.refresh();
	}

	public boolean isShowEnclosingDefinitions() {
		return fShowEnclosingDefinitions;
	}

	@Override
	protected void elementsChanged(Object[] objects) {
		if (contentProvider != null)
			contentProvider.elementsChanged(objects);
	}

	@Override
	protected void clear() {
		if (contentProvider != null)
			contentProvider.clear();
	}

	/**
	 * Supply a sorter for the list and tree content providers to supply some order to the
	 * large numbers of matches that may result.  
	 * <p>
	 * This sorter categorizes the different kinds of ICElement matches (as well as IStatus
	 * messages and External Files groups) to place them in groups.  The items within a
	 * category are sorted in the default way {@link ViewerSorter#compare(Viewer, Object, Object)} works,
	 * by comparing text labels.
	 * <p>
	 * A potential concern here is that, in sorting the elements by name, the user may 
	 * find himself randomly jumping around a file when navigating search results in order.
	 * As this only happens when a search matches different identifiers or identifiers of
	 * different types, and since the user can use a textual search within a file to navigate
	 * the same results (ignoring extraneous hits in comments or disabled code), I argue it's not
	 * a big deal.  Furthermore, usually it would be a wildcard search that would result in 
	 * this situation -- indicating the user doesn't know the identifier and wants to find it using
	 * search.  In such a case, a sorted list of results in much more friendly to navigate.
	 * @author eswartz
	 *
	 */
	private class SearchViewerComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof LineSearchElement && e2 instanceof LineSearchElement) {
				LineSearchElement l1 = (LineSearchElement) e1;
				LineSearchElement l2 = (LineSearchElement) e2;
				if (viewer instanceof TableViewer) {
					String p1 = l1.getLocation().getURI().getPath();
					String p2 = l2.getLocation().getURI().getPath();
					int cmp = p1.compareTo(p2);
					if (cmp != 0)
						return cmp;
				}
				return l1.getLineNumber() - l2.getLineNumber();
			}
			return super.compare(viewer, e1, e2);
		}
		
		@Override
		public int category(Object element) {
			// place status messages first
			if (element instanceof IStatus) { 
				return -1000;
			}
			
			// keep elements of the same type together
			if (element instanceof TypeInfoSearchElement) {
				TypeInfoSearchElement searchElement = (TypeInfoSearchElement)element;
				int type = searchElement.getTypeInfo().getCElementType();
				// handle unknown types
				if (type < 0) {
					type = 0;
				}
				return type;
			} else if (element instanceof ICElement) {
				int type = ((ICElement) element).getElementType();
				// handle unknown types
				if (type < 0) {
					type = 0;
				}
				return Math.min(Math.max(0, type), 900);
			}
			
			// place external folders next to last
			if (element instanceof IPath || element instanceof IIndexFileLocation) {
				return 999;
			}
			
			// place external file matches last
			if (element == IPDOMSearchContentProvider.URI_CONTAINER) {
				return 1000;
			}
			
			return 2000;
		}
	}
	
	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		contentProvider = new CSearchTreeContentProvider(this);
		viewer.setComparator(new SearchViewerComparator());
		viewer.setContentProvider((CSearchTreeContentProvider)contentProvider);
		CSearchTreeLabelProvider innerLabelProvider = new CSearchTreeLabelProvider(this);
		ColoringLabelProvider labelProvider = new ColoringLabelProvider(innerLabelProvider);
		viewer.setLabelProvider(labelProvider);
	}

	@Override
	protected void configureTableViewer(TableViewer viewer) {
		createColumns(viewer);
		contentProvider = new CSearchListContentProvider(this);
		viewer.setComparator(new SearchViewerComparator());
		viewer.setContentProvider((CSearchListContentProvider)contentProvider);
	}
	
	@Override
	protected TableViewer createTableViewer(Composite parent) {
		TableViewer tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tableViewer.getControl().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				saveColumnWidths();
			}
		});
		return tableViewer;
	}
	
	private void saveColumnWidths() {
		StructuredViewer viewer = getViewer();
		if (viewer instanceof TableViewer) {
			TableViewer tableViewer = (TableViewer) viewer;
			for (int i = 0; i < fColumnLabels.length; i++) {
				if (i == DEFINITION_COLUMN_INDEX && !fShowEnclosingDefinitions)
					continue;
				fColumnWidths[i] = tableViewer.getTable().getColumn(i).getWidth(); 
			}		
		}
	}

	private void createColumns(TableViewer viewer) {
		for (int i = 0; i < fColumnLabels.length; i++) {
			TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
			viewerColumn.setLabelProvider(new CSearchListLabelProvider(this, i));
			TableColumn tableColumn = viewerColumn.getColumn();
			tableColumn.setText(fColumnLabels[i]);
			tableColumn.setWidth(fColumnWidths[i]);
			tableColumn.setResizable(true);
			tableColumn.setMoveable(false);
			if (i == DEFINITION_COLUMN_INDEX && !fShowEnclosingDefinitions) {
				tableColumn.setWidth(0);
				tableColumn.setResizable(false);
			}
		}
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	@Override
	protected void showMatch(Match match, int currentOffset, int currentLength, boolean activate) throws PartInitException {
		if (!(match instanceof CSearchMatch))
			return;
		
		try {
			Object element= ((CSearchMatch) match).getElement();
			IIndexFileLocation ifl= ((CSearchElement) element).getLocation();
			IPath path = IndexLocationFactory.getPath(ifl);
			IEditorPart editor = EditorUtility.openInEditor(path, null, activate);
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) editor;
				textEditor.selectAndReveal(currentOffset, currentLength);
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
	
	@Override
	public StructuredViewer getViewer() {
		return super.getViewer();
	}
}
