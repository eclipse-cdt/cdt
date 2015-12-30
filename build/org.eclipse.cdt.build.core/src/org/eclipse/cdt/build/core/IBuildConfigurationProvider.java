/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.core;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * A provider for build configurations.
 */
public interface IBuildConfigurationProvider {

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

	/**
	 * Load a previously created build configuration.
	 * 
	 * @param buildConfig
	 * @return
	 */
	CBuildConfiguration loadBuildConfiguration(IBuildConfiguration buildConfig);
}
