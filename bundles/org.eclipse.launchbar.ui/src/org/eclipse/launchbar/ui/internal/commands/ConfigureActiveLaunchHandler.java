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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPresentationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
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

	
	public static IStatus canOpenConfigurationEditor(ILaunchDescriptor desc) {
		if (desc == null)
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.DescriptorMustNotBeNull, new Exception(Messages.DescriptorMustNotBeNullDesc));
		LaunchBarManager manager = Activator.getDefault().getLaunchBarUIManager().getManager();
		ILaunchMode mode = manager.getActiveLaunchMode();
		IRemoteConnection target = manager.getActiveLaunchTarget();
		if (target == null) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,  Messages.NoActiveTarget, new Exception(Messages.NoActiveTargetDesc));
		}
		
		ILaunchConfigurationType configType = null;
		try {
			configType = manager.getLaunchConfigurationType(desc, target);
		} catch(CoreException ce) {/* ignore */ };
		if (configType == null) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.NoLaunchConfigType, new Exception(Messages.CannotEditLaunchConfiguration));
		}
		
		if( mode == null ) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.NoLaunchModeSelected, new Exception(Messages.NoLaunchModeSelected));
		}
		
		ILaunchGroup group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(configType, mode.getIdentifier());
		if( group == null ) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.NoLaunchGroupSelected, new Exception(Messages.NoLaunchGroupSelected));
		}
		
		String mode2 = group.getMode();
		if( mode2 == null ) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.NoLaunchModeSelected, new Exception(Messages.CannotEditLaunchConfiguration));
		}
		
		LaunchGroupExtension groupExt = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(group.getIdentifier());
		if (groupExt != null) {
			ILaunchConfiguration config = null;
			try {
				config = manager.getLaunchConfiguration(desc, target);
			} catch(CoreException ce) {
				// Ignore
			}
			if (config == null) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LaunchConfigurationNotFound, new Exception(Messages.LaunchConfigurationNotFoundDesc));
			}
			try {
				LaunchConfigurationPresentationManager mgr = LaunchConfigurationPresentationManager.getDefault();
				ILaunchConfigurationTabGroup tabgroup = mgr.getTabGroup(config, mode.getIdentifier());
			} catch(CoreException ce) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,Messages.NoLaunchTabsDefined, new Exception(Messages.NoLaunchTabsDefinedDesc));
			}
		} else {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,  Messages.CannotEditLaunchConfiguration, new Exception(Messages.CannotEditLaunchConfiguration));
		}
		return Status.OK_STATUS;
	}
	
	
	public static void openConfigurationEditor(ILaunchDescriptor desc) {
		if (desc == null)
			return;
		
		// Display the error message
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		IStatus s = canOpenConfigurationEditor(desc);
		if( !s.isOK()) {
			MessageDialog.openError(shell, s.getMessage(), s.getException() == null ? s.getMessage() : s.getException().getMessage());
			return;
		}
		
		// At this point, no error handling should be needed. 
		try {
			LaunchBarManager manager = Activator.getDefault().getLaunchBarUIManager().getManager();
			ILaunchMode mode = manager.getActiveLaunchMode();
			IRemoteConnection target = manager.getActiveLaunchTarget();
			ILaunchConfigurationType configType = manager.getLaunchConfigurationType(desc, target);
			ILaunchGroup group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(configType, mode.getIdentifier());
			LaunchGroupExtension groupExt = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(group.getIdentifier());
			ILaunchConfiguration config = manager.getLaunchConfiguration(desc, target);
			if (config.isWorkingCopy() && ((ILaunchConfigurationWorkingCopy) config).isDirty()) {
				config = ((ILaunchConfigurationWorkingCopy) config).doSave();
			}
			final LaunchConfigurationEditDialog dialog = new LaunchConfigurationEditDialog(shell, config, groupExt);
			dialog.setInitialStatus(Status.OK_STATUS);
			dialog.open();
		} catch (CoreException e2) {
			Activator.log(e2);
		}
	}
}
