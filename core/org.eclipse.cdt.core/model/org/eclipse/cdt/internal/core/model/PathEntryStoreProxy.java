/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.settings.model.AbstractCExtensionProxy;
import org.eclipse.cdt.internal.core.settings.model.ConfigBasedPathEntryStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class PathEntryStoreProxy extends AbstractCExtensionProxy implements IPathEntryStore, IPathEntryStoreListener {
	private List<IPathEntryStoreListener> fListeners;
	private IPathEntryStore fStore;

	public PathEntryStoreProxy(IProject project){
		super(project, PathEntryManager.PATHENTRY_STORE_UNIQ_ID);
		fListeners = Collections.synchronizedList(new ArrayList<IPathEntryStoreListener>());
	}

	public IPathEntryStore getStore(){
		providerRequested();
		return fStore;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#addPathEntryStoreListener(org.eclipse.cdt.core.resources.IPathEntryStoreListener)
	 */
	@Override
	public void addPathEntryStoreListener(IPathEntryStoreListener listener) {
		fListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#removePathEntryStoreListener(org.eclipse.cdt.core.resources.IPathEntryStoreListener)
	 */
	@Override
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
	@Override
	public void close() {
		super.close();
		PathEntryStoreChangedEvent evt = new PathEntryStoreChangedEvent(this, getProject(), PathEntryStoreChangedEvent.STORE_CLOSED);
		IPathEntryStoreListener[] observers = new IPathEntryStoreListener[fListeners.size()];
		fListeners.toArray(observers);
		for (int i = 0; i < observers.length; i++) {
			observers[i].pathEntryStoreChanged(evt);
		}
	}

	@Override
	public IProject getProject() {
		return super.getProject();
	}

	@Override
	public ICExtensionReference getExtensionReference() {
		//TODO: calculate
		return null;
	}

	@Override
	public ICConfigExtensionReference getConfigExtensionReference() {
		return null;
	}

	@Override
	public IPathEntry[] getRawPathEntries() throws CoreException {
		providerRequested();
		return fStore.getRawPathEntries();
	}

	@Override
	public void setRawPathEntries(IPathEntry[] entries) throws CoreException {
		providerRequested();
		fStore.setRawPathEntries(entries);

	}

	@Override
	public void pathEntryStoreChanged(PathEntryStoreChangedEvent event) {
		notifyListeners(event);
	}

	@Override
	protected Object createDefaultProvider(ICConfigurationDescription cfgDes,
			boolean newStile) {
		if(newStile){
			return new ConfigBasedPathEntryStore(getProject());
		}
		return new DefaultPathEntryStore(getProject());
	}

	@Override
	protected void deinitializeProvider(Object o) {
		IPathEntryStore store = (IPathEntryStore)o;
		store.removePathEntryStoreListener(this);
		store.close();
	}

	@Override
	protected void initializeProvider(Object o) {
		IPathEntryStore store = (IPathEntryStore)o;
		fStore = store;
		store.addPathEntryStoreListener(this);
	}

	@Override
	protected boolean isValidProvider(Object o) {
		return o instanceof IPathEntryStore;
	}

	@Override
	protected void postProcessProviderChange(Object newProvider,
			Object oldProvider) {
//		if(oldProvider != null)
			fireContentChangedEvent(getProject());
	}

	@Override
	protected boolean doHandleEvent(CProjectDescriptionEvent event) {
		IPathEntryStore oldStore = fStore;
		boolean result = super.doHandleEvent(event);
		if(!result)
			postProcessProviderChange(fStore, oldStore);

		return result;
	}

}
