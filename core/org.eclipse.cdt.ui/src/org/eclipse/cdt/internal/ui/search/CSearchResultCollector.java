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
 * Created on Jun 11, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.search.ui.IActionGroupFactory;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.ui.actions.ActionGroup;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchResultCollector implements ICSearchResultCollector {
	/**
	 * 
	 */
	public CSearchResultCollector() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#aboutToStart()
	 */
	public void aboutToStart() {
		_view = SearchUI.getSearchResultView();
		_matchCount = 0;
		if( _view != null ){
			_view.searchStarted(
				new ActionGroupFactory(),
				_operation.getSingularLabel(),
				_operation.getPluralLabelPattern(),
				_operation.getImageDescriptor(),
				CSearchPage.EXTENSION_POINT_ID,
				new CSearchResultLabelProvider(),
				new GotoMarkerAction(),
				new GroupByKeyComputer(),
				_operation
			);
		}
		if( !getProgressMonitor().isCanceled() ){
			getProgressMonitor().subTask( SEARCHING );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#accept(org.eclipse.core.resources.IResource, int, int, java.lang.Object, int)
	 */
	public void accept(
		IResource resource,
		int start,
		int end,
		ICElement enclosingElement,
		int accuracy)
		throws CoreException 
	{
		IMarker marker = resource.createMarker( SearchUI.SEARCH_MARKER );
		
		Object groupKey = enclosingElement;
		
		HashMap markerAttributes = new HashMap( 2 );
		
		//we can hang any other info we want off the marker
		markerAttributes.put( IMarker.CHAR_START, new Integer( Math.max( start, 0 ) ) );		
		markerAttributes.put( IMarker.CHAR_END,   new Integer( Math.max( end, 0 ) ) );
		
		marker.setAttributes( markerAttributes );
		
		_view.addMatch( enclosingElement.getElementName(), groupKey, resource, marker );
		_matchCount++;
	}

	public void accept(
		IPath path,
		int start,
		int end,
		ICElement enclosingElement,
		int accuracy)
		throws CoreException 
	{
		if( _matches == null ){
			_matches = new HashSet();
		}
		
		_matches.add( new Match( path.toString(), start, end ) );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#done()
	 */
	public void done() {
		if( !getProgressMonitor().isCanceled() ){
			String matchesString;
			if( _matchCount == 1 ){
				matchesString = MATCH;
			} else {
				matchesString = MessageFormat.format( MATCHES, new Integer[]{ new Integer(_matchCount) } );
			}
			
			getProgressMonitor().setTaskName( MessageFormat.format( DONE, new String[]{ matchesString } ) );
		}

		if( _view != null ){
			_view.searchFinished();
		}
		
		_view    = null;
		_monitor = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#getProgressMonitor()
	 */
	public IProgressMonitor getProgressMonitor() {
		return _monitor;
	}

	public void setProgressMonitor(IProgressMonitor monitor) {
		this._monitor = monitor;
	}

	public void setOperation( CSearchOperation operation ) {
		_operation = operation;
	}
	
	public Set getMatches(){
		return _matches;
	}
	
	private class ActionGroupFactory implements IActionGroupFactory {
		public ActionGroup createActionGroup( ISearchResultView part ){
			return new CSearchViewActionGroup( part );
		}
	}
	
	public static class Match {
		public Match( String path, int start, int end ){
			this.path = path;
			this.start = start;
			this.end = end;
		}
	
		public String path;
		public int start;
		public int end;
	}
		
	private static final String SEARCHING = CSearchMessages.getString("CSearchResultCollector.searching"); //$NON-NLS-1$
	private static final String MATCH     = CSearchMessages.getString("CSearchResultCollector.match"); //$NON-NLS-1$
	private static final String MATCHES   = CSearchMessages.getString("CSearchResultCollector.matches"); //$NON-NLS-1$
	private static final String DONE      = CSearchMessages.getString("CSearchResultCollector.done"); //$NON-NLS-1$

		
	
	private IProgressMonitor 	_monitor;
	private CSearchOperation 	_operation;
	private ISearchResultView 	_view;
	private int					_matchCount;
	private Set					_matches;
}
