/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core.launch;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * Manages and persists the mapping between launch configurations and
 * remote connections that they run on. Each launch configuration has an
 * active remote connection.
 * 
 * @since 2.0
 */
public interface IRemoteLaunchConfigService {

	/**
	 * Sets the active remote connection for the given launch configuration.
	 * 
	 * @param launchConfig launch configuration
	 * @param connection active remote connection
	 */
	void setActiveConnection(ILaunchConfiguration launchConfig, IRemoteConnection connection);

	/**
	 * Gets the active remote connection for the given launch configuration
	 * @param launchConfig launch configuration
	 * @return active remote connection
	 */
	IRemoteConnection getActiveConnection(ILaunchConfiguration launchConfig);

	/**
	 * For a given launch configuration type, get the remote connection that was last
	 * used by a launch configuration of that type.
	 * 
	 * This is used for new launch configurations with the assumption that the user 
	 * will want to use the same remote connection.
	 * 
	 * @param launchConfigType launch configuration type
	 * @return last active remote configuration
	 */
	IRemoteConnection getLastActiveConnection(ILaunchConfigurationType launchConfigType);

}
