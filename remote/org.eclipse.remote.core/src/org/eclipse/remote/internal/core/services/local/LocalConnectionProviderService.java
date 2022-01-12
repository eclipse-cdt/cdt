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
package org.eclipse.remote.internal.core.services.local;

import org.eclipse.remote.core.IRemoteConnectionProviderService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionType.Service;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.core.RemoteCorePlugin;

public class LocalConnectionProviderService implements IRemoteConnectionProviderService {

	private static final String localConnectionName = Messages.LocalConnectionProviderService_LocalConnectionName;

	private IRemoteConnectionType connectionType;

	public static class Factory implements IRemoteConnectionType.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnectionType connectionType, Class<T> service) {
			if (service.equals(IRemoteConnectionProviderService.class)) {
				return (T) new LocalConnectionProviderService(connectionType);
			}
			return null;
		}
	}

	public LocalConnectionProviderService(IRemoteConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	@Override
	public void init() {
		if (connectionType.getConnections().isEmpty()) {
			try {
				connectionType.newConnection(localConnectionName).save();
			} catch (RemoteConnectionException e) {
				RemoteCorePlugin.log(e.getStatus());
			}
		}
	}

	@Override
	public IRemoteConnectionType getConnectionType() {
		return connectionType;
	}

}
