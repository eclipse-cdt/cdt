/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.StandardBuildConfiguration;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @since 7.4
 */
public class MakefileBuildConfigurationProvider implements ICBuildConfigurationProvider {

	public static final String ID = "org.eclipse.cdt.make.core.provider"; //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name)
			throws CoreException {
		return new StandardBuildConfiguration(config, name);
	}

	@Override
	public ICBuildConfiguration createBuildConfiguration(IProject project, IToolChain toolChain,
			String launchMode, IProgressMonitor monitor) throws CoreException {
		ICBuildConfigurationManager configManager = MakeCorePlugin.getService(ICBuildConfigurationManager.class);

		StringBuilder configName = new StringBuilder("make."); //$NON-NLS-1$
		configName.append(launchMode);
		String os = toolChain.getProperty(IToolChain.ATTR_OS);
		if (os != null) {
			configName.append('.');
			configName.append(os);
		}
		String arch = toolChain.getProperty(IToolChain.ATTR_ARCH);
		if (arch != null && !arch.isEmpty()) {
			configName.append('.');
			configName.append(arch);
		}
		String name = configName.toString();
		int i = 0;
		while (configManager.hasConfiguration(this, project, name)) {
			name = configName.toString() + '.' + (++i);
		}

		IBuildConfiguration config = configManager.createBuildConfiguration(this, project, name, monitor);
		StandardBuildConfiguration makeConfig = new StandardBuildConfiguration(config, name, toolChain,
				launchMode);
		configManager.addBuildConfiguration(config, makeConfig);
		return makeConfig;
	}

}
