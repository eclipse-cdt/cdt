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
import org.eclipse.cdt.debug.internal.core.InternalDebugCoreMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.ILaunchBarListener;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * A launchbar listener that attempts to set the active core build configuration
 * on the project adapted from the launch descriptor.
 * 
 * @since 8.3
 */
public class CoreBuildLaunchBarTracker implements ILaunchBarListener {

	private final ILaunchBarManager launchBarManager = CDebugCorePlugin.getService(ILaunchBarManager.class);
	private final ICBuildConfigurationManager configManager = CDebugCorePlugin
			.getService(ICBuildConfigurationManager.class);
	private final IToolChainManager toolChainManager = CDebugCorePlugin.getService(IToolChainManager.class);

	private ILaunchMode lastMode;
	private ILaunchDescriptor lastDescriptor;
	private ILaunchTarget lastTarget;

	private void setActiveBuildConfig(ILaunchMode mode, ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		IProject project = descriptor.getAdapter(IProject.class);
		if (project == null) {
			// Can we get the project name from the config
			ILaunchConfiguration configuration = launchBarManager.getLaunchConfiguration(descriptor, target);
			if (configuration == null) {
				// TODO why is the configuration null?
				return;
			}
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

		IProject finalProject = project;

		lastMode = mode;
		lastDescriptor = descriptor;
		lastTarget = target;

		// Pick build config based on toolchain for target
		// Since this may create a new config, need to run it in a Job
		Job job = new Job(InternalDebugCoreMessages.CoreBuildLaunchBarTracker_Job) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Map<String, String> properties = new HashMap<>();
					properties.putAll(target.getAttributes());
					Collection<IToolChain> tcs = toolChainManager.getToolChainsMatching(properties);
					if (!tcs.isEmpty()) {
						IToolChain toolChain = tcs.iterator().next();
						ICBuildConfiguration buildConfig = configManager.getBuildConfiguration(finalProject, toolChain,
								mode.getIdentifier(), monitor);
						if (buildConfig != null
								&& !buildConfig.getBuildConfiguration().equals(finalProject.getActiveBuildConfig())) {
							IProjectDescription desc = finalProject.getDescription();
							desc.setActiveBuildConfig(buildConfig.getBuildConfiguration().getName());
							finalProject.setDescription(desc, monitor);
						}
					}

					return Status.OK_STATUS;
				} catch (CoreException e) {
					return e.getStatus();
				}
			}
		};
		job.setRule(project.getWorkspace().getRoot());
		job.schedule();
	}

	@Override
	public void activeLaunchTargetChanged(ILaunchTarget target) {
		try {
			if (target == null || target.equals(ILaunchTarget.NULL_TARGET) || target.equals(lastTarget)) {
				return;
			}

			ILaunchMode mode = launchBarManager.getActiveLaunchMode();
			if (mode == null) {
				return;
			}

			ILaunchDescriptor descriptor = launchBarManager.getActiveLaunchDescriptor();
			setActiveBuildConfig(mode, descriptor, target);
		} catch (CoreException e) {
			CDebugCorePlugin.log(e.getStatus());
		}
	}

	@Override
	public void activeLaunchDescriptorChanged(ILaunchDescriptor descriptor) {
		try {
			if (descriptor == null || descriptor.equals(lastDescriptor)) {
				return;
			}

			ILaunchMode mode = launchBarManager.getActiveLaunchMode();
			if (mode == null) {
				return;
			}

			ILaunchTarget target = launchBarManager.getActiveLaunchTarget();
			setActiveBuildConfig(mode, descriptor, target);
		} catch (CoreException e) {
			CDebugCorePlugin.log(e.getStatus());
		}

	}

	@Override
	public void activeLaunchModeChanged(ILaunchMode mode) {
		try {
			if (mode == null || mode.equals(lastMode)) {
				return;
			}

			ILaunchDescriptor descriptor = launchBarManager.getActiveLaunchDescriptor();
			ILaunchTarget target = launchBarManager.getActiveLaunchTarget();
			setActiveBuildConfig(mode, descriptor, target);
		} catch (CoreException e) {
			CDebugCorePlugin.log(e.getStatus());
		}
	}

}
