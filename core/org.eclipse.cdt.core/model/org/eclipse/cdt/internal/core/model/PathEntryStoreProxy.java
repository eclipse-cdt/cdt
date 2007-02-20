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
package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.resources.IPathEntryStoreListener;
import org.eclipse.cdt.core.resources.PathEntryStoreChangedEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.settings.model.AbstractCExtensionProxy;
import org.eclipse.cdt.internal.core.settings.model.ConfigBasedPathEntryStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class PathEntryStoreProxy extends AbstractCExtensionProxy implements IPathEntryStore, IPathEntryStoreListener {
	private List fListeners;
	private IPathEntryStore fStore;
	
	public PathEntryStoreProxy(IProject project){
		super(project, PathEntryManager.PATHENTRY_STORE_UNIQ_ID);
		fListeners = Collections.synchronizedList(new ArrayList());
	}
	
	public IPathEntryStore getStore(){
		providerRequested();
		return fStore;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#addPathEntryStoreListener(org.eclipse.cdt.core.resources.IPathEntryStoreListener)
	 */
	public void addPathEntryStoreListener(IPathEntryStoreListener listener) {		
		fListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#removePathEntryStoreListener(org.eclipse.cdt.core.resources.IPathEntryStoreListener)
	 */
	public void removePathEntryStoreListener(IPathEntryStoreListener listener) {
		fListeners.remove(listener);
	}

	private void fireContentChangedEvent(IProject project) {
		PathEntryStoreChangedEvent evt = new PathEntryStoreChangedEvent(this, project, PathEntryStoreChangedEvent.CONTENT_CHANGED);
		notifyListeners(evt);
	}
	
	private void notifyListeners(PathEntryStoreChangedEvent evt){
		IPathEntryStoreListener[] observers = new IPathEntryStoreListener[fListeners.size()];
		fListeners.toArray(observers);
		for (int i = 0; i < observers.length; i++) {
			observers[i].pathEntryStoreChanged(evt);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#fireClosedChangedEvent(IProject)
	 */
	public void close() {
		super.close();
		PathEntryStoreChangedEvent evt = new PathEntryStoreChangedEvent(this, getProject(), PathEntryStoreChangedEvent.STORE_CLOSED);
		IPathEntryStoreListener[] observers = new IPathEntryStoreListener[fListeners.size()];
		fListeners.toArray(observers);
		for (int i = 0; i < observers.length; i++) {
			observers[i].pathEntryStoreChanged(evt);
		}
	}

	public IProject getProject() {
		return super.getProject();
	}

	public ICExtensionReference getExtensionReference() {
		//TODO: calculate
		return null;
	}

	public IPathEntry[] getRawPathEntries() throws CoreException {
		providerRequested();
		return fStore.getRawPathEntries();
	}

	public void setRawPathEntries(IPathEntry[] entries) throws CoreException {
		providerRequested();
		fStore.setRawPathEntries(entries);

	}

	public void pathEntryStoreChanged(PathEntryStoreChangedEvent event) {
		notifyListeners(event);
	}
	
	protected Object createDefaultProvider(ICConfigurationDescription cfgDes,
			boolean newStile) {
		if(newStile){
			return new ConfigBasedPathEntryStore(getProject());
		}
		return new DefaultPathEntryStore(getProject());
	}

	protected void deinitializeProvider(Object o) {
		IPathEntryStore store = (IPathEntryStore)o;
		store.removePathEntryStoreListener(this);
		store.close();
	}

	protected void initializeProvider(Object o) {
		IPathEntryStore store = (IPathEntryStore)o;
		fStore = store;
		store.addPathEntryStoreListener(this);
	}

	protected boolean isValidProvider(Object o) {
		return o instanceof IPathEntryStore;
	}

	protected void postProcessProviderChange(Object newProvider,
			Object oldProvider) {
		if(oldProvider != null)
			fireContentChangedEvent(getProject());
	}
}
