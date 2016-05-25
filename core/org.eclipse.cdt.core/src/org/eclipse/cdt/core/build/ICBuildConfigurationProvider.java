/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.CoreException;

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

}
