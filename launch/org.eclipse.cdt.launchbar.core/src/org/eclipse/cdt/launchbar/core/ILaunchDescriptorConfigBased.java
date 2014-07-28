/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Interface for descriptors which are based on launch configurations
 */
public interface ILaunchDescriptorConfigBased extends ILaunchDescriptor {
	/**
	 * Return launch configuration on which it is based
	 */
	public ILaunchConfiguration getConfig();
}
