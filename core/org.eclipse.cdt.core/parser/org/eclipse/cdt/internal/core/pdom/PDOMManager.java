/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexerStateListener;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.index.IndexChangeEvent;
import org.eclipse.cdt.internal.core.index.IndexFactory;
import org.eclipse.cdt.internal.core.index.IndexerStateEvent;
import org.eclipse.cdt.internal.core.index.provider.IndexProviderManager;
import org.eclipse.cdt.internal.core.pdom.PDOM.IListener;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMProjectIndexLocationConverter;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMRebuildTask;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMResourceDeltaTask;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMUpdateTask;
import org.eclipse.cdt.internal.core.pdom.indexer.nulli.PDOMNullIndexer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

/**
 * The PDOM Provider. This is likely temporary since I hope
 * to integrate the PDOM directly into the core once it has
 * stabilized.
 * 
 * @author Doug Schaefer
 */
public class PDOMManager implements IPDOMManager, IWritableIndexManager, IListener {

	private static final class PerInstanceSchedulingRule implements ISchedulingRule {
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	}
	
	private final class PCL implements IPreferenceChangeListener {
		private ICProject fProject;
		public PCL(ICProject prj) {
			fProject= prj;
		}
		public void preferenceChange(PreferenceChangeEvent event) {
			onPreferenceChange(fProject, event);
		}
	}


	private static final QualifiedName indexerProperty= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomIndexer"); //$NON-NLS-1$
	private static final QualifiedName dbNameProperty= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomName"); //$NON-NLS-1$

	private static final ISchedulingRule NOTIFICATION_SCHEDULING_RULE = new PerInstanceSchedulingRule();
	private static final ISchedulingRule INDEXER_SCHEDULING_RULE = new PerInstanceSchedulingRule();

	/**
	 * Protects indexerJob, currentTask and taskQueue.
	 */
    private Object fTaskQueueMutex = new Object();
    private PDOMIndexerJob fIndexerJob;
	private IPDOMIndexerTask fCurrentTask;
	private LinkedList fTaskQueue = new LinkedList();
	private int fCompletedSources;
	private int fCompletedHeaders;
	
    /**
     * Stores mapping from pdom to project, used to serialize\ creation of new pdoms.
     */
    private Map fProjectToPDOM= new HashMap();
    private Map fFileToProject= new HashMap();
	private ListenerList fChangeListeners= new ListenerList();
	private ListenerList fStateListeners= new ListenerList();
	
	private IndexChangeEvent fIndexChangeEvent= new IndexChangeEvent();
	private IndexerStateEvent fIndexerStateEvent= new IndexerStateEvent();

	private IElementChangedListener fCModelListener= new CModelListener(this);
	private IndexFactory fIndexFactory= new IndexFactory(this);
    private IndexProviderManager manager = new IndexProviderManager();
    
	/**
	 * Serializes creation of new indexer, when acquiring the lock you are 
	 * not allowed to hold a lock on fPDOMs.
	 */
	private Object fIndexerMutex= new Object();
	private HashMap fPrefListeners= new HashMap();
    
