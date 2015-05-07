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
 * Martin Oberhuber (Wind River) - [168197] Replace JFace MessagDialog by SWT MessageBox
 * Martin Oberhuber (Wind River) - [221184] Redesign Serial Terminal Ownership Handling
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.serial.connector;

import gnu.io.CommPortOwnershipListener;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.terminal.connector.serial.nls.Messages;

/**
 * UNDER CONSTRUCTION
 */
public class SerialPortHandler implements SerialPortEventListener, CommPortOwnershipListener {

    /* default */ final ITerminalControl fControl;
	/* default */ final SerialConnector fConn;
	protected byte[] bytes = new byte[2048];

	/**
	 * UNDER CONSTRUCTION
	 * @param control TODO
	 */
	public SerialPortHandler(SerialConnector conn,ITerminalControl control) {
		super();
		fControl = control;
		fConn=conn;
	}

	// Message handlers

	/**
	 * UNDER CONSTRUCTION
	 */
	public void onSerialDataAvailable(Object data) {
		try {
			while (fConn.getInputStream() != null && fConn.getInputStream().available() > 0) {
				int nBytes = fConn.getInputStream().read(bytes);
				fControl.getRemoteToTerminalOutputStream().write(bytes, 0, nBytes);
			}
		} catch (IOException ex) {
			fControl.displayTextInTerminal(ex.getMessage());
		} catch (Exception exception) {
			Logger.logException(exception);
		}
	}

	public void onSerialOwnershipRequested(Object data) {
		//Bug 221184: We immediately release the port on any ownership request
		try {
			throw new Exception();
		} catch (Exception e) {
			StackTraceElement[] elems = e.getStackTrace();
			final String requester = elems[elems.length - 4].getClassName();
			Display.getDefault().asyncExec(new Runnable() {
				@Override
                public void run() {
					fConn.disconnect();
					String req = requester;
					String myPackage = this.getClass().getPackage().getName();
					if (req.startsWith(myPackage)) {
						req = Messages.SerialConnectWorker_ANOTHER_TERMINAL;
					}
					fControl.displayTextInTerminal(NLS.bind(Messages.SerialConnectWorker_OWNERSHIP_GRANTED, req));
				}
			});
			fConn.disconnect();
		}
	}

	// SerialPortEventListener interface
	@Override
    public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.DATA_AVAILABLE:
			onSerialDataAvailable(null);
			break;
		}
	}

	// CommPortOwnershipListener interface

	/**
	 * UNDER CONSTRUCTION
	 */
	@Override
    public void ownershipChange(int nType) {
		switch (nType) {
		case CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED:
			onSerialOwnershipRequested(null);
			break;
		}
	}
}