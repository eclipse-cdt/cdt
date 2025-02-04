/*******************************************************************************
 * Copyright (c) 2025 Renesas Electronics Europe.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @since 9.0
 */
public class CBuildConfigUtils {

	private CBuildConfigUtils() {
		// empty
	}

	/**
	 * @param provider Core Build configuration provider. Must not be null.
	 * @param project Project in which the Platform Build configuration is created. Must not be null.
	 * @param cBuildConfigName  The Core Build config name to be used as part of the the Platform Build configuration
	 * name. See {@link ICBuildConfigurationProvider#getCBuildConfigName}. Must not be null.
	 * @param cBuildConfigManager Core Build configuration manager. Must not be null.
	 * @param monitor
	 * @return a Platform Build Configuration.
	 * @throws CoreException
	 */
	public static IBuildConfiguration createBuildConfiguration(ICBuildConfigurationProvider provider, IProject project,
			String cBuildConfigName, ICBuildConfigurationManager cBuildConfigManager, IProgressMonitor monitor)
			throws CoreException {
		IBuildConfiguration retVal = null;
		// Try to reuse any IBuildConfiguration with the same name for the project
		if (cBuildConfigManager.hasConfiguration(provider, project, cBuildConfigName)) {
			retVal = project.getBuildConfig(provider.getId() + '/' + cBuildConfigName);
		}
		if (retVal == null) {
			retVal = cBuildConfigManager.createBuildConfiguration(provider, project, cBuildConfigName, monitor);
		}
		return retVal;
	}
}
