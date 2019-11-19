/*******************************************************************************
 * Copyright (c) 2014,2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.ui.ILaunchBarUIManager;
import org.eclipse.launchbar.ui.internal.Activator;

public class ConfigureActiveLaunchHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
			ILaunchDescriptor launchDesc = launchBarManager.getActiveLaunchDescriptor();
			ILaunchBarUIManager uiManager = Activator.getService(ILaunchBarUIManager.class);
			return uiManager.openConfigurationEditor(launchDesc);
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

}
