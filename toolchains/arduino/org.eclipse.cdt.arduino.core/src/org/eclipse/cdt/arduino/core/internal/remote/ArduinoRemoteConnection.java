/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPackage;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionPropertyService;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.serial.core.SerialPortCommandShell;

public class ArduinoRemoteConnection
		implements IRemoteConnectionPropertyService, IRemoteCommandShellService, IRemoteConnectionChangeListener {

	public static final String TYPE_ID = "org.eclipse.cdt.arduino.core.connectionType"; //$NON-NLS-1$

	private static final String PORT_NAME = "arduinoPortName"; //$NON-NLS-1$
	private static final String PACKAGE_NAME = "arduinoPackageName"; //$NON-NLS-1$
	private static final String PLATFORM_NAME = "arduinoPlatformName"; //$NON-NLS-1$
	private static final String BOARD_NAME = "arduinoBoardName"; //$NON-NLS-1$
	private static final String MENU_QUALIFIER = "menu_"; //$NON-NLS-1$
	private static final String PROGRAMMER = "programmer"; //$NON-NLS-1$

	private final IRemoteConnection remoteConnection;
	private SerialPort serialPort;
	private SerialPortCommandShell commandShell;
	private ArduinoBoard board;

	private static final Map<IRemoteConnection, ArduinoRemoteConnection> connectionMap = new HashMap<>();

	public ArduinoRemoteConnection(IRemoteConnection remoteConnection) {
		this.remoteConnection = remoteConnection;
		remoteConnection.addConnectionChangeListener(this);
	}

	public static void setBoardId(IRemoteConnectionWorkingCopy workingCopy, ArduinoBoard board) {
		workingCopy.setAttribute(BOARD_NAME, board.getId());

		ArduinoPlatform platform = board.getPlatform();
		workingCopy.setAttribute(PLATFORM_NAME, platform.getArchitecture());

		ArduinoPackage pkg = platform.getPackage();
		workingCopy.setAttribute(PACKAGE_NAME, pkg.getName());
	}

	public static void setPortName(IRemoteConnectionWorkingCopy workingCopy, String portName) {
		workingCopy.setAttribute(PORT_NAME, portName);
	}

	public static void setMenuValue(IRemoteConnectionWorkingCopy workingCopy, String key, String value) {
		workingCopy.setAttribute(MENU_QUALIFIER + key, value);
	}

	public static void setProgrammer(IRemoteConnectionWorkingCopy workingCopy, String programmer) {
		workingCopy.setAttribute(PROGRAMMER, programmer);
	}

	public String getMenuValue(String key) {
		return remoteConnection.getAttribute(MENU_QUALIFIER + key);
	}

	public String getProgrammer() {
		return remoteConnection.getAttribute(PROGRAMMER);
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
			if (ArduinoRemoteConnection.class.equals(service)) {
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
				return (T) remoteConnection.getService(ArduinoRemoteConnection.class);
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

	public ArduinoBoard getBoard() throws CoreException {
		if (board == null) {
			String pkgName = remoteConnection.getAttribute(PACKAGE_NAME);
			String platName = remoteConnection.getAttribute(PLATFORM_NAME);
			String boardName = remoteConnection.getAttribute(BOARD_NAME);
			ArduinoManager manager = Activator.getService(ArduinoManager.class);
			board = manager.getBoard(pkgName, platName, boardName);

			if (board == null) {
				// Old style board attributes?
				ArduinoPackage pkg = manager.getPackage(pkgName);
				if (pkg != null) {
					for (ArduinoPlatform plat : pkg.getAvailablePlatforms()) {
						if (plat.getName().equals(platName)) {
							platName = plat.getArchitecture();
							for (ArduinoBoard b : plat.getBoards()) {
								if (b.getName().equals(boardName)) {
									board = b;
									return board;
								}
							}
						}
					}
				}
			}
		}

		return board;
	}

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
