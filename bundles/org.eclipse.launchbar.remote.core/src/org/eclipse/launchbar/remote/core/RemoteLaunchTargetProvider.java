/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.remote.core;

import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.TargetStatus;
import org.eclipse.launchbar.core.target.TargetStatus.Code;
import org.eclipse.launchbar.remote.core.internal.Activator;
import org.eclipse.launchbar.remote.core.internal.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;

public abstract class RemoteLaunchTargetProvider implements ILaunchTargetProvider, IRemoteConnectionChangeListener {

	private static final TargetStatus CLOSED = new TargetStatus(Code.ERROR, Messages.RemoteLaunchTargetProvider_Closed);

	private ILaunchTargetManager targetManager;

	protected abstract String getTypeId();

	@Override
	public void init(ILaunchTargetManager targetManager) {
		this.targetManager = targetManager;
		String typeId = getTypeId();

		IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);

		// Remove missing ones
		for (ILaunchTarget target : targetManager.getLaunchTargetsOfType(typeId)) {
			IRemoteConnection connection = target.getAdapter(IRemoteConnection.class);
			if (connection == null) {
				targetManager.removeLaunchTarget(target);
			}
		}

		// Add new ones
		IRemoteConnectionType remoteType = remoteManager.getConnectionType(typeId);
		for (IRemoteConnection remote : remoteType.getConnections()) {
			String id = remote.getName();
			if (targetManager.getLaunchTarget(typeId, id) == null) {
				targetManager.addLaunchTarget(typeId, id);
			}
		}

		remoteManager.addRemoteConnectionChangeListener(this);
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

	@Override
	public void connectionChanged(RemoteConnectionChangeEvent event) {
		IRemoteConnection connection = event.getConnection();
		if (connection.getConnectionType().getId().equals(getTypeId())) {
			switch (event.getType()) {
			case RemoteConnectionChangeEvent.CONNECTION_ADDED:
				targetManager.addLaunchTarget(getTypeId(), connection.getName());
				break;
			case RemoteConnectionChangeEvent.CONNECTION_REMOVED:
				ILaunchTarget target = targetManager.getLaunchTarget(getTypeId(), connection.getName());
				if (target != null) {
					targetManager.removeLaunchTarget(target);
				}
				break;
			}
		}
	}

}
