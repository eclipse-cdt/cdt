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
	 * Return the CDT build configuration associated with the given Platform
	 * build configuration.
	 * 
	 * @param buildConfig
	 * @return the matching CDT build configuration
	 */
	ICBuildConfiguration getBuildConfiguration(IBuildConfiguration buildConfig);

}
