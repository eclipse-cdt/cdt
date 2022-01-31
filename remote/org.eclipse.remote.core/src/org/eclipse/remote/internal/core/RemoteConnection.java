/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

	@Override
	public IRemoteConnectionType getConnectionType() {
		return connectionType;
	}

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

	@Override
	public <T extends Service> boolean hasService(Class<T> service) {
		return servicesMap.get(service.getName()) != null || connectionType.hasConnectionService(service);
	}

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

			Preferences newPrefs = connectionType.getPreferenceNode().node(URLEncoder.encode(newName, "UTF-8"));
			Preferences oldPrefs = getPreferences();
			for (String key : oldPrefs.keys()) {
				newPrefs.put(key, oldPrefs.get(key, null));
			}

			oldPrefs.removeNode();
		} catch (BackingStoreException | UnsupportedEncodingException e) {
			RemoteCorePlugin.log(e);
		}

		this.name = newName;
	}

	Preferences getPreferences() {
		try {
			return connectionType.getPreferenceNode().node(URLEncoder.encode(name, "UTF-8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			// Should not happen!
			throw new RuntimeException(e);
		}
	}

	ISecurePreferences getSecurePreferences() {
		try {
			return connectionType.getSecurePreferencesNode().node(URLEncoder.encode(name, "UTF-8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			// Should not happen!
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getAttribute(String key) {
		return getPreferences().get(key, EMPTY_STRING);
	}

	@Override
	public String getSecureAttribute(String key) {
		try {
			return getSecurePreferences().get(key, EMPTY_STRING);
		} catch (StorageException e) {
			RemoteCorePlugin.log(e);
			return EMPTY_STRING;
		}
	}

	@Override
	public IRemoteConnectionWorkingCopy getWorkingCopy() {
		return new RemoteConnectionWorkingCopy(this);
	}

	@Override
	public String getProperty(String key) {
		IRemoteConnectionPropertyService propertyService = getService(IRemoteConnectionPropertyService.class);
		if (propertyService != null) {
			return propertyService.getProperty(key);
		} else {
			return null;
		}
	}

	@Override
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		IRemoteConnectionControlService controlService = getService(IRemoteConnectionControlService.class);
		if (controlService != null) {
			controlService.open(monitor);
		}
	}

	@Override
	public void close() {
		IRemoteConnectionControlService controlService = getService(IRemoteConnectionControlService.class);
		if (controlService != null) {
			controlService.close();
		}
	}

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

	@Override
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.add(listener);
	}

	@Override
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.remove(listener);
	}

	@Override
	public void fireConnectionChangeEvent(final int type) {
		RemoteConnectionChangeEvent event = new RemoteConnectionChangeEvent(this, type);
		for (Object listener : fListeners.getListeners()) {
			((IRemoteConnectionChangeListener) listener).connectionChanged(event);
		}
		// fire to the global listeners too
		connectionType.getRemoteServicesManager().fireRemoteConnectionChangeEvent(event);
	}

	@Override
	public String toString() {
		return name + " - " + connectionType.getName(); //$NON-NLS-1$
	}

}
