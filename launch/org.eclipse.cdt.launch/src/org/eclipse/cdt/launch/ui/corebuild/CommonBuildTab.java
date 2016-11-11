/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.launch.ui.corebuild;

import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchConfigDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.ui.ILaunchBarLaunchConfigDialog;

/**
 * Common utilities for Core Build launch configuration tabs.
 * 
 * @since 9.1
 */
public abstract class CommonBuildTab extends AbstractLaunchConfigurationTab {

	public ILaunchBarLaunchConfigDialog getLaunchBarLaunchConfigDialog() {
		ILaunchConfigurationDialog dialog = getLaunchConfigurationDialog();
		return dialog instanceof ILaunchBarLaunchConfigDialog ? (ILaunchBarLaunchConfigDialog) dialog : null;
	}

	public ILaunchTarget getLaunchTarget() {
		ILaunchBarLaunchConfigDialog dialog = getLaunchBarLaunchConfigDialog();
		return dialog != null ? dialog.getLaunchTarget() : null;
	}

	public ICBuildConfiguration getBuildConfiguration(ILaunchConfiguration configuration) throws CoreException {
		String mode = getLaunchConfigurationDialog().getMode();
		ILaunchTarget target = getLaunchTarget();
		if (target == null) {
			return null;
		}

		ICBuildConfigurationManager bcManager = LaunchUIPlugin.getService(ICBuildConfigurationManager.class);
		IProject project = CoreBuildLaunchConfigDelegate.getProject(configuration);
		Map<String, String> properties = target.getAttributes();
		return bcManager.getBuildConfiguration(project, properties, mode, new NullProgressMonitor());
	}

}
