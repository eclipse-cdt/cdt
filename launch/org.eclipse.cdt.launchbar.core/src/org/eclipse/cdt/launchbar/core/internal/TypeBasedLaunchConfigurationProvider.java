
/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems (Elena Laskavaia) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.DefaultLaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;

public class TypeBasedLaunchConfigurationProvider implements ILaunchConfigurationProvider {
	protected ILaunchBarManager manager;
	private String typeId;

	public TypeBasedLaunchConfigurationProvider(String launchConfigurationTypeId) {
		this.typeId = launchConfigurationTypeId;
	}

	@Override
	public void init(ILaunchBarManager manager) throws CoreException {
		this.manager = manager;
	}

	public boolean ownsConfiguration(ILaunchConfiguration element) {
		try {
			return element.getType().getIdentifier().equals(typeId);
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (ownsConfiguration(configuration)) {
			manager.launchObjectAdded(configuration);
			return true;
		}
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		if (ownsConfiguration(configuration)) {
			manager.launchObjectRemoved(configuration);
			return true;
		}
		return false;
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor) throws CoreException {
		if (descriptor instanceof DefaultLaunchDescriptor) {
			return ((DefaultLaunchDescriptor) descriptor).getConfig();
		}
		return null;
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor) throws CoreException {
		if (descriptor instanceof DefaultLaunchDescriptor) {
			return ((DefaultLaunchDescriptor) descriptor).getConfig().getType();
		}
		return null;
	}
}
