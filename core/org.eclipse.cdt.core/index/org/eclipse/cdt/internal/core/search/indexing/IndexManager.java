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
package org.eclipse.cdt.internal.core.search.indexing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexStorage;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.index.sourceindexer.CIndexStorage;
import org.eclipse.cdt.internal.core.index.sourceindexer.IndexRequest;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Bogdan Gheorghe
 */
public class IndexManager extends JobManager{
	
	public final static String INDEX_MODEL_ID = CCorePlugin.PLUGIN_ID + ".cdtindexers"; //$NON-NLS-1$
	public final static String INDEXERID = "indexerID"; //$NON-NLS-1$
	public final static QualifiedName indexerIDKey = new QualifiedName(INDEX_MODEL_ID, INDEXERID);
	
	public static final String CDT_INDEXER = "cdt_indexer"; //$NON-NLS-1$
	public static final String INDEXER_ID = "indexerID"; //$NON-NLS-1$
	public static final String INDEXER_ID_VALUE = "indexerIDValue"; //$NON-NLS-1$

	public static boolean VERBOSE = false;
	
    //Map of Contributed Indexers; keyed by project
    private HashMap contributedIndexerMap = null;
   
    //Map of Persisted Indexers; keyed by project
    private HashMap indexerMap = null;
   
    /**
     * Create an indexer only on request
     */
    protected static class CDTIndexer {

        IConfigurationElement element;
        ICDTIndexer indexer;
        
        public CDTIndexer(IConfigurationElement _element) {
            element = _element;
        }

        public ICDTIndexer getIndexer() throws CoreException {
            if (indexer == null) {
            	indexer = (ICDTIndexer) element.createExecutableExtension("class"); //$NON-NLS-1$
            }
            return indexer;
        }

        public String getName() {
            return element.getAttribute("name"); //$NON-NLS-1$
        }
        
    }
    
    
	/**
	 * Flush current state
	 */
	public void reset() {
		super.reset();
		
		initializeIndexersMap();
		this.indexerMap = new HashMap(5);
	}
	


	
	public synchronized String getIndexerID(IProject project) throws CoreException {
		//See if there's already one associated with the resource for this session
		 String indexerID = (String) project.getSessionProperty(indexerIDKey);

		// Try to load one for the project
		if (indexerID == null) {
			indexerID = loadIndexerIDFromCDescriptor(project);
		}
	
		// There is nothing persisted for the session, or saved in a file so
		// create a build info object
		if (indexerID != null) {
			project.setSessionProperty(indexerIDKey, indexerID);
		}
		else{
			//Hmm, no persisted indexer value. Could be an old project - need to run project
			//update code here	
		}
		
		return indexerID;
	}
	
	/**
	 * Loads indexerID from .cdtproject file
	 * @param project
	 * @param includes
	 * @param symbols
	 * @throws CoreException
	 */
	private String loadIndexerIDFromCDescriptor(IProject project) throws CoreException {
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project, true);
		
		Node child = descriptor.getProjectData(CDT_INDEXER).getFirstChild();
		
		String indexerID = null;
		
		while (child != null) {
			if (child.getNodeName().equals(INDEXER_ID)) 
				  indexerID = ((Element)child).getAttribute(INDEXER_ID_VALUE);
			
			child = child.getNextSibling();
		}
		
