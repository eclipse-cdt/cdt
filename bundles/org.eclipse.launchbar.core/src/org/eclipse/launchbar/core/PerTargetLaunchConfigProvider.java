package org.eclipse.launchbar.core;

import java.util.HashMap;
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

	public static final String ATTR_CONNECTION_TYPE = "connectionType"; //$NON-NLS-1$
	public static final String ATTR_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$

	private final Map<ILaunchDescriptor, Map<IRemoteConnection, ILaunchConfiguration>> configMap = new HashMap<>();

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target)
			throws CoreException {
		Map<IRemoteConnection, ILaunchConfiguration> targetMap = configMap.get(descriptor);
		if (targetMap != null) {
			ILaunchConfiguration config = targetMap.get(target);
			if (config != null) {
				return config;
			}
		}

		ILaunchConfiguration config = createLaunchConfiguration(descriptor, target);
		if (targetMap == null) {
			targetMap = new HashMap<>();
			configMap.put(descriptor, targetMap);
		}
		targetMap.put(target, config);
		return config;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		super.populateLaunchConfiguration(descriptor, target, workingCopy);
		workingCopy.setAttribute(ATTR_CONNECTION_TYPE, target.getConnectionType().getId());
		workingCopy.setAttribute(ATTR_CONNECTION_NAME, target.getName());
	}

	public static IRemoteConnection getTarget(ILaunchConfiguration configuration) throws CoreException {
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
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
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
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		// TODO re-create map
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		configMap.remove(descriptor);
	}

	@Override
	public void launchTargetRemoved(IRemoteConnection target) throws CoreException {
		for (Entry<ILaunchDescriptor, Map<IRemoteConnection, ILaunchConfiguration>> descEntry : configMap.entrySet()) {
			descEntry.getValue().remove(target);
			if (descEntry.getValue().isEmpty()) {
				configMap.remove(descEntry.getKey());
			}
		}
	}

}
