/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
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
