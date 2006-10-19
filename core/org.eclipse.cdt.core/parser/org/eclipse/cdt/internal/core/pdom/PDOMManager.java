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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

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
import org.eclipse.cdt.internal.core.pdom.indexer.nulli.PDOMNullIndexer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
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

	private static final QualifiedName indexerProperty= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomIndexer"); //$NON-NLS-1$
	private static final QualifiedName dbNameProperty= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomName"); //$NON-NLS-1$
	private static final QualifiedName pdomProperty= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdom"); //$NON-NLS-1$

	private static final String INDEXER_ID_KEY = "indexerId"; //$NON-NLS-1$
	private static final ISchedulingRule NOTIFICATION_SCHEDULING_RULE = new ISchedulingRule(){
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	};

    private PDOMIndexerJob indexerJob;
	private IPDOMIndexerTask currentTask;
	private LinkedList taskQueue = new LinkedList();
    private Object taskQueueMutex = new Object();
	private Object fWakeupOnIdle= new Object();
    
    private Map fPDOMs= new HashMap();
	private ListenerList fChangeListeners= new ListenerList();
	private ListenerList fStateListeners= new ListenerList();
	
	private IndexChangeEvent fIndexChangeEvent= new IndexChangeEvent();
	private IndexerStateEvent fIndexerStateEvent= new IndexerStateEvent();

	// classes that help out to make this one simpler
	private IElementChangedListener fCModelListener= new CModelListener(this);
	private IndexFactory fIndexFactory= new IndexFactory(this);
    
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
		} 
	}

	public IPDOM getPDOM(ICProject project) throws CoreException {
		synchronized (fPDOMs) {
			IProject rproject = project.getProject();
			WritablePDOM pdom = (WritablePDOM)rproject.getSessionProperty(pdomProperty);
			if (pdom != null) {
				ICProject oldProject= (ICProject) fPDOMs.get(pdom);
				if (project.equals(oldProject)) {
					return pdom;
				}

				if (oldProject != null && oldProject.getProject().exists()) {
					// old project exists, don't use pdom
					String dbName= getDefaultName(project);
					rproject.setPersistentProperty(dbNameProperty, dbName);
				}
				else {
					// pdom can be reused, as the other project has gone
					fPDOMs.put(pdom, project);
					return pdom;
				}
			}		

			// make sure we get a unique name.
			String dbName= rproject.getPersistentProperty(dbNameProperty);
			if (dbName != null) {
				IPath dbPath= CCorePlugin.getDefault().getStateLocation().append(dbName);
				for (Iterator iter = fPDOMs.keySet().iterator(); dbName != null && iter.hasNext();) {
					PDOM existingPDOM = (PDOM) iter.next();
					if (existingPDOM.getPath().equals(dbPath)) {
						dbName= null;
					}
				}
			}
			
			if (dbName == null) {
				dbName = getDefaultName(project); 
				rproject.setPersistentProperty(dbNameProperty, dbName);
			}
			IPath dbPath = CCorePlugin.getDefault().getStateLocation().append(dbName);
			pdom = new WritablePDOM(dbPath);
			pdom.addListener(this);
			rproject.setSessionProperty(pdomProperty, pdom);
			fPDOMs.put(pdom, project);
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
    
    public String getIndexerId(ICProject project) throws CoreException {
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

    public void setIndexerId(final ICProject project, String indexerId) throws CoreException {
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
    
	public IPDOMIndexer getIndexer(ICProject project) {
		return getIndexer(project, true);
	}
	
	public void onPreferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(INDEXER_ID_KEY)) {
			Preferences node = event.getNode();
			if (CCorePlugin.PLUGIN_ID.equals(node.name())) {
				node= node.parent();
				IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(node.name());
				if (project.exists() && project.isOpen()) {
					ICProject cproject= CoreModel.getDefault().create(project);
					if (cproject != null) {
						try {
							String oldId= (String) event.getOldValue();
							String newId= (String) event.getNewValue();
							if (newId != null && !newId.equals(oldId)) {
								changeIndexer(cproject, newId);
							}
						}
						catch (Exception e) {
							CCorePlugin.log(e);
						}
					}
				}
			}
		}
	}

	private void changeIndexer(ICProject cproject, String newid) throws CoreException {
		IPDOMIndexer indexer= getIndexer(cproject, false);
		if (indexer != null) {
			stopIndexer(indexer);
		}
		indexer= createIndexer(cproject, newid, true);
	}

	public IPDOMIndexer getIndexer(ICProject project, boolean create) {
		try {
			synchronized (fIndexerMutex) {
				IProject rproject = project.getProject();
				if (!rproject.isOpen()) {
					return null;
				}
				IPDOMIndexer indexer = (IPDOMIndexer)rproject.getSessionProperty(indexerProperty);
				
				if (indexer != null) {
					ICProject indexerProject= indexer.getProject();
					if (!project.equals(indexerProject)) {
						indexer= null;
					}
				}
				if (indexer == null && create) {
					indexer = createIndexer(project, getIndexerId(project), false);
				}

				return indexer;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
		
    private IPDOMIndexer createIndexer(ICProject project, String indexerId, boolean forceReindex) throws CoreException {
		PDOM pdom= (PDOM) getPDOM(project);
		boolean reindex= forceReindex || pdom.versionMismatch() || pdom.isEmpty();
    	synchronized (fIndexerMutex) {
    		IPDOMIndexer indexer = null;
    		// Look up in extension point
    		IExtension indexerExt = Platform.getExtensionRegistry().getExtension(CCorePlugin.INDEXER_UNIQ_ID, indexerId);
    		if (indexerExt != null) {
    			IConfigurationElement[] elements = indexerExt.getConfigurationElements();
    			for (int i = 0; i < elements.length; ++i) {
    				IConfigurationElement element = elements[i];
    				if ("run".equals(element.getName())) { //$NON-NLS-1$
    					indexer = (IPDOMIndexer)element.createExecutableExtension("class"); //$NON-NLS-1$
    					break;
    				}
    			}
    		}
    		if (indexer == null) 
    			// Unknown index, default to the null one
    			indexer = new PDOMNullIndexer();

    		indexer.setProject(project);
    		project.getProject().setSessionProperty(indexerProperty, indexer);
    		registerPreferenceListener(project);

    		if (reindex) {
    			indexer.reindex();
    		}
    		return indexer;
    	}
    }

    public void enqueue(IPDOMIndexerTask subjob) {
    	boolean notifyBusy= false;
    	synchronized (taskQueueMutex) {
    		taskQueue.addLast(subjob);
			if (indexerJob == null) {
				indexerJob = new PDOMIndexerJob(this);
				indexerJob.schedule();
				notifyBusy= true;
			}
		}
    	if (notifyBusy) {
    		notifyState(IndexerStateEvent.STATE_BUSY);
    	}
    }
    
	IPDOMIndexerTask getNextTask() {
    	synchronized (taskQueueMutex) {
    		currentTask= taskQueue.isEmpty() ? null : (IPDOMIndexerTask)taskQueue.removeFirst();
    		return currentTask;
		}
    }
    
    void cancelledByUser() {
    	synchronized (taskQueueMutex) {
    		taskQueue.clear();
    		currentTask= null;
    		indexerJob= null;
		}
		notifyState(IndexerStateEvent.STATE_IDLE);
    }
    
    boolean finishIndexerJob() {
    	boolean idle= false;
    	synchronized (taskQueueMutex) {
    		currentTask= null;
			if (taskQueue.isEmpty()) {
				indexerJob = null;
				idle= true;
			} 
    	}
    	if (idle) {
    		notifyState(IndexerStateEvent.STATE_IDLE);
    	}
    	return idle;
    }
    
    private boolean isIndexerIdle() {
    	synchronized (taskQueueMutex) {
    		return currentTask == null && taskQueue.isEmpty();
    	}
    }
    
	private int getFilesToIndexCount() {
		int count= 0;
		synchronized (taskQueueMutex) {
			if (currentTask != null) {
				count= currentTask.getFilesToIndexCount();
			}
			for (Iterator iter = taskQueue.iterator(); iter.hasNext();) {
				IPDOMIndexerTask task= (IPDOMIndexerTask) iter.next();
				count+= task.getFilesToIndexCount();
			}
		}
		return count;
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

	public void removeProject(ICProject project) throws CoreException {
		IPDOMIndexer indexer= getIndexer(project, false);
		if (indexer != null) {
			stopIndexer(indexer);
		}
	}

    public void deleteProject(ICProject project, IResourceDelta delta) {
    	// Project is about to be deleted. Stop all indexing tasks for it
    	IPDOMIndexer indexer = getIndexer(project, false);
    	if (indexer != null) {
    		stopIndexer(indexer);
    	}
    }

	private void stopIndexer(IPDOMIndexer indexer) {
		ICProject project= indexer.getProject();
		unregisterPreferenceListener(project);
		synchronized (taskQueueMutex) {
			if (indexerJob != null) {
				indexerJob.cancelJobs(indexer);
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
    		assert !Thread.holdsLock(taskQueueMutex);
    		synchronized(fWakeupOnIdle) {
    			fWakeupOnIdle.notifyAll();
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
		synchronized (fPDOMs) {
			project = (ICProject) fPDOMs.get(pdom);
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
		monitor.beginTask(Messages.PDOMManager_JoinIndexerTask, IProgressMonitor.UNKNOWN);
		long limit= System.currentTimeMillis()+waitMaxMillis;
		try {
			while (true) {
				if (monitor.isCanceled()) {
					return false;
				}
				boolean wouldFail= false;
				int wait= 1000;
				if (waitMaxMillis >= 0) {
					int rest= (int) (limit - System.currentTimeMillis());
					if (rest < wait) {
						if (rest <= 0) {
							wouldFail= true;
						}

						wait= rest;
					}
				}
				
	    		assert !Thread.holdsLock(taskQueueMutex);
				synchronized(fWakeupOnIdle) {
					if (isIndexerIdle()) {
						return true;
					}
					if (wouldFail) {
						return false;
					}

					monitor.subTask(MessageFormat.format(Messages.PDOMManager_FilesToIndexSubtask, new Object[] {new Integer(getFilesToIndexCount())}));
					try {
						fWakeupOnIdle.wait(wait);
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
