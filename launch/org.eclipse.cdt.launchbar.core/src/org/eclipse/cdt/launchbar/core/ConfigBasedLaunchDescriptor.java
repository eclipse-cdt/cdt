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

public class ConfigBasedLaunchDescriptor extends AbstractLaunchDescriptor implements ILaunchDescriptorConfigBased {
	private final ILaunchDescriptorType type;
	private final ILaunchConfiguration config;

	public ConfigBasedLaunchDescriptor(ILaunchDescriptorType type, ILaunchConfiguration config) {
		this.type = type;
		this.config = config;
	}

	@Override
	public String getName() {
		return config.getName();
	}

	@Override
	public ILaunchDescriptorType getType() {
		return type;
	}

	public ILaunchConfiguration getConfig() {
		return config;
	}

	@Override
	public String toString() {
		return getId();
	}
}
