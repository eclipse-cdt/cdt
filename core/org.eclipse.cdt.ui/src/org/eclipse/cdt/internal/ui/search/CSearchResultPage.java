/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import java.util.HashMap;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CSearchResultLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.internal.ui.text.DelegatingLabelProvider;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class CSearchResultPage extends AbstractTextSearchViewPage {
	private CSearchContentProvider _contentProvider;
	private int _currentSortOrder;
	private int _currentGrouping;
	
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#showMatch(org.eclipse.search.ui.text.Match, int, int)
	 */
	protected void showMatch(Match match, int currentOffset, int currentLength)
			throws PartInitException {
		// TODO Auto-generated method stub
		IEditorPart editor= null;
		Object element= match.getElement();
		if (element instanceof ICElement) {
			ICElement cElement= (ICElement) element;
			try {
				editor= EditorUtility.openInEditor(cElement, false);
			} catch (PartInitException e1) {
				return;
			} catch (CModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} else if (element instanceof IFile) {
			editor= IDE.openEditor(CUIPlugin.getActivePage(), (IFile) element, false);
		} else if (element instanceof BasicSearchMatch){
			BasicSearchMatch x = (BasicSearchMatch) element;
			editor = IDE.openEditor(CUIPlugin.getActivePage(), (IFile) x.resource, false);
			showWithMarker(editor, (IFile) x.resource, currentOffset, currentLength);
		}
		if (editor instanceof ITextEditor) {
		ITextEditor textEditor= (ITextEditor) editor;
			textEditor.selectAndReveal(currentOffset, currentLength);
		} else if (editor != null){
			if (element instanceof IFile) {
				IFile file= (IFile) element;
				showWithMarker(editor, file, currentOffset, currentLength);
			}
		} 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#elementsChanged(java.lang.Object[])
	 */
	protected void elementsChanged(Object[] objects) {
		// TODO Auto-generated method stub
		if (_contentProvider != null)
			_contentProvider.elementsChanged(objects);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#clear()
	 */
	protected void clear() {
		if (_contentProvider!=null)
			_contentProvider.clear();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTreeViewer(org.eclipse.jface.viewers.TreeViewer)
	 */
	protected void configureTreeViewer(TreeViewer viewer) {
		//viewer.setSorter(new ViewerSorter());
		viewer.setLabelProvider(new CSearchResultLabelProvider());
		_contentProvider= new LevelTreeContentProvider(viewer, _currentGrouping);
		viewer.setContentProvider(_contentProvider);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTableViewer(org.eclipse.jface.viewers.TableViewer)
	 */
	protected void configureTableViewer(TableViewer viewer) {
		viewer.setLabelProvider(new DelegatingLabelProvider(this, new CSearchResultLabelProvider()));
		_contentProvider=new CSearchTableContentProvider(viewer);
		viewer.setContentProvider(_contentProvider);
		//setSortOrder(_currentSortOrder);
	}
	
	private void showWithMarker(IEditorPart editor, IFile file, int offset, int length) throws PartInitException {
		try {
			IMarker marker= file.createMarker(SearchUI.SEARCH_MARKER);
			HashMap attributes= new HashMap(4);
			attributes.put(IMarker.CHAR_START, new Integer(offset));
			attributes.put(IMarker.CHAR_END, new Integer(offset + length));
			marker.setAttributes(attributes);
			IDE.gotoMarker(editor, marker);
			marker.delete();
		} catch (CoreException e) {
			throw new PartInitException("Search Result Error", e); //$NON-NLS-1$
		}
	}
}
