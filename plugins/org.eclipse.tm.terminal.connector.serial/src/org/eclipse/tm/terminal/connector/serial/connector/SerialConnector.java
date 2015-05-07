/*******************************************************************************
 * Copyright (c) 2003, 2015 Wind River Systems, Inc. and others.
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
 * Martin Oberhuber (Wind River) - [206892] Don't connect if already connecting
 * Martin Oberhuber (Wind River) - [208029] COM port not released after quick disconnect/reconnect
 * Martin Oberhuber (Wind River) - [225853][api] Provide more default functionality in TerminalConnectorImpl
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.serial.connector;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.NullSettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;
import org.eclipse.tm.terminal.connector.serial.activator.UIPlugin;
import org.eclipse.tm.terminal.connector.serial.nls.Messages;

public class SerialConnector extends TerminalConnectorImpl {
	private OutputStream fOutputStream;
	private InputStream fInputStream;
	private SerialPort fSerialPort;
    private CommPortIdentifier fSerialPortIdentifier;
	private SerialPortHandler fTerminalSerialPortHandler;
	private SerialSettings fSettings;
	private SerialConnectWorker fConnectWorker = null;
	/* default */ volatile boolean fDisconnectGoingOn = false;

	public SerialConnector() {
	}
	public SerialConnector(SerialSettings settings) {
		fSettings=settings;
	}
	@Override
    public void initialize() throws Exception {
		try {
			fSettings=new SerialSettings();
		} catch (NoClassDefFoundError e) {
			// tell the user how to install the library
			throw new CoreException(new Status(IStatus.WARNING,UIPlugin.getUniqueIdentifier(),0, Messages.SerialConnector_Error_LiberayNotInstalled,e));
		}
	}
	@Override
    public void connect(ITerminalControl control) {
		super.connect(control);
		synchronized(this) {
			if (fConnectWorker!=null || fDisconnectGoingOn) {
				//avoid multiple background connect/disconnect threads at the same time
				return;
			}
			fConnectWorker = new SerialConnectWorker(this, control);
		}
		fControl.setState(TerminalState.CONNECTING);
		fConnectWorker.start();
	}
	/**
	 * Indicate that the connectWorker is finished.
	 */
	void doneConnect() {
		synchronized(this) {
			fConnectWorker = null;
		}
	}
	@Override
    public void doDisconnect() {
		synchronized(this) {
			//avoid multiple background connect/disconnect threads at the same time
			if (fConnectWorker!=null) {
				fConnectWorker.interrupt();
				return;
			} else if (fDisconnectGoingOn) {
				return;
			}
			fDisconnectGoingOn = true;
		}

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
			@Override
            public void run() {
				try {
					if (getSerialPortIdentifier() != null) {
						try {
							getSerialPortIdentifier()
								.removePortOwnershipListener(getSerialPortHandler());
						} catch(Exception e) {
							Logger.logException(e);
						}
					}

					if (getSerialPort() != null) {
						//Event listener is removed as part of close(),
						//but exceptions need to be caught to ensure that close() really succeeds
						try {
							getSerialPort().removeEventListener();
							Thread.sleep(50); //allow a little time for RXTX Native to catch up - makes stuff more stable
						} catch(Exception e) {
							Logger.logException(e);
						}
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

					if (getTerminalToRemoteStream() != null) {
						try {
							getTerminalToRemoteStream().close();
						} catch (Exception exception) {
							Logger.logException(exception);
						}
					}

					setSerialPortIdentifier(null);
					cleanSerialPort();
					setSerialPortHandler(null);
				} catch(Exception e) {
					Logger.logException(e);
				} finally {
					fDisconnectGoingOn = false;
				}
			}

		}.start();
	}
	public InputStream getInputStream() {
		return fInputStream;
	}
	@Override
    public OutputStream getTerminalToRemoteStream() {
		return fOutputStream;
	}
	private void setInputStream(InputStream inputStream) {
		fInputStream = inputStream;
	}
	private void setOutputStream(OutputStream outputStream) {
		fOutputStream = outputStream;
	}
	@Override
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
		//System.out.println("setSerialPortId: "+Thread.currentThread().getName()+ " - "+serialPortIdentifier + " - "+System.currentTimeMillis());
		fSerialPortIdentifier = serialPortIdentifier;
	}
	void setSerialPortHandler(SerialPortHandler serialPortHandler) {
		fTerminalSerialPortHandler=serialPortHandler;
	}
	SerialPortHandler getSerialPortHandler() {
		return fTerminalSerialPortHandler;
	}
	/**
	 * Return the Serial Settings.
	 *
	 * @return the settings for a concrete connection.
	 */
	public ISerialSettings getSerialSettings() {
		return fSettings;
	}
	@Override
	public void setDefaultSettings() {
	    fSettings.load(new NullSettingsStore());
	}
	@Override
    public String getSettingsSummary() {
		return fSettings.getSummary();
	}
	@Override
    public void load(ISettingsStore store) {
		fSettings.load(store);
	}
	@Override
    public void save(ISettingsStore store) {
		fSettings.save(store);
	}
}
