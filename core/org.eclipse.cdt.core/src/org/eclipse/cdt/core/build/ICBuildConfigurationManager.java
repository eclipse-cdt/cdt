/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The OSGi service that manages the mapping from platform build configuration
 * to CDT build configuration.
 * 
 * @since 6.0
 */
public interface ICBuildConfigurationManager {

	/**
	 * Return the build configuration provider with the given id.
	 * 
	 * @param id
	 * @return build configuration provider
	 */
	ICBuildConfigurationProvider getProvider(String id);

	/**
	 * Create a new build configuration to be owned by a provider.
	 * 
	 * @param provider
	 * @param project
	 * @param configName
	 * @param monitor
	 * @return new build configuration
	 * @throws CoreException
	 */
	IBuildConfiguration createBuildConfiguration(ICBuildConfigurationProvider provider, IProject project,
			String configName, IProgressMonitor monitor) throws CoreException;

	/**
	 * Called by providers to add new build configurations as they are created.
	 * 
	 * @param buildConfig
	 *            platform build configuration
	 * @param cConfig
	 *            CDT build configuration
	 */
	void addBuildConfiguration(IBuildConfiguration buildConfig, ICBuildConfiguration cConfig);

	/**
	 * Return the CDT build configuration associated with the given Platform
	 * build configuration.
	 * 
	 * @param buildConfig
	 * @return the matching CDT build configuration
	 */
	ICBuildConfiguration getBuildConfiguration(IBuildConfiguration buildConfig) throws CoreException;
	
}
