/*******************************************************************************
 * Copyright (c) 2017, 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.core.launch;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.build.ErrorBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration2;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.InternalDebugCoreMessages;
import org.eclipse.core.resources.IBuildConfiguration;
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
import org.eclipse.launchbar.core.target.ILaunchTargetListener;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.TargetStatus;

/**
 * A launchbar listener that attempts to set the active core build configuration
 * on the project adapted from the launch descriptor.
 *
 * @since 8.3
 */
public class CoreBuildLaunchBarTracker implements ILaunchBarListener, ILaunchTargetListener {

	private final ILaunchBarManager launchBarManager = CDebugCorePlugin.getService(ILaunchBarManager.class);
	private final ICBuildConfigurationManager configManager = CDebugCorePlugin
			.getService(ICBuildConfigurationManager.class);
	private final IToolChainManager toolChainManager = CDebugCorePlugin.getService(IToolChainManager.class);
	private final ILaunchTargetManager targetManager = CDebugCorePlugin.getService(ILaunchTargetManager.class);

	private ILaunchMode lastMode;
	private ILaunchDescriptor lastDescriptor;
	private ILaunchTarget lastTarget;

	public CoreBuildLaunchBarTracker() {
		targetManager.addListener(this);
	}

	public void dispose() {
		targetManager.removeListener(this);
	}

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
					properties.putAll(lastTarget.getAttributes());
					Collection<IToolChain> tcs = toolChainManager.getToolChainsMatching(properties);
					ICBuildConfiguration buildConfig = null;
					if (!tcs.isEmpty()) {
						// First, see if any existing non default build configs match
						configs: for (IBuildConfiguration config : finalProject.getBuildConfigs()) {
							if (!config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
								ICBuildConfiguration testConfig = configManager.getBuildConfiguration(config);
								if (testConfig != null && !(testConfig instanceof ErrorBuildConfiguration)) {
									for (IToolChain tc : tcs) {
										if (testConfig.getToolChain().equals(tc)) {
											buildConfig = testConfig;
											break configs;
										}
									}
								}
							}
						}

						if (buildConfig == null) {
							for (IToolChain toolChain : tcs) {
								buildConfig = configManager.getBuildConfiguration(finalProject, toolChain,
										mode.getIdentifier(), monitor);
								if (buildConfig != null) {
									break;
								}
							}
						}
					} else {
						// No toolchain, set to error builder since we can't do much in this situation
						// TODO check if it's already an error builder and just set the message
						String error = String.format(
								InternalDebugCoreMessages.CoreBuildLaunchBarTracker_NoToolchainForTarget,
								target.getId());
						if (!targetManager.getStatus(target).equals(TargetStatus.OK_STATUS)) {
							error += '\n' + InternalDebugCoreMessages.CoreBuildLaunchBarTracker_TargetNotAvailable;
						}
						IBuildConfiguration config = configManager.createBuildConfiguration(
								ErrorBuildConfiguration.PROVIDER, finalProject, ErrorBuildConfiguration.NAME, monitor);
						buildConfig = new ErrorBuildConfiguration(config, error);
						configManager.addBuildConfiguration(config, buildConfig);
					}

					if (buildConfig != null
							&& !buildConfig.getBuildConfiguration().equals(finalProject.getActiveBuildConfig())) {
						CoreModel m = CoreModel.getDefault();
						synchronized (m) {
							// set it as active
							IProjectDescription desc = finalProject.getDescription();
							IBuildConfiguration[] configs = finalProject.getBuildConfigs();
							Set<String> names = new LinkedHashSet<>();
							for (IBuildConfiguration config : configs) {
								names.add(config.getName());
							}
							// must add default config name as it may not be in build config list
							names.add(IBuildConfiguration.DEFAULT_CONFIG_NAME);
							// ensure active config is last in list so clean build will clean
							// active config last and this will be left in build console for user to see
							names.remove(buildConfig.getBuildConfiguration().getName());
							names.add(buildConfig.getBuildConfiguration().getName());

							desc.setBuildConfigs(names.toArray(new String[0]));
							desc.setActiveBuildConfig(buildConfig.getBuildConfiguration().getName());
							finalProject.setDescription(desc, monitor);
						}
						// notify the active build config that it is active
						((ICBuildConfiguration2) buildConfig).setActive();
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
	public void launchTargetStatusChanged(ILaunchTarget target) {
		try {
			if (targetManager.getStatus(target).equals(TargetStatus.OK_STATUS)) {
				// Now that we have access to the target, it may change what the build config
				ILaunchMode mode = launchBarManager.getActiveLaunchMode();
				if (mode == null) {
					return;
				}

				ILaunchDescriptor descriptor = launchBarManager.getActiveLaunchDescriptor();
				setActiveBuildConfig(mode, descriptor, target);
			}
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
