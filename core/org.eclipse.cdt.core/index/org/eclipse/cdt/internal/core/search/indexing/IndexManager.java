/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.search.indexing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexStorage;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.index.IndexRequest;
import org.eclipse.cdt.internal.core.index.cindexstorage.CIndexStorage;
import org.eclipse.cdt.internal.core.index.domsourceindexer.DOMSourceIndexer;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Bogdan Gheorghe
 */
public class IndexManager extends JobManager{
	
    public static interface IIndexerSelectionListener
    {
        public void indexerSelectionChanged(IProject project);
    }
    
    private IIndexerSelectionListener [] listeners = new IIndexerSelectionListener[1];
    
    public synchronized void subscribeForIndexerChangeNotifications( IIndexerSelectionListener listener )
    {
        listeners = (IIndexerSelectionListener[]) ArrayUtil.append( IIndexerSelectionListener.class, listeners, listener );
    }
    
    public synchronized void unSubscribeForIndexerChangeNotifications( IIndexerSelectionListener listener )
    {
        if( listeners == null ) return;
        if( listener == null ) return;
        for( int i = 0; i < listeners.length; ++i )
        {
            if( listeners[i] == listener )
                listeners[i] = null;
        }
    }
    
    
    
	public final static String INDEX_MODEL_ID = CCorePlugin.PLUGIN_ID + ".cdtindexers"; //$NON-NLS-1$
	public final static String INDEXERID = "indexerID"; //$NON-NLS-1$
	public final static QualifiedName indexerIDKey = new QualifiedName(INDEX_MODEL_ID, INDEXERID);
	
	public static final String nullIndexerID = "org.eclipse.cdt.core.nullindexer"; //$NON-NLS-1$
	
	public static final String CDT_INDEXER = "cdt_indexer"; //$NON-NLS-1$
	public static final String INDEXER_ID = "indexerID"; //$NON-NLS-1$
	public static final String INDEXER_ID_VALUE = "indexerIDValue"; //$NON-NLS-1$

	public static boolean VERBOSE = false;
	
    //Map of Persisted Indexers; keyed by project
    private HashMap indexerMap = null;

	private ReadWriteMonitor monitor = new ReadWriteMonitor();
    
    private static ElementChangeListener elementChangeListener = null;

	/**
	 * Flush current state
	 */
	public void reset() {
		try{
			monitor.enterWrite();
			super.reset();
			//Set default upgrade values
			CCorePlugin.getDefault().getPluginPreferences().setValue(CCorePlugin.PREF_INDEXER, CCorePlugin.DEFAULT_INDEXER_UNIQ_ID);
			this.indexerMap = new HashMap(5);
            
            // subscribe for path entry changes
            if (elementChangeListener != null) {
                CoreModel.getDefault().removeElementChangedListener(elementChangeListener);
            }
            elementChangeListener = new ElementChangeListener();
            CoreModel.getDefault().addElementChangedListener(elementChangeListener);
		} finally{
			monitor.exitWrite();
		}
	}
    
    public class ElementChangeListener implements IElementChangedListener {
        private boolean scannerInfoChanged = false;
        private IProject currentProject = null;
        private List changedElements = new ArrayList();
        
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
         */
        public void elementChanged(ElementChangedEvent event) {
            scannerInfoChanged = false;
            currentProject = null;
            changedElements.clear();
            processDelta(event.getDelta());
            
            if (!scannerInfoChanged) 
                return;
            if (changedElements.size() > 0) {
                for (Iterator i = changedElements.iterator(); i.hasNext();) {
                    IFile file = (IFile) i.next();
                    addResource(currentProject, file);
                }
            }
            else {
                if (!CoreModel.isScannerInformationEmpty(currentProject)) {
                    addResource(currentProject, currentProject);
                }
            }
        }

        /**
         * @param delta
         */
        private void processDelta(ICElementDelta delta) {
            ICElement element= delta.getElement();

            IResource res = element.getResource();
            if (element instanceof ICProject && res instanceof IProject) {
                currentProject = (IProject) res;
            }
            if (isPathEntryChange(delta)) {
                scannerInfoChanged = true;
                if (element instanceof ITranslationUnit && res instanceof IFile) {
                    if (!changedElements.contains(res)) {
                        changedElements.add(res);
                    }
                }
            }
                
            ICElementDelta[] affectedChildren= delta.getAffectedChildren();
            for (int i= 0; i < affectedChildren.length; i++) {
                processDelta(affectedChildren[i]);
            }
        }

