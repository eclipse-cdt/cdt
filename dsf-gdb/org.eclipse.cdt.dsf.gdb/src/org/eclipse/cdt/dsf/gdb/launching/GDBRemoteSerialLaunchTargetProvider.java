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

import java.io.IOException;

import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.TargetStatus;
import org.eclipse.launchbar.core.target.TargetStatus.Code;

/**
 * @since 5.7
 */
public class GDBRemoteSerialLaunchTargetProvider implements ILaunchTargetProvider {

	public static final String TYPE_ID = "org.eclipse.cdt.dsf.gdb.remoteSerialLaunchTargetType"; //$NON-NLS-1$

	@Override
	public void init(ILaunchTargetManager targetManager) {
		// No automatic targets. Adding all serial ports would be noisy.
	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		String device = target.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, ""); //$NON-NLS-1$
		if (device.isEmpty()) {
			return new TargetStatus(Code.ERROR, LaunchMessages.getString("GDBRemoteSerialLaunchTargetProvider_NoPort")); //$NON-NLS-1$
		}
		try {
			for (String port : SerialPort.list()) {
				if (device.equals(port)) {
					return TargetStatus.OK_STATUS;
				}
			}
			return new TargetStatus(Code.ERROR,
					String.format(LaunchMessages.getString("GDBRemoteSerialLaunchTargetProvider_NotFound"), device)); //$NON-NLS-1$
		} catch (IOException e) {
			return new TargetStatus(Code.ERROR, e.getLocalizedMessage());
		}
	}

}
