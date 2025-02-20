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
	/**
	 * Useful for testing. Allows tests to wait for LaunchBarTracker to finish processing.
	 * <pre>
	 * Job.getJobManager().join(CoreBuildLaunchBarTracker.JOB_FAMILY_CORE_BUILD_LAUNCH_BAR_TRACKER, null);
	 * </pre>
	 *
	 * @since 9.0
	 */
	public static final Object JOB_FAMILY_CORE_BUILD_LAUNCH_BAR_TRACKER = new Object();
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

	/**
	 * Resets CoreBuildLaunchBarTracker. Used for testing only.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void reset() {
		lastMode = null;
		lastDescriptor = null;
		lastTarget = null;
	}

	/**
	 * @since 8.4
	 */
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
			// The project is not a Core Build project.
			return;
		}

		IProject finalProject = project;

		lastMode = mode;
		lastDescriptor = descriptor;
		lastTarget = target;

		/*
		 * Core build projects do not work with the concept of active build
		 * configurations, like managed build projects. Instead they rely
		 * on the launch mode and launch target set in the launchBar. A core
		 * build launch configuration looks at the launchBar launch mode
		 * and then the set of toolchains associated with the launch target
		 * to pick the build configuration. Core build projects still have an
		 * active build configuration, but it is hidden for the user and ignored for
		 * launching.
		 *
		 * Core build launch configurations have no options to set, so it is
		 * possible that users will use non core build launch configurations to
		 * launch a core build project binary. Non core build launch
		 * configurations typically launch the active build configuration. The
		 * active build configuration needs to match the launchBar launch mode.
		 */

		// Pick core build config based on launch mode and toolchain for target
		// Since this may create a new config, need to run it in a Job
		Job job = new Job(InternalDebugCoreMessages.CoreBuildLaunchBarTracker_Job) {

			@Override
			public boolean belongsTo(Object family) {
				return JOB_FAMILY_CORE_BUILD_LAUNCH_BAR_TRACKER == family;
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Map<String, String> properties = new HashMap<>();
					properties.putAll(lastTarget.getAttributes());
					Collection<IToolChain> tcs = toolChainManager.getToolChainsMatching(properties);
					ICBuildConfiguration buildConfig = null;
					if (!tcs.isEmpty()) {
						configs: for (IBuildConfiguration config : finalProject.getBuildConfigs()) {
							ICBuildConfiguration testConfig = configManager.getBuildConfiguration(config);
							if (testConfig != null && !(testConfig instanceof ErrorBuildConfiguration)) {
								// Match launch mode run/debug.
								if (testConfig.getLaunchMode().equals(lastMode.getIdentifier())) {
									// Match launch target
									if (testConfig.getLaunchTarget().equals(lastTarget)) {
										// Match toolchain.
										for (IToolChain tc : tcs) {
											if (testConfig.getToolChain().equals(tc)) {
												buildConfig = testConfig;
												break configs;
											}
										}
									}
								}
							}
						}

						if (buildConfig == null) {
							for (IToolChain toolChain : tcs) {
								buildConfig = configManager.getBuildConfiguration(finalProject, toolChain,
										mode.getIdentifier(), lastTarget, monitor);
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

						// Do we already have an error build config?
						for (IBuildConfiguration config : finalProject.getBuildConfigs()) {
							if (!config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
								ICBuildConfiguration testConfig = configManager.getBuildConfiguration(config);
								if (testConfig instanceof ErrorBuildConfiguration) {
									((ErrorBuildConfiguration) testConfig).setErrorMessage(error);
									buildConfig = testConfig;
									break;
								}
							}
						}

						// Nope, create one
						if (buildConfig == null) {
							IBuildConfiguration config = configManager.createBuildConfiguration(
									ErrorBuildConfiguration.PROVIDER, finalProject, ErrorBuildConfiguration.NAME,
									monitor);
							buildConfig = new ErrorBuildConfiguration(config, error);
							configManager.addBuildConfiguration(config, buildConfig);
						}
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
