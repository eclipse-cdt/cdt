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

import org.eclipse.cdt.arduino.core.internal.ArduinoProjectNature;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ProjectPerTypeLaunchConfigProvider;
import org.eclipse.remote.core.IRemoteConnection;

public class ArduinoLaunchConfigurationProvider extends ProjectPerTypeLaunchConfigProvider {

	@Override
	protected String getLaunchConfigurationTypeId() {
		return ArduinoLaunchConfigurationDelegate.TYPE_ID;
	}

	@Override
	protected String getRemoteConnectionTypeId() {
		return ArduinoRemoteConnection.TYPE_ID;
	}

	@Override
	public boolean supports(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		if (!super.supports(descriptor, target)) {
			return false;
		}

		// must have the arduino nature
		IProject project = descriptor.getAdapter(IProject.class);
		return ArduinoProjectNature.hasNature(project);
	}

}
