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
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
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
public class PDOMManager implements IPDOMManager, IElementChangedListener, IJobChangeListener {

	private PDOMUpdator currJob;
	
	private static final QualifiedName pdomProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdom"); //$NON-NLS-1$

	public IPDOM getPDOM(IProject project) {
		try {
			IPDOM pdom = (IPDOM)project.getSessionProperty(pdomProperty);
			
			if (pdom == null) {
				pdom = new PDOM(project, createIndexer(project));
				project.setSessionProperty(pdomProperty, pdom);
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
		
		// TODO turn off indexing for now.
		return;
//		currJob = new PDOMUpdator(event.getDelta(), currJob);
//		currJob.addJobChangeListener(this);
//		currJob.schedule();
	}

	public void aboutToRun(IJobChangeEvent event) {
	}

	public void awake(IJobChangeEvent event) {
	}

	public synchronized void done(IJobChangeEvent event) {
		if (currJob == event.getJob())
			currJob = null;
	}

	public void running(IJobChangeEvent event) {
	}

	public void scheduled(IJobChangeEvent event) {
	}

	public void sleeping(IJobChangeEvent event) {
	}

	public void deletePDOM(IProject project) throws CoreException {
		IPDOM pdom = (IPDOM)project.getSessionProperty(pdomProperty); 
		project.setSessionProperty(pdomProperty, null);
		pdom.delete();
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
    
    public String getIndexerId(IProject project) {
    	IEclipsePreferences prefs = new ProjectScope(project).getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs == null)
    		return getDefaultIndexerId();
    	
    	String indexerId = prefs.get(INDEXER_ID_KEY, null);
    	if (indexerId == null) {
    		// See if it is in the ICDescriptor
    		try {
    			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(project, true);
    			ICExtensionReference[] ref = desc.get(CCorePlugin.INDEXER_UNIQ_ID);
    			if (ref != null && ref.length > 0) {
    				indexerId = ref[0].getID();
    			}
    		} catch (CoreException e) {
    		}
    		
    		if (indexerId == null)
    			// make it the default
    			indexerId = getDefaultIndexerId();
    		
    		setIndexerId(project, indexerId);
    	}
  	    return indexerId;
    }

    public void setIndexerId(IProject project, String indexerId) {
    	IEclipsePreferences prefs = new ProjectScope(project).getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs == null)
    		return; // TODO why would this be null?
    	
    	prefs.put(INDEXER_ID_KEY, indexerId);
    	try {
    		prefs.flush();
    	} catch (BackingStoreException e) {
    	}
    }
    
    private IPDOMIndexer createIndexer(IProject project) throws CoreException {
    	String indexerId = getIndexerId(project);
    	
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
