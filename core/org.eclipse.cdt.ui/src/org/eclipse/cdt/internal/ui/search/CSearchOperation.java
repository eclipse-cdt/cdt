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
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchOperation extends WorkspaceModifyOperation {

	public CSearchOperation(IWorkspace workspace, ICElement element, int limitTo, ICSearchScope scope, String scopeDescription, CSearchResultCollector collector) {
		this( workspace, limitTo, scope, scopeDescription, collector );
		_elementPattern = element;
	}

	public CSearchOperation(IWorkspace workspace, String pattern, boolean caseSensitive, int searchFor, int limitTo, ICSearchScope scope, String scopeDescription, CSearchResultCollector collector) {
		this( workspace, limitTo, scope, scopeDescription, collector );
		_stringPattern = pattern;
		_caseSensitive = caseSensitive;
		_searchFor = searchFor;
	}

	public CSearchOperation(IWorkspace workspace, int limitTo, ICSearchScope scope, String scopeDescription, CSearchResultCollector collector ){
		_workspace = workspace;
		_limitTo = limitTo;
		_scope = scope;
		_scopeDecsription = scopeDescription;
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


	private CSearchResultCollector 	_collector;
	private IWorkspace 				_workspace;
	private ICElement 				_elementPattern;	
	private ICSearchScope			_scope;
	private String					_stringPattern;
	private String					_scopeDecsription;
	private boolean					_caseSensitive;
	private int						_limitTo;
	private int						_searchFor;
		
}
