package org.eclipse.cdt.build.core;

import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * The manager which managed build configurations.
 * 
 * @noimplement
 */
public interface IBuildConfigurationManager {

	/**
	 * Returns a build configuration that knows how to build the thing described
	 * by the launch descriptor for the given mode running on the given target.
	 * 
	 * @param descriptor
	 * @param mode
	 * @param target
	 * @return
	 */
	CBuildConfiguration getBuildConfiguration(ILaunchDescriptor descriptor, String mode, ILaunchTarget target);

}
