package org.eclipse.cdt.arduino.core.internal.remote;

import org.eclipse.launchbar.remote.core.RemoteLaunchTargetProvider;

public class ArduinoLaunchTargetProvider extends RemoteLaunchTargetProvider {

	@Override
	protected String getTypeId() {
		return ArduinoRemoteConnection.TYPE_ID;
	}

}
