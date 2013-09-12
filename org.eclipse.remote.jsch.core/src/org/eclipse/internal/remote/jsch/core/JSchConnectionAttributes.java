/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.remote.jsch.core;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class JSchConnectionAttributes {
	public static final String CONNECTIONS_KEY = "connections"; //$NON-NLS-1$

	public static final String ADDRESS_ATTR = "JSCH_ADDRESS_ATTR"; //$NON-NLS-1$
	public static final String USERNAME_ATTR = "JSCH_USERNAME_ATTR"; //$NON-NLS-1$
	public static final String PASSWORD_ATTR = "JSCH_PASSWORD_ATTR"; //$NON-NLS-1$
	public static final String PORT_ATTR = "JSCH_PORT_ATTR"; //$NON-NLS-1$
	public static final String IS_PASSWORD_ATTR = "JSCH_IS_PASSWORD_ATTR"; //$NON-NLS-1$
	public static final String PASSPHRASE_ATTR = "JSCH_PASSPHRASE_ATTR"; //$NON-NLS-1$
	public static final String KEYFILE_ATTR = "JSCH_KEYFILE_ATTR"; //$NON-NLS-1$
	public static final String TIMEOUT_ATTR = "JSCH_TIMEOUT_ATTR"; //$NON-NLS-1$
	public static final String USE_LOGIN_SHELL_ATTR = "JSCH_USE_LOGIN_SHELL_ATTR"; //$NON-NLS-1$

	private String fName;
	private String fNewName;

	private final Map<String, String> fAttributes = Collections.synchronizedMap(new HashMap<String, String>());
	private final Map<String, String> fSecureAttributes = Collections.synchronizedMap(new HashMap<String, String>());

	public JSchConnectionAttributes(String name) {
		fName = name;
		load();
	}

	private void clearPreferences() {
		try {
			getPreferences().clear();
		} catch (BackingStoreException e) {
			Activator.log(e.getMessage());
		}
		getSecurePreferences().clear();
	}

	public JSchConnectionAttributes copy() {
		JSchConnectionAttributes copy = new JSchConnectionAttributes(fName);
		copy.getAttributes().putAll(fAttributes);
		copy.getSecureAttributes().putAll(fSecureAttributes);
		return copy;
	}

	private void flushPreferences() {
		try {
			getPreferences().flush();
		} catch (BackingStoreException e) {
			Activator.log(e.getMessage());
		}
		try {
			getSecurePreferences().flush();
		} catch (IOException e) {
			Activator.log(e.getMessage());
		}
	}

	public String getAttribute(String key, String def) {
		if (fAttributes.containsKey(key)) {
			return fAttributes.get(key);
		}
		return def;
	}

	public Map<String, String> getAttributes() {
		return fAttributes;
	}

	public String getName() {
		if (fNewName == null) {
			return fName;
		}
		return fNewName;
	}

	private Preferences getPreferences() {
		IEclipsePreferences root = InstanceScope.INSTANCE.getNode(Activator.getUniqueIdentifier());
		Preferences connections = root.node(CONNECTIONS_KEY);
		return connections.node(fName);
	}

	public String getSecureAttribute(String key, String def) {
		if (fSecureAttributes.containsKey(key)) {
			return fSecureAttributes.get(key);
		}
		return def;
	}

	public int getInt(String key, int def) {
		try {
			return Integer.parseInt(fAttributes.get(key));
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public boolean getBoolean(String key, boolean def) {
		if (fAttributes.containsKey(key)) {
			return Boolean.parseBoolean(fAttributes.get(key));
		}
		return def;
	}

	public Map<String, String> getSecureAttributes() {
		return fSecureAttributes;
	}

	private ISecurePreferences getSecurePreferences() {
		ISecurePreferences secRoot = SecurePreferencesFactory.getDefault();
		ISecurePreferences secConnections = secRoot.node(CONNECTIONS_KEY);
		return secConnections.node(fName);
	}

	private void load() {
		IEclipsePreferences root = InstanceScope.INSTANCE.getNode(Activator.getUniqueIdentifier());
		Preferences connections = root.node(CONNECTIONS_KEY);
		Preferences nodes = connections.node(fName);
		try {
			loadAttributes(nodes);
		} catch (BackingStoreException e) {
			Activator.log(e.getMessage());
		}
		ISecurePreferences secRoot = SecurePreferencesFactory.getDefault();
		ISecurePreferences secConnections = secRoot.node(CONNECTIONS_KEY);
		ISecurePreferences secNode = secConnections.node(fName);
		try {
			loadAuthAttributes(secNode);
		} catch (StorageException e) {
			Activator.log(e.getMessage());
		}
	}

	private void loadAttributes(Preferences node) throws BackingStoreException {
		fAttributes.clear();
		for (String key : node.keys()) {
			fAttributes.put(key, node.get(key, null));
		}
	}

	private void loadAuthAttributes(ISecurePreferences node) throws StorageException {
		fSecureAttributes.clear();
		for (String key : node.keys()) {
			fSecureAttributes.put(key, node.get(key, null));
		}
	}

	public void save() {
		clearPreferences();
		if (fNewName != null) {
			fName = fNewName;
			fNewName = null;
		}
		savePreferences();
		flushPreferences();
	}

	public void remove() {
		clearPreferences();
		flushPreferences();
	}

	private void savePreferences() {
		Preferences node = getPreferences();
		synchronized (fAttributes) {
			for (Entry<String, String> entry : fAttributes.entrySet()) {
				node.put(entry.getKey(), entry.getValue());
			}
		}
		try {
			ISecurePreferences secNode = getSecurePreferences();
			synchronized (fSecureAttributes) {
				for (Entry<String, String> entry : fSecureAttributes.entrySet()) {
					secNode.put(entry.getKey(), entry.getValue(), true);
				}
			}
		} catch (StorageException e) {
			Activator.log(e.getMessage());
		}
	}

	public void setAttribute(String key, String value) {
		fAttributes.put(key, value);
	}

	public void setName(String name) {
		fNewName = name;
	}

	public void setSecureAttribute(String key, String value) {
		fSecureAttributes.put(key, value);
	}
}
