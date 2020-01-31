/*******************************************************************************
 * Copyright (c) 2015, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.remote.internal;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.terminal.connector.remote.IRemoteSettings;

@SuppressWarnings("restriction")
public class RemoteSettings implements IRemoteSettings {
	protected String connectionTypeId;
	protected String connectionName;

	public RemoteSettings() {
	}

	@Override
	public String getConnectionName() {
		return connectionName;
	}

	@Override
	public String getConnectionTypeId() {
		return connectionTypeId;
	}

	public String getSummary() {
		return "Remote:" + getConnectionTypeId() + '_' + getConnectionName(); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return getSummary();
	}

	/**
	 * Load information into the RemoteSettings object.
	 */
	public void load(ISettingsStore store) {
		connectionTypeId = store.get(CONNECTION_TYPE_ID, ""); //$NON-NLS-1$
		connectionName = store.get(CONNECTION_NAME, ""); //$NON-NLS-1$
	}

	/**
	 * Extract information from the RemoteSettings object.
	 */
	public void save(ISettingsStore store) {
		store.put(CONNECTION_TYPE_ID, connectionTypeId);
		store.put(CONNECTION_NAME, connectionName);
	}

	public void setConnectionName(String name) {
		connectionName = name;
	}

	public void setConnectionTypeId(String id) {
		connectionTypeId = id;
	}
}
