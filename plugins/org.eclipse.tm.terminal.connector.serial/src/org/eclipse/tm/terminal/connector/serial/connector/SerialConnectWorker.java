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
 * Martin Oberhuber (Wind River) - [207158] improve error message when port not available
 * Martin Oberhuber (Wind River) - [208029] COM port not released after quick disconnect/reconnect
 * Martin Oberhuber (Wind River) - [206884] Update Terminal Ownership ID to "org.eclipse.tm.terminal.serial"
 * Martin Oberhuber (Wind River) - [221184] Redesign Serial Terminal Ownership Handling
 * Michael Scharf (Wind River) - [262996] get rid of TerminalState.OPENED
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.serial.connector;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import java.util.Arrays;
import java.util.Enumeration;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.connector.serial.nls.Messages;

@SuppressWarnings("restriction")
public class SerialConnectWorker extends Thread {
    /* default */ final ITerminalControl fControl;
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
			Enumeration<?> portIdEnum= CommPortIdentifier.getPortIdentifiers();
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
		// Reinitialize the ports because we have changed the list of known ports
		CommPortIdentifier.getPortIdentifiers();
	}

	/**
	 * Return the ID that this connector uses for RXTX Comm Ownership Handling.
	 *
	 * Note that this was changed in Terminal 2.0 as per
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=206884 - previous versions
	 * of the serial terminal connector used a different string,
	 * "org.eclipse.tm.internal.terminal.serial".
	 *
	 * @since org.eclipse.tm.terminal.serial 2.0
	 * @return ownership ID, "org.eclipse.tm.terminal.serial"
	 */
	public static final String getOwnershipId() {
		return "org.eclipse.tm.terminal.serial"; //$NON-NLS-1$
	}

	@Override
    public void run() {
		String portName=null;
		final String strID = getOwnershipId();
		SerialPort serialPort = null;
		try {
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

			//Bug 221184: Warn about serial port already in use
			String currentOwner = fConn.getSerialPortIdentifier().getCurrentOwner();
			if (strID.equals(currentOwner)) {
				currentOwner = Messages.SerialConnectWorker_ANOTHER_TERMINAL;
			}
			final int[] answer = { SWT.YES };
			final String fPortName = portName;
			final String fCurrentOwner = currentOwner;
			if (currentOwner != null) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
                    public void run() {
						MessageBox mb = new MessageBox(fControl.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
						mb.setText(Messages.SerialConnectWorker_PROP_TITLE);
						mb.setMessage(NLS.bind(Messages.SerialConnectWorker_PORT_IN_USE, fPortName, fCurrentOwner));
						answer[0] = mb.open();
					}
				});
			}

			if (answer[0] != SWT.YES) {
				// don't try to steal the port
				fControl.setState(TerminalState.CLOSED);
				fConn.setSerialPortHandler(null);
				return;
			}

			// Try to steal the port -- may throw PortInUseException
			int timeoutInMs = s.getTimeout() * 1000;
			serialPort = (SerialPort) fConn.getSerialPortIdentifier().open(strID, timeoutInMs);
			serialPort.setSerialPortParams(s.getBaudRate(), s.getDataBits(), s.getStopBits(), s.getParity());
			serialPort.setFlowControlMode(s.getFlowControl());
			serialPort.addEventListener(fConn.getSerialPortHandler());
			serialPort.notifyOnDataAvailable(true);
			fConn.getSerialPortIdentifier().addPortOwnershipListener(fConn.getSerialPortHandler());
			fConn.setSerialPort(serialPort);
			if (fCurrentOwner != null) {
				fControl.displayTextInTerminal(NLS.bind(Messages.SerialConnectWorker_PORT_STOLEN, fPortName, fCurrentOwner));

			}
			fControl.setState(TerminalState.CONNECTED);

		} catch (PortInUseException portInUseException) {
			fControl.setState(TerminalState.CLOSED);
			String theOwner = portInUseException.currentOwner;
			if (strID.equals(theOwner)) {
				theOwner = Messages.SerialConnectWorker_ANOTHER_TERMINAL;
			}
			fControl.displayTextInTerminal(NLS.bind(Messages.SerialConnectWorker_PORT_NOT_STOLEN, portName, theOwner));
		} catch (NoSuchPortException e) {
			fControl.setState(TerminalState.CLOSED);
			String msg=e.getMessage();
			if(msg==null)
				msg=portName;
			fControl.displayTextInTerminal(NLS.bind(Messages.SerialConnectWorker_NO_SUCH_PORT, msg));

		} catch (Exception exception) {
			Logger.logException(exception);
			if (serialPort!=null) {
				//Event listener is removed as part of close(),
				//but exceptions need to be caught to ensure that close() really succeeds
				try {
					serialPort.removeEventListener();
					Thread.sleep(50); //allow a little time for RXTX Native to catch up - makes stuff more stable
				} catch(Exception e) {
					Logger.logException(e);
				}
				serialPort.close();
				fConn.getSerialPortIdentifier().removePortOwnershipListener(fConn.getSerialPortHandler());
			}
			fControl.setState(TerminalState.CLOSED);
		} finally {
			fConn.doneConnect();
		}
	}
}