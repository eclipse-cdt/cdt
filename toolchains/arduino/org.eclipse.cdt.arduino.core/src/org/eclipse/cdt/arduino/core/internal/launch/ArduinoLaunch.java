/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.launch;

import java.io.IOException;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.serial.SerialPort;
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
			SerialPort port = SerialPort.get(remote.getPortName());
			if (port != null) {
				try {
					port.pause();
				} catch (IOException e) {
					Activator.log(e);
				}
			}
		}
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		super.handleDebugEvents(events);
		if (isTerminated() && wasOpen) {
			SerialPort port = SerialPort.get(remote.getPortName());
			if (port != null) {
				try {
					port.resume();
				} catch (IOException e) {
					Activator.log(e);
				}
			}
			wasOpen = false;
		}
	}

}
