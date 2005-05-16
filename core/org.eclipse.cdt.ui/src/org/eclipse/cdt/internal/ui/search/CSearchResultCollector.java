/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jun 11, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import java.util.HashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.ui.CSearchResultLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
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
	
	public static final String IMATCH = CSearchMessages.getString("CSearchResultCollector.4"); //$NON-NLS-1$
	
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
		
		//_view = NewSearchUI.getSearchResultView();
	
		
		CSearchResultLabelProvider labelProvider = new CSearchResultLabelProvider();
		labelProvider.setOrder( CSearchResultLabelProvider.SHOW_PATH );
		
		_computer = new GroupByKeyComputer();
		
		if( _view != null ){
			if (_operation != null){
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
			else if (_query != null){
				_view.searchStarted(
					null,//new ActionGroupFactory(),
					_query.getSingularLabel(),
					_query.getPluralLabelPattern(),
					_query.getImageDescriptor(),
					CSearchPage.EXTENSION_POINT_ID,
					labelProvider,
					new GotoMarkerAction(),
					_computer,
					null
				);
			}
		}
		
		if( getProgressMonitor() != null && !getProgressMonitor().isCanceled() ){
			getProgressMonitor().subTask( SEARCHING );
		}
	}

	public boolean acceptMatch( IMatch match ) throws CoreException
	{
		BasicSearchMatch searchMatch = (BasicSearchMatch) match;
				
		if( !super.acceptMatch( match ) )
			return false;
		
		if( searchMatch.resource == null &&
			searchMatch.path == null)
			return false;
			 
	    if (searchMatch.resource != null){
			IMarker marker =  searchMatch.resource.createMarker( SearchUI.SEARCH_MARKER );
		
			HashMap markerAttributes = new HashMap( 2 );
			
			//we can hang any other info we want off the marker
			/*markerAttributes.put( IMarker.CHAR_START, new Integer( Math.max( searchMatch.startOffset, 0 ) ) );		
			markerAttributes.put( IMarker.CHAR_END,   new Integer( Math.max( searchMatch.endOffset, 0 ) ) );*/
			markerAttributes.put( IMATCH, searchMatch );
		
			marker.setAttributes( markerAttributes );
			
			if( _view != null ){
				_view.addMatch( searchMatch.name, _computer.computeGroupByKey( marker ), searchMatch.resource, marker );		
			}
	    }
	    else {
	    	//Check to see if external markers are enabled
	    	IPreferenceStore store =  CUIPlugin.getDefault().getPreferenceStore();
	    	if (store.getBoolean(CSearchPage.EXTERNALMATCH_ENABLED)){
		    	//Create Link in referring file's project
		    	IPath refLocation = searchMatch.getReferenceLocation();
		    	IFile refFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(refLocation);
		    	IProject refProject = refFile.getProject();
		    	IPath externalMatchLocation = searchMatch.getLocation();
		    	IFile linksFile = refProject.getFile(externalMatchLocation.lastSegment());
		        //Delete links file to keep up to date with latest prefs
		    	if (linksFile.exists())
		    		linksFile.delete(true,null);
		    	
		    	//Check to see if the file already exists - create if doesn't, mark team private 
		    	if (!linksFile.exists()){
		    		linksFile.createLink(externalMatchLocation,IResource.NONE,null);
		    		int number = store.getInt(CSearchPage.EXTERNALMATCH_VISIBLE);
		    		if (number==0){
		    			linksFile.setDerived(true);
		    		}
		    		else{
		    			linksFile.setTeamPrivateMember(true);
		    		}
		    		
		    	}
		    	
		    	IMarker marker =  linksFile.createMarker( SearchUI.SEARCH_MARKER );
				
				HashMap markerAttributes = new HashMap( 2 );
				
				/*markerAttributes.put( IMarker.CHAR_START, new Integer( Math.max( searchMatch.startOffset, 0 ) ) );		
				markerAttributes.put( IMarker.CHAR_END,   new Integer( Math.max( searchMatch.endOffset, 0 ) ) );*/
				markerAttributes.put( IMATCH, searchMatch );
	
				marker.setAttributes( markerAttributes );
				
				if( _view != null ){
					_view.addMatch( searchMatch.name, _computer.computeGroupByKey( marker ), linksFile, marker );		
				}
	    	}	
	    }
		_matchCount++;
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#done()
	 */
	public void done() {
	/*	if( !getProgressMonitor().isCanceled() ){
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
		_monitor = null;*/
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
	
	private IProgressMonitor 	  _monitor;
	private CSearchOperation 	  _operation;
	private ISearchResultView     _view;
	private IGroupByKeyComputer   _computer;
	private int					  _matchCount;
	private CSearchQuery 		  _query;

	/**
	 * @param query
	 */
	public void setOperation(CSearchQuery query) {
		_query = query;
	}
}