		return indexerID;
	}
	
	
	  /**
     * Adds all the contributed Indexer Pages to a map
     */
    private void initializeIndexersMap() {
    	
        contributedIndexerMap = new HashMap(5);
        
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, "CIndexer"); //$NON-NLS-1$
        IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getName().equals("indexer")) { //$NON-NLS-1$
                String id = infos[i].getAttribute("id"); //$NON-NLS-1$
                contributedIndexerMap.put(id, new CDTIndexer(infos[i]));
            }
        }
    }

	/**
	 * Notify indexer which scheduled this job that the job has completed  
	 * 
	 */
	protected synchronized void jobFinishedNotification(IIndexJob job) {
		if (job instanceof IndexRequest ){
			IndexRequest indexRequest = (IndexRequest) job;
			IPath path = indexRequest.getIndexPath();
			IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(path.toOSString()); 
			ICDTIndexer indexer = getIndexerForProject(project);
			
			if (indexer != null)
				indexer.indexJobFinishedNotification(job);
		}
	}




	/**
	 * @param project
	 * @param element
	 * @param delta
	 */
	public void addResourceEvent(IProject project, ICElement element, IResourceDelta delta) {
		//Get indexer for this project
		ICDTIndexer indexer = getIndexerForProject(project);	
		
		if (indexer != null)
			indexer.addRequest(element, delta);
		else{
		//Maybe indexer hasn't been created for this project yet
		//Scenarios:
		//1) New Project created - UI has set env var telling which indexer to use
		//2) Existing Project - the indexer has been persisted to file, need to load it up from CCorePlugin
			
		}
	}

	/**
	 * @param project
	 * @param element
	 * @param delta
	 */
	public void removeResourceEvent(IProject project, ICElement element, IResourceDelta delta) {
		//Get the indexer for this project
		ICDTIndexer indexer = null;
		indexer = (ICDTIndexer) indexerMap.get(project);
		
		if (indexer != null)
			indexer.removeRequest(element, delta);
	}
	
	
	/**
	 * Name of the background process
	 */
	public String processName(){
		return org.eclipse.cdt.internal.core.Util.bind("process.name"); //$NON-NLS-1$
	}
	


	public void shutdown() {
		//Send shutdown messages to all indexers
		
		/*if (IndexManager.VERBOSE)
			JobManager.verbose("Shutdown"); //$NON-NLS-1$
		//Get index entries for all projects in the workspace, store their absolute paths
		IndexSelector indexSelector = new IndexSelector(new CWorkspaceScope(), null, false, this);
		IIndex[] selectedIndexes = indexSelector.getIndexes();
		SimpleLookupTable knownPaths = new SimpleLookupTable();
		for (int i = 0, max = selectedIndexes.length; i < max; i++) {
			String path = selectedIndexes[i].getIndexFile().getAbsolutePath();
			knownPaths.put(path, path);
		}
		//Any index entries that are in the index state must have a corresponding
		//path entry - if not they are removed from the saved indexes file
		if (indexStates != null) {
			Object[] indexNames = indexStates.keyTable;
			for (int i = 0, l = indexNames.length; i < l; i++) {
				String key = (String) indexNames[i];
				if (key != null && !knownPaths.containsKey(key)) //here is an index that is in t
					updateIndexState(key, null);
			}
		}

		//Clean up the .metadata folder - if there are any files in the directory that
		//are not associated to an index we delete them
		File indexesDirectory = new File(getCCorePluginWorkingLocation().toOSString());
		if (indexesDirectory.isDirectory()) {
			File[] indexesFiles = indexesDirectory.listFiles();
			if (indexesFiles != null) {
				for (int i = 0, indexesFilesLength = indexesFiles.length; i < indexesFilesLength; i++) {
					String fileName = indexesFiles[i].getAbsolutePath();
					if (!knownPaths.containsKey(fileName) && fileName.toLowerCase().endsWith(".index")) { //$NON-NLS-1$
						if (IndexManager.VERBOSE)
							JobManager.verbose("Deleting index file " + indexesFiles[i]); //$NON-NLS-1$
						indexesFiles[i].delete();
					}
				}
			}
		}
		
		indexModelListener.shutdown();
		
		this.timeoutThread = null;*/
		
		//Send shutdown notification to all indexers
		if (indexerMap != null){
			Set projects = indexerMap.keySet();
			Iterator i = projects.iterator();
			while (i.hasNext()){
				IProject tempProject = (IProject) i.next();
		   		ICDTIndexer indexer = (ICDTIndexer) indexerMap.get(tempProject);
		   		if (indexer != null)
		   			indexer.shutdown();
			}
		}
		
		super.shutdown();
	}
	
	public IIndexStorage getIndexStorageForIndexer(ICDTIndexer indexer){
		//For now we have only one index storage format that all indexers are to use
		return new CIndexStorage(indexer);
	}
	
	public synchronized int getJobStart(){
		return jobStart;
	}
	
	public synchronized int getJobEnd(){
		return jobEnd;
	}

	/**
	 * Returns the job at position in the awaiting job queue
	 * @param position
	 * @return
	 */
	public synchronized IIndexJob getAwaitingJobAt(int position){
		return this.awaitingJobs[position];
	}




	/**
	 * Check to see if the indexer associated with this project
	 * requires dependency update notifications
	 * @param resource
	 * @param resource2
	 */
	public void updateDependencies(IProject project, IResource resource) {
		ICDTIndexer indexer = getIndexerForProject(project);
		if (indexer instanceof SourceIndexer)
			((SourceIndexer) indexer).updateDependencies(resource);
		
	}
	
	public synchronized ICDTIndexer getIndexerForProject(IProject project){
		
		ICDTIndexer indexer = null;
		indexer = (ICDTIndexer) indexerMap.get(project);
		
		if (indexer == null){
			String indexerID = null;
			try {
				//Indexer has not been created yet for this session
				//Check to see if the indexer has been set in a session property 
				indexerID = (String) project.getSessionProperty(indexerIDKey);
			} catch (CoreException e) {}
			
			if (indexerID == null){
				try{
				//Need to load the indexer from descriptor
				indexerID = loadIndexerIDFromCDescriptor(project);
				} catch (CoreException e){}
			}
			
			//Make sure that we have an indexer ID
			if (indexerID == null)
				return null;
			
			//Create the indexer and store it
			indexer = getIndexer(indexerID);
			indexerMap.put(project,indexer);
			
		}
		return indexer;
	}
	
   protected ICDTIndexer getIndexer(String indexerId) {
	    CDTIndexer configElement = (CDTIndexer) contributedIndexerMap.get(indexerId);
	    if (configElement != null) {
	        try {
	            return configElement.getIndexer();
	        } catch (CoreException e) {}
	    }
	    return null;
   }
   
   protected void notifyIdle(long idlingTime) {
   	//Notify all indexers
   	if (indexerMap == null)
   		return;
   		
   	Set mapKeys = indexerMap.keySet();
   	Iterator i = mapKeys.iterator();
   	while (i.hasNext()){
   		IProject tempProject = (IProject) i.next();
   		ICDTIndexer indexer = (ICDTIndexer) indexerMap.get(tempProject);
   		indexer.notifyIdle(idlingTime);
   	}
   }
   
  
}
