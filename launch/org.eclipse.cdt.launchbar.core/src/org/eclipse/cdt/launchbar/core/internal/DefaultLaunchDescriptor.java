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
package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorType;
import org.eclipse.debug.core.ILaunchConfiguration;

public class DefaultLaunchDescriptor implements ILaunchDescriptor {

	private final DefaultLaunchDescriptorType type;
	private final ILaunchConfiguration config;
	
	public DefaultLaunchDescriptor(DefaultLaunchDescriptorType type, ILaunchConfiguration config) {
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

}