	/**
	 * Startup the PDOM. This mainly sets us up to handle model
	 * change events.
	 */
	public void startup() {
		// the model listener is attached outside of the job in
		// order to avoid a race condition where its not noticed
		// that new projects are being created
		final CoreModel model = CoreModel.getDefault();
		model.addElementChangedListener(fCModelListener);
		
		IProject[] projects= ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			addProject(project);
		}
	}

	public IndexProviderManager getIndexProviderManager() {
		return manager;
	}

	public IPDOM getPDOM(ICProject project) throws CoreException {
		synchronized (fProjectToPDOM) {
			IProject rproject = project.getProject();
			WritablePDOM pdom = (WritablePDOM) fProjectToPDOM.get(rproject);
			if (pdom != null) {
				return pdom;
			}

			String dbName= rproject.getPersistentProperty(dbNameProperty);
			File dbFile= null;
			if (dbName != null) {
				dbFile= fileFromDatabaseName(dbName);
				ICProject currentCOwner= (ICProject) fFileToProject.get(dbFile);
				if (currentCOwner != null) {
					IProject currentOwner= currentCOwner.getProject();
					if (currentOwner.exists()) {
						dbName= null;
						dbFile= null;
					}
					else {
						pdom= (WritablePDOM) fProjectToPDOM.remove(currentOwner);
						fFileToProject.remove(dbFile);
					}
				}
			}

			if (pdom == null) {
				if (dbName == null) {
					dbName = createNewDatabaseName(project);
					dbFile= fileFromDatabaseName(dbName);
					storeDatabaseName(rproject, dbName);
				}
			
				boolean newPDOM= !dbFile.exists();
				pdom = new WritablePDOM(dbFile, new PDOMProjectIndexLocationConverter(rproject));
				if(newPDOM) {
					writeProjectPDOMProperties(pdom, rproject);
				}
				pdom.addListener(this);
			}
			
			fFileToProject.put(dbFile, project);
			fProjectToPDOM.put(rproject, pdom);
			return pdom;
		}
	}

	private void storeDatabaseName(IProject rproject, String dbName)
			throws CoreException {
		rproject.setPersistentProperty(dbNameProperty, dbName);
	}

	private String createNewDatabaseName(ICProject project) {
		String dbName;
		long time= System.currentTimeMillis();
		File file;
		do {
			dbName= getDefaultName(project, time++);
			file= fileFromDatabaseName(dbName);
		}
		while (file.exists());
		return dbName;
	}

	private File fileFromDatabaseName(String dbName) {
		return CCorePlugin.getDefault().getStateLocation().append(dbName).toFile();
	}

	private String getDefaultName(ICProject project, long time) {
		return project.getElementName() + "." + time + ".pdom";  //$NON-NLS-1$//$NON-NLS-2$
	}

	public String getDefaultIndexerId() {
		return getIndexerId(null);
	}

	public void setDefaultIndexerId(String indexerId) {
		setIndexerId(null, indexerId);
	}
	
    public String getIndexerId(ICProject project) {
    	IProject prj= project != null ? project.getProject() : null;
    	return IndexerPreferences.get(prj, IndexerPreferences.KEY_INDEXER_ID, ID_NO_INDEXER);
    }

    public void setIndexerId(final ICProject project, String indexerId) {
    	IProject prj= project.getProject();
    	IndexerPreferences.set(prj, IndexerPreferences.KEY_INDEXER_ID, indexerId);
    	CCoreInternals.savePreferences(prj);
    }
	
	protected void onPreferenceChange(ICProject cproject, PreferenceChangeEvent event) {
		IProject project= cproject.getProject();
		if (project.exists() && project.isOpen()) {
			try {
				changeIndexer(cproject);
			}
			catch (Exception e) {
				CCorePlugin.log(e);
			}
		}
	}

	private void changeIndexer(ICProject cproject) throws CoreException {
		assert !Thread.holdsLock(fProjectToPDOM);
		IPDOMIndexer oldIndexer= null;
		IProject prj= cproject.getProject();
		
		String newid= IndexerPreferences.get(prj, IndexerPreferences.KEY_INDEXER_ID, ID_NO_INDEXER);
		Properties props= IndexerPreferences.getProperties(prj);
		
		synchronized (fIndexerMutex) {
			oldIndexer= getIndexer(cproject);
			if (oldIndexer != null) {
				if (oldIndexer.getID().equals(newid)) {
					if (!oldIndexer.needsToRebuildForProperties(props)) {
						oldIndexer.setProperties(props);
						return;
					}
				}
				IPDOMIndexer indexer= createIndexer(cproject, newid, props);
				registerIndexer(cproject, indexer);
				enqueue(new PDOMRebuildTask(indexer));
			}
		}
		
		if (oldIndexer != null) {
			stopIndexer(oldIndexer);
		}
	}

	private void registerIndexer(ICProject project, IPDOMIndexer indexer) throws CoreException {
		assert Thread.holdsLock(fIndexerMutex);
		indexer.setProject(project);
		registerPreferenceListener(project);
		project.getProject().setSessionProperty(indexerProperty, indexer);
	}

	private IPDOMIndexer getIndexer(ICProject project) {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fIndexerMutex) {
			IProject prj = project.getProject();
			if (!prj.isOpen()) {
				return null;
			}

			IPDOMIndexer indexer;
			try {
				indexer = (IPDOMIndexer)prj.getSessionProperty(indexerProperty);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return null;
			}

			if (indexer != null && indexer.getProject().equals(project)) {
				return indexer;
			}

			return null;
		}
	}

	private void createIndexer(ICProject project, IProgressMonitor pm) {
		assert !Thread.holdsLock(fProjectToPDOM);
		IProject prj= project.getProject();
		try {
			synchronized (fIndexerMutex) {
				PDOM pdom= (PDOM) getPDOM(project);
				Properties props= IndexerPreferences.getProperties(prj);
				IPDOMIndexer indexer= createIndexer(project, getIndexerId(project), props);

				boolean performImport= false;
				boolean rebuild= false;
				if (!IPDOMManager.ID_NO_INDEXER.equals(indexer.getID()) && pdom.isEmpty()) {
					performImport= true;
				}
				else if (pdom.versionMismatch()) {
					rebuild= true;
				}
				
				if (!performImport) {
					registerIndexer(project, indexer);
					if (rebuild) {
						enqueue(new PDOMRebuildTask(indexer));
					}
					return;
				}
			}
					
			// perform import
			TeamPDOMImportOperation operation= new TeamPDOMImportOperation(project);
			operation.run(pm);

			synchronized (fIndexerMutex) {
				Properties props= IndexerPreferences.getProperties(prj);
				IPDOMIndexer indexer= createIndexer(project, getIndexerId(project), props);

				registerIndexer(project, indexer);
				IPDOMIndexerTask task= null;
				if (operation.wasSuccessful()) {
					task= new PDOMUpdateTask(indexer);
				}
				else {
					task= new PDOMRebuildTask(indexer);
				}
				enqueue(task);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
		
    private IPDOMIndexer createIndexer(ICProject project, String indexerId, Properties props) throws CoreException  {
    	IPDOMIndexer indexer = null;
    	// Look up in extension point
    	IExtension indexerExt = Platform.getExtensionRegistry().getExtension(CCorePlugin.INDEXER_UNIQ_ID, indexerId);
    	if (indexerExt != null) {
    		IConfigurationElement[] elements = indexerExt.getConfigurationElements();
    		for (int i = 0; i < elements.length; ++i) {
    			IConfigurationElement element = elements[i];
    			if ("run".equals(element.getName())) { //$NON-NLS-1$
    				try {
						indexer = (IPDOMIndexer)element.createExecutableExtension("class"); //$NON-NLS-1$
						indexer.setProperties(props);
					} catch (CoreException e) {
						CCorePlugin.log(e);
					} 
    				break;
    			}
    		}
    	}

    	// Unknown index, default to the null one
    	if (indexer == null) 
    		indexer = new PDOMNullIndexer();

		return indexer;
    }

	public void enqueue(IPDOMIndexerTask subjob) {
    	synchronized (fTaskQueueMutex) {
    		fTaskQueue.addLast(subjob);
			if (fIndexerJob == null) {
				fCompletedSources= 0;
				fCompletedHeaders= 0;
				fIndexerJob = new PDOMIndexerJob(this);
				fIndexerJob.setRule(INDEXER_SCHEDULING_RULE);
				fIndexerJob.schedule();
	    		notifyState(IndexerStateEvent.STATE_BUSY);
			}
		}
    }
    
	IPDOMIndexerTask getNextTask() {
		IPDOMIndexerTask result= null;
    	synchronized (fTaskQueueMutex) {
    		if (fTaskQueue.isEmpty()) {
    			fCurrentTask= null;
        		fIndexerJob= null;
        		notifyState(IndexerStateEvent.STATE_IDLE);
    		}
    		else {
    			if (fCurrentTask != null) {
    				IndexerProgress info= fCurrentTask.getProgressInformation();
    				fCompletedSources+= info.fCompletedSources;
    				fCompletedHeaders+= info.fCompletedHeaders;
    			}
    			result= fCurrentTask= (IPDOMIndexerTask)fTaskQueue.removeFirst();
    		}
		}
    	return result;
    }
    
    void cancelledJob(boolean byManager) {
    	synchronized (fTaskQueueMutex) {
    		fCurrentTask= null;
    		if (!byManager) {
    			fTaskQueue.clear();
    		}
    		if (fTaskQueue.isEmpty()) {
        		fIndexerJob= null;
        		notifyState(IndexerStateEvent.STATE_IDLE);
    		}
    		else {
    			fIndexerJob = new PDOMIndexerJob(this);
    			fIndexerJob.setRule(INDEXER_SCHEDULING_RULE);
    			fIndexerJob.schedule();
    		}
    	}
    }
        
    public boolean isIndexerIdle() {
    	synchronized (fTaskQueueMutex) {
    		return fCurrentTask == null && fTaskQueue.isEmpty();
    	}
    }
    
	public void addProject(final IProject project) {
		Job addProject= new Job(Messages.PDOMManager_StartJob_name) {
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("", 100); //$NON-NLS-1$
				if (project.isOpen() && CoreModel.hasCNature(project)) {
					ICProject cproject= CoreModel.getDefault().create(project);
					if (cproject != null) {
						syncronizeProjectSettings(project, new SubProgressMonitor(monitor, 1));
						if (getIndexer(cproject) == null) {
							createIndexer(cproject, new SubProgressMonitor(monitor, 99));
						}
					}
				}
				return Status.OK_STATUS;
			}

			private void syncronizeProjectSettings(IProject project, IProgressMonitor monitor) {
				try {
					IFolder settings= project.getFolder(".settings");  //$NON-NLS-1$
					settings.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
				monitor.done();
			}

			public boolean belongsTo(Object family) {
				return family == PDOMManager.this;
			}
		};
		addProject.setRule(project); 
		addProject.setSystem(true);
		addProject.schedule();
	}

	private void registerPreferenceListener(ICProject project) {
		IProject prj= project.getProject();
		PCL pcl= (PCL) fPrefListeners.get(prj);
		if (pcl == null) {
			pcl= new PCL(project);
			fPrefListeners.put(prj, pcl);
		}
		IndexerPreferences.addChangeListener(prj, pcl);
	}

	private void unregisterPreferenceListener(ICProject project) {
		IProject prj= project.getProject();
		PCL pcl= (PCL) fPrefListeners.remove(prj);
		if (pcl != null) {
			IndexerPreferences.removeChangeListener(prj, pcl);
		}
	}

	public void changeProject(ICProject project, ICElementDelta delta) throws CoreException {
		IPDOMIndexer indexer = getIndexer(project);
		if (indexer != null) {
			PDOMResourceDeltaTask resourceDeltaTask = new PDOMResourceDeltaTask(indexer, delta);
			if (!resourceDeltaTask.isEmpty()) {
				enqueue(resourceDeltaTask);
			}
		}
	}
	
	public void removeProject(ICProject project) {
		IPDOMIndexer indexer= getIndexer(project);
		if (indexer != null) {
			stopIndexer(indexer);
		}
    	unregisterPreferenceListener(project);
	}

	public void deleteProject(ICProject cproject, IResourceDelta delta) {
		// Project is about to be deleted. Stop all indexing tasks for it
		IPDOMIndexer indexer = getIndexer(cproject);
		if (indexer != null) {
			stopIndexer(indexer);
		}
		unregisterPreferenceListener(cproject);

		// remove entry for project from PDOM map
		synchronized (fProjectToPDOM) {
			IProject project= cproject.getProject();
			fProjectToPDOM.remove(project);
		}
	}

	private void stopIndexer(IPDOMIndexer indexer) {
		ICProject project= indexer.getProject();
		synchronized (fIndexerMutex) {
			IProject rp= project.getProject();
			if (rp.isOpen()) {
				try {
					if (rp.getSessionProperty(indexerProperty) == indexer) {
						rp.setSessionProperty(indexerProperty, null);
					}
				}
				catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
		cancelIndexerJobs(indexer);
	}

	private void cancelIndexerJobs(IPDOMIndexer indexer) {
		PDOMIndexerJob jobToCancel= null;
		synchronized (fTaskQueueMutex) {
			for (Iterator iter = fTaskQueue.iterator(); iter.hasNext();) {
				IPDOMIndexerTask task= (IPDOMIndexerTask) iter.next();
				if (task.getIndexer() == indexer) {
					iter.remove();
				}
			}
			jobToCancel= fIndexerJob;
		}
		
		if (jobToCancel != null) {
			assert !Thread.holdsLock(fTaskQueueMutex);
			jobToCancel.cancelJobs(indexer);
		}
	}    

	public void reindex(ICProject project) throws CoreException {
		assert !Thread.holdsLock(fProjectToPDOM);
		IPDOMIndexer indexer= null;
		synchronized (fIndexerMutex) {
			indexer= getIndexer(project);
		}
		// don't attempt to hold lock on indexerMutex while cancelling
		if (indexer != null) {
			cancelIndexerJobs(indexer);
		}
		
		synchronized(fIndexerMutex) {
			indexer= getIndexer(project);
			if (indexer != null) {
				enqueue(new PDOMRebuildTask(indexer));
			}
		}
	}

	public void addIndexChangeListener(IIndexChangeListener listener) {
		fChangeListeners.add(listener);
	}

	public void removeIndexChangeListener(IIndexChangeListener listener) {
		fChangeListeners.remove(listener);
	}
	
	public void addIndexerStateListener(IIndexerStateListener listener) {
		fStateListeners.add(listener);
	}

	public void removeIndexerStateListener(IIndexerStateListener listener) {
		fStateListeners.remove(listener);
	}

    private void notifyState(final int state) {
    	if (state == IndexerStateEvent.STATE_IDLE) {
    		synchronized(fTaskQueueMutex) {
    			fTaskQueueMutex.notifyAll();
    		}
    	}
    	
    	if (fStateListeners.isEmpty()) {
    		return;
    	}
    	Job notify= new Job(Messages.PDOMManager_notifyJob_label) {
    		protected IStatus run(IProgressMonitor monitor) {
    			fIndexerStateEvent.setState(state);
    			Object[] listeners= fStateListeners.getListeners();
    			monitor.beginTask(Messages.PDOMManager_notifyTask_message, listeners.length);
    			for (int i = 0; i < listeners.length; i++) {
    				final IIndexerStateListener listener = (IIndexerStateListener) listeners[i];
    				SafeRunner.run(new ISafeRunnable(){
    					public void handleException(Throwable exception) {
    						CCorePlugin.log(exception);
    					}
    					public void run() throws Exception {
    						listener.indexChanged(fIndexerStateEvent);
    					}
    				});
    				monitor.worked(1);
    			}
    			return Status.OK_STATUS;
    		}
    	};
		notify.setRule(NOTIFICATION_SCHEDULING_RULE);
    	notify.setSystem(true);
    	notify.schedule();
	}

	public void handleChange(PDOM pdom) {
		if (fChangeListeners.isEmpty()) {
			return;
		}
		
		ICProject project;
		synchronized (fProjectToPDOM) {
			project = (ICProject) fFileToProject.get(pdom.getPath());
		}		
		
		if (project != null) {
			final ICProject finalProject= project;
			Job notify= new Job(Messages.PDOMManager_notifyJob_label) {
				protected IStatus run(IProgressMonitor monitor) {
					fIndexChangeEvent.setAffectedProject(finalProject);
					Object[] listeners= fChangeListeners.getListeners();
					monitor.beginTask(Messages.PDOMManager_notifyTask_message, listeners.length);
					for (int i = 0; i < listeners.length; i++) {
						final IIndexChangeListener listener = (IIndexChangeListener) listeners[i];
						SafeRunner.run(new ISafeRunnable(){
							public void handleException(Throwable exception) {
								CCorePlugin.log(exception);
							}
							public void run() throws Exception {
								listener.indexChanged(fIndexChangeEvent);
							}
						});
						monitor.worked(1);
					}
					return Status.OK_STATUS;
				}
			};
			notify.setRule(NOTIFICATION_SCHEDULING_RULE);
			notify.setSystem(true);
			notify.schedule();
		}
	}

	public boolean joinIndexer(final int waitMaxMillis, final IProgressMonitor monitor) {
		assert monitor != null;
		Thread th= new Thread() {
			public void run() {
				try {
					Thread.sleep(waitMaxMillis);
					monitor.setCanceled(true);
				}
				catch (InterruptedException e) {
				}
			}
		};
		th.setDaemon(true);
		th.start();
		try {
			try {
				Job.getJobManager().join(this, monitor);
				return true;
			} catch (OperationCanceledException e1) {
			} catch (InterruptedException e1) {
			}
			return Job.getJobManager().find(this).length == 0;
		}
		finally {
			th.interrupt();
		}
	}
	
	public boolean joinIndexerOld(int waitMaxMillis, IProgressMonitor monitor) {
		final int totalTicks = 1000;
		monitor.beginTask(Messages.PDOMManager_JoinIndexerTask, totalTicks);
		long limit= System.currentTimeMillis()+waitMaxMillis;
		try {
			int currentTicks= 0;
			while (true) {
				if (monitor.isCanceled()) {
					return false;
				}
				currentTicks= getMonitorMessage(monitor, currentTicks, totalTicks);
				synchronized(fTaskQueueMutex) {
					if (isIndexerIdle()) {
						return true;
					}
					int wait= 1000;
					if (waitMaxMillis >= 0) {
						int rest= (int) (limit - System.currentTimeMillis());
						if (rest < wait) {
							if (rest <= 0) {
								return false;
							}
							wait= rest;
						}
					}

					try {
						fTaskQueueMutex.wait(wait);
					} catch (InterruptedException e) {
						return false;
					}
				}
			}
		}
		finally {
			monitor.done();
		}
	}
	
	int getMonitorMessage(IProgressMonitor monitor, int currentTicks, int base) {
		assert !Thread.holdsLock(fTaskQueueMutex);
		int remainingSources= 0;
		int completedSources= 0;
		int completedHeaders= 0;
		int totalEstimate= 0;
		String detail= null;
		IndexerProgress info;
		synchronized (fTaskQueueMutex) {
			completedHeaders= fCompletedHeaders;
			completedSources= fCompletedSources;
			totalEstimate= fCompletedHeaders+fCompletedSources;
			for (Iterator iter = fTaskQueue.iterator(); iter.hasNext();) {
				IPDOMIndexerTask task = (IPDOMIndexerTask) iter.next();
				info= task.getProgressInformation();
				remainingSources+= info.getRemainingSources();
				totalEstimate+= info.getTimeEstimate();
			}
			if (fCurrentTask != null) {
				info= fCurrentTask.getProgressInformation();
				remainingSources+= info.getRemainingSources();
				completedHeaders+= info.fCompletedHeaders;
				completedSources+= info.fCompletedSources;
				detail= PDOMIndexerJob.sMonitorDetail;
				totalEstimate+= info.getTimeEstimate();
			}
		}
		
		int totalSources = remainingSources+completedSources;
		String msg= MessageFormat.format(Messages.PDOMManager_indexMonitorDetail, new Object[] { 
					new Integer(completedSources), new Integer(totalSources), 
					new Integer(completedHeaders)}); 
		if (detail != null) {
			msg= msg+ ": " + detail; //$NON-NLS-1$
		}
		monitor.subTask(msg);
		
		if (completedSources > 0 && totalEstimate >= completedSources) {
			int newTick= completedSources*base/totalEstimate;
			if (newTick > currentTicks) {
				monitor.worked(newTick-currentTicks);
				return newTick;
			}
		}
		return currentTicks;
	}


	public IWritableIndex getWritableIndex(ICProject project) throws CoreException {
		return fIndexFactory.getWritableIndex(project);
	}

	public IIndex getIndex(ICProject project) throws CoreException {
		return fIndexFactory.getIndex(new ICProject[] {project}, 0);
	}

	public IIndex getIndex(ICProject[] projects) throws CoreException {
		return fIndexFactory.getIndex(projects, 0);
	}

	public IIndex getIndex(ICProject project, int options) throws CoreException {
		return fIndexFactory.getIndex(new ICProject[] {project}, options);
	}

	public IIndex getIndex(ICProject[] projects, int options) throws CoreException {
		return fIndexFactory.getIndex(projects, options);
	}
	
	/**
     * Exports the project PDOM to the specified location, rewriting locations with
     * the specified location converter.
     * <br>
     * Note. This will acquire a write lock while the pdom is exported
	 * @param targetLocation a location that does not currently exist
	 * @param newConverter
	 * @throws CoreException
	 * @throws IllegalArgumentException if a file exists at targetLocation
	 */
	public void exportProjectPDOM(ICProject cproject, File targetLocation, final IIndexLocationConverter newConverter) throws CoreException {
		if(targetLocation.exists()) {
			boolean deleted= targetLocation.delete();
			if(!deleted) {
				throw new IllegalArgumentException(
						MessageFormat.format(Messages.PDOMManager_ExistingFileCollides,
								new Object[] {targetLocation})
				);
			}
		}
		try {
			// copy it
			PDOM pdom= (PDOM) getPDOM(cproject);
			pdom.acquireWriteLock();
			try {
				File db = pdom.getDB().getLocation();
				FileChannel from = new FileInputStream(db).getChannel();
				FileChannel to = new FileOutputStream(targetLocation).getChannel();
				from.transferTo(0, from.size(), to);
				to.close();
				from.close();
			} finally {
				pdom.releaseWriteLock();
			}

			// overwrite internal location representations
			final WritablePDOM newPDOM = new WritablePDOM(targetLocation, pdom.getLocationConverter());
			
			newPDOM.acquireWriteLock();
			try {
				List notConverted= newPDOM.rewriteLocations(newConverter);
				
				// remove content where converter returns null
				for(Iterator i = notConverted.iterator(); i.hasNext(); ) {
					PDOMFile file = (PDOMFile) i.next();
					file.clear();
				}
				
				// ensure fragment id has a sensible value, in case callee's do not
				// overwrite their own values
				String oldId= pdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
				newPDOM.setProperty(IIndexFragment.PROPERTY_FRAGMENT_ID, "exported."+oldId); //$NON-NLS-1$
			} finally {
				newPDOM.releaseWriteLock();
			}
		} catch(IOException ioe) {
			throw new CoreException(CCorePlugin.createStatus(ioe.getMessage()));
		} catch(InterruptedException ie) {
			throw new CoreException(CCorePlugin.createStatus(ie.getMessage()));
		}
	}

	/**
	 * Resets the pdom for the project with the provided stream. 
	 * @throws CoreException
	 * @throws OperationCanceledException in case the thread was interrupted
	 * @since 4.0
	 */
	public void importProjectPDOM(ICProject project, InputStream stream) throws CoreException, IOException {
		// make a copy of the database
		String newName= createNewDatabaseName(project);
		File newFile= fileFromDatabaseName(newName);
		OutputStream out= new FileOutputStream(newFile);
		try {
			byte[] buffer= new byte[2048];
			int read;
			while ((read= stream.read(buffer)) >= 0) {
				out.write(buffer, 0, read);
			}
		}
		finally {
			out.close();
		}
		
		WritablePDOM pdom= (WritablePDOM) getPDOM(project);
		try {
			pdom.acquireWriteLock();
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
		try {
			pdom.reloadFromFile(newFile);
			storeDatabaseName(project.getProject(), newName);
			writeProjectPDOMProperties(pdom, project.getProject());
		}
		finally {
			pdom.releaseWriteLock();
		}
	}
	
	public void export(ICProject project, String location, int options, IProgressMonitor monitor) throws CoreException {
		TeamPDOMExportOperation operation= new TeamPDOMExportOperation(project);
		operation.setTargetLocation(location);
		operation.setOptions(options);
		operation.run(monitor);
	}
	
	/**
	 * Write metadata appropriate for a project pdom
	 * @param pdom the pdom to write to
	 * @param project the project to write metadata about
	 * @throws CoreException
	 */
	public static void writeProjectPDOMProperties(WritablePDOM pdom, IProject project) throws CoreException {
		String DELIM = "\0"; //$NON-NLS-1$
		String id= CCorePlugin.PLUGIN_ID + ".pdom.project." + DELIM + project.getName() + DELIM; //$NON-NLS-1$
		pdom.setProperty(IIndexFragment.PROPERTY_FRAGMENT_ID, id);
	}
}
