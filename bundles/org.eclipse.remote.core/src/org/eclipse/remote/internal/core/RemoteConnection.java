/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionControlService;
import org.eclipse.remote.core.IRemoteConnectionPropertyService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.core.exception.ConnectionExistsException;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The standard root class for remote connections. Implements common hook up
 * with the remote services and the remote services manager as well as handling
 * for services.
 */
public class RemoteConnection implements IRemoteConnection {

	private final RemoteConnectionType connectionType;
	private String name;

	private final Map<Class<? extends Service>, Service> servicesMap = new HashMap<>();

	private final ListenerList fListeners = new ListenerList();

	final static String EMPTY_STRING = ""; //$NON-NLS-1$

	public RemoteConnection(RemoteConnectionType connectionType, String name) {
		this.connectionType = connectionType;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getConnectionType()
	 */
	@Override
	public IRemoteConnectionType getConnectionType() {
		return connectionType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getService(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Service> T getService(Class<T> service) {
		T obj = (T) servicesMap.get(service);
		if (obj == null) {
			obj = connectionType.getConnectionService(this, service);
			if (obj != null) {
				servicesMap.put(service, obj);
			}
		}

		return obj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#hasService(java.lang.Class)
	 */
	@Override
	public <T extends Service> boolean hasService(Class<T> service) {
		return servicesMap.get(service.getName()) != null || connectionType.hasConnectionService(service);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Called from working copy when name has changed.
	 * 
	 * @param name
	 *            the new name
	 */
	void rename(String newName) throws ConnectionExistsException {
		try {
			// Copy the old preferences over and remove the old node
			if (connectionType.getPreferenceNode().nodeExists(newName)) {
				throw new ConnectionExistsException(newName);
			}

			Preferences newPrefs = connectionType.getPreferenceNode().node(newName);
			Preferences oldPrefs = getPreferences();
			for (String key : oldPrefs.keys()) {
				newPrefs.put(key, oldPrefs.get(key, null));
			}

			oldPrefs.removeNode();
		} catch (BackingStoreException e) {
			RemoteCorePlugin.log(e);
		}

		this.name = newName;
	}

	Preferences getPreferences() {
		return connectionType.getPreferenceNode().node(name);
	}

	ISecurePreferences getSecurePreferences() {
		return connectionType.getSecurePreferencesNode().node(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getAttribute(java.lang.String)
	 */
	@Override
	public String getAttribute(String key) {
		return getPreferences().get(key, EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getSecureAttribute(java.lang.String)
	 */
	@Override
	public String getSecureAttribute(String key) {
		try {
			return getSecurePreferences().get(key, EMPTY_STRING);
		} catch (StorageException e) {
			RemoteCorePlugin.log(e);
			return EMPTY_STRING;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getWorkingCopy()
	 */
	@Override
	public IRemoteConnectionWorkingCopy getWorkingCopy() {
		return new RemoteConnectionWorkingCopy(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {
		IRemoteConnectionPropertyService propertyService = getService(IRemoteConnectionPropertyService.class);
		if (propertyService != null) {
			return propertyService.getProperty(key);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#open(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		IRemoteConnectionControlService controlService = getService(IRemoteConnectionControlService.class);
		if (controlService != null) {
			controlService.open(monitor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#close()
	 */
	@Override
	public void close() {
		IRemoteConnectionControlService controlService = getService(IRemoteConnectionControlService.class);
		if (controlService != null) {
			controlService.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#isOpen()
	 */
	@Override
	public boolean isOpen() {
		IRemoteConnectionControlService controlService = getService(IRemoteConnectionControlService.class);
		if (controlService != null) {
			return controlService.isOpen();
		} else {
			// default is always open
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnection#addConnectionChangeListener(org.eclipse.remote.core.IRemoteConnectionChangeListener
	 * )
	 */
	@Override
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnection#removeConnectionChangeListener(org.eclipse.remote.core.
	 * IRemoteConnectionChangeListener
	 * )
	 */
	@Override
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#fireConnectionChangeEvent(int)
	 */
	@Override
	public void fireConnectionChangeEvent(final int type) {
		RemoteConnectionChangeEvent event = new RemoteConnectionChangeEvent(this, type);
		for (Object listener : fListeners.getListeners()) {
			((IRemoteConnectionChangeListener) listener).connectionChanged(event);
		}
		// fire to the global listeners too
		connectionType.getRemoteServicesManager().fireRemoteConnectionChangeEvent(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + " - " + connectionType.getName(); //$NON-NLS-1$
	}

}
