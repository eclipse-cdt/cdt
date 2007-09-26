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

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public class SerialConnectWorker extends Thread {
	private final ITerminalControl fControl;
	private final SerialConnector fConn;

	/**
	 * UNDER CONSTRUCTION
	 * @param conn TODO
	 * @param control TODO
	 */
	public SerialConnectWorker(SerialConnector conn, ITerminalControl control) {
		super();
		fControl = control;
		fConn = conn;
		fControl.setState(TerminalState.CONNECTING);
	}
	public void run() {

		try {
			fControl.setState(TerminalState.OPENED);
			String strID = getClass().getPackage().getName();
			ISerialSettings s=fConn.getSerialSettings();

			fConn.setSerialPortHandler(new SerialPortHandler(fConn,fControl));
			fConn.setSerialPortIdentifier(CommPortIdentifier.getPortIdentifier(s.getSerialPort()));
			int timeoutInMs = s.getTimeout() * 1000;

			SerialPort serialPort=(SerialPort) fConn.getSerialPortIdentifier().open(strID,timeoutInMs);
			serialPort.setSerialPortParams(s.getBaudRate(), s.getDataBits(), s.getStopBits(), s.getParity());
			serialPort.setFlowControlMode(s.getFlowControl());
			serialPort.addEventListener(fConn.getSerialPortHandler());
			serialPort.notifyOnDataAvailable(true);
			fConn.getSerialPortIdentifier().addPortOwnershipListener(fConn.getSerialPortHandler());
			fConn.setSerialPort(serialPort);
			fControl.setState(TerminalState.CONNECTED);
		} catch (PortInUseException portInUseException) {
			fControl.setState(TerminalState.CLOSED);
			fControl.setMsg("Connection Error!\n" + portInUseException.getMessage()); //$NON-NLS-1$

		} catch (Exception exception) {
			fControl.setState(TerminalState.CLOSED);
		}
	}
}