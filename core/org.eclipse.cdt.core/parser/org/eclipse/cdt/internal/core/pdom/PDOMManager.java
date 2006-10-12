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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.EmptyCIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.index.WritableCIndex;
import org.eclipse.cdt.internal.core.pdom.indexer.nulli.PDOMNullIndexer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The PDOM Provider. This is likely temporary since I hope
 * to integrate the PDOM directly into the core once it has
 * stabilized.
 * 
 * @author Doug Schaefer
 */
public class PDOMManager implements IPDOMManager, IWritableIndexManager, IElementChangedListener {

	private static final QualifiedName indexerProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomIndexer"); //$NON-NLS-1$
	private static final QualifiedName dbNameProperty
	= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomName"); //$NON-NLS-1$
	private static final QualifiedName pdomProperty
	= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdom"); //$NON-NLS-1$

	public synchronized IPDOM getPDOM(ICProject project) throws CoreException {
		IProject rproject = project.getProject();
		WritablePDOM pdom = (WritablePDOM)rproject.getSessionProperty(pdomProperty);
		if (pdom == null) {
			String dbName = rproject.getPersistentProperty(dbNameProperty);
			if (dbName == null) {
				dbName = project.getElementName() + "." //$NON-NLS-1$
					+ System.currentTimeMillis() + ".pdom"; //$NON-NLS-1$
				rproject.setPersistentProperty(dbNameProperty, dbName);
			}
			IPath dbPath = CCorePlugin.getDefault().getStateLocation().append(dbName);
			pdom = new WritablePDOM(dbPath);
			rproject.setSessionProperty(pdomProperty, pdom);
			if (pdom.versionMismatch())
				getIndexer(project).reindex();
		}
		return pdom;
	}

