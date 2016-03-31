package org.eclipse.cdt.core.build;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.IAdaptable;

/**
 * This is the root interface for "new style" CDT build configurations. Adapting
 * IBuildConfiguration to this interface will get you one of these. From here,
 * adapt to the specific interface that you need and the configuration will
 * provide one.
 * 
 * @since 6.0
 */
public interface ICBuildConfiguration extends IAdaptable, IScannerInfoProvider {

	/**
	 * Returns the resources build configuration that this CDT build
	 * configuration is associated with.
	 * 
	 * @return resources build configuration
	 */
	IBuildConfiguration getBuildConfiguration();

	/**
	 * Build Configurations are configurations for a given toolchain.
	 *  
	 * @return the toolchain for this build configuration
	 */
	IToolChain getToolChain();

	IEnvironmentVariable getVariable(String name);
	
	IEnvironmentVariable[] getVariables();

}
