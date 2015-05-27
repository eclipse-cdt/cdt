package org.eclipse.launchbar.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;

public abstract class PerTargetLaunchConfigProvider extends AbstractLaunchConfigProvider {
	public final String ATTR_CONNECTION_TYPE = getConnectionTypeAttribute();
	public final String ATTR_CONNECTION_NAME = getConnectionNameAttribute();

	private final Map<ILaunchDescriptor, Map<IRemoteConnection, ILaunchConfiguration>> configMap = new HashMap<>();
	private final Collection<ILaunchConfiguration> ownedConfigs = new LinkedHashSet<>();

	protected String getConnectionNameAttribute() {
		return "org.eclipse.launchbar.core.connectionName";//$NON-NLS-1$
	}

	protected String getConnectionTypeAttribute() {
		return "org.eclipse.launchbar.core.connectionType";//$NON-NLS-1$
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target)
			throws CoreException {
		Map<IRemoteConnection, ILaunchConfiguration> targetMap = getTargetMap(descriptor);
		ILaunchConfiguration config = targetMap.get(target);
		if (config != null) {
			return config;
		}
		// first search for owned configurations, to see if any match to descriptor
		config = findLaunchConfiguration(descriptor, target);
		if (config == null) {
			config = createLaunchConfiguration(descriptor, target);
			launchConfigurationAdded(config);
		}
		targetMap.put(target, config);
		return config;
	}

	protected Map<IRemoteConnection, ILaunchConfiguration> getTargetMap(ILaunchDescriptor descriptor) {
		Map<IRemoteConnection, ILaunchConfiguration> targetMap = configMap.get(descriptor);
		if (targetMap == null) {
			targetMap = new HashMap<>();
			configMap.put(descriptor, targetMap);
		}
		return targetMap;
	}

	protected ILaunchConfiguration findLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target)
			throws CoreException {
		for (ILaunchConfiguration configuration : ownedConfigs) {
			if (descriptorAndTargetMatchesConfiguration(descriptor, target, configuration)) {
				return configuration;
			}
		}
		return null;
	}

	protected boolean descriptorAndTargetMatchesConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target,
			ILaunchConfiguration configuration) {
		if (targetMatchesConfiguration(target, configuration) == false)
			return false;
		if (descriptorMatchesConfiguration(descriptor, configuration) == false)
			return false;
		return true;
	}

	/**
	 * This method should be overridden to check that configuration does actually represent the descriptor.
	 * You don't need to check ownership since this method will be only called on owned configurations
	 */
	protected boolean descriptorMatchesConfiguration(ILaunchDescriptor descriptor, ILaunchConfiguration configuration) {
		// we using startsWith instead of equals because new configuration using "generateLaunchConfigurationName" method which
		// means only prefix guaranteed to be matching, and the prefix is the descriptor name
		return configuration.getName().startsWith(descriptor.getName());
	}

	protected boolean targetMatchesConfiguration(IRemoteConnection target, ILaunchConfiguration configuration) {
		String targetName;
		try {
			targetName = configuration.getAttribute(ATTR_CONNECTION_NAME, "");
		} catch (CoreException e) {
			return false;
		}
		if (target != null && target.getName().equals(targetName)) {
			return true;
		} else if (target == null && (targetName == null || targetName.isEmpty())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		super.populateLaunchConfiguration(descriptor, target, workingCopy);
		workingCopy.setAttribute(ATTR_CONNECTION_TYPE, target.getConnectionType().getId());
		workingCopy.setAttribute(ATTR_CONNECTION_NAME, target.getName());
	}

	public IRemoteConnection getTarget(ILaunchConfiguration configuration) throws CoreException {
		IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
		String connectionTypeId = configuration.getAttribute(ATTR_CONNECTION_TYPE, ""); //$NON-NLS-1$
		if (connectionTypeId.isEmpty()) {
			return null;
		}
		IRemoteConnectionType connectionType = remoteManager.getConnectionType(connectionTypeId);
		if (connectionType == null) {
			return null;
		}
		String connectionName = configuration.getAttribute(ATTR_CONNECTION_NAME, ""); //$NON-NLS-1$
		if (connectionName.isEmpty()) {
			return null;
		}
		return connectionType.getConnection(connectionName);
	}

	@Override
	public boolean ownsLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		return ownedConfigs.contains(configuration);
	}

	public boolean ownsLaunchConfigurationByAttributes(ILaunchConfiguration configuration) {
		try {
			return super.ownsLaunchConfiguration(configuration);
		} catch (CoreException e) {
			// will happened if called after LC is deleted
			return false;
		}
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		boolean owned = ownsLaunchConfiguration(configuration);
		if (owned) {
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
		}
		return owned;
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfigurationByAttributes(configuration)) {
			ownedConfigs.add(configuration);
			return true;
		}
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfigurationByAttributes(configuration)) {
			// clear cache, target could have changed
			launchConfigurationRemoved(configuration);
			ownedConfigs.add(configuration);
			return true;
		} else if (ownedConfigs.contains(configuration)) {
			// user did something that will cause us to loose ownership of this configuration
			launchConfigurationRemoved(configuration);
		}
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		Map<IRemoteConnection, ILaunchConfiguration> map = configMap.remove(descriptor);
		if (map == null)
			return;
		for (ILaunchConfiguration config : map.values()) {
			ownedConfigs.remove(config);
			config.delete(); // remove all auto-configs associated with descriptor
		}
	}

	@Override
	public void launchTargetRemoved(IRemoteConnection target) throws CoreException {
		for (Iterator<Entry<ILaunchDescriptor, Map<IRemoteConnection, ILaunchConfiguration>>> iterator = configMap.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<ILaunchDescriptor, Map<IRemoteConnection, ILaunchConfiguration>> descEntry = iterator.next();
			Map<IRemoteConnection, ILaunchConfiguration> map = descEntry.getValue();
			ILaunchConfiguration config = map.remove(target);
			if (config != null) {
				config.delete(); // remove all auto-configs associated with target
			}
			if (map.isEmpty()) {
				iterator.remove();
			}
		}
	}
}
