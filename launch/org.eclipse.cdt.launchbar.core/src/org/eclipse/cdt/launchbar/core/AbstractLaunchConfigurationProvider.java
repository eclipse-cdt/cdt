/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Elena Laskavaia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;

/**
 * Abstract provider can work with any ITypeBaseLaunchDescriptor to provide launch configurations
 */
public abstract class AbstractLaunchConfigurationProvider implements ILaunchConfigurationProvider {
	protected ILaunchBarManager manager;

	@Override
	public void init(ILaunchBarManager manager) throws CoreException {
		this.manager = manager;
	}

	public ILaunchBarManager getManager() {
		return manager;
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor) throws CoreException {
		if (descriptor instanceof ILaunchDescriptorConfigBased) {
			return ((ILaunchDescriptorConfigBased) descriptor).getConfig();
		}
		return null;
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor) throws CoreException {
		ILaunchConfiguration config = getLaunchConfiguration(descriptor);
		if (config != null) {
			return config.getType();
		}
		return null;
	}
}
