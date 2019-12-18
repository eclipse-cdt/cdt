/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.ui.internal.Activator;

public class LaunchActiveCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
			StopActiveCommandHandler.stopActiveLaunches(launchBarManager);
			ILaunchConfiguration config = launchBarManager.getActiveLaunchConfiguration();
			if (config == null) {
				return Status.OK_STATUS;
			}

			ILaunchMode launchMode = launchBarManager.getActiveLaunchMode();
			if (launchMode == null) {
				return Status.OK_STATUS;
			}
			DebugUITools.launch(config, launchMode.getIdentifier());

			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	protected String getMode(ILaunchMode launchMode) {
		return launchMode.getIdentifier();
	}

}
