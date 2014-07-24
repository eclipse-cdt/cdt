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

public class ConfigBasedLaunchConfigurationProvider extends AbstractLaunchConfigurationProvider implements
        ILaunchConfigurationProvider {
	private String typeId;

	public ConfigBasedLaunchConfigurationProvider(String launchConfigurationTypeId) {
		this.typeId = launchConfigurationTypeId;
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
	public String toString() {
		return "Provider for " + typeId;
	}
}
