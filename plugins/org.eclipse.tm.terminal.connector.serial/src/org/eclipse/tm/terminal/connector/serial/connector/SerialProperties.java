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
 * Michael Scharf (Wind River) - extracted from TerminalProperties 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.serial.connector;

import gnu.io.CommPortIdentifier;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class SerialProperties {
	protected List fSerialPortTable;
	protected List fBaudRateTable;
	protected List fDataBitsTable;
	protected List fStopBitsTable;
	protected List fParityTable;
	protected List fFlowControlTable;
	protected String fDefaultConnType;
	protected String fDefaultSerialPort;
	protected String fDefaultBaudRate;
	protected String fDefaultDataBits;
	protected String fDefaultStopBits;
	protected String fDefaultParity;
	protected String fDefaultFlowControl;

	public SerialProperties() {
		setupProperties();
	}
	public List getSerialPortTable() {
		return fSerialPortTable;
	}

	public List getBaudRateTable() {
		return fBaudRateTable;
	}

	public List getDataBitsTable() {
		return fDataBitsTable;
	}

	public List getStopBitsTable() {
		return fStopBitsTable;
	}

	public List getParityTable() {
		return fParityTable;
	}

	public List getFlowControlTable() {
		return fFlowControlTable;
	}
	public String getDefaultConnType() {
		return fDefaultConnType;
	}

	public String getDefaultSerialPort() {
		return fDefaultSerialPort;
	}

	public String getDefaultBaudRate() {
		return fDefaultBaudRate;
	}

	public String getDefaultDataBits() {
		return fDefaultDataBits;
	}

	public String getDefaultStopBits() {
		return fDefaultStopBits;
	}

	public String getDefaultParity() {
		return fDefaultParity;
	}

	public String getDefaultFlowControl() {
		return fDefaultFlowControl;
	}
	public String getDefaultTimeout() {
		return "5"; //$NON-NLS-1$
	}
	protected void setupProperties() {
		fSerialPortTable = new Vector();
		fBaudRateTable = new Vector();
		fDataBitsTable = new Vector();
		fStopBitsTable = new Vector();
		fParityTable = new Vector();
		fFlowControlTable = new Vector();
		fDefaultConnType = ""; //$NON-NLS-1$
		fDefaultSerialPort = ""; //$NON-NLS-1$
		fDefaultBaudRate = ""; //$NON-NLS-1$
		fDefaultDataBits = ""; //$NON-NLS-1$
		fDefaultStopBits = ""; //$NON-NLS-1$
		fDefaultParity = ""; //$NON-NLS-1$
		fDefaultFlowControl = ""; //$NON-NLS-1$

		fBaudRateTable.add("300"); //$NON-NLS-1$
		fBaudRateTable.add("1200"); //$NON-NLS-1$
		fBaudRateTable.add("2400"); //$NON-NLS-1$
		fBaudRateTable.add("4800"); //$NON-NLS-1$
		fBaudRateTable.add("9600"); //$NON-NLS-1$
		fBaudRateTable.add("19200"); //$NON-NLS-1$
		fBaudRateTable.add("38400"); //$NON-NLS-1$
		fBaudRateTable.add("57600"); //$NON-NLS-1$
		fBaudRateTable.add("115200"); //$NON-NLS-1$

		fDataBitsTable.add("5"); //$NON-NLS-1$
		fDataBitsTable.add("6"); //$NON-NLS-1$
		fDataBitsTable.add("7"); //$NON-NLS-1$
		fDataBitsTable.add("8"); //$NON-NLS-1$

		fStopBitsTable.add("1"); //$NON-NLS-1$
		fStopBitsTable.add("1_5"); //$NON-NLS-1$
		fStopBitsTable.add("2"); //$NON-NLS-1$

		fParityTable.add("None"); //$NON-NLS-1$
		fParityTable.add("Even"); //$NON-NLS-1$
		fParityTable.add("Odd"); //$NON-NLS-1$
		fParityTable.add("Mark"); //$NON-NLS-1$
		fParityTable.add("Space"); //$NON-NLS-1$

		fFlowControlTable.add("None"); //$NON-NLS-1$
		fFlowControlTable.add("RTS/CTS"); //$NON-NLS-1$
		fFlowControlTable.add("Xon/Xoff"); //$NON-NLS-1$

		fDefaultBaudRate = (String) fBaudRateTable.get(4);
		fDefaultDataBits = (String) fDataBitsTable.get(3);
		fDefaultStopBits = (String) fStopBitsTable.get(0);
		fDefaultParity = (String) fParityTable.get(0);
		fDefaultFlowControl = (String) fFlowControlTable.get(0);

		Enumeration portIdEnum= CommPortIdentifier.getPortIdentifiers();
		while (portIdEnum.hasMoreElements()) {
			CommPortIdentifier identifier = (CommPortIdentifier) portIdEnum.nextElement();
			String strName = identifier.getName();
			int nPortType = identifier.getPortType();

			if (nPortType == CommPortIdentifier.PORT_SERIAL)
				fSerialPortTable.add(strName);
		}

		Collections.sort(fSerialPortTable);

		if (!fSerialPortTable.isEmpty()) {
			fDefaultSerialPort = (String) fSerialPortTable.get(0);
		}
	}
}
