/*******************************************************************************
 * Copyright (c) 2019, 2025 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.TargetStatus;

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
		// The launch target wizard NewGdbRemoteSerialTargetWizard ensures attributes are properly set.
		// The user is allowed to enter a custom device name. To be flexible, we do not validate the device name here.
		// This method is also called on a target that was removed from the manager after the user deleted it. Validation
		// would fail with an IllegalStateException, because the node is removed from the preferences.
		return TargetStatus.OK_STATUS;
	}
}
