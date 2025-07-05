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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GDBRemoteSerialLaunchTargetProvider;
import org.eclipse.cdt.dsf.gdb.launching.GDBRemoteTCPLaunchTargetProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.AbstractLaunchConfigProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ProjectLaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/*
 * This is a modified copy of org.eclipse.cdt.debug.internal.core.launch.CoreBuildLocalLaunchConfigProvider.
 * TODO: Base both this and CoreBuildLocalLaunchConfigProvider on a standard CoreBuild LaunchConfigProvider.
 *       E.g. org.eclipse.cdt.debug.core.launch.CoreBuildGenericLaunchConfigProvider.
 */

public class CoreBuildGdbManualRemoteLaunchConfigProvider extends AbstractLaunchConfigProvider {

	private static final String TYPE_ID = "org.eclipse.cdt.debug.core.GdbManualRemoteCoreBuildLaunchConfigType"; //$NON-NLS-1$

	private Map<IProject, ILaunchConfiguration> configs = new HashMap<>();

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if (descriptor instanceof ProjectLaunchDescriptor) {
			String targetTypeId = target.getTypeId();
			return targetTypeId.equals(GDBRemoteTCPLaunchTargetProvider.TYPE_ID)
					|| targetTypeId.equals(GDBRemoteSerialLaunchTargetProvider.TYPE_ID);
		}
		return false;
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(TYPE_ID);
	}

	@Override
	protected ILaunchConfiguration createLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		IProject project = descriptor.getAdapter(IProject.class);
		String name = project.getName() + "_GdbManualRemote"; //$NON-NLS-1$
		ILaunchConfigurationType type = getLaunchConfigurationType(descriptor, target);
		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, name);

		// Set the default launcher to Manual.
		Map<String, String> remoteMap = new HashMap<>();
		remoteMap.put("[debug]", "org.eclipse.cdt.dsf.gdb.launch.remoteCLaunch"); //$NON-NLS-1$ //$NON-NLS-2$
		workingCopy.setAttribute("org.eclipse.debug.core.preferred_launchers", remoteMap); //$NON-NLS-1$

		populateLaunchConfiguration(descriptor, target, workingCopy);

		return workingCopy.doSave();
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		ILaunchConfiguration config = null;
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			config = configs.get(project);
			if (config == null) {
				config = createLaunchConfiguration(descriptor, target);
				// launch config added will get called below to add it to the
				// configs map
			}
		}
		return config;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		super.populateLaunchConfiguration(descriptor, target, workingCopy);

		// Set the project and the connection
		IProject project = descriptor.getAdapter(IProject.class);
		// CMainTab2 expects these attributes when calling CLaunchConfigurationTab.getContext()
		// Using empty string for default Core Build program.
		workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
		workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, ""); //$NON-NLS-1$
		workingCopy.setMappedResources(new IResource[] { project });
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfiguration(configuration)) {
			IProject project = configuration.getMappedResources()[0].getProject();
			configs.put(project, configuration);
			return true;
		}
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		for (Entry<IProject, ILaunchConfiguration> entry : configs.entrySet()) {
			if (configuration.equals(entry.getValue())) {
				configs.remove(entry.getKey());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			configs.remove(project);
		}
	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) throws CoreException {
		// TODO Auto-generated method stub

	}
}
