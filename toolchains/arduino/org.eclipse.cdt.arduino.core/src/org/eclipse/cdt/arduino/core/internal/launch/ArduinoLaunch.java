package org.eclipse.cdt.arduino.core.internal.launch;

import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.remote.core.IRemoteConnection;

public class ArduinoLaunch extends Launch {

	private final ArduinoRemoteConnection target;
	private boolean wasOpen;

	public ArduinoLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator,
			IRemoteConnection target) {
		super(launchConfiguration, mode, locator);
		this.target = target.getService(ArduinoRemoteConnection.class);

		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	public void start() {
		this.wasOpen = target.getRemoteConnection().isOpen();
		if (wasOpen) {
			target.pause();
		}
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		super.handleDebugEvents(events);
		if (isTerminated() && wasOpen) {
			target.resume();
			wasOpen = false;
		}
	}

}
