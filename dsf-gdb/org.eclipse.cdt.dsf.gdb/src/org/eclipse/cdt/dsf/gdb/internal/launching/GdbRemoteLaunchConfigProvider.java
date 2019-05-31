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
package org.eclipse.cdt.dsf.gdb.internal.launching;

import org.eclipse.cdt.dsf.gdb.launching.GDBRemoteSerialLaunchTargetProvider;
import org.eclipse.cdt.dsf.gdb.launching.GDBRemoteTCPLaunchTargetProvider;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.DefaultLaunchConfigProvider;
import org.eclipse.launchbar.core.DefaultLaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public class GdbRemoteLaunchConfigProvider extends DefaultLaunchConfigProvider {

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if (descriptor instanceof DefaultLaunchDescriptor) {
			ILaunchConfiguration config = descriptor.getAdapter(ILaunchConfiguration.class);
			if (LaunchUtils.getSessionType(config) != SessionType.REMOTE) {
				return false;
			}

			String targetTypeId = target.getTypeId();
			return targetTypeId.equals(GDBRemoteTCPLaunchTargetProvider.TYPE_ID)
					|| targetTypeId.equals(GDBRemoteSerialLaunchTargetProvider.TYPE_ID);
		}

		return false;
	}

}
