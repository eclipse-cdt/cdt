/*******************************************************************************
 * Copyright (c) 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.launch.ILaunchConfigurationTargetedDelegate;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;

/**
 * Launch delegate that adds the ILaunchTarget to the GdbLaunch.
 *
 * There are other things we could do with this such as make sure the binary we are
 * launching matches the cpu architecture and OS of the target, and add the launch
 * checks to do the same. For now, though, we are assuming the user knows what they
 * are doing when they set this up since it's all manual at this point.
 * @since 5.7
 */
public class GdbTargetedLaunchDelegate extends GdbLaunchDelegate implements ILaunchConfigurationTargetedDelegate {

	@Override
	public ITargetedLaunch getLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target)
			throws CoreException {
		GdbLaunch launch = (GdbLaunch) super.getLaunch(configuration, mode);
		launch.setLaunchTarget(target);
		return launch;
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		ILaunchTarget target = GdbPlugin.getService(ILaunchTargetManager.class).getDefaultLaunchTarget(configuration);
		return getLaunch(configuration, mode, target);
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		return super.buildForLaunch(configuration, mode, monitor);
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		return super.finalLaunchCheck(configuration, mode, monitor);
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		return super.preLaunchCheck(configuration, mode, monitor);
	}

}
