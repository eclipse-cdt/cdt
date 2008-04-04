/*******************************************************************************
 * Copyright (c) 2005, 2007 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorImpl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public class TelnetConnector extends TerminalConnectorImpl {
	private OutputStream fOutputStream;
	private InputStream fInputStream;
	private Socket fSocket;
	private ITerminalControl fControl;
	private TelnetConnection fTelnetConnection;
	private final TelnetSettings fSettings;
	private int fWidth = -1;
	private int fHeight = -1;

	public TelnetConnector() {
		this(new TelnetSettings());
	}
	public TelnetConnector(TelnetSettings settings) {
		fSettings=settings;
	}
	public void initialize() throws Exception {
	}
	public void connect(ITerminalControl control) {
		Logger.log("entered."); //$NON-NLS-1$
		fControl=control;
		fWidth=-1;
		fHeight=-1;
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
		if(fTelnetConnection!=null && (newWidth!=fWidth || newHeight!=fHeight)) {
			//avoid excessive communications due to change size requests by caching previous size
			fTelnetConnection.setTerminalSize(newWidth, newHeight);
			fWidth=newWidth;
			fHeight=newHeight;
		}
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
	public void displayTextInTerminal(String text) {
		fControl.displayTextInTerminal(text);
	}
	public OutputStream getRemoteToTerminalOutputStream () {
		return fControl.getRemoteToTerminalOutputStream();
		
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
