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
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A special launch descriptor that managed configurations that aren't owned by other
 * descriptors. 
 */
public class DefaultLaunchDescriptor extends PlatformObject implements ILaunchDescriptor {

	private final DefaultLaunchDescriptorType type;
	private final ILaunchConfiguration configuration;

	public DefaultLaunchDescriptor(DefaultLaunchDescriptorType type, ILaunchConfiguration configuration) {
		this.type = type;
		this.configuration = configuration;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (ILaunchConfiguration.class.equals(adapter)) {
			return configuration;
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
