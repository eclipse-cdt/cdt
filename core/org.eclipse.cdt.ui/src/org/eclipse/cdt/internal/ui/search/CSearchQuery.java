/*
 * Created on Mar 26, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
/**
 * @author bog 
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CSearchQuery implements ISearchQuery, ICSearchConstants {
	
	private CSearchResultCollector 	_collector;
	private IWorkspace 				_workspace;	
	private ICSearchScope			_scope;
	private String					_stringPattern;
	private String					_scopeDescription;
	private boolean					_caseSensitive;
	private LimitTo					_limitTo;
	private List					_searchFor;
	
	public CSearchQuery(IWorkspace workspace, String pattern, boolean caseSensitive, List searchFor, LimitTo limitTo, ICSearchScope scope, String scopeDescription, CSearchResultCollector collector) {
		this( workspace, limitTo, scope, scopeDescription, collector );
		_stringPattern = pattern;
		_caseSensitive = caseSensitive;
		_searchFor = searchFor;
	}

	public CSearchQuery(IWorkspace workspace, LimitTo limitTo, ICSearchScope scope, String scopeDescription, CSearchResultCollector collector ){
		_workspace = workspace;
		_limitTo = limitTo;
		_scope = scope;
		_scopeDescription = scopeDescription;
		_collector = collector;
		_collector.setOperation( this );
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
		_collector.setProgressMonitor( monitor );	
		
		SearchEngine engine = new SearchEngine( CUIPlugin.getSharedWorkingCopies() );

		ICSearchPattern pattern = null;
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
			engine.search( _workspace, pattern, _scope, _collector, false );
		} catch (InterruptedException e) {
		}
		
		return new Status(IStatus.OK, CUIPlugin.getPluginId(),0,"",null); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#getLabel()
	 */
	public String getLabel() {
		if (_limitTo == REFERENCES)
			return CSearchMessages.getString("CSearchQuery.searchfor_references"); //$NON-NLS-1$
		else if (_limitTo == DECLARATIONS)
			return CSearchMessages.getString("CSearchQuery.searchfor_declarations"); //$NON-NLS-1$
		else if (_limitTo == DEFINITIONS)
			return CSearchMessages.getString("CSearchQuery.searchfor_definitions"); //$NON-NLS-1$
		else if (_limitTo == ALL_OCCURRENCES)
			return CSearchMessages.getString("CSearchQuery.searchfor_all"); //$NON-NLS-1$ 
		
		return CSearchMessages.getString("CSearchQuery.search_label"); //$NON-NLS-1$;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#canRerun()
	 */
	public boolean canRerun() {
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}
}
