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
package org.eclipse.remote.internal.core.services.local;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnection.Service;
import org.eclipse.remote.core.IRemoteConnectionPropertyService;
import org.eclipse.remote.internal.core.RemoteCorePlugin;

public class LocalConnectionPropertyService implements IRemoteConnectionPropertyService {

	private final IRemoteConnection connection;

	public LocalConnectionPropertyService(IRemoteConnection connection) {
		this.connection = connection;
	}

	public static class Factory implements IRemoteConnectionPropertyService.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service) {
			if (service.equals(IRemoteConnectionPropertyService.class)) {
				return (T) new LocalConnectionPropertyService(remoteConnection);
			}
			return null;
		}
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return connection;
	}

	@Override
	public String getProperty(String key) {
		switch (key) {
		case IRemoteConnection.OS_NAME_PROPERTY:
			return RemoteCorePlugin.getDefault().getBundle().getBundleContext().getProperty("osgi.os"); //$NON-NLS-1$
		case IRemoteConnection.OS_ARCH_PROPERTY:
			return RemoteCorePlugin.getDefault().getBundle().getBundleContext().getProperty("osgi.arch"); //$NON-NLS-1$
		case IRemoteConnection.LOCALE_CHARMAP_PROPERTY:
			return System.getProperty("file.encoding"); //$NON-NLS-1$
		}
		return System.getProperty(key);
	}

}
