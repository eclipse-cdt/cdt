/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.core.target.launch;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * A ITargetedLaunch implementation that simply extends the standard Launch
 * class to store the launch target.
 */
public class TargetedLaunch extends Launch implements ITargetedLaunch {

	private final ILaunchTarget launchTarget;

	public TargetedLaunch(ILaunchConfiguration launchConfiguration, String mode, ILaunchTarget launchTarget,
			ISourceLocator locator) {
		super(launchConfiguration, mode, locator);
		this.launchTarget = launchTarget;
	}

	@Override
	public ILaunchTarget getLaunchTarget() {
		return launchTarget;
	}

}
