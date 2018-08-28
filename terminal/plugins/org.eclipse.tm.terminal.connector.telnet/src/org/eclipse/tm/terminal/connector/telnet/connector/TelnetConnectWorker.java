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
 * Uwe Stieber (Wind River) - [287158][terminal][telnet] Connect worker is giving up to early
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.connector;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

class TelnetConnectWorker extends Thread {
	private final ITerminalControl fControl;
	private final TelnetConnector fConn;
	protected TelnetConnectWorker(TelnetConnector conn,ITerminalControl control) {
		fControl = control;
		fConn = conn;
		fControl.setState(TerminalState.CONNECTING);
	}
	@Override
    public void run() {
		// Retry the connect with after a little pause in case the
		// remote telnet server isn't ready. ConnectExceptions might
		// happen if the telnet server process did not initialized itself.
		// This is seen especially if the telnet server is a process
		// providing it's input and output via a built in telnet server.
		int remaining = 10;

		while (remaining >= 0) {
			// Pause before we re-try if the remaining tries are less than the initial value
			if (remaining < 10) try { Thread.sleep(500); } catch (InterruptedException e) { /* ignored on purpose */ }

			try {
				int nTimeout = fConn.getTelnetSettings().getTimeout() * 1000;
				String strHost = fConn.getTelnetSettings().getHost();
				int nPort = fConn.getTelnetSettings().getNetworkPort();
				InetSocketAddress address = new InetSocketAddress(strHost, nPort);
				Socket socket=new Socket();

				socket.connect(address, nTimeout);

				// If we get to this point, the connect succeeded and we will
				// force the remaining counter to be 0.
				remaining = 0;

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
				// No re-try in case of UnknownHostException, there is no indication that
				// the DNS will fix itself
				remaining = 0;
				//Construct error message and signal failed
				String txt="Unknown host: " + ex.getMessage(); //$NON-NLS-1$
				connectFailed(txt,"Unknown host: " + ex.getMessage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (SocketTimeoutException socketTimeoutException) {
				// Time out occurred. No re-try in this case either. Time out can
				// be increased by the user. Multiplying the timeout with the remaining
				// counter is not desired.
				remaining = 0;
				// Construct error message and signal failed
				connectFailed(socketTimeoutException.getMessage(), "Connection Error!\n" + socketTimeoutException.getMessage()); //$NON-NLS-1$
			} catch (ConnectException connectException) {
				// In case of a ConnectException, do a re-try. The server could have been
				// simply not ready yet and the worker would give up to early. If the terminal
				// control is already closed (disconnected), don't print "Connection refused" errors
				if (remaining == 0 && TerminalState.CLOSED != fControl.getState()) {
					connectFailed(connectException.getMessage(),"Connection refused!"); //$NON-NLS-1$
				}
			} catch (Exception exception) {
				// Any other exception on connect. No re-try in this case either
				remaining = 0;
				// Log the exception
				Logger.logException(exception);
				// And signal failed
				connectFailed(exception.getMessage(),""); //$NON-NLS-1$
			} finally {
				remaining--;
			}
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
