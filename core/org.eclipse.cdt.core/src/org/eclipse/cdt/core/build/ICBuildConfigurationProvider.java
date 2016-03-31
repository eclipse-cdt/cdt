package org.eclipse.cdt.core.build;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;

/**
 * A CBuildConfigurationProvider provides C build configurations.
 * 
 * @since 6.0
 */
public interface ICBuildConfigurationProvider {

	/**
	 * Return the id of this provider
	 * @return provider id
	 */
	String getId();
	
	/**
	 * Returns the ICBuildConfiguration that owns this build configuration.
	 * 
	 * @param config
	 * @return CDT build configuration for the Platform build configuration
	 */
	ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config);

	/**
	 * Returns a default C build configuration for a given project if any.
	 * 
	 * @param project
	 * @return default C build configuration for the project 
	 */
	ICBuildConfiguration getDefaultCBuildConfiguration(IProject project);
	
}
