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
 * Created on Jun 18, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.SelectionUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class GotoMarkerAction extends Action {

	public GotoMarkerAction(){
			
	}
	
	public void run() {
		ISearchResultView view = SearchUI.getSearchResultView();
		Object element = SelectionUtil.getSingleElement( view.getSelection() );
		if( element instanceof ISearchResultViewEntry ) {
			ISearchResultViewEntry entry = (ISearchResultViewEntry) element;
			show( entry.getSelectedMarker() );
		}
	}
	
	private void show( IMarker marker ){
		IResource resource = marker.getResource();
		if( resource == null || !resource.exists() ){
			return;
		}
		
		IWorkbenchPage page = CUIPlugin.getActivePage();
		ICElement element = CSearchUtil.getCElement( marker );
		
		if( SearchUI.reuseEditor() ){
			showWithReuse( marker, resource, element, page );
		} else {
			showWithoutReuse( marker, element, page );
		}
	}
	
	private void showWithoutReuse( IMarker marker, ICElement element, IWorkbenchPage page ){
		IEditorPart editor = null;
		try{
			Object objectToOpen = ( element != null ) ? (Object) element : (Object) marker.getResource();
			editor = EditorUtility.openInEditor( objectToOpen, false );
		} catch ( CoreException e ) {
			//boo
		}
		if( editor != null ){
			IDE.gotoMarker(editor, marker);
		}
	}
	
	private void showWithReuse( IMarker marker, IResource resource, ICElement element, IWorkbenchPage page ) {
		if( !(resource instanceof IFile) ){
			return;
		}
		
		IEditorPart editor = EditorUtility.isOpenInEditor( element );
		if( editor != null ){
			page.bringToTop( editor );
		} else {
			
		}
	}
}
