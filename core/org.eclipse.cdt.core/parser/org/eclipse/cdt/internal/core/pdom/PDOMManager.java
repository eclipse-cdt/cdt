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
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

/**
 * The PDOM Provider. This is likely temporary since I hope
 * to integrate the PDOM directly into the core once it has
 * stabilized.
 * 
 * @author Doug Schaefer
 */
public class PDOMManager implements IElementChangedListener, IJobChangeListener {

	private static PDOMManager instance;
	private PDOMUpdator currJob;
	
	private static final QualifiedName pdomProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdom"); //$NON-NLS-1$

	public static PDOMManager getInstance() {
		if (instance == null)
			instance = new PDOMManager();
		return instance;
	}
	
	public IPDOM getPDOM(IProject project) {
		try {
			IPDOM pdom = (IPDOM)project.getSessionProperty(pdomProperty);
			
			if (pdom == null) {
				pdom = new PDOMDatabase(project, this);
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
		
		currJob = new PDOMUpdator(event.getDelta(), currJob);
		currJob.addJobChangeListener(this);
		currJob.schedule();
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

}
