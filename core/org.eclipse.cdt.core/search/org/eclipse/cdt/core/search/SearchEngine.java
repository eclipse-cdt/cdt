/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 11, 2003
 */
package org.eclipse.cdt.core.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.search.CSearchScope;
import org.eclipse.cdt.internal.core.search.CWorkspaceScope;
import org.eclipse.cdt.internal.core.search.PathCollector;
import org.eclipse.cdt.internal.core.search.PatternSearchJob;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.matching.CSearchPattern;
import org.eclipse.cdt.internal.core.search.matching.MatchLocator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SearchEngine implements ICSearchConstants{

	public static boolean VERBOSE = false;

	/**
	 * A list of working copies that take precedence over their original 
	 * compilation units.
	 */
	private IWorkingCopy[] workingCopies = null;
	private int waitingPolicy = ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH;
	
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
	 * Policy is one of ICSearchConstants.FORCE_IMMEDIATE_SEARCH, ICSearchConstants.CANCEL_IF_NOT_READY_TO_SEARCH
	 * or ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH
	 * @param policy
	 */
	public void setWaitingPolicy( int policy ){
		if( policy == FORCE_IMMEDIATE_SEARCH ||
			policy == CANCEL_IF_NOT_READY_TO_SEARCH ||
			policy == WAIT_UNTIL_READY_TO_SEARCH )
		{
			waitingPolicy = policy;
		}
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

	/**
	 * @param objects
	 * @return
	 */
	public static ICSearchScope createCFileSearchScope(IFile sourceFile, ArrayList elements) {
		CSearchScope scope = new CSearchScope();
		
		if (sourceFile != null){
			//Add the source file and project
			scope.addFile(sourceFile.getFullPath(), sourceFile.getProject());
			IPath rootPath = CCorePlugin.getWorkspace().getRoot().getLocation();
			int segCount = CCorePlugin.getWorkspace().getRoot().getLocation().segmentCount();
			if (elements!=null){
				Iterator i = elements.iterator();
				while (i.hasNext()){
				  IPath tempPath = new Path((String) i.next());
				  if (rootPath.isPrefixOf(tempPath)){
					//path is in workspace  
					IFile tempFile = CCorePlugin.getWorkspace().getRoot().getFile(tempPath);
					IPath finalPath = tempFile.getFullPath().removeFirstSegments(segCount);
					tempFile = CCorePlugin.getWorkspace().getRoot().getFile(finalPath);
					scope.addFile(tempFile.getFullPath(), tempFile.getProject());
				  }
				  else{
				  	scope.addFile(tempPath,null);
				  }
				  
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

	
	public void search(IWorkspace workspace, ICSearchPattern pattern, ICSearchScope scope, ICSearchResultCollector collector, boolean excludeLocalDeclarations) throws InterruptedException {

		MatchLocator matchLocator = new MatchLocator(pattern,collector,scope);
		matchLocator.setShouldExcludeLocalDeclarations(excludeLocalDeclarations);
		
		search(workspace, pattern, scope, collector, excludeLocalDeclarations, matchLocator);
	}			
	/**
	 * @param _workspace
	 * @param pattern
	 * @param _scope
	 * @param _collector
	 */
	public void search(IWorkspace workspace, ICSearchPattern pattern, ICSearchScope scope, ICSearchResultCollector collector, boolean excludeLocalDeclarations, IMatchLocator matchLocator) throws InterruptedException {
		if( VERBOSE ) {
			System.out.println("Searching for " + pattern + " in " + scope); //$NON-NLS-1$//$NON-NLS-2$
		}

		if( pattern == null ){
			return;
		}
		
		/* search is starting */
		collector.aboutToStart();

		//initialize progress monitor
		IProgressMonitor progressMonitor = collector.getProgressMonitor();
		if( progressMonitor != null ){
			progressMonitor.beginTask( Util.bind("engine.searching"), 100 ); //$NON-NLS-1$
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
		    waitingPolicy,
			subMonitor,
			null );
		
		subMonitor = (progressMonitor == null ) ? null : new SubProgressMonitor( progressMonitor, 95 );
		
		matchLocator.setProgressMonitor(subMonitor);
		
		if( progressMonitor != null && progressMonitor.isCanceled() )
			throw new InterruptedException();
		
		//indexer might have had a # files left to index subtask, replace that with searching again
		if( progressMonitor != null )
			progressMonitor.subTask( Util.bind( "engine.searching" ) );	//$NON-NLS-1$
		
		//String[] indexerPaths = pathCollector.getPaths();
		//BasicSearchMatch[] matches = pathCollector.getMatches();
		//pathCollector = null; // release
		
		//TODO: BOG Put MatchLocator in for Working Copy
		//matchLocator.locateMatches( indexerPaths, workspace, filterWorkingCopies(this.workingCopies, scope));
		Iterator i =pathCollector.getMatches();
		
		while (i.hasNext()){
			try {
				collector.acceptMatch((BasicSearchMatch) i.next() );
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		collector.done();
	}

	/**
	 * @param copies
	 * @param scope
	 * @return
	 */
	private IWorkingCopy[] filterWorkingCopies(IWorkingCopy[] copies, ICSearchScope scope) {
		
		if (copies == null || 
			copies.length == 0)
			return copies;
		
		int length = copies.length;
		IWorkingCopy[] results= new IWorkingCopy[length];
		int index=0;

		try {
			for (int i=0;i<length;i++){
				IWorkingCopy workingCopy = copies[i];
				if(scope.encloses(workingCopy.getPath().toOSString()) &&
				  	 workingCopy.hasUnsavedChanges()){
				  	results[index++]=workingCopy;
				  }
			}
		} catch (CModelException e) {}
		
		System.arraycopy(results,0,results= new IWorkingCopy[index],0,index);
		
		return results;
	}
}
