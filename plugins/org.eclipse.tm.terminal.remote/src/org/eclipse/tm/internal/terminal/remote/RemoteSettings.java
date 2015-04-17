/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.remote;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.terminal.remote.IRemoteSettings;

@SuppressWarnings("restriction")
public class RemoteSettings implements IRemoteSettings {
	protected String fRemoteServices;
	protected String fConnectionName;

	public RemoteSettings() {
	}

	public String getConnectionName() {
		return fConnectionName;
	}

	public String getRemoteServices() {
		return fRemoteServices;
	}

	public String getSummary() {
		return "Remote:" + getRemoteServices() + '_' + getConnectionName(); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return getSummary();
	}

	/**
	 * Load information into the RemoteSettings object.
	 */
	public void load(ISettingsStore store) {
		fRemoteServices = store.get(REMOTE_SERVICES, ""); //$NON-NLS-1$
		fConnectionName = store.get(CONNECTION_NAME, ""); //$NON-NLS-1$
	}

	/**
	 * Extract information from the RemoteSettings object.
	 */
	public void save(ISettingsStore store) {
		store.put(REMOTE_SERVICES, fRemoteServices);
		store.put(CONNECTION_NAME, fConnectionName);
	}

	public void setConnectionName(String name) {
		fConnectionName = name;
	}

	public void setRemoteServices(String remoteServices) {
		fRemoteServices = remoteServices;
	}
}
