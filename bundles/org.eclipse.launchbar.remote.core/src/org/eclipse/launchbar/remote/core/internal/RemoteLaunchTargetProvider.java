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
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.TargetStatus;
import org.eclipse.launchbar.core.target.TargetStatus.Code;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServicesManager;

public class RemoteLaunchTargetProvider implements ILaunchTargetProvider {

	public static final String TYPE_ID = "org.eclipse.launchbar.remote.core.launchTargetType"; //$NON-NLS-1$
	public static final String DELIMITER = "|"; //$NON-NLS-1$

	private static final TargetStatus CLOSED = new TargetStatus(Code.ERROR, Messages.RemoteLaunchTargetProvider_Closed);

	@Override
	public void init(ILaunchTargetManager targetManager) {
		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);

		// Remove missing ones
		for (ILaunchTarget target : targetManager.getLaunchTargetsOfType(TYPE_ID)) {
			IRemoteConnection connection = target.getAdapter(IRemoteConnection.class);
			if (connection == null) {
				targetManager.removeLaunchTarget(target);
			}
		}

		// Add new ones
		// TODO filter out the Local connection?
		for (IRemoteConnection connection : manager.getAllRemoteConnections()) {
			String id = getTargetId(connection);
			if (targetManager.getLaunchTarget(TYPE_ID, id) == null) {
				targetManager.addLaunchTarget(TYPE_ID, id);
			}
		}
	}

	public static String getTargetId(IRemoteConnection connection) {
		return connection.getConnectionType().getId() + DELIMITER + connection.getName();
	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		IRemoteConnection connection = target.getAdapter(IRemoteConnection.class);
		if (connection != null) {
			if (connection.isOpen()) {
				return TargetStatus.OK_STATUS;
			} else {
				return CLOSED;
			}
		}
		return new TargetStatus(Code.ERROR, Messages.RemoteLaunchTargetProvider_Missing);
	}

}
