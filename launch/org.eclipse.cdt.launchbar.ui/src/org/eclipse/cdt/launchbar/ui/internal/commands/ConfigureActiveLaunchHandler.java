/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.ui.internal.commands;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

public class ConfigureActiveLaunchHandler extends AbstractHandler {

	private final ILaunchBarManager launchBarManager;
	
	public ConfigureActiveLaunchHandler() {
		launchBarManager = Activator.getService(ILaunchBarManager.class);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ILaunchDescriptor desc = launchBarManager.getActiveLaunchDescriptor();
			ILaunchTarget target = launchBarManager.getActiveLaunchTarget();
			ILaunchConfiguration launchConfiguration = launchBarManager.getLaunchConfiguration(desc, target);
			if (launchConfiguration == null)
				return Status.OK_STATUS;
			ILaunchConfigurationWorkingCopy wc = launchConfiguration.getWorkingCopy();

			ILaunchMode activeLaunchMode = launchBarManager.getActiveLaunchMode();
			ILaunchGroup group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(launchConfiguration.getType(), activeLaunchMode.getIdentifier());
			
			if (DebugUITools.openLaunchConfigurationPropertiesDialog(HandlerUtil.getActiveShell(event), wc, group.getIdentifier()) == Window.OK)
				wc.doSave();
		} catch (CoreException e) {
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}
}
