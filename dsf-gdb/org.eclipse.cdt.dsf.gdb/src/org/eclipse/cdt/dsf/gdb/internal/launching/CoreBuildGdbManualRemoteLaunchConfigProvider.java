/*******************************************************************************
 * Copyright (c) 2019 - 2025 QNX Software Systems and others.
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

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GDBRemoteSerialLaunchTargetProvider;
import org.eclipse.cdt.dsf.gdb.launching.GDBRemoteTCPLaunchTargetProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.AbstractLaunchConfigProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ProjectLaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

/*
 * TODO: Refactor this and CoreBuildLocalLaunchConfigProvider and
 *       org.eclipse.cdt.debug.core.launch.CoreBuildGenericLaunchConfigProvider
 *       to reduce duplicate code.
 */

public class CoreBuildGdbManualRemoteLaunchConfigProvider extends AbstractLaunchConfigProvider {

	public static final String TYPE_ID = "org.eclipse.cdt.debug.core.GdbManualRemoteCoreBuildLaunchConfigType"; //$NON-NLS-1$
	private static final String EMPTY = ""; //$NON-NLS-1$

	private Map<IProject, Map<String, ILaunchConfiguration>> configs = new HashMap<>();

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
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		String targetTypeId = target.getTypeId();
		String prefix = descriptor.getName() + " " + target.getId(); //$NON-NLS-1$
		if (targetTypeId.equals(GDBRemoteTCPLaunchTargetProvider.TYPE_ID)) {
			prefix = prefix + " TCP"; //$NON-NLS-1$
		} else if (targetTypeId.equals(GDBRemoteSerialLaunchTargetProvider.TYPE_ID)) {
			prefix = prefix + " Serial"; //$NON-NLS-1$
		}
		String name = launchManager.generateLaunchConfigurationName(prefix);
		ILaunchConfigurationType type = getLaunchConfigurationType(descriptor, target);
		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, name);

		populateLaunchConfiguration(descriptor, target, workingCopy);

		return workingCopy.doSave();
	}

	private String getTargetConfigKey(ILaunchTarget target) {
		String targetTypeId = target.getTypeId();
		if (targetTypeId.equals(GDBRemoteTCPLaunchTargetProvider.TYPE_ID)) {
			String host = target.getAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, EMPTY);
			String port = target.getAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, EMPTY);
			return host + '.' + port;
		}
		if (targetTypeId.equals(GDBRemoteSerialLaunchTargetProvider.TYPE_ID)) {
			String serialPort = target.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, EMPTY);
			String baudRate = target.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED, EMPTY);
			return serialPort + '.' + baudRate;
		}
		return target.getId(); // Fallback to target ID if no specific attributes are set
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		ILaunchConfiguration config = null;
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			Map<String, ILaunchConfiguration> projectConfigs = configs.get(project);
			if (projectConfigs == null) {
				projectConfigs = new HashMap<>();
				configs.put(project, projectConfigs);
			}

			config = projectConfigs.get(getTargetConfigKey(target));
			if (config == null) {
				config = createLaunchConfiguration(descriptor, target);
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
		workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EMPTY);
		workingCopy.setMappedResources(new IResource[] { project });

		// Set the default launcher to Manual.
		Map<String, String> remoteMap = new HashMap<>();
		remoteMap.put("[debug]", "org.eclipse.cdt.dsf.gdb.launch.remoteCLaunch"); //$NON-NLS-1$ //$NON-NLS-2$
		workingCopy.setAttribute("org.eclipse.debug.core.preferred_launchers", remoteMap); //$NON-NLS-1$

		String targetTypeId = target.getTypeId();
		if (targetTypeId.equals(GDBRemoteTCPLaunchTargetProvider.TYPE_ID)) {
			String host = target.getAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, EMPTY);
			String port = target.getAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, EMPTY);
			workingCopy.setAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
			workingCopy.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, host);
			workingCopy.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, port);
		}
		if (targetTypeId.equals(GDBRemoteSerialLaunchTargetProvider.TYPE_ID)) {
			String serialPort = target.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, EMPTY);
			String baudRate = target.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED, EMPTY);
			workingCopy.setAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, false);
			workingCopy.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, serialPort);
			workingCopy.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED, baudRate);
		}
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfiguration(configuration)) {
			IProject project = configuration.getMappedResources()[0].getProject();
			Map<String, ILaunchConfiguration> projectConfigs = configs.get(project);
			if (projectConfigs == null) {
				projectConfigs = new HashMap<>();
				configs.put(project, projectConfigs);
			}
			boolean isTCP = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
			if (isTCP) {
				// For TCP connections, we use host and port as the key
				String host = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, EMPTY);
				String port = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, EMPTY);
				String targetConfig = host + '.' + port;
				projectConfigs.put(targetConfig, configuration);
				return true;
			} else {
				// For Serial connections, we use serial port and baud rate as the key
				String serialPort = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV, EMPTY);
				String baudRate = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEV_SPEED, EMPTY);
				String targetConfig = serialPort + '.' + baudRate;
				projectConfigs.put(targetConfig, configuration);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		for (Entry<IProject, Map<String, ILaunchConfiguration>> projectEntry : configs.entrySet()) {
			Map<String, ILaunchConfiguration> projectConfigs = projectEntry.getValue();
			for (Entry<String, ILaunchConfiguration> entry : projectConfigs.entrySet()) {
				if (configuration.equals(entry.getValue())) {
					projectConfigs.remove(entry.getKey());
					if (projectConfigs.isEmpty()) {
						configs.remove(projectEntry.getKey());
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
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
		// Any other targets have the same host/port or device/baud-rate?
		ILaunchTargetManager targetManager = CDebugCorePlugin.getService(ILaunchTargetManager.class);
		for (ILaunchTarget t : targetManager.getLaunchTargets()) {
			if (!target.equals(t) && getTargetConfigKey(target).equals(getTargetConfigKey(t))) {
				// Yup, nothing to do then
				return;
			}
		}

		for (Entry<IProject, Map<String, ILaunchConfiguration>> projectEntry : configs.entrySet()) {
			Map<String, ILaunchConfiguration> projectConfigs = projectEntry.getValue();
			ILaunchConfiguration config = projectConfigs.get(getTargetConfigKey(target));
			if (config != null) {
				config.delete();
			}
		}

	}
}
