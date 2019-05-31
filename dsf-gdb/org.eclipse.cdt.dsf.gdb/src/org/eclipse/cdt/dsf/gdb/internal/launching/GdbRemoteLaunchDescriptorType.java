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

import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.DefaultLaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;

public class GdbRemoteLaunchDescriptorType implements ILaunchDescriptorType {

	@Override
	public ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException {
		if (launchObject instanceof ILaunchConfiguration) {
			ILaunchConfiguration config = (ILaunchConfiguration) launchObject;
			if (LaunchUtils.getSessionType(config) == SessionType.REMOTE) {
				return new DefaultLaunchDescriptor(this, config);
			}
		}
		return null;
	}

}
