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
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Bogdan Gheorghe
 */
public class IndexManager extends JobManager{
	
	public final static String INDEX_MODEL_ID = CCorePlugin.PLUGIN_ID + ".cdtindexers"; //$NON-NLS-1$
	public final static String INDEXERID = "indexerID"; //$NON-NLS-1$
	public final static QualifiedName indexerIDKey = new QualifiedName(INDEX_MODEL_ID, INDEXERID);
	
	public static final String nullIndexerID = "org.eclipse.cdt.core.nullindexer"; //$NON-NLS-1$
	
	public static final String CDT_INDEXER = "cdt_indexer"; //$NON-NLS-1$
	public static final String INDEXER_ID = "indexerID"; //$NON-NLS-1$
	public static final String INDEXER_ID_VALUE = "indexerIDValue"; //$NON-NLS-1$

	public static boolean VERBOSE = false;
	
    //Map of Contributed Indexers; keyed by project
    private HashMap contributedIndexerMap = null;
   
    //Map of Persisted Indexers; keyed by project
    private HashMap indexerMap = null;
   
    //Upgrade index version
    private boolean upgradeIndexEnabled = false;
    private int		upgradeIndexProblems = 0;
   
	private ReadWriteMonitor monitor = new ReadWriteMonitor();
	private boolean enableUpdates = true;
	
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
    
    private class UpdateIndexVersionJob extends Job{
		private final IProject project;
		public UpdateIndexVersionJob( IProject project, String name ){
			super( name );
			this.project = project;
		}

		protected IStatus run(IProgressMonitor monitor) {
			IWorkspaceRunnable job = new IWorkspaceRunnable( ){
				public void run(IProgressMonitor monitor){
					doProjectUpgrade(project);
					doSourceIndexerUpgrade(project);
				}
			};
			try {
				CCorePlugin.getWorkspace().run(job, project, 0, null);
			} catch (CoreException e) {
			}
			return Status.OK_STATUS;
		}
	}
	
    
    
	/**
	 * Flush current state
	 */
	public synchronized void reset() {
		super.reset();
		
		initializeIndexersMap();
		this.indexerMap = new HashMap(5);
		try{
		monitor.enterWrite();
		initializeIndexerID();
		} finally {
		monitor.exitWrite();
		}
	}
	


	
	/**
	 * 
	 */
	private void initializeIndexerID() {
		IProject[] projects = CCorePlugin.getWorkspace().getRoot().getProjects();
		//Make sure that all projects are added to the indexer map and updated
		//where neccesary
		for (int i=0; i<projects.length; i++){
			try {
				if (projects[i].isAccessible())
					initializeIndexer(projects[i]);
			} catch (CoreException e) {}
		}
		
	}

	private ICDTIndexer initializeIndexer(IProject project) throws CoreException {

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
			if (indexerID == null && 
				enableUpdates) {
				//No persisted info on file? Must be old project - run temp. upgrade
					UpdateIndexVersionJob job = new UpdateIndexVersionJob(project, "Update Index Version" ); //$NON-NLS-1$
				
					IProgressMonitor group = this.getIndexJobProgressGroup();
					
					job.setRule( project );
					if( group != null )
						job.setProgressGroup( group, 0 );
					job.setPriority( Job.SHORT );
					job.schedule();	
			}
			
			//If we're asking for the null indexer,return null
			if (indexerID == null ||
				indexerID.equals(nullIndexerID)) 
				return null;
			
			//Create the indexer and store it
			indexer = getIndexer(indexerID);
			indexerMap.put(project,indexer);
			
		}
		return indexer;
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
		//Make sure we're not updating list
		monitor.enterRead();
		//All indexers that were previously persisted should have been loaded at 
		//this point
		ICDTIndexer indexer = null;
		indexer = (ICDTIndexer) indexerMap.get(project);
		try {
			if (indexer == null){
				String indexerID = null;
				try {
					//Indexer has not been created yet for this session
					//Check to see if the indexer has been set in a session property 
					indexerID = (String) project.getSessionProperty(indexerIDKey);
				} catch (CoreException e) {}
				
				//Project was either closed at startup or imported
				if (indexerID == null &&
						project.isAccessible()){
					try {
						indexer=initializeIndexer(project);
					} catch (CoreException e1) {}
				}
				else{
					//Create the indexer and store it
					indexer = getIndexer(indexerID);
				}
				
				//Make sure we're not putting null in map
				if (indexer != null)
					indexerMap.put(project,indexer);
			}
		} finally {	
			monitor.exitRead();
		}
		
