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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
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

	private static final QualifiedName pdomProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdom"); //$NON-NLS-1$

	public IPDOM getPDOM(ICProject project) {
		try {
			IProject rproject = project.getProject();
			IPDOM pdom = (IPDOM)rproject.getSessionProperty(pdomProperty);
			
			if (pdom == null) {
				pdom = new PDOM(project, createIndexer(getIndexerId(project)));
				rproject.setSessionProperty(pdomProperty, pdom);
			}
			
			return pdom;
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
		processDelta(event.getDelta());
	}
	
	private void processDelta(ICElementDelta delta) {
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
					IPDOM pdom = getPDOM(project);
					if (pdom != null)
						// TODO project delete, should do something fancier here.
						pdom.getIndexer().handleDelta(delta);
				}
			}
			// TODO handle delete too.
		}
	}
	
	public void deletePDOM(ICProject project) throws CoreException {
		IProject rproject = project.getProject();
		IPDOM pdom = (IPDOM)rproject.getSessionProperty(pdomProperty); 
		rproject.setSessionProperty(pdomProperty, null);
		pdom.clear();
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
    			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(project.getProject(), true);
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
    	IPDOM pdom = getPDOM(project);
    	pdom.setIndexer(createIndexer(indexerId));
    }
    
    private IPDOMIndexer createIndexer(String indexerId) throws CoreException {
    	// Look up in extension point
    	IExtension indexerExt = Platform.getExtensionRegistry()
    		.getExtension(CCorePlugin.INDEXER_UNIQ_ID, indexerId);
    	IConfigurationElement[] elements = indexerExt.getConfigurationElements();
    	for (int i = 0; i < elements.length; ++i) {
    		IConfigurationElement element = elements[i];
    		if ("run".equals(element.getName())) //$NON-NLS-1$
    			return (IPDOMIndexer)element.createExecutableExtension("class"); //$NON-NLS-1$
    	}
    	throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID,
    			0, CCorePlugin.getResourceString("indexer.notFound"), null)); //$NON-NLS-1$	
    }
    
	/**
	 * Startup the PDOM. This mainly sets us up to handle model
	 * change events.
	 */
	public void startup() {
		CoreModel.getDefault().addElementChangedListener(this);
	}

}
