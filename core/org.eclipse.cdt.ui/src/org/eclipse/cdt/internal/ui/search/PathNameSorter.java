/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jun 24, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.ui.CSearchResultLabelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.search.ui.SearchUI;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PathNameSorter extends ViewerSorter {

	/*
	 * Overrides method from ViewerSorter
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		String name1 = null;
		String name2 = null;
		ISearchResultViewEntry entry1 = null;
		ISearchResultViewEntry entry2 = null;
		IMatch match1 = null;
		IMatch match2 = null;

		if( e1 instanceof ISearchResultViewEntry ) {
			entry1 = (ISearchResultViewEntry)e1;
			try {
				match1 = (IMatch)entry1.getSelectedMarker().getAttribute( CSearchResultCollector.IMATCH );
			} catch (CoreException e) {
			}
			name1 = match1.getLocation().toString();
		}
		if( e2 instanceof ISearchResultViewEntry ) {
			entry2 = (ISearchResultViewEntry)e2;
			try {
				match2 = (IMatch)entry2.getSelectedMarker().getAttribute( CSearchResultCollector.IMATCH );
			} catch (CoreException e) {
			}
			//name2 = _labelProvider.getText( e2 );
			name2 = match2.getLocation().toString();
		}
		
		if( name1 == null )
			name1 = ""; //$NON-NLS-1$
			
		if( name2 == null )
			name2 = ""; //$NON-NLS-1$
		
		int compare = getCollator().compare( name1, name2 );
		
		if( compare == 0 ){
			int startPos1 = -1;
			int startPos2 = -1;
			
			if (match1 != null)
				startPos1 = match1.getStartOffset();
			if (match2 != null)
				startPos2 = match2.getStartOffset();
			
			compare = startPos1 - startPos2;
		}
		
		return compare;
	}

	/*
	 * Overrides method from ViewerSorter
	 */
	public boolean isSorterProperty(Object element, String property) {
		return true;
	}

	/*
	 * Overrides method from ViewerSorter
	 */
	public void sort( Viewer viewer, Object[] elements ) {
		// Set label provider to show "path - resource"
		ISearchResultView view = SearchUI.getSearchResultView();
		if( view == null )
			return;
		
		_labelProvider = view.getLabelProvider();
		if( _labelProvider instanceof CSearchResultLabelProvider )
			((CSearchResultLabelProvider)_labelProvider).setOrder( CSearchResultLabelProvider.SHOW_PATH );
		super.sort( viewer, elements );
	}
	
	private ILabelProvider _labelProvider;
}