		return indexer;
	}
	
   /**
	 * @param project
	 */
	private synchronized void doSourceIndexerUpgrade(IProject project) {
		ICDescriptor descriptor = null;
		Element rootElement = null;
		IProject newProject = null;
		
		try {
			newProject = project;
			descriptor = CCorePlugin.getDefault().getCProjectDescription(newProject, true);
			rootElement = descriptor.getProjectData(SourceIndexer.SOURCE_INDEXER);
		
			// Clear out all current children
			Node child = rootElement.getFirstChild();
			while (child != null) {
				rootElement.removeChild(child);
				child = rootElement.getFirstChild();
			}
			Document doc = rootElement.getOwnerDocument();
	
					
			saveIndexerEnabled(upgradeIndexEnabled, rootElement, doc);
			saveIndexerProblemsEnabled( upgradeIndexProblems, rootElement, doc );
			
			descriptor.saveProjectData();
			
			//Update project session property
			
			project.setSessionProperty(SourceIndexer.activationKey,new Boolean(upgradeIndexEnabled));
			project.setSessionProperty(SourceIndexer.problemsActivationKey, new Integer( upgradeIndexProblems ));	
	
		} catch (CoreException e) {}
	}

	private static void saveIndexerEnabled (boolean indexerEnabled, Element rootElement, Document doc ) {
		
		Element indexEnabled = doc.createElement(SourceIndexer.INDEXER_ENABLED);
		Boolean tempValue= new Boolean(indexerEnabled);
		
		indexEnabled.setAttribute(SourceIndexer.INDEXER_VALUE,tempValue.toString());
		rootElement.appendChild(indexEnabled);

	}
	private static void saveIndexerProblemsEnabled ( int problemValues, Element rootElement, Document doc ) {
		
		Element enabled = doc.createElement(SourceIndexer.INDEXER_PROBLEMS_ENABLED);
		Integer tempValue= new Integer( problemValues );
		
		enabled.setAttribute(SourceIndexer.INDEXER_PROBLEMS_VALUE, tempValue.toString());
		rootElement.appendChild(enabled);
	}

/**
	 * @return
	 */
	private synchronized String doProjectUpgrade(IProject project) {
		ICDescriptor descriptor = null;
		Element rootElement = null;
		IProject newProject = null;
		
		try {
			//Get the old values from .cdtproject before upgrading
			Boolean tempEnabled = loadIndexerEnabledFromCDescriptor(project);
			if (tempEnabled != null)
				upgradeIndexEnabled = tempEnabled.booleanValue();
			
			Integer tempProblems = loadIndexerProblemsEnabledFromCDescriptor(project);
			if (tempProblems != null)
				upgradeIndexProblems = tempProblems.intValue();
			
		} catch (CoreException e1) {}
		
		
		//For now all upgrades will be to the old source indexer
		String indexerPageID = "org.eclipse.cdt.ui.originalSourceIndexerUI"; //$NON-NLS-1$
		String indexerID = "org.eclipse.cdt.core.originalsourceindexer"; //$NON-NLS-1$
		
		try {
			newProject = project;
			descriptor = CCorePlugin.getDefault().getCProjectDescription(newProject, true);
			rootElement = descriptor.getProjectData(IndexManager.CDT_INDEXER);
		
			// Clear out all current children
			Node child = rootElement.getFirstChild();
			while (child != null) {
				rootElement.removeChild(child);
				child = rootElement.getFirstChild();
			}
			Document doc = rootElement.getOwnerDocument();
			
			saveIndexerInfo(indexerID, indexerPageID, rootElement, doc);
		
			descriptor.saveProjectData();
			
			//Update project session property
			
			project.setSessionProperty(IndexManager.indexerIDKey, indexerID);	
			//project.setSessionProperty(indexerUIIDKey, indexerPageID);
	
		} catch (CoreException e) {}
		
		return indexerID;
	}


	private static void saveIndexerInfo (String indexerID, String indexerUIID, Element rootElement, Document doc ) {
		
		//Save the indexer id
		Element indexerIDElement = doc.createElement(IndexManager.INDEXER_ID);
		indexerIDElement.setAttribute(IndexManager.INDEXER_ID_VALUE,indexerID);
		rootElement.appendChild(indexerIDElement);
		
		//Save the indexer UI id
		Element indexerUIIDElement = doc.createElement("indexerUI"); //$NON-NLS-1$
		indexerUIIDElement.setAttribute("indexerUIValue",indexerUIID); //$NON-NLS-1$
		rootElement.appendChild(indexerUIIDElement);
	}

	private Boolean loadIndexerEnabledFromCDescriptor(IProject project) throws CoreException {
		// Check if we have the property in the descriptor
		// We pass false since we do not want to create the descriptor if it does not exists.
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project, false);
		Boolean strBool = null;
		if (descriptor != null) {
			Node child = descriptor.getProjectData(CDT_INDEXER).getFirstChild();
		
			while (child != null) {
				if (child.getNodeName().equals(SourceIndexer.INDEXER_ENABLED)) 
					strBool = Boolean.valueOf(((Element)child).getAttribute(SourceIndexer.INDEXER_VALUE));
			
			
				child = child.getNextSibling();
			}
		}
		
		return strBool;
	}
	private Integer loadIndexerProblemsEnabledFromCDescriptor(IProject project) throws CoreException {
	// we are only checking for the settings do not create the descriptor.
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project, false);
		Integer strInt = null;
		if( descriptor != null ){
			Node child = descriptor.getProjectData(CDT_INDEXER).getFirstChild();
			
			while (child != null) {
				if (child.getNodeName().equals(SourceIndexer.INDEXER_PROBLEMS_ENABLED)){
					String val = ((Element)child).getAttribute(SourceIndexer.INDEXER_PROBLEMS_VALUE);
					try{
						strInt = Integer.valueOf( val );
					} catch( NumberFormatException e ){
						//some old projects might have a boolean stored, translate that into just preprocessors
						Boolean bool = Boolean.valueOf( val );
						if( bool.booleanValue() )
							strInt = new Integer( SourceIndexer.PREPROCESSOR_PROBLEMS_BIT );
						else 
							strInt = new Integer( 0 );
					}
					break;
				}
				child = child.getNextSibling();
			}
		}
		
		return strInt;
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
   		if (indexer != null)
   			indexer.notifyIdle(idlingTime);
   	}
   }
   
	public void setEnableUpdates(boolean enableUpdates) {
		this.enableUpdates = enableUpdates;
	}
}
