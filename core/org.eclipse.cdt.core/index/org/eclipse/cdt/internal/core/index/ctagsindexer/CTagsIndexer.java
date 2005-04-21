/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.index.ctagsindexer;

import java.io.IOException;
import java.util.HashSet;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexStorage;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.index.sourceindexer.CIndexStorage;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @author Bogdan Gheorghe
 */
public class CTagsIndexer extends AbstractCExtension implements ICDTIndexer {

	private CIndexStorage		indexStorage = null;
	public 	ReadWriteMonitor	storageMonitor = null;
	private IndexManager  		indexManager = null; 
	
	private HashSet 			jobSet = null;
	
	public CTagsIndexer(){
	    this.indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
    	this.indexStorage = (CIndexStorage) indexManager.getIndexStorageForIndexer(this);
    	this.jobSet	= new HashSet();
    	this.storageMonitor = new ReadWriteMonitor();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.ICDTIndexer#getIndexerFeatures()
	 */
	public int getIndexerFeatures() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.ICDTIndexer#addRequest(org.eclipse.cdt.core.model.ICElement, org.eclipse.core.resources.IResourceDelta)
	 */
	public void addRequest(IProject project, IResourceDelta delta, int kind) {
	    
		switch (kind) {
		 
			case ICDTIndexer.PROJECT:
			    this.indexAll(project);
				break;
	        
			/*case ICDTIndexer.FOLDER:
				this.indexSourceFolder(element.getCProject().getProject(),element.getPath(),null);
			break;*/
		
			
			case ICDTIndexer.COMPILATION_UNIT:
				IFile file = (IFile) delta.getResource();
				this.addSource(file, project.getFullPath());
				break;		
				
			default:
			    this.indexAll(project);
			    break;
		}
		
		
	}

	/**
	 * @param project
	 */
	public void indexAll(IProject project) {
	    CTagsIndexRequest request = new CTagsIndexAll(project, this);
		for (int i = indexManager.getJobEnd(); i > indexManager.getJobStart(); i--) // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
			if (request.equals(indexManager.getAwaitingJobAt(i))) return;
		indexManager.request(request);	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.ICDTIndexer#removeRequest(org.eclipse.cdt.core.model.ICElement, org.eclipse.core.resources.IResourceDelta)
	 */
	public void removeRequest(IProject project, IResourceDelta delta, int kind) {
		switch (kind) {
		case ICDTIndexer.PROJECT :
			IPath fullPath = project.getFullPath();
			if( delta.getKind() == IResourceDelta.CHANGED )
				indexManager.discardJobs(fullPath.segment(0));
			indexStorage.removeIndexFamily(fullPath);
			// NB: Discarding index jobs belonging to this project was done during PRE_DELETE
			break;
			// NB: Update of index if project is opened, closed, or its c nature is added or removed
			//     is done in updateCurrentDeltaAndIndex
		
		/*
		  case ICDTIndexer.FOLDER :
			this.removeSourceFolderFromIndex(project,project.getFullPath(),null);
			break;*/
		
		case ICDTIndexer.COMPILATION_UNIT:
			IFile file = (IFile) delta.getResource();
			this.remove(file.getFullPath().toString(), file.getProject().getFullPath());
			break;				
	}	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.ICDTIndexer#indexJobFinishedNotification(org.eclipse.cdt.internal.core.search.processing.IIndexJob)
	 */
	public void indexJobFinishedNotification(IIndexJob job) {
		((CIndexStorage)indexStorage).setNeedToSave(true);
		
		if (job instanceof CTagsAddCompilationUnitToIndex){
		    CTagsAddCompilationUnitToIndex tempJob = (CTagsAddCompilationUnitToIndex) job;
			jobSet.remove(tempJob.getResource().getLocation());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.ICDTIndexer#shutdown()
	 */
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.ICDTIndexer#notifyIdle(long)
	 */
	public void notifyIdle(long idlingTime) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.IIndexer#index(org.eclipse.cdt.internal.core.index.IDocument, org.eclipse.cdt.internal.core.index.IIndexerOutput)
	 */
	public void index(IFile document, IIndexerOutput output)
			throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.IIndexer#shouldIndex(org.eclipse.core.resources.IFile)
	 */
	public boolean shouldIndex(IFile file) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param path
	 * @param reuseIndexFile
	 * @param createIfMissing
	 * @return
	 */
	public synchronized IIndex getIndex(IPath path, boolean reuseExistingFile, boolean createIfMissing) {
		IIndex index = null;
		try{
			storageMonitor.enterRead();
			index = indexStorage.getIndex(path,reuseExistingFile, createIfMissing);
		}
		finally{
			storageMonitor.exitRead();
		}
		return index;
	}
	
	/**
	 * @param index
	 * @return
	 */
	public ReadWriteMonitor getMonitorFor(IIndex index) {
		ReadWriteMonitor monitor = null;
		try{
			storageMonitor.enterRead();
			monitor=indexStorage.getMonitorForIndex();
		}
		finally{
			storageMonitor.exitRead();
		}
		return monitor;
	}

	/**
	 * @param index
	 */
	public void saveIndex(IIndex index) throws IOException {
		try{
			storageMonitor.enterWrite();
			indexStorage.saveIndex(index);
		} 
		finally {
			storageMonitor.exitWrite();
		}
		
	}
	
	/**
	 * Forward job request to Index Manager
	 * @param cleanHeaders
	 */
	public void request(IIndexJob indexJob) {
		this.indexManager.request(indexJob);
	}

    /**
     * @param indexPath
     * @param integer
     */
    public void aboutToUpdateIndex(IPath indexPath, Integer indexState) {
        indexStorage.aboutToUpdateIndex(indexPath, indexState);        
    }

    /**
     * @param project
     * @return
     */
    public boolean isIndexEnabled(IProject project) {
        //Return true for now
        return true;
    }
    
	/**
	 * @param path
	 */
	public void jobWasCancelled(IPath path) {
	 try{
	 	storageMonitor.enterWrite();
	 	indexStorage.jobWasCancelled(path);
	 }
	 finally{
	 	storageMonitor.exitWrite();
	 }
	}

    /**
     * @param request
     */
    public void jobFinishedNotification(CTagsIndexRequest request) {
        this.indexJobFinishedNotification(request);
    }

    /**
     * @param path
     */
    public void removeIndex(IPath path) {
    	try{
			storageMonitor.enterWrite();
			indexStorage.removeIndex(path);
		}
		finally{
			storageMonitor.exitWrite();
		}
    }
    
	/**
	 * Trigger removal of a resource to an index
	 * Note: the actual operation is performed in background
	 */
	public void remove(String resourceName, IPath indexedContainer){
		IProject project = CCorePlugin.getWorkspace().getRoot().getProject(indexedContainer.toString());
      	if( isIndexEnabled( project ) )
      		request(new CTagsRemoveFromIndex(resourceName, indexedContainer, this));
	}

    /**
     * @param file
     * @param path
     */
    public void addSource(IFile resource, IPath indexedContainers) {
        IProject project = resource.getProject();
		
		boolean indexEnabled = false;
		if (project != null)
			indexEnabled = isIndexEnabled(project);
		else
			org.eclipse.cdt.internal.core.model.Util.log(null, "CTagsIndexer addSource: File has no project associated : " + resource.getName(), ICLogConstants.CDT); //$NON-NLS-1$ 
			
		if (CCorePlugin.getDefault() == null) return;	
		
		if (indexEnabled){
			CTagsAddCompilationUnitToIndex job = new CTagsAddCompilationUnitToIndex(resource, indexedContainers, this);

			//If we are in WAITING mode, we need to kick ourselves into enablement
			if (!jobSet.add(resource.getLocation()) &&
				indexManager.enabledState()==IndexManager.ENABLED)
				return;
			
			
			if (indexManager.awaitingJobsCount() < CIndexStorage.MAX_FILES_IN_MEMORY) {
				// reduces the chance that the file is open later on, preventing it from being deleted
				if (!job.initializeContents()) return;
			}
			
			this.indexManager.request(job);
		} 
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.index.ICDTIndexer#getIndexStorage()
     */
    public IIndexStorage getIndexStorage() {
        return indexStorage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.index.ICDTIndexer#notifyIndexerChange(org.eclipse.core.resources.IProject)
     */
    public void notifyIndexerChange(IProject project) {
       this.indexAll(project);
    }
	
	protected void createProblemMarker(String message, IProject project){

		try {
			IMarker[] markers = project.findMarkers(ICModelMarker.INDEXER_MARKER, true,IResource.DEPTH_ZERO);
		
           boolean newProblem = true;
           
           if (markers.length > 0) {
               IMarker tempMarker = null;
               String tempMsgString = null;
        
               for (int i=0; i<markers.length; i++) {
                   tempMarker = markers[i];
                   tempMsgString = (String) tempMarker.getAttribute(IMarker.MESSAGE);
                   if (tempMsgString.equalsIgnoreCase( message )){
                       newProblem = false;
                       break;
                   }
               }
           }
		  
		   if (newProblem){
		        IMarker marker = project.createMarker(ICModelMarker.INDEXER_MARKER);
		        int start = 0;
		        int end = 1;
		        marker.setAttribute(IMarker.MESSAGE, message); 
		        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		        marker.setAttribute(IMarker.CHAR_START, start);
		        marker.setAttribute(IMarker.CHAR_END, end); 
		   }
		} catch (CoreException e1) {}
	}

	public void indexerRemoved(IProject project) {
		//Remove any existing problem markers
		try {
			project.deleteMarkers(ICModelMarker.INDEXER_MARKER, true,IResource.DEPTH_ZERO);
		} catch (CoreException e) {}
		
	}
	

}
