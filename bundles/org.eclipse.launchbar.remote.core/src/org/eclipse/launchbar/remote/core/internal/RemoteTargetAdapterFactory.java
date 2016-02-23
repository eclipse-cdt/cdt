/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.remote.core.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;

public class RemoteTargetAdapterFactory implements IAdapterFactory {

	private static final IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof ILaunchTarget) {
			ILaunchTarget target = (ILaunchTarget) adaptableObject;
			IRemoteConnectionType remoteType = remoteManager.getConnectionType(target.getTypeId());
			if (remoteType != null) {
				return (T) remoteType.getConnection(target.getId());
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IRemoteConnection.class };
	}

}
