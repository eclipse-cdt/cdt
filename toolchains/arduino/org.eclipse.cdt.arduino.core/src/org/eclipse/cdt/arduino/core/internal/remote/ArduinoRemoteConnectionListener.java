/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.remote;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;

public class ArduinoRemoteConnectionListener implements IRemoteConnectionChangeListener {

	public static ArduinoRemoteConnectionListener INSTANCE = new ArduinoRemoteConnectionListener();

	@Override
	public void connectionChanged(RemoteConnectionChangeEvent event) {
		switch (event.getType()) {
		case RemoteConnectionChangeEvent.CONNECTION_ADDED:
			if (event.getConnection().getConnectionType().getId().equals(ArduinoRemoteConnection.TYPE_ID)) {
				ILaunchTargetManager targetManager = Activator.getService(ILaunchTargetManager.class);
				targetManager.addLaunchTarget(ArduinoRemoteConnection.TYPE_ID, event.getConnection().getName());
			}
		case RemoteConnectionChangeEvent.CONNECTION_REMOVED:
			if (event.getConnection().getConnectionType().getId().equals(ArduinoRemoteConnection.TYPE_ID)) {
				ILaunchTargetManager targetManager = Activator.getService(ILaunchTargetManager.class);
				ILaunchTarget target = targetManager.getLaunchTarget(ArduinoRemoteConnection.TYPE_ID,
						event.getConnection().getName());
				if (target != null) {
					targetManager.removeLaunchTarget(target);
				}
			}
		}
	}

}
