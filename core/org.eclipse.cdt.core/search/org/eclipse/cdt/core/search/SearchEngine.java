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
package org.eclipse.cdt.core.search;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.search.PatternSearchJob;
import org.eclipse.cdt.internal.core.search.Util;
import org.eclipse.cdt.internal.core.search.matching.CSearchPattern;
import org.eclipse.cdt.internal.core.search.matching.MatchLocator;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SearchEngine {

	private boolean VERBOSE = false;

	/**
	 * 
	 */
	public SearchEngine() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return
	 */
	public static ICSearchScope createWorkspaceScope() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param objects
	 * @return
	 */
	public static ICSearchScope createCSearchScope(Object[] objects) {
		// TODO Auto-generated method stub
		return null;
	}

	public static ICSearchPattern createSearchPattern( String stringPattern, int searchFor, int limitTo, boolean isCaseSensitive){
		int mode;
		
		if( stringPattern.indexOf( '*' ) != -1  || stringPattern.indexOf( '?' ) != -1 ){
			mode = ICSearchConstants.PATTERN_MATCH;		
		} else {
			mode = ICSearchConstants.EXACT_MATCH;
		}
		
		return CSearchPattern.createPattern( stringPattern, searchFor, limitTo, mode, isCaseSensitive );
	}

	/**
	 * @param _workspace
	 * @param pattern
	 * @param _scope
	 * @param _collector
	 */
	public void search(IWorkspace workspace, ICSearchPattern pattern, ICSearchScope scope, ICSearchResultCollector collector) {
		if( VERBOSE ) {
			System.out.println("Searching for " + pattern + " in " + scope); //$NON-NLS-1$//$NON-NLS-2$
		}

		if( pattern == null ){
			return;
		}
		
		/* search is starting */
		collector.aboutToStart();
		
		try{
			//initialize progress monitor
			IProgressMonitor progressMonitor = collector.getProgressMonitor();
			if( progressMonitor != null ){
				progressMonitor.beginTask( Util.bind("engine.searching"), 100 ); //$NON_NLS-1$
			}
			
			CModelManager modelManager = CModelManager.getDefault();
			IndexManager indexManager = modelManager.getIndexManager();
			
			SubProgressMonitor subMonitor = (progressMonitor == null ) ? null : new SubProgressMonitor( progressMonitor, 5 );
			
			indexManager.performConcurrentJob( 
				new PatternSearchJob(),
				ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				subMonitor );
			
			subMonitor = (progressMonitor == null ) ? null : new SubProgressMonitor( progressMonitor, 95 );
				
			MatchLocator matchLocator = new MatchLocator( pattern, collector, scope, subMonitor );
			
			if( progressMonitor != null && progressMonitor.isCanceled() )
				throw new OperationCanceledException();
			
			//matchLocator.locateMatches( pathCollector.getPaths(), workspace, workingCopies );
		} finally {
			collector.done();
		}
	}

	/**
	 * @param _workspace
	 * @param _elementPattern
	 * @param _limitTo
	 * @param _scope
	 * @param _collector
	 */
	public void search(IWorkspace workspace, ICElement elementPattern, int limitTo, ICSearchScope scope, ICSearchResultCollector collector) {
		// TODO Auto-generated method stub
		
	}
		
}
