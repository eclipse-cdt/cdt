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
package org.eclipse.tm.internal.terminal.serial;

import gnu.io.CommPortOwnershipListener;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;

/**
 * UNDER CONSTRUCTION
 */
public class SerialPortHandler implements
		SerialPortEventListener, CommPortOwnershipListener {

	private final ITerminalControl fControl;
	private final SerialConnector fConn;
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
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				String[] args = new String[] { fConn.getSerialSettings().getSerialPort() };
				String strMsg = MessageFormat.format(SerialMessages.PORT_IN_USE, args);

				if (!MessageDialog.openQuestion(fControl.getShell(), SerialMessages.PROP_TITLE, strMsg))
					return;
				fConn.disconnect();
			}
			
		});
	}

	// SerialPortEventListener interface
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
	public void ownershipChange(int nType) {
		switch (nType) {
		case CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED:
			onSerialOwnershipRequested(null);
			break;
		}
	}
}