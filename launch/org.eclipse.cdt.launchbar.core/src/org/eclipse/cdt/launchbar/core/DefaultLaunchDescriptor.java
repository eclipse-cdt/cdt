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

public class DefaultLaunchDescriptor implements ILaunchDescriptor {
	private final ILaunchDescriptorType id;
	private final ILaunchConfiguration config;
	
	public DefaultLaunchDescriptor(ILaunchDescriptorType type, ILaunchConfiguration config) {
		this.id = type;
		this.config = config;
	}

	@Override
	public String getName() {
		return config.getName();
	}

	@Override
	public ILaunchDescriptorType getType() {
		return id;
	}

	public ILaunchConfiguration getConfig() {
		return config;
	}

	public String getId() {
		return config.getName() + "." + id.getId();
	}

	@Override
	public int hashCode() {
		return 17 + getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DefaultLaunchDescriptor))
			return false;
		DefaultLaunchDescriptor other = (DefaultLaunchDescriptor) obj;
		if (!getId().equals(other.getId()))
			return false;
		return true;
	}
}
