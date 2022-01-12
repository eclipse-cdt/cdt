/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public abstract class PerTargetLaunchConfigProvider extends AbstractLaunchConfigProvider {
	private final Map<ILaunchDescriptor, Map<ILaunchTarget, ILaunchConfiguration>> configMap = new HashMap<>();
	private final Map<ILaunchDescriptor, ILaunchConfiguration> defaultConfigs = new HashMap<>();
	private final Collection<ILaunchConfiguration> ownedConfigs = new LinkedHashSet<>();

	protected ILaunchBarManager getManager() {
		return Activator.getService(ILaunchBarManager.class);
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		if (target != null) {
			Map<ILaunchTarget, ILaunchConfiguration> targetMap = configMap.get(descriptor);
			if (targetMap != null) {
				ILaunchConfiguration config = targetMap.get(target);
				if (config != null) {
					return config;
				}
			}
		} else {
			ILaunchConfiguration config = defaultConfigs.get(descriptor);
			if (config != null) {
				return config;
			}
		}

		// The config will get added to the cache when launchConfigurationAdded
		// is called when the new config is saved.
		return createLaunchConfiguration(descriptor, target);
	}

	protected abstract ILaunchDescriptor getLaunchDescriptor(ILaunchConfiguration configuration) throws CoreException;

	protected abstract ILaunchTarget getLaunchTarget(ILaunchConfiguration configuration) throws CoreException;

	protected boolean providesForNullTarget() {
		return false;
	}

	private boolean addLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		ILaunchDescriptor desc = getLaunchDescriptor(configuration);
		if (desc == null) {
			return false;
		}

		ILaunchTarget target = getLaunchTarget(configuration);
		if (target == null) {
			if (providesForNullTarget()) {
				defaultConfigs.put(desc, configuration);
			} else {
				return false;
			}
		} else {
			Map<ILaunchTarget, ILaunchConfiguration> targetMap = configMap.get(desc);
			if (targetMap == null) {
				targetMap = new HashMap<>();
				configMap.put(desc, targetMap);
			}
			targetMap.put(target, configuration);
		}

		ownedConfigs.add(configuration);
		return true;
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfiguration(configuration)) {
			return addLaunchConfiguration(configuration);
		}
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfiguration(configuration)) {
			// clear cache, target could have changed
			launchConfigurationRemoved(configuration);
			return addLaunchConfiguration(configuration);
		} else if (ownedConfigs.contains(configuration)) {
			// something changed that will cause us to loose ownership of this
			// configuration. Remove and add it back in.
			ILaunchBarManager manager = getManager();
			manager.launchConfigurationRemoved(configuration);
			manager.launchConfigurationAdded(configuration);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		ownedConfigs.remove(configuration);
		for (Entry<ILaunchDescriptor, Map<ILaunchTarget, ILaunchConfiguration>> descEntry : configMap.entrySet()) {
			for (Entry<ILaunchTarget, ILaunchConfiguration> targetEntry : descEntry.getValue().entrySet()) {
				if (targetEntry.getValue().equals(configuration)) {
					descEntry.getValue().remove(targetEntry.getKey());
					if (descEntry.getValue().isEmpty()) {
						configMap.remove(descEntry.getKey());
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		Map<ILaunchTarget, ILaunchConfiguration> map = configMap.remove(descriptor);
		if (map != null) {
			for (ILaunchConfiguration config : map.values()) {
				ownedConfigs.remove(config);
				// remove all auto-configs associated with descriptor
				config.delete();
			}
		}

		ILaunchConfiguration config = defaultConfigs.remove(descriptor);
		if (config != null) {
			ownedConfigs.remove(config);
			config.delete();
		}
	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) throws CoreException {
		for (Iterator<Entry<ILaunchDescriptor, Map<ILaunchTarget, ILaunchConfiguration>>> iterator = configMap
				.entrySet().iterator(); iterator.hasNext();) {
			Entry<ILaunchDescriptor, Map<ILaunchTarget, ILaunchConfiguration>> descEntry = iterator.next();
			Map<ILaunchTarget, ILaunchConfiguration> map = descEntry.getValue();
			ILaunchConfiguration config = map.remove(target);
			if (config != null) {
				// remove all auto-configs associated with target
				config.delete();
			}
			if (map.isEmpty()) {
				iterator.remove();
			}
		}
	}
}
