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
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import java.util.Arrays;
import java.util.Enumeration;

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
	
	/**
	 * Adds the named port to the name of known ports to rxtx
	 * @param name
	 */
	void addPort(String name) {
		// Rxtx either takes the connection from the properties OR using
		// the port scan.
		// Unfortunately, setting gnu.io.rxtx.SerialPorts only temporarily does not
		// work, because rxtx closes connections that are unknown.
		// The only solution I could come up with: add the known connections
		// to the gnu.io.rxtx.SerialPorts property....
		final String GNU_IO_RXTX_SERIAL_PORTS = "gnu.io.rxtx.SerialPorts"; //$NON-NLS-1$
		String sep = System.getProperty("path.separator", ":"); //$NON-NLS-1$//$NON-NLS-2$
		// get the existing names
		String names = System.getProperty(GNU_IO_RXTX_SERIAL_PORTS);
		if (names == null) {
			StringBuffer buffer=new StringBuffer();
			boolean sepNeeded=false;
			// When we add a port to this property, rxtx forgets the
			// ports it finds by scanning the system.
			
			// iterate over the known ports and add them to the property
			Enumeration portIdEnum= CommPortIdentifier.getPortIdentifiers();
			while (portIdEnum.hasMoreElements()) {
				CommPortIdentifier identifier = (CommPortIdentifier) portIdEnum.nextElement();
				if (identifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					if(sepNeeded)
						buffer.append(sep);
					else
						sepNeeded=true;
					buffer.append(identifier.getName());
				}
			}
			// append our new port
			if(sepNeeded)
				buffer.append(sep);
			buffer.append(name);

			System.setProperty(GNU_IO_RXTX_SERIAL_PORTS,buffer.toString());
		} else if (!Arrays.asList(names.split(sep)).contains(name)) {
			// the list does not contain the name, therefore we add it
			// since there is at least one name in the list, we append it
			System.setProperty(GNU_IO_RXTX_SERIAL_PORTS, names + sep + name);
		} else {
			// nothing to do -- should never happen...
			return;
		}
		// Reinitialise the ports because we have changed the list of known ports
		CommPortIdentifier.getPortIdentifiers();
	}
	
	public void run() {
		String portName=null;
		try {
			fControl.setState(TerminalState.OPENED);
			String strID = getClass().getPackage().getName();
			ISerialSettings s=fConn.getSerialSettings();
			portName=s.getSerialPort();
			try {
				fConn.setSerialPortIdentifier(CommPortIdentifier.getPortIdentifier(portName));
			} catch (NoSuchPortException e) {
				// let's try 
				addPort(portName);
				fConn.setSerialPortIdentifier(CommPortIdentifier.getPortIdentifier(portName));
			}
			fConn.setSerialPortHandler(new SerialPortHandler(fConn,fControl));
			fConn.setSerialPortIdentifier(CommPortIdentifier.getPortIdentifier(portName));
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
			fControl.displayTextInTerminal("Connection Error!\n" + portInUseException.getMessage()); //$NON-NLS-1$
		} catch (NoSuchPortException e) {
			fControl.setState(TerminalState.CLOSED);
			String msg=e.getMessage();
			if(msg==null)
				msg=portName;
			fControl.displayTextInTerminal("No such port: \"" + msg+"\"\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
			
		} catch (Exception exception) {
			fControl.setState(TerminalState.CLOSED);
		}
	}
}