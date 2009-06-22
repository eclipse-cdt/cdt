/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * Implementation of the search view page for index based searches.
 */
public class PDOMSearchViewPage extends AbstractTextSearchViewPage {

	private IPDOMSearchContentProvider contentProvider;
	
	public PDOMSearchViewPage(int supportedLayouts) {
		super(supportedLayouts);
	}

	public PDOMSearchViewPage() {
		super();
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
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
		 */
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
		contentProvider = new PDOMSearchTreeContentProvider(this);
		viewer.setComparator(new SearchViewerComparator());
		viewer.setContentProvider((PDOMSearchTreeContentProvider)contentProvider);
		viewer.setLabelProvider(new PDOMSearchTreeLabelProvider(this));
	}

	@Override
	protected void configureTableViewer(TableViewer viewer) {
		contentProvider = new PDOMSearchListContentProvider(this);
		viewer.setComparator(new SearchViewerComparator());
		viewer.setContentProvider((PDOMSearchListContentProvider)contentProvider);
		viewer.setLabelProvider(new PDOMSearchListLabelProvider(this));
	}

	@Override
	protected void showMatch(Match match, int currentOffset, int currentLength, boolean activate) throws PartInitException {
		if (!(match instanceof PDOMSearchMatch))
			return;
		
		try {
			Object element= ((PDOMSearchMatch)match).getElement();
			IIndexFileLocation ifl= ((PDOMSearchElement)element).getLocation();
			IPath path = IndexLocationFactory.getPath(ifl);
			IEditorPart editor = EditorUtility.openInEditor(path, null);
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor)editor;
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
