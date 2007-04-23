/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.resources.ScannerProvider;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class ScannerInfoProviderProxy extends AbstractCExtensionProxy implements IScannerInfoProvider, IScannerInfoChangeListener{
	private Map listeners;
	private IScannerInfoProvider fProvider;


	public ScannerInfoProviderProxy(IProject project) {
		super(project, CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
		CProjectDescriptionManager.getInstance().addCProjectDescriptionListener(this, CProjectDescriptionEvent.LOADDED | CProjectDescriptionEvent.APPLIED);
	}

	public IScannerInfo getScannerInformation(IResource resource) {
		providerRequested();
		return fProvider.getScannerInformation(resource);
	}
	
	
	/**
	 * @param project
	 * @param info
	 */
	protected void notifyInfoListeners(IResource rc, IScannerInfo info) {
		// Call in the cavalry
		List listeners = (List)getListeners().get(rc);
		if (listeners == null) {
			return;
		}
		IScannerInfoChangeListener[] observers = new IScannerInfoChangeListener[listeners.size()];
		listeners.toArray(observers);
		for (int i = 0; i < observers.length; i++) {
			observers[i].changeNotification(rc, info);
		}
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#subscribe(org.eclipse.core.resources.IResource,
	 *      org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public synchronized void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		if (resource == null || listener == null) {
			return;
		}
		IProject project = resource.getProject();
		// Get listeners for this resource
		Map map = getListeners();
		List list = (List)map.get(project);
		if (list == null) {
			// Create a new list
			list = new ArrayList();
			map.put(project, list);
		}
		if (!list.contains(listener)) {
			// Add the new listener for the resource
			list.add(listener);
		}
	}

	/*
	 * @return
	 */
	private Map getListeners() {
		if (listeners == null) {
			listeners = new HashMap();
		}
		return listeners;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#unsubscribe(org.eclipse.core.resources.IResource,
	 *      org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public synchronized void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		if (resource == null || listener == null) {
			return;
		}
		IProject project = resource.getProject();
		// Remove the listener
		Map map = getListeners();
		List list = (List)map.get(project);
		if (list != null && !list.isEmpty()) {
			// The list is not empty so try to remove listener
			list.remove(listener);
		}
	}

	public void changeNotification(IResource rc, IScannerInfo info) {
		notifyInfoListeners(rc, info);
	}

	protected Object createDefaultProvider(ICConfigurationDescription des, boolean newStile){
		if(newStile)
			return new DescriptionScannerInfoProvider(getProject());
		return ScannerProvider.getInstance();
	}

	protected void deinitializeProvider(Object o) {
		IScannerInfoProvider provider = (IScannerInfoProvider)o;
		provider.unsubscribe(getProject(), this);
		if(provider instanceof DescriptionScannerInfoProvider){
			((DescriptionScannerInfoProvider)provider).close();
		}
	}

	protected void initializeProvider(Object o) {
		IScannerInfoProvider provider = (IScannerInfoProvider)o;
		fProvider = provider;
		provider.subscribe(getProject(), this);
	}
	
	protected void postProcessProviderChange(Object newProvider,
			Object oldProvider) {
		if(oldProvider != null)
			notifyInfoListeners(getProject(), getScannerInformation(getProject()));
	}

	protected boolean isValidProvider(Object o) {
		return o instanceof IScannerInfoProvider;
	}
}
