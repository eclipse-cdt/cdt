package org.eclipse.cdt.core.build;

import org.eclipse.core.resources.IBuildConfiguration;

public interface ICBuildConfigurationProvider {

	/**
	 * Returns the ICBuildConfiguration that owns this build configuration.
	 * 
	 * @param config
	 * @return
	 */
	ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config);
	
}
