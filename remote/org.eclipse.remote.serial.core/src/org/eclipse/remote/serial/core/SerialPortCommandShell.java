/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - initial contribution
 *******************************************************************************/
package org.eclipse.remote.serial.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.serial.internal.core.Activator;

public class SerialPortCommandShell implements IRemoteProcess {

	private final IRemoteConnection remoteConnection;
	private final SerialPort serialPort;

	public SerialPortCommandShell(IRemoteConnection remoteConnection, SerialPort serialPort) throws IOException {
		this.remoteConnection = remoteConnection;
		this.serialPort = serialPort;
		serialPort.open();
	}
	
	
	@Override
	public synchronized void destroy() {
		if (serialPort.isOpen()) {
			try {
				serialPort.close();
			} catch (IOException e) {
				Activator.log(e);
			}
			notifyAll();
		}
	}

	@Override
	public int exitValue() {
		return 0;
	}

	@Override
	public InputStream getErrorStream() {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				synchronized (SerialPortCommandShell.this) {
					if (serialPort.isOpen()) {
						try {
							wait();
						} catch (InterruptedException e) {
						}
					}
				}
				return 0;
			}
		};
	}

	@Override
	public InputStream getInputStream() {
		return serialPort.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() {
		return serialPort.getOutputStream();
	}

	@Override
	public synchronized int waitFor() throws InterruptedException {
		if (serialPort.isOpen()) {
			wait();
		}
		return 0;
	}

	@Override
	public boolean isCompleted() {
		return false;
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	@Override
	public <T extends Service> T getService(Class<T> service) {
		return null;
	}

	@Override
	public <T extends Service> boolean hasService(Class<T> service) {
		return false;
	}

	@Override
	public IRemoteProcessBuilder getProcessBuilder() {
		return null;
	}

}
