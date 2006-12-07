/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.internal.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.tm.terminal.ISettingsPage;
import org.eclipse.tm.terminal.ISettingsStore;
import org.eclipse.tm.terminal.ITerminalConnector;
import org.eclipse.tm.terminal.ITerminalControl;
import org.eclipse.tm.terminal.Logger;
import org.eclipse.tm.terminal.TerminalState;

public class TelnetConnector implements ITerminalConnector {
	private OutputStream fOutputStream;
	private InputStream fInputStream;
	private Socket fSocket;
	private ITerminalControl fControl;
	private TelnetConnection fTelnetConnection;
	private final TelnetSettings fSettings;
	public TelnetConnector() {
		this(new TelnetSettings());
	}
	public String getId() {
		return getClass().getName();
	}
	public TelnetConnector(TelnetSettings settings) {
		fSettings=settings;
	}
	public void connect(ITerminalControl control) {
		Logger.log("entered."); //$NON-NLS-1$
		fControl=control;
		TelnetConnectWorker worker = new TelnetConnectWorker(this,control);
		worker.start();
	}
	public void disconnect() {
		Logger.log("entered."); //$NON-NLS-1$
	
		if (getSocket() != null) {
			try {
				getSocket().close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
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
		cleanSocket();
		setState(TerminalState.CLOSED);
	}
	public boolean isLocalEcho() {
		if(fTelnetConnection!=null)
			return false;
		return fTelnetConnection.localEcho();
	}
	public void setTerminalSize(int newWidth, int newHeight) {
		if(fTelnetConnection!=null)
			fTelnetConnection.setTerminalSize(newWidth, newHeight);
		
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
	Socket getSocket() {
		return fSocket;
	}
	
	/**
	 * sets the socket to null
	 */
	void cleanSocket() {
		fSocket=null;
		setInputStream(null);
		setOutputStream(null);
	}
	
	void setSocket(Socket socket) throws IOException {
		if(socket==null) {
			cleanSocket();
		} else {
			fSocket = socket;
			setInputStream(socket.getInputStream());
			setOutputStream(socket.getOutputStream());
		}

	}
	public void setTelnetConnection(TelnetConnection connection) {
		fTelnetConnection=connection;		
	}
	public void writeToTerminal(String txt) {
		fControl.writeToTerminal(txt);
		
	}
	public void setState(TerminalState state) {
		fControl.setState(state);
		
	}
	public ITelnetSettings getTelnetSettings() {
		return fSettings;
	}
	public ISettingsPage makeSettingsPage() {
		return new TelnetSettingsPage(fSettings);
	}
	public String getStatusString(String strConnected) {
		return fSettings.getStatusString(strConnected);
	}
	public void load(ISettingsStore store) {
		fSettings.load(store);
		
	}
	public void save(ISettingsStore store) {
		fSettings.save(store);
	}
	public boolean isInstalled() {
		return true;
	}
}