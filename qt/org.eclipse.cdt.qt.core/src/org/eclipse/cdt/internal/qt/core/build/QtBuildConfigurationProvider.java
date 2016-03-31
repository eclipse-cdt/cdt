package org.eclipse.cdt.internal.qt.core.build;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.QtNature;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

public class QtBuildConfigurationProvider implements ICBuildConfigurationProvider {

	private static Map<IBuildConfiguration, QtBuildConfiguration> configs = new HashMap<>();

	@Override
	public ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config) {
		try {
			if (config.getProject().hasNature(QtNature.ID)) {
				if (!IBuildConfiguration.DEFAULT_CONFIG_NAME.equals(config.getName())) {
					return getConfiguration(config);
				} else {
					// try the local target as the default
					ILaunchTargetManager targetManager = Activator.getService(ILaunchTargetManager.class);
					ILaunchTarget localTarget = targetManager
							.getLaunchTargetsOfType(ILaunchTargetManager.localLaunchTargetTypeId)[0];
					QtBuildConfiguration qtConfig = getConfiguration(config.getProject(), localTarget, "run", //$NON-NLS-1$
							new NullProgressMonitor());
					if (qtConfig != null) {
						return qtConfig;
					}

					// local didn't work, try and find one that does
					for (ILaunchTarget target : targetManager.getLaunchTargets()) {
						if (!target.equals(localTarget)) {
							qtConfig = getConfiguration(config.getProject(), target, "run", new NullProgressMonitor()); //$NON-NLS-1$
							if (qtConfig != null) {
								return qtConfig;
							}
						}
					}

				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
		return null;
	}

	private static QtBuildConfiguration getConfiguration(IBuildConfiguration config) {
		QtBuildConfiguration qtConfig = configs.get(config);
		if (qtConfig == null) {
			qtConfig = new QtBuildConfiguration(config);
			configs.put(config, qtConfig);
		}
		return qtConfig;
	}

	public static QtBuildConfiguration getConfiguration(IProject project, ILaunchTarget target, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			QtBuildConfiguration qtConfig = getConfiguration(config);
			if (qtConfig.supports(target, launchMode)) {
				return qtConfig;
			}
		}

		// Not found, make a new one
		QtBuildConfiguration qtConfig = QtBuildConfiguration.createConfiguration(project, target, launchMode, monitor);
		if (qtConfig != null) {
			configs.put(qtConfig.getBuildConfiguration(), qtConfig);
		}
		return qtConfig;
	}
	
	private static IResourceChangeListener resourceListener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.PRE_CLOSE
					|| event.getType() == IResourceChangeEvent.PRE_DELETE) {
				if (event.getResource().getType() == IResource.PROJECT) {
					IProject project = event.getResource().getProject();
					if (project.isOpen() && project.exists() && QtNature.hasNature(project)) {
						try {
							for (IBuildConfiguration config : project.getBuildConfigs()) {
								configs.remove(config);
							}
						} catch (CoreException e) {
							Activator.log(e);
						}
					}
				}
			}
		}
	};

	public static void startup() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
	}
	
	public static void shutdown() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
	}

}
