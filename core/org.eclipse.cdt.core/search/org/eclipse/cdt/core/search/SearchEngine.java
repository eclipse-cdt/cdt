/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 11, 2003
 */
package org.eclipse.cdt.core.search;

import java.util.HashSet;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.search.CSearchScope;
import org.eclipse.cdt.internal.core.search.CWorkspaceScope;
import org.eclipse.cdt.internal.core.search.PathCollector;
import org.eclipse.cdt.internal.core.search.PatternSearchJob;
import org.eclipse.cdt.internal.core.search.Util;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
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
public class SearchEngine implements ICSearchConstants{

	private boolean VERBOSE = false;

	/**
	 * A list of working copies that take precedence over their original 
	 * compilation units.
	 */
	private IWorkingCopy[] workingCopies = null;
	
	/**
	 * 
	 */
	public SearchEngine() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SearchEngine(IWorkingCopy[] workingCopies) {
		this.workingCopies = workingCopies;
	}
	
	/**
	 * @return
	 */
	public static ICSearchScope createWorkspaceScope() {
		// TODO Auto-generated method stub
		return new CWorkspaceScope();
	}

	public static ICSearchScope createCSearchScope(ICElement[] elements) {
		return createCSearchScope(elements, true);
	}
	/**
	 * @param objects
	 * @return
	 */
	public static ICSearchScope createCSearchScope(ICElement[] elements, boolean includeReferencedProjects) {
		CSearchScope scope = new CSearchScope();
		HashSet visitedProjects = new HashSet(2);
		for (int i = 0, length = elements.length; i < length; i++) {
			ICElement element = elements[i];
			if (element != null) {
				try {
					if (element instanceof ICProject) {
						scope.add((ICProject)element, includeReferencedProjects, visitedProjects);
					} else {
						scope.add(element);
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return scope;
	}

	public static ICSearchPattern createSearchPattern( String stringPattern, SearchFor searchFor, LimitTo limitTo, boolean isCaseSensitive){
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
			
			/* index search */
			PathCollector pathCollector = new PathCollector();
					
			CModelManager modelManager = CModelManager.getDefault();
			IndexManager indexManager = modelManager.getIndexManager();
			
			SubProgressMonitor subMonitor = (progressMonitor == null ) ? null : new SubProgressMonitor( progressMonitor, 5 );
	
			indexManager.performConcurrentJob( 
				new PatternSearchJob(
					(CSearchPattern) pattern,
					scope,
					pathCollector,
					indexManager
				),
				ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				subMonitor );
			
			subMonitor = (progressMonitor == null ) ? null : new SubProgressMonitor( progressMonitor, 95 );
				
			MatchLocator matchLocator = new MatchLocator( pattern, collector, scope, subMonitor );
			
			if( progressMonitor != null && progressMonitor.isCanceled() )
				throw new OperationCanceledException();
			
			//TODO: BOG Filter Working Copies...
			matchLocator.locateMatches( pathCollector.getPaths(), workspace, this.workingCopies);
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
	public void search(IWorkspace workspace, ICElement elementPattern, LimitTo limitTo, ICSearchScope scope, ICSearchResultCollector collector) {
		// TODO Auto-generated method stub
		
	}
		
}
