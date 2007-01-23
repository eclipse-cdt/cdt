/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
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
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.index.IndexChangeEvent;
import org.eclipse.cdt.internal.core.index.IndexFactory;
import org.eclipse.cdt.internal.core.index.IndexerStateEvent;
import org.eclipse.cdt.internal.core.pdom.PDOM.IListener;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMProjectIndexLocationConverter;
import org.eclipse.cdt.internal.core.pdom.indexer.nulli.PDOMNullIndexer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

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

	private static final QualifiedName indexerProperty= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomIndexer"); //$NON-NLS-1$
	private static final QualifiedName dbNameProperty= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomName"); //$NON-NLS-1$

	public static final String INDEXER_ID_KEY = "indexerId"; //$NON-NLS-1$
	public static final String INDEX_ALL_FILES = "indexAllFiles"; //$NON-NLS-1$
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
	private IPreferenceChangeListener fPreferenceChangeListener= new IPreferenceChangeListener(){
		public void preferenceChange(PreferenceChangeEvent event) {
			onPreferenceChange(event);
		}
	};
    
	/**
	 * Startup the PDOM. This mainly sets us up to handle model
	 * change events.
	 */
	public void startup() {
		Job startup= new Job(Messages.PDOMManager_StartJob_name) {
			protected IStatus run(IProgressMonitor monitor) {
				CoreModel model = CoreModel.getDefault();
				model.addElementChangedListener(fCModelListener);
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
    	IPreferencesService prefService = Platform.getPreferencesService();
    	return prefService.getString(CCorePlugin.PLUGIN_ID, INDEXER_ID_KEY,
    			CCorePlugin.DEFAULT_INDEXER, null);
    }
    
    public void setDefaultIndexerId(String indexerId) {
    	IEclipsePreferences prefs = new InstanceScope().getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs != null) {
    		prefs.put(INDEXER_ID_KEY, indexerId);
    		try {
    			prefs.flush();
    		} catch (BackingStoreException e) {
    		}
    	}
    }
    
    public String getIndexerId(ICProject project) {
    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs == null)
    		return getDefaultIndexerId();
    	
    	String indexerId = prefs.get(INDEXER_ID_KEY, null);
    	if (indexerId == null) {
    		// See if it is in the ICDescriptor
    		try {
    			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(project.getProject(), false);
    			if (desc != null) {
	    			ICExtensionReference[] ref = desc.get(CCorePlugin.INDEXER_UNIQ_ID);
	    			if (ref != null && ref.length > 0) {
	    				indexerId = ref[0].getID();
	    			}
	    			if (indexerId != null) {
	    				// Make sure it is a valid indexer
	    		    	IExtension indexerExt = Platform.getExtensionRegistry()
	    	    			.getExtension(CCorePlugin.INDEXER_UNIQ_ID, indexerId);
	    		    	if (indexerExt == null) {
	    		    		// It is not, forget about it.
	    		    		indexerId = null;
	    		    	}
	    			}
    			}
    		} catch (CoreException e) {
    		}
    		
        	// if Indexer still null schedule a job to get it
       		if (indexerId == null || indexerId.equals("org.eclipse.cdt.core.ctagsindexer")) //$NON-NLS-1$
       			// make it the default, ctags is gone
       			indexerId = getDefaultIndexerId();
       		
       		// Start a job to set the id.
    		setIndexerId(project, indexerId);
    	}
    	
  	    return indexerId;
    }

    public void setIndexerId(final ICProject project, String indexerId) {
    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs == null)
    		return; // TODO why would this be null?

    	prefs.put(INDEXER_ID_KEY, indexerId);
    	Job job= new Job(Messages.PDOMManager_savePrefsJob) {
        	protected IStatus run(IProgressMonitor monitor) {
       	    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
       	    	if (prefs != null) {
       	    		try {
       	    			prefs.flush();
       	    		} catch (BackingStoreException e) {
       	    		}
       	    	}
       	    	return Status.OK_STATUS;
        	}
    	};
    	job.setSystem(true);
    	job.setRule(project.getProject());
    	job.schedule(2000);
    }

    public void setIndexAllFiles(final ICProject project, boolean val) {
    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs == null)
    		return; // TODO why would this be null?

    	prefs.putBoolean(INDEX_ALL_FILES, val);
    	Job job= new Job(Messages.PDOMManager_savePrefsJob) {
        	protected IStatus run(IProgressMonitor monitor) {
       	    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
       	    	if (prefs != null) {
       	    		try {
       	    			prefs.flush();
       	    		} catch (BackingStoreException e) {
       	    		}
       	    	}
       	    	return Status.OK_STATUS;
        	}
    	};
    	job.setSystem(true);
    	job.setRule(project.getProject());
    	job.schedule(2000);
    }

    public boolean getIndexAllFiles(ICProject project) {
    	IScopeContext[] scope= new IScopeContext[] {new ProjectScope(project.getProject()), new InstanceScope()};
		return Platform.getPreferencesService().getBoolean(CCorePlugin.PLUGIN_ID, INDEX_ALL_FILES, false, scope); 
    }

    public IPDOMIndexer getIndexer(ICProject project) {
		return getIndexer(project, true);
	}
	
	public void onPreferenceChange(PreferenceChangeEvent event) {
		Object key= event.getKey();
		if (key.equals(INDEXER_ID_KEY) || key.equals(INDEX_ALL_FILES)) {
			Preferences node = event.getNode();
			if (CCorePlugin.PLUGIN_ID.equals(node.name())) {
				node= node.parent();
				IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(node.name());
				if (project.exists() && project.isOpen()) {
					ICProject cproject= CoreModel.getDefault().create(project);
					if (cproject != null) {
						try {
							changeIndexer(cproject);
						}
						catch (Exception e) {
							CCorePlugin.log(e);
						}
					}
				}
			}
		}
	}

	private void changeIndexer(ICProject cproject) throws CoreException {
		assert !Thread.holdsLock(fProjectToPDOM);
		IPDOMIndexer oldIndexer= null;
		String newid= getIndexerId(cproject);
		boolean allFiles= getIndexAllFiles(cproject);
		
		synchronized (fIndexerMutex) {
			oldIndexer= getIndexer(cproject, false);
			if (oldIndexer != null) {
				if (oldIndexer.getID().equals(newid) && oldIndexer.isIndexAllFiles(allFiles)) {
					return;
				}
			}
			createIndexer(cproject, newid, allFiles, true);
		}
		
		if (oldIndexer != null) {
			stopIndexer(oldIndexer);
		}
	}

	public IPDOMIndexer getIndexer(ICProject project, boolean create) {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fIndexerMutex) {
			IProject rproject = project.getProject();
			if (!rproject.isOpen()) {
				return null;
			}

			IPDOMIndexer indexer;
			try {
				indexer = (IPDOMIndexer)rproject.getSessionProperty(indexerProperty);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return null;
			}

			if (indexer != null && indexer.getProject().equals(project)) {
				return indexer;
			}

			if (create) {
				try {
					return createIndexer(project, getIndexerId(project), getIndexAllFiles(project), false);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
			return null;
		}
	}
		
    private IPDOMIndexer createIndexer(ICProject project, String indexerId, boolean allHeaders, boolean forceReindex) throws CoreException  {
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
						indexer.setIndexAllFiles(allHeaders);
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
			indexer.reindex();
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
    				fCompletedSources+= fCurrentTask.getCompletedSourcesCount();
    				fCompletedHeaders+= fCurrentTask.getCompletedHeadersCount();
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
    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs != null) {
    		prefs.addPreferenceChangeListener(fPreferenceChangeListener);
    	}
	}

	private void unregisterPreferenceListener(ICProject project) {
    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs != null) {
    		prefs.removePreferenceChangeListener(fPreferenceChangeListener);
    	}
	}

	public void changeProject(ICProject project, ICElementDelta delta) throws CoreException {
		IPDOMIndexer indexer = getIndexer(project, true);
		if (indexer != null)
			indexer.handleDelta(delta);

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
			indexer.reindex();
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
			while (true) {
				int currentTicks= 0;
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
		String detail= null;
		synchronized (fTaskQueueMutex) {
			completedHeaders= fCompletedHeaders;
			completedSources= fCompletedSources;
			for (Iterator iter = fTaskQueue.iterator(); iter.hasNext();) {
				IPDOMIndexerTask task = (IPDOMIndexerTask) iter.next();
				remainingSources+= task.estimateRemainingSources();
			}
			if (fCurrentTask != null) {
				remainingSources+= fCurrentTask.estimateRemainingSources();
				completedHeaders+= fCurrentTask.getCompletedHeadersCount();
				completedSources+= fCurrentTask.getCompletedSourcesCount();
				detail= fCurrentTask.getMonitorMessageDetail();
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
