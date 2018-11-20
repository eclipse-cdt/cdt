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
package org.eclipse.cdt.internal.qt.core.build;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.qt.core.IQtBuildConfiguration;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
			if (config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
				// try the toolchain for the local target
				Map<String, String> properties = new HashMap<>();
				properties.put(IToolChain.ATTR_OS, Platform.getOS());
				properties.put(IToolChain.ATTR_ARCH, Platform.getOSArch());
				for (IToolChain toolChain : toolChainManager.getToolChainsMatching(properties)) {
					for (IQtInstall qtInstall : qtInstallManager.getInstalls()) {
						if (qtInstallManager.supports(qtInstall, toolChain)) {
							return new QtBuildConfiguration(config, name, toolChain, qtInstall, null);
						}
					}
				}

				// local didn't work, try and find one that does
				for (IToolChain toolChain : toolChainManager.getToolChainsMatching(new HashMap<>())) {
					for (IQtInstall qtInstall : qtInstallManager.getInstalls()) {
						if (qtInstallManager.supports(qtInstall, toolChain)) {
							return new QtBuildConfiguration(config, name, toolChain, qtInstall, null);
						}
					}
				}

				// No valid combinations
				return null;
			} else {
				return new QtBuildConfiguration(config, name);
			}
		} catch (CoreException e) {
			// Failed to create the build config. Return null so it gets
			// recreated.
			Activator.log(e);
			return null;
		}
	}

	@Override
	public ICBuildConfiguration createBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		IQtInstall qtInstall = getQtInstall(toolChain);
		if (qtInstall != null) {
			// See if one exists
			for (IBuildConfiguration config : project.getBuildConfigs()) {
				ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
				if (cconfig != null) {
					IQtBuildConfiguration qtConfig = cconfig.getAdapter(IQtBuildConfiguration.class);
					if (qtConfig != null && launchMode.equals(qtConfig.getLaunchMode())
							&& qtConfig.getToolChain().equals(toolChain)) {
						return qtConfig;
					}
				}
			}

			// TODO what if multiple matches, this returns first match
			String configName = "qt." + qtInstall.getSpec() + "." + launchMode; //$NON-NLS-1$ //$NON-NLS-2$
			IBuildConfiguration config = configManager.createBuildConfiguration(this, project, configName, monitor);
			QtBuildConfiguration qtConfig = new QtBuildConfiguration(config, configName, toolChain, qtInstall,
					launchMode);
			configManager.addBuildConfiguration(config, qtConfig);
			return qtConfig;
		} else {
			return null;
		}
	}

	private IQtInstall getQtInstall(IToolChain toolChain) {
		for (IQtInstall qtInstall : qtInstallManager.getInstalls()) {
			if (qtInstallManager.supports(qtInstall, toolChain)) {
				return qtInstall;
			}
		}

		return null;
	}

}