	public IPDOMIndexer getIndexer(ICProject project) {
		try {
			IProject rproject = project.getProject();
			IPDOMIndexer indexer = (IPDOMIndexer)rproject.getSessionProperty(indexerProperty);
			
			if (indexer == null) {
				indexer = createIndexer(project, getIndexerId(project));
			}
			
			return indexer;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
	
	public IIndex getIndex(ICProject project) throws CoreException {
		return getIndex(new ICProject[] {project}, 0);
	}

	public IIndex getIndex(ICProject project, int options) throws CoreException {
		return getIndex(new ICProject[] {project}, options);
	}
	
	public IIndex getIndex(ICProject[] projects) throws CoreException {
		return getIndex(projects, 0);
	}
	
	public IIndex getIndex(ICProject[] projects, int options) throws CoreException {
		boolean addDependencies= (options & ADD_DEPENDENCIES) != 0;
		boolean addDependent=    (options & ADD_DEPENDENT) != 0;
		
		HashMap map= new HashMap();
		Collection selectedProjects= getProjects(projects, addDependencies, addDependent, map, new Integer(1));
		
		ArrayList pdoms= new ArrayList();
		for (Iterator iter = selectedProjects.iterator(); iter.hasNext(); ) {
			ICProject project = (ICProject) iter.next();
			IWritableIndexFragment pdom= (IWritableIndexFragment) getPDOM(project);
			if (pdom != null) {
				pdoms.add(pdom);
			}
		}
		if (pdoms.isEmpty()) {
			return EmptyCIndex.INSTANCE;
		}
		
		// todo add the SDKs
		int primaryFragmentCount= pdoms.size();
		
		if (!addDependencies) {
			projects= (ICProject[]) selectedProjects.toArray(new ICProject[selectedProjects.size()]);
			selectedProjects.clear();
			// don't clear the map, so projects are not selected again
			selectedProjects= getProjects(projects, true, false, map, new Integer(2));
			for (Iterator iter = selectedProjects.iterator(); iter.hasNext(); ) {
				ICProject project = (ICProject) iter.next();
				IWritableIndexFragment pdom= (IWritableIndexFragment) getPDOM(project);
				if (pdom != null) {
					pdoms.add(pdom);
				}
			}
			// todo add further SDKs
		}
		
		return new CIndex((IIndexFragment[]) pdoms.toArray(new IIndexFragment[pdoms.size()]), primaryFragmentCount); 
	}

	private Collection getProjects(ICProject[] projects, boolean addDependencies, boolean addDependent, HashMap map, Integer markWith) {
		List projectsToSearch= new ArrayList();
		
		for (int i = 0; i < projects.length; i++) {
			ICProject cproject = projects[i];
			IProject project= cproject.getProject();
			checkAddProject(project, map, projectsToSearch, markWith);
		}
		
		if (addDependencies || addDependent) {
			for (int i=0; i<projectsToSearch.size(); i++) {
				IProject project= (IProject) projectsToSearch.get(i);
				IProject[] nextLevel;
				try {
					if (addDependencies) {
						nextLevel = project.getReferencedProjects();
						for (int j = 0; j < nextLevel.length; j++) {
							checkAddProject(nextLevel[j], map, projectsToSearch, markWith);
						}
					}
					if (addDependent) {
						nextLevel= project.getReferencingProjects();
						for (int j = 0; j < nextLevel.length; j++) {
							checkAddProject(nextLevel[j], map, projectsToSearch, markWith);
						}
					}
				} catch (CoreException e) {
					// silently ignore
					map.put(project, new Integer(0));
				}
			}
		}
		
		CoreModel cm= CoreModel.getDefault();
		Collection result= new ArrayList();
		for (Iterator iter= map.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry entry= (Map.Entry) iter.next();
			if (entry.getValue() == markWith) {
				ICProject cproject= cm.create((IProject) entry.getKey());
				if (cproject != null) {
					result.add(cproject);
				}
			}
		}
		return result;
	}

	private void checkAddProject(IProject project, HashMap map, List projectsToSearch, Integer markWith) {
		if (map.get(project) == null) {
			if (project.isOpen()) {
				map.put(project, markWith);
				projectsToSearch.add(project);
			}
			else {
				map.put(project, new Integer(0));
			}
		}
	}

	public IWritableIndex getWritableIndex(ICProject project) throws CoreException {
// mstodo to support dependent projects: Collection selectedProjects= getSelectedProjects(new ICProject[]{project}, false);
		
		Collection selectedProjects= Collections.singleton(project);
		
		ArrayList pdoms= new ArrayList();
		for (Iterator iter = selectedProjects.iterator(); iter.hasNext(); ) {
			ICProject p = (ICProject) iter.next();
			IWritableIndexFragment pdom= (IWritableIndexFragment) getPDOM(p);
			if (pdom != null) {
				pdoms.add(pdom);
			}
		}
		
		if (pdoms.isEmpty()) {
			throw new CoreException(CCorePlugin.createStatus(
					MessageFormat.format("Cannot obtain index for project ''{0}''", new Object[]{project.getElementName()})));
		}
		
		return new WritableCIndex((IWritableIndexFragment[]) pdoms.toArray(new IWritableIndexFragment[pdoms.size()]), new IIndexFragment[0]);
	}

	public synchronized void elementChanged(ElementChangedEvent event) {
		// Only respond to post change events
		if (event.getType() != ElementChangedEvent.POST_CHANGE)
			return;
		
		// Walk the delta sending the subtrees to the appropriate indexers
		try { 
			processDelta(event.getDelta());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
	
	private void processDelta(ICElementDelta delta) throws CoreException {
		int type = delta.getElement().getElementType();
		switch (type) {
		case ICElement.C_MODEL:
			// Loop through the children
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i)
				processDelta(children[i]);
			break;
		case ICElement.C_PROJECT:
			// Find the appropriate indexer and pass the delta on
			final ICProject project = (ICProject)delta.getElement();
			switch (delta.getKind()) {
			case ICElementDelta.ADDED:
		    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
		    	prefs.addNodeChangeListener(new IEclipsePreferences.INodeChangeListener() {
		    		public void added(NodeChangeEvent event) {
		    			String indexerId = event.getParent().get(INDEXER_ID_KEY, null);
		    			try {
		    				createIndexer(project, indexerId);
		    			} catch (CoreException e) {
		    				CCorePlugin.log(e);
		    			}
		    		}
		    		public void removed(NodeChangeEvent event) {
		    		}
		    	});
		    	break;
			case ICElementDelta.CHANGED:
				IPDOMIndexer indexer = getIndexer(project);
				if (indexer != null)
					indexer.handleDelta(delta);
			}
			// TODO handle delete too.
		}
	}
	
	public IElementChangedListener getElementChangedListener() {
		return this;
	}

    private static final String INDEXER_ID_KEY = "indexerId"; //$NON-NLS-1$

    public String getDefaultIndexerId() {
    	IPreferencesService prefService = Platform.getPreferencesService();
    	return prefService.getString(CCorePlugin.PLUGIN_ID, INDEXER_ID_KEY,
    			CCorePlugin.DEFAULT_INDEXER, null);
    }
    
    public void setDefaultIndexerId(String indexerId) {
    	IEclipsePreferences prefs = new InstanceScope().getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs == null)
    		return; // TODO why would this be null?
    	
    	prefs.put(INDEXER_ID_KEY, indexerId);
    	try {
    		prefs.flush();
    	} catch (BackingStoreException e) {
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

    // This job is only used when setting during a get. Sometimes the get is being
    // done in an unfriendly environment, e.g. startup.
    private class SavePrefs extends Job {
    	private final ICProject project;
    	public SavePrefs(ICProject project) {
    		super("Save Project Preferences"); //$NON-NLS-1$
    		this.project = project;
    		setSystem(true);
    		setRule(project.getProject());
    	}
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
    }
    
    public void setIndexerId(ICProject project, String indexerId) throws CoreException {
    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs == null)
    		return; // TODO why would this be null?

    	String oldId = prefs.get(INDEXER_ID_KEY, null);
    	if (!indexerId.equals(oldId)) {
	    	prefs.put(INDEXER_ID_KEY, indexerId);
	    	createIndexer(project, indexerId).reindex();
	    	new SavePrefs(project).schedule(2000);
    	}
    }
    
    private IPDOMIndexer createIndexer(ICProject project, String indexerId) throws CoreException {
    	IPDOMIndexer indexer = null;
    	// Look up in extension point
    	IExtension indexerExt = Platform.getExtensionRegistry()
    		.getExtension(CCorePlugin.INDEXER_UNIQ_ID, indexerId);
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

    	return indexer;
    }

    // Indexer manager
    private PDOMIndexerJob indexerJob;
	private LinkedList indexerJobQueue = new LinkedList();
    private Object indexerJobMutex = new Object();
    
    public void enqueue(IPDOMIndexerTask subjob) {
    	synchronized (indexerJobMutex) {
    		indexerJobQueue.addLast(subjob);
			if (indexerJob == null) {
				indexerJob = new PDOMIndexerJob(this);
				indexerJob.schedule();
			}
		}
    }
    
    IPDOMIndexerTask getNextTask() {
    	synchronized (indexerJobMutex) {
    		return indexerJobQueue.isEmpty()
    			? null
    			: (IPDOMIndexerTask)indexerJobQueue.removeFirst();
		}
    }
    
    void cancelledByUser() {
    	synchronized (indexerJobMutex) {
    		indexerJobQueue.clear();
		}
    }
    
    boolean finishIndexerJob() {
    	synchronized (indexerJobMutex) {
			if (indexerJobQueue.isEmpty()) {
				indexerJob = null;
				return true;
			} else
				// No way, there's more work to do
				return false;
		}
    }
    
    public void deleting(ICProject project) {
    	// Project is about to be deleted. Stop all indexing tasks for it
    	IPDOMIndexer indexer = getIndexer(project);
    	synchronized (indexerJobMutex) {
			if (indexerJob != null) {
				indexerJob.cancelJobs(indexer);
			}
		}
    }
    
	/**
	 * Startup the PDOM. This mainly sets us up to handle model
	 * change events.
	 */
	public void startup() {
		CoreModel.getDefault().addElementChangedListener(this);
	}

}
