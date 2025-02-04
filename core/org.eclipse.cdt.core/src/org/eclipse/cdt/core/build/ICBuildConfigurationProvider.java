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
package org.eclipse.cdt.core.build;

import java.util.Collection;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * A CBuildConfigurationProvider provides C build configurations.
 *
 * @since 6.0
 */
public interface ICBuildConfigurationProvider {

	/**
	 * Return the id of this provider
	 *
	 * @return provider id
	 */
	String getId();

	/**
	 * Returns the ICBuildConfiguration that owns this build configuration.
	 *
	 * @param config
	 * @return CDT build configuration for the Platform build configuration
	 */
	ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name) throws CoreException;

	/**
	 * Create a Core Build Configuration.
	 * @param project Project to associate this Core Build Configuration to.
	 * @param toolChain Toolchain to associate with this CMakeBuildConfiguration. Must not be null.
	 * @param launchMode Launch mode (eg "debug") to associate with this CMakeBuildConfiguration.
	 * @param launchTarget Launch target to associate with this CMakeBuildConfiguration. Must not be null.
	 * @param monitor
	 * @return
	 * @throws CoreException
	 *             if the Launch Target is null.
	 * @since 9.0
	 */
	ICBuildConfiguration createBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			ILaunchTarget launchTarget, IProgressMonitor monitor) throws CoreException;

	/**
	 * Return a collection of supported toolchains for build configurations of this
	 * type.
	 *
	 * @since 6.4
	 */
	default Collection<IToolChain> getSupportedToolchains(Collection<IToolChain> toolchains) throws CoreException {
		return toolchains;
	}

}
