package org.eclipse.remote.internal.core.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.launch.IRemoteLaunchConfigService;
import org.eclipse.remote.internal.core.RemoteCorePlugin;
import org.osgi.service.prefs.Preferences;

public class RemoteLaunchConfigService implements IRemoteLaunchConfigService {

	private static final String REMOTE_LAUNCH_CONFIG = "remoteLaunchConfig"; //$NON-NLS-1$
	private static final String REMOTE_LAUNCH_TYPE = "remoteLaunchType"; //$NON-NLS-1$

	private Preferences getPreferences(String node) {
		return InstanceScope.INSTANCE.getNode(RemoteCorePlugin.getUniqueIdentifier()).node(node);
	}
	
	private IRemoteConnection getRemoteConnection(String remoteId) {
		if (remoteId == null) {
			return null;
		}
		
		String[] ids = remoteId.split(":"); //$NON-NLS-1$
		if (ids.length < 2) {
			return null;
		}

		IRemoteServicesManager manager = RemoteCorePlugin.getService(IRemoteServicesManager.class);
		IRemoteConnectionType connectionType = manager.getConnectionType(ids[0]);
		if (connectionType == null) {
			return null;
		}
		
		return connectionType.getConnection(ids[1]);
	}

	@Override
	public void setActiveConnection(ILaunchConfiguration launchConfig, IRemoteConnection connection) {
		String remoteId = connection.getConnectionType().getId() + ":" + connection.getName(); //$NON-NLS-1$
		getPreferences(REMOTE_LAUNCH_CONFIG).put(launchConfig.getName(), remoteId);
		try {
			getPreferences(REMOTE_LAUNCH_TYPE).put(launchConfig.getType().getIdentifier(), remoteId);
		} catch (CoreException e) {
			RemoteCorePlugin.log(e.getStatus());
		}
	}
	
	@Override
	public IRemoteConnection getActiveConnection(ILaunchConfiguration launchConfig) {
		String remoteId = getPreferences(REMOTE_LAUNCH_CONFIG).get(launchConfig.getName(), null);
		return getRemoteConnection(remoteId);
	}

	@Override
	public IRemoteConnection getLastActiveConnection(ILaunchConfigurationType launchConfigType) {
		String remoteId = getPreferences(REMOTE_LAUNCH_TYPE).get(launchConfigType.getIdentifier(), null);
		return getRemoteConnection(remoteId);
	}

}
