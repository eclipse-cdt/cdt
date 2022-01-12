/*******************************************************************************
 * Copyright (c) 2005, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - extracted from TerminalControl
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [225853][api] Provide more default functionality in TerminalConnectorImpl
 * Sean Adams (Cisco) - [231959][terminal][telnet] NPE in TelnetConnector.java
 * David Sciamma (Anyware-Tech)  - [288254][telnet] local echo is always disabled
 * Anton Leherbauer (Wind River) - [453393] Add support for copying wrapped lines without line break
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.connector;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.NullSettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

public class TelnetConnector extends TerminalConnectorImpl {

	static final class TelnetOutputStream extends FilterOutputStream {
		final static byte CR = 13;
		final static byte LF = 10;
		final static byte NUL = 0;
		final static byte[] CRNUL = { CR, NUL };
		final static byte[] CRLF = { CR, LF };
		final byte[] EOL;

		public TelnetOutputStream(OutputStream outputStream, String endOfLine) {
			super(outputStream);
			if (ITelnetSettings.EOL_CRLF.equals(endOfLine))
				EOL = CRLF;
			else
				EOL = CRNUL;
		}

		@Override
		public void write(int b) throws IOException {
			if (b == CR)
				out.write(EOL);
			else
				out.write(b);
		}
	}

	private OutputStream fOutputStream;
	private InputStream fInputStream;
	private Socket fSocket;
	private TelnetConnection fTelnetConnection;
	private final TelnetSettings fSettings;
	private int fWidth = -1;
	private int fHeight = -1;

	public TelnetConnector() {
		this(new TelnetSettings());
	}

	public TelnetConnector(TelnetSettings settings) {
		fSettings = settings;
	}

	@Override
	public void connect(ITerminalControl control) {
		super.connect(control);
		fWidth = -1;
		fHeight = -1;
		// TERM=xterm implies VT100 line wrapping mode
		control.setVT100LineWrapping(true);
		TelnetConnectWorker worker = new TelnetConnectWorker(this, control);
		worker.start();
	}

	@Override
	public void doDisconnect() {
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

		if (getTerminalToRemoteStream() != null) {
			try {
				getTerminalToRemoteStream().close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}
		cleanSocket();
	}

	@Override
	public boolean isLocalEcho() {
		if (fTelnetConnection == null)
			return false;
		return fTelnetConnection.localEcho();
	}

	@Override
	public void setTerminalSize(int newWidth, int newHeight) {
		if (fTelnetConnection != null && (newWidth != fWidth || newHeight != fHeight)) {
			//avoid excessive communications due to change size requests by caching previous size
			fTelnetConnection.setTerminalSize(newWidth, newHeight);
			fWidth = newWidth;
			fHeight = newHeight;
		}
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
		if (outputStream == null) {
			fOutputStream = null;
			return;
		}
		// translate CR to telnet end-of-line sequence - RFC 854
		fOutputStream = new TelnetOutputStream(outputStream, fSettings.getEndOfLine());
	}

	Socket getSocket() {
		return fSocket;
	}

	/**
	 * sets the socket to null
	 */
	void cleanSocket() {
		fSocket = null;
		setInputStream(null);
		setOutputStream(null);
	}

	void setSocket(Socket socket) throws IOException {
		if (socket == null) {
			cleanSocket();
		} else {
			fSocket = socket;
			setInputStream(socket.getInputStream());
			setOutputStream(socket.getOutputStream());
		}

	}

	public void setTelnetConnection(TelnetConnection connection) {
		fTelnetConnection = connection;
	}

	public void displayTextInTerminal(String text) {
		fControl.displayTextInTerminal(text);
	}

	public OutputStream getRemoteToTerminalOutputStream() {
		return fControl.getRemoteToTerminalOutputStream();
	}

	public void setState(TerminalState state) {
		fControl.setState(state);
	}

	public ITelnetSettings getTelnetSettings() {
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
