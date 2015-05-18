/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial
 *******************************************************************************/
package org.eclipse.launchbar.core.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.remote.core.IRemoteConnection;

public abstract class RemoteLaunchConfigurationDelegate extends LaunchConfigurationDelegate
		implements IRemoteLaunchConfigurationDelegate {

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode, IRemoteConnection target)
			throws CoreException {
		return getLaunch(configuration, mode);
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IRemoteConnection target,
			IProgressMonitor monitor) throws CoreException {
		return buildForLaunch(configuration, mode, monitor);
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IRemoteConnection target,
			IProgressMonitor monitor) throws CoreException {
		return finalLaunchCheck(configuration, mode, monitor);
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IRemoteConnection target,
			IProgressMonitor monitor) throws CoreException {
		return preLaunchCheck(configuration, mode, monitor);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, IRemoteConnection target, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		launch(configuration, mode, launch, monitor);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// do nothing by default assuming the subclass has implemented a proper remote launch() method.
	}

}
