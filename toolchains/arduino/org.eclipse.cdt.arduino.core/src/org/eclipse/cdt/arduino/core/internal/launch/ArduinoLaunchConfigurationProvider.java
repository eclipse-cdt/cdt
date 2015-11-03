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

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoProjectNature;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ProjectPerTargetLaunchConfigProvider;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

public class ArduinoLaunchConfigurationProvider extends ProjectPerTargetLaunchConfigProvider {

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(ArduinoLaunchConfigurationDelegate.TYPE_ID);
	}

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if (!super.supports(descriptor, target)) {
			return false;
		}

		if (target != null && !target.getTypeId().equals(ArduinoRemoteConnection.TYPE_ID)) {
			return false;
		}

		// must have the arduino nature
		IProject project = descriptor.getAdapter(IProject.class);
		return ArduinoProjectNature.hasNature(project);
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		super.populateLaunchConfiguration(descriptor, target, workingCopy);
		if (target != null) {
			workingCopy.setAttribute(ArduinoLaunchConfigurationDelegate.CONNECTION_NAME, target.getName());
		}
	}

	@Override
	protected ILaunchTarget getLaunchTarget(ILaunchConfiguration configuration) throws CoreException {
		String name = configuration.getAttribute(ArduinoLaunchConfigurationDelegate.CONNECTION_NAME, ""); //$NON-NLS-1$
		if (name.isEmpty()) {
			return null;
		}
		ILaunchTargetManager manager = Activator.getService(ILaunchTargetManager.class);
		return manager.getLaunchTarget(ArduinoRemoteConnection.TYPE_ID, name);
	}

	@Override
	protected boolean providesForNullTarget() {
		return true;
	}

}
