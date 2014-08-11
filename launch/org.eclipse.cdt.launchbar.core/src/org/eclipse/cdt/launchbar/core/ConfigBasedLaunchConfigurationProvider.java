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

import java.util.HashMap;

import org.eclipse.cdt.launchbar.core.internal.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;

public class ConfigBasedLaunchConfigurationProvider extends AbstractLaunchConfigurationProvider implements
        ILaunchConfigurationProvider {
	protected HashMap<ILaunchConfiguration, Object> configMap = new HashMap<>();
	protected String typeId;

	public ConfigBasedLaunchConfigurationProvider(String launchConfigurationTypeId) {
		this.typeId = launchConfigurationTypeId;
	}

	public boolean ownsConfiguration(ILaunchConfiguration element) {
		try {
			// cannot use getType method when config is deleted, event sent after, no getters work on it
			if (configMap.containsKey(element))
				return true;
			return element.getType().getIdentifier().equals(typeId);
		} catch (DebugException e) {
			return false; // config does not exists, not point logging
		} catch (CoreException e) {
			Activator.log(e);
			return false;
		}
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (ownsConfiguration(configuration)) {
			rememberConfiguration(configuration);
			manager.launchObjectAdded(configuration);
			return true;
		}
		return false;
	}

	protected void rememberConfiguration(ILaunchConfiguration configuration) {
		configMap.put(configuration, null);
    }

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		if (ownsConfiguration(configuration)) {
			manager.launchObjectRemoved(configuration);
			forgetConfiguration(configuration);
			return true;
		}
		return false;
	}

	protected void forgetConfiguration(ILaunchConfiguration configuration) {
		configMap.remove(configuration);
    }

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor) throws CoreException {
		ILaunchConfigurationType type = super.getLaunchConfigurationType(descriptor);
		if (type!=null) return type;
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(typeId);
	}
	
	@Override
	public String toString() {
		return "provider for " + typeId;
	}
}
