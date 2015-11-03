package org.eclipse.cdt.arduino.core.internal.remote;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;

public class ArduinoTargetAdapterFactory implements IAdapterFactory {

	private IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof ILaunchTarget) {
			ILaunchTarget target = (ILaunchTarget) adaptableObject;
			if (target.getTypeId().equals(ArduinoRemoteConnection.TYPE_ID)) {
				IRemoteConnectionType connectionType = remoteManager.getConnectionType(target.getTypeId());
				IRemoteConnection connection = connectionType.getConnection(target.getName());
				if (connection != null) {
					return (T) connection.getService(ArduinoRemoteConnection.class);
				}
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { ArduinoRemoteConnection.class };
	}

}
