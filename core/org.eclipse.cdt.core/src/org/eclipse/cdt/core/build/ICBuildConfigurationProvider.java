package org.eclipse.cdt.core.build;

import org.eclipse.core.resources.IBuildConfiguration;

/**
 * @since 6.0
 */
public interface ICBuildConfigurationProvider {

	String getId();
	
	/**
	 * Returns the ICBuildConfiguration that owns this build configuration.
	 * 
	 * @param config
	 * @return CDT build configuration for the Platform build configuration
	 */
	ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config);
	
}
