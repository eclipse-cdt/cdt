/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher.ui.launchbar;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.docker.launcher.ContainerTargetTypeProvider;
import org.eclipse.cdt.docker.launcher.IContainerLaunchTarget;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.launchbar.core.AbstractLaunchConfigProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * @since 1.2
 * @author jjohnstn
 *
 */
public class CoreBuildContainerLaunchConfigProvider extends AbstractLaunchConfigProvider {

	private static final String TYPE_ID = "org.eclipse.cdt.docker.launcher.launchConfigurationType"; //$NON-NLS-1$

	private Map<IProject, Map<String, ILaunchConfiguration>> configs = new HashMap<>();

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		return target != null && ContainerTargetTypeProvider.TYPE_ID.equals(target.getTypeId());
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(TYPE_ID);
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		ILaunchConfiguration config = null;
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			Map<String, ILaunchConfiguration> configMap = configs.get(project);
			if (configMap == null) {
				configMap = new HashMap<>();
			}
			String connection = target.getAttribute(IContainerLaunchTarget.ATTR_CONNECTION_URI, ""); //$NON-NLS-1$
			String imageId = target.getAttribute(IContainerLaunchTarget.ATTR_IMAGE_ID, ""); //$NON-NLS-1$
			String imageName = connection + "-" + imageId; //$NON-NLS-1$
			config = configMap.get(imageName);
			if (config == null) {
				config = createLaunchConfiguration(descriptor, target);
				// launch config added will get called below to add it to the
				// configs map
			}
		}
		return config;
	}

	private String getImageName(ILaunchConfiguration config) throws CoreException {
		IProject project = config.getMappedResources()[0].getProject();
		ICBuildConfiguration cconfig = project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class);
		String image = cconfig.getToolChain().getProperty(IContainerLaunchTarget.ATTR_IMAGE_ID);
		String connection = cconfig.getToolChain().getProperty(IContainerLaunchTarget.ATTR_CONNECTION_URI); // $NON-NLS-1$
		String imageName = "unknown"; //$NON-NLS-1$
		if (connection != null && image != null) {
			imageName = connection + "-" + image; //$NON-NLS-1$
		}

		return imageName;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy wc) throws CoreException {
		super.populateLaunchConfiguration(descriptor, target, wc);

		// Set the project and the connection
		IProject project = descriptor.getAdapter(IProject.class);
		wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
		wc.setAttribute(IContainerLaunchTarget.ATTR_CONNECTION_URI,
				target.getAttribute(IContainerLaunchTarget.ATTR_CONNECTION_URI, null));
		wc.setAttribute(IContainerLaunchTarget.ATTR_IMAGE_ID,
				target.getAttribute(IContainerLaunchTarget.ATTR_IMAGE_ID, null));

		// DSF settings...use GdbUIPlugin preference store for defaults
		IPreferenceStore preferenceStore = GdbUIPlugin.getDefault().getPreferenceStore();
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
				preferenceStore.getString(IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND));
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT,
				preferenceStore.getString(IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_INIT));
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
				preferenceStore.getBoolean(IGdbDebugPreferenceConstants.PREF_DEFAULT_NON_STOP));
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
				IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT);
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
				IGDBLaunchConfigurationConstants.DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND_DEFAULT);
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_DEBUG_ON_FORK,
				IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_ON_FORK_DEFAULT);
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_TRACEPOINT_MODE,
				IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_MODE_DEFAULT);
		wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null); // default is the project directory

		wc.setMappedResources(new IResource[] { project });
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfiguration(configuration)) {
			IProject project = configuration.getMappedResources()[0].getProject();
			Map<String, ILaunchConfiguration> configMap = configs.get(project);
			if (configMap == null) {
				configMap = new HashMap<>();
				configs.put(project, configMap);
			}
			String imageName = getImageName(configuration);
			if (!imageName.equals("-")) { //$NON-NLS-1$
				configMap.put(imageName, configuration);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		for (Entry<IProject, Map<String, ILaunchConfiguration>> entry : configs.entrySet()) {
			for (Entry<String, ILaunchConfiguration> innerEntry : entry.getValue().entrySet()) {
				if (configuration.equals(innerEntry.getValue())) {
					entry.getValue().remove(innerEntry.getKey());
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
		// if (ownsLaunchConfiguration(configuration)) {
		// IProject project = configuration.getMappedResources()[0]
		// .getProject();
		// Map<String, ILaunchConfiguration> configMap = configs.get(project);
		// if (configMap == null) {
		// configMap = new HashMap<>();
		// configs.put(project, configMap);
		// }
		// String imageName = getImageName(configuration);
		// if (!imageName.isEmpty()) {
		// configMap.put(imageName, configuration);
		// }
		// return true;
		// }
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
		// nothing to do since the Local connection can't be removed
	}

}
