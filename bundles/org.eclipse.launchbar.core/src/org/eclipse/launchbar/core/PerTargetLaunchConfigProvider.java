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
import org.eclipse.remote.core.IRemoteConnection;

public abstract class PerTargetLaunchConfigProvider extends AbstractLaunchConfigProvider {
	private final Map<ILaunchDescriptor, Map<IRemoteConnection, ILaunchConfiguration>> configMap = new HashMap<>();
	private final Map<ILaunchDescriptor, ILaunchConfiguration> defaultConfigs = new HashMap<>();
	private final Collection<ILaunchConfiguration> ownedConfigs = new LinkedHashSet<>();

	protected ILaunchBarManager getManager() {
		return Activator.getService(ILaunchBarManager.class);
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target)
			throws CoreException {
		if (target != null) {
			Map<IRemoteConnection, ILaunchConfiguration> targetMap = configMap.get(descriptor);
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

	protected abstract IRemoteConnection getLaunchTarget(ILaunchConfiguration configuration) throws CoreException;

	protected boolean providesForNullTarget() {
		return false;
	}

	private boolean addLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		ILaunchDescriptor desc = getLaunchDescriptor(configuration);
		if (desc == null) {
			return false;
		}

		IRemoteConnection target = getLaunchTarget(configuration);
		if (target == null) {
			if (providesForNullTarget()) {
				defaultConfigs.put(desc, configuration);
			} else {
				return false;
			}
		} else {
			Map<IRemoteConnection, ILaunchConfiguration> targetMap = configMap.get(desc);
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
		for (Entry<ILaunchDescriptor, Map<IRemoteConnection, ILaunchConfiguration>> descEntry : configMap.entrySet()) {
			for (Entry<IRemoteConnection, ILaunchConfiguration> targetEntry : descEntry.getValue().entrySet()) {
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
		Map<IRemoteConnection, ILaunchConfiguration> map = configMap.remove(descriptor);
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
	public void launchTargetRemoved(IRemoteConnection target) throws CoreException {
		for (Iterator<Entry<ILaunchDescriptor, Map<IRemoteConnection, ILaunchConfiguration>>> iterator = configMap
				.entrySet().iterator(); iterator.hasNext();) {
			Entry<ILaunchDescriptor, Map<IRemoteConnection, ILaunchConfiguration>> descEntry = iterator.next();
			Map<IRemoteConnection, ILaunchConfiguration> map = descEntry.getValue();
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
