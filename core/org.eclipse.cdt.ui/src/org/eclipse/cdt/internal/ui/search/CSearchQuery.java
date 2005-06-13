/*******************************************************************************
 * Copyright (c) 2004,2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Mar 26, 2004
 */
package org.eclipse.cdt.internal.ui.search;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
/**
 * @author bog 
 */
public class CSearchQuery implements ISearchQuery, ICSearchConstants {
	
	private IWorkspace 					_workspace;	
	private ICSearchScope				_scope;
	private String						_stringPattern;
	private String						_scopeDescription;
	private boolean						_caseSensitive;
	private LimitTo						_limitTo;
	private List						_searchFor;
	private CSearchResult  				_result;
	 
	public CSearchQuery(IWorkspace workspace, String pattern, boolean caseSensitive, List searchFor, LimitTo limitTo, ICSearchScope scope, String scopeDescription) {
		this( workspace, limitTo, scope, scopeDescription );
		_stringPattern = pattern;
		_caseSensitive = caseSensitive;
		_searchFor = searchFor;
	}

	public CSearchQuery(IWorkspace workspace, LimitTo limitTo, ICSearchScope scope, String scopeDescription){
		_workspace = workspace;
		_limitTo = limitTo;
		_scope = scope;
		_scopeDescription = scopeDescription;
	}

	/**
	 * @return
	 */
	public String getSingularLabel() {
		String desc = null;
		
		//if( _elementPattern != null ){
		//	desc = _elementPattern.getElementName();
		//} else {
			desc = _stringPattern; 
		//}
		
		String [] args = new String [] { desc, _scopeDescription };

		if( _limitTo == DECLARATIONS ){
			return CSearchMessages.getFormattedString( "CSearchOperation.singularDeclarationsPostfix", args ); //$NON_NLS-1$ //$NON-NLS-1$
		} else if( _limitTo == REFERENCES ){
			return CSearchMessages.getFormattedString( "CSearchOperation.singularReferencesPostfix", args ); //$NON_NLS-1$ //$NON-NLS-1$
		} else {
			return CSearchMessages.getFormattedString( "CSearchOperation.singularOccurrencesPostfix", args ); //$NON_NLS-1$ //$NON-NLS-1$
		}
	}

	/**
	 * @return
	 */
	public String getPluralLabelPattern() {
		String desc = null;
		
	//	if( _elementPattern != null ){
	//		desc = _elementPattern.getElementName();
	//	} else {
			desc = _stringPattern; 
	//	}
		
		String [] args = new String [] { desc, "{0}", _scopeDescription }; //$NON-NLS-1$
		if( _limitTo == DECLARATIONS ){
			return CSearchMessages.getFormattedString( "CSearchOperation.pluralDeclarationsPostfix", args ); //$NON_NLS-1$ //$NON-NLS-1$
		} else if ( _limitTo == REFERENCES ){
			return CSearchMessages.getFormattedString( "CSearchOperation.pluralReferencesPostfix", args ); //$NON_NLS-1$ //$NON-NLS-1$
		} else {
			return CSearchMessages.getFormattedString( "CSearchOperation.pluralOccurrencesPostfix", args ); //$NON_NLS-1$ //$NON-NLS-1$
		}
	}

	/**
	 * @return
	 */
	public ImageDescriptor getImageDescriptor() {
		if( _limitTo == ICSearchConstants.DECLARATIONS ){
			return CPluginImages.DESC_OBJS_SEARCH_DECL;
		} else {
			return CPluginImages.DESC_OBJS_SEARCH_REF;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		
		final CSearchResult textResult= (CSearchResult) getSearchResult();
		textResult.removeAll();
		
		SearchEngine engine = new SearchEngine( CUIPlugin.getSharedWorkingCopies() );

		int totalTicks= 1000;

		monitor.beginTask("", totalTicks); //$NON-NLS-1$
		IProgressMonitor mainSearchPM= new SubProgressMonitor(monitor, 1000);

		NewSearchResultCollector finalCollector= new NewSearchResultCollector(textResult, mainSearchPM);

		ICSearchPattern pattern = null;
		if( _searchFor.size() > 0 ){
			if( _searchFor.size() > 1 ){
				OrPattern orPattern = new OrPattern();
				for (Iterator iter = _searchFor.iterator(); iter.hasNext();) {
					SearchFor element = (SearchFor)iter.next();
					orPattern.addPattern( SearchEngine.createSearchPattern( _stringPattern, element, _limitTo, _caseSensitive ) );	
				}
				
				pattern = orPattern;
				
			} else {
				Iterator iter = _searchFor.iterator();
				pattern = SearchEngine.createSearchPattern( _stringPattern, (SearchFor)iter.next(), _limitTo, _caseSensitive );
			}
			
			try {
				engine.search( _workspace, pattern, _scope, finalCollector, false );
			} catch (InterruptedException e) {
			}
		}
		monitor.done();
		
		return new Status(IStatus.OK, CUIPlugin.getPluginId(), 0,"", null); //$NON-NLS-1$	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#getLabel()
	 */
	public String getLabel() {
		String label;
		if (_limitTo == REFERENCES)
			label = CSearchMessages.getString("CSearchQuery.searchfor_references"); //$NON-NLS-1$
		else if (_limitTo == DECLARATIONS)
			label = CSearchMessages.getString("CSearchQuery.searchfor_declarations"); //$NON-NLS-1$
		else if (_limitTo == DEFINITIONS)
			label = CSearchMessages.getString("CSearchQuery.searchfor_definitions"); //$NON-NLS-1$
		else if (_limitTo == ALL_OCCURRENCES)
			label = CSearchMessages.getString("CSearchQuery.searchfor_all"); //$NON-NLS-1$ 
		else
			label = CSearchMessages.getString("CSearchQuery.search_label"); //$NON-NLS-1$;
		
		label += " \""; //$NON-NLS-1$
		label += _stringPattern;
		label += '"';
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#canRerun()
	 */
	public boolean canRerun() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
	 */
	public boolean canRunInBackground() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#getSearchResult()
	 */
	public ISearchResult getSearchResult() {
		if (_result == null)
			_result= new CSearchResult(this);
		return _result;
	}
		
}
