/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.util.LinkedList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The PDOM Provider. This is likely temporary since I hope
 * to integrate the PDOM directly into the core once it has
 * stabilized.
 * 
 * @author Doug Schaefer
 */
public class PDOMManager implements IPDOMManager, IElementChangedListener {

	private static final QualifiedName indexerProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomIndexer"); //$NON-NLS-1$
	private static final QualifiedName dbNameProperty
	= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomName"); //$NON-NLS-1$
	private static final QualifiedName pdomProperty
	= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdom"); //$NON-NLS-1$

	private IProgressMonitor group;
	
	private final ISchedulingRule indexerSchedulingRule = new ISchedulingRule() {
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	};
	
	public synchronized IPDOM getPDOM(ICProject project) throws CoreException {
		IProject rproject = project.getProject();
		PDOM pdom = (PDOM)rproject.getSessionProperty(pdomProperty);
		if (pdom == null) {
			String dbName = rproject.getPersistentProperty(dbNameProperty);
			if (dbName == null) {
				dbName = project.getElementName() + "."
					+ System.currentTimeMillis() + ".pdom";
				rproject.setPersistentProperty(dbNameProperty, dbName);
			}
			IPath dbPath = CCorePlugin.getDefault().getStateLocation().append(dbName);
			pdom = new PDOM(dbPath);
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
				indexer = createIndexer(getIndexerId(project), project);
			}
			
			return indexer;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
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
			ICProject project = (ICProject)delta.getElement();
			if (delta.getKind() != ICElementDelta.REMOVED) {
				if (project.getProject().exists()) {
					IPDOMIndexer indexer = getIndexer(project);
					if (indexer != null)
						// TODO project delete, should do something fancier here.
						indexer.handleDelta(delta);
				}
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
    			CCorePlugin.DEFAULT_INDEXER_UNIQ_ID, null);
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
    		
    		if (indexerId == null)
    			// make it the default
    			indexerId = getDefaultIndexerId();
    		
    		// Start a job to set the id.
    		new SetId(project, indexerId).schedule();
    	}
  	    return indexerId;
    }

    private static class SetId extends Job {
    	private final ICProject project;
    	private final String indexerId;
    	public SetId(ICProject project, String indexerId) {
    		super("Set Indexer Id");
    		this.project = project;
    		this.indexerId = indexerId;
    		setSystem(true);
    	}
    	protected IStatus run(IProgressMonitor monitor) {
    		try {
    			setId(project, indexerId);
        		return Status.OK_STATUS;
    		} catch (CoreException e) {
    			return e.getStatus();
    		}
    	}
    }
    
    private static void setId(ICProject project, String indexerId) throws CoreException {
    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs == null)
    		return; // TODO why would this be null?
    	
    	prefs.put(INDEXER_ID_KEY, indexerId);
    	try {
    		prefs.flush();
    	} catch (BackingStoreException e) {
    	}
    }
    
    public void setIndexerId(ICProject project, String indexerId) throws CoreException {
    	setId(project, indexerId);
		if (project.getProject().getSessionProperty(indexerProperty) != null)
			createIndexer(indexerId, project);    	
    }
    
    private IPDOMIndexer createIndexer(String indexerId, ICProject project) throws CoreException {
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
    
    public ISchedulingRule getIndexerSchedulingRule() {
    	return indexerSchedulingRule;
    }
    
    public IProgressMonitor getProgressGroup() {
    	if (group == null)
    		group = Platform.getJobManager().createProgressGroup();
    	return group;
    }
    
	/**
	 * Startup the PDOM. This mainly sets us up to handle model
	 * change events.
	 */
	public void startup() {
		CoreModel.getDefault().addElementChangedListener(this);
	}

}