        /**
         * @param delta
         * @return
         */
        private boolean isPathEntryChange(ICElementDelta delta) {
            int flags= delta.getFlags();
            return (delta.getKind() == ICElementDelta.CHANGED && 
                    ((flags & ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE) != 0 ||
                    (flags & ICElementDelta.F_CHANGED_PATHENTRY_MACRO) != 0 ||
                    (flags & ICElementDelta.F_PATHENTRY_REORDER) !=0));
        }
        
    }
    
	 /**
	 * Notify indexer which scheduled this job that the job has completed  
	 * 
	 */
	protected void jobFinishedNotification(IIndexJob job) {
	
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
	public void addResourceEvent(IProject project, IResourceDelta delta, int kind) {
		//Get indexer for this project
		ICDTIndexer indexer = getIndexerForProject(project);	
		
		if (indexer != null)
			indexer.addRequest(project, delta, kind);
	}

	/**
	 * @param project
	 * @param element
	 * @param delta
	 */
	public void removeResourceEvent(IProject project, IResourceDelta delta, int kind) {
		//Get the indexer for this project
		ICDTIndexer indexer = null;
		indexer = (ICDTIndexer) indexerMap.get(project);
		
		if (indexer != null)
			indexer.removeRequest(project, delta, kind);
	}
	
	/**
	 * @param project
	 * @param element
	 * @param delta
	 */
	public void addResource(IProject project, IResource resource) {
		//Get indexer for this project
		ICDTIndexer indexer = getIndexerForProject(project);	
		
		if (indexer != null)
			indexer.addResource(project, resource);
	}

	public void addResourceByPath(IProject project, IPath path, int resourceType) {
		//Get indexer for this project
		ICDTIndexer indexer = getIndexerForProject(project);	
		
		if (indexer != null)
			indexer.addResourceByPath(project, path,resourceType);
		
	}
	/**
	 * @param project
	 * @param element
	 * @param delta
	 */
	public void removeResource(IProject project, IResource resource) {
		//Get the indexer for this project
		ICDTIndexer indexer = null;
		indexer = (ICDTIndexer) indexerMap.get(project);
		
		if (indexer != null)
			indexer.removeResource(project, resource);
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
		
        if (elementChangeListener != null) {
            CoreModel.getDefault().removeElementChangedListener(elementChangeListener);
            elementChangeListener = null;
        }
		super.shutdown();
	}
	
	public IIndexStorage getIndexStorageForIndexer(ICDTIndexer indexer){
		//For now we have only one index storage format that all indexers are to use
		return new CIndexStorage(indexer);
	}
	
	public int getJobStart(){
		return jobStart;
	}
	
	public int getJobEnd(){
		return jobEnd;
	}
	/**
	 * Returns the job at position in the awaiting job queue
	 * @param position
	 * @return
	 */
	public IIndexJob getAwaitingJobAt(int position){
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
		if (indexer instanceof DOMSourceIndexer)
			((DOMSourceIndexer) indexer).updateDependencies(resource);
		
	}
	
	public ICDTIndexer getIndexerForProject(IProject project){
		ICDTIndexer indexer = null;
		try {
			//Make sure we're not updating list
			monitor.enterRead();
			
			//See if indexer exists already
			indexer = (ICDTIndexer) indexerMap.get(project);
			
			//Create the indexer and store it
			if (indexer == null) {
				monitor.exitRead();
				try {
					monitor.enterWrite();
					indexer = getIndexer(project);
					//Make sure we're not putting null in map
					if (indexer != null)
						indexerMap.put(project,indexer);
				} finally{
					monitor.exitWriteEnterRead();
				}
			}
			return indexer;
				
			}finally {
				monitor.exitRead();
			}
	}
	
	public ICDTIndexer getDefaultIndexer(IProject project) throws CoreException {
		ICDTIndexer indexer = null;
		String id = CCorePlugin.getDefault().getPluginPreferences().getDefaultString(CCorePlugin.PREF_INDEXER);
		if (id == null || id.length() == 0) {
			id = CCorePlugin.DEFAULT_INDEXER_UNIQ_ID;
		}
		
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, CCorePlugin.INDEXER_SIMPLE_ID);
		IExtension extension = extensionPoint.getExtension(id);
		if (extension != null) {
			IConfigurationElement element[] = extension.getConfigurationElements();
			for (int i = 0; i < element.length; i++) {
				if (element[i].getName().equalsIgnoreCase("cextension")) { //$NON-NLS-1$
					indexer = (ICDTIndexer) element[i].createExecutableExtension("run"); //$NON-NLS-1$
                    indexer.setIndexerProject(project);
					break;
				}
			}
		} else {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,"No Indexer Found", null); //$NON-NLS-1$
			throw new CoreException(s);
		}
		return indexer;
	}
	
   protected ICDTIndexer getIndexer(IProject project) {
   	ICDTIndexer indexer = null;
   	try{
		ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project,true);
		ICExtensionReference[] cextensions = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID, false);
		
		if (cextensions != null && cextensions.length > 0)
			indexer = (ICDTIndexer) cextensions[0].createExtension();
	
   	} catch (CoreException e){}
   	
	if (indexer == null)
		try {
			indexer = getDefaultIndexer(project);
		} catch (CoreException e1) {}
	
	 return indexer;
   }
   
   protected void notifyIdle(long idlingTime) {
   	//Notify all indexers
   	monitor.enterRead();
   	try{
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
   	} finally{
   		monitor.exitRead();
   	}
   }
   
   /**
	* The indexer previously associated with this project has been changed to a
	* new value. Next time project gets asked for indexer, a new one will be created
	* of the new type.
	* 
	* @param project
	*/
	public void indexerChangeNotification(IProject project) {
		
		//Get rid of any jobs scheduled by the old indexer
        this.discardJobs(project.getName());
        
		//Get rid of the old index file
	    ICDTIndexer currentIndexer = getIndexerForProject(project);
		
		currentIndexer.indexerRemoved(project);
		
	    IIndexStorage storage = currentIndexer.getIndexStorage();
	    if (storage instanceof CIndexStorage)
	    	((CIndexStorage) storage).removeIndex(project.getFullPath());
	    
	    monitor.enterWrite();
	    try{ 
	        //Purge the old indexer from the indexer map
	        indexerMap.remove(project);   
	    } finally { 
	        monitor.exitWrite();
	        final ICDTIndexer indexer = this.getIndexerForProject(project);
	        final IProject finalProject = project;
	        
	    	//Notify new indexer in a job of change
			Job job = new Job("Index Change Notification"){ //$NON-NLS-1$
				protected IStatus run(IProgressMonitor monitor)	{	
					Platform.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							CCorePlugin.log(exception);
						}
						public void run() throws Exception {
						    indexer.notifyIndexerChange(finalProject);
						}
					});
					
					return Status.OK_STATUS;
				}
			};

			job.schedule();
            
            if( listeners != null )
                for( int i = 0; i < listeners.length; ++i )
                    if( listeners[i] != null )
                        listeners[i].indexerSelectionChanged(project);
	    }
	}
    
    static private class RemoveIndexMarkersJob extends Job{
        private final IResource resource;
        public RemoveIndexMarkersJob( IResource resource, String name ){
            super( name );
            this.resource = resource;
        }
        protected IStatus run(IProgressMonitor monitor) {
            try {
                resource.deleteMarkers( ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_INFINITE );
            } catch (CoreException e) {
                return Status.CANCEL_STATUS;
            }
            return Status.OK_STATUS;        
        }
        
    }
    
    public void removeIndexerProblems( IResource resource){
        String jobName = "remove markers"; //$NON-NLS-1$
        RemoveIndexMarkersJob job = new RemoveIndexMarkersJob( resource, jobName );
        job.setRule( resource );
        job.setPriority( Job.DECORATE );
        job.schedule();
    }

    
}
