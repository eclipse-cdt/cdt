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

import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.ui.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.search.ui.IGroupByKeyComputer;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.SearchUI;


/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchResultCollector extends BasicSearchResultCollector{
	
	public static final String IMATCH = "IMatchObject";
	
	/**
	 * 
	 */
	public CSearchResultCollector() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#aboutToStart()
	 */
	public void aboutToStart() {
		super.aboutToStart();
		
		_matchCount = 0;
		
		_view = SearchUI.getSearchResultView();
		
		CSearchResultLabelProvider labelProvider = new CSearchResultLabelProvider();
		labelProvider.setOrder( CSearchResultLabelProvider.SHOW_PATH );
		
		_computer = new GroupByKeyComputer();
		
		if( _view != null ){
			_view.searchStarted(
				null,//new ActionGroupFactory(),
				_operation.getSingularLabel(),
				_operation.getPluralLabelPattern(),
				_operation.getImageDescriptor(),
				CSearchPage.EXTENSION_POINT_ID,
				labelProvider,
				new GotoMarkerAction(),
				_computer,
				_operation
			);
		}
		
		if( getProgressMonitor() != null && !getProgressMonitor().isCanceled() ){
			getProgressMonitor().subTask( SEARCHING );
		}
	}

	public boolean acceptMatch( IMatch match ) throws CoreException
	{
		BasicSearchMatch searchMatch = (BasicSearchMatch) match;
		if( searchMatch.resource == null  )
			return false;
					
		if( !super.acceptMatch( match ) )
			return false;
		 
		IMarker marker =  searchMatch.resource.createMarker( SearchUI.SEARCH_MARKER );
	
		HashMap markerAttributes = new HashMap( 2 );
		
		//we can hang any other info we want off the marker
		markerAttributes.put( IMarker.CHAR_START, new Integer( Math.max( searchMatch.startOffset, 0 ) ) );		
		markerAttributes.put( IMarker.CHAR_END,   new Integer( Math.max( searchMatch.endOffset, 0 ) ) );
		markerAttributes.put( IMATCH, searchMatch );
		
		marker.setAttributes( markerAttributes );
		
		if( _view != null ){
			_view.addMatch( searchMatch.name, _computer.computeGroupByKey( marker ), searchMatch.resource, marker );		
		}

		_matchCount++;
		
		return true;
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
	
	private static final String SEARCHING = CSearchMessages.getString("CSearchResultCollector.searching"); //$NON-NLS-1$
	private static final String MATCH     = CSearchMessages.getString("CSearchResultCollector.match"); //$NON-NLS-1$
	private static final String MATCHES   = CSearchMessages.getString("CSearchResultCollector.matches"); //$NON-NLS-1$
	private static final String DONE      = CSearchMessages.getString("CSearchResultCollector.done"); //$NON-NLS-1$
	
	private IProgressMonitor 	_monitor;
	private CSearchOperation 	_operation;
	private ISearchResultView 	_view;
	private IGroupByKeyComputer _computer;
	private int					_matchCount;
}