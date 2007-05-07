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
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.telnet;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.eclipse.tm.terminal.ITerminalControl;
import org.eclipse.tm.terminal.Logger;
import org.eclipse.tm.terminal.TerminalState;

class TelnetConnectWorker extends Thread {
	private final ITerminalControl fControl;
	private final TelnetConnector fConn;
	protected TelnetConnectWorker(TelnetConnector conn,ITerminalControl control) {
		fControl = control;
		fConn = conn;
		fControl.setState(TerminalState.CONNECTING);
	}
	public void run() {
		try {
			int nTimeout = fConn.getTelnetSettings().getTimeout() * 1000;
			String strHost = fConn.getTelnetSettings().getHost();
			int nPort = fConn.getTelnetSettings().getNetworkPort();
			InetSocketAddress address = new InetSocketAddress(strHost, nPort);
			Socket socket=new Socket();

			socket.connect(address, nTimeout);

			// This next call causes reads on the socket to see TCP urgent data
			// inline with the rest of the non-urgent data.  Without this call, TCP
			// urgent data is silently dropped by Java.  This is required for
			// TELNET support, because when the TELNET server sends "IAC DM", the
			// IAC byte is TCP urgent data.  If urgent data is silently dropped, we
			// only see the DM, which looks like an ISO Latin-1 '�' character.

			socket.setOOBInline(true);
			
			fConn.setSocket(socket);

			TelnetConnection connection=new TelnetConnection(fConn, socket);
			socket.setKeepAlive(true);
			fConn.setTelnetConnection(connection);
			connection.start();
			fControl.setState(TerminalState.CONNECTED);

		} catch (UnknownHostException ex) {
			String txt="Unknown host: " + ex.getMessage(); //$NON-NLS-1$
			connectFailed(txt,"Unknown host: " + ex.getMessage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (SocketTimeoutException socketTimeoutException) {
			connectFailed(socketTimeoutException.getMessage(), "Connection Error!\n" + socketTimeoutException.getMessage()); //$NON-NLS-1$
		} catch (ConnectException connectException) {
			connectFailed(connectException.getMessage(),"Connection refused!"); //$NON-NLS-1$
		} catch (Exception exception) {
			Logger.logException(exception);

			connectFailed(exception.getMessage(),""); //$NON-NLS-1$
		}
	}

	private void connectFailed(String terminalText, String msg) {
		Logger.log(terminalText);
		fControl.displayTextInTerminal(terminalText);
		fConn.cleanSocket();
		fControl.setState(TerminalState.CLOSED);
		fControl.setMsg(msg);
	}
}
