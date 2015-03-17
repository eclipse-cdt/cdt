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
package org.eclipse.launchbar.ui.internal.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.dialogs.LaunchConfigurationEditDialog;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class ConfigureActiveLaunchHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LaunchBarManager launchBarManager = Activator.getDefault().getLaunchBarUIManager().getManager();
		ILaunchDescriptor launchDesc = launchBarManager.getActiveLaunchDescriptor();
		if (launchDesc == null)
			return Status.OK_STATUS;
		openConfigurationEditor(launchDesc);
		return Status.OK_STATUS;
	}

	public static void openConfigurationEditor(ILaunchDescriptor desc) {
		if (desc == null)
			return;
		try {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			LaunchBarManager manager = Activator.getDefault().getLaunchBarUIManager().getManager();
			ILaunchMode mode = manager.getActiveLaunchMode();
			IRemoteConnection target = manager.getActiveLaunchTarget();
			if (target == null) {
				MessageDialog.openError(shell, "No Active Target", "You must create a target to edit this launch configuration.");
				return;
			}
			ILaunchConfigurationType configType = manager.getLaunchConfigurationType(desc, target);
			if (configType == null) {
				MessageDialog.openError(shell, "No launch configuration type", "Cannot edit this configuration");
				return;
			}
			ILaunchGroup group = DebugUIPlugin.getDefault().getLaunchConfigurationManager()
					.getLaunchGroup(configType, mode.getIdentifier());
			LaunchGroupExtension groupExt = DebugUIPlugin.getDefault().getLaunchConfigurationManager()
					.getLaunchGroup(group.getIdentifier());
			if (groupExt != null) {
				ILaunchConfiguration config = manager.getLaunchConfiguration(desc, target);
				if (config == null) {
					MessageDialog.openError(shell, "No launch configuration", "Cannot edit this configuration");
					return;
				}
				if (config.isWorkingCopy() && ((ILaunchConfigurationWorkingCopy) config).isDirty()) {
					config = ((ILaunchConfigurationWorkingCopy) config).doSave();
				}
				final LaunchConfigurationEditDialog dialog = new LaunchConfigurationEditDialog(shell, config, groupExt);
				dialog.setInitialStatus(Status.OK_STATUS);
				dialog.open();
			} else {
				MessageDialog.openError(shell, "Cannot determine mode", "Cannot edit this configuration");
				return;
			}
		} catch (CoreException e2) {
			Activator.log(e2);
		}
	}
}
