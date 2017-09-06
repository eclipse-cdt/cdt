/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.core.launch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.ILaunchBarListener;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * A launchbar listener that attempts to set the active build configuration on
 * the project adapted from the launch descriptor that supports the given
 * target.
 * 
 * @since 8.3
 */
public class CoreBuildLaunchBarTracker implements ILaunchBarListener {

	private final ILaunchBarManager launchBarManager = CDebugCorePlugin.getService(ILaunchBarManager.class);
	private final ICBuildConfigurationManager configManager = CDebugCorePlugin
			.getService(ICBuildConfigurationManager.class);
	private final IToolChainManager toolChainManager = CDebugCorePlugin.getService(IToolChainManager.class);

	private final String targetTypeId;

	public CoreBuildLaunchBarTracker(String targetTypeId) {
		this.targetTypeId = targetTypeId;
	}

	private void setActiveBuildConfig(String mode, ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		if (!targetTypeId.equals(target.getTypeId())) {
			return;
		}

		IProject project = descriptor.getAdapter(IProject.class);
		if (project == null) {
			// Can we get the project name from the config
			ILaunchConfiguration configuration = launchBarManager.getLaunchConfiguration(descriptor, target);
			String projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
			if (!projectName.isEmpty()) {
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
			
			if (project == null) {
				// Try the mapped resources
				IResource[] mappedResources = configuration.getMappedResources();
				if (mappedResources != null && mappedResources.length > 0) {
					project = mappedResources[0].getProject();
				}
			}
		}

		if (project == null || !configManager.supports(project)) {
			return;
		}

		// Pick build config based on toolchain for target
		Map<String, String> properties = new HashMap<>();
		properties.putAll(target.getAttributes());
		Collection<IToolChain> tcs = toolChainManager.getToolChainsMatching(properties);
		if (!tcs.isEmpty()) {
			IToolChain toolChain = tcs.iterator().next();
			IProgressMonitor monitor = new NullProgressMonitor();
			ICBuildConfiguration buildConfig = configManager.getBuildConfiguration(project, toolChain, mode,
					new NullProgressMonitor());
			if (buildConfig != null) {
				IProjectDescription desc = project.getDescription();
				desc.setActiveBuildConfig(buildConfig.getBuildConfiguration().getName());
				project.setDescription(desc, monitor);

				// Copy over the build attributes from the launch config
				ILaunchConfiguration configuration = launchBarManager.getLaunchConfiguration(descriptor, target);
				Map<String, String> buildProps = configuration.getAttribute(
						CoreBuildLaunchConfigDelegate.getBuildAttributeName(mode),
						buildConfig.getDefaultProperties());
				buildConfig.setProperties(buildProps);
			}
		}
	}

	@Override
	public void activeLaunchTargetChanged(ILaunchTarget target) {
		try {
			if (target == null || target.equals(ILaunchTarget.NULL_TARGET)) {
				return;
			}

			ILaunchMode launchMode = launchBarManager.getActiveLaunchMode();
			if (launchMode == null) {
				return;
			}

			String mode = launchMode.getIdentifier();
			ILaunchDescriptor descriptor = launchBarManager.getActiveLaunchDescriptor();
			setActiveBuildConfig(mode, descriptor, target);
		} catch (CoreException e) {
			CDebugCorePlugin.log(e.getStatus());
		}
	}

	@Override
	public void activeLaunchDescriptorChanged(ILaunchDescriptor descriptor) {
		try {
			if (descriptor == null) {
				return;
			}

			ILaunchMode launchMode = launchBarManager.getActiveLaunchMode();
			if (launchMode == null) {
				return;
			}

			String mode = launchMode.getIdentifier();
			ILaunchTarget target = launchBarManager.getActiveLaunchTarget();
			setActiveBuildConfig(mode, descriptor, target);
		} catch (CoreException e) {
			CDebugCorePlugin.log(e.getStatus());
		}

	}

	@Override
	public void activeLaunchModeChanged(ILaunchMode mode) {
		try {
			if (mode == null) {
				return;
			}

			ILaunchDescriptor descriptor = launchBarManager.getActiveLaunchDescriptor();
			ILaunchTarget target = launchBarManager.getActiveLaunchTarget();
			setActiveBuildConfig(mode.getIdentifier(), descriptor, target);
		} catch (CoreException e) {
			CDebugCorePlugin.log(e.getStatus());
		}
	}

}
