/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (QNX) - initial
 *******************************************************************************/
package org.eclipse.remote.core.api2;

import org.eclipse.debug.core.ILaunchConfigurationType;

/**
 * Service that supports setting up launch configurations for
 * launching over the remote connection.
 */
public interface IRemoteLaunchConfigService extends IRemoteService {

	/**
	 * Does this remote service support launching on this launch config type.
	 * 
	 * @param launchConfigType
	 * @return boolean supports launching on this connection
	 */
	boolean supportsType(ILaunchConfigurationType launchConfigType);

}
