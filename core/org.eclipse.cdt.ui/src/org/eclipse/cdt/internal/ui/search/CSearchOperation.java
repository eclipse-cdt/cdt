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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchOperation extends WorkspaceModifyOperation implements ICSearchConstants{

	public CSearchOperation(IWorkspace workspace, ICElement element, LimitTo limitTo, ICSearchScope scope, String scopeDescription, CSearchResultCollector collector) {
		this( workspace, limitTo, scope, scopeDescription, collector );
		_elementPattern = element;
	}

	public CSearchOperation(IWorkspace workspace, String pattern, boolean caseSensitive, SearchFor searchFor, LimitTo limitTo, ICSearchScope scope, String scopeDescription, CSearchResultCollector collector) {
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
	protected void execute(IProgressMonitor monitor)
		throws CoreException, InvocationTargetException, InterruptedException 
	{
		_collector.setProgressMonitor( monitor );	
	
		SearchEngine engine = new SearchEngine( );
		if( _elementPattern != null ){
			engine.search( _workspace, _elementPattern, _limitTo, _scope, _collector );
		} else {
			ICSearchPattern pattern = SearchEngine.createSearchPattern( _stringPattern, _searchFor, _limitTo, _caseSensitive );
			engine.search( _workspace, pattern, _scope, _collector );
		}

	}
	
	/**
	 * @return
	 */
	public String getSingularLabel() {
		String desc = null;
		
		if( _elementPattern != null ){
			desc = _elementPattern.getElementName();
		} else {
			desc = _stringPattern; 
		}
		
		String [] args = new String [] { desc, _scopeDescription };

		if( _limitTo == DECLARATIONS ){
			return CSearchMessages.getFormattedString( "CSearchOperation.singularDeclarationsPostfix", args ); //$NON_NLS-1$
		} else if( _limitTo == REFERENCES ){
			return CSearchMessages.getFormattedString( "CSearchOperation.singularReferencesPostfix", args ); //$NON_NLS-1$
		} else {
			return CSearchMessages.getFormattedString( "CSearchOperation.singularOccurencesPostfix", args ); //$NON_NLS-1$
		}
	}

	/**
	 * @return
	 */
	public String getPluralLabelPattern() {
		String desc = null;
		
		if( _elementPattern != null ){
			desc = _elementPattern.getElementName();
		} else {
			desc = _stringPattern; 
		}
		
		String [] args = new String [] { desc, "{0}", _scopeDescription };
		if( _limitTo == DECLARATIONS ){
			return CSearchMessages.getFormattedString( "CSearchOperation.pluralDeclarationsPostfix", args ); //$NON_NLS-1$
		} else if ( _limitTo == REFERENCES ){
			return CSearchMessages.getFormattedString( "CSearchOperation.pluralReferencesPostfix", args ); //$NON_NLS-1$
		} else {
			return CSearchMessages.getFormattedString( "CSearchOperation.pluralOccurencesPostfix", args ); //$NON_NLS-1$
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
	private ICElement 				_elementPattern;	
	private ICSearchScope			_scope;
	private String					_stringPattern;
	private String					_scopeDescription;
	private boolean					_caseSensitive;
	private LimitTo					_limitTo;
	private SearchFor				_searchFor;

		
}
