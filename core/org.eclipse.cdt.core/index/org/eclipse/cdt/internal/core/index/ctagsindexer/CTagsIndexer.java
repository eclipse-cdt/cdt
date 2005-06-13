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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexStorage;
import org.eclipse.cdt.core.index.IndexChangeEvent;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.CIndexStorage;
import org.eclipse.cdt.internal.core.index.impl.IndexDelta;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Bogdan Gheorghe
 */
public class CTagsIndexer extends AbstractCExtension implements ICDTIndexer {

	public static boolean VERBOSE = false;
	
	public final static String CTAGS_INTERNAL = "ctags_internal"; //$NON-NLS-1$
	public final static String CTAGS_EXTERNAL = "ctags_external"; //$NON-NLS-1$
	public final static String CTAGS_LOCATION = "ctags_location"; //$NON-NLS-1$
	
	protected List indexChangeListeners = Collections.synchronizedList(new ArrayList());
	public static final String INDEX_NOTIFICATION_NAME = Util.bind( "indexNotificationJob" ); //$NON-NLS-1$
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
	        
			case ICDTIndexer.FOLDER:
				IFolder folder = (IFolder) delta.getResource();
				this.indexSourceFolder(project,folder);
			break;
		
			
			case ICDTIndexer.COMPILATION_UNIT:
				IFile file = (IFile) delta.getResource();
				this.addSource(file, project.getFullPath());
				break;		
				
			default:
			    this.indexAll(project);
			    break;
		}
		
		
	}

	public void indexSourceFolder(IProject project, IFolder folder) {
	
		final HashSet indexables = new HashSet();
		try {
			folder.accept(new IResourceVisitor(){
				public boolean visit(IResource resource) throws CoreException {
					if (resource instanceof IFile)
						indexables.add(resource);
					
					return true;
				} 
			});
		} catch (CoreException e) {}
		
		Iterator i = indexables.iterator();
		while (i.hasNext()){
			this.addSource((IFile) i.next(), project.getFullPath());
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
		indexStorage.setNeedToSave(true);
		
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
      	storageMonitor.enterRead();
		try{
			indexStorage.aboutToUpdateIndex(indexPath, indexState);
		}
		finally {
			storageMonitor.exitRead();
		}
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
        indexManager.removeIndexerProblems(project);
	}
	
	public void addIndexChangeListener(IIndexChangeListener listener) {
		synchronized(indexChangeListeners) {
			if (!indexChangeListeners.contains(listener)) {
				indexChangeListeners.add(listener);
			}
		}
	}
	
	public void removeIndexChangeListener(IIndexChangeListener listener) {
		synchronized(indexChangeListeners) {
			int i = indexChangeListeners.indexOf(listener);
			if (i != -1) {
				indexChangeListeners.remove(i);
			}
		}
	}
	/**
	 * @param indexDelta
	 */
	public void notifyListeners(IndexDelta indexDelta) {
		final IndexChangeEvent indexEvent = new IndexChangeEvent(indexDelta);
		for (int i= 0; i < indexChangeListeners.size(); i++) {
			    IIndexChangeListener tempListener = null;
			    synchronized(indexChangeListeners){
			    	tempListener = (IIndexChangeListener) indexChangeListeners.get(i);
			    }
			    final IIndexChangeListener listener = tempListener;
				long start = -1;
				if (VERBOSE) {
					System.out.print("Listener #" + (i+1) + "=" + listener.toString());//$NON-NLS-1$//$NON-NLS-2$
					start = System.currentTimeMillis();
				}
				
				// wrap callbacks with Safe runnable for subsequent listeners to be called when some are causing grief
				Job job = new Job(INDEX_NOTIFICATION_NAME){
					protected IStatus run(IProgressMonitor monitor)	{	
						Platform.run(new ISafeRunnable() {
							public void handleException(Throwable exception) {
								CCorePlugin.log(exception);
							}
							public void run() throws Exception {
								listener.indexChanged(indexEvent);
							}
						});
						
						return Status.OK_STATUS;
					}
				};
				
				job.schedule();
				if (VERBOSE) {
					System.out.println(" -> " + (System.currentTimeMillis()-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		
		}
	
	public boolean validCTagsInstalled(){
		String[] args = {"--version"}; //$NON-NLS-1$
    	
        String errMsg = null;
        CommandLauncher launcher = new CommandLauncher();
       
        CTagLineReader parser = new CTagLineReader();
        IConsoleParser[] parsers = { parser };
        ConsoleOutputSniffer sniffer = new ConsoleOutputSniffer(parsers);
        
        OutputStream consoleOut = sniffer.getOutputStream();
        OutputStream consoleErr = sniffer.getErrorStream();
        
        Process p = launcher.execute(new Path("ctags"), args,new String[0], new Path(".")); //$NON-NLS-1$ //$NON-NLS-2$
        if (p != null) {
            try {
                // Close the input of the Process explicitely.
                // We will never write to it.
                p.getOutputStream().close();
            } catch (IOException e) {}
            if (launcher.waitAndRead(consoleOut, consoleErr, new NullProgressMonitor()) != CommandLauncher.OK) {
                errMsg = launcher.getErrorMessage();
            }
        }
        else {
            errMsg = launcher.getErrorMessage();
            return false;
        }

        try {
			consoleOut.close();
			consoleErr.close();
		} catch (IOException e) {}
       
		
		if (parser.isExuberantCtags)
			return true;
		
		return false;
	}
	
	class CTagLineReader implements IConsoleParser{

		boolean isExuberantCtags=false;

		public boolean processLine(String line) {
			if (line.startsWith("Exuberant Ctags")) //$NON-NLS-1$
				isExuberantCtags=true;
			
			return true;
		}

		public void shutdown() {}
	}

	public void addResource(IProject project, IResource resource) {

		if (resource instanceof IProject){
		    this.indexAll(project);
		} 
		else if (resource instanceof IFolder){
			IFolder folder = (IFolder) resource;
			this.indexSourceFolder(project,folder);
		}
		else if (resource instanceof IFile){
			IFile file = (IFile) resource;
			this.addSource(file, project.getFullPath());
		}
		else {
		    this.indexAll(project);
		}
		
	}

	public void removeResource(IProject project, IResource resource) {
		if (resource instanceof IProject){
			IPath fullPath = project.getFullPath();
			indexManager.discardJobs(fullPath.segment(0));
			indexStorage.removeIndexFamily(fullPath);
		}
		else if (resource instanceof IFile){
			IFile file = (IFile) resource;
			this.remove(file.getFullPath().toString(), file.getProject().getFullPath());			
		}	
	}

	public void addResourceByPath(IProject project, IPath path, int resourceType) {
	  //Nothing yet
	}

}
