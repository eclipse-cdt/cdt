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
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexerStateListener;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.index.IndexChangeEvent;
import org.eclipse.cdt.internal.core.index.IndexFactory;
import org.eclipse.cdt.internal.core.index.IndexerStateEvent;
import org.eclipse.cdt.internal.core.pdom.PDOM.IListener;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMProjectIndexLocationConverter;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMImportTask;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMRebuildTask;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMResourceDeltaTask;
import org.eclipse.cdt.internal.core.pdom.indexer.nulli.PDOMNullIndexer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
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
		
		Job startup= new Job(Messages.PDOMManager_StartJob_name) {
			protected IStatus run(IProgressMonitor monitor) {
				ICProject[] projects;
				try {
					projects = model.getCModel().getCProjects();
					for (int i = 0; i < projects.length; i++) {
						ICProject project = projects[i];
						if (project.getProject().isOpen()) {
							addProject(project, null);
						}
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
					return e.getStatus();
				} 
				return Status.OK_STATUS;
			}
		};
		startup.setSystem(true);
		startup.setRule(INDEXER_SCHEDULING_RULE); // block indexer until init is done.
		startup.schedule(1000);
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
				dbFile= CCorePlugin.getDefault().getStateLocation().append(dbName).toFile();
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
					dbName= getDefaultName(project);
					rproject.setPersistentProperty(dbNameProperty, dbName);
					dbFile= CCorePlugin.getDefault().getStateLocation().append(dbName).toFile();
				}
			
				pdom = new WritablePDOM(dbFile, new PDOMProjectIndexLocationConverter(rproject));
				pdom.addListener(this);
			}
			
			fFileToProject.put(dbFile, project);
			fProjectToPDOM.put(rproject, pdom);
			return pdom;
		}
	}

	private String getDefaultName(ICProject project) {
		return project.getElementName() + "." + System.currentTimeMillis() + ".pdom";  //$NON-NLS-1$//$NON-NLS-2$
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
			oldIndexer= getIndexer(cproject, false);
			if (oldIndexer != null) {
				if (oldIndexer.getID().equals(newid)) {
					if (!oldIndexer.needsToRebuildForProperties(props)) {
						oldIndexer.setProperties(props);
						return;
					}
				}
				createIndexer(cproject, newid, props, true);
			}
		}
		
		if (oldIndexer != null) {
			stopIndexer(oldIndexer);
		}
	}

	private IPDOMIndexer getIndexer(ICProject project, boolean create) {
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

			if (create) {
				try {
					Properties props= IndexerPreferences.getProperties(prj);
					return createIndexer(project, getIndexerId(project), props, false);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
			return null;
		}
	}
		
    private IPDOMIndexer createIndexer(ICProject project, String indexerId, Properties props, boolean forceReindex) throws CoreException  {
    	assert Thread.holdsLock(fIndexerMutex);
    	
    	PDOM pdom= (PDOM) getPDOM(project);
    	boolean reindex= forceReindex || pdom.versionMismatch() || pdom.isEmpty();

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

		indexer.setProject(project);
		registerPreferenceListener(project);
		project.getProject().setSessionProperty(indexerProperty, indexer);

		if (reindex) {
			if (forceReindex) {
				enqueue(new PDOMRebuildTask(indexer));
			}
			else {
				enqueue(new PDOMImportTask(indexer));
			}
		}
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
    				if (info != null) {
    					fCompletedSources+= info.fCompletedSources;
    					fCompletedHeaders+= info.fCompletedHeaders;
    				}
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
    
	public void addProject(ICProject project, ICElementDelta delta) {
		getIndexer(project, true); // if the indexer is new this triggers a rebuild
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
		IPDOMIndexer indexer = getIndexer(project, true);
		if (indexer != null) {
			PDOMResourceDeltaTask resourceDeltaTask = new PDOMResourceDeltaTask(indexer, delta);
			if (!resourceDeltaTask.isEmpty()) {
				enqueue(resourceDeltaTask);
			}
		}
	}
	
	public void removeProject(ICProject project) {
		IPDOMIndexer indexer= getIndexer(project, false);
		if (indexer != null) {
			stopIndexer(indexer);
		}
    	unregisterPreferenceListener(project);
	}

	public void deleteProject(ICProject cproject, IResourceDelta delta) {
		// Project is about to be deleted. Stop all indexing tasks for it
		IPDOMIndexer indexer = getIndexer(cproject, false);
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
			indexer= getIndexer(project, false);
		}
		// don't attempt to hold lock on indexerMutex while cancelling
		if (indexer != null) {
			cancelIndexerJobs(indexer);
		}
		
		synchronized(fIndexerMutex) {
			indexer= getIndexer(project, true);
			enqueue(new PDOMRebuildTask(indexer));
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

	public boolean joinIndexer(int waitMaxMillis, IProgressMonitor monitor) {
		monitor.beginTask(Messages.PDOMManager_JoinIndexerTask, 1000);
		long limit= System.currentTimeMillis()+waitMaxMillis;
		try {
			int currentTicks= 0;
			while (true) {
				if (monitor.isCanceled()) {
					return false;
				}
				currentTicks= getMonitorMessage(monitor, currentTicks, 1000);
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
		int unknown= 0;
		String detail= null;
		IndexerProgress info;
		synchronized (fTaskQueueMutex) {
			completedHeaders= fCompletedHeaders;
			completedSources= fCompletedSources;
			for (Iterator iter = fTaskQueue.iterator(); iter.hasNext();) {
				IPDOMIndexerTask task = (IPDOMIndexerTask) iter.next();
				info= task.getProgressInformation();
				if (info == null) {
					unknown++;
				}
				else {
					remainingSources+= info.getRemainingSources();
				}
			}
			if (fCurrentTask != null) {
				info= fCurrentTask.getProgressInformation();
				if (info == null) {
					unknown++;
				}
				else {
					remainingSources+= info.getRemainingSources();
					completedHeaders+= info.fCompletedHeaders;
					completedSources+= info.fCompletedSources;
					detail= info.fMonitorDetail;
				}
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
		
		totalSources+= unknown*1000;
		if (completedSources > 0 && totalSources >= completedSources) {
			int newTick= completedSources*base/totalSources;
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

}
