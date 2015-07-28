/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.IArduinoRemoteConnection;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionPropertyService;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.serial.core.SerialPortCommandShell;

public class ArduinoRemoteConnection implements IRemoteConnectionPropertyService, IRemoteCommandShellService,
		IArduinoRemoteConnection, IRemoteConnectionChangeListener {

	private final IRemoteConnection remoteConnection;
	private SerialPort serialPort;
	private SerialPortCommandShell commandShell;

	private static final Map<IRemoteConnection, ArduinoRemoteConnection> connectionMap = new HashMap<>();

	public ArduinoRemoteConnection(IRemoteConnection remoteConnection) {
		this.remoteConnection = remoteConnection;
		remoteConnection.addConnectionChangeListener(this);
	}

	@Override
	public void connectionChanged(RemoteConnectionChangeEvent event) {
		if (event.getType() == RemoteConnectionChangeEvent.CONNECTION_REMOVED) {
			synchronized (connectionMap) {
				connectionMap.remove(event.getConnection());
			}
		}
	}

	public static class Factory implements IRemoteConnection.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends IRemoteConnection.Service> T getService(IRemoteConnection remoteConnection,
				Class<T> service) {
			if (IArduinoRemoteConnection.class.equals(service)) {
				synchronized (connectionMap) {
					ArduinoRemoteConnection connection = connectionMap.get(remoteConnection);
					if (connection == null) {
						connection = new ArduinoRemoteConnection(remoteConnection);
						connectionMap.put(remoteConnection, connection);
					}
					return (T) connection;
				}
			} else if (IRemoteConnectionPropertyService.class.equals(service)
					|| IRemoteCommandShellService.class.equals(service)) {
				return (T) remoteConnection.getService(IArduinoRemoteConnection.class);
			}
			return null;
		}
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	@Override
	public String getProperty(String key) {
		if (IRemoteConnection.OS_NAME_PROPERTY.equals(key)) {
			return "arduino"; //$NON-NLS-1$
		} else if (IRemoteConnection.OS_ARCH_PROPERTY.equals(key)) {
			return "avr"; // TODO handle arm //$NON-NLS-1$
		} else {
			return null;
		}
	}

	@Override
	public String getBoardName() {
		return remoteConnection.getAttribute(BOARD_ID);
	}

	@Override
	public String getPlatformName() {
		return remoteConnection.getAttribute(PLATFORM_ID);
	}

	@Override
	public String getPackageName() {
		return remoteConnection.getAttribute(PACKAGE_ID);
	}

	@Override
	public String getPortName() {
		return remoteConnection.getAttribute(PORT_NAME);
	}

	@Override
	public IRemoteProcess getCommandShell(int flags) throws IOException {
		if (serialPort != null && serialPort.isOpen()) {
			// can only have one open at a time
			return null;
		}

		serialPort = new SerialPort(getPortName());
		commandShell = new SerialPortCommandShell(remoteConnection, serialPort);
		return commandShell;
	}

	@Override
	public void pause() {
		if (serialPort != null) {
			try {
				if (serialPort.isOpen())
					serialPort.pause();
			} catch (IOException e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public void resume() {
		if (serialPort != null) {
			try {
				if (serialPort.isOpen())
					serialPort.resume();
			} catch (IOException e) {
				Activator.log(e);
			}
		}
	}

}
