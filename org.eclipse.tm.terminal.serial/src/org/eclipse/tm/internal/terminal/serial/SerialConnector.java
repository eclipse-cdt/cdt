/*******************************************************************************
 * Copyright (c) 2003, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - extracted from TerminalControl 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [206892] Dont connect if already connecting
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public class SerialConnector implements ITerminalConnector {
	private OutputStream fOutputStream;
	private InputStream fInputStream;
	private ITerminalControl fControl;
	private SerialPort fSerialPort;
    private CommPortIdentifier fSerialPortIdentifier;
	private SerialPortHandler fTerminalSerialPortHandler;
	private SerialSettings fSettings;
	private SerialConnectWorker fConnectWorker;
	public SerialConnector() {
	}
	public SerialConnector(SerialSettings settings) {
		fSettings=settings;
	}
	public void initialize() throws Exception {
		try {
			fSettings=new SerialSettings();			
		} catch (NoClassDefFoundError e) {
			// tell the user how to install the library
			throw new CoreException(new Status(IStatus.WARNING,Activator.PLUGIN_ID,0, SerialMessages.ERROR_LIBRARY_NOT_INSTALLED,e));
		}
	}
	public void connect(ITerminalControl control) {
		Logger.log("entered."); //$NON-NLS-1$
		if (fConnectWorker!=null && fConnectWorker.isAlive()) {
			//already connecting
			return;
		}
		fConnectWorker = new SerialConnectWorker(this, control);
		fControl=control;
		fConnectWorker.start();
	}
	public void disconnect() {
		Logger.log("entered."); //$NON-NLS-1$
	
		// Fix for SPR 112422.  When output is being received from the serial port, the
		// below call to removePortOwnershipListener() attempts to lock the serial port
		// object, but that object is already locked by another Terminal view thread
		// waiting for the SWT display thread to process a syncExec() call.  Since this
		// method is called on the display thread, the display thread is waiting to
		// lock the serial port object and the thread holding the serial port object
		// lock is waiting for the display thread to process a syncExec() call, so the
		// two threads end up deadlocked, which hangs the Workbench GUI.
		//
		// The solution is to spawn a short-lived worker thread that calls
		// removePortOwnershipListener(), thus preventing the display thread from
		// deadlocking with the other Terminal view thread.
	
		new Thread("Terminal View Serial Port Disconnect Worker") //$NON-NLS-1$
		{
			public void run() {
	
				if (getSerialPortIdentifier() != null) {
					getSerialPortIdentifier()
							.removePortOwnershipListener(getSerialPortHandler());
				}
	
				if (getSerialPort() != null) {
					getSerialPort().removeEventListener();
					Logger.log("Calling close() on serial port ..."); //$NON-NLS-1$
					getSerialPort().close();
				}
	
				if (getInputStream() != null) {
					try {
						getInputStream().close();
					} catch (Exception exception) {
						Logger.logException(exception);
					}
				}
	
				if (getOutputStream() != null) {
					try {
						getOutputStream().close();
					} catch (Exception exception) {
						Logger.logException(exception);
					}
				}
	
				setSerialPortIdentifier(null);
				cleanSerialPort();
				setSerialPortHandler(null);
			}

		}.start();
		fControl.setState(TerminalState.CLOSED);
	}
	public InputStream getInputStream() {
		return fInputStream;
	}
	public OutputStream getOutputStream() {
		return fOutputStream;
	}
	private void setInputStream(InputStream inputStream) {
		fInputStream = inputStream;
	}
	private void setOutputStream(OutputStream outputStream) {
		fOutputStream = outputStream;
	}
	public boolean isLocalEcho() {
		return false;
	}
	public void setTerminalSize(int newWidth, int newHeight) {
		// TODO
	}
	
	protected SerialPort getSerialPort() {
		return fSerialPort;
	}
	
	/**
	 * sets the socket to null
	 */
	void cleanSerialPort() {
		fSerialPort=null;
		setInputStream(null);
		setOutputStream(null);
	}
	
	protected void setSerialPort(SerialPort serialPort) throws IOException {
		cleanSerialPort();			
		if(serialPort!=null) {
			fSerialPort = serialPort;
			setOutputStream(serialPort.getOutputStream());
			setInputStream(serialPort.getInputStream());
		}
	}
	protected CommPortIdentifier getSerialPortIdentifier() {
		return fSerialPortIdentifier;
	}
	protected void setSerialPortIdentifier(CommPortIdentifier serialPortIdentifier) {
		fSerialPortIdentifier = serialPortIdentifier;
	}
	void setSerialPortHandler(SerialPortHandler serialPortHandler) {
		fTerminalSerialPortHandler=serialPortHandler;
	}
	SerialPortHandler getSerialPortHandler() {
		return fTerminalSerialPortHandler;
	}
	public ISerialSettings getSerialSettings() {
		return fSettings;
	}
	public ISettingsPage makeSettingsPage() {
		return new SerialSettingsPage(fSettings);
	}
	public String getSettingsSummary() {
		return fSettings.getSummary();
	}
	public void load(ISettingsStore store) {
		fSettings.load(store);
	}
	public void save(ISettingsStore store) {
		fSettings.save(store);
	}
}