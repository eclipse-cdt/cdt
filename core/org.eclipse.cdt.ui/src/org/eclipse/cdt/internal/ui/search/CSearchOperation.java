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

import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchOperation implements IRunnableWithProgress,ICSearchConstants{
	public CSearchOperation(IWorkspace workspace, String pattern, boolean caseSensitive, List searchFor, LimitTo limitTo, ICSearchScope scope, String scopeDescription, CSearchResultCollector collector) {
		this( workspace, limitTo, scope, scopeDescription, collector );
		_stringPattern = pattern;
		_caseSensitive = caseSensitive;
		_searchFor = searchFor;
	}

	public CSearchOperation(IWorkspace workspace, LimitTo limitTo, ICSearchScope scope, String scopeDescription, CSearchResultCollector collector ){
		_workspace = workspace;
		_limitTo = limitTo;
		_scope = scope;
		_scopeDescription = scopeDescription;
		_collector = collector;
		_collector.setOperation( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor)throws InvocationTargetException
	{
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

	private CSearchResultCollector 	_collector;
	private IWorkspace 				_workspace;
	//private ICElement 				_elementPattern;	
	private ICSearchScope			_scope;
	private String					_stringPattern;
	private String					_scopeDescription;
	private boolean					_caseSensitive;
	private LimitTo					_limitTo;
	private List					_searchFor;


		
}
