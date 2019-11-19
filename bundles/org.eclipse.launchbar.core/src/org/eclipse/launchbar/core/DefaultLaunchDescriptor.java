/*******************************************************************************
 * Copyright (c) 2014, 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
