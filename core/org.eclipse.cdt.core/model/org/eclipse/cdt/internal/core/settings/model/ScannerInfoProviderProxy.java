/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class ScannerInfoProviderProxy extends AbstractCExtensionProxy
		implements IScannerInfoProvider, IScannerInfoChangeListener {
	private Map<IProject, List<IScannerInfoChangeListener>> listeners;
	private IScannerInfoProvider fProvider;

	public ScannerInfoProviderProxy(IProject project) {
		super(project, CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		providerRequested();
		return fProvider.getScannerInformation(resource);
	}

	protected void notifyInfoListeners(IResource rc, IScannerInfo info) {
		// Call in the cavalry
		List<IScannerInfoChangeListener> listeners = getListeners().get(rc);
		if (listeners == null) {
			return;
		}
		IScannerInfoChangeListener[] observers = new IScannerInfoChangeListener[listeners.size()];
		listeners.toArray(observers);
		for (int i = 0; i < observers.length; i++) {
			observers[i].changeNotification(rc, info);
		}
	}

	@Override
	public synchronized void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		if (resource == null || listener == null) {
			return;
		}
		IProject project = resource.getProject();
		// Get listeners for this resource
		Map<IProject, List<IScannerInfoChangeListener>> map = getListeners();
		List<IScannerInfoChangeListener> list = map.get(project);
		if (list == null) {
			// Create a new list
			list = new ArrayList<>();
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
	private Map<IProject, List<IScannerInfoChangeListener>> getListeners() {
		if (listeners == null) {
			listeners = new HashMap<>();
		}
		return listeners;
	}

	@Override
	public synchronized void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		if (resource == null || listener == null) {
			return;
		}
		IProject project = resource.getProject();
		// Remove the listener
		Map<IProject, List<IScannerInfoChangeListener>> map = getListeners();
		List<IScannerInfoChangeListener> list = map.get(project);
		if (list != null && !list.isEmpty()) {
			// The list is not empty so try to remove listener
			list.remove(listener);
		}
	}

	@Override
	public void changeNotification(IResource rc, IScannerInfo info) {
		notifyInfoListeners(rc, info);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Object createDefaultProvider(ICConfigurationDescription des, boolean newStile) {
		if (newStile)
			return new DescriptionScannerInfoProvider(getProject());
		return org.eclipse.cdt.core.resources.ScannerProvider.getInstance();
	}

	@Override
	protected void deinitializeProvider(Object o) {
		IScannerInfoProvider provider = (IScannerInfoProvider) o;
		provider.unsubscribe(getProject(), this);
		if (provider instanceof DescriptionScannerInfoProvider) {
			((DescriptionScannerInfoProvider) provider).close();
		}
	}

	@Override
	protected void initializeProvider(Object o) {
		IScannerInfoProvider provider = (IScannerInfoProvider) o;
		fProvider = provider;
		provider.subscribe(getProject(), this);
	}

	@Override
	protected void postProcessProviderChange(Object newProvider, Object oldProvider) {
		if (oldProvider != null)
			notifyInfoListeners(getProject(), getScannerInformation(getProject()));
	}

	@Override
	protected boolean isValidProvider(Object o) {
		return o instanceof IScannerInfoProvider;
	}
}
