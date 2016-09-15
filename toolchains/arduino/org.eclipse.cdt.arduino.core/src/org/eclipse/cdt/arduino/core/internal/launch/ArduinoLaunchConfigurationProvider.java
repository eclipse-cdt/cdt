/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.launch;

import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ProjectLaunchConfigProvider;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.remote.core.IRemoteConnection;

public class ArduinoLaunchConfigurationProvider extends ProjectLaunchConfigProvider {

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if (target != null) {
			IRemoteConnection connection = target.getAdapter(IRemoteConnection.class);
			if (connection != null) {
				return connection.getConnectionType().getId().equals(ArduinoRemoteConnection.TYPE_ID);
			}
		}
		return false;
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(ArduinoLaunchConfigurationDelegate.TYPE_ID);
	}

}
