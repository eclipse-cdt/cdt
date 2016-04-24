/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.build;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.core.runtime.Platform;

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
	public ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name) {
		try {
			// Double check to make sure this config is ours
			if (!config.getProject().hasNature(QtNature.ID)) {
				return null;
			}

			return new QtBuildConfiguration(config, name);
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
			Map<String, String> properties = new HashMap<>();
			properties.put(IToolChain.ATTR_OS, Platform.getOS());
			properties.put(IToolChain.ATTR_ARCH, Platform.getOSArch());
			for (IToolChain toolChain : toolChainManager.getToolChainsMatching(properties)) {
				IQtBuildConfiguration qtConfig = getConfiguration(project, toolChain, "run", new NullProgressMonitor()); //$NON-NLS-1$
				if (qtConfig == null) {
					qtConfig = createConfiguration(project, toolChain, "run", new NullProgressMonitor()); //$NON-NLS-1$
					if (qtConfig != null) {
						return qtConfig;
					}
				}
			}

			// local didn't work, try and find one that does
			for (IToolChain toolChain : toolChainManager.getToolChainsMatching(new HashMap<>())) {
				IQtBuildConfiguration qtConfig = getConfiguration(project, toolChain, "run", new NullProgressMonitor()); //$NON-NLS-1$
				if (qtConfig == null) {
					qtConfig = createConfiguration(project, toolChain, "run", new NullProgressMonitor()); //$NON-NLS-1$
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

	public IQtBuildConfiguration getConfiguration(IProject project, IToolChain toolChain, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
			if (cconfig != null) {
				IQtBuildConfiguration qtConfig = cconfig.getAdapter(IQtBuildConfiguration.class);
				if (qtConfig != null && qtConfig.getLaunchMode().equals(launchMode)
						&& qtConfig.getToolChain().equals(toolChain)) {
					return qtConfig;
				}
			}
		}
		return null;
	}

	public QtBuildConfiguration createConfiguration(IProject project, IToolChain toolChain, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		for (IQtInstall qtInstall : qtInstallManager.getInstalls()) {
			if (qtInstallManager.supports(qtInstall, toolChain)) {
				// TODO what if multiple matches
				String configName = "qt." + qtInstall.getSpec() + "." + launchMode; //$NON-NLS-1$ //$NON-NLS-2$
				IBuildConfiguration config = configManager.createBuildConfiguration(this, project, configName,
						monitor);
				QtBuildConfiguration qtConfig = new QtBuildConfiguration(config, configName, toolChain, qtInstall,
						launchMode);
				configManager.addBuildConfiguration(config, qtConfig);
				return qtConfig;
			}
		}

		return null;
	}

}
