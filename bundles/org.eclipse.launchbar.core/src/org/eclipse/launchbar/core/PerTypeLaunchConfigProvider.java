/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.launchbar.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * Common launch configuration provider for cases where it provides for a single
 * connection type and a single launch configuration type.
 */
public abstract class PerTypeLaunchConfigProvider extends AbstractLaunchConfigProvider {

	// Map from launch object to launch configuration
	private Map<Object, ILaunchConfiguration> configMap = new HashMap<>();

	protected abstract String getRemoteConnectionTypeId();

	protected abstract String getLaunchConfigurationTypeId();

	protected abstract Object getLaunchObject(ILaunchDescriptor descriptor);
	
	protected abstract Object getLaunchObject(ILaunchConfiguration configuration) throws CoreException;

	protected ILaunchConfigurationType getLaunchConfigurationType() {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(getLaunchConfigurationTypeId());
	}

	@Override
	public boolean supports(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		// If target is null, assume we support it.
		return target == null || target.getConnectionType().getId().equals(getRemoteConnectionTypeId());
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		if (supports(descriptor, target)) {
			return getLaunchConfigurationType();
		}
		return null;
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		if (supports(descriptor, target)) {
			Object launchObject = getLaunchObject(descriptor);
			ILaunchConfiguration config = configMap.get(launchObject);
			if (config == null) {
				config = createLaunchConfiguration(descriptor, target);
				configMap.put(launchObject, config);
			}
			return config;
		} else { 
			return null;
		}
	}

	@Override
	public boolean ownsLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		if (!super.ownsLaunchConfiguration(configuration)) {
			return false;
		}
		
		// Must be of our type
		return configuration.getType().equals(getLaunchConfigurationType());
	}

	@Override
	public Object launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfiguration(configuration)) {
			Object launchObject = getLaunchObject(configuration);
			configMap.put(launchObject, configuration);
			return launchObject;
		}
		return null;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfiguration(configuration)) {
			Object launchObject = getLaunchObject(configuration);
			configMap.remove(launchObject);
			return true;
		}
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		Object launchObject = getLaunchObject(descriptor);
		if (launchObject != null) {
			configMap.remove(launchObject);
		}
	}

	@Override
	public void launchTargetRemoved(IRemoteConnection target) throws CoreException {
		// nothing to do per target
	}

}
