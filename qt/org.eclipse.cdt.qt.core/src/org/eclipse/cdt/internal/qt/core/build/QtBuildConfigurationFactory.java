/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.build;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.cdt.build.core.IToolChainManager;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.QtNature;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.cdt.qt.core.QtBuildConfiguration;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

public class QtBuildConfigurationFactory implements IAdapterFactory {

	private static IQtInstallManager qtInstallManager = Activator.getService(IQtInstallManager.class);
	private static IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);

	private static Map<IBuildConfiguration, QtBuildConfiguration> cache = new HashMap<>();

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { QtBuildConfiguration.class };
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType.equals(QtBuildConfiguration.class) && adaptableObject instanceof IBuildConfiguration) {
			IBuildConfiguration config = (IBuildConfiguration) adaptableObject;
			synchronized (cache) {
				QtBuildConfiguration qtConfig = cache.get(config);
				if (qtConfig == null) {
					if (!config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
						qtConfig = new QtBuildConfiguration(config);
						cache.put(config, qtConfig);
						return (T) qtConfig;
					} else {
						// Default to local
						ILaunchTargetManager targetManager = Activator.getService(ILaunchTargetManager.class);
						ILaunchTarget localTarget = targetManager
								.getLaunchTargetsOfType(ILaunchTargetManager.localLaunchTargetTypeId)[0];
						qtConfig = createDefaultConfig(config, localTarget);
						if (qtConfig != null) {
							cache.put(config, qtConfig);
							return (T) qtConfig;
						}

						// Just find a combination that works
						for (ILaunchTarget target : targetManager.getLaunchTargets()) {
							if (!target.equals(localTarget)) {
								qtConfig = createDefaultConfig(config, localTarget);
								if (qtConfig != null) {
									cache.put(config, qtConfig);
									return (T) qtConfig;
								}
							}
						}

						// TODO if we don't have a target, need another way to
						// match whatever qtInstalls we have with matching
						// toolchains
					}
				}
				return (T) qtConfig;
			}
		}
		return null;
	}

	private static QtBuildConfiguration createDefaultConfig(IBuildConfiguration config, ILaunchTarget target) {
		for (IQtInstall qtInstall : qtInstallManager.getInstalls()) {
			if (qtInstallManager.supports(qtInstall, target)) {
				// Find the toolchain
				for (IToolChain toolChain : toolChainManager.getToolChainsSupporting(target)) {
					if (qtInstallManager.supports(qtInstall, toolChain)) {
						return new QtBuildConfiguration(config, toolChain, qtInstall, "run"); //$NON-NLS-1$
					}
				}
			}
		}

		return null;
	}

	public static QtBuildConfiguration getConfig(IProject project, String launchMode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		// return it if it exists already
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			QtBuildConfiguration qtConfig = config.getAdapter(QtBuildConfiguration.class);
			if (qtConfig != null) {
				IQtInstall qtInstall = qtConfig.getQtInstall();
				if (qtInstall != null && qtInstallManager.supports(qtInstall, target)
						&& launchMode.equals(qtConfig.getLaunchMode())) {
					return qtConfig;
				}
			}
		}

		// Nope, create it
		for (IQtInstall qtInstall : qtInstallManager.getInstalls()) {
			if (qtInstallManager.supports(qtInstall, target)) {
				// Create the build config
				Set<String> configNames = new HashSet<>();
				for (IBuildConfiguration config : project.getBuildConfigs()) {
					configNames.add(config.getName());
				}
				String baseName = qtInstall.getSpec() + "." + launchMode; //$NON-NLS-1$
				String newName = baseName;
				int n = 0;
				while (configNames.contains(newName)) {
					newName = baseName + (++n);
				}
				configNames.add(newName);
				IProjectDescription projectDesc = project.getDescription();
				projectDesc.setBuildConfigs(configNames.toArray(new String[configNames.size()]));
				project.setDescription(projectDesc, monitor);

				// Find the toolchain
				IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);
				for (IToolChain toolChain : toolChainManager.getToolChainsSupporting(target)) {
					if (qtInstallManager.supports(qtInstall, toolChain)) {
						QtBuildConfiguration qtConfig = new QtBuildConfiguration(project.getBuildConfig(newName),
								toolChain, qtInstall, launchMode);
						return qtConfig;
						// TODO what if there's more than toolChain supported?
					}
				}
			}
		}
		return null;
	}

	public static class Cleanup implements IResourceChangeListener {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.PRE_CLOSE
					|| event.getType() == IResourceChangeEvent.PRE_DELETE) {
				if (event.getResource().getType() == IResource.PROJECT) {
					IProject project = event.getResource().getProject();
					if (QtNature.hasNature(project)) {
						try {
							for (IBuildConfiguration config : project.getBuildConfigs()) {
								cache.remove(config);
							}
						} catch (CoreException e) {
							Activator.log(e);
						}
					}
				}
			}
		}
	}

}