/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.launch;

import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.TargetedLaunch;
import org.eclipse.remote.core.IRemoteConnection;

public class ArduinoLaunch extends TargetedLaunch {

	private final ArduinoRemoteConnection remote;
	private boolean wasOpen;

	public ArduinoLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator,
			ILaunchTarget target) {
		super(launchConfiguration, mode, target, locator);
		IRemoteConnection connection = target.getAdapter(IRemoteConnection.class);
		this.remote = connection.getService(ArduinoRemoteConnection.class);

		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	public void start() {
		this.wasOpen = remote.getRemoteConnection().isOpen();
		if (wasOpen) {
			remote.pause();
		}
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		super.handleDebugEvents(events);
		if (isTerminated() && wasOpen) {
			remote.resume();
			wasOpen = false;
		}
	}

}
