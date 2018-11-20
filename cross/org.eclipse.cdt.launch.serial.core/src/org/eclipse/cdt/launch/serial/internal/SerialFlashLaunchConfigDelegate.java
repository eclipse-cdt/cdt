/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.launch.serial.internal;

import org.eclipse.cdt.debug.core.launch.CoreBuildGenericLaunchConfigDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;

public class SerialFlashLaunchConfigDelegate extends CoreBuildGenericLaunchConfigDelegate {

	public static final String TYPE_ID = "org.eclipse.cdt.launch.serial.launchConfigurationType"; //$NON-NLS-1$

	@Override
	public ITargetedLaunch getLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target)
			throws CoreException {
		return new SerialFlashLaunch(configuration, mode, null, target);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// Start the launch (pause the serial port)
		((SerialFlashLaunch) launch).start();

		super.launch(configuration, mode, launch, monitor);
	}

}
