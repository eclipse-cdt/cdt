/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.remote.core.internal;

import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;

public class RemoteConnectionListener implements IRemoteConnectionChangeListener {

	private ILaunchTargetManager targetManager = Activator.getService(ILaunchTargetManager.class);

	@Override
	public void connectionChanged(RemoteConnectionChangeEvent event) {
		IRemoteConnection connection = event.getConnection();
		switch (event.getType()) {
		case RemoteConnectionChangeEvent.CONNECTION_ADDED:
			targetManager.addLaunchTarget(RemoteLaunchTargetProvider.TYPE_ID,
					RemoteLaunchTargetProvider.getTargetId(connection), connection.getName());
			break;
		case RemoteConnectionChangeEvent.CONNECTION_REMOVED:
			ILaunchTarget target = targetManager.getLaunchTarget(RemoteLaunchTargetProvider.TYPE_ID,
					RemoteLaunchTargetProvider.getTargetId(connection));
			if (target != null) {
				targetManager.removeLaunchTarget(target);
			}
			break;
		}
	}

}
