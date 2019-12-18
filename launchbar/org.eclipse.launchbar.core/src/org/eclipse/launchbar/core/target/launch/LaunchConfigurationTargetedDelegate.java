/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.core.target.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * An implementation of the ILaunchConfigurationTargetedDelegate.
 *
 * Implements the ILaunchConfigurationDelegate2 interfaces to pick out the
 * default target and pass it the targeted delegate methods.
 *
 * The default for the targeted delegate methods is to call the non targeted
 * methods in the super class.
 *
 */
public abstract class LaunchConfigurationTargetedDelegate extends LaunchConfigurationDelegate
		implements ILaunchConfigurationTargetedDelegate {

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		ILaunchTarget target = Activator.getLaunchTargetManager().getDefaultLaunchTarget(configuration);
		return getLaunch(configuration, mode, target);
	}

	@Override
	public ITargetedLaunch getLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target)
			throws CoreException {
		return new TargetedLaunch(configuration, mode, target, null);
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		ILaunchTarget target = Activator.getLaunchTargetManager().getDefaultLaunchTarget(configuration);
		return buildForLaunch(configuration, mode, target, monitor);
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		return super.buildForLaunch(configuration, mode, monitor);
	}

	public boolean superBuildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		return super.buildForLaunch(configuration, mode, monitor);
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		ILaunchTarget target = Activator.getLaunchTargetManager().getDefaultLaunchTarget(configuration);
		return preLaunchCheck(configuration, mode, target, monitor);
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		return super.preLaunchCheck(configuration, mode, monitor);
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		ILaunchTarget target = Activator.getLaunchTargetManager().getDefaultLaunchTarget(configuration);
		return finalLaunchCheck(configuration, mode, target, monitor);
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		return super.finalLaunchCheck(configuration, mode, monitor);
	}

}
