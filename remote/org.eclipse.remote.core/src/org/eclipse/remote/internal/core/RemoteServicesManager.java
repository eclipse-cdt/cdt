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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionProviderService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.osgi.service.prefs.Preferences;

/**
 * The implementation for the remote services manager service.
 *
 */
public class RemoteServicesManager implements IRemoteServicesManager {

	private static final String LOCAL_SERVICES_ID = "org.eclipse.remote.LocalServices"; //$NON-NLS-1$

	// Map from id to remote services
	private final Map<String, RemoteConnectionType> connectionTypeMap = new HashMap<>();

	// Map from URI scheme to remote services
	private final Map<String, IRemoteConnectionType> schemeMap = new HashMap<>();

	private final List<IRemoteConnectionChangeListener> listeners = new LinkedList<>();
	private boolean inited;

	/**
	 * Loads up the services extensions and then loads up the persisted connections.
	 * This stuff can't be done from the constructor for the manager since that's done
	 * in the bundle activator which is way too early. It also gives us the ability
	 * to override the initialization in tests
	 */
	protected void init() {
		if (inited) {
			return;
		}
		inited = true;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(RemoteCorePlugin.getUniqueIdentifier(), "remoteServices"); //$NON-NLS-1$

		// Load up the connection types
		for (IExtension ext : point.getExtensions()) {
			for (IConfigurationElement ce : ext.getConfigurationElements()) {
				if (ce.getName().equals("connectionType")) { //$NON-NLS-1$
					RemoteConnectionType services = new RemoteConnectionType(ce, this);
					connectionTypeMap.put(services.getId(), services);
					String scheme = services.getScheme();
					if (scheme != null) {
						schemeMap.put(scheme, services);
					}
				}
			}
		}

		// Load up the services
		for (IExtension ext : point.getExtensions()) {
			for (IConfigurationElement ce : ext.getConfigurationElements()) {
				String name = ce.getName();
				if (name.equals("connectionTypeService") || name.equals("connectionService") //$NON-NLS-1$//$NON-NLS-2$
						|| name.equals("processService")) { //$NON-NLS-1$
					String id = ce.getAttribute("connectionTypeId"); //$NON-NLS-1$
					RemoteConnectionType services = connectionTypeMap.get(id);
					if (services != null) {
						services.addService(ce);
					}
				}
			}
		}

		// Init connection providers
		for (IRemoteConnectionType connectionType : connectionTypeMap.values()) {
			IRemoteConnectionProviderService providerService = connectionType
					.getService(IRemoteConnectionProviderService.class);
			if (providerService != null) {
				providerService.init();
			}
		}
	}

	public Preferences getPreferenceNode() {
		return InstanceScope.INSTANCE.getNode(RemoteCorePlugin.getUniqueIdentifier()).node("connections"); //$NON-NLS-1$
	}

	public ISecurePreferences getSecurePreferenceNode() {
		return SecurePreferencesFactory.getDefault().node(RemoteCorePlugin.getUniqueIdentifier()).node("connections"); //$NON-NLS-1$
	}

	@Override
	public IRemoteConnectionType getConnectionType(String id) {
		init();
		return connectionTypeMap.get(id);
	}

	@Override
	public IRemoteConnectionType getConnectionType(URI uri) {
		init();
		return schemeMap.get(uri.getScheme());
	}

	@Override
	public IRemoteConnectionType getLocalConnectionType() {
		return getConnectionType(LOCAL_SERVICES_ID);
	}

	@Override
	public List<IRemoteConnectionType> getAllConnectionTypes() {
		init();
		return new ArrayList<IRemoteConnectionType>(connectionTypeMap.values());
	}

	@Override
	@SafeVarargs
	public final List<IRemoteConnectionType> getConnectionTypesSupporting(
			Class<? extends IRemoteConnection.Service>... services) {
		List<IRemoteConnectionType> connTypes = new ArrayList<IRemoteConnectionType>();
		for (IRemoteConnectionType connType : getAllConnectionTypes()) {
			for (Class<? extends IRemoteConnection.Service> service : services) {
				if (connType.hasConnectionService(service)) {
					connTypes.add(connType);
					break;
				}
			}
		}
		return connTypes;
	}

	@Override
	@SafeVarargs
	public final List<IRemoteConnectionType> getConnectionTypesByService(
			Class<? extends IRemoteConnectionType.Service>... services) {
		List<IRemoteConnectionType> connTypes = new ArrayList<IRemoteConnectionType>();
		for (IRemoteConnectionType connType : getAllConnectionTypes()) {
			for (Class<? extends IRemoteConnectionType.Service> service : services) {
				if (!connType.hasService(service)) {
					connTypes.add(connType);
					break;
				}
			}
		}
		return connTypes;
	}

	@Override
	public List<IRemoteConnectionType> getRemoteConnectionTypes() {
		init();
		List<IRemoteConnectionType> connTypes = new ArrayList<>(connectionTypeMap.values().size() - 1);
		IRemoteConnectionType localServices = getLocalConnectionType();
		for (IRemoteConnectionType s : connectionTypeMap.values()) {
			if (!s.equals(localServices)) {
				connTypes.add(s);
			}
		}
		return connTypes;
	}

	@Override
	public List<IRemoteConnection> getAllRemoteConnections() {
		List<IRemoteConnection> connections = new ArrayList<>();
		for (IRemoteConnectionType connType : getAllConnectionTypes()) {
			connections.addAll(connType.getConnections());
		}
		return connections;
	}

	@Override
	public void addRemoteConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeRemoteConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public void fireRemoteConnectionChangeEvent(RemoteConnectionChangeEvent event) {
		List<IRemoteConnectionChangeListener> iListeners;
		synchronized (listeners) {
			iListeners = new ArrayList<>(listeners);
		}
		for (IRemoteConnectionChangeListener listener : iListeners) {
			listener.connectionChanged(event);
		}
	}

}
