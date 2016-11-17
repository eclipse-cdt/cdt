/*******************************************************************************
* Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.core;

import org.eclipse.remote.core.IRemoteConnectionProviderService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionType.Service;

public class ProxyConnectionProviderService implements IRemoteConnectionProviderService {

	private IRemoteConnectionType connectionType;
	
	public static class Factory implements IRemoteConnectionType.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnectionType connectionType, Class<T> service) {
			if (service.equals(IRemoteConnectionProviderService.class)) {
				return (T) new ProxyConnectionProviderService(connectionType);
			}
			return null;
		}
	}
	
	public ProxyConnectionProviderService(IRemoteConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	@Override
	public void init() {
	}

	@Override
	public IRemoteConnectionType getConnectionType() {
		return connectionType;
	}

}
