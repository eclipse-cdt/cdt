/*******************************************************************************
 * Copyright (c) 2014, 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A special launch descriptor that managed configurations that aren't owned by other
 * descriptors.
 *  
 * @since 2.3
 */
public class DefaultLaunchDescriptor extends PlatformObject implements ILaunchDescriptor {

	private final ILaunchDescriptorType type;
	private final ILaunchConfiguration configuration;

	public DefaultLaunchDescriptor(ILaunchDescriptorType type, ILaunchConfiguration configuration) {
		this.type = type;
		this.configuration = configuration;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (ILaunchConfiguration.class.equals(adapter)) {
			return adapter.cast(configuration);
		}

		T obj = configuration.getAdapter(adapter);
		if (obj != null) {
			return obj;
		}

		return super.getAdapter(adapter);
	}

	@Override
	public String getName() {
		return configuration.getName();
	}

	@Override
	public ILaunchDescriptorType getType() {
		return type;
	}

}
