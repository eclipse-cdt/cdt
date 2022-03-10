/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - initial contribution
 *******************************************************************************/
package org.eclipse.remote.serial.core;

import java.io.IOException;

import org.eclipse.cdt.serial.BaudRate;
import org.eclipse.cdt.serial.ByteSize;
import org.eclipse.cdt.serial.Parity;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.cdt.serial.StopBits;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnection.Service;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.serial.internal.core.Activator;
import org.eclipse.remote.serial.internal.core.Messages;

public class SerialPortConnection implements ISerialPortService, IRemoteCommandShellService {

	private final IRemoteConnection remoteConnection;
	private SerialPort serialPort;

	private SerialPortConnection(IRemoteConnection remoteConnection) {
		this.remoteConnection = remoteConnection;
		this.remoteConnection.addConnectionChangeListener(new IRemoteConnectionChangeListener() {

			@Override
			public void connectionChanged(RemoteConnectionChangeEvent event) {
				if (event.getType() == RemoteConnectionChangeEvent.ATTRIBUTES_CHANGED) {
					serialPort = null;
				}
			}
		});
	}

	public static class Factory implements IRemoteConnection.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service) {
			if (ISerialPortService.class.equals(service)) {
				return (T) new SerialPortConnection(remoteConnection);
			} else if (IRemoteCommandShellService.class.equals(service)) {
				return (T) getService(remoteConnection, ISerialPortService.class);
			}
			return null;
		}
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	@Override
	public SerialPort getSerialPort() {
		if (serialPort == null) {
			String portName = remoteConnection.getAttribute(PORT_NAME_ATTR);
			if (portName != null) {
				serialPort = new SerialPort(portName);
				try {
					serialPort.setBaudRate(
							BaudRate.fromStringIndex(Integer.parseInt(remoteConnection.getAttribute(BAUD_RATE_ATTR))));
					serialPort.setByteSize(
							ByteSize.fromStringIndex(Integer.parseInt(remoteConnection.getAttribute(BYTE_SIZE_ATTR))));
					serialPort.setParity(
							Parity.fromStringIndex(Integer.parseInt(remoteConnection.getAttribute(PARITY_ATTR))));
					serialPort.setStopBits(
							StopBits.fromStringIndex(Integer.parseInt(remoteConnection.getAttribute(STOP_BITS_ATTR))));
				} catch (IOException e) {
					Activator.log(e);
				}
			}
		}
		return serialPort;
	}

	@Override
	public IRemoteProcess getCommandShell(int flags) throws IOException {
		SerialPort serialPort = getSerialPort();
		if (serialPort == null) {
			throw new IOException(Messages.SerialPortConnection_SerialPortNotAvailable);
		}
		return new SerialPortCommandShell(remoteConnection, getSerialPort());
	}

}
