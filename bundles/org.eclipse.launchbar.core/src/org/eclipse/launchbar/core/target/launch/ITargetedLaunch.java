/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.core.target.launch;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * A launch that knows what target it's running on.
 */
public interface ITargetedLaunch extends ILaunch {

	/**
	 * The target this launch will or is running on.
	 *
	 * @return launch target
	 */
	ILaunchTarget getLaunchTarget();

}
