package org.eclipse.cdt.arduino.core.internal.remote;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.TargetStatus;
import org.eclipse.launchbar.core.target.TargetStatus.Code;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;

public class ArduinoLaunchTargetProvider implements ILaunchTargetProvider {

	@Override
	public void init(ILaunchTargetManager targetManager) {
		IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType remoteType = remoteManager.getConnectionType(ArduinoRemoteConnection.TYPE_ID);

		// remove any targets that don't have connections
		for (ILaunchTarget target : targetManager.getLaunchTargetsOfType(ArduinoRemoteConnection.TYPE_ID)) {
			if (remoteType.getConnection(target.getName()) == null) {
				targetManager.removeLaunchTarget(target);
			}
		}

		// add any targets that are missing
		for (IRemoteConnection connection : remoteType.getConnections()) {
			if (targetManager.getLaunchTarget(ArduinoRemoteConnection.TYPE_ID, connection.getName()) == null) {
				targetManager.addLaunchTarget(ArduinoRemoteConnection.TYPE_ID, connection.getName());
			}
		}
	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		ArduinoRemoteConnection connection = target.getAdapter(ArduinoRemoteConnection.class);
		if (connection.getRemoteConnection().isOpen()) {
			return TargetStatus.OK_STATUS;
		} else {
			return new TargetStatus(Code.ERROR, "Not connected");
		}
	}

}
