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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionControlService;
import org.eclipse.remote.core.IRemoteConnectionPropertyService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class RemoteConnectionWorkingCopy implements IRemoteConnectionWorkingCopy {

	private RemoteConnection original;
	private final RemoteConnectionType connectionType;
	private String newName;
	private final Map<String, String> newAttributes = new HashMap<>();
	private final Map<String, String> newSecureAttributes = new HashMap<>();
	private final List<IRemoteConnectionChangeListener> newListeners = new ArrayList<>();

	/**
	 * New Connection.
	 */
	public RemoteConnectionWorkingCopy(RemoteConnectionType connectionType, String name) {
		this.connectionType = connectionType;
		this.newName = name;
	}

	/**
	 * Edit Connection
	 */
	public RemoteConnectionWorkingCopy(RemoteConnection original) {
		this.original = original;
		this.connectionType = (RemoteConnectionType) original.getConnectionType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getName()
	 */
	@Override
	public String getName() {
		if (newName != null) {
			return newName;
		}

		if (original != null) {
			return original.getName();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		// set it only if it's changed
		if (original == null || !name.equals(original.getName())) {
			newName = name;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getAttribute(java.lang.String)
	 */
	@Override
	public String getAttribute(String key) {
		String value = newAttributes.get(key);
		if (value != null) {
			return value;
		}

		if (original != null) {
			return original.getAttribute(key);
		}

		return RemoteConnection.EMPTY_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void setAttribute(String key, String value) {
		// set only if it's changed or value is null
		if (original == null || value == null || !value.equals(original.getAttribute(key))) {
			newAttributes.put(key, value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getSecureAttribute(java.lang.String)
	 */
	@Override
	public String getSecureAttribute(String key) {
		String value = newSecureAttributes.get(key);
		if (value != null) {
			return value;
		}

		if (original != null) {
			return original.getSecureAttribute(key);
		}

		return RemoteConnection.EMPTY_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setSecureAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void setSecureAttribute(String key, String value) {
		// set only if it's changed or value is null
		if (original == null || value == null || !value.equals(original.getSecureAttribute(key))) {
			newSecureAttributes.put(key, value);
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
		if (original != null) {
			original.addConnectionChangeListener(listener);
		} else {
			newListeners.add(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnection#removeConnectionChangeListener(org.eclipse.remote.core.IRemoteConnectionChangeListener
	 * )
	 */
	@Override
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		if (original != null) {
			original.removeConnectionChangeListener(listener);
		} else {
			newListeners.remove(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#fireConnectionChangeEvent(int)
	 */
	@Override
	public void fireConnectionChangeEvent(int type) {
		if (original != null) {
			original.fireConnectionChangeEvent(type);
		} else {
			RemoteConnectionChangeEvent event = new RemoteConnectionChangeEvent(this, type);
			for (IRemoteConnectionChangeListener listener : newListeners) {
				listener.connectionChanged(event);
			}
			connectionType.getRemoteServicesManager().fireRemoteConnectionChangeEvent(event);
		}
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
	 * @see org.eclipse.remote.core.IRemoteConnection#getWorkingCopy()
	 */
	@Override
	public IRemoteConnectionWorkingCopy getWorkingCopy() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getService(java.lang.Class)
	 */
	@Override
	public <T extends Service> T getService(Class<T> service) {
		return connectionType.getConnectionService(this, service);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#hasService(java.lang.Class)
	 */
	@Override
	public <T extends Service> boolean hasService(Class<T> service) {
		if (original != null) {
			return original.hasService(service);
		} else {
			return connectionType.hasConnectionService(service);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#getOriginal()
	 */
	@Override
	public IRemoteConnection getOriginal() {
		return original;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return newName != null || !newAttributes.isEmpty() || !newSecureAttributes.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#save()
	 */
	@Override
	public IRemoteConnection save() throws RemoteConnectionException {
		if (newName != null && original != null) {
			// rename, delete the old one
			original.fireConnectionChangeEvent(RemoteConnectionChangeEvent.CONNECTION_RENAMED);
			connectionType.removeConnection(original.getName());
			original.rename(newName);
		}

		boolean added = false;
		if (original == null) {
			original = new RemoteConnection(connectionType, newName);
			added = true;
		}

		Preferences prefs = original.getPreferences();
		for (Map.Entry<String, String> entry : newAttributes.entrySet()) {
			String value = entry.getValue();
			if (value != null && !value.isEmpty()) {
				prefs.put(entry.getKey(), value);
			} else {
				prefs.remove(entry.getKey());
			}
		}
		try {
			prefs.flush();
		} catch (BackingStoreException e1) {
			throw new RemoteConnectionException(e1);
		}

		ISecurePreferences securePrefs = original.getSecurePreferences();
		for (Map.Entry<String, String> entry : newSecureAttributes.entrySet()) {
			String value = entry.getValue();
			if (value != null && !value.isEmpty()) {
				try {
					securePrefs.put(entry.getKey(), value, true);
				} catch (StorageException e) {
					throw new RemoteConnectionException(e);
				}
			} else {
				securePrefs.remove(entry.getKey());
			}
		}
		try {
			securePrefs.flush();
		} catch (IOException e) {
			throw new RemoteConnectionException(e);
		}

		/*
		 * Reset state for isDirty()
		 */
		newAttributes.clear();
		newSecureAttributes.clear();
		newName = null;

		connectionType.addConnection(original);
		if (added) {
			original.fireConnectionChangeEvent(RemoteConnectionChangeEvent.CONNECTION_ADDED);
		}
		return original;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#open(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		if (original != null) {
			original.open(monitor);
		} else {
			IRemoteConnectionControlService controlService = connectionType.getConnectionService(this,
					IRemoteConnectionControlService.class);
			if (controlService != null) {
				controlService.open(monitor);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#close()
	 */
	@Override
	public void close() {
		if (original != null) {
			original.close();
		} else {
			IRemoteConnectionControlService controlService = connectionType.getConnectionService(this,
					IRemoteConnectionControlService.class);
			if (controlService != null) {
				controlService.close();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#isOpen()
	 */
	@Override
	public boolean isOpen() {
		if (original != null) {
			return original.isOpen();
		}

		IRemoteConnectionControlService controlService = connectionType.getConnectionService(this,
				IRemoteConnectionControlService.class);
		if (controlService != null) {
			return controlService.isOpen();
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {
		if (original != null) {
			return original.getProperty(key);
		}

		IRemoteConnectionPropertyService propertyService = connectionType.getConnectionService(this,
				IRemoteConnectionPropertyService.class);
		if (propertyService != null) {
			return propertyService.getProperty(key);
		}

		return null;
	}

}
