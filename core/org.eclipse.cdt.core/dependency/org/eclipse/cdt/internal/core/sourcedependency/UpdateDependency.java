/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Oct 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.sourcedependency;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.PathCollector;
import org.eclipse.cdt.internal.core.search.PatternSearchJob;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.matching.CSearchPattern;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author bgheorgh
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UpdateDependency implements IIndexJob {
	PathCollector pathCollector;
	IFile resource=null;
	SourceIndexer indexer;
	
	/**
	 * @param resource
	 */
	public UpdateDependency(IResource resource, SourceIndexer indexer) {
		if (resource instanceof IFile)
			this.resource = (IFile) resource;
		
		this.indexer = indexer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#belongsTo(java.lang.String)
	 */
	public boolean belongsTo(String jobFamily) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#cancel()
	 */
	public void cancel() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean execute(IProgressMonitor progress) {	
		if (resource == null) return false;	
		
		IPath location = resource.getLocation();
		if (location == null) return false;
		  
		PathCollector pathCollector = new PathCollector();
			//SubProgressMonitor subMonitor = (progressMonitor == null ) ? null : new SubProgressMonitor( progressMonitor, 5 );
			ICSearchScope scope = SearchEngine.createWorkspaceScope();
			CSearchPattern pattern = CSearchPattern.createPattern(location.toOSString(),ICSearchConstants.INCLUDE, ICSearchConstants.REFERENCES,ICSearchConstants.EXACT_MATCH,true);
			IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
			indexManager.performConcurrentJob( 
				new PatternSearchJob(
					pattern,
					scope,
					pathCollector,
					indexManager 
				),
				ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null,
				this );
					
		String[] iPath = pathCollector.getPaths();
		
		if (iPath.length > 0){
        //If we found any dependents on this header file, indexing them will also index the header
			for (int i=0;i<iPath.length; i++){
				IPath pathToReindex = new Path(iPath[i]);
				IWorkspaceRoot workRoot = resource.getWorkspace().getRoot();
				IFile fileToReindex = workRoot.getFile(pathToReindex);
					
				if (fileToReindex!=null && fileToReindex.exists() ) {
	//			if (VERBOSE)
	//			 System.out.println("Going to reindex " + fileToReindex.getName());
				 indexer.addSource(fileToReindex,fileToReindex.getProject().getFullPath());
				}
			}
		}
		else {
			//No dependents found, just index the stand alone header	
			indexer.addSource(resource,resource.getProject().getFullPath());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#isReadyToRun()
	 */
	public boolean isReadyToRun() {
	
		return true;
	}

}
