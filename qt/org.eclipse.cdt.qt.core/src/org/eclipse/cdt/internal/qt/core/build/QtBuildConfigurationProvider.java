package org.eclipse.cdt.internal.qt.core.build;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.QtNature;
import org.eclipse.cdt.qt.core.IQtBuildConfiguration;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

public class QtBuildConfigurationProvider implements ICBuildConfigurationProvider {

	public static final String ID = "org.eclipse.cdt.qt.core.qtBuildConfigProvider"; //$NON-NLS-1$

	private IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);
	private IQtInstallManager qtInstallManager = Activator.getService(IQtInstallManager.class);
	private ICBuildConfigurationManager configManager = Activator.getService(ICBuildConfigurationManager.class);

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config) {
		try {
			// Double check to make sure this config is ours
			if (!config.getName().startsWith(getId() + '/')) {
				return null;
			}

			if (!config.getProject().hasNature(QtNature.ID)) {
				return null;
			}

			return new QtBuildConfiguration(config);
		} catch (CoreException e) {
			Activator.log(e);
		}
		return null;
	}

	@Override
	public ICBuildConfiguration getDefaultCBuildConfiguration(IProject project) {
		try {
			if (!project.hasNature(QtNature.ID)) {
				return null;
			}

			// try the local target as the default
			ILaunchTargetManager targetManager = Activator.getService(ILaunchTargetManager.class);
			ILaunchTarget localTarget = targetManager
					.getLaunchTargetsOfType(ILaunchTargetManager.localLaunchTargetTypeId)[0];
			QtBuildConfiguration qtConfig = createConfiguration(project, localTarget, "run", //$NON-NLS-1$
					new NullProgressMonitor());
			if (qtConfig != null) {
				return qtConfig;
			}

			// local didn't work, try and find one that does
			for (ILaunchTarget target : targetManager.getLaunchTargets()) {
				if (!target.equals(localTarget)) {
					qtConfig = createConfiguration(project, target, "run", //$NON-NLS-1$
							new NullProgressMonitor());
					if (qtConfig != null) {
						return qtConfig;
					}
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
		return null;
	}

	public IQtBuildConfiguration getConfiguration(IProject project, ILaunchTarget target, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
			if (cconfig != null) {
				IQtBuildConfiguration qtConfig = cconfig.getAdapter(IQtBuildConfiguration.class);
				if (qtConfig != null && qtConfig.supports(target, launchMode)) {
					return qtConfig;
				}
			}
		}
		return null;
	}

	public QtBuildConfiguration createConfiguration(IProject project, ILaunchTarget target, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		for (IQtInstall qtInstall : qtInstallManager.getInstalls()) {
			if (qtInstallManager.supports(qtInstall, target)) {
				String configName = qtInstall.getSpec() + "." + launchMode; //$NON-NLS-1$
				IBuildConfiguration config = configManager.createBuildConfiguration(this, project, configName, monitor);

				// Find the toolchain
				for (IToolChain toolChain : toolChainManager.getToolChainsSupporting(target)) {
					if (qtInstallManager.supports(qtInstall, toolChain)) {
						QtBuildConfiguration qtConfig = new QtBuildConfiguration(config, toolChain, qtInstall,
								launchMode);
						return qtConfig;
						// TODO what if there's more than toolChain supported?
					}
				}
			}
		}
		return null;
	}

}